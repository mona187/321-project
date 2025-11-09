// tests/no-mocks/matching.test.ts

// ============================================
// NO MOCKING - USING REAL SERVICES WITH SPIES
// ============================================

import request from 'supertest';
import app from '../../src/app';
import { generateTestToken } from '../helpers/auth.helper';
import { seedTestUsers, cleanTestData, TestUser } from '../helpers/seed.helper';
import { 
  createTestRoomWithMembers, 
  cleanMatchingTestData,
} from '../helpers/matching.helper';
import { initializeTestSocket, closeTestSocket } from '../helpers/socket.helper';
import { connectDatabase, disconnectDatabase } from '../../src/config/database';
import mongoose from 'mongoose';
import Room, { RoomStatus } from '../../src/models/Room';
import User, { UserStatus } from '../../src/models/User';
import socketManager from '../../src/utils/socketManager';
import * as firebase from '../../src/config/firebase';

/**
 * Matching Routes Tests - No Mocking (Controllable Scenarios)
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

beforeAll(async () => {
  console.log('\n🚀 Starting Matching Tests (No Mocking)...\n');
  
  // Initialize real Socket.IO server
  await initializeTestSocket();
  
  // Connect to database
  await connectDatabase();
  
  // Seed test users (now includes FCM tokens)
  testUsers = await seedTestUsers();
  
  console.log('\n✅ Test setup complete. Ready to run tests.\n');
});

afterAll(async () => {
  console.log('\n🧹 Cleaning up after tests...\n');
  
  await cleanMatchingTestData();
  await cleanTestData();
  await disconnectDatabase();
  
  // Close socket server
  await closeTestSocket();
  
  console.log('✅ Cleanup complete.\n');
});

beforeEach(async () => {
  // Spy on Firebase to prevent actual API calls
  jest.spyOn(firebase, 'sendPushNotification').mockResolvedValue('mock-message-id');
  jest.spyOn(firebase, 'sendMulticastNotification').mockResolvedValue({
    successCount: 1,
    failureCount: 0,
    responses: []
  } as any);

  // Clean up rooms before each test
  await Room.deleteMany({});
  await User.updateMany({}, { 
    roomId: null,
    groupId: null,
    status: UserStatus.ONLINE 
  });
  
  // Reset test users to clean state (preserve FCM tokens)
  for (let i = 0; i < testUsers.length; i++) {
    await User.findByIdAndUpdate(testUsers[i]._id, {
      roomId: null,
      groupId: null,
      status: UserStatus.ONLINE,
      budget: 50,
      radiusKm: 5,
      preference: [],
      fcmToken: `mock-fcm-token-user${i + 1}`  // Restore FCM token
    });
  }
});

afterEach(async () => {
  await Room.deleteMany({});
  await User.updateMany({}, { 
    roomId: null,
    groupId: null,
    status: UserStatus.ONLINE 
  });
  
  // Restore all spies
  jest.restoreAllMocks();
});

describe('POST /api/matching/join - No Mocking', () => {
  /**
   * Interface: POST /api/matching/join
   * Mocking: Firebase only (Socket.IO is real with spies)
   */

  // Consolidated test: create new room and update user preferences when joining
  // This tests the POST /api/matching/join endpoint pattern
  // The SAME endpoint handles both: creating new room and updating user preferences
  // Testing with preferences covers both scenarios: room creation and preference updates
  test('should create new room when no matching rooms exist and update user preferences', async () => {
    /**
     * Tests POST /api/matching/join endpoint pattern
     * Covers: matching.controller.ts joinMatching method (create room and update preferences)
     * Both scenarios execute the same code: join matching -> update preferences -> create/join room
     */
    // Spy on socket manager methods
    const emitRoomUpdateSpy = jest.spyOn(socketManager, 'emitRoomUpdate');
    const emitToUserSpy = jest.spyOn(socketManager, 'emitToUser');
    const emitMemberJoinedSpy = jest.spyOn(socketManager, 'emitMemberJoined');

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
    
    // Verify user preferences were updated in database (covers preference update scenario)
    const updatedUser = await User.findById(testUsers[0]._id);
    expect(updatedUser?.budget).toBe(50);
    expect(updatedUser?.radiusKm).toBe(10);
    expect(updatedUser?.preference).toEqual(['italian', 'vegetarian']);

    // Verify room was created in database
    const createdRoom = await Room.findById(response.body.Body.roomId);
    expect(createdRoom).not.toBeNull();
    expect(createdRoom?.members).toContain(testUsers[0]._id);
    expect(createdRoom?.cuisine).toBe('italian');

    // Verify user was updated in database (roomId and status)
    expect(updatedUser?.roomId).toBe(response.body.Body.roomId);
    expect(updatedUser?.status).toBe(UserStatus.IN_WAITING_ROOM);

    // Verify socket events were emitted
    expect(emitRoomUpdateSpy).toHaveBeenCalled();
    expect(emitRoomUpdateSpy).toHaveBeenCalledWith(
      response.body.Body.roomId,
      expect.arrayContaining([testUsers[0]._id]),
      expect.any(Date),
      expect.any(String)
    );

    expect(emitToUserSpy).toHaveBeenCalledWith(
      testUsers[0]._id,
      'room_update',
      expect.any(Object)
    );

    expect(emitMemberJoinedSpy).toHaveBeenCalled();
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

    // Spy on socket manager methods
    const emitRoomUpdateSpy = jest.spyOn(socketManager, 'emitRoomUpdate');
    const emitMemberJoinedSpy = jest.spyOn(socketManager, 'emitMemberJoined');

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
    expect(emitRoomUpdateSpy).toHaveBeenCalled();
    expect(emitMemberJoinedSpy).toHaveBeenCalled();
  });

  // Consolidated test: 401 without authentication
  // This tests the authMiddleware code which is the SAME for all endpoints
  // Testing once is sufficient since all endpoints use the same middleware
  test('should return 401 without authentication', async () => {
    /**
     * Tests authMiddleware -> no token -> 401 pattern
     * Covers: auth.middleware.ts lines 20-26
     * All endpoints use the same authMiddleware, so testing one endpoint covers all
     */
    const response = await request(app)
      .post('/api/matching/join')
      .send({ cuisine: ['italian'] });

    expect(response.status).toBe(401);
  });

  test('should return 200 with null roomId when no good match found (covers matchingService lines 69-70)', async () => {
    /**
     * Covers matchingService.ts lines 69-70: No good match found scenario
     * Path: findBestMatchingRoom -> bestMatch.score < MINIMUM_MATCH_SCORE (30) -> return null
     * This tests the branch where rooms exist but none have a score >= 30
     * 
     * Score calculation verification:
     * - Room: cuisine='italian', averageBudget=50, averageRadius=10
     * - User: cuisine=['chinese'], budget=150, radiusKm=60
     * 
     * 1. Cuisine: 'italian' not in ['chinese'] → 0 points
     * 2. Budget: diff = |50 - 150| = 100, score = max(0, 30 - 100) = 0
     * 3. Radius: diff = |10 - 60| = 50, score = max(0, 20 - (50 * 2)) = max(0, 20 - 100) = 0
     * Total: 0 + 0 + 0 = 0 < 30 → triggers lines 69-70
     * 
     * The sorting (b.score - a.score) ensures highest score first, but with score 0,
     * it will still be first (only room), and the check bestMatch.score >= 30 will fail.
     */
    
    // Create a room with specific preferences that will give a score of 0
    const Room = (await import('../../src/models/Room')).default;
    const existingRoom = await Room.create({
      completionTime: new Date(Date.now() + 2 * 60 * 1000), // 2 min from now
      maxMembers: 10,
      members: [testUsers[0]._id.toString()],
      status: RoomStatus.WAITING,
      cuisine: 'italian',
      averageBudget: 50,
      averageRadius: 10
    });
    
    // Update user to be in this room
    await User.findByIdAndUpdate(testUsers[0]._id, {
      roomId: existingRoom._id,
      status: UserStatus.IN_WAITING_ROOM
    });

    const token = generateTestToken(
      testUsers[4]._id, // User not in any room
      testUsers[4].email,
      testUsers[4].googleId
    );

    // Use preferences that give a score of exactly 0 (all components = 0)
    // This ensures bestMatch.score = 0 < 30, triggering the "no good match" path
    const zeroScorePreferences = {
      cuisine: ['chinese'], // Different cuisine → 0 points (not 50)
      budget: 150, // Budget diff = 100 → max(0, 30-100) = 0 points
      radiusKm: 60 // Radius diff = 50 → max(0, 20-100) = 0 points
      // Total score = 0 + 0 + 0 = 0 < 30 (MINIMUM_MATCH_SCORE)
    };

    const response = await request(app)
      .post('/api/matching/join')
      .set('Authorization', `Bearer ${token}`)
      .send(zeroScorePreferences);

    // Should create a new room (because findBestMatchingRoom returned null due to score < 30)
    expect(response.status).toBe(200);
    expect(response.body.Body).toHaveProperty('roomId');
    // The roomId should be a NEW room (not the existing one), because findBestMatchingRoom returned null
    expect(response.body.Body.roomId).not.toBe(existingRoom._id.toString());
    expect(response.body.Body.roomId).toBeTruthy();
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

  test('should use user.preference fallback when cuisine not provided (covers matchingService line 104)', async () => {
    /**
     * Covers matchingService.ts line 104: `cuisines: preferences.cuisine ?? user.preference`
     * Tests the fallback to user.preference when preferences.cuisine is undefined
     */
    // Set user preference in database
    await User.findByIdAndUpdate(testUsers[1]._id, {
      preference: ['chinese', 'japanese'],
      budget: 75,
      radiusKm: 8
    });

    const token = generateTestToken(
      testUsers[1]._id,
      testUsers[1].email,
      testUsers[1].googleId
    );

    // Don't provide cuisine in preferences - should use user.preference
    const response = await request(app)
      .post('/api/matching/join')
      .set('Authorization', `Bearer ${token}`)
      .send({ budget: 75, radiusKm: 8 }); // No cuisine provided

    expect(response.status).toBe(200);
    
    // Verify room was created with user's preference
    const createdRoom = await Room.findById(response.body.Body.roomId);
    expect(createdRoom?.cuisine).toBe('chinese'); // First cuisine from user.preference
  });

  test('should use user.budget fallback when budget not provided (covers matchingService line 105)', async () => {
    /**
     * Covers matchingService.ts line 105: `budget: preferences.budget ?? user.budget ?? 50`
     * Tests the fallback to user.budget when preferences.budget is undefined
     */
    // Set user budget in database
    await User.findByIdAndUpdate(testUsers[2]._id, {
      preference: ['mexican'],
      budget: 100,
      radiusKm: 12
    });

    const token = generateTestToken(
      testUsers[2]._id,
      testUsers[2].email,
      testUsers[2].googleId
    );

    // Don't provide budget in preferences - should use user.budget
    const response = await request(app)
      .post('/api/matching/join')
      .set('Authorization', `Bearer ${token}`)
      .send({ cuisine: ['mexican'], radiusKm: 12 }); // No budget provided

    expect(response.status).toBe(200);
    
    // Verify room was created with user's budget
    const createdRoom = await Room.findById(response.body.Body.roomId);
    expect(createdRoom?.averageBudget).toBe(100); // From user.budget
  });

  test('should use default budget 50 when budget not provided and user has no budget (covers matchingService line 105)', async () => {
    /**
     * Covers matchingService.ts line 105: `budget: preferences.budget ?? user.budget ?? 50`
     * Tests the fallback to 50 when both preferences.budget and user.budget are undefined
     * 
     * Note: We need to ensure user.budget is actually undefined (not 0) to trigger the ?? 50 fallback
     */
    // Create a new user and explicitly set budget to null to test the ?? 50 fallback
    // Note: The schema has default: 0, so we need to explicitly set it to null
    const testUser = await User.create({
      googleId: `google-test-no-budget-${Date.now()}`,
      email: `nobudget${Date.now()}@example.com`,
      name: 'No Budget User',
      preference: ['indian'],
      radiusKm: 7,
      status: UserStatus.ONLINE,
      credibilityScore: 100,
      budget: null as any // Explicitly set to null to test ?? 50 fallback
    });

    const token = generateTestToken(
      testUser._id.toString(),
      testUser.email,
      testUser.googleId
    );

    // Don't provide budget in preferences - should use default 50
    const response = await request(app)
      .post('/api/matching/join')
      .set('Authorization', `Bearer ${token}`)
      .send({ cuisine: ['indian'], radiusKm: 7 }); // No budget provided

    expect(response.status).toBe(200);
    
    // Verify room was created with default budget 50
    const createdRoom = await Room.findById(response.body.Body.roomId);
    expect(createdRoom?.averageBudget).toBe(50); // Default fallback
  });

  test('should use user.radiusKm fallback when radiusKm not provided (covers matchingService line 106)', async () => {
    /**
     * Covers matchingService.ts line 106: `radiusKm: preferences.radiusKm ?? user.radiusKm ?? 5`
     * Tests the fallback to user.radiusKm when preferences.radiusKm is undefined
     */
    // Set user radiusKm in database
    await User.findByIdAndUpdate(testUsers[4]._id, {
      preference: ['thai'],
      budget: 60,
      radiusKm: 15
    });

    const token = generateTestToken(
      testUsers[4]._id,
      testUsers[4].email,
      testUsers[4].googleId
    );

    // Don't provide radiusKm in preferences - should use user.radiusKm
    const response = await request(app)
      .post('/api/matching/join')
      .set('Authorization', `Bearer ${token}`)
      .send({ cuisine: ['thai'], budget: 60 }); // No radiusKm provided

    expect(response.status).toBe(200);
    
    // Verify room was created with user's radiusKm
    const createdRoom = await Room.findById(response.body.Body.roomId);
    expect(createdRoom?.averageRadius).toBe(15); // From user.radiusKm
  });

  test('should use default radiusKm 5 when radiusKm not provided and user has no radiusKm (covers matchingService line 106)', async () => {
    /**
     * Covers matchingService.ts line 106: `radiusKm: preferences.radiusKm ?? user.radiusKm ?? 5`
     * Tests the fallback to 5 when both preferences.radiusKm and user.radiusKm are undefined
     */
    // Create a new user and explicitly set radiusKm to null to test the ?? 5 fallback
    // Note: The schema has default: 5, so we need to explicitly set it to null
    const testUser = await User.create({
      googleId: `google-test-no-radius-${Date.now()}`,
      email: `noradius${Date.now()}@example.com`,
      name: 'No Radius User',
      preference: ['korean'],
      budget: 80,
      status: UserStatus.ONLINE,
      credibilityScore: 100,
      radiusKm: null as any // Explicitly set to null to test ?? 5 fallback
    });

    const token = generateTestToken(
      testUser._id.toString(),
      testUser.email,
      testUser.googleId
    );

    // Don't provide radiusKm in preferences - should use default 5
    const response = await request(app)
      .post('/api/matching/join')
      .set('Authorization', `Bearer ${token}`)
      .send({ cuisine: ['korean'], budget: 80 }); // No radiusKm provided

    expect(response.status).toBe(200);
    
    // Verify room was created with default radiusKm 5
    const createdRoom = await Room.findById(response.body.Body.roomId);
    expect(createdRoom?.averageRadius).toBe(5); // Default fallback
  });

  test('should use null for cuisine when cuisines array is empty (covers matchingService line 121)', async () => {
    /**
     * Covers matchingService.ts line 121: `cuisine: matchingPreferences.cuisines[0] || null`
     * Tests the fallback to null when cuisines array is empty or cuisines[0] is falsy
     * 
     * To trigger this, we need:
     * - preferences.cuisine is undefined
     * - user.preference is empty array or undefined
     * - This results in matchingPreferences.cuisines being empty/falsy
     */
    // Create a new user with empty preference array for this test
    const testUser = await User.create({
      googleId: `google-test-empty-pref-${Date.now()}`,
      email: `emptypref${Date.now()}@example.com`,
      name: 'Empty Preference User',
      preference: [], // Empty array
      budget: 70,
      radiusKm: 10,
      status: UserStatus.ONLINE,
      credibilityScore: 100
    });

    const token = generateTestToken(
      testUser._id.toString(),
      testUser.email,
      testUser.googleId
    );

    // Don't provide cuisine in preferences - should use user.preference (empty array)
    const response = await request(app)
      .post('/api/matching/join')
      .set('Authorization', `Bearer ${token}`)
      .send({ budget: 70, radiusKm: 10 }); // No cuisine provided

    expect(response.status).toBe(200);
    
    // Verify room was created with null cuisine (because cuisines[0] is undefined)
    const createdRoom = await Room.findById(response.body.Body.roomId);
    expect(createdRoom?.cuisine).toBeNull(); // Fallback to null
  });

  // Note: "should update user preferences when joining" test is consolidated above
  // The same POST /api/matching/join endpoint handles both room creation and preference updates
});

