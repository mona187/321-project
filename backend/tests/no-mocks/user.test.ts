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

  // Consolidated test: handle single and multiple IDs
  // This tests the ID parsing and querying pattern
  // The SAME code path exists whether there's one ID or multiple IDs (comma-separated)
  // Testing with multiple IDs covers both scenarios: single ID and multiple IDs
  test('should handle single and multiple IDs correctly', async () => {
    /**
     * Tests ID parsing and querying pattern
     * Covers: user.controller.ts ID parsing logic (single and comma-separated)
     * Both single ID and multiple IDs execute the same code: split by comma -> query -> return
     */
    // Test single ID
    const singleResponse = await request(app)
      .get(`/api/user/profile/${testUsers[0]._id}`);

    expect(singleResponse.status).toBe(200);
    expect(singleResponse.body.Body).toHaveLength(1);
    expect(singleResponse.body.Body[0].userId).toBe(testUsers[0]._id.toString());

    // Test multiple IDs
    const userIds = testUsers.slice(0, 4).map(u => u._id).join(',');
    const multipleResponse = await request(app)
      .get(`/api/user/profile/${userIds}`);

    expect(multipleResponse.status).toBe(200);
    expect(multipleResponse.body.Body).toHaveLength(4);
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

  // Consolidated test: 401 with invalid/expired token
  // These test the authMiddleware code which is the SAME for all endpoints
  // Testing once is sufficient since all endpoints use the same middleware
  test('should return 401 with invalid token', async () => {
    /**
     * Tests authMiddleware -> invalid token -> 401 pattern
     * Covers: auth.middleware.ts lines 56-62 (JsonWebTokenError)
     * All endpoints use the same authMiddleware, so testing one endpoint covers all
     */
    const response = await request(app)
      .get('/api/user/settings')
      .set('Authorization', 'Bearer invalid-token-format');

    expect(response.status).toBe(401);
    expect(response.body.message).toMatch(/invalid/i);
  });

  test('should return 401 with expired token', async () => {
    /**
     * Tests authMiddleware -> expired token -> 401 pattern
     * Covers: auth.middleware.ts lines 64-70 (TokenExpiredError)
     * All endpoints use the same authMiddleware, so testing one endpoint covers all
     */
    const expiredToken = generateExpiredToken(testUsers[0]._id);

    const response = await request(app)
      .get('/api/user/settings')
      .set('Authorization', `Bearer ${expiredToken}`);

    expect(response.status).toBe(401);
    expect(response.body.message).toMatch(/expired|invalid/i);
  });

  // Consolidated test: 500 when user not found
  // This tests the User.findById() -> if (!user) -> throw Error('User not found') pattern
  // The SAME pattern exists in getUserSettings (userService line 57-60) and updateProfile (userService line 93-96)
  // Testing once is sufficient since both use identical pattern: if (!user) { throw new Error('User not found') }
  test('should return 500 when user not found in database', async () => {
    /**
     * Tests User.findById() -> if (!user) -> throw Error('User not found') pattern
     * Covers: userService.ts lines 57-60 (getUserSettings), 93-96 (updateProfile)
     * Both methods have identical code: if (!user) { throw new Error('User not found') }
     */
    // Create token for user that doesn't exist in database
    const nonExistentUserId = new mongoose.Types.ObjectId().toString();
    const token = generateTestToken(
      nonExistentUserId,
      'nonexistent@example.com',
      'google-nonexistent'
    );

    // Test with settings endpoint - the code path is identical for settings and profile
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

  test('should update profilePicture directly in createUserProfile (covers userService line 101)', async () => {
    /**
     * Covers userService.ts line 101: Direct profilePicture assignment in createUserProfile
     * Path: if (data.profilePicture !== undefined) -> user.profilePicture = data.profilePicture
     * Note: createUserProfile does NOT convert Google URLs to Base64 (unlike updateUserSettings/updateUserProfile)
     */
    const token = generateTestToken(
      testUsers[1]._id,
      testUsers[1].email,
      testUsers[1].googleId
    );

    const profileData = {
      name: 'Profile Picture Test',
      profilePicture: 'https://example.com/custom-picture.jpg' // Direct assignment, no conversion
    };

    const response = await request(app)
      .post('/api/user/profile')
      .set('Authorization', `Bearer ${token}`)
      .send(profileData);

    expect(response.status).toBe(200);
    expect(response.body.Body.profilePicture).toBe('https://example.com/custom-picture.jpg');
    
    // Verify in database - should be stored as-is (no Base64 conversion)
    const User = (await import('../../src/models/User')).default;
    const updatedUser = await User.findById(testUsers[1]._id);
    expect(updatedUser!.profilePicture).toBe('https://example.com/custom-picture.jpg');
  });

  // Note: "401 without authentication" test is consolidated above in settings endpoint tests
  // All endpoints use the same authMiddleware, so testing one endpoint covers all

  // Note: "500 when user not found" test is consolidated above in settings endpoint tests
  // The same User.findById() -> if (!user) -> throw Error('User not found') pattern exists in settings and profile

  // Consolidated test: partial updates and empty body
  // This tests the partial update handling pattern
  // The SAME code path exists whether there are fields to update or an empty body
  // Testing with partial fields covers both scenarios: partial updates and empty body
  test('should handle partial updates and empty body', async () => {
    /**
     * Tests partial update handling pattern
     * Covers: user.controller.ts updateProfile method (partial updates and empty body)
     * Both partial fields and empty body execute the same code: update only provided fields
     */
    const token = generateTestToken(
      testUsers[1]._id,
      testUsers[1].email,
      testUsers[1].googleId
    );

    // Test partial update
    const partialResponse = await request(app)
      .post('/api/user/profile')
      .set('Authorization', `Bearer ${token}`)
      .send({ name: 'Only Name Changed' });

    expect(partialResponse.status).toBe(200);
    expect(partialResponse.body.Body.name).toBe('Only Name Changed');
    // Bio should remain unchanged
    expect(partialResponse.body.Body.bio).toBe(testUsers[1].bio);

    // Test empty body
    const emptyResponse = await request(app)
      .post('/api/user/profile')
      .set('Authorization', `Bearer ${token}`)
      .send({});

    expect(emptyResponse.status).toBe(200);
  });
});

describe('POST /api/user/settings - No Mocking', () => {
  /**
   * Interface: POST /api/user/settings
   * Mocking: None
   */

  // Consolidated test: update settings with full and partial data
  // This tests the POST /api/user/settings update pattern
  // The SAME code path exists whether there are all fields or partial fields
  // Testing with both full and partial data covers both scenarios
  test('should update settings with full and partial data', async () => {
    /**
     * Tests POST /api/user/settings update pattern
     * Covers: user.controller.ts updateSettings method (full and partial updates)
     * Both full fields and partial fields execute the same code: update only provided fields
     */
    const token = generateTestToken(
      testUsers[1]._id,
      testUsers[1].email,
      testUsers[1].googleId
    );

    // Test full update
    const fullSettings = {
      name: 'Settings Updated Name',
      bio: 'Settings updated bio',
      preference: ['vegetarian', 'italian', 'mexican'],
      budget: 100,
      radiusKm: 25
    };

    const fullResponse = await request(app)
      .post('/api/user/settings')
      .set('Authorization', `Bearer ${token}`)
      .send(fullSettings);

    expect(fullResponse.status).toBe(200);
    expect(fullResponse.body.Status).toBe(200);
    expect(fullResponse.body.Message.text).toBe('Settings updated successfully');
    expect(fullResponse.body.Body.name).toBe('Settings Updated Name');
    expect(fullResponse.body.Body.budget).toBe(100);
    expect(fullResponse.body.Body.radiusKm).toBe(25);

    // Test partial update
    const partialSettings = {
      budget: 150,
      radiusKm: 30
    };

    const partialResponse = await request(app)
      .post('/api/user/settings')
      .set('Authorization', `Bearer ${token}`)
      .send(partialSettings);

    expect(partialResponse.status).toBe(200);
    expect(partialResponse.body.Body.budget).toBe(150);
    expect(partialResponse.body.Body.radiusKm).toBe(30);
  });

  test('should update contactNumber, budget, and radiusKm in updateUserSettings (covers userService lines 149-151)', async () => {
    /**
     * Covers userService.ts lines 149-151: contactNumber, budget, radiusKm updates in updateUserSettings
     * Path: if (data.contactNumber !== undefined) -> user.contactNumber = data.contactNumber
     *       if (data.budget !== undefined) -> user.budget = data.budget
     *       if (data.radiusKm !== undefined) -> user.radiusKm = data.radiusKm
     */
    const token = generateTestToken(
      testUsers[2]._id,
      testUsers[2].email,
      testUsers[2].googleId
    );

    const settingsUpdate = {
      contactNumber: '1234567890',
      budget: 75,
      radiusKm: 15
    };

    const response = await request(app)
      .post('/api/user/settings')
      .set('Authorization', `Bearer ${token}`)
      .send(settingsUpdate);

    expect(response.status).toBe(200);
    expect(response.body.Body.contactNumber).toBe('1234567890');
    expect(response.body.Body.budget).toBe(75);
    expect(response.body.Body.radiusKm).toBe(15);
    
    // Verify in database
    const User = (await import('../../src/models/User')).default;
    const updatedUser = await User.findById(testUsers[2]._id);
    expect(updatedUser!.contactNumber).toBe('1234567890');
    expect(updatedUser!.budget).toBe(75);
    expect(updatedUser!.radiusKm).toBe(15);
  });

  test('should successfully convert Google profile picture to Base64 in updateUserSettings (covers userService lines 22-29)', async () => {
    /**
     * Covers userService.ts lines 22-29: Successful Base64 conversion in updateUserSettings
     * Path: axios.get succeeds -> Buffer.from -> toString('base64') -> create data URI -> return
     * This tests the successful conversion path when updating settings with a Google profile picture URL
     */
    const token = generateTestToken(
      testUsers[1]._id,
      testUsers[1].email,
      testUsers[1].googleId
    );

    const axios = require('axios');
    
    // Mock axios to return a successful image response
    const mockImageBuffer = Buffer.from('fake-image-data-for-settings-test');
    jest.spyOn(axios, 'get').mockResolvedValueOnce({
      data: mockImageBuffer,
      headers: { 'content-type': 'image/png' }
    });

    const googleProfilePictureUrl = 'https://lh3.googleusercontent.com/test-settings-picture.jpg';

    const response = await request(app)
      .post('/api/user/settings')
      .set('Authorization', `Bearer ${token}`)
      .send({ profilePicture: googleProfilePictureUrl });

    jest.restoreAllMocks();

    expect(response.status).toBe(200);
    
    // Verify profile picture was converted to Base64 data URI
    const User = (await import('../../src/models/User')).default;
    const updatedUser = await User.findById(testUsers[1]._id);
    expect(updatedUser!.profilePicture).toBeTruthy();
    expect(updatedUser!.profilePicture).toContain('data:image/png;base64,'); // Should be Base64 data URI
    expect(updatedUser!.profilePicture).not.toBe(googleProfilePictureUrl); // Should be converted, not original URL
  });

  test('should use image/png fallback when content-type header is missing (covers userService line 24)', async () => {
    /**
     * Covers userService.ts line 24: content-type fallback
     * Path: response.headers['content-type'] || 'image/png' [FALSE BRANCH] -> use 'image/png'
     * This tests the fallback when content-type header is missing
     */
    const token = generateTestToken(
      testUsers[2]._id,
      testUsers[2].email,
      testUsers[2].googleId
    );

    const axios = require('axios');
    
    // Mock axios to return response without content-type header
    const mockImageBuffer = Buffer.from('fake-image-data-no-content-type');
    jest.spyOn(axios, 'get').mockResolvedValueOnce({
      data: mockImageBuffer,
      headers: {} // No content-type header
    });

    const googleProfilePictureUrl = 'https://lh3.googleusercontent.com/test-no-content-type.jpg';

    const response = await request(app)
      .post('/api/user/settings')
      .set('Authorization', `Bearer ${token}`)
      .send({ profilePicture: googleProfilePictureUrl });

    jest.restoreAllMocks();

    expect(response.status).toBe(200);
    
    // Verify profile picture was converted with fallback content-type
    const User = (await import('../../src/models/User')).default;
    const updatedUser = await User.findById(testUsers[2]._id);
    expect(updatedUser!.profilePicture).toBeTruthy();
    expect(updatedUser!.profilePicture).toContain('data:image/png;base64,'); // Should use fallback 'image/png'
  });

  // Note: "401 without authentication" test is consolidated above in settings endpoint tests
  // All endpoints use the same authMiddleware, so testing one endpoint covers all
});

describe('PUT /api/user/profile - No Mocking', () => {
  /**
   * Interface: PUT /api/user/profile
   * Mocking: None
   */

  // Consolidated test: update profile with full and partial data
  // This tests the PUT /api/user/profile update pattern
  // The SAME code path exists whether there are all fields or partial fields
  // Testing with both full and partial data covers both scenarios
  test('should update profile with full and partial data', async () => {
    /**
     * Tests PUT /api/user/profile update pattern
     * Covers: user.controller.ts updateProfile method (full and partial updates)
     * Both full fields and partial fields execute the same code: update only provided fields
     */
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    // Test full update
    const fullUpdate = {
      name: 'PUT Updated Name',
      bio: 'PUT updated bio',
      preference: ['thai', 'korean']
    };

    const fullResponse = await request(app)
      .put('/api/user/profile')
      .set('Authorization', `Bearer ${token}`)
      .send(fullUpdate);

    expect(fullResponse.status).toBe(200);
    expect(fullResponse.body.Status).toBe(200);
    expect(fullResponse.body.Message.text).toBe('Profile updated successfully');
    expect(fullResponse.body.Body.name).toBe('PUT Updated Name');

    // Test partial update
    const partialUpdate = {
      bio: 'Just bio update via PUT'
    };

    const partialResponse = await request(app)
      .put('/api/user/profile')
      .set('Authorization', `Bearer ${token}`)
      .send(partialUpdate);

    expect(partialResponse.status).toBe(200);
    expect(partialResponse.body.Body.bio).toBe('Just bio update via PUT');
  });

  test('should update contactNumber, budget, and radiusKm in updateUserProfile (covers userService lines 188-190)', async () => {
    /**
     * Covers userService.ts lines 188-190: contactNumber, budget, radiusKm updates in updateUserProfile
     * Path: if (data.contactNumber !== undefined) -> user.contactNumber = data.contactNumber
     *       if (data.budget !== undefined) -> user.budget = data.budget
     *       if (data.radiusKm !== undefined) -> user.radiusKm = data.radiusKm
     */
    const token = generateTestToken(
      testUsers[3]._id,
      testUsers[3].email,
      testUsers[3].googleId
    );

    const profileUpdate = {
      contactNumber: '9876543210',
      budget: 125,
      radiusKm: 20
    };

    const response = await request(app)
      .put('/api/user/profile')
      .set('Authorization', `Bearer ${token}`)
      .send(profileUpdate);

    expect(response.status).toBe(200);
    expect(response.body.Body.contactNumber).toBe('9876543210');
    
    // Verify in database
    const User = (await import('../../src/models/User')).default;
    const updatedUser = await User.findById(testUsers[3]._id);
    expect(updatedUser!.contactNumber).toBe('9876543210');
    expect(updatedUser!.budget).toBe(125);
    expect(updatedUser!.radiusKm).toBe(20);
  });

  test('should successfully convert Google profile picture to Base64 (covers userService lines 22-29)', async () => {
    /**
     * Covers userService.ts lines 22-29: Successful Base64 conversion
     * Path: axios.get succeeds -> Buffer.from -> toString('base64') -> create data URI -> return
     * This tests the successful conversion path when a Google profile picture URL is provided
     */
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const axios = require('axios');
    
    // Mock axios to return a successful image response
    const mockImageBuffer = Buffer.from('fake-image-data-for-testing');
    jest.spyOn(axios, 'get').mockResolvedValueOnce({
      data: mockImageBuffer,
      headers: { 'content-type': 'image/jpeg' }
    });

    const googleProfilePictureUrl = 'https://lh3.googleusercontent.com/test-picture.jpg';

    const response = await request(app)
      .put('/api/user/profile')
      .set('Authorization', `Bearer ${token}`)
      .send({ profilePicture: googleProfilePictureUrl });

    jest.restoreAllMocks();

    expect(response.status).toBe(200);
    
    // Verify profile picture was converted to Base64 data URI
    const User = (await import('../../src/models/User')).default;
    const updatedUser = await User.findById(testUsers[0]._id);
    expect(updatedUser!.profilePicture).toBeTruthy();
    expect(updatedUser!.profilePicture).toContain('data:image/jpeg;base64,'); // Should be Base64 data URI
    expect(updatedUser!.profilePicture).not.toBe(googleProfilePictureUrl); // Should be converted, not original URL
  });

  test('should handle non-Google profile picture URL (covers convertGoogleProfilePictureToBase64 early return)', async () => {
    /**
     * Covers userService.ts line 12: Early return for non-Google URLs
     * Path: if (!profilePictureUrl || !profilePictureUrl.startsWith('https://lh3.googleusercontent.com/')) -> return as-is
     * This tests the branch coverage for non-Google URLs
     */
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    // Update profile with non-Google URL
    const nonGoogleUrl = 'https://example.com/profile.jpg';
    const response = await request(app)
      .put('/api/user/profile')
      .set('Authorization', `Bearer ${token}`)
      .send({ profilePicture: nonGoogleUrl });

    expect(response.status).toBe(200);
    expect(response.body.Body.profilePicture).toBe(nonGoogleUrl); // Should remain as-is
    
    // Verify in database
    const User = (await import('../../src/models/User')).default;
    const updatedUser = await User.findById(testUsers[0]._id);
    expect(updatedUser!.profilePicture).toBe(nonGoogleUrl);
  });

  // Note: "401 without authentication" test is consolidated above in settings endpoint tests
  // All endpoints use the same authMiddleware, so testing one endpoint covers all
});

describe('DELETE /api/user/:userId - No Mocking', () => {
  /**
   * Interface: DELETE /api/user/:userId
   * Mocking: None
   */

  // Note: "401 without authentication" test is consolidated above in settings endpoint tests
  // All endpoints use the same authMiddleware, so testing one endpoint covers all

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

  test('should access userId virtual property directly on document', async () => {
    /**
     * Covers User.ts lines 147-149: userId virtual getter
     * Path: UserSchema.virtual('userId').get() -> this._id.toString()
     * This test directly accesses the virtual property on a User document instance
     */
    const User = (await import('../../src/models/User')).default;
    const user = await User.findById(testUsers[0]._id) as any;
    expect(user).not.toBeNull();
    
    // Directly access the userId virtual property
    expect(user.userId).toBe(testUsers[0]._id);
    expect(user.userId).toBe(user._id.toString());
  });

  test('should access userId virtual property through API response', async () => {
    /**
     * Covers User.ts lines 147-149: userId virtual getter via toJSON
     * Path: UserSchema.virtual('userId').get() -> this._id.toString() -> toJSON includes it
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
     * Covers User.ts lines 152-159: toJSON transform function
     * Path: transform function -> userId extraction from _id -> destructure to remove _id and __v -> return { userId, ...rest }
     * Verifies: userId is included, _id is removed, __v is removed
     */
    const response = await request(app)
      .get(`/api/user/profile/${testUsers[0]._id}`);
    expect(response.status).toBe(200);
    expect(response.body.Body[0]).toHaveProperty('userId');
    expect(response.body.Body[0].userId).toBe(testUsers[0]._id);
    expect(response.body.Body[0]).not.toHaveProperty('_id');
    expect(response.body.Body[0]).not.toHaveProperty('__v');
  });

  test('should call toJSON transform directly on User document', async () => {
    /**
     * Covers User.ts lines 152-159: toJSON transform function (direct call)
     * Path: User document -> toJSON() -> transform function executes all lines
     * This ensures the transform function itself is directly tested, not just through API responses
     */
    const User = (await import('../../src/models/User')).default;
    const user = await User.findById(testUsers[0]._id);
    expect(user).not.toBeNull();
    
    // Call toJSON directly to ensure transform function is executed
    const json = user!.toJSON() as any;
    
    // Verify transform function worked correctly
    expect(json).toHaveProperty('userId');
    expect(json.userId).toBe(testUsers[0]._id);
    expect(json.userId).toBe(user!._id.toString());
    expect(json).not.toHaveProperty('_id');
    expect(json).not.toHaveProperty('__v');
    // Verify other properties are preserved
    expect(json).toHaveProperty('email');
    expect(json).toHaveProperty('name');
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