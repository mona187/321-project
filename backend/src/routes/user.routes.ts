import { Router } from 'express';
import { userController } from '../controllers/user.controller';
import { authMiddleware } from '../middleware/auth.middleware';

const router = Router();

/**
 * @route   GET /api/user/profile/:ids
 * @desc    Get user profiles by IDs (comma-separated)
 * @access  Public
 */
router.get('/profile/:ids', userController.getUserProfiles.bind(userController));

/**
 * @route   GET /api/user/settings
 * @desc    Get current user's settings
 * @access  Private
 */
router.get('/settings', authMiddleware, userController.getUserSettings.bind(userController));

/**
 * @route   POST /api/user/profile
 * @desc    Create/update user profile
 * @access  Private
 */
router.post('/profile', authMiddleware, userController.createUserProfile.bind(userController));

/**
 * @route   POST /api/user/settings
 * @desc    Update user settings
 * @access  Private
 */
router.post('/settings', authMiddleware, userController.updateUserSettings.bind(userController));

/**
 * @route   PUT /api/user/profile
 * @desc    Update user profile
 * @access  Private
 */
router.put('/profile', authMiddleware, userController.updateUserProfile.bind(userController));

/**
 * @route   DELETE /api/user/:userId
 * @desc    Delete user account
 * @access  Private
 */
router.delete('/:userId', authMiddleware, userController.deleteUser.bind(userController));

export default router;