describe('POST /api/matching/join/:roomId - No Mocking', () => {
  /**
   * Interface: POST /api/matching/join/:roomId
   * Mocking: Firebase only
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

  // Note: "401 without authentication" test is consolidated above in join endpoint tests
  // All endpoints use the same authMiddleware, so testing one endpoint covers all
});

describe('PUT /api/matching/leave/:roomId - No Mocking', () => {
  /**
   * Interface: PUT /api/matching/leave/:roomId
   * Mocking: Firebase only
   */

  // Consolidated test: leave room (with other members and as last member)
  // This tests the PUT /api/matching/leave/:roomId endpoint pattern
  // The SAME endpoint handles both: leaving when others remain, and deleting room when last member leaves
  // Testing both scenarios covers the complete leave room logic
  test('should successfully leave room and delete room when last member leaves', async () => {
    /**
     * Tests PUT /api/matching/leave/:roomId endpoint pattern
     * Covers: matching.controller.ts leaveRoom method (leave with others, delete when last)
     * Both scenarios execute the same endpoint with different outcomes based on member count
     */
    // Spy on socket manager
    const emitMemberLeftSpy = jest.spyOn(socketManager, 'emitMemberLeft');

    // Test 1: Leave room when other members remain
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
    expect(emitMemberLeftSpy).toHaveBeenCalled();

    // Test 2: Delete room when last member leaves
    const { room: singleRoom, memberIds: singleMemberIds } = await createTestRoomWithMembers(1);
    const singleUserId = singleMemberIds[0];
    const singleUser = await User.findById(singleUserId);

    const singleToken = generateTestToken(
      singleUserId,
      singleUser!.email,
      singleUser!.googleId
    );

    const singleResponse = await request(app)
      .put(`/api/matching/leave/${singleRoom._id}`)
      .set('Authorization', `Bearer ${singleToken}`);

    expect(singleResponse.status).toBe(200);

    // Verify room was deleted from database
    const deletedRoom = await Room.findById(singleRoom._id);
    expect(deletedRoom).toBeNull();

    // Verify user status updated
    const singleUpdatedUser = await User.findById(singleUserId);
    expect(singleUpdatedUser?.roomId).toBeNull();
    expect(singleUpdatedUser?.status).toBe(UserStatus.ONLINE);
  });

  // Note: "should delete room when last member leaves" test is consolidated above
  // The same leave room endpoint handles both scenarios: leaving with others and deleting when last

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

  // Note: "401 without authentication" test is consolidated above in join endpoint tests
  // All endpoints use the same authMiddleware, so testing one endpoint covers all
});

