import { Response, NextFunction } from 'express';
import { AuthRequest } from '../types';
import { UserService } from '../services/userService';

const userService = new UserService();

export class UserController {
  /**
   * GET /user/profile/:ids
   * Get user profiles by IDs
   */
  async getUserProfiles(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const { ids } = req.params;
      const userIds = ids.split(',').map(id => id.trim());

      const profiles = await userService.getUserProfiles(userIds);

      res.status(200).json({
        Status: 200,
        Message: {},
        Body: profiles
      });
    } catch (error) {
      next(error);
    }
  }

  /**
   * GET /user/settings
   * Get user settings
   */
  async getUserSettings(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
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

      const settings = await userService.getUserSettings(userId);

      res.status(200).json({
        Status: 200,
        Message: {},
        Body: settings
      });
    } catch (error) {
      next(error);
    }
  }

  /**
   * POST /user/profile
   * Create/update user profile
   */
  async createUserProfile(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
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

      const { name, bio, profilePicture, contactNumber } = req.body;

      const profile = await userService.createUserProfile(userId, {
        name,
        bio,
        profilePicture,
        contactNumber,
      });

      res.status(200).json({
        Status: 200,
        Message: { text: 'Profile updated successfully' },
        Body: profile
      });
    } catch (error) {
      next(error);
    }
  }

  /**
   * POST /user/settings
   * Update user settings
   */
  async updateUserSettings(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
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

      const { name, bio, preference, profilePicture, contactNumber, budget, radiusKm } = req.body;

      const settings = await userService.updateUserSettings(userId, {
        name,
        bio,
        preference,
        profilePicture,
        contactNumber,
        budget,
        radiusKm,
      });

      res.status(200).json({
        Status: 200,
        Message: { text: 'Settings updated successfully' },
        Body: settings
      });
    } catch (error) {
      next(error);
    }
  }

  /**
   * PUT /user/profile
   * Update user profile
   */
  async updateUserProfile(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
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

      const { name, bio, preference, profilePicture, contactNumber, budget, radiusKm } = req.body;

      const profile = await userService.updateUserProfile(userId, {
        name,
        bio,
        preference,
        profilePicture,
        contactNumber,
        budget,
        radiusKm,
      });

      res.status(200).json({
        Status: 200,
        Message: { text: 'Profile updated successfully' },
        Body: profile
      });
    } catch (error) {
      next(error);
    }
  }

  /**
   * DELETE /user/:userId
   * Delete user account
   */
  async deleteUser(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const { userId } = req.params;
      const requesterId = req.user?.userId;

      // Only allow users to delete their own account
      if (userId !== requesterId) {
        res.status(403).json({
          Status: 403,
          Message: { error: 'Forbidden' },
          Body: null
        });
        return;
      }

      const result = await userService.deleteUser(userId);

      res.status(200).json({
        Status: 200,
        Message: { text: 'User deleted successfully' },
        Body: result
      });
    } catch (error) {
      next(error);
    }
  }
}

export const userController = new UserController();