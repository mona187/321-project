import { Router } from 'express';
import { authController } from '../controllers/auth.controller';
import { authMiddleware } from '../middleware/auth.middleware';
import { RequestHandler } from 'express';

const router = Router();

// Public routes
router.post('/signup', authController.signUp.bind(authController));
router.post('/signin', authController.signIn.bind(authController));
router.post('/google', authController.googleAuth.bind(authController)); // Legacy endpoint

// Protected routes
router.post('/logout', authMiddleware, authController.logout.bind(authController) as unknown as RequestHandler);
router.post('/fcm-token', authMiddleware, authController.updateFCMToken.bind(authController) as unknown as RequestHandler);
router.delete('/account', authMiddleware, authController.deleteAccount.bind(authController) as unknown as RequestHandler);

router.get('/verify', authMiddleware, authController.verifyToken.bind(authController) as unknown as RequestHandler);

export default router;