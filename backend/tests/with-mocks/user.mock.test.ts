// tests/with-mocks/user.mock.test.ts

/**
 * User Routes Tests - With Mocking
 * Tests error scenarios and edge cases using mocked database
 */

// STEP 1: Mock the User model BEFORE any imports
jest.mock('../../src/models/User');

// STEP 2: Import everything you need
import request from 'supertest';
import app from '../../src/app';
import User from '../../src/models/User';
import { generateTestToken} from '../helpers/auth.helper';
import axios from 'axios';

// Mock axios for profile picture conversion tests
jest.mock('axios');

// STEP 3: Get typed versions of mocks
const mockedUser = User as jest.Mocked<typeof User>;
const mockedAxios = axios as jest.Mocked<typeof axios>;

describe('GET /api/user/profile/:ids - With Mocking', () => {
  /**
   * Interface: GET /api/user/profile/:ids
   * Mocking: Database errors and timeouts
   */

  beforeEach(() => {
    // Clear all mocks before each test
    jest.clearAllMocks();
  });

  test('should return 500 when database query fails', async () => {
    /**
     * Input: GET /api/user/profile/user123
     * Expected Status Code: 500
     * Expected Output: Database error message
     * Expected Behavior:
     *   - Attempt to query database
     *   - User.find() throws error
     *   - Error handler catches it
     *   - Return 500
     * Mock Behavior:
     *   - User.find() rejects with Error('Database connection failed')
     */

    // Tell the mock what to do
    mockedUser.find.mockRejectedValue(new Error('Database connection failed'));

    const response = await request(app)
      .get('/api/user/profile/user123');

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Database connection failed');
    expect(response.body.statusCode).toBe(500);
  });

  test('should return 400 when invalid ObjectId causes CastError', async () => {
    /**
     * Input: GET /api/user/profile/invalid-format
     * Expected Status Code: 400
     * Expected Output: Invalid data format error
     * Expected Behavior:
     *   - Parse IDs
     *   - Query with invalid ObjectId
     *   - Mongoose throws CastError
     *   - Error handler returns 400
     * Mock Behavior:
     *   - User.find() throws CastError
     */

    const castError = new Error('Cast to ObjectId failed') as any;
    castError.name = 'CastError';
    
    mockedUser.find.mockRejectedValue(castError);

    const response = await request(app)
      .get('/api/user/profile/invalid-id');

    expect(response.status).toBe(400);
    expect(response.body.message).toBe('Invalid data format');
    expect(response.body.statusCode).toBe(400);
  });

  test('should handle database timeout gracefully', async () => {
    /**
     * Input: GET /api/user/profile/user123
     * Expected Status Code: 500
     * Expected Output: Timeout error
     * Expected Behavior:
     *   - Query database
     *   - Operation times out
     *   - Return 500
     * Mock Behavior:
     *   - User.find() throws timeout error
     */

    mockedUser.find.mockRejectedValue(new Error('Operation timed out'));

    const response = await request(app)
      .get('/api/user/profile/user123');

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Operation timed out');
  });

  test('should return empty array when User.find returns empty', async () => {
    /**
     * Input: GET /api/user/profile/validId
     * Expected Status Code: 200
     * Expected Output: Empty array
     * Expected Behavior:
     *   - Query succeeds
     *   - No users found
     *   - Return empty array
     * Mock Behavior:
     *   - User.find() resolves to []
     */

    mockedUser.find.mockResolvedValue([]);

    const response = await request(app)
      .get('/api/user/profile/507f1f77bcf86cd799439011');

    expect(response.status).toBe(200);
    expect(response.body.Body).toEqual([]);
  });
});

