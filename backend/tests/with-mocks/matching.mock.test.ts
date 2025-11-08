// tests/with-mocks/matching.mock.test.ts

/**
 * Matching Routes Tests - With Mocking
 * Tests error scenarios and edge cases by mocking database and infrastructure
 * 
 * PURPOSE: Test how the application handles failures, errors, and edge cases
 * that are difficult or impossible to reproduce with a real database.
 */

// ============================================
// MOCK ALL DEPENDENCIES (before imports)
// ============================================

// Mock the Room model (database)
jest.mock('../../src/models/Room');

// Mock the User model (database)
jest.mock('../../src/models/User');

// Mock the Group model (database)
jest.mock('../../src/models/Group');

// Mock socket infrastructure
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

// Mock notification service
jest.mock('../../src/services/notificationService', () => ({
  notifyRoomMatched: jest.fn(),
  notifyRoomExpired: jest.fn()
}));

// ============================================
// IMPORT EVERYTHING
// ============================================

import request from 'supertest';
import app from '../../src/app';
import { generateTestToken } from '../helpers/auth.helper';
import Room from '../../src/models/Room';
import User, { UserStatus } from '../../src/models/User';
import mongoose from 'mongoose';

// Get typed mocks
const mockedRoom = Room as jest.Mocked<typeof Room>;
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
 * We mock the database to simulate:
 * - Connection failures
 * - Query errors
 * - Data not found
 * - Validation errors
 * - Timeout errors
 */

