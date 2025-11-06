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
router.post('/logout', authMiddleware, asyncHandler((req, res, next) => authController.logout(req, res, next)));
router.post('/fcm-token', authMiddleware, asyncHandler((req, res, next) => authController.updateFCMToken(req, res, next)));
router.delete('/account', authMiddleware, asyncHandler((req, res, next) => authController.deleteAccount(req, res, next)));

router.get('/verify', authMiddleware, asyncHandler((req, res, next) => authController.verifyToken(req, res, next)));

export default router;