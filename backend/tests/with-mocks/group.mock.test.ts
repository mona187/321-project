// tests/with-mocks/group.mock.test.ts

/**
 * Group Routes Tests - With Mocking
 * Tests error scenarios and edge cases by mocking database and services
 * 
 * PURPOSE: Test how the application handles failures, errors, and edge cases
 * that are difficult or impossible to reproduce with a real database.
 */

// ============================================
// MOCK ALL DEPENDENCIES (before imports)
// ============================================

// Mock the Group model (database)
jest.mock('../../src/models/Group');

// Mock the User model (database)
jest.mock('../../src/models/User');

// Mock the GroupService
jest.mock('../../src/services/groupService', () => ({
  __esModule: true,
  default: {
    getGroupStatus: jest.fn(),
    voteForRestaurant: jest.fn(),
    leaveGroup: jest.fn(),
    getGroupByUserId: jest.fn(),
  }
}));

// ============================================
// IMPORT EVERYTHING
// ============================================

import request from 'supertest';
import app from '../../src/app';

import groupService from '../../src/services/groupService';
import { generateTestToken } from '../helpers/auth.helper';

// Get typed mocks

const mockedGroupService = groupService as jest.Mocked<typeof groupService>;

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
 * - Connection failures
 * - Query errors
 * - Data not found
 * - Service method failures
 * - Business logic errors
 */

describe('GET /api/group/status - With Mocking', () => {
  /**
   * Interface: GET /api/group/status
   * Mocking: GroupService, User model, Group model
   */

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('should return 500 when groupService.getGroupByUserId throws error', async () => {
    /**
     * SCENARIO: Database error when finding user's group
     * 
     * Input: GET /api/group/status with valid token
     * Expected Status Code: 500
     * Expected Output: 
     *   {
     *     error: "Error",
     *     message: "Database connection failed",
     *     statusCode: 500
     *   }
     * 
     * Expected Behavior:
     *   - Auth middleware validates token ✓
     *   - Service tries to find user's group: groupService.getGroupByUserId(userId)
     *   - Database connection fails
     *   - Service throws Error('Database connection failed')
     *   - Error handler catches it and returns 500
     * 
     * Mock Behavior:
     *   - groupService.getGroupByUserId() rejects with connection error
     * 
     * WHY THIS TEST: Verifies error handling when database is unavailable
     */

    mockedGroupService.getGroupByUserId.mockRejectedValue(new Error('Database connection failed'));

    const token = generateTestToken('test-user-id-123');

    const response = await request(app)
      .get('/api/group/status')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Database connection failed');
    expect(response.body.statusCode).toBe(500);
  });

  test('should return 500 when groupService.getGroupStatus throws error', async () => {
    /**
     * SCENARIO: Error when retrieving group status after finding group
     * 
     * Input: GET /api/group/status with valid token
     * Expected Status Code: 500
     * Expected Output: Failed to get status error
     * 
     * Expected Behavior:
     *   - Auth succeeds ✓
     *   - getGroupByUserId() succeeds, returns group ✓
     *   - Call groupService.getGroupStatus(groupId)
     *   - Service throws error (e.g., group data corrupted)
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - groupService.getGroupByUserId() resolves with mock group
     *   - groupService.getGroupStatus() rejects with error
     * 
     * WHY THIS TEST: Verifies error handling when group exists but status retrieval fails
     */

    const mockGroup = {
      _id: 'test-group-id-123',
      roomId: 'test-room-123'
    };

    mockedGroupService.getGroupByUserId.mockResolvedValue(mockGroup as any);
    mockedGroupService.getGroupStatus.mockRejectedValue(new Error('Failed to get status'));

    const token = generateTestToken('test-user-id-123');

    const response = await request(app)
      .get('/api/group/status')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Failed to get status');
  });
});

