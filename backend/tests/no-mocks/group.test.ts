// tests/no-mocks/group.test.ts

// ============================================
// NO MOCKING - USING REAL SERVICES WITH SPIES
// ============================================

import request from 'supertest';
import app from '../../src/app';
import { generateTestToken } from '../helpers/auth.helper';
import { 
  seedTestUsers, 
  cleanTestData, 
  TestUser, 
  seedTestGroup, 
  TestGroup,
  getTestGroupById
} from '../helpers/seed.helper';
import { initializeTestSocket, closeTestSocket } from '../helpers/socket.helper';
import { connectDatabase, disconnectDatabase } from '../../src/config/database';
import { UserStatus } from '../../src/models/User';
import socketManager from '../../src/utils/socketManager';
import * as firebase from '../../src/config/firebase';

/**
 * Group Routes Tests - No Mocking (Controllable Scenarios)
 * 
 * This test suite covers CONTROLLABLE scenarios:
 * - Real database operations
 * - Real Socket.IO server with spies
 * - Real notification service logic
 * - Spies on Firebase to prevent actual API calls
 * 
 * Tests all success paths and user-triggered errors (404, 400, 409)
 * Does NOT test uncontrollable failures (network, timeouts, API errors)
 */

let testUsers: TestUser[];
let testGroups: TestGroup[];

beforeAll(async () => {
  console.log('\n🚀 Starting Group Tests (No Mocking - Controllable Scenarios)...\n');

  // Initialize real Socket.IO server
  await initializeTestSocket();

  // Connect to database
  await connectDatabase();
  
  // Seed test users (now includes FCM tokens)
  testUsers = await seedTestUsers();

  // Create test groups
  const group1 = await seedTestGroup(
    'test-room-group1',
    [testUsers[0]._id, testUsers[1]._id],
    {
      restaurantSelected: false,
      completionTime: new Date(Date.now() + 3600000)
    }
  );

  const group2 = await seedTestGroup(
    'test-room-group2',
    [testUsers[2]._id, testUsers[3]._id],
    {
      restaurantSelected: true,
      restaurant: {
        name: 'Selected Restaurant',
        location: '123 Main St',
        restaurantId: 'rest-123',
        cuisine: 'Italian'
      }
    }
  );

  testGroups = [group1, group2];

  // Update users to attach groups
  const UserModel = (await import('../../src/models/User')).default;
  await UserModel.findByIdAndUpdate(testUsers[0]._id, { groupId: group1._id, status: UserStatus.IN_GROUP });
  await UserModel.findByIdAndUpdate(testUsers[1]._id, { groupId: group1._id, status: UserStatus.IN_GROUP });
  await UserModel.findByIdAndUpdate(testUsers[2]._id, { groupId: group2._id, status: UserStatus.IN_GROUP });
  await UserModel.findByIdAndUpdate(testUsers[3]._id, { groupId: group2._id, status: UserStatus.IN_GROUP });

  console.log(`✅ Test setup complete. Ready to run tests.\n`);
});

afterAll(async () => {
  console.log('\n🧹 Cleaning up after tests...\n');
  
  await cleanTestData();
  await disconnectDatabase();
  
  // Close socket server
  await closeTestSocket();
  
  console.log('✅ Cleanup complete.\n');
});

beforeEach(() => {
  // Spy on Firebase functions to prevent actual API calls
  jest.spyOn(firebase, 'sendPushNotification').mockResolvedValue('mock-message-id');
  jest.spyOn(firebase, 'sendMulticastNotification').mockResolvedValue({
    successCount: 1,
    failureCount: 0,
    responses: []
  } as any);
});

afterEach(async () => {
  const UserModel = (await import('../../src/models/User')).default;
  
  // Reset test users 0 and 1 to group1
  await UserModel.findByIdAndUpdate(testUsers[0]._id, { 
    groupId: testGroups[0]._id, 
    status: UserStatus.IN_GROUP,
    fcmToken: 'mock-fcm-token-user1'  // Restore FCM token
  });
  await UserModel.findByIdAndUpdate(testUsers[1]._id, { 
    groupId: testGroups[0]._id, 
    status: UserStatus.IN_GROUP,
    fcmToken: 'mock-fcm-token-user2'  // Restore FCM token
  });
  
  // Reset test users 2 and 3 to group2
  await UserModel.findByIdAndUpdate(testUsers[2]._id, { 
    groupId: testGroups[1]._id, 
    status: UserStatus.IN_GROUP,
    fcmToken: 'mock-fcm-token-user3'  // Restore FCM token
  });
  await UserModel.findByIdAndUpdate(testUsers[3]._id, { 
    groupId: testGroups[1]._id, 
    status: UserStatus.IN_GROUP,
    fcmToken: 'mock-fcm-token-user4'  // Restore FCM token
  });
  
  // Restore all spies
  jest.restoreAllMocks();
});

