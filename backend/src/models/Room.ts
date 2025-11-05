import mongoose, { Schema, Model, HydratedDocument } from 'mongoose';

// Room status enum
export enum RoomStatus {
  WAITING = 'waiting',
  MATCHED = 'matched',
  EXPIRED = 'expired'
}

// Base Room interface (schema shape)
export interface IRoom {
  completionTime: Date;
  maxMembers: number;
  members: string[];
  status: RoomStatus;
  cuisine: string | null;
  averageBudget: number | null;
  averageRadius: number | null;
  createdAt?: Date;
  updatedAt?: Date;
}

// Instance methods on Room documents
export interface IRoomMethods {
  isFull(): boolean;
  isExpired(): boolean;
  addMember(userId: string): void;
  removeMember(userId: string): void;
  getTimeRemaining(): number;
}

// Document type = Schema + Methods + Mongoose doc
export type IRoomDocument = HydratedDocument<IRoom, IRoomMethods>;

// Static methods (Mongoose model)
export interface IRoomModel extends Model<IRoom, {}, IRoomMethods> {
  findActiveRooms(): Promise<IRoomDocument[]>;
  findByUserId(userId: string): Promise<IRoomDocument | null>;
}

// Schema
const RoomSchema = new Schema<IRoom, IRoomModel, IRoomMethods>(
  {
    completionTime: { type: Date, required: true, index: true },
    maxMembers: { type: Number, required: true, default: 4, min: 2, max: 10 },
    members: {
      type: [String],
      default: [],
      validate: {
        validator(this: IRoomDocument, v: string[]) {
          return v.length <= this.maxMembers;
        },
        message: 'Members cannot exceed maxMembers'
      }
    },
    status: {
      type: String,
      enum: Object.values(RoomStatus),
      default: RoomStatus.WAITING
    },
    cuisine: { type: String, default: null },
    averageBudget: { type: Number, default: null },
    averageRadius: { type: Number, default: null }
  },
  {
    timestamps: true,
    collection: 'rooms',
    toJSON: { virtuals: true },
    toObject: { virtuals: true }
  }
);

// Virtual: roomId
RoomSchema.virtual('roomId').get(function (this: IRoomDocument) {
  return this._id.toString();
});

// JSON transform (_id → roomId)
RoomSchema.set('toJSON', {
  virtuals: true,
  transform(_doc, ret: any) {
    const roomId = ret._id.toString();
    delete ret._id;
    delete ret.__v;
    return { roomId, ...ret };
  }
});

// Methods
RoomSchema.methods.isFull = function () { return this.members.length >= this.maxMembers; };
RoomSchema.methods.isExpired = function () { return new Date() > this.completionTime; };
RoomSchema.methods.addMember = function (userId: string) {
  if (!this.isFull() && !this.members.includes(userId)) this.members.push(userId);
};
RoomSchema.methods.removeMember = function (userId: string) {
  this.members = this.members.filter(id => id !== userId);
};
RoomSchema.methods.getTimeRemaining = function () {
  return Math.max(0, this.completionTime.getTime() - Date.now());
};

// Statics
RoomSchema.statics.findActiveRooms = function () {
  return this.find({ status: RoomStatus.WAITING, completionTime: { $gt: new Date() } });
};
RoomSchema.statics.findByUserId = function (userId: string) {
  return this.findOne({ members: userId, status: RoomStatus.WAITING });
};

// Auto-expire rooms
RoomSchema.pre('save', function (this: IRoomDocument, next) {
  if (this.isExpired() && this.status === RoomStatus.WAITING) {
    this.status = RoomStatus.EXPIRED;
  }
  next();
});

// Model
const Room = mongoose.model<IRoom, IRoomModel>('Room', RoomSchema);
export default Room;
