import { User } from '../models/User';
import { Room } from '../models/Room';
import { Group } from '../models/Group';
import { AppError } from '../middleware/errorHandler';
import { v4 as uuidv4 } from 'uuid';

export class MatchingService {
  private readonly WAITING_ROOM_TIMEOUT = parseInt(process.env.WAITING_ROOM_TIMEOUT || '600000'); // 10 minutes
  private readonly MIN_GROUP_SIZE = parseInt(process.env.MIN_GROUP_SIZE || '4');
  private readonly MAX_GROUP_SIZE = parseInt(process.env.MAX_GROUP_SIZE || '10');

  async joinMatching(userId: string, location: { longitude: number; latitude: number }) {
    // Update user location
    const user = await User.findByIdAndUpdate(
      userId,
      {
        $set: {
          location: {
            type: 'Point',
            coordinates: [location.longitude, location.latitude],
          },
          status: 'in_waiting_room',
          lastActive: new Date(),
        },
      },
      { new: true }
    );

    if (!user) {
      throw new AppError(404, 'User not found');
    }

    // Find compatible rooms
    const compatibleRoom = await this.findCompatibleRoom(user);

    if (compatibleRoom) {
      // Join existing room
      await this.addUserToRoom(compatibleRoom.roomId.toString(), userId);
      return {
        roomId: compatibleRoom.roomId,
        joined: true,
        message: 'Joined existing waiting room',
      };
    }

    // Create new room
    const newRoom = await this.createNewRoom(userId);
    return {
      roomId: newRoom.roomId,
      joined: true,
      message: 'Created new waiting room',
    };
  }

  async joinSpecificRoom(userId: string, roomId: string) {
    const room = await Room.findOne({ roomId, status: 'waiting' });

    if (!room) {
      throw new AppError(404, 'Room not found or already matched');
    }

    if (room.users.length >= room.maxMembers) {
      throw new AppError(400, 'Room is full');
    }

    if (room.users.includes(userId)) {
      throw new AppError(400, 'User already in room');
    }

    await this.addUserToRoom(room.roomId.toString(), userId);

    return {
      roomId: room.roomId,
      message: 'Successfully joined room',
    };
  }

  async leaveRoom(userId: string, roomId: string) {
    const room = await Room.findOne({ roomId });

    if (!room) {
      throw new AppError(404, 'Room not found');
    }

    if (!room.users.includes(userId)) {
      throw new AppError(400, 'User not in this room');
    }

    // Remove user from room
    room.users = room.users.filter((id) => id !== userId);
    await room.save();

    // Update user status
    await User.findByIdAndUpdate(userId, {
      $set: {
        status: 'active',
        currentRoomId: undefined,
      },
    });

    // Delete room if empty
    if (room.users.length === 0) {
      await Room.findByIdAndDelete(room._id);
    }

    return {
      message: 'Successfully left room',
      roomId: room.roomId,
    };
  }

  async getRoomStatus(roomId: string) {
    const room = await Room.findOne({ roomId });

    if (!room) {
      throw new AppError(404, 'Room not found');
    }

    const timeRemaining = Math.max(
      0,
      room.completionTime.getTime() - Date.now()
    );

    return {
      roomId: room.roomId,
      completionTime: room.completionTime,
      timeRemaining,
      numberOfMembers: room.users.length,
      minMembers: room.minMembers,
      maxMembers: room.maxMembers,
      status: room.status,
    };
  }

  async getRoomUsers(roomId: string) {
    const room = await Room.findOne({ roomId }).populate('users', 'name profilePicture credibilityScore bio');

    if (!room) {
      throw new AppError(404, 'Room not found');
    }

    return {
      roomId: room.roomId,
      users: room.users,
    };
  }

  private async findCompatibleRoom(user: any) {
    const now = new Date();

    // Find active waiting rooms
    const rooms = await Room.find({
      status: 'waiting',
      completionTime: { $gt: now },
      users: { $nin: [user._id.toString()] },
      $expr: { $lt: [{ $size: '$users' }, '$maxMembers'] },
    }).populate('users');

    if (rooms.length === 0) {
      return null;
    }

    // Find most compatible room based on preferences and location
    let bestRoom = null;
    let bestScore = -1;

    for (const room of rooms) {
      const score = await this.calculateRoomCompatibility(user, room);
      if (score > bestScore) {
        bestScore = score;
        bestRoom = room;
      }
    }

    return bestRoom;
  }

