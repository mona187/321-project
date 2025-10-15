import { Response, NextFunction } from 'express';
import { AuthRequest } from '../middleware/auth.middleware';
import { GroupService } from '../services/groupService';
import { RestaurantService } from '../services/restaurantService';
import { CredibilityService } from '../services/credibilityService';

const groupService = new GroupService();
const restaurantService = new RestaurantService();
const credibilityService = new CredibilityService();

export class GroupController {
  async getGroupStatus(req: AuthRequest, res: Response, next: NextFunction) {
    try {
      const { groupId } = req.params;

      const status = await groupService.getGroupStatus(groupId);

      res.status(200).json(status);
    } catch (error) {
      next(error);
    }
  }

  async leaveGroup(req: AuthRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.userId!;
      const { groupId } = req.params;

      const result = await groupService.leaveGroup(userId, groupId);

      res.status(200).json(result);
    } catch (error) {
      next(error);
    }
  }

  async voteRestaurant(req: AuthRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.userId!;
      const { groupId } = req.params;
      const { restaurantId } = req.body;

      if (!restaurantId) {
        res.status(400).json({ error: 'Restaurant ID is required' });
        return;
      }

      const result = await groupService.voteRestaurant(
        userId,
        groupId,
        restaurantId
      );

      res.status(200).json(result);
    } catch (error) {
      next(error);
    }
  }

  async getGroupMembers(req: AuthRequest, res: Response, next: NextFunction) {
    try {
      const { groupId } = req.params;

      const members = await groupService.getGroupMembers(groupId);

      res.status(200).json(members);
    } catch (error) {
      next(error);
    }
  }

  async checkIn(req: AuthRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.userId!;
      const { groupId } = req.params;

      const result = await credibilityService.checkIn(userId, groupId);

      res.status(200).json(result);
    } catch (error) {
      next(error);
    }
  }
}