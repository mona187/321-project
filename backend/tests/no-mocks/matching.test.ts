// tests/no-mocks/matching.test.ts

// ============================================
// MOCK DEPENDENCIES FIRST (before any imports)
// ============================================

// Mock the socketManager singleton
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
  },
  socketManager: {
    initialize: jest.fn(),
    getIO: jest.fn(),
    getEmitter: jest.fn(),
    emitRoomUpdate: jest.fn(),
    emitToUser: jest.fn(),
    emitMemberJoined: jest.fn(),
    emitMemberLeft: jest.fn(),
    emitGroupReady: jest.fn(),
    emitRoomExpired: jest.fn(),
  }
}));

// Mock notification service
jest.mock('../../src/services/notificationService', () => ({
  notifyRoomMatched: jest.fn().mockResolvedValue(undefined),
  notifyRoomExpired: jest.fn().mockResolvedValue(undefined)
}));

// ============================================
// NOW IMPORT EVERYTHING
// ============================================

import request from 'supertest';
import app from '../../src/app';
import { generateTestToken } from '../helpers/auth.helper';
import { seedTestUsers, cleanTestData, TestUser } from '../helpers/seed.helper';
import { 
  createTestRoomWithMembers, 
  cleanMatchingTestData,
} from '../helpers/matching.helper';
import { connectDatabase, disconnectDatabase } from '../../src/config/database';
import mongoose from 'mongoose';
import Room from '../../src/models/Room';
import User, { UserStatus } from '../../src/models/User';
import socketManager from '../../src/utils/socketManager';

/**
 * Matching Routes Tests - No Mocking
 * Tests matching endpoints with actual database interactions
 * (Socket infrastructure is mocked as it's external to business logic)
 */

let testUsers: TestUser[];

beforeAll(async () => {
  console.log('\n🚀 Starting Matching Tests (No Mocking)...\n');
  
  await connectDatabase();
  testUsers = await seedTestUsers();
  
  console.log('\n✅ Test setup complete. Ready to run tests.\n');
});

afterAll(async () => {
  console.log('\n🧹 Cleaning up after tests...\n');
  
  await cleanMatchingTestData();
  await cleanTestData();
  await disconnectDatabase();
  
  console.log('✅ Cleanup complete.\n');
});

afterEach(async () => {
  // Clean up rooms and groups after each test
  await Room.deleteMany({});
  await User.updateMany({}, { 
    roomId: null,
    groupId: null,
    status: UserStatus.ONLINE 
  });
  
  // Clear socket mock calls
  jest.clearAllMocks();

  // TO AVOID CONFLICT BETWEEN TESTS - Reset test users to clean state
  for (const testUser of testUsers) {
    await User.findByIdAndUpdate(testUser._id, {
      roomId: null,
      groupId: null,
      status: UserStatus.ONLINE,
      budget: 50,
      radiusKm: 5,
      preference: []
    });
  }
});

