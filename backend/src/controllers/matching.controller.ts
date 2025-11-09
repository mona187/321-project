import { Response, NextFunction } from 'express';
import { AuthRequest } from '../types';
import matchingService from '../services/matchingService';
import { requireParam } from '../middleware/errorHandler';
import { ensureAuthenticated } from '../utils/authGuard';

export class MatchingController {
  /**
   * POST /api/matching/join
   * Join the matching pool
   */
  async joinMatching(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      if (!ensureAuthenticated(req, res)) return;
      const userId = req.user.userId;

      const { cuisine, budget, radiusKm } = req.body;

      const result = await matchingService.joinMatching(userId, {
        cuisine,
        budget,
        radiusKm,
      });

      res.status(200).json({
        Status: 200,
        Message: { text: 'Successfully joined matching' },
        Body: result
      });
    } catch (error) {
      next(error);
    }
  }

  /**
   * POST /api/matching/join/:roomId
   * Join a specific room
   */
  async joinSpecificRoom(_req: AuthRequest, res: Response, _next: NextFunction): Promise<void> {
    // This functionality might not be needed based on your specs
    // But keeping it for flexibility
    res.status(501).json({
      Status: 501,
      Message: { error: 'Not implemented - use /api/matching/join instead' },
      Body: null
    });
  }

  /**
   * PUT /api/matching/leave/:roomId
   * Leave a waiting room
   */
  async leaveRoom(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      if (!ensureAuthenticated(req, res)) return;
      const userId = req.user.userId;
      
      const roomId = requireParam(req, 'roomId');

      await matchingService.leaveRoom(userId, roomId);

      res.status(200).json({
        Status: 200,
        Message: { text: 'Successfully left room' },
        Body: { roomId }
      });
    } catch (error) {
      next(error);
    }
  }

  /**
   * GET /api/matching/status/:roomId
   * Get status of a waiting room
   */
  async getRoomStatus(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const roomId = requireParam(req, 'roomId');

      const status = await matchingService.getRoomStatus(roomId);

      res.status(200).json({
        Status: 200,
        Message: {},
        Body: status
      });
    } catch (error) {
      next(error);
    }
  }

  /**
   * GET /api/matching/users/:roomId
   * Get users in a room
   */
  async getRoomUsers(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const roomId = requireParam(req, 'roomId');

      const users = await matchingService.getRoomUsers(roomId);

      res.status(200).json({
        Status: 200,
        Message: {},
        Body: {
          roomID: roomId,
          Users: users
        }
      });
    } catch (error) {
      next(error);
    }
  }
}

export const matchingController = new MatchingController();