import mongoose, { Schema, Document } from 'mongoose';
import { IRoom } from '../types';

export interface IRoomDocument extends IRoom, Document {}

const roomSchema = new Schema<IRoomDocument>(
  {
    roomId: { type: String, required: true, unique: true, index: true },
    users: [{ type: String, ref: 'User' }],
    completionTime: { type: Date, required: true },
    status: {
      type: String,
      enum: ['waiting', 'matched', 'expired'],
      default: 'waiting',
    },
    minMembers: { type: Number, default: 4 },
    maxMembers: { type: Number, default: 10 },
  },
  {
    timestamps: true,
  }
);

roomSchema.index({ completionTime: 1 });
roomSchema.index({ status: 1 });

export const Room = mongoose.model<IRoomDocument>('Room', roomSchema);