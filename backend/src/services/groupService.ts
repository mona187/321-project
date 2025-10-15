import { Group } from '../models/Group';
import { User } from '../models/User';
import { AppError } from '../middleware/errorHandler';

export class GroupService {
  async getGroupStatus(groupId: string) {
    const group = await Group.findOne({ groupId }).populate(
      'users',
      'name profilePicture credibilityScore'
    );

    if (!group) {
      throw new AppError(404, 'Group not found');
    }

    return {
      groupId: group.groupId,
      roomId: group.roomId,
      completionTime: group.completionTime,
      numMembers: group.users.length,
      users: group.users,
      restaurantSelected: group.restaurantSelected,
      restaurant: group.restaurant,
      status: group.status,
    };
  }

  async leaveGroup(userId: string, groupId: string) {
    const group = await Group.findOne({ groupId });

    if (!group) {
      throw new AppError(404, 'Group not found');
    }

    if (!group.users.includes(userId)) {
      throw new AppError(400, 'User not in this group');
    }

    // Remove user from group
    group.users = group.users.filter((id) => id !== userId);
    
    // Remove user's votes
    group.votes.delete(userId);
    
    await group.save();

    // Update user status
    await User.findByIdAndUpdate(userId, {
      $set: {
        status: 'active',
        currentGroupId: undefined,
      },
    });

    // If restaurant was selected and user leaves, decrease credibility
    if (group.restaurantSelected) {
      await User.findByIdAndUpdate(userId, {
        $inc: { credibilityScore: -0.5 },
      });
    }

    // Delete group if empty
    if (group.users.length === 0) {
      await Group.findByIdAndDelete(group._id);
    }

    return {
      message: 'Successfully left group',
      groupId: group.groupId,
    };
  }

  async voteRestaurant(userId: string, groupId: string, restaurantId: string) {
    const group = await Group.findOne({ groupId, status: 'voting' });

    if (!group) {
      throw new AppError(404, 'Group not found or voting is closed');
    }

    if (!group.users.includes(userId)) {
      throw new AppError(400, 'User not in this group');
    }

    // Record vote
    group.votes.set(userId, restaurantId);
    await group.save();

    // Check if majority reached
    const voteCount = this.countVotes(group.votes);
    const majority = Math.ceil(group.users.length / 2);

    for (const [restId, count] of Object.entries(voteCount)) {
      if (count >= majority) {
        // Restaurant selected by majority
        return {
          message: 'Vote recorded - majority reached',
          restaurantId: restId,
          votes: voteCount,
          majorityReached: true,
        };
      }
    }

    return {
      message: 'Vote recorded',
      votes: voteCount,
      majorityReached: false,
    };
  }

  async selectRestaurant(groupId: string, restaurant: any) {
    const group = await Group.findOne({ groupId });

    if (!group) {
      throw new AppError(404, 'Group not found');
    }

    group.restaurant = restaurant;
    group.restaurantSelected = true;
    group.status = 'confirmed';
    await group.save();

    return {
      message: 'Restaurant selected successfully',
      group,
    };
  }

  async getGroupMembers(groupId: string) {
    const group = await Group.findOne({ groupId }).populate(
      'users',
      'name profilePicture bio contactNumber credibilityScore'
    );

    if (!group) {
      throw new AppError(404, 'Group not found');
    }

    return {
      groupId: group.groupId,
      members: group.users,
    };
  }

  private countVotes(votes: Map<string, string>): Record<string, number> {
    const count: Record<string, number> = {};

    votes.forEach((restaurantId) => {
      count[restaurantId] = (count[restaurantId] || 0) + 1;
    });

    return count;
  }

  async completeGroup(groupId: string) {
    const group = await Group.findOne({ groupId });

    if (!group) {
      throw new AppError(404, 'Group not found');
    }

    group.status = 'completed';
    await group.save();

    // Update all users in group
    await User.updateMany(
      { _id: { $in: group.users } },
      {
        $set: {
          status: 'active',
          currentGroupId: undefined,
        },
      }
    );

    return {
      message: 'Group completed',
      groupId: group.groupId,
    };
  }
}