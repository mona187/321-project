import { Router } from 'express';
import { authController } from '../controllers/auth.controller';
import { authMiddleware } from '../middleware/auth.middleware';

import { asyncHandler } from '../middleware/errorHandler';

import { RequestHandler } from 'express';


const router = Router();

// =======================
// 🔓 Public routes
// =======================
router.post('/signup', asyncHandler(async (req, res, next) => {
  await authController.signUp(req, res, next);
}));


router.post('/signin', asyncHandler(async (req, res, next) => {
  await authController.signIn(req, res, next);
}));

router.post('/google', asyncHandler(async (req, res, next) => {
  await authController.googleAuth(req, res, next); // Legacy endpoint
}));

// Protected routes
router.post('/logout', authMiddleware, authController.logout.bind(authController) as unknown as RequestHandler);
router.post('/fcm-token', authMiddleware, authController.updateFCMToken.bind(authController) as unknown as RequestHandler);
router.delete('/account', authMiddleware, authController.deleteAccount.bind(authController) as unknown as RequestHandler);

router.get('/verify', authMiddleware, authController.verifyToken.bind(authController) as unknown as RequestHandler);


// >>>>>>> jest-testing-sandhiya //// incase

// =======================
// 🔐 Protected routes
// =======================
// router.post('/logout', authMiddleware, asyncHandler(async (req, res, next) => {
//   await authController.logout(req, res, next);
// }));

// router.post('/fcm-token', authMiddleware, asyncHandler(async (req, res, next) => {
//   await authController.updateFCMToken(req, res, next);
// }));

// router.delete('/account', authMiddleware, asyncHandler(async (req, res, next) => {
//   await authController.deleteAccount(req, res, next);
// }));

// router.get('/verify', authMiddleware, asyncHandler(async (req, res, next) => {
//   await authController.verifyToken(req, res, next);
// }));

export default router;
