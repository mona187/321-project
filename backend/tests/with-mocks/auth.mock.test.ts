// // tests/with-mocks/auth.mock.test.ts

// /**
//  * Auth Routes Tests - With Mocking (Uncontrollable Failures)
//  * 
//  * This test suite covers UNCONTROLLABLE failures:
//  * - Google OAuth API failures
//  * - Database connection errors
//  * - Database query timeouts
//  * - User save/delete failures
//  * - Service method failures
//  * 
//  * These failures cannot be reliably triggered in no-mocks tests.
//  */

// // ============================================
// // MOCK ALL DEPENDENCIES (before imports)
// // ============================================

// // Mock the User model (database)
// jest.mock('../../src/models/User');

// // Mock Google OAuth client
// jest.mock('google-auth-library', () => ({
//   OAuth2Client: jest.fn().mockImplementation(() => ({
//     verifyIdToken: jest.fn(),
//   }))
// }));

// // Mock axios for profile picture conversion
// jest.mock('axios');

// // ============================================
// // IMPORTS
// // ============================================

// import request from 'supertest';
// import app from '../../src/app';
// import User from '../../src/models/User';
// import { generateTestToken } from '../helpers/auth.helper';
// import { connectDatabase, disconnectDatabase } from '../../src/config/database';
// import { seedTestUsers, cleanTestData, TestUser } from '../helpers/seed.helper';

// const mockedUser = User as jest.Mocked<typeof User>;

// let testUsers: TestUser[];

// beforeAll(async () => {
//   console.log('\n🚀 Starting Auth Tests (With Mocks - Uncontrollable Failures)...\n');
//   await connectDatabase();
//   testUsers = await seedTestUsers();
//   console.log('✅ Test setup complete.\n');
// });

// afterAll(async () => {
//   console.log('\n🧹 Cleaning up after tests...\n');
//   await cleanTestData();
//   await disconnectDatabase();
//   console.log('✅ Cleanup complete.\n');
// });

// afterEach(() => {
//   jest.clearAllMocks();
// });

// describe('POST /api/auth/logout - Database Failures', () => {
//   test('should return 500 when User.findById() fails', async () => {
//     /**
//      * Scenario: Database connection lost while finding user
//      * Expected: Error handler catches, returns 500
//      */

//     mockedUser.findById.mockRejectedValueOnce(
//       new Error('MongoNetworkError: connection lost')
//     );

//     const token = generateTestToken(
//       testUsers[0]._id,
//       testUsers[0].email,
//       testUsers[0].googleId
//     );

//     const response = await request(app)
//       .post('/api/auth/logout')
//       .set('Authorization', `Bearer ${token}`);

//     expect(response.status).toBe(500);
//     expect(response.body.message).toContain('connection lost');
//   });

//   test('should return 500 when user.save() fails', async () => {
//     /**
//      * Scenario: Database write operation fails when updating user status
//      * Expected: Save error caught, returns 500
//      */

//     const mockUser = {
//       _id: testUsers[0]._id,
//       status: 'ONLINE',
//       save: jest.fn().mockRejectedValue(new Error('MongoServerError: write failed'))
//     };

//     mockedUser.findById.mockResolvedValueOnce(mockUser as any);

//     const token = generateTestToken(
//       testUsers[0]._id,
//       testUsers[0].email,
//       testUsers[0].googleId
//     );

//     const response = await request(app)
//       .post('/api/auth/logout')
//       .set('Authorization', `Bearer ${token}`);

//     expect(response.status).toBe(500);
//     expect(response.body.message).toContain('write failed');
//   });

//   test('should return 500 when database timeout occurs', async () => {
//     /**
//      * Scenario: Database query exceeds timeout
//      * Expected: Timeout error caught, returns 500
//      */

//     mockedUser.findById.mockRejectedValueOnce(
//       new Error('MongoServerError: operation exceeded time limit')
//     );

