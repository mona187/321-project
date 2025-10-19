import { Response, NextFunction } from 'express';
import { AuthRequest } from '../types';
import restaurantService from '../services/restaurantService';

export class RestaurantController {
  /**
   * GET /api/restaurant/search
   * Search for restaurants near a location
   */
  async searchRestaurants(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const { latitude, longitude, radius, cuisineTypes, priceLevel } = req.query;

      if (!latitude || !longitude) {
        res.status(400).json({
          Status: 400,
          Message: { error: 'Latitude and longitude are required' },
          Body: null
        });
        return;
      }

      const lat = parseFloat(latitude as string);
      const lng = parseFloat(longitude as string);
      const rad = radius ? parseInt(radius as string) : 5000;
      const cuisines = cuisineTypes ? (cuisineTypes as string).split(',') : undefined;
      const price = priceLevel ? parseInt(priceLevel as string) : undefined;

      const restaurants = await restaurantService.searchRestaurants(
        lat,
        lng,
        rad,
        cuisines,
        price
      );

      res.status(200).json({
        Status: 200,
        Message: {},
        Body: restaurants
      });
    } catch (error) {
      next(error);
    }
  }

  /**
   * GET /api/restaurant/:restaurantId
   * Get restaurant details by ID
   */
  async getRestaurantDetails(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const { restaurantId } = req.params;

      const restaurant = await restaurantService.getRestaurantDetails(restaurantId);

      res.status(200).json({
        Status: 200,
        Message: {},
        Body: restaurant
      });
    } catch (error) {
      next(error);
    }
  }

  /**
   * GET /api/restaurant/recommendations/:groupId
   * Get restaurant recommendations for a group
   */
  async getGroupRecommendations(req: AuthRequest, res: Response, next: NextFunction): Promise<void> {
    try {
      const { groupId } = req.params;
      const { userPreferences } = req.body;

      if (!userPreferences || !Array.isArray(userPreferences)) {
        res.status(400).json({
          Status: 400,
          Message: { error: 'User preferences array is required' },
          Body: null
        });
        return;
      }

      const recommendations = await restaurantService.getRecommendationsForGroup(
        groupId,
        userPreferences
      );

      res.status(200).json({
        Status: 200,
        Message: {},
        Body: recommendations
      });
    } catch (error) {
      next(error);
    }
  }
}

export const restaurantController = new RestaurantController();