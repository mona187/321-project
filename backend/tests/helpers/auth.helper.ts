// tests/helpers/auth.helper.ts
import jwt from 'jsonwebtoken';
import { JWTPayload } from '../../src/middleware/auth.middleware';

/**
 * Generate a test JWT token for authenticated requests
 */
export function generateTestToken(
  userId: string, 
  email: string = 'test@example.com',
  googleId: string = 'test-google-id-123'
): string {
  const secret = process.env.JWT_SECRET || 'test-jwt-secret-key';
  
  const payload: Omit<JWTPayload, 'iat' | 'exp'> = {
    userId,
    email,
    googleId
  };
  
  return jwt.sign(payload, secret, { expiresIn: '24h' });
}

/**
 * Generate an expired token for testing
 */
export function generateExpiredToken(
  userId: string,
  email: string = 'test@example.com',
  googleId: string = 'test-google-id-123'
): string {
  const secret = process.env.JWT_SECRET || 'test-jwt-secret-key';
  
  const payload: Omit<JWTPayload, 'iat' | 'exp'> = {
    userId,
    email,
    googleId
  };
  
  return jwt.sign(payload, secret, { expiresIn: '-1h' });
}

/**
 * Generate an invalid/malformed token
 */
export function generateInvalidToken(): string {
  return 'invalid.jwt.token.here';
}