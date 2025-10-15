import { Router } from 'express';
import { MatchingController } from '../controllers/matching.controller';
import { authenticateToken } from '../middleware/auth.middleware';

const router = Router();
const matchingController = new MatchingController();

// All routes require authentication
router.use(authenticateToken);

router.post('/join', matchingController.joinMatching);
router.post('/join/:roomId', matchingController.joinSpecificRoom);
router.put('/leave/:roomId', matchingController.leaveRoom);
router.get('/status/:roomId', matchingController.getRoomStatus);
router.get('/users/:roomId', matchingController.getRoomUsers);

export default router;