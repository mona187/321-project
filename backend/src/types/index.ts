export interface IUser {
  _id: string;
  googleId: string;
  name: string;
  email: string;
  bio?: string;
  profilePicture?: string;
  contactNumber?: string;
  preferences: {
    cuisineTypes: string[];
    budget: number;
    radiusKm: number;
  };
  credibilityScore: number;
  location?: {
    type: 'Point';
    coordinates: [number, number]; // [longitude, latitude]
  };
  fcmToken?: string;
  currentRoomId?: string;
  currentGroupId?: string;
  status: 'active' | 'in_waiting_room' | 'in_group' | 'inactive';
  createdAt: Date;
  updatedAt: Date;
  lastActive: Date;
}

export interface IRoom {
  roomId: string;
  users: string[]; // Array of user IDs
  completionTime: Date;
  createdAt: Date;
  status: 'waiting' | 'matched' | 'expired';
  minMembers: number;
  maxMembers: number;
}

export interface IGroup {
  groupId: string;
  roomId: string;
  users: string[]; // Array of user IDs
  completionTime: Date;
  createdAt: Date;
  restaurantSelected: boolean;
  restaurant?: IRestaurant;
  votes: Map<string, string>; // userId -> restaurantId
  status: 'voting' | 'confirmed' | 'completed' | 'cancelled';
}

export interface IRestaurant {
  placeId: string;
  name: string;
  address: string;
  location: {
    lat: number;
    lng: number;
  };
  cuisineType: string[];
  priceLevel?: number;
  rating?: number;
  photoUrl?: string;
  phoneNumber?: string;
  website?: string;
  openingHours?: string[];
}

export interface ICredibilityLog {
  userId: string;
  groupId: string;
  checkedIn: boolean;
  timestamp: Date;
  scoreChange: number;
}