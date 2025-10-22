import mongoose, { Document, Schema, Model } from 'mongoose';

// Credibility action types
export enum CredibilityAction {
  NO_SHOW = 'no_show',                    // User didn't show up to restaurant
  LATE_CANCEL = 'late_cancel',            // Canceled too late
  LEFT_GROUP_EARLY = 'left_group_early',  // Left group before restaurant selected
  COMPLETED_MEETUP = 'completed_meetup',  // Successfully completed meetup
  POSITIVE_REVIEW = 'positive_review',    // Received positive review from group
  NEGATIVE_REVIEW = 'negative_review'     // Received negative review from group
}

// Base CredibilityLog interface
export interface ICredibilityLog {
  userId: string;
  action: CredibilityAction;
  scoreChange: number; // Positive or negative change
  groupId?: string; // Reference to group if applicable
  roomId?: string; // Reference to room if applicable
  previousScore: number;
  newScore: number;
  notes?: string;
  createdAt: Date;
}

// Instance methods interface
export interface ICredibilityLogMethods {
  // Add methods if needed
}

// Document interface
export interface ICredibilityLogDocument extends Document, ICredibilityLog, ICredibilityLogMethods {
  logId: string; // Virtual property
}

// Static methods interface
export interface ICredibilityLogModel extends Model<ICredibilityLog, {}, ICredibilityLogMethods> {
  findByUserId(userId: string, limit?: number): Promise<ICredibilityLogDocument[]>;
  getRecentLogs(days?: number): Promise<ICredibilityLogDocument[]>;
}

// Schema definition
const CredibilityLogSchema = new Schema<ICredibilityLog, ICredibilityLogModel, ICredibilityLogMethods>(
  {
    userId: {
      type: String,
      required: true,
      index: true
    },
    action: {
      type: String,
      enum: Object.values(CredibilityAction),
      required: true
    },
    scoreChange: {
      type: Number,
      required: true
    },
    groupId: {
      type: String,
      default: null
    },
    roomId: {
      type: String,
      default: null
    },
    previousScore: {
      type: Number,
      required: true,
      min: 0,
      max: 100
    },
    newScore: {
      type: Number,
      required: true,
      min: 0,
      max: 100
    },
    notes: {
      type: String,
      maxlength: 500
    }
  },
  {
    timestamps: { createdAt: true, updatedAt: false }, // Only need createdAt
    collection: 'credibility_logs'
  }
);

// Indexes
CredibilityLogSchema.index({ userId: 1, createdAt: -1 });
CredibilityLogSchema.index({ groupId: 1 });
CredibilityLogSchema.index({ roomId: 1 });
CredibilityLogSchema.index({ action: 1 });
CredibilityLogSchema.index({ createdAt: -1 });

// Virtual for logId
CredibilityLogSchema.virtual('logId').get(function() {
  return this._id.toString();
});

// Configure JSON serialization
CredibilityLogSchema.set('toJSON', {
  virtuals: true,
  transform: function(_doc, ret) {
    const logId = ret._id.toString();
    const { _id, __v, ...rest } = ret;
    return { logId, ...rest };
  }
});

CredibilityLogSchema.set('toObject', { 
  virtuals: true 
});

// Static method: Find logs by user ID
CredibilityLogSchema.statics.findByUserId = async function(
  userId: string, 
  limit: number = 50
): Promise<ICredibilityLogDocument[]> {
  return this.find({ userId })
    .sort({ createdAt: -1 })
    .limit(limit)
    .exec() as unknown as Promise<ICredibilityLogDocument[]>;
};

// Static method: Get recent logs
CredibilityLogSchema.statics.getRecentLogs = async function(
  days: number = 30
): Promise<ICredibilityLogDocument[]> {
  const dateThreshold = new Date();
  dateThreshold.setDate(dateThreshold.getDate() - days);
  
  return this.find({
    createdAt: { $gte: dateThreshold }
  }).sort({ createdAt: -1 })
    .exec() as unknown as Promise<ICredibilityLogDocument[]>;
};

// Create model
const CredibilityLog = mongoose.model<ICredibilityLog, ICredibilityLogModel>(
  'CredibilityLog',
  CredibilityLogSchema
);

export default CredibilityLog;