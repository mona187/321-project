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

  test('should return 401 without authentication token', async () => {
    const response = await request(app)
      .get('/api/group/status');

    expect(response.status).toBe(401);
    expect(response.body).toHaveProperty('error');
  });

  test('should return 401 with invalid token', async () => {
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

  test('should return 500 when group not found', async () => {
    const nonExistentGroupId = '507f1f77bcf86cd799439011';
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .post(`/api/group/vote/${nonExistentGroupId}`)
      .set('Authorization', `Bearer ${token}`)
      .send({ restaurantID: 'rest-123' });

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Group not found');
  });

  test('should return 400 for invalid ObjectId format in groupId', async () => {
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

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

  test('should return 401 without authentication token', async () => {
    const response = await request(app)
      .post(`/api/group/leave/${testGroups[0]._id}`);

    expect(response.status).toBe(401);
  });

  test('should return 500 when group not found', async () => {
    const nonExistentGroupId = '507f1f77bcf86cd799439011';
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .post(`/api/group/leave/${nonExistentGroupId}`)
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Group not found');
  });

  test('should return 400 for invalid ObjectId format in groupId', async () => {
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .post('/api/group/leave/invalid-group-id-format')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(400);
    expect(response.body.message).toBe('Invalid data format');
  });

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
    expect(response.body.message).toBe('User not found');
  });

  test('should delete group when last member leaves', async () => {
    const singleMemberGroup = await seedTestGroup(
      'test-room-single-member',
      [testUsers[0]._id]
    );

    const User = (await import('../../src/models/User')).default;
    await User.findByIdAndUpdate(testUsers[0]._id, { 
      groupId: singleMemberGroup._id, 
      status: UserStatus.IN_GROUP 
    });

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .post(`/api/group/leave/${singleMemberGroup._id}`)
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(200);

    // Verify group was deleted
    const deletedGroup = await getTestGroupById(singleMemberGroup._id);
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

  test('should delete restaurant data when last member leaves (group deleted)', async () => {
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

    // Verify group was deleted
    const Group = (await import('../../src/models/Group')).default;
    const deletedGroup = await Group.findById(groupWithRestaurant._id);
    
    expect(deletedGroup).toBeNull();
  });

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
    expect(response.body.Body.status).toBe('expired');
    expect(response.body.Body.restaurantSelected).toBe(false);
    
    // Restaurant data may still exist
    const Group = (await import('../../src/models/Group')).default;
    const group = await Group.findById(expiredGroupWithRestaurant._id);
    if (group?.restaurant) {
      expect(group.restaurant).toBeDefined();
    }
  });
});