describe('POST /api/matching/join - No Mocking', () => {
  /**
   * Interface: POST /api/matching/join
   * Mocking: Socket infrastructure only
   */

  test('should create new room when no matching rooms exist', async () => {
    /**
     * Input: POST /api/matching/join with preferences
     *   Body: { cuisine: ["italian"], budget: 50, radiusKm: 10 }
     * Expected Status Code: 200
     * Expected Output:
     *   {
     *     Status: 200,
     *     Message: { text: "Successfully joined matching" },
     *     Body: { roomId: string, room: object }
     *   }
     * Expected Behavior:
     *   - Authenticate user
     *   - Check for existing matching rooms
     *   - No good match found
     *   - Create new room with user as first member
     *   - Update user status to IN_WAITING_ROOM
     *   - Emit socket events (mocked infrastructure)
     *   - Return roomId and room details
     */

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const preferences = {
      cuisine: ['italian', 'vegetarian'],
      budget: 50,
      radiusKm: 10
    };

    const response = await request(app)
      .post('/api/matching/join')
      .set('Authorization', `Bearer ${token}`)
      .send(preferences);

    expect(response.status).toBe(200);
    expect(response.body.Status).toBe(200);
    expect(response.body.Message.text).toBe('Successfully joined matching');
    expect(response.body.Body).toHaveProperty('roomId');
    expect(response.body.Body).toHaveProperty('room');
    expect(response.body.Body.room.members).toContain(testUsers[0]._id);

    // Verify room was created in database
    const room = await Room.findById(response.body.Body.roomId);
    expect(room).not.toBeNull();
    expect(room?.members).toContain(testUsers[0]._id);
    expect(room?.cuisine).toBe('italian');

    // Verify user was updated in database
    const user = await User.findById(testUsers[0]._id);
    expect(user?.roomId).toBe(response.body.Body.roomId);
    expect(user?.status).toBe(UserStatus.IN_WAITING_ROOM);
    
    // Verify socket events were called (testing integration with socket layer)
    expect(socketManager.emitRoomUpdate).toHaveBeenCalled();
    expect(socketManager.emitToUser).toHaveBeenCalledWith(
      testUsers[0]._id,
      'room_update',
      expect.any(Object)
    );
    expect(socketManager.emitMemberJoined).toHaveBeenCalled();
  });

  test('should join existing room with matching preferences', async () => {
    /**
     * Input: POST /api/matching/join with preferences matching existing room
     * Expected Status Code: 200
     * Expected Output: Joined existing room
     * Expected Behavior:
     *   - Find existing room with similar preferences
     *   - Room has high match score (cuisine match + similar budget)
     *   - Add user to existing room
     *   - Update room averages in database
     *   - Emit socket events
     *   - Return existing roomId
     */

    // Create existing room with user1
    const { room: existingRoom } = await createTestRoomWithMembers(1, 'italian');

    // User2 joins with matching preferences
    const token = generateTestToken(
      testUsers[1]._id,
      testUsers[1].email,
      testUsers[1].googleId
    );

    const preferences = {
      cuisine: ['italian'],
      budget: 50,
      radiusKm: 10
    };

    const response = await request(app)
      .post('/api/matching/join')
      .set('Authorization', `Bearer ${token}`)
      .send(preferences);

    expect(response.status).toBe(200);
    expect(response.body.Body.roomId).toBe(existingRoom._id);

    // Verify user was added to existing room in database
    const room = await Room.findById(existingRoom._id);
    expect(room?.members).toHaveLength(2);
    expect(room?.members).toContain(testUsers[1]._id);
    
    // Verify socket events were called
    expect(socketManager.emitRoomUpdate).toHaveBeenCalled();
    expect(socketManager.emitMemberJoined).toHaveBeenCalled();
  });

  test('should return 401 without authentication', async () => {
    /**
     * Input: POST /api/matching/join without Authorization header
     * Expected Status Code: 401
     * Expected Output: Unauthorized error
     * Expected Behavior: Auth middleware blocks request
     */

    const response = await request(app)
      .post('/api/matching/join')
      .send({ cuisine: ['italian'] });

    expect(response.status).toBe(401);
  });

  test('should return 500 when user already in room', async () => {
    /**
     * Input: POST /api/matching/join when user already has roomId
     * Expected Status Code: 500
     * Expected Output: Error message about already being in room
     * Expected Behavior:
     *   - Check if user has roomId or groupId in database
     *   - User is already in a room
     *   - Service throws error
     *   - Error handler returns 500
     */

    // Put user in a room first
    const { room } = await createTestRoomWithMembers(1);
    await User.findByIdAndUpdate(testUsers[0]._id, {
      roomId: room._id,
      status: UserStatus.IN_WAITING_ROOM
    });

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .post('/api/matching/join')
      .set('Authorization', `Bearer ${token}`)
      .send({ cuisine: ['italian'] });

    expect(response.status).toBe(500);
    expect(response.body.message).toMatch(/already in a room/i);
  });

  test('should update user preferences when joining', async () => {
    /**
     * Input: POST /api/matching/join with new preferences
     * Expected Status Code: 200
     * Expected Output: Successfully joined
     * Expected Behavior:
     *   - Update user.budget, user.radiusKm, user.preference in database
     *   - Save user with new preferences
     *   - Use updated preferences for matching algorithm
     *   - Create/join room with new preferences
     */

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const newPreferences = {
      cuisine: ['mexican', 'thai'],
      budget: 75,
      radiusKm: 15
    };

    const response = await request(app)
      .post('/api/matching/join')
      .set('Authorization', `Bearer ${token}`)
      .send(newPreferences);

    expect(response.status).toBe(200);

    // Verify user preferences were updated in database
    const user = await User.findById(testUsers[0]._id);
    expect(user?.budget).toBe(75);
    expect(user?.radiusKm).toBe(15);
    expect(user?.preference).toEqual(['mexican', 'thai']);
  });
});