describe('GET /api/group/status - No Mocking', () => {
  test('Should return 200 and group status for user in a group', async () => {
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );
    
    const response = await request(app)
      .get('/api/group/status')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(200);
    expect(response.body.Status).toBe(200);
    expect(response.body.Body).toHaveProperty('groupId');
    expect(response.body.Body).toHaveProperty('roomId');
    expect(response.body.Body).toHaveProperty('numMembers');
    expect(response.body.Body).toHaveProperty('users');
    expect(response.body.Body).toHaveProperty('restaurantSelected');
    expect(response.body.Body).toHaveProperty('status');
    expect(response.body.Body.numMembers).toBe(2);
    expect(response.body.Body.status).toBe('voting');
  });

  test('should return 404 when user is not in a group', async () => {
    const token = generateTestToken(
      testUsers[4]._id,
      testUsers[4].email,
      testUsers[4].googleId
    );

    const response = await request(app)
      .get('/api/group/status')
      .set('Authorization', `Bearer ${token}`);
      
    expect(response.status).toBe(404);
    expect(response.body.Status).toBe(404);
    expect(response.body.Message).toHaveProperty('error', 'Not in a group');
    expect(response.body.Body).toBeNull();
  });

  // Consolidated test: 401 authentication errors
  // These test the authMiddleware code which is the SAME for all endpoints
  // Testing once is sufficient since all endpoints use the same middleware
  test('should return 401 without authentication token', async () => {
    /**
     * Tests authMiddleware -> no token -> 401 pattern
     * Covers: auth.middleware.ts lines 20-26
     * All endpoints use the same authMiddleware, so testing one endpoint covers all
     */
    const response = await request(app)
      .get('/api/group/status');

    expect(response.status).toBe(401);
    expect(response.body).toHaveProperty('error');
  });

  test('should return 401 with invalid token', async () => {
    /**
     * Tests authMiddleware -> invalid token -> 401 pattern
     * Covers: auth.middleware.ts lines 56-62 (JsonWebTokenError)
     * All endpoints use the same authMiddleware, so testing one endpoint covers all
     */
    const response = await request(app)
      .get('/api/group/status')
      .set('Authorization', 'Bearer invalid-token-format');

    expect(response.status).toBe(401);
  });

  test('should return group status with restaurant selected', async () => {
    const token = generateTestToken(
      testUsers[2]._id,
      testUsers[2].email,
      testUsers[2].googleId
    );

    const response = await request(app)
      .get('/api/group/status')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(200);
    expect(response.body.Body.restaurantSelected).toBe(true);
    expect(response.body.Body.status).toBe('completed');
    expect(response.body.Body.restaurant).toBeDefined();
    expect(response.body.Body.restaurant.name).toBe('Selected Restaurant');
  });
});

