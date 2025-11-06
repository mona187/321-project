import { Router } from 'express';
import { authController } from '../controllers/auth.controller';
import { authMiddleware } from '../middleware/auth.middleware';
import { asyncHandler } from '../middleware/errorHandler';

const router = Router();

// =======================
// 🔓 Public routes
// =======================
router.post('/signup', asyncHandler(async (req, res, next) => {
  void await authController.signUp(req, res, next);
}));

router.post('/signin', asyncHandler(async (req, res, next) => {
  void await authController.signIn(req, res, next);
}));

router.post('/google', asyncHandler(async (req, res, next) => {
  void await authController.googleAuth(req, res, next); // Legacy endpoint
}));

// =======================
// 🔐 Protected routes
// =======================
router.post('/logout', authMiddleware, asyncHandler(async (req, res, next) => {
  void await authController.logout(req, res, next);
}));

router.post('/fcm-token', authMiddleware, asyncHandler(async (req, res, next) => {
  void await authController.updateFCMToken(req, res, next);
}));

router.delete('/account', authMiddleware, asyncHandler(async (req, res, next) => {
  void await authController.deleteAccount(req, res, next);
}));

router.get('/verify', authMiddleware, asyncHandler(async (req, res, next) => {
  void await authController.verifyToken(req, res, next);
}));

export default router;
