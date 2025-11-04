// tests/mocks/restaurant.mocks.test.ts

// ============================================
// CRITICAL: Clear module cache and mock FIRST
// ============================================

// Clear all modules to ensure fresh imports
beforeEach(() => {
  jest.clearAllMocks();
});

// Mock axios at module level
jest.mock('axios', () => ({
  get: jest.fn(),
  post: jest.fn(),
  put: jest.fn(),
  delete: jest.fn(),
  defaults: {
    headers: {
      common: {}
    }
  }
}));

// ============================================
// IMPORTS AFTER MOCK
// ============================================
import request from 'supertest';
import axios from 'axios';
import app from '../../src/app';
import { connectDatabase, disconnectDatabase } from '../../src/config/database';
import { seedTestUsers, cleanTestData, TestUser } from '../helpers/seed.helper';
import { generateTestToken } from '../helpers/auth.helper';

const mockedAxios = axios as jest.Mocked<typeof axios>;

let testUsers: TestUser[];
let originalApiKey: string | undefined;

beforeAll(async () => {
  console.log('\n🚀 Starting Restaurant Tests (With Mocks - Failure Scenarios)...\n');
  
  // Save original API key
  originalApiKey = process.env.GOOGLE_PLACES_API_KEY;
  
  // Set API key so service tries to use axios (instead of returning mock data immediately)
  process.env.GOOGLE_PLACES_API_KEY = 'test-api-key-for-mocking';
  
  await connectDatabase();
  testUsers = await seedTestUsers();
  console.log('✅ Test setup complete.\n');
});

afterAll(async () => {
  console.log('\n🧹 Cleaning up after tests...\n');
  
  // Restore original API key
  if (originalApiKey) {
    process.env.GOOGLE_PLACES_API_KEY = originalApiKey;
  } else {
    delete process.env.GOOGLE_PLACES_API_KEY;
  }
  
  await cleanTestData();
  await disconnectDatabase();
  console.log('✅ Cleanup complete.\n');
});

afterEach(() => {
  jest.clearAllMocks();
});