describe('POST /api/matching/join/:roomId - No Mocking', () => {
  /**
   * Interface: POST /api/matching/join/:roomId
   * Mocking: Socket infrastructure only
   */

  test('should return 501 Not Implemented', async () => {
    /**
     * Input: POST /api/matching/join/:roomId
     * Expected Status Code: 501
     * Expected Output: Not implemented message
     * Expected Behavior:
     *   - This endpoint is not fully implemented
     *   - Return 501 status code with explanation
     */

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const roomId = new mongoose.Types.ObjectId().toString();

    const response = await request(app)
      .post(`/api/matching/join/${roomId}`)
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(501);
    expect(response.body.Message.error).toContain('Not implemented');
  });

  test('should return 401 without authentication', async () => {
    /**
     * Input: POST /api/matching/join/:roomId without token
     * Expected Status Code: 401
     * Expected Output: Unauthorized error
     * Expected Behavior: Auth middleware blocks request
     */

    const roomId = new mongoose.Types.ObjectId().toString();

    const response = await request(app)
      .post(`/api/matching/join/${roomId}`);

    expect(response.status).toBe(401);
  });
});

describe('PUT /api/matching/leave/:roomId - No Mocking', () => {
  /**
   * Interface: PUT /api/matching/leave/:roomId
   * Mocking: Socket infrastructure only
   */

  test('should successfully leave room', async () => {
    /**
     * Input: PUT /api/matching/leave/:roomId
     * Expected Status Code: 200
     * Expected Output:
     *   {
     *     Status: 200,
     *     Message: { text: "Successfully left room" },
     *     Body: { roomId: string }
     *   }
     * Expected Behavior:
     *   - Find room by ID in database
     *   - Remove user from room.members array
     *   - Update user status to ONLINE in database
     *   - Clear user.roomId in database
     *   - Update room averages in database
     *   - Emit socket events
     *   - If room empty, delete it from database
     */

    // Create room with 2 users
    const { room, memberIds } = await createTestRoomWithMembers(2);
    const userId = memberIds[0];
    const user = await User.findById(userId);

    const token = generateTestToken(
      userId,
      user!.email,
      user!.googleId
    );

    const response = await request(app)
      .put(`/api/matching/leave/${room._id}`)
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(200);
    expect(response.body.Message.text).toBe('Successfully left room');
    expect(response.body.Body.roomId).toBe(room._id);

    // Verify user was removed from room in database
    const updatedRoom = await Room.findById(room._id);
    expect(updatedRoom?.members).not.toContain(userId);
    expect(updatedRoom?.members).toHaveLength(1);

    // Verify user status updated in database
    const updatedUser = await User.findById(userId);
    expect(updatedUser?.roomId).toBeNull();
    expect(updatedUser?.status).toBe(UserStatus.ONLINE);
    
    // Verify socket event was emitted
    expect(socketManager.emitMemberLeft).toHaveBeenCalled();
  });

  test('should delete room when last member leaves', async () => {
    /**
     * Input: PUT /api/matching/leave/:roomId (last member)
     * Expected Status Code: 200
     * Expected Output: Successfully left
     * Expected Behavior:
     *   - Remove user from room
     *   - room.members.length === 0
     *   - Delete empty room from database
     *   - Update user status
     */

    const { room, memberIds } = await createTestRoomWithMembers(1);
    const userId = memberIds[0];
    const user = await User.findById(userId);

    const token = generateTestToken(
      userId,
      user!.email,
      user!.googleId
    );

    const response = await request(app)
      .put(`/api/matching/leave/${room._id}`)
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(200);

    // Verify room was deleted from database
    const deletedRoom = await Room.findById(room._id);
    expect(deletedRoom).toBeNull();
    
    // Verify user status updated
    const updatedUser = await User.findById(userId);
    expect(updatedUser?.roomId).toBeNull();
    expect(updatedUser?.status).toBe(UserStatus.ONLINE);
  });

  test('should handle leaving non-existent room gracefully', async () => {
    /**
     * Input: PUT /api/matching/leave/:roomId for non-existent room
     * Expected Status Code: 200
     * Expected Output: Successfully left (clears stale roomId)
     * Expected Behavior:
     *   - Try to find room in database
     *   - Room doesn't exist
     *   - Clear user's roomId anyway (cleanup stale state)
     *   - Update user status to ONLINE
     *   - Return success (graceful handling)
     */

    // Set user's roomId to non-existent room
    const fakeRoomId = new mongoose.Types.ObjectId().toString();
    await User.findByIdAndUpdate(testUsers[0]._id, {
      roomId: fakeRoomId,
      status: UserStatus.IN_WAITING_ROOM
    });

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .put(`/api/matching/leave/${fakeRoomId}`)
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(200);

    // Verify user's roomId was cleared in database
    const user = await User.findById(testUsers[0]._id);
    expect(user?.roomId).toBeNull();
    expect(user?.status).toBe(UserStatus.ONLINE);
  });

  test('should return 401 without authentication', async () => {
    /**
     * Input: PUT /api/matching/leave/:roomId without token
     * Expected Status Code: 401
     * Expected Output: Unauthorized error
     * Expected Behavior: Auth middleware blocks request
     */

    const roomId = new mongoose.Types.ObjectId().toString();

    const response = await request(app)
      .put(`/api/matching/leave/${roomId}`);

    expect(response.status).toBe(401);
  });
});

