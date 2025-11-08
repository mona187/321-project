// tests/with-mocks/auth.mock.test.ts

/**
 * Auth Routes Tests - With Mocking (Uncontrollable Failures)
 * 
 * This test suite covers UNCONTROLLABLE failures:
 * - Database connection errors
 * - Database query timeouts
 * - User save/delete failures
 * - Write operation failures
 * 
 * These failures cannot be reliably triggered in no-mocks tests.
 * We use spies (not mocks) to intercept database calls.
 * 
 * NOTE: Google OAuth success paths are NOT tested here because:
 * - AuthService is instantiated inside controllers (can't easily mock)
 * - Would require module-level mocks that break other tests
 * - Success paths are covered by no-mocks tests and manual integration testing
 */

import request from 'supertest';
import app from '../../src/app';
import User from '../../src/models/User';
import { generateTestToken } from '../helpers/auth.helper';
import { connectDatabase, disconnectDatabase } from '../../src/config/database';
import { seedTestUsers, cleanTestData, TestUser } from '../helpers/seed.helper';

let testUsers: TestUser[];

beforeAll(async () => {
  console.log('\n🚀 Starting Auth Tests (With Mocks - Uncontrollable Failures)...\n');
  await connectDatabase();
  testUsers = await seedTestUsers();
  console.log('✅ Test setup complete.\n');
});

afterAll(async () => {
  console.log('\n🧹 Cleaning up after tests...\n');
  await cleanTestData();
  await disconnectDatabase();
  console.log('✅ Cleanup complete.\n');
});

afterEach(() => {
  jest.restoreAllMocks();
});

describe('POST /api/auth/logout - Database Failures', () => {
  test('should return 500 when User.findById() fails', async () => {
    jest.spyOn(User, 'findById').mockRejectedValueOnce(
      new Error('MongoNetworkError: connection lost')
    );

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .post('/api/auth/logout')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toContain('connection lost');
  });

  test('should return 500 when user.save() fails', async () => {
    const mockUser = {
      _id: testUsers[0]._id,
      status: 'ONLINE',
      save: jest.fn().mockRejectedValue(new Error('MongoServerError: write failed'))
    };

    jest.spyOn(User, 'findById').mockResolvedValueOnce(mockUser as any);

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .post('/api/auth/logout')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toContain('write failed');
  });

  test('should return 500 when database timeout occurs', async () => {
    jest.spyOn(User, 'findById').mockRejectedValueOnce(
      new Error('MongoServerError: operation exceeded time limit')
    );

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .post('/api/auth/logout')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toContain('time limit');
  });

  test('should return 500 when network partition occurs', async () => {
    jest.spyOn(User, 'findById').mockRejectedValueOnce(
      new Error('MongoNetworkError: network partition detected')
    );

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .post('/api/auth/logout')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toContain('network partition');
  });
});

describe('GET /api/auth/verify - Database Failures', () => {
  test('should return 500 when User.findById() fails', async () => {
    jest.spyOn(User, 'findById').mockRejectedValueOnce(
      new Error('MongoNetworkError: connection refused')
    );

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .get('/api/auth/verify')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toContain('connection refused');
  });

  test('should return 500 when database timeout occurs', async () => {
    jest.spyOn(User, 'findById').mockRejectedValueOnce(
      new Error('MongoServerSelectionError: server selection timed out')
    );

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .get('/api/auth/verify')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toContain('timed out');
  });

  test('should return 500 when replica set fails', async () => {
    jest.spyOn(User, 'findById').mockRejectedValueOnce(
      new Error('MongoServerError: replica set not available')
    );

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .get('/api/auth/verify')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toContain('replica set not available');
  });
});

