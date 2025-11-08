// tests/helpers/matching.helper.ts
import Room, { RoomStatus } from '../../src/models/Room';
import User, { UserStatus } from '../../src/models/User';
import Group from '../../src/models/Group';
import mongoose from 'mongoose';

export interface TestRoom {
  _id: string;
  completionTime: Date;
  maxMembers: number;
  members: string[];
  status: RoomStatus;
  cuisine?: string | null;
  averageBudget?: number;
  averageRadius?: number;
}

export interface TestGroup {
  _id: string;
  roomId: string;
  completionTime: Date;
  maxMembers: number;
  members: string[];
  restaurantSelected: boolean;
}

/**
 * Create a test room
 */
export async function createTestRoom(data: Partial<TestRoom>): Promise<TestRoom> {
  const roomData = {
    _id: new mongoose.Types.ObjectId(),
    completionTime: data.completionTime || new Date(Date.now() + 2 * 60 * 1000), // 2 min from now
    maxMembers: data.maxMembers || 10,
    members: data.members || [],
    status: data.status || RoomStatus.WAITING,
    cuisine: data.cuisine || 'italian',
    averageBudget: data.averageBudget || 50,
    averageRadius: data.averageRadius || 5
  };

  const room = await Room.create(roomData);
  
  return {
    _id: room._id.toString(),
    completionTime: room.completionTime,
    maxMembers: room.maxMembers,
    members: room.members,
    status: room.status,
    cuisine: room.cuisine,
    averageBudget: room.averageBudget,
    averageRadius: room.averageRadius
  };
}

/**
 * Create a test room with members
 */
export async function createTestRoomWithMembers(
  memberCount: number,
  cuisine: string = 'italian'
): Promise<{ room: TestRoom; memberIds: string[] }> {
  const memberIds: string[] = [];
  
  // Create test users
  for (let i = 0; i < memberCount; i++) {
    const user = await User.create({
      googleId: `google-room-test-${Date.now()}-${i}`,
      email: `roomtest${Date.now()}-${i}@example.com`,
      name: `Room Test User ${i}`,
      preference: [cuisine],
      budget: 50,
      radiusKm: 5,
      status: UserStatus.IN_WAITING_ROOM,
      credibilityScore: 100
    });
    memberIds.push(user._id.toString());
  }

  const room = await createTestRoom({
    members: memberIds,
    cuisine,
    status: RoomStatus.WAITING
  });

  // Update users with roomId
  await User.updateMany(
    { _id: { $in: memberIds } },
    { roomId: room._id }
  );

  return { room, memberIds };
}

/**
 * Create an expired test room
 */
export async function createExpiredTestRoom(
  memberCount: number = 1
): Promise<{ room: TestRoom; memberIds: string[] }> {
  const result = await createTestRoomWithMembers(memberCount);
  
  // Set completion time to past
  await Room.findByIdAndUpdate(result.room._id, {
    completionTime: new Date(Date.now() - 1000) // 1 second ago
  });

  return result;
}

/**
 * Create a full test room (10 members)
 */
export async function createFullTestRoom(): Promise<{ room: TestRoom; memberIds: string[] }> {
  return createTestRoomWithMembers(10, 'italian');
}

/**
 * Clean all matching-related test data
 */
export async function cleanMatchingTestData(): Promise<void> {
  console.log('🧹 Cleaning matching test data...');

  // Delete test rooms
  await Room.deleteMany({});

  // Delete test groups
  await Group.deleteMany({});

  // Clean up users created for room tests
  await User.deleteMany({ 
    email: /roomtest.*@example\.com/
  });

  console.log('✅ Matching test data cleaned');
}

/**
 * Get room by ID
 */
export async function getTestRoom(roomId: string): Promise<TestRoom | null> {
  const room = await Room.findById(roomId);
  
  if (!room) {
    return null;
  }

  return {
    _id: room._id.toString(),
    completionTime: room.completionTime,
    maxMembers: room.maxMembers,
    members: room.members,
    status: room.status,
    cuisine: room.cuisine,
    averageBudget: room.averageBudget,
    averageRadius: room.averageRadius
  };
}

/**
 * Get group by ID
 */
export async function getTestGroup(groupId: string): Promise<TestGroup | null> {
  const group = await Group.findById(groupId);
  
  if (!group) {
    return null;
  }

  return {
    _id: group._id.toString(),
    roomId: group.roomId,
    completionTime: group.completionTime,
    maxMembers: group.maxMembers,
    members: group.members,
    restaurantSelected: group.restaurantSelected
  };
}