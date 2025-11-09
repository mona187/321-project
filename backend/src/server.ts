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
      console.log(`🔒 Environment: ${process.env.NODE_ENV || 'development'}`);
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
  setInterval(() => {
    void (async () => {
      try {
        await matchingService.checkExpiredRooms();
      } catch (error) {
        console.error('Error checking expired rooms:', error);
      }
    })();
  }, 60000); // 1 minute

  // Check expired groups every 2 minutes
  setInterval(() => {
    void (async () => {
      try {
        await groupService.checkExpiredGroups();
      } catch (error) {
        console.error('Error checking expired groups:', error);
      }
    })();
  }, 120000); // 2 minutes

  console.log('✅ Background tasks started');
}

// Shutdown state to prevent multiple shutdown attempts
let isShuttingDown = false;

// ✅ Handle unhandled promise rejections
// ALL code paths must call process.exit() to satisfy static analysis
process.on('unhandledRejection', (reason: Error) => {
  console.error('❌ Unhandled Rejection:', reason);
  console.error('Stack:', reason.stack);
  
  // If already shutting down, exit immediately
  if (isShuttingDown) {
    process.exit(1); // ✅ Exit even if duplicate
  }
  
  isShuttingDown = true;

  // Set timeout to force exit
  const shutdownTimeout = setTimeout(() => {
    console.error('⚠️  Forcing exit after timeout');
    process.exit(1); // ✅ Force exit after timeout
  }, 10000);

  // Attempt graceful shutdown
  server.close(() => {
    clearTimeout(shutdownTimeout);
    console.log('Server closed after unhandled rejection');
    process.exit(1); // ✅ Exit after graceful close
  });
  process.exitCode = 1;

}); // ✅ All paths lead to process.exit()

// ✅ Handle uncaught exceptions
// ALL code paths must call process.exit() to satisfy static analysis
process.on('uncaughtException', (error: Error) => {
  console.error('❌ Uncaught Exception:', error);
  console.error('Stack:', error.stack);
  
  // If already shutting down, exit immediately
  if (isShuttingDown) {
    process.exit(1); // ✅ Exit even if duplicate
  }
  
  isShuttingDown = true;

  // Set timeout to force exit
  const shutdownTimeout = setTimeout(() => {
    console.error('⚠️  Forcing exit after timeout');
    process.exit(1); // ✅ Force exit after timeout
  }, 10000);

  // Attempt graceful shutdown
  server.close(() => {
    clearTimeout(shutdownTimeout);
    console.log('Server closed after uncaught exception');
    process.exit(1); // ✅ Exit after graceful close
  });
  process.exitCode = 1;

}); // ✅ All paths lead to process.exit()

// ✅ Graceful shutdown on SIGTERM
process.on('SIGTERM', () => {
  console.log('SIGTERM signal received: closing HTTP server');
  
  // If already shutting down, exit immediately
  if (isShuttingDown) {
    process.exit(0); // ✅ Exit even if duplicate
  }
  
  isShuttingDown = true;

  // Set timeout to force exit
  const shutdownTimeout = setTimeout(() => {
    console.error('⚠️  Forcing exit after timeout');
    process.exit(0); // ✅ Force exit after timeout
  }, 10000);

  // Attempt graceful shutdown
  server.close(() => {
    clearTimeout(shutdownTimeout);
    console.log('HTTP server closed');
    process.exit(0); // ✅ Exit after graceful close
  });
}); // ✅ All paths lead to process.exit()

// ✅ Graceful shutdown on SIGINT (Ctrl+C)
process.on('SIGINT', () => {
  console.log('SIGINT signal received: closing HTTP server');
  
  // If already shutting down, exit immediately
  if (isShuttingDown) {
    process.exit(0); // ✅ Exit even if duplicate
  }
  
  isShuttingDown = true;

  // Set timeout to force exit
  const shutdownTimeout = setTimeout(() => {
    console.error('⚠️  Forcing exit after timeout');
    process.exit(0); // ✅ Force exit after timeout
  }, 10000);

  // Attempt graceful shutdown
  server.close(() => {
    clearTimeout(shutdownTimeout);
    console.log('HTTP server closed');
    process.exit(0); // ✅ Exit after graceful close
  });
}); // ✅ All paths lead to process.exit()

// Start the server
void startServer();