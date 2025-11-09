import { OAuth2Client } from 'google-auth-library';
import jwt from 'jsonwebtoken';
import User, { UserStatus, IUserDocument } from '../models/User';
import { AppError } from '../middleware/errorHandler';
import axios from 'axios';

export class AuthService {
  private googleClient: OAuth2Client;

  constructor() {
    this.googleClient = new OAuth2Client(process.env.GOOGLE_CLIENT_ID);
  }

  /**
   * Convert Google profile picture URL to Base64
   */
  private async convertGoogleProfilePictureToBase64(profilePictureUrl: string): Promise<string> {
    try {
      if (!profilePictureUrl || !profilePictureUrl.startsWith('https://lh3.googleusercontent.com/')) {
        return profilePictureUrl; // Return as-is if not a Google URL
      }

      console.log(`[AuthService] Converting Google profile picture to Base64: ${profilePictureUrl}`);
      
      const response = await axios.get(profilePictureUrl, {
        responseType: 'arraybuffer',
        timeout: 10000
      });

      const buffer = Buffer.from(response.data);
      const base64 = buffer.toString('base64');
      const mimeType = response.headers['content-type'] || 'image/png';
      
      const base64DataUri = `data:${mimeType};base64,${base64}`;
      console.log(`[AuthService] Successfully converted to Base64 (${base64DataUri.length} chars)`);
      
      return base64DataUri;
    } catch (error) {
      console.error(`[AuthService] Failed to convert profile picture to Base64:`, error);
      return profilePictureUrl; // Return original URL if conversion fails
    }
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
  }): Promise<IUserDocument> {
    let user = (await User.findOne({ googleId: googleData.googleId })) as unknown as IUserDocument | null;

    // Convert Google profile picture to Base64 if present
    let convertedProfilePicture = '';
    if (googleData.picture) {
      convertedProfilePicture = await this.convertGoogleProfilePictureToBase64(googleData.picture);
    }

    if (!user) {
      // Create new user
      user = (await User.create({
        googleId: googleData.googleId,
        email: googleData.email,
        name: googleData.name,
        profilePicture: convertedProfilePicture,
        status: UserStatus.ONLINE,
        preference: [],
        credibilityScore: 100,
        budget: 0,
        radiusKm: 5,
      })) as unknown as IUserDocument;

        console.log(`✅ New user created: ${user._id.toString()}`);
    } else {
      // Update existing user
      user.status = UserStatus.ONLINE;
      // Only update profile picture if user doesn't have a custom one
      if (convertedProfilePicture && (!user.profilePicture || user.profilePicture === '')) {
        user.profilePicture = convertedProfilePicture;
        console.log(`✅ Updated profile picture from Google for user: ${user._id.toString()}`);
      } else if (user.profilePicture && user.profilePicture !== '') {
        console.log(`✅ Keeping existing custom profile picture for user: ${user._id.toString()}`);
      }
      await user.save();
      console.log(`✅ User logged in: ${user._id.toString()}`);
    }

    return user;
  }

  /**
   * Generate JWT token
   */
  generateToken(user: IUserDocument): string {
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
   * Logout user
   */
  async logoutUser(userId: string): Promise<void> {
    const user = (await User.findById(userId)) as unknown as IUserDocument | null;

    if (user) {
      user.status = UserStatus.OFFLINE;
      await user.save();
    }
  }
}

export default new AuthService();