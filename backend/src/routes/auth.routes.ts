import { Router } from 'express';
import { authController } from '../controllers/auth.controller';
import { authMiddleware } from '../middleware/auth.middleware';
import { asyncHandler } from '../middleware/errorHandler';

const router = Router();

// Public routes
router.post('/signup', asyncHandler(authController.signUp.bind(authController)));
router.post('/signin', asyncHandler(authController.signIn.bind(authController)));
router.post('/google', asyncHandler(authController.googleAuth.bind(authController))); // Legacy endpoint

// Protected routes
router.post('/logout', authMiddleware, asyncHandler(authController.logout.bind(authController)));
router.post('/fcm-token', authMiddleware, asyncHandler(authController.updateFCMToken.bind(authController)));
router.delete('/account', authMiddleware, asyncHandler(authController.deleteAccount.bind(authController)));

router.get('/verify', authMiddleware, asyncHandler(authController.verifyToken.bind(authController)));

export default router;