import { Response, NextFunction } from 'express';
import { AuthRequest } from '../middleware/auth.middleware';
import { UserService } from '../services/userService';

const userService = new UserService();

export class UserController {
  async getProfile(req: AuthRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.userId!;
      const profile = await userService.getUserProfile(userId);

      res.status(200).json(profile);
    } catch (error) {
      next(error);
    }
  }

  async getSettings(req: AuthRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.userId!;
      const settings = await userService.getUserSettings(userId);

      res.status(200).json(settings);
    } catch (error) {
      next(error);
    }
  }

  async updateProfile(req: AuthRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.userId!;
      const { name, bio, profilePicture, contactNumber } = req.body;

      const updatedUser = await userService.updateUserProfile(userId, {
        name,
        bio,
        profilePicture,
        contactNumber,
      });

      res.status(200).json({
        message: 'Profile updated successfully',
        user: updatedUser,
      });
    } catch (error) {
      next(error);
    }
  }

  async updateSettings(req: AuthRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.userId!;
      const data = req.body;

      const updatedUser = await userService.updateUserSettings(userId, data);

      res.status(200).json({
        message: 'Settings updated successfully',
        user: updatedUser,
      });
    } catch (error) {
      next(error);
    }
  }

  async deleteUser(req: AuthRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.userId!;
      await userService.deleteUser(userId);

      res.status(200).json({
        message: 'User deleted successfully',
      });
    } catch (error) {
      next(error);
    }
  }

  async updateLocation(req: AuthRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.userId!;
      const { longitude, latitude } = req.body;

      if (longitude === undefined || latitude === undefined) {
        res.status(400).json({ error: 'Longitude and latitude required' });
        return;
      }

      const updatedUser = await userService.updateUserLocation(
        userId,
        longitude,
        latitude
      );

      res.status(200).json({
        message: 'Location updated successfully',
        location: updatedUser.location,
      });
    } catch (error) {
      next(error);
    }
  }

  async updateFCMToken(req: AuthRequest, res: Response, next: NextFunction) {
    try {
      const userId = req.userId!;
      const { fcmToken } = req.body;

      if (!fcmToken) {
        res.status(400).json({ error: 'FCM token is required' });
        return;
      }

      await userService.updateFCMToken(userId, fcmToken);

      res.status(200).json({
        message: 'FCM token updated successfully',
      });
    } catch (error) {
      next(error);
    }
  }
}