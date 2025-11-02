// tests/helpers/seed.helper.ts
import User from '../../src/models/User';
import { UserStatus } from '../../src/models/User';
import Group from '../../src/models/Group';
import mongoose from 'mongoose';

/**
 * Test User interface matching the actual User model (without timestamps)
 */
export interface TestUser {
  _id: string;
  googleId: string;
  email: string;
  name: string;
  bio?: string;
  profilePicture?: string;
  preference: string[];
  credibilityScore: number;
  contactNumber?: string;
  budget?: number;
  radiusKm?: number;
  status: UserStatus;
  roomId?: string | null;
  groupId?: string | null;
  fcmToken?: string | null;
}

/**
 * Convert Mongoose document to TestUser
 */
function convertToTestUser(user: any): TestUser {
  return {
    _id: user._id.toString(),
    googleId: user.googleId,
    email: user.email,
    name: user.name,
    bio: user.bio,
    profilePicture: user.profilePicture,
    preference: user.preference,
    credibilityScore: user.credibilityScore,
    contactNumber: user.contactNumber,
    budget: user.budget,
    radiusKm: user.radiusKm,
    status: user.status,
    roomId: user.roomId,
    groupId: user.groupId,
    fcmToken: user.fcmToken
  };
}

/**
 * Create test users in the database
 */
export async function seedTestUsers(): Promise<TestUser[]> {
  console.log('🌱 Seeding test users...');

  const testUsersData = [
    {
      _id: new mongoose.Types.ObjectId(),
      googleId: 'google-test-id-1',
      email: 'testuser1@example.com',
      name: 'Test User 1',
      bio: 'I love Italian food',
      profilePicture: 'https://example.com/avatar1.jpg',
      preference: ['italian', 'sushi'],
      credibilityScore: 100,
      contactNumber: '1234567890',
      budget: 50,
      radiusKm: 10,
      status: UserStatus.ONLINE,
      roomId: null,
      groupId: null,
      fcmToken: null
    },
    {
      _id: new mongoose.Types.ObjectId(),
      googleId: 'google-test-id-2',
      email: 'testuser2@example.com',
      name: 'Test User 2',
      bio: 'Vegan food enthusiast',
      profilePicture: 'https://example.com/avatar2.jpg',
      preference: ['chinese', 'japanese'],
      credibilityScore: 95,
      contactNumber: '0987654321',
      budget: 75,
      radiusKm: 15,
      status: UserStatus.ONLINE,
      roomId: null,
      groupId: null,
      fcmToken: null
    },
    {
      _id: new mongoose.Types.ObjectId(),
      googleId: 'google-test-id-3',
      email: 'testuser3@example.com',
      name: 'Test User 3',
      bio: 'User currently in a waiting room',
      profilePicture: 'https://example.com/avatar3.jpg',
      preference: ['italian', 'japanese'],
      credibilityScore: 88,
      contactNumber: '5555555555',
      budget: 40,
      radiusKm: 8,
      status: UserStatus.IN_WAITING_ROOM,
      roomId: 'test-room-123',
      groupId: null,
      fcmToken: null
    },
    {
      _id: new mongoose.Types.ObjectId(),
      googleId: 'google-test-id-4',
      email: 'testuser4@example.com',
      name: 'Test User 4',
      bio: 'User in a group',
      profilePicture: 'https://example.com/avatar4.jpg',
      preference: ['indian', 'sushi'],
      credibilityScore: 92,
      contactNumber: '6666666666',
      budget: 60,
      radiusKm: 12,
      status: UserStatus.IN_GROUP,
      roomId: null,
      groupId: 'test-group-456',
      fcmToken: null
    },
    {
      _id: new mongoose.Types.ObjectId(),
      googleId: 'google-test-id-5',
      email: 'testuser5@example.com',
      name: 'Test User 5',
      bio: 'Offline user',
      profilePicture: '',
      preference: ['indian'],
      credibilityScore: 100,
      contactNumber: '7777777777',
      budget: 80,
      radiusKm: 20,
      status: UserStatus.OFFLINE,
      roomId: null,
      groupId: null,
      fcmToken: 'test-fcm-token-5'
    }
  ];

  // Clear existing test users (those with test emails)
  await User.deleteMany({ email: /testuser.*@example\.com/ });

  // Insert new test users
  const createdUsers = await User.insertMany(testUsersData);

  console.log(`✅ Seeded ${createdUsers.length} test users`);

  // Convert to TestUser objects
  return createdUsers.map(user => convertToTestUser(user));
}

