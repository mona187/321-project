import User from '../models/User';
import { UserProfileResponse, UserSettingsResponse } from '../types';

export class UserService {
  /**
   * Get user profiles by IDs
   */
  async getUserProfiles(userIds: string[]): Promise<UserProfileResponse[]> {
    const users = await User.find({ _id: { $in: userIds } });

    return users.map(user => ({
      userId: parseInt(user._id.toString().slice(-6), 16),
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
    const user = await User.findById(userId);

    if (!user) {
      throw new Error('User not found');
    }

    return {
      userId: parseInt(user._id.toString().slice(-6), 16),
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
   * Get most recent user settings (for unauthenticated users)
   */
  async getMostRecentUserSettings(): Promise<UserSettingsResponse> {
    const user = await User.findOne().sort({ createdAt: -1 });

    if (!user) {
      throw new Error('No user found');
    }

    return {
      userId: parseInt(user._id.toString().slice(-6), 16),
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
      userId: parseInt(user._id.toString().slice(-6), 16),
      name: user.name,
      bio: user.bio,
      profilePicture: user.profilePicture,
      contactNumber: user.contactNumber,
    };
  }

  /**
   * Create user settings
   */
  async createUserSettings(
    userId: string | null,
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
    let user;

    if (userId) {
      // Update existing user
      user = await User.findById(userId);
      if (!user) {
        throw new Error('User not found');
      }
    } else {
      // Create new user with unique identifiers
      const timestamp = Date.now();
      const randomId = Math.random().toString(36).substring(2, 15);
      const email = `user_${timestamp}_${randomId}@temp.com`;
      const googleId = `temp_${timestamp}_${randomId}`;
      
      try {
        user = await User.create({
          name: data.name && data.name.trim() ? data.name : 'User',
          bio: data.bio || '',
          preference: data.preference || [],
          profilePicture: data.profilePicture || '',
          contactNumber: data.contactNumber || '',
          budget: data.budget || 0,
          radiusKm: data.radiusKm || 5,
          credibilityScore: 100,
          status: 1, // ONLINE status as number
          email: email,
          googleId: googleId,
        });
      } catch (createError) {
        console.error('❌ Error creating user:', createError);
        throw createError;
      }
    }

    // Update user fields
    if (data.name !== undefined) user.name = data.name && data.name.trim() ? data.name : 'User';
    if (data.bio !== undefined) user.bio = data.bio;
    if (data.preference !== undefined) user.preference = data.preference;
    if (data.profilePicture !== undefined) user.profilePicture = data.profilePicture;
    if (data.contactNumber !== undefined) user.contactNumber = data.contactNumber;
    if (data.budget !== undefined) user.budget = data.budget;
    if (data.radiusKm !== undefined) user.radiusKm = data.radiusKm;

    try {
      await user.save();
      
      // Return the user data directly instead of calling getUserSettings
      return {
        userId: parseInt(user._id.toString().slice(-6), 16),
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
    } catch (error) {
      console.error('❌ Error saving user:', error);
      throw error;
    }
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
    const user = await User.findById(userId);

    if (!user) {
      throw new Error('User not found');
    }

    if (data.name !== undefined) user.name = data.name;
    if (data.bio !== undefined) user.bio = data.bio;
    if (data.preference !== undefined) user.preference = data.preference;
    if (data.profilePicture !== undefined) user.profilePicture = data.profilePicture;
    if (data.contactNumber !== undefined) user.contactNumber = data.contactNumber;
    if (data.budget !== undefined) user.budget = data.budget;
    if (data.radiusKm !== undefined) user.radiusKm = data.radiusKm;

    await user.save();

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
    if (data.profilePicture !== undefined) user.profilePicture = data.profilePicture;
    if (data.contactNumber !== undefined) user.contactNumber = data.contactNumber;
    if (data.budget !== undefined) user.budget = data.budget;
    if (data.radiusKm !== undefined) user.radiusKm = data.radiusKm;

    await user.save();

    return {
      userId: parseInt(user._id.toString().slice(-6), 16),
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