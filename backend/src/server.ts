import dotenv from 'dotenv';

// Load environment variables FIRST
dotenv.config();

import app from './app';  // Import app from the new file
import http from 'http';
import { connectDatabase } from './config/database';
import { initializeFirebase } from './config/firebase';
import socketManager from './utils/socketManager';
import matchingService from './services/matchingService';
import groupService from './services/groupService';

const PORT = process.env.PORT || 3000;

// Create HTTP server
const server = http.createServer(app);

// Initialize services and start server
const startServer = async () => {
  try {
    // Connect to database
    await connectDatabase();

    // Initialize Firebase (optional)
    try {
      initializeFirebase();
      console.log('✅ Firebase initialized successfully');
    } catch (error) {
      console.warn('⚠️  Firebase initialization skipped:', error);
    }

    // Initialize Socket.IO
    socketManager.initialize(server);

    // Start background tasks
    startBackgroundTasks();

    // Start server
    server.listen(Number(PORT), '0.0.0.0', () => {
      console.log('=================================');
      console.log(`🚀 Server running on http://0.0.0.0:${PORT}`);
      console.log(`📝 Environment: ${process.env.NODE_ENV || 'development'}`);
      console.log(`🔗 Health: http://localhost:${PORT}/health`);
      console.log('=================================');
    });
  } catch (error) {
    console.error('❌ Failed to start server:', error);
    process.exit(1);
  }
};

// Background tasks
function startBackgroundTasks() {
  // Check expired rooms every minute
  setInterval(async () => {
    try {
      await matchingService.checkExpiredRooms();
    } catch (error) {
      console.error('Error checking expired rooms:', error);
    }
  }, 60000); // 1 minute

  // Check expired groups every 2 minutes
  setInterval(async () => {
    try {
      await groupService.checkExpiredGroups();
    } catch (error) {
      console.error('Error checking expired groups:', error);
    }
  }, 120000); // 2 minutes

  console.log('✅ Background tasks started');
}

// Handle unhandled promise rejections
process.on('unhandledRejection', (reason: Error) => {
  console.error('❌ Unhandled Rejection:', reason);
  server.close(() => process.exit(1));
});

// Handle uncaught exceptions
process.on('uncaughtException', (error: Error) => {
  console.error('❌ Uncaught Exception:', error);
  server.close(() => process.exit(1));
});

// Graceful shutdown
process.on('SIGTERM', () => {
  console.log('SIGTERM signal received: closing HTTP server');
  server.close(() => {
    console.log('HTTP server closed');
    process.exit(0);
  });
});

process.on('SIGINT', () => {
  console.log('SIGINT signal received: closing HTTP server');
  server.close(() => {
    console.log('HTTP server closed');
    process.exit(0);
  });
});

// Start the server
startServer();

