import { User } from '../models/User';
import { AppError } from '../middleware/errorHandler';

export class UserService {
  async getUserProfile(userId: string) {
    const user = await User.findById(userId).select('-googleId -fcmToken');
    
    if (!user) {
      throw new AppError(404, 'User not found');
    }

    return user;
  }

  async getUserSettings(userId: string) {
    const user = await User.findById(userId);
    
    if (!user) {
      throw new AppError(404, 'User not found');
    }

    return {
      userId: user._id,
      name: user.name,
      bio: user.bio,
      preferences: user.preferences,
      profilePicture: user.profilePicture,
      credibilityScore: user.credibilityScore,
      contactNumber: user.contactNumber,
      status: user.status,
      currentRoomId: user.currentRoomId,
      currentGroupId: user.currentGroupId,
    };
  }

  async updateUserProfile(userId: string, data: {
    name?: string;
    bio?: string;
    profilePicture?: string;
    contactNumber?: string;
  }) {
    const user = await User.findByIdAndUpdate(
      userId,
      { $set: data, lastActive: new Date() },
      { new: true, runValidators: true }
    );

    if (!user) {
      throw new AppError(404, 'User not found');
    }

    return user;
  }

  async updateUserSettings(userId: string, data: {
    name?: string;
    bio?: string;
    preferences?: {
      cuisineTypes?: string[];
      budget?: number;
      radiusKm?: number;
    };
    profilePicture?: string;
    contactNumber?: string;
  }) {
    const updateData: any = { lastActive: new Date() };

    if (data.name) updateData.name = data.name;
    if (data.bio !== undefined) updateData.bio = data.bio;
    if (data.profilePicture) updateData.profilePicture = data.profilePicture;
    if (data.contactNumber) updateData.contactNumber = data.contactNumber;
    
    if (data.preferences) {
      if (data.preferences.cuisineTypes) {
        updateData['preferences.cuisineTypes'] = data.preferences.cuisineTypes;
      }
      if (data.preferences.budget !== undefined) {
        updateData['preferences.budget'] = data.preferences.budget;
      }
      if (data.preferences.radiusKm !== undefined) {
        updateData['preferences.radiusKm'] = data.preferences.radiusKm;
      }
    }

    const user = await User.findByIdAndUpdate(
      userId,
      { $set: updateData },
      { new: true, runValidators: true }
    );

    if (!user) {
      throw new AppError(404, 'User not found');
    }

    return user;
  }

  async deleteUser(userId: string) {
    const user = await User.findByIdAndDelete(userId);
    
    if (!user) {
      throw new AppError(404, 'User not found');
    }

    return { message: 'User deleted successfully' };
  }

  async updateUserLocation(userId: string, longitude: number, latitude: number) {
    const user = await User.findByIdAndUpdate(
      userId,
      {
        $set: {
          location: {
            type: 'Point',
            coordinates: [longitude, latitude],
          },
          lastActive: new Date(),
        },
      },
      { new: true }
    );

    if (!user) {
      throw new AppError(404, 'User not found');
    }

    return user;
  }

  async updateFCMToken(userId: string, fcmToken: string) {
    const user = await User.findByIdAndUpdate(
      userId,
      { $set: { fcmToken, lastActive: new Date() } },
      { new: true }
    );

    if (!user) {
      throw new AppError(404, 'User not found');
    }

    return user;
  }
}