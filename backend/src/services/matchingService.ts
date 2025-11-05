import Room, { IRoomDocument, RoomStatus } from '../models/Room';
import User, { UserStatus } from '../models/User';
import Group from '../models/Group';
import socketManager from '../utils/socketManager';
import { notifyRoomMatched, notifyRoomExpired } from './notificationService';
import { RoomStatusResponse } from '../types';

export class MatchingService {
  private readonly ROOM_DURATION_MS = 2 * 60 * 1000; // 2 minutes
  private readonly MAX_MEMBERS = 10;
  private readonly MIN_MEMBERS = 2;
  private readonly MINIMUM_MATCH_SCORE = 30;
  private readonly VOTING_TIME = 30 * 60 * 1000; // 30 minutes

  /**
   * Find best matching room
   */
  private async findBestMatchingRoom(preferences: {
    cuisines: string[];
    budget: number;
    radiusKm: number;
  }): Promise<IRoomDocument | null> {
    const availableRooms = await Room.find({
      status: RoomStatus.WAITING,
      completionTime: { $gt: new Date() },
      $expr: { $lt: [{ $size: '$members' }, this.MAX_MEMBERS] }
    });

    if (availableRooms.length === 0) return null;

    const scoredRooms = availableRooms.map(room => {
      let score = 0;

      if (room.cuisine && preferences.cuisines.includes(room.cuisine)) {
        score += 50;
      }

      const budgetDiff = Math.abs((room.averageBudget ?? 0) - preferences.budget);
      score += Math.max(0, 30 - budgetDiff);

      const radiusDiff = Math.abs((room.averageRadius ?? 5) - preferences.radiusKm);
      score += Math.max(0, 20 - radiusDiff * 2);

      return { room, score };
    });

    scoredRooms.sort((a, b) => b.score - a.score);
    const best = scoredRooms[0];

    if (best.score >= this.MINIMUM_MATCH_SCORE) {
      return best.room;
    }

    return null;
  }

  /**
   * Join matching
   */
  async joinMatching(
    userId: string,
    prefs: { cuisine?: string[]; budget?: number; radiusKm?: number }
  ): Promise<{ roomId: string; room: IRoomDocument }> {
    const user = await User.findById(userId);
    if (!user) throw new Error('User not found');

    if (user.roomId || user.groupId) {
      throw new Error('User is already in a room or group');
    }

    if (prefs.budget !== undefined) user.budget = prefs.budget;
    if (prefs.radiusKm !== undefined) user.radiusKm = prefs.radiusKm;
    if (prefs.cuisine !== undefined) user.preference = prefs.cuisine;
    await user.save();

    const matchPrefs = {
      cuisines: prefs.cuisine ?? user.preference ?? [],
      budget: prefs.budget ?? user.budget ?? 50,
      radiusKm: prefs.radiusKm ?? user.radiusKm ?? 5,
    };

    let room = await this.findBestMatchingRoom(matchPrefs);

    if (!room) {
      const completionTime = new Date(Date.now() + this.ROOM_DURATION_MS);

      room = await Room.create({
        completionTime,
        maxMembers: this.MAX_MEMBERS,
        members: [userId],
        status: RoomStatus.WAITING,
        cuisine: matchPrefs.cuisines[0] ?? null,
        averageBudget: matchPrefs.budget,
        averageRadius: matchPrefs.radiusKm,
      });
    } else {
      room.members.push(userId);
      await this.updateRoomAverages(room);
      await room.save();
    }

    user.status = UserStatus.IN_WAITING_ROOM;
    user.roomId = room._id.toString();
    await user.save();

    const roomIdString = room._id.toString();

    socketManager.emitRoomUpdate(
      roomIdString,
      room.members,
      room.completionTime,
      room.status
    );

    socketManager.emitToUser(userId, 'room_update', {
      roomId: roomIdString,
      members: room.members,
      expiresAt: room.completionTime,
      status: room.status,
    });

    socketManager.emitMemberJoined(
      roomIdString,
      userId,
      user.name,
      room.members.length,
      room.maxMembers
    );

    if (room.members.length >= this.MAX_MEMBERS) {
      await this.createGroupFromRoom(roomIdString);
    }

    return { roomId: roomIdString, room };
  }

