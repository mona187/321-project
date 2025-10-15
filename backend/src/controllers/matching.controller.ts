import { Response, NextFunction } from 'express';
import { AuthRequest } from '../middleware/auth.middleware';
import { MatchingService } from '../services/matchingService';

const matchingService = new MatchingService();

export class MatchingController {
  async joinMatching(req: AuthRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.userId!;
      const { longitude, latitude } = req.body;

      if (longitude === undefined || latitude === undefined) {
        res.status(400).json({ error: 'Location is required' });
        return;
      }

      const result = await matchingService.joinMatching(userId, {
        longitude,
        latitude,
      });

      res.status(200).json(result);
    } catch (error) {
      next(error);
    }
  }

  async joinSpecificRoom(req: AuthRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.userId!;
      const { roomId } = req.params;

      const result = await matchingService.joinSpecificRoom(userId, roomId);

      res.status(200).json(result);
    } catch (error) {
      next(error);
    }
  }

  async leaveRoom(req: AuthRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.userId!;
      const { roomId } = req.params;

      const result = await matchingService.leaveRoom(userId, roomId);

      res.status(200).json(result);
    } catch (error) {
      next(error);
    }
  }

  async getRoomStatus(req: AuthRequest, res: Response, next: NextFunction) {
    try {
      const { roomId } = req.params;

      const status = await matchingService.getRoomStatus(roomId);

      res.status(200).json(status);
    } catch (error) {
      next(error);
    }
  }

  async getRoomUsers(req: AuthRequest, res: Response, next: NextFunction) {
    try {
      const { roomId } = req.params;

      const users = await matchingService.getRoomUsers(roomId);

      res.status(200).json(users);
    } catch (error) {
      next(error);
    }
  }
}