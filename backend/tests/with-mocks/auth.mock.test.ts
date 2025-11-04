// tests/with-mocks/auth.mock.test.ts

/**
 * Auth Routes Tests - With Mocking
 * Tests error scenarios and edge cases by mocking database and services
 * 
 * PURPOSE: Test how the application handles failures, errors, and edge cases
 * that are difficult or impossible to reproduce with a real database.
 */

// ============================================
// MOCK ALL DEPENDENCIES (before imports)
// ============================================

// Mock the User model (database)
jest.mock('../../src/models/User');

// Create a shared mock object that will store the mock functions
const authServiceMocks = {
  verifyGoogleToken: jest.fn(),
  findOrCreateUser: jest.fn(),
};

// Mock the AuthService (for route tests)
jest.mock('../../src/services/authService', () => ({
  __esModule: true,
  AuthService: jest.fn().mockImplementation(() => ({
    verifyGoogleToken: authServiceMocks.verifyGoogleToken,
    findOrCreateUser: authServiceMocks.findOrCreateUser,
  })),
}));

// Mock Google OAuth client (for direct service tests)
jest.mock('google-auth-library', () => ({
  OAuth2Client: jest.fn().mockImplementation(() => ({
    verifyIdToken: jest.fn(),
  }))
}));

// Mock axios for profile picture conversion
jest.mock('axios');

// ============================================
// IMPORT EVERYTHING
// ============================================

import request from 'supertest';
import app from '../../src/app';
import User from '../../src/models/User';
import { generateTestToken } from '../helpers/auth.helper';

// Get typed mocks
const mockedUser = User as jest.Mocked<typeof User>;

/**
 * WHAT ARE WE TESTING?
 * 
 * In with-mocks tests, we simulate failures and errors to verify:
 * 1. Error handling works correctly
 * 2. Appropriate error messages are returned
 * 3. Application doesn't crash on errors
 * 4. Edge cases are handled properly
 * 
 * We mock the service and database to simulate:
 * - Google OAuth verification failures
 * - Database connection errors
 * - Query errors
 * - User save/delete failures
 * - Service method failures
 */

describe('POST /api/auth/signup - With Mocking', () => {
  /**
   * Interface: POST /api/auth/signup
   * Mocking: AuthService, User model, Google OAuth
   */

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('should return 500 when AuthService.verifyGoogleToken throws error', async () => {
    /**
     * SCENARIO: Google token verification fails
     * 
     * Input: POST /api/auth/signup with invalid idToken
     * Expected Status Code: 500
     * Expected Output: Error message
     * 
     * Expected Behavior:
     *   - Validate idToken exists ✓
     *   - Call authService.verifyGoogleToken(idToken)
     *   - Google token verification fails
     *   - Service throws error
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - authService.verifyGoogleToken() rejects with Error('Invalid Google token')
     * 
     * WHY THIS TEST: Verifies error handling when Google authentication fails
     */

    authServiceMocks.verifyGoogleToken.mockRejectedValue(new Error('Invalid Google token'));

    const response = await request(app)
      .post('/api/auth/signup')
      .send({
        idToken: 'invalid-google-token'
      });

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Invalid Google token');
  });

  test('should return 500 when User.findOne() fails', async () => {
    /**
     * SCENARIO: Database error when checking if user exists
     * 
     * Input: POST /api/auth/signup with valid idToken
     * Expected Status Code: 500
     * Expected Output: Database error
     * 
     * Expected Behavior:
     *   - Verify Google token succeeds ✓
     *   - Try to check if user exists: User.findOne()
     *   - Database query fails
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - authService.verifyGoogleToken() resolves successfully
     *   - User.findOne() rejects with database error
     * 
     * WHY THIS TEST: Verifies error handling when database connection fails during signup
     */

    authServiceMocks.verifyGoogleToken.mockResolvedValue({
      googleId: 'google-123',
      email: 'test@example.com',
      name: 'Test User'
    });
    mockedUser.findOne.mockRejectedValue(new Error('Database connection failed'));

    const response = await request(app)
      .post('/api/auth/signup')
      .send({
        idToken: 'valid-google-token'
      });

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Database connection failed');
  });

  test('should return 500 when AuthService.findOrCreateUser throws error', async () => {
    /**
     * SCENARIO: Error when creating new user
     * 
     * Input: POST /api/auth/signup with valid new user token
     * Expected Status Code: 500
     * Expected Output: User creation error
     * 
     * Expected Behavior:
     *   - Verify token succeeds ✓
     *   - User doesn't exist ✓
     *   - Try to create user: authService.findOrCreateUser()
     *   - User creation fails
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - authService.verifyGoogleToken() succeeds
     *   - User.findOne() resolves to null
     *   - authService.findOrCreateUser() rejects with error
     * 
     * WHY THIS TEST: Verifies error handling when user creation fails
     */

    authServiceMocks.verifyGoogleToken.mockResolvedValue({
      googleId: 'google-new-123',
      email: 'new@example.com',
      name: 'New User'
    });
    mockedUser.findOne.mockResolvedValue(null);
    authServiceMocks.findOrCreateUser.mockRejectedValue(new Error('Failed to create user'));

    const response = await request(app)
      .post('/api/auth/signup')
      .send({
        idToken: 'valid-google-token-new'
      });

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Failed to create user');
  });
});

