// tests/with-mocks/restaurant.mock.test.ts

/**
 * Restaurant Routes Tests - With Mocking
 * Tests error scenarios and edge cases by mocking services and database
 * 
 * PURPOSE: Test how the application handles failures, errors, and edge cases
 * that are difficult or impossible to reproduce with a real database.
 */

// ============================================
// MOCK ALL DEPENDENCIES (before imports)
// ============================================

// Mock the RestaurantService
jest.mock('../../src/services/restaurantService', () => ({
  __esModule: true,
  default: {
    searchRestaurants: jest.fn(),
    getRestaurantDetails: jest.fn(),
    getRecommendationsForGroup: jest.fn(),
  }
}));

// Mock the Group model (for recommendations endpoint)
jest.mock('../../src/models/Group');

// Mock the User model (for recommendations endpoint)
jest.mock('../../src/models/User');

// Mock axios for external API calls
jest.mock('axios');

// ============================================
// IMPORT EVERYTHING
// ============================================

import request from 'supertest';
import app from '../../src/app';
import restaurantService from '../../src/services/restaurantService';
import { generateTestToken } from '../helpers/auth.helper';

// Get typed mocks
const mockedRestaurantService = restaurantService as jest.Mocked<typeof restaurantService>;

/**
 * WHAT ARE WE TESTING?
 * 
 * In with-mocks tests, we simulate failures and errors to verify:
 * 1. Error handling works correctly
 * 2. Appropriate error messages are returned
 * 3. Application doesn't crash on errors
 * 4. Edge cases are handled properly
 * 
 * We mock the service and external APIs to simulate:
 * - External API failures (Google Places API)
 * - Database connection errors
 * - Service method failures
 * - Data not found scenarios
 */

describe('GET /api/restaurant/search - With Mocking', () => {
  /**
   * Interface: GET /api/restaurant/search
   * Mocking: RestaurantService
   */

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('should return 500 when restaurantService.searchRestaurants throws error', async () => {
    /**
     * SCENARIO: External API failure when searching restaurants
     * 
     * Input: GET /api/restaurant/search?latitude=40.7128&longitude=-74.0060
     * Expected Status Code: 500
     * Expected Output: Error message
     * 
     * Expected Behavior:
     *   - Validate latitude/longitude ✓
     *   - Call restaurantService.searchRestaurants()
     *   - External API (Google Places) fails
     *   - Service throws error
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - restaurantService.searchRestaurants() rejects with Error('API request failed')
     * 
     * WHY THIS TEST: Verifies error handling when external restaurant API fails
     */

    mockedRestaurantService.searchRestaurants.mockRejectedValue(new Error('API request failed'));

    const response = await request(app)
      .get('/api/restaurant/search')
      .query({
        latitude: '40.7128',
        longitude: '-74.0060'
      });

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('API request failed');
  });

  test('should return 500 when database connection fails', async () => {
    /**
     * SCENARIO: Database error during restaurant search
     * 
     * Input: GET /api/restaurant/search with valid coordinates
     * Expected Status Code: 500
     * Expected Output: Database error
     * 
     * Expected Behavior:
     *   - Validate query parameters ✓
     *   - Call restaurantService.searchRestaurants()
     *   - Service tries to access database (if caching/preferences)
     *   - Database connection fails
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - restaurantService.searchRestaurants() rejects with Error('Database connection lost')
     * 
     * WHY THIS TEST: Verifies error handling when database fails during search
     */

    mockedRestaurantService.searchRestaurants.mockRejectedValue(new Error('Database connection lost'));

    const response = await request(app)
      .get('/api/restaurant/search')
      .query({
        latitude: '40.7128',
        longitude: '-74.0060'
      });

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Database connection lost');
  });
});

