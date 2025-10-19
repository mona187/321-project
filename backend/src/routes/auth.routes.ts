import { Router } from 'express';
import { googleAuth, logout, updateFCMToken, deleteAccount } from '../controllers/auth.controller';
import { authMiddleware } from '../middleware/auth.middleware';

const router = Router();

// Public routes
router.post('/google', googleAuth);

// Protected routes
router.post('/logout', authMiddleware, logout);
router.post('/fcm-token', authMiddleware, updateFCMToken);
router.delete('/account', authMiddleware, deleteAccount);

export default router;