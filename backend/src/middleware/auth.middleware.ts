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

/**
 * Middleware to require authentication
 */
export const authMiddleware = async (
  req: AuthRequest,
  res: Response,
  next: NextFunction
): Promise<void> => {
  try {
    const authHeader = req.headers.authorization;

    if (!authHeader?.startsWith('Bearer ')) {
      res.status(401).json({
        error: 'Unauthorized',
        message: 'No token provided'
      });
      return; // <- void return
    }

    const token = authHeader.substring(7);

    const jwtSecret = process.env.JWT_SECRET;
    if (!jwtSecret) {
      console.error('JWT_SECRET is not defined in environment variables');
      res.status(500).json({
        error: 'Server Error',
        message: 'Authentication configuration error'
      });
      return; // <- void return
    }

    let decoded: JWTPayload;
    try {
      decoded = jwt.verify(token, jwtSecret) as JWTPayload;
    } catch (err) {
      // Only handle JWT-specific errors (JsonWebTokenError, TokenExpiredError, etc.)
      // Re-throw non-JWT errors to be caught by outer catch block (returns 500)
      if (err instanceof jwt.JsonWebTokenError) {
        if (err instanceof jwt.TokenExpiredError) {
          res.status(401).json({ error: 'Unauthorized', message: 'Token expired' });
          return; // <- void return
        }
        res.status(401).json({ error: 'Unauthorized', message: 'Invalid token' });
        return; // <- void return
      }
      // Re-throw non-JWT errors to outer catch block
      throw err;
    }

    // Attach safe user info to request
    req.user = {
      userId: decoded.userId,
      email: decoded.email,
      googleId: decoded.googleId
    };

    next();
  } catch (error) {
    console.error('Auth middleware error:', error);
    res.status(500).json({
      error: 'Server Error',
      message: 'Authentication failed'
    });
    return; // <- void return
  }
};

/**
 * Optional authentication middleware
 * Attaches user info if token exists and is valid
 */
export const optionalAuth = async (
  req: AuthRequest,
  _res: Response,
  next: NextFunction
): Promise<void> => {
  try {
    const authHeader = req.headers.authorization;

    if (authHeader?.startsWith('Bearer ')) {
      const token = authHeader.substring(7);
      const jwtSecret = process.env.JWT_SECRET;

      if (jwtSecret) {
        try {
          const decoded = jwt.verify(token, jwtSecret) as JWTPayload;
          req.user = {
            userId: decoded.userId,
            email: decoded.email,
            googleId: decoded.googleId
          };
        } catch {
          // Ignore invalid/expired token for optional auth
        }
      }
    }

    next(); // <- void return
  } catch (error) {
    console.error('Optional auth middleware error:', error);
    next(); // <- void return
  }
};
