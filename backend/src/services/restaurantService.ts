import axios from 'axios';
import { AppError } from '../middleware/errorHandler';
import { RestaurantType } from '../types';

// Google Places API response types
interface GooglePlacePhoto {
  photo_reference: string;
  width?: number;
  height?: number;
}

interface GooglePlace {
  place_id: string;
  name?: string;
  formatted_address?: string;
  vicinity?: string;
  price_level?: number;
  rating?: number;
  photos?: GooglePlacePhoto[];
  formatted_phone_number?: string;
  website?: string;
  url?: string;
}

export class RestaurantService {
  private readonly GOOGLE_PLACES_API_KEY = process.env.GOOGLE_PLACES_API_KEY || '';

  /**
   * Search for restaurants near a location
   */
  async searchRestaurants(
    latitude: number,
    longitude: number,
    radius: number = 5000, // in meters
    cuisineTypes?: string[],
    priceLevel?: number
  ): Promise<RestaurantType[]> {
    try {
      // If no API key, return mock data
      if (!this.GOOGLE_PLACES_API_KEY) {
        console.warn('⚠️  No Google Places API key - returning mock data');
        return this.getMockRestaurants();
      }

      console.log('✅ Using Google Places API with key');

      // Build the search query
      const keyword = cuisineTypes && cuisineTypes.length > 0 ? cuisineTypes.join(' ') : 'restaurant';
      
      const response = await axios.get(
        'https://maps.googleapis.com/maps/api/place/nearbysearch/json',
        {
          params: {
            location: `${latitude},${longitude}`,
            radius: radius,
            type: 'restaurant',
            keyword: keyword,
            key: this.GOOGLE_PLACES_API_KEY,
          },
        }
      );

      console.log('📡 Google API Response Status:', response.data.status);

      if (response.data.status !== 'OK' && response.data.status !== 'ZERO_RESULTS') {
        throw new AppError(`Google Places API error: ${response.data.status}`, 500);
      }

      let results: GooglePlace[] = (response.data.results as GooglePlace[]) || [];
      console.log(`🍽️ Found ${results.length} restaurants from Google Places`);

      // Filter by price level if specified
      if (priceLevel) {
        results = results.filter((place: GooglePlace) => place.price_level === priceLevel);
      }

      return results.map((place: GooglePlace) => this.formatPlaceData(place));
    } catch (error: any) {
      if (error instanceof AppError) throw error;
      console.error('❌ Failed to search restaurants:', error.message);
      // Return mock data on error
      return this.getMockRestaurants();
    }
  }

  /**
   * Get restaurant details by place ID
   */
  async getRestaurantDetails(placeId: string): Promise<RestaurantType> {
    try {
      if (!this.GOOGLE_PLACES_API_KEY) {
        return this.getMockRestaurant(placeId);
      }

      // Fetch from Google Places API
      const response = await axios.get(
        'https://maps.googleapis.com/maps/api/place/details/json',
        {
          params: {
            place_id: placeId,
            fields: 'name,formatted_address,geometry,photos,price_level,rating,formatted_phone_number,website,opening_hours,types',
            key: this.GOOGLE_PLACES_API_KEY,
          },
        }
      );

      if (response.data.status !== 'OK') {
        throw new AppError(`Restaurant not found: ${response.data.status}`, 404);
      }


      const place = response.data.result as GooglePlace;
      return this.formatPlaceData(place);
    } catch (error: any) {
      if (error instanceof AppError) throw error;
      console.error('Failed to get restaurant details:', error);
      return this.getMockRestaurant(placeId);
    }
  }

  /**
   * Format place data from Google Places API
   */
  private formatPlaceData(place: GooglePlace): RestaurantType {
    return {
      name: place.name || '',
      location: place.formatted_address || place.vicinity || '',
      restaurantId: place.place_id,
      address: place.formatted_address || place.vicinity || '',
      priceLevel: place.price_level,
      rating: place.rating,
      photos: place.photos?.map((photo: GooglePlacePhoto) => this.getPhotoUrl(photo.photo_reference)) || [],
      phoneNumber: place.formatted_phone_number,
      website: place.website,
      url: place.url,
    };
  }

  /**
   * Get photo URL from photo reference
   */
  private getPhotoUrl(photoReference: string, maxWidth: number = 400): string {
    return `https://maps.googleapis.com/maps/api/place/photo?maxwidth=${maxWidth}&photo_reference=${photoReference}&key=${this.GOOGLE_PLACES_API_KEY}`;
  }

  /**
   * Get recommended restaurants for a group
   */
  async getRecommendationsForGroup(
    _groupId: string,
    userPreferences: Array<{
      cuisineTypes: string[];
      budget: number;
      location: { coordinates: [number, number] };
      radiusKm: number;
    }>
  ): Promise<RestaurantType[]> {
    // Calculate average location
    const avgLat = userPreferences.reduce((sum, p) => sum + p.location.coordinates[1], 0) / userPreferences.length;
    const avgLng = userPreferences.reduce((sum, p) => sum + p.location.coordinates[0], 0) / userPreferences.length;

    // Get all cuisine preferences
    const allCuisines = [...new Set(userPreferences.flatMap(p => p.cuisineTypes))];

    // Calculate average budget (convert to price level 1-4)
    const avgBudget = userPreferences.reduce((sum, p) => sum + p.budget, 0) / userPreferences.length;
    const priceLevel = Math.ceil(avgBudget / 25); // Rough conversion

    // Get average radius
    const avgRadius = userPreferences.reduce((sum, p) => sum + p.radiusKm, 0) / userPreferences.length;

    // Search for restaurants
    const restaurants = await this.searchRestaurants(
      avgLat,
      avgLng,
      avgRadius * 1000, // Convert km to meters
      allCuisines,
      Math.min(4, priceLevel)
    );

    return restaurants;
  }

  /**
   * Mock data for testing without API key
   */
  private getMockRestaurants(): RestaurantType[] {
    return [
      {
        name: 'Sushi Paradise',
        location: '123 Main St, Vancouver, BC',
        restaurantId: 'mock_001',
        priceLevel: 2,
        rating: 4.5,
        phoneNumber: '+1-604-555-0001',
        url: 'https://example.com/sushi-paradise',
      },
      {
        name: 'Italian Bistro',
        location: '456 Oak Ave, Vancouver, BC',
        restaurantId: 'mock_002',
        priceLevel: 3,
        rating: 4.7,
        phoneNumber: '+1-604-555-0002',
        url: 'https://example.com/italian-bistro',
      },
      {
        name: 'Burger Joint',
        location: '789 Elm St, Vancouver, BC',
        restaurantId: 'mock_003',
        priceLevel: 1,
        rating: 4.2,
        phoneNumber: '+1-604-555-0003',
        url: 'https://example.com/burger-joint',
      },
    ];
  }

  private getMockRestaurant(id: string): RestaurantType {
    return {
      name: 'Sample Restaurant',
      location: '123 Sample St, Vancouver, BC',
      restaurantId: id,
      priceLevel: 2,
      rating: 4.5,
      phoneNumber: '+1-604-555-0000',
      url: 'https://example.com/sample-restaurant',
    };
  }
}

export default new RestaurantService();