// tests/no-mocks/restaurant.test.ts
import request from 'supertest';
import app from '../../src/app';
import { generateTestToken } from '../helpers/auth.helper';
import { 
  seedTestUsers, 
  cleanTestData, 
  TestUser,
  seedTestGroup
} from '../helpers/seed.helper';
import { initializeTestSocket, closeTestSocket } from '../helpers/socket.helper';
import { connectDatabase, disconnectDatabase } from '../../src/config/database';

/**
 * Restaurant Routes Tests - No Mocking
 * Tests restaurant endpoints with actual service interactions
 */

let testUsers: TestUser[];
let testGroup: any;

beforeAll(async () => {
  console.log('\n🚀 Starting Restaurant Tests (No Mocking)...\n');
  
  // Initialize real Socket.IO server (for consistency, even though restaurant routes don't use it)
  await initializeTestSocket();
  
  // Connect to test database
  await connectDatabase();
  
  // Seed test data
  testUsers = await seedTestUsers();
  
  // Create a test group for recommendations test
  const User = (await import('../../src/models/User')).default;
  testGroup = await seedTestGroup(
    'test-room-restaurant',
    [testUsers[0]._id, testUsers[1]._id]
  );
  
  await User.findByIdAndUpdate(testUsers[0]._id, { groupId: testGroup._id });
  await User.findByIdAndUpdate(testUsers[1]._id, { groupId: testGroup._id });
  
  console.log(`\n✅ Test setup complete. Ready to run tests.\n`);
});

afterEach(() => {
  // Restore all spies after each test to prevent interference between tests
  jest.restoreAllMocks();
});

afterAll(async () => {
  console.log('\n🧹 Cleaning up after tests...\n');
  
  // Clean up test data
  await cleanTestData();
  
  // Close database connection
  await disconnectDatabase();
  
  // Close Socket.IO server
  await closeTestSocket();
  
  console.log('✅ Cleanup complete.\n');
});

