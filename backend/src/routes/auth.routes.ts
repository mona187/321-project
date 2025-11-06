import { Router } from 'express';
import { authController } from '../controllers/auth.controller';
import { authMiddleware } from '../middleware/auth.middleware';
import { asyncHandler } from '../middleware/errorHandler';

const router = Router();

// =======================
// 🔓 Public routes
// =======================
router.post('/signup', asyncHandler((req, res, next): Promise<void> => {
  return authController.signUp(req, res, next);
}));

router.post('/signin', asyncHandler((req, res, next): Promise<void> => {
  return authController.signIn(req, res, next);
}));

router.post('/google', asyncHandler((req, res, next): Promise<void> => {
  return authController.googleAuth(req, res, next); // Legacy endpoint
}));

// =======================
// 🔐 Protected routes
// =======================
router.post('/logout', authMiddleware, asyncHandler((req, res, next): Promise<void> => {
  return authController.logout(req, res, next);
}));

router.post('/fcm-token', authMiddleware, asyncHandler((req, res, next): Promise<void> => {
  return authController.updateFCMToken(req, res, next);
}));

router.delete('/account', authMiddleware, asyncHandler((req, res, next): Promise<void> => {
  return authController.deleteAccount(req, res, next);
}));

router.get('/verify', authMiddleware, asyncHandler((req, res, next): Promise<void> => {
  return authController.verifyToken(req, res, next);
}));

export default router;
