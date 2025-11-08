import mongoose, { Document, Schema, Model } from 'mongoose';

// Restaurant type as defined in your specifications
export interface IRestaurant {
  name: string;
  location: string;
  restaurantId?: string;
  url?: string;
  phoneNumber?: string;
  cuisine?: string;
  priceRange?: string;
}

// Base Group interface
export interface IGroup {
  roomId: string; // Reference to the room that created this group
  completionTime: Date; // When the group expires
  maxMembers: number;
  members: string[]; // Array of user IDs
  restaurantSelected: boolean;
  restaurant?: IRestaurant;
  votes: Map<string, string>; // Map of userId -> restaurantId
  restaurantVotes: Map<string, number>; // Map of restaurantId -> vote count
  createdAt: Date;
  updatedAt: Date;
}

// Instance methods interface
export interface IGroupMethods {
  addVote(userId: string, restaurantId: string): void;
  removeVote(userId: string): void;
  getWinningRestaurant(): string | null;
  hasAllVoted(): boolean;
  removeMember(userId: string): void;
}

// Document interface
export interface IGroupDocument extends Document, IGroup, IGroupMethods {
  groupId: string; // Virtual property
}

// Static methods interface
export interface IGroupModel extends Model<IGroup, {}, IGroupMethods> {
}

// Schema definition
const RestaurantSchema = new Schema<IRestaurant>(
  {
    name: { type: String, required: true },
    location: { type: String, required: true },
    restaurantId: { type: String },
    url: { type: String },
    phoneNumber: { type: String },
    cuisine: { type: String },
    priceRange: { type: String }
  },
  { _id: false }
);

const GroupSchema = new Schema<IGroup, IGroupModel, IGroupMethods>(
  {
    roomId: {
      type: String,
      required: true,
      index: true
    },
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
      required: true,
      default: []
    },
    restaurantSelected: {
      type: Boolean,
      default: false
    },
    restaurant: {
      type: RestaurantSchema,
      default: null
    },
    votes: {
      type: Map,
      of: String,
      default: new Map()
    },
    restaurantVotes: {
      type: Map,
      of: Number,
      default: new Map()
    }
  },
  {
    timestamps: true,
    collection: 'groups'
  }
);

// Indexes
// GroupSchema.index({ members: 1 });
// GroupSchema.index({ roomId: 1 });
// GroupSchema.index({ restaurantSelected: 1 });

// Virtual for groupId
GroupSchema.virtual('groupId').get(function() {
  return this._id.toString();
});

// Configure JSON serialization
GroupSchema.set('toJSON', {
  virtuals: true,
  transform: function(_doc, ret) {
    const groupId = ret._id.toString();
    const { _id, __v, ...rest } = ret;
    
    // Mongoose already converts Maps to plain objects before transform runs
    return { 
      groupId, 
      ...rest
    };
  }
});

GroupSchema.set('toObject', { 
  virtuals: true 
});

// Instance method: Add vote
GroupSchema.methods.addVote = function(userId: string, restaurantId: string): void {
  // Remove previous vote if exists
  const previousVote = this.votes.get(userId);
  if (previousVote) {
    const prevCount = this.restaurantVotes.get(previousVote) || 0;
    this.restaurantVotes.set(previousVote, Math.max(0, prevCount - 1));
  }
  
  // Add new vote
  this.votes.set(userId, restaurantId);
  const currentCount = this.restaurantVotes.get(restaurantId) || 0;
  this.restaurantVotes.set(restaurantId, currentCount + 1);
};

// Instance method: Remove vote
GroupSchema.methods.removeVote = function(userId: string): void {
  const restaurantId = this.votes.get(userId);
  if (restaurantId) {
    this.votes.delete(userId);
    const count = this.restaurantVotes.get(restaurantId) || 0;
    this.restaurantVotes.set(restaurantId, Math.max(0, count - 1));
  }
};

// Instance method: Get winning restaurant
GroupSchema.methods.getWinningRestaurant = function(): string | null {
  let maxVotes = 0;
  let winner: string | null = null;
  
  this.restaurantVotes.forEach((votes, restaurantId) => {
    if (votes > maxVotes) {
      maxVotes = votes;
      winner = restaurantId;
    }
  });
  
  return winner;
};

// Instance method: Check if all members have voted
GroupSchema.methods.hasAllVoted = function(): boolean {
  return this.votes.size === this.members.length;
};

// Instance method: Remove member
GroupSchema.methods.removeMember = function(userId: string): void {
  this.members = this.members.filter(id => id !== userId);
  this.removeVote(userId);
};

// Create model
const Group = mongoose.model<IGroup, IGroupModel>('Group', GroupSchema);

export default Group;