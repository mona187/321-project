// tests/with-mocks/restaurant.mock.test.ts

/**
 * Restaurant Routes Tests - With Mocking
 * Tests error scenarios by mocking external dependencies (Google Places API via axios)
 * 
 * PURPOSE: Test how the application handles Google Places API failures and errors
 * that are difficult to reproduce with a real API.
 */

// ============================================
// MOCK ALL EXTERNAL DEPENDENCIES (before imports)
// ============================================

// Mock axios for Google Places API calls
jest.mock('axios');

// Set API key BEFORE importing app (which imports the service)
// This ensures the service reads the env var when it's instantiated
process.env.GOOGLE_PLACES_API_KEY = 'test-api-key';

// ============================================
// IMPORTS AFTER MOCK
// ============================================

import request from 'supertest';
import axios from 'axios';
import app from '../../src/app';
import { generateTestToken } from '../helpers/auth.helper';

// Get typed mock
const mockedAxios = axios as jest.Mocked<typeof axios>;

/**
 * WHAT ARE WE TESTING?
 * 
 * In with-mocks tests, we simulate Google Places API failures to verify:
 * 1. Service correctly handles API errors (returns mock data on network failures)
 * 2. Service correctly throws AppError for specific API status codes
 * 3. Controller correctly handles errors from service
 * 4. Application doesn't crash on external API failures
 * 
 * We mock axios to simulate:
 * - Network failures (ECONNREFUSED, ETIMEDOUT, etc.)
 * - API error status codes (OVER_QUERY_LIMIT, REQUEST_DENIED, etc.)
 * - Missing API responses
 * - Invalid API responses
 */

describe('GET /api/restaurant/search - With Mocking (Google Places API)', () => {
  /**
   * Interface: GET /api/restaurant/search
   * Mocking: axios (Google Places API)
   */

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('should return mock data when Google Places API network fails', async () => {
    /**
     * SCENARIO: Network failure when calling Google Places API
     * 
     * Input: GET /api/restaurant/search?latitude=40.7128&longitude=-74.0060
     * Expected Status Code: 200
     * Expected Output: Mock restaurant data (fallback)
     * 
     * Expected Behavior:
     *   - Validate latitude/longitude ✓
     *   - Service calls Google Places API via axios
     *   - axios.get() throws network error
     *   - Service catches error and returns mock data (fallback)
     *   - Controller returns 200 with mock data
     * 
     * Mock Behavior:
     *   - axios.get() rejects with Error('Network Error')
     * 
     * WHY THIS TEST: Verifies service correctly falls back to mock data on network failures
     */

    mockedAxios.get.mockRejectedValue(new Error('Network Error'));

    const response = await request(app)
      .get('/api/restaurant/search')
      .query({
        latitude: '40.7128',
        longitude: '-74.0060'
      });

    expect(response.status).toBe(200);
    expect(response.body.Body).toBeInstanceOf(Array);
    expect(response.body.Body.length).toBeGreaterThan(0);
    // Verify it's mock data (has expected mock restaurant names)
    expect(response.body.Body.some((r: any) => r.name === 'Sushi Paradise' || r.name === 'Italian Bistro')).toBe(true);
  });

  test('should return 500 when Google Places API returns error status', async () => {
    /**
     * SCENARIO: Google Places API returns error status code
     * 
     * Input: GET /api/restaurant/search?latitude=40.7128&longitude=-74.0060
     * Expected Status Code: 500
     * Expected Output: Error message
     * 
     * Expected Behavior:
     *   - Validate latitude/longitude ✓
     *   - Service calls Google Places API via axios
     *   - API returns status: 'OVER_QUERY_LIMIT' (not OK or ZERO_RESULTS)
     *   - Service throws AppError with 500
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - axios.get() resolves with { data: { status: 'OVER_QUERY_LIMIT' } }
     * 
     * WHY THIS TEST: Verifies service correctly throws AppError for API error statuses
     */

    mockedAxios.get.mockResolvedValue({
      data: {
        status: 'OVER_QUERY_LIMIT',
        error_message: 'You have exceeded your daily request quota'
      }
    } as any);

    const response = await request(app)
      .get('/api/restaurant/search')
      .query({
        latitude: '40.7128',
        longitude: '-74.0060'
      });

    expect(response.status).toBe(500);
    expect(response.body.message).toContain('Google Places API error');
    expect(response.body.message).toContain('OVER_QUERY_LIMIT');
  });

  test('should return mock data when Google Places API times out', async () => {
    /**
     * SCENARIO: Google Places API request times out
     * 
     * Input: GET /api/restaurant/search?latitude=40.7128&longitude=-74.0060
     * Expected Status Code: 200
     * Expected Output: Mock restaurant data (fallback)
     * 
     * Expected Behavior:
     *   - Service calls Google Places API via axios
     *   - axios.get() throws timeout error
     *   - Service catches error and returns mock data (fallback)
     *   - Controller returns 200 with mock data
     * 
     * Mock Behavior:
     *   - axios.get() rejects with Error('ETIMEDOUT')
     * 
     * WHY THIS TEST: Verifies service correctly handles timeout errors
     */

    const timeoutError: any = new Error('ETIMEDOUT');
    timeoutError.code = 'ETIMEDOUT';
    mockedAxios.get.mockRejectedValue(timeoutError);

    const response = await request(app)
      .get('/api/restaurant/search')
      .query({
        latitude: '40.7128',
        longitude: '-74.0060'
      });

    expect(response.status).toBe(200);
    expect(response.body.Body).toBeInstanceOf(Array);
    expect(response.body.Body.length).toBeGreaterThan(0);
  });

  test('should return mock data when Google Places API connection refused', async () => {
    /**
     * SCENARIO: Google Places API connection refused
     * 
     * Input: GET /api/restaurant/search?latitude=40.7128&longitude=-74.0060
     * Expected Status Code: 200
     * Expected Output: Mock restaurant data (fallback)
     * 
     * Expected Behavior:
     *   - Service calls Google Places API via axios
     *   - axios.get() throws ECONNREFUSED error
     *   - Service catches error and returns mock data (fallback)
     * 
     * Mock Behavior:
     *   - axios.get() rejects with Error('connect ECONNREFUSED')
     * 
     * WHY THIS TEST: Verifies service correctly handles connection refused errors
     */

    const connError: any = new Error('connect ECONNREFUSED');
    connError.code = 'ECONNREFUSED';
    mockedAxios.get.mockRejectedValue(connError);

    const response = await request(app)
      .get('/api/restaurant/search')
      .query({
        latitude: '40.7128',
        longitude: '-74.0060'
      });

    expect(response.status).toBe(200);
    expect(response.body.Body).toBeInstanceOf(Array);
  });
});