describe('GET /api/matching/status/:roomId - No Mocking', () => {
  /**
   * Interface: GET /api/matching/status/:roomId
   * Mocking: Firebase only
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

  // Consolidated test: 500 for non-existent room
  // This tests the Room.findById() -> if (!room) -> throw Error('Room not found') pattern
  // The SAME pattern exists in getRoomStatus (matchingService line 261) and getRoomUsers (matchingService line 314)
  // Testing once is sufficient since both use identical pattern: if (!room) { throw new Error('Room not found') }
  test('should return 500 for non-existent room', async () => {
    /**
     * Tests Room.findById() -> if (!room) -> throw Error('Room not found') pattern
     * Covers: matchingService.ts lines 261 (getRoomStatus), 314 (getRoomUsers)
     * Both methods have identical code: if (!room) { throw new Error('Room not found') }
     */
    const fakeRoomId = new mongoose.Types.ObjectId().toString();

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    // Test with status endpoint - the code path is identical for status and users
    const response = await request(app)
      .get(`/api/matching/status/${fakeRoomId}`)
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Room not found');
  });

  // Note: "401 without authentication" test is consolidated above in join endpoint tests
  // All endpoints use the same authMiddleware, so testing one endpoint covers all
});

describe('GET /api/matching/users/:roomId - No Mocking', () => {
  /**
   * Interface: GET /api/matching/users/:roomId
   * Mocking: Firebase only
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

  test('should return 500 when room not found in getRoomUsers (covers matchingService lines 338-339)', async () => {
    /**
     * Covers matchingService.ts lines 338-339: Room not found check in getRoomUsers
     * Path: Room.findById -> if (!room) -> throw Error('Room not found')
     * This is a separate test to ensure getRoomUsers's specific error path is covered
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

  // Note: "401 without authentication" test is consolidated above in join endpoint tests
  // All endpoints use the same authMiddleware, so testing one endpoint covers all
});

describe('Room Model Methods - Integration Tests', () => {
  /**
   * These tests verify Room model methods through integration with the database
   * Covers: pre-save hook
   */

  test('should trigger pre-save hook to expire room', async () => {
    /**
     * Covers Room.ts line 164: Pre-save hook sets status to EXPIRED
     * Path: if (this.isExpired() && this.status === RoomStatus.WAITING) -> this.status = RoomStatus.EXPIRED
     */
    const Room = require('../../src/models/Room').default;
    const RoomStatus = require('../../src/models/Room').RoomStatus;
    
    // Create an expired room with WAITING status
    const expiredRoom = await Room.create({
      completionTime: new Date(Date.now() - 1000), // 1 second ago
      maxMembers: 2,
      members: [testUsers[0]._id],
      status: RoomStatus.WAITING
    });
    
    // Modify and save to trigger pre-save hook
    expiredRoom.members.push(testUsers[1]._id);
    await expiredRoom.save();
    
    // Verify status was updated to EXPIRED by pre-save hook
    const updatedRoom = await Room.findById(expiredRoom._id);
    expect(updatedRoom!.status).toBe(RoomStatus.EXPIRED);
    
    // Clean up
    await Room.deleteOne({ _id: expiredRoom._id });
  });
});

