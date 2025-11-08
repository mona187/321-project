import { Response, NextFunction } from 'express';
import jwt from 'jsonwebtoken';
import { AuthRequest } from '../types';

export interface JWTPayload {
  userId: string;
  email: string;
  googleId: string;
  iat?: number;
  exp?: number;
}

export const authMiddleware = async (
  req: AuthRequest,
  res: Response,
  next: NextFunction
): Promise<void> => {
  try {
    // Get token from Authorization header
    const authHeader = req.headers.authorization;

    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      res.status(401).json({
        error: 'Unauthorized',
        message: 'No token provided'
      });
      return;
    }

    // Extract token
    const token = authHeader.substring(7); // Remove 'Bearer ' prefix

    // Verify JWT secret exists
    const jwtSecret = process.env.JWT_SECRET;
    if (!jwtSecret) {
      console.error('JWT_SECRET is not defined in environment variables');
      res.status(500).json({
        error: 'Server Error',
        message: 'Authentication configuration error'
      });
      return;
    }

    // Verify token
    const decoded = jwt.verify(token, jwtSecret) as JWTPayload;

    // Attach user info to request
    req.user = {
      userId: decoded.userId,
      email: decoded.email,
      googleId: decoded.googleId
    };

    next();
  } catch (error) {
    // Check TokenExpiredError first since it extends JsonWebTokenError
    if (error instanceof jwt.TokenExpiredError) {
      res.status(401).json({
        error: 'Unauthorized',
        message: 'Token expired'
      });
      return;
    }

    if (error instanceof jwt.JsonWebTokenError) {
      res.status(401).json({
        error: 'Unauthorized',
        message: 'Invalid token'
      });
      return;
    }

    console.error('Auth middleware error:', error);
    res.status(500).json({
      error: 'Server Error',
      message: 'Authentication failed'
    });
  }
};

// Optional middleware to check if user is authenticated but don't require it
export const optionalAuth = async (
  req: AuthRequest,
  _res: Response,
  next: NextFunction
): Promise<void> => {
  try {
    const authHeader = req.headers.authorization;

    if (authHeader && authHeader.startsWith('Bearer ')) {
      const token = authHeader.substring(7);
      const jwtSecret = process.env.JWT_SECRET;

      if (jwtSecret) {
        const decoded = jwt.verify(token, jwtSecret) as JWTPayload;
        req.user = {
          userId: decoded.userId,
          email: decoded.email,
          googleId: decoded.googleId
        };
      }
    }

    next();
  } catch (error) {
    // If optional auth fails, just continue without user
    next();
  }
};