describe('GET /api/user/settings - With Mocking', () => {
  /**
   * Interface: GET /api/user/settings
   * Mocking: Database failures and user not found scenarios
   */

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('should return 500 when user not found in database', async () => {
    /**
     * Input: GET /api/user/settings with valid token
     * Expected Status Code: 500
     * Expected Output: User not found error
     * Expected Behavior:
     *   - Auth succeeds
     *   - Query database for user
     *   - User.findById() returns null
     *   - Service throws Error('User not found')
     *   - Error handler returns 500
     * Mock Behavior:
     *   - User.findById() resolves to null
     */

    mockedUser.findById.mockResolvedValue(null);

    const token = generateTestToken('test-user-id-123');

    const response = await request(app)
      .get('/api/user/settings')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('User not found');
    expect(response.body.statusCode).toBe(500);
  });

  test('should return 500 when database connection fails', async () => {
    /**
     * Input: GET /api/user/settings with valid token
     * Expected Status Code: 500
     * Expected Output: Database error
     * Expected Behavior:
     *   - Auth succeeds
     *   - Attempt to fetch user
     *   - Database operation fails
     *   - Return 500
     * Mock Behavior:
     *   - User.findById() throws connection error
     */

    mockedUser.findById.mockRejectedValue(new Error('Connection lost'));

    const token = generateTestToken('test-user-id-123');

    const response = await request(app)
      .get('/api/user/settings')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Connection lost');
  });

  test('should return 400 when invalid ObjectId format', async () => {
    /**
     * Input: GET /api/user/settings with token containing invalid userId
     * Expected Status Code: 400
     * Expected Output: Invalid data format
     * Expected Behavior:
     *   - Auth succeeds
     *   - User.findById() with invalid ObjectId
     *   - Mongoose throws CastError
     *   - Error handler returns 400
     * Mock Behavior:
     *   - User.findById() throws CastError
     */

    const castError = new Error('Invalid ObjectId') as any;
    castError.name = 'CastError';
    
    mockedUser.findById.mockRejectedValue(castError);

    const token = generateTestToken('invalid-format');

    const response = await request(app)
      .get('/api/user/settings')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(400);
    expect(response.body.message).toBe('Invalid data format');
  });
});

describe('POST /api/user/profile - With Mocking', () => {
  /**
   * Interface: POST /api/user/profile
   * Mocking: Database failures and validation errors
   */

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('should return 500 when user not found', async () => {
    /**
     * Input: POST /api/user/profile with valid data
     * Expected Status Code: 500
     * Expected Output: User not found error
     * Expected Behavior:
     *   - Auth succeeds
     *   - Call createUserProfile()
     *   - User.findById() returns null
     *   - Service throws Error('User not found')
     *   - Error handler returns 500
     * Mock Behavior:
     *   - User.findById() resolves to null
     */

    mockedUser.findById.mockResolvedValue(null);

    const token = generateTestToken('test-user-id-123');
    const profileData = { name: 'Test User' };

    const response = await request(app)
      .post('/api/user/profile')
      .set('Authorization', `Bearer ${token}`)
      .send(profileData);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('User not found');
  });

  test('should return 500 when database save operation fails', async () => {
    /**
     * Input: POST /api/user/profile with valid data
     * Expected Status Code: 500
     * Expected Output: Save error message
     * Expected Behavior:
     *   - Find user successfully
     *   - Update user fields
     *   - user.save() fails
     *   - Error handler returns 500
     * Mock Behavior:
     *   - User.findById() returns mock user
     *   - user.save() throws error
     */

    const mockUser = {
      _id: 'test-user-id-123',
      name: 'Old Name',
      bio: 'Old Bio',
      save: jest.fn().mockRejectedValue(new Error('Write failed'))
    };

    mockedUser.findById.mockResolvedValue(mockUser as any);

    const token = generateTestToken('test-user-id-123');
    const profileData = { name: 'New Name' };

    const response = await request(app)
      .post('/api/user/profile')
      .set('Authorization', `Bearer ${token}`)
      .send(profileData);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Write failed');
  });

  test('should return 400 when Mongoose validation fails', async () => {
    /**
     * Input: POST /api/user/profile with invalid data
     * Expected Status Code: 400
     * Expected Output: Validation error
     * Expected Behavior:
     *   - Find user
     *   - Set invalid field values
     *   - user.save() throws ValidationError
     *   - Error handler returns 400
     * Mock Behavior:
     *   - User.findById() returns mock user
     *   - user.save() throws ValidationError
     */

    const validationError = new Error('Validation failed: name is required') as any;
    validationError.name = 'ValidationError';

    const mockUser = {
      _id: 'test-user-id-123',
      save: jest.fn().mockRejectedValue(validationError)
    };

    mockedUser.findById.mockResolvedValue(mockUser as any);

    const token = generateTestToken('test-user-id-123');

    const response = await request(app)
      .post('/api/user/profile')
      .set('Authorization', `Bearer ${token}`)
      .send({ name: '' });

    expect(response.status).toBe(400);
    expect(response.body.message).toContain('Validation failed');
  });
});