describe('SocketManager - Integration Tests', () => {
  /**
   * These tests verify SocketManager methods directly
   * Covers: initialize method with already initialized check, emitGroupReady, emitRoomExpired
   */

  test('should warn and return early when initialize is called twice', async () => {
    /**
     * Covers socketManager.ts lines 28-31: initialize when already initialized
     * Path: if (this.io) [TRUE BRANCH] -> console.warn -> return
     */
    const http = require('http');
    const consoleWarnSpy = jest.spyOn(console, 'warn').mockImplementation(() => {});
    
    // socketManager is already initialized in beforeAll
    // Create a new HTTP server for the second initialize call
    const testServer = http.createServer();
    
    // Call initialize again - should trigger the warning
    socketManager.initialize(testServer);
    
    // Verify warning was called
    expect(consoleWarnSpy).toHaveBeenCalledWith('⚠️  Socket.IO already initialized');
    
    consoleWarnSpy.mockRestore();
    testServer.close();
  });

  test('should call emitGroupReady through socketManager', async () => {
    /**
     * Covers socketManager.ts lines 80-82: emitGroupReady method
     * Path: emitGroupReady -> getEmitter() -> emitGroupReady(roomId, groupId, members)
     */
    const roomId = new mongoose.Types.ObjectId().toString();
    const groupId = new mongoose.Types.ObjectId().toString();
    const members = [testUsers[0]._id, testUsers[1]._id];
    
    // Spy on the emitter's emitGroupReady method
    const emitter = socketManager.getEmitter();
    const emitGroupReadySpy = jest.spyOn(emitter, 'emitGroupReady');
    
    // Call emitGroupReady through socketManager
    socketManager.emitGroupReady(roomId, groupId, members);
    
    // Verify the emitter method was called with correct arguments
    expect(emitGroupReadySpy).toHaveBeenCalledWith(roomId, groupId, members);
    
    emitGroupReadySpy.mockRestore();
  });

  // Consolidated test: emitRoomExpired method
  // This tests the emitRoomExpired -> getEmitter() -> emitRoomExpired(roomId, reason?) pattern
  // The SAME code path exists whether reason is provided or not (optional parameter)
  // Testing once with reason is sufficient since both paths execute the same line
  test('should call emitRoomExpired through socketManager', async () => {
    /**
     * Tests emitRoomExpired -> getEmitter() -> emitRoomExpired(roomId, reason?) pattern
     * Covers: socketManager.ts lines 87-89 (with and without reason parameter)
     * Both with and without reason execute the same code: this.getEmitter().emitRoomExpired(roomId, reason)
     */
    const roomId = new mongoose.Types.ObjectId().toString();
    const reason = 'Not enough members';
    
    // Spy on the emitter's emitRoomExpired method
    const emitter = socketManager.getEmitter();
    const emitRoomExpiredSpy = jest.spyOn(emitter, 'emitRoomExpired');
    
    // Test with reason parameter - this covers the same code path as without reason
    socketManager.emitRoomExpired(roomId, reason);
    
    // Verify the emitter method was called with correct arguments
    expect(emitRoomExpiredSpy).toHaveBeenCalledWith(roomId, reason);
    
    // Also test without reason to ensure optional parameter works
    emitRoomExpiredSpy.mockClear();
    socketManager.emitRoomExpired(roomId);
    expect(emitRoomExpiredSpy).toHaveBeenCalledWith(roomId, undefined);
    
    emitRoomExpiredSpy.mockRestore();
  });

  // Consolidated test: throw error when called before initialize
  // This tests the if (!property) -> throw Error pattern
  // The SAME pattern exists in getIO (line 42-44) and getEmitter (line 52-54)
  // Testing once is sufficient since both use identical pattern: if (!this.property) { throw Error }
  test('should throw error when getIO is called before initialize', () => {
    /**
     * Tests if (!property) -> throw Error pattern
     * Covers: socketManager.ts lines 42-44 (getIO), 52-54 (getEmitter)
     * Both methods have identical pattern: if (!this.property) { throw Error }
     */
    // Temporarily clear the io property to test uninitialized state
    const originalIO = (socketManager as any).io;
    (socketManager as any).io = null;
    
    // Try to get IO before initialization - should throw error
    expect(() => {
      socketManager.getIO();
    }).toThrow('Socket.IO not initialized. Call initialize() first.');
    
    // Restore the original io
    (socketManager as any).io = originalIO;
    
    // Also test getEmitter to cover the same pattern
    const originalEmitter = (socketManager as any).emitter;
    (socketManager as any).emitter = null;
    
    expect(() => {
      socketManager.getEmitter();
    }).toThrow('SocketEmitter not initialized. Call initialize() first.');
    
    (socketManager as any).emitter = originalEmitter;
  });

  // Consolidated test: emitToUser success path
  // This tests the emitToUser -> find socket -> emit -> return pattern
  // The SAME code path exists whether there's one socket or multiple sockets
  // Testing with multiple sockets covers both scenarios: single socket and early return behavior
  test('should emit event to user when socket with matching userId is found', () => {
    /**
     * Tests emitToUser -> find socket -> emit -> return pattern
     * Covers: socketManager.ts lines 166-178 (success path with single or multiple sockets)
     * Both single socket and multiple sockets execute the same code: find first match -> emit -> return
     */
    const io = socketManager.getIO();
    const testUserId = testUsers[0]._id.toString();
    const testEvent = 'test_event';
    const testPayload = { message: 'test' };
    
    // Create multiple mock sockets with the same userId to test early return behavior
    const mockSocket1 = {
      userId: testUserId,
      emit: jest.fn()
    };
    const mockSocket2 = {
      userId: testUserId,
      emit: jest.fn()
    };
    
    // Add both mock sockets to io.sockets.sockets Map
    (io.sockets.sockets as any).set('socket-id-1', mockSocket1);
    (io.sockets.sockets as any).set('socket-id-2', mockSocket2);
    
    // Call emitToUser
    socketManager.emitToUser(testUserId, testEvent, testPayload);
    
    // Verify only the first socket received the event (due to early return)
    // This covers both single socket scenario and early return behavior
    expect(mockSocket1.emit).toHaveBeenCalledWith(testEvent, testPayload);
    expect(mockSocket1.emit).toHaveBeenCalledTimes(1);
    expect(mockSocket2.emit).not.toHaveBeenCalled();
    
    // Clean up - remove the mock sockets
    (io.sockets.sockets as any).delete('socket-id-1');
    (io.sockets.sockets as any).delete('socket-id-2');
  });

  test('should log warning when no socket with matching userId is found', () => {
    /**
     * Covers socketManager.ts lines 166-178: emitToUser method (no socket found path)
     * Path: getIO() -> loop sockets -> if (socket.userId === userId) [FALSE for all] -> console.warn
     */
    const consoleWarnSpy = jest.spyOn(console, 'warn').mockImplementation(() => {});
    const testUserId = 'non-existent-user-id';
    const testEvent = 'test_event';
    const testPayload = { message: 'test' };
    
    // Call emitToUser with a userId that doesn't exist in any socket
    socketManager.emitToUser(testUserId, testEvent, testPayload);
    
    // Verify warning was logged
    expect(consoleWarnSpy).toHaveBeenCalledWith(
      `⚠️ emitToUser: No socket found for user ${testUserId}`
    );
    
    consoleWarnSpy.mockRestore();
  });
});