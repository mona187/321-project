import { Response, NextFunction } from 'express';
import { OAuth2Client } from 'google-auth-library';
import jwt from 'jsonwebtoken';
import User, { UserStatus } from '../models/User';
import { AuthRequest, GoogleAuthRequest, AuthResponse } from '../types';

const client = new OAuth2Client(process.env.GOOGLE_CLIENT_ID);

export class AuthController {
  /**
   * POST /api/auth/google
   * Exchange Google ID token for JWT
   */
  async googleAuth(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const { idToken } = req.body as GoogleAuthRequest;

      if (!idToken) {
        res.status(400).json({
          error: 'Bad Request',
          message: 'Google ID token is required'
        });
        return;
      }

      // Verify Google token
      const ticket = await client.verifyIdToken({
        idToken,
        audience: process.env.GOOGLE_CLIENT_ID,
      });

      const payload = ticket.getPayload();

      if (!payload || !payload.sub || !payload.email) {
        res.status(401).json({
          error: 'Unauthorized',
          message: 'Invalid Google token'
        });
        return;
      }

      const { sub: googleId, email, name, picture } = payload;

      // Find or create user
      let user = await User.findOne({ googleId });

      if (!user) {
        // Create new user
        user = await User.create({
          googleId,
          email,
          name: name || 'User',
          profilePicture: picture || '',
          status: UserStatus.ONLINE,
          preference: [],
          credibilityScore: 100,
          budget: 0,
          radiusKm: 5,
        });

        console.log(`✅ New user created: ${user._id}`);
      } else {
        // Update last login status
        user.status = UserStatus.ONLINE;
        await user.save();
        console.log(`✅ User logged in: ${user._id}`);
      }

      // Generate JWT
      const jwtSecret = process.env.JWT_SECRET;
      if (!jwtSecret) {
        res.status(500).json({
          error: 'Server Error',
          message: 'JWT configuration error'
        });
        return;
      }

      const token = jwt.sign(
        {
          userId: user._id.toString(),
          email: user.email,
          googleId: user.googleId,
        },
        jwtSecret,
        { expiresIn: '7d' }
      );

      // Prepare response
      const response: AuthResponse = {
        token,
        user: {
          userId: user._id.toString(),
          name: user.name,
          email: user.email,
          profilePicture: user.profilePicture,
          credibilityScore: user.credibilityScore,
        },
      };

      res.status(200).json(response);
    } catch (error) {
      next(error);
    }
  }

  /**
   * POST /api/auth/logout
   * Logout user (set status to offline)
   */
  async logout(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      if (!req.user) {
        res.status(401).json({
          error: 'Unauthorized',
          message: 'Not authenticated'
        });
        return;
      }

      const user = await User.findById(req.user.userId);

      if (user) {
        user.status = UserStatus.OFFLINE;
        await user.save();
      }

      res.status(200).json({
        message: 'Logged out successfully'
      });
    } catch (error) {
      next(error);
    }
  }

  /**
   * GET /api/auth/verify
   * Verify JWT token and return user info
   */
  async verifyToken(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      if (!req.user) {
        res.status(401).json({
          error: 'Unauthorized',
          message: 'Invalid token'
        });
        return;
      }

      const user = await User.findById(req.user.userId);

      if (!user) {
        res.status(404).json({
          error: 'Not Found',
          message: 'User not found'
        });
        return;
      }

      res.status(200).json({
        user: {
          userId: user._id.toString(),
          name: user.name,
          email: user.email,
          profilePicture: user.profilePicture,
          credibilityScore: user.credibilityScore,
          status: user.status,
        },
      });
    } catch (error) {
      next(error);
    }
  }

  /**
   * POST /api/auth/fcm-token
   * Update user's FCM token for push notifications
   */
  async updateFCMToken(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      if (!req.user) {
        res.status(401).json({
          error: 'Unauthorized',
          message: 'Not authenticated'
        });
        return;
      }

      const { fcmToken } = req.body;

      if (!fcmToken) {
        res.status(400).json({
          error: 'Bad Request',
          message: 'FCM token is required'
        });
        return;
      }

      const user = await User.findById(req.user.userId);

      if (!user) {
        res.status(404).json({
          error: 'Not Found',
          message: 'User not found'
        });
        return;
      }

      user.fcmToken = fcmToken;
      await user.save();

      res.status(200).json({
        message: 'FCM token updated successfully'
      });
    } catch (error) {
      next(error);
    }
  }

  /**
   * DELETE /api/auth/account
   * Delete user account
   */
  async deleteAccount(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      if (!req.user) {
        res.status(401).json({
          error: 'Unauthorized',
          message: 'Not authenticated'
        });
        return;
      }

      const user = await User.findById(req.user.userId);

      if (!user) {
        res.status(404).json({
          error: 'Not Found',
          message: 'User not found'
        });
        return;
      }

      if (user.roomId || user.groupId) {
        res.status(400).json({
          error: 'Bad Request',
          message: 'Cannot delete account while in a room or group. Please leave first.'
        });
        return;
      }

      await User.findByIdAndDelete(req.user.userId);

      res.status(200).json({
        message: 'Account deleted successfully'
      });
    } catch (error) {
      next(error);
    }
  }
}

export const authController = new AuthController();