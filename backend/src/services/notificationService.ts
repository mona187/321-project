import { sendPushNotification, sendMulticastNotification } from '../config/firebase';
import User from '../models/User';
import { NotificationPayload } from '../types';

/**
 * Send notification to a single user
 */
export const sendNotificationToUser = async (
  userId: string,
  notification: NotificationPayload
): Promise<void> => {
  try {
    const user = await User.findById(userId);
    
    if (!user) {
      throw new Error(`User not found: ${userId}`);
    }

    if (!user.fcmToken) {
      console.warn(`User ${userId} has no FCM token registered`);
      return;
    }

    await sendPushNotification(user.fcmToken, notification, notification.data);
    console.log(`✅ Notification sent to user ${userId}`);
  } catch (error) {
    console.error(`Failed to send notification to user ${userId}:`, error);
    throw error;
  }
};

/**
 * Send notification to multiple users
 */
export const sendNotificationToUsers = async (
  userIds: string[],
  notification: NotificationPayload
): Promise<void> => {
  try {
    const users = await User.find({ _id: { $in: userIds } });
    
    const tokens = users
      .map(user => user.fcmToken)
      .filter((token): token is string =>  token !== undefined);

    if (tokens.length === 0) {
      console.warn('No valid FCM tokens found for the provided users');
      return;
    }

    await sendMulticastNotification(tokens, notification, notification.data);
    console.log(`✅ Notification sent to ${tokens.length} users`);
  } catch (error) {
    console.error('Failed to send notifications to users:', error);
    throw error;
  }
};

/**
 * Send notification to all members of a room
 */
export const notifyRoomMembers = async (
  memberIds: string[],
  notification: NotificationPayload
): Promise<void> => {
  await sendNotificationToUsers(memberIds, notification);
};

/**
 * Send notification to all members of a group
 */
export const notifyGroupMembers = async (
  memberIds: string[],
  notification: NotificationPayload
): Promise<void> => {
  await sendNotificationToUsers(memberIds, notification);
};

/**
 * Notify user when room is ready (matched)
 */
export const notifyRoomMatched = async (
  userId: string,
  roomId: string,
  groupId: string
): Promise<void> => {
  const notification: NotificationPayload = {
    title: 'Group Matched! 🎉',
    body: 'Your waiting room is full! Time to vote for a restaurant.',
    data: {
      type: 'room_matched',
      roomId,
      groupId,
    },
  };

  await sendNotificationToUser(userId, notification);
};

/**
 * Notify user when room expires
 */
export const notifyRoomExpired = async (
  userId: string,
  roomId: string
): Promise<void> => {
  const notification: NotificationPayload = {
    title: 'Room Expired ⏰',
    body: 'Your waiting room expired. Try matching again!',
    data: {
      type: 'room_expired',
      roomId,
    },
  };

  await sendNotificationToUser(userId, notification);
};

/**
 * Notify group when restaurant is selected
 */
export const notifyRestaurantSelected = async (
  memberIds: string[],
  restaurantName: string,
  groupId: string
): Promise<void> => {
  const notification: NotificationPayload = {
    title: 'Restaurant Selected! 🍽️',
    body: `Your group chose ${restaurantName}. See you there!`,
    data: {
      type: 'restaurant_selected',
      groupId,
      restaurantName,
    },
  };

  await sendNotificationToUsers(memberIds, notification);
};

export default {
  sendNotificationToUser,
  sendNotificationToUsers,
  notifyRoomMembers,
  notifyGroupMembers,
  notifyRoomMatched,
  notifyRoomExpired,
  notifyRestaurantSelected,
};