  /**
   * Update room budget & radius averages
   */
  private async updateRoomAverages(room: IRoomDocument): Promise<void> {
    const users = await User.find({ _id: { $in: room.members } });

    const totalBudget = users.reduce((s, u) => s + (u.budget ?? 0), 0);
    const totalRadius = users.reduce((s, u) => s + (u.radiusKm ?? 5), 0);

    room.averageBudget = totalBudget / users.length;
    room.averageRadius = totalRadius / users.length;
  }

  /**
   * Leave room
   */
  async leaveRoom(userId: string, roomId: string): Promise<void> {
    const user = await User.findById(userId);
    if (!user) throw new Error('User not found');

    const room = await Room.findById(roomId);

    if (!room) {
      if (user.roomId) {
        user.roomId = undefined;
        user.status = UserStatus.ONLINE;
        await user.save();
      }
      return;
    }

    room.members = room.members.filter(id => id !== userId);

    user.status = UserStatus.ONLINE;
    user.roomId = undefined;
    await user.save();

    socketManager.emitMemberLeft(roomId, userId, user.name, room.members.length);

    if (room.members.length === 0) {
      await Room.findByIdAndDelete(roomId);
    } else {
      await this.updateRoomAverages(room);
      await room.save();

      socketManager.emitRoomUpdate(
        roomId,
        room.members,
        room.completionTime,
        room.status
      );
    }
  }

  /**
   * Create group when room full/expired
   */
  private async createGroupFromRoom(roomId: string): Promise<void> {
    const room = await Room.findById(roomId);
    if (!room) throw new Error('Room not found');

    room.status = RoomStatus.MATCHED;
    await room.save();

    const completionTime = new Date(Date.now() + this.VOTING_TIME);

    const group = await Group.create({
      roomId,
      completionTime,
      maxMembers: room.members.length,
      members: room.members,
      restaurantSelected: false,
    });

    const groupIdString = group._id.toString();

    await User.updateMany(
      { _id: { $in: room.members } },
      { status: UserStatus.IN_GROUP, groupId: groupIdString, roomId: undefined }
    );

    socketManager.emitGroupReady(roomId, groupIdString, room.members);

    for (const m of room.members) {
      await notifyRoomMatched(m, roomId, groupIdString);
    }
  }

  /**
   * ✅ Typed room status
   */
  async getRoomStatus(roomId: string): Promise<RoomStatusResponse> {
    const room = await Room.findById(roomId);
    if (!room) throw new Error('Room not found');

    return {
      roomID: room._id.toString(),
      completionTime: room.completionTime.getTime(),
      members: room.members,
      groupReady: room.status === RoomStatus.MATCHED,
      status: room.status,
    };
  }

  async getRoomUsers(roomId: string): Promise<string[]> {
    const room = await Room.findById(roomId);
    if (!room) throw new Error('Room not found');
    return room.members;
  }

  /**
   * Expire rooms
   */
  async checkExpiredRooms(): Promise<void> {
    const rooms = await Room.find({
      status: RoomStatus.WAITING,
      completionTime: { $lt: new Date() },
    });

    for (const room of rooms) {
      const roomIdString = room._id.toString();
      
      if (room.members.length >= this.MIN_MEMBERS) {
        await this.createGroupFromRoom(roomIdString);
      } else {
        room.status = RoomStatus.EXPIRED;
        await room.save();

        await User.updateMany(
          { _id: { $in: room.members } },
          { status: UserStatus.ONLINE, roomId: undefined }
        );

        socketManager.emitRoomExpired(roomIdString, 'Not enough members');

        for (const m of room.members) {
          await notifyRoomExpired(m, roomIdString);
        }
      }
    }
  }
}

export default new MatchingService();
