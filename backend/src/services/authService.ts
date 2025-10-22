import { OAuth2Client } from 'google-auth-library';
import jwt from 'jsonwebtoken';
import User, { UserStatus } from '../models/User';
import { AppError } from '../middleware/errorHandler';

export class AuthService {
  private googleClient: OAuth2Client;

  constructor() {
    this.googleClient = new OAuth2Client(process.env.GOOGLE_CLIENT_ID);
  }

  /**
   * Verify Google ID token
   */
  async verifyGoogleToken(idToken: string): Promise<{
    googleId: string;
    email: string;
    name: string;
    picture?: string;
  }> {
    try {
      const ticket = await this.googleClient.verifyIdToken({
        idToken,
        audience: process.env.GOOGLE_CLIENT_ID,
      });

      const payload = ticket.getPayload();

      if (!payload || !payload.sub || !payload.email) {
        throw new AppError('Invalid Google token', 401);
      }

      return {
        googleId: payload.sub,
        email: payload.email,
        name: payload.name || 'User',
        picture: payload.picture,
      };
    } catch (error) {
      throw new AppError('Failed to verify Google token', 401);
    }
  }

  /**
   * Find or create user from Google data
   */
  async findOrCreateUser(googleData: {
    googleId: string;
    email: string;
    name: string;
    picture?: string;
  }): Promise<any> {
    let user = await User.findOne({ googleId: googleData.googleId });

    if (!user) {
      // Create new user
      user = await User.create({
        googleId: googleData.googleId,
        email: googleData.email,
        name: googleData.name,
        profilePicture: googleData.picture || '',
        status: UserStatus.ONLINE,
        preference: [],
        credibilityScore: 100,
        budget: 0,
        radiusKm: 5,
      });

      console.log(`✅ New user created: ${user._id}`);
    } else {
      // Update existing user
      user.status = UserStatus.ONLINE;
      if (googleData.picture) {
        user.profilePicture = googleData.picture;
      }
      await user.save();
      console.log(`✅ User logged in: ${user._id}`);
    }

    return user;
  }

  /**
   * Generate JWT token
   */
  generateToken(user: any): string {
    const jwtSecret = process.env.JWT_SECRET;
    
    if (!jwtSecret) {
      throw new AppError('JWT configuration error', 500);
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

    return token;
  }

  /**
   * Verify JWT token
   */
  verifyToken(token: string): {
    userId: string;
    email: string;
    googleId: string;
  } {
    const jwtSecret = process.env.JWT_SECRET;
    
    if (!jwtSecret) {
      throw new AppError('JWT configuration error', 500);
    }

    try {
      const decoded = jwt.verify(token, jwtSecret) as {
        userId: string;
        email: string;
        googleId: string;
      };

      return decoded;
    } catch (error) {
      throw new AppError('Invalid or expired token', 401);
    }
  }

  /**
   * Logout user
   */
  async logoutUser(userId: string): Promise<void> {
    const user = await User.findById(userId);

    if (user) {
      user.status = UserStatus.OFFLINE;
      await user.save();
    }
  }

  /**
   * Update FCM token
   */
  async updateFCMToken(userId: string, fcmToken: string): Promise<void> {
    const user = await User.findById(userId);

    if (!user) {
      throw new AppError('User not found', 404);
    }

    user.fcmToken = fcmToken;
    await user.save();
  }

  /**
   * Delete user account
   */
  async deleteAccount(userId: string): Promise<void> {
    const user = await User.findById(userId);

    if (!user) {
      throw new AppError('User not found', 404);
    }

    // Check if user is in a room or group
    if (user.roomId || user.groupId) {
      throw new AppError('Cannot delete account while in a room or group', 400);
    }

    await User.findByIdAndDelete(userId);
  }
}

export default new AuthService();