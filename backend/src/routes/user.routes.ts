import { Router } from 'express';
import { userController } from '../controllers/user.controller';
import { authMiddleware } from '../middleware/auth.middleware';
import { asyncHandler } from '../middleware/errorHandler';

const router = Router();

/**
 * @route   GET /api/user/profile/:ids
 * @desc    Get user profiles by IDs (comma-separated)
 * @access  Public
 */
router.get('/profile/:ids', asyncHandler(async (req, res, next) => {
  void await userController.getUserProfiles(req, res, next);
}));

/**
 * @route   GET /api/user/settings
 * @desc    Get current user's settings
 * @access  Private
 */
router.get('/settings', authMiddleware, asyncHandler(async (req, res, next) => {
  void await userController.getUserSettings(req, res, next);
}));

/**
 * @route   POST /api/user/profile
 * @desc    Create/update user profile
 * @access  Private
 */
router.post('/profile', authMiddleware, asyncHandler(async (req, res, next) => {
  void await userController.createUserProfile(req, res, next);
}));

/**
 * @route   POST /api/user/settings
 * @desc    Update user settings
 * @access  Private
 */
router.post('/settings', authMiddleware, asyncHandler(async (req, res, next) => {
  void await userController.updateUserSettings(req, res, next);
}));

/**
 * @route   PUT /api/user/profile
 * @desc    Update user profile
 * @access  Private
 */
router.put('/profile', authMiddleware, asyncHandler(async (req, res, next) => {
  void await userController.updateUserProfile(req, res, next);
}));

/**
 * @route   DELETE /api/user/:userId
 * @desc    Delete user account
 * @access  Private
 */
router.delete('/:userId', authMiddleware, asyncHandler(async (req, res, next) => {
  void await userController.deleteUser(req, res, next);
}));

export default router;