describe('POST /api/auth/fcm-token - Database Failures', () => {
  test('should return 500 when User.findById() fails', async () => {
    jest.spyOn(User, 'findById').mockRejectedValueOnce(
      new Error('MongoNetworkError: socket closed')
    );

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .post('/api/auth/fcm-token')
      .set('Authorization', `Bearer ${token}`)
      .send({ fcmToken: 'test-token' });

    expect(response.status).toBe(500);
    expect(response.body.message).toContain('socket closed');
  });

  test('should return 500 when user.save() fails', async () => {
    const mockUser = {
      _id: testUsers[0]._id,
      fcmToken: '',
      save: jest.fn().mockRejectedValue(new Error('MongoServerError: disk full'))
    };

    jest.spyOn(User, 'findById').mockResolvedValueOnce(mockUser as any);

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .post('/api/auth/fcm-token')
      .set('Authorization', `Bearer ${token}`)
      .send({ fcmToken: 'new-token' });

    expect(response.status).toBe(500);
    expect(response.body.message).toContain('disk full');
  });

  test('should return 500 when network partition occurs', async () => {
    jest.spyOn(User, 'findById').mockRejectedValueOnce(
      new Error('MongoNetworkError: network partition detected')
    );

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .post('/api/auth/fcm-token')
      .set('Authorization', `Bearer ${token}`)
      .send({ fcmToken: 'test-token' });

    expect(response.status).toBe(500);
    expect(response.body.message).toContain('network partition');
  });

  test('should return 500 when cursor timeout occurs', async () => {
    jest.spyOn(User, 'findById').mockRejectedValueOnce(
      new Error('MongoServerError: cursor timeout')
    );

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .post('/api/auth/fcm-token')
      .set('Authorization', `Bearer ${token}`)
      .send({ fcmToken: 'test-token' });

    expect(response.status).toBe(500);
    expect(response.body.message).toContain('cursor timeout');
  });
});

describe('DELETE /api/auth/account - Database Failures', () => {
  test('should return 500 when User.findById() fails', async () => {
    jest.spyOn(User, 'findById').mockRejectedValueOnce(
      new Error('MongoNetworkError: no connection available')
    );

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .delete('/api/auth/account')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toContain('no connection available');
  });

  test('should return 500 when User.findByIdAndDelete() fails', async () => {
    const mockUser = {
      _id: testUsers[0]._id,
      roomId: null,
      groupId: null
    };

    jest.spyOn(User, 'findById').mockResolvedValueOnce(mockUser as any);
    jest.spyOn(User, 'findByIdAndDelete').mockRejectedValueOnce(
      new Error('MongoServerError: delete operation failed')
    );

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .delete('/api/auth/account')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toContain('delete operation failed');
  });

  test('should return 500 when replica set is unavailable', async () => {
    jest.spyOn(User, 'findById').mockRejectedValueOnce(
      new Error('MongoServerError: replica set not available')
    );

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .delete('/api/auth/account')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toContain('replica set not available');
  });

  test('should return 500 when database connection is lost', async () => {
    jest.spyOn(User, 'findById').mockRejectedValueOnce(
      new Error('MongoNetworkError: connection closed')
    );

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .delete('/api/auth/account')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toContain('connection closed');
  });
});

describe('Network Timeout Scenarios', () => {
  test('should handle database query timeout in logout', async () => {
    jest.spyOn(User, 'findById').mockRejectedValueOnce(
      new Error('MongoServerError: operation exceeded time limit')
    );

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .post('/api/auth/logout')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toContain('time limit');
  });

  test('should handle cursor timeout in verify', async () => {
    jest.spyOn(User, 'findById').mockRejectedValueOnce(
      new Error('MongoServerError: cursor timeout')
    );

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .get('/api/auth/verify')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toContain('cursor timeout');
  });

  test('should handle connection pool exhaustion', async () => {
    jest.spyOn(User, 'findById').mockRejectedValueOnce(
      new Error('MongoServerError: connection pool exhausted')
    );

    const token = generateTestToken(
      testUsers[0]._id,
      testUsers[0].email,
      testUsers[0].googleId
    );

    const response = await request(app)
      .post('/api/auth/logout')
      .set('Authorization', `Bearer ${token}`);

    expect(response.status).toBe(500);
    expect(response.body.message).toContain('connection pool exhausted');
  });
});