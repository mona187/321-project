// tests/helpers/socket.helper.ts
import { Server as HTTPServer } from 'http';
import { Server as SocketIOServer } from 'socket.io';
import { initializeSocket } from '../../src/config/socket';
import socketManager from '../../src/utils/socketManager';

let httpServer: HTTPServer;
let io: SocketIOServer;

/**
 * Initialize Socket.IO server for testing
 */
export async function initializeTestSocket(): Promise<void> {
  return new Promise((resolve) => {
    // Create HTTP server (no null check needed)
    httpServer = require('http').createServer();
    
    // Initialize Socket.IO
    io = initializeSocket(httpServer);
    
    // Initialize socket manager
    socketManager.initialize(httpServer);
    
    // Start server
    httpServer.listen(0, () => {
      const address = httpServer.address();
      const port = typeof address === 'string' ? 0 : address?.port || 0;
      console.log(`✅ Test Socket.IO server initialized on port ${port}`);
      resolve();
    });
  });
}

/**
 * Close Socket.IO server after tests
 */
export async function closeTestSocket(): Promise<void> {
  return new Promise((resolve) => {
    io?.close(() => {
      console.log('✅ Socket.IO server closed');
    });
    
    httpServer?.close(() => {
      console.log('✅ HTTP server closed');
      resolve();
    });
  });
}