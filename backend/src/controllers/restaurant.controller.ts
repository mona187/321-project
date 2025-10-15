import { Response, NextFunction } from 'express';
import { AuthRequest } from '../middleware/auth.middleware';
import { RestaurantService } from '../services/restaurantService';

const restaurantService = new RestaurantService();

export class RestaurantController {
  async getRestaurantsForGroup(
    req: AuthRequest,
    res: Response,
    next: NextFunction
  ) {
    try {
      const { groupId } = req.params;
      const { userIds } = req.body;

      if (!userIds || !Array.isArray(userIds)) {
        res.status(400).json({ error: 'User IDs array is required' });
        return;
      }

      const restaurants = await restaurantService.getRestaurantsForGroup(
        userIds
      );

      res.status(200).json({
        groupId,
        restaurants,
      });
    } catch (error) {
      next(error);
    }
  }

  async searchRestaurants(
    req: AuthRequest,
    res: Response,
    next: NextFunction
  ) {
    try {
      const { latitude, longitude, cuisineTypes, maxBudget, radius } =
        req.query;

      if (!latitude || !longitude) {
        res.status(400).json({ error: 'Location is required' });
        return;
      }

      const restaurants = await restaurantService.searchRestaurants(
        {
          lat: parseFloat(latitude as string),
          lng: parseFloat(longitude as string),
        },
        {
          cuisineTypes: cuisineTypes
            ? (cuisineTypes as string).split(',')
            : [],
          maxBudget: maxBudget ? parseFloat(maxBudget as string) : 50,
          radius: radius ? parseFloat(radius as string) : 10,
        }
      );

      res.status(200).json({ restaurants });
    } catch (error) {
      next(error);
    }
  }

  async getPlaceDetails(req: AuthRequest, res: Response, next: NextFunction) {
    try {
      const { placeId } = req.params;

      const details = await restaurantService.getPlaceDetails(placeId);

      if (!details) {
        res.status(404).json({ error: 'Place not found' });
        return;
      }

      res.status(200).json(details);
    } catch (error) {
      next(error);
    }
  }

  async calculateTravelTime(
    req: AuthRequest,
    res: Response,
    next: NextFunction
  ) {
    try {
      const { originLat, originLng, destLat, destLng } = req.query;

      if (!originLat || !originLng || !destLat || !destLng) {
        res.status(400).json({ error: 'Origin and destination required' });
        return;
      }

      const travelInfo = await restaurantService.calculateTravelTime(
        {
          lat: parseFloat(originLat as string),
          lng: parseFloat(originLng as string),
        },
        {
          lat: parseFloat(destLat as string),
          lng: parseFloat(destLng as string),
        }
      );

      res.status(200).json(travelInfo);
    } catch (error) {
      next(error);
    }
  }
}