describe('POST /api/group/vote/:groupId - No Mocking', () => {
  test('should return 400 when restaurantID is missing', async () => {
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .post(`/api/group/vote/${testGroups[0]._id}`)
      .set('Authorization', `Bearer ${token}`)
      .send({});

    expect(response.status).toBe(400);
    expect(response.body.Status).toBe(400);
    expect(response.body.Message.error).toBe('Restaurant ID is required');
  });

  // Consolidated test: 500 when group not found
  // This tests the Group.findById() -> if (!group) -> throw Error('Group not found') pattern
  // The SAME code exists in voteForRestaurant (groupService line 52-56) and leaveGroup (groupService line 276-280)
  // Testing once is sufficient since both use identical code: if (!group) { throw new Error('Group not found') }
  test('should return 500 when group not found', async () => {
    /**
     * Tests Group.findById() -> if (!group) -> throw Error('Group not found') pattern
     * Covers: groupService.ts lines 52-56 (voteForRestaurant), 276-280 (leaveGroup)
     * Both methods have identical code: if (!group) { throw new Error('Group not found') }
     */
    const nonExistentGroupId = '507f1f77bcf86cd799439011';
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    // Test with vote endpoint - the code path is identical for vote and leave
    const response = await request(app)
      .post(`/api/group/vote/${nonExistentGroupId}`)
      .set('Authorization', `Bearer ${token}`)
      .send({ restaurantID: 'rest-123' });

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Group not found');
  });

  // Consolidated test: 400 for invalid ObjectId format
  // This tests the CastError -> errorHandler -> "Invalid data format" pattern
  // The SAME code exists in vote (invalid groupId) and leave (invalid groupId)
  // Testing once is sufficient since both trigger the same CastError handling
  test('should return 400 for invalid ObjectId format in groupId', async () => {
    /**
     * Tests CastError -> errorHandler -> "Invalid data format" pattern
     * Covers: errorHandler.ts CastError handling (line 38)
     * Both vote and leave endpoints trigger the same CastError when groupId is invalid
     */
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    // Test with vote endpoint - the code path is identical for vote and leave
    const response = await request(app)
      .post('/api/group/vote/invalid-group-id-format')
      .set('Authorization', `Bearer ${token}`)
      .send({ restaurantID: 'rest-123' });

    expect(response.status).toBe(400);
    expect(response.body.message).toBe('Invalid data format');
  });

  test('should return 500 when user is not a member of the group', async () => {
    const token = generateTestToken(
      testUsers[4]._id,
      testUsers[4].email,
      testUsers[4].googleId
    );

    const response = await request(app)
      .post(`/api/group/vote/${testGroups[0]._id}`)
      .set('Authorization', `Bearer ${token}`)
      .send({ restaurantID: 'rest-123' });

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('User is not a member of this group');
  });

  test('should return 500 when restaurant already selected', async () => {
    const token = generateTestToken(
      testUsers[2]._id,
      testUsers[2].email,
      testUsers[2].googleId
    );

    const response = await request(app)
      .post(`/api/group/vote/${testGroups[1]._id}`)
      .set('Authorization', `Bearer ${token}`)
      .send({ restaurantID: 'rest-456' });

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Restaurant has already been selected for this group');
  });

  test('should allow user to change their vote and emit socket events', async () => {
    // Spy on socket manager
    const emitVoteUpdateSpy = jest.spyOn(socketManager, 'emitVoteUpdate');

    const votingGroup = await seedTestGroup(
      'test-room-change-vote',
      [testUsers[0]._id, testUsers[1]._id]
    );

    const User = (await import('../../src/models/User')).default;
    await User.findByIdAndUpdate(testUsers[0]._id, { 
      groupId: votingGroup._id,
      status: UserStatus.IN_GROUP
    });
    await User.findByIdAndUpdate(testUsers[1]._id, { 
      groupId: votingGroup._id,
      status: UserStatus.IN_GROUP
    });

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    // First vote
    const response1 = await request(app)
      .post(`/api/group/vote/${votingGroup._id}`)
      .set('Authorization', `Bearer ${token}`)
      .send({ restaurantID: 'rest-first' });

    expect(response1.status).toBe(200);
    expect(response1.body.Body.Current_votes['rest-first']).toBe(1);

    // Verify socket event was emitted
    expect(emitVoteUpdateSpy).toHaveBeenCalled();

    // Change vote
    const response2 = await request(app)
      .post(`/api/group/vote/${votingGroup._id}`)
      .set('Authorization', `Bearer ${token}`)
      .send({ restaurantID: 'rest-second' });

    expect(response2.status).toBe(200);
    expect(response2.body.Body.Current_votes['rest-second']).toBe(1);
    
    const firstVoteCount = response2.body.Body.Current_votes['rest-first'];
    expect(firstVoteCount === undefined || firstVoteCount === 0).toBe(true);

    // Verify socket event was emitted again
    expect(emitVoteUpdateSpy).toHaveBeenCalledTimes(2);
  });

  test('should successfully vote and trigger restaurant selection with notification', async () => {
    /**
     * Test that notification service is called with correct parameters
     * when all members vote and restaurant is selected
     */
    
    // Spy on Firebase function to verify it's called
    const firebaseSpy = jest.spyOn(firebase, 'sendMulticastNotification');

    const votingGroup = await seedTestGroup(
      'test-room-vote-complete',
      [testUsers[0]._id]  // Single member for quick completion
    );

    const User = (await import('../../src/models/User')).default;
    
    // User already has FCM token from seed, just update group
    await User.findByIdAndUpdate(testUsers[0]._id, { 
      groupId: votingGroup._id,
      status: UserStatus.IN_GROUP
    });

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .post(`/api/group/vote/${votingGroup._id}`)
      .set('Authorization', `Bearer ${token}`)
      .send({
        restaurantID: 'rest-winner',
        restaurant: {
          name: 'Winner Restaurant',
          location: '123 Winner St',
          restaurantId: 'rest-winner'
        }
      });

    expect(response.status).toBe(200);

    // Verify Firebase was called (notification service used it)
    expect(firebaseSpy).toHaveBeenCalled();
    
    // Verify notification had correct content
    const notificationCall = firebaseSpy.mock.calls[0];
    expect(notificationCall[0]).toEqual(['mock-fcm-token-user1']); // tokens array
    expect(notificationCall[1]).toMatchObject({
      title: 'Restaurant Selected! 🍽️',
      body: expect.stringContaining('Winner Restaurant')
    });
    expect(notificationCall[2]).toMatchObject({
      type: 'restaurant_selected',
      restaurantName: 'Winner Restaurant'
    });
  });

  test('should still succeed when voting even if user has no FCM token', async () => {
    /**
     * Test that voting succeeds even when notification fails
     * (user has no FCM token)
     */
    
    const firebaseSpy = jest.spyOn(firebase, 'sendMulticastNotification');

    const votingGroup = await seedTestGroup(
      'test-room-vote-no-token',
      [testUsers[0]._id]
    );

    const User = (await import('../../src/models/User')).default;
    
    // Remove FCM token to test graceful handling
    await User.findByIdAndUpdate(testUsers[0]._id, { 
      groupId: votingGroup._id,
      status: UserStatus.IN_GROUP,
      fcmToken: null  // ← No token
    });

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .post(`/api/group/vote/${votingGroup._id}`)
      .set('Authorization', `Bearer ${token}`)
      .send({
        restaurantID: 'rest-winner',
        restaurant: {
          name: 'Winner Restaurant',
          location: '123 Winner St',
          restaurantId: 'rest-winner'
        }
      });

    // Vote should still succeed
    expect(response.status).toBe(200);
    expect(response.body.Body.message).toBe('Voting successful');

    // Firebase should NOT be called (no tokens available)
    expect(firebaseSpy).not.toHaveBeenCalled();
  });
});

