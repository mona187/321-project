import mongoose, { Schema, Document } from 'mongoose';

/**
 * Restaurant Interface (for cached data)
 */
export interface IRestaurant extends Document {
  placeId: string;
  name: string;
  address: string;
  location: {
    type: 'Point';
    coordinates: [number, number];
  };
  cuisineTypes: string[];
  priceLevel?: number;
  rating?: number;
  photos?: string[];
  phoneNumber?: string;
  website?: string;
  openingHours?: {
    weekdayText: string[];
  };
  lastUpdated: Date;
  createdAt: Date;
  updatedAt: Date;
}

/**
 * Restaurant Schema
 */
const RestaurantSchema = new Schema<IRestaurant>(
  {
    placeId: {
      type: String,
      required: true,
      unique: true,
      index: true,
    },
    name: {
      type: String,
      required: true,
    },
    address: {
      type: String,
      required: true,
    },
    location: {
      type: {
        type: String,
        enum: ['Point'],
        required: true,
      },
      coordinates: {
        type: [Number],
        required: true,
      },
    },
    cuisineTypes: {
      type: [String],
      default: [],
    },
    priceLevel: {
      type: Number,
      min: 1,
      max: 4,
    },
    rating: {
      type: Number,
      min: 0,
      max: 5,
    },
    photos: {
      type: [String],
      default: [],
    },
    phoneNumber: {
      type: String,
    },
    website: {
      type: String,
    },
    openingHours: {
      weekdayText: {
        type: [String],
        default: [],
      },
    },
    lastUpdated: {
      type: Date,
      default: Date.now,
    },
  },
  {
    timestamps: true,
  }
);

// Indexes for efficient queries
// RestaurantSchema.index({ placeId: 1 });
RestaurantSchema.index({ location: '2dsphere' });
RestaurantSchema.index({ cuisineTypes: 1 });
RestaurantSchema.index({ priceLevel: 1 });
RestaurantSchema.index({ rating: -1 });

export const Restaurant = mongoose.model<IRestaurant>('Restaurant', RestaurantSchema);