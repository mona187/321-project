import Room, { RoomStatus } from '../models/Room';
import User, { UserStatus } from '../models/User';
import Group from '../models/Group';
import socketManager from '../utils/socketManager';
import { notifyRoomMatched, notifyRoomExpired } from './notificationService';

export class MatchingService {
  private readonly ROOM_DURATION_MS = 5 * 60 * 1000; // 5 minutes
  private readonly MAX_MEMBERS = 4; // Maximum members per room
  private readonly MIN_MEMBERS = 2; // Minimum members to form a group

  /**
   * Join a user to the matching pool
   * Finds an available room or creates a new one
   */
  async joinMatching(
    userId: string,
    preferences: {
      cuisine?: string[];
      budget?: number;
      radiusKm?: number;
    }
  ): Promise<{ roomId: string; room: any }> {
    // Get user
    const user = await User.findById(userId);
    if (!user) {
      throw new Error('User not found');
    }

    // Check if user is already in a room or group
    if (user.roomId || user.groupId) {
      throw new Error('User is already in a room or group');
    }

    // Update user preferences if provided
    if (preferences.budget !== undefined) user.budget = preferences.budget;
    if (preferences.radiusKm !== undefined) user.radiusKm = preferences.radiusKm;
    if (preferences.cuisine !== undefined) user.preference = preferences.cuisine;
    await user.save();

    // Find an available room or create new one
    let room = await this.findAvailableRoom(preferences.cuisine?.[0]);

    if (!room) {
      // Create new room
      const completionTime = new Date(Date.now() + this.ROOM_DURATION_MS);
      
      room = await Room.create({
        completionTime,
        maxMembers: this.MAX_MEMBERS,
        members: [userId],
        status: RoomStatus.WAITING,
        cuisine: preferences.cuisine?.[0] || null,
        averageBudget: user.budget,
        averageRadius: user.radiusKm,
      });

      console.log(`✅ Created new room: ${room._id}`);
    } else {
      // Add user to existing room
      room.members.push(userId);
      
      // Update averages
      await this.updateRoomAverages(room);
      
      await room.save();
      console.log(`✅ User ${userId} joined room: ${room._id}`);
    }

    // Update user status
    user.status = UserStatus.IN_WAITING_ROOM;
    user.roomId = room._id.toString();
    await user.save();

    // Emit room update to all members
    socketManager.emitRoomUpdate(
      room._id.toString(),
      room.members,
      room.completionTime,
      room.status
    );

    // Emit member joined notification
    socketManager.emitMemberJoined(
      room._id.toString(),
      userId,
      user.name,
      room.members.length,
      room.maxMembers
    );

    // Check if room is full and create group
    if (room.members.length >= this.MAX_MEMBERS) {
      await this.createGroupFromRoom(room._id.toString());
    }

    return {
      roomId: room._id.toString(),
      room: room.toJSON(),
    };
  }

  /**
   * Find an available room for matching
   */
  private async findAvailableRoom(cuisine?: string): Promise<any | null> {
    const query: any = {
      status: RoomStatus.WAITING,
      completionTime: { $gt: new Date() },
      $expr: { $lt: [{ $size: '$members' }, this.MAX_MEMBERS] }
    };

    // Match by cuisine if provided
    if (cuisine) {
      query.cuisine = cuisine;
    }

    return Room.findOne(query).sort({ createdAt: 1 }); // Oldest first
  }

  /**
   * Update room averages (budget, radius)
   */
  private async updateRoomAverages(room: any): Promise<void> {
    const users = await User.find({ _id: { $in: room.members } });

    const totalBudget = users.reduce((sum, user) => sum + (user.budget || 0), 0);
    const totalRadius = users.reduce((sum, user) => sum + (user.radiusKm || 5), 0);

    room.averageBudget = totalBudget / users.length;
    room.averageRadius = totalRadius / users.length;
  }

