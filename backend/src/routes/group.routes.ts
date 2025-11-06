import { Router, Request, Response, NextFunction } from 'express';
import { groupController } from '../controllers/group.controller';
import { authMiddleware } from '../middleware/auth.middleware';
import { asyncHandler } from '../middleware/errorHandler';

const router = Router();

/**
 * @route   GET /api/group/status
 * @desc    Get current user's group status
 * @access  Private
 */
router.get('/status', authMiddleware, asyncHandler(async (req, res, next) => {
  await groupController.getGroupStatus(req, res, next);
}));

/**
 * @route   POST /api/group/vote/:groupId
 * @desc    Vote for a restaurant
 * @access  Private
 */
router.post('/vote/:groupId', authMiddleware, asyncHandler(async (req, res, next) => {
  await groupController.voteForRestaurant(req, res, next);
}));

/**
 * @route   POST /api/group/leave/:groupId
 * @desc    Leave a group
 * @access  Private
 */
const leaveGroupHandler = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  await groupController.leaveGroup(req, res, next);
};
router.post('/leave/:groupId', authMiddleware, asyncHandler(leaveGroupHandler));

export default router;
