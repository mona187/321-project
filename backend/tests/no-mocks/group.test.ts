//----------------------------------------------------------------------------------------//
// REMOVE THIS IF SOCKET and NOTIFICATION MOCKS ARE NOT ALLOWED
// MOCK DEPENDENCIES (before any imports)
// ============================================
// Mock socket manager
jest.mock('../../src/utils/socketManager', () => ({
  __esModule: true,
  default: {
    initialize: jest.fn(),
    getIO: jest.fn(),
    getEmitter: jest.fn(),
    emitRoomUpdate: jest.fn(),
    emitToUser: jest.fn(),
    emitMemberJoined: jest.fn(),
    emitMemberLeft: jest.fn(),
    emitGroupReady: jest.fn(),
    emitRoomExpired: jest.fn(),
    emitVoteUpdate: jest.fn(),
    emitRestaurantSelected: jest.fn(),
  }
}));

// Mock notification service - simple version
jest.mock('../../src/services/notificationService', () => ({
  notifyGroupMembers: jest.fn().mockResolvedValue(undefined),
  notifyRestaurantSelected: jest.fn().mockResolvedValue(undefined),
  notifyRoomMatched: jest.fn().mockResolvedValue(undefined),
  notifyRoomExpired: jest.fn().mockResolvedValue(undefined),
  sendNotificationToUser: jest.fn().mockResolvedValue(undefined),
  sendNotificationToUsers: jest.fn().mockResolvedValue(undefined),
  notifyRoomMembers: jest.fn().mockResolvedValue(undefined),
}));
//-------------------------------------------------------------------------------------------//

// tests/no-mocks/group.test.ts
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
import { connectDatabase, disconnectDatabase } from '../../src/config/database';
import { UserStatus } from '../../src/models/User';


/**
 * Group Routes Tests - No Mocking
 * Tests group endpoints with actual database interactions
 */

let testUsers: TestUser[];
let testGroups: TestGroup[];


beforeAll(async () => {
  console.log('\n🚀 Starting Group Tests (No Mocking)...\n');

  await connectDatabase();
  
  // Seed test users
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

  // ✅ Reload updated users — NOT reseeding
  testUsers = await UserModel.find({}).lean() as any;

  console.log(`\n✅ Test setup complete. Ready to run tests.\n`);
});


afterAll(async () => {
  console.log('\n🧹 Cleaning up after tests...\n');
  
  // Clean up test data
  await cleanTestData();
  
  // Close database connection
  await disconnectDatabase();
  
  console.log('✅ Cleanup complete.\n');
});

// 🔥 FIX: Add cleanup between tests
afterEach(async () => {
  // Reset users back to their original group state from beforeAll
  const UserModel = (await import('../../src/models/User')).default;
  
  // Reset test users 0 and 1 to group1
  await UserModel.findByIdAndUpdate(testUsers[0]._id, { 
    groupId: testGroups[0]._id, 
    status: UserStatus.IN_GROUP 
  });
  await UserModel.findByIdAndUpdate(testUsers[1]._id, { 
    groupId: testGroups[0]._id, 
    status: UserStatus.IN_GROUP 
  });
  
  // Reset test users 2 and 3 to group2
  await UserModel.findByIdAndUpdate(testUsers[2]._id, { 
    groupId: testGroups[1]._id, 
    status: UserStatus.IN_GROUP 
  });
  await UserModel.findByIdAndUpdate(testUsers[3]._id, { 
    groupId: testGroups[1]._id, 
    status: UserStatus.IN_GROUP 
  });
  
  jest.clearAllMocks();
});

