// tests/helpers/seed.helper.ts

import User, { UserStatus } from '../../src/models/User';
import Group from '../../src/models/Group';
import Room from '../../src/models/Room';
import mongoose from 'mongoose';

export interface TestUser {
  _id: string;
  googleId: string;
  email: string;
  name: string;
  bio?: string;
}

export interface TestGroup {
  _id: string;
  roomId: string;
  members: string[];
  completionTime: Date;
  restaurantSelected: boolean;
}

export interface TestRoom {
  _id: string;
  members: string[];
  completionTime: Date;
  status: string;
}

/**
 * Seed test users into the database
 * All users have FCM tokens by default for notification testing
 * Users 2 and 3 have roomId/groupId for deletion tests
 */
export const seedTestUsers = async (): Promise<TestUser[]> => {
  // Create dummy IDs for room and group (for user 2 and 3)
  const testRoomId = new mongoose.Types.ObjectId().toString();
  const testGroupId = new mongoose.Types.ObjectId().toString();

  const users = [
    {
      googleId: 'google-test-user-1',
      email: 'testuser1@example.com',
      name: 'Test User 1',
      profilePicture: 'https://example.com/pic1.jpg',
      fcmToken: 'mock-fcm-token-user1',
      status: UserStatus.ONLINE,
      bio: 'Test bio for user 1',
      budget: 50,           // ← Added budget
      radiusKm: 10,         // ← Added radiusKm
      preference: [],       // ← Added preference
    },
    {
      googleId: 'google-test-user-2',
      email: 'testuser2@example.com',
      name: 'Test User 2',
      profilePicture: 'https://example.com/pic2.jpg',
      fcmToken: 'mock-fcm-token-user2',
      status: UserStatus.ONLINE,
      bio: 'Test bio for user 2',
      budget: 50,
      radiusKm: 10,
      preference: [],
    },
    {
      googleId: 'google-test-user-3',
      email: 'testuser3@example.com',
      name: 'Test User 3',
      profilePicture: 'https://example.com/pic3.jpg',
      fcmToken: 'mock-fcm-token-user3',
      status: UserStatus.IN_WAITING_ROOM,
      bio: 'Test bio for user 3',
      roomId: testRoomId,  // User 3 is in a waiting room
      budget: 50,
      radiusKm: 10,
      preference: [],
    },
    {
      googleId: 'google-test-user-4',
      email: 'testuser4@example.com',
      name: 'Test User 4',
      profilePicture: 'https://example.com/pic4.jpg',
      fcmToken: 'mock-fcm-token-user4',
      status: UserStatus.IN_GROUP,
      bio: 'Test bio for user 4',
      groupId: testGroupId,  // User 4 is in a group
      budget: 50,
      radiusKm: 10,
      preference: [],
    },
    {
      googleId: 'google-test-user-5',
      email: 'testuser5@example.com',
      name: 'Test User 5',
      profilePicture: 'https://example.com/pic5.jpg',
      fcmToken: 'mock-fcm-token-user5',
      status: UserStatus.ONLINE,
      bio: 'Test bio for user 5',
      budget: 50,
      radiusKm: 10,
      preference: [],
    },
  ];

  const createdUsers = await User.insertMany(users);
  return createdUsers.map(user => ({
    _id: user._id.toString(),
    googleId: user.googleId,
    email: user.email,
    name: user.name,
    bio: user.bio,
  }));
};

/**
 * Seed a deletable test user (for delete tests)
 * This user can be deleted without affecting other tests
 */
export const seedDeletableUser = async (): Promise<TestUser> => {
  const user = await User.create({
    googleId: 'google-deletable-user',
    email: 'deletable@example.com',
    name: 'Deletable User',
    profilePicture: 'https://example.com/deletable.jpg',
    fcmToken: 'mock-fcm-token-deletable',
    status: UserStatus.ONLINE,
    bio: 'This user can be deleted',
    budget: 50,
    radiusKm: 10,
    preference: [],
  });

  return {
    _id: user._id.toString(),
    googleId: user.googleId,
    email: user.email,
    name: user.name,
    bio: user.bio,
  };
};

/**
 * Get a test user by ID
 */
export const getTestUserById = async (userId: string): Promise<any | null> => {
  return User.findById(userId);
};

/**
 * Clean up all test data
 */
export const cleanTestData = async (): Promise<void> => {
  await User.deleteMany({
    email: { $regex: /testuser.*@example\.com|deletable@example\.com/ }
  });
  await Group.deleteMany({
    roomId: { $regex: /^test-room-/ }
  });
  await Room.deleteMany({
    cuisine: { $regex: /^test-/ }
  });
};

/**
 * Seed a test group with specified members
 */
export const seedTestGroup = async (
  roomId: string,
  memberIds: string[],
  options?: {
    restaurantSelected?: boolean;
    restaurant?: any;
    completionTime?: Date;
  }
): Promise<TestGroup> => {
  const group = await Group.create({
    roomId,
    members: memberIds,
    completionTime: options?.completionTime || new Date(Date.now() + 3600000), // 1 hour from now
    maxMembers: 4,
    restaurantSelected: options?.restaurantSelected || false,
    restaurant: options?.restaurant || undefined,
    votes: new Map(),
    restaurantVotes: new Map(),
  });

  return {
    _id: group._id.toString(),
    roomId: group.roomId,
    members: group.members,
    completionTime: group.completionTime,
    restaurantSelected: group.restaurantSelected,
  };
};

/**
 * Get a test group by ID
 */
export const getTestGroupById = async (groupId: string): Promise<any | null> => {
  return Group.findById(groupId);
};

/**
 * Seed a test room with specified members
 */
export const seedTestRoom = async (
  memberIds: string[],
  options?: {
    cuisine?: string;
    budget?: number;
    radiusKm?: number;
    completionTime?: Date;
    status?: string;
  }
): Promise<TestRoom> => {
  const room = await Room.create({
    members: memberIds,
    cuisine: options?.cuisine || 'test-cuisine',
    budget: options?.budget || 50,
    radiusKm: options?.radiusKm || 5,
    completionTime: options?.completionTime || new Date(Date.now() + 600000), // 10 minutes from now
    status: options?.status || 'waiting',
    maxMembers: 4,
  });

  return {
    _id: room._id.toString(),
    members: room.members,
    completionTime: room.completionTime,
    status: room.status,
  };
};

/**
 * Get a test room by ID
 */
export const getTestRoomById = async (roomId: string): Promise<any | null> => {
  return Room.findById(roomId);
};