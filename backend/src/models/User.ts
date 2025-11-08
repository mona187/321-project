import mongoose, { Document, Schema, Model } from 'mongoose';

// User status enum for better type safety
export enum UserStatus {
  OFFLINE = 0,
  ONLINE = 1,
  IN_WAITING_ROOM = 2,
  IN_GROUP = 3
}

// Base User interface (properties stored in DB)
export interface IUser {
  googleId: string;
  email: string;
  name: string;
  bio?: string;
  profilePicture?: string;
  preference: string[];
  credibilityScore: number;
  contactNumber?: string;
  budget?: number;
  radiusKm?: number;
  status: UserStatus;
  roomId?: string;
  groupId?: string;
  fcmToken?: string;  // Add this line
}

// Instance methods interface
export interface IUserMethods {
}

// Document interface (includes Document properties + virtuals)
export interface IUserDocument extends Document, IUser, IUserMethods {
  _id: mongoose.Types.ObjectId;
  userId: string; // Virtual property
  createdAt: Date;
  updatedAt: Date;
}

// Static methods interface
export interface IUserModel extends Model<IUser, {}, IUserMethods> {
}

// Profile interface (subset for public profile)
export interface IUserProfile {
  userId: string;
  name: string;
  bio?: string;
  preference: string[];
  profilePicture?: string;
}

// Schema definition
const UserSchema = new Schema<IUser, IUserModel, IUserMethods>(
  {
    googleId: {
      type: String,
      required: true,
      unique: true,
      index: true
    },
    email: {
      type: String,
      required: true,
      unique: true,
      lowercase: true,
      trim: true
    },
    name: {
      type: String,
      required: true,
      trim: true
    },
    bio: {
      type: String,
      default: '',
      maxlength: 500
    },
    profilePicture: {
      type: String,
      default: ''
    },
    preference: {
      type: [String],
      default: [],
      validate: {
        validator: function(v: string[]) {
          return v.length <= 20;
        },
        message: 'Cannot have more than 20 cuisine preferences'
      }
    },
    credibilityScore: {
      type: Number,
      default: 100.0,
      min: 0,
      max: 100
    },
    contactNumber: {
      type: String,
      trim: true,
      default: ''
    },
    budget: {
      type: Number,
      min: 0,
      default: 0
    },
    radiusKm: {
      type: Number,
      min: 0,
      max: 100,
      default: 5
    },
    status: {
      type: Number,
      enum: [0, 1, 2, 3], // UserStatus enum values
      default: UserStatus.OFFLINE
    },
    roomId: {
      type: String,
      default: null
    },
    groupId: {
      type: String,
      default: null
    },
    fcmToken: {
      type: String,
      default: null
    }
  },
  {
    timestamps: true,
    collection: 'users'
  }
);

// // Indexes
// UserSchema.index({ email: 1 });
// UserSchema.index({ googleId: 1 });
// UserSchema.index({ roomId: 1 });
// UserSchema.index({ groupId: 1 });
// UserSchema.index({ status: 1 });

// Virtual for userId
UserSchema.virtual('userId').get(function() {
  return this._id.toString();
});

// Configure JSON serialization
UserSchema.set('toJSON', {
  virtuals: true,
  transform: function(_doc, ret) {
    const userId = ret._id.toString();
    const { _id, __v, ...rest } = ret;
    return { userId, ...rest };
  }
});

UserSchema.set('toObject', { 
  virtuals: true 
});

// Pre-save hook
UserSchema.pre('save', function(next) {
  if (this.roomId && this.status !== UserStatus.IN_WAITING_ROOM) {
    this.status = UserStatus.IN_WAITING_ROOM;
  }
  
  if (this.groupId && this.status !== UserStatus.IN_GROUP) {
    this.status = UserStatus.IN_GROUP;
  }
  
  if (!this.roomId && this.status === UserStatus.IN_WAITING_ROOM) {
    this.status = UserStatus.ONLINE;
  }
  
  if (!this.groupId && this.status === UserStatus.IN_GROUP) {
    this.status = UserStatus.ONLINE;
  }
  
  next();
});

// Create model
const User = mongoose.model<IUser, IUserModel>('User', UserSchema);

export default User;