describe('POST /api/matching/join - With Mocking', () => {
  /**
   * Interface: POST /api/matching/join
   * Mocking: Database (Room, User models)
   */

  beforeEach(() => {
    // Clear all mocks before each test
    jest.clearAllMocks();
  });

  test('should return 500 when user not found in database', async () => {
    /**
     * SCENARIO: User's token is valid, but user doesn't exist in database
     * 
     * Input: POST /api/matching/join with valid token
     * Expected Status Code: 500
     * Expected Output: 
     *   {
     *     error: "Error",
     *     message: "User not found",
     *     statusCode: 500
     *   }
     * 
     * Expected Behavior:
     *   - Auth middleware validates token ✓
     *   - Service tries to find user: User.findById(userId)
     *   - Database returns null (user doesn't exist)
     *   - Service throws Error('User not found')
     *   - Error handler catches it and returns 500
     * 
     * Mock Behavior:
     *   - User.findById() resolves to null
     * 
     * WHY THIS TEST: Verifies error handling when user account was deleted
     * but they still have a valid token
     */

    // Setup: Make User.findById return null
    mockedUser.findById.mockResolvedValue(null);

    const token = generateTestToken('test-user-123');

    const response = await request(app)
      .post('/api/matching/join')
      .set('Authorization', `Bearer ${token}`)
      .send({ cuisine: ['italian'], budget: 50 });

    // Verify error response
    expect(response.status).toBe(500);
    expect(response.body.message).toBe('User not found');
    expect(response.body.statusCode).toBe(500);

    // Verify User.findById was called
    expect(mockedUser.findById).toHaveBeenCalledWith('test-user-123');
  });

  test('should return 500 when database connection fails', async () => {
    /**
     * SCENARIO: Database connection is lost during operation
     * 
     * Input: POST /api/matching/join
     * Expected Status Code: 500
     * Expected Output: Connection error message
     * 
     * Expected Behavior:
     *   - Service tries to find user
     *   - Database connection fails
     *   - MongoDB throws connection error
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - User.findById() rejects with connection error
     * 
     * WHY THIS TEST: Verifies graceful handling of database failures
     */

    mockedUser.findById.mockRejectedValue(new Error('Database connection lost'));

    const token = generateTestToken('test-user-123');

    const response = await request(app)
      .post('/api/matching/join')
      .set('Authorization', `Bearer ${token}`)
      .send({ cuisine: ['italian'] });

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Database connection lost');
  });

  test('should return 500 when user already in room', async () => {
    /**
     * SCENARIO: User tries to join matching while already in a room
     * 
     * Input: POST /api/matching/join
     * Expected Status Code: 500
     * Expected Output: Already in room error
     * 
     * Expected Behavior:
     *   - Find user in database
     *   - User has roomId set
     *   - Service throws Error('User is already in a room or group')
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - User.findById() returns user with roomId set
     * 
     * WHY THIS TEST: Verifies business logic prevents double-joining
     */

    const mockUser = {
      _id: 'test-user-123',
      roomId: 'existing-room-id',  // User already in a room
      groupId: null,
      save: jest.fn()
    };

    mockedUser.findById.mockResolvedValue(mockUser as any);

    const token = generateTestToken('test-user-123');

    const response = await request(app)
      .post('/api/matching/join')
      .set('Authorization', `Bearer ${token}`)
      .send({ cuisine: ['italian'] });

    expect(response.status).toBe(500);
    expect(response.body.message).toMatch(/already in a room/i);
  });

  test('should return 500 when Room.find() fails', async () => {
    /**
     * SCENARIO: Finding matching rooms fails due to database error
     * 
     * Input: POST /api/matching/join
     * Expected Status Code: 500
     * Expected Output: Database error
     * 
     * Expected Behavior:
     *   - Find user ✓
     *   - Try to find matching rooms: Room.find()
     *   - Database query fails
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - User.findById() succeeds
     *   - Room.find() throws error
     * 
     * WHY THIS TEST: Verifies error handling during room search
     */

    const mockUser = {
      _id: 'test-user-123',
      roomId: null,
      groupId: null,
      budget: 50,
      radiusKm: 5,
      preference: ['italian'],
      save: jest.fn().mockResolvedValue(true)
    };

    mockedUser.findById.mockResolvedValue(mockUser as any);
    mockedRoom.find.mockRejectedValue(new Error('Query timeout'));

    const token = generateTestToken('test-user-123');

    const response = await request(app)
      .post('/api/matching/join')
      .set('Authorization', `Bearer ${token}`)
      .send({ cuisine: ['italian'] });

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Query timeout');
  });

  test('should return 500 when Room.create() fails', async () => {
    /**
     * SCENARIO: Creating new room fails due to database error
     * 
     * Input: POST /api/matching/join
     * Expected Status Code: 500
     * Expected Output: Database creation error
     * 
     * Expected Behavior:
     *   - Find user ✓
     *   - No matching rooms found
     *   - Try to create new room: Room.create()
     *   - Database create fails
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - User.findById() succeeds
     *   - Room.find() returns empty array (no matches)
     *   - Room.create() throws error
     * 
     * WHY THIS TEST: Verifies error handling when room creation fails
     */

    const mockUser = {
      _id: 'test-user-123',
      roomId: null,
      groupId: null,
      budget: 50,
      radiusKm: 5,
      preference: ['italian'],
      save: jest.fn().mockResolvedValue(true)
    };

    mockedUser.findById.mockResolvedValue(mockUser as any);
    mockedRoom.find.mockResolvedValue([]);  // No matching rooms
    mockedRoom.create.mockRejectedValue(new Error('Failed to create room'));

    const token = generateTestToken('test-user-123');

    const response = await request(app)
      .post('/api/matching/join')
      .set('Authorization', `Bearer ${token}`)
      .send({ cuisine: ['italian'] });

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Failed to create room');
  });

  test('should return 400 when invalid ObjectId format', async () => {
    /**
     * SCENARIO: Invalid user ID format causes CastError
     * 
     * Input: POST /api/matching/join with invalid userId in token
     * Expected Status Code: 400
     * Expected Output: Invalid data format error
     * 
     * Expected Behavior:
     *   - Service tries User.findById(invalidId)
     *   - Mongoose throws CastError
     *   - Error handler catches CastError
     *   - Returns 400 with "Invalid data format"
     * 
     * Mock Behavior:
     *   - User.findById() throws CastError
     * 
     * WHY THIS TEST: Verifies handling of malformed IDs
     */

    const castError = new Error('Cast to ObjectId failed') as any;
    castError.name = 'CastError';

    mockedUser.findById.mockRejectedValue(castError);

    const token = generateTestToken('invalid-id-format');

    const response = await request(app)
      .post('/api/matching/join')
      .set('Authorization', `Bearer ${token}`)
      .send({ cuisine: ['italian'] });

    expect(response.status).toBe(400);
    expect(response.body.message).toBe('Invalid data format');
  });

  test('should return 400 when user.save() fails with ValidationError', async () => {
    /**
     * SCENARIO: Saving user with invalid data fails validation
     * 
     * Input: POST /api/matching/join
     * Expected Status Code: 400
     * Expected Output: Validation error
     * 
     * Expected Behavior:
     *   - Find user ✓
     *   - Update user preferences
     *   - Try user.save()
     *   - Mongoose validation fails
     *   - Error handler returns 400
     * 
     * Mock Behavior:
     *   - User.findById() succeeds
     *   - user.save() throws ValidationError
     * 
     * WHY THIS TEST: Verifies validation error handling
     */

    const validationError = new Error('Validation failed: budget must be positive') as any;
    validationError.name = 'ValidationError';

    const mockUser = {
      _id: 'test-user-123',
      roomId: null,
      groupId: null,
      budget: -10,  // Invalid budget
      save: jest.fn().mockRejectedValue(validationError)
    };

    mockedUser.findById.mockResolvedValue(mockUser as any);

    const token = generateTestToken('test-user-123');

    const response = await request(app)
      .post('/api/matching/join')
      .set('Authorization', `Bearer ${token}`)
      .send({ cuisine: ['italian'], budget: -10 });

    expect(response.status).toBe(400);
    expect(response.body.message).toContain('Validation failed');
  });
});

