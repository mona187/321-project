import { Router } from 'express';
import { AuthController } from '../controllers/auth.controller';

const router = Router();
const authController = new AuthController();

// Updated routes to match frontend expectations
router.post('/signin', authController.googleAuth);
router.post('/signup', authController.googleAuth);

export default router;