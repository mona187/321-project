import { Router } from 'express';
import { groupController } from '../controllers/group.controller';
import { authMiddleware } from '../middleware/auth.middleware';

const router = Router();

/**
 * @route   GET /api/group/status
 * @desc    Get current user's group status
 * @access  Private
 */
router.get('/status', authMiddleware, groupController.getGroupStatus.bind(groupController));

/**
 * @route   POST /api/group/vote/:groupId
 * @desc    Vote for a restaurant
 * @access  Private
 */
router.post('/vote/:groupId', authMiddleware, groupController.voteForRestaurant.bind(groupController));

/**
 * @route   POST /api/group/leave/:groupId
 * @desc    Leave a group
 * @access  Private
 */
router.post('/leave/:groupId', authMiddleware, groupController.leaveGroup.bind(groupController));

export default router;