describe('GET /api/restaurant/search - External Failures', () => {
  test('should return mock data when Google Places API is unavailable', async () => {
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    // Mock API failure - axios.get throws error
    mockedAxios.get.mockRejectedValueOnce(
      new Error('Service Unavailable')
    );

    const response = await request(app)
      .get('/api/restaurant/search')
      .query({ latitude: '49.2827', longitude: '-123.1207' })
      .set('Authorization', `Bearer ${token}`);

    // Should gracefully fall back to mock data
    expect(response.status).toBe(200);
    expect(response.body.Body).toBeInstanceOf(Array);
    expect(response.body.Body.length).toBeGreaterThan(0);
    
    // Verify axios was called (proving mock is working)
    expect(mockedAxios.get).toHaveBeenCalledWith(
      'https://maps.googleapis.com/maps/api/place/nearbysearch/json',
      expect.objectContaining({
        params: expect.objectContaining({
          location: '49.2827,-123.1207',
          key: 'test-api-key-for-mocking'
        })
      })
    );
  });

  test('should return mock data when network timeout occurs', async () => {
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    mockedAxios.get.mockRejectedValueOnce(
      new Error('ETIMEDOUT')
    );

    const response = await request(app)
      .get('/api/restaurant/search')
      .query({ latitude: '49.2827', longitude: '-123.1207' })
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(200);
    expect(response.body.Body).toBeInstanceOf(Array);
  });

  test('should return mock data when DNS resolution fails', async () => {
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    mockedAxios.get.mockRejectedValueOnce(
      new Error('getaddrinfo ENOTFOUND maps.googleapis.com')
    );

    const response = await request(app)
      .get('/api/restaurant/search')
      .query({ latitude: '49.2827', longitude: '-123.1207' })
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(200);
    expect(response.body.Body).toBeInstanceOf(Array);
  });

  test('should return mock data when API returns OVER_QUERY_LIMIT', async () => {
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    // Mock rate limit response
    mockedAxios.get.mockResolvedValueOnce({
      data: {
        status: 'OVER_QUERY_LIMIT',
        error_message: 'You have exceeded your daily request quota',
        results: []
      }
    } as any);

    const response = await request(app)
      .get('/api/restaurant/search')
      .query({ latitude: '49.2827', longitude: '-123.1207' })
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(200);
    expect(response.body.Body).toBeInstanceOf(Array);
  });

  test('should return mock data when API returns REQUEST_DENIED', async () => {
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    mockedAxios.get.mockResolvedValueOnce({
      data: {
        status: 'REQUEST_DENIED',
        error_message: 'This API project is not authorized',
        results: []
      }
    } as any);

    const response = await request(app)
      .get('/api/restaurant/search')
      .query({ latitude: '49.2827', longitude: '-123.1207' })
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(200);
    expect(response.body.Body).toBeInstanceOf(Array);
  });

  test('should return mock data when API returns INVALID_REQUEST', async () => {
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    mockedAxios.get.mockResolvedValueOnce({
      data: {
        status: 'INVALID_REQUEST',
        error_message: 'Invalid request parameters',
        results: []
      }
    } as any);

    const response = await request(app)
      .get('/api/restaurant/search')
      .query({ latitude: '49.2827', longitude: '-123.1207' })
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(200);
    expect(response.body.Body).toBeInstanceOf(Array);
  });

  test('should handle malformed response from Google API', async () => {
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    // Malformed response
    mockedAxios.get.mockResolvedValueOnce({
      data: { status: 'OK' }  // Missing 'results' field
    } as any);

    const response = await request(app)
      .get('/api/restaurant/search')
      .query({ latitude: '49.2827', longitude: '-123.1207' })
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(200);
    expect(response.body.Body).toBeInstanceOf(Array);
  });

  test('should handle network error (ECONNREFUSED)', async () => {
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const error: any = new Error('connect ECONNREFUSED');
    error.code = 'ECONNREFUSED';
    mockedAxios.get.mockRejectedValueOnce(error);

    const response = await request(app)
      .get('/api/restaurant/search')
      .query({ latitude: '49.2827', longitude: '-123.1207' })
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(200);
    expect(response.body.Body).toBeInstanceOf(Array);
  });

  test('should handle SSL/TLS errors', async () => {
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    mockedAxios.get.mockRejectedValueOnce(
      new Error('unable to verify the first certificate')
    );

    const response = await request(app)
      .get('/api/restaurant/search')
      .query({ latitude: '49.2827', longitude: '-123.1207' })
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(200);
    expect(response.body.Body).toBeInstanceOf(Array);
  });

  test('should successfully search restaurants with valid API response', async () => {
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    // Mock successful API response
    mockedAxios.get.mockResolvedValueOnce({
      data: {
        status: 'OK',
        results: [
          {
            place_id: 'google_place_123',
            name: 'Test Restaurant from Google',
            vicinity: '123 Test St',
            geometry: {
              location: { lat: 49.2827, lng: -123.1207 }
            },
            rating: 4.5,
            price_level: 2
          }
        ]
      }
    } as any);

    const response = await request(app)
      .get('/api/restaurant/search')
      .query({ latitude: '49.2827', longitude: '-123.1207' })
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(200);
    expect(response.body.Body).toBeInstanceOf(Array);
    expect(response.body.Body.length).toBeGreaterThan(0);
    expect(response.body.Body[0].name).toBe('Test Restaurant from Google');
    expect(response.body.Body[0].restaurantId).toBe('google_place_123');
  });

  test('should handle ZERO_RESULTS status gracefully', async () => {
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    mockedAxios.get.mockResolvedValueOnce({
      data: {
        status: 'ZERO_RESULTS',
        results: []
      }
    } as any);

    const response = await request(app)
      .get('/api/restaurant/search')
      .query({ latitude: '49.2827', longitude: '-123.1207' })
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(200);
    expect(response.body.Body).toEqual([]);
  });

  test('should filter results by price level when specified', async () => {
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    mockedAxios.get.mockResolvedValueOnce({
      data: {
        status: 'OK',
        results: [
          {
            place_id: 'cheap_place',
            name: 'Cheap Eats',
            vicinity: '123 Budget St',
            price_level: 1,
            rating: 4.0
          },
          {
            place_id: 'expensive_place',
            name: 'Fancy Dining',
            vicinity: '456 Luxury Ave',
            price_level: 4,
            rating: 4.8
          }
        ]
      }
    } as any);

    const response = await request(app)
      .get('/api/restaurant/search')
      .query({ 
        latitude: '49.2827', 
        longitude: '-123.1207',
        priceLevel: '1'
      })
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(200);
    expect(response.body.Body).toHaveLength(1);
    expect(response.body.Body[0].name).toBe('Cheap Eats');
    expect(response.body.Body[0].priceLevel).toBe(1);
  });
});

