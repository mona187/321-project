import { Server as SocketIOServer } from 'socket.io';
import { Server as HTTPServer } from 'http';
import initializeSocket, { SocketEmitter } from '../config/socket';

/**
 * Global Socket Manager
 * Singleton pattern for managing socket connections across the app
 */

class SocketManager {
  private static instance: SocketManager;
  private io: SocketIOServer | null = null;
  private emitter: SocketEmitter | null = null;

  private constructor() {}

  public static getInstance(): SocketManager {
    if (!SocketManager.instance) {
      SocketManager.instance = new SocketManager();
    }
    return SocketManager.instance;
  }

  /**
   * Initialize Socket.IO with HTTP server
   */
  public initialize(server: HTTPServer): void {
    if (this.io) {
      console.warn('⚠️  Socket.IO already initialized');
      return;
    }

    this.io = initializeSocket(server);
    this.emitter = new SocketEmitter(this.io);
    console.log('✅ SocketManager initialized');
  }

  /**
   * Get the Socket.IO instance
   */
  public getIO(): SocketIOServer {
    if (!this.io) {
      throw new Error('Socket.IO not initialized. Call initialize() first.');
    }
    return this.io;
  }

  /**
   * Get the SocketEmitter for emitting events
   */
  public getEmitter(): SocketEmitter {
    if (!this.emitter) {
      throw new Error('SocketEmitter not initialized. Call initialize() first.');
    }
    return this.emitter;
  }

  // ==================== CONVENIENCE METHODS ====================

  /**
   * Emit room update to all members in a room
   */
  public emitRoomUpdate(
    roomId: string,
    members: string[],
    expiresAt: Date,
    status: 'waiting' | 'matched' | 'expired'
  ): void {
    this.getEmitter().emitRoomUpdate(roomId, {
      roomId,
      members,
      expiresAt: expiresAt.toISOString(),
      status,
    });
  }

  /**
   * Notify that a group is ready
   */
  public emitGroupReady(roomId: string, groupId: string, members: string[]): void {
    this.getEmitter().emitGroupReady(roomId, groupId, members);
  }

  /**
   * Notify that a room has expired
   */
  public emitRoomExpired(roomId: string, reason?: string): void {
    this.getEmitter().emitRoomExpired(roomId, reason);
  }

  /**
   * Emit vote update to group
   */
  public emitVoteUpdate(
    groupId: string,
    restaurantId: string,
    votes: Record<string, number>,
    membersVoted: number,
    totalMembers: number
  ): void {
    const totalVotes = Object.values(votes).reduce((sum, count) => sum + count, 0);
    
    this.getEmitter().emitVoteUpdate(groupId, {
      restaurantId,
      votes,
      totalVotes,
      membersVoted,
      totalMembers,
    });
  }

  /**
   * Emit restaurant selected to group
   */
  public emitRestaurantSelected(
    groupId: string,
    restaurantId: string,
    restaurantName: string,
    votes: Record<string, number>
  ): void {
    this.getEmitter().emitRestaurantSelected(groupId, {
      restaurantId,
      restaurantName,
      votes,
    });
  }

  /**
   * Emit member joined to room
   */
  public emitMemberJoined(
    roomId: string,
    userId: string,
    userName: string,
    currentMembers: number,
    maxMembers: number
  ): void {
    this.getEmitter().emitMemberJoined(roomId, {
      userId,
      userName,
      currentMembers,
      maxMembers,
    });
  }

  /**
   * Emit member left to room
   */
  public emitMemberLeft(
    roomId: string,
    userId: string,
    userName: string,
    remainingMembers: number
  ): void {
    this.getEmitter().emitMemberLeft(roomId, {
      userId,
      userName,
      remainingMembers,
    });
  }


  /**
   * Emit an event directly to a specific user (by userId)
   */
  public emitToUser(userId: string, event: string, payload: any): void {
    const io = this.getIO();

    // You must make sure your socket connection stores userId on handshake
    for (const [_, socket] of io.sockets.sockets) {
      if ((socket as any).userId === userId) {
        socket.emit(event, payload);
        return;
      }
    }

    console.warn(`⚠️ emitToUser: No socket found for user ${userId}`);
  }
}

// Export singleton instance
export const socketManager = SocketManager.getInstance();
export default socketManager;