describe('PUT /api/matching/leave/:roomId - With Mocking', () => {
  /**
   * Interface: PUT /api/matching/leave/:roomId
   * Mocking: Database (Room, User models)
   */

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('should return 500 when user not found', async () => {
    /**
     * SCENARIO: Valid token but user doesn't exist
     * 
     * Input: PUT /api/matching/leave/:roomId
     * Expected Status Code: 500
     * Expected Output: User not found error
     * 
     * Expected Behavior:
     *   - Auth succeeds
     *   - Service tries User.findById()
     *   - User doesn't exist
     *   - Throw error
     * 
     * Mock Behavior:
     *   - User.findById() returns null
     * 
     * WHY THIS TEST: Verifies error handling for deleted users
     */

    mockedUser.findById.mockResolvedValue(null);

    const roomId = new mongoose.Types.ObjectId().toString();
    const token = generateTestToken('test-user-123');

    const response = await request(app)
      .put(`/api/matching/leave/${roomId}`)
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('User not found');
  });

  test('should return 500 when Room.findById() fails', async () => {
    /**
     * SCENARIO: Database error when finding room
     * 
     * Input: PUT /api/matching/leave/:roomId
     * Expected Status Code: 500
     * Expected Output: Database error
     * 
     * Expected Behavior:
     *   - Find user ✓
     *   - Try Room.findById()
     *   - Database query fails
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - User.findById() succeeds
     *   - Room.findById() throws error
     * 
     * WHY THIS TEST: Verifies error handling during room lookup
     */

    const mockUser = {
      _id: 'test-user-123',
      roomId: 'room-123',
      save: jest.fn()
    };

    mockedUser.findById.mockResolvedValue(mockUser as any);
    mockedRoom.findById.mockRejectedValue(new Error('Database timeout'));

    const roomId = 'room-123';
    const token = generateTestToken('test-user-123');

    const response = await request(app)
      .put(`/api/matching/leave/${roomId}`)
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Database timeout');
  });

  test('should handle gracefully when room not found (cleanup stale state)', async () => {
    /**
     * SCENARIO: User has roomId but room doesn't exist in database
     * 
     * Input: PUT /api/matching/leave/:roomId
     * Expected Status Code: 200
     * Expected Output: Successfully left
     * 
     * Expected Behavior:
     *   - Find user ✓
     *   - Try to find room
     *   - Room is null (was deleted)
     *   - Clear user's roomId anyway (cleanup)
     *   - Return success
     * 
     * Mock Behavior:
     *   - User.findById() returns user with roomId
     *   - Room.findById() returns null
     *   - user.save() succeeds
     * 
     * WHY THIS TEST: Verifies graceful cleanup of stale references
     */

    const mockUser = {
      _id: 'test-user-123',
      roomId: 'non-existent-room',
      status: UserStatus.IN_WAITING_ROOM,
      save: jest.fn().mockImplementation(function(this: any) {
        this.roomId = null;
        this.status = UserStatus.ONLINE;
        return Promise.resolve(this);
      })
    };

    mockedUser.findById.mockResolvedValue(mockUser as any);
    mockedRoom.findById.mockResolvedValue(null);  // Room doesn't exist

    const roomId = 'non-existent-room';
    const token = generateTestToken('test-user-123');

    const response = await request(app)
      .put(`/api/matching/leave/${roomId}`)
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(200);
    expect(mockUser.save).toHaveBeenCalled();
    expect(mockUser.roomId).toBeNull();
    expect(mockUser.status).toBe(UserStatus.ONLINE);
  });

  test('should return 500 when room.save() fails', async () => {
    /**
     * SCENARIO: Saving updated room fails
     * 
     * Input: PUT /api/matching/leave/:roomId
     * Expected Status Code: 500
     * Expected Output: Save error
     * 
     * Expected Behavior:
     *   - Find user and room ✓
     *   - Remove user from room.members
     *   - Try room.save()
     *   - Save fails
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - User and Room found
     *   - room.save() throws error
     * 
     * WHY THIS TEST: Verifies error handling when updating room fails
     */

    const mockUser = {
      _id: 'test-user-123',
      name: 'Test User',
      roomId: 'room-123',
      status: UserStatus.IN_WAITING_ROOM,
      save: jest.fn().mockResolvedValue(true)
    };

    const mockRoom = {
      _id: 'room-123',
      members: ['test-user-123', 'other-user'],
      save: jest.fn().mockRejectedValue(new Error('Failed to update room'))
    };

    mockedUser.findById.mockResolvedValue(mockUser as any);
    mockedRoom.findById.mockResolvedValue(mockRoom as any);
    mockedUser.find.mockResolvedValue([mockUser] as any);  // For updateRoomAverages

    const roomId = 'room-123';
    const token = generateTestToken('test-user-123');

    const response = await request(app)
      .put(`/api/matching/leave/${roomId}`)
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Failed to update room');
  });

  test('should return 500 when Room.findByIdAndDelete() fails', async () => {
    /**
     * SCENARIO: Deleting empty room fails
     * 
     * Input: PUT /api/matching/leave/:roomId (last member)
     * Expected Status Code: 500
     * Expected Output: Delete error
     * 
     * Expected Behavior:
     *   - Find user and room ✓
     *   - Remove last member
     *   - room.members.length === 0
     *   - Try Room.findByIdAndDelete()
     *   - Delete fails
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - Room has only 1 member
     *   - Room.findByIdAndDelete() throws error
     * 
     * WHY THIS TEST: Verifies error handling when room deletion fails
     */

    const mockUser = {
      _id: 'test-user-123',
      name: 'Test User',
      roomId: 'room-123',
      status: UserStatus.IN_WAITING_ROOM,
      save: jest.fn().mockResolvedValue(true)
    };

    const mockRoom = {
      _id: 'room-123',
      members: ['test-user-123'],  // Only 1 member
      save: jest.fn()
    };

    mockedUser.findById.mockResolvedValue(mockUser as any);
    mockedRoom.findById.mockResolvedValue(mockRoom as any);
    mockedRoom.findByIdAndDelete.mockRejectedValue(new Error('Delete failed'));

    const roomId = 'room-123';
    const token = generateTestToken('test-user-123');

    const response = await request(app)
      .put(`/api/matching/leave/${roomId}`)
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Delete failed');
  });
});

