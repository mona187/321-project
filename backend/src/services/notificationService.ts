import { getMessaging } from '../config/firebase';
import { User } from '../models/User';
import { AppError } from '../middleware/errorHandler';

export class NotificationService {
  async sendNotification(userId: string, notification: {
    title: string;
    body: string;
    data?: Record<string, string>;
  }) {
    try {
      const user = await User.findById(userId);

      if (!user || !user.fcmToken) {
        console.log(`No FCM token for user ${userId}`);
        return;
      }

      const message = {
        token: user.fcmToken,
        notification: {
          title: notification.title,
          body: notification.body,
        },
        data: notification.data || {},
      };

      const response = await getMessaging().send(message);
      console.log('Successfully sent notification:', response);

      return response;
    } catch (error) {
      console.error('Error sending notification:', error);
      // Don't throw error, just log it
    }
  }

  async sendMulticastNotification(
    userIds: string[],
    notification: {
      title: string;
      body: string;
      data?: Record<string, string>;
    }
  ) {
    try {
      const users = await User.find({
        _id: { $in: userIds },
        fcmToken: { $exists: true, $ne: null },
      });

      const tokens = users
        .map((user) => user.fcmToken)
        .filter((token): token is string => !!token);

      if (tokens.length === 0) {
        console.log('No valid FCM tokens found');
        return;
      }

      const message = {
        tokens,
        notification: {
          title: notification.title,
          body: notification.body,
        },
        data: notification.data || {},
      };

      const response = await getMessaging().sendEachForMulticast(message);
      console.log(
        `Successfully sent ${response.successCount} notifications`
      );

      return response;
    } catch (error) {
      console.error('Error sending multicast notification:', error);
    }
  }

  // Specific notification helpers
  async notifyRoomUpdate(roomId: string, userIds: string[], message: string) {
    await this.sendMulticastNotification(userIds, {
      title: 'Waiting Room Update',
      body: message,
      data: { type: 'room_update', roomId },
    });
  }

  async notifyGroupFormed(groupId: string, userIds: string[]) {
    await this.sendMulticastNotification(userIds, {
      title: 'Match Found!',
      body: 'Your group has been formed. Start voting on restaurants!',
      data: { type: 'group_formed', groupId },
    });
  }

  async notifyVotingStarted(groupId: string, userIds: string[]) {
    await this.sendMulticastNotification(userIds, {
      title: 'Voting Started',
      body: 'Start swiping on restaurant options!',
      data: { type: 'voting_started', groupId },
    });
  }

  async notifyRestaurantSelected(
    groupId: string,
    userIds: string[],
    restaurantName: string
  ) {
    await this.sendMulticastNotification(userIds, {
      title: 'Restaurant Selected!',
      body: `Your group will meet at ${restaurantName}`,
      data: { type: 'restaurant_selected', groupId },
    });
  }

  async notifyRoomExpired(userIds: string[]) {
    await this.sendMulticastNotification(userIds, {
      title: 'Waiting Room Expired',
      body: 'Not enough members joined. Please try again.',
      data: { type: 'room_expired' },
    });
  }
}