describe('POST /api/group/vote/:groupId - With Mocking', () => {
  /**
   * Interface: POST /api/group/vote/:groupId
   * Mocking: GroupService, Group model
   */

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('should return 500 when groupService.voteForRestaurant throws "Group not found" error', async () => {
    /**
     * SCENARIO: User tries to vote in a group that doesn't exist
     * 
     * Input: POST /api/group/vote/non-existent-group-id with restaurantID
     * Expected Status Code: 500
     * Expected Output: 
     *   {
     *     message: 'Group not found',
     *     statusCode: 500
     *   }
     * 
     * Expected Behavior:
     *   - Auth succeeds ✓
     *   - Call groupService.voteForRestaurant(userId, groupId, restaurantID)
     *   - Service tries to find group
     *   - Group doesn't exist in database
     *   - Service throws Error('Group not found')
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - groupService.voteForRestaurant() rejects with Error('Group not found')
     * 
     * WHY THIS TEST: Verifies error handling when group was deleted but user still has reference
     */

    mockedGroupService.voteForRestaurant.mockRejectedValue(new Error('Group not found'));

    const token = generateTestToken('test-user-id-123');

    const response = await request(app)
      .post('/api/group/vote/non-existent-group-id')
      .set('Authorization', `Bearer ${token}`)
      .send({ restaurantID: 'rest-123' });

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Group not found');
    expect(response.body.statusCode).toBe(500);
  });

  test('should return 500 when groupService.voteForRestaurant throws "User is not a member" error', async () => {
    /**
     * SCENARIO: User tries to vote in a group they're not a member of
     * 
     * Input: POST /api/group/vote/:groupId with valid token but user not in group
     * Expected Status Code: 500
     * Expected Output: User is not a member error
     * 
     * Expected Behavior:
     *   - Auth succeeds ✓
     *   - Call groupService.voteForRestaurant()
     *   - Service finds group
     *   - Service checks if user is in group.members
     *   - User is not in members array
     *   - Service throws Error('User is not a member of this group')
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - groupService.voteForRestaurant() rejects with Error('User is not a member of this group')
     * 
     * WHY THIS TEST: Verifies business logic prevents unauthorized voting
     */

    mockedGroupService.voteForRestaurant.mockRejectedValue(new Error('User is not a member of this group'));

    const token = generateTestToken('test-user-id-123');

    const response = await request(app)
      .post('/api/group/vote/test-group-id-123')
      .set('Authorization', `Bearer ${token}`)
      .send({ restaurantID: 'rest-123' });

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('User is not a member of this group');
  });

  test('should return 500 when groupService.voteForRestaurant throws "Restaurant already selected" error', async () => {
    /**
     * SCENARIO: User tries to vote after restaurant has already been selected
     * 
     * Input: POST /api/group/vote/:groupId when restaurant already selected
     * Expected Status Code: 500
     * Expected Output: Restaurant already selected error
     * 
     * Expected Behavior:
     *   - Auth succeeds ✓
     *   - Call groupService.voteForRestaurant()
     *   - Service finds group
     *   - Service checks group.restaurantSelected
     *   - Restaurant already selected (group.restaurantSelected === true)
     *   - Service throws Error('Restaurant has already been selected for this group')
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - groupService.voteForRestaurant() rejects with Error('Restaurant has already been selected for this group')
     * 
     * WHY THIS TEST: Verifies business logic prevents voting after restaurant selection
     */

    mockedGroupService.voteForRestaurant.mockRejectedValue(new Error('Restaurant has already been selected for this group'));

    const token = generateTestToken('test-user-id-123');

    const response = await request(app)
      .post('/api/group/vote/test-group-id-123')
      .set('Authorization', `Bearer ${token}`)
      .send({ restaurantID: 'rest-123' });

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Restaurant has already been selected for this group');
  });

  test('should return 500 when database query fails in voteForRestaurant', async () => {
    /**
     * SCENARIO: Database error during vote operation
     * 
     * Input: POST /api/group/vote/:groupId with valid data
     * Expected Status Code: 500
     * Expected Output: Database error
     * 
     * Expected Behavior:
     *   - Auth succeeds ✓
     *   - Call groupService.voteForRestaurant()
     *   - Service tries to query/update database
     *   - Database connection fails
     *   - Service throws database error
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - groupService.voteForRestaurant() rejects with Error('Database connection lost')
     * 
     * WHY THIS TEST: Verifies graceful handling of database failures during voting
     */

    mockedGroupService.voteForRestaurant.mockRejectedValue(new Error('Database connection lost'));

    const token = generateTestToken('test-user-id-123');

    const response = await request(app)
      .post('/api/group/vote/test-group-id-123')
      .set('Authorization', `Bearer ${token}`)
      .send({ restaurantID: 'rest-123' });

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Database connection lost');
  });
});

