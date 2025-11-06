import { Router } from 'express';
import { matchingController } from '../controllers/matching.controller';
import { authMiddleware } from '../middleware/auth.middleware';
import { asyncHandler } from '../middleware/errorHandler';

const router = Router();

/**
 * @route   POST /api/matching/join
 * @desc    Join the matching pool
 * @access  Private
 */
router.post('/join', authMiddleware, asyncHandler(matchingController.joinMatching.bind(matchingController)));

/**
 * @route   POST /api/matching/join/:roomId
 * @desc    Join a specific room (not implemented)
 * @access  Private
 */
router.post('/join/:roomId', authMiddleware, asyncHandler(matchingController.joinSpecificRoom.bind(matchingController)));

/**
 * @route   PUT /api/matching/leave/:roomId
 * @desc    Leave a waiting room
 * @access  Private
 */
router.put('/leave/:roomId', authMiddleware, asyncHandler(matchingController.leaveRoom.bind(matchingController)));

/**
 * @route   GET /api/matching/status/:roomId
 * @desc    Get status of a waiting room
 * @access  Private
 */
router.get('/status/:roomId', authMiddleware, asyncHandler(matchingController.getRoomStatus.bind(matchingController)));

/**
 * @route   GET /api/matching/users/:roomId
 * @desc    Get users in a room
 * @access  Private
 */
router.get('/users/:roomId', authMiddleware, asyncHandler(matchingController.getRoomUsers.bind(matchingController)));

export default router;