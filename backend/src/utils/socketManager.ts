import { Server as SocketIOServer, Socket } from 'socket.io';
import { Server as HTTPServer } from 'http';
import jwt from 'jsonwebtoken';

export class SocketManager {
  private io: SocketIOServer;
  private userSockets: Map<string, string> = new Map(); // userId -> socketId

  constructor(server: HTTPServer) {
    this.io = new SocketIOServer(server, {
      cors: {
        origin: '*',
        methods: ['GET', 'POST'],
      },
    });

    this.setupMiddleware();
    this.setupEventHandlers();
  }

  private setupMiddleware() {
    this.io.use((socket, next) => {
      const token = socket.handshake.auth.token;

      if (!token) {
        return next(new Error('Authentication error'));
      }

      try {
        const secret = process.env.JWT_SECRET;
        if (!secret) {
          return next(new Error('Server configuration error'));
        }

        const decoded = jwt.verify(token, secret) as { userId: string };
        socket.data.userId = decoded.userId;
        next();
      } catch (error) {
        next(new Error('Invalid token'));
      }
    });
  }

  private setupEventHandlers() {
    this.io.on('connection', (socket: Socket) => {
      const userId = socket.data.userId;
      console.log(`User connected: ${userId}`);

      this.userSockets.set(userId, socket.id);

      // Join room
      socket.on('join_room', (roomId: string) => {
        socket.join(`room_${roomId}`);
        console.log(`User ${userId} joined room ${roomId}`);
      });

      // Leave room
      socket.on('leave_room', (roomId: string) => {
        socket.leave(`room_${roomId}`);
        console.log(`User ${userId} left room ${roomId}`);
      });

      // Join group
      socket.on('join_group', (groupId: string) => {
        socket.join(`group_${groupId}`);
        console.log(`User ${userId} joined group ${groupId}`);
      });

      // Leave group
      socket.on('leave_group', (groupId: string) => {
        socket.leave(`group_${groupId}`);
        console.log(`User ${userId} left group ${groupId}`);
      });

      // Disconnect
      socket.on('disconnect', () => {
        console.log(`User disconnected: ${userId}`);
        this.userSockets.delete(userId);
      });
    });
  }

  // Emit to specific room
  emitToRoom(roomId: string, event: string, data: any) {
    this.io.to(`room_${roomId}`).emit(event, data);
  }

  // Emit to specific group
  emitToGroup(groupId: string, event: string, data: any) {
    this.io.to(`group_${groupId}`).emit(event, data);
  }

  // Emit to specific user
  emitToUser(userId: string, event: string, data: any) {
    const socketId = this.userSockets.get(userId);
    if (socketId) {
      this.io.to(socketId).emit(event, data);
    }
  }

  // Get IO instance
  getIO() {
    return this.io;
  }
}

export let socketManager: SocketManager;

export const initializeSocketManager = (server: HTTPServer) => {
  socketManager = new SocketManager(server);
  return socketManager;
};