describe('POST /api/auth/signin - With Mocking', () => {
  /**
   * Interface: POST /api/auth/signin
   * Mocking: AuthService, User model
   */

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('should return 500 when AuthService.verifyGoogleToken throws error', async () => {
    /**
     * SCENARIO: Google token verification fails during signin
     * 
     * Input: POST /api/auth/signin with invalid idToken
     * Expected Status Code: 500
     * Expected Output: Token verification error
     * 
     * Expected Behavior:
     *   - Validate idToken exists ✓
     *   - Call authService.verifyGoogleToken(idToken)
     *   - Service throws error
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - authService.verifyGoogleToken() rejects with Error('Invalid token')
     * 
     * WHY THIS TEST: Verifies error handling when Google authentication fails during signin
     */

    authServiceMocks.verifyGoogleToken.mockRejectedValue(new Error('Invalid token'));

    const response = await request(app)
      .post('/api/auth/signin')
      .send({
        idToken: 'invalid-token'
      });

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Invalid token');
  });

  test('should return 500 when User.findOne() fails', async () => {
    /**
     * SCENARIO: Database error when finding user during signin
     * 
     * Input: POST /api/auth/signin with valid idToken
     * Expected Status Code: 500
     * Expected Output: Database error
     * 
     * Expected Behavior:
     *   - Verify token succeeds ✓
     *   - Try to find user: User.findOne()
     *   - Database query fails
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - authService.verifyGoogleToken() succeeds
     *   - User.findOne() rejects with database error
     * 
     * WHY THIS TEST: Verifies error handling when database connection fails during signin
     */

    authServiceMocks.verifyGoogleToken.mockResolvedValue({
      googleId: 'google-123',
      email: 'test@example.com',
      name: 'Test User'
    });
    mockedUser.findOne.mockRejectedValue(new Error('Database connection lost'));

    const response = await request(app)
      .post('/api/auth/signin')
      .send({
        idToken: 'valid-google-token'
      });

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Database connection lost');
  });
});

describe('POST /api/auth/logout - With Mocking', () => {
  /**
   * Interface: POST /api/auth/logout
   * Mocking: User model errors
   */

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('should return 500 when user save fails', async () => {
    /**
     * SCENARIO: Database error when updating user status to offline
     * 
     * Input: POST /api/auth/logout with valid token
     * Expected Status Code: 500
     * Expected Output: Database error
     * 
     * Expected Behavior:
     *   - Auth succeeds ✓
     *   - Find user successfully
     *   - Try to save user (update status to OFFLINE)
     *   - user.save() fails
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - User.findById() returns mock user
     *   - user.save() throws error
     * 
     * WHY THIS TEST: Verifies error handling when updating user status fails
     */

    const mockUser = {
      _id: 'test-user-id-123',
      status: 'ONLINE',
      save: jest.fn().mockRejectedValue(new Error('Save failed'))
    };

    mockedUser.findById.mockResolvedValue(mockUser as any);

    const token = generateTestToken('test-user-id-123');

    const response = await request(app)
      .post('/api/auth/logout')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Save failed');
  });

  test('should return 500 when database query fails', async () => {
    /**
     * SCENARIO: Database error when finding user
     * 
     * Input: POST /api/auth/logout with valid token
     * Expected Status Code: 500
     * Expected Output: Database error
     * 
     * Expected Behavior:
     *   - Auth succeeds ✓
     *   - Try to find user
     *   - Database query fails
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - User.findById() throws error
     * 
     * WHY THIS TEST: Verifies error handling when database connection fails
     */

    mockedUser.findById.mockRejectedValue(new Error('Database connection lost'));

    const token = generateTestToken('test-user-id-123');

    const response = await request(app)
      .post('/api/auth/logout')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Database connection lost');
  });
});