describe('GET /api/matching/status/:roomId - No Mocking', () => {
  /**
   * Interface: GET /api/matching/status/:roomId
   * Mocking: Socket infrastructure only
   */

  test('should return room status for valid roomId', async () => {
    /**
     * Input: GET /api/matching/status/:roomId
     * Expected Status Code: 200
     * Expected Output:
     *   {
     *     Status: 200,
     *     Message: {},
     *     Body: {
     *       roomID: string,
     *       completionTime: number,
     *       members: string[],
     *       groupReady: boolean,
     *       status: number
     *     }
     *   }
     * Expected Behavior:
     *   - Find room by ID in database
     *   - Return room status details
     *   - Include completionTime as timestamp
     *   - Include groupReady flag based on room.status
     */

    const { room } = await createTestRoomWithMembers(3);

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .get(`/api/matching/status/${room._id}`)
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(200);
    expect(response.body.Body).toHaveProperty('roomID', room._id);
    expect(response.body.Body).toHaveProperty('completionTime');
    expect(response.body.Body).toHaveProperty('members');
    expect(response.body.Body.members).toHaveLength(3);
    expect(response.body.Body).toHaveProperty('groupReady');
    expect(response.body.Body).toHaveProperty('status');
  });

  test('should return 500 for non-existent room', async () => {
    /**
     * Input: GET /api/matching/status/:roomId for invalid ID
     * Expected Status Code: 500
     * Expected Output: Room not found error
     * Expected Behavior:
     *   - Try to find room in database
     *   - Room doesn't exist
     *   - Service throws Error('Room not found')
     *   - Error handler returns 500
     */

    const fakeRoomId = new mongoose.Types.ObjectId().toString();

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .get(`/api/matching/status/${fakeRoomId}`)
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Room not found');
  });

  test('should return 401 without authentication', async () => {
    /**
     * Input: GET /api/matching/status/:roomId without token
     * Expected Status Code: 401
     * Expected Output: Unauthorized error
     * Expected Behavior: Auth middleware blocks request
     */

    const roomId = new mongoose.Types.ObjectId().toString();

    const response = await request(app)
      .get(`/api/matching/status/${roomId}`);

    expect(response.status).toBe(401);
  });
});

describe('GET /api/matching/users/:roomId - No Mocking', () => {
  /**
   * Interface: GET /api/matching/users/:roomId
   * Mocking: Socket infrastructure only
   */

  test('should return list of users in room', async () => {
    /**
     * Input: GET /api/matching/users/:roomId
     * Expected Status Code: 200
     * Expected Output:
     *   {
     *     Status: 200,
     *     Message: {},
     *     Body: {
     *       roomID: string,
     *       Users: string[]
     *     }
     *   }
     * Expected Behavior:
     *   - Find room by ID in database
     *   - Return room.members array
     */

    const { room, memberIds } = await createTestRoomWithMembers(4);

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .get(`/api/matching/users/${room._id}`)
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(200);
    expect(response.body.Body.roomID).toBe(room._id);
    expect(response.body.Body.Users).toHaveLength(4);
    expect(response.body.Body.Users).toEqual(expect.arrayContaining(memberIds));
  });

  test('should return 500 for non-existent room', async () => {
    /**
     * Input: GET /api/matching/users/:roomId for invalid ID
     * Expected Status Code: 500
     * Expected Output: Room not found error
     * Expected Behavior:
     *   - Try to find room in database
     *   - Room doesn't exist
     *   - Service throws error
     */

    const fakeRoomId = new mongoose.Types.ObjectId().toString();

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .get(`/api/matching/users/${fakeRoomId}`)
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Room not found');
  });

  test('should return 401 without authentication', async () => {
    /**
     * Input: GET /api/matching/users/:roomId without token
     * Expected Status Code: 401
     * Expected Output: Unauthorized error
     * Expected Behavior: Auth middleware blocks request
     */

    const roomId = new mongoose.Types.ObjectId().toString();

    const response = await request(app)
      .get(`/api/matching/users/${roomId}`);

    expect(response.status).toBe(401);
  });
});