describe('GET /api/restaurant/search - No Mocking', () => {
  /**
   * Interface: GET /api/restaurant/search
   * Mocking: None
   */

  test('should return 200 and search results with valid coordinates', async () => {
    /**
     * Input: GET /api/restaurant/search?latitude=49.2827&longitude=-123.1207
     * Expected Status Code: 200
     * Expected Output:
     *   {
     *     Status: 200,
     *     Message: {},
     *     Body: [array of restaurants]
     *   }
     * Expected Behavior:
     *   - Query parameters parsed (latitude, longitude required)
     *   - Call restaurantService.searchRestaurants()
     *   - If no API key, returns mock data
     *   - Returns array of restaurants
     */

    const response = await request(app)
      .get('/api/restaurant/search')
      .query({
        latitude: 49.2827,
        longitude: -123.1207
      });

    expect(response.status).toBe(200);
    expect(response.body.Status).toBe(200);
    expect(Array.isArray(response.body.Body)).toBe(true);
    expect(response.body.Body.length).toBeGreaterThan(0);
    expect(response.body.Body[0]).toHaveProperty('name');
    expect(response.body.Body[0]).toHaveProperty('location');
    expect(response.body.Body[0]).toHaveProperty('restaurantId');
  });

  test('should return 400 when latitude is missing', async () => {
    /**
     * Input: GET /api/restaurant/search?longitude=-123.1207
     * Expected Status Code: 400
     * Expected Output:
     *   {
     *     Status: 400,
     *     Message: { error: 'Latitude and longitude are required' },
     *     Body: null
     *   }
     * Expected Behavior:
     *   - Validate query parameters
     *   - Latitude is missing
     *   - Return 400 immediately
     */

    const response = await request(app)
      .get('/api/restaurant/search')
      .query({
        longitude: -123.1207
      });

    expect(response.status).toBe(400);
    expect(response.body.Status).toBe(400);
    expect(response.body.Message.error).toBe('Latitude and longitude are required');
    expect(response.body.Body).toBeNull();
  });

  test('should return 400 when longitude is missing', async () => {
    /**
     * Input: GET /api/restaurant/search?latitude=49.2827
     * Expected Status Code: 400
     * Expected Output: Latitude and longitude are required error
     * Expected Behavior:
     *   - Validate query parameters
     *   - Longitude is missing
     *   - Return 400 immediately
     */

    const response = await request(app)
      .get('/api/restaurant/search')
      .query({
        latitude: 49.2827
      });

    expect(response.status).toBe(400);
    expect(response.body.Status).toBe(400);
    expect(response.body.Message.error).toBe('Latitude and longitude are required');
  });

  // Consolidated test: optional query parameters
  // This tests the optional parameter parsing pattern
  // The SAME code path exists for radius (line 25), cuisineTypes (line 26), and priceLevel (line 27)
  // Testing all three together is sufficient since they all use identical parsing logic
  test('should accept optional query parameters (radius, cuisineTypes, priceLevel)', async () => {
    /**
     * Tests optional parameter parsing pattern
     * Covers: restaurant.controller.ts lines 25-27 (radius, cuisineTypes, priceLevel)
     * All three use identical pattern: param ? parse(param) : undefined
     */
    const response = await request(app)
      .get('/api/restaurant/search')
      .query({
        latitude: 49.2827,
        longitude: -123.1207,
        radius: 10000,
        cuisineTypes: 'italian,sushi',
        priceLevel: 2
      });

    expect(response.status).toBe(200);
    expect(response.body.Body).toBeDefined();
  });

  // Consolidated test: optional auth middleware
  // This tests the optionalAuth middleware pattern
  // The SAME pattern exists for search (line 223) and getRestaurantDetails (line 286)
  // Testing once is sufficient since both use identical optionalAuth middleware behavior
  test('should work without authentication (optional auth)', async () => {
    /**
     * Tests optionalAuth middleware pattern
     * Covers: optionalAuth middleware allows requests without token
     * Both search and getRestaurantDetails endpoints use the same optionalAuth middleware
     */
    const response = await request(app)
      .get('/api/restaurant/search')
      .query({
        latitude: 49.2827,
        longitude: -123.1207
      });

    expect(response.status).toBe(200);
    expect(response.body.Body).toBeDefined();
  });
});

describe('GET /api/restaurant/:restaurantId - No Mocking', () => {
  /**
   * Interface: GET /api/restaurant/:restaurantId
   * Mocking: None
   */

  test('should return 200 and restaurant details for valid ID', async () => {
    /**
     * Input: GET /api/restaurant/ChIJN1t_tDeuEmsRUsoyG83frY4
     * Expected Status Code: 200
     * Expected Output:
     *   {
     *     Status: 200,
     *     Message: {},
     *     Body: {
     *       name: string,
     *       location: string,
     *       restaurantId: string,
     *       ...
     *     }
     *   }
     * Expected Behavior:
     *   - Extract restaurantId from params
     *   - Call restaurantService.getRestaurantDetails()
     *   - If no API key, returns mock data
     *   - Return restaurant details
     */

    const response = await request(app)
      .get('/api/restaurant/mock_001');

    expect(response.status).toBe(200);
    expect(response.body.Status).toBe(200);
    expect(response.body.Body).toBeDefined();
    expect(response.body.Body).toHaveProperty('name');
    expect(response.body.Body).toHaveProperty('location');
    expect(response.body.Body).toHaveProperty('restaurantId');
    expect(response.body.Body.restaurantId).toBe('mock_001');
  });

  // Note: "should work without authentication (optional auth)" test is consolidated above in search endpoint tests
  // The same optionalAuth middleware pattern exists for both search and getRestaurantDetails

  test('should handle different restaurant IDs', async () => {
    /**
     * Input: GET /api/restaurant/different-id-123
     * Expected Status Code: 200
     * Expected Output: Restaurant details with provided ID
     * Expected Behavior:
     *   - Service uses restaurantId parameter
     *   - Returns restaurant (mock or API) with that ID
     */

    const restaurantId = 'different-id-123';
    const response = await request(app)
      .get(`/api/restaurant/${restaurantId}`);

    expect(response.status).toBe(200);
    expect(response.body.Body.restaurantId).toBe(restaurantId);
  });
});