  private async calculateRoomCompatibility(user: any, room: any): Promise<number> {
    let score = 0;

    const roomUsers = await User.find({ _id: { $in: room.users } });

    for (const roomUser of roomUsers) {
      // Check cuisine preferences overlap
      const cuisineOverlap = user.preferences.cuisineTypes.filter((cuisine: string) =>
        roomUser.preferences.cuisineTypes.includes(cuisine)
      ).length;
      score += cuisineOverlap * 2;

      // Check budget compatibility (within 30% range)
      const budgetDiff = Math.abs(user.preferences.budget - roomUser.preferences.budget);
      if (budgetDiff < user.preferences.budget * 0.3) {
        score += 3;
      }

      // Check location proximity
      if (user.location && roomUser.location) {
        const distance = this.calculateDistance(
          user.location.coordinates,
          roomUser.location.coordinates
        );
        if (distance < Math.min(user.preferences.radiusKm, roomUser.preferences.radiusKm)) {
          score += 5;
        }
      }
    }

    // Prioritize rooms with higher average credibility
    const avgCredibility = roomUsers.reduce((sum, u) => sum + u.credibilityScore, 0) / roomUsers.length;
    score += avgCredibility;

    return score;
  }

  private calculateDistance(coord1: number[], coord2: number[]): number {
    const [lon1, lat1] = coord1;
    const [lon2, lat2] = coord2;

    const R = 6371; // Earth's radius in km
    const dLat = this.toRad(lat2 - lat1);
    const dLon = this.toRad(lon2 - lon1);

    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(this.toRad(lat1)) *
        Math.cos(this.toRad(lat2)) *
        Math.sin(dLon / 2) *
        Math.sin(dLon / 2);

    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
  }

  private toRad(degrees: number): number {
    return (degrees * Math.PI) / 180;
  }

  private async createNewRoom(userId: string) {
    const roomId = uuidv4();
    const completionTime = new Date(Date.now() + this.WAITING_ROOM_TIMEOUT);

    const room = await Room.create({
      roomId,
      users: [userId],
      completionTime,
      status: 'waiting',
      minMembers: this.MIN_GROUP_SIZE,
      maxMembers: this.MAX_GROUP_SIZE,
    });

    await User.findByIdAndUpdate(userId, {
      $set: { currentRoomId: roomId },
    });

    return room;
  }

  private async addUserToRoom(roomDbId: string, userId: string) {
    const room = await Room.findByIdAndUpdate(
      roomDbId,
      {
        $addToSet: { users: userId },
      },
      { new: true }
    );

    if (!room) {
      throw new AppError(404, 'Room not found');
    }

    await User.findByIdAndUpdate(userId, {
      $set: {
        currentRoomId: room.roomId,
        status: 'in_waiting_room',
      },
    });

    // Check if room is ready to form a group
    if (room.users.length >= room.minMembers) {
      // Optionally auto-form group when max members reached
      if (room.users.length === room.maxMembers) {
        await this.formGroup(room.roomId);
      }
    }

    return room;
  }

  async formGroup(roomId: string) {
    const room = await Room.findOne({ roomId, status: 'waiting' });

    if (!room) {
      throw new AppError(404, 'Room not found or already matched');
    }

    if (room.users.length < room.minMembers) {
      throw new AppError(400, 'Not enough users to form a group');
    }

    const groupId = uuidv4();
    const completionTime = new Date(Date.now() + 3600000); // 1 hour for voting

    const group = await Group.create({
      groupId,
      roomId: room.roomId,
      users: room.users,
      completionTime,
      restaurantSelected: false,
      status: 'voting',
    });

    // Update room status
    room.status = 'matched';
    await room.save();

    // Update all users
    await User.updateMany(
      { _id: { $in: room.users } },
      {
        $set: {
          status: 'in_group',
          currentGroupId: groupId,
          currentRoomId: undefined,
        },
      }
    );

    return group;
  }

  async checkExpiredRooms() {
    const now = new Date();
    const expiredRooms = await Room.find({
      status: 'waiting',
      completionTime: { $lte: now },
    });

    for (const room of expiredRooms) {
      if (room.users.length >= room.minMembers) {
        // Form group if minimum members met
        await this.formGroup(room.roomId);
      } else {
        // Mark as expired and notify users
        room.status = 'expired';
        await room.save();

        await User.updateMany(
          { _id: { $in: room.users } },
          {
            $set: {
              status: 'active',
              currentRoomId: undefined,
            },
          }
        );
      }
    }
  }
}