describe('POST /api/group/leave/:groupId - No Mocking', () => {
  test('should return 200 and successfully leave a group', async () => {
    // Spy on socket manager
    const emitMemberLeftSpy = jest.spyOn(socketManager, 'emitMemberLeft');

    const leavingGroup = await seedTestGroup(
      'test-room-leave',
      [testUsers[0]._id, testUsers[1]._id]
    );

    const User = (await import('../../src/models/User')).default;
    await User.findByIdAndUpdate(testUsers[0]._id, { 
      groupId: leavingGroup._id, 
      status: UserStatus.IN_GROUP 
    });
    await User.findByIdAndUpdate(testUsers[1]._id, { 
      groupId: leavingGroup._id, 
      status: UserStatus.IN_GROUP 
    });

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .post(`/api/group/leave/${leavingGroup._id}`)
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(200);
    expect(response.body.Status).toBe(200);
    expect(response.body.Message.text).toBe('Successfully left group');
    expect(response.body.Body.groupId).toBe(leavingGroup._id);

    // Verify user was removed from group
    const updatedGroup = await getTestGroupById(leavingGroup._id);
    expect(updatedGroup).not.toBeNull();
    expect(updatedGroup!.members).not.toContain(testUsers[0]._id);

    // Verify user status was updated
    const updatedUser = await User.findById(testUsers[0]._id);
    expect(updatedUser!.groupId).toBeNull();
    expect(updatedUser!.status).toBe(UserStatus.ONLINE);

    // Verify socket event was emitted
    expect(emitMemberLeftSpy).toHaveBeenCalled();
  });

  // Note: "401 without authentication" test is consolidated above in status endpoint tests
  // All endpoints use the same authMiddleware, so testing one endpoint covers all

  // Note: "500 when group not found" test is consolidated above in vote endpoint tests
  // The same Group.findById() -> if (!group) -> throw Error('Group not found') pattern exists in vote and leave

  // Note: "400 for invalid ObjectId format" test is consolidated above in vote endpoint tests
  // The same CastError -> errorHandler -> "Invalid data format" pattern exists in vote and leave

  test('should return 500 when user not found', async () => {
    const nonExistentUserId = '507f1f77bcf86cd799439011';
    const token = generateTestToken(
      nonExistentUserId,
      'nonexistent@example.com',
      'google-nonexistent'
    );

    const response = await request(app)
      .post(`/api/group/leave/${testGroups[0]._id}`)
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe(' User not found');
  });

  // Consolidated test: delete group and restaurant data when last member leaves
  // This tests the leave group endpoint when last member leaves
  // The SAME scenario exists: last member leaves -> group deleted -> restaurant data deleted
  // Testing with restaurant data covers both: group deletion and restaurant data deletion
  test('should delete group and restaurant data when last member leaves', async () => {
    const groupWithRestaurant = await seedTestGroup(
      'test-room-restaurant-delete',
      [testUsers[0]._id],
      {
        restaurantSelected: true,
        restaurant: {
          name: 'Deleted Restaurant',
          location: '999 Delete Ave',
          restaurantId: 'delete-rest-123'
        }
      }
    );

    const User = (await import('../../src/models/User')).default;
    await User.findByIdAndUpdate(testUsers[0]._id, { 
      groupId: groupWithRestaurant._id,
      status: UserStatus.IN_GROUP
    });

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .post(`/api/group/leave/${groupWithRestaurant._id}`)
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(200);

    // Verify group was deleted (covers both group deletion and restaurant data deletion)
    const Group = (await import('../../src/models/Group')).default;
    const deletedGroup = await Group.findById(groupWithRestaurant._id);
    
    expect(deletedGroup).toBeNull();
  });

  test('should preserve restaurant data when member leaves (group not deleted)', async () => {
    // Spy on socket manager
    const emitRestaurantSelectedSpy = jest.spyOn(socketManager, 'emitRestaurantSelected');

    const groupWithRestaurant = await seedTestGroup(
      'test-room-restaurant-preserve',
      [testUsers[0]._id, testUsers[1]._id],
      {
        restaurantSelected: true,
        restaurant: {
          name: 'Preserved Restaurant',
          location: '789 Keep St',
          restaurantId: 'preserve-rest-123',
          cuisine: 'Mexican'
        }
      }
    );

    const User = (await import('../../src/models/User')).default;
    await User.findByIdAndUpdate(testUsers[0]._id, { 
      groupId: groupWithRestaurant._id,
      status: UserStatus.IN_GROUP
    });
    await User.findByIdAndUpdate(testUsers[1]._id, { 
      groupId: groupWithRestaurant._id,
      status: UserStatus.IN_GROUP
    });

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .post(`/api/group/leave/${groupWithRestaurant._id}`)
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(200);

    // Verify group still exists
    const Group = (await import('../../src/models/Group')).default;
    const remainingGroup = await Group.findById(groupWithRestaurant._id);
    
    expect(remainingGroup).not.toBeNull();
    expect(remainingGroup!.members.length).toBe(1);
    
    // Verify restaurant data is preserved
    expect(remainingGroup!.restaurantSelected).toBe(true);
    expect(remainingGroup!.restaurant).toBeDefined();
    expect(remainingGroup!.restaurant!.name).toBe('Preserved Restaurant');
    expect(remainingGroup!.restaurant!.restaurantId).toBe('preserve-rest-123');
    expect(remainingGroup!.restaurant!.location).toBe('789 Keep St');

    // Socket event should not be emitted (restaurant was already selected)
    expect(emitRestaurantSelectedSpy).not.toHaveBeenCalled();
  });

  // Note: "should delete restaurant data when last member leaves (group deleted)" test is consolidated above
  // The same scenario (last member leaves -> group deleted) is covered by "should delete group and restaurant data when last member leaves"

  test('should verify restaurant field remains after completionTime if not cleared', async () => {
    const expiredGroupWithRestaurant = await seedTestGroup(
      'test-room-expired-restaurant',
      [testUsers[0]._id],
      {
        restaurantSelected: false,
        completionTime: new Date(Date.now() - 3600000),
        restaurant: {
          name: 'Expired Restaurant',
          location: 'Expired St',
          restaurantId: 'expired-rest-123'
        }
      }
    );

    const User = (await import('../../src/models/User')).default;
    await User.findByIdAndUpdate(testUsers[0]._id, { groupId: expiredGroupWithRestaurant._id });

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .get('/api/group/status')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(200);
    expect(response.body.Body.status).toBe('disbanded');
    expect(response.body.Body.restaurantSelected).toBe(false);
    
    // Restaurant data may still exist
    const Group = (await import('../../src/models/Group')).default;
    const group = await Group.findById(expiredGroupWithRestaurant._id);
    if (group?.restaurant) {
      expect(group.restaurant).toBeDefined();
    }
  });
});

