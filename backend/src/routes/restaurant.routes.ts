import { Router } from 'express';
import { RestaurantController } from '../controllers/restaurant.controller';
import { authenticateToken } from '../middleware/auth.middleware';

const router = Router();
const restaurantController = new RestaurantController();

// All routes require authentication
router.use(authenticateToken);

router.post('/group/:groupId', restaurantController.getRestaurantsForGroup);
router.get('/search', restaurantController.searchRestaurants);
router.get('/details/:placeId', restaurantController.getPlaceDetails);
router.get('/travel-time', restaurantController.calculateTravelTime);

export default router;