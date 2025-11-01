// tests/setup.ts
import dotenv from 'dotenv';

// Increase timeout for all tests (if needed)
jest.setTimeout(10000);

// Global test setup
beforeAll(() => {
  console.log('Starting test suite...');
});

// Global test cleanup
afterAll(() => {
  console.log('Test suite completed.');
});

// Load test environment variables
dotenv.config({ path: '.env.test' });

// Set test environment
process.env.NODE_ENV = 'test';
process.env.JWT_SECRET = process.env.JWT_SECRET || 'test-jwt-secret-key';