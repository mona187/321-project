import { OAuth2Client } from 'google-auth-library';
import jwt, { SignOptions } from 'jsonwebtoken';
import { User } from '../models/User';
import { AppError } from '../middleware/errorHandler';
import { IUser } from '../types';

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
      if (!payload) {
        throw new AppError(401, 'Invalid Google token');
      }

      return {
        googleId: payload.sub,
        email: payload.email!,
        name: payload.name!,
        picture: payload.picture,
      };
    } catch (error) {
      throw new AppError(401, 'Failed to verify Google token');
    }
  }

  async authenticateUser(idToken: string, isSignup: boolean = false): Promise<{
    token: string;
    user: any;
    isNewUser: boolean;
    message?: string;
  }> {
    const googleUser = await this.verifyGoogleToken(idToken);

    let user: IUser | null = await User.findOne({ googleId: googleUser.googleId });
    let isNewUser = false;

    if (!user) {
      if (isSignup) {
        // Only create new user if it's a signup request
        user = await User.create({
          googleId: googleUser.googleId,
          email: googleUser.email,
          name: googleUser.name,
          profilePicture: googleUser.picture,
          preferences: {
            cuisineTypes: [],
            budget: 50,
            radiusKm: 10,
          },
          credibilityScore: 5.0,
          location: {
            type: 'Point',
            coordinates: [0, 0] // Default coordinates - user can update later
          },
        }) as IUser;
        isNewUser = true;
        
        // For signup, don't return token - just confirm account creation
        return {
          token: '', // No token for signup
          user: null, // No user data for signup
          isNewUser: true,
          message: 'Account created successfully! Please sign in to continue.'
        };
      } else {
        // For signin, user must exist
        throw new AppError(404, 'No account found! Please sign up first.');
      }
    } else {
      // User exists
      if (isSignup) {
        // If user exists and they're trying to signup, throw error
        throw new AppError(409, 'You already have an account! Please sign in instead.');
      } else {
        // For signin, update their info if needed and return token
        await User.findByIdAndUpdate(user._id, {
          name: googleUser.name,
          profilePicture: googleUser.picture,
        });
        // Refresh user data
        user = await User.findById(user._id) as IUser;
        
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

    const expiresIn = (process.env.JWT_EXPIRES_IN || '7d') as jwt.SignOptions['expiresIn'];
    return jwt.sign({ userId }, secret, { expiresIn });
  }

  sanitizeUser(user: IUser) {
    return {
      userId: parseInt(user._id.toString().slice(-6), 16), // Use only last 6 chars to fit in Java int
      name: user.name,
      bio: user.bio || '',
      preference: user.preferences.cuisineTypes.join(','), // Convert array to string
      profilePicture: user.profilePicture,
      credibilityScore: user.credibilityScore,
      contactNumber: user.contactNumber,
      budget: user.preferences.budget,
      radiusKm: user.preferences.radiusKm,
      status: user.status === 'active' ? 1 : user.status === 'in_waiting_room' ? 2 : user.status === 'in_group' ? 3 : 0, // Convert to int
      roomId: user.currentRoomId,
      groupId: user.currentGroupId,
    };
  }
}