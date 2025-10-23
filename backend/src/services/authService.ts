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

  async authenticateUser(
    idToken: string,
    isSignup: boolean = false
  ): Promise<{
    token: string;
    user: any;
    isNewUser: boolean;
    message?: string;
  }> {
    const googleUser = await this.verifyGoogleToken(idToken);

    let user = await User.findOne({ email: googleUser.email });

    // -------------------------------------------
    // CASE 1: no user in DB
    // -------------------------------------------
    if (!user) {
      if (isSignup) {
        // ✅ Create a new account (signup flow)
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

        // ✅ Return signup confirmation (no token yet)
        return {
          token: '',
          user: null,
          isNewUser: true,
          message: 'Account created successfully! Please sign in to continue.',
        };
      } else {
        // ❌ Trying to sign in without an account
        throw new Error('No account found! Please sign up first.');
      }
    }

    // -------------------------------------------
    // CASE 2: user already exists
    // -------------------------------------------
    if (isSignup) {
      // ❌ Trying to sign up again
      throw new Error('You already have an account! Please sign in instead.');
    }

    // ✅ Valid sign-in flow
    user.status = UserStatus.ONLINE;
    await user.save();

    const token = this.generateToken(user._id.toString());

    return {
      token,
      user: this.sanitizeUser(user),
      isNewUser: false,
      message: 'Signed in successfully!',
    };
  }

  // -------------------------------------------
  // Helpers
  // -------------------------------------------

  generateToken(userId: string): string {
    const secret = process.env.JWT_SECRET;
    if (!secret) {
      throw new Error('JWT_SECRET not configured');
    }
    return jwt.sign({ userId }, secret, { expiresIn: '7d' });
  }

  sanitizeUser(user: any) {
    return {
      userId: parseInt(user._id.toString().slice(-6), 16),
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
