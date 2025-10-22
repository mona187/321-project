import { Router } from 'express';
import { authController } from '../controllers/auth.controller';
import { authMiddleware } from '../middleware/auth.middleware';

const router = Router();

// Public routes
router.post('/signin', authController.googleAuth);
router.post('/signup', authController.googleAuth);

// Protected routes
router.post('/logout', authMiddleware, authController.logout.bind(authController));
router.post('/fcm-token', authMiddleware, authController.updateFCMToken.bind(authController));
router.delete('/account', authMiddleware, authController.deleteAccount.bind(authController));

router.get('/verify', authMiddleware, authController.verifyToken.bind(authController));

export default router;