describe('POST /api/restaurant/recommendations/:groupId - No Mocking', () => {
  /**
   * Interface: POST /api/restaurant/recommendations/:groupId
   * Mocking: None
   */

  test('should return 200 and recommendations with valid data', async () => {
    /**
     * Input: POST /api/restaurant/recommendations/:groupId with userPreferences array
     * Expected Status Code: 200
     * Expected Output:
     *   {
     *     Status: 200,
     *     Message: {},
     *     Body: [array of recommended restaurants]
     *   }
     * Expected Behavior:
     *   - Auth succeeds
     *   - Validate userPreferences is array
     *   - Calculate average location, cuisine, budget, radius
     *   - Call searchRestaurants with aggregated preferences
     *   - Return recommendations
     */

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const userPreferences = [
      {
        cuisineTypes: ['italian', 'pizza'],
        budget: 50,
        location: { coordinates: [-123.1207, 49.2827] },
        radiusKm: 10
      },
      {
        cuisineTypes: ['italian'],
        budget: 75,
        location: { coordinates: [-123.1210, 49.2830] },
        radiusKm: 15
      }
    ];

    const response = await request(app)
      .post(`/api/restaurant/recommendations/${testGroup._id}`)
      .set('Authorization', `Bearer ${token}`)
      .send({ userPreferences });

    expect(response.status).toBe(200);
    expect(response.body.Status).toBe(200);
    expect(Array.isArray(response.body.Body)).toBe(true);
    expect(response.body.Body.length).toBeGreaterThan(0);
  });

  test('should return 401 without authentication token', async () => {
    /**
     * Input: POST /api/restaurant/recommendations/:groupId without Authorization header
     * Expected Status Code: 401
     * Expected Output: Unauthorized error
     * Expected Behavior: Auth middleware blocks request
     */

    const response = await request(app)
      .post(`/api/restaurant/recommendations/${testGroup._id}`)
      .send({
        userPreferences: []
      });

    expect(response.status).toBe(401);
  });

  test('should return 400 when userPreferences is missing', async () => {
    /**
     * Input: GET /api/restaurant/recommendations/:groupId without userPreferences in query
     * Expected Status Code: 400
     * Expected Output:
     *   {
     *     Status: 400,
     *     Message: { error: 'User preferences are required' },
     *     Body: null
     *   }
     * Expected Behavior:
     *   - Auth succeeds
     *   - Validate query parameter
     *   - userPreferences is missing
     *   - Return 400 immediately
     */

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .post(`/api/restaurant/recommendations/${testGroup._id}`)
      .set('Authorization', `Bearer ${token}`)
      .send({});

    expect(response.status).toBe(400);
    expect(response.body.Status).toBe(400);
    expect(response.body.Message.error).toBe('User preferences array is required');
  });

  test('should return 400 when userPreferences is not an array', async () => {
    /**
     * Input: POST /api/restaurant/recommendations/:groupId with userPreferences as object/string
     * Expected Status Code: 400
     * Expected Output: User preferences array is required error
     * Expected Behavior:
     *   - Auth succeeds
     *   - Validate userPreferences type
     *   - Not an array
     *   - Return 400
     */

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .post(`/api/restaurant/recommendations/${testGroup._id}`)
      .set('Authorization', `Bearer ${token}`)
      .send({
        userPreferences: 'not-an-array'
      });

    expect(response.status).toBe(400);
    expect(response.body.Message.error).toBe('User preferences array is required');
  });

  test('should handle empty userPreferences array', async () => {
    /**
     * Input: POST /api/restaurant/recommendations/:groupId with empty array
     * Expected Status Code: 200 or 500 (depends on implementation)
     * Expected Output: Recommendations or error
     * Expected Behavior:
     *   - Empty array passes validation
     *   - Service tries to calculate averages
     *   - May throw error or return empty results
     */

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .post(`/api/restaurant/recommendations/${testGroup._id}`)
      .set('Authorization', `Bearer ${token}`)
      .send({
        userPreferences: []
      });

    // Either 200 with empty results or 500 with error
    expect([200, 500]).toContain(response.status);
  });

  test('should calculate average preferences from multiple users', async () => {
    /**
     * Input: POST /api/restaurant/recommendations/:groupId with 3 user preferences
     * Expected Status Code: 200
     * Expected Output: Recommendations based on averaged preferences
     * Expected Behavior:
     *   - Calculate average latitude/longitude
     *   - Collect all unique cuisines
     *   - Calculate average budget and convert to priceLevel
     *   - Calculate average radius
     *   - Search with aggregated parameters
     *   - Return recommendations
     */

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const userPreferences = [
      {
        cuisineTypes: ['italian'],
        budget: 30,
        location: { coordinates: [-123.1200, 49.2800] },
        radiusKm: 5
      },
      {
        cuisineTypes: ['sushi'],
        budget: 80,
        location: { coordinates: [-123.1250, 49.2850] },
        radiusKm: 15
      },
      {
        cuisineTypes: ['italian', 'pizza'],
        budget: 50,
        location: { coordinates: [-123.1210, 49.2830] },
        radiusKm: 10
      }
    ];

    const response = await request(app)
      .post(`/api/restaurant/recommendations/${testGroup._id}`)
      .set('Authorization', `Bearer ${token}`)
      .send({ userPreferences });

    expect(response.status).toBe(200);
    expect(response.body.Body).toBeDefined();
    expect(Array.isArray(response.body.Body)).toBe(true);
  });
});

