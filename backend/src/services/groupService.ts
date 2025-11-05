import Group from '../models/Group';
import User, { UserStatus } from '../models/User';
import socketManager from '../utils/socketManager';
import { notifyGroupMembers, notifyRestaurantSelected } from './notificationService';
import { RestaurantType, GroupStatusResponse } from '../types';

export class GroupService {
  /**
   * Get group status
   */
  async getGroupStatus(groupId: string): Promise<GroupStatusResponse> {
    const group = await Group.findById(groupId);

    if (!group) {
      throw new Error('Group not found');
    }

   return {
  groupId: group._id.toString(),
  roomId: group.roomId,
  completionTime: group.completionTime.getTime(),
  numMembers: group.members.length,
  users: group.members,
  restaurantSelected: group.restaurantSelected,
  restaurant: group.restaurant || undefined,
  status: this.getGroupStatusString(group),
};
  }

  /**
   * Determine group status string
   */
  private getGroupStatusString(group: any): 'voting' | 'matched' | 'completed' | 'disbanded' {
    if (group.restaurantSelected) return 'completed';
    if (new Date() > group.completionTime) return 'disbanded';
    return 'voting';
  }

  /**
   * Vote for a restaurant
   */
  async voteForRestaurant(
    userId: string,
    groupId: string,
    restaurantId: string,
    restaurant?: RestaurantType
  ): Promise<{ message: string; Current_votes: Record<string, number> }> {

    const group = await Group.findById(groupId);
    if (!group) throw new Error('Group not found');

    if (!group.members.includes(userId)) {
      throw new Error('User is not a member of this group');
    }

    if (group.restaurantSelected) {
      throw new Error('Restaurant already selected');
    }

    group.addVote(userId, restaurantId);

    group.markModified('votes');
    group.markModified('restaurantVotes');
    await group.save();

    const currentVotes: Record<string, number> = {};
    group.restaurantVotes.forEach((count: number, id: string) => {
      currentVotes[id] = count;
    });

    socketManager.emitVoteUpdate(
      groupId,
      restaurantId,
      currentVotes,
      group.votes.size,
      group.members.length
    );

    // If everyone voted — pick winner
    if (group.hasAllVoted()) {
      const winningRestaurantId = group.getWinningRestaurant();
      if (winningRestaurantId) {
        if (restaurant) group.restaurant = restaurant;
        group.restaurantSelected = true;
        await group.save();

        socketManager.emitRestaurantSelected(
          groupId,
          winningRestaurantId,
          group.restaurant?.name ?? 'Selected Restaurant',
          currentVotes
        );

        await notifyRestaurantSelected(
          group.members,
          group.restaurant?.name ?? 'Selected Restaurant',
          groupId
        );

        console.log(`✅ Restaurant selected for group ${groupId}`);
      }
    }

    return {
      message: 'Voting successful',
      Current_votes: currentVotes,
    };
  }

  /**
   * Leave a group
   */
  async leaveGroup(userId: string, groupId: string): Promise<void> {
    const group = await Group.findById(groupId);
    if (!group) throw new Error('Group not found');

    const user = await User.findById(userId);
    if (!user) throw new Error('User not found');

    group.removeMember(userId);

    user.status = UserStatus.ONLINE;
    user.groupId = undefined;
    await user.save();

    if (group.members.length === 0) {
      await Group.findByIdAndDelete(groupId);
      console.log(`🗑️ Deleted empty group: ${groupId}`);
      return;
    }

    await group.save();

    socketManager.emitMemberLeft(
      `group_${groupId}`,
      userId,
      user.name,
      group.members.length
    );

    if (group.hasAllVoted() && !group.restaurantSelected) {
      const winningRestaurantId = group.getWinningRestaurant();

      if (winningRestaurantId && group.restaurant) {
        group.restaurantSelected = true;
        await group.save();

        const currentVotes: Record<string, number> = {};
        group.restaurantVotes.forEach((count: number, id: string) => {
          currentVotes[id] = count;
        });

        socketManager.emitRestaurantSelected(
          groupId,
          winningRestaurantId,
          group.restaurant.name,
          currentVotes
        );

        await notifyRestaurantSelected(
          group.members,
          group.restaurant.name,
          groupId
        );
      }
    }
  }

  /**
   * Get group by user ID
   */
  async getGroupByUserId(userId: string) {
    const user = await User.findById(userId);
    if (!user || !user.groupId) return null;
    return Group.findById(user.groupId);
  }

  /**
   * Close a group
   */
  async closeGroup(groupId: string): Promise<void> {
    const group = await Group.findById(groupId);
    if (!group) throw new Error('Group not found');

    await User.updateMany(
      { _id: { $in: group.members } },
      { status: UserStatus.ONLINE, groupId: undefined }
    );

    await Group.findByIdAndDelete(groupId);

    console.log(`✅ Closed group: ${groupId}`);
  }

  /**
   * Expire old groups
   */
  async checkExpiredGroups(): Promise<void> {
    const expiredGroups = await Group.find({
      restaurantSelected: false,
      completionTime: { $lt: new Date() },
    });

    for (const group of expiredGroups) {
      const winningRestaurantId = group.getWinningRestaurant();

      if (winningRestaurantId && group.votes.size > 0) {
        group.restaurantSelected = true;
        await group.save();

        const currentVotes: Record<string, number> = {};
        group.restaurantVotes.forEach((count: number, id: string) => {
          currentVotes[id] = count;
        });

        socketManager.emitRestaurantSelected(
          group._id.toString(),
          winningRestaurantId,
          group.restaurant?.name ?? '',
          currentVotes
        );

        await notifyGroupMembers(group.members, {
          title: 'Voting Time Expired ⏰',
          body: `${group.restaurant?.name ?? 'Restaurant'} was auto-selected.`,
          data: {
            type: 'restaurant_selected',
            groupId: group._id.toString(),
          },
        });

        console.log(`⏳ Auto-chose restaurant for group ${group._id}`);
      } else {
        await this.closeGroup(group._id.toString());

        await notifyGroupMembers(group.members, {
          title: 'Group Expired ⏰',
          body: 'No restaurant selected — group closed.',
          data: {
            type: 'group_expired',
            groupId: group._id.toString(),
          },
        });

        console.log(`⏳ Disbanded expired group ${group._id}`);
      }
    }
  }
}

export default new GroupService();