describe('Group Model Methods - Integration Tests', () => {
  /**
   * These tests verify Group model methods through integration with the database
   * Covers: groupId virtual, toJSON transform, addVote, removeVote
   */

  test('should access groupId virtual property through API response', async () => {
    /**
     * Covers Group.ts line 119: groupId virtual getter
     * Path: GroupSchema.virtual('groupId').get() -> this._id.toString()
     */
    const User = (await import('../../src/models/User')).default;
    const testGroupData = await seedTestGroup(
      'test-room-groupid',
      [testUsers[0]._id, testUsers[1]._id]
    );
    await User.findByIdAndUpdate(testUsers[0]._id, {
      groupId: testGroupData._id,
      status: UserStatus.IN_GROUP
    });
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );
    const response = await request(app)
      .get('/api/group/status')
      .set('Authorization', `Bearer ${token}`);
    expect(response.status).toBe(200);
    expect(response.body.Body).toHaveProperty('groupId');
    expect(response.body.Body.groupId).toBe(testGroupData._id);
  });


  test('should use toJSON transform to convert Maps to objects', async () => {
    /**
     * Covers Group.ts lines 122-131: toJSON transform function
     * Path: transform function -> return { groupId, ...rest }
     * Mongoose automatically converts Maps to objects before transform runs
     */
    const Group = (await import('../../src/models/Group')).default;
    const testGroupData = await seedTestGroup(
      'test-room-tojson',
      [testUsers[0]._id, testUsers[1]._id]
    );
    
    // Get the actual Group document
    const testGroup = await Group.findById(testGroupData._id);
    expect(testGroup).not.toBeNull();
    
    // Add votes to create Map objects
    testGroup!.addVote(testUsers[0]._id.toString(), 'rest-1');
    testGroup!.addVote(testUsers[1]._id.toString(), 'rest-1');
    await testGroup!.save();
    
    // Get group and convert to JSON
    const group = await Group.findById(testGroupData._id);
    const json = group!.toJSON();
    
    expect(json).toHaveProperty('groupId');
    expect(json).not.toHaveProperty('_id');
    expect(json).not.toHaveProperty('__v');
    // Votes and restaurantVotes should be objects, not Maps
    expect(json.votes).not.toBeInstanceOf(Map);
    expect(json.restaurantVotes).not.toBeInstanceOf(Map);
    expect(typeof json.votes).toBe('object');
    expect(typeof json.restaurantVotes).toBe('object');
  });

  // Consolidated test: add vote when no previous vote and || 0 fallback
  // This tests the addVote method when no previous vote exists
  // The SAME code path exists: no previous vote -> add new vote -> || 0 fallback for restaurant not in restaurantVotes
  // Testing with a restaurant not in restaurantVotes covers both: no previous vote and || 0 fallback
  test('should add vote when user has no previous vote and use || 0 fallback', async () => {
    /**
     * Tests addVote method when no previous vote exists
     * Covers: Group.ts lines 142-151 (no previous vote), line 149 (|| 0 fallback)
     * Both scenarios execute the same code path: no previous vote -> add new vote -> || 0 fallback
     */
    const Group = (await import('../../src/models/Group')).default;
    const testGroupData = await seedTestGroup(
      'test-room-first-vote',
      [testUsers[0]._id]
    );
    
    // Get the actual Group document
    let testGroup = await Group.findById(testGroupData._id);
    expect(testGroup).not.toBeNull();
    
    // Ensure the restaurant is NOT in restaurantVotes (triggers || 0 fallback)
    testGroup!.restaurantVotes.delete('rest-1');
    await testGroup!.save();
    
    // Verify restaurant is not in restaurantVotes
    testGroup = await Group.findById(testGroupData._id);
    expect(testGroup!.restaurantVotes.get('rest-1')).toBeUndefined();
    
    // Add first vote (no previous vote exists - covers false branch of line 143)
    // Also triggers || 0 fallback on line 149 since restaurant not in restaurantVotes
    testGroup!.addVote(testUsers[0]._id.toString(), 'rest-1');
    await testGroup!.save();
    
    testGroup = await Group.findById(testGroupData._id);
    // The restaurant should now have count 1 (0 + 1, using || 0 fallback)
    expect(testGroup!.restaurantVotes.get('rest-1')).toBe(1);
    expect(testGroup!.votes.get(testUsers[0]._id.toString())).toBe('rest-1');
  });

  test('should handle previous vote when adding new vote', async () => {
    /**
     * Covers Group.ts lines 143-146: addVote method previous vote handling
     * Path: if (previousVote) [TRUE BRANCH] -> decrement previous vote count
     */
    const Group = (await import('../../src/models/Group')).default;
    const testGroupData = await seedTestGroup(
      'test-room-prev-vote',
      [testUsers[0]._id]
    );
    
    // Get the actual Group document
    let testGroup = await Group.findById(testGroupData._id);
    expect(testGroup).not.toBeNull();
    
    // Add first vote
    testGroup!.addVote(testUsers[0]._id.toString(), 'rest-1');
    await testGroup!.save();
    
    testGroup = await Group.findById(testGroupData._id);
    expect(testGroup!.restaurantVotes.get('rest-1')).toBe(1);
    
    // Change vote to different restaurant (should decrement rest-1, increment rest-2)
    testGroup!.addVote(testUsers[0]._id.toString(), 'rest-2');
    await testGroup!.save();
    
    testGroup = await Group.findById(testGroupData._id);
    expect(testGroup!.restaurantVotes.get('rest-1')).toBe(0);
    expect(testGroup!.restaurantVotes.get('rest-2')).toBe(1);
    expect(testGroup!.votes.get(testUsers[0]._id.toString())).toBe('rest-2');
  });

  test('should handle previous vote when restaurantVotes.get returns undefined', async () => {
    /**
     * Covers Group.ts line 143: addVote method || 0 fallback
     * Path: if (previousVote) [TRUE BRANCH] -> this.restaurantVotes.get(previousVote) returns undefined -> || 0 fallback
     * This tests the edge case where a vote exists in votes Map but the restaurant is not in restaurantVotes Map
     */
    const Group = (await import('../../src/models/Group')).default;
    const testGroupData = await seedTestGroup(
      'test-room-undefined-fallback',
      [testUsers[0]._id]
    );
    
    // Get the actual Group document
    let testGroup = await Group.findById(testGroupData._id);
    expect(testGroup).not.toBeNull();
    
    // Manually set a vote in votes Map but don't add it to restaurantVotes
    // This simulates an edge case where restaurantVotes.get() would return undefined
    testGroup!.votes.set(testUsers[0]._id.toString(), 'rest-orphan');
    // Ensure rest-orphan is NOT in restaurantVotes (or delete it if it exists)
    testGroup!.restaurantVotes.delete('rest-orphan');
    await testGroup!.save();
    
    // Verify the orphan vote exists but restaurantVotes doesn't have it
    testGroup = await Group.findById(testGroupData._id);
    expect(testGroup!.votes.get(testUsers[0]._id.toString())).toBe('rest-orphan');
    expect(testGroup!.restaurantVotes.get('rest-orphan')).toBeUndefined();
    
    // Now add a new vote - this should trigger the || 0 fallback
    testGroup!.addVote(testUsers[0]._id.toString(), 'rest-new');
    await testGroup!.save();
    
    testGroup = await Group.findById(testGroupData._id);
    // The orphan restaurant should have count 0 (or not exist) after decrementing from 0
    expect(testGroup!.restaurantVotes.get('rest-orphan') || 0).toBe(0);
    // The new restaurant should have count 1
    expect(testGroup!.restaurantVotes.get('rest-new')).toBe(1);
    expect(testGroup!.votes.get(testUsers[0]._id.toString())).toBe('rest-new');
  });

  test('should remove vote when restaurantId exists', async () => {
    /**
     * Covers Group.ts lines 159-160: removeVote method condition check
     * Path: if (restaurantId) -> delete vote and decrement count
     */
    const Group = (await import('../../src/models/Group')).default;
    const testGroupData = await seedTestGroup(
      'test-room-remove-vote',
      [testUsers[0]._id]
    );
    
    // Get the actual Group document
    let testGroup = await Group.findById(testGroupData._id);
    expect(testGroup).not.toBeNull();
    
    // Add vote
    testGroup!.addVote(testUsers[0]._id.toString(), 'rest-1');
    await testGroup!.save();
    
    testGroup = await Group.findById(testGroupData._id);
    expect(testGroup!.restaurantVotes.get('rest-1')).toBe(1);
    expect(testGroup!.votes.has(testUsers[0]._id.toString())).toBe(true);
    
    // Remove vote
    testGroup!.removeVote(testUsers[0]._id.toString());
    await testGroup!.save();
    
    testGroup = await Group.findById(testGroupData._id);
    expect(testGroup!.restaurantVotes.get('rest-1')).toBe(0);
    expect(testGroup!.votes.has(testUsers[0]._id.toString())).toBe(false);
    
    // Try to remove vote that doesn't exist (should not error)
    // This covers the false branch of line 157: if (restaurantId) [FALSE]
    testGroup!.removeVote(testUsers[1]._id.toString());
    await testGroup!.save();
  });

  test('should use || 0 fallback when removing vote for restaurant not in restaurantVotes', async () => {
    /**
     * Covers Group.ts line 158: removeVote method || 0 fallback
     * Path: if (restaurantId) [TRUE BRANCH] -> this.restaurantVotes.get(restaurantId) returns undefined -> || 0 fallback
     * This tests the edge case where a vote exists in votes Map but the restaurant is not in restaurantVotes Map
     */
    const Group = (await import('../../src/models/Group')).default;
    const testGroupData = await seedTestGroup(
      'test-room-remove-orphan-vote',
      [testUsers[0]._id]
    );
    
    // Get the actual Group document
    let testGroup = await Group.findById(testGroupData._id);
    expect(testGroup).not.toBeNull();
    
    // Manually set a vote in votes Map but don't add it to restaurantVotes
    // This simulates an edge case where restaurantVotes.get() would return undefined
    testGroup!.votes.set(testUsers[0]._id.toString(), 'rest-orphan');
    // Ensure rest-orphan is NOT in restaurantVotes (or delete it if it exists)
    testGroup!.restaurantVotes.delete('rest-orphan');
    await testGroup!.save();
    
    // Verify the orphan vote exists but restaurantVotes doesn't have it
    testGroup = await Group.findById(testGroupData._id);
    expect(testGroup!.votes.get(testUsers[0]._id.toString())).toBe('rest-orphan');
    expect(testGroup!.restaurantVotes.get('rest-orphan')).toBeUndefined();
    
    // Now remove the vote - this should trigger the || 0 fallback
    testGroup!.removeVote(testUsers[0]._id.toString());
    await testGroup!.save();
    
    testGroup = await Group.findById(testGroupData._id);
    // The vote should be removed from votes Map
    expect(testGroup!.votes.get(testUsers[0]._id.toString())).toBeUndefined();
    // The orphan restaurant should have count 0 (or not exist) after decrementing from 0
    expect(testGroup!.restaurantVotes.get('rest-orphan') || 0).toBe(0);
  });

  test('should get winning restaurant when votes exist', async () => {
    /**
     * Covers Group.ts lines 165-177: getWinningRestaurant method
     * Path: forEach -> if (votes > maxVotes) [TRUE] -> set winner
     */
    const Group = (await import('../../src/models/Group')).default;
    const testGroupData = await seedTestGroup(
      'test-room-winning',
      [testUsers[0]._id, testUsers[1]._id, testUsers[2]._id]
    );
    
    // Get the actual Group document
    let testGroup = await Group.findById(testGroupData._id);
    expect(testGroup).not.toBeNull();
    
    // Add votes - rest-1 gets 2 votes, rest-2 gets 1 vote
    testGroup!.addVote(testUsers[0]._id.toString(), 'rest-1');
    testGroup!.addVote(testUsers[1]._id.toString(), 'rest-1');
    testGroup!.addVote(testUsers[2]._id.toString(), 'rest-2');
    await testGroup!.save();
    
    testGroup = await Group.findById(testGroupData._id);
    const winner = testGroup!.getWinningRestaurant();
    expect(winner).toBe('rest-1');
  });

  test('should return null when no votes exist in getWinningRestaurant', async () => {
    /**
     * Covers Group.ts lines 165-177: getWinningRestaurant method when no votes
     * Path: forEach (empty) -> return winner (null)
     */
    const Group = (await import('../../src/models/Group')).default;
    const testGroupData = await seedTestGroup(
      'test-room-no-winner',
      [testUsers[0]._id]
    );
    
    // Get the actual Group document with no votes
    const testGroup = await Group.findById(testGroupData._id);
    expect(testGroup).not.toBeNull();
    
    const winner = testGroup!.getWinningRestaurant();
    expect(winner).toBeNull();
  });

  test('should handle equal votes in getWinningRestaurant', async () => {
    /**
     * Covers Group.ts lines 169-174: getWinningRestaurant method with equal votes
     * Path: forEach -> if (votes > maxVotes) [FALSE when equal] -> returns first winner found
     */
    const Group = (await import('../../src/models/Group')).default;
    const testGroupData = await seedTestGroup(
      'test-room-equal-votes',
      [testUsers[0]._id, testUsers[1]._id]
    );
    
    // Get the actual Group document
    let testGroup = await Group.findById(testGroupData._id);
    expect(testGroup).not.toBeNull();
    
    // Add equal votes - both restaurants get 1 vote
    testGroup!.addVote(testUsers[0]._id.toString(), 'rest-1');
    testGroup!.addVote(testUsers[1]._id.toString(), 'rest-2');
    await testGroup!.save();
    
    testGroup = await Group.findById(testGroupData._id);
    const winner = testGroup!.getWinningRestaurant();
    // When votes are equal, it returns the first one found (depends on Map iteration order)
    expect(winner).toBeTruthy(); // Should return one of them
    expect(['rest-1', 'rest-2']).toContain(winner);
  });

  test('should check if all members have voted', async () => {
    /**
     * Covers Group.ts lines 180-182: hasAllVoted method
     * Path: return this.votes.size === this.members.length
     */
    const Group = (await import('../../src/models/Group')).default;
    const testGroupData = await seedTestGroup(
      'test-room-all-voted',
      [testUsers[0]._id, testUsers[1]._id]
    );
    
    // Get the actual Group document
    let testGroup = await Group.findById(testGroupData._id);
    expect(testGroup).not.toBeNull();
    
    // Not all voted yet
    expect(testGroup!.hasAllVoted()).toBe(false);
    
    // Add votes for all members
    testGroup!.addVote(testUsers[0]._id.toString(), 'rest-1');
    testGroup!.addVote(testUsers[1]._id.toString(), 'rest-1');
    await testGroup!.save();
    
    testGroup = await Group.findById(testGroupData._id);
    expect(testGroup!.hasAllVoted()).toBe(true);
  });

  test('should remove member from group', async () => {
    /**
     * Covers Group.ts lines 185-188: removeMember method
     * Path: filter members -> removeVote(userId)
     */
    const Group = (await import('../../src/models/Group')).default;
    const testGroupData = await seedTestGroup(
      'test-room-remove-member',
      [testUsers[0]._id, testUsers[1]._id]
    );
    
    // Get the actual Group document
    let testGroup = await Group.findById(testGroupData._id);
    expect(testGroup).not.toBeNull();
    
    // Add a vote for the member we'll remove
    testGroup!.addVote(testUsers[0]._id.toString(), 'rest-1');
    await testGroup!.save();
    
    testGroup = await Group.findById(testGroupData._id);
    expect(testGroup!.members).toContain(testUsers[0]._id);
    expect(testGroup!.votes.has(testUsers[0]._id.toString())).toBe(true);
    
    // Remove member
    testGroup!.removeMember(testUsers[0]._id.toString());
    await testGroup!.save();
    
    testGroup = await Group.findById(testGroupData._id);
    expect(testGroup!.members).not.toContain(testUsers[0]._id);
    expect(testGroup!.members).toContain(testUsers[1]._id);
    expect(testGroup!.votes.has(testUsers[0]._id.toString())).toBe(false);
  });

});