describe('GET /api/restaurant/:restaurantId - With Mocking (Google Places API)', () => {
  /**
   * Interface: GET /api/restaurant/:restaurantId
   * Mocking: axios (Google Places API)
   */

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('should return 404 when Google Places API returns NOT_FOUND', async () => {
    /**
     * SCENARIO: Google Places API returns NOT_FOUND for restaurant
     * 
     * Input: GET /api/restaurant/invalid-place-id
     * Expected Status Code: 404
     * Expected Output: Error message
     * 
     * Expected Behavior:
     *   - Extract restaurantId from params ✓
     *   - Service calls Google Places API via axios
     *   - API returns status: 'NOT_FOUND'
     *   - Service throws AppError with 404
     *   - Error handler returns 404
     * 
     * Mock Behavior:
     *   - axios.get() resolves with { data: { status: 'NOT_FOUND' } }
     * 
     * WHY THIS TEST: Verifies service correctly throws 404 for not found restaurants
     */

    mockedAxios.get.mockResolvedValue({
      data: {
        status: 'NOT_FOUND',
        error_message: 'Restaurant not found'
      }
    } as any);

    const response = await request(app)
      .get('/api/restaurant/invalid-place-id');

    expect(response.status).toBe(404);
    expect(response.body.message).toContain('Restaurant not found');
  });

  test('should return mock data when Google Places API network fails', async () => {
    /**
     * SCENARIO: Network failure when fetching restaurant details
     * 
     * Input: GET /api/restaurant/ChIJN1t_tDeuEmsRUsoyG83frY4
     * Expected Status Code: 200
     * Expected Output: Mock restaurant data (fallback)
     * 
     * Expected Behavior:
     *   - Service calls Google Places API via axios
     *   - axios.get() throws network error
     *   - Service catches error and returns mock data (fallback)
     *   - Controller returns 200 with mock data
     * 
     * Mock Behavior:
     *   - axios.get() rejects with Error('Network Error')
     * 
     * WHY THIS TEST: Verifies service correctly falls back to mock data on network failures
     */

    mockedAxios.get.mockRejectedValue(new Error('Network Error'));

    const response = await request(app)
      .get('/api/restaurant/ChIJN1t_tDeuEmsRUsoyG83frY4');

    expect(response.status).toBe(200);
    expect(response.body.Body).toHaveProperty('restaurantId', 'ChIJN1t_tDeuEmsRUsoyG83frY4');
    expect(response.body.Body).toHaveProperty('name');
  });

  test('should return mock data when Google Places API times out', async () => {
    /**
     * SCENARIO: Google Places API request times out
     * 
     * Input: GET /api/restaurant/test-place-id
     * Expected Status Code: 200
     * Expected Output: Mock restaurant data (fallback)
     * 
     * Expected Behavior:
     *   - Service calls Google Places API via axios
     *   - axios.get() throws timeout error
     *   - Service catches error and returns mock data (fallback)
     * 
     * Mock Behavior:
     *   - axios.get() rejects with Error('ETIMEDOUT')
     * 
     * WHY THIS TEST: Verifies service correctly handles timeout errors
     */

    const timeoutError: any = new Error('ETIMEDOUT');
    timeoutError.code = 'ETIMEDOUT';
    mockedAxios.get.mockRejectedValue(timeoutError);

    const response = await request(app)
      .get('/api/restaurant/test-place-id');

    expect(response.status).toBe(200);
    expect(response.body.Body).toHaveProperty('restaurantId', 'test-place-id');
  });
});