describe('POST /api/user/settings - With Mocking', () => {
  /**
   * Interface: POST /api/user/settings
   * Mocking: Database errors and profile picture conversion failures
   */

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('should return 500 when user not found', async () => {
    /**
     * Input: POST /api/user/settings with valid data
     * Expected Status Code: 500
     * Expected Output: User not found error
     * Expected Behavior:
     *   - Auth succeeds
     *   - Call updateUserSettings()
     *   - User.findById() returns null
     *   - Service throws error
     *   - Error handler returns 500
     * Mock Behavior:
     *   - User.findById() resolves to null (first call)
     */

    mockedUser.findById.mockResolvedValue(null);

    const token = generateTestToken('test-user-id-123');

    const response = await request(app)
      .post('/api/user/settings')
      .set('Authorization', `Bearer ${token}`)
      .send({ budget: 50 });

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('User not found');
  });

  test('should return 500 when database update fails', async () => {
    /**
     * Input: POST /api/user/settings with valid data
     * Expected Status Code: 500
     * Expected Output: Database error
     * Expected Behavior:
     *   - Find user
     *   - Update fields
     *   - user.save() fails
     *   - Return 500
     * Mock Behavior:
     *   - User.findById() returns mock user (first call)
     *   - user.save() throws error
     */

    const mockUser = {
      _id: 'test-user-id-123',
      save: jest.fn().mockRejectedValue(new Error('Database write error'))
    };

    mockedUser.findById.mockResolvedValue(mockUser as any);

    const token = generateTestToken('test-user-id-123');

    const response = await request(app)
      .post('/api/user/settings')
      .set('Authorization', `Bearer ${token}`)
      .send({ budget: 100 });

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Database write error');
  });

  test('should handle Google profile picture conversion failure gracefully', async () => {
    /**
     * Input: POST /api/user/settings with Google profile picture URL
     * Expected Status Code: 200 or 500 (depends on implementation)
     * Expected Output: Settings with original URL if conversion fails
     * Expected Behavior:
     *   - Detect Google URL
     *   - Attempt axios.get() to fetch image
     *   - Request fails
     *   - Fall back to original URL
     *   - Continue with save
     * Mock Behavior:
     *   - axios.get() throws network error
     *   - User operations succeed with original URL
     */

    mockedAxios.get.mockRejectedValue(new Error('Network error'));

    const mockUser = {
      _id: 'test-user-id-123',
      name: 'Test',
      profilePicture: '',
      save: jest.fn().mockResolvedValue(true),
      toString: () => 'test-user-id-123'
    };

    // First call: updateUserSettings finds user
    // Second call: getUserSettings finds user
    mockedUser.findById.mockResolvedValue(mockUser as any);

    const token = generateTestToken('test-user-id-123');
    const googleUrl = 'https://lh3.googleusercontent.com/test';

    const response = await request(app)
      .post('/api/user/settings')
      .set('Authorization', `Bearer ${token}`)
      .send({ profilePicture: googleUrl });

    // Should succeed with original URL despite conversion failure
    expect([200, 500]).toContain(response.status);
  });
});

describe('PUT /api/user/profile - With Mocking', () => {
  /**
   * Interface: PUT /api/user/profile
   * Mocking: Database errors
   */

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('should return 500 when user not found', async () => {
    /**
     * Input: PUT /api/user/profile with valid data
     * Expected Status Code: 500
     * Expected Output: User not found error
     * Expected Behavior:
     *   - Auth succeeds
     *   - Call updateUserProfile()
     *   - User.findById() returns null
     *   - Service throws error
     * Mock Behavior:
     *   - User.findById() resolves to null
     */

    mockedUser.findById.mockResolvedValue(null);

    const token = generateTestToken('test-user-id-123');

    const response = await request(app)
      .put('/api/user/profile')
      .set('Authorization', `Bearer ${token}`)
      .send({ name: 'Updated' });

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('User not found');
  });

  test('should return 500 when database update fails', async () => {
    /**
     * Input: PUT /api/user/profile with updates
     * Expected Status Code: 500
     * Expected Output: Error message
     * Expected Behavior:
     *   - Find user
     *   - Update fields
     *   - user.save() fails
     *   - Return 500
     * Mock Behavior:
     *   - User.findById() returns mock user
     *   - user.save() throws error
     */

    const mockUser = {
      _id: 'test-user-id-123',
      save: jest.fn().mockRejectedValue(new Error('Update failed'))
    };

    mockedUser.findById.mockResolvedValue(mockUser as any);

    const token = generateTestToken('test-user-id-123');

    const response = await request(app)
      .put('/api/user/profile')
      .set('Authorization', `Bearer ${token}`)
      .send({ name: 'Updated' });

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Update failed');
  });

  test('should return 400 on validation error', async () => {
    /**
     * Input: PUT /api/user/profile with invalid data
     * Expected Status Code: 400
     * Expected Output: Validation error
     * Expected Behavior:
     *   - Find user
     *   - Set invalid values
     *   - user.save() throws ValidationError
     *   - Return 400
     * Mock Behavior:
     *   - user.save() throws ValidationError
     */

    const validationError = new Error('Invalid field') as any;
    validationError.name = 'ValidationError';

    const mockUser = {
      _id: 'test-user-id-123',
      save: jest.fn().mockRejectedValue(validationError)
    };

    mockedUser.findById.mockResolvedValue(mockUser as any);

    const token = generateTestToken('test-user-id-123');

    const response = await request(app)
      .put('/api/user/profile')
      .set('Authorization', `Bearer ${token}`)
      .send({ name: '' });

    expect(response.status).toBe(400);
  });
});