//     const token = generateTestToken(
//       testUsers[0]._id,
//       testUsers[0].email,
//       testUsers[0].googleId
//     );

//     const response = await request(app)
//       .post('/api/auth/logout')
//       .set('Authorization', `Bearer ${token}`);

//     expect(response.status).toBe(500);
//     expect(response.body.message).toContain('time limit');
//   });
// });

// describe('GET /api/auth/verify - Database Failures', () => {
//   test('should return 500 when User.findById() fails', async () => {
//     /**
//      * Scenario: Database connection error during user lookup
//      * Expected: Database error caught, returns 500
//      */

//     mockedUser.findById.mockRejectedValueOnce(
//       new Error('MongoNetworkError: connection refused')
//     );

//     const token = generateTestToken(
//       testUsers[0]._id,
//       testUsers[0].email,
//       testUsers[0].googleId
//     );

//     const response = await request(app)
//       .get('/api/auth/verify')
//       .set('Authorization', `Bearer ${token}`);

//     expect(response.status).toBe(500);
//     expect(response.body.message).toContain('connection refused');
//   });

//   test('should return 500 when database timeout occurs', async () => {
//     /**
//      * Scenario: MongoDB query timeout
//      * Expected: Timeout error caught, returns 500
//      */

//     mockedUser.findById.mockRejectedValueOnce(
//       new Error('MongoServerSelectionError: server selection timed out')
//     );

//     const token = generateTestToken(
//       testUsers[0]._id,
//       testUsers[0].email,
//       testUsers[0].googleId
//     );

//     const response = await request(app)
//       .get('/api/auth/verify')
//       .set('Authorization', `Bearer ${token}`);

//     expect(response.status).toBe(500);
//     expect(response.body.message).toContain('timed out');
//   });
// });

// describe('POST /api/auth/fcm-token - Database Failures', () => {
//   test('should return 500 when User.findById() fails', async () => {
//     /**
//      * Scenario: Database error while finding user
//      * Expected: Database error caught, returns 500
//      */

//     mockedUser.findById.mockRejectedValueOnce(
//       new Error('MongoNetworkError: socket closed')
//     );

//     const token = generateTestToken(
//       testUsers[0]._id,
//       testUsers[0].email,
//       testUsers[0].googleId
//     );

//     const response = await request(app)
//       .post('/api/auth/fcm-token')
//       .set('Authorization', `Bearer ${token}`)
//       .send({ fcmToken: 'test-token' });

//     expect(response.status).toBe(500);
//     expect(response.body.message).toContain('socket closed');
//   });

//   test('should return 500 when user.save() fails', async () => {
//     /**
//      * Scenario: Write operation fails when saving FCM token
//      * Expected: Save error caught, returns 500
//      */

//     const mockUser = {
//       _id: testUsers[0]._id,
//       fcmToken: '',
//       save: jest.fn().mockRejectedValue(new Error('MongoServerError: disk full'))
//     };

//     mockedUser.findById.mockResolvedValueOnce(mockUser as any);

//     const token = generateTestToken(
//       testUsers[0]._id,
//       testUsers[0].email,
//       testUsers[0].googleId
//     );

//     const response = await request(app)
//       .post('/api/auth/fcm-token')
//       .set('Authorization', `Bearer ${token}`)
//       .send({ fcmToken: 'new-token' });

//     expect(response.status).toBe(500);
//     expect(response.body.message).toContain('disk full');
//   });

//   test('should return 500 when network partition occurs', async () => {
//     /**
//      * Scenario: Network partition during database operation
//      * Expected: Network error caught, returns 500
//      */

//     mockedUser.findById.mockRejectedValueOnce(
//       new Error('MongoNetworkError: network partition detected')
//     );

//     const token = generateTestToken(
//       testUsers[0]._id,
//       testUsers[0].email,
//       testUsers[0].googleId
//     );

//     const response = await request(app)
//       .post('/api/auth/fcm-token')
//       .set('Authorization', `Bearer ${token}`)
//       .send({ fcmToken: 'test-token' });