describe('POST /api/restaurant/recommendations/:groupId - With Mocking (Google Places API)', () => {
  /**
   * Interface: POST /api/restaurant/recommendations/:groupId
   * Mocking: axios (Google Places API - called internally by getRecommendationsForGroup -> searchRestaurants)
   */

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('should return mock data when Google Places API fails during recommendations', async () => {
    /**
     * SCENARIO: Google Places API fails when generating recommendations
     * 
     * Input: POST /api/restaurant/recommendations/:groupId with userPreferences
     * Expected Status Code: 200
     * Expected Output: Mock restaurant data (fallback)
     * 
     * Expected Behavior:
     *   - Auth succeeds ✓
     *   - Validate userPreferences array ✓
     *   - Service calls getRecommendationsForGroup()
     *   - Which calls searchRestaurants() internally
     *   - searchRestaurants() calls Google Places API via axios
     *   - axios.get() throws error
     *   - Service catches error and returns mock data (fallback)
     *   - Controller returns 200 with mock data
     * 
     * Mock Behavior:
     *   - axios.get() rejects with Error('API Unavailable')
     * 
     * WHY THIS TEST: Verifies service correctly handles API failures during recommendations
     */

    mockedAxios.get.mockRejectedValue(new Error('API Unavailable'));

    const token = generateTestToken('test-user-id-123');

    const response = await request(app)
      .post('/api/restaurant/recommendations/test-group-id-123')
      .set('Authorization', `Bearer ${token}`)
      .send({
        userPreferences: [
          {
            cuisineTypes: ['italian'],
            budget: 50,
            location: { coordinates: [-123.1207, 49.2827] },
            radiusKm: 5
          }
        ]
      });

    expect(response.status).toBe(200);
    expect(response.body.Body).toBeInstanceOf(Array);
    expect(response.body.Body.length).toBeGreaterThan(0);
  });

  test('should return 500 when Google Places API returns error status during recommendations', async () => {
    /**
     * SCENARIO: Google Places API returns error status when generating recommendations
     * 
     * Input: POST /api/restaurant/recommendations/:groupId
     * Expected Status Code: 500
     * Expected Output: Error message
     * 
     * Expected Behavior:
     *   - Auth succeeds ✓
     *   - Validate input ✓
     *   - Service calls getRecommendationsForGroup()
     *   - Which calls searchRestaurants() internally
     *   - searchRestaurants() calls Google Places API via axios
     *   - API returns status: 'REQUEST_DENIED'
     *   - Service throws AppError with 500
     *   - Error handler returns 500
     * 
     * Mock Behavior:
     *   - axios.get() resolves with { data: { status: 'REQUEST_DENIED' } }
     * 
     * WHY THIS TEST: Verifies service correctly throws AppError for API error statuses during recommendations
     */

    mockedAxios.get.mockResolvedValue({
      data: {
        status: 'REQUEST_DENIED',
        error_message: 'This API project is not authorized'
      }
    } as any);

    const token = generateTestToken('test-user-id-123');

    const response = await request(app)
      .post('/api/restaurant/recommendations/test-group-id-123')
      .set('Authorization', `Bearer ${token}`)
      .send({
        userPreferences: [
          {
            cuisineTypes: ['italian'],
            budget: 50,
            location: { coordinates: [-123.1207, 49.2827] },
            radiusKm: 5
          }
        ]
      });

    expect(response.status).toBe(500);
    expect(response.body.message).toContain('Google Places API error');
    expect(response.body.message).toContain('REQUEST_DENIED');
  });
});
