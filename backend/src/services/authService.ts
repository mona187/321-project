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

  async authenticateUser(idToken: string): Promise<{
    token: string;
    user: any;
    isNewUser: boolean;
  }> {
    const googleUser = await this.verifyGoogleToken(idToken);

    let user: IUser | null = await User.findOne({ googleId: googleUser.googleId });
    let isNewUser = false;

    if (!user) {
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
      }) as IUser;
      isNewUser = true;
    }

    const token = this.generateToken(user._id.toString());

    return {
      token,
      user: this.sanitizeUser(user),
      isNewUser,
    };
  }

  generateToken(userId: string): string {
    const secret = process.env.JWT_SECRET;
    if (!secret) {
      throw new Error('JWT_SECRET not configured');
    }

    // Explicitly cast expiresIn to the correct type
    const expiresIn = (process.env.JWT_EXPIRES_IN || '7d') as jwt.SignOptions['expiresIn'];

    return jwt.sign({ userId }, secret, { expiresIn });
  }


  sanitizeUser(user: IUser) {
    return {
      userId: user._id.toString(),
      name: user.name,
      email: user.email,
      bio: user.bio,
      profilePicture: user.profilePicture,
      contactNumber: user.contactNumber,
      preferences: user.preferences,
      credibilityScore: user.credibilityScore,
      status: user.status,
    };
  }
}