import mongoose, { Schema, Document } from 'mongoose';
import { ICredibilityLog } from '../types';

export interface ICredibilityLogDocument extends ICredibilityLog, Document {}

const credibilityLogSchema = new Schema<ICredibilityLogDocument>(
  {
    userId: { type: String, required: true, ref: 'User', index: true },
    groupId: { type: String, required: true, ref: 'Group' },
    checkedIn: { type: Boolean, required: true },
    timestamp: { type: Date, default: Date.now },
    scoreChange: { type: Number, required: true },
  },
  {
    timestamps: true,
  }
);

credibilityLogSchema.index({ userId: 1, groupId: 1 });

export const CredibilityLog = mongoose.model<ICredibilityLogDocument>(
  'CredibilityLog',
  credibilityLogSchema
);