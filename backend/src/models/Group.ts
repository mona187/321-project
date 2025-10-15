import mongoose, { Schema, Document } from 'mongoose';
import { IGroup, IRestaurant } from '../types';

export interface IGroupDocument extends IGroup, Document {}

const restaurantSchema = new Schema({
  placeId: { type: String, required: true },
  name: { type: String, required: true },
  address: { type: String, required: true },
  location: {
    lat: { type: Number, required: true },
    lng: { type: Number, required: true },
  },
  cuisineType: [{ type: String }],
  priceLevel: { type: Number },
  rating: { type: Number },
  photoUrl: { type: String },
  phoneNumber: { type: String },
  website: { type: String },
  openingHours: [{ type: String }],
}, { _id: false });

const groupSchema = new Schema<IGroupDocument>(
  {
    groupId: { type: String, required: true, unique: true, index: true },
    roomId: { type: String, required: true },
    users: [{ type: String, ref: 'User' }],
    completionTime: { type: Date, required: true },
    restaurantSelected: { type: Boolean, default: false },
    restaurant: { type: restaurantSchema },
    votes: {
      type: Map,
      of: String,
      default: new Map(),
    },
    status: {
      type: String,
      enum: ['voting', 'confirmed', 'completed', 'cancelled'],
      default: 'voting',
    },
  },
  {
    timestamps: true,
  }
);

groupSchema.index({ status: 1 });
groupSchema.index({ completionTime: 1 });

export const Group = mongoose.model<IGroupDocument>('Group', groupSchema);