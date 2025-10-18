import express, { Express } from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import http from 'http';
import { connectDatabase } from './config/database';
// import { initializeFirebase } from './config/firebase';
// import { initializeSocketManager } from './utils/socketManager';
import { errorHandler } from './middleware/errorHandler';

// Import routes
import authRoutes from './routes/auth.routes';
// import userRoutes from './routes/user.routes';
// import matchingRoutes from './routes/matching.routes';
// import groupRoutes from './routes/group.routes';
// import restaurantRoutes from './routes/restaurant.routes';

// Import services for background tasks
// import { MatchingService } from './services/matchingService';
// import { CredibilityService } from './services/credibilityService';

// Load environment variables
dotenv.config();

const app: Express = express();
const PORT = process.env.PORT || 3000;

// Create HTTP server
const server = http.createServer(app);

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Health check
app.get('/health', (req, res) => {
  res.status(200).json({ status: 'ok', timestamp: new Date().toISOString() });
});

// API Routes
app.use('/api/auth', authRoutes);
// app.use('/api/user', userRoutes);
// app.use('/api/matching', matchingRoutes);
// app.use('/api/group', groupRoutes);
// app.use('/api/restaurant', restaurantRoutes);

// Error handling middleware (must be last)
app.use(errorHandler);

// Initialize services and start server
const startServer = async () => {
  try {
    // Connect to database
    await connectDatabase();

    // Initialize Firebase
    // initializeFirebase();

    // Initialize Socket.IO
    // const socketManager = initializeSocketManager(server);
    // console.log('Socket.IO initialized');

    // Start background tasks
    // startBackgroundTasks();

    // Start server
    server.listen(PORT, () => {
      console.log(`Server is running on port ${PORT}`);
      console.log(`Environment: ${process.env.NODE_ENV}`);
    });
  } catch (error) {
    console.error('Failed to start server:', error);
    process.exit(1);
  }
};

// Background tasks
// function startBackgroundTasks() {
//   // const matchingService = new MatchingService();
//   // const credibilityService = new CredibilityService();

//   // Check expired rooms every minute
//   setInterval(async () => {
//     try {
//       // await matchingService.checkExpiredRooms();
//     } catch (error) {
//       console.error('Error checking expired rooms:', error);
//     }
//   }, 60000); // 1 minute

//   console.log('Background tasks started');
// }

// Handle graceful shutdown
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