  /**
   * Leave a room
   */
  async leaveRoom(userId: string, roomId: string): Promise<void> {
    const room = await Room.findById(roomId);
    if (!room) {
      throw new Error('Room not found');
    }

    const user = await User.findById(userId);
    if (!user) {
      throw new Error('User not found');
    }

    // Remove user from room
    room.members = room.members.filter(id => id !== userId);
    
    // Update user status
    user.status = UserStatus.ONLINE;
    user.roomId = undefined;
    await user.save();

    // Emit member left notification
    socketManager.emitMemberLeft(
      roomId,
      userId,
      user.name,
      room.members.length
    );

    if (room.members.length === 0) {
      // Delete empty room
      await Room.findByIdAndDelete(roomId);
      console.log(`🗑️ Deleted empty room: ${roomId}`);
    } else {
      // Update room averages and save
      await this.updateRoomAverages(room);
      await room.save();
      
      // Emit room update
      socketManager.emitRoomUpdate(
        roomId,
        room.members,
        room.completionTime,
        room.status
      );
    }
  }

  /**
   * Create a group from a full room
   */
  private async createGroupFromRoom(roomId: string): Promise<void> {
    const room = await Room.findById(roomId);
    if (!room) {
      throw new Error('Room not found');
    }

    // Update room status
    room.status = RoomStatus.MATCHED;
    await room.save();

    // Create group
    const completionTime = new Date(Date.now() + 30 * 60 * 1000); // 30 minutes for voting
    
    const group = await Group.create({
      roomId: room._id.toString(),
      completionTime,
      maxMembers: room.members.length,
      members: room.members,
      restaurantSelected: false,
    });

    console.log(`✅ Created group: ${group._id} from room: ${roomId}`);

    // Update all users
    await User.updateMany(
      { _id: { $in: room.members } },
      {
        status: UserStatus.IN_GROUP,
        groupId: group._id.toString(),
        roomId: undefined,
      }
    );

    // Emit group ready to all members
    socketManager.emitGroupReady(
      roomId,
      group._id.toString(),
      room.members
    );

    // Send push notifications
    for (const memberId of room.members) {
      try {
        await notifyRoomMatched(memberId, roomId, group._id.toString());
      } catch (error) {
        console.error(`Failed to notify user ${memberId}:`, error);
      }
    }
  }

  /**
   * Get room status
   */
  async getRoomStatus(roomId: string): Promise<any> {
    const room = await Room.findById(roomId);
    if (!room) {
      throw new Error('Room not found');
    }

    return {
      roomID: room._id.toString(),
      completionTime: room.completionTime.getTime(),
      members: room.members,
      groupReady: room.status === RoomStatus.MATCHED,
      status: room.status,
    };
  }

  /**
   * Get users in a room
   */
  async getRoomUsers(roomId: string): Promise<string[]> {
    const room = await Room.findById(roomId);
    if (!room) {
      throw new Error('Room not found');
    }

    return room.members;
  }

  /**
   * Check and expire old rooms (background task)
   */
  async checkExpiredRooms(): Promise<void> {
    const expiredRooms = await Room.find({
      status: RoomStatus.WAITING,
      completionTime: { $lt: new Date() },
    });

    for (const room of expiredRooms) {
      // Check if room has enough members
      if (room.members.length >= this.MIN_MEMBERS) {
        // Create group even if not full
        await this.createGroupFromRoom(room._id.toString());
      } else {
        // Expire the room
        room.status = RoomStatus.EXPIRED;
        await room.save();

        // Update users
        await User.updateMany(
          { _id: { $in: room.members } },
          {
            status: UserStatus.ONLINE,
            roomId: undefined,
          }
        );

        // Notify members
        socketManager.emitRoomExpired(room._id.toString(), 'Not enough members');

        for (const memberId of room.members) {
          try {
            await notifyRoomExpired(memberId, room._id.toString());
          } catch (error) {
            console.error(`Failed to notify user ${memberId}:`, error);
          }
        }

        console.log(`⏰ Expired room: ${room._id}`);
      }
    }
  }
}

export default new MatchingService();