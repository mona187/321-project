import Group, { IGroupDocument } from '../models/Group';
import User, { UserStatus } from '../models/User';
import socketManager from '../utils/socketManager';
import { notifyGroupMembers, notifyRestaurantSelected } from './notificationService';
import { RestaurantType, GroupStatusResponse } from '../types';

export class GroupService {
  /**
   * Get group status
   */
  async getGroupStatus(groupId: string): Promise<GroupStatusResponse & { groupId: string }> {
    const group = await Group.findById(groupId);

    if (!group) {
      throw new Error('Group not found');
    }

    return {
      groupId: group._id.toString(), // ← ADD THIS LINE
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
   * Get group status string
   */
  private getGroupStatusString(group: IGroupDocument | { restaurantSelected: boolean; completionTime: Date }): 'voting' | 'matched' | 'completed' | 'disbanded' {
    if (group.restaurantSelected) {
      return 'completed';
    }
    if (new Date() > group.completionTime) {
      return 'disbanded';
    }
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

    if (!group) {
      throw new Error('Group not found');
    }

    // Check if user is in the group
    if (!group.members.includes(userId)) {
      throw new Error('User is not a member of this group');
    }

    // Check if restaurant is already selected
    if (group.restaurantSelected) {
      throw new Error('Restaurant has already been selected for this group');
    }

    // Add/update vote
    group.addVote(userId, restaurantId);
    
    // Mark Map fields as modified for Mongoose to persist changes
    group.markModified('votes');
    group.markModified('restaurantVotes');
    
    await group.save();

    // Convert Map to object for response - using Object.fromEntries for safe conversion
    const currentVotes: Record<string, number> = Object.fromEntries(
      Array.from(group.restaurantVotes.entries()).map(([id, count]) => {
        // Ensure id is a valid string to prevent injection
        const safeId = String(id);
        return [safeId, count];
      })
    );

    // // Emit vote update to all group members
    // socketManager.emitVoteUpdate(
    //   groupId,
    //   restaurantId,
    //   currentVotes,
    //   group.votes.size,
    //   group.members.length
    // );

    // 🔥 FIX: Wrap socket emission in try-catch
    try {
      socketManager.emitVoteUpdate(
        groupId,
        restaurantId,
        currentVotes,
        group.votes.size,
        group.members.length
      );
    } catch (error) {
      console.error('Failed to emit vote update:', error);
      // Continue execution - socket failure shouldn't block voting
    }


    // Check if all members have voted
    if (group.hasAllVoted()) {
      const winningRestaurantId = group.getWinningRestaurant();
      
      if (winningRestaurantId) {
        // Set the restaurant
        if (restaurant) {
          group.restaurant = restaurant;
        }
        group.restaurantSelected = true;
        await group.save();

        // Emit restaurant selected
        try {
          socketManager.emitRestaurantSelected(
            groupId,
            winningRestaurantId,
            group.restaurant?.name || 'Selected Restaurant',
            currentVotes
          );
        } catch (error) {
          console.error('Failed to emit restaurant selected:', error);
        }

        // // Send notifications
        // await notifyRestaurantSelected(
        //   group.members,
        //   group.restaurant?.name || 'Selected Restaurant',
        //   groupId
        // );

      // 🔥 FIX: Wrap notification in try-catch
        try {
          await notifyRestaurantSelected(
            group.members,
            group.restaurant?.name || 'Selected Restaurant',
            groupId
          );
        } catch (error) {
          console.error('Failed to send notification:', error);
        }

        console.log(`âœ… Restaurant selected for group ${groupId}`);
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

    if (!group) {
      throw new Error('Group not found');
    }

    const user = await User.findById(userId);
    if (!user) {
      throw new Error('User not found');
    }

    // Remove user from group
    group.removeMember(userId);

    // Update user status
    user.status = UserStatus.ONLINE;
    user.groupId = undefined;
    await user.save();

    if (group.members.length === 0) {
      // Delete empty group
      await Group.findByIdAndDelete(groupId);
      console.log(`ðŸ—‘ï¸ Deleted empty group: ${groupId}`);
    } else {
      await group.save();

      // // Notify remaining members
      // socketManager.emitMemberLeft(
      //   `group_${groupId}`,
      //   userId,
      //   user.name,
      //   group.members.length
      // );
       // 🔥 FIX: Wrap socket emission in try-catch
      try {
        socketManager.emitMemberLeft(
          `group_${groupId}`,
          userId,
          user.name,
          group.members.length
        );
      } catch (error) {
        console.error('Failed to emit member left:', error);
      }

      // Check if restaurant should be auto-selected (all remaining voted)
      if (group.hasAllVoted() && !group.restaurantSelected) {
        const winningRestaurantId = group.getWinningRestaurant();
        
        if (winningRestaurantId && group.restaurant) {
          group.restaurantSelected = true;
          await group.save();

          // Convert Map to object for response - using Object.fromEntries for safe conversion
          const currentVotes: Record<string, number> = Object.fromEntries(
            Array.from(group.restaurantVotes.entries()).map(([id, count]) => {
              // Ensure id is a valid string to prevent injection
              const safeId = String(id);
              return [safeId, count];
            })
          );

          // socketManager.emitRestaurantSelected(
          //   groupId,
          //   winningRestaurantId,
          //   group.restaurant.name,
          //   currentVotes
          // );

          // await notifyRestaurantSelected(
          //   group.members,
          //   group.restaurant.name,
          //   groupId
          // );
          try {
            socketManager.emitRestaurantSelected(
              groupId,
              winningRestaurantId,
              group.restaurant.name,
              currentVotes
            );
          } catch (error) {
            console.error('Failed to emit restaurant selected:', error);
          }

        // 🔥 FIX: Wrap notification in try-catch
          try {
            await notifyRestaurantSelected(
              group.members,
              group.restaurant.name,
              groupId
            );
          } catch (error) {
            console.error('Failed to send notification:', error);
          }
        }
      }
    }
  }

  /**
   * Get group by user ID
   */
  async getGroupByUserId(userId: string): Promise<IGroupDocument | null> {
    const user = await User.findById(userId);
    
    if (!user || !user.groupId) {
      return null;
    }

    return Group.findById(user.groupId);
  }

  /**
   * Close/disband a group (after restaurant visit or timeout)
   */
  async closeGroup(groupId: string): Promise<void> {
    const group = await Group.findById(groupId);

    if (!group) {
      throw new Error('Group not found');
    }

    // Update all users
    await User.updateMany(
      { _id: { $in: group.members } },
      {
        status: UserStatus.ONLINE,
        groupId: undefined,
      }
    );

    // Delete group
    await Group.findByIdAndDelete(groupId);

    console.log(`âœ… Closed group: ${groupId}`);
  }

  /**
   * Check for expired groups (background task)
   */
  async checkExpiredGroups(): Promise<void> {
    const expiredGroups = await Group.find({
      restaurantSelected: false,
      completionTime: { $lt: new Date() },
    });

    for (const group of expiredGroups) {
      // Auto-select restaurant with most votes
      const winningRestaurantId = group.getWinningRestaurant();
      
      if (winningRestaurantId && group.votes.size > 0) {
        group.restaurantSelected = true;
        await group.save();

        // Notify members
        if (group.restaurant) {
          // Convert Map to object for response - using Object.fromEntries for safe conversion
          const currentVotes: Record<string, number> = Object.fromEntries(
            Array.from(group.restaurantVotes.entries()).map(([id, count]) => {
              // Ensure id is a valid string to prevent injection
              const safeId = String(id);
              return [safeId, count];
            })
          );

          socketManager.emitRestaurantSelected(
            group._id.toString(),
            winningRestaurantId,
            group.restaurant.name,
            currentVotes
          );
          
          await notifyGroupMembers(group.members, {
            title: 'Voting Time Expired',
            body: `${group.restaurant.name} was selected based on the votes received.`,
            data: {
              type: 'restaurant_selected',
              groupId: group._id.toString(),
            },
          });
        }

        console.log(`â° Auto-selected restaurant for expired group: ${group._id.toString()}`);
      } else {
        // No votes - disband group
        await this.closeGroup(group._id.toString());
        
        await notifyGroupMembers(group.members, {
          title: 'Group Expired',
          body: 'Your group expired without selecting a restaurant.',
          data: {
            type: 'group_expired',
            groupId: group._id.toString(),
          },
        });

        console.log(`â° Disbanded expired group with no votes: ${group._id.toString()}`);
      }
    }
  }
}

export default new GroupService();