/**
 * Clean all test data from database
 */
export async function cleanTestData(): Promise<void> {
  console.log('🧹 Cleaning test data...');

  // Delete all test groups first (before users)
  const groupResult = await Group.deleteMany({ roomId: /^test-room-/ });
  
  // Delete all test users (emails matching pattern)
  const userResult = await User.deleteMany({ email: /testuser.*@example\.com/ });

  console.log(`✅ Test data cleaned (${userResult.deletedCount} users, ${groupResult.deletedCount} groups removed)`);
}

/**
 * Get a specific test user by email
 */
export async function getTestUserByEmail(email: string): Promise<TestUser | null> {
  const user = await User.findOne({ email });
  
  if (!user) {
    return null;
  }

  return convertToTestUser(user);
}

/**
 * Get a specific test user by ID
 */
export async function getTestUserById(userId: string): Promise<TestUser | null> {
  const user = await User.findById(userId);
  
  if (!user) {
    return null;
  }

  return convertToTestUser(user);
}

/**
 * Create a single test user with custom data
 */
export async function createTestUser(data: Partial<TestUser>): Promise<TestUser> {
  const userData = {
    _id: new mongoose.Types.ObjectId(),
    googleId: data.googleId || `google-custom-${Date.now()}`,
    email: data.email || `custom-${Date.now()}@example.com`,
    name: data.name || 'Custom User',
    bio: data.bio || '',
    profilePicture: data.profilePicture || '',
    preference: data.preference || [],
    credibilityScore: data.credibilityScore ?? 100,
    contactNumber: data.contactNumber || '',
    budget: data.budget ?? 0,
    radiusKm: data.radiusKm ?? 5,
    status: data.status ?? UserStatus.ONLINE,
    roomId: data.roomId || null,
    groupId: data.groupId || null,
    fcmToken: data.fcmToken || null
  };

  const user = await User.create(userData);
  
  return convertToTestUser(user);
}

/**
 * Seed a user that can be deleted (not in room or group)
 */
export async function seedDeletableUser(): Promise<TestUser> {
  const userData = {
    _id: new mongoose.Types.ObjectId(),
    googleId: `google-deletable-${Date.now()}`,
    email: `deletable-${Date.now()}@example.com`,
    name: 'Deletable User',
    bio: 'This user can be safely deleted',
    preference: ['any'],
    credibilityScore: 100,
    status: UserStatus.ONLINE,
    roomId: null,
    groupId: null
  };

  const user = await User.create(userData);
  
  return convertToTestUser(user);
}

/**
 * Group test helpers
 */
export interface TestGroup {
  _id: string;
  roomId: string;
  completionTime: Date;
  maxMembers: number;
  members: string[];
  restaurantSelected: boolean;
  restaurant?: any;
}

/**
 * Create a test group with members
 */
export async function seedTestGroup(
  roomId: string,
  members: string[],
  options?: {
    restaurantSelected?: boolean;
    restaurant?: any;
    completionTime?: Date;
  }
): Promise<TestGroup> {
  const completionTime = options?.completionTime || new Date(Date.now() + 3600000); // 1 hour from now
  
  const groupData: any = {
    roomId,
    completionTime,
    maxMembers: 4,
    members,
    restaurantSelected: options?.restaurantSelected || false,
    votes: new Map(),
    restaurantVotes: new Map(),
  };

  if (options?.restaurant) {
    groupData.restaurant = options.restaurant;
  }

  const group = await Group.create(groupData);
  
  return {
    _id: group._id.toString(),
    roomId: group.roomId,
    completionTime: group.completionTime,
    maxMembers: group.maxMembers,
    members: group.members,
    restaurantSelected: group.restaurantSelected,
    restaurant: group.restaurant,
  };
}

/**
 * Clean all test groups
 */
export async function cleanTestGroups(): Promise<void> {
  await Group.deleteMany({ roomId: /^test-room-/ });
}

/**
 * Get a group by ID
 */
export async function getTestGroupById(groupId: string): Promise<TestGroup | null> {
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
    restaurantSelected: group.restaurantSelected,
    restaurant: group.restaurant,
  };
}