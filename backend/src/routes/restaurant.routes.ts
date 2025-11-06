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
router.post('/join', authMiddleware, asyncHandler(async (req, res, next) => {
  await matchingController.joinMatching(req, res, next);
}));

/**
 * @route   POST /api/matching/join/:roomId
 * @desc    Join a specific room (not implemented)
 * @access  Private
 */
router.post('/join/:roomId', authMiddleware, asyncHandler(async (req, res, next) => {
  await matchingController.joinSpecificRoom(req, res, next);
}));

/**
 * @route   PUT /api/matching/leave/:roomId
 * @desc    Leave a waiting room
 * @access  Private
 */
router.put('/leave/:roomId', authMiddleware, asyncHandler(async (req, res, next) => {
  await matchingController.leaveRoom(req, res, next);
}));

/**
 * @route   GET /api/matching/status/:roomId
 * @desc    Get status of a waiting room
 * @access  Private
 */
router.get('/status/:roomId', authMiddleware, asyncHandler(async (req, res, next) => {
  await matchingController.getRoomStatus(req, res, next);
}));

/**
 * @route   GET /api/matching/users/:roomId
 * @desc    Get users in a room
 * @access  Private
 */
router.get('/users/:roomId', authMiddleware, asyncHandler(async (req, res, next) => {
  await matchingController.getRoomUsers(req, res, next);
}));

export default router;