describe('GET /api/matching/status/:roomId - With Mocking', () => {
  /**
   * Interface: GET /api/matching/status/:roomId
   * Mocking: Database (Room model)
   */

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('should return 500 when room not found', async () => {
    /**
     * SCENARIO: Room doesn't exist in database
     * 
     * Input: GET /api/matching/status/:roomId
     * Expected Status Code: 500
     * Expected Output: Room not found error
     * 
     * Expected Behavior:
     *   - Service tries Room.findById()
     *   - Room doesn't exist
     *   - Throw Error('Room not found')
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - Room.findById() returns null
     * 
     * WHY THIS TEST: Verifies error handling for non-existent rooms
     */

    mockedRoom.findById.mockResolvedValue(null);

    const roomId = new mongoose.Types.ObjectId().toString();
    const token = generateTestToken('test-user-123');

    const response = await request(app)
      .get(`/api/matching/status/${roomId}`)
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Room not found');
  });

  test('should return 500 when database query fails', async () => {
    /**
     * SCENARIO: Database error when finding room
     * 
     * Input: GET /api/matching/status/:roomId
     * Expected Status Code: 500
     * Expected Output: Database error
     * 
     * Expected Behavior:
     *   - Try Room.findById()
     *   - Database connection fails
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - Room.findById() throws error
     * 
     * WHY THIS TEST: Verifies error handling during database failures
     */

    mockedRoom.findById.mockRejectedValue(new Error('Connection refused'));

    const roomId = new mongoose.Types.ObjectId().toString();
    const token = generateTestToken('test-user-123');

    const response = await request(app)
      .get(`/api/matching/status/${roomId}`)
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Connection refused');
  });

  test('should return 400 for invalid ObjectId format', async () => {
    /**
     * SCENARIO: Invalid room ID format
     * 
     * Input: GET /api/matching/status/invalid-id
     * Expected Status Code: 400
     * Expected Output: Invalid data format error
     * 
     * Expected Behavior:
     *   - Try Room.findById(invalidId)
     *   - Mongoose throws CastError
     *   - Error handler returns 400
     * 
     * Mock Behavior:
     *   - Room.findById() throws CastError
     * 
     * WHY THIS TEST: Verifies handling of malformed IDs
     */

    const castError = new Error('Cast to ObjectId failed') as any;
    castError.name = 'CastError';

    mockedRoom.findById.mockRejectedValue(castError);

    const roomId = 'invalid-id-format';
    const token = generateTestToken('test-user-123');

    const response = await request(app)
      .get(`/api/matching/status/${roomId}`)
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(400);
    expect(response.body.message).toBe('Invalid data format');
  });
});

