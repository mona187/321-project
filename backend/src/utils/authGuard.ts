import { Response } from 'express';
import { AuthRequest } from '../types';

/**
 * Type guard to ensure user exists on request
 * Use after authMiddleware to safely access req.user
 * 
 * @example
 * if (!ensureAuthenticated(req, res)) return;
 * const userId = req.user.userId; // TypeScript knows user is defined
 */
export function ensureAuthenticated(
  req: AuthRequest,
  res: Response
): req is AuthRequest & { user: { userId: string; email: string; googleId: string } } {
  if (!req.user) {
    res.status(401).json({
      Status: 401,
      Message: { error: 'Unauthorized - User not authenticated' },
      Body: null
    });
    return false;
  }
  return true;
}