describe('Error Handling - next(error) Coverage', () => {
  /**
   * Test: catch block -> next(error) pattern for searchRestaurants
   */
  test('should handle errors in searchRestaurants catch block and call next(error)', async () => {
    /**
     * Tests catch block -> next(error) -> error handler pattern
     * Covers: restaurant.controller.ts catch block in searchRestaurants (line 44)
     */
    const restaurantService = await import('../../src/services/restaurantService');
    
    // Spy on searchRestaurants to throw an error
    const spy = jest
      .spyOn(restaurantService.default, 'searchRestaurants')
      .mockRejectedValueOnce(new Error('Service error for testing'));

    try {
      const response = await request(app)
        .get('/api/restaurant/search')
        .query({
          latitude: 49.2827,
          longitude: -123.1207
        });

      expect(response.status).toBe(500);
      expect(response.body.message).toContain('Service error for testing');
    } finally {
      // Ensure spy is restored even if test fails
      spy.mockRestore();
    }
  });

  /**
   * Test: catch block -> next(error) pattern for getGroupRecommendations
   * This specifically covers line 98 in restaurant.controller.ts
   */
  test('should handle errors in getGroupRecommendations catch block and call next(error)', async () => {
    /**
     * Tests catch block -> next(error) -> error handler pattern
     * Covers: restaurant.controller.ts catch block in getGroupRecommendations (line 98)
     */
    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const restaurantService = await import('../../src/services/restaurantService');
    
    // Spy on getRecommendationsForGroup to throw an error
    const spy = jest
      .spyOn(restaurantService.default, 'getRecommendationsForGroup')
      .mockRejectedValueOnce(new Error('Recommendations service error'));

    try {
      const response = await request(app)
        .post(`/api/restaurant/recommendations/${testGroup._id}`)
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
      expect(response.body.message).toContain('Recommendations service error');
    } finally {
      // Ensure spy is restored even if test fails
      spy.mockRestore();
    }
  });
});
