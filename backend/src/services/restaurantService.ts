import axios from 'axios';
import { AppError } from '../middleware/errorHandler';
import { User } from '../models/User';

interface PlaceDetails {
  placeId: string;
  name: string;
  address: string;
  location: {
    lat: number;
    lng: number;
  };
  cuisineType: string[];
  priceLevel?: number;
  rating?: number;
  photoUrl?: string;
  phoneNumber?: string;
  website?: string;
  openingHours?: string[];
}

export class RestaurantService {
  private readonly GOOGLE_PLACES_API_KEY = process.env.GOOGLE_PLACES_API_KEY;
  private readonly PLACES_API_URL = 'https://maps.googleapis.com/maps/api/place';

  async getRestaurantsForGroup(groupUserIds: string[]) {
    // Get all users in the group
    const users = await User.find({ _id: { $in: groupUserIds } });

    if (users.length === 0) {
      throw new AppError(404, 'No users found');
    }

    // Calculate center point of all users
    const centerPoint = this.calculateCenterPoint(users);

    // Find common preferences
    const commonPreferences = this.findCommonPreferences(users);

    // Search for restaurants
    const restaurants = await this.searchRestaurants(
      centerPoint,
      commonPreferences
    );

    return restaurants;
  }

  async searchRestaurants(
    location: { lat: number; lng: number },
    preferences: {
      cuisineTypes: string[];
      maxBudget: number;
      radius: number;
    }
  ): Promise<PlaceDetails[]> {
    try {
      const url = `${this.PLACES_API_URL}/nearbysearch/json`;

      // Determine time of day for meal type
      const hour = new Date().getHours();
      let mealType = 'restaurant';
      if (hour >= 6 && hour < 11) {
        mealType = 'breakfast';
      } else if (hour >= 11 && hour < 15) {
        mealType = 'lunch';
      } else if (hour >= 15 && hour < 18) {
        mealType = 'cafe';
      } else {
        mealType = 'dinner';
      }

      const params = {
        location: `${location.lat},${location.lng}`,
        radius: preferences.radius * 1000, // Convert km to meters
        type: 'restaurant',
        keyword: preferences.cuisineTypes.join('|') || mealType,
        maxprice: Math.min(4, Math.ceil(preferences.maxBudget / 25)), // Convert to 0-4 scale
        key: this.GOOGLE_PLACES_API_KEY,
      };

      const response = await axios.get(url, { params });

      if (response.data.status !== 'OK' && response.data.status !== 'ZERO_RESULTS') {
        throw new AppError(500, `Places API error: ${response.data.status}`);
      }

      const restaurants: PlaceDetails[] = [];

      for (const place of response.data.results.slice(0, 10)) {
        const details = await this.getPlaceDetails(place.place_id);
        if (details) {
          restaurants.push(details);
        }
      }

      return restaurants;
    } catch (error) {
      console.error('Error searching restaurants:', error);
      throw new AppError(500, 'Failed to search restaurants');
    }
  }

  async getPlaceDetails(placeId: string): Promise<PlaceDetails | null> {
    try {
      const url = `${this.PLACES_API_URL}/details/json`;

      const params = {
        place_id: placeId,
        fields: 'name,formatted_address,geometry,types,price_level,rating,photos,formatted_phone_number,website,opening_hours',
        key: this.GOOGLE_PLACES_API_KEY,
      };

      const response = await axios.get(url, { params });

      if (response.data.status !== 'OK') {
        return null;
      }

      const place = response.data.result;

      // Get photo URL if available
      let photoUrl: string | undefined;
      if (place.photos && place.photos.length > 0) {
        photoUrl = `${this.PLACES_API_URL}/photo?maxwidth=400&photoreference=${place.photos[0].photo_reference}&key=${this.GOOGLE_PLACES_API_KEY}`;
      }

      return {
        placeId,
        name: place.name,
        address: place.formatted_address,
        location: {
          lat: place.geometry.location.lat,
          lng: place.geometry.location.lng,
        },
        cuisineType: place.types.filter((type: string) =>
          ['restaurant', 'food', 'cafe', 'bar'].includes(type)
        ),
        priceLevel: place.price_level,
        rating: place.rating,
        photoUrl,
        phoneNumber: place.formatted_phone_number,
        website: place.website,
        openingHours: place.opening_hours?.weekday_text,
      };
    } catch (error) {
      console.error('Error getting place details:', error);
      return null;
    }
  }

  async calculateTravelTime(
    origin: { lat: number; lng: number },
    destination: { lat: number; lng: number }
  ): Promise<{ distance: string; duration: string }> {
    try {
      const url = 'https://maps.googleapis.com/maps/api/distancematrix/json';

      const params = {
        origins: `${origin.lat},${origin.lng}`,
        destinations: `${destination.lat},${destination.lng}`,
        mode: 'driving',
        key: this.GOOGLE_PLACES_API_KEY,
      };

      const response = await axios.get(url, { params });

      if (response.data.status !== 'OK') {
        throw new AppError(500, `Distance Matrix API error: ${response.data.status}`);
      }

      const element = response.data.rows[0].elements[0];

      if (element.status !== 'OK') {
        throw new AppError(500, 'Could not calculate travel time');
      }

      return {
        distance: element.distance.text,
        duration: element.duration.text,
      };
    } catch (error) {
      console.error('Error calculating travel time:', error);
      throw new AppError(500, 'Failed to calculate travel time');
    }
  }

  private calculateCenterPoint(users: any[]): { lat: number; lng: number } {
    const validUsers = users.filter((user) => user.location);

    if (validUsers.length === 0) {
      // Default location if no users have location
      return { lat: 49.2827, lng: -123.1207 }; // Vancouver
    }

    const sumLat = validUsers.reduce(
      (sum, user) => sum + user.location.coordinates[1],
      0
    );
    const sumLng = validUsers.reduce(
      (sum, user) => sum + user.location.coordinates[0],
      0
    );

    return {
      lat: sumLat / validUsers.length,
      lng: sumLng / validUsers.length,
    };
  }

  private findCommonPreferences(users: any[]) {
    // Find cuisine types that appear in multiple users' preferences
    const cuisineMap = new Map<string, number>();
    users.forEach((user) => {
      user.preferences.cuisineTypes.forEach((cuisine: string) => {
        cuisineMap.set(cuisine, (cuisineMap.get(cuisine) || 0) + 1);
      });
    });

    const commonCuisines = Array.from(cuisineMap.entries())
      .sort((a, b) => b[1] - a[1])
      .slice(0, 3)
      .map(([cuisine]) => cuisine);

    // Calculate average budget
    const avgBudget =
      users.reduce((sum, user) => sum + user.preferences.budget, 0) /
      users.length;

    // Use minimum radius to ensure all users are within range
    const minRadius = Math.min(
      ...users.map((user) => user.preferences.radiusKm)
    );

    return {
      cuisineTypes: commonCuisines,
      maxBudget: avgBudget,
      radius: minRadius,
    };
  }
}