describe('GET /api/auth/verify - With Mocking', () => {
  /**
   * Interface: GET /api/auth/verify
   * Mocking: User model errors
   */

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('should return 500 when database query fails', async () => {
    /**
     * SCENARIO: Database error when finding user
     * 
     * Input: GET /api/auth/verify with valid token
     * Expected Status Code: 500
     * Expected Output: Database error
     * 
     * Expected Behavior:
     *   - Auth succeeds ✓
     *   - Try to find user
     *   - Database query fails
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - User.findById() throws error
     * 
     * WHY THIS TEST: Verifies error handling when database connection fails during verification
     */

    mockedUser.findById.mockRejectedValue(new Error('Database connection failed'));

    const token = generateTestToken('test-user-id-123');

    const response = await request(app)
      .get('/api/auth/verify')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Database connection failed');
  });
});

describe('POST /api/auth/fcm-token - With Mocking', () => {
  /**
   * Interface: POST /api/auth/fcm-token
   * Mocking: User model errors
   */

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('should return 500 when user save fails', async () => {
    /**
     * SCENARIO: Database error when saving FCM token
     * 
     * Input: POST /api/auth/fcm-token with fcmToken
     * Expected Status Code: 500
     * Expected Output: Save error
     * 
     * Expected Behavior:
     *   - Auth succeeds ✓
     *   - Find user successfully
     *   - Update fcmToken
     *   - user.save() fails
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - User.findById() returns mock user
     *   - user.save() throws error
     * 
     * WHY THIS TEST: Verifies error handling when FCM token update fails
     */

    const mockUser = {
      _id: 'test-user-id-123',
      fcmToken: '',
      save: jest.fn().mockRejectedValue(new Error('Failed to save FCM token'))
    };

    mockedUser.findById.mockResolvedValue(mockUser as any);

    const token = generateTestToken('test-user-id-123');

    const response = await request(app)
      .post('/api/auth/fcm-token')
      .set('Authorization', `Bearer ${token}`)
      .send({ fcmToken: 'new-fcm-token' });

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Failed to save FCM token');
  });

  test('should return 500 when database query fails', async () => {
    /**
     * SCENARIO: Database error when finding user
     * 
     * Input: POST /api/auth/fcm-token with valid token
     * Expected Status Code: 500
     * Expected Output: Database error
     * 
     * Expected Behavior:
     *   - Auth succeeds ✓
     *   - Try to find user
     *   - Database query fails
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - User.findById() throws error
     * 
     * WHY THIS TEST: Verifies error handling when database connection fails
     */

    mockedUser.findById.mockRejectedValue(new Error('Connection lost'));

    const token = generateTestToken('test-user-id-123');

    const response = await request(app)
      .post('/api/auth/fcm-token')
      .set('Authorization', `Bearer ${token}`)
      .send({ fcmToken: 'test-token' });

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Connection lost');
  });
});

describe('DELETE /api/auth/account - With Mocking', () => {
  /**
   * Interface: DELETE /api/auth/account
   * Mocking: User model errors
   */

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('should return 500 when database delete fails', async () => {
    /**
     * SCENARIO: Database error when deleting user account
     * 
     * Input: DELETE /api/auth/account with valid token
     * Expected Status Code: 500
     * Expected Output: Delete error
     * 
     * Expected Behavior:
     *   - Auth succeeds ✓
     *   - Find user successfully
     *   - User has no roomId/groupId
     *   - Try to delete user
     *   - User.findByIdAndDelete() fails
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - User.findById() returns valid user (no roomId/groupId)
     *   - User.findByIdAndDelete() throws error
     * 
     * WHY THIS TEST: Verifies error handling when account deletion fails
     */

    const mockUser = {
      _id: 'test-user-id-123',
      roomId: null,
      groupId: null
    };

    mockedUser.findById.mockResolvedValue(mockUser as any);
    mockedUser.findByIdAndDelete.mockRejectedValue(new Error('Delete operation failed'));

    const token = generateTestToken('test-user-id-123');

    const response = await request(app)
      .delete('/api/auth/account')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Delete operation failed');
  });

  test('should return 500 when database query fails', async () => {
    /**
     * SCENARIO: Database error when finding user
     * 
     * Input: DELETE /api/auth/account with valid token
     * Expected Status Code: 500
     * Expected Output: Database error
     * 
     * Expected Behavior:
     *   - Auth succeeds ✓
     *   - Try to find user
     *   - Database query fails
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - User.findById() throws error
     * 
     * WHY THIS TEST: Verifies error handling when database connection fails
     */

    mockedUser.findById.mockRejectedValue(new Error('Database connection lost'));

    const token = generateTestToken('test-user-id-123');

    const response = await request(app)
      .delete('/api/auth/account')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Database connection lost');
  });
});

