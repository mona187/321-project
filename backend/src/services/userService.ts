import User from '../models/User';
import { UserProfileResponse, UserSettingsResponse } from '../types';
import axios from 'axios';

export class UserService {
  /**
   * Convert Google profile picture URL to Base64
   */
  private async convertGoogleProfilePictureToBase64(profilePictureUrl: string): Promise<string> {
    try {
      if (!profilePictureUrl || !profilePictureUrl.startsWith('https://lh3.googleusercontent.com/')) {
        return profilePictureUrl; // Return as-is if not a Google URL
      }

      console.log(`[UserService] Converting Google profile picture to Base64: ${profilePictureUrl}`);
      
      const response = await axios.get(profilePictureUrl, {
        responseType: 'arraybuffer',
        timeout: 10000
      });

      const buffer = Buffer.from(response.data);
      const base64 = buffer.toString('base64');
      const mimeType = response.headers['content-type'] || 'image/png';
      
      const base64DataUri = `data:${mimeType};base64,${base64}`;
      console.log(`[UserService] Successfully converted to Base64 (${base64DataUri.length} chars)`);
      
      return base64DataUri;
    } catch (error) {
      console.error(`[UserService] Failed to convert profile picture to Base64:`, error);
      return profilePictureUrl; // Return original URL if conversion fails
    }
  }

  /**
   * Get user profiles by IDs
   */
  async getUserProfiles(userIds: string[]): Promise<UserProfileResponse[]> {
    const users = await User.find({ _id: { $in: userIds } });

    return users.map(user => ({
      userId: user._id.toString(),
      name: user.name,
      bio: user.bio,
      profilePicture: user.profilePicture,
      contactNumber: user.contactNumber,
    }));
  }

  /**
   * Get user settings
   */
  async getUserSettings(userId: string): Promise<UserSettingsResponse> {
    console.log(`[UserService] getUserSettings called for userId: ${userId}`);
    
    const user = await User.findById(userId);

    if (!user) {
      throw new Error('User not found');
    }

    console.log(`[UserService] User found. profilePicture: "${user.profilePicture}"`);

    return {
      userId: user._id.toString(),
      name: user.name,
      bio: user.bio,
      preference: user.preference,
      profilePicture: user.profilePicture,
      credibilityScore: user.credibilityScore,
      contactNumber: user.contactNumber,
      budget: user.budget || 0,
      radiusKm: user.radiusKm || 5,
      status: user.status,
      roomID: user.roomId,
      groupID: user.groupId,
    };
  }

  /**
   * Create user profile
   */
  async createUserProfile(
    userId: string,
    data: {
      name?: string;
      bio?: string;
      profilePicture?: string;
      contactNumber?: string;
    }
  ): Promise<UserProfileResponse> {
    const user = await User.findById(userId);

    if (!user) {
      throw new Error('User not found');
    }

    if (data.name !== undefined) user.name = data.name;
    if (data.bio !== undefined) user.bio = data.bio;
    if (data.profilePicture !== undefined) user.profilePicture = data.profilePicture;
    if (data.contactNumber !== undefined) user.contactNumber = data.contactNumber;

    await user.save();

    return {
      userId: user._id.toString(),
      name: user.name,
      bio: user.bio,
      profilePicture: user.profilePicture,
      contactNumber: user.contactNumber,
    };
  }

  /**
   * Update user settings
   */
  async updateUserSettings(
    userId: string,
    data: {
      name?: string;
      bio?: string;
      preference?: string[];
      profilePicture?: string;
      contactNumber?: string;
      budget?: number;
      radiusKm?: number;
    }
  ): Promise<UserSettingsResponse> {
    console.log(`[UserService] updateUserSettings called for userId: ${userId}`);
    console.log(`[UserService] Data received:`, data);
    
    const user = await User.findById(userId);

    if (!user) {
      throw new Error('User not found');
    }

    if (data.name !== undefined) user.name = data.name;
    if (data.bio !== undefined) user.bio = data.bio;
    if (data.preference !== undefined) user.preference = data.preference;
    if (data.profilePicture !== undefined) {
      console.log(`[UserService] Updating profilePicture from "${user.profilePicture}" to "${data.profilePicture}"`);
      
      // Convert Google profile picture URL to Base64 if it's a Google URL
      const convertedProfilePicture = await this.convertGoogleProfilePictureToBase64(data.profilePicture);
      user.profilePicture = convertedProfilePicture;
    }
    if (data.contactNumber !== undefined) user.contactNumber = data.contactNumber;
    if (data.budget !== undefined) user.budget = data.budget;
    if (data.radiusKm !== undefined) user.radiusKm = data.radiusKm;

    await user.save();
    console.log(`[UserService] User saved. Current profilePicture: "${user.profilePicture}"`);

    return this.getUserSettings(userId);
  }

  /**
   * Update user profile
   */
  async updateUserProfile(
    userId: string,
    data: {
      name?: string;
      bio?: string;
      preference?: string[];
      profilePicture?: string;
      contactNumber?: string;
      budget?: number;
      radiusKm?: number;
    }
  ): Promise<UserProfileResponse> {
    const user = await User.findById(userId);

    if (!user) {
      throw new Error('User not found');
    }

    if (data.name !== undefined) user.name = data.name;
    if (data.bio !== undefined) user.bio = data.bio;
    if (data.preference !== undefined) user.preference = data.preference;
    if (data.profilePicture !== undefined) {
      // Convert Google profile picture URL to Base64 if it's a Google URL
      const convertedProfilePicture = await this.convertGoogleProfilePictureToBase64(data.profilePicture);
      user.profilePicture = convertedProfilePicture;
    }
    if (data.contactNumber !== undefined) user.contactNumber = data.contactNumber;
    if (data.budget !== undefined) user.budget = data.budget;
    if (data.radiusKm !== undefined) user.radiusKm = data.radiusKm;

    await user.save();

    return {
      userId: user._id.toString(),
      name: user.name,
      bio: user.bio,
      profilePicture: user.profilePicture,
      contactNumber: user.contactNumber,
    };
  }

  /**
   * Delete user
   */
  async deleteUser(userId: string): Promise<{ deleted: boolean }> {
    const user = await User.findById(userId);

    if (!user) {
      throw new Error('User not found');
    }

    if (user.roomId || user.groupId) {
      throw new Error('Cannot delete account while in a room or group');
    }

    await User.findByIdAndDelete(userId);

    return { deleted: true };
  }
}