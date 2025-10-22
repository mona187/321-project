import { OAuth2Client } from 'google-auth-library';
import jwt from 'jsonwebtoken';
import User, { UserStatus } from '../models/User';

const client = new OAuth2Client(process.env.GOOGLE_CLIENT_ID);

export class AuthService {
  async verifyGoogleToken(idToken: string): Promise<{
    googleId: string;
    email: string;
    name: string;
    picture?: string;
  }> {
    try {
      const ticket = await client.verifyIdToken({
        idToken,
        audience: process.env.GOOGLE_CLIENT_ID,
      });

      const payload = ticket.getPayload();
      if (!payload || !payload.sub || !payload.email) {
        throw new Error('Invalid Google token');
      }

      return {
        googleId: payload.sub,
        email: payload.email,
        name: payload.name || 'User',
        picture: payload.picture,
      };
    } catch (error) {
      throw new Error('Failed to verify Google token');
    }
  }

  async authenticateUser(idToken: string, isSignup: boolean = false): Promise<{
    token: string;
    user: any;
    isNewUser: boolean;
    message?: string;
  }> {
    const googleUser = await this.verifyGoogleToken(idToken);

    let user = await User.findOne({ googleId: googleUser.googleId });

    if (!user) {
      if (isSignup) {
        // Only create new user if it's a signup request
        user = await User.create({
          googleId: googleUser.googleId,
          email: googleUser.email,
          name: googleUser.name,
          profilePicture: googleUser.picture || '',
          preference: [],
          credibilityScore: 100,
          budget: 0,
          radiusKm: 5,
          status: UserStatus.ONLINE,
        });
        
        const token = this.generateToken(user._id.toString());
        return {
          token,
          user: this.sanitizeUser(user),
          isNewUser: true,
        };
      } else {
        // For signin, user must exist
        throw new Error('No account found! Please sign up first.');
      }
    } else {
      // User exists
      if (isSignup) {
        // If user exists and they're trying to signup, throw error
        throw new Error('You already have an account! Please sign in instead.');
      } else {
        // For signin, update their info if needed and return token
        user.status = UserStatus.ONLINE;
        await user.save();
        
        const token = this.generateToken(user._id.toString());
        return {
          token,
          user: this.sanitizeUser(user),
          isNewUser: false,
        };
      }
    }
  }

  generateToken(userId: string): string {
    const secret = process.env.JWT_SECRET;
    if (!secret) {
      throw new Error('JWT_SECRET not configured');
    }

    return jwt.sign({ userId }, secret, { expiresIn: '7d' });
  }

  sanitizeUser(user: any) {
    return {
      userId: parseInt(user._id.toString().slice(-6), 16), // Convert to int-like format (smaller number)
      name: user.name,
      bio: user.bio || '',
      preference: user.preference || [],
      profilePicture: user.profilePicture || '',
      credibilityScore: user.credibilityScore,
      contactNumber: user.contactNumber || '',
      budget: user.budget || 0,
      radiusKm: user.radiusKm || 5,
      status: user.status,
      roomId: user.roomId || '',
      groupId: user.groupId || '',
    };
  }
}