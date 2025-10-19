import { Router } from 'express';
import { matchingController } from '../controllers/matching.controller';
import { authMiddleware } from '../middleware/auth.middleware';

const router = Router();

/**
 * @route   POST /api/matching/join
 * @desc    Join the matching pool
 * @access  Private
 */
router.post('/join', authMiddleware, matchingController.joinMatching.bind(matchingController));

/**
 * @route   POST /api/matching/join/:roomId
 * @desc    Join a specific room (not implemented)
 * @access  Private
 */
router.post('/join/:roomId', authMiddleware, matchingController.joinSpecificRoom.bind(matchingController));

/**
 * @route   PUT /api/matching/leave/:roomId
 * @desc    Leave a waiting room
 * @access  Private
 */
router.put('/leave/:roomId', authMiddleware, matchingController.leaveRoom.bind(matchingController));

/**
 * @route   GET /api/matching/status/:roomId
 * @desc    Get status of a waiting room
 * @access  Private
 */
router.get('/status/:roomId', authMiddleware, matchingController.getRoomStatus.bind(matchingController));

/**
 * @route   GET /api/matching/users/:roomId
 * @desc    Get users in a room
 * @access  Private
 */
router.get('/users/:roomId', authMiddleware, matchingController.getRoomUsers.bind(matchingController));

export default router;