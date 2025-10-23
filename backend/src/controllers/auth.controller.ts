import { Response, NextFunction } from 'express';
import { OAuth2Client } from 'google-auth-library';
import jwt from 'jsonwebtoken';
import User, { UserStatus } from '../models/User';
import { AuthRequest } from '../types';

const client = new OAuth2Client(process.env.GOOGLE_CLIENT_ID);

export class AuthController {
  /**
   * POST /api/auth/signup
   * Register new user with Google ID token
   */
  async googleSignup(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const { idToken } = req.body;

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

      // Check if user already exists
      const existingUser = await User.findOne({ googleId });
      if (existingUser) {
        res.status(400).json({
          error: 'Bad Request',
          message: 'User already exists. Please use sign in instead.'
        });
        return;
      }

      // Create new user
      const user = await User.create({
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

      // Prepare response in frontend-compatible format
      const response = {
        message: 'Signup successful',
        data: {
          token,
          user: {
            userId: parseInt(user._id.toString().slice(-6), 16), // Convert to int-like format
            name: user.name,
            bio: user.bio || '',
            preference: user.preference || [],
            profilePicture: user.profilePicture || '',
            credibilityScore: user.credibilityScore,
            contactNumber: user.contactNumber || '',
            budget: user.budget,
            radiusKm: user.radiusKm,
            status: user.status,
            roomId: user.roomId || '',
            groupId: user.groupId || ''
          }
        }
      };

      res.status(201).json(response);
    } catch (error) {
      next(error);
    }
  }

  /**
   * POST /api/auth/signin
   * Sign in existing user with Google ID token
   */
  async googleSignin(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const { idToken } = req.body;

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

      const { sub: googleId } = payload;

      // Find existing user
      const user = await User.findOne({ googleId });
      if (!user) {
        res.status(404).json({
          error: 'Not Found',
          message: 'User not found. Please sign up first.'
        });
        return;
      }

      // Update last login status
      user.status = UserStatus.ONLINE;
      await user.save();
      console.log(`✅ User logged in: ${user._id}`);

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

      // Prepare response in frontend-compatible format
      const response = {
        message: 'Sign in successful',
        data: {
          token,
          user: {
            userId: parseInt(user._id.toString().slice(-6), 16), // Convert to int-like format
            name: user.name,
            bio: user.bio || '',
            preference: user.preference || [],
            profilePicture: user.profilePicture || '',
            credibilityScore: user.credibilityScore,
            contactNumber: user.contactNumber || '',
            budget: user.budget,
            radiusKm: user.radiusKm,
            status: user.status,
            roomId: user.roomId || '',
            groupId: user.groupId || ''
          }
        }
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
        message: 'Logged out successfully',
        data: null
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
        message: 'Token verified successfully',
        data: {
          user: {
            userId: parseInt(user._id.toString().slice(-6), 16), // Convert to int-like format
            name: user.name,
            bio: user.bio || '',
            preference: user.preference || [],
            profilePicture: user.profilePicture || '',
            credibilityScore: user.credibilityScore,
            contactNumber: user.contactNumber || '',
            budget: user.budget,
            radiusKm: user.radiusKm,
            status: user.status,
            roomId: user.roomId || '',
            groupId: user.groupId || ''
          }
        }
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
        message: 'FCM token updated successfully',
        data: null
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
        message: 'Account deleted successfully',
        data: null
      });
    } catch (error) {
      next(error);
    }
  }
}

export const authController = new AuthController();