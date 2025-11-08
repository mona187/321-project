// tests/no-mocks/user.test.ts
import request from 'supertest';
import app from '../../src/app';
import { generateTestToken } from '../helpers/auth.helper';
import { generateExpiredToken } from '../helpers/auth.helper';
import { seedTestUsers, cleanTestData, TestUser, seedDeletableUser, getTestUserById } from '../helpers/seed.helper';
import { connectDatabase, disconnectDatabase } from '../../src/config/database';
import mongoose from 'mongoose';

/**
 * User Routes Tests - No Mocking
 * Tests user endpoints with actual database interactions
 */

let testUsers: TestUser[];

beforeAll(async () => {
  console.log('\n🚀 Starting User Tests (No Mocking)...\n');
  
  // Connect to test database
  await connectDatabase();
  
  // Seed test data
  testUsers = await seedTestUsers();
  
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

describe('GET /api/user/profile/:ids - No Mocking', () => {
  /**
   * Interface: GET /api/user/profile/:ids
   * Mocking: None
   */

  test('should return 200 and user profiles for valid IDs', async () => {
    /**
     * Input: GET /api/user/profile/userId1,userId2
     * Expected Status Code: 200
     * Expected Output: 
     *   {
     *     Status: 200,
     *     Message: {},
     *     Body: [array of user profiles]
     *   }
     * Expected Behavior:
     *   - Parse comma-separated IDs from URL
     *   - Query real database for users
     *   - Return array of user profiles
     */

    const userIds = `${testUsers[0]._id},${testUsers[1]._id}`;
    
    const response = await request(app)
      .get(`/api/user/profile/${userIds}`);

    expect(response.status).toBe(200);
    expect(response.body.Status).toBe(200);
    expect(Array.isArray(response.body.Body)).toBe(true);
    expect(response.body.Body).toHaveLength(2);
    
    // Verify we got the correct users
    const returnedIds = response.body.Body.map((u: any) => u.userId);
    expect(returnedIds).toContain(testUsers[0]._id);
    expect(returnedIds).toContain(testUsers[1]._id);
  });

  test('should return 200 with empty array for non-existent IDs', async () => {
    /**
     * Input: GET /api/user/profile/nonexistent-id
     * Expected Status Code: 200
     * Expected Output: Empty array in Body
     * Expected Behavior:
     *   - Query database with valid ObjectId format but non-existent ID
     *   - Database returns empty array
     *   - Return 200 with empty array
     */

    // Valid ObjectId format but doesn't exist in database
    const nonExistentId = new mongoose.Types.ObjectId().toString();
    
    const response = await request(app)
      .get(`/api/user/profile/${nonExistentId}`);

    expect(response.status).toBe(200);
    expect(response.body.Status).toBe(200);
    expect(response.body.Body).toHaveLength(0);
  });

  test('should handle multiple IDs correctly', async () => {
    /**
     * Input: GET /api/user/profile/id1,id2,id3,id4
     * Expected Status Code: 200
     * Expected Output: Array with all 4 users
     * Expected Behavior: Query and return all matching users
     */

    const userIds = testUsers.slice(0, 4).map(u => u._id).join(',');
    
    const response = await request(app)
      .get(`/api/user/profile/${userIds}`);

    expect(response.status).toBe(200);
    expect(response.body.Body).toHaveLength(4);
  });

  test('should return 400 for invalid MongoDB ObjectId format', async () => {
    /**
     * Input: GET /api/user/profile/invalid-format
     * Expected Status Code: 400
     * Expected Output: CastError with "Invalid data format"
     * Expected Behavior:
     *   - Mongoose tries to cast invalid string to ObjectId
     *   - Throws CastError
     *   - Error handler returns 400
     */

    const response = await request(app)
      .get('/api/user/profile/invalid-id-format-123');

    expect(response.status).toBe(400);
    expect(response.body.message).toBe('Invalid data format');
  });

  test('should handle single user ID', async () => {
    /**
     * Input: GET /api/user/profile/singleUserId
     * Expected Status Code: 200
     * Expected Output: Array with 1 user
     * Expected Behavior: Query and return single user in array
     */

    const response = await request(app)
      .get(`/api/user/profile/${testUsers[0]._id}`);

    expect(response.status).toBe(200);
    expect(response.body.Body).toHaveLength(1);
    expect(response.body.Body[0].userId).toBe(testUsers[0]._id);
  });
});

describe('GET /api/user/settings - No Mocking', () => {
  /**
   * Interface: GET /api/user/settings
   * Mocking: None
   */

  test('should return 200 and user settings with valid authentication', async () => {
    /**
     * Input: GET /api/user/settings with valid JWT for testUsers[0]
     * Expected Status Code: 200
     * Expected Output: Complete user settings from database
     * Expected Behavior:
     *   - Auth middleware verifies token
     *   - Extract userId from token
     *   - Query database for user
     *   - Return full settings object
     */

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .get('/api/user/settings')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(200);
    expect(response.body.Status).toBe(200);
    expect(response.body.Body.userId).toBe(testUsers[0]._id);
    expect(response.body.Body.name).toBe('Test User 1');
    expect(response.body.Body.budget).toBe(50);
    expect(response.body.Body.radiusKm).toBe(10);
  });

  test('should return 401 without authentication token', async () => {
    /**
     * Input: GET /api/user/settings without Authorization header
     * Expected Status Code: 401
     * Expected Output: Unauthorized error from auth middleware
     * Expected Behavior: Auth middleware blocks request
     */

    const response = await request(app)
      .get('/api/user/settings');

    expect(response.status).toBe(401);
    expect(response.body).toHaveProperty('error');
    expect(response.body.message).toMatch(/token|unauthorized/i);
  });

  test('should return 401 with invalid token', async () => {
    /**
     * Input: GET /api/user/settings with malformed JWT token
     * Expected Status Code: 401
     * Expected Output: Invalid token error
     * Expected Behavior: Auth middleware verifies and rejects invalid token
     */

    const response = await request(app)
      .get('/api/user/settings')
      .set('Authorization', 'Bearer invalid-token-format');

    expect(response.status).toBe(401);
    expect(response.body.message).toMatch(/invalid/i);
  });

  test('should return 401 with expired token', async () => {
    /**
     * Input: GET /api/user/settings with expired JWT
     * Expected Status Code: 401
     * Expected Output: Token expired error
     * Expected Behavior: Auth middleware catches TokenExpiredError
     */

    const expiredToken = generateExpiredToken(testUsers[0]._id);

    const response = await request(app)
      .get('/api/user/settings')
      .set('Authorization', `Bearer ${expiredToken}`);

    expect(response.status).toBe(401);
    expect(response.body.message).toMatch(/expired|invalid/i);
  });

  test('should return 500 when user not found in database', async () => {
    /**
     * Input: GET /api/user/settings with valid token for non-existent user
     * Expected Status Code: 500
     * Expected Output: User not found error
     * Expected Behavior:
     *   - Auth succeeds (token is valid)
     *   - Query database for userId from token
     *   - User doesn't exist
     *   - Service throws Error('User not found')
     *   - Error handler returns 500
     */

    // Create token for user that doesn't exist in database
    const nonExistentUserId = new mongoose.Types.ObjectId().toString();
    const token = generateTestToken(
      nonExistentUserId,
      'nonexistent@example.com',
      'google-nonexistent'
    );

    const response = await request(app)
      .get('/api/user/settings')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('User not found');
  });
});

describe('POST /api/user/profile - No Mocking', () => {
  /**
   * Interface: POST /api/user/profile
   * Mocking: None
   */

  test('should create/update profile with valid data', async () => {
    /**
     * Input: POST /api/user/profile with updated profile fields
     * Expected Status Code: 200
     * Expected Output: Updated profile from database
     * Expected Behavior:
     *   - Auth succeeds
     *   - Find user in database
     *   - Update specified fields
     *   - Save to database
     *   - Return updated profile
     */

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const updatedData = {
      name: 'Updated Test User 1',
      bio: 'Updated bio text',
      contactNumber: '9999999999'
    };

    const response = await request(app)
      .post('/api/user/profile')
      .set('Authorization', `Bearer ${token}`)
      .send(updatedData);

    expect(response.status).toBe(200);
    expect(response.body.Status).toBe(200);
    expect(response.body.Message.text).toBe('Profile updated successfully');
    expect(response.body.Body.name).toBe('Updated Test User 1');
    expect(response.body.Body.bio).toBe('Updated bio text');
    expect(response.body.Body.contactNumber).toBe('9999999999');
  });

  test('should return 401 without authentication', async () => {
    /**
     * Input: POST /api/user/profile without Authorization header
     * Expected Status Code: 401
     * Expected Output: Unauthorized error
     * Expected Behavior: Auth middleware blocks request
     */

    const response = await request(app)
      .post('/api/user/profile')
      .send({ name: 'Test' });

    expect(response.status).toBe(401);
  });

  test('should return 500 when user not found', async () => {
    /**
     * Input: POST /api/user/profile with valid token for non-existent user
     * Expected Status Code: 500
     * Expected Output: User not found error
     * Expected Behavior:
     *   - Auth succeeds
     *   - Service tries to find user
     *   - User doesn't exist
     *   - Throws Error('User not found')
     */

    const nonExistentUserId = new mongoose.Types.ObjectId().toString();
    const token = generateTestToken(
      nonExistentUserId,
      'nonexistent@example.com',
      'google-nonexistent'
    );

    const response = await request(app)
      .post('/api/user/profile')
      .set('Authorization', `Bearer ${token}`)
      .send({ name: 'Test' });

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('User not found');
  });

  test('should handle partial updates', async () => {
    /**
     * Input: POST /api/user/profile with only name field
     * Expected Status Code: 200
     * Expected Output: Profile with only name updated
     * Expected Behavior:
     *   - Update only provided field
     *   - Other fields remain unchanged in database
     */

    const token = generateTestToken(
      testUsers[1]._id,
      testUsers[1].email,
      testUsers[1].googleId
    );

    const response = await request(app)
      .post('/api/user/profile')
      .set('Authorization', `Bearer ${token}`)
      .send({ name: 'Only Name Changed' });

    expect(response.status).toBe(200);
    expect(response.body.Body.name).toBe('Only Name Changed');
    // Bio should remain unchanged
    expect(response.body.Body.bio).toBe(testUsers[1].bio);
  });

  test('should accept empty body without errors', async () => {
    /**
     * Input: POST /api/user/profile with empty object {}
     * Expected Status Code: 200
     * Expected Output: Unchanged profile
     * Expected Behavior:
     *   - No fields updated
     *   - Return current profile
     */

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .post('/api/user/profile')
      .set('Authorization', `Bearer ${token}`)
      .send({});

    expect(response.status).toBe(200);
  });
});

describe('POST /api/user/settings - No Mocking', () => {
  /**
   * Interface: POST /api/user/settings
   * Mocking: None
   */

  test('should update settings with valid data', async () => {
    /**
     * Input: POST /api/user/settings with settings data
     * Expected Status Code: 200
     * Expected Output: Updated settings
     * Expected Behavior:
     *   - Auth succeeds
     *   - Update user settings in database
     *   - Return complete settings via getUserSettings()
     */

    const token = generateTestToken(
      testUsers[1]._id,
      testUsers[1].email,
      testUsers[1].googleId
    );

    const settingsData = {
      name: 'Settings Updated Name',
      bio: 'Settings updated bio',
      preference: ['vegetarian', 'italian', 'mexican'],
      budget: 100,
      radiusKm: 25
    };

    const response = await request(app)
      .post('/api/user/settings')
      .set('Authorization', `Bearer ${token}`)
      .send(settingsData);

    expect(response.status).toBe(200);
    expect(response.body.Status).toBe(200);
    expect(response.body.Message.text).toBe('Settings updated successfully');
    expect(response.body.Body.name).toBe('Settings Updated Name');
    expect(response.body.Body.budget).toBe(100);
    expect(response.body.Body.radiusKm).toBe(25);
  });

  test('should return 401 without authentication', async () => {
    /**
     * Input: POST /api/user/settings without token
     * Expected Status Code: 401
     * Expected Output: Unauthorized error
     * Expected Behavior: Auth middleware blocks request
     */

    const response = await request(app)
      .post('/api/user/settings')
      .send({ preference: ['vegan'] });

    expect(response.status).toBe(401);
  });

  test('should accept partial settings updates', async () => {
    /**
     * Input: POST /api/user/settings with only budget and radiusKm
     * Expected Status Code: 200
     * Expected Output: Settings with only those fields updated
     * Expected Behavior:
     *   - Update only provided fields
     *   - Other fields remain unchanged
     */

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const partialSettings = {
      budget: 150,
      radiusKm: 30
    };

    const response = await request(app)
      .post('/api/user/settings')
      .set('Authorization', `Bearer ${token}`)
      .send(partialSettings);

    expect(response.status).toBe(200);
    expect(response.body.Body.budget).toBe(150);
    expect(response.body.Body.radiusKm).toBe(30);
  });
});

describe('PUT /api/user/profile - No Mocking', () => {
  /**
   * Interface: PUT /api/user/profile
   * Mocking: None
   */

  test('should update profile with valid data', async () => {
    /**
     * Input: PUT /api/user/profile with updated profile data
     * Expected Status Code: 200
     * Expected Output: Updated profile
     * Expected Behavior:
     *   - Auth succeeds
     *   - Update user profile in database
     *   - Return updated profile
     */

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const updatedData = {
      name: 'PUT Updated Name',
      bio: 'PUT updated bio',
      preference: ['thai', 'korean']
    };

    const response = await request(app)
      .put('/api/user/profile')
      .set('Authorization', `Bearer ${token}`)
      .send(updatedData);

    expect(response.status).toBe(200);
    expect(response.body.Status).toBe(200);
    expect(response.body.Message.text).toBe('Profile updated successfully');
    expect(response.body.Body.name).toBe('PUT Updated Name');
  });

  test('should return 401 without authentication', async () => {
    /**
     * Input: PUT /api/user/profile without token
     * Expected Status Code: 401
     * Expected Output: Unauthorized error
     * Expected Behavior: Auth middleware blocks request
     */

    const response = await request(app)
      .put('/api/user/profile')
      .send({ name: 'Test' });

    expect(response.status).toBe(401);
  });

  test('should handle partial profile updates', async () => {
    /**
     * Input: PUT /api/user/profile with subset of fields
     * Expected Status Code: 200
     * Expected Output: Profile with only provided fields updated
     * Expected Behavior: Update only specified fields
     */

    const token = generateTestToken(
      testUsers[1]._id,
      testUsers[1].email,
      testUsers[1].googleId
    );

    const partialUpdate = {
      bio: 'Just bio update via PUT'
    };

    const response = await request(app)
      .put('/api/user/profile')
      .set('Authorization', `Bearer ${token}`)
      .send(partialUpdate);

    expect(response.status).toBe(200);
    expect(response.body.Body.bio).toBe('Just bio update via PUT');
  });
});

describe('DELETE /api/user/:userId - No Mocking', () => {
  /**
   * Interface: DELETE /api/user/:userId
   * Mocking: None
   */

  test('should return 401 without authentication', async () => {
    /**
     * Input: DELETE /api/user/someUserId without Authorization header
     * Expected Status Code: 401
     * Expected Output: Unauthorized error
     * Expected Behavior: Auth middleware blocks request
     */

    const response = await request(app)
      .delete(`/api/user/${testUsers[0]._id}`);

    expect(response.status).toBe(401);
  });

  test('should return 403 when trying to delete different user', async () => {
    /**
     * Input: DELETE /api/user/otherUserId with token for different user
     * Expected Status Code: 403
     * Expected Output: Forbidden error
     * Expected Behavior:
     *   - Auth succeeds
     *   - Compare userId param with token userId
     *   - IDs don't match
     *   - Return 403 immediately (no database call)
     */

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .delete(`/api/user/${testUsers[1]._id}`)
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(403);
    expect(response.body.Status).toBe(403);
    expect(response.body.Message.error).toBe('Forbidden');
    expect(response.body.Body).toBeNull();
  });

  test('should return 500 when user is in a waiting room', async () => {
    /**
     * Input: DELETE /api/user/:userId for user with roomId
     * Expected Status Code: 500
     * Expected Output: Error about being in room
     * Expected Behavior:
     *   - Auth and authorization succeed
     *   - Query database for user
     *   - User has roomId set
     *   - Service throws error
     *   - Return 500
     */

    // testUsers[2] has roomId = 'test-room-123'
    const token = generateTestToken(
      testUsers[2]._id,
      testUsers[2].email,
      testUsers[2].googleId
    );

    const response = await request(app)
      .delete(`/api/user/${testUsers[2]._id}`)
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Cannot delete account while in a room or group');
  });

  test('should return 500 when user is in a group', async () => {
    /**
     * Input: DELETE /api/user/:userId for user with groupId
     * Expected Status Code: 500
     * Expected Output: Error about being in group
     * Expected Behavior:
     *   - Auth and authorization succeed
     *   - Query database for user
     *   - User has groupId set
     *   - Service throws error
     *   - Return 500
     */

    // testUsers[3] has groupId = 'test-group-456'
    const token = generateTestToken(
      testUsers[3]._id,
      testUsers[3].email,
      testUsers[3].googleId
    );

    const response = await request(app)
      .delete(`/api/user/${testUsers[3]._id}`)
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Cannot delete account while in a room or group');
  });

  test('should successfully delete user not in room or group', async () => {
    /**
     * Input: DELETE /api/user/:userId for user without roomId/groupId
     * Expected Status Code: 200
     * Expected Output: Success message with deleted: true
     * Expected Behavior:
     *   - Auth and authorization succeed
     *   - Find user in database
     *   - User has no roomId or groupId
     *   - Delete user from database
     *   - Return success
     */

    // Create a fresh deletable user
    const deletableUser = await seedDeletableUser();
    
    const token = generateTestToken(
      deletableUser._id,
      deletableUser.email,
      deletableUser.googleId
    );

    const response = await request(app)
      .delete(`/api/user/${deletableUser._id}`)
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(200);
    expect(response.body.Status).toBe(200);
    expect(response.body.Message.text).toBe('User deleted successfully');
    expect(response.body.Body.deleted).toBe(true);

    // Verify user was actually deleted from database
    const deletedUser = await getTestUserById(deletableUser._id);
    expect(deletedUser).toBeNull();
  });

  test('should return 500 when trying to delete non-existent user', async () => {
    /**
     * Input: DELETE /api/user/:userId for non-existent user
     * Expected Status Code: 500
     * Expected Output: User not found error
     * Expected Behavior:
     *   - Auth and authorization succeed
     *   - Try to find user in database
     *   - User doesn't exist
     *   - Service throws Error('User not found')
     */

    const nonExistentId = new mongoose.Types.ObjectId().toString();
    const token = generateTestToken(
      nonExistentId,
      'nonexistent@example.com',
      'google-nonexistent'
    );

    const response = await request(app)
      .delete(`/api/user/${nonExistentId}`)
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('User not found');
  });
});

describe('User Model Methods - Integration Tests', () => {
  /**
   * These tests verify User model methods through integration with the database
   * Covers: pre-save hooks
   */

  test('should access userId virtual property through API response', async () => {
    /**
     * Covers User.ts line 151: userId virtual getter
     * Path: UserSchema.virtual('userId').get() -> this._id.toString()
     */
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );
    const response = await request(app)
      .get(`/api/user/profile/${testUsers[0]._id}`)
      .set('Authorization', `Bearer ${token}`);
    expect(response.status).toBe(200);
    expect(response.body.Body[0]).toHaveProperty('userId');
    expect(response.body.Body[0].userId).toBe(testUsers[0]._id);
  });

  test('should use toJSON transform to include userId', async () => {
    /**
     * Covers User.ts lines 160-162: toJSON transform function
     * Path: transform function -> userId extraction -> return { userId, ...rest }
     */
    const response = await request(app)
      .get(`/api/user/profile/${testUsers[0]._id}`);
    expect(response.status).toBe(200);
    expect(response.body.Body[0]).toHaveProperty('userId');
    expect(response.body.Body[0]).not.toHaveProperty('_id');
    expect(response.body.Body[0]).not.toHaveProperty('__v');
  });

  test('should trigger pre-save hook when roomId is set', async () => {
    /**
     * Covers User.ts lines 193-194: Pre-save hook sets status to IN_WAITING_ROOM
     * Path: if (this.roomId && this.status !== UserStatus.IN_WAITING_ROOM) -> this.status = IN_WAITING_ROOM
     */
    const User = require('../../src/models/User').default;
    const UserStatus = require('../../src/models/User').UserStatus;
    
    // Set user to ONLINE first
    await User.findByIdAndUpdate(testUsers[0]._id, {
      status: UserStatus.ONLINE,
      roomId: undefined
    });
    
    // Set roomId - this should trigger pre-save hook
    const user = await User.findById(testUsers[0]._id);
    user.roomId = new mongoose.Types.ObjectId();
    await user.save();
    
    // Verify status was updated by pre-save hook
    const updatedUser = await User.findById(testUsers[0]._id);
    expect(updatedUser.status).toBe(UserStatus.IN_WAITING_ROOM);
    
    // Clean up
    await User.findByIdAndUpdate(testUsers[0]._id, {
      roomId: undefined,
      status: UserStatus.ONLINE
    });
  });

  test('should trigger pre-save hook when groupId is set', async () => {
    /**
     * Covers User.ts lines 197-198: Pre-save hook sets status to IN_GROUP
     * Path: if (this.groupId && this.status !== UserStatus.IN_GROUP) -> this.status = IN_GROUP
     */
    const User = require('../../src/models/User').default;
    const UserStatus = require('../../src/models/User').UserStatus;
    
    // Set user to ONLINE first
    await User.findByIdAndUpdate(testUsers[0]._id, {
      status: UserStatus.ONLINE,
      groupId: undefined
    });
    
    // Set groupId - this should trigger pre-save hook
    const user = await User.findById(testUsers[0]._id);
    user.groupId = new mongoose.Types.ObjectId();
    await user.save();
    
    // Verify status was updated by pre-save hook
    const updatedUser = await User.findById(testUsers[0]._id);
    expect(updatedUser.status).toBe(UserStatus.IN_GROUP);
    
    // Clean up
    await User.findByIdAndUpdate(testUsers[0]._id, {
      groupId: undefined,
      status: UserStatus.ONLINE
    });
  });

  test('should trigger pre-save hook when roomId is removed', async () => {
    /**
     * Covers User.ts lines 201-203: Pre-save hook sets status to ONLINE when roomId removed
     * Path: if (!this.roomId && this.status === UserStatus.IN_WAITING_ROOM) -> this.status = ONLINE
     */
    const User = require('../../src/models/User').default;
    const UserStatus = require('../../src/models/User').UserStatus;
    
    // First, ensure user has no groupId (which would override roomId status)
    const userClean = await User.findById(testUsers[0]._id);
    userClean.groupId = undefined;
    userClean.roomId = undefined;
    userClean.status = UserStatus.ONLINE;
    await userClean.save();
    
    // Set user to IN_WAITING_ROOM with roomId using save() to ensure pre-save hook runs
    const roomId = new mongoose.Types.ObjectId();
    const userWithRoom = await User.findById(testUsers[0]._id);
    userWithRoom.roomId = roomId;
    userWithRoom.groupId = undefined; // Ensure no groupId
    await userWithRoom.save(); // Pre-save hook will set status to IN_WAITING_ROOM
    
    // Verify user is in IN_WAITING_ROOM status
    const userBefore = await User.findById(testUsers[0]._id);
    expect(userBefore.status).toBe(UserStatus.IN_WAITING_ROOM);
    expect(userBefore.roomId).toBeDefined();
    
    // Remove roomId - this should trigger pre-save hook
    const user = await User.findById(testUsers[0]._id);
    user.roomId = undefined;
    await user.save();
    
    // Verify status was updated by pre-save hook
    const updatedUser = await User.findById(testUsers[0]._id);
    expect(updatedUser.status).toBe(UserStatus.ONLINE);
    expect(updatedUser.roomId).toBeNull();
  });

  test('should trigger pre-save hook when groupId is removed', async () => {
    /**
     * Covers User.ts lines 205-207: Pre-save hook sets status to ONLINE when groupId removed
     * Path: if (!this.groupId && this.status === UserStatus.IN_GROUP) -> this.status = ONLINE
     */
    const User = require('../../src/models/User').default;
    const UserStatus = require('../../src/models/User').UserStatus;
    
    // First, ensure user has no roomId (which could interfere)
    const userClean = await User.findById(testUsers[0]._id);
    userClean.groupId = undefined;
    userClean.roomId = undefined;
    userClean.status = UserStatus.ONLINE;
    await userClean.save();
    
    // Set user to IN_GROUP with groupId using save() to ensure pre-save hook runs
    const groupId = new mongoose.Types.ObjectId();
    const userWithGroup = await User.findById(testUsers[0]._id);
    userWithGroup.groupId = groupId;
    userWithGroup.roomId = undefined; // Ensure no roomId
    await userWithGroup.save(); // Pre-save hook will set status to IN_GROUP
    
    // Verify user is in IN_GROUP status
    const userBefore = await User.findById(testUsers[0]._id);
    expect(userBefore.status).toBe(UserStatus.IN_GROUP);
    expect(userBefore.groupId).toBeDefined();
    
    // Remove groupId - this should trigger pre-save hook
    const user = await User.findById(testUsers[0]._id);
    user.groupId = undefined;
    await user.save();
    
    // Verify status was updated by pre-save hook
    const updatedUser = await User.findById(testUsers[0]._id);
    expect(updatedUser.status).toBe(UserStatus.ONLINE);
    expect(updatedUser.groupId).toBeNull();
  });
});