import { Response, NextFunction } from 'express';
import { AuthRequest } from '../types';
import groupService from '../services/groupService';

export class GroupController {
  /**
   * GET /api/group/status
   * Get current user's group status
   */
  async getGroupStatus(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const userId = req.user?.userId;

      if (!userId) {
        res.status(401).json({
          Status: 401,
          Message: { error: 'Unauthorized' },
          Body: null
        });
        return;
      }

      // Get user's current group
      const group = await groupService.getGroupByUserId(userId);

      if (!group) {
        res.status(404).json({
          Status: 404,
          Message: { error: 'Not in a group' },
          Body: null
        });
        return;
      }

      const status = await groupService.getGroupStatus(group._id.toString());

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
   * POST /api/group/vote/:groupId
   * Vote for a restaurant
   */
  async voteForRestaurant(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const userId = req.user?.userId;
      const { groupId } = req.params;
      const { restaurantID, restaurant } = req.body;

      if (!userId) {
        res.status(401).json({
          Status: 401,
          Message: { error: 'Unauthorized' },
          Body: null
        });
        return;
      }

      if (!restaurantID) {
        res.status(400).json({
          Status: 400,
          Message: { error: 'Restaurant ID is required' },
          Body: null
        });
        return;
      }

      const result = await groupService.voteForRestaurant(
        userId,
        groupId,
        restaurantID,
        restaurant
      );

      res.status(200).json({
        Status: 200,
        Message: { text: result.message },
        Body: { Current_votes: result.Current_votes }
      });
    } catch (error) {
      next(error);
    }
  }

  /**
   * POST /api/group/leave/:groupId
   * Leave a group
   */
  async leaveGroup(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const userId = req.user?.userId;
      const { groupId } = req.params;

      if (!userId) {
        res.status(401).json({
          Status: 401,
          Message: { error: 'Unauthorized' },
          Body: null
        });
        return;
      }

      await groupService.leaveGroup(userId, groupId);

      res.status(200).json({
        Status: 200,
        Message: { text: 'Successfully left group' },
        Body: { groupId }
      });
    } catch (error) {
      next(error);
    }
  }
}

export const groupController = new GroupController();