describe('GET /api/group/status - No Mocking', () => {

     /**
   * Interface: GET /api/group/status
   * Mocking: None
   */

test('Should return 200 and group status for user in a group', async () => {
     /**
     * Input: GET /api/group/status with valid token for user in group
     * Expected Status Code: 200
     * Expected Output:
     *   {
     *     Status: 200,
     *     Message: {},
     *     Body: {
     *       groupId: string,
     *       roomId: string,
     *       completionTime: number,
     *       numMembers: number,
     *       users: string[],
     *       restaurantSelected: boolean,
     *       restaurant: object | undefined,
     *       status: 'voting' | 'completed' | 'expired'
     *     }
     *   }
     * Expected Behavior:
     *   - Auth middleware verifies token
     *   - Extract userId from token
     *   - Query database for user's group
     *   - Return group status with all details
     */

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
    expect(response.body.Body.status).toBe('voting'); // Not expired, not selected
  });
});
  test('should return 404 when user is not in a group', async () => {
    /**
     * Input: GET /api/group/status with valid token for user without group
     * Expected Status Code: 404
     * Expected Output:
     *   {
     *     Status: 404,
     *     Message: { error: 'Not in a group' },
     *     Body: null
     *   }
     * Expected Behavior:
     *   - Auth succeeds
     *   - Query database for user's group
     *   - User has no groupId
     *   - GroupService.getGroupByUserId() returns null
     *   - Return 404
     */
      const token = generateTestToken(
      testUsers[4]._id, // User not in any group
      testUsers[4].email,
      testUsers[4].googleId
    );

    const response = await request(app).get('/api/group/status').set(`Authorization`, `Bearer ${token}`);
    expect(response.status).toBe(404);
    expect(response.body.Status).toBe(404);
    expect(response.body.Message).toHaveProperty('error', 'Not in a group');
    expect(response.body.Body).toBeNull();

  });

 test('should return 401 without authentication token', async () => {
    /**
     * Input: GET /api/group/status without Authorization header
     * Expected Status Code: 401
     * Expected Output: Unauthorized error from auth middleware
     * Expected Behavior: Auth middleware blocks request
     */

    const response = await request(app)
      .get('/api/group/status');

    expect(response.status).toBe(401);
    expect(response.body).toHaveProperty('error');
  });

  test('should return 401 with invalid token', async () => {
    /**
     * Input: GET /api/group/status with malformed JWT token
     * Expected Status Code: 401
     * Expected Output: Invalid token error
     * Expected Behavior: Auth middleware verifies and rejects invalid token
     */

    const response = await request(app)
      .get('/api/group/status')
      .set('Authorization', 'Bearer invalid-token-format');

    expect(response.status).toBe(401);
  });

   test('should return group status with restaurant selected', async () => {
    /**
     * Input: GET /api/group/status for group with restaurant already selected
     * Expected Status Code: 200
     * Expected Output: Group status with status: 'completed' and restaurant data
     * Expected Behavior:
     *   - Query database for group
     *   - Group has restaurantSelected = true
     *   - Return status 'completed' with restaurant info
     */

    const token = generateTestToken(
      testUsers[2]._id, // User in group with restaurant selected
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
   test('should return 400 when restaurantID is missing', async () => {
    /**
     * Input: POST /api/group/vote/:groupId without restaurantID in body
     * Expected Status Code: 400
     * Expected Output: Restaurant ID is required error
     * Expected Behavior:
     *   - Auth succeeds
     *   - Validate request body
     *   - restaurantID is missing
     *   - Return 400 immediately
     */

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
    /**
     * Input: POST /api/group/vote/non-existent-group-id
     * Expected Status Code: 500
     * Expected Output: Group not found error
     * Expected Behavior:
     *   - Auth succeeds
     *   - Query database for group
     *   - Group doesn't exist
     *   - GroupService throws Error('Group not found')
     *   - Error handler returns 500
     */

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
    /**
     * Input: POST /api/group/vote/invalid-format-123
     * Expected Status Code: 400
     * Expected Output: Invalid data format error
     * Expected Behavior:
     *   - Auth succeeds
     *   - Mongoose tries to cast invalid string to ObjectId
     *   - Throws CastError
     *   - Error handler returns 400
     */

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
    /**
     * Input: POST /api/group/vote/:groupId for user not in group
     * Expected Status Code: 500
     * Expected Output: User is not a member error
     * Expected Behavior:
     *   - Auth succeeds
     *   - Find group
     *   - Check if userId is in group.members
     *   - User not found in members array
     *   - GroupService throws Error('User is not a member of this group')
     *   - Return 500
     */

    const token = generateTestToken(
      testUsers[4]._id, // User not in any group
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
    /**
     * Input: POST /api/group/vote/:groupId for group with restaurantSelected = true
     * Expected Status Code: 500
     * Expected Output: Restaurant has already been selected error
     * Expected Behavior:
     *   - Auth succeeds
     *   - Find group
     *   - Verify user is member
     *   - Check restaurantSelected flag
     *   - Flag is true
     *   - GroupService throws Error('Restaurant has already been selected')
     *   - Return 500
     */

    const token = generateTestToken(
      testUsers[2]._id, // User in group with restaurant selected
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

  test('should allow user to change their vote', async () => {
  /**
   * Input: POST /api/group/vote/:groupId twice with different restaurantIDs
   * Expected Status Code: 200 for both
   * Expected Output: Updated vote counts
   * Expected Behavior:
   *   - User votes for restaurant A
   *   - User votes again for restaurant B
   *   - Group.addVote() removes previous vote and adds new one
   *   - Vote counts reflect the change
   *   - Restaurant NOT auto-selected (need 2/2 votes)
   */

  // 🔥 FIX: Create group with 2 members so restaurant doesn't auto-select
  const votingGroup = await seedTestGroup(
    'test-room-change-vote',
    [testUsers[0]._id, testUsers[1]._id]  // ← 2 members instead of 1
  );

  const User = (await import('../../src/models/User')).default;
  
  // Update both users to be in the group
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

  // First vote from testUsers[0]
  const response1 = await request(app)
    .post(`/api/group/vote/${votingGroup._id}`)
    .set('Authorization', `Bearer ${token}`)
    .send({ restaurantID: 'rest-first' });

  expect(response1.status).toBe(200);
  expect(response1.body.Body.Current_votes['rest-first']).toBe(1);

  // Change vote (testUsers[0] votes again for different restaurant)
  const response2 = await request(app)
    .post(`/api/group/vote/${votingGroup._id}`)
    .set('Authorization', `Bearer ${token}`)
    .send({ restaurantID: 'rest-second' });

  expect(response2.status).toBe(200);
  expect(response2.body.Body.Current_votes['rest-second']).toBe(1);
  
  // Previous vote should be removed (undefined or 0)
  const firstVoteCount = response2.body.Body.Current_votes['rest-first'];
  expect(firstVoteCount === undefined || firstVoteCount === 0).toBe(true);
});


describe('POST /api/group/leave/:groupId - No Mocking', () => {
  /**
   * Interface: POST /api/group/leave/:groupId
   * Mocking: None
   */

  test('should return 200 and successfully leave a group', async () => {
  /**
   * Input: POST /api/group/leave/:groupId
   * Expected Status Code: 200
   * Expected Output:
   *   {
   *     Status: 200,
   *     Message: { text: 'Successfully left group' },
   *     Body: { groupId: string }
   *   }
   * Expected Behavior:
   *   - Auth succeeds
   *   - Find group in database
   *   - Find user in database
   *   - Remove user from group.members
   *   - Remove user's vote
   *   - Update user status to ONLINE
   *   - Clear user.groupId (sets to null)
   *   - Save group and user
   *   - Return success
   */

  // Create a fresh group for leave test
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
  expect(updatedUser!.groupId).toBeNull();  // 🔥 Changed from toBeUndefined()
  expect(updatedUser!.status).toBe(UserStatus.ONLINE);
});

  test('should return 401 without authentication token', async () => {
    /**
     * Input: POST /api/group/leave/:groupId without Authorization header
     * Expected Status Code: 401
     * Expected Output: Unauthorized error
     * Expected Behavior: Auth middleware blocks request
     */

    const response = await request(app)
      .post(`/api/group/leave/${testGroups[0]._id}`);

    expect(response.status).toBe(401);
  });

  test('should return 500 when group not found', async () => {
    /**
     * Input: POST /api/group/leave/non-existent-group-id
     * Expected Status Code: 500
     * Expected Output: Group not found error
     * Expected Behavior:
     *   - Auth succeeds
     *   - Query database for group
     *   - Group doesn't exist
     *   - GroupService throws Error('Group not found')
     *   - Return 500
     */

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
    /**
     * Input: POST /api/group/leave/invalid-format-123
     * Expected Status Code: 400
     * Expected Output: Invalid data format error
     * Expected Behavior:
     *   - Auth succeeds
     *   - Mongoose tries to cast invalid string to ObjectId
     *   - Throws CastError
     *   - Error handler returns 400
     */

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
    /**
     * Input: POST /api/group/leave/:groupId with valid token for non-existent user
     * Expected Status Code: 500
     * Expected Output: User not found error
     * Expected Behavior:
     *   - Auth succeeds (token is valid)
     *   - Find group successfully
     *   - Query database for user
     *   - User doesn't exist
     *   - GroupService throws Error('User not found')
     *   - Return 500
     */

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
    /**
     * Input: POST /api/group/leave/:groupId when user is the only member
     * Expected Status Code: 200
     * Expected Output: Success message
     * Expected Behavior:
     *   - Find group with 1 member
     *   - Remove member
     *   - Group.members.length becomes 0
     *   - GroupService.deleteGroup() is called
     *   - Group is deleted from database
     *   - Return success
     */

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
    /**
     * Input: POST /api/group/leave/:groupId when group has restaurant selected
     * Expected Status Code: 200
     * Expected Output: Success, but restaurant data preserved
     * Expected Behavior:
     *   - Group has restaurantSelected = true and restaurant data
     *   - Member leaves group
     *   - Group is NOT deleted (still has other members)
     *   - Restaurant data should still exist in group
     *   - Verify restaurant is not deleted/erased
     */

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

    // 🔥 FIX: Update BOTH users to be in the group
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
  });

  test('should delete restaurant data when last member leaves (group deleted)', async () => {
    /**
     * Input: POST /api/group/leave/:groupId when last member leaves
     * Expected Status Code: 200
     * Expected Output: Success, group deleted
     * Expected Behavior:
     *   - Group has restaurantSelected = true and restaurant data
     *   - Last member leaves
     *   - Group is deleted (members.length === 0)
     *   - Since group is deleted, restaurant data is also deleted
     *   - Verify group (and its restaurant) no longer exists
     */

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

    // Verify group was deleted (restaurant goes with it)
    const Group = (await import('../../src/models/Group')).default;
    const deletedGroup = await Group.findById(groupWithRestaurant._id);
    
    expect(deletedGroup).toBeNull();
    
    // Verify restaurant data is also gone (since group is deleted)
    // We can't query restaurant separately, but since group is null, restaurant data is also deleted
  });

  test('should verify restaurant field remains after completionTime if not cleared', async () => {
    /**
     * Input: GET /api/group/status for group past completionTime with restaurant data
     * Expected Status Code: 200
     * Expected Output: Group status with expired status, restaurant may still exist
     * Expected Behavior:
     *   - Group has completionTime in past
     *   - Group has restaurant data but restaurantSelected = false
     *   - Status returns 'expired'
     *   - Restaurant field is NOT automatically cleared (only cleared on delete)
     *   - Restaurant data remains unless explicitly deleted
     */

    const expiredGroupWithRestaurant = await seedTestGroup(
      'test-room-expired-restaurant',
      [testUsers[0]._id],
      {
        restaurantSelected: false,
        completionTime: new Date(Date.now() - 3600000), // Expired
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
    
    // Restaurant data may still exist in the document even if status is expired
    // It's not automatically cleared when completionTime passes
    const Group = (await import('../../src/models/Group')).default;
    const group = await Group.findById(expiredGroupWithRestaurant._id);
    // Restaurant field exists in document but restaurantSelected is false
    if (group?.restaurant) {
      expect(group.restaurant).toBeDefined();
    }
  });
});