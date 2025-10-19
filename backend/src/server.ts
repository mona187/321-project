import express, { Express } from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import http from 'http';
import { connectDatabase } from './config/database';
import { initializeFirebase } from './config/firebase';
import socketManager from './utils/socketManager';
import { errorHandler, notFoundHandler } from './middleware/errorHandler';

// Import routes
import authRoutes from './routes/auth.routes';
import userRoutes from './routes/user.routes';
import matchingRoutes from './routes/matching.routes';
import groupRoutes from './routes/group.routes';
import restaurantRoutes from './routes/restaurant.routes';

import matchingService from './services/matchingService';
import groupService from './services/groupService';

// Load environment variables
dotenv.config();

const app: Express = express();
const PORT = process.env.PORT || 3000;

// Create HTTP server
const server = http.createServer(app);

// Middleware
app.use(cors({
  origin: process.env.ALLOWED_ORIGINS?.split(',') || ['http://localhost:3000'],
  credentials: true,
}));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Health check
app.get('/health', (_req, res) => {
  res.status(200).json({ 
    status: 'ok', 
    timestamp: new Date().toISOString(),
    uptime: process.uptime()
  });
});

// API Routes
app.use('/api/auth', authRoutes);
app.use('/api/user', userRoutes);
app.use('/api/matching', matchingRoutes);
app.use('/api/group', groupRoutes);
app.use('/api/restaurant', restaurantRoutes);

// 404 handler
app.use(notFoundHandler);

// Error handling middleware (must be last)
app.use(errorHandler);

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

    // Start background tasks (uncomment when services are ready)
    startBackgroundTasks();

    // Start server
    server.listen(PORT, () => {
      console.log('=================================');
      console.log(`🚀 Server running on port ${PORT}`);
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

export default app;