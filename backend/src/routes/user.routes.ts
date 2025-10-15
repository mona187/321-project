import { Router } from 'express';
import { UserController } from '../controllers/user.controller';
import { authenticateToken } from '../middleware/auth.middleware';

const router = Router();
const userController = new UserController();

// All routes require authentication
router.use(authenticateToken);

router.get('/profile', userController.getProfile);
router.get('/settings', userController.getSettings);
router.post('/profile', userController.updateProfile);
router.post('/settings', userController.updateSettings);
router.put('/profile', userController.updateProfile);
router.delete('/:userId', userController.deleteUser);
router.post('/location', userController.updateLocation);
router.post('/fcm-token', userController.updateFCMToken);

export default router;