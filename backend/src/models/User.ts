import mongoose, { Schema, Document } from 'mongoose';
import { IUser } from '../types';

export interface IUserDocument extends Document, Omit<IUser, '_id'> {}

const userSchema = new Schema<IUserDocument>(
  {
    googleId: { type: String, required: true, unique: true, index: true },
    name: { type: String, required: true },
    email: { type: String, required: true, unique: true },
    bio: { type: String, default: '' },
    profilePicture: { type: String },
    contactNumber: { type: String },
    preferences: {
      cuisineTypes: [{ type: String }],
      budget: { type: Number, required: true },
      radiusKm: { type: Number, required: true },
    },
    credibilityScore: { type: Number, default: 5.0, min: 0, max: 5 },
    location: {
      type: {
        type: String,
        enum: ['Point'],
        default: 'Point',
      },
      coordinates: {
        type: [Number], // [longitude, latitude]
        index: '2dsphere',
      },
    },
    fcmToken: { type: String },
    currentRoomId: { type: String },
    currentGroupId: { type: String },
    status: {
      type: String,
      enum: ['active', 'in_waiting_room', 'in_group', 'inactive'],
      default: 'active',
    },
    lastActive: { type: Date, default: Date.now },
  },
  {
    timestamps: true,
  }
);

// Indexes for performance
userSchema.index({ location: '2dsphere' });
userSchema.index({ credibilityScore: -1 });
userSchema.index({ status: 1 });

export const User = mongoose.model<IUserDocument>('User', userSchema);