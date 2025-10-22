import { Server as SocketIOServer } from 'socket.io';
import { Server as HTTPServer } from 'http';
import jwt from 'jsonwebtoken';

/**
 * Socket.IO Configuration
 * Handles real-time communication for waiting rooms and group voting
 */

export const initializeSocket = (server: HTTPServer): SocketIOServer => {
  const io = new SocketIOServer(server, {
    cors: {
      origin: process.env.CORS_ORIGIN || '*',
      methods: ['GET', 'POST'],
      credentials: true,
    },
    pingTimeout: 60000,
    pingInterval: 25000,
  });

  // Authentication middleware
  io.use((socket, next) => {
    try {
      const token = socket.handshake.auth.token;

      if (!token) {
        return next(new Error('Authentication error: No token provided'));
      }

      const secret = process.env.JWT_SECRET;
      if (!secret) {
        return next(new Error('Server configuration error'));
      }

      // Verify JWT token
      const decoded = jwt.verify(token, secret) as { userId: string };
      socket.data.userId = decoded.userId;
      
      console.log(`✅ Socket authenticated for user: ${decoded.userId}`);
      next();
    } catch (error) {
      console.error('❌ Socket authentication failed:', error);
      next(new Error('Invalid token'));
    }
  });

  // Connection handler
  io.on('connection', (socket) => {
    const userId = socket.data.userId;
    console.log(`🔌 User connected via socket: ${userId} (${socket.id})`);

    // ==================== WAITING ROOM EVENTS ====================

    /**
     * Client emits: join_room
     * Payload: { userId: string }
     */
    socket.on('join_room', async (data: { userId: string }) => {
      try {
        console.log(`User ${data.userId} requesting to join room`);
        
        // Join the socket room (will be handled by matching service)
        // The actual room assignment is done via REST API
        socket.emit('join_room_ack', {
          success: true,
          message: 'Join room request received',
        });
      } catch (error) {
        console.error('Error in join_room:', error);
        socket.emit('error', { message: 'Failed to join room' });
      }
    });

    /**
     * Client emits: leave_room
     * Payload: { userId: string }
     */
    socket.on('leave_room', async (data: { userId: string }) => {
      try {
        console.log(`User ${data.userId} leaving room`);
        
        socket.emit('leave_room_ack', {
          success: true,
          message: 'Leave room request received',
        });
      } catch (error) {
        console.error('Error in leave_room:', error);
        socket.emit('error', { message: 'Failed to leave room' });
      }
    });

    // ==================== GROUP EVENTS ====================

    /**
     * Join a specific room (for receiving updates)
     */
    socket.on('subscribe_to_room', (roomId: string) => {
      socket.join(`room_${roomId}`);
      console.log(`User ${userId} subscribed to room ${roomId}`);
    });

    /**
     * Unsubscribe from room updates
     */
    socket.on('unsubscribe_from_room', (roomId: string) => {
      socket.leave(`room_${roomId}`);
      console.log(`User ${userId} unsubscribed from room ${roomId}`);
    });

    /**
     * Join a group (for voting updates)
     */
    socket.on('subscribe_to_group', (groupId: string) => {
      socket.join(`group_${groupId}`);
      console.log(`User ${userId} subscribed to group ${groupId}`);
    });

    /**
     * Unsubscribe from group updates
     */
    socket.on('unsubscribe_from_group', (groupId: string) => {
      socket.leave(`group_${groupId}`);
      console.log(`User ${userId} unsubscribed from group ${groupId}`);
    });

    // ==================== DISCONNECT ====================

    socket.on('disconnect', (reason) => {
      console.log(`🔌 User disconnected: ${userId} (Reason: ${reason})`);
    });

    // ==================== ERROR HANDLING ====================

    socket.on('error', (error) => {
      console.error(`❌ Socket error for user ${userId}:`, error);
    });
  });

  console.log('✅ Socket.IO initialized successfully');
  return io;
};

/**
 * Socket event emitter helpers
 * These functions are used by services to emit events to clients
 */

export class SocketEmitter {
  constructor(private io: SocketIOServer) {}

  /**
   * Server emits: room_update
   * Sent when a room's status changes (new member, etc.)
   */
  emitRoomUpdate(roomId: string, data: {
    roomId: string;
    members: string[];
    expiresAt: string;
    status: 'waiting' | 'matched' | 'expired';
  }) {
    this.io.to(`room_${roomId}`).emit('room_update', data);
    console.log(`📤 Emitted room_update for room ${roomId}`);
  }

  /**
   * Server emits: group_ready
   * Sent when a group is formed and ready for voting
   */
  emitGroupReady(roomId: string, groupId: string, members: string[]) {
    this.io.to(`room_${roomId}`).emit('group_ready', {
      groupId,
      members,
      ready: true,
    });
    console.log(`📤 Emitted group_ready for room ${roomId} → group ${groupId}`);
  }

  /**
   * Server emits: room_expired
   * Sent when a room expires without enough members
   */
  emitRoomExpired(roomId: string, reason: string = 'Not enough members') {
    this.io.to(`room_${roomId}`).emit('room_expired', {
      roomId,
      reason,
    });
    console.log(`📤 Emitted room_expired for room ${roomId}`);
  }

  /**
   * Server emits: vote_update
   * Sent when someone votes in the group
   */
  emitVoteUpdate(groupId: string, data: {
    restaurantId: string;
    votes: Record<string, number>;
    totalVotes: number;
    membersVoted: number;
    totalMembers: number;
  }) {
    this.io.to(`group_${groupId}`).emit('vote_update', data);
    console.log(`📤 Emitted vote_update for group ${groupId}`);
  }

  /**
   * Server emits: restaurant_selected
   * Sent when a restaurant is selected by majority vote
   */
  emitRestaurantSelected(groupId: string, data: {
    restaurantId: string;
    restaurantName: string;
    votes: Record<string, number>;
  }) {
    this.io.to(`group_${groupId}`).emit('restaurant_selected', data);
    console.log(`📤 Emitted restaurant_selected for group ${groupId}`);
  }

  /**
   * Server emits: member_joined
   * Sent when a new member joins a waiting room
   */
  emitMemberJoined(roomId: string, data: {
    userId: string;
    userName: string;
    currentMembers: number;
    maxMembers: number;
  }) {
    this.io.to(`room_${roomId}`).emit('member_joined', data);
    console.log(`📤 Emitted member_joined for room ${roomId}`);
  }

  /**
   * Server emits: member_left
   * Sent when a member leaves a waiting room or group
   */
  emitMemberLeft(roomId: string, data: {
    userId: string;
    userName: string;
    remainingMembers: number;
  }) {
    this.io.to(`room_${roomId}`).emit('member_left', data);
    console.log(`📤 Emitted member_left for room ${roomId}`);
  }

  /**
   * Emit to a specific user
   */
  emitToUser(userId: string, event: string, _data: any) {
    // This requires tracking user socket IDs
    // For now, we'll rely on room-based emissions
    console.log(`📤 Attempted to emit ${event} to user ${userId}`);
  }
}

export default initializeSocket;