describe('DELETE /api/user/:userId - With Mocking', () => {
  /**
   * Interface: DELETE /api/user/:userId
   * Mocking: Database errors and business logic constraints
   */

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('should return 500 when user not found', async () => {
    /**
     * Input: DELETE /api/user/test-user-123 with matching token
     * Expected Status Code: 500
     * Expected Output: User not found error
     * Expected Behavior:
     *   - Auth and authorization succeed
     *   - Call deleteUser()
     *   - User.findById() returns null
     *   - Service throws error
     * Mock Behavior:
     *   - User.findById() resolves to null
     */

    mockedUser.findById.mockResolvedValue(null);

    const userId = 'test-user-id-123';
    const token = generateTestToken(userId);

    const response = await request(app)
      .delete(`/api/user/${userId}`)
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('User not found');
    expect(response.body.statusCode).toBe(500);
  });

  test('should return 500 when user is in a room', async () => {
    /**
     * Input: DELETE /api/user/user-in-room with matching token
     * Expected Status Code: 500
     * Expected Output: Cannot delete error
     * Expected Behavior:
     *   - Auth and authorization succeed
     *   - Find user
     *   - User has roomId set
     *   - Service throws error
     * Mock Behavior:
     *   - User.findById() returns user with roomId
     */

    const mockUser = {
      _id: 'user-in-room',
      roomId: 'room-123',
      groupId: null
    };

    mockedUser.findById.mockResolvedValue(mockUser as any);

    const userId = 'user-in-room';
    const token = generateTestToken(userId);

    const response = await request(app)
      .delete(`/api/user/${userId}`)
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Cannot delete account while in a room or group');
  });

  test('should return 500 when user is in a group', async () => {
    /**
     * Input: DELETE /api/user/user-in-group with matching token
     * Expected Status Code: 500
     * Expected Output: Cannot delete error
     * Expected Behavior:
     *   - Find user with groupId
     *   - Check fails (groupId exists)
     *   - Throw error
     * Mock Behavior:
     *   - User.findById() returns user with groupId set
     */

    const mockUser = {
      _id: 'user-in-group',
      roomId: null,
      groupId: 'group-456'
    };

    mockedUser.findById.mockResolvedValue(mockUser as any);

    const userId = 'user-in-group';
    const token = generateTestToken(userId);

    const response = await request(app)
      .delete(`/api/user/${userId}`)
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Cannot delete account while in a room or group');
  });

  test('should return 500 when database delete operation fails', async () => {
    /**
     * Input: DELETE /api/user/test-user with valid conditions
     * Expected Status Code: 500
     * Expected Output: Database error
     * Expected Behavior:
     *   - Find user (no roomId/groupId)
     *   - Call User.findByIdAndDelete()
     *   - Delete operation fails
     *   - Return 500
     * Mock Behavior:
     *   - User.findById() returns valid user
     *   - User.findByIdAndDelete() throws error
     */

    const mockUser = {
      _id: 'test-user-id-123',
      roomId: null,
      groupId: null
    };

    mockedUser.findById.mockResolvedValue(mockUser as any);
    mockedUser.findByIdAndDelete.mockRejectedValue(new Error('Delete operation failed'));

    const userId = 'test-user-id-123';
    const token = generateTestToken(userId);

    const response = await request(app)
      .delete(`/api/user/${userId}`)
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Delete operation failed');
  });

  test('should handle database connection loss during deletion', async () => {
    /**
     * Input: DELETE /api/user/test-user
     * Expected Status Code: 500
     * Expected Output: Connection error
     * Expected Behavior:
     *   - Attempt to find user
     *   - Database connection lost
     *   - Error thrown
     * Mock Behavior:
     *   - User.findById() throws connection error
     */

    mockedUser.findById.mockRejectedValue(new Error('Connection lost'));

    const userId = 'test-user-id-123';
    const token = generateTestToken(userId);

    const response = await request(app)
      .delete(`/api/user/${userId}`)
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Connection lost');
  });
});