describe('GET /api/restaurant/:restaurantId - With Mocking', () => {
  /**
   * Interface: GET /api/restaurant/:restaurantId
   * Mocking: RestaurantService
   */

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('should return 500 when restaurantService.getRestaurantDetails throws error', async () => {
    /**
     * SCENARIO: External API failure when fetching restaurant details
     * 
     * Input: GET /api/restaurant/ChIJN1t_tDeuEmsRUsoyG83frY4
     * Expected Status Code: 500
     * Expected Output: Error message
     * 
     * Expected Behavior:
     *   - Extract restaurantId from params ✓
     *   - Call restaurantService.getRestaurantDetails(restaurantId)
     *   - External API (Google Places) fails
     *   - Service throws error
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - restaurantService.getRestaurantDetails() rejects with Error('Restaurant not found')
     * 
     * WHY THIS TEST: Verifies error handling when restaurant details API fails
     */

    mockedRestaurantService.getRestaurantDetails.mockRejectedValue(new Error('Restaurant not found'));

    const response = await request(app)
      .get('/api/restaurant/ChIJN1t_tDeuEmsRUsoyG83frY4');

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Restaurant not found');
  });

  test('should return 500 when external API times out', async () => {
    /**
     * SCENARIO: External API timeout when fetching restaurant details
     * 
     * Input: GET /api/restaurant/:restaurantId
     * Expected Status Code: 500
     * Expected Output: Timeout error
     * 
     * Expected Behavior:
     *   - Call restaurantService.getRestaurantDetails()
     *   - External API request times out
     *   - Service throws timeout error
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - restaurantService.getRestaurantDetails() rejects with Error('Request timeout')
     * 
     * WHY THIS TEST: Verifies error handling when external API is slow/unresponsive
     */

    mockedRestaurantService.getRestaurantDetails.mockRejectedValue(new Error('Request timeout'));

    const response = await request(app)
      .get('/api/restaurant/test-restaurant-id');

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Request timeout');
  });
});

describe('POST /api/restaurant/recommendations/:groupId - With Mocking', () => {
  /**
   * Interface: POST /api/restaurant/recommendations/:groupId
   * Mocking: RestaurantService, Group model
   */

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('should return 500 when restaurantService.getRecommendationsForGroup throws error', async () => {
    /**
     * SCENARIO: Error when generating restaurant recommendations for group
     * 
     * Input: POST /api/restaurant/recommendations/:groupId with userPreferences
     * Expected Status Code: 500
     * Expected Output: Error message
     * 
     * Expected Behavior:
     *   - Auth succeeds ✓
     *   - Validate userPreferences array ✓
     *   - Call restaurantService.getRecommendationsForGroup()
     *   - Service fails (e.g., group not found, API error)
     *   - Service throws error
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - restaurantService.getRecommendationsForGroup() rejects with Error('Group not found')
     * 
     * WHY THIS TEST: Verifies error handling when recommendation generation fails
     */

    mockedRestaurantService.getRecommendationsForGroup.mockRejectedValue(new Error('Group not found'));

    const token = generateTestToken('test-user-id-123');

    const response = await request(app)
      .post('/api/restaurant/recommendations/test-group-id-123')
      .set('Authorization', `Bearer ${token}`)
      .send({
        userPreferences: ['italian', 'pizza']
      });

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Group not found');
  });

  test('should return 500 when external API fails during recommendations', async () => {
    /**
     * SCENARIO: External API failure when generating recommendations
     * 
     * Input: POST /api/restaurant/recommendations/:groupId
     * Expected Status Code: 500
     * Expected Output: API error
     * 
     * Expected Behavior:
     *   - Auth succeeds ✓
     *   - Validate input ✓
     *   - Call restaurantService.getRecommendationsForGroup()
     *   - Service tries to fetch restaurants from external API
     *   - External API fails
     *   - Service throws error
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - restaurantService.getRecommendationsForGroup() rejects with Error('API request failed')
     * 
     * WHY THIS TEST: Verifies error handling when external restaurant API fails during recommendations
     */

    mockedRestaurantService.getRecommendationsForGroup.mockRejectedValue(new Error('API request failed'));

    const token = generateTestToken('test-user-id-123');

    const response = await request(app)
      .post('/api/restaurant/recommendations/test-group-id-123')
      .set('Authorization', `Bearer ${token}`)
      .send({
        userPreferences: ['italian']
      });

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('API request failed');
  });

  test('should return 500 when database connection fails', async () => {
    /**
     * SCENARIO: Database error when fetching group data for recommendations
     * 
     * Input: POST /api/restaurant/recommendations/:groupId
     * Expected Status Code: 500
     * Expected Output: Database error
     * 
     * Expected Behavior:
     *   - Auth succeeds ✓
     *   - Call restaurantService.getRecommendationsForGroup()
     *   - Service tries to fetch group data from database
     *   - Database connection fails
     *   - Service throws error
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - restaurantService.getRecommendationsForGroup() rejects with Error('Database connection lost')
     * 
     * WHY THIS TEST: Verifies error handling when database fails during recommendations
     */

    mockedRestaurantService.getRecommendationsForGroup.mockRejectedValue(new Error('Database connection lost'));

    const token = generateTestToken('test-user-id-123');

    const response = await request(app)
      .post('/api/restaurant/recommendations/test-group-id-123')
      .set('Authorization', `Bearer ${token}`)
      .send({
        userPreferences: ['italian']
      });

    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Database connection lost');
  });
});