describe('GET /api/matching/users/:roomId - With Mocking', () => {
  /**
   * Interface: GET /api/matching/users/:roomId
   * Mocking: Database (Room model)
   */

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('should return 500 when room not found', async () => {
    /**
     * SCENARIO: Room doesn't exist
     * 
     * Input: GET /api/matching/users/:roomId
     * Expected Status Code: 500
     * Expected Output: Room not found error
     * 
     * Expected Behavior:
     *   - Try Room.findById()
     *   - Room is null
     *   - Throw error
     * 
     * Mock Behavior:
     *   - Room.findById() returns null
     * 
     * WHY THIS TEST: Verifies error handling for missing rooms
     */

    mockedRoom.findById.mockResolvedValue(null);

    const roomId = new mongoose.Types.ObjectId().toString();
    const token = generateTestToken('test-user-123');

    const response = await request(app)
      .get(`/api/matching/users/${roomId}`)
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Room not found');
  });

  test('should return 500 when database connection fails', async () => {
    /**
     * SCENARIO: Database error
     * 
     * Input: GET /api/matching/users/:roomId
     * Expected Status Code: 500
     * Expected Output: Database error
     * 
     * Expected Behavior:
     *   - Try Room.findById()
     *   - Database fails
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - Room.findById() throws error
     * 
     * WHY THIS TEST: Verifies error handling during database failures
     */

    mockedRoom.findById.mockRejectedValue(new Error('Network error'));

    const roomId = new mongoose.Types.ObjectId().toString();
    const token = generateTestToken('test-user-123');

    const response = await request(app)
      .get(`/api/matching/users/${roomId}`)
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Network error');
  });
});