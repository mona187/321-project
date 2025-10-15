import { Router } from 'express';
import { GroupController } from '../controllers/group.controller';
import { authenticateToken } from '../middleware/auth.middleware';

const router = Router();
const groupController = new GroupController();

// All routes require authentication
router.use(authenticateToken);

router.get('/status/:groupId', groupController.getGroupStatus);
router.post('/leave/:groupId', groupController.leaveGroup);
router.post('/vote/:groupId', groupController.voteRestaurant);
router.get('/members/:groupId', groupController.getGroupMembers);
router.post('/checkin/:groupId', groupController.checkIn);

export default router;