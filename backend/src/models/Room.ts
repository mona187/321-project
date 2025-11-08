import mongoose, { Document, Schema, Model } from 'mongoose';

// Room status enum
export enum RoomStatus {
  WAITING = 'waiting',
  MATCHED = 'matched',
  EXPIRED = 'expired'
}

// Base Room interface
export interface IRoom {
  completionTime: Date; // When the room expires or completes
  maxMembers: number; // Maximum number of members (e.g., 4)
  members: string[]; // Array of user IDs
  status: RoomStatus;
  cuisine?: string; // Cuisine preference for this room
  averageBudget?: number; // Average budget of members
  averageRadius?: number; // Average radius of members
  createdAt: Date;
  updatedAt: Date;
}

// Instance methods interface
export interface IRoomMethods {
  isExpired(): boolean;
}

// Document interface
export interface IRoomDocument extends Document, IRoom, IRoomMethods {
  roomId: string; // Virtual property
}

// Static methods interface
export interface IRoomModel extends Model<IRoom, {}, IRoomMethods> {
}

// Schema definition
const RoomSchema = new Schema<IRoom, IRoomModel, IRoomMethods>(
  {
    completionTime: {
      type: Date,
      required: true,
      index: true
    },
    maxMembers: {
      type: Number,
      required: true,
      default: 4,
      min: 2,
      max: 10
    },
    members: {
      type: [String],
      default: [],
      validate: {
        validator: function(this: IRoom, v: string[]) {
          return v.length <= this.maxMembers;
        },
        message: 'Members cannot exceed maxMembers limit'
      }
    },
    status: {
      type: String,
      enum: Object.values(RoomStatus),
      default: RoomStatus.WAITING
    },
    cuisine: {
      type: String,
      default: null
    },
    averageBudget: {
      type: Number,
      min: 0,
      default: null
    },
    averageRadius: {
      type: Number,
      min: 0,
      default: null
    }
  },
  {
    timestamps: true,
    collection: 'rooms'
  }
);

// // Indexes
// RoomSchema.index({ status: 1 });
// RoomSchema.index({ completionTime: 1 });
// RoomSchema.index({ members: 1 });

// Virtual for roomId
RoomSchema.virtual('roomId').get(function() {
  return this._id.toString();
});

// Configure JSON serialization
RoomSchema.set('toJSON', {
  virtuals: true,
  transform: function(_doc, ret) {
    const roomId = ret._id.toString();
    const { _id, __v, ...rest } = ret;
    return { roomId, ...rest };
  }
});

RoomSchema.set('toObject', { 
  virtuals: true 
});

// Instance method: Check if room is expired
RoomSchema.methods.isExpired = function(): boolean {
  return new Date() > this.completionTime;
};

// Pre-save hook: Auto-expire rooms
RoomSchema.pre('save', function(next) {
  if (this.isExpired() && this.status === RoomStatus.WAITING) {
    this.status = RoomStatus.EXPIRED;
  }
  next();
});

// Create model
const Room = mongoose.model<IRoom, IRoomModel>('Room', RoomSchema);

export default Room;