describe('GET /api/restaurant/:restaurantId - External Failures', () => {
  test('should return mock data when Google Places API unavailable', async () => {
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    mockedAxios.get.mockRejectedValueOnce(
      new Error('Service Unavailable')
    );

    const response = await request(app)
      .get('/api/restaurant/test-place-id')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(200);
    expect(response.body.Body).toHaveProperty('name');
    expect(response.body.Body).toHaveProperty('restaurantId', 'test-place-id');
  });

  test('should return mock data when network timeout occurs', async () => {
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    mockedAxios.get.mockRejectedValueOnce(
      new Error('ETIMEDOUT')
    );

    const response = await request(app)
      .get('/api/restaurant/timeout-test')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(200);
    expect(response.body.Body).toHaveProperty('restaurantId', 'timeout-test');
  });

  test('should throw 404 when restaurant not found in Google API', async () => {
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    mockedAxios.get.mockResolvedValueOnce({
      data: {
        status: 'NOT_FOUND',
        error_message: 'Place not found'
      }
    } as any);

    const response = await request(app)
      .get('/api/restaurant/non-existent-place')
      .set('Authorization', `Bearer ${token}`);

    // AppError with 404 is thrown and NOT caught by service
    expect(response.status).toBe(404);
    expect(response.body.message).toContain('Restaurant not found');
  });

  test('should successfully get restaurant details with valid API response', async () => {
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    mockedAxios.get.mockResolvedValueOnce({
      data: {
        status: 'OK',
        result: {
          place_id: 'valid_place_123',
          name: 'Great Restaurant',
          formatted_address: '789 Main St, Vancouver, BC',
          geometry: {
            location: { lat: 49.2827, lng: -123.1207 }
          },
          rating: 4.7,
          price_level: 3,
          formatted_phone_number: '+1-604-555-1234',
          website: 'https://greatrestaurant.com'
        }
      }
    } as any);

    const response = await request(app)
      .get('/api/restaurant/valid_place_123')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(200);
    expect(response.body.Body.name).toBe('Great Restaurant');
    expect(response.body.Body.restaurantId).toBe('valid_place_123');
    expect(response.body.Body.rating).toBe(4.7);
  });

  test('should handle API returning INVALID_REQUEST', async () => {
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    mockedAxios.get.mockResolvedValueOnce({
      data: {
        status: 'INVALID_REQUEST',
        error_message: 'Invalid place ID format'
      }
    } as any);

    const response = await request(app)
      .get('/api/restaurant/invalid_format')
      .set('Authorization', `Bearer ${token}`);

    // Should return mock data (200) because service catches non-NOT_FOUND errors
    expect(response.status).toBe(200);
    expect(response.body.Body).toHaveProperty('restaurantId', 'invalid_format');
  });
});

describe('GET /api/restaurant/recommendations/:groupId - External Failures', () => {
  test('should return mock data when Google Places API fails', async () => {
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    mockedAxios.get.mockRejectedValueOnce(
      new Error('API Unavailable')
    );

    const response = await request(app)
      .post('/api/restaurant/recommendations/test-group-123')
      .send({
        userPreferences: [
          {
            cuisineTypes: ['italian'],
            budget: 50,
            location: { coordinates: [-123.1207, 49.2827] },
            radiusKm: 5
          }
        ]
      })
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(200);
    expect(response.body.Body).toBeInstanceOf(Array);
  });

  test('should handle network timeout when getting recommendations', async () => {
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    mockedAxios.get.mockRejectedValueOnce(
      new Error('ETIMEDOUT')
    );

    const response = await request(app)
      .post('/api/restaurant/recommendations/test-group-456')
      .send({
        userPreferences: [
          {
            cuisineTypes: ['japanese'],
            budget: 75,
            location: { coordinates: [-123.1207, 49.2827] },
            radiusKm: 10
          }
        ]
      })
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(200);
    expect(response.body.Body).toBeInstanceOf(Array);
  });

  test('should aggregate preferences and search successfully', async () => {
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    mockedAxios.get.mockResolvedValueOnce({
      data: {
        status: 'OK',
        results: [
          {
            place_id: 'aggregated_result',
            name: 'Perfect Match Restaurant',
            vicinity: '123 Middle Ground St',
            rating: 4.6,
            price_level: 2
          }
        ]
      }
    } as any);

    const response = await request(app)
      .post('/api/restaurant/recommendations/test-group-789')
      .send({
        userPreferences: [
          {
            cuisineTypes: ['italian', 'japanese'],
            budget: 50,
            location: { coordinates: [-123.1207, 49.2827] },
            radiusKm: 5
          },
          {
            cuisineTypes: ['mexican'],
            budget: 30,
            location: { coordinates: [-123.1100, 49.2800] },
            radiusKm: 3
          }
        ]
      })
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(200);
    expect(response.body.Body).toBeInstanceOf(Array);
    expect(response.body.Body.length).toBeGreaterThan(0);
    expect(response.body.Body[0].name).toBe('Perfect Match Restaurant');
  });
});

describe('Request Validation - Controller Level', () => {
  test('should return 400 when latitude is missing', async () => {
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .get('/api/restaurant/search')
      .query({ longitude: '-123.1207' })
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(400);
    expect(response.body.Message.error).toContain('Latitude and longitude are required');
  });

  test('should return 400 when longitude is missing', async () => {
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .get('/api/restaurant/search')
      .query({ latitude: '49.2827' })
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(400);
    expect(response.body.Message.error).toContain('Latitude and longitude are required');
  });

  test('should return 400 when userPreferences is missing in recommendations', async () => {
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .post('/api/restaurant/recommendations/test-group')
      .send({})
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(400);
    expect(response.body.Message.error).toContain('User preferences array is required');
  });

  test('should return 400 when userPreferences is not an array', async () => {
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .post('/api/restaurant/recommendations/test-group')
      .send({ userPreferences: 'not-an-array' })
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(400);
    expect(response.body.Message.error).toContain('User preferences array is required');
  });
});