//     expect(response.status).toBe(500);
//     expect(response.body.message).toContain('network partition');
//   });
// });

// describe('DELETE /api/auth/account - Database Failures', () => {
//   test('should return 500 when User.findById() fails', async () => {
//     /**
//      * Scenario: Database error when finding user
//      * Expected: Database error caught, returns 500
//      */

//     mockedUser.findById.mockRejectedValueOnce(
//       new Error('MongoNetworkError: no connection available')
//     );

//     const token = generateTestToken(
//       testUsers[0]._id,
//       testUsers[0].email,
//       testUsers[0].googleId
//     );

//     const response = await request(app)
//       .delete('/api/auth/account')
//       .set('Authorization', `Bearer ${token}`);

//     expect(response.status).toBe(500);
//     expect(response.body.message).toContain('no connection available');
//   });

//   test('should return 500 when User.findByIdAndDelete() fails', async () => {
//     /**
//      * Scenario: Delete operation fails (write error)
//      * Expected: Delete error caught, returns 500
//      */

//     const mockUser = {
//       _id: testUsers[0]._id,
//       roomId: null,
//       groupId: null
//     };

//     mockedUser.findById.mockResolvedValueOnce(mockUser as any);
//     mockedUser.findByIdAndDelete.mockRejectedValueOnce(
//       new Error('MongoServerError: delete operation failed')
//     );

//     const token = generateTestToken(
//       testUsers[0]._id,
//       testUsers[0].email,
//       testUsers[0].googleId
//     );

//     const response = await request(app)
//       .delete('/api/auth/account')
//       .set('Authorization', `Bearer ${token}`);

//     expect(response.status).toBe(500);
//     expect(response.body.message).toContain('delete operation failed');
//   });

//   test('should return 500 when replica set is unavailable', async () => {
//     /**
//      * Scenario: MongoDB replica set failure
//      * Expected: Replica set error caught, returns 500
//      */

//     mockedUser.findById.mockRejectedValueOnce(
//       new Error('MongoServerError: replica set not available')
//     );

//     const token = generateTestToken(
//       testUsers[0]._id,
//       testUsers[0].email,
//       testUsers[0].googleId
//     );

//     const response = await request(app)
//       .delete('/api/auth/account')
//       .set('Authorization', `Bearer ${token}`);

//     expect(response.status).toBe(500);
//     expect(response.body.message).toContain('replica set not available');
//   });
// });

// describe('Network Timeout Scenarios', () => {
//   test('should handle database query timeout in logout', async () => {
//     /**
//      * Scenario: MongoDB query exceeds maxTimeMS
//      * Expected: Timeout error caught, returns 500
//      */

//     (mockedUser.findById as jest.Mock).mockImplementationOnce(() => {
//       return new Promise((_, reject) => {
//         setTimeout(() => reject(new Error('MongoServerError: operation exceeded time limit')), 100);
//      });
//     });


//     const token = generateTestToken(
//       testUsers[0]._id,
//       testUsers[0].email,
//       testUsers[0].googleId
//     );

//     const response = await request(app)
//       .post('/api/auth/logout')
//       .set('Authorization', `Bearer ${token}`);

//     expect(response.status).toBe(500);
//     expect(response.body.message).toContain('time limit');
//   });

//   test('should handle cursor timeout in verify', async () => {
//     /**
//      * Scenario: Database cursor timeout
//      * Expected: Cursor timeout error caught, returns 500
//      */

//     mockedUser.findById.mockRejectedValueOnce(
//       new Error('MongoServerError: cursor timeout')
//     );

//     const token = generateTestToken(
//       testUsers[0]._id,
//       testUsers[0].email,
//       testUsers[0].googleId
//     );

//     const response = await request(app)
//       .get('/api/auth/verify')
//       .set('Authorization', `Bearer ${token}`);

//     expect(response.status).toBe(500);
//     expect(response.body.message).toContain('cursor timeout');
//   });
// });