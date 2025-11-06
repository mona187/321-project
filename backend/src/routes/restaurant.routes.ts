import { Router } from 'express';
import { restaurantController } from '../controllers/restaurant.controller';
import { authMiddleware, optionalAuth } from '../middleware/auth.middleware';
import { asyncHandler } from '../middleware/errorHandler';

const router = Router();

/**
 * @route   GET /api/restaurant/search
 * @desc    Search for restaurants near a location
 * @access  Public (optional auth)
 */
router.get('/search', optionalAuth, asyncHandler((req, res, next) => restaurantController.searchRestaurants(req, res, next)));

/**
 * @route   GET /api/restaurant/:restaurantId
 * @desc    Get restaurant details by ID
 * @access  Public (optional auth)
 */
router.get('/:restaurantId', optionalAuth, asyncHandler((req, res, next) => restaurantController.getRestaurantDetails(req, res, next)));

/**
 * @route   POST /api/restaurant/recommendations/:groupId
 * @desc    Get restaurant recommendations for a group
 * @access  Private
 */
router.post('/recommendations/:groupId', authMiddleware, asyncHandler(async (req, res, next) => {
  await restaurantController.getGroupRecommendations(req, res, next);
}));

export default router;