describe('POST /api/group/leave/:groupId - With Mocking', () => {
  /**
   * Interface: POST /api/group/leave/:groupId
   * Mocking: GroupService, Group model, User model
   */

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('should return 500 when groupService.leaveGroup throws "Group not found" error', async () => {
    /**
     * SCENARIO: User tries to leave a group that doesn't exist
     * 
     * Input: POST /api/group/leave/non-existent-group-id
     * Expected Status Code: 500
     * Expected Output: Group not found error
     * 
     * Expected Behavior:
     *   - Auth succeeds ✓
     *   - Call groupService.leaveGroup(userId, groupId)
     *   - Service tries to find group
     *   - Group doesn't exist in database
     *   - Service throws Error('Group not found')
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - groupService.leaveGroup() rejects with Error('Group not found')
     * 
     * WHY THIS TEST: Verifies error handling when group was deleted but user still has reference
     */

    mockedGroupService.leaveGroup.mockRejectedValue(new Error('Group not found'));

    const token = generateTestToken('test-user-id-123');

    const response = await request(app)
      .post('/api/group/leave/non-existent-group-id')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Group not found');
  });

  test('should return 500 when groupService.leaveGroup throws "User not found" error', async () => {
    /**
     * SCENARIO: Valid token but user doesn't exist in database
     * 
     * Input: POST /api/group/leave/:groupId with valid token for non-existent user
     * Expected Status Code: 500
     * Expected Output: User not found error
     * 
     * Expected Behavior:
     *   - Auth succeeds (token is valid) ✓
     *   - Call groupService.leaveGroup()
     *   - Service tries to find user: User.findById(userId)
     *   - User doesn't exist in database
     *   - Service throws Error('User not found')
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - groupService.leaveGroup() rejects with Error('User not found')
     * 
     * WHY THIS TEST: Verifies error handling when user account was deleted but they still have a valid token
     */

    mockedGroupService.leaveGroup.mockRejectedValue(new Error('User not found'));

    const token = generateTestToken('test-user-id-123');

    const response = await request(app)
      .post('/api/group/leave/test-group-id-123')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('User not found');
  });

  test('should return 500 when database save fails in leaveGroup', async () => {
    /**
     * SCENARIO: Database error when saving updated group/user
     * 
     * Input: POST /api/group/leave/:groupId with valid data
     * Expected Status Code: 500
     * Expected Output: Database save error
     * 
     * Expected Behavior:
     *   - Auth succeeds ✓
     *   - Call groupService.leaveGroup()
     *   - Service finds group and user
     *   - Service removes user from group
     *   - Try to save updated group/user
     *   - Database save operation fails
     *   - Service throws database error
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - groupService.leaveGroup() rejects with Error('Failed to save group')
     * 
     * WHY THIS TEST: Verifies error handling when database update fails
     */

    mockedGroupService.leaveGroup.mockRejectedValue(new Error('Failed to save group'));

    const token = generateTestToken('test-user-id-123');

    const response = await request(app)
      .post('/api/group/leave/test-group-id-123')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Failed to save group');
  });

  test('should return 500 when database delete fails in leaveGroup', async () => {
    /**
     * SCENARIO: Error when deleting empty group after last member leaves
     * 
     * Input: POST /api/group/leave/:groupId when last member leaves
     * Expected Status Code: 500
     * Expected Output: Database delete error
     * 
     * Expected Behavior:
     *   - Auth succeeds ✓
     *   - Call groupService.leaveGroup()
     *   - Service removes last member from group
     *   - group.members.length === 0
     *   - Service tries to delete empty group: Group.findByIdAndDelete()
     *   - Group deletion fails
     *   - Service throws error
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - groupService.leaveGroup() rejects with Error('Failed to delete group')
     * 
     * WHY THIS TEST: Verifies error handling when group cleanup fails
     */

    mockedGroupService.leaveGroup.mockRejectedValue(new Error('Failed to delete group'));

    const token = generateTestToken('test-user-id-123');

    const response = await request(app)
      .post('/api/group/leave/test-group-id-123')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Failed to delete group');
  });

  test('should return 500 when database connection fails', async () => {
    /**
     * SCENARIO: Database connection is lost during leave operation
     * 
     * Input: POST /api/group/leave/:groupId with valid token
     * Expected Status Code: 500
     * Expected Output: Database connection error
     * 
     * Expected Behavior:
     *   - Auth succeeds ✓
     *   - Call groupService.leaveGroup()
     *   - Service tries to access database
     *   - Database connection fails
     *   - Service throws connection error
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - groupService.leaveGroup() rejects with Error('Database connection lost')
     * 
     * WHY THIS TEST: Verifies graceful handling of database connection failures
     */

    mockedGroupService.leaveGroup.mockRejectedValue(new Error('Database connection lost'));

    const token = generateTestToken('test-user-id-123');

    const response = await request(app)
      .post('/api/group/leave/test-group-id-123')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Database connection lost');
  });
});

