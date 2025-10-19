import admin from 'firebase-admin';

let firebaseApp: admin.app.App | null = null;

export const initializeFirebase = (): admin.app.App => {
  try {
    // Prevent multiple initializations
    if (firebaseApp) {
      return firebaseApp;
    }

    // Check if required environment variables are present
    const serviceAccountPath = process.env.FIREBASE_SERVICE_ACCOUNT_PATH;
    const serviceAccountKey = process.env.FIREBASE_SERVICE_ACCOUNT_KEY;

    if (!serviceAccountPath && !serviceAccountKey) {
      throw new Error(
        'Firebase configuration missing. Set either FIREBASE_SERVICE_ACCOUNT_PATH or FIREBASE_SERVICE_ACCOUNT_KEY'
      );
    }

    let credential: admin.credential.Credential;

    // Option 1: Load from file path (recommended for local development)
    if (serviceAccountPath) {
      credential = admin.credential.cert(serviceAccountPath);
    } 
    // Option 2: Load from JSON string (recommended for production/deployment)
    else if (serviceAccountKey) {
      const serviceAccount = JSON.parse(serviceAccountKey);
      credential = admin.credential.cert(serviceAccount);
    } else {
      throw new Error('No valid Firebase credential configuration found');
    }

    // Initialize Firebase Admin SDK
    firebaseApp = admin.initializeApp({
      credential: credential,
      databaseURL: process.env.FIREBASE_DATABASE_URL, // Optional: only if using Realtime Database
    });

    console.log('✅ Firebase Admin SDK initialized successfully');
    
    return firebaseApp;
  } catch (error) {
    console.error('❌ Failed to initialize Firebase:', error);
    throw error;
  }
};

// Get Firebase Messaging instance
export const getMessaging = (): admin.messaging.Messaging => {
  if (!firebaseApp) {
    throw new Error('Firebase not initialized. Call initializeFirebase() first');
  }
  return admin.messaging();
};

// Get Firestore instance (if needed)
export const getFirestore = (): admin.firestore.Firestore => {
  if (!firebaseApp) {
    throw new Error('Firebase not initialized. Call initializeFirebase() first');
  }
  return admin.firestore();
};

// Helper function to send push notification
export const sendPushNotification = async (
  token: string,
  notification: {
    title: string;
    body: string;
  },
  data?: { [key: string]: string }
): Promise<string> => {
  try {
    const messaging = getMessaging();
    
    const message: admin.messaging.Message = {
      token,
      notification,
      data,
      android: {
        priority: 'high',
        notification: {
          sound: 'default',
          priority: 'high',
        },
      },
      apns: {
        payload: {
          aps: {
            sound: 'default',
            badge: 1,
          },
        },
      },
    };

    const response = await messaging.send(message);
    console.log('✅ Push notification sent successfully:', response);
    return response;
  } catch (error) {
    console.error('❌ Error sending push notification:', error);
    throw error;
  }
};

// Helper function to send to multiple devices
export const sendMulticastNotification = async (
  tokens: string[],
  notification: {
    title: string;
    body: string;
  },
  data?: { [key: string]: string }
): Promise<admin.messaging.BatchResponse> => {
  try {
    const messaging = getMessaging();
    
    const message: admin.messaging.MulticastMessage = {
      tokens,
      notification,
      data,
      android: {
        priority: 'high',
        notification: {
          sound: 'default',
          priority: 'high',
        },
      },
      apns: {
        payload: {
          aps: {
            sound: 'default',
            badge: 1,
          },
        },
      },
    };

    const response = await messaging.sendEachForMulticast(message);
    
    console.log(`✅ Multicast notification sent: ${response.successCount} succeeded, ${response.failureCount} failed`);
    
    // Log any failures
    if (response.failureCount > 0) {
      response.responses.forEach((resp, idx) => {
        if (!resp.success) {
          console.error(`Failed to send to token ${tokens[idx]}:`, resp.error);
        }
      });
    }
    
    return response;
  } catch (error) {
    console.error('❌ Error sending multicast notification:', error);
    throw error;
  }
};

// Helper to send notification to a topic (e.g., all users in a group)
export const sendTopicNotification = async (
  topic: string,
  notification: {
    title: string;
    body: string;
  },
  data?: { [key: string]: string }
): Promise<string> => {
  try {
    const messaging = getMessaging();
    
    const message: admin.messaging.Message = {
      topic,
      notification,
      data,
      android: {
        priority: 'high',
      },
      apns: {
        payload: {
          aps: {
            sound: 'default',
          },
        },
      },
    };

    const response = await messaging.send(message);
    console.log(`✅ Topic notification sent to ${topic}:`, response);
    return response;
  } catch (error) {
    console.error('❌ Error sending topic notification:', error);
    throw error;
  }
};

// Subscribe users to a topic (useful for group notifications)
export const subscribeToTopic = async (
  tokens: string[],
  topic: string
): Promise<admin.messaging.MessagingTopicManagementResponse> => {
  try {
    const messaging = getMessaging();
    const response = await messaging.subscribeToTopic(tokens, topic);
    
    console.log(`✅ ${response.successCount} tokens subscribed to topic ${topic}`);
    
    if (response.failureCount > 0) {
      console.error(`Failed subscriptions: ${response.failureCount}`);
    }
    
    return response;
  } catch (error) {
    console.error('❌ Error subscribing to topic:', error);
    throw error;
  }
};

// Unsubscribe users from a topic
export const unsubscribeFromTopic = async (
  tokens: string[],
  topic: string
): Promise<admin.messaging.MessagingTopicManagementResponse> => {
  try {
    const messaging = getMessaging();
    const response = await messaging.unsubscribeFromTopic(tokens, topic);
    
    console.log(`✅ ${response.successCount} tokens unsubscribed from topic ${topic}`);
    
    return response;
  } catch (error) {
    console.error('❌ Error unsubscribing from topic:', error);
    throw error;
  }
};

export default {
  initializeFirebase,
  getMessaging,
  getFirestore,
  sendPushNotification,
  sendMulticastNotification,
  sendTopicNotification,
  subscribeToTopic,
  unsubscribeFromTopic,
};