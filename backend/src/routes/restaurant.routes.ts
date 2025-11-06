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
router.get('/search', optionalAuth, asyncHandler(restaurantController.searchRestaurants.bind(restaurantController)));

/**
 * @route   GET /api/restaurant/:restaurantId
 * @desc    Get restaurant details by ID
 * @access  Public (optional auth)
 */
router.get('/:restaurantId', optionalAuth, asyncHandler(restaurantController.getRestaurantDetails.bind(restaurantController)));

/**
 * @route   POST /api/restaurant/recommendations/:groupId
 * @desc    Get restaurant recommendations for a group
 * @access  Private
 */
router.post('/recommendations/:groupId', authMiddleware, asyncHandler((req, res, next) => restaurantController.getGroupRecommendations(req, res, next)));

export default router;