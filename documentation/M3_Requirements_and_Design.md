## **Part I – Refined Project Requirements and Complete Design**

### **1\. Change History**

| Date | Section Changed | Rationale |
| ----- | ----- | ----- |
| 2025-10-26 | Use Case Diagram | Fixed errors related to scope of use cases |
| 2025-10-26 | Description of Use Cases | Fixed naming inconsistency and details related to implementation |
| 2025-10-26 | Non-Functional Requirements | Previous requirements were incorrect and not aligned with our app |

---

### **2\. Project Description**

*(Copy from M2, refine if needed. 1–2 paragraphs.)*

In today’s social media driven world, many people may struggle with spontaneity and meeting new people, especially in unfamiliar environments. Whether it’s the anxiety of stepping outside one's comfort zone or the uncertainty in planning social events, socializing may feel stressful. Our goal is to help address this issue. Through providing simple and quick restaurant recommendations without preplanning, this app promotes a stress-free way of connecting with others and exploring new places.

Through quick, personalized recommendations for restaurants based on user preferences, the app offers real-time suggestions and forms groups based on user choices. This project would encourage users to step outside their comfort zone and be more spontaneous in the hopes of bringing together diverse people.

The app targets a wide range of audiences such as people who are new to the city and may not know anyone or places to have fun, locals who are looking to break out of their routine and try something fun, or individuals looking for a nice break from their daily schedule to try out a new restaurant. Through encouraging people to explore new restaurants and strengthen their social circle, this app brings together a meaningful community.  
---

## **3\. Requirements Specification**

### **3.1 Project Features**

Features: 

1. **Authentication**: New users must sign up using Google Authentication service before using the application. To participate in main app features, the user must be signed in beforehand using Google sign-in. Users also have the option to sign out or remove their account.  
2. **Profile Management:** The user has the option to select a profile photo and fill in a short bio. The user must select their restaurant preferences, including maximum budget, maximum location (ex: within 10 km) and preferred types of cuisine. Preferences are not visible to other users and only used for internal matching logic.   
3. **Find Matches:** When the user is interested in attending a restaurant with a group, they should hit a button to trigger the app to use their current location and preferences to place them in a “waiting room”. The waiting room stays open for 10 minutes, while other matched users can also be placed in the room. If a user closes the app while being in a waiting room then rejoins the application, they will automatically be redirected to the waiting room screen to see who else has joined. They have the option to leave the room if they are no longer interested.   
4. **Group-based Voting:** Matched users will be placed in a group together and shown a restaurant option with a Yes or No button. If the majority of group members vote Yes, that restaurant is selected and all location information is shown in the group, along with the meeting time.  
5. **Credibility Score:** All users have a 5-star (100%) rating which is visible to other users. When attending an event, users must “check-in” on the app to confirm that they attended. If a user joins a group and does not attend the event, they lose points and their rating will drop. The credibility score affects how the user is given priority during matching.

---

### **3.2 Use Case Diagram**

*![][image1]*

---

### **3.3 Actors**

* User: The main participant who interacts with the app to log in, set preferences, find matches, join group chats, and attend events.

* Google Authentication Service: External service used for secure sign-in and account verification.

* Google Maps API: External service that provides restaurant data, locations, and distance calculations.

* Notification Service: External service (e.g., Firebase) that sends real-time alerts for matches, waiting room updates, polls, and check-ins.

---

### **3.4 Use Cases by Feature**

Features: 

Authentication

* **Sign Up**: The user signs up with Google authentication to create an account to access the app.  
* **Sign In:** The user signs in to access the app.  
* **Sign Out**: The user signs out when done.  
* **Remove Account**: The user deletes their account, and the system removes their data.

Profile Creation

* **Set Preferences**: The user specifies cuisine, budget, and distance preferences for matchmaking.  
* **Add Profile Information:** The user can add a bio, phone number and profile picture to their profile. The phone number will become visible to matched users only.   
* **Update Profile Information**: The user can go back and edit their profile after initial profile creation.

Match with Other Users

* **Request Matches (Join Waiting Room)**: The user presses the “Find match”  button or shakes their phone to trigger the matchmaking function. The system then places the user in a “Waiting Room” while matchmaking takes place to find a group of compatible users.   
  * The waiting room stays open for 10 minutes, while other matched users can also be placed in the room. A minimum of 4 members and maximum of 10 are required to form a matched “group”.   
  * If the 10 minute time-limit is reached and a minimum of 4 members are not placed in the waiting room, all users are notified that a group could not be formed at this time and they should try again later.   
  * The waiting room screen exits and moves to the next screen (Group Voting) when either 10 members have joined the waiting room or 10 minutes have been reached with at least 4 members.  
* **Exit Waiting Room**: The user may cancel and leave the waiting room at any time.  
* **Rejoin Waiting room:** The user may automatically re-enter a waiting room by opening the app.

Group- Based Voting

* **Vote on Restaurant**: Users are presented with restaurant options, which they can vote Yes/No on by swiping. Each restaurant option card shows an image of the restaurant interior, a short description of the type of cuisine, and the approximate commute time to the location. When the majority of users vote Yes, the restaurant is selected.  
* **View Restaurant choice:** Users view the final selected restaurant and are provided additional details. They are able to see information such as the address, Google review, restaurant phone number, website and level of occupancy.  
* **Leave/Rematch**: Users can exit the group or request a new match.  
* **View Group History:** Users can view all previous groups and corresponding group details.

Credibility Score

* **Check-In**: When a user registers for the application, they are given a 5-star credibility rating. The user confirms attendance at the restaurant to maintain their credibility rating by clicking the “check-in” button. If the user misses an event without checking in, their credibility score decreases. During the matching phase, priority is given for users with higher credibility scores to join waiting rooms. 

Other Use Cases

* **Receive Notifications:** Users will receive push notifications when the matching process has been completed, a waiting room has expired, and when voting has completed.   
* **View Member Profiles:** Users can view basic profile information such as name, bio, phone number etc. of other users who are in the same group.

---

### **3.5 Formal Use Case Specifications (5 most central use cases, exclude authentication)**

*General Extensions, Not Use case specific*  

- Generic wifi failure \[overwritten by specific wifi failures\]   
  - Error message is contained,   
    - If 4\*\* error display code,   
    - If 5\*\* display internal server error  
  - User is prompted to check their network connection and wait a set time period before trying again

*Profile Creation / Modification*   
**Title:** Profile Creation / Modification   
**Description:** The user creates and modifies preferences and items in their profile  
**Primary Actor:** Base User   
**Preconditions:** User is signed in and in the profile screen \[either through user choice or through initial sign up flow\]   
**Postconditions:** User is sent back to the previous dialog page they came from  
**Main Success Scenarios:** 

1. Actor *on signup* sets matchmaking preferences by pressing on the following preference fields where buttons displaying possible preferences are displayed (mandatory fields marked)  
   1. Preferences \[not final\]   
      1. Distance \[mandatory\] \-\> Integer in a range  
      2. Restaurant Type \-\> Multiple Answer   
2. Actor presses the add bio button, a text prompt is displayed where they can fill in their new bio for their page  
3. Actor presses the pencil icon within their profile picture, the actor is prompted with a photo input dialog \[device specific\]; the actor submits a photo to be used as their profile picture   
4. Actor while viewing profile updates matchmaking preferences by pressing on preferences buttons and inputting their ideal distance, the mandatory fields are filled \[via preference list in (1)\]   
   1. *Precondition: it is fair to assume mandatory fields have values at the start of this process*

**Extensions:**  
1a. User does not fill out all mandatory fields 

- 1a1. Fields not filled will be marked red   
- 1a2. A message is displayed telling the user to fill said fields

1b. During profile setup there are network problems

- 1b1. An error message is displayed   
- 1b2. User is prompted to check their network connection and wait a set time period 

1c. During profile setup user exits the app 

- Account is marked as incomplete on the server  
- When the user reopens the app they get prompted to complete their profile 

2a. Actor does not write anything

- 2a1. Empty bios are allowed, nothing happens. 

3a. Device specific dialog fails

- 3a1. Failure is noted and counted locally   
- 3a2. If this happens multiple times in a row we tell the user to check their permissions or try again later

3b. User does not submit an image & device dialog exits successfully 

- 3b1. No changes are made to the profile picture / previous image remains as current 

4a. User leaves mandatory fields blank while editing 

- 4a1. Mandatory fields marked in red  
- Message displayed telling user to fill said fields

4b. User exits app during edit

- Upon reopening the app, they are launched into the main menu screen;   
- *Optional (Implementation dependent): user data is not saved*

*Find Matches / Waiting Room*  
**Title:** Find Matches / Waiting Room  
**Description:** Dialog relating to finding, joining and exiting matches   
**Primary Actor:** Base User   
**Preconditions:** User logged in  
**Postconditions:**   
**Main Success Scenarios:** 

1. Select Find Match: The user presses the “Find Match Button” or shakes their phone, their screen changes to a waiting room screen  
   1. Waiting room screen contains an exit waiting room button, alongside information about the quantity of other users in the waiting room  
   2. Other users are notified that they joined the waiting room   
2. Exit Waiting Room: If the user presses the exit waiting room button they will be taken to the main menu screen; other users in waiting room are notified that they left  
3. Rejoin Waiting Room: When a user reopens the app they are put onto the main screen where they can rejoin the waiting room   
4. Receive Notification about Match: Given an implementation dependent condition \[likely enough users being present\] system notifies the users, via push notification if user does not have the app open, and transitions to the group screen. 

**Extensions:**  
1a. Waiting room timeout, not enough users for too long 

- 1a1. Users will be notified of this via a text box   
  - Text box tells users to try again later and displays reason for failure   
- 1a2. will be sent to the main menu screen

1b. Find match failure

- 1b1. Failure via network errors fall under the general network failure extension 

1c. User exits app while loading into waiting room \[process is not instant\] 

- 1c1. Upon reopening the app they are not in a waiting room, and cannot rejoin a waiting room; must restart the process

2a. User exits app during exit process 

- 2a1. When user reopens app they are not in the room 

4a. User is in the app

- 4a1. User receives an internal app notification not a device notification

4b. User is not in app

- 4a2. User receives a device-type notification which they can click on to enter the group

*Group-Based Voting*  
**Title:** Group Based Voting / Group Stage   
**Description:** Once in a group users can vote on restaurants to decide on a place to go  
**Primary Actor:** Base User   
**Preconditions:** User is within a group   
**Postconditions:**   
**Main Success Scenarios:** 

1. Vote on restaurant \- users are given a menu with restaurant options they can press yes/no by swiping. The restaurant’s dialog contain a picture, distance/location, cuisine type and description   
2. View restaurant choice \- Once a final restaurant is selected, users are provided with additional info: address information, google review information, phone numbers, website, and level of occupancy  
3. View member profiles \- Users can press a profile icon button to view other members in the group, this will show basic profile information including profile picture, bio and phone number, this will be a side panel that displays itself. The user’s information is included.   
4. Leave/Rematch \- Users are given a button where they can either leave the group, or request a new group 

**Extensions:**  
1a. Users do not agree on a restaurant 

- We continually prompt them for restaurants, after a certain amount of restaurants dialog is provided saying that if they do not choose through the next \[x\] restaurants we will rematch them. 

3a. No other members

- Users are displayed with their own information on the side-panel. 

4a. Users leaves once restaurant is chosen \[same with rejoin\] 

- They are notified that their credibility will reduce   
- They can select a confirm or cancel button to continue 

*Group Chat*   
**Title:** Group Chat feature   
**Description:** Once in a group, users can discuss information, allowing the decision process to be more open  
**Primary Actor:** Base User   
**Preconditions:** Users are in a group   
**Postconditions:**   
**Main Success Scenarios:** 

1. User can view all other messages in a dialog screen  
2. User can send messages by pressing a send button

**Extensions:**  
1a. User gets message while not in the app

- Message will pop up on their device as a notification they can select to reopen the app  
- After three messages are sent and they are outside the app, no more notifications will be sent. 

2a. User exits while message is sending

- Message will continue to send as long 

*Credibility Score*   
**Title:** Credibility Score  
**Description:** The users can develop their credibility through different user actions. When a user registers for the app they are given a 5-star score, based on participation this can be retained or reduced.  
**Primary Actor:** Base User   
**Preconditions:** User is authenticated   
**Postconditions:**  
**Main Success Scenarios:** 

1. Check-in \- When a group goes to a restaurant the user can choose to check-in, a dialog option that will pop up once the restaurant has been chosen.  

**Extensions:**  
1a. Check-in fails

- User is notified that it failed, they are notified they can try again \[x\] more times 

1b. Check-in success

- User is notified of the success and the modifications to their credibility score are noted. 

---

---

### **4\. Design Specification**

#### **4.1 Component Interfaces**

\[for reference\]  
![][image2]

*(List backend interfaces in Java-style signatures and frontend ↔ backend in REST-style)*

**Backend Interfaces**

# Component Interfaces Documentation

## Backend Component Interactions (Java-style Method Signatures)

---

## **1\. Authentication Service Interface**

**Purpose:** Handles Google OAuth verification, JWT token management, and user authentication lifecycle.

### **Methods:**

GoogleData verifyGoogleToken(String idToken)

* **Parameters:** `idToken` \- Google OAuth ID token from client  
* **Returns:** GoogleData object containing googleId, email, name, picture  
* **Description:** Verifies Google ID token with Google Auth Library and extracts user information for authentication.

User findOrCreateUser(GoogleData googleData)

* **Parameters:** `googleData` \- User information from Google authentication  
* **Returns:** User document from database  
* **Description:** Finds existing user by googleId or creates new user account; converts Google profile picture URL to Base64 format for storage.

String generateToken(User user)

* **Parameters:** `user` \- User document containing userId, email, googleId  
* **Returns:** JWT token string (valid for 7 days)  
* **Description:** Generates signed JWT token for authenticated user sessions using JWT\_SECRET.

TokenPayload verifyToken(String token)

* **Parameters:** `token` \- JWT authentication token  
* **Returns:** TokenPayload containing userId, email, googleId  
* **Description:** Validates JWT token signature and expiration; throws error if invalid.

void logoutUser(String userId)

* **Parameters:** `userId` \- User's unique identifier  
* **Returns:** void  
* **Description:** Sets user status to OFFLINE in database.

void updateFCMToken(String userId, String fcmToken)

* **Parameters:** `userId` \- User's unique identifier, `fcmToken` \- Firebase Cloud Messaging device token  
* **Returns:** void  
* **Description:** Updates user's FCM token for push notifications.

void deleteAccount(String userId)

* **Parameters:** `userId` \- User's unique identifier  
* **Returns:** void  
* **Description:** Deletes user account from system; throws error if user is in active room or group.

---

## **2\. User Service Interface**

**Purpose:** Manages user profiles, preferences, and settings.

### **Methods:**

List\<UserProfile\> getUserProfiles(List\<String\> userIds)

* **Parameters:** `userIds` \- List of user unique identifiers  
* **Returns:** List of UserProfile objects containing userId, name, bio, profilePicture, contactNumber  
* **Description:** Batch retrieves user profiles for displaying group member information.

UserSettings getUserSettings(String userId)

* **Parameters:** `userId` \- User's unique identifier  
* **Returns:** UserSettings object with all user data including preferences, credibilityScore, budget, radiusKm, status, roomID, groupID  
* **Description:** Retrieves complete user settings for profile/settings screens.

UserProfile createUserProfile(String userId, ProfileData data)

* **Parameters:** `userId` \- User's unique identifier, `data` \- Profile fields (name, bio, profilePicture, contactNumber)  
* **Returns:** Updated UserProfile object  
* **Description:** Creates or updates user profile information during onboarding.

UserSettings updateUserSettings(String userId, SettingsData data)

* **Parameters:** `userId` \- User's unique identifier, `data` \- Settings fields (name, bio, preference, profilePicture, contactNumber, budget, radiusKm)  
* **Returns:** Updated UserSettings object  
* **Description:** Updates user preferences and matching constraints; converts Google profile picture URLs to Base64.

UserProfile updateUserProfile(String userId, ProfileData data)

* **Parameters:** `userId` \- User's unique identifier, `data` \- Profile fields to update  
* **Returns:** Updated UserProfile object  
* **Description:** Updates user profile with new information; converts Google profile pictures to Base64.

DeleteResult deleteUser(String userId)

* **Parameters:** `userId` \- User's unique identifier  
* **Returns:** DeleteResult object with `deleted: true`  
* **Description:** Deletes user account; throws error if user in active room/group.

---

## **3\. Matching Service Interface**

**Purpose:** Handles room creation, matching algorithm, and group formation.

**Constants:**

* `ROOM_DURATION_MS = 2 minutes`  
* `MAX_MEMBERS = 10`  
* `MIN_MEMBERS = 2`  
* `MINIMUM_MATCH_SCORE = 30`  
* `VOTING_TIME = 30 minutes`

### **Methods:**

JoinMatchingResult joinMatching(String userId, MatchingCriteria criteria)

* **Parameters:** `userId` \- User's unique identifier, `criteria` \- Object containing cuisine\[\], budget, radiusKm  
* **Returns:** JoinMatchingResult object with roomId and room data  
* **Description:** Finds compatible room using scoring algorithm (cuisine: 50pts, budget: 30pts, radius: 20pts) or creates new room; automatically forms group when room reaches 10 members; emits real-time updates via Socket.IO.

void leaveRoom(String userId, String roomId)

* **Parameters:** `userId` \- User's unique identifier, `roomId` \- Room's unique identifier  
* **Returns:** void  
* **Description:** Removes user from waiting room; deletes room if empty, otherwise updates room averages and notifies remaining members via Socket.IO.

RoomStatus getRoomStatus(String roomId)

* **Parameters:** `roomId` \- Room's unique identifier  
* **Returns:** RoomStatus object with roomID, completionTime, members\[\], groupReady, status  
* **Description:** Retrieves current room state for countdown timer and member list display.

List\<String\> getRoomUsers(String roomId)

* **Parameters:** `roomId` \- Room's unique identifier  
* **Returns:** List of user IDs in the room  
* **Description:** Gets list of users currently in waiting room.

void checkExpiredRooms()

* **Parameters:** None  
* **Returns:** void  
* **Description:** Background task that checks for expired rooms; forms group if \>= 2 members, otherwise expires room and notifies users.

void createGroupFromRoom(String roomId)

* **Parameters:** `roomId` \- Room's unique identifier  
* **Returns:** void  
* **Description:** Internal method that converts full/expired room into group, updates user statuses, and sends notifications.

---

## **4\. Group Service Interface**

**Purpose:** Manages groups, restaurant voting, and group lifecycle.

### **Methods:**

GroupStatus getGroupStatus(String groupId)

* **Parameters:** `groupId` \- Group's unique identifier  
* **Returns:** GroupStatus object with groupId, roomId, completionTime, numMembers, users\[\], restaurantSelected, restaurant, status  
* **Description:** Retrieves current group state including voting progress and selected restaurant.

VoteResult voteForRestaurant(String userId, String groupId, String restaurantId, Restaurant restaurant)

* **Parameters:** `userId` \- User's unique identifier, `groupId` \- Group's unique identifier, `restaurantId` \- Restaurant's unique identifier, `restaurant` \- Optional restaurant details  
* **Returns:** VoteResult object with message and Current\_votes map  
* **Description:** Records user's vote; automatically selects restaurant when all members vote; emits real-time vote updates via Socket.IO; sends push notifications when restaurant selected.

void leaveGroup(String userId, String groupId)

* **Parameters:** `userId` \- User's unique identifier, `groupId` \- Group's unique identifier  
* **Returns:** void  
* **Description:** Removes user from group; deletes group if empty; auto-selects restaurant if remaining members all voted; notifies remaining members via Socket.IO.

Group getGroupByUserId(String userId)

* **Parameters:** `userId` \- User's unique identifier  
* **Returns:** Group object or null if not in group  
* **Description:** Finds active group that user is currently part of.

void closeGroup(String groupId)

* **Parameters:** `groupId` \- Group's unique identifier  
* **Returns:** void  
* **Description:** Closes/disbands group and updates all member statuses to ONLINE; used after restaurant visit or manual closure.

void checkExpiredGroups()

* **Parameters:** None  
* **Returns:** void  
* **Description:** Background task that checks for expired groups; auto-selects restaurant with most votes or disbands if no votes; sends notifications.

---

## **5\. Restaurant Service Interface**

**Purpose:** Integrates with Google Places API for restaurant search and recommendations.

### **Methods:**

List\<Restaurant\> searchRestaurants(double latitude, double longitude, int radius, List\<String\> cuisineTypes, int priceLevel)

* **Parameters:** `latitude` \- Location latitude, `longitude` \- Location longitude, `radius` \- Search radius in meters (default 5000), `cuisineTypes` \- Optional cuisine keywords, `priceLevel` \- Optional price filter (1-4)  
* **Returns:** List of Restaurant objects  
* **Description:** Searches Google Places API for restaurants matching criteria; falls back to mock data if no API key configured.

Restaurant getRestaurantDetails(String placeId)

* **Parameters:** `placeId` \- Google Places API place\_id  
* **Returns:** Restaurant object with detailed information  
* **Description:** Fetches detailed restaurant information including photos, hours, contact from Google Places API.

List\<Restaurant\> getRecommendationsForGroup(String groupId, List\<UserPreferences\> userPreferences)

* **Parameters:** `groupId` \- Group's unique identifier, `userPreferences` \- Array of individual user preferences  
* **Returns:** List of recommended Restaurant objects  
* **Description:** Calculates average location, combines cuisine preferences, averages budget/radius; searches for restaurants matching aggregated preferences.

---

## **6\. Credibility Service Interface**

**Purpose:** Manages user credibility scores based on participation and behavior.

**Score Changes:**

* `NO_SHOW: -15`  
* `LATE_CANCEL: -10`  
* `LEFT_GROUP_EARLY: -5`  
* `COMPLETED_MEETUP: +5`  
* `POSITIVE_REVIEW: +3`  
* `NEGATIVE_REVIEW: -8`

### **Methods:**

CredibilityChange updateCredibilityScore(String userId, CredibilityAction action, String groupId, String roomId, String notes)

* **Parameters:** `userId` \- User's unique identifier, `action` \- Type of action (enum), `groupId` \- Optional group context, `roomId` \- Optional room context, `notes` \- Optional description  
* **Returns:** CredibilityChange object with previousScore, newScore, scoreChange  
* **Description:** Updates user's credibility score (clamped 0-100) and logs the change with timestamp.

void recordCompletedMeetup(String userId, String groupId)

* **Parameters:** `userId` \- User's unique identifier, `groupId` \- Group's unique identifier  
* **Returns:** void  
* **Description:** Records successful meetup attendance (+5 credibility).

void recordNoShow(String userId, String groupId)

* **Parameters:** `userId` \- User's unique identifier, `groupId` \- Group's unique identifier  
* **Returns:** void  
* **Description:** Records user no-show at restaurant (-15 credibility).

void recordLeftGroupEarly(String userId, String groupId)

* **Parameters:** `userId` \- User's unique identifier, `groupId` \- Group's unique identifier  
* **Returns:** void  
* **Description:** Records user leaving group before restaurant selected (-5 credibility).

void recordLateCancellation(String userId, String roomId)

* **Parameters:** `userId` \- User's unique identifier, `roomId` \- Room's unique identifier  
* **Returns:** void  
* **Description:** Records late room cancellation (-10 credibility).

List\<CredibilityLog\> getUserCredibilityLogs(String userId, int limit)

* **Parameters:** `userId` \- User's unique identifier, `limit` \- Maximum logs to return (default 20\)  
* **Returns:** List of CredibilityLog objects  
* **Description:** Retrieves credibility change history for transparency.

CredibilityStats getUserCredibilityStats(String userId)

* **Parameters:** `userId` \- User's unique identifier  
* **Returns:** CredibilityStats object with currentScore, totalLogs, positiveActions, negativeActions, recentTrend  
* **Description:** Calculates statistics and trend (improving/stable/declining) based on recent 10 actions.

boolean isCredibilityAcceptable(double score, double minimumRequired)

* **Parameters:** `score` \- User's current score, `minimumRequired` \- Minimum threshold (default 50\)  
* **Returns:** Boolean indicating if score meets requirement  
* **Description:** Checks if user meets minimum credibility for matching.

void restoreCredibilityScore(String userId, int amount, String notes)

* **Parameters:** `userId` \- User's unique identifier, `amount` \- Points to restore, `notes` \- Reason for restoration  
* **Returns:** void  
* **Description:** Manually restores credibility (admin function or appeal system).

---

## **7\. Notification Service Interface**

**Purpose:** Sends Firebase Cloud Messaging push notifications to users.

### **Methods:**

void sendNotificationToUser(String userId, NotificationPayload notification)

* **Parameters:** `userId` \- User's unique identifier, `notification` \- Object with title, body, data  
* **Returns:** void  
* **Description:** Sends push notification to single user via FCM token; logs warning if no token registered.

void sendNotificationToUsers(List\<String\> userIds, NotificationPayload notification)

* **Parameters:** `userIds` \- List of user identifiers, `notification` \- Notification payload  
* **Returns:** void  
* **Description:** Sends multicast notification to multiple users efficiently.

void notifyRoomMembers(List\<String\> memberIds, NotificationPayload notification)

* **Parameters:** `memberIds` \- Room member user IDs, `notification` \- Notification payload  
* **Returns:** void  
* **Description:** Sends notification to all members of a waiting room.

void notifyGroupMembers(List\<String\> memberIds, NotificationPayload notification)

* **Parameters:** `memberIds` \- Group member user IDs, `notification` \- Notification payload  
* **Returns:** void  
* **Description:** Sends notification to all members of a group.

void notifyRoomMatched(String userId, String roomId, String groupId)

* **Parameters:** `userId` \- User's unique identifier, `roomId` \- Room ID, `groupId` \- New group ID  
* **Returns:** void  
* **Description:** Notifies user that room is full and group is ready for voting.

void notifyRoomExpired(String userId, String roomId)

* **Parameters:** `userId` \- User's unique identifier, `roomId` \- Room ID  
* **Returns:** void  
* **Description:** Notifies user that waiting room expired without enough members.

void notifyRestaurantSelected(List\<String\> memberIds, String restaurantName, String groupId)

* **Parameters:** `memberIds` \- Group member user IDs, `restaurantName` \- Selected restaurant name, `groupId` \- Group ID  
* **Returns:** void  
* **Description:** Notifies all group members when restaurant voting is complete.

---

## **8\. Socket Manager Interface**

**Purpose:** Manages real-time Socket.IO communication for rooms and groups.

### **Methods:**

void emitRoomUpdate(String roomId, List\<String\> members, Date expiresAt, String status)

* **Parameters:** `roomId` \- Room's unique identifier, `members` \- List of user IDs, `expiresAt` \- Expiration timestamp, `status` \- 'waiting'|'matched'|'expired'  
* **Returns:** void  
* **Description:** Broadcasts updated room state to all connected members in room channel.

void emitGroupReady(String roomId, String groupId, List\<String\> members)

* **Parameters:** `roomId` \- Room's unique identifier, `groupId` \- New group ID, `members` \- List of user IDs  
* **Returns:** void  
* **Description:** Notifies all room members that group formation is complete and voting begins.

void emitRoomExpired(String roomId, String reason)

* **Parameters:** `roomId` \- Room's unique identifier, `reason` \- Expiration reason message  
* **Returns:** void  
* **Description:** Notifies room members that room expired without forming group.

void emitVoteUpdate(String groupId, String restaurantId, Map\<String, Integer\> votes, int membersVoted, int totalMembers)

* **Parameters:** `groupId` \- Group's unique identifier, `restaurantId` \- Restaurant ID, `votes` \- Vote count map, `membersVoted` \- Number who voted, `totalMembers` \- Total group size  
* **Returns:** void  
* **Description:** Broadcasts real-time voting progress to all group members in group channel.

void emitRestaurantSelected(String groupId, String restaurantId, String restaurantName, Map\<String, Integer\> votes)

* **Parameters:** `groupId` \- Group's unique identifier, `restaurantId` \- Restaurant ID, `restaurantName` \- Restaurant name, `votes` \- Final vote counts  
* **Returns:** void  
* **Description:** Notifies all group members that restaurant has been selected by majority vote.

void emitMemberJoined(String roomId, String userId, String userName, int currentMembers, int maxMembers)

* **Parameters:** `roomId` \- Room's unique identifier, `userId` \- New member ID, `userName` \- New member name, `currentMembers` \- Current count, `maxMembers` \- Maximum capacity  
* **Returns:** void  
* **Description:** Notifies room members when new user joins waiting room.

void emitMemberLeft(String roomId, String userId, String userName, int remainingMembers)

* **Parameters:** `roomId` \- Room's unique identifier, `userId` \- Departing user ID, `userName` \- Departing user name, `remainingMembers` \- Remaining member count  
* **Returns:** void  
* **Description:** Notifies room/group members when user leaves.

void emitToUser(String userId, String event, Object payload)

* **Parameters:** `userId` \- Target user's unique identifier, `event` \- Event name, `payload` \- Event data  
* **Returns:** void  
* **Description:** Sends event directly to specific user's socket connection.

---

## **9\. Database Model Interfaces (Mongoose)**

### **User Model**

User create(UserData userData)

User findById(String userId)

User findOne(Query query)

User findByIdAndUpdate(String userId, UpdateData updates)

boolean findByIdAndDelete(String userId)

List\<User\> find(Query query)

List\<User\> findByIds(List\<String\> userIds)

### **Room Model**

Room create(RoomData roomData)

Room findById(String roomId)

Room findOne(Query query)

Room findByIdAndUpdate(String roomId, UpdateData updates)

boolean findByIdAndDelete(String roomId)

List\<Room\> find(Query query)

List\<Room\> findActiveRooms()

Room findByUserId(String userId)

### **Group Model**

Group create(GroupData groupData)

Group findById(String groupId)

Group findOne(Query query)

Group findByIdAndUpdate(String groupId, UpdateData updates)

boolean findByIdAndDelete(String groupId)

List\<Group\> find(Query query)

Group findByUserId(String userId)

List\<Group\> findActiveGroups()

void addVote(String userId, String restaurantId)

void removeVote(String userId)

int getVoteCount(String restaurantId)

String getWinningRestaurant()

boolean hasAllVoted()

void removeMember(String userId)

### **CredibilityLog Model**

CredibilityLog create(LogData logData)

List\<CredibilityLog\> findByUserId(String userId, int limit)

List\<CredibilityLog\> getRecentLogs(int days)

---

## **10\. External API Interfaces**

### **Google Auth Library**

TokenInfo verifyIdToken(String idToken)

* **Parameters:** `idToken` \- Google ID token from client  
* **Returns:** TokenInfo with userId (sub), email, name, picture  
* **Description:** Validates Google OAuth token against Google's servers.

### **Google Places API (HTTP/REST)**

GET https://maps.googleapis.com/maps/api/place/nearbysearch/json

Query Parameters: location, radius, type, keyword, key

Returns: { results: Restaurant\[\] }

* **Description:** Searches for restaurants near specified location.

GET https://maps.googleapis.com/maps/api/place/details/json

Query Parameters: place\_id, fields, key

Returns: { result: Restaurant }

* **Description:** Retrieves detailed restaurant information.

### **Firebase Cloud Messaging**

String send(Message message)

* **Parameters:** `message` \- FCM message object with token, notification, data  
* **Returns:** Message ID string  
* **Description:** Sends push notification to user's device.

BatchResponse sendEachForMulticast(MulticastMessage message)

* **Parameters:** `message` \- FCM multicast message with tokens\[\], notification, data  
* **Returns:** BatchResponse with success/failure counts  
* **Description:** Sends notification to multiple devices efficiently.

**Frontend \<-\> Backend Interfaces**

\#\# Base Information

\*\*Base URL:\*\* \`http://localhost:3000\` (Development)    
\*\*API Version:\*\* 1.0    
\*\*Protocol:\*\* REST \+ WebSocket (Socket.IO)

\---

\#\# Table of Contents  
1\. \[Authentication\](\#authentication)  
2\. \[User Management\](\#user-management)  
3\. \[Matching System\](\#matching-system)  
4\. \[Group System\](\#group-system)  
5\. \[Restaurant System\](\#restaurant-system)  
6\. \[WebSocket Events\](\#websocket-events)  
7\. \[Error Handling\](\#error-handling)  
8\. \[Data Models\](\#data-models)

\---

## Authentication

All authenticated endpoints require a Bearer token in the Authorization header:  
\`\`\`  
Authorization: Bearer \<JWT\_TOKEN\>  
\`\`\`

### POST /api/auth/signin

Purpose: Exchange Google ID token for JWT authentication token.

\*\*Request:\*\*  
\`\`\`json  
{  
  "idToken": "string" // Google OAuth ID token  
}  
\`\`\`

\*\*Response (200):\*\*  
\`\`\`json  
{  
  "token": "string", // JWT token for future requests  
  "user": {  
    "userId": "string",  
    "name": "string",  
    "email": "string",  
    "profilePicture": "string",  
    "credibilityScore": 100  
  }  
}  
\`\`\`

\*\*Errors:\*\*  
\- 400: Invalid request (missing idToken)  
\- 401: Invalid Google token

### POST /api/auth/signup

Purpose: Exchange Google ID token for JWT authentication token.

\*\*Request:\*\*  
\`\`\`json  
{  
  "idToken": "string" // Google OAuth ID token  
}  
\`\`\`

\*\*Response (200):\*\*  
\`\`\`json  
{  
  "token": "string", // JWT token for future requests  
  "user": {  
    "userId": "string",  
    "name": "string",  
    "email": "string",  
    "profilePicture": "string",  
    "credibilityScore": 100  
  }  
}  
\`\`\`

\*\*Errors:\*\*  
\- 400: Invalid request (missing idToken)  
\- 401: Invalid Google token

### POST /api/auth/google

Purpose: Exchange Google ID token for JWT authentication token.

\*\*Request:\*\*  
\`\`\`json  
{  
  "idToken": "string" // Google OAuth ID token  
}  
\`\`\`

\*\*Response (200):\*\*  
\`\`\`json  
{  
  "token": "string", // JWT token for future requests  
  "user": {  
    "userId": "string",  
    "name": "string",  
    "email": "string",  
    "profilePicture": "string",  
    "credibilityScore": 100  
  }  
}  
\`\`\`

\*\*Errors:\*\*  
\- 400: Invalid request (missing idToken)  
\- 401: Invalid Google token

\---

### POST /api/auth/logout

Purpose: Logout current user (sets status to offline).

\*\*Headers:\*\* \`Authorization: Bearer \<token\>\`

No Request parameters

\*\*Response (200):\*\*  
\`\`\`json  
{  
  "message": "Logged out successfully"  
}  
\`\`\`

\---

### POST /api/auth/fcm-token

Purpose: Update user's Firebase Cloud Messaging token for push notifications.

\*\*Headers:\*\* \`Authorization: Bearer \<token\>\`

\*\*Request:\*\*  
\`\`\`json  
{  
  "fcmToken": "string"  
}  
\`\`\`

\*\*Response (200):\*\*  
\`\`\`json  
{  
  "message": "FCM token updated successfully"  
}  
\`\`\`

\---

### DELETE /api/auth/account

Delete user account (cannot be in a room or group).

\*\*Headers:\*\* \`Authorization: Bearer \<token\>\`

\*\*Response (200):\*\*  
\`\`\`json  
{  
  "message": "Account deleted successfully"  
}  
\`\`\`

\*\*Errors:\*\*  
\- 400: User is in a room or group

\---

## User Management

### GET /api/user/profile/:ids

Get user profiles by IDs (comma-separated).

\*\*Parameters:\*\*  
\- \`ids\` (path): Comma-separated user IDs (e.g., "user1,user2,user3")

\*\*Response (200):\*\*  
\`\`\`json  
\[  
  {  
    "userId": "string",  
    "name": "string",  
    "bio": "string",  
    "profilePicture": "string",  
    "contactNumber": "string"  
  }  
\]  
\`\`\`

\---

### GET /api/user/settings

Get current user's settings and preferences.

\*\*Headers:\*\* \`Authorization: Bearer \<token\>\`

\*\*Response (200):\*\*  
\`\`\`json  
{  
  "Status": 200,  
  "Message": {},  
  "Body": {  
    "userId": "string",  
    "name": "string",  
    "bio": "string",  
    "preference": \["string"\], // cuisine preferences  
    "profilePicture": "string",  
    "credibilityScore": 100,  
    "contactNumber": "string",  
    "budget": 50,  
    "radiusKm": 5,  
    "status": 1, // 0: offline, 1: online, 2: in\_waiting\_room, 3: in\_group  
    "roomID": "string" | null,  
    "groupID": "string" | null  
  }  
}  
\`\`\`

\---

### POST /api/user/profile

Create or update user profile.

\*\*Headers:\*\* \`Authorization: Bearer \<token\>\`

\*\*Request:\*\*  
\`\`\`json  
{  
  "name": "string",  
  "bio": "string",  
  "profilePicture": "string",  
  "contactNumber": "string"  
}  
\`\`\`

\*\*Response (200):\*\*  
\`\`\`json  
{  
  "Status": 200,  
  "Message": { "text": "Profile updated successfully" },  
  "Body": {  
    "userId": "string",  
    "name": "string",  
    "bio": "string",  
    "profilePicture": "string",  
    "contactNumber": "string"  
  }  
}  
\`\`\`

\---

### POST /api/user/settings

Update user settings and preferences.

\*\*Headers:\*\* \`Authorization: Bearer \<token\>\`

\*\*Request:\*\*  
\`\`\`json  
{  
  "name": "string",  
  "bio": "string",  
  "preference": \["Italian", "Japanese", "Mexican"\], // cuisine preferences  
  "profilePicture": "string",  
  "contactNumber": "string",  
  "budget": 50,  
  "radiusKm": 5  
}  
\`\`\`

\*\*Response (200):\*\*  
\`\`\`json  
{  
  "Status": 200,  
  "Message": { "text": "Settings updated successfully" },  
  "Body": { /\* Updated settings object \*/ }  
}  
\`\`\`

\---

### PUT /api/user/profile

Update user profile (similar to POST but uses PUT method).

\*\*Headers:\*\* \`Authorization: Bearer \<token\>\`

\*\*Request:\*\* Same as POST /api/user/profile

\*\*Response:\*\* Same as POST /api/user/profile

\---

### DELETE /api/user/:userId

Delete a user account.

\*\*Headers:\*\* \`Authorization: Bearer \<token\>\`

\*\*Parameters:\*\*  
\- \`userId\` (path): User ID to delete (must match authenticated user)

\*\*Response (200):\*\*  
\`\`\`json  
{  
  "Status": 200,  
  "Message": { "text": "User deleted successfully" },  
  "Body": { "deleted": true }  
}  
\`\`\`

\*\*Errors:\*\*  
\- 403: Can only delete own account  
\- 400: Cannot delete while in room/group

\---

## Matching System

### POST /api/matching/join

Join the matching pool to find a group.

\*\*Headers:\*\* \`Authorization: Bearer \<token\>\`

\*\*Request:\*\*  
\`\`\`json  
{  
  "cuisine": \["Italian", "Japanese"\], // preferred cuisines  
  "budget": 50,  
  "radiusKm": 5  
}  
\`\`\`

\*\*Response (200):\*\*  
\`\`\`json  
{  
  "Status": 200,  
  "Message": { "text": "Successfully joined matching" },  
  "Body": {  
    "roomId": "string",  
    "room": {  
      "roomId": "string",  
      "completionTime": "2025-01-19T20:30:00.000Z",  
      "maxMembers": 4,  
      "members": \["userId1", "userId2"\],  
      "status": "waiting", // "waiting" | "matched" | "expired"  
      "cuisine": "Italian",  
      "averageBudget": 45,  
      "averageRadius": 5  
    }  
  }  
}  
\`\`\`

\*\*Notes:\*\*  
\- User will be matched to existing room or new room created  
\- When room fills (4 members), group is automatically created  
\- Real-time updates sent via WebSocket

\---

### PUT /api/matching/leave/:roomId

Leave a waiting room.

\*\*Headers:\*\* \`Authorization: Bearer \<token\>\`

\*\*Parameters:\*\*  
\- \`roomId\` (path): Room ID to leave

\*\*Response (200):\*\*  
\`\`\`json  
{  
  "Status": 200,  
  "Message": { "text": "Successfully left room" },  
  "Body": { "roomId": "string" }  
}  
\`\`\`

\---

### GET /api/matching/status/:roomId

Get current status of a waiting room.

\*\*Headers:\*\* \`Authorization: Bearer \<token\>\`

\*\*Parameters:\*\*  
\- \`roomId\` (path): Room ID

\*\*Response (200):\*\*  
\`\`\`json  
{  
  "Status": 200,  
  "Message": {},  
  "Body": {  
    "roomID": "string",  
    "completionTime": 1705699800000, // Unix timestamp in milliseconds  
    "members": \["userId1", "userId2"\],  
    "groupReady": false,  
    "status": "waiting" // "waiting" | "matched" | "expired"  
  }  
}  
\`\`\`

\---

### GET /api/matching/users/:roomId

Get list of users in a room.

\*\*Headers:\*\* \`Authorization: Bearer \<token\>\`

\*\*Parameters:\*\*  
\- \`roomId\` (path): Room ID

\*\*Response (200):\*\*  
\`\`\`json  
{  
  "Status": 200,  
  "Message": {},  
  "Body": {  
    "roomID": "string",  
    "Users": \["userId1", "userId2", "userId3"\]  
  }  
}  
\`\`\`

\---

## Group System

### GET /api/group/status

Get current user's group status.

\*\*Headers:\*\* \`Authorization: Bearer \<token\>\`

\*\*Response (200):\*\*  
\`\`\`json  
{  
  "Status": 200,  
  "Message": {},  
  "Body": {  
    "roomId": "string",  
    "completionTime": 1705699800000,  
    "numMembers": 4,  
    "users": \["userId1", "userId2", "userId3", "userId4"\],  
    "restaurantSelected": false,  
    "restaurant": {  
      "name": "string",  
      "location": "string",  
      "restaurantId": "string",  
      "priceLevel": 2,  
      "rating": 4.5  
    },  
    "status": "voting" // "voting" | "matched" | "completed" | "disbanded"  
  }  
}  
\`\`\`

\*\*Errors:\*\*  
\- 404: User not in a group

\---

### POST /api/group/vote/:groupId

Vote for a restaurant in your group.

\*\*Headers:\*\* \`Authorization: Bearer \<token\>\`

\*\*Parameters:\*\*  
\- \`groupId\` (path): Group ID

\*\*Request:\*\*  
\`\`\`json  
{  
  "restaurantID": "string",  
  "restaurant": {  
    "name": "Sushi Paradise",  
    "location": "123 Main St, Vancouver, BC",  
    "restaurantId": "rest\_001",  
    "priceLevel": 2,  
    "rating": 4.5,  
    "phoneNumber": "+1-604-555-0001",  
    "url": "https://example.com/sushi-paradise"  
  }  
}  
\`\`\`

\*\*Response (200):\*\*  
\`\`\`json  
{  
  "Status": 200,  
  "Message": { "text": "Voting successful" },  
  "Body": {  
    "Current\_votes": {  
      "rest\_001": 3,  
      "rest\_002": 1  
    }  
  }  
}  
\`\`\`

\*\*Notes:\*\*  
\- Real-time vote updates sent via WebSocket to all group members  
\- When all members vote, restaurant with most votes is automatically selected

\---

### POST /api/group/leave/:groupId

Leave a group.

\*\*Headers:\*\* \`Authorization: Bearer \<token\>\`

\*\*Parameters:\*\*  
\- \`groupId\` (path): Group ID to leave

\*\*Response (200):\*\*  
\`\`\`json  
{  
  "Status": 200,  
  "Message": { "text": "Successfully left group" },  
  "Body": { "groupId": "string" }  
}  
\`\`\`

\*\*Notes:\*\*  
\- Affects credibility score  
\- If all members leave, group is deleted

\---

## Restaurant System

### GET /api/restaurant/search

Search for restaurants near a location.

\*\*Query Parameters:\*\*  
\- \`latitude\` (required): Latitude coordinate (e.g., 49.2827)  
\- \`longitude\` (required): Longitude coordinate (e.g., \-123.1207)  
\- \`radius\`: Search radius in meters (default: 5000\)  
\- \`cuisineTypes\`: Comma-separated cuisine types (e.g., "Italian,Japanese")  
\- \`priceLevel\`: Price level 1-4 (1=cheap, 4=expensive)

\*\*Example:\*\*  
\`\`\`  
GET /api/restaurant/search?latitude=49.2827\&longitude=-123.1207\&radius=5000\&cuisineTypes=Italian,Japanese\&priceLevel=2  
\`\`\`

\*\*Response (200):\*\*  
\`\`\`json  
{  
  "Status": 200,  
  "Message": {},  
  "Body": \[  
    {  
      "name": "Sushi Paradise",  
      "location": "123 Main St, Vancouver, BC",  
      "restaurantId": "rest\_001",  
      "address": "123 Main St, Vancouver, BC",  
      "priceLevel": 2,  
      "rating": 4.5,  
      "photos": \["url1", "url2"\],  
      "phoneNumber": "+1-604-555-0001",  
      "website": "https://example.com",  
      "url": "https://maps.google.com/..."  
    }  
  \]  
}  
\`\`\`

\---

### GET /api/restaurant/:restaurantId

Get detailed information about a specific restaurant.

\*\*Parameters:\*\*  
\- \`restaurantId\` (path): Restaurant ID

\*\*Response (200):\*\*  
\`\`\`json  
{  
  "Status": 200,  
  "Message": {},  
  "Body": {  
    "name": "string",  
    "location": "string",  
    "restaurantId": "string",  
    "address": "string",  
    "priceLevel": 2,  
    "rating": 4.5,  
    "photos": \["url1"\],  
    "phoneNumber": "string",  
    "website": "string",  
    "url": "string"  
  }  
}  
\`\`\`

\---

### POST /api/restaurant/recommendations/:groupId

Get restaurant recommendations for a group.

\*\*Headers:\*\* \`Authorization: Bearer \<token\>\`

\*\*Parameters:\*\*  
\- \`groupId\` (path): Group ID

\*\*Request:\*\*  
\`\`\`json  
{  
  "userPreferences": \[  
    {  
      "cuisineTypes": \["Italian", "Japanese"\],  
      "budget": 50,  
      "location": {  
        "coordinates": \[-123.1207, 49.2827\]  
      },  
      "radiusKm": 5  
    }  
  \]  
}  
\`\`\`

\*\*Response (200):\*\*  
\`\`\`json  
{  
  "Status": 200,  
  "Message": {},  
  "Body": \[  
    { /\* Restaurant objects \*/ }  
  \]  
}  
\`\`\`

\---

## WebSocket Events

Connect to Socket.IO at: \`http://localhost:3000\`

### Authentication

Send JWT token during connection:  
\`\`\`javascript  
const socket \= io('http://localhost:3000', {  
  auth: {  
    token: 'YOUR\_JWT\_TOKEN'  
  }  
});  
\`\`\`

\---

### Client → Server Events

#### join\_room

Join a room to receive updates.  
\`\`\`javascript  
socket.emit('join\_room', { userId: 'user123' });  
\`\`\`

#### leave\_room

Leave a room.  
\`\`\`javascript  
socket.emit('leave\_room', { userId: 'user123' });  
\`\`\`

#### subscribe\_to\_room

Subscribe to room updates.  
\`\`\`javascript  
socket.emit('subscribe\_to\_room', 'room\_123');  
\`\`\`

#### subscribe\_to\_group

Subscribe to group updates.  
\`\`\`javascript  
socket.emit('subscribe\_to\_group', 'group\_123');  
\`\`\`

\---

### Server → Client Events

#### room\_update

Sent when room status changes.  
\`\`\`javascript  
socket.on('room\_update', (data) \=\> {  
  // data \= {  
  //   roomId: 'string',  
  //   members: \['userId1', 'userId2'\],  
  //   expiresAt: '2025-01-19T20:30:00.000Z',  
  //   status: 'waiting' | 'matched' | 'expired'  
  // }  
});  
\`\`\`

#### group\_ready

Sent when a group is formed (room is full).  
\`\`\`javascript  
socket.on('group\_ready', (data) \=\> {  
  // data \= {  
  //   groupId: 'string',  
  //   members: \['userId1', 'userId2', 'userId3', 'userId4'\],  
  //   ready: true  
  // }  
});  
\`\`\`

#### room\_expired

Sent when a room expires.  
\`\`\`javascript  
socket.on('room\_expired', (data) \=\> {  
  // data \= {  
  //   roomId: 'string',  
  //   reason: 'Not enough members'  
  // }  
});  
\`\`\`

#### vote\_update

Sent when someone votes in a group.  
\`\`\`javascript  
socket.on('vote\_update', (data) \=\> {  
  // data \= {  
  //   restaurantId: 'string',  
  //   votes: { 'rest\_001': 3, 'rest\_002': 1 },  
  //   totalVotes: 4,  
  //   membersVoted: 3,  
  //   totalMembers: 4  
  // }  
});  
\`\`\`

#### restaurant\_selected

Sent when a restaurant is selected.  
\`\`\`javascript  
socket.on('restaurant\_selected', (data) \=\> {  
  // data \= {  
  //   restaurantId: 'string',  
  //   restaurantName: 'Sushi Paradise',  
  //   votes: { 'rest\_001': 3, 'rest\_002': 1 }  
  // }  
});  
\`\`\`

#### member\_joined

Sent when a member joins a room.  
\`\`\`javascript  
socket.on('member\_joined', (data) \=\> {  
  // data \= {  
  //   userId: 'string',  
  //   userName: 'string',  
  //   currentMembers: 2,  
  //   maxMembers: 4  
  // }  
});  
\`\`\`

#### member\_left

Sent when a member leaves.  
\`\`\`javascript  
socket.on('member\_left', (data) \=\> {  
  // data \= {  
  //   userId: 'string',  
  //   userName: 'string',  
  //   remainingMembers: 2  
  // }  
});  
\`\`\`

\---

## Error Handling

### Standard Error Response

\`\`\`json  
{  
  "error": "Error Type",  
  "message": "Detailed error message",  
  "statusCode": 400  
}  
\`\`\`

### Common HTTP Status Codes

\- \*\*200\*\*: Success  
\- \*\*400\*\*: Bad Request (invalid input)  
\- \*\*401\*\*: Unauthorized (missing/invalid token)  
\- \*\*403\*\*: Forbidden (insufficient permissions)  
\- \*\*404\*\*: Not Found  
\- \*\*409\*\*: Conflict (duplicate resource)  
\- \*\*500\*\*: Internal Server Error

\---

**Databases**

- UserDB  
- GroupDB  
  - Table : Rooms  
  - Table: Groups

#### **4.2 Frameworks**

*(List all frameworks \+ rationale, e.g., Express.js, Retrofit, Jetpack Compose, Socket.io, Prisma, AWS EC2)*

#### *Frontend:*

| Framework | Rational |
| :---- | :---- |
| Kotlin | For general frontend code. Used often in industry for similar styled apps.  |
| Google Authentication | Outside of the backend explanation, we note that the familiar google dialog creates a layer of trust.  |
| ThreeTenABP | Helps manage time information; it is a port of JSR-310. The value is that it encapsulates date items.  |
| Jetpack Compose | It allows for very strong encapsulation within the UI layer, thus speeding up development, furthermore it is by android, for android. We also use DataStore from compose to help with how we store data on the front end.  We use material icons from compose as well for ease of use  |
| Square Retrofit2 | We use retrofit as an encapsulation layer from http to kotlin, this reduces error on the parsing step.  |
| Square OkHttp3  | We encapsulate our http client through this api. It saves a lot of networking code.  |
| Socket IO | Handles our asynchronous requests from the backend. We chose socket as it was a strong fit for our project.  |

#### *Backend:*	

| Framework | Rational |
| :---- | :---- |
| Typescript | For general backend code. It’s a solid language for this type of programming, used a lot in industry.  |
| MongoDB | Database. Initially we were planning to use PostgresSQL, however we decided to swap because MongoDB served our tasks better, specifically the document database better stored the way we wanted to hold user information and group information. |
| Google Authentication | For user authentication, the main rationale was the security we can derive from encapsulating. In security you are as strong as your weakest link, ergo google a strong link.  |
| Firebase Admin | We used firebase admin to handle our push notifications. We chose to use it over other notification tools as it seemed this one was the most well maintained. |
| Socket io | We choose socket to handle our asynchronous requests to the client as it is a very strong tool. We choose it over its competitors as it better fits our use case.  |
| Axios | Axios helps handle http requests; we use it as it is a functional encapsulation layer allowing our code to be more human readable.  |
| Zod | Zod is used to parse schemas, similar to axios it helps encapsulate the layer between typescript object and http request.  |
| Express | We use express to allow for easy HTTP requests, the rationale for using it is that it is the main service for this type of task.  |

#### **4.3 Dependency Diagram**

*(Insert diagram image here — lifelines for each component)*

*User Service*

*Matching Service*

*Group Service*

*Restaurant Service*

*Credibility Service* 

#### **4.4 Databases**

We are using MongoDB and we have a couple different but helpful collections:

1. Users. collection: user

We store a user with the information as cited in the structure of a user section. The rational for storing user information is quite simple, mainly we want to have users and a lot of our tasks involve selected a singular user as such users need to be stored separately

2. Rooms 

Following the structure in the structure of a room section. We store the rooms as a separate collection due to their access pattern. We often have a need for a room so being able to access them as a separate query is very helpful. A note on the implementation rational, we store the user ids of the members so we can query by user id’s at a later point, this helps keep the frontend and backend encapsulated in a very organized fashion. 

3. Groups

Defined in the structure of a group section we store a collection of these group objects. This again allows for quick access to the specific group. Something interesting about the implementation is that we tried to keep data very closely related to the group to minimize queries. An example of this are votes. We have the related votes for a room come with the query to reduce server load. 

4. Restaurants 

Defined in the structure of a restaurant section we store restaurants as their own special object as we need to load lots of separate restaurants, thus by having it as their own object we can keep things organized. Furthermore we work with the google places api so we can have a very quick access pattern. 

5. Credibility Logging, credibility\_logs

We have a logging system for credibility (collection written this paragraph),  which contains the relevant logging information for credibility changes. We utilize this collection to allow documentation of changes in credibility. This includes notes to help with repeat offenders. Alongside helping speed up our process, part of the rationale for this item was during our presentation we were asked about how we can manage repeat offenders. By utilizing an advanced logging system we can keep track of the offenders thus allowing us to moderate in a safer fashion. 

{

  \_id: ObjectId,

  userId: string,           

  action: 'no\_show'|'late\_cancel'|'left\_group\_early'|'completed\_meetup'|'positive\_review'|'negative\_review'

  scoreChange: number,    

  previousScore: number,

  newScore: number,

  groupId?: string,

  roomId?: string,

  notes?: string,

  createdAt: Date,

}

#### **4.5 External Modules**

*(Google Auth, Places API, Firebase, etc.)*

| Name  | Rational  |
| :---- | :---- |
| Google Authentication | Handles all authentication, encapsulates security  |
| Google Places | Places lets us parse geospatial data to get relevant restaurants for our users. It is a critical component as there are not a lot of other services that do this. |
| Firebase Admin | Handles push notifications for our users. The rationale for this service is that it helps encapsulate a very complicated android behavior.  |

#### **4.6 Sequence Diagrams**

*(One per 5 major use cases. Insert diagrams as images.)*

***4.6.1 Set Preferences*** 

*![][image3]*

***4.6.2 \- Request Matches*** 

***![][image4]***

***4.6.3 \- Receive Notification about Match***

***![][image5]***

***4.6.4 \- Vote on Restaurant*** 

***![][image6]***

***4.6.5 \- View Restaurant Choice***

#### **![][image7]**

[*https://app.diagrams.net/\#G1q9MxsTL9xvzYNCV5fXcTlO\_P9JBD3SdL\#%7B%22pageId%22%3A%2213e1069c-82ec-6db2-03f1-153e76fe0fe0%22%7D*](https://app.diagrams.net/#G1q9MxsTL9xvzYNCV5fXcTlO_P9JBD3SdL#%7B%22pageId%22%3A%2213e1069c-82ec-6db2-03f1-153e76fe0fe0%22%7D) 

#### **4.7 Non-Functional Requirements Implementation**

| Non-Functional Requirement | How it is Implemented |
| ----- | ----- |
| Performance: System must return a restaurant match suggestion in 5 seconds for 90% of requests 95% of user interactions must respond within 2 seconds | We were able to implement both of the performance requirements due to some main reasons. Firstly we use light weight REST api calls, so we are never doing insanely high computational tasks. Secondly for longer items like waiting rooms, which are by design going to be long, we have it as an asynchronous call; thus not making it blocking.   |
| Scalability The matchmaking algorithm must support up to 100 concurrent users in the waiting room. The system must be able to support 1000 users.  | We have purposely designed the application to be as scalable as possible. Specifically the vast majority of the code runs in O(N) where N is the number of user’s time, furthermore our database is not a limitation as we can continually add to it. Furthermore the server is not going to pose a limitation on the quantity of users; by virtue of the scaling models of modern cloud hosting. Currently, it is set up so the room has a maximum of 10 people, but this could be easily expanded to 100\. This can be done by simply changing the constant in the backend file. |
| Reliability System must be up 90% of the time | We host our system on a AWS EC2 instance, alongside depending on Firebase administration, while depending on multiple pieces of infrastructure does promote some risk, for the most part these cloud hosting services do not go offline.  Reliability is unfortunately or maybe fortunately for the most part out of our hands and within the control of the cloud providers (who promise \>99% uptime\*). \*Minus AWS US East 1 |

---

[image1]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAgQAAAJuCAYAAADPbRqqAACAAElEQVR4Xuy9a3RdxX2/z1r9tf2vrl7yquVFL0mTNrhNm7plJaiLlQQlECyuVnDAaowxikuIMcQIcxO3gGNiozjBEeYSczMIg0EGDDLhYpNwsU0AEzDY3CJooAhiWoW4oKQumf95Rhp5NGfvOeeMpI91zH6yvsHa3zP72bNlnzNn9lz2+s1vfmNcDAwM2PCPVZMjXnnllbJjlcql5pQuQulTugilT+kilD6li1D6lC5C6VO6CKVP6SKUPqWLUPqULsL37VVtwVguPGm15VJzSheh9CldhNKndBFKn9JFKH1KF6H0KV2E0qd0EUqf0kUofUoX4fv2Mh4c+N///V//0DCxHPzHf/xHeMgSK5eaU7pA6VO6IPStWbPGnHPOOWbJkiW23KZNm+zPBPT29pqzzjrLBvzP//zPiDw+cvz81ltv2WMuf99999mfL774YvvzunXr7M9XXnmlLbNy5Ur7s7uGpUuX2vM98sgjZdfAnynDNYbXAPx57ty5ZdewZcsWe84FCxaMuIarr77a/nzjjTfac/rXANwHv97uGpxz27ZtmdfA6//zP/9z+Gd3DcA1kA+vgfvANd5+++1l1+DOwe8tvAbug3+N4PLcB/93467B/S7uvfdeW2//GoD7wH3MugbO9+KLL5ZdA392v5vwGsBdY3gN7j4sXry47Br4ufg7qf876VPre4kjVi41p3SB0qd0ge+r2wbBjh07zH/913+NOB4rk5oDdd2yiJVJzUHo442B2Lx5sy3nfnZvGLzpkHvqqafsz7zx+Xl85PjZvfG5vHvzDc/pzud+9vP++fxr4M/uGsNrcOfAF15D3jndn/Pq7XKu3u4aXJ4336xroIx78w3PWWu9/bxrEPh57oN/jX4Z1yDIO2es3tzH8PUE56NBEF6Df77wGiBWb3C+MJ93jcXfyfH7O+lT63uJI1YuNad0gdKndMEe0SCgm+OnP/3piOOxMqk5UNcti1iZ1ByEPt5UeDOLlUvNhS5HrExqDpQ+pQuUPqULlD6lC5S+VBeNBd4HQmLlINWXklO6QOlTusD3FQ0CE8+Bum5ZxMqk5iD0/du//VvRIEjIKV2g9CldoPQpXaD0pbpoEPz7v/97mIqWg1RfSk7pAqVP6QLfVzQITDwH6rplESuTmoPQxxtB0SCoPad0gdKndIHSp3SB0pfqKhoE5Sh9Shf4vrqdZfDf//3fw89FqymTmiPUdQuPVSqTmiOUPqWLUPqULkLpU7oIpU/pIpQ+pYtQ+pQuQulTugjfV7c9BFnEyqTmQOlTuiD00TtQ9BDUnlO6QOlTukDpU7pA6Ut19ff32/eAkFg5SPWl5JQuUPqULvB9RYPAxHOg9CldEPr2pDEEjJZ+6KGHzLXXXmsuvPBCM2fOHNPS0mKOOOIIc/jhh9s48sgj7TFyvGb58uVm/fr15he/+EV4uqhPXTelT+kCpU/pAqUv1VU8MihH6VO6wPcVDQITz4HSp3RB6KvnBsGzzz5r52h/+ctfNk1NTWbGjBl2Tvvq1avNE088YV5//XXz3nvveWcYhGPkeA2vXbRokZk1a5Y9B+finJw79PmMd91ClD6lC5Q+pQuUvlRX0SAoR+lTusD31W2D4LXXXjNbt24dcTxWJjUH6rplESuTmgOlbzxcTz75pGlvbzeHHHKIOfXUU83atWvNu+++G740Gc7FOTk3DhZtefrpp8OXjUvd8nKg9CldoPQpXaD0KV2g9CldoPQpXeD76rZBwECIYpbBIKk5UPrG0nXbbbfZrv+TTz7ZPPbYY2F63MB1yimn2EcNXINjLOsGsRwofUoXKH1KFyh9ShcofUoXKH1KF/i+up1l8LOf/cyuqlVtmdQcoa5beKxSmdQcEfqmT59uZ3DEyqXmQlc1ZcLcrbfeag477LDhZWx3F7i5Bq6FaxqLulWbI5Q+pYtQ+pQuQulLdf385z83ra2tmbm8ckSqLyWndBFKn9JF+L66bRC8+uqrw891qymTmiPUdQuPVSqTmiNC30RtEDz//PPmqKOOMhdddFH42bzb4ZroreAa864/PD6aHDGaexker5RTugilT+kilL5UV9EgKA+lT+kifF/dPjLIIlYmNQdKn9IFoY+1HSbaoMLrrrvOdtHzgTtR4dq4xhUrVow4XqluKTlIvZcpOaULlD6lC5S+VBeDCt1+C2Eurxyk+lJyShcofUoX+L6iQWDiOVD6lC5Q+lJcDBg8++yzw8MTFq733HPPHf45VrfUHKTcy9Sc0gVKn9IFSp/SBUqf0gVKn9IFvq9oEJh4DpQ+pQtC37x58yZMDwGD9y677LLw8IRn2bJl9tohr26QmoNa7yWk5pQuUPqULlD6Ul30EPA+EBIrB6m+lJzSBUqf0gW+r24bBMX2x7tIzUHomyjrEPAtmw/WeoVrpw5ZdXOk5qCWe+lIzSldoPQpXaD0pbqKdQjKUfqULvB9ddsgYCBEMe1wkNQchD72Qd/dDYKVK1eaM88803tFfUIdbrzxxtz7EdbbJ5aDau+lT2pO6QKlT+kCpS/VRYOA94GQWDlI9aXklC5Q+pQu8H1Fg8DEc6CuWxaxMqk5UPqqcb3zzjt2Kt+ewqGHHmrefvvt8LAldq9iOajmXoak5pQuUPqULlD6lC5Q+pQuUPqULvB9e7388svGxYsvvmjDP1ZNjmAKYHisUrnUHC7WINi4cWPVZVJzhLpu4bFKZVJzROg77bTT7LeDWLnUXOjKKsOeAvfff7/317W+oS7UKaxzWO8wYjmimnsZRmpO6SKUPqWLUPpSXbyv8j6QlcsrR6T6UnJKF6H0KV2E76vbHoJiDMEuUnMQ+nbnGIJf//rXdsOhPQ16PKhbSOxexXJQ6V5mkZpTukDpU7pA6Ut1FWMIylH6lC7wfXXbIMgiViY1B0qf0gWhb926dbutQXDTTTeZa665JkzH6d9gOqY1mIb9J5vGGV1m687SsVc6Tduy3vCVFeg1nfM6zXCp0jma5q3zX5DM1VdfbesWErtXsRxUupdZpOaULlD6lC5Q+lJdNAh4HwiJlYNUX0pO6QKlT+kC31c0CEw8B0qf0gVKXyUX30rYtKoWtp7baNo2Dv2wfo5pXFL6SN85YPp3DB4a6O8zff0Dpf/2m9L/m37758FjI+k1HTM6djUIXuowk2b0lP4wVGZ7VpnqoE61fuOK5aDSvcwiNad0gdKndIHSp3SB0qd0gdKndIHvKxoEJp4DpU/pgtDHt9nd1UPAALxaGVjbaibt12Y61242fUONAD7MWxb32lzT3C7Tvbj0mg+1mJ7S/1r2bjBty7pNx9RJuxoSlrwGQanM/zfZzCmV6ZzRYOY87BWpgay6xe5VLAeV7mUWqTmlC5Q+pQuUvlQXPQS8D4TEykGqLyWndIHSp3SB76vbBkGx/fEuUnMQ+nbnGILPfOYzYao6dvSZzas6TOv+HzENS3uHGwQ9M1pMN48QTJ9ZfsBQg+CwLmO/569pMS1r/JNEGgTTu4ePNc7d4F5RE01NTeGh6L2K5aDSvcwiNad0gdKndIHSl+oqxhCUo/QpXeD76nZzo2K3w9HniNDHvPndsbkROwZ+/OMf9/6aVkO/6Zo+x/TYD/0SO7tNy4HLTb9rEMxqNl2216D0Yf+JoQaB/ZA32Q2C/Vp2nevxdtNw1mZjGwSuEbFtgWmyx2pnypQpmfXOu1exHBG7l3nlUnNKF6H0KV2E0pfqYnMj3geycnnliFRfSk7pIpQ+pYvwfUWDoEKOUNctPFapTGqOUPryXDRA2GXx85//fPg5WpltpW/t+zSaltmtpmnfyabt4YHhHgJyTfs3m9ZpjWbvD7VWaBCUmhdrWs2kfZtM6+wW07hvi+mywxlKZT402TTPbzetBzSb5S+NLFMtzDQI6x27V7EckXcvY+VSc0oXofQpXYTSp3QRSp/SRSh9Shfh+4pHBiaeA3XdsoiVSc1B6FuzZo38kQEzCzo7O80555xjnnjiiTCdTO+qDtPDh/rODaZtqvc4oCa8RkQi1Im6hcTuVSwHefcyVi41p3SB0qd0gdKX6uKRAe8DIbFykOpLySldoPQpXeD76rZBkEWsTGoOlD6lC0KfegzBm2++aY444gjT19dnHn74YXP++eeHL0mnf4PpnD/HzDmx3XTXNnnBY7NZflXaYwLHeeedZ+sWErtXsRxk3UuIlUvNKV2g9CldoPSluooxBOUofUoX+L6iQWDiOVD6lC4IfUuXLpU2COgZWLFixXCZrNH49c4hhxwSHrLE7lUsB1n3EmLlUnNKFyh9ShcofakuGgS8D4TEykGqLyWndIHSp3SB7ysaBCaeA6VP6QKlL3SxnObMmTNtA8SVYTDTd77znRGvq2c6OjrM9ddfHx62xO5VLAfhvXTEyqXmlC5Q+pQuUPqULlD6lC5Q+pQu8H112yAoli7eRWoOQt+mTZtkPQQLFy40d911V1mZ5uZm8/zzz3uvrE+ow9SpU3PvR1hvn1gOwnvpiJVLzSldoPQpXaD0pbroIeB9ICRWDlJ9KTmlC5Q+pQt8XzHLoEKOUNctPFapTGqOCH2M9ldMO2QDFTb9ySpDz8GesOMhdaBRkHc/wnpXmyPC31s15VJzSheh9CldhNKX6mLaYWtra2YurxyR6kvJKV2E0qd0Eb6vaBBUyBHquoXHKpVJzRGh76yzzpI0CObPn2/XS88rc88995ijjjoq/IytG7h26pBVt2ruVSxHhL+3asql5pQuQulTugilL9VFg4D3gaxcXjki1ZeSU7oIpU/pInxf3T4yoBI//elPRxyPlUnNgbpuWcTKpOZA6XOu9evXm/b29uHjeWV4nECXe9ZOgRMVrpVHHlw75NUNUnOwO35vIbEyqTlQ+pQuUPqULlD6lC5Q+pQu8H112yAoxhDsIjUHoa+3t3fcxxB8/etfN0899dTw8ViZH//4x7br/Sc/+UmYmnBwjVzro48+OnwsVrfUHIS/N0esXGpO6QKlT+kCpS/VxRgC3gdCYuUg1ZeSU7pA6VO6wPfVbYMgi1iZ1BwofUoXhL7xXofgjjvuMIsWLRpxPFaG3FtvvWVaWlrMxRdfHKYnDBdddJG9Rh63+FSqW0oOwt+bI1YuNad0gdKndIHSl+oq1iEoR+lTusD3FQ0CE8+B0qd0QejjjWC8GgQvvPCC+cpXvmLHf/jEyvi5rq4u+w2cfQ8mClwL13TZZZeFKUu1dQuJ5SD8vTli5VJzShcofUoXKH2prqJBUI7Sp3RB0SAIiOVA6VO6QOn77ne/ay6//PLwcLRMVu6SSy6xH8LXXXfdiONKWFuAa+BaQHkfQelTukDpU7pA6VO6QOlTukDpU7rA99XtLINXX33VPPvss1WXSc0R6rqFxyqVSc0RoY8u7/GYZcDeE1/84hfNG2+8UZbLK1Mpd8UVV9gP5blz55of/ehH3l/x8QEHLpxXXnnliGsJ72M115+aI5Q+pYtQ+pQuQulLdb399tv2PSArl1eOSPWl5JQuQulTugjfV7cNgmLa4ehzROgbr3UILr30Uhvh8ViZanMMULzwwgvtssdc/7Jly+zfjdHCOTgX5+TcOJ588smy6yDC+xheY3h8NDlC6VO6CKVP6SKUvlRXsQ5BeSh9Shfh++r2kQGVKKYdDpKag9A3HoMK3RLFL7/88ojjjqwyjlpzzDxhyh8bCrGHQFNTk9086aSTTjILFiywz/rp7l+5cqUN/swxcrzm8MMPN1OmTLHTHTkH53KzWbJ8jvA+OmJlUnOg9CldoPQpXaD0pbqKMQTlKH1KF/i+um0QFNsf7yI1BwqfW6JY4fJxvvfff9/+mWmB9957r7nlllvsAMWbb77ZrF692h4jx2toSeedM+bbXXULiZVLzSldoPQpXaD0KV2g9CldoPQpXeD76rZBkEWsTGoOlD6lC8bb98QTT9jn7jDerhClT+kCpU/pAqVP6QKlT+kCpU/pAqVP6QLfVzQITDwHSp/SBaFvrB8ZnHnmmXZxIQhdjrCMT2oOlD6lC5Q+pQuUPqULlL5UV/HIoBylT+kC31c0CEw8B0qf0gWhbywbBOESxaHLUe35QmI5UPqULlD6lC5Q+pQuUPpSXUWDoBylT+kC31e3DYJi6eJdpOYg9LEq4Fg1CMIlikOXo9rzhcRyoPQpXaD0KV2g9CldoPSlumgQ8D4QEisHqb6UnNIFSp/SBb5vL0Z+u2A0OOEfqyZHsCZAeKxSudQcLqaEsYVutWVSc4S6buGxSmVSc8R4+Vgj4IwzzhiRGy9XeNyF0qd0EUqf0kUofUoXofQpXYTSp3QRSp/SRfi+uu0hKKYd7iI1B6Fv3rx5o+4heOeddzKXKA5djkrnS8mB87FRC+MYWGaYBYVYXZCZD9/+9rfNkiVL7DFyvIbllfPOGfPtrrqFxMql5pQuUPqULlD6Ul30EPA+EBIrB6m+lJzSBUqf0gW+r2gQmHgO1HXLIlYmNQehbyzGEFx77bWZSxSHLkel89WS4xFFZ2enOfbYY81BBx1k1xU4+eSTTUdHh7nhhhtMT0+P/eCnd4ngzxwjx2t4LSsRsoYB5+Bc7rFHls+hqJuP0qd0gdKndIHSl+oqxhCUo/QpXeD76rZBUIwh2EVqDkLfli1bRtUgYH2II4880mzfvj1MlbkcsfNVk1u7dq19A+NDnFkN/BzuOpgC5+BcnJNzz549266nkMV41S0PpU/pAqVP6QKlL9VFg4D3gZBYOUj1peSULlD6lC7wfXXbIMgiViY1B0qf0gVj7WN54hUrVoSHLWPp4k3r3HPPtUsK882eHqPxBgcunOeff769BsdY1g1iOVD6lC5Q+pQuUPqULlD6lC5Q+pQu8H1Fg8DEc6D0KV0Q+i6++OLkHgIGrsyYMcO8++67YcoSuhx554Mwx4fwKaecYqZNm2YefPDBXS8Ug5tr4Fq4prGom08sB0qf0gVKn9IFSl+qi7/PvA+ExMpBqi8lp3SB0qd0ge+r282NwmOVyqTmCKVP6SJC32g2N7rooovsUsBZOSJ0VTpfmONNiscRmzZt8v467164Fq7pnHPOKbv28PrDSM0Ro72XYcRySheh9CldhNKX6io2NyoPpU/pInxf3TYIiu2PR58jQh/r+qc0CBigN2fOnMxcnit2Pj/HFFM2KGIjookKAym5Rga6htcfq1tKjki9lyk5pYtQ+pQuQulLddEg4H0gK5dXjkj1peSULkLpU7oI31e3jwyoRDHLYJDUHIyVzy1RnJVzpLjYiIhv4P7z+okK18i1dnd3Dx+L1S01Byn3MjWndIHSp3SB0qd0gdKndIHSp3SB7ysaBCaeA3XdsoiVSc1B6Lv66qtrHkPgL1Ec5nxClyOvzPLlyzOnP010TjjhBHvtkFc3SM1BrfcSUnNKFyh9ShcofakuGra8D4TEykGqLyWndIHSp3SB76vbBkGx/fEuUnMQ+lLWIfCXKA5zPqHLkVXmpptusuetV7h26pBVN0dqDmq5l47UnNIFSp/SBUpfqqtYh6AcpU/pAt9Xtw2CLGJlUnOg9CldEPpWrlxZU4PgjjvuMIsWLcrMhYQuR1jmySefNM3Nzd4r6hPqwKDDvPsR1tsnloNq76VPak7pAqVP6QKlL9VFg4D3gZBYOUj1peSULlD6lC7wfUWDwMRzoPQpXTAa33vvvVe2RHGsXLUuVhfMWtio3qAOrHqYdz/CevvEclDtvfRJzSldoPQpXaD0KV2g9CldoPQpXeD76naWQXisUpnUHKH0KV1E6GPaYLWzDK655hq7tG9WLiyT5coqs3Tp0uHn73sC1IXFmsI6h/UOI5YjqrmXYaTmlC5C6VO6CKUv1cUsA94HsnJ55YhUX0pO6SKUPqWL8H1120NQLF28i9QchL5qxxAwhmPq1Kll3+Rj5UKXwy/DEsF7Gk1NTeEhS+xexXJQzb0MSc0pXaD0KV2g9KW6ijEE5Sh9Shf4vrptENCqKWYZDJKag9DHN/RqGgR86+3q6gpT0XKhy+HKMPd58eLFYbruYYwFdQuJ3atYDirdyyxSc0oXKH1KFyh9qS4aBLwPhMTKQaovJad0gdKndIHvKxoEJp4Ddd2yiJVJzUGK77nnnrM7AfKakFi5Sq7TTjvNLjaVQv/GDtO8X4Np2LfRtKwYnH3Su6zNdL4y8nWVWWfapnSa3vDwKKBO1C0kdq9iOah0L7NIzSldoPQpXaD0KV2g9CldoPQpXeD7igaBiedAXbcsYmVScxD6GBVfqYeAJYqZXZBFrFzocrgyDMBLY6tpP6DNbNg5+NO6ExtNxyulP+zoNwNDxwa295n+HQNmoH+An0x/P3/uM332Z58e0/KJjlKDIPaa2smqW+xexXJQ6V5mkZpTukDpU7pA6Ut10UOQtTx4rByk+lJyShcofUoX+L66bRAUYwh2kZqD0FdpDMETTzxhlyjOykFeOQhdDlfm4IMPDlNVMmB6Zk0yDfM6Tc/jfcONgN7FLabjpdJ/lzSZ5sXdpmtek9n7AD7sSx/6ezeYtmXdpmPqJNO20T+XaxDEXlM7WeMIYvcqloNK9zKL1JzSBUqf0gVKX6qrGENQjtKndIHvK2YZVMgRSp/SRYS+s846KzrLYP78+eb+++/PzBF55YjQ5ZdhxsKnP/1p769p7Qy8ttl0L241DR9rMJ00BGyDoNd0TF9gBh8ibDBzXIPgsC5jv/evaTEta/yzeA2C3NfUzpQpUzLrnXevYjkidi/zyqXmlC5C6VO6CKUv1cUsA94HsnJ55YhUX0pO6SKUPqWL8H1Fg6BCjlD6lC6iFt99991n3yiycrFyLvJcPH6g14EPzST6u0zL3J5dP69uMY3X9O9qEExtN5s5vrP0Ib/v0If9jKHXl33Yew2C3NfUDrMnwnrH7lUsR+Tdy1i51JzSRSh9Sheh9CldhNKndBFKn9JF+L66fWSQRaxMag6UPqULQl9vb2/uIwO3RHFWzhHLhS547LHH7JbLL7/8spk7d679bwpblzSaSQe0mNbZTWbyvm1mw45djwz618wxDVNaTMu0yeZDu6FB4OoWErtXsRxk3UuIlUvNKV2g9CldoPSlunhkwPtASKwcpPpSckoXKH1KF/i+um0QFHsZ7CI1B6EvbwyBv0RxmPOJ5UIXH5RHH320bRRQ5vbbb7cLHY0tA2bD0k6zmXEF/ctN0yyvJ0HE97//fVu3kNi9iuUgvJeOWLnUnNIFSp/SBUpfqqsYQ1CO0qd0ge+r2wYB3RzFLINBUnMQ+ngjCBsE4RLFsXPGcr6rv7/f9jgwHsEvc+ihhw6/Zsx4rdu0z51j5szrNBv6w+T4k7fYUuxexXIQ/t4csXKpOaULlD6lC5S+VFfRIChH6VO6wPcVDQITz4G6blnEyqTmoBrfihUrzOWXX56ZC4nlfNe5555rbr75Zvtnv8zChQtNd3f38Ovqndtuu80sWLAgPGyJ3atYDqr5vYWk5pQuUPqULlD6lC5Q+pQuUPqULvB9ddsgKB4Z7CI1B6GP3gG/h+DNN98sW6I4ds5Yzrm+973vmcsuu2z4eFiGXoLf/va3wz/XK9SB3oG8+xHW2yeWg/D35oiVS80pXaD0KV2g9KW66L3jPSAkVg5SfSk5pQuUPqULfF/dNgiyiJVJzYHSp3RB6AvHEPBMP1yiOHbOWA7XDTfcYC688MIRx8MyzGaYPXu294r6hDqsXbs2936E9faJ5SD8vTli5VJzShcofUoXKH2pruKRQTlKn9IFvm8vBna5ePHFF234x6rJESzPGh6rVC41p3QRSp/SRYQ+th7esmWLLcMHc3Nzs9m2bVvV54zleOwwc+ZM88wzz1Qs881vftOcc8453l/b+oJrpw5ZdYvVu5ocEf7eqimXmlO6CKVP6SKUvlTXxo0bzTHHHJOZyytHpPpSckoXofQpXYTvK3oITDwHSp/SBTEfSxTfddddYSp6zrwcMwl49JDlyyvDeALGGtQb5513nr12yKsbpOYg6z5CrFxqTukCpU/pAqVP6QKlT+kCpU/pAt9Xtw2CYuniXaTmIM/HtwQWC8oids6sHC1Pphf29GRP+csq42DXtVmzZoWHJyzHH398dHyET2oO8n5vsXKpOaULlD6lC5Q+pQuUPqULlD6lC/aIBkExy2AXqTkIfW4MAUsUr1+/fkTOETtnmPOnF4YuR1jGhxxbB7M50Lp168L0hIH6cY0PPPDAiOOV6paSg9R7mZJTukDpU7pA6Ut1FWMIylH6lC7wfUWDwMRzoK5bFrEyqTkIfTQIGAjHEsV55WLnDHP+9MLQ5QjL+LgcS2+ecsop5mtf+5p5/fXXw5ftNrgWrok3UK4xpJq6ZRHLwWjuZRaxnNIFSp/SBUpfqqtoEJSj9Cld4PuKBoGJ50BdtyxiZVJzEPreeustOzr+8ccfzy0XO6efC6cXhi5HteeDn/zkJ+aoo44yp556qn0UsbvAzTVwLVzTWNTNJ5YDpU/pAqVP6QKlL9VFg4D3gZBYOUj1peSULlD6lC7wfXW7uRE78vGXttoyqTlCXbfwWKUyqTki9LGQDgPiYuWqyV177bXm/PPPj7rCMuHxWO7hhx82xx13nDnyyCPNqlWrvL/e4wsuBkfi5hrGo26VcoTSp3QRSp/SRSh9Sheh9CldhNKndBG+r24bBOGxSmVSc4TSp3QRvu+Xv/yl2WeffewGRrFylXLse3DSSSfZRlueKywTO18s19fXZ5YtW2aOOOII86UvfclObXzuuefCz/FkOBfn5NxMyWRdhrAhSoxH3fJyhNKndBFKn9JFKH2pLrY/PvnkkzNzeeWIVF9KTukilD6li/B9dfvIIItYmdQcKH1KF/g+lij+1Kc+VbaXQUgs98gjj5iWlpbMemQdg9j5asn96le/MnfeeacdEMkAv4MPPtjOlFiyZInt+eDaWN2SNzxWYCT4M8fI8ZrvfOc7dhAkqyVyDs7FOTl36PMZ77qFKH1KFyh9ShcofamuYgxBOUqf0gW+r2gQmHgOlD6lC5zPLVFMN3hqg4Dn6tOmTbPP1LPYHXUjHnzwQdvYueSSS8zZZ59tey9OOOEEOxiQb0IcI8drWIzJbeIUEvPtjrplESuXmlO6QOlTukDpS3XRIGBxspBYOUj1peSULlD6lC7wfXXbICj2MthFag6cL1yiOFYuK+emF8aW6t1ddQuJlUvNKV2g9CldoPQpXaD0KV2g9CldoPQpXeD76rZBwHOPYpbBIKk5wMeyljNnzrSvu/jii5N6CNz0wqycY3fULYtYudSc0gVKn9IFSp/SBUpfqoseAt4HQmLlINWXklO6QOlTusD3FQ0CE8+Bum5ZxMqk5gAfswrcEsXh5kZZhDl/emGY89kddcsiVi41p3SB0qd0gdKndIHSl+oqxhCUo/QpXeD76naWwauvvmo3Zai2TGqOUNctPFapTGqOWLNmjR14535mVUBmB8TK+blwemGsnLpuSp/SRSh9Sheh9CldhNKX6mKgLe8DWbm8ckSqLyWndBFKn9JF+L66bRCExyqVSc0RSp/SRZx44ol2SeDweKycy9GYCKcXxsqp66b0KV2E0qd0EUqf0kUofUoXofQpXYTSp3QRvq9uHxlkESuTmgOlT+lir4K5c+eOOHb11VdX9cjg0UcfNV/5ylfKrjdWLnytI1YmNQehj/UDnn/+ebNp0yY7m4INnJ588kl7zK3MFjtnLBe6HLEyqTlQ+pQuUPqULlD6Ul08MuB9ICRWDlJ9KTmlC5Q+pQt8X9EgMPEcKH1KF7MC7rnnnhHHqhlDsG3bNju9cPPmzWEqWk5Vt2eeeca+oX31q1+1iwk1NTXZ//Iz6wpccMEFdtwEA6e++c1v2mPutVOmTLHrDzAdkXNwLkeeD1R1cyh9ShcofUoXKH2prmIMQTlKn9IFvq9uGwTF9se7SMmxmuCiRYvKfCtXrow2CJheyBz+sCHhyCsHocsRK1NNjkWDbrjhBrsgEgsKnXHGGWb16tW2KywVynIOzsU5Ofd1111X9nfOMV51y0PpU7pA6VO6QOlLddEg4H0gJFYOUn0pOaULlD6lC3xf3TYIeMMuZhkMUmvuvffes939LMBTq4/phTfeeGNmDvLKQa0uiOVY84APahZUYlEhxjKMF5z7+uuvt3sn0IvCAkY+Y123WA6UPqULlD6lC5Q+pQuUPqULlD6lC3xf0SAw8Ryo65ZFrEytOT48WZ8fQh8DBfN6CNz0wqycI5YLXY5Ymawc317o3r/ooovsXgZqcC5YsMBeg/smNVZ1c8RyoPQpXaD0KV2g9KW66CHgfSAkVg5SfSk5pQuUPqULfF/dzjLg2y3PsKstk5oj1HULj1UqU0uOFR75lvvGG2/Yn0Pf9OnTM6cd+tMLw1zM50foqqaMn3vggQfshzB7E0wUuBauiV0Qw2sPrz+M1Bwx2nsZRiyndBFKn9JFKH2pLqYdtra2ZubyyhGpvpSc0kUofUoX4fvqtkFQbH+cluNbPj0E7ufQxwdc2CAIpxeG54z5/Ahd1ZRxuba2NrteAmNHJhpcE/sicI151x8eH02OGM29DI9XyildhNKndBFKX6qLBgHvA1m5vHJEqi8lp3QRSp/SRfi+un1kkEWsTGoOlL7xdPlLFDsq+R577LGy6YXV+kIqubJgSiDfwBkEOdHhGtmG2d8cKVa31Byk3MvUnNIFSp/SBUqf0gVKn9IFSp/SBb6vaBCYeA6UvvF0+UsUO0Ifc/TdGAI+jI8++uiy6YXV+kJClyOvDLsmMgWQFmy9wLXSgHE7PubVDVJzUOu9hNSc0gVKn9IFSl+qizEEvA+ExMpBqi8lp3SB0qd0ge8rGgQmngOlb7xcTzzxRNkiRBD63DoEPI6hG/z+++8fkYdqfFmELkdWGQaM8sHKtdQbXDPXTh2y6uZIzUEt99KRmlO6QOlTukDpS3UV6xCUo/QpXeD76rZBUGx/vItqcmeeeab58Y9/HKbLfOecc479QDv77LPt9MIsqvFlEbocYRnWFaBnIG/Ofz3AtVOHt99+O/d+hPX2ieWg2nvpk5pTukDpU7pA6Ut10SDgfSAkVg5SfSk5pQuUPqULfF/dNgjomi2mHQ5SKcec+fb29jBlyfIx8PDSSy+NnjMll+WCsMzMmTPt0sj1DnWYMWNG7v0I6+0Ty0G199InNad0gdKndIHSp3SB0qd0gdKndIHvKxoEJp4Ddd2yiJWplKPr/6mnngpTltDH6GK+HVQ6Z0oudDn8MoxxYPGjPQXu5e233x4etsTuVSwH1dzLkNSc0gVKn9IFSl+qix6C3t7eMBUtB6m+lJzSBUqf0gW+b6+XX37ZuGAUOuEfqyZHsBVxeKxSudQcLhoDDNyqtkxqjlDXLTxWqUwst2zZMnP66aeXHXfh+1i7/6//+q/tfY2dMzVXTd0OPvhg+xd4T4G6fPGLXyyrc1jvMGI5opp7GUZqTukilD6li1D6Ul1s+nXMMcdk5vLKEam+lJzSRSh9Shfh++q2hyCLWJnUHCh9Y+liiWKW9n3hhRfC1DDO56YX8vq8lQodqblKdWPnwdNOOy1M1z2sT0DdQmL3KpaDSvcyi9Sc0gVKn9IFSl+qqxhUWI7Sp3SB7ysaBCaeA6VvLF0sQNTZ2ZmZc+CjZRhOL8w7J6TmKtWN5YDZlrha+la3msZ9G0zDfk2mfX2/PbZuXptZF7yuKrZvMB3TSufaf7JpnNFpNgyeLoN+s2FjeXdqDOpE3UJi9yqWg0r3MovUnNIFSp/SBUqf0gVKn9IFSp/SBb6vaBCYeA6UvrFyvfnmm3bTH5YoDnM+W7Zssdsgu+mF9A7srh4CllT+7W9/G6azGegyzVOWm8HP7T7TeUCr6eFwf78ZcC/Z3mf6dwyUjpWO7BwY/LM9NnyWIbaa9v1aTPf2oR9f6zSNBy4vnbVUhrIw0G/sH3eUvNO6Bv9cJdSJuoXE7lUsB5XuZRapOaULlD6lC5S+VBc7mmZN942Vg1RfSk7pAqVP6YI9okFQbH+8i6wcPQNdXV2ZOZ+TTz7Z3HzzzcM/u3UIYuVSc5Xq1tTUFKYi9JqO/SaZ5sVdZt2WXY2AnhkttmHQu6SplOs2XfOazN4HdJjelzpM4z4tZsGKLjNnvyaz3N8T6fF203DWyMWXemZMNh2v9JiWGZytxJoW07Km1C7YtsA07N9mNr824uUVyapb7F7FclDpXmaRmlO6QOlTukDpS3UVjwzKUfqULtgjGgTFLINdhDkGkMycObhEcZjzYXohOwb67I4GAYMYzzvvPPOJT3wiTFWg9A1+yzrTeW6TmfyxVtOzwzUISo2F6QvM4CoVG8ycoQZB09CHfu/iFtPxkneah+eYxsUjHwP0zJhUek15g6CU2XWsBqZMmRIeit6rWA7y7mWsXGpO6QKlT+kCpS/VVTQIylH6lC4Y0SDgxS5imyDEcoS/QUK15VJzuIrdDvNzfMgz1S0r58LtXjgWvmpzocs1BHhksXbt2sxv0bk8vsA0L931Ib51YaNp2+g1CKa2G/vxv7P0Ab7vYIOgZehDv6xBwGOAfUsNiJ1DP+/cbNr3ays1JUplp3cPHlvdMqoGAXUL70fsXsVyRHgvqymXmlO6CKVP6SKUPqWLUPqULkLpU7oI31c0CCrkCHXdwmOVyvg5pgyxK2BWzoW/e+FofWHEcs7lGgJc5z333DNc5thjjzXbt7sH+ZXoNz2zJ5nJh7Wa1umNZvLU5abX7Hpk0L9mjmmYUvoQnzbZfKhSg4DXr28zk/dtMq2zW0zTPzWZ9o2MTugzy6dMNk2lYy377j3UINhg2vZpMG1r/WcOcagTdQvvR+xexXL+vQwjVi41p3QRSp/SRSh9Sheh9CldhNKndBG+r24fGRRjCHbh51ii+KGHHsrMQbh7Yegbz0cGfPh/85vftHsq+HskuDKsg+CPZ0hnwGxY2mk2842/f7lpmlX7N/qxhDpRt5DYvYrlIPy9OWLlUnNKFyh9Shcofamu4pFBOUqf0gW+r24bBFnEyqTmQOkbjWv9+vVlSxT75bKmF4a+8WgQsEoiDYHjjjsuulkSDTxmRowJr3Wb9rlzzJx5sSmEGqhT2HiFrHvliOUg/L05YuVSc0oXKH1KFyh9qa6iQVCO0qd0QdEgCIjlQOkbjYtn8XkDLZlK5E8vdIQ+djkcqwaBawi4HoHQ5fDLnHjiiXZnxj0F6sLS0VlUex+zqOZehqTmlC5Q+pQuUPpSXTQIeB8IiZWDVF9KTukCpU/pAt9XNAhMPAdKX6rrtttuM4sWLQpTw+XYHyCrOz7VF8s9/vjjmY8GqnHxmqw5+/XKEUccYce7ZFHpPubloJp7GZKaU7pA6VO6QOlTukDpU7pA6VO6wPfVbYOg2P54F7/85S/tksN5G5J85zvfMZdddlmYsoS+efPmJfcQ0CPgBguGPREQuhzh+bjWjo4O7xX1CXVYunRp5r2CsN4+sRxUey99UnNKFyh9Shcofakuegh4HwiJlYNUX0pO6QKlT+kC31fMMqiQI9R1C49VKsOAte9///tlx4lrrrnGfkiHx12EvunTp9vZBzFfmPNnDTB9MK9c6Mo7H8EzzFtvvdX7a1tfcO3UIatusXpXkyNquZejzSldhNKndBFKX6rr5z//uWltbc3M5ZUjUn0pOaWLUPqULsL31W0PAZXIe16eRWoO1HXLIq8MSxTTxd7XVz4Njg9nphcyfiCP0MdSxtX2EIRjBPxcFqHLkVeGHde6u4fWAagjuGauHfLqBqk5qPVeQmpO6QKlT+kCpS/VRQ8B7wMhsXKQ6kvJKV2g9Cld4PvqtkFQPDIYhCWK2cQozLnphS+99FJZzqdWH7gegXCMAMTKpbj4pkLvR73A72P27NnDP8fqlpqDlHuZmlO6QOlTukDpU7pA6VO6QOlTusD31W2DIItYmdQcKH21uNwSxXyj93P+9MKscj6h7+KLL87tIXA9Ajwa+OEPfzgi58gq5whdjlgZclzT8ccfn7nhykSBa+Maw4GdleqWkoPUe5mSU7pA6VO6QOlLddFDwL+5kFg5SPWl5JQuUPqULvB9RYPAxHOg9NXiWrhwobn77rtH5MLphVnlfEJf1joE4aOB2DljudDliJVxObYQPvzww+2SyxMNromlibO2bq6mblnEcjCae5lFLKd0gdKndIHSl+oq1iEoR+lTusD3FQ0CE8+B0leti/ntfECHuXB6YVguJPStW7duuEGQN30wds5YLnQ5YmXCHKP2DzvsMDvNcnfDNXAtXNNY1M0nlgOlT+kCpU/pAqUv1UWDgPeBkFg5SPWl5JQuUPqULvB9dTvLgJHwLJ5RbZnUHKGuW3gsq8z8+fPtP1o/xzS3Sy+9NFoujCwfYwRoWNDTwHLDYT52zlguy1WpTFbu3XfftXU95JBDzCWXXJI5oHK8wOXc/JdrGcu6VZMjlD6li1D6lC5C6VO6CKVP6SKUPqWL8H112yD4IE87vPfee81ZZ501Isf0QnYvjJXLCt9HQ4BvvHQXxqYPxs4Zy1VTtzAq5djVkcccXDfbOYcDTccCzsm5ceC64447yq5lPOqWlyOUPqWLUPqULkLpS3Ux7fDKK6/MzOWVI1J9KTmli1D6lC7C99XtIwMq8UGddhguUczuhXTrZw26i7kAnz9G4POf/3zZGIKQ1Fw1dQupNvf++++bnp4e09bWZg499FA7FZNejlWrVtmG49tvvx2ULofX8FrKXHDBBfYcnItzcm7+UeVdy3jWLQulT+kCpU/pAqUv1VWMIShH6VO6wPcVDQITz8Fo6vbqq6/ab/N0MU+ZMsUuZXv55Zfnlqvk4tupP5Kd6YWsUsjMgixi10hD4NRTTx0xRmDlypV12SAIIXfXXXeZG264wX64M32Re+9+Dy4YDMgxcryGRsR1111nx09wjvCceT5l3UDpU7pA6VO6QOlLddEg4H0gJFYOUn0pOaULlD6lC3xf3TYI6mH74+uvv96OjH/00UeHj+/cudP84Ac/sB9CWc++Y6533nnHri3glih20wtpFMSuI8z5PQJdXV0jco6sco7UXKxueWVSc6D0KV2g9CldoPQpXaD0KV2g9CldoPQpXeD76rZBkEWsTGoOUnwM+ON5cx7PPPOM7Y4OibmY3nbFFVfYn/3phbHr8HPh9EEIfTx+2BN6CEDpU7pA6VO6QOlTukDpS3XRQ8D7QEisHKT6UnJKFyh9Shf4vqJBYOI5SPHxYR/2YIQwcIfBgD55Lgb68EzbPQv3pxfGroNc3vRBCH1Z6xCEpOZClyNWJjUHSp/SBUqf0gVKn9IFSl+qqxhDUI7Sp3SB7ysaBCaeg1p9fGjTtV8JBqmFW/3muRjlzhLF7s/+7oV511Fp90EIfcypLxoEteeULlD6lC5Q+pQuUPpSXTQIeB8IiZWDVF9KTukCpU/pAt+3F8+hXbAULuEfqyZHPPvss2XHKpVLzeFiQCHT5Kotk5ojaq0bg9nOOecc75bn84UvfKGi64EHHjDNzc3mueees1sZf+Mb34heB/5TTjnFHHvssXaAXNY1xnxZ5xyLnNJFKH1KF6H0KV2E0qd0EUqf0kUofUoXofQpXYTvq9segok8y4BBf24VwUqE4wiyXCxRzHx7nuvxQR9OL3TXkTVGIO8aHaFv06ZNRQ9BQk7pAqVP6QKlT+kCpS/VRQ8B7wMhsXKQ6kvJKV2g9Cld4PuKBoGJ5yClbkxpqwQDD5kS5xO63BLFjzzyiJ1eGObB332Q3gSf2DVCeL5iDEFaTukCpU/pAqVP6QKlL9VVjCEoR+lTusD31W2DYKJvf8wzuO9+97vh4RHQO8Dyyz6h68wzz7RzgqdNm2Y/+H1Gu/sghD4edRQNgtpzShcofUoXKH1KFyh9qS4aBFmPPGPlINWXklO6QOlTusD31W2DIItYmdQcpPpOOOEE+8w/5PXXX7djArq7u8PUCNf69evNaaedZqcXspSwc/mPBugRqHQdeTlIrVtKTukCpU/pAqVP6QKlT+kCpU/pAqVP6QKlT+kC31c0CEw8B6PxsW4Ajw/oij/xxBPtN30WK9qwYUNYxOK7aAjQdcf0Qs7nTx/0Hw1Ucx15hHVj/EPRQ1B7TukCpU/pAqVP6QKlL9VFD4Fb/CzM5ZWDVF9KTukCpU/pAt9Xt5sbhccqlUnNEWPh+/GPf2ynIrJLY5jzw7nYWpfVDNm9cLx2H/R9LqZPn17xGlNzoauaMqk5QulTugilT+kilD6li1D6Ul2secIy3lm5vHJEqi8lp3QRSp/SRfi+um0Q1Nv2xy+88II5+eSTM3N+4PrlL39pGhoa7IezW0dgPHYfdD7/Z94IigZB7Tmli1D6lC5C6VO6CKUv1VU0CMpD6VO6CN9Xt48MqMREmWWwZcuWsmDXPJ71u58Z9MejgqycH/fdd5/tGfjwhz9svva1rw0/GohdY2oOKtUti9Sc0gVZPv5hsIcEb3r8l0WkOOaInTOWy3JBrExqDpQ+pQuUPqULlD6lC5Q+pQuUPqULfF/RIDDxHFSqGyNyw2CHQ8L9fNJJJ5lPfvKTmTk/GB+w1157mT/4gz+wjwl4dPDSSy9FrzE1B2HdGD9Q72MI6GGhIXX++eebr371q+awww6zuxoyq+PLX/6yOe644+yAT2LWrFn2GDn3Gr4dMRiUc3AuR54PVHVzKH1KFyh9Shcofaku9kkJ1zpxubxykOpLySldoPQpXeD7igaBiedgLOrGIJ158+Zl5nxw/f7v/775vd/7PfOtb33LLF++3DYSZs6caT+kHnzwQdul7xM7XywHYd3qdR0CxmgwRZMP9GOOOcYsWbLE3qtf/OIXQanKUIaynINzcU7OzboRedcynnXLQulTukDpU7pA6Ut1FesQlKP0KV3g++q2QVAP2x/7uVoaBH/1V39l/t//+3/moIMOMqtXr7bH6eLu6ekxixYtsgsUMR3xhhtusLsmxs4Xy0FYt3pqEDz99NPmjDPOsB/YF110kX0MM15wbhw8zqF3Z9u2bSPyY123WA6UPqULlD6lC5S+VFfRIChH6VO6wPfVbYMgi1iZ1ByMha+WBsHBBx9sfvd3f9fsu+++ZubMmbYhEJZh/emuri77gUiX94IFC+zAQ/4x+4TlQsaibj6x3Fi5WLWR6Zu8aWUtsTre4ORxA9fAtcBY1c0Ry4HSp3SB0qd0gdKndIHSp3SB0qd0ge+r21kG4bFKZVJzxFj4apllwIf77/zO79iNj9rb2+1z7tgsA2Zb8LyblRF5LYMRly1bZj+seAaeV875wmNE7BpTc6N1sTIlMy9OPfVU88Ybb3h/pXcPXAPXwjX96Ec/Krv28PrDSM0Ro72XYcRySheh9CldhNKndBFKn9JFKH1KF+H7igZBhRwxFr5aGgT33nuvfWTw2c9+1j7DZmAhHzpuVcIwwvPhuvXWW+2gRGY20MXN4kZ8oIZlw7pN1HUIOjo67OqOYVf9RIBr4j5zjXnXHx4fTY4Yzb0Mj1fKKV2E0qd0EUpfqquYdlgeSp/SRfi+un1kMNH3MghztTwy4Jsngwr3339/c/rpp5v58+fbAZRHHXWUeeyxx8Ii0fMxHmDjxo3DgxPpQfje9743PDgxrNtEG0PAiGe65unxmOhwjTy+effdd4ePxeqWmoOUe5maU7pA6VO6QOlLdRVjCMpR+pQu8H112yCgVbOnzjL4v//7P/OHf/iH5m//9m/t6oYXXnihueOOO+yKhVOnTh1VvZl7z5oIbnAi//Dd4ETg8cNEaRC8+uqrdspg3jLPExGuld4Crh3y6gapOaj1XkJqTukCpU/pAqUv1UWDINx0zeXyykGqLyWndIHSp3RB0SAIiOVgLOpWS4MA/vIv/9J86EMfsnPpb7zxRvvNnW/LfNs/+uijR/SOxM4Xy8H9999vByfSE8HjiYsvvtiOV+D+5pWLnTOWq+U+8qZEY+Dll18ecbwe4Jq5duqQVTdHag5quZeO1JzSBUqf0gVKn9IFSp/SBUqf0gV7RINgT35kAP/6r/9q/uiP/shcfvnldkMj/nvllVfaHHPujz32WPOzn/3M/hw7XywHft0YgEiXN8/CebTAZkxXXXWVfUzx61//evh1sXPGcrXcxyOOOKLyeIH72szef773YOzTaFqXbjD94Wt2E1w7dciqmyM1B7XcS0dqTukCpU/pAqUv1UVD1r1Xhbm8cpDqS8kpXaD0KV3g++q2QZBFrExqDsbCV2uDgO78P/7jP7Zd+3z4P/fcc3bAn/vGzBLHs2fPtlspx84Xy0FYN38MAYMTb7/9dnPBBRfYRxVu5UQaYnnnjPlClyMsw31as2aN94oc1rSYluGXDZju6Q2m45WhH3f0m76+fjO4KPGA6e8fMAPb+0xf6b9mwM+V2FnK9/WZ/h2DPw7wmiGG/+xesytVEepwyimn5N6PsN4+sRxUey99UnNKFyh9ShcofamuYgxBOUqf0gW+r5hlUCFHjIWvllkG/JcP3z/5kz8xRx55pO0ZuP766+0iRfQWuNfyYc23eHpL8s4Xc/k+F+yzkDXLgEYCz8fZzpldF2mksHIijxz8TabCcjFXVpn169fbZZ6rotQgaF7RZ8dF9L2yzrTt22y6Sh/qA2vnmMbZnaZ7xRzTNKvH9Jse07J3k1mwosvM2W9vM3luh+le2GQmn0sPU6/pmNJk2ld0m45pk03bw8asO7HFnsfs6DLNpfJmR4+Zc0Cr6VxVKj+l1fTU0A1BXaqdHVJtjqjmXoaRmlO6CKVP6SKUvlQXswx4H8jK5ZUjUn0pOaWLUPqULsL3FQ2CCjliLHy1Ngiuvvpq86d/+qfm4x//uO1+Zk1+yvDtmRkC7vW33HKLPS/L7YbnquTyfWHEynGcQXN33323WbhwoR17wHVde+215vHHH88tV42LBlDVyw2XGgQN87tN97I20/DhUmNg++DhnhmTTOs1peOruk3b/o1meV+pQXBYl+0R6F3cYhbYJxGlYzNKH/YvdZiWhUOPnvqWm0YaAA/PMc1X9ZmBm1rMnPWDnkmzl9vzdc9vMI2lXLVQF+oU1jmsdxixHFHNvQwjNad0EUqf0kUofUoXofQpXYTSp3QRvq9uHxnU29LFdLOzqU5Wzse5GDz4Z3/2Z3YZY8YKMPWQY0888YTdDtlnxYoVdlCg/5zfEXNBWDcGFabMMnArJ7KkMuMQ3OBEf+XE0OVw56PhQ49H1fiPDB5vN5NKH/p8ee+Z1WQ6XxrsOdj88AbTv3Pow98MNgg6XuJPQ8de6TDNZ20ePEepcdA4lxkNW037jDbTNr3d2MzaVtO0tHewJ+LxDWbDUMOjWlgoKms8RNZ9dMRyUOleZpGaU7pA6VO6QOlLdfFvln+/IbFykOpLySldoPQpXeD76rZBQKumnmYZfPvb3zaNjY3moYceKsv5OBf1Ywvkv/iLv7C9ADyPZjwBMOiPhYccnI91BpiREBJzQVi3sViHgC2FGfj4/e9/3/Zs0IBhcOKdd94ZbbRQLx5BVM2IMQTGbJj7EdOyutQk2Fb6kD+gzXSUPvwbZw89MshrEJCd3Whazm03Lfs2mo6hz+3exZPMR+wjhRI7t5qOqY2mbXGHaTmgtkcGQJ2oW0il+5iXg/D35oiVS80pXaD0KV2g9KW6ijEE5Sh9ShcUDYKAWA5GWzfWEGBr4y1bttiBgVx7Xjnnogfkb/7mb8zHPvYx841vfMPOAGBgH/9lIOGXvvSl4a5152KsAV34PrFrhLBu7Og32gZBmGNAJeMdeKxAHc477zzT3d1tt3X2y7DwUlaDod6hTtQtJOteOWI5CH9vjli51JzSBUqf0gVKX6qLBgHvAyGxcpDqS8kpXaD0KV1QNAgCYjkYTd1cI8B1ndM4YCnhvHK+61/+5V/MRz/6UfP5z3/eno+9CviWDaxNsHTpUvtn/zo4xmA/R+waYTR1yyKWw0WOcQb+yolcLwPvDjzwwLDIHkNTU1N4KHqvYjlQ/96yiJVJzYHSp3SB0qd0gdKndIHSp3SB76vbBkE9jCF455137Ih8HhP4LF682PzgBz8Ycczhu9jW9+///u/NP/zDP9gGxZNPPmk304H333/fTjvM2v74kksusd31EOZCwroxmHGsewgcoQt4Lu8GJ06ePDlM7zFMmTIlPBS9V7EcZN1LiJVLzSldoPQpXaD0pbp4r+F9ICRWDlJ9KTmlC5Q+pQt8X902CLKIlUnNQaqPD35G3ofQ7c9As7ChAL6LLXb/+Z//2eyzzz52Oh4uvlW7BZno1jv77LMzr4MdE3lun5XzCes2FmMI8nKhy+HKZH2L3lPIqlvsXsVyUOleZpGaU7pA6VO6QOlLdRVjCMpR+pQu8H17sdCNixdffNGGf6yaHMEo8/BYpXKpOaWLSPGxsiC9A+FxV66np8cub/voo4+OyPmutrY283d/93e2QcBjBsp1dnbavQ3ca5jnTqMjvA5+JkejJMzl+YhLL73UPuaI1S01F7rCMuxmSK9KrQysbjWTzxXsd9C/wWwYHPZQE9SJuuXVOzxeKUdUupfh8dHklC5C6VO6CKUv1cVsJt4HsnJ55YhUX0pO6SKUPqWL8H1FD4GJ56BWHx+o7DfgNrgJceXcYEMf38XsAhoDnx3aBpky27dvt3/euXOnfQ0rGM6cOdN+qw9577337DRAxhvkUWvdIDVXyUVjx42RqJ4+0zmlxbROG1yUaJgRKxVm/GxXK9y18uBA/8hVDYdXN+wfWt2QIzc1m+YV3jmqhDpRt5DYvYrloNK9zCI1p3SB0qd0gdKndIHSp3SB0qd0ge+r2wbBRN3LgA9hegbozg9zDr8cA+r8xwq+i6l7n/zkJ80XvvAFO8CQcRPAHGEG4TkYSHjNNdcM/+zDTAQWLmKVwyzCujG9cXc9MmCFtBkzZoTpOC91mKazNg9+WN809MG9ttU0zBpaqZBph8HPAzt6TOv+QysP7t9klr/GYkYtZnBiopuOyOqGDaZtWbfpmDrJtG0cMFsXNpiG+ZtN9csSDcKOldQtJHavYjmodC+zSM0pXaD0KV2g9KW6eGSQtXR4rByk+lJyShcofUoX+L66bRBM1FkG7gM+K+fwc64B4cYT+C4aPA0NDeZTn/qU3eyIRYmAJYRZ2tjx5ptvmmnTpmVeJy6mKeLgMUVIWGZ3jiEA/G46YjVsPXeyabqw23Tf1GYm79dpP6x7Zu3qLRjYMVD2s12/wLWPHp5jGpf0ZjcIhlY3HF7vIFj3oBqoC/tSZBG7V7EcVHMvQ1JzShcofUoXKH2prmIMQTlKn9IFvq9oEJh4Dqqtm/8IIMz5hDl/aqLv4vFAQ6lBwIA0RuDfdNNNwzmm67nXcj4WKnILF/k4F99QW1tbR/QsQFg3eht2Z4OAWRN8o66KnevMnAMWmM2sIFiK7tmTTfsWv0HQZzav32puD36mB8H1JvAhz1LENAi6eQqzs3tXg2BoMaPRNAho4DA7JIvYvYrloJp7GZKaU7pA6VO6QOlLdfE+5KY0h7m8cpDqS8kpXaD0KV3g++q2QTDRHhmE6w3EzpmVc42J0PXpT3/afvv/zGc+YxcocrDZEUsWgzsfO+tt2rRp+DV+Dhg0woetP7sh9DmyrtGRmqvWddFFF2XOzihj/RzTtNTrwN/SbiafuG54pcL2eY2mecnW8p/dyoPntpnGfQdXHuy7ptTomtJqWqZPNnvnNQg2tplJ+7WZniqfGVAHNqPKux9hvX1iOaj2Xvqk5pQuUPqULlD6lC5Q+pQuUPqULvB9xeZGFXJEJZ+bRsi4gTAXlonlmBHAGgL+MWYiHHHEEXaluwMOOGD4OKNG6SXwz8cHvdtAKc9FrwrneuSRR+zPYd04nrXbYeyc1eZCV6wMKzEyhqJe4dqpQ1bdYvWuJkfUci9Hm1O6CKVP6SKUvlQXvYnu/SHM5ZUjUn0pOaWLUPqULsL3FQ2CCjmiks8tNJSVC8vEcjQsGFTnNyxoaBx00EG26/mzn/2s3TXR5c4880zz8MMPjzgf6w/cdtttUddjjz1mGxmsGBjWjV6OidAg4LEF18i0zHqDa+baqUNW3WL1riZH1HIvR5tTugilT+kilL5Ul3u8mJXLK0ek+lJySheh9CldhO+r20cGWcTKpOYg5mOgXjh10OXyzhnL3XfffeaMM84YfvRAjwEDCnlswBLG99xzz/Br2TSHTZP88/HLZVpif//gDjx5LuYaMzUyXLOcuuzOMQQ+/CVm/j4NnHqBPRpczwDk1Q1Sc1DrvYTUnNIFSp/SBUpfqov3nlrf0yDVl5JTukDpU7pgxCMD73i0YCwHykrgmkhLF/tb/Ia5LGK50MUaAp/73OfswMKGhga7n4GDc7DVMAPq/PNdd911dmEkiLlctzbbK4fEyqXmwro5YmXIMTYi6w1qosE1+uM8oFLdUnKQei9TckoXKH1KFyh9ShcofUoXKH1KF+wRDQK+CU+0WQY+qbnQde+995ovfvGLdjAgz/7p0ve57LLL7AJG/vnYXe+4444z27Zti7qABgd7IjA1EdiZcKL0EIDLUcfDDz+8tu2RRXBN7FXANYZUU7csYjkYzb3MIpZTukDpU7pA6Ut18QWH94GQWDlI9aXklC5Q+pQuKBoEAbEcqOvms3nzZvthw1iCOXPm2EWK3n777eE8KxWyRHF4vh/+8Ifm/PPPj7oA31133WXPzTTH3b0OQYife/fdd+3jFHpFGAexu+EauBau6fnnnw/TlmrrFhLLwWjvZUgsp3SB0qd0gdKX6irWIShH6VO6wPcVDQITz4G6bj4M8KGHgE2N6I7mWzIjgH2YWUDDIYT9DxiTkOcC5+PZNzsp0rMwURsEDpaE5loZuLdq1aoROQU4cXMNbnnqsaqbI5YDpU/pAqVP6QKlL9VVNAjKUfqULvB9dTvLgJHwb731VtVlUnOEum7+z2yMw3RDegH4Fs9YgiuvvHLEa/iAYmxBeC5WNmS0MKshhrksH+sanH766eZXv/pV9BpTc2HdqikTy9FTwgIqTM386le/atdyoBdhrOGcnBsHLraW5u+ffy1jXbdYjlD6lC5C6VO6CKVP6SKUPqWLUPqULsL31W0PQRaxMqk5UPqyXEceeaRdc4Bv7wceeKDdBdGHhhGDAxk7ELJkyRJzww03hIeHCX1XXHFF7pbKjtRc6HLEylSb4y81AykZZ3HIIYfYgYjs6f6Tn/zEfnhXC6+lDIsKcQ7OxTk5N2s/5F3LeNYtC6VP6QKlT+kCpS/VxQylrM3SYuUg1ZeSU7pA6VO6wPcVDQITz0F1vnWmbd6uKXy/WfsNM+X7Lw7/7BPzZbl4XHDooYfaAYV8SDH9kHM4+DPTDxmAGMJgQRoUWTMhIPQxhoBv3awWmHeNseuP5UKXI1YmNcceAvSkUA8aUnyzp3eF4EOe+0nwZ3ec18ycOdOuLshUx3BPhZhPWTdQ+pQuUPqULlD6Ul3FI4NylD6lC3xf0SAw8RxU5/OWuiW3+hizz8UvDv7gtt4d3LG4VO4d84uf/9z0u+16S/kBXtM/kOlid0PGETCG4IILLrC9Aaz57+A6WAyHMQMh5NgDgU2Xsgh9blAhPQssuJRF7H7FcqHLESuTmgOlT+kCpU/pAqVP6QKlL9VVNAjKUfqULvB9ddsgmGh7GeQ2CLYsMM1zu+zWuw1Tlps+02suPeJIc9a1q0zHtCbTsc2Y3sWN5iPT2kz7TZszXcuXL7ff8tnhcOHChbangM2MHO46mD4YThdyOXY7zNpkJ8sHlKPXgeflIbH7FcvFXHllUnOg9CldoPQpXaD0KV2g9CldoPQpXaD0KV3g++q2QcAz44k1yyCnQfDactP0T02mfVmP2by9lHipw+z3xW+aVStXme6lLWbS3A2lBkEp//hguSzXnXfeaVcq/Na3vmWDHgK6wh3uOlhTINwUyOXY52D+/PkjcpDlA1eOpZCvuuqqzFwWsVwlVxapOVD6lC5Q+pQuUPqULlD6lC5Q+pQuUPqULvB9dTvLgNX1mGpXbZnUHFFd3e40s/5lkXlhKNd//cHm4Ov7zW/eecG80PuOeWvzneZr/zLL3PniInPwv99up6v9fOuPzY+3vmNeuPgYs2hrvosVBZnvTmOAWQBTp061DYTwOugdYC+EvGtkXQJGy/v50BfuZUC0t7fbAXpZ5wwjlgtd1ZRJzRFKn9JFKH1KF6H0KV2E0pfqKvYyKA+lT+kifF/dNgj4QH322WerLpOaI6qt29OXfM7s89ljzKzWg80nP3+B2bS9lOu9whz8ySPNoou/Zvb70hXm5795y9x+/OfM0Qu/bb7x2dLxZ35TsUHA4kOMdL/wwgvtBzazAPiZUe/hdbB87oMPPph5jZynpaVlxJS50Bc2CDjGpkunnXaa7YEIzxlGLBe6qimTmiOUPqWLUPqULkLpU7oIpS/VVTQIykPpU7oI31e3jwyyiJVJzYHSl+ViGhAL4TDd8Pjjj7fP9fkvqxGCf77169fbngRH6GJXRsYkOEIfUxizFiaiUcCgxtWrV5flfGK50OWIlUnNgdKndIHSp3SB0qd0gdKX6mJQIe8DIbFykOpLySldoPQpXeD7igaBiedA6ctzsUIh396ZZUD3PQ2CSy+91Ob8873//vu2F8D9gw5dbArFTIKXX37Z/pznC8sB52RwImMawpwjq5yjFpcjNQdKn9IFSp/SBUqf0gVKn9IFSp/SBUqf0gW+r2gQmHgOlL481wknnGB7CZYtW2auueYau8UxH84Qno/5925J3zAH7F3AYEEIffPmzcvsIXDQncgiSa53IiSvHIQuR6xMag6UPqULlD6lC5Q+pQuUvlQXPQS8D4TEykGqLyWndIHSp3SB76vbBsFE2v44i9RcnouxAex4eP3115vvfe97ZubMmXagIfcgPB8b7bDMMYQ5B2MCHn744TJfNZsbMd2TXghmLoTEyoUuR6xMag6UPqULlD6lC5Q+pQuUvlRXsQ5BOUqf0gW+r24bBAyEmFjTDkeSmstz0QhgLf2uri67qQ7TDtkBccOGDZnn4wP/qaeeyswB+xzQaAh9W7ZsqdggIPf0009n7joYKxe6HLEyqTlQ+pQuUPqULlD6lC5Q+lJdNAh4HwiJlYNUX0pO6QKlT+kC31c0CEw8B+q6ZcEIfxoA/JcZBqw+yDLGPD7IOh+PBWhEZOUcHR0ddo3+LGLlXI5pn0yB9H8PsXJ5dYuVSc2B0qd0gdKndIHSp3SB0qd0gdKndIHSp3SB79uLwWUumMZG+MeqyRFMAQyPVSqXmsPFh9HGjRurLpOaI9R1C48RPCrgkQGrFdKtz2wBfmZsQdb5+AZ/0EEH2f+GORcsd8xmSWzm447Rs8C3g6xzuvBzrPnPfgDsoxDmwsirW6xMao5Q+pQuQulTugilT+kilL5UF++rvA9k5fLKEam+lJzSRSh9Shfh++q2h+CDNoaAZYd5rnfmmWfavQ3uuece2zBgSWO25806H/sRxGYEADMV2MzIUc0YgjDHwknHHnusXSwqzPnk1S1WJjUHSp/SBUqf0gVKn9IFSl+qqxhDUI7Sp3SB76vbBkEWsTKpOVD68lwc50OXnfsYR7By5Ur7j5ZxAPSWZJ2PRgRrF2TlHDx6YQ8Et1nSunXram4QwH333WfPw/nCnCOvblnnc6Tmit0Oy0nNKV2g9CldoPSlumgQ8D4QEisHqb6UnNIFSp/SBb6vaBCYeA6UvjwXvQA8r2e64f33328WLVpkgx6Dm2++Ofd8NBrCTaB88PGPn9UPfWLXmJdj3MKJJ55o3njjjTBlyatb3vmg2hwNEcZDML6CD3k3voLHIay8WC28ljLsCcE5OBfn5Nx0u+Vdy3jWLQulT+kCpU/pAqVP6QKlT+kCpU/pAt9XNAhMPAdKX8zFN10GFjJdkJ6B7u5uu88ASxrnnY9BiFdccUV4eBjnO++88+w4ABY9SukhcNxyyy12RcN33nknTEXrlne+WI4P787OTrtoE70T7NNAw2ms4Zycm1ke9CKwFgSPrHzGum6xHCh9ShcofUoXKH2pLnoIeB8IiZWDVF9KTukCpU/pAt9Xtw2Cibf98UhSczEXA314BPDII4+Y5uZmO3WQKYg89887HwsJsWZAHs7HvWTBIVZDHE2DgNwNN9xgzjrrLPPrX/96RC5Wt9j5whz7WFBvFmpyCzApwYmba+BaYKzq5ojlQOlTukDpU7pA6Ut1FWMIylH6lC7wfXW7udHE2+1wbHIxF4MJ3SOCk08+2Y4RYNdD/vHSnR2WceXY5ZDHDGEu9DG4kB6IcHOjMKrJMQuCxZTyXFllwuNhjv0U2MI5a/2D3QHXwLVwTdu2bSu79vD6w0jNEaO9l2HEckoXofQpXYTSl+riS4TbyCzM5ZUjUn0pOaWLUPqULsL3FQ2CCjlCXbfwmCvjPmRZX+CSSy6xA/l4Zs/z/7vvvrusjCv3wAMP2EcCYS70vfnmm/Z5OYPpYtdYbe6yyy6zg/qyXHllwnA51l2gu56GzUSDa5oyZYq9xrzrD4+PJkeM5l6GxyvllC5C6VO6CKVP6SKUPqWLUPqULsL3FY8MTDwH6rplQRmmEPLBzrgBRsFfd9115rvf/a7dl4D/ZuFczFCgKzAk9NG4cB/ieddYS45eh+985zv2z6HLEZbxIcfgPhpCEx2ukfETPpXqlpKD1HuZklO6QOlTukDpS3XxPrFmzZowFS0Hqb6UnNIFSp/SBb6vbhsEWcTKpOZA6Yu5WEiID2z2KmckPIMJGdnPf+kpyMK5GCTEN9iQ0Md4BDZNYuBi3jXGrj8rR28GWzaHLkdWGaBVy1gJGj/1AgM9v/SlL9lrh7y6QWoOar2XkJpTukDpU7pA6Ut1FWMIylH6lC7wfUWDwMRzoPTFXIwT4Bso4wZ4zseod3pJGNzGwMGs6XXOxSpUrGoYEvr4Rr9+/Xozd+7c3GuMXX9ejl6Mb3/72+FhS1YZfmbRJRpB9QbXzLVTh6y6OVJzEP7eHLFyqTmlC5Q+pQuUvlQXDQJ/sTI/l1cOUn0pOaULlD6lC4oGQUAsB0pfzMUHPt/g+bBmdDszAjjGiPcLLrjALjka4rsYkPj444+PyOf5+ADP+2Yeu/683Pvvv2+7/lmCOSSrDI0eVkAcC/pWt5rGfRtMw35Npn19f5geot9s2NgbHkyGa6cOWXVzpOYg7/cWK5eaU7pA6VO6QOlTukDpU7pA6VO6YI9oEHzQli52ZWgEuA//M844wzz33HO214BlihlTEOK7WO6YDY18Qt+mTZvstEN6IxhB399f/gEau/5Y7oUXXrDXzCwJn7AMYxhYGGhMGOgyzVOWm8Fa9JnOA1pNj0v195m+7YNd+2ZH6XXTukz/0I9jAXVg1cO8+xHW2yeWg/D35oiVS80pXaD0KV2g9KW66CHgfSAkVg5SfSk5pQuUPqULRjQIeLGL2GjEWI5QjozE9UGcZUCwIBFd77feeqvt1mMMAfsRMPPg9NNPzy3Hn5m2x2qH27dvz/W5XgfKMKuBhX9i5wwjlsP1i1/8wk6ZZC5/VhmmUtILMnb0mo79JpnmxV1m3ZZ+4z7ve5c2m+Zzu0z34mbTtHirGdi2wDTs32Y2vzai8KihLvTKhPcirHcYsRwR/t6qKZeaU7oIpU/pIpS+VBePIxmnlJXLK0ek+lJySheh9CldhO8rGgQVcoS6buExvwxrCtAbwCC922+/3U7to1GwePFi++jgvffeyyznfqYsA9/yfCwo5BoENCCYncA+B7FzVptzLmaIMJ6BWRNhGcZChHsIjJ4B079lnek8t8lM/lir6dlRaiTs32QWrOo23as6Tcsn5pgNpse0zHB9B2MHdaGRFd6LsN5hxHJE+HurplxqTukilD6li1D6Ul00CHgfyMrllSNSfSk5pYtQ+pQuwvfV7SMDKvHTn/50xPFYmdQcqOuWhSvDmvp8a2f6IVsbMwXx+eefNyeddJLtIeARQlY5B2UYhOio5PvhD39oGyFZuSxiOd/lvoWwRoIrw7EZM2Z4JcaAxxeY5qW9wz9uXdho2jaWGgSHzTE9fX2mr6/XbHi4t9RkGJ8GAbBNNXULid2rWA4q/d6ySM0pXaD01eriGGXYIpzeLLrWeXxHLxD/9viiQkM6j1p9kJpTukDpU7pA6VO6wPfVbYPggzqGgIF+9A4wLYjn+3yAkmOqG40Fvv1nlfNhUKJbwyH09fb2li1dzLcFZh44ss7piOVCF9+e+bDk3JShoUOvwdjSb3pmTzKTD2s1rdMbzeSpyw3Ng/41raZxRofpmNdompdwLzaYtn0aTNvavvAEo4Y6UbeQ2L2K5SC8l45YudSc0gVKX56Lv5s8lmMMCBuKsckVwb8zerdofJNj9VCCcS/8O2HK7tFHHz28iyaP6BjMyw6l/JvL88WuMTWX6mIMAe8DIbFykOpLySldoPQpXeD76rZBkEWsTGoOlL5KLkavs9AP8/N37txpNzxid0HeoFasWGEWLlyYWc6HRgWNBwh9PPMOGwT0KtAA+e1vf2t/zjqnI5YLXcC3KlZHZKoejwuyNkWqd6hT1n4SsXsVy0HWvYRYudSc0gVKn3MxroY9OGbOnGk/zFnCe/ny5fab/2g2zGJ2zbPPPmvXAGEvkoMPPtg2KnjUx1RgR+waU3Op97FYh6AcpU/pgqJBEBDLgdJXycUHKN84GFxINzSPDlhT/8orr7RT+mbOnJlZzoc3P7710KAIfbwRhA0CYOCiW9gozPnEcqHLwfUz/mH//fcPU3sMfFsMid2rWA7y7mWsXGpO6QKVj823WOGTNSMY58H6/W+99Vb4sjGH57msBMhiYizHzcwfphFnXSPkXT/Ecqn3sWgQlKP0KV1QNAgCYjlQ+iq5GKXPgkR0UbJaId9i+MbPs3hWBOSbjf+8Os9FNydlKvkcvFHSK8GbRZjzieViLlZG/Md//McwtcfAXgchsXsVy0HsXuaVS80pXTDePjaiYk0MGqE0ov/v//7Pe7WetWvX2gYJkbX2Rnj9PrHceN/HEKVP6QKlT+mCEQ0CXuwiNhoxliOUIyNx0aKmK67aMqk5Ql238FhYhjcyN16ANxMeIdD9OHv2bPttg/UGssr5wRsPyyCHPmYY5O12SA8BsxmycpV8ROgKy2R9aO4pULe8eofHK+WISvcyPD6anNJFjJePgYA8Yjv++OPtDKWJBrNv6P2jx8INtq22buFxIvU+vv322/Y9ICuXV45I9aXklC5C6VO6CN9Xtw2CD+q0Q/7M6HwWIVq2bJl9k2NhInJ8w1i5cqVdkyCrXBgzZ860z+79Y/46BFnl6IHYsGFDZo7IK0dUqhvPVunGzWadafvzvc3exD6NpnXphqHFhiL095jWfSeb1pvGfqBgLVAn6pZX7/B4pRxR6V6Gx0eTU7qIsfYxFZcxNozjCGfhTEQYLN3W1mYHDPM+F6tbLJd6H4t1CMpD6VO6CN9Xt48MqMQHcdoh8M3+lltusaOcebNjUB45pgfybJIpiFnlQtxuiT5Zgwp9HnroITttMSsHeeWgUt3o3cjf3njktMAN8yeZOQ+z2mC/GdjRb/p3cHTA9Pf17frzimbTdFWfGdhZ+nHnUM6tTLRjsFzf0IERqxby2h2lf0Tb3bkGGfGawQOmzz9nDtQpXCESYvcqloNK9zKL1JzSBWPpY9rsoYceanp6xmdK6XjC+xu9BVdccUVm3SCv3pB6H4sxBOUofUoXjHhk4B2PFozlQFkJXB/E7Y9dGT7EmRLFt3XgUQGPUJhlQEOBb0JuLnTMRRkGF2YRK0fDI296YKxcpbrxbDdv18aRDYIB0zN7smnfUjo640OmYV67Wb72MdM5tdm0r+g2HdOaTMe2PrN5foNpmL/ObH2zx8w5oNV0ruoyc6a0mp5+Y3oXN5qPTGsz7TdtLlu10LzUYRr3aTELVpRev1+TWd5XvrKh2dFtWqctMN2rOkzzPm1mw4hrHQm/J+oWErtXsRxUupdZpOaULhgrH+NsGHxb7yxatMg+5sgiq96OsbqPjlgOlD6lC5Q+pQv2iAZBFrEyqTlQ+qpxMRqaD36+PTAVkO2PGZRHV/63vvUt+zMj98NyWbAmQaVNkULodnU9CSGxctXUjXnbDJwsp9Qg+HCjaZ3daqN99eBjgJ4ZTaaLb/GlD/GGKXxAd5vupS1m0tzSR/SaFtPCtu6l/06avXwwV2okNF7VV2oQNJl2u89TxqqFpXM1nTX4jLl3cYvpeCnjNTs3mLZ/ajCtLIn8Un4XAXXh95RF7F7FclDNvQxJzSldMBa+WbNm2W/Wewr3/vBeO3aIHimfsN4+Y3EffWI5UPqULlD6lC4oGgQBsRwofdW46AZlHjPfPOneo+ufhU+YCcAbIY0Ft6tgzAWU87clrvTIAMjxZssMh5BYuWrqxiMJGinlZK8k2DOjZXCzoldKH+In9tg3zL6XNpQ+1Ad2NQjWtpqmpb2DucdLue3ug56CGasWlhoELYt77fmHGwTha3b2m97X+k3/K5tN52GNpuOV4UsaAY9v/EWdfGL3KpaDau5lSGpO6YLR+thlMq8Hq56hIc40RXpHHbF7knofi0cG5Sh9ShcUDYKAWA6UvmpcLJnK+AF2PeTP69atG/5Qp0HABxDjDMJyWTA7gV0N3aqP1TYI3FbM/uIqLpdXrpq6wbx58+xYiJFUaBDYFQkbSx/kHabtgGbTQQ+9axDs3Go6pjaatlKuhd0O7SMD1yDIWLWwrEGQ8ZqhHgLO2bx/m9nAOIUA6sD0trz7EdbbJ5aDau+lT2pO6YLR+Fj58u677w7TewzsQnr44YcP987F7knqfSwaBOUofUoX7BENgg/q0sXAKGC+ebIYEW9+vEm4Z6U8MvjRj35kR7WzSlrMBfjobXBLHtPLUE2DgBybKi1YsCAzl0U1dXPQPZr13L1e4NqpQ1bdHKk5qOVeOlJzShek+tjX45prrglTexw8GmQgMcTuSep9pEGQtUBTrByk+lJyShcofUoXjGgQ8A3PBR8shH+smhzBmgDhsUrlUnO4mHLIs+9qy6TmCHXdwmNhGboOWV6VBgELDL3wwgvmwAMPtP9lRUFGtLN40b333ht1OR/rFsycOTPXF4afw3PzzTdn5sKopm4umA7JMq/8ud7gmrl26pBVt1i9q8kRtdzL0eaULiLFd+21144ckHpf2+AU1YrTVNeZtnnrwoOW3mVtpvOV8KhPqez0LjP4VH+rWbBfo+kc6nXqXdo62EsVsG5em8HWt3GzLVfZkQ17Y5x77rnRe5JyH1NzhNKndBFKn9JF+L667SH4IE87BOYo82iAb+jk6DFgQ5InnnjCzgL4wQ9+YLdHDsuFOB9rGbCmAd311fYQAD5/JHesXLV1czALgmemfCOqF7hWunS5dsirG6TmoNZ7Cak5pQtSfDSQ6REbxj0usgyY7ukNu8Z5MN20r98MTTg1/W7eqJua6qaaMjV159DUVjvF1JVxDJiuqS2mh8dFL3WY1gMbTfNVfMz3meVT2VKblwxNTR065+C5Npv2ae1mM8diDvdz6b9ZU1sZgMsbfd49SbmP5Ogh4H0gJFYOUn0pOaULlD6lC0b0EHjHowVjOVBWomgQGLsWADML+DAmxwqCPCpguiHP9snRexCWC3E+Gg/soljtGAI/R48E0yCzcj7V1s2HHR0ZJMYiTBMdrpHxGP5mOLG6peYg5V6m5pQuqNXHqp1ljwpKDYLmFQwCLcUrpW/y+zbb2SgDa+eYxtmdpnvFHNM0q4eRJ0NjU3pNx5SmoWmrk03bw7vGj/TM2Ns0zO+0U04nzRvZOO1d0mxnq/Rd1WLaN5bONbWr9AHfY1pL52Zqasv+c0zXqi7TfmCznb5qx7y8xvEW07VtIN+xo3SOKZQtNTT2+ZDXuNkF44fYeTHrnkCt9xHIFWMIylH6lC7YIxoEH+QxBMCuhnzouxXwWKGQdQiA2Qc0llimNSwX4nwMEuS5JG8ytTYIXn/9dXsdTLELcz7V1s3H5Vh7gb0UJuK4AjdeIFzkCaqpWxaxHIzmXmYRyyldUKuP3oEySg2ChvndpntZm2n4cKkxsH3wcM+MSab1GqaPdpu2/RtLH9JDDQIGki4cWtekb7lpLH2g7/qwHpramjWw9fF203JVr+maQY/AUI/B+jmm5Sa+0g+Y/qfW2amqc/6pcehcDIItNT5mdBiGreY66OFYPajou6oxs0EA/LvjAzyLWu8jkON89BaGxMpBqi8lp3SB0qd0wR7RIMgiViY1B0pftS4eCTCoj0WJmIb0yCOPDA/wY5Mjegv49sBjhDwX+D56GRh3AKHPJyvH9MWlS5dm5hzV1s3Hz7FHPT0YLOvKls+7G66Ba+GasjalgWrrFhLLwWjvZUgsp3RBLT7GabDjZxn+I4PSh/akw7rsGIKeWU2m86XBnoPND28w/TuHPoBf6TDNQ2tP2IWp5m7wPqzdTJaMBkGpGTBndslFj4AZ7CloOmyO7Q0wG9tM4/x1pnf7gNm60D9XVoMgcKxtNc22UcFrJuU2CFavXm3HDWVRy310pOZA6VO6QOlTuqBoEATEcqD0Veu64447bDcpA4vYs52ZB66bj+5/1ghYsmSJXbMgzwW+j8WMPve5z9XcQwA8v6VxQg9DmHNUWzefrByNHx4jUN9NmzaNyCnASWOLa+BaYKzq5ojlQOlTuqAWH1NvM/8OjBhDUPrYnvuR0jfuUpNgW+mD/4A201H6IG6c7T8yGJq2em7pG/++jXZAYO6HdUDPjL1M09CHN42JSfssMLavgQWuprSbrmVtpXNOtkttD56rzyyfMsk0XbU538FU2SkNpnl2s2n88w+Z1rXDuhGwhTm9U1nUch8d5Ogh4HFjSKwcpPpSckoXKH1KF4xoEPBiF7FNEGI5wt8godpyqTmli1D6qnXRA8BjA76VswgLOZ5f88ydD2U2c+E4Gx3lubJ8H/3oR81TTz1V5otdiwt6F/Bm5YjQVel8lXI8FsFHlzHL1HLd4wXnZgVIXGeccYZ5+umnR1zLWNctliOUPqWLqMXHAM7Rkf0hv9t5pdt0rBmcv7BhfnPuwleQtZNmrffRzxWbG5WH0qd0Eb6vbhsEH+TtjwnqzsDCVatWmauuusrm6L5+5pln7MBCni3ybPvrX/96rivLx4cdU5pCX+xa/Pj/2Tv/GLuO8vxHaqV+q/4BRS3yHxE0tCrZltJsZdFsZLXyhfzwbd3iLSZkG8c4iwXR1oC1OCQspik4KZglEGcbwDKGOmwghk0gsKYEbH64bAqhjgCxoRhtKhl1AUfdUAMbyYH5ns/c+96dO3fO3HPn3n3tXZ9HGnv3PGfOc2bu3XPemXnnfQmIxFKGf5ziaxW5XlGO4EzcOy9s8jMwO/KlL30pJwxyHNShLtfgWmIEkKQo716Ws22hoqmnqUXpRK+7lNnzZvrtw2bPsfCGxHOLBTOzb9SM7BhphOnOAxlK8afy+6uTfnQ5DAKM+xCXV4+SqpfCaWpRNPU0tSiu3opdMqARF/IuA5wAcRokFgOjYzhe5OID8PrXv96mTr322mtbnC9d+Hr4I2zdurVFz0WMY+Qs9X34WoLY9VI4DCLyybP9kjgJbF2sVqs26x2zKPQbU/4UIjtyDE7OIZHMe97zHnsNSRIF8vSAVtsEmnqaWqATPT6zCx3EXwg5FnbSj4JUDmjqaWoBTT1NLdC0ZOAcj1aMcUCzEaVBUAO7AtiHzNZDOELl4mwIWErAOMDHILaP39f70Ic+ZEf5jJB9PUHoXgRwbF+UXAoufC1Bu+ulcCCkh6WMQxmjIP5/8skn7TFB7JoxLqQFYnVSOaCpp6kFiujhLIsx8MIXvtA768IDRm9oJqxIP/qAw7jgOeAjVg+k6qVwmlpAU09TC7h6K9YguJDTHwsYGTALgGEAxzo3BgAg/zv74smMSBS3PPh6eMwfOXLE5krw9QShexHAEfaUmQkJziPwtQTtrpfCAU09TS2gqaepBdrpEWb7xS9+sfn1X/91c9lll/mn9QBzZmJDPcphVvo3jpmppXxC5x2YAQuhXT+GAFfGIWiFpp6mFnD1VqxBEEKsTioHNPU60WK7lcQbYAscSwNbt261HGGM8TH4xje+YW699damei58PeIZ/N///Z9dOye+QAihexEIx04HN4si8LUERa4XQowDmnqaWkBTT1ML5Olh8GIEX3zxxeaiiy4yv/Ebv2Fe9rKXtaQE7h5L2wItHt9j+uvbCluiGRoCCrKNsTny4eLp7BjhBf3og8uAPD+KvH6M9T8cBgHPAR+xeiBVL4XT1AKaeppawNUrDQIT54CmXidaTM3j6HbLLbfY3A5g69at1r/gmWeesaFNT58+bQP65CFPj4RH9913n3/YInQvApcj05+7JSxPq+j1fMQ4oKmnqQU09TS1QJ4eWwyf//znm0suucQ897nPNc961rOsIy0xMHqLzCC4bszM2HTX82Z2f9X072Y2sjWa4eKRYTOwrRb5cGDDAdwUzdCaqtlzaNKMXL7G9O8YN1N3SP3eA8fhcLrw/H6M9X8qBzT1NLWApp6mFnD1VuwuA/9YuzqpHEVTrxOte++910YnxBuerIccY/2fuAT8zAwBMwi8mNl94F8zpEegEwwKnANxuvPPp4TuJcR99atftQ+rPK1QHb+kchRNPU0tiqaephalnR6hsvGsf85znmNnomIGbxqyF//GITNxeDJ78V+SvcxP1Eb4gWiGxA+YIpeBIdZBxYw/kRkEGyft+cQY2GMDay7f1ka2whJrxO+rIv3oHxcO/xqeAyEurx4lVS+F09SiaOppalFcvRU7Q3Chhy4GeMHjPMj0nsRyx6kQ50LwgQ98wD4wifVOIKMQfD03lwEGRWhvf+heBD5HgBPuAfhaAr+Oi1QOaOppagFNPU0tUESPeBt9fX020yZLZiyR9Q7uksGCmdzYZ/MVhKIZTm+r5UgA01ucUMhcpR50aDkNArbE5vVlkX70AVf6ELRCU09TC7h6K9YgwKq50HcZMIrHiZAdARJZjJ0FbD8ELCcwmiIr4rve9S63agO+HgaGGAQYEQQ28hG6F4HP8TnhYMgD3NcS+HVcpHIAY4ZZCpwqGUmxG2NoaMhGdiOgDeXlL3+5PQbHOUR4pL9CXtsxPe22aeppaoE8PZJG8fn84he/sL8zKAA8B9gq2jt4PgSnDpjK2j1m9mxrNEOJfDi2s2L6myIfLr9BQO4Mvtt5fZnXj7H+h8Mg4DngI1YPpOqlcJpaQFNPUwuUBoGHGAe02xZCqA6OhCQyYh2RZQHAz0TvA/QRTlg4B+Y9MGN6zMDgh4CB4HP+vQhC3Ec+8hE7SxHT8usIOuEI1sSDjLgCbEkjRTT5GZj+JE0z/SAvExccg+MczsV4Ij4B1+BaXFOCYOXdy3K3zYemnqYWyNNjK2vIQAWk7/7c5z7nH161YFofYzbWl3n9GKuTygFNPU0toKmnqQVKg8BDjAPabQshVIffiUjI6JtROGAUxQ4BAfH24ckzUCR4CU6Abi4D/BPYwugidC+CEPf000/baV2iCYYQqiNoxxGmeWxszE6dssTBlkk3/XC34Fpck2ujwY4NZmZ8+P0oaHf/KRzQ1NPUAnl6fDdD32HAd4ygUnzXLwRgqC+XkUofh/JDxOqBVL0UTlMLaOppaoFVYRCUPgQ1MJLFE5rRMM6AgPU/2YrFDgRGvrJ04MPXc30I0JO8CC7y7gXkcTg+MYoLIa8OyOM++clP2ql/IjKSlEkLaDEbw+iMexD4/SjIu3+QygFNPU0tENJj10ysHhxRO3lRrnawswKHYhDrk1A/glgduNKHoBWaeppaoMkg4GQpMW/EGEdxPRWL1kvlNLUomnqdavFwYMT6pje9yc6YcIygQmTh4+cPfvCD1ukQfwC2Kfr1fT1GwBgWrh4Ph+9+97tt76UdR16Fhx9+uOV4rI7PfeITn7CBWJi5iH3Jlxtocw/cC/fk92Pe/feCo2jqaWpRfD221JJsh9mavHpyTZZ88AlZrSAcN0tafrv9/gj1Y5E6HGc5gudAiMurR0nVS+E0tSiaeppaFFevNAjacBRNvU61cCZkKp4ZANl+xI4DjAB+ZifC7bffbqM6ulsAO9Ej2iE+ACHOLzGONV7CrDK9W7SOcN/73vdsREbyNpxv4J6YreAe8+7fP94NRynyufklldPUovh6bKXluxOr53IYaGxFJDXwagJGP8G+8trtF78fi9RJ5SiaeppaFE09TS2Kq7dilwxCiNVJ5YCmXqdaeBnzACQ4Cy9uQGpkkvMAwgfzEgb4FohntsDXI0a8u2QAWH64/vrrG+fk3QuIcWjdddddLQGPYnXgcEpkip4X7vkK7o17JC6Ei3ZtS+GA/7kJYvVSOU0t0Au9r3zlK3YXCfE3Vjp++tOfWl8g/g58+O12kdqPLBnwHPARqwdS9VI4TS2gqaepBZqWDJzj0YoxDmg2Aq0yl0ENBCQiZ4HMEgD+mGXXAfXwC2AZgOlGgha58PV8HwIBo2AMDeBzLmIcWuQ5YPTmOofF6uAwyAhxpYD7lXwSINa2VA74n5sgVi+V09QCrp5850CsXojDkGVWiaibKxXEFGFZ6mtf+5pPWYTaLUj93FJ9CNgS+qd/+qcthbwTf/ri1uPdcC960YtajrWrk8pRNPVEy3egjfV/KgdWhUHANMeFvssA4A3M/nnW+JlSBBK2+Je//KWtR84DchowMpdZBIGvx4MgZBCwnx/fBOBzLmKcaB0+fLgxgwHy6mDUrMSHOQaaa5CF2gZSOeB/boJYvVROUwuIHjNfRb4nIMYxK8WSDn8DKwVsg8VvAqfgWNtiXOrn1o1B4BrDJdJBforSIHAQ40qDYAlkO8TTnh0XjO4F+AuwXEA99m9jDLBjwP+D7UTvhhtusF/SECeIca4WDobcDwjV4T55sfqYvXPADB4MJbM5akZ3Nm9rnLtn1Ew84R7JzqlnsFtzacUM75sxbTepLUyb4bX9ZviuO8zoPXPGPDFR+78NuHfaEGqbIJUDnXxuglROUwuIHrs4mAkUxOq147gmMTl4yfozi+cT+DtmlontxOwOAu3alsdpf26lQdA7lAaBhxiHVrlkUAOZCfGqhmOd/6mnnrLH3/3ud9tRPfXYccCUP17arEW68PWYHQjNEABypGNYhDhBjHO1uLddu3bZn/06OEQyMmrB2aNmZNOwGb5yzLifPJnlFs5MLUWDq2eYm21EihM0R4yb2dVnRkhQs7BgFs8s1DLYNWWzy34+NGiq++etU449Rjz7vXWD4Ew8kx1tYEYmrz/8druIccD/3ASxeqmcphZYTr3vfOc71qeGXAh5cTHOBdgphBHPchqOwC6Kts1Haj8Sy8EPRiZcXj1QGgS9Q2kQeIhxmlpAUy9Fi2h6GAK8YMXx7v7777cvI+qxlrp161Z7fOfOnU1x3329PB8C8IMf/KCR8MjnBDHO18JIkaRMUgcHqrz87uahITN436KZ3V0xY9+pHZq7s2oG906ZyZ0D5iJe9meyEf2GETN5eNwMrumPGASLZnp7v73O9JZnm4GdY+bAka972ezmzYldA2Zg11Ez+626IVA3CBaPjJjK9lqWu+o2QtaGQbCcJ5980j9sEeurGAf8vhTE6qVymloAv4E9e/ZYI8xFrF6nHAMKvn9XX321XXLDUNAG98BWYL7vzPL5/j2C0P0LYlzq51YuGZx7nDODgIe8lO9///u2uMeKcBSiZvnH2tVL5TS1KJp6KVoEJeIhivMduw04xvorMQqkHkYD/gY8aIlNkKeHZzYPxzw9DAL2eoc4Sl49iq/FCA3PfEZHUoecAuRgaMWimdzUb4YPTpmpOwdN302M7khTu6c+W1B/2WdGw9ADtRpLseQF2Tm/VzHD24dtGXugtvQwvaVaS1ATyGZnr0euKJkZqP8/vaWvdi+Hp8zoOpLaNESaQFtok98X7foqxlH8vixSL5XT1KIwUubl4h+P1UvlaBsJkvheX3XVVXZZAR8XwoL3GszmsX2SQF8YIsxSkIys3T2mcqmfGwGe2JEU4vLqUTBuWO4o0T2uvPJKMzMzU7j/UzmK+z0pZwhMnAOaeila7B7gZc90PtPtAOvSHc0Tr4ClAwwH4vsLOtXjgUZglBAH8uqBkBYPRLIyUof4BBgkQZyaMJXrJu1sx/z8rBlfT5a5zCDYNGZs/rmz9SWDI8N2FgHM3hEwCAJJZkhha48GstnlGgTbqmbiJPcyb04cnzELkS3vjABpm49YX8U4EOpLEKuXymlqAfEt8RGrl8r5bePBydIYvgbkssAZkRf4/v37beIwjOUf/ehHLbMXAI3Tp0/bfCJEBWWbIA69kluDpT1mJULti91jKue3TRCrk8qBcoagdzhnMwTO8WjFGAc0v3xolaGLl4Bl/pnPfMY+hMiAJuBBxBQ89Zgx4AHFC4yHnaBTPRIBsYMhZRo8pMXnyEiJhygGjaRx9jG/r2pGji39vnjfoKnsmzcLDw2bypYxM3bTgFnDy/5sZixsqpjR3aOmemmlM4OAXHV+Nrscg0Cy3I3vHTIVm+UuH+J74SPWVzEOhPoSxOqlctpa2nohSB126uCrxN8XO17e+ta32ul0cocwu+VmzeTvjb8tDAiSME1NTdkoi25ujXZ6IaRymlqgNAh6h9Ig8BDj0Cp3GSyBWQECFPFSffOb39w4zjZERjTU48Ekf6w4VUmuA18v5kMgIGQvywYhxOr5WoIHH3zQbmnkQet6la8W0KZO12RjHMjry1i9VE5L69Of/rSdydLSA5paQFMvVav0ITj3KA0CDzGuNAiawTo1uwrwDmZroIC0vWTqox7rl4zEAed+6Utfsj/7ekUMArZDpSQq8rUE1CGb4BVXXOFTqwY4F/qI9VWMA7G+zKuXymloMVNEYC220WroCTS1gKZeqlZpEJx7lAaBhxhXGgTNoB9kZoB1SglPTEIjHAilHnv/uT7Rz1i3B74ekQTbGQRwXMvf9ilcXj1fS0AdfCB+//d/36dWDVhD9hHrqxgHYn2ZVy+V09QCmnqaWkBTL1WLFxHPAR+xeqA0CHqHc2YQcLKUWBKEGEfRTMiAFqF4+dIWrZPKUbTb5h9rV4fsZKRB5mci5PGi5mdS9bLzQOrhTIhjFFEN2eaUqsdx1sQnJiaCXF69dlr8EaxW0La8dvvH23GUdn3pH++GW24tHPkInoV/Cr8vt55bNLUomnqaWhQchEuDoDe45pprGoHlivR/Kkdxvycr1iDwj7Wrk8pRNPVStHiQiic7a7A4F3KcrVNsSZR6ODkRQY+fifGOUeXrYSj46Y/9wnHWxXGikoe4y+XV87X8OrnxBwxRB6tm9GH/aA2zd1ZN3+V7zIxPdINTM+bEfCjaYRpoW167/ePtOEq7vvSPd8Mtt9Ydd9xhnV7l9+XWc4umFkVTL1WLAYYMGHwurx6lNAh6h3NmELg3wYG8qYUYB1Knp1I4TS2gqZeqhXczMyb33nuv3UctwCeArVCA/aZ4QAP+cNn+5OsV8SEQjgd5J1HVfC2B1MGDW8K1+pjb21fz9j+zYL/cC0QIZKvfmRNmbOOoObpQ2wa2uMBWQIkemJ2XHV88Xa9zxolCSJTB00tbxxr17PbBRXPi1kEz9uhiTa++pZCIiEt16temXl07D7SJtvmI9VWMA+36MoRUbrm12PfuYrn1XGhqAU29VK3Sh+Dc45wtGTjHoxVjHEj98qVwmlpAUy9VCx8CXvg4GOJMKCB8rux7ZgaBGOm/+tWvbAAjiq/HroSiBgGBM/wHQKyeryWQOsePH7cxFUIQg2Bub8X0bdljJg+NmIENB8x/Pz5phtYNmYlH583ikWEzsK0WPRBunm2Gzx4wo7sPmOmZcVO5dNiME8Hw0kvM4K1Z+2/qtzELFh8YMgM7JrN6Y6aymXrzZuq6ATN0aNY8Xg9uRETE6u7snL2Dpn8ncxHZtddk175nyoxv6jOjze+0JrAXnbb5iPVVjAPt+jKEVE5TC2jqaWoBTb1ULV5EociNsXqgNAh6h9Ig8BDj0CpzGSwBTnYOYBTceuutDY690zgXCtgdcPLkSTtq5UWVqiccvgvuNWL1imiFvPHBkkFQzUbu9ogZ3zKe/Sv/1+IJTNVH8zM7Kmb8ieylvXGyNltwctxU60GHGudJTIHFBXPi4Skzdc+I6V9fu5ZEOaz970ZEnDcHrhzOzAHn2hKrIAd/9Vd/5R+yiPVVjANF+tJHKrdcWgS5kgyaLpZLLwRNLaCpp6kFSoOgdygNAg8xDi3WPcpdBjXAEXSIdVgCBvGSFrCjwJ0xwBGQBzE7EQiu4uvhg1B0hgCge+jQoSDnw9cSuHXIv+CmvBUsGQQSfTBgEGwjemHt/OkthBN2AhE5SYkagYjqx2Z2VszosTmzcGbW7LkuxyCQiIjorR8xM26Qo4hBwFY6HOZCiPVVjANF+tJHKrccWnzHCOlM7Awfy6GXx2lqAU29VC1eRDwHfMTqgdIg6B1Kg8BDjCsNgmbAkSRI0gXzosfZD9BHklUQYAxgFABmC77yla80ONCJDwHA8cWNfBirV7RtZHyTJE2CIgaBRA8cy17w/TZ6YDGDQJYDJnYOmf7LeNkbM3+wavo2HDCfqes1IiJu6TeVO5kraG8Q0AaiOub1h99uFzEOFO1LF6mcphbQ1NPUApp6qVqlD8G5xzkzCDhZSswbMcZReEH7x9rVS+XQ4kXE9HjROqkcRbtt/rF2dTjOej5LAPxOchgyGvIzDoUYCHIuyyzw/IxhQFhd91psSyyyy8Dl8FNgjTzEuaVo29iGFttxsFJAGzAK8vrDb3dRjlK0L3vB9Vprbm7O5tTwj0vptV6M09SiaOqlarHLgOdAiMurR2m7y2BhxkxsyYz1dQNmYMOomeoqKOlRM7rzqH/QQ3bOxWvMGsqlFTO8b8aGGD+6c9S0q5mEM1Nm+LIxM9PIaxLWj+2YEpyzXQZFK8Y4/6JF66VymloUTb1ULV6iBAvidzIaMvIXjiUEfC74HcdCRq1ELnz44YdtnAL/ekX0XI5oiLfffnuQc0snbWMmg62RKxXcO20ItS3W7iIcpZO+7JbrtRb+LpKaO1R6rRfjNLUomnqaWpS4QTBnxq8cMlO1DU+ZcTBlhi4Xvxx/Bw8HFmx4dXcDj+zoWVxgF5EzQ3e2vnuoZbNPc+6SmV19ZuS4M0NYr9e0S8i/lv+7CdxrHeRbGdo22Eiulqff2DEVwTkzCNyb4EDe1EKMA6nTUymcphbQ1EvVIokRKUsB6/rEHBAOz/1vfOMbjfPZesjsCnEKJJyxgBmDTpYM5HdiEpBsyudcdNo2EspgvIQyBZ6v4F5Z8uDeQV7bQCoHOu1LkMr1UotkQWyNDXGCXuqBGFdUi595QTHzxt8SmUPZLknwL9J3kzoW/x2/no+iei5SuVQtpqp5DviI1QP4y+QaBJI9NICWHTxnps3wumEzcXjSjKyrmgOnjN1BVGUn0N5h0/dsXuj1l2127sj6+rkbhs10U5Yx94WcmRDb+83Yd8QgmDV71g2a8cNTZmLLQG2XkKvLtX7Ueu3ZOwbNyKEpM7ljwFQPujnPM4Nn45g5cWbSDG6uOxvn6BcxCM7ZkoFzPFoxxoHUL18Kp6kFNPW60br++uutYfBv//ZvdneBcPxxf/KTn2ycTypXEskAlhNI5yro1IdAgB5GSIgTpLSNmQ6m3l2D5nwF98i9fu1rX2sci7UtlQMpfZnKaWoBTT1fi+12zF6wDMYWXXaIUDDyWFfHHwcDmxkxHO/4n5cguThuvPFGm/mQMNXsllcmK+oAAIAASURBVCEoGLN17PJhFBbSE8TuMZVL1UrxISD2yfOe9zyzbt06n6oBf5076vMBD4/aafRn/zov5sAOHnxyJHfa8RFTuXPO2UGUnbPeMQiyc/u2HzBT2Yt9ateAqex3X9LZOb9XMcPbh20Ze6DGyQzBwhMzZjp72Y9vuqT2gnZSp5sz2ag6cG18i/o3jJmJIyeaU55/Zyw7vic7d9KMrh0wE3Y5JKxfGgQ59VI5tMr0x0sQbufOnTY5DCMWCYQDx9IAGQoFR48ebXjys0XRdSwkc2KKQcCMg0Q38zlBatsIuESOhpDn8/kC8txzj/hfuGjXthQOpPZlCtcrLb53LBeEOBe90hPEOHbh8L0ifTHGHPE8+BuQLKHdAOOcGQSc7dhRgWHBjNf73/9+u/XXReweU7nUfuRFRB/4CNUjBwl/93yuf/Znf2YNoyDOHjUja7MRtPMSnbqubhD4O3jcF3P2UuZFvLSDKDvnRY5BwMzBvjk7ezP/6IyZkSUJi+Yp+8ZRDIL5A2Zw0wEzSzCyB4ZaDIL5R4+a2anWay+ezH4/s2Bmj4yY/m1L1z56U8XseZTgZll5YNj07/Ycjx2UBkFOvVQOLSzucpdBDcIROZA/UF6gpDgWDsc2NzshGvgbAB5WJEDyUUTPBxqkWQ5xoJu2AQIp8dBme+X5Au6Fe5IZGR9F2+YjxoFu+9JHjOuV1mtf+1prrIY4F73SE/gcBjCOtYzi8aH56le/6py9vGAgw+zcP/zDP1j9t7/97Y3cI0Xv30WMW85+ZLmRJQKMAZ45DDqYUcldMsiw8AhBwfpNNRstV9eusTuBGDO37OA5O5sZCRUzunvUVNbWlwEeHzfVdYNmeHPFrHl2PQ4IL1s5d++4GVofWzJwjmIQyPLAoT1meEOfGWRmwdXdNG5mF1uvbWcINo+b8R0DZlCWDDB2NkzYttQwa8bWjpijZ8P6pUGQUy+VKw2CZgjHUoCsXePUxnHKz3/+czvl6QLfARwLCWbkWvWMmFJmCMCDDz5o3ve+9wU50E3bXDAa4SWMr8S5ArEFuAfuBfSqbYIYBzT1eqWF93oe56JXegI4ZhOZFeMzIxiSv631XAFjhNmDv/3bv7W+FSG0a1sel9qPvIh4DviQeiwPMKPihkhniYVZyZhB0A3mDo+baabhz86Y0U31bcarGOfMIOBkKTFvxBhHcT0Vi9ZL5dBiapzRaNE6qRxFu23+sXZ1hGO0iqcvx5gBYFpSuJtuusk6PkkdRkZMZ6KHZf/UU0/Z4xgKnW47lMLMBIaHXMsv3bTNP04hhTMPeEZ7bGNbbqCBFprMqrj30uu2xTiKpl63WowmMQZCXKh0q+eWH/7wh3bdnxdu6CV3vgBnS5YSmDW46667CrWtHZfaj3xWxBbxOYwXnivvete77EtEjn/72982r3vd69rsMugSbFncNWJGbhrrcrviysA522VQtGKM8y9atF4qh1aZ/riVY7TPeijHmIokNoBwLCfASx2WClgnRA8/AowDjuNrkGoQUNBhlsI/Tummbf5xl3vsscdse3mYYtAQoAljsVtwDa7FNWV6l7wQ/n1Qlqtt/nEpmnrdahEp88Mf/nCQC5Vu9aTg18FI1g++db6DmQKMTiKA5rUt1m4pqf2IQcBzQI7x8scIwBjAKPDr8Pnef//9y2sQXGA4ZwaBexMcyJtaiHEgdXoqhdPUApp63WixJUqiErKzAO9m4Xj5swYvYNsUxgN6PIB8J6IieiHgYY9jVgjdtC2EEMfUMAYJQZpw4sLjm9Eha7Z4fLPWz3Q/7aXwM8fgOAcvcabrcP7iGlxLnFdDegKNtrnQ1OtGi1kpjER+97k8dKMH2GXD5+jurFmJYDmKpT98DIq020e3/Qjc5QGfAyxHygxjGamwdzhnSwbO8WjFGAd68eVzEeM0tYCmXjdavLhe/epX22MEC2IKUjhe1K6XPt6wOB6i9+ijj5rbbrvNHsdBKNWHAMAx3Ug0Oh/dtC2EGAdEj+lYfmZbIBHYGM1gHH384x83DzzwgD0GxzlY0nnXjOmdq7b5iNVL5TS1QDd6LIW5DrQrHRhUGKcHDx70KYtYn6T2o8QhwGEQx0GWfITz62Ewv/e977U/lwZB71AaBB5inKYW0NTrVosRBT8z3c1+aeGIVIgfgYutW7dax0ycCyWoUWocAgEco26miH102zYfMQ5o6mlqAU29VC2WqEKj9Fg9kKJHUCBGqvfdd59PrQrwtxyaeYv1SUo/UoeZsRe96EV2cOAiVA/jCx8CUBoEvUNpEHiIcWiV6Y+X4HI49/BFwqpnm5fLYSy4Uf9YY5XUyIwGGI3gKdytQcCalMxUuOi2bT5iHNDU09QCmnqpWnjPM/vkI1YPdKrH950lgpUQvKobsKvGzWQK8voEdNqPLAvQjzjMShwTF349BhPuDqXSIOgdSoPAQ4xDi5dOue2wBpfjD1L6halGsh4Kxx8vOQ8EjKbwaAY4z5ExUVBUz4dwjGjwU3DRbdt8xDigqaepBTT1UrWOHTvmH7aI1QOd6LFMxksM/5kLAewkco2CUJ8IivajBBdieYA6edfz6+F46D4zSoOgdygNAg8xrjQImuFyePx+8YtftD/jFcxavnDECBAOMKISC1+iF3J+tzMEcLwMCOvqotu2+YhxQFNPUwto6nWqRYS+PA7EONCJHkYvu0wuJLAcx84gEOoTQbt+9IMLCceLKOQD5GqxE4kZR3xuBKVB0DucM4OAaWIpjB4p7rEiHIXQtf6xdvVSObQwBnihFa2TylG02+Yfa1fH5XjpU/iZ9T2c5oRjFoDtQ1KPLXSMrvgZp0P8CPidsK1F9fwiHIVrsZ1RuG7b5pcYR9HU09SiaOp1qsWuAkqIi9WTUlSPJQkcQ7vC4rQZ2XzAiTI3byavGzHTrcnszPwjJ5zzzi3e9KY32b9nv0+K9iMzg1deeaW5++67WziSNvEsCNUTLVKnsw3X5bnW2NiYf6slEsBnQ0r7vP73SypHcb8nK3aGIIRYnVQOaOp1q/WFL3zBzhIAIhdiEAj3zW9+s7GbQED8fbFEmYokIUsvZggAa5FuNLNu2+YjxgH04JlOZsaC3QUEM2I7Fy+sd77znXbak2NwnMMfB1upQojpnYu2hRCrl8p1okXAsO3bt9sRpM8JQvVcFNEjSBTbRHuBozdV68loMpyaMNWbjtofm9LcLp4wY5vJZlf/lVS8gRS4jXS59fPs7yTKOe0c6xGIVcB2v7y+DPUjMwEYUgwMZPeAC/qY50G75EY4KPvLNOUMQe9wzmYInOPRijEOhL58IFYvldPUApp63Woxc4KXMGBLEFsPhfvJT35iM7K5YOpRYrnzgpSfi+r5cDnCw/LwEXTbNh8+x+8sfbBUQcTEq666yv4/Ojpqt0ax1ZDtmASqYRRE4WeOwXEO5zINTfwC6nItrsm1fT0Xy902H5p6nWqxzTOPAzEOFNEjtgQvw57g2Iip7pNMdIO1FLV+St5TU2Zo3ZCZfHzRzO0bNIN1rrrXdWyeM+Mbqmbs0JQZ39xvRo+bWtrfS4fMnkOTZuTyqjnQwykG4i2wzTKvL91+dJcHCFiWVyf22QjHrJ8sWbgoDYLeoTQIPMQ4TS2gqdetFl8i2V6IhzfOfW49thWyxitgdCx7nNlxwFRgr2YIAFObssbbbdt8wOFLwj0TPIWXBA89lj/y6nQCrsG1uCbXZgmE2RfC4fpYjrblcUBTr6gWLwpmoUKcixgH2ulhoPG97h1mzZiNj5/9f+VY9m8gJS/HtnBO9v+6qtlDStzDE2boRSNmRi7jpvidP2AqZMPLjlVvrefy2ztkxpuTHHYNZgnYNhyC9KOfeyDW/3ALCwv2GeBD6jG75voiCUqDoHcoDQIPMQ6tMv3xElyO/3H2Aby42Hro1sOyx0dAwChD1v0ef/xx09fX11ODgFkKfBpAt21zgbc1DzmWODBkfvWrX/mn9BxooHXDDTfYWQQ362Iv2wZiHNDUK6rF7AoGVIhzEeNAOz1muQjr2kvYmYH9Y2bIzhQEUvK6BsHGETNNmtv5OTNzfM40Fg6eGDeD9Ze/nRnYMVMzEvbWHPSWwyAgyigv4hD4rvrBhUCs/+FiSwaENb7++uuDf2+lQdA7lAaBhxiHFiPDcpdBDT5HDACMpWeeecaObF2OJYTp6aWUnBgB1157rf2ZP/LnPe95NsiLf00XnXCMXnh5YmT0om3MBpBXgJF6aBSjBbS5B+6Fe+pF21zEOKCpV0QLb3M3+2TsejEOtNMjJHXPcWrCVH590EzW1/lbUvIyU7Chz1T3n6hz42Z8Z8UMWk6wYKa3V8zQ7sywWFsx44+bZTcIGBgRkMmFLA/gE+QHFwKx/oeLGQQEHcuLmlgaBL3DOTMIOFlKLAlCjKPwgvaPtauXyqFVZjvM59hKiHMcPxOe2L0mudh5gcnvcCwx4IXK76QxZanBv2ZMrx2HEx+63bSNlw0vAhK+nG/gnkhGwj3m3b9/vBuO0k1f+sfbce20ZJthiPOPt+MoMT2i4rnBcErUXh7SR8QWYWmL/2P9mNf/7Thmx8ii6nOUMrlR73DOkhsVrRjj/IsWrZfKoVUaBPmcm9mQ5QCc54Sjz26++ebG7+gxmvjc5z5nf8f6/+hHP9pyzZheO45RCtPJKW1jeYMZBoyK8x3cI/f63e9+t1DbUjlKSl+mcu20iHvvGpntrhfjKDE9DEu225VYwitf+Uq7pi+7BxjxtevHvP6Pcexg4oXvH5dSGgS9wzkzCNyb4EDe1EKMA+2m+UJI5dAqfQiW4HO81B988EH7Mw9qNw88ozkcCwXosc6PcyHgi4gR4V/TRQrHLARbxULIq8OImyUPRiQrBdwrvg0yfZ7XNpDKgV58T1zEuJgWD5PNmzf37G8RxPTwR/Ezc17oYEcMCcX85YFYP+b1P1zekgHhjBlo5IElA+6jpdwYONYlx7Kof6xdnWRuWFcPrXO2ZOAcj1aMcSD1y5fCaWoBTb1eaPGCx1oHJJchHoELnLLYggjQw4+A3QCA0QbFv6aLFI4AMoxeQgjVYeliJY82uHfaEGqbIJUDvfieuIhx7bQIaOIjdr0YB3w9/DVwkKtUKuaP//iP7Tp2iSXgK4Dfjw+/HwWx/ocLGQQYfujk1QM8d1hu9AsBdpil9I93w2lqUTT1RKs0CBzEOE0toKnXCy1GCiwbAPIJEFHMBcGJZHsYetRlqhsnxB//+Mc2QRIxBIrqucjjSLeMRgh+HQLbiEGzkkEbsPhD/QH8druIcaAX3xMXMS5Pi0ihP/rRj/zDFrHrxTggeji/ElPjD/7gD8xv/dZvmYsuusg6wLppvItjzkxsWGPWXFwr/RvHzJQEI2qLBTPzyJx/sHc4NWNOdBGfgK2HIeR9brH+h+NFxHPABaN/fGXy6oFUvRROUwto6mlqgdIg8BDjgKZeL7QYse3cudP+zBQ2OQ1csKRABEMgejhqSeQxWWYoqucixuFHEFo2cOuwTZIgQasF+GP4oy1BrK9iHOjF98RFjMvTwheFdeUQYteLcUD0cIhl18tzn/tcawz89m//tp1pklTdnUG2Ddbx+B7TT5wACy+6IEfcKIVnJs3g5kmzwK/1SITz9he4BbN4tl6nfmxxITuWHbfXC52/yDGpt2hO3Dpoxh4NRD0sCJYMQsj73GL9H+IYKBDRlAyzPueiV3qCGKepBTT1NLXAqjAIyvTHS/A5/ATYnw/wtSDingsckPzYADhqMSLDkCCDGQ5yRfVcxDj27f/TP/2Tf7hRB241Ooz9y7/8S7TdIcQ40IvviYsYF9Lib49lkbw6sevFOODqsVuGiJMYBn/4h39op67Z6tk5CDY0ZmZs/IB5M7u/avp38/yYMxObBuvRBat2q+DsHYNmJPt9cseAqR6cN4uZ8TCwbtScODVr9qwbNOOHp8zElgEz+oi7lXDJ4Jje8mwzsHPMHDjy1cD5FdO3ZY+ZPDRiBjaQQ2HeTF03YIYOzS7FM+gA5CPZtWuXf9gi9LmBWP/DMUMgAwrw8MMP2yimsXogVS+F09QCmnqaWqDJIOBkKTFvxBhHcT0Vi9ZL5dAqdxnEOfwACO3Kcdb+CFIkHF7wb3jDG5r0MAaI63/dddfZhzCjM/+aMb0iHFo4NIa8Z5mxIIjKagVtw9HTb3deX8U4Sq++J0U4X0vOi9VJ5SiiR8Q8vqf4waxfv978+Z//ua0n21g7AwGFhszE4cnsxX9JZgycqL2AT45nL+Y9ZorIg/uGTN+OGTN/MDMWNoyZiSMnzIIdxU+boS212YSFJ2bMNNfYdIkZeijPIKguxTNoOb9qxh5tPr+b+AQYA4Qb9/vQ7Ue/xPqf4wQfYqlLjqHBmnesHiVVL4XT1KJo6mlqUVy9FTtDQCPKwEQ1hDgSv3AtOOKdf+tb32pwTz/9dCOaoehhYPHSYpsfTlzMMISSn4CQniDGocUWNfZIu+B+loLNHDWjO2vJZSweHjXVe3hsxuDVcbEwY2baPmyz+vW15TWXVszwvhmz4J/iY2HaDK/tN8N33WFGs/ubu2fUTDzhn9QM2uhGeIv1VYwDvfqeCGKcr4WXPy/kWJ1UDrh64vzKDAtLL9TDUGD3SWdwlwwWzOTGvtqL+YlxU71p2s4azJ/ku5I9PE/OmfkzC2b2yEh9WaFuEMwfMIObDphZpvsfGGoYBHsIQGRmzZ7rxCAYMtZ8yDnfNyBSDQKScBFzIK8v/c9NEOt/OGYIJJopDsey5BirB1L1UjhNLaCpp6kFmmYInOPRijEOaDYCrXLJYAkhjgcojoNwRNQjRLELtgHSh64e28d+8Ytf2J/37NkTXO8HIT1BjEML/wb8BFzg5CjbJN3RmMVD2aiNSG/1rHGhtd6FM1NLdRYX7MNdzlm8b9AMHlqojQYlE13L3Gyz5syuPjNyPLwWXLtu9vOhQVPdP28tb441PdTPcA91TQe00XXwjPVVjAO9+p4IYpyrhbMZzqH0caxOKgfQI9EULyMXbr3m70wReD4Epw6Yyto9ZvZsPbrg3nEzun7QLhnYGYLN42Z8x4AZPIi334wZvXTAjE591AyvGzYTh/aY4Q19ZjD7/M3xUdO/bsgMX1c1l6z3DIIz0y3nhwwC9Po2HKiHSi4OdrGwvJfXl734jrDcNTU1FeR89ELPRYzT1AKaeppaYFUYBCHE6qRyQFOvV1qsxWMEwJHUxA0rC/DUJu68q8cDhilaZgioQ/riEEJ6ghgnWhINEXB+85pwjkFwctz0Xzxos8aNXTloDpwyNiPd4N4pM7lzwFxEHQyDdSNm8nD9nPlFM3vHgBnYdSIb8U2bkfXDdsp4ZMOwmW6aAnA1F7MXRL/NeLe0Fvx1L4vdvDmxi+seNbPfqoWmlQf9YjaqrGyfMFOHRkw1G136Mw20lTaDWF/FONCr74kgxvla4kgYq5PKgb179zZ2ybjw6+Fdz+zShQgyFsq0fl5f+p+bIFYHjhkCng/MQBAWWbJKxuqBVL0UTlMLaOppaoHSIPAQ44CmXq+0cOAj8Qkca4z+Vi08+Zn6dfXYWvQXf/EX1iBgOYZdASGE9AQxTrSIikh0RIAm97mEfIOgkUkuO1bZ/3UnI53UyUbrjx21WehGLqvURmLZuUzV2utsP1BbK85e5hVGdw1k9X+vYoa3D9sy9kCNa6wFh7LYyXXrserFIJje0meGD5IJb8qMrqu0pLt1k9HE+irGgV59TwQxTrTwPXERq5PKAXxMQrxfj9TV4jx7IYH8IBhD7ZKQpX5HJA4B++GZqXG5vHogVS+F09QCmnqaWqA0CDzEOKCp1ystIorhJAhHngJG/i4YYUiYUwF1mM7nQcNUOBH3QiOwkJ4gxokWyxIy8kCDOktgbX5panfxvqqp3rfYlEbWPDCUHXt8KSPd2fqSwSOjppKN2udOMzNQn5qVF/eRYVPdN1dbK350xsycrgtYeEaIHJWp31AWuzyDYFvVTJysebKfOD5Td0pbAm2lzfJzXl/FONCr74kgxokWKYc///nPN47H6qRwXJvvZSdtI+gWGTwvJLBkQwwIEOoTQSf9KIDDICDF9Bvf+EabO8Ll8uqBVL0UTlMLaOppaoEmg4CTpcS8EWMcxfVULFovlUPrf//3f+16ZtE6qRxFu23+sXZ1Qtxjjz1m3vzmN9vjOGHhH+DyrOXjR+Dqzc3N2aBAck28i8UPoZ1eEc7VwlhhbTK0hWz2zorpWz+Ujdarpv/KPeZEfZReuTgbxe8eNYPrR83MGScj3U0DZg0vdIyGDWNm8p5RM7S23/oBYCT0XT5qpk/NZgZExYzuzV7g62NLBs5RMQhCWexyDALz+Li9v/Hs98r21iUDIMsGsb6KcX5fFq2XyqHFC4JolkXrdMphCLDDhVgYnbYNw1ZSeK92sIWYHUHt+oTSaT+6HEt6shPJ5/w6UrrR84+34zS1KJp6mloUV2/FGgTltsM4h8Mga4zCbd261e4acM9hpOpnLmNrF1O21CHSHjsCiugV4dy2YWhwT+5+5yicNLIrHbSZ6fdYX8U4vy+L1kvlRMvPahirk8LxEuL/lLYRV4MIm6sVbBtmRwH+G0X7JKUfOc62QwYQhIv2ubx6lFS9FE5Ti6Kpp6lFcfVW7JIBjSi3HdYQ4n75y182pvzh2HooW4kETAniX+DiT/7kT+xojWuGwh6DkJ4gxvltY7TDLEYhzE+bA0e6iO96HoHZEXZwxPoqxgG/LwWxeqkczqXu1LEgVqcox4uOF4+L1LYRy4LvvETcXC0gaijbLAkn7iPWJ6n9iLH6ghe8wL5EfC6vHkjVS+E0tYCmnqYWaFoycI5HK8Y4oNmI0iBoRh5HcCEeuHBsPfTDzPJiIhSsC/YckzyGa7IH3M2MKMjTAzHObxvTvHmOi6sZ73//+xvppvP6KsYBvy8FsXopHMtNOLCF9PLqgCIcznEYpTivuQhpgSLXZHaLGBt33323f8qKA+2hf5hRatfuEFL7Eb8MZhd9xOqBVL0UTlMLaOppaoFVYRCU6Y+XkMfhbIXRBMc2wmZv/lomxPe85z1Nx4hIdvvttzeuSYAjjC8XeXogxvltY5SMUXKhYSXNELBMIDtCfOTVAUU5f3YA9KJtvNQYVftG8EoBO1Hk/v22uYhxqf2IY3FoliVWD6TqpXCaWkBTT1MLrAqDIIRYnVQOaOr1UouXPXkL4FgGYH+3CzyV/RE6RgNbueSajLQYzbrI0wMxzm8bux9IqlQEc/dUzejD/tEaZu+smr7L95gZn+gG9Qx0RSIQdgraTNtjfRXjgN+Xgli9Tjn8S8ByaOXFuAC90mPQwFIZXvnHjh1r4s5XkFGQGZkPfOADjWOhtgliXEo/sky4Y8cOu0zhI1YPpOilcppaQFNPUwuUBoGHGAc09XqpxcOFWANwPNz9ly+pa/3McSwRYBDIUgOJTSQRkiBPD8S4UNtCuwxCmNvbV/Pq97PFnTlhxjaOmqONTHNs+ZMogeywWDSLp+t13EiHRBOUbHZuPbtN0MlA52azczPgybWp1xr6MAppc6yvYhwI9SWI1euUY6aI6fxea7FkEgo8JOi1Ht/lt771rdYpj9DZZ896e0HPMbg/+prvRchQirUtxqX04zve8Q5z//33BzN0xuqBFL1UTlMLaOppaoHSIPAQ44CmXi+1GNkzwocjtgApTH0wLclISoCvAfvNCfpCPTyO2Z7oIk8PxLhQ21rjEIQhBoGfLe6/H580Q+uGzMSj82bxyLAZ2FaLEljLJDdthp49YEZ3HzDTM+OmcumwGT88bgYvvcQM3jppJm/qN4P3Ldo48wM7JrN6Y6ayuTkD3eP17YRERazuzs7ZO2j6dzIXkV17TXbte6bM+KY+m8muCGjrSohD8MgjjzR2gPRai3gXofgWgl7ruRwzYMwY3HDDDdYJsch3bzlAnApZ1ti6dav9Wy1y/z5iXKf9yC4knhEYKDwHfOTVE3SqB1I5TS2gqaepBZoMAvajS2Eak+IeK8JRyJDnH2tXL5VDi7VxpryL1knlKNpt84+1q5PHsT7LXmLheBCyVdM9B+chQhy7x+666y6bUc6txzbBdnrtuFDbWMbAibEdlgwCP1ucm2VuyEzVB38zOypm/Inspb1xspHVTgIbNc6TrYyLC+bEw1Nm6p4R01+PRy9xBWr/kzpXoiLOmwNXDmfmgHNtiUlQALSVNrfrqxiX15ft6nXKESiI/3uldfDgQTtTFeLc0iu9GIevDLMUGzZsMNVq1e6mwa9Dcnn0GsRMISEUs3RXX321nZkjvDj5GvLuMXb/RbhO+5ElRiKahrhYPSmd6nXDaWpRNPU0tSiu3oqdISh3GSwhj8PjemRkpMGxxc9PCEUSI9dH4D/+4z+svwFTrFKP6UzyHgjy9ECMC7WN84ssGywZBH5yGMcg2DbYSDs7vYWwwU7AISeOQSPgUP3YzM6KGT02ZxbOLGWsazEIJCoieutHzIwbzKgDg2Al5DJgmcCdNeqFFt8fRuVcy+d89ELPRYwTLdbOMYJJG46BwE4F/A9w7sMwYtslI+iQwUAGS3ZMMJuG4cz6+8TEhH35s0xBlksMb5YDWIILIXaPqVyn/UiadAw2IhXyHPCRV0/QqR5I5TS1gKaephZomiFwjkcrxjig2YjSIGhGHkdykmuvvbbBEZfcd65i1OauW+JDwAOBqHFSj4iCnCfI0wMxLq9tRTLXFTEIJErgWPaC77dRAosZBLIcMLFzyPRfxst+KQPdZ+p6jaiIW/pN5U6Mqs4NgpWQ7ZDvDD4k4lAIeqHFSFhiGficj17ouYhxeVrsrmA2jVE9Gf92795tl86YUmeanxc9hZ+Jp0GfkT6c7zIOgRgRjMp85OnF7jGV60SLJUIypMJJLgMfoXouOtETpHKaWkBTT1MLrAqDoEx/vIQYx4tdtmcSc4CkRi54cLlhX5kZwN+AEMYYXYAppZtvvrlxTkwvxuW1jfVkRlGrHbSR0aQg1lcxDuT1ZaxeEQ4j23ds60aLusSzCHF56EYvhBinqQU09TrRwm9IApJhEPAc8BGq56ITPUEqp6kFNPU0tYCrt2INghBidVI5oKnXay1GLbKnmDVSP+4A0QtDGePe+c532i2LAK9s/AhEI6YX42Jtw9+Be12toG1+AJ5YX8U4EOvLvHrtOGYHQkjVOn36tN3CduTIkRYurx5I1UvhNLWApl5RLQx/CUTkcy5iHCiq5yKV09QCmnqaWqA0CDzEOKCp12stpqhl/Z/QpyQscoHeq1/9avPkk0/a30lwxAwB6ZPdkeItt9xSaMo3xrVrG1OWOFqtNjDlTNt8xPoqxoF2fRlCO44kQTiY+kjVYqTJtlcfsXogVS+F09QCmnpFtfCTIGeJcHxuPAd8+PV8FNVzkcppagFNPU0t4Oqt2ORG/rF2dVI5iqZer7V4Gcn2KkZsrIG6PHosGXz961+3v7PEQCZJnKMwHuQ8Eh3xcG+nF+OKtI2lio9+9KPO13Vlg7ZIBkm/xPoqxlGK9KVfYhzbDEkO5B+npGix9JDHxepRUvRSOU0tiqZeES3+Z8cDS7DyO86RzBjE6oVKET2/pHKaWhRNPU0tiqu3Yg2CMv1xMY4gI+x3lt954TMbIL+jx0wAYYz5nQcBfUvBWUrOIwmSOB3F9GJc0bZxDxggKx20QTJO+m0OtbsoRynal51wfiY9KZ1q4WHP9rUQF6snpVO9bjhNLYqmXhEtlnMkVLlwpUHQWjT1NLUort6KXTKgEeUugxpiHEFg8AcQkNPejVOOHluh/EAkXJM8BqSZBhgI4msQ04txnbSNJQo8u1cquHfaEGqbIJUDnfSlII9j50keBzrRIssmSw8hThDjQCd6glROUwto6hXRYisyaadDnI8YB4ro+UjlNLWApp6mFnD1SoPAxDmg3bYQYnViHE6DGAECEtWwxUiAHr4Fcg7+AxSuyTZFdiEIyIRIbIOYXozrtG0f+chH7LYudwvc+Q7ulUiE3DvIaxtI5UCnfQlCHA6jjAYlCVYIRbXwUs/jXMQ4UFTPRSqnqQU09dppsRWUOCU+R4ZLngE+YlqgnV4IqZymFtDU09QCrl5pEJg4B7TbFkKsTowjROrWrVsbv7Pt0E15jB7BVgjIAohDIAYBe7BdJz9mETAQYnoxLqVt5GZnhwNBY853cI8ss7j7z2NtS+VASl+GOGIjEN46xAmKaLGkRGAb1qJ9zkeMA0X0fKRymlpAU6+dFv5FxBjxuTIOQSs09TS1gKu3Yg2CMv3xEtpx7pbBo0ePNiUrEj1SnvIgcA0C4g+4CZEwBghr3E4vj+umbcSgJ8ofCZvON3BP11xzTUt6aVCkbSHEONBNX/ogCE8eB4pokTQI4y3E+YhxoIiej1ROUwto6sW0nnrqKWu88r/PlQZBKzT1NLWAq7diDYIQYnVSOaCptxxa/HETdhXw0GZtWyB6OBbhaS6gHo4orjHBcgHLBu308rhetA2HNQwDZitC05paQJt74F64p160zUWMA73Qw7hjuSjEuYhphaLxgdj1YhyI6eXVS+U0tYCmXkyLmSE/i6lwedeLcSCml1cvldPUApp6mlrA1Vuxuwz8Y+3qpHIUTb3l0Lr11lvtNkJ+J+UxzoHCix5r3iwn+NdkhoD4A3KcbYsYFzG9PK6XbWPZA/8C2kJQIzcC4HIBDR6kaOInwD0sR9vacZRu9QjLe+ONNzZ+j9XL02IkyczSZz/72RYudr0YR8nTi9VL5TS1KJp6MS0chv/zP/8zyOVdL8ZRYnp59VI5TS2Kpp6mFsXVKw2CNhxFU285tIhOSMAZOUZ+AxyH+Fn0WEpgN4LEIZBrMvrlJSh12XpIxMOYXh63HG1jFwQjXYktzzo+gZhiFnFRcA2uxTXl+oyqCNri38tytC2Po3Srx8ucpFYhzi95WocPH24yIt0Su16Mo+TpxeqlcppaFE29PC2yxBI50z8u1yu3HbYWTT1NLYqrt2KXDMpcBktox7G27ToSkueeVJdA9PhSEGbW9SHgmuxNZ7eBgAiG7K+P6eVxy9E2l+N3DBuWP1gfveqqq+z/o6Ojtg28vNh3zS4Llkco/MwxOM7hXJZJyHhHXa7FNbm2r+diudvmoxs9PmsfsXrdaIUQ44CmXqoW+RnoR2bPeMHyXSJDIIVjP/zhD4OZEVP1Urg8LdJv4zAcAtcrfQhaoamnqQVcvRVrEPDHWO4yqKEdx0gQT3IB+8RJ9wpEj/3jvAhZDnANAvqZ6UUB2xiJYBjTy+OWo215HEAPnrgL7LUnSBOZ6Bjx33HHHXZG5M4777TH4DgHR8q8mP4xvXPRthBi9YQja99jjz0W5ELwtfBOp99idVI54OsJYvVSuZgWL3Vm1tiq+5rXvMZs3LjRGor8z7LRG97wBpsEiBTiBGLiO8UMGnv76WN27pDQijosMbH8xrUwIJ555pkWvbx7TOVCbZOAY6RrDoHrYRAQ9M1HTAuE9ECsXiqnqQU09TS1gKtXGgQmzgHttoUQq9OOI/DI2972tsYxvOIZ6QNXjwecxDB3r8kSgzjwcYyp89DoB8TuZTnalscBTT1NLZCqx0yRn+BKuLx6rhaxBsiMB2J1UjmQ2rYUztXCv4bZNKbLeYmT4wNDmlkkP2NjCohRwfIbRgP+LxgWb3nLWxozUHn3mMqF+pG/e9qUVyd2vRgHQnogVi+V09QCmnqaWsDVW7EGQblksIR2nOwOEBCZUGYMXD1GL5s3b26aIQDkOnCNL4IY+caYIHYvy9G2PA5o6mlqgW70JFZAiAtBtMRxU4zDWJ1UDnTTthBiHMay+KDccMMNdumI2B1aYKaGGSuZdQhN5cfuP8aF+pGZC2bB8upwPWYIWFb0EdMCIT0Qq5fKaWoBTT1NLeDqrViDIIRYnVQOaOothxYPcF70Aqb9GZkAVw8nsSuuuKLFIDh48GBTABNyIzDFHkLsXpajbXkc0NTT1AIpeh/+8Idzt2rG6qFFCGwMQ3ffeqxOKgdS2tYpx1IAU/rMfj300ENN3LkChghbWWXmIDRb5yPG+f1IAjN2HMXqwJU+BK3Q1NPUAq5eucugDUfR1FsuLUYfTIlyDD8BpkP52dXDr4CZBHeXAceZ0mR9VM7j5SCJjvwSu5flapt/XIqmnqYWpVM9RoXXX399Y3eJX/LqUb74xS+arVu3Nm0/bVcnlaN02rZOONL8MhvAun8vlgGWC+xu4W+W6I9sES3SNr/4/fiOd7zD+hPF6nCcXQZohri8ehRfr0i9VE5Ti6Kpp6lFcfVKg6ANR9HUWy4tpgB5KcjxV7ziFTZCnauHpSjbjdxrsuTgbkOSiIa+ll/PL8vVNv+4FE09TS1Kp3q8YFi/DnGxenzWaIX08up0w1FCWu3qteOYEWDdPuQ/cT6DfAMY7+QcYAYh1La8dvt/28QQefrpp6N1UjnKcnxueZymFkVTT1OL4uqt2CWDMnTxEopw7CzgpSBgHzJfBF/vxS9+sR05+ddktMLMAYAjII1fV7i8ewmdD2J1UjmgqaepBTT0+B687nWvs3kKQgjVEaRyoJdtw/mVNuDhn7d7ZCWAv10MGpbvXOS1G7j9+NGPfrRRN1YHDiOQXRM+YvVALz83EOM0tYCmnqYWcPVWrEHAy8x3bIvVSeWAdttCiNUpwvEwIKKfgD/4mZmZFr0/+qM/akQwc6/JEgHbpQAcIy2cE3349Vz4WoJYnVQOaOppaoFO9Ng9wvpxiBP4HCPJm2++2fqOdKIlSOVAr/RY6mJ5wM3uudKBAyLOj7LLJ9RugduPLD3I77E6cKUPQSs09TS1gKtXGgQmzgHttoUQq1OEw3GKgEICHMweeOCBFj2CE+Hp7F+TEQbrrwCOcLVubAOBX8+FryWI1UnlgKaephYoqsdnKaM9n3MR4jAKQFEtF6kc6IUeWTp5Ca5G4OuDoYNzsN9uF9KPGEQY9IJYHTgMAowpH7F6oBefm4sYp6kFNPU0tYCrVxoEJs4B7baFEKtThCOSmjsNiHMRD01fTwwH/5pEYMMpCcCR1MYNWCTw67nwtQSxOqkc0NTT1AKd6BHeOY8TuBye7swoCDrREqRyoFu9t7/97ea2227zzlhdYDmHJQTCiOf1ifQjcSOIHyEo2o8+Yhzo9nPzEeM0tYCmnqYWcPVWrEFQ+hAsoQhHqGI3lTHG1O7du1v0eJASac2/5unTp83WrVvtz8LhWIjXugu/ngtfSxCrk8oBTT1NLVBET6JRhjgfwpHkiheqiyJaPlI50I0euwfwl7kQcPbsWWsUsPQXAv3IwAmHYBft+pEZAtJZ+4jVA918biHEOE0toKmnqQVcvRVrEIQQq5PKAU295dJi/ziOgQK2IG7fvr1Fj3gFsoPAvybhW8XTGY4ZA2YOXITqCXwtQaxOKgc09TS1QDs9XgQkqmInic+F4HJ4JLtopxVCKgdS9Q4cONCIr3GhgIERcQv4e/ZBP9InstQnaNePpQ9BKzT1NLWAq3cRI0cpTANT3GNFOApb2vxj7eqlcppaFE295dQirvp3v/vdBnfNNdfYmQL3fInaRgQ3/5rkMCAgkVwTHwISpeTp+WU52xYqmnqaWpR2ekz7v//97w9yfh3Kv/7rv9qlIv84pZ2Wf7wbjpKiNz09nX1vX2YmNo6ambNLD7uZ3VUz8eUJM3rP3NLBTvDwqFlz8ZpaubRihvfNmOY5sQhOzZgTyxX0cGHGzJys/fitb33L/PVf/3VLnxA3AmMBo71oP3Kc5UWeAyEurx4l5XNL5TS1KJp6mloUV6+cITBxDmjqLacWa/4EHRGwt5k/fh8sJWAQ+NckDjpTynJNHkREPnMRu5flbFsI6DEzgvMVOyIYKfGSxIiR5EbsluAYHOcQkOXJJ5/0L2UR0zsXbQshVi+P47Mmap8fClzQSy0Q40CKHlPnjGzn91XNyLH6wbNHzciGCTN/dtEsnJFj2c/z82aBSRB+tj8Ys7iwYBYxJNxzwUNDZqgRyHDRTF03YMafqP96ZsHMz2f1GjS/16+dHT1x66AZe1SuP1871xorS7rUadzLGe6tdr3Q+faY3O99g2bw0JI2acr5HrvAEZjvuI9YP6ZyIOVzS+U0tYCmnqYWaJohcI5HK8Y4oNkItMpcBksoyuEfwJZCAeutfthWfufhwnYz/5q8LDEW5Jr8T+Y0MiUKYveynG0D3/ve9+xIlxgLjJiuvvpqG52PaWTaRGIfnCnZVy/pj/mZY3Ccw7ksmZDchmtwLa7JtX09F8vdNh8xPVI2S9hbn8u7Jn2Rx8W08uqkcqBTPYy5RsChUxOmelPdQ/6hYTN4X/bKPDluhvZm/XFm2oysHzYThyczQ2HYTGej7JFNB8y8mTPjL/p/ZgS/u6zOsPsnkRkEg4d4OWfliaNmdO2gmcwMhsUjI6ayPfs7OTRiqtums5f5lBnevMdMHR43g5eOmpnsqhgPQ4dmzcIDQ2Zgx2R27pipbEZv2gxtmW5c3xoc2T1W/mDQjO6eNP8ROn/NgBm9Z8qMb+ozo48smtk7BszArhPGnYBgNgC/AgF5CzD2fOT1I4DDsPKfC8Ll1QOdfm4gldPUApp6mlpgVRgE5S6DJRTlSFtLxDYB64sUF7wM2ar2vve9r+WapExlXdq9JssILEMIYvfS67Yx+udlcOONN9oXOPvtebEz9dUrcC2uybU3bNhgt7KR88HP9tjrtsU4kKf3mc98pimzpQv/mqw7k/kyxLnI04rVSeVAp3q8CJewaCY3Zy/7s/xfe3k3DILs5du3/UD20p4yU7sGTGX/vDl605CZPDVpRrcMmeHsnJmdw7U6gqzOwK7s/HtGzcDvZdc7XTs8vaXPDB+cstcaXVcxB07NmNHLBrJrTJqjJ2vj9rm9Q2acaf3FBXPiYa4xYvrXj2fmR9ggqN56onYsdP7GydpsgJzfNHNRA99LpvoBEQ6JVxBCXj8CuNKHoBWaeppaoDQIPMQ4oN22EGJ1inI8MJhGFLDuytS5C9aemUUYHR0NXpNcB7wkhcOgIJ6BIHYvvWobAZZe9apXWV8HEi2Rt14LaKGJNvcgwZ561TZBjAN5evxdsEQSgntNDBo+Y7n/mF6eVqxOKgc60WMEzK6YJjAzsH/CDMlMgRgER4ZNdd9cbbT/6IyZ4eWenTu0ZcSMHj9hxrYMmsEt9Rdv41pDSy/eR8dMX/ZixodgelvVTJyszRycOD5jFrKX+NypBbPwxAkzsbFilxXEIJjZWTGjx+bMwplZs+e6+gv+unqysAfq15d7NDnn+wZEwCAA+AkBjP8PfvCDHltDqB8FcBgEPAd8xOqBTj43QSqnqQU09TS1wKowCMolgyUU5UhK9N73vrfB8eIPpTklxCtryqFrMjX7+c9/vsGxtc2NbxC7l27aRthkjBdmAu68807z4x//2D9VHdwD98I9sdQgoZ1dFGlbCDEOhPqSB3msns8dOyaL7a2ci5AWiNVJ5UAneux08bdXWt+BZ/eZMbGL5GV7dtaMb8petnuz39ezZMC52cv214fM9NnsRbzj/5kqSwwuvBfvzI5LzNADWcXHx83g+lEznr30K9unzcLZ2gwB1x5cV3NsnD9YNX0bDpjP3Fk11d2TZmLnkOm/bMQuJxzY0G+q27Nrr13TYhDMtZwfMAgeGTV9l4+aac9pkTDNPBdZyitiGPpI5UAnn5sgldPUApp6mlqgySDgZCmxJAgxjsLIxD/Wrl4qp6lF0dRbTi18AG655ZYGR9IiP0kRD1debOxdDt0LvgWMPuSavITYzhjS80voeu3qcH2WJZgW/uxnPuN8jc8vsObKPRLu101CE2tbKkfx+5LPctOmTXYGI6+eXHN8fDyX849TfK0idVI5Sid6zNSUWAJOv29605vsZ9xJP7ocjsc8B0JcXj1Kql4Kp6lF0dTT1KK4eqVB0IajaOotpxazKm7WQkLTXnXVVU118RHAIMABEYc7/3qPPfaYnWp26xDPgGh4vp5fOm0bMwI49pF+d6WAe+WeJV10Xtu64Sh+X/ICmJycjNbjONPIBB4KcXn1fK0idVI5SkiP9jGjId8zKfh1lFgCzqFXXHGFnf0L9SMl1v8cxyBwnxNF6lFS9VI4TS2Kpp6mFsXVW7FLBiHE6qRyQFNvObXYDcAo0gVe+O4a/Fvf+lbzs5/9zCZD+tjHPuacWQNGBNfgugKWIb7whS/Yn2P3UrRtOD6yFsoLbqWCe6cN5HzI6w+/3S5iHCjaly7gJHGVj1i9VK0UDoT0WJb53d/9XXPppZeatWvXWoc5lrs4XmIJGE0veclL7M+hfgSx/odjVo7ngI9YPZCql8JpagFNPU0t0LRk4ByPVoxxQLMRaJWhi5fQCYflTxx0AeGMv/nNbzZ+F/CCZ79+CCRAcv03jhw5YrfsAV/PRZG2MfvAVr+8a6wk0Ab6KuSnAWJ9FeOA25cEixLk1SPGAp97iAN59UCRz81HKgdCetR56Utfan7t137NXHTRReY3f/M37ewWW0tLLIHYIGwNBqF+BLH+T+WApp6mFtDU09QCq8IgYJqj3GVQQyccPgTuyxyHQEaxAvavM0PAvvtQ8iLAlkS3DtO4vPiAr+ci1jaiqjHV7u5YWC2gTbStV06wQPqSrHRjY2ON46F6RKobGhqyn6nPCUL1BLHPLa9OKgdEj61zbCvl+wbYUvesZz3L/M7v/I71K/nVr37V8KrvOU5Pm7ENA2ZgXb+pbJ8y82zvf6JIxMOjZnRna6ZALWCck7QMpH5uzBB0GscCpOqlcJpaQFNPUwuUBoGHGAe02xZCrE4nHLsE3PzwJDFx0yLjZIhBwLY0or+5QYcE7HWX/c4CXjjEBfD1XOS17cEHH7QvzDx+NYC24XRI3wlifRXjgPQVzl+MCgWheiwJ4fsR4gQxLu9zidVJ4UiUxU4YsvMBDALiJGAsAvwInvOc5zRF38N/hRdYbzFrxi4fqu1CyLBALII7Zus7AWbN4un5pmiGjaiCtd+WohC6URGF43c3zkGPgYGP7w9I/dzKOASt0NTT1AKlQeAhxgHttoUQq9MJR9S9T37yk43f8Y6XtMaABwEGAfUIbUw/+2Cky7S+C/KtkzLX13MRahu+CnkBVFYjaCttBrG+inEg1JfArceD3Q3SFLtmjCui5aMox0tffFX4mUyFbqwMF6997Wtbkhfhq4FnfU/x+J6lIEEuiCZ46ZDZc2jSjFxeNQfmiVg4bAa21SIWDmxwohCemTbD65yoiGfmzMSmQTN2aMqMb66a8cf9i/cGbpCm1M+tNAhaoamnqQWaDAJOlhLzRoxxFE3PSLTwhGcfeNE6qRxFu23+sXZ1OuEkNLH8TupU9i6H6uEpz5S0zzF78PKXv9xGLpRjPJh50fl6bvHbxn3kLUusZtBm2h7rqxhHYQspBhvbDUP18K/BaGO5wuf8a7Xj/M+tSJ08jr9Z4ljAMbNBBEi+N+45negxq9DzrYePjpkqMwI+nGiCEnRoesuQmapHC57ZQUCiukFwpB42GZzJ7jurO7CB0MZTZmrfkOnbEU5Z3A2Y5t+6dWtSP3bLUTT1NLUomnqaWhRXb8XOEIQQq5PKAU295dYisps7I8D6P0FMBMwOyAwBL/nQaA2Ovc5kyRIwW8Pozddz4baNUd2FaAwIaDvLNXl9FetHQB6Ke++91z/cqMduEP+zi10zxnX7nWTkDwhxzbZWDM28eqBTPQwfZqd6BoIVrZ9YyhNwasJUspc8L/VG8CAxCLbVwyMbjIOKOTDfahDMP3rUzH43MyZumq5FSTxJpkIvAFIPgEOuG6Sp034EcBhZPAN8xOqBVL0UTlMLaOppaoGmGQLneLRijAOajdDUApp6y63FiNKd7keP6UEeVEB8CKh3/PjxltDGAI7MauQ8EGCB/t3f/Z19EeXdi7SNbYXoXOjA74Lw0SH4n5sP+jLEU+9rX/uaf9gids0Y1+l3khcKHEbi1q1b7U4WF3n1BJ3qcT4zVr3EwkPDpm9t1QxvHzKVtfWohgGDQCIWju2smH4iFsqSgURF3D1qKpvGzezZjNleyeqPm9H1gz1fMsBh9Nprr2061mk/ArhyyaAVmnqaWqA0CDzEOKCpt9xavOwZpQnQIxkOTmfANQgwHlhO8AHHNrZGhrk6mDXAASzvXtBiemrZPMNXIOgL+sSH/7m5+MY3vpH7PfnABz5gsx2GELtmjMvT8usQKpe8AhLUBs7d4irw6/koqucCR0PXOfZCA7FByHnvIqUf4UqDoBWaeppaYFUYBGUugyV0yvHSJ44DQI/RPtuVXEg9chr4mf3gWGog0ZELHsif+MQnWvQEaLFzQaaQS9Sm0+kTH6HPDTBrw5a7vO/J3XffbWdrQsi7JohxeVr4MeD5/5rXvKZxTHYExK4X40CeXqweHEtfbnrvCwVEngwZQ6n9mMIBTT1NLaCpp6kFVoVBwKiq3GVQQ6ccjlyyzxg9tv2J57tA6jHqZzoyxDHTgGOhgO1OjE59PQHRz/x0yyVqGSPxrncR+twAfgP4gfjfk8997nP2/7x6IJUTLdkRINkFqYMh2eksQIwDftsEsXpwOCziYMhg4UIB3x3+RkNI7ccUDmjqaWoBTT1NLdBkEHCylJg3YoyjaHpGosUIlRFK0TqpHEW7bf6xdnU65dgiyDozP6PHqFPi20suA6lHNj8e+qFr4kQoWw0p7HdnW52vR8GhrDlvfQkXrIHTR34f+/0oxf2eEFeCWZ+TJ09G63XK8YJlaYjId3IMw1HCH4fqxK5XhKN08zfArCHfM66x2oFTKlsx8/oktR/LXAatRVNPU4vi6q1Yg4D1bbzci9ZJ5SjabfOPtavTKUe2wk996lP2Z/RYe2QLG7/7BgExC0iIE7omoxNGjC7Hg8RPQEPhhZWXjrVEbf3dzTzpf25PPvmkefe73934frjfE6L59eolzZQ/sxByHKdSggNh7OXV8Y93w1G6/Rv4r//6LztTkOdcuRpAACKcNWN9ktqPpUHQWjT1NLUort6KXTIIIVYnlQOaehpa7E2XYC7osTNg8+bN9ndGheJUSD1mYXA6dCEcW5x4MLkg/wH7zF0wi4DBUSIO+ki2z/mfG2vEjAgFfG48uH349VyEOIkOKBx5Fwhe5Yau1fhOuuiFHn4vr3rVq6x/zGrCz3/+czsLh68I8NvtIrUfcSrkOeAjVg+k6qVwmlpAU09TCzQtGTjHoxVjHNBshKYW0NTT0GKtn9EmED0/6ZHUY2T66le/unHc5diq6DqUAWYe5GEl2LJli53OLhEHfURfAf9zw4p394YzO0b+CH/bol/PhXCuUyczQvgvMEuUV0/jO+mil3oseeEYy4ziSsfU1JR1QHVnPvLaDXrZjyDGAU09TS2gqaepBUqDwEOMA5p6Glq8EMQxTPRIkMN0MSNEd4YA8JJiJClwua1bt5rTp083OK7tBhz6n//5n5b90SXyQV/RZ24fu/0rYKkmlJ469HnLZwfHZ8ln7L8gQ/UEGt9JF73WY0stMTLytmOe72A5iRk8fH98xNqd2o/MEIQydMbqgVS9FE5TC2jqaWqBVWEQlOmPl9Apx6j/xhtvtD+LHqF0mep34xBIPYyFvAQ6RD0km57L8VKT3QeM0NyEPkuYMxMb1pg1F9dK/8YxM7XczuELRIhrPjR3T9X07XLCyH5njxnYMGGWJswdBOrX0LsMd/QVfSZ9zBICu0J8FP2eYPgxCyAhjvMSAfn1XBTVcpHKgeXSYzcNvgX079mz9XjD5zEw0JmB42/V3+kjiLU7tR/LOASt0NTT1AKrwiBgCrXcdlhDCseebR6KoofzIGvHjEZ8gwCnwrwsfTi0uSFy4fBKJ3gOIIthGHNmfMv40ov38T2mf5tMfweywi0u1DLKncnKWX4tkl3O2Mx086frZ943aAYPSb0a5vZWzSVrR8zR+vthdnfFrNlYv6/69ebrF2yu795jLTqdzXrnip+p33PjQPP18kCfSR+z+8OdIibwEH4aoe8J6YCJC8AsgPB5hpyPGBfSArE6qRxYbj2m3tnVIYG0zjdguBBoCJ8S2XlStG0uUvsRgyDkAByrB1L1UjhNLaCpp6kFSoPAQ4wD2m0LIVYnhWO7ElPTosdLx00r69Zjjzs7E0IcL353KhMOAwFPdWZw3KiIzcgMguvGzAwvyKzM7q+a/t0EmgpkhSNz3IYRM3l43Ayu6W8klamZD0vZ5UbWO9nlFrKX+x2DZiS7zuSOAVM9+N/Z7wNmYNeJpRj1qO0dMmO7h83IMX47YcZ2jJkxa6jMmj3rBs344SkzsWXAjD6y6NTP7n1DdekeT2b3sCY7557s90192blkwRsxle21LHjVbYS0XTRT2wfNnsNL5+SBPqNP/M+N7zuxHACfG6mpJS6AOBjiU5AyCxDjtL6TAi29b37zm/bvgG2K+NSwO+Fc4Ytf/KL1CWEGY9++fblLdD5inFY/CjT1NLWApp6mFmgyCEiPKgXHIop7rAhHwcnJP9auXiqHFt7vjzzySOE6qRxFu23+sXZ1UjhGH7w8RA9P823bttlRE6MDtx65B3hwhq7JdjdGMy6HYyHXxzOepYgwspfqxiH7Ah/ffElmDJyojaRDWeEeGjJDD9RrOVnmmgyC7Jy+7Qdq9XYNmMr+zNA4mBkZG8bMxJETZoEZAK7zkOg713t00gzvOGqz3I3cd6Ixc7HwxIyZ5v42XVKrJ/WfyAwTSY/Llh6S4WycrN1//ZzpLX1m+OCUvZ/RdSS9MWZmZ78Z2D5uJo/NNc1S+KDPMMDuueeeRr+yDk7f0qcs4fAC4Tgvss9+9rPBz8YvqZzWd1KKpp5oMTvG9PxVV11lp8tJ7MWgY7mAIU3ob5wEr7nmGhvTA8Pbv7929x/jUvuR5yrPgRCXV4+SqpfCaWpRNPU0tSiu3oqdISh9CJaQwt111102tbHosZ2JbVohHwL62h3p+9fkASqjUrif/vSnDScoWTpohbtksGAmN/aZsUeNfdm2ZIVzMsfN3uGlnT071cguV903V6v3aFbvdPauPpn9fmbBzGajdbsckWcQZBqTm4fN2K0jZvJM/b7mD5jBTQfMLFP+D9TrhQyCk0fN0VN1owSIQbCtaiZO1mY/ThyfsQbJQvb7wsKcObGvaip3Br0ULOgzprQxDHg433bbbXYdmVkAdgTg64FBHIL/2bhI5bS+kwJNvZAWWy7ZlotRy/INhZ/xPWC5AZ8ZzmEU/8wzz/jV7TZetu0RJOnYsWM2jgN/C9dff701ODCg+Z3Pkb87ELvHVC7UNhCrA1f6ELRCU09TCzTNEDjHoxVjHNBshKYW0NTT0mIE9PGPf7xJj+2FjO59gwAwevrRj35kf/Y5XlJsZXQ5gqaQuCeURrUGz4fg1AFTWbsnnBWukTluxFQurliDoDb6HzZD1/WbNW52uaze0PrakoE9Z/O4Gd8xYAYPZkP0R0ZN3+WjZtpZM5AZB/wDLrIv9fp9sUyxbthMHNpjhjf0mcH9bv36Pe4eq2u1GgSSBW88u37FZsGrzxDszO5n84AZPb50Dz7os0qlYl8qL3nJS8yLX/xic/jw4aZztL4nQFMLaOoV1eI8ZtGYSSD1NI6e/L2we4FRPlP9FH7GqZYX6q233mre97732fwehJvGgCiq5yKVS9XCIGCw4CNWD6TqpXCaWkBTT1MLuHqlQWDiHNDU09IiJC0jUFePBxhOTMCvh3ObjPZ9jlGT5EIQDkfEtWvXNs7pFRppZ1c5qtWqjUuAEYajIJHGXGh9T4CmFtDU09QCmnqaWkBTT1MLaOppaoHSIPAQ44CmnpYWa9L/+I//2KTHMgJrh6EZAqZQ2YkAfA5nN9a1XY74+i984Qsb5/QK80cONI3wVyte9rKXmZtuuqmRlhqw1vf617/e9q3W9wRoagFNPU0toKmXqsUMgRsV0+Xy6oFUvRROUwto6mlqgVVhEJTpj5eQwtF/pC929e6//35zxRVXBA0C1kJZQwU+x/kSfEg4RrYveMELGueUKA7WpZ///OcHM0PiGc/sC59bKGCR/9m4SOW0vpMCTT1NLaCpl6pV+hC0QlNPUws0GQScLCWWBCHGUfDI9Y+1q5fKoVVmO+yOI3DQK17xiiY91g2HhoaakhsJRwRCRqd512Q0iyery23YsMH52pXoBJdddpmNFkcQKf+zo/C5seRD8iGC1sjx0GfTLaf1nZSiqaepRdHUS9XCeZWtwyEurx4lVS+F09SiaOppalFcvdIgaMNRtNvmH2tXJ5XD65nANfI7/gNvfOMbg/WYBcDzPcRRWOdm25TLNRkEZ2fNgXuOOtvt8qP7zd0zaiae8I+6yOpeN1mPJzBr9lxeMRN1v4K5fcM1R0QPR3eOGtTmH6nFIZDfO0OmW4+suObiPhtnYGZpu3g6Fo+aiYPNs130HQ5s/mcmRb4nLOOwZMPPxJUIfTZSUjnN7yRFU09Ti6Kpp6lF0dTT1KJo6mlqUVy9csnAxDmg3bYQYnVSudHRUfPlL3+58TuzBn/5l38ZXDIAbL3i/kIckQyJoudyBHwRzO8fNnuagp85nvk2CqETYdCNRigRChv1bAUzuWnITLPt8OS4Gb6yUtsFkL3qD2waMTYQsVyzHu2wdq0TZmzzmDlxpv5747inIb9n/zcHFXTuOQPbEQdk+6DfBg45URJt1MMzTnRDohgKl2F+X2YEOaGb2eom/Yh3O3/QLvzvCdvXMPAwIvJ2doQ+N0GM87UEsTqpHNDU09QCmnqpWiwZPPSQtz+3zuXVA6l6KZymFtDU09QCTUsGzvFoxRgHNBuhqQU09TS1iExIKmQXl1xyiX0ghOqxvfD48eNBjiUFHBJdjmBGtfgERCXcY5rNt6UIg3Z7HxEG11XNgVNu8KE1ZmDXhJnaO2j6djr5BjLM3Tlo4xbM7x8yY49k19o0aQMEDRNv4MyUGVpHZMNJM3bloA0KZAMZneL4kJl8fLER2KhFw4mKOHzps724BZnO5slarIP5OXN0V38tPkKoDXdWTXX3pL1uP9fNDJfKpcNmnGiLl15iBm+dNJM31euDx/eYob0144I+2759u3UiJFEUDp1EKKRvBaHvCX4bOIb6n40g9LkJYlxIC8TqpHJAU09TC2jqpWqVPgSt0NTT1AKlQeAhxgFNPU0tPIl9x7WXvvSljaUYvx5hcolfEOL4nYAr5KEXjhCsRDm0L9LrpprOdyMMShRCc3zEBuxZMgiqZtLJFdCER8fM0P45M7mFGYH6jMGxETNkX7DZSPyxo2bq8IQZuawWt6BmACzFPlgyCDwN537m91daDYJ1o/a6o5dnL/X76usFLW34imMAzZsDVw6b6cwgqNaDGTWCKmXHxAhw20ifvfe977X9yP8sC8i2TkG77wlBb2RHgs+FEOPaaYWQygFNPU0toKmXqoVBwN+uj1g9kKqXwmlqAU09TS1QGgQeYhzQ1NPUYlqQRDku2EnAFHWoHvkO/vmf/znIAYIRMaoVjih7tTwHS6F9Fx4eMxPE8XciDDZGydmLlZDDueGJmzBjRrYPmaF6QiRmCqobR+xsAAGEKruOmrnT5B9wrxUyCDwN537m9va1GgRyH2dPmLFLM2MCm6ClDV8345vGTO31n2muz4wW5+Xf0HQNAumPDPQZgZ7oRz9NsQQoKvI9YUcCYY2B+IqEPjcQ44po+UjlgKaephbQ1NPUApp6mlpAU09TC6wKg6AMXbyEVG5mZsYmxnFBOuMPf/jDwXr4bbCbIMQBYu9/+tOfbuJq2Q6zUfKW+gvy5IQZuKyajbQvMVWiBzaiEGYv8bW1CIPFDAL4i0xVXsTZy7Xv0vqonNH4hjEzec+oGVrbb0aOy7Wy+9jQZ6r7T+QbBNzPhgEzuH3QVC5+thk+ImrOOYLjI+aS66bMQqANCw8Nm8oWEiX1m8qds00v/6BBYGc8am6SbrZDAZHuAFsOb7/99o6/J2SgZEdCXgKfvHqgUy2QygFNPU0toKmXqsUMgZvS3OXy6oFUvRROUwto6mlqgSaDgJOlxLwRYxxF0zMSrXKXQfccTpmEJHaPsWTACyevHmmTn3rqqSBHsiQStrgcORBY2168TzIKnud4YsqMP1R7Mc/sGjTjTzTTy4XZO0bt0gV9RZ+5/Y8hxnIMHL+zhJDyPSGELuGQ+ZkdCUXrpWilchRNPU0tiqZeqhbbDoeHh4NcXj1Kql4Kp6lF0dTT1KK4eqVB0IajaLfNP9auTir3k5/8xL7g3WM4E7H2nFePGO5kQwxx7IdnJ4LLMcWNoxuzBNOH6xkNz2ssmJl9o2Zkx4gZe0ArJOKsmTo8Z3+ir+gzv//JcigzNxS+J6zz+p+BXy/Esb2ULaTsSGCmrV09ze8kRVNPU4uiqZeqhUFAGPMQl1ePkqqXwmlqUTT1NLUort6KXTKgEey/dhGrk8oB7baFEKuTygGmp/myCBj9b9myJbceyVpwegtxgGQvZDt0QVz+EsUgfeX3P5+LC74nfBbMyLjw67lwOWYbSLEM2G4aq6f9ndTUS9XCmOYZ9O1vf9vm+MBfhml2Csd++MMfWgdbH6l6KZymFtDU09QCmnqaWqBpycA5Hq0Y44BmI9AqfQiWkMqBG264wU5JC0jrShhi+jZUj22KjFZDHLj55ptbUvOyBBHa11yiGfQRfQVCnxsOhvISl+8Jjp4uQvUEeRw7GJjZCWW4A9rfSU29mBYvdYxfgm6xtEZcDQw2/ufvhtkytoOS/RBnW3w0cAjFLwdfm1e+8pU24yd1mJHB6ZZrYUD4aZNj95jKxdqWVwcOHwKeAz5i9UCqXgqnqQU09TS1wKowCEKI1UnlgKaephYgPK77Av/7v/97u2TAtHKoHp7rOKiFOEBwItaqXWC81ZwLS8RAH9FXIPS5Pf3003b2hpGp+z05e/ZsY5tYqJ4gxjGyFcdFN6ES0P5Oauq5Wsya3HvvvXb9nJc46Y3vvvtuu3WTGYFuQfbKBx980BoNpEnGsHjLW95iDbHYPaZyqf1YxiFohaaephYoDQIPMQ5o6mlqAXYVsGddwIOAUSoPqVA9How8KEMc+MIXvmADGPl497vfbQPslAiDFxF9JMj73Jih8Q0CQJApZg/y6oEiHMYFswV8hvjpAF9LUOR6IcQ4oKmHEYTfxt/8zd/YUT9xNgg8pQWMLz53jAOMPXbp+Ijdf4xL7cfSIGiFpp6mFigNAg8xDmjqaWoBXiI8BF2Q2IRjefXwgifpTghMNYYeJuCqq66yIXZLNIM+YWrZRbvPjbgCPthSGKvXCfepT32q8bM/YyDw67hI5YDG3wBLAUzpszx2vixnYYgw0yMzBzJtH7p/QYzT6EcXmnqaWkBTT1MLNBkEnCwl5o0Y4yianpFosZ5KEJyidVI5inbb/GPt6qRylPvuu8+ua8rvZDrkZcCxvHq7du2ySwf+cQp1XvWqV9mZBJ/7+Mc/bkPylmgGffLwww+39GNe/zOK5DPwj1P4uyAehH+83TXzOO7r6quvbtqR0K5ONxxlOf8G+L4zG8C6fy+WAZYL+IYwY7Bt2za7pFekbX5J7UeMfZ4DIS6vHiVVL4XT1KJo6mlqUVy9FWsQlNsOu+cojJRuueWWxu+M/glYxAsnrx6jGNZC/eMU6oyNjVmva5+jbW9729tawiVfyKAv6BO/r9p9bkxv45zmH2e2AUe3/fv3t3Cxa8Y4ptXZCsnPbnbMWJ1UjrIcfwN8z1m393dlnO8gRwhLdCzjMIMQalteu1P7sYxD0Fo09TS1KK7eil0yoBHltsMaUjnASN+d4sepkF0HvHDy6jFCnZiY8A9boMd++fvvv9+nGm0j9gFJki500AcEHAqh3edGdskQqIdRwAjPR+yaMc79TuJNv2PHDutoF6uTyoFe/g2w9e91r3ud9fBfyctVzBhg0Pg5LfLaDVL7sfQhaIWmnqYWaFoycI5HK8Y4oNkItMr0x0tI5QDrlLygfbC+6scTEDAz44c8FqBHHH7ZPudC2iZr5nlhdC8E0HY82f34AoJ2n5v0JTMxLtx67IfHWTHE+Yhx/ncSI/L06dO2juxM8BG7XowDvp4gVi/E4RjL8oCb4GmlAwdEjHWJcRBqt6BX/SiIcUBTT1MLaOppaoFVYRCEEKuTygFNPU0tgN6NN95oH/AuGAXmvbA5l6WFENBjhME1fbhtY60bo4CZngsNtJm2sw0t77Mp8rkxaiTugwu3HoYXy0HiNBq7ZozL+05y/+y3d3ckCGLXi3EgTy9Wz+fwo2D9fTXi3//9362hQ8RQv90uetGPLmIc0NTT1AKaeppaoDQIPMQ4oKmnqQXQY7Qvsy0sGfzsZz+zI/y8kRXXZF0z5JQlejyMiZvvwm8bLxRejMQ8uFBAW8UYiH02MQ5IX+Kv8eUvf7lx3K9HFMI8zkWM8z83gdSRrXKsU4phGbtejAPt9EJwube//e3mtttu885YXeBvjyUEPvu8Pkntx3LJoBWaeppaoDQIPMQ4oKmnqQXQY0cB0/xADIIPfehDQT8AwDVxhGPq2IfoYVDgnOgi1DaiwfGCzIuSt5pAGwk+RJtB7LOJcSDUlyCv3rFjx8zHPvaxIAfy6oGiWmxXxSeCGQmfcxHjQFE9F8KxeyAUB2M1gpgRGAX+35kgtR9Lg6AVmnqaWmBVGARl6OIlpHIAPbaUTU1N2d8Z1WMQkLmwlpSoFVyT8MUEyfEhehgThw4dauLy2sbUNksQEpZ3NYK2DQ0N2Ye4IPbZxDjg9iVLB3jRg7x6bCVjGYjdAiHk1QN5n1uoDtH+vv71r1tO7slHqJ6LTvQEcOzYYP/+hQSeg8QtoN99pPYjBoE/uydcXj2QqpfCaWoBTT1NLdBkEPzgBz8wUr7//e/b4h4rwlGICeAfa1cvlUMLxzYcqorWSeUo2m3zj7Wrk8pR0ONBSsRC9zhe7KwP++fLNf8/e2cfG1d15n9Wv61U7V/tH5Wyr1qq3VWzqnY3W7Sqpawq3IVtptA2LumSEambmqiACSU1KVA3lKUhQDABgnkL4S1goIADDTgsL4Fu05q2AXfLS0ILNZVAdbfQNSUqRgrw/OZzxsc+c+bcMzPH48ce537QIfb9znO/c8+M7z333HOeQyOC6W0hjUIuAy5Avpf/ejeGFdZOOumkqfS9CwGOhWPi2LKO299eS6O4dfnQQw+Z3A+14khPnKXF4mp9bv52q/X09JgBcMz997WsOEqK34MPPmieq1czKv3H98jwdDtMhjcUpP/FPdKzLrFX6pEeWbR8u0zlMzw4KF1/0SON720G78GBwaP0PPl1klKPqRpF00/Ti6Lpp+lFcf1atocgn3Y4TaoG+DGfna5WYG0Degi4S8galMU+GXPAVC4f68foeR4/uNRzbIxb4BFC1uOKVoJjYCZB1uOQ2GcT08CvS7x4fSzOarzWn7YYi/O9LLEYqz399NNT22zGw1gcpPjRdc53NsTY1oJ0Pz75y6E90r2sv3Qxn5Dx8clVPg+Vfh4bE/MrP09unxgflwkaEmxz26i7inLkXxelf3JNsIk7uuXIRUUZsvrBcRkbG59c6ru8v4nXxmSM/U642pAUVw0arWL/wfjJ95IB04Cvvfbaim0p9YhGPXIe8InFQapfiqbpBZp+ml5Q0UPgbI8GxjTQPIi8QVBJqga2Llm5DewYAuJYypgFdXzQuPM94YQTfKnCj/nf7kqKjRwbC8AwHTKUone+w3vmvXMMoWOzpGqQVZd8dllxdp+MYfjKV74iu3fvrtJCZHnFYnyNxxp2jQS6t7PioFE/eiCiCYde6ZfCqZONsl1d0nEHl1ouxqVL+MEh6T66S/rvHig1FLpkaHxYus3d/6j0ffT90k26jFJMl5vduNQgKG7oleJW+ggmZGBtr/SuKjcIJnZ3S/uafhnc0S2F1UMyjs+igmzcUdr/xxfJkrV9MripIEs2MIi3pL1/iXRfMyh9K5ZIz96M+A+0Sc+G7TJUY4kFHh24j6QarUdAy8cQVKPpp+kFC6JBkI8hmCZVA/y4iPB8G5jKZBsEnMCZHuhj98kjBfeC72rAnGl3BHyjx8ZI/M7OTnNyypoCOZ/gPXKhZQYG7x2yjg1SNQjVJSvzMbAvK87dpx8f8/Nfa4nFZGnMSEB74YUXgrNUoFE/LoRxShftFaWL/SH+7ZABczc+2SAoXdwXr9kug3cPyuD6NmnfNiZ7Ti3KwCsD0lO6yHdtHpXhdV2TMZPQINi1v9QI6Jexg6ULfeniPjTZIBhatVi6bho0++tZ2i7bx0o+xw+Yu/3RzUXZeMDx5t+V5bE78mKftK8djsbXgvEh7rifRusR0GgQcB7wicVBql+KpukFmn6aXrAgGgQhYjGpGmj6aXqB9aNB4D67J45xBX7iG6uxT7LWcecX0oD0xm6a4tRjY5Aaz6LJ7W5nQ8wneE8MiuQ9ul3kUOvYUjQI1SV3/itWrKhqKFtC+2SwIytUhjRLyAtiMbU0Vr60MxJ8GvEjMVJWkqwK6BnY1i9F21NgL8q7u6SwddSkBR7bNyzDzJosvba4qrt0xz5Suuh3SMcq74JsGgQi+zd0SO+53dL7LA2ByQbBasYnjJn9jewdlvFD9uJfbhD0mXai0yCwF/sDG6Vw7kg0vh7cRbIaqUdLqgaafppeoOmn6QV5g8AjpoGmn6YXWD8eGfDogIu87SFgJgEXdR+7T07mTGULacBgJxLjWGZ6bCS/YRQ5J70tW7ZMTd+bC/DmPfBeeE9ZqZjrPTafmAZZdUmPTlZcaJ8MJOrs7JQnnniiSrNkeYX2Z6lHYyS7/QzdMQ2N+NFoJVFPTRg78IHF5uJdZvJCe2i/9C1vl57NfVI8mkcGvLak/XHpAn9IZHjt+6VgHjE4TDYI5NleOfKjfTLK3iYbBHKgTzqO7pG+0sW/fc1kl3+sQfCBJdK1qU+6lhZkO1okvh54TGeTfTVSjxY0egg4D/jE4iDVL0XT9AJNP00vqGgQ8GJbYosgxDQKX0J/W624VE3Ti6Lpp+lFsX4kcuFunztd8uATQ/cu3dB+jN0nF5FNmzYFNX5mYCHjEHwvv8TeY5bGlEd6DLggc/IK5URoNnjghSe9AbyH2Ti2Whol5ofGST2khfbJKGO2M/3T1ygxr9D+GtU4ITGo9bTTTjMzIRrxC88sOHxh9g9jKqibRurR1fLFjaqLpp+mF8X1a9kGQb788cw1ivWj65gGwMMPPzzVIODkTCY8P8buk7tLxhGENPs74xB4Xux6+cWPaVTjPTOtjws1z5M3bNgg9957r5kJEWsZZ0EMseyDfbFP9k1vB3Pr/fdBma1j87fbEvMjqRSfZ0jL2iePi5hGSiPP12JeWftL0fi+8S/ra4RWcgzFLVu2zP/4Dmt4xEdSMOom9XOjQcDfVEjLiqOk+qVoml4UTT9NL4rr17KPDDiIfJZBmVQNrB+Jibi7sBDHgMFao40ZTe/u3/djqWROLqBxbO+99565k2fFxdNPP930UDD1j8KFnfn6TKdk8B+FNRfYhmZfxzHxXJp9sC/2meVn0Tg2l5jf+Pi46cngxO5rWftE4++Jng8acC4xr9j+UjS46667pmYkuITi+LxypmG5ZPuYTvtz0/TT9AJNP00vqHhk4GyPBsY00DyIvEFQSaoG1o+BcaQw5u7SjiEgjoujO5UJ3H2eeeaZpqcgpAEZD1loBubq2FxYJe7Xv/61aewwcIsMfnblOIjtM6aFvCAWk6rBbPnZ2RFuprrZ8srC+tlxGeSmYEZCKC5vEFTClFd6tSD1c+NxE+cBn1gcpPqlaJpeoOmn6QWuX8s2CPLlj6dJ1cD6UZfcFbt5CIjjuW5saiED68iTH9KAqXg2gdFcHZtPLC5V0/SCWn7vvPOOmYZmL/CuFsLVaDDRu2LHZdTyCpGqge/HEs7MSKDnwI3jpuAf/uEfnFc2g1HpX7ZIFv0FZYkUNgzKWCQhUJlxGVqzRJas/oacYjIPNicDYQrklphpAzzPQ1CNpp+mF7h+LdsgCBGLSdVA00/TC6wfq9QxkIhZA26DgFHcPM91cffJc3amkIU04MLEyZxtc3VsPrG4VE3TC+rx27VrV0W3e2yfvsZ0Phpy9KDU4+WTqkHIjx4LG2dnJNxzzz3yJ3/yJxUN0pkzKn2ryjMHYPymgslNQPbACTII2iyGZB58zWY0HJCO47fL2EGb5dCZGeBmQSy/2vxu99NseFRkp+aG6hFi9Y9Gg8CfPWS1rDhI9UvRNL1A00/TC1y/vEEgcQ00/TS9wPXj8QAXcLBxTD3kwuLi7pPGAsvNhjTLWWedZZ5tzuWxucTiUjVNL6jXz50O6WsuIY2xCECeghChGEuqBrFj43vERY91MkjC9L73vU/++Z//2X/pDKhsEIxuaTeZDUc3t8uRK3qk946R0raCFDYMyODmDlmybljG9vVI29Ie2XPgvunphMEsiBMyuKZDNt49KH3LF0tPdYqPGeMmaYrVY1b9p2qg6afpBZp+ml5Q0SDgxbbERiPGNIo7UrHeuFRN04ui6afpRXH9Tj75ZLnpppumZhlQyENAF2TWPnmcwMC8kGYLedbZz1weW71xqZqmF0XLj0RLLJzDOgS+lhUzE41Sz7E98sgjZsrh//t//0/++I//WK644grnFDcTSg2Co5dIx5ou6SqVnq3DQrNodHNBevdN6is3Svlh5ZhsP6Z0oXfzCrj/BrIgDq9bIm1r+mTg8dG6sg82ArMzOjs7G6pHv7CdwajulNp64iipfimaphdF00/Ti+L6tWwPQZ66eJpUDVw/phgyvc59ZMBzZHLyu/j7JHeBzXLoa8Ad5uWXXz6nx+YSi0vVNL2gET8GfvIIIKRZYhqPhRpNVJOqQa1j41HB3/zN35jGwBFHHGHKhz70ITPGYOZU9hBMbZ1KKlTSl/fKSHlrqfHQLcN+Q8D+G8iCOP7imIyPj8rI1oK0b/FdZgarS7pJmmrVYwi0fAxBNZp+ml5Q0UPgbI8GxjTQPAi8aNXkswzKpGrg+nGXRVes2yAgkxy5Blz8fTIY8fnnnw9qwGfFILW5PDaXWFyqpukFjfgxSp9siiHNEtNcL5ZRtcRiUjWodWzcCTO1jvn25J9g7Yg//dM/leHhYT8kgVoNgtJFfVeXtK9iQaMlpYv65CJFoQZBIAui6SFY1yd9K9rMYkbNgqmi//Ef/1GxrVY9hkCjQcB0YZ9YHKT6pWiaXqDpp+kFrl/eIJC4BtrHFiIWk6qB60cqYrvssI1799135XOf+9zUXHxXs5AE56GHHgpqFk5WPP8NkRUDqRpo1qWmF2j6WS8G73V2dk5NM43FpGqQcmwXX3yxGe9yuMLAXbexBin1mKqBpp+mF2j6aXpB3iDwiGmgfWwhYjGpGrh+JBBiAKDbQwDkR3fXmvf3yajv66+/PqhZeBxBlr8QWTGQqoFmXWp6QYofWeyytFic68XjH3vhjcWkapBybGgMivUXlzocYFBvqDGUWo/8rZM10icWB6l+KZqmF2j6aXrBgmgQ5GMIpknVwPUbGRkx87r9BgHrHLgnWn+fJEPhgh/SLAxWvO666/zNhqwYSNVAsy41vSDFj9TEKY2yLC96CrJiYvuLaZDlF4tDY2oiAw39vBkLGVYTXb9+vb/ZkFqP+RiCajT9NL2gokHAi22JjUaMaRTu2P1tteJSNU0viqafphfF9WM1wY997GMVswzYziOB+++/P3OfZJEjoVFIs2XPnj3mua+/PRYzE42iWZeaXpQUP6aIkoHO314rLuTFVFRmpfCd8bVa+4tplJBfrTirkWCLqXfsY6HDZ0n67aw6Sa1HZhkwNiOkZcVRUv1SNE0viqafphfF9csbBDU0iqafphfF9XvzzTflhBNOqIpjnQMeCcT2SQ58MtyFNAqLUTEbwd+etb+ZahTNutT0omj6ZXnxvSAbor+91v5iGiXLLxbnamTHpKeA1TsXKsz6IHFUrE5mWo9+iWkUTT9NL4qmn6YXxfVr2UcGIWIxqRpo+ml6ge/Hc1i7dK6N+/GPf2yWp7WE9nneeeeZRw4hzcK+/cc8EItJ1cA/NkssLlXT9IJUP7r5mQLqDhK1WlZcPV70EmVpPjEN6vHz8TXWp2DRqmuvvdZ5VevDEtU0vlmWHPzjdkmtR/7+mcnhE4uDVL8UTdMLNP00vaDikYGzPRoY00DzIPDK1zKYJlUD32/x4sVmTIAbRxciq89ZQvvkWaZ9rOBrFu5o/DTIEItJ1Xjswfx53hfjG5ia9tnPftbkWWDJXFtYHIdtaLyGrlJ6Q0j/yj5cYn5+PVpiMakazMSPBgF1E9JC1PIiiQ25DtzGXmx/MQ1q+YXI0lhr4wtf+ILpoWp16JHhe+r2fGQdN6TWYz6GoBpNP00vWBANAro58lkGZVI18P3+5V/+xUwvc+P4l2WELaF9kjmOucshzXLllVfKHXfc4W+OxtSrcRwMXOSizkX+pJNOkk2bNplGyrPPPjuVhjcGr+G1xLAGAPtgX+yTfccG0fn1aKn3/fvENJiJHxdH/w4wFlePF8mC3KQ4sf3FNKjHzyemkWWR7y95C1oRvpMrVqyoShAGseNOrce8QVCNpp+mF+QNAo+YBtrHFiIWk6qB78cd9Xe/+92quDVr1shvfvMb87OvAYlRmLIY0ixcaFksyScWE9NYyY/556TWZYAbORT8rutmwD7ZN4s/0ZuwefPmqnrzf7fE3n+qBs3wc3tAfM2lES/WwmAsSkizxDRoxM9Sj0b6bMYW0GvgL+k9H3nmmWfM95rU4Px9hYgd92zVYxaafppeoOmn6QULokGQPzKYJlUD348Ln127wI1jjXXbAPM14JktXbMhzUJ8Z2envzkaE9KYOscdH89Ss6bRzSZ44s17sP5+PVpC79+SqsFM/Wjc8f5toyAW14gXya2YTUJvi69ZQnEujfhZGtHoeifZFg1YLrrzDRouJBriMR0ZQP337xLTUuuRz46pxz6xOEj1S9E0vUDTT9MLFkSDIEQsJlUDTT9NL/D9jj32WJPoxI+jkVArGyFToOi5CWmAF3fZ5HZ3ydofuBoJkLjD4/2xXPNcw3vgvfCe3CWgXeo9Np+YBv7nZonF+RqPQZi6FtJcGvXasWOH6eYOaZAVZ2nUD1I01ungO8s0xUsvvdTMTpgrHnvsMZM2nO8Sj97cR1xZ7x9iWmo95o8MqtH00/SCigYBz0VtIfUlxd1Wj0Z57rnnqrbVikvVNL0omn6aXhTf75Of/KR0dnZWxdEg4Ll6bJ8srsI0tJBmvUiiQi+Euz1rf1ajIcJjARLr8OWeb/CeGLjIe6THwH//sWNL0Sj+51ZPnK8xKyRLc0uqF2sL0NUd0rLiKKl+KZr1uvXWW033PA1iLob0dNC4nS0YXMujCwYJfupTnzJrTfA9999frfcf01LrkYyWzNAIaVlxlFS/FE3Ti6Lpp+lFcf3yHgKJa6Dpp+kFvh93JjQI/DhO7nbFO1+z3HbbbeZEGtIALwaf+XfTWfsDGiEMqPIHwc1HeI88Nunr65vaFju2VA38z80Si8vSQmNGXFK9tm3bZrJcsh6Gr2XFQapfihby4nPkO0qXPY08Cj9zAedxA2l9eQ1/K4yZ8Hn77bdN1kQeaTJAlx4TBgQySJUGB48D+J1U4UwjhNh7TNVCxwaxmFQNNP00vUDTT9MLKnoInO3RwJgGmgeBV566eJpUDUJ+XIAZGObGcbd0xhlnmJ+z9smqegzyC2mAF3eljEdwCe3PJklqxXnkpGjmvc9kcF1Mg9DnBrG4kEbCEi5UdJf7mmUmXjQKGvk7hZn4hYhp9XrxuieeeML0JJCT46tf/aqZfcI4DO7y6eqn8DMLedHLwBRWVhClEczy0zQg6vVzSdU0vUDTT9MLNP00vcD1a9kGQT7LYJpUDXw/UhBzMuMOyI3jwsHdL2Ttk8/ktNNOC2qAFxdJMha6+Pujm5nnuqEFVloF3jvHkPosPaaB/7lZYnFZGnkEeNQR0qAZXtRDlubTDD+XmKbpBZp+qV75GIJqNP00vcD1yxsEEtdA+9hCxGJSNfD9aBBwZ8MzTj+O6U8MpMvaJ13D3CHReAhhvWg0uL7u/niexYV0ISxQwzHQ3ex/Ty1Z9QgxDfzPzRKLS9Wa4cV6GDz+CWk+zfBziWmaXqDpl+qVNwiq0fTT9ALXL28QSFwD7WMLEYtJ1cD349nnZZddJkNDQ1VxDHziTi+2z9NPPz1ztLb1Yv8sdmSx++NExAXU5jtYCHAsHJO7fLQlVo8xDfzPzRKLi2k87gnliIBmeTF6PktzaZafJaZpeoGmX6oX31XOAz6xOEj1S9E0vUDTT9MLXL+WXdyI+dN8aeuNSdUo2sfmb6sVk6pRQn633367GVTlx5Hydvfu3dF9MoaArIX+dor1YmCWzXVAsfujd4F51wsNjolj8+sjVo8xjRL63GrF1dJOOeUU0zPka832YjBdlkZptl9M0/SiaPppelE0/TS9KJp+ml4U169lGwT+tloxqRpF00/Ti+L7MXCQjIKMlPfj7rzzTjN3PbZPZhlkLbFrvUgla7MaUtgXvQ+htMYLBY6NqYlufcTqMaZR/M+tnrhaGtPeGKzra832otHI98vfbkuz/WKaphdF0y/Vi7VLOA+EtKw4SqpfiqbpRdH00/SiuH4t+8ggRCwmVQNNP00v8P0YQ8Dqhowj8OP27t1rngPH9kk++1DOdbBeTMtidLZdcY+7UjL/ZTF6TUEW/cWi6bJu+nHDFC/3S881o6UfxmX4Sf617JGelQNSToW0XzZ+vF36Xywro1u7pO+A89JJ9qzrEePwyrCMjOHfI/0vey9KoLOz0zSGLLF6jGngf26WWFy9mjsAEGbDiwWkspgNvyxN0ws0/VK98jEE1Wj6aXqB65c3CCSugaafphf4flwMfvnLX5q1C/w4ElesW7cuuk/2R4KXEK4Xq+OxFgHUyjMwurkofZMX8SkOjsv4wfKPE+PjpgVsfj84IB0rBmR8alzjhAwsL8oQqetf7JOuY9qlYxvNgzHZvrxbhss7MNkTK/ZX+m/k3A7p3TdhvCYOWR9eiz7JoZIvv1v/CBwjU9IssXqMaeB/bpZYXD0aK+l9/etfr9BmywtIZOUzm34+ml6g6ZfqRYPAbxRaLSsOUv1SNE0v0PTT9ALXr2UbBPlaBtOkahDyY+EXcr37ceQ3Z8pgbJ9oTE+0CVdcXK/+/n7TRc3z9dDdiAsNgt4nx8xF21y4uRqPD0rxmD7Zf6BPCquHZLx0sS9uHpWJAxulbWmPjDiTFEa3cGEvNQG2sZ8hKS4fKF3gh6SrFCcHS/tZ2i0Ddw9I7zEdsr3UVhhaVWpAlBoMgyvbpLhjvxyYbJAMrVokbev7ZXBzhyxeR1NiVPqWdUhfKbZn2SJp35zdqLGQKteOk6hVj1kahD43iMXVq/Fo43vf+96UNptezOn3ewtm089H0ws0/TS9QNNP0ws0/TS9wPVr2QYBzz3yWQZlUjXw/chGyIW/s7Ozas0BYDsj57P2iR8pjA8cqO6Ld71oDNAoIEmRu757iNHNBSluHZTBu8vFXuzHdxZl0aIuGeLOfLJBULpsS3HVkBsusq9XittGZWAVPQKTPQaPd0vxDloWpTv7n+4p7bdfuv+pffLCT4NgumfC/ju0qiADphdg0gPPTZON0r3ddTUIOFabmCn22cQ08D83SyyuXu3VV1+t0GbTC8iU6DLbfi6aXqDpl+pFD4HNSuprWXGQ6peiaXqBpp+mF7h+eYNA4hpoH1uIWEyqBr4fYwhoEDDoL9RtyEp2P/vZzzL3id+VV15pRpL7uF48LuCxAcsJ1yL4yKDE/k0d0n5Mu7n7jzYISs2A7jVFKdIjIOWegsLx3aY3QJ7skfb1e2T0tYnS/uyFP6tBUN4+5fFyn3ScO1K22FWUJXU0CMAec+yziWngf26WWFwjGo1B7ZUcb775ZvOvlh9oeoGmX6pXPoagGk0/TS9w/Vp2lsGvfvUrk8Sm3phUjaJ9bP62WjGpGsX340LOlE7uEFiBzX89OQS4u8/aJ9vvvfdek3LY11wvBhaSgIgUv7UY3dwuS1Z0SdeaybJtxNyRt3ExPjQivR8v3fm/YBsEw9LzkTbp2V3ZuzG06ggpmB4BMY2HxR/ZKObevvRzYVmvDFzTI8Wjlkj33ukL/9hNBVm8bLs8kNUgkHEZWtsmhZVF6finD8iSLfU1CHikQjrf2GcT0/y6rDeuEY3Bj4wF4efZ9qIwu4GBrMxQ0fCzRdOLoumX6sUsA84DIS0rjpLql6JpelE0/TS9KK5fyzYI/G21YlI1iqafphcly++GG26Qu+66q2p7Vo4CW9jOLAWmEfqa78XMglNPPdW/XrYOE8PSf025h2C81Hjo2u3pGZx33nlmZkXss4lpobqsJ65RjemBzzzzjIoX5fe//70Zu6DlR9H0omj6aXpRNP00vSiafppeFNevZR8ZhIjFpGqg6afpBb4fd2g8MiAXAcmDfMhox9TDrH3iR8Kozs5OX6ryYsYCg+xambGdvdK9tlt6tg7L9Ar2cRg78eijj0Y/m5gGfl1aYnGpGrNLQsRiUjVgNkYoJ0UsLlVLrcff/va35kRKg4nGHUsGs34FhW2Mw3jrrbf80GS/FC3Vi0cGnAd8YnGQ6peiaXqBpp+mF1Q8MnC2RwNjGmgehKYXaPppeoHvZ8cQMPjtggsuqNCAtMRcyLP2af06OzvNowcX34vR5Tad7eEEDS2ez8c+m5gGfl1aYnEpGtMCacCEyIqBVA2YhcEYlu985zsV22NxqVqsHrmo8zmRRInHJzziKhQK5t9Vq1aZFQ+/+c1vmpkSPGLbtGmTycFx9tlnm54vHg0xXoQYZu187WtfM/uiAeEvmxx7j6la7NiyYtDyMQTVaPppesGCaBDkyx9Pk6qB70c2QhoEXPjXrl1boQHdukw9zNqn9WMkvT/o0/fiburCCy+s2HY4wDFz7LHPJqaBX5eWWFyKxkWRCyCrVPpkxUCqBhwbjclf/OIXFdtjcamaW4/Mnrntttukq6vLXMRZ3viqq64yvWL0CMwUBtLed999ptFAKmvqlUdrrOsRe4+pWup3hAYB5wGfWByk+qVoml6g6afpBQuiQUB3nX/BicWkaqB9bCFiMakaZPnR2PKXKbYUi0V5/fXX/c0G68cYhF27dlVovhcnYFZQbB57pGcqo+FiaV/TL8P19uMrwjFz7LHPJqaBX5eWWFyqxiDRELGYVA3cY2O2AxdRiMWlavv27TOzYj7zmc+YMS2k3g5Nt50tGLx56aWXTvU6+FMwIfb+Y5rmdwQ0/TS9QNNP0wvyBoFHTAPtYwsRi0nVwPfjIk4PAXEnnnii+dmHrk9meISwfoxS9ruafS/gTqx5VE45nNhZlDY78t9mI3RWZp4YJ9GRzTo4IeMlceK10jZe5GckbCL2mGOfTUyDUF1CLC5Vs14sfe0Si0nVwD02xqOwgibfy1hcoxqPAujSJ3Ok33CdK/h+8gjN9hzY7J2h92+JaanfEXoIQnUSi4NUvxRN0ws0/TS9oKJBwIttiY1GjGkUzZGReJFed2RkpO6YVI2ifWz+tloxqRrF96NXgO5aYrq7u023rR/DoMKsFQ2tHwOrSFAU86IwqNDvGk6n1CBYUb7DGxsblT3rl0gHUw0PDknX0i7pv3tAupcWZPsrpev97i5pW90vgzu6pW3ZdhkjdlFBNu4ovebji2TJ2j4Z3FSQJRsqs2HOFI6VY3bryq+TWlpWXdaKS9XwYlGrq6++uu6YVI3iHxsnLBoGsbh6NQYr0hvAc/9mPAaYLRjDQ4/B6tWrzXmunmPzi1+P9cSwnWmHPDYJaVlxlFS/FE3Ti6Lpp+lFcf1atkGQL388c43i+23ZsmWqQfCtb31LhoeHq2KYdrhjx46q7RTrNz4+bu7AYl4UehKYhtccShf1pT0m62DPx48sNQYmnxfsKkpx5+RLyCi4ZdTkFBhkfYMSw2vbpe/lUuzxA6ZHgEREG02ixVCSo5lBndo53rHPJqZl1WWtuFTN/r3xqIOGeD0xqRol69ieeuop06Xub6+1T7bTI8Bze/JotBJk/GQsA41zGrqhY8s67qx6jMWwnQYB54GQlhVHSfVL0TS9KJp+ml4U169lHxmEiMWkaqDpp+kFMT/uCHfvrp5czwWN0dIhXD9GKv/617+e0rK8jjvuuKpR12k4F3ASFn2kIAO0CXZ3lXsKoNQ4aN82JkOrOybTEJNwqF22j03HTmdGbG6DgGPkWC2xzyamQVZdxuJSNU0vyPJjUB6NEnqnfLL2ydS/U045xTzmCq2v0SrQY0CDhp4al6zjhqx6jMWkaqDpp+kFmn6aXlDxyMDZHg2MaaB5EJpeoOmn6QW+H6Pf7RgCpn3deuutFTqQuphpYSFcP7pl2Z/F97IwaI0755njXcD3dsuRKwdl/NB+6VveLj0beqT9qC4ZopFwoE86ju6R3nXtsmTNEDkHZ71BwDGS38ES+2xiGmTVZSwuVXO9uEu3q1TGYlI1iB1b1uMl7oD8fTJ6n8cDzBJYKDAAkcGPNsdBrC5j9ZgVg8YYAvfv1tWy4iDVL0XT9AJNP00vyBsEHjENNP00vcD3s3kIiOOEGupi5VENzzdDuH48VrjnnnumNN/LQgwDGLnYLFQ4Nn/WRuyziWkQq8usuFTN9WKwGWNIIBaTqkE9x/b444/LE088YX7+6Ec/ah4luPsk1wPP3xciP/jBD0xDh7VGYnVZTz36oOV5CKrR9NP0ggXRIMiXP54mVQPfj0QrtkHAwEAWM/JBy5qB4PqRhtZtUPheFmI4CdGd/t577/lyy8MxcWw0pFxin01Mg1hdZsWlar4XvUbkJYjFpGrg+1ncuBdeeME0Xrdt2yYf+tCHzNx+q5FQ6/zzz3dDFxwMiOQRAn9jWXVZTz362L9FzgM+sThI9UvRNL1A00/TCxZEg4CBEPm0wzKpGsT8eP7PaGMfNFYqtF3Hvmb9mDZFNjdLzIsYeiSYBrbQ4Jg4Np/YZxPToFZdhkjVNL2gHj8aowxu/cQnPiFHHHGEHHXUUUbjMZXtwVjoHDp0yDQKGPgbop569EnVQNNP0ws0/TS9IG8QeMQ00D62ELGYVA18Py7itoeAuOXLl1cN+ENj4Zu9e/dWbLea9SOO+Hfffdf87ntZ3BgWVFpIXb2MEA/l5YfYZxPToJ669EnVQl5XXHGFuRBlxcT2F9Mg5Adu3Mc+9jH54Ac/aBoDlL/6q78ys1WYv384QcZW8haQ6Mqnnnr0QaOHwOZA8LWsOEj1S9E0vUDTT9MLKhoELFxiCwN2KO62ejQKiWr8bbXiUjW8aAyQE7zemFSNon1s/rZaMakaxfezzyZtHI8GGGDk75PBTaR19ffn+zEAikFdIa+sGJ7/kvs99iWe7/DeOQZmamTVv3/c9WqUeuuyGVrIi0YOo/ezYmL7i2mUkJ8fx0wDvoOf/OQn5W//9m/lfe97n/zd3/2d/zEcFjDIl0dSfn3VU49+YTsLNfF3H9Ky4iipfimaphdF00/Ti+L6tWwPQYhYTKoGmn6aXuD7MZjI7SHgjosGggvaQw89FFyYyPfjpG1HefteFj8GiOHOpxUHGvKeee8cQ+jYLKkaNFKXllQty4vn+Fkxsf3FNMjyy4pjLNHf//3fm+/a4QpZQa+99tqKbY3WI6Dlgwqr0fTT9IKKHgJnezQwpoHmQWh6gaafphfU8rv88svNiG5fIwd7qHvW92PqIgvGQC0vH1Llktxo8+bNvjRv4aLEmAGb5jfr2CBVg0brElK1mJc/UNIS219Mg5hfKI7eitBsmMMNGqGMK7A0Wo+QqoGmn6YXaPppekHeIPCIaaDpp+kFvh+9A24Pwe23326WwHVBI2ta6Fm/70f3I8vCgu9l8WNc0LZv325OdmSbm6+QwIn32EjimFQNUusyRYt5sarlY4895kvR/cU0iPmF4qj3nPJS1SzUZGm0HgGNLKO1ZhCFSPVL0TS9QNNP0wsWRIMgX/54mlQNfD83DwFxZCXkObiL1bgTtglSfM1C9yPPmsH3svgxLlYjy1xvb695Lh+6AM0VvBfeE7MuQpnw6jm2EDENZlKXIWJazIvHI6GkUrH9xTSI+flxrFZ49tlnV2w7nPn0pz899XMj9WhByx8ZVKPpp+kFC6JBkM8ymCZVA9/PbxA8/fTTVXO6rcaUQn80csiPbn8aDr6XJRRj8TW6qBlNziCq66+/PngRnm3wZP4774H3wntqxrG5xDTQ9NP0gkb8vv3tb5tEPZXskZ511dM8m8Xw5j4ZeaRncpntUvlIu3RtHZamrLQ9PizD1bN5pd5jovHNuREaqUcLWt4gqEbTT9MLKhoEvNiW2CIIMY2iuSADXvlqhzPXKLX8qOdTTz01qDHn2w6c8zV327p168zAxFpe/vZaGglyuDtfsWKFmZMemnrVLNg3HieccILxZFyE+16afWwxjaLpV4/X4OCgvP3220HNLzGNUo+fLcyKqSYj5fRBb0nrQxMyzsqYk2tiTzhrY0/9PPmaaWlEes8dKi+YNbVC8IQMrmyTvpcnf63wqbGstv8e7uiQjh2TeoV3+ZjMkt3uGt4efEcZU9FoPc5Uo2j6aXpRNP00vSiuX94gqKFRtI/N31YrJlWj1PL7/e9/by64IY1n+wwaDGnuNlZO4xl7LS9/e70aF2vSJHd2dkqhUDDLC3MHz3RJ3n+jEEMs+2Bf7JN9c7INrThHma1j87fboulXjxeDKckhEdL8EtMo9fjZsmzZMv/jk1CDYGJ3t7SvKS93XVjN2hX7ZePSDum7e1D6V7VJz5Ole/BTi+UFrw4OSEfpNSyb3X305LLZyybXwHixT3pZKKvUIOjYwTLbpfJy6e79qPJiWdU+sWW1/fcwIfs3tUnb+hEZc5fsxvsg+ym95ppB6Vu+2LzfEIzZufDCCxuux5lqFE0/TS+Kpp+mF8X1a9lHBvkYgmlSNfD9/EcGwLoFb7zxxtRrrMYgv6zxBS4s6EPjwfeyhGIsKRrLt+JJ2mXeO137XNT5l3EPDIbkQk/hZ7bZ1/AM9qSTTjK9H+yDfVmy/EDr2CyafvV4/epXv6oY2xHbX0yDevwsfGbVVDcIhlYtlq6bBmWwdPHtWcrqliLjLw/LUOmC27f8yPLd/t5u6dg2VrpLL0r342Iu+ovXbDcxg+vbzCqZY9t6TSxa2/rS9mt6pO2vS42B8qSSgE98We2q92B7HtwVOg+WTujETO6nsneiEpZLtunGG6lHC1r+yKAaTT9NL6h4ZOBsjwbGNNA8CE0v0PTT9ALfL9QgYOlYN02x1UiI4uc8D/nRk8Ozdt/LEoqxpGoQ8qOlzF0dF3r+ff311802S2yfMS3kBbGYVA00/TS9oBG/uhsEqwvS/2L5jn5k77CMv7JdOpZvl/103+8sTl5g90vvqh7pWdkrI/xauigXto6WewH2DcvwaxMycG6fjKK5F+V9vbK4dLGmA6HK51BkFc2xwHsINAjG9u2R/fQQ2GOKNAj4m2TmBzRSjxa0vEFQjaafphe4fnmDQOIaaPppeoHvxwA5v0Fw0UUXma5Ii9V47cknnzy13dVcuOhyJ+57WUIxllQNNP00vUDTr14vHt2sX79e/u///q9Kc4lpUK8fuKPqpyldPP+6XbrWdJmycffY1HLXfaWLcjvLXdsu+R0bpWvZYtMzAKObF8uRpju/hF02e3OfFI/ukqHXhqR3g2kqVF2Uh9eW7vB3jlf7xJbVDr2HJ3tk8cd7ZOgVZ8nu5X2y32lY82FG1QAAUBBJREFU+N4uPJoj0yc0Uo8WNBoEofwSsThI9UvRNL1A00/TC1y/vEEgcQ00/TS9oB6/G264wSx7G9IYZMfvIc2Frns/46ElKwZSNajn2HxSNU0v0PRrxIs8DDfeeGNQs8Q0aMRvzZo15gKWU4bG+/e//33zcyP1aEnVQNNP0ws0/TS9YEE0CPLlj6dJ1cD3Y0aA30PAs3RO8hZXO/300yv2keVHDgFyGoTIioFUDfxjs8TiUjVNL9D0a8SLpDZ2toGvWWIaNOI3MDBgBnvmlHGTNDVSjxY0GlicB3xicZDql6JpeoGmn6YXuH75LIMaGkX72PxttWJSNYrvt3LlStPl68ax5jrZBkP7ZB165oGHNLcw+NDeOfolK2YmGsU/tnriUjVNL4qmX4oX+SuytFgcpRE/GiDhqYeHH+QE6ezsTKpHV2N8Dcueh7SsOEqqX4qm6UXR9NP0orh+LdtDwEHkiYnKpGrg+9Gt7/cQsFLWWWedNfUaV+MiTw9CSHNhMSRG7ofIioFUDfxjs8TiUjVNL9D0a9SLk0+xWDTfmxBZcZZG/c444wz58Y9/7G8+7Ojp6alI0tRoPQIaPQShx3uxOEj1S9E0vUDTT9MLKnoInO3RwJgGmgeBV/7IYJpUDerx4y6ss7MzqA0NDcl1110X1Fyef/55k+AoRFYMpGpQz7H5pGqaXqDpl+JFXn1/9T1LLA4a9eP1JIs6nGHlSabPujRaj5Cqgaafphdo+ml6gevXsg2CELGYVA00/TS9wPdjUJLfQwA2ORG4Go9t3Fz2fpyFvBGf/exn/c2GrBhI1cA/NkssLlXT9AJNP00vSPG7+OKLzeDXw5Xly5dX9cik1CMaPQScB3xicZDql6JpeoGmn6YX5A0Cj5gGmn6aXuD7hfIQAHf3r776qvnZ1X7961+bBD8WP87lxBNPDKYXjsWkauAfmyUWl6ppeoGmX6oXvXihXoJYHKT6dXR0mLELhxsXXHBBsDGUWo95HoJqNP00vSBvEHjENND00/QC32/Pnj3BBgGJhX7605+an32Nu5N33nknqLnwjPMnP/mJvzkak6qBf2yWWFyqpukFmn4z8WIWiv3e+FoWqX7MnWeAIQ2RwwUygLrje1xS65EGAecBn1gcpPqlaJpeoOmn6QUVDQJebEtsNGJMo2iOjMSLkfCcAOqNSdUo2sfmb6sVk6pR6vW7/PLLTdKTkEYPASOcQ5pb+vr6KvLdZ3k1Q6PUe2zN0DS9KJp+M/EivTXZ80KaH2PLTPwYV8TUO/ax0GFAL397WXUyk3r0t9fSKJp+ml4UTT9NL4rr17INgnza4cw1iu/HksL+tEPK7bffbhYQCu2TMQTDw8NBzS2swsYiOP72WEyqRvGPrZ64VE3Ti6Lp1wwvBqZmaX6Zqd/Pf/5z01Pwwx/+0L+GLhh4xk9K8VidpNYj0w45D4S0rDhKql+KpulF0fTT9KK4fi37yICDyKcdlknVwPfLGkNAF+KVV15pfvY1VgV84IEHgpoLGdTOPPNMf3M0JlUD/9gssbhUTdMLNP1m6sWF+etf/3pQCzFTP3jrrbfMmJXQGIZW5g9/+IN88YtflKuuusr87h+3S2o95mMIqtH00/SCikcGzvZoYEwDzYPIGwSVpGrg+915553BBoG7aIqv0RigURDSXOjVYeCXTywmVaOX49577zXPWMmS+KUvfcnMciD3PUvm2mJXOETjNeeee645Fhov7MMl5ufXoyUWk6qBpl8zvPgMSHAV0nya4Wdh6e0vfOELZjXGVmdwcNB8T92ej6zjhtR6pEHAecAnFgepfimaphdo+ml6wYJoEOTLH0+TqkG9fswmOOWUU4Lavn37zEjnkOaCV3d3d5VnLKZejX2SR5+LOhd5ljAmuyJJk0iyQpd1LXgNryXmkksuMftgX+yTfb/00kuZ78U/Jku9798npoGmXzO8nnrqqUzNpxl+Lgxq/PznPy8XXnihL7UEfCeZ9ktWUJ/YcTe7HmMaaPppeoGmn6YXuH4t2yAIEYtJ1UDTT9MLfD8WMQr1EBw6dMicVMHXGNV92mmnBTUXvLjQ7t27t2J7LCamsSQz88+PO+44s+rid77zHfntb3/rv2zGsE/2TTpXehM2b95cVW/+75bY+0/VQNOvmV4MMszSLM30A6vdd999ZmwBvQZ8n+c7zzzzjPlef/nLXzaJh0LEjju1HukhcBczc7WsOEj1S9E0vUDTT9MLXL+8QSBxDTT9NL3A98saQwAsYcxSxr7GlEOmHoKvueBFVyQL0rjEYkIaFxUaJzxL5Wdt8MSb92D9/Xq0hN6/JVUDTb9meT333HNmVHxIc2mWn8XX6HonsyHT9bjozjdouPD3RG8aGT799+8S01LrMR9DUI2mn6YX5A0Cj5gGmn6aXuD7bd26NbNBwMAw7lRCGif6sbGxoGbBi+effha0WIyrMVaBOzweT7z22mveK/XhPfBeeE+33nqrLxvqPTafmAb+52aJxaVqzfRilgmPZEKapZl+kKXxGIPvLdMUeV/MTpgrHnvsMVm7dq35LvE36D7iynr/ENNS65EGAe/BJxYHqX4pmqYXaPppekFFg4DnoraQ+pLibqtHo9D697fVikvV8GJAIUlu6o1J1Sjax+ZvqxWTqlEa8TvnnHNMHoGQxgIzdDOGNNeLxVfohajl5WrkP+CxwDe/+U3z5Z5v8J4YNMd7pMfAf/+xY0vRKI18bjPVmunFY54szZZm+tXSrBcNOrrnjz32WHN3TC8WA5dnC85dl112mRkk+KlPfUq+8Y1vmAXA/PdX6/3HNM16pGj6aXpRNP00vSiuX8v2EOSzDKZJ1cD3+9GPfpTZQ3DzzTfLzp07gxrTu2ziIl+zWK8TTjhB3n777antsRjGHDCgisRH8x3eIyPaScBkiR1bqgb+52aJxaVqzfbiO+Y/NnJptl9MC3nxOd5yyy2my55GHoWfGXvA4wbeP6/hLt5m6HThu03SNJIkPf744yZ/BwMCGaRKg4PHAfz+8MMPm2mEEHuPqVro2CAWg0YPAcfoE4uDVL8UTdMLNP00vaCih8DZHg2MaaB5EHmDoJJUDXy/2BiCBx980EzJC2l0A5M5LaRZrBcJVWixWkIxb775pmk4tOI8clZ/5L1zDKFjs6Rq4H9ullhcqtZsL/52aeRRPyGa7RfT6vXidU888YTpSWAZ769+9atm9gnjSLjLp6ufws+sOkgvA1NYr7jiCrnnnnvMTBwaEPX6uaRqqV75GIJqNP00vcD1a9kGQb788TSpGvh+dMtnNQhYc56pWyGNO4oszWK9OEnyzNTixzBOgee6obuUVoH3zjEwZSyrPvzjdolp4H9ullhcqjYbXjQef//73/uSYTb8sjRNL9D0S/WiQcB5wCcWB6l+KZqmF2j6aXqB69eyDYIQsZhUDTT9NL2gET+6Sck0GNJI/MJCNiHNYr147MC8fosbw/MsLqQLYYEajoHuZr8nyxKrq5gGjXxullRtNr1C00Rn089H0ws0/TS9QNNP0ws0/TS9wPXLGwQS10DTT9MLfD8u+lk9BHTx8hw0pLGNbvKQZrFejO4+//zzp7bbGO5MuICGlkhuVTgWjolj84nVVUwD/3OzxOJStdny4tk6OfN9ZssvhKYXaPqlevFdDY3ZicVBql+KpukFmn6aXuD6teziRv62WjGpGkXTT9OL4vutXLkyuLiRLQyaI0NkSCNPAQujhDTXixMOz1/tduvF81fmXS80OCaOza+PrDqupVH8z62euFRttrz4HvF9IqW1q8+Wn7+doulF0fRL9eJvmCRcIS0rjpLql6JpelE0/TS9KK5fyzYI8uWPZ65RfD9OBLEGAaOtma8d0hhExXSqkOZ70fBgHj8/83qmXbEa4kKFY2NqolsfWXVcS6P4n1s9canabHodOHCgSp9NP79oelE0/VK98gZBddH00/SiuH4t+8iAg/CfzcZiUjXQPrYQsZhUDRr1IxHPk08+GdSuvvpqMxMhpIHrReOBAXdAI4LMfwudzs5Ok1ffklXHENOg0c8NUrXZ9mL0PfkJLLPt56LpBZp+ml6g6afpBZp+ml7g+uUNAolroH1sIWIxqRr4fowfyBpDAEwDJAFRSGOONqsLhjRwvdjP0NCQ+blV8gzMFI6RKWmWrDqGmAb+52aJxaVqs+313e9+1+SbsMy2n4umF2j6pXrRQOMc4BOLg1S/FE3TCzT9NL3A9csbBBLXQPvYQsRiUjXw/WJ5CIAlhZkhENJIS8zUw5AGrheNARoFPF8PzXleqJAq146TyKpjiGngf26WWFyqpuF13nnnTfUYafhZNL1A0y/VK89DUI2mn6YXuH4t2yDIlz+eJlUD369Wg4A17VntL6RxB0w+9pAGrhcnfx4bbNiwoWJ994UOx8oxQ1YdQ0wD/3OzxOJSNU0v0PTT9AJNv1SvvEFQjaafphe4fi3bIAgRi0nVQNNP0wsa9ePulkWOQtpbb71luv9DGrheJKWh8cFywocb9piz6hhiGjT6uUGqpuXFoygy+mn5gaYXaPppeoGmn6YXaPppeoHr17KzDPxttWJSNYqmn6YXpVE/7h6YXhjSKAwO5DX+dorvRYOA6XipjO/ulcLH26TtqHbp2jlmto1e0yP9L1e+riYv90vPNRljGGJaIu7Uzax6jGmhuqwnLlXT8tqzZ4+cffbZan4UTS+Kpp+mF0XTT9OLoumn6UVx/fIGQQ2Noumn6UXx/WrlIaCQs51FWfztlPXr18vTTz9dtZ3ie9EtuWbNGv96WR/P9krbqiEpLxI7LkOr2mTjgVKDYHNR+g5MyPjYuEwcmn75xPiYjL02Mb3hEK8Zk3E2vdgnxc2j5de9VhlnXnew9O/BcVMf/n5T4Hm5nZ6ZVccxLVSX9cSlapperPin6afpRdH0S/XKpx1WF00/TS+K69eyjwzytQymSdXA96s1hgA4Wbz66qv+ZgMrwrEEcAjf66yzzkpuEOzfVJDeff5WGgTtsnjVRhnY0S1ty7YL/QajWzukY8OADG7ukMLm0nfm4JB0Le2S/rsHpHtZlwz9jAbBfhm7oygdWyq/U7axENpvKv39/fLoo49G6zimgV+XllhcqqbpBZp+qV6kXOZE+swzz5jGHVNxWb+Cwjb+PniE5pPql6KleuVjCKrR9NP0AtevZRsE/DHmswzKpGrg+5HsqVaDgG5dv+4tJOBh6dgQvtcNN9xgFjpKYeTcgukR8BndbBsKo9K3qq/0/9K/S0uvvXtQBu/ul+JHu2V4d5d03DHZW3Cw1HouXfQLR7XJouO3l3scXhkpvZbXj8jYVIPA328611xzjWk0xeo4poFfl5ZYXKqm6QWsl+EufmWJxaVqsWPjos7nxHLWJ598slljo1AomH95NMaKhywCxOqHF110kWzatMksa8zfx6mnnmoeDTFehJjPfe5zZpVP9kUDwl82OfYeU7XYsWXFoNEg4DzgE4uDVL8UTdMLNP00vSBvEHjENNA+thCxmFQNUvwuvfRSc4cbgvXfL774Yn+zwfeyKyQmsaso7Vun79PHtrZLcddE+ZGByXHjNAiO75ahsTEZGxuV4b2jMuE0CMb27ZH9Pys1CNYPy/5N7dK1e4LnC6XX8vpx01goNwj8/abDMXPssTqOaeDXpSUWl6ppegEXYTJi+sTiUjX32Fh34rbbbjM9YFzESa991VVXyX//938HF2FqFJIv3XfffabRwNgZGhZk6GTsROw9pmran5umn6YXaPppesGCaBDkjwymSdXA91u3bl3NHoKbb75Z7rzzTn+zgc+EO7wQvhcn4C9/+csV2+pnXIbWLJYlx3dJ18p2WbKmPJ4gdOEe39Ul7aWf+9a1lx8JHNovfcvbpWdDj7Qv75P9L0yOITg0Ir0f75IhxgxYZqFBwDFz7LE6jmng16UlFpeqaXoBfm+//ba/ORqXqjGr4corrzTjYrjrHxgYMI1BLchcSQObxsGqVatMkiaf2PuPaamfGz0EnAd8YnGQ6peiaXqBpp+mFyyIBkGIWEyqBpp+ml7g+9UzhoD0xNw5hWBA4oknnuhvNvhewJ3Y4YY95lgdxzQI1SXE4lI1TS+wfiyF/d57701tj8U1qtELQZc+mSOZ7jgfoCGydevWqZ4Dm70z9P4tMS31c8vHEFSj6afpBRUNAl5sS2w0Ykyj0IXvb6sVl6ppelE0/TS9KL7fyMhIzVkGw8PDJrmOv51CDI0Kf+Epiu9FIXPfL37xC+frubDhWDlmW1dZdRzTsuqyVlyqpulFsX7cOd911111xdWrMcaF3gCe+zfjMcBsQQIregyY4svfZD3H5pfUz41ZBniGtKw4SqpfiqbpRdH00/SiuH55g6CGRtH00/SipPhxUWPQlL/dxvX09JiR1r4W8nr44YfNNLzDhW9961vmmG1dZdVxTMuqy1pxqZqmF8X68cydAZhvvPFGzbhaGj0CPLe/7LLL/I9kXsNqkIxlYEwFPQihY8s67rn63PwSi0vVNL0omn6aXhTXr2UfGeSpi6dJ1cD3Y7R0rUcGZBmkuzUEcYykZnChj+9lOe6446pGXS9EOEaO1RKr45gGWXUZi0vVNL2gmX5M/TvllFPMCH9yZ7Qq9BjQoGEdEZes44bUeuSRAecBn1gcpPqlaJpeoOmn6QUVjwyc7dHAmAaaB4EXrZp8lkGZVA18v3rGEKDxOhoGPmi333676Zr18b0sLJjEnfNCh2O8//77p36vVcdZGmTVZSwuVdP0AtePR08sgsVjrFhcSGP0Po8HmCWwUOAxCoMfbY6D0HFbUj+3fAxBNZp+ml6QNwg8YhpoH1uIWEyqBr4fJ9F6GgTMwX7ppZd8yWjMIQ91zfpeFmIYiPjUU0/50oKBYyMLpEutOs7SIFaXWXGpmqYX+H7cFd94443ROF/jUQPP3xciP/jBD0xDhwXC/ON28evREotBo0HAecAnFgepfimaphdo+ml6Qd4g8IhpoH1sIWIxqRqk+n3729822dl80BgdTgpjn5gXJyG6091R5QsFjolj85O91KrjLA1idZkVl6ppeoHvNz4+bqazxuJc7YILLpDzzz/fe8XCggGRPEJg9dGsOvHr0VJvPfrENND00/QCTT9NL1gQDYJ8DME0qRr4ftyJ1dNDcN1118kDDzzgS0Z7/fXXzQhpH9/LYr24K2Ea2EKDY2r0jiumQa26DJGqaXpBlh+DC7Pi7D6ZPXDJJZf48oLk0KFDplHArJ8QWfUYq380GuecB3xicZDql6JpeoGmn6YXLIgGQYhYTKoGmn6aXuD71TuGgOf+/gAnqxHHft58880KzfeyuF5MM1tIXb2MEA+Np4BadZylQT116ZOqaXpByI9R0sViMXOKKvvcvn27mb9/OMGNEXkLSHTlE6pHiNU/Wj6GoBpNP00vqGgQ8BzYFv7YKO62ejQK3cT+tlpxqZqmF0XTT9OL4vuRtY1nk7E4tnPhJmd7SKOQ+52xBDEvP8b+ziAycr/HvsTzHd47x3D11VdH6zFFo9Rbl83QNL0oWX4sCnXuuedWbaeQLIvn6jWZGJLuFe7iVGMysLJbbroyYdlsh7GdPVNLcRe3Dk+uwqkDU3x5JOXXSVY9xuqf7TwK5DwQ0rLiKKl+KZqmF0XTT9OL4vrlPQQS10DTT9MLUv2ef/754DgBG8fUQ390dyNexHLn04oDDXnPvHeOIXRsllQNGqlLS6qm6QUpfnSdc2dbD3tOLUj/K5O/vNIvhVP3lJe3tstaH2Qti9Lvkz+bJbJLv43b5bPd16Ls7pL2C/aXXy+sxLlEOnZOTC6dPblkNq9jCW67X/ZX3rFZO8P8yH6td4NLbNNYoiHtklKPqRpo+ml6gaafphdU9BA426OBMQ00DwKvfC2DaVI18P1I41rPIwMGyNEd7mPjmHr4ne98p0LzvSxZXq+99prJd7B582ZfmrcwLYwxA7x3yDo2SNWg0bqEVE3TC2r5kdgJGDxIo4tHMqFZLZk83i2FyYWxRjd3SO+z02tgTOzulvY1/TK4o/Sa1UMyvrdbOraNmTUtFr+/W3haP7S6S4acC/bQqnbZHlr+oBTT/jcd0rNhQH5UajS0rS7vt7x89pAUVw2VX7erKMVdvIclsmg5S2z3SvvyxpfYphHKuAJLrXoMgUbDKpTOORYHqX4pmqYXaPppesGCaBDkswymSdXA96t3DAHaCSecYH4OaQyi85c29r0stbx4NszJjmxz85WHHnrIvEd/XEWtY0vRILUuUzRNL6jld/rpp5tHVn/5l39p6pt6b4z90rucBapK/x7TK9xW2AbB0KrF0nUTS18PSs/S0oX+lT3SvWpAxu7oKV3Au0qvGZae1QNTvQEwtKoog+Y6PCr9yxbJokXvlyUslsWy2ueOeK8RGV7bLn0vhxoExaklvTMbGRHuvvtu09VvqVWPIdDyMQTVaPppekHeIPCIaaB9bCFiMaka+H4srlJvg4D0xX43rdV4pHDOOedUaL6XpR4vssz19vaa5/KMTZgv8F54T6zwGMqEV8+xhYhpMJO6DBHTNL0gy48lt1k6+LTTTpOPfOQj8kd/9EdmVgFjWRrF9Axs65XiVE/BZINgdUH6X6Rrf0xG9g7L+CG2FaV4ao8M7yu9fnmHFCeXzraw9HbhDmfUwIGN5dUzJ1fKhKHVHTIwuYpm+WJfahCsHCxv2FmcbBAUpHdfedPgysLU6xvh05/+9NTPWfUYq380/qY5D/jE4iDVL0XT9AJNP00vWBANgvyRwTSpGszEjwWOshplZJbr7Oys0GbiZeFRBWsfMIjq+uuvD16EZxs8t23bZt4D74X31Ixjc4lpoOmn6QVZflu2bJE/+7M/Mw2BI444wpSOjg6TqKdhXumX9j+evkhPLW99oE86ju6RvtLv7ZNLanMHf4S5mx+W7vcHLtSHxmRwzRJZfHTRLMW9+CMd0rdvoqJBYPfbu84u1T0m25ctkcKaohSPWjTZIGiXRUd3lV7TIe3rhyt6IeqFNM3cLEFWPcbqP1UDTT9NL9D00/SCigYBL7YltghCTKPwJfS31YpL1TS9KJp+ml4U34+Ta63VDq12+eWXm67ykMbPPP8nqUyWVyjGLzHt1ltvNXfnK1askFtuuSU49apZsG88eEyC52233VbxXpp9bDGNoumn6UWJ+b3wwgvmsdYHP/hB0yD4x3/8R/+jalmmGiUzgL8JxlTUqses+mc7qx1yHghpWXGUVL8UTdOLoumn6UVx/fIGQQ2Noumn6UXx/UivW2+DgIuif2F048444wyzWluWVyjGL/VoXKx37NghnZ2dUigUzPLC3MH/6Ec/Cq63UAtiiGUf7It9sm8aBKEV5yizdWz+dls0/TS9KPX4MZ3zz//8z+XDH/6w//G1LGO7t8tQg+MGfJgyeOGFF9Zdj35hOw2Crq6uoJYVR0n1S9E0vSiafppeFNevZR8ZhIjFpGqg6afpBb7fN7/5zbrHEPD83H/W6MaRMc7tzvW9LPV4hcjSOKGxiBBjGMiYSNc+F3X+ZQYAiY+40FP4mW32NTyDPemkk8yzafbBvixZfqB1bBZNP00vqNePMQX//u//7rwihwa4HbtTbz26oDGGgPOATywOUv1SNE0v0PTT9IKKRwbO9mhgTAPNg8ArT108TaoGM/H72c9+ZsYRhDTgrv2ee+6Z0mbiFSKmQciPljJ3+Vzo+Zc0y2yzxPYZ00JeEItJ1UDTT9MLGvGjEZczjfs32Ug9WlI10PTT9AJNP00vcP1atkFAN0fWgLYQqRpoH1uIWEyqBr7f6Oho3T0E3Ekw0yCkwSOPPCJXXXXVlOZ7WerxChHTQNNP0ws0/TS9oBE/d1R989gjPeuctSce6ZHCNaMiL/dLD/9WMS7DT4a267N7926z0iM0Uo8WNP6uOQ/4xOIg1S9F0/QCTT9NL3D98gaBxDXQPrYQsZhUDXy/RvIQUBhkF9KAlKpMFbT4XpZ6vELENND00/QCTT9NL2jEb82aNVVTX2eOkyMAdhVlMbMFTObByW1uRsGDA9KxYqCcbZDXjI1Nv87JVjiVmVBITpgyh6A2F110kXz/+983PzdSjxa0PA9BNZp+ml7g+uUNAolroH1sIWIxqRr4fpwI6m0QANkK3Uc3rka2vi9/+ctTmu9lqdfLJ6aBpp+mF2j6aXpBI34DAwNmsGdzKTUIlvaY5ESmrG8rNwjsNMJnN0rH2oGprIO/OrBR2kqvH3llVPqWFaR3x6D0rVgiPXulIlvh9TZl8qEh6VpRmdyoWbhJmhqpRwta3iCoRtNP0wtcv5adZcBIeOZ/1xuTqlG0j83fVismVaPM1O9rX/uaWQwppFHoQWDVw2Z4+SWmUTT9NL0omn6aXpRG/JjWWteiRg1RahCULtiMMTFlR0dlg+CV7VL4p9KF/5ohGTEZqid7FNA3TeZGGdsu7avL22y2QiGx0dYxmbijS7oft17Ng27+zs7OpHqcqUbR9NP0omj6aXpRXL+W7SEIEYtJ1UDTT9MLfD96BxrpIWCdAXcmgR/H1EO+cOB7WfwYl1QNNP00vUDTT9MLGvXjO/bjH//Y3zwDMh4Z2AbBxKiMvjIh488OSfdRXaVXT77+5T7psBd/egbWDlcmJzIpk3uld1W37Glw8aJ66OnpacqsHhpZnAN8YnGQ6peiaXqBpp+mF7h+eYNA4hpo+ml6ge/XyBgCuPnmm+W+++4LarBp0yYzNxp8L4sf45Kqgaafphdo+ml6QaN+vJ5kUc2jRoPA9BB0SN/mbmkzSykPS89H2qRn989laE27FDf0SvGoduljXYKKBgFpjttkkW00NBESNjF91qXRegS0/JFBNZp+ml7g+uUNAolroOmn6QW+X6MNggceeMAk8AlpQINh586d5mffy+LHuKRqoOmn6QWafppekOJ38cUXyw033OBvnneMbima1RWbzfLly82a9y4p9YiWNwiq0fTT9ALXr2UbBPlaBtOkajBTPzL60QsQ0oDUxjOdBpWigaafphdo+ml6Qaof6xo8/fTTvjRvGNu9UXq2jTR9MCHLQIcaQ6n1mKKBpp+mF2j6aXqB69eyDQKeS+ezDMqkajBTv1/+8peybt26oAZ8RnmilHBMqgaafppekOrHIGMGGHKzcLjA0uDr16/3NxtS6zFFA00/TS/Q9NP0AtevZWcZcCEaGRmpOyZVo2gfm7+tVkyqRvH9GlnLgJ/J9FcsFoMaxc2L7ntlxTRDo2j6aXpRNP00vSgz8aPXkKl37GOhc+ONN5r021l1klqP+VoG1UXTT9OL4vq1bIPgV7/6lTz33HN1x6RqFO1j87fViknVKL5fow0CCqsavvHGG0GNwvNNxiX4Xln7a4ZG0fTT9KJo+ml6UWbq9/Of/9z0FPzwhz/0r6ELBhIQMeU3Viep9Zg3CKqLpp+mF8X1a9lHBiFiMakaaPppeoHvR7drI4MKYe3atVP78TXo7u42JxnfyxKKsaRqoOmn6QWafppe0Ay/t956S0488US59tprnVe1Pn/4wx/ki1/84lRKcP+4XVLrkUGFnAd8YnGQ6peiaXqBpp+mF7h+eYNA4hpo+ml6QTP8/vM//1OeeuqpoAasHMg88WZ4ucQ00PTT9AJNP00vaKbfli1bTA8WPYqtzuDgoHz2s5+t6PnIOm5oZj1CTANNP00v0PTT9ALXL28QSFwDTT9NL/D9GCDYaA8BswiYTRDSgIFP3/3ud6u8LKEYS6oGmn6aXqDpp+kFzfb76U9/Kp///Oflwgsv9KWWgEygK1asMA1vn9hxp9YjPQTuQGFXy4qDVL8UTdMLNP00vcD1a9kGQb788TSpGvh+jeYhAJY4vu2224IaPPjgg3L99ddXeVlCMZZUDTT9NL1A00/TC2bLjwRajC2g1+DQoVlIFdhkWBzs5JNPNuuBkHgoROy4U+sxz0NQjaafphe4fi3bIGAgRD7tsEyqBr4fdyONNgi+973vmZNsSAPmhp9//vlVXpZQjCVVA00/TS/Q9NP0gtn2o+udzIZnnXWWuejON2i4MBCXsTfPP/981ft3iWmp9UiDgPOATywOUv1SNE0v0PTT9ALXL28QSFwD7WMLEYtJ1aAZfpy0zj333KAGnGBOOeWUpni5xDTQ9NP0Ak0/TS/Q8mPcC9P2mKZ46aWXmtkJc8Vjjz1mBufSg7F161aznoAl6/1DTNOqR4umn6YXaPppeoHrd8RLL70ktpD6kuJuq0ejMAXQ31YrLlXDixwE5MivNyZVo2gfm7+tVkyqRvH9uGvi7iAW52sMGCSPekijvPjii3LMMceYuzF/X1kxM9Uo/rHVE5eqaXpRNP00vSiaftbr1ltvNd3zxx57rOkuZ1llbjpmi5/85CemV41Bgp/61KfkG9/4hhmH47+/Wu8/pqXWI+dVzgMhLSuOkuqXoml6UTT9NL0orl/Tegj4gnMn6BdG99IC8bfPRMOLA+TOtN6YVI2ifWz2Z5dY/adq4LdGU8YQvPfee6YLln99zUIPAccWIisGUjXwj80Si0vVNL1A00/TCzT9Ql4sI3zLLbeYLvvjjjvOFH7mAs7jBtJ18xru4t955x0/XN5++20zbY8kSY8//rjs2LHDDAg86aSTTIODxwH8/vDDD5tphBB7j6la6NggFoPGuScfQ1CJpp+mF7h+TWsQMJJ82bJlVYXWL8XfPhONPyp/W62YVI2i6We95qpBsGfPnoYbBMDdFSfBkAacABlcGCIrBlI07uzwYtwC3bCcgD/96U9LoVAwhZ85ydNVzL/udho29uTPPty7xCw/8OvREotJ1UDTT9MLNP3q9eJ1TzzxhOlJYBrtV7/6VfnSl75kZi9wl09XP4Wf6S3jgspjtCuuuMIMut23b59pQNTr55KqpXpx7uE84BOLg1S/FE3TCzT9NL1g1hoENmd9zsyYywaBJRYX0jj50fUU0oBZBqx8GCIrBurRhoeHzQJLXMy5qJ9++uly3XXXmRM4CZEahRhi2Qf7sg0FLgTf//73/ZcbmlWPlpgGmn6aXqDpp+kFmn6aXqDpp+kFmn6aXpA3COY5c9kgID96Sg8Bd9TMNghpsGvXLrnkkkv8zYasGMjSuHtfvXq1ucOn92Hv3r3+S5oOHnjhSWpXt8fDr0dL1vuHVA00/TS9QNNP0ws0/VK9OPdwHvCJxUGqX4qm6QWafppekDcI5jlz2SBIGUMA5CGgazSkAeMHGKgUIisGXI1BLwy+4oJ8+eWXz+mqdnjzHngvvKesnoN6j80npoH/uVlicamaphdo+ml6gaZfqlc+hqAaTT9NL6hoEPBiW2KLIMQ0Cutx5w2C5sD4AgYe1lv/qRrFX0jj9ttvb3hxI8oDDzxg8quHNAqrU65atapqe9b+XI1ZDORv7+zsnJcL1vCeaEjxHnlW7L//2LGlaBT/c6snLlXT9KJo+ml6UTT9Ur14ZMZ5IKRlxVFS/VI0TS+Kpp+mF8X1yxsE85C5bBDUExfSmKr0rW99K6hRGE3Ncfnbs/ZHoeXKncqaNWvMrJL5Du+R98p75r3Hjm0mGqVZn1s9mqYXRdNP04ui6afpRdH00/SiaPppelFcv/yRwTxkLh8Z8Kw/5ZEB+2FEf0izFItF+c1vfuNvDsawPgID+bKmKs5neM+8d44hdGyWVA38z80Si0vVNL1A00/TCzT9Ur0493Ae8InFQapfiqbpBZp+ml5Q8cjA2R4NjGmQNwiax1w2CFLHENjlZkOahalaLDTj48bwuILV6cjW1upwDCeccEJwKVmI1VVMA/9zs8TiUjVNL9D00/QCTb9Ur3wMQTWafppekDcI5jlz2SDgIpbSIAASr7z++utBDVhtzq6K6GL3x501c7ntUsoLAY6FfAehno6seoSYBv7nZonFpWqaXqDpp+kFmn6pXpx7Qg3yWByk+qVoml6g6afpBXmDYJ4zlw0CSywuSzvzzDPNqmwhDbZt2yY33XSTv9nsj4YCCYRYxXKhwTFxbI888kjF9qx6hJgGzfzcIKZpeoGmn6YXaPppeoGmn6YXaPppekHeIJjnzGWDgLSsqT0E9AAwVz+kwf33328SCPk8+uij5oK50Ono6DAJjyxZ9QgxDfzPzRKLS9U0vUDTT9MLNP1SvTj3cB7wicVBql+KpukFmn6aXlDRIODFtsRGI8Y0Sj7LoHnM5SyDlStXJk07pDCIbufOnUGNQmOBtMDutgMHDpguddZBWOhwjBwrxxyrx1oaxf/c6olL1TS9KJp+ml4UTb9UL6YdkngrpGXFUVL9UjRNL4qmn6YXxfWb8wbB6DUFWfQXi6ZKzyMie9b1yB7/hR6j1/RI/8v+1hny2pD0LmuTtqVLpH3NoIwdKm17uV96rhn1X1mTmby/uWwQkII4tUFAYiIeC4Q0CsvLkvvd3caYARaKSaXi+3NUQXp3jvkvqZuZfGb1wrFyzLF6rKVR/M+tnrhUTdOLoumn6UXR9Ev1okHAeSCkZcVRUv1SNE0viqafphfF9ZvzRwajmxdL0ZvhMjE+LhP234PjMm4eK0/I+NjY5M/EFaXv2XEZGyu/1nCo/Jqx8aktIgcnX8PF3XmN+5Iy+6X340UZmlyGfHxXUdo27Rd5sU+Km8sXrInxMcevtB+7k4nx6f1NlP328/5enNzWIHP5yMASi8vSfvCDH8hFF10U1AAv7jx++9vfmt8vvvhisxLcTDDfA1vPh/bLxqO6ZGjyV/N5veZ80FWffeV3iu8K35MJ58sx9bP9Hk0p6XDMHHtWPUJMg2Z+bhDTNL1A00/TCzT9NL1A00/TCzT9NL2g4pGBsz0aGNNgJg2Cjh1caKcvtkOriuakPrTqA9K2rle27/6x9C/vkN4dg9K3oiB9B4hbIouWb5SBHd3Stmy7jJUu6BuXdkjf3YPSv6pNep4s7eDZjdKxdkAG7WsODkn30V3Sf/eAdC/rmrr4Gw5slMK5I86GSSYbBBO7u6Rtdf/0vkrvsLhq8tJTajyYRk1p/13LumXg7j7pWLSkJRsE3MGmjiFgQGFPT09QA7x6e3vlmWeeMY0CVoabKTQIep+c/P48u10KR/WWvgml7Vs7pGND6bPf3CGFzfvLn81S57M/OBr4TpUbF3tOLcoAjYSDA9Kxeqj02XdL+5ryZ18o/e5+bVLh2F999dXMusqqY4v/uVlicamaphdo+ml6gaZfqhfnnlCvXSwOUv1SNE0v0PTT9IJ51yBoWz8og3dTRoQO3+kGQaF8Yi5dlNuWbSy/ZmtRFq8dNifvjQfK+xha1S7bS4HjLw/LUOmE37f8yPIF+pXSBeKfCtJ7zZCMvCbmwr14zfbyfta3Sfs2p3t5X68U6BHwmWwQ8J4GJ3sZhte2S9/LgQYB/+4sb6q4c22QuWwQpOYhgN/97nfS2dkZ1AAv0hsz2v68884LLrHaKKObC1LcOigDpQv/kaXGwIi52x+VvqUF2Wi+U/1S/Gi3DJcadB132Lv9CZnI+E6Zz2xvt3SUvhsTdxSl+3G+X4ul66byd7Rnafm7NlM4dhpHWXWVVccW/3OzxOJSNU0v0PTT9AJNv1SvPA9BNZp+ml4w7xoE/iOD6QZB+V95uU8Kpw6V7wJfHJbhFyfMhaB3X/n1gytLDYeXtkvH8u2yn16GncXyPidGZfSVCRl/dki66UouXRQKW0fL+9lX2g+NBMuh0gX+6H7TIDG80i/tpQs+Fw7TIFjdUW6ciG2AlF6/crC8wfo5F539m1qzQcCJILVBAMwWIE1xCLzuvfdes6Y8Swo3A7fhNX5HQRabXp5Sg+D4bhkyvU6jMry33MNjP5uxfXtk//Oh75Td137pXdUjPStLDYzSb0OrC9L/YrkXYmTvsIzbx08zpFAoZNZjrI7B/9wssbhUTdMLNP00vUDTL9UrbxBUo+mn6QWt1yCQcRla0166MPdJz9Edk9277bLo6G7p21CQtnXDMmG7hHdslK5li80dXrmHoPT6zd3StmK7jB3aL33L26WntJ/i0d4jA1x2dcniowrStaYo7TQg0O0YggN90nF0j/Sua5cla+g2HpPty5ZIofTa4lGLysdg97+hRwofaW/JBoElFhfTbB7/EGxnnMFpp51mlhFuBpU9MaXP5OglpueIz7J9VZ/0lT6vji37Kz6b9uV9sv9Q6Ds1vS++l0dumOwxmvzs+0p6u/nsm8P555+ftEoiZNVxLC5V0/QCTT9NL9D00/QCTT9NL9D00/SCigYBL7YlNhoxplFSZxnkVDOXswyYYZA6y4DC6GSy8vnbKXgx5e5f//Vf5fHHH/cP+7CDOrjkkkuq6qlWHdu69LfVikvVNL0omn6aXhRNv1Qvso1yDghpWXGUVL8UTdOLoumn6UVx/fIGwTxkLhsEM8lDQLnssstk9+7dVdspeI2Pj8uHP/zh4CJHhxvUQWi+d606tnXpb6sVl6ppelE0/TS9KJp+qV55HoLqoumn6UVx/eb8kUFONccee6xJ8nPnnXdOldtuu82sUe5um6lG6e/vr/h96dKlcsstt0TjYhqPA04//fSq7RTrtWjRInnnnXf8wz7sePfddzMzNNb6e9PsVtT0Ak0/TS/Q9Ev1yscQVKPpp+kFFY8MnO3RwJgGeYOgecxlg6CeuJh2zjnnSGdnZ9V2ivX6xCc+YS6Ghzt5gyCMpp+mF2j6aXqBpp+mF2j6aXpBSzUIyES3eP3w9IZnN0rbsn4Znd7SdOrJlDibtPKgwn379pnpdCGsF3cf9T8y2CM9xzAIcPLXR3qkkJU58pVhGRnjOzOZcXB8SLqOWiJdV26qP9ukv49ZhDo4+eST/c2GWB1Dsz+3mKbpBZp+ml6g6afpBZp+ml6g6afpBa3VINhckCOP6pY9kxeE/RvaZdHxfVMNgqpsdH5mQpM50MlOF8hmaPZR+r0iQ+JkJkKrZb12NpjLBsFM8hAAz6O+8pWv+JsN1otxBvUPKhyS4gc+IO02RwS5JCYzR1Z+thMycm6H9O6bmMw4WPr8dnRIgXwCExMV2QgrMg7afUxmw6zch31JdYbKqu/Fa973sA6og82bN/ubDbE6Bv9zs8TiUjVNL9D00/QCTb9Ur/yRQTWafppe0GINgqL0bugyCWJERqR3ba/0rio3CKqy0VVlJhyUrhUkn+mTjo/0yHAgmyHz0wvEbO6SxR9wpzuWLkSLSq+5ZlD6li/OfO1s0MoNAmJZryCE9frhD3/YwLRDEkBtN7kfTCIq2yBwMw8uLcj2V8ZkcGWbFHfslwNm+uCYjKxvk7b1e2T/z6qzTRbWDMlE6ftRXEpmyQHpPaZDto/5+yh/5lUZKr3vxf5NHdK9Y1AG1rZJ4ab6sxbl0w7DaPppeoGmX6pX3iCoRtNP0wtarkHQt29AutbuMdkEu+8YkT7TIAhko/MzEx4alp5/apOuzQOy58Xy3ZufzXA6AyFz2L0GwfED5bvCyUyEodfOBnPZIPjf//3fGTUI0L74xS+amQo+rlf9iYkmM0KOl/49ZqPs3znZIHCyQpJZsH3L6FQegal8AjaDZCC51MRB0xck4z/dY74/3f9Uzhvh7yOYodL7XozdVJAly3qlf/dIQ0mL8sREYTT9NL1A0y/Vi3MP5wGfWByk+qVoml6g6afpBRUNgpdeekls+cUvfmGKu60ejUJK2qxnxzOhfGKekIEVXdJ7bulu7uDodIPAz0bnZyY8NF76fbzUCBiR/uNLJ/OfVGcznL5IlPb3Ua9B4KUmDr12NjjmmGNkeHi47vpP1SjPPfdc1bZacbU0piw9+uijVZrrtW7dOnnsscf8Qw8w/Tmw4FT7RxeXGwRuKmK2bxuruphnNwjGZOTx/TLxZI+0r98jo69NTGWW9PcRzFDpfS8mXhyVsYPjsn93tyxZXd+3gmM/88wzo/WYpfl1WW9cqqbpRdH00/SiaPppelE0/TS9KJp+ml4U1681egjour2jQ44wJ2LbIAhko6vKTFjuISAzYcfSHhlmkJmfzfBAnxSWdkjXinZZ9IHySnlZDYLQa2eDuewh4EI90x6CTZs2mYyEPq5X/YsbOZ8DGStXfaDcIHAzD05mleROffGy7fJARoPAzTZpvi+l7YXSnf3ANT1SPGqJdO8N7KMqQ2X198L0EKwofQ/XtklHnY8M8sWNwhpo+ml6gaZfqhfnHs4DPrE4SPVL0TS9QNNP0wsqegic7dHAmAaz1SCYbUbv7pOhV6T8eGH59GDFEI28dibMZYNgpmMI0EhSdf/99/tSlVczlj9uRfLlj7M10PTT9AJNv1SvfAxBNZp+ml6QNwhcxoelf323dJ/aK4Nc7GM08toZMJcNgmeffXbGDYL77rvPNAp8fC/gTjm01OpChWO1PSO16jFLg1BdQiwuVdP0Ak0/TS/Q9Ev14tzDecAnFgepfimaphdo+ml6Qd4gmOfMZYPAEourpfG4gMcGPiEvnlsdf/zx8t577/nSgoNj/MxnPmOOGWrVY5YGobqEWFyqpukFmn6aXqDpp+kFmn6aXqDpp+kFeYNgnjOXDYKLLrpoxj0EL7zwgnzta1/zpSovCwMQszL2LSQ6OjrkiSeemPq9Vj1maZBVl7G4VE3TCzT9NL1A0y/Vi3MP5wGfWByk+qVoml6g6afpBRUNAl5sS2wRhJhGyRc3ah6tvLgR2xkwyFgEX/O93JgHH3xQPve5z8nBgzaD0MKBY+LY/EWfatVjlkaJ1WVWXKqm6UXR9NP0omj6pXrlixtVF00/TS+K65c3COYhc9kgePjhh2fcIKCsWLFC3njjjaiXH8NUSx4fPPXUU36VtCwcy3HHHWeOLeu4/e21NEqtuvS3z0TT9KJo+ml6UTT9Ur1oEHAeCGlZcZRUvxRN04ui6afpRXH98kcG85C5fGRgicXVo7Hiob9v/3eLuz8aI1/4whdk69at3qtaD47hhBNOCCZ5gXrqMYt66tInVdP0Ak0/TS/Q9NP0Ak0/TS/Q9NP0gopHBs72aGBMg7xB0DzmskFw4403zngMARqpiZ9++ukKzfeyhPbHao90s//kJz+p2N4K8J557xxD6NgsqRo0UpeWVE3TCzT9NL1A0y/Vi3MP5wGfWByk+qVoml6g6afpBXmDYJ4zlw2CZuQhQLv66qvlv/7rvyo038uStT8WHWI+9Jo1a0ymrfkO75H3e8opp5j3DlnHBqkaNFqXkKppeoGmn6YXaPqleuV5CKrR9NP0grxBMM+ZywbBnXfe2ZQGwd133y233XZbheZ7WWrtj+fwnZ2dprAw0nyD93TSSSeZ9/c///M/FVqtY0vRILUuUzRNL9D00/QCTb9UL849nAd8YnGQ6peiaXqBpp+mF+QNgnnOXDYILLG4ejSm111++eUV2ky9mL//jW98wwzSY9+vvDKL2aFqgDfvgffCe0pZtTBVg5nWpU9M0/QCTT9NL9D00/QCTT9NL9D00/SCigYBL7YlNhoxplHyWQbNYy5nGezcubMpswx++tOfytlnnx318mP87TGN1MirV682qyYyXmHv3r1+NTYdPFiyGE+8eQ+zcWy1NIqmn6YXRdNP04ui6ZfqxSwDzgMhLSuOkuqXoml6UTT9NL0orl/eQzAPmcsegmaNIWBkPc/+XXwvSz37C2E1pvSRGZGBfFysmeFw3XXXmV4KTm6NQgyx7IN9sU/2vXHjxsyegNk6tiw0/TS9QNNP0ws0/VK98jEE1Wj6aXpBRQ+Bsz0aGNOABgEXMr9wt0vxt89EO/bYY035t3/7t7pjUjUKXv62WnGpmvWaqwYBU+Wa0SB49913TfZBNyWx72WpZ38hsjRavCQ64m5+7dq15n1wUS8UCqbwM1395DzgX3c7F/7u7m7ZsmWL2Qf7smT5gdaxWTT9NL1A00/TCzT9Ur0494Sm/cbiINUvRdP0Ak0/TS+YlQYBU634IvmFrm8M/e0z0fD60Y9+ZFLe1huTqlG0j83+7BKr/1QNZvPLR7YzshZaZtMrhKafphdo+ml6gaafphdo+ml6gaafphdo+ml6gevXtAaB5kHgxZ1bPqK7TKoGvh8NrWb0EMA555wjzz///JTme1nq3Z9PTANNP00v0PTT9AJNP00v0PRL9eJmhPOATywOUv1SNE0v0PTT9ALXr2UbBOSH/93vflexPRaTqoH2sYWIxaRq4Ps1awwBXHbZZfK9731vSvO9LPXuzyemgaafphdo+ml6gaafphdo+qV60SDIxxBUoumn6QWuX9NmGWiOjNT0omj6aXpRfL9zzz23KbMM+Pmmm26Su+66K9MrFOOXVI2i6afpRdH00/SiaPppelE0/VK9GFTLeSCkZcVRUv1SNE0viqafphfF9csbBDU0iqafphdlNv127dol/f39U9pseoWKpp+mF0XTT9OLoumn6UXR9NP0omj6aXpRNP00vSiuX8s+MggRi0nVQNNP0wt8v9HR0aY9Mti3b59ccMEFU5rvZal3fz4xDTT9NL1A00/TCzT9NL1A0y/Vi0cGnAd8YnGQ6peiaXqBpp+mF7h+LdsgIFPc/v37K7bHYlI10D62ELGYVA18v2aOIWA2xRlnnDGl+V6WevfnE9NA00/TCzT9NL1A00/TCzT9Ur3yMQTVaPppeoHr17INAro58lkGZVI18P04ETSrQfCHP/xBVq5cOaX5XpZ69+cT00DTT9MLNP00vUDTT9MLNP1SvfIGQTWafppe4PrlDQKJa6B9bCFiMakazLYfPQ5vvvmm+Xm2vXw0/TS9QNNP0ws0/TS9QNNP0ws0/TS9QNNP0wtcv5ZtEOSPDKZJ1cD3o3egWT0EcOaZZ8ovf/lL87PvZfFjXFI10PTT9AJNP00v0PTT9AJNv1Sv8fFxcw7wicXB/2/v7F6jutY47J9wLr04N4VzI3hxhN4IvTHlUDIlLZlWMAO5qKNgO6QS40c1tb2oHlsbBG1aMMQjRsaPiKfVmmDV2EJAa1NSJPUzNgdq6bRYGqmUuQjlPfPbMzuzsmbPmsmbmZ9OXA+8JO533jzuNTp7Ze2119L6NDmmCzB9TBcwfQ3bIYjCVaPNAaaP6QK2r5ZzCMDu3bvl2rVrwfe2K8SuMdHmANPHdAGmj+kCTB/TBZg+rcvfMiiF6WO6gOlbgi1lw7h7924Q5rFqcojvv/++5FilOm2O6UIwfUwXwva99NJLMjEx4aybTw4bAmEnzChXuZpa5BBMH9OFYPqYLgTTx3QhmD6t6+rVq7JmzZrIXLk6hNanyTFdCKaP6UKYPj9CIO4cYPqYLlBvH7ZRPXz4cPB9vV02TB/TBZg+pgswfUwXYPqYLsD0MV2A6WO6gOlr2A6BX7q4iDYH6u0bHR2VDz/8MPi+3i7ssHj9+nU5efJkcKuivb1dXn755Tk7HWJ3QzPsnQ4xVIraU6dOBSMlJrbPpN7nZsP0MV2A6WO6ANPHdAGmj+kCTB/TBUxfw3YI/FMGRbQ5YPtqPYfg9u3bsnXr1uB72xVi15i4crjXefz4cdm4cWNwccfFf8eOHXL06NFgc5Zff/3VLqlIJpMJhkwHBgaCzZlwCwU/u7OzUwYHB8v+zFqfmysHmD6mCzB9TBdg+rQuP4egFKaP6QKmz3cIxJ0D7HOLwlWjzQHbV+sOAUZx1q5dG3xvu0LsGhM7Nzk5KXv37g0u1Pi7Hjp0KLj3VW/u3LkjfX19wb1VuPft2zdnNbdanJuJKweYPqYLMH1MF2D6tC7fISiF6WO6gOnzHQJx5wD73KJw1WhzwPbhN+BadgjAK6+8IjMzMyWukKiakDB35MiRYEh/w4YNcvHiRftldM6fPy/r1q2T1tZWSafTCzq3KFw5wPQxXYDpY7oA06d1oUMQNRLmqgNanybHdAGmj+kCpq9hNzfCjnz4R1ttjTaHYJ+bfaxSjTaHYPjWr18fLGM8XxfeY+yFgCF77Jz4JIKOzsGDB6W5uVnef//9YG5LNee2kBxivm25kBzThWD6mC4E08d0IZg+pgvB9DFdCNPXsB0C+1ilGm0OwfQxXQjbh70HarX9cRjYSvXbb78tcblqcHHF0Py5c+fsa/ATy6effjp7O8F1bgvNIebTlgvNMV0Ipo/pQjB9Whe2P8bnQFSuXB1C69PkmC4E08d0IUxfw94yiMJVo80Bpo/pArav1nMIwP79+2VkZKTEFWLWDA8PBxfVEydOWK9qHDAhERMcL126FNkeIdocqKYtbbQ5pgswfUwXYPq0Lj+HoBSmj+kCps93CMSdA0wf0wVsHx61q3WH4NixY8EF3naFhDWbNm2SzZs32+mGBOeD37K2bdtW0h4hUW0V4sqBSm0ZhTbHdAGmj+kCTJ/WhQ6B/chtmCtXB7Q+TY7pAkwf0wVMX8N2CPxeBkW0OcDwXbhwQT766KOyLjw5gN+oz5w5Y6caHqyJgImHv/zyi52KbKsQVw6Ua0tXnTbHdAGmj+kCTB/TBZg+pgswfUwXMH0N2yHAfQ//lEEebQ7Yvj179tR8hOC7776Td999t8QFMLegpaWF8ujg4wJLg+I2yI0bN+Ycj2qrEFcORLUlcNVpc0wXYPqYLsD0aV0YIcDngI2rDmh9mhzTBZg+pguYPt8hEHcOsM8tCleNNgdsXz3mEPz000/yxhtvlLjGx8eDkYFwe+TFDJ6IQafAHNWKaqsQVw7YbRniqtPmmC7A9DFdgOnTuvwcglKYPqYLmL6GfcoAj7HhN69qa7Q5BPvc7GOVarQ5hO374osvav6UAR7Fw1oEpgvvH0YGHj58aPzTXNxgFUScc/i4bFRbudrRDPt9q6ZOm2O6EEwf04Vg+rQuPGWAz4GoXLk6hNanyTFdCKaP6UKYvobtENjHKtVocwimj+lCsHzYVwAjOuGfcV/dHkIH2eGUxP+TKR7IpCXx+pDc+qRLev9XPFw9U9LbvEy6RotHbv57pcQ+mSoeqJIp9d+hyNjYWNA5crVVpRyC9b4hmC4E08d0IZg+pgvB9DFdCKaP6UKYvoa9ZRCFq0abA0wf0wVsH5YCrvUtA4CnB7766qvge2x2VHahoZkRSTX3StglyByISepy7ptH05KdyR/LTmck8yBb+L5wfCYr09PGsfxLBR2CnpZnZEXHSOHPN6V71VKJ7Q07BLm63G/u048Kf4QnF5lM/udmHxRzU3sT0jNRyBVeXra+8HeJore3Vz7++OOybQVcOWC/byGuOm2O6QJMH9MFmD6tC7cM8Dlg46oDWp8mx3QBpo/pAnNuGRjHnYWuHGCeBNMFmD6mC9i+eswhAFhoCFshY7Y9liB2MfJ6THrv47vcxby1W3DXPbgYT+a+HohLfGdaTu+N5y7qNyXTF5cUfvs/m5Alq/pzHYlcTXuPFH//x5+7pfu1lIyg4zDWLamd3ZIIOgRT0tsal+6B09KzOiY9t+BpkmXre4Kfv+wfyKUl9Wxc0o+QWyFLW3dJeiAlK5vzrqj6Z1Z3Sfex8dm/QRSYT4B9HqLaCpRrxxD7fQtx1WlzTBdg+pguwPRpXX4OQSlMH9MFFkWHwG9/XESbA7YPuwfWo0OA3zj6+/uDVQvDkYKyTHRLHBfs3NemnflJePkOQe7i/lxMdg2eltODvZJYnpIr93Nft4/L+PYuSbyWlKH7/ZIs1OTJdxDGjyWDkYbxnSlJj/XkOwSTPbkL+67cz8r9vAMJWdZxJeeJSfcY6oYk0XbacOe/7rqV/6lD7U3Sf8VV72ZoaChYljmqrUC5dgyx37cQV502x3QBpo/pAkyf1hXuImrjqgNanybHdAGmj+kCi6JDgPse/imDPNocYPnOnj0rH3zwQbAvQWXyIwP92xOFkQKjQ9CSkqFMRjKZKbkyOiXZ4IKfkERHv0wdS0i8NWFdkAsjBo/SEn+tW7o70pKdLHQI/tcjsdeHgsl+mckrcmUyO3vxDzoE7UOGG1+LF/vTbTFJ33DVVyYWi0W2FSjXjiGs9w0wXYDpY7oA08d0AaaP6QJMH9MFfIfAwpUD7HOLwlWjzQHbhwt3PUYIrl69GkwsxNbF1ZA50CRLVqdn79WHF9rps0lpyl3gezqbJL4vHD1YJstwgc/0S9PSbpk7WB/eQshKunWJJM5KMDKQv2UwLUPrm3Lf90jXqnhhyN/VIWiSpatS0rMzJis7r2D2gKO+Mu+8846MjhqzHQ3KtWOI/b6FuOq0OaYLMH1MF2D6tC6MEOBzwMZVB7Q+TY7pAkwf0wXmdAjw4jBcsxFdOQQu0PaxSnXaHFw//PBD8Bx7tTXaHIJ9bvaxSjXaHML2tbW11fyxQwSev3/++efl2rVrxj/Fp5vLly8HoyZ2W7naMQz7faumTptjuhBMH9OFYPq0Ljx2mEwmI3Pl6hBanybHdCGYPqYLYfoatkPgtz9eeA5h+7BTXz06BA8ePJAVK1bIn3/+aV8Xn1rw73ft2rUlbeVqxzDs962aOm2O6UIwfUwXgunTutAhMHfsrKYOofVpckwXguljuhCmr2FvGUThqtHmANPHdAGmb/ny5fahpx6s1BiFqx0B831jugDTx3QBpo/pAkwf0wWYPqYLzLllYBx3FrpygHkSTBdg+pguYPu+/vrruswhAC+88IJ96KnHdwhKYfqYLsD0aV2YQ4DPARtXHdD6NDmmCzB9TBfwHQILVw4wfUwXsH31WocAxOPx2t8yuNAlS/++NIhlq5LSe3XafkUpt3oktnyldG3JrzxYixUINeCWAe7VRuFqR2C/byGuOm2O6QJMH9MFmD6ty69DUArTx3SBRdEh8NsfF9HmgO17++2369Yh2LJlS+0nFZ5N5J8cADNXpAtrE2D+/3RWsg8KqxhmsbpgRoLFA2eyMr49Jl2X8ysKIm8+GWCughhgrFpYazCpsNxTF652BPb7FuKq0+aYLsD0MV2A6dO60CHA54CNqw5ofZoc0wWYPqYLLIoOASZC+McO82hzgOnDYiflLoBqzA5BdkiSz2JlwyFJ/G2ldO3sl6F7uWPPJaV3MC2p52LSf/umpNtWSuLAuFwzFhyKWgVRJnZJvCP359mVCWuLf+wwGqaP6QJMH9MFmD6mCzB9TBfwHQILVw6wzy0KV402B2zf1NRU3UYI4KpuYaJ5kOsQPPOvpCTXI7rldLCQUa5D0FJYwwAdhv8WXjuakqZ9U3NWHix+jVoFsV9i/4xJ9ydDMv5g1lgz/MJE0TB9TBdg+rQujBDgc8DGVQe0Pk2O6QJMH9MFTN+Se/fuSRh3794NwjxWTQ6BrYjtY5XqtDm40Bn45ptvqq7R5hDsc7OPVarR5hC2D2vsT0xMOOu0Obg6Ojrkyy+/LP5rXCjmCMEsxUWFZDgp8WOFWwC51zb1Zcp3COxVELO5zsP9rExPDEnq2aQUfmJN+Pzzz2Xr1q1l28rVjmFb2scq1WlzTBeC6WO6EEyf1oVFxNasWROZK1eH0Po0OaYLwfQxXQjT17AjBFG4arQ5wPQxXcD2YTJRPUcIqtncaF5U6hDM3JSe1ibp2tklTbioT9sdgeLXklUQgxGCuPTsTcnK1bW9ZeA3N4rOAaaP6QJMn9blJxWWwvQxXcD0+Q6BuHOA6WO6ANMXupzbHz8F+O2Py+cA08d0AaaP6QJMH9MFmD6mC5g+3yEQdw4wfUwXsH0YHajnCEEIHkG8ceOGkX06GBsbk1dffTX43tVWrhyw37cQV502x3QBpo/pAkyf1jU9PR18Bti46oDWp8kxXYDpY7qA6WvYDoHf/riINgdsXz3XITBdP//8czBs/vDhQ+MVixs8+ohzxgcucLWVKwfs9y3EVafNMV2A6WO6ANOndflbBqUwfUwXMH0N2yHwTxkU0eaA7WN1CAA2p8JKfX/88cec44sRLEKEzoC5doarrVw5YLdliKtOm2O6ANPHdAGmT+vyHYJSmD6mC5i+ht3cyO92uPAcgumLcmGJ1JaWlmCG62IFs3hxjujAVttWrhwiqi0r1WlzTBeC6WO6EEwf04Vg+pguBNPHdCFMn+8QVMgh2OdmH6tUo80hmL5yrjt37gQjBWfOnLGvpQ3PyZMng6cqsLKmfd6utnLlEOXa0lWnzTFdCKaP6UIwfUwXguljuhBMH9OFMH0Ne8vAzyEoos0B28e8ZRAS1mzatEk2b95spxsSnM+bb74p27ZtK9serrZy5UCltoxCm2O6ANPHdAGmT+vytwxKYfqYLmD6GrZDEIWrRpsDTB/TBWzf4+wQgOHh4eBe+4kTJ6xXNQ4DAwPBiMelS5ec7aHNgWra0kabY7oA08d0AaZP6/IdglKYPqYLmD7fIRB3DjB9TBewfZj89jg7BCFYqwAX1XPnzs05/iTz2WefBZ2Z/fv3zx6LOrcQbQ7Mpy1DtDmmCzB9TBdg+rQudAjwOWDjqgNanybHdAGmj+kCps93CMSdA0wf0wWYvvm60DF57733ggl5hw8fttNPBDMzM9LX1yfNzc3Bxk04F5Ny5wa0OTDftgTaHNMFmD6mCzB9TBdg+pguwPQxXcD0NWyHwG9/XESbA7avs7PziRghCAlzR44cCSbnbdiwQS5evGi/jM758+dl3bp10traKul0ekHnFoUrB5g+pgswfUwXYPq0LowQ4HPAxlUHtD5NjukCTB/TBUyff8qgQg7BPjf7WKUabQ5h+9ra2uT333931mlztquaGjuH1Q337NkTjBpgvsOhQ4cojyziSQiMBGDTF9wWwC0NHKvluVWbQzB9TBeC6WO6EEyf1vXjjz9KMpmMzJWrQ2h9mhzThWD6mC6E6WvYEQKchF+YKI82B2wfdjp8EkcIosBvMsePH5eNGzcG2ypjzsGOHTvk6NGjwfoGUfdBK4HVBLHbGyYGvvXWW8HFHz8bvzENDg6W/Zm1PjdXDjB9TBdg+pguwPRpXfh/hc8BG1cd0Po0OaYLMH1MFzB9Ddsh8LcMimhzgOmrt+uvv/6S69evB8/+7969W9rb24NOwosvviixWCwIXNzNCI/jNbglgdnVqD116lTJh6LtM6n3udkwfUwXYPqYLsD0MV2A6WO6ANPHdAHT17AdgihcNdocYPqYLmD7MBzfKCMErhxg+pguwPQxXYDpY7oA06d1YYQAnwM2rjqg9WlyTBdg+pguYPp8h0DcOcD0MV3A9j3udQhstDnA9DFdgOljugDTx3QBpk/r8usQlML0MV3A9PkOgbhzgOljuoDtGxkZ8R0CRY7pAkwf0wWYPqYLMH1aFzoE+BywcdUBrU+TY7oA08d0AdPXsE8ZYCY8JnhVW6PNIdjnZh+rVKPNIZg+pgvB9DFdCKaP6UIwfUwXguljuhBMH9OFYPqYLoTpa9gOgX/scOE5hO07ePDgE/3YYbU5BNPHdCGYPqYLwfQxXQimT+vCY4f4HIjKlatDaH2aHNOFYPqYLoTpW/Lbb7+JDx8+fPjw4ePpDt8h8OHDhw8fPnzI/wEMjsbPbzeClQAAAABJRU5ErkJggg==>

[image2]: <data:image/png;base64,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>

[image3]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAnAAAAHQCAYAAAAh51fQAABGg0lEQVR4Xu3dMa8cW1LA8WV5egRkOMFfADICWLD4BEYbETl9ASBv9IInOSHZ5QNYZBjQihw2IHbAihhWkLzQWhLnfALeRXVX5a0pd3fN3Kqac7r7/5Na994+PXWqz+kzU+65c/29BwAAAOzK9/wOAAAAzI0CDgAAYGco4AAAAHaGAg4AAGBnKOAAAAB2hgIOAABgZyjgAAAAdoYCDgAAYGco4AAAAHaGAg4AAGBnKOAAAAB2hgIOAABgZyjgAAAAdoYCDgAAYGco4AAAAHaGAg4AAGBnKOAAAAB2hgIOAABgZyjgAAAAdoYCDgAAYGco4AAAAHaGAg4AAGBnKOAAAAB2hgIOAABgZyjgMMSLFy8e3rx58+nnd+/ePXzve1yOAABcg1dMDLFVwEmbfC/bq1evHvd9+PDh0z4lj5djZQMA4Ewo4DBEVMCp58+fP36VNjnGtsvjuWsHADgjXv0wxFYBp0Wbev/+/cU+OU7uyMnj/bEAAJwBBRyGWCrgtBjTO2v6FqoWd3ajgAMAnBkFHIbQokwKMSGFmBZ0+ntvul/Yt1D1Th0FHADgrCjgMIy/06akKNP9WtTZDzFo0UcBBwA4Kwo4AACAnaGAAwAA2BkKOAAAgJ2hgAMAANgZCjgAAICdoYADAADYGQo4AACAnaGAAwAA2BkKOEznf//3fx9+9KMfPfzgBz94/MO98r3sOyP/X4ix3bbNzufLdts2M58r2+0btjFCmM7v/u7vPvz1X//1w3/+538+/izfy75//ud/dkceH09iwLLZ18bs+c2O8YsxQpiK3G1bI4XcVvsR8SQGLJt9bcye3+wYvxgjhKnI26Zr5I7cVvsR8SQGLJt9bcye3+wYvxgjhKlEizZqP5qznS9wrdnXxuz5zY7xizFCmMrWHTbuwAFQs6+N2fObHeMXY4QwFfm06b//+7/73Y/75IMMZ/s0Kk9iwLLZ18bs+c2O8YsxQpgOn0L9NZ7EgGWzr43Z85sd4xdjhDAd/g7cr/EkBiybfW3Mnt/sGL8YI4SpnX0Rn/38gTWzr43Z85sd4xdjhDDUf//3fz/8+Z//+eomi9jvs5s8/sh4EgOWzb42Zs9vdoxfjBHCcP/6r/+6uski9vvsdnQ8iQHLZl8bs+c3O8YvxghhamdfxGc/f2DN7Gtj9vxmx/jFGCFM7eyLuOv8379//xjbbm/evPGHXe3Fixepx2s+uI4f73fv3pWOn70uMiRHybVDNrduXfn5dStzv8VfK941MUboGr8jYYQwtbMv4urz1yfqtYJJ9skTvrb5F3J9QX7+/Pmnfa9evbo4RosJG1++1+Psi4kep23Y9uHDh8ev/kXZFnA6f3Y85XF+nzxefrYFlh6n/Qj5WeLbGBpHrwfdJ9eVkjZbwC0dp9eRzetaT3nMPVXmZ8d1aZx1jO1alPnya9O3azzdbws5Pc7ut/uUXnvVhbrtA8sYIUzt7Iu46/y3Cjj7JK5FgjzBa9Egx+gLiLZrm8bVFwdt1yd4f6dI+6KAu81aASdf7QupzpO26TwKebwt1JQt4C17bUgcodeDFguak/wsmy8uhOQkhZvs131PuQO0lONMuvKz60/ompMxlHFVOrZ6ray1y2Nlv127ugl5rLT7+RX2MXKMvSazusbvSBghTO3si7jr/P1bqPrErk/GeozSuy/6ZK78i4Rsuk/YuPYujnxdehHCdbYKODs/QgsmYcd/68VW42lMYePaAk332771MbaAW7qe9FoTtxZxs18vXflJ3GvWjl+bni3g7Bz7edB5lePt2hZ2vS9dexlL54RLjBCmdvZF3HX+a0/69kXVPpFfW8DJz/rCL9tWAbcUH9fxL8r2xVP26/hrm50THf+lF3VPrxNf8NsY9nrQ/fYO3VIBJ3w8XzhE9Pxm1ZWfzoeya1nmQsfTr821djv29lg7NzrHendW+1tb7xW6xu9IGCFM7eyLuOv8rynghD6ZyxO1PDn7F2z/ImHfUtH9Ql8k/J0XG38pHyzTokzp3Ng7IkJf6HX87ePWCjg9VujxOm92v/DXgxaPOu+2gNP+9Bqxb6FKfF/gRWa/Xrryk7g6Vjov8rOMpY61LaLtP66W2nUu7Nq1xbTMr2xyjO7X9WsfY+NX6Bq/I2GEMLWzL+Lq89cn4GsLOPlZN+FfsO2dFj3G3vFR8v1SAafHUcBdxxfXdvyUvcuidNzt2K8VcEKPtcfbGBrbXw++0LMFnM1LC5ClXK/1lMfcU2V+tri1c2DH0s6PFl1Cr5O1dvlZ199SP7ZIs/0qXe+Vd9+E7QPLGCFM7eyL+OznD6yZfW3Mnt/sGL8YI4Rp/fSnP334jd/4Db/7VHgSA5bNvjZmz292jF+MEcKU/uu//uvhN3/zNx/+6Z/+6fH7s+JJDFg2+9qYPb/ZMX4xRghT+pM/+ZOHv/u7v/v0/VnxJAYsm31tzJ7f7Bi/GCOE6fzoRz96+Iu/+ItPP8v3su+MeBIDls2+NmbPb3aMX4wRwlTk7dL/+I//8Lsf933/+98/3dupPIkBy2ZfG7PnNzvGL8YIYSp//Md/7Hd9Ih9X32o/Ip7EgGWzr43Z85sd4xdjhDCN169fP/zlX/6l330haj8ansSAZbOvjdnzmx3jF2OEMIV//Md/fPjDP/zDh//7v//zTRekXY49C57EgGWzr43Z85sd4xdjhDDcD37wg4e///u/97tXybHymDPgSQxYNvvamD2/2TF+MUYIw/3VX/2V3xWSx8hbrkfHkxiwbPa1MXt+s2P8YowQhvqHf/iHh++++87vDslj5C1XefyR8SQGLJt9bcye3+wYvxgjhKF+/OMf+10XfvKTn/hdn/ziF78IH7938iTG9vRtdj5fttu2mflc2W7fsI0RwtRYxMAYrL1jY373jxnE1LbuwAHow9o7NuZ3/yjgAAAAdoYCDgAAYGco4DA1fk8DGIO1d2zM7/4xg5gaTzLAGKy9Y2N+948ZxNT4RVtgDNbesTG/+0cBBwAAsDMUcJga/0oExmDtHRvzu38UcJgav6cBjMHaOzbmd/+YQUyNJxlgDNbesTG/+8cMYmrc5gfGYO0dG/O7fxRwAAAAO0MBh6nxr0RgDNbesTG/+0cBh6nxexrAGKy9Y2N+9y89g3IRsLGxsbGxsbGxXb9lpSNUJLFnZz9/AABwm4raIR2hIok9O/v5AwCA21TUDukIFUns2dnPvxu/aAuMwdo7NuZ3rIraIR2hIok9O/v5d2N8gTFYe8fG/I5VMf7pCBVJ7NnZz78b/0oExmDtHRvzO1ZF7ZCOUJHEnp39/AEAwG0qaod0hIok9uzs5w8AAG5TUTukI1QksWdnP/9u3OYHxmDtHRvzO1ZF7ZCOUJHEnp39/LsxvsAYrL1jY37Hqhj/dISKJK4h/dhty/v37/2uNlEuyOFficAYrL1jY37Hqqgd0hEqkrjGmzdvFr9fQgEHAABmVVE7pCNUJBH58OHD42a9e/fuU5u9K6c/P3/+3B7e5h7nDwAAjqOidkhHqEjiGlKw+bdQ5U6bFmpauMlX7sAdB+MLjMHaOzbmd6yK8U9HqEjiVlKgyeaLOgq442F8gTFYe8fG/I5VMf7pCBVJRPTtUr/P3oGzKOCOg1+0BcZg7R0b8ztWRe2QjlCRxDVsESff6+/ESf/ys96NExRw++XvqC5tAADsWcVrWTpCRRJ7dvbzrxaNZ9QOoAZ3aI6N+R2r4rUsHaEiiT07+/lXi8Yzahf+mBcvXlz8fCt792/pLftOr169+tS3/yQ20MmvIxwL8ztWxfinI1QksWdnP/9q0XhG7cIfky3grOhvEFaSXwXQ3KV4u3fxiHPz6wjHwvyOVTH+6QgVSezZ2c+/WjSeUbvwx2gRpHez5KuQAkl+1nb5PUr5fulDM0oKOHm8HCePtX+HUIs7/VkLrrWfNU/Zrz/bvteKNvt46dMWlfIY+dnGB56Ct9iOjfkdq+L5OR2hIok9O/v5V4vGM2oXvujRgk0LGym+bOElmxwjxZMeu0WPFf5P2fifxdbPkqsWg7Jt3eFbKwAlH71b5/vfKkYBAGPo83dGOsJaEvZFZO2YNfJCtfVCNpNbzw3bovGM2oW/i+avJVvAWU8p4LY+8Sz9+jzk563H2OJT7/bZNs1d2BzkOGm3b7sCGdyhOTbmdyz/+vMU6QhrSfgXqWvvBCy9sM5sT7nuQTSeUbvy/3iwd9z8W6iyaXF1awEnbF82pr9j5n+Wzd59k82vE/shBolt3x61hZr9R489xq9D4Fpy/eC4mN+xKsY/HWEtCf/CIS8q/neO1v4fU9nke9+uceTx9veW/IuZPkY2e/fF7hdL8e1x/sV0iX8scqLxjNoB1GCtHRvzO1bF+KcjrCVhiyU9Rgs4KZzke/t2ke7XomqtXQoy3W/fYpJiy97hEBpr6a7eWnyJd81dGOXjIicaz6gdAIDZVbyWpSOsJeHvwOk+LZT8L1v7Am6t3RZw9u0l2W4p4NbiC31biztw9xeNZ9QOAMDsKl7L0hHWkogKOH8HTK3dgVNrd+Csawq4tfhq6TFLrjkG14vGM2oHUINfcj825nesiteydIS1JKICTsj3epfLF11r7baAs/GkkNPfg/OxbFGoMfQ4H18/wXhNAfftt9+Gx+A20XhG7QCeTtbX1objYD7Hqhj/dISKJPbo7du3Dy9fvvS7kRRdT1E7gB6svWPhDtxYFespHaEiiT2S85Y7cKgVXU/Srndeddv6XUV9i33prfaI/3MhS/yfAdG7y1vkuB/+8IePsaP4wsa/lv190WtyAiK3XH8AtlWsp3SEiiT2SO7AoV50PWkB5/8G2hqNF8V9KpuHz2uJL6auKeDsryNcc7yQ4s3+CR0gq2sNAWdUsZ7SESqS2BuKtz7R9bRUwNm7bFLgSLv+ORg5Xr/6P6SrhZG9i2ZpPNvuiyJfwOnvYsqxmot8r8dJDrJpbC3I7B/f9ZaKNj3e/51DveOmsewdOBtf9mmeUdFpaYy1DcfF/B4Lb6GOVbGe0hEqktgT+b03Crg+0fUk7b6A0yLFF3B6vP1q30qVGPI4LXCkGLLFksazj/F3+5aKHz1e4toP3Mh+7csWcLLZImipYFOaoz9ez8UeZ99CtYWc3Sf8Y4El0drEvjCfY1WMfzpCRRJ7wQcX+kXX01oBJ7S4sX9exhdwtjjSfV0FnLC5ybZUwPnz8XxhtnS8jSff+wLOFrdasFHA4RbR2sS+cAdurIr1lI5QkcQe6J8M4YMLvaLrSQs4+aqbFl1ShMjPUpBoEaVFinzV75ceJzoKOM1VH7dUwAn7Fqr9nTdhz1UtHW/7kbj6s/apx8vPFHC4lb3+AORUrKd0hIok9oC3Tu8jup6iduR8/PjR7wIesfaAOhXrKR2hIonZyV037rzdR3Q9Re3IkfF99uzZ49evvvrKN7fQu5RK76TqW7+2Tb63dzjtHVCJ4+9e2g95eB13Hf0d24jkp+fgc58Na+9YmM+xKsY/HaEiidnxe2/3E11PUTtyZHx1k0JOirjuu3K+gBPy9q7ut4WWLe6Efctb+OJpq4DTNomncWxsHQfbh+7Toku+yib7tBizb2lHj9ef5e8Cylf7Nr/+vqLs0+OU7pOv/m14/zZ/Fds/9o/5HKti/NMRKpKYGR9cuK/oeorakaOFgm72blxXIecLOPvzUjFiixY5zhdt1lYBZ4soX8DpB0G0YBSSlxZ3koMWbDZHPfbax6/dgZPH6Djofu3HPkZzt/1o39XsHGH/+BDDWBXrKR1Bkjj6xtun9yPjvcXPDdt9ti+//PLxawctVOymRcvSh0RknxYoWrjo5m0VMnq8fF0q4HxhaGPJ8VJQSS5Lb+le+/iogLPFocazBax+bwu4Lt3xgTOpWE/pCBVJACq6nqJ25NhiaORbqEoLKmXfQrX71VbRZMl+XwSJtf70MZ4v4HxO0eOrCjiLO3C4BnfgxqpYT+kIFUkAKrqeonbkyPh+8cUXj19fv37tm1tsFXBSjNg2+d4WTL548sXMWiEjhY9/O1NoASdFlb+zZd8ClX7lGFvA2eLt2sc/pYBbegvVHksBh2swn2NVjH86QkUSgIqup6gdOVq4dd91s7YKOCHFiLTL5gsTKWS0bSmGP15IEWULI/8Wrt7d0599YSWbPt4WcLZ4vPbx2rctyPTrWgEnZL/G0X612NVY1TQ3HAPzOVbF+KcjVCQBqOh6itqRc8/CDXmyHvzbtlsy88vaOxbeQh2rYj2lI1QkAajoeoraAayT9fPUv/PH2gPqVKyndISKJAAVXU9RO4B1sn50u/VDKqy9Y+EO3FgV6ykdoSIJQEXXU9QOYJ0t4LSIk6/XFHKsvWNhPseqGP90hIokABVdT1E7xvIFAts+tmv+zl/Ujn1hPseqGP90hIokABVdT1E7gHW2aOMtVGCcivWUjlCRBKCi6ylqB7BO1s9T/84faw+oU7Ge0hEqkgBUdD1F7QDWaeF27V03i7V3LHyIYayK9ZSOUJEEoKLrKWoHsO4phZti7R0L8zlWxfinI1QkAajoeoraAfRg7R0Ld+DGqlhP6QgVSQAqup6idgA9WHtAnYr1lI5QkQSgouspagfQg7UH1KlYT+kIFUlkSP9sx9q2+GPZ2NjY2Ng6tk4V8dMRKpLIGN3/aEc7/+h8onYAPVh7x8J8busen4r46QgVSWSM7n+0o51/dD5RO4AerL1j4UMM27qv94r46QgVSWSM7n+0o51/dD5RO4AerD2cSff1XhE/HaEiiYzR/Y92tPOPzidqB9CDtYcz6b7eK+KnI1QkkTG6/9GOdv7R+UTtAHqw9o6F+dzWPT4V8dMRKpLIGN3/aEc7/+h8onYAPVh7x8J8busen4r46QgVSWSM7n+0o51/dD5RO4AerL1j4UMM27qv94r46QgVSWSM7n+0o51/dD5RO4AerD2cSff1XhE/HaEiiYzR/Y92tPOPzidqB9CDtXcs3IHb1n29V8RPR6hIImOr//fv33/W/vz584c3b95c7HuKFy9efIojfdjtw4cP7uhf5/Lu3btP+2yMJXK8PG6LP7+9i84nagfQg7V3LMzntu7xqYifjlCRRMZW//cs4KylIk5zkf4VBdznovOJ2gH0YO0dC/O5rXt8KuKnI1QkkbHV/1YBJ5u0+aJKvreFk8bwcbYKuKXCTONIm7LHab8ay/+8Jmrfm+h8onYAPVh7x8JbqNu6r/eK+OkIFUlkbPW/VcDJfn+XzO6T4+TtzqUYYquAk/22UBM2jrZpDNlv77Tp26x+/xLf995F5xO1A+jB2sOZdF/vFfHTESqSyNjqf6n48oWZbLJPCjf9WbeuAk76k58p4D4XnU/UDqAHa+9YuAO3rft6r4ifjlCRRMZW/1qU2TttWsBZr169evzqjxVPKeC23kIV0r/kQQH3ueh8onYAPVh7x8J8busen4r46QgVSWRE/UuhpAWaFEZ6vP29N22XgsoWc1I8PaWA8z8LH0ceLz9LDOlT79hJjlpEag5blvras+h8onYAPVh7x8J8busen4r46QgVSWSM7n+0o51/dD5RO4AerD2cSff1XhE/HaEiiYzR/Y92tPOPzidqB9CDtYcz6b7eK+KnI1QkkTG6/9GOdv7R+UTtAHqw9o6FDzFs677eK+KnI1QkkTG6/9GOdv7R+UTtAHqw9o6F+dzWPT4V8dMRKpLIGN3/aEc7/+h8onacm/8EuP3gkvAfJrL8n/7xnyT3n163JKbGtX3IV/vJdnvc3uw1byzjDty27uu9In46QkUSGZX920+sKvnZ7xP2hcJ/UnTrRaLavfq5l+h8onacW2cB5//EkNJPkWtc+5xh85GvPp892WvewFN0X+8V8dMRKpLIqOxf/yabJT/7Ak34Fwpr60Wi2r36uZfofKJ2nJtfl75g2lqbUQHnfxYST/8wt8a1Ofh/APp89mSveQNP0X29V8RPR6hIIqOyf/3Dv1qw6R/cFbJfN9m/dAdOn8TlCbsyry336udeovOJ2nFueifMb+rWAm4tjtLijQIOe8NbqNu6r/eK+OkIFUlkVPcvT7z2v9byb5v4/0VB+CfwrReJavfq516i84nacW6dd+CWSCy7ibW3UIXPZ0/2mjeWMZ/busenIn46QkUSGdX9a+EmT7r2Cd0+SS8VcPaJWWPcw736uZfofKJ2nFtUMFUXcGrtH3Dy1f4j0OezJ3vNG8u4A7et+3qviJ+OUJFERkf/UqDZ/zNV3yIV3IHrFZ1P1I5zm6GAE/r2qz6HKJ/Pnuw1b+Apuq/3ivjpCBVJZHT0r0++yj4Ja3HnCzhtk333+h24b7/99i793FN0PlE75vXx40e/CzvC2sOZdF/vFfHTESqSyBjd/yhv3759ePnypd+9e9F8Ru2YyzfffPM4Z/KVAm7fWHvHwnxu6x6fivjpCBVJZIzufwS98yZfjyaaz6gdc5BiTYo2CrfjYO0dC/O5rXt8KuKnI1QkkTG6/xHkzpvcgTuiaD6jdoylhZvMk3zFcbD2joUPMWzrvt4r4qcjVCSRMbr/e5O7bke886ai+YzaMRYF3HGx9nAm3dd7Rfx0hIokMkb3f29H/L03K5rPqB1z4C3U42HtHQt34LZ1X+8V8dMRKpLIGN3/PR31gwtWNJ9RO+bChxiOg7V3LMzntu7xqYifjlCRRMbo/u/lyB9csKL5jNoxH/u2KvaL+TsW5nNb9/hUxE9HqEgiY3T/93CGO28qms+ofYn+kVW/dfB/uHXtD7r6P/xqreUZ/WHZtXhABa4vnEn39V4RPx2hIomM0f3fg5zj0e+8qWg+o/YlW8VSNduP9uv/X11bUC7xhZr9z9C3rMUDKnB94Uy6r/eK+OkIFUlkjO7/Ho76J0OWRPMZtS/ZKuD0Dpn+zxryVf9LJf1vj+x/sSTfyz5fZAkptOyxa/+puTx+679U8rH1OLtf/tcP2S9flR6nsX18/R9E7H8TB1zLX0/YNz7EsK37eq+In45QkUTG6P67nal4E9F8Ru1LogJO74zZ/0JN75rJY/W/SBP+/8tUepz9f3Nt0WaLOXFrASdxl2JJmxZj8r3NW+hjtDjVfingcKu16xX7xHxu6x6fivjpCBVJZIzuH7Wi+Yzal0QFnP1e72jZu2lS7Oj+pQJOHqcF1T0KOC3SdLMFnL8LqHnbu27cgcNTrF2v2CfuwG3rvt4r4qcjVCSRMbp/1IrmM2pf8pQCTu9YiaiA07dV7SbW3kIVtxZwdr+NZYsxOc7mre3+OAo4PMXa9QocUff1XhE/HaEiiYzR/aNWNJ9R+5KnFHD+LVRbiG2xd+DWPsQgbing9GdbtOn3EmPrLVTNWws7zYkCDrdau16BI+q+3ivipyNUJJExun/UiuYzal+iRYvfpNhZK+CEFlm2aLulgBMSc6lg2irgfJ5Kc9XHyuZ/H863W3Jukj934PAU/nrCvvEW6rbu670ifjpCRRIZo/tHrWg+l9rlD8V+/fXXi0ULfsW+zUvxhqdgbR0L87mte3wq4qcjVCSRMbp/1Irm07dL8Sb7nj17RgEHNGJtHQt34LZ1X+8V8dMRKpLIGN0/akXzqe1SuH311Vefija7AajH2sKZdF/vFfHTESqSyBjdP2pF8yntWrjZu24UcEAv1hbOpPt6r4ifjlCRRMbo/lErmk8KOGAM1taxMJ/busenIn46QkUSGaP7R61oPrWdt1CB+2JtHQvzua17fCripyNUJJExun/UiubTt+uHGL744gsKOKARa+tY+BDDtu7rvSJ+OkJFEhmj+0etaD6X2qWIe/36NQUc0Ii1hTPpvt4r4qcjVCSRMbp/1IrmM2oH0IO1dyzcgdvWfb1XxE9HqEgiY3T/qBXNZ9QOoAdr71iYz23d41MRPx2hIomM0f2jVjSfUTuAHqy9Y2E+t3WPT0X8dISKJDJG949a0XxG7QB6sPaOhbdQt3Vf7xXx0xEqksgY3f9oRzv/6HyidgA9WHs4k+7rvSJ+OkJFEhmj+x/taOcfnU/UDqAHa+9YuAO3rft6r4ifjlCRRMbo/kc72vlH5xO1A+jB2jsW5nNb9/hUxE9HqEgiY3T/ox3t/KPzidoB9GDtHQvzua17fCripyNUJJExuv/Rjnb+0flE7QB6VK09ifP+/Xu/+9GbN28eXrx44Xd/4ts/fPiwGe8a8liJIdtW38L2b8ejamwklygH3EfVnK6piJ+OUJFExuj+Rzva+UfnE7UD6FG19rYKLl+geb49W8D5x0vsa/uvGg/r+fPnfteqrTyR1zG/VkX8dISKJDJG9z/a0c4/Op+oHUAPv/ZssfHq1avHr1JUSJEjx+o+pfu0YNLvdXv37t2n76XN/qzFylYBp/3KZnOT7/UYoTFkkz7ssXo3TuL6/u3PP/zhDy/6ke81rvZnyT5tkzj6GBtD9vsx2yLxnlq4Cj7EsM3PYbWK+OkIFUlkjO5/tKOdf3Q+UTuAHn7trRVwcpwWVlqsaNGlRZAUHvbxSwWaxhF67FYBZ/Oz+ej32q6FnpLv/bnp/q3+fQyNawtF+xg9d/mqcYXkp8fcUpDJY7SPp1g6Z/xa9/hUxE9HqEgiY3T/ox3t/KPzidoB9PBrb62A0++1KNEiS8njtICT/brZxwjbttQuNLZ8tb/LJrG1zW5SPEkMm7tlizv/WG3fKuCWxsTeddu6A6fncS1fBEdsvkui9rPpHo+K+OkIFUlkjO5/tKOdf3Q+UTuAHn7tLRUrtoCTr/7ul37vf1nfF312n+Xf8rQFnGX79W220NK7X8rGW+q/soBTmoMWndfyYxjx8+dF7WfTPR4V8dMRKpLIGN3/aEc7/+h8onYAPfza8wWMkIJCv5evWqxoMWPfQtXiw96h8wWSFjT6eD3WxtXjl4onW1Bqv7bQsr/zpo9buiO2VGD6818r4PQx2pfkbt8q1QJOj7mWPMYWnxE/f17Ufjbd41ERPx2hIomM0f2PdrTzj84nagfQw689LdZkswWT7vfFheyTNily/NudWpDpfjlOiz09RvnHKslB99s7U3KM7NM+fKG1Fm+pf5uftgn56uPaO3iyX8/dv4Wqucp++xibr+z3b5neWvBprmui9rPpHo+K+OkIFUlkjO5/tKOdf3Q+UTuAHtesPXvHC79iiz3/9ql3y9ui/u1W7ePrr79++Pjx40Wbtm+J2s+mezwq4qcjVCSRMbr/0Y52/tH5RO0AerD25qYF3LNnzx6/+iIumr+o/Wy6x6MifjpCRRIZo/sf7WjnH51P1A6gB2tvblrA2e2rr776VMhF8xe1n033eFTET0eoSCJjdP+jHe38o/OJ2gH0YO3NzRdvsundOCnkovmL2s+mezwq4qcjVCSRof37POSXPf0v0T6VxrG/7Kqb/z0E4X8HRH72+5T9RJP/hVTtb0vUvjfR+UTtAHqw9ubmX5tko4B7uu7xqIifjlCRRIb27/PoLOCUfkrJ8/vkZ1+cqa1fWPX9LYna9yY6n6gdQA/W3txkfvzGW6hP1z0eFfHTESqSyND+fR62gLN3zuzf9dF9cpz/eLjl41hLBZgcYws2/ei3/Vi6fhpp6Q6cHqM5bona9yY6n6gdQA/W3tz0deOLL754/MqHGHK6x6MifjpCRRIZ2r/PwxZwtoDSgkn36R+G1OJs6WPeWwXc0l0+6cPul+/tX/i2fy9oqYCzf6TS9+dF7XsTnU/UDqAHa29uMj+yvX79+rPiTdu3RO1n0z0eFfHTESqSyND+fR72DyYKvbj1Dpz+rJsWZ0u/03ZrAeeLNWXv+q0VcDZn+xfK10TtexOdT9QOoAdrb9+i+Yvaz6Z7PCripyNUJJGh/WvBpHwBJ+wdON/21AJu6S1UoXcAtVCzj5W+1wo4+9brUn9e1L430flE7QB6sPb2LZq/qP1susejIn46QkUSGdq/FET2k542L1s4+bdQ9funFHBrH2IQ8hhps33bXNcKOH2s4C3Uz0XtAHqw9vYtmr+o/Wy6x6MifjpCRRIZtn8tmGSzhZj98ICyb2fq/zPnH6d8AWe3NXqs///wZJ/ta6mAs3lt9SGi9r2JzidqB9CDtbdv0fxF7WfTPR4V8dMRKpLIGN3/aEc7/+h8onYAPVh7+xbNX9R+Nt3jURE/HaEiiYzR/Y92tPOPzidqB9CDtbdv0fxF7WfTPR4V8dMRKpLIGN3/aEc7/+h8onYAPVh7+xbNX9R+Nt3jURE/HaEiiYzR/Y92tPOPzidqB9CDtbdv0fxF7WfTPR4V8dMRKpLIGN3/SC9fvnx4+/at371r0XxG7QB6sPb2LZq/qP1susejIn46QkUSGaP7H0UKNyngjiaaz6gdQA/W3r5F8xe1n033eFTET0eoSCJjdP8jfPvtt4/nLV+PJprPqB1AD9bevkXzF7WfTfd4VMRPR6hIImN0/yMc8a1TFc1n1A6gB2tv36L5i9rPpns8KuKnI1QkkTG6/3uTu25HvPOmovmM2gH0GLX25I+d2/9T2v5hdr/psX6/bEv7l/5wu/2D7fa/SpSft0Tto0X5Re1n0z0eFfHTESqSyBjd/70d8fferGg+o3YAPUatvbUCTvn/4lD5x8n3tiCz/ye1koJOYun/iiPH2/8tZ0vUPlqUX9R+Nt3jURE/HaEiiYzR/d/TUT+4YEXzGbUD6DFq7flCrKqA02LN8kWdxv75z3/++FXb9LGyyX95qD9ru8bV/fZ42aSfe/Pn6kXtZ9M9HhXx0xEqksgY3f+9HPmDC1Y0n1E7gB6j1p4vxKoKOP+z0iLLs/u0ULP/r7Vt1+9tAae5+GPvJeozaj+b7vGoiJ+OUJFExuj+7+EMd95UNJ9RO4Aeo9aeL8SqCrhr6O/NCduH/z08X5Tp97aAk771eO7Aza97PCripyNUJJExuv97kHM8+p03Fc1n1A6gx6i15wuxzgJO3g61j/FvgSr9fq3dv9UqX5U/9l6iPqP2s+kej4r46QgVSWSM7v8ejvonQ5ZE8xm1A+gxau35QqyzgLNviQop6NZ+r03b9Xibgz5G7+BJu95188feS9Rn1H423eNRET8doSKJjNH9dztT8Sai+YzaAfQYtfZ8IdZZwAn7Z0TsBxp0n9DCTT/0YN9StTF8gaebfsr1npbGyIraz6Z7PCripyNUJJExun/UiuYzagfQY23t/exnP3v40z/904e//du/9U2YyNr8qaj9bLrHoyJ+OkJFEhmj+0etaD6jdgA9/NrTwk02+R5z8/PnRe1n0z0eFfHTESqSyBjdP2pF8xm1A+hh154Wb//yL//y8N133z1uQr/XnzP77M9L+9Yed+2+s8WPnjuj9rPpHo+K+OkIFUlkjO4ftaL5jNoB9JC1Z++6/fKXv3z4t3/7t8c/cvs///M/j8fYfUr36TG6zz5ObMVa2qeIv7xP6b7ouTNqP5vu8aiIn45QkUTG6P5RK5rPqB1AD117WsTxtum+RM+dUfvZdI9HRfx0hIokMkb3j1rRfEbtAHr4tcfvv+2Lnz8vaj+b7vGoiJ+OUJFExuj+USuaz6gdQI+ltWffUsXclubPitrPpns8KuKnI1QkkTG6f9SK5jNqB9Bja+1JIffTn/7U78ZEtuZPRO1n0z0eFfHTESqSyBjdP2pF8xm1A+jB2tu3aP6i9rPpHo+K+OkIFUlkjO4ftaL5jNoB9GDt7Vs0f1H72XSPR0X8dISKJDJG949a0XxG7QB6sPb2LZq/qP1susejIn46QkUSGaP7R61oPqN2AD1Ye/sWzV/Ufjbd41ERPx2hIomM0f2jVjSfUTuAHqy9fYvmL2o/m+7xqIifjlCRRMbo/lErms+oHUAP1t6+RfMXtZ9N93hUxE9HqEgiY3T/qBXNZ9QOoAdrb9+i+Yvaz6Z7PCripyNUJJExun/UiuYzagfQg7W3b9H8Re1n0z0eFfHTESqSyBjdP2pF8xm1A+jB2juWn/zkJ34XjO7rvSJ+OkJFEhmj+0etaD6jdgA9WHs4k+7rvSJ+OkJFEhmj+0etaD6jdgA9WHs4k+7rvSJ+OkJFEhmj+0etaD6jdgA9WHvHwluo27qv94r46QgVSWSM7h+1ovmM2gH0qF571fFwG8Z/W/f4VMRPR6hIImN0/6gVzWfUDqBH9dqrjofbcAduW/f1WRE/HaEiiYzR/aNWNJ9RO4Ae1WuvOh5Qqfv6rIifjlCRRMbo/lErms+oHUCP6rVXHQ+o1H19VsRPR6hIImN0/6gVzWfUDqBG91rrjo9tjP+27vGpiJ+OUJFExuj+USuaz6gdQI3utdYdH9sY/23d41MRPx2hIomM0f2jVjSfUTuAGt2/5M5aHqt7fveu+/qsiJ+OUJFExuj+USuaz6gdwD6wljGz7uuzIn46QkUSGaP7R61oPqN2ADW679Cwlsfqnt+9674+K+KnI1QkkTG6f9SK5jNqB1Cje611x8c2xn9b9/hUxE9HqEgiY3T/qBXNZ9QOoEb3WuuOj22M/7bu8amIn45QkUTG6P5RK5rPqB1Aje632FjLY3XP7951X58V8dMRKpLIGN0/akXzGbUD2AfWMmbWfX1WxE9HqEgiY3T/qBXNZ9QOoEb3HRrW8ljd87t33ddnRfx0hIokMkb3j1rRfEbtAGp0r7Xu+NjG+G/rHp+K+OkIFUlkjO4ftaL5jNoB1Ohea93xsY3x39Y9PhXx0xEqksgY3T9qRfMZtQPYB9YyZtZ9fVbET0eoSCJjdP+oFc1n1A5gH1jLmFn39VkRPx2hIomM0f2jVjSfUTuAGt2/5M5aHqt7fveu+/qsiJ+OUJFExuj+USuaz6gdQI3utebjy8/v37//9POrV68eXrx4YY64jcTSPt68eeNa10mf8jifTxQjap+NH39c6h6fivjpCBVJZIzuH7Wi+YzaAdTovkPj17IvmHwBp0WVbB8+fHj8KsfoPqWFm7YJW1w9f/78s75s27t37z79LP1LHI2lbbZfycW2S18SR4+TTejx2jZa9/zunb2mOlTET0eoSCJjdP+oFc1n1A5gH/xa9kWVLeCWiio5fqldC6mlAk4fq8d5cpzs12N8m5DYWoTZeNq+VMBJkafk573drTujpeujUkX8dISKJDJG949a0XxG7QD2wa9l+dlvS2+hLt0RkwJJNvu26dJbqPKzFlP+bpvSu3uy2UJuqeiyd9iiAs4WcZifvz6rVcRPR6hIImN0/6gVzWfUDqBG91tsfi3Lz2t34LTdFlby1Rdw8rPG1UJMSFFlCzPdlgo4S46xhZnSt2E1F9u+VMAJPZ63UPdBr50uFfHTESqSyBjdP2pF8xm1A6jRvdZ8fPl5rYDTIsj/7psv4IQUSFJE6TFi7S1U/3twtl370j40huZlc7HttojUdn9eS3fz7s2PPy51j09F/HSEiiQyRvePWtF8Ru0AanTfofFr2RdUtoDTYknf9tQ7YEsFnC/yxNLds7W7b/ZTqP536+ydPJuLbbcxlu7ALb0tPEL3/O6dvz6rVcRPR6hIImN0/6gVzWfUDmAfWMuYWff1WRE/HaEiiYzR/aNWNJ9RO4B9YC1jZt3XZ0X8dISKJDJG949a0XxG7QB6VK+96ni4DeO/rXt8KuKnI1QkkTG6f9SK5jNqB9Cjeu1Vx8NtGP9t3eNTET8doSKJjNH9o1Y0n1E7gB7Va686Hm7Dhxi2dV+fFfHTESqSyBjdP2pF8xm1A+hRvfaq4wGVuq/PivjpCBVJZIzuH7Wi+YzaAfSoXnvV8XAb7sBt674+K+KnI1QkkTG6f9SK5jNqB9Cjeu1Vx8NtGP9t3eNTET8doSKJjNH9o1Y0n1E7gB7Va686Hm7D+G/rHp+K+OkIFUlkjO4ftaL5jNoB9Khee9XxgErd12dF/HSEiiQyRvePWtF8Ru0AelSvvep4QKXu67MifjpCRRIZo/tHrWg+o3YAParXXnU83IYPMWzrvj4r4qcjVCSRMbp/1IrmM2oH0KN67VXHw20Y/23d41MRPx2hIomM0f2jVjSfUTuAHtHa++abb/yuTVE89OIO3Lbu67MifjpCRRIZo/tHrWg+o3acz4sXLx7evHnz6ed37959uk7kq930WL9ftvfv33+278OHD5/iKnucxLP7t0Tts9Mx9T5+/Pjw9ddfr7avufV44J66r8+K+OkIFUlkjO4ftaL5jNpxPlsFnNCCa4l9nLAFmXj+/PnFz1LQSSwtxuR4fUxUoEXts/NjKIXbV1999bj/2bNnn7VHbj0euKfu67MifjpCRRIZo/tHrWg+o3acT2cB5x8nsW1Rp7F//vOfP37VNi30ZHv16tWnn7VdvpdYul/v9OljpG02diykeJOftXDT7Ra3Ho9avIW6rfv6rIifjlCRRMbo/lErms+oHecjRZctInwxcUsBtxbDk6JMj9Hia+kOmy3QbLvs8wWcxJyZnu+XX3752Tg9dcM4jP+27vGpiJ+OUJFExuj+USuaz6gd59N5By6iv08nbIGmOeh2TQGn+7VtNpKXfdt0acN+cAduW/f1XBE/HaEiiYzR/aNWNJ9RO87nngWc3CWzj9m6wyY/r7XLW6lLBZzQfbOxOVW8hQrMrPt6roifjlCRRMbo/lErms+oHedzzwJOY2nBJQWd/l6bFmhagMnP+lbrUgEnfesdPGnXvvdQwAkp4l6/fv24/4svvvisHdiz7uu5In46QkUSGaP7R61oPqN2zEte8LFf0dq79e/AYaxoPs+ue3wq4qcjVCSRMbp/1IrmM2rHfKRwkxd35m7fmL9jYT63dY9PRfx0hIokMkb3j1rRfEbtmIsWbvKVO3D7xto7Fj7EsK37eq+In45QkUTG6P5RK5rPqB1z0LtuFG7HwdrDmXRf7xXx0xEqksgY3T9qRfMZtWMsmZ8/+qM/evjFL37x8N133z3+/P3vf//hb/7mbx7bf/zjH1/sk2PsPmH36eN0n41F/J74a+QYHAd34LZ1X+8V8dMRKpLIGN0/akXzGbVjLPv7bvxS+7Gw9o6F+dzWPT4V8dMRKpLIGN0/akXzGbVjDryFejysvWNhPrd1j09F/HSEiiQyRvePWtF8Ru2YCx9iOA7W3rHwFuq27uu9In46QkUSGaP7R61oPqN2zIsCbt9YeziT7uu9In46QkUSGaP7R61oPqN2AD1Ye8fCHbht3dd7Rfx0hIokMkb3j1rRfEbtAHqw9o6F+dzWPT4V8dMRKpLIGN0/akXzGbUD6MHaOxbmc1v3+FTET0eoSCKju3+Jz8bGxsY23wZ06b6+KuKnI1QkkdHdf3f82d37/KP+onYA58BzATp1X18V8dMRKpLI6O6/O/7s7n3+UX9RO4Aas/+SO88FObPP72jd11dF/HSEiiQyuvvvjj+7e59/1F/UDqDG7Gtt9vxmx/ht6x6fivjpCBVJZHT33x1/dvc+/6i/qB1Ajdnv0PBckDP7/I7WfX1VxE9HqEgio7v/7vizu/f5R/1F7U/x5s2bi587+oi8ePHiUx769d27d4+52LaIHAucwYh1ivPovr4q4qcjVCSR0d1/d/zZ3fv8o/6i9qfwxVFHH5GlIu3Vq1ef7YtQwOEsRqxTnEf39VURPx2hIomM7v6748/u3ucf9Re1P4Uvkmwf8r1scjfM79NiSb7KJvtsLPle99uYUpjpPjnG/iz9+H22uHv+/Pmn/e/fv//Uj88JyJr9LTa7pnC72ed3tO7rqyJ+OkJFEhnd/XfHn929zz/qL2p/irUCTooo2Sx7rBRbUlBJ0eSPE3KstAsby8bQfUtvoepjbNuHDx9+9cCHXxVzWiQqCjhU6VhrlWbPb3aM37bu8amIn45QkURGd/9r8e2dFb0Tovu3RO1ZeofG9mPv0Ng7SXoOtijw1s6/S9Rf1P4UvoDTokvoGOm42Ttjst2zgJN5sn3Lpu2q+/rCecx+h6bjueBMZp/f0bqvr4r46QgVSWR0978W375QyjFaxEUvoFF7hryY64u/3p0Reg5aAAh758YWLN7a+XeJ+ovan8IXX348/Lh59yrghC+27TyKzusLmEnHcwGguq+vivjpCBVJZHT3vxbf38nSF1s5Xtv0bph9Edd2+6Iv7foYeUG2j3kqWzDY4sIWbdqn9GfvIlpr598l6i9qfwqJaQsjLZZkrGSMbAEn46THanF1awGnsWzcaws4jaGP1a/X/gMCOIqO5wJAdV9fFfHTESqSyOjufy2+fTtrqUCybLGk7UsFnH1Bz9Ai0P6sbBGhL/o2P+vbb78tyecWUX9RO4Aas6+12fObHeO3rXt8KuKnI1QkkdHd/zXx5Rh710b431e6poATUlTJ8Zk7cPL4pTtK4pYC7uXLlw9v3771u1tF4x21A6gx+1qbPb/ZMX7busenIn46QkUSGd39L8X3v4dk3x6zBZoWTkt34OxjlgooabOFl9C3ZHVbKvL8Y8TSHcLoLVQp3KSAu7el8baidgA1Zv8ld54Lcmaf39G6r6+K+OkIFUlkdPe/Ft8WRXKML9CkQNJiaqldf7fJtksRZX9naqkY2yIxloo6PQf/y/g2V0vfOpWv97Y23ipqB3AOPBegU/f1VRE/HaEiiYzu/rvjz2jEnTcVjXfUDqDG7HdoeC7ImX1+R+u+viripyNUJJHR3X93/NnIXbcRd95UNN5Re4b/ROeZnPW8sa5zrVWYPb/ZMX7busenIn46QkUSGd39d8efzci7byIa76g9Q96CXvoU8RlQwMHrXGsVZs9vdozftu7xqYifjlCRREZ3/93xZzLqgwtWNN5Ru5Bj9E+p6PH6O3/6sxRrtj36Wcjj7YdT5PcUtS+lj7H79ZPFsmmBuBTfHuc/KCO0zX7gxfbn/wyN/KxF2VKuQvZpGwUcrNnfYrPXOm43+/yO1n19VcRPR6hIIqO7/+74sxjxJ0OWROMdtQtf5Mj3UrjohzXke/shFN2vHwJZa7d/+kW+1z7sJ4o1P/uHdv1dvbX4S59GVraPJdpuj7s2V/2eAg57cs1zAfBU3ddXRfx0hIokMrr7744/gxnuvKlovKN24Qs4KZhsAad3nHTzBdxauy3gZJ89xhdFWwXcWnyh+5buwOkdxLU7cBLXfpLZfvXH+bt1cgwFHKzZ79DY6xe3m31+R+u+viripyNUJJHR3X93/BnIOY784IIVjXfULuSYW+7AqbU7cGrtDpyl+W0VcGvxlcT1j1H+T8Eof2dNz0VEuer3FHCwrllrI82e3+wYv23d41MRPx2hIomM7v67489ghrdOVTTeUbuQY7QAkqJEih5bwOnvmuldLvt7aXonaqndFnA2nhZ9QvOzBZzNWWKsxdd9SwWcFmE2nuai+2xxKT9rTmu56vESV46ngIN1zVobafb8Zsf4besen4r46QgVSWR09y/xj77NJMpnq/1nP/vZ4tuMAI5n67kAyOq+viripyNUJJHR3X93fFyKxnupXQq3P/iDP3j4nd/5nU9FKQUccGxLzwVAle7rqyJ+OkJFEhnd/XfHx6VovH27FG/Pnj37VLj5dgBPM/svubPWc2af39G6r6+K+OkIFUlkdPffHR+XovHWdincfu/3fu+z4i16PIDrzL6WZs9vdozftu7xqYifjlCRREZ3/93xcSkab2n/rd/6rc+KNrv92Z/9GVvThvOY/Q5N9FyBbbPP72jd11dF/HSEiiQyuvvvjo9L0XhL+9bdt+jxAI6BtY5O3ddXRfx0hIokMrr7746PS9F423Yt5CjggPNhraNT9/VVET8doSKJjO7+u+PjUjTeS+2///u///Dbv/3bFHBAodnfYmOt58w+v6N1X18V8dMRKpLI6O6/Oz4uReMdtctdOQB50Vobbfb8Zsf4besen4r46QgVSWR0998dH5ei8Y7aAdSY/Q4NzwU5s8/vaN3XV0X8dISKJDK6+++Oj0vReEftAM6B5wJ06r6+KuKnI1QkkdHdf3d8XIrGO2oHcA48F6BT9/VVET8doSKJjO7+u+PjUjTeUTuAGrOvtdnzmx3jt617fCripyNUJJHR3X93fFyKxjtqB1Bj9rU2e36zY/y2dY9PRfx0hIokMrr7746PS9F4R+0Aasz+S+48F+TMPr+jdV9fFfHTESqSyOjuvzs+LkXjHbUDOAeeC9Cp+/qqiJ+OUJFERnf/3fFxKRrvqB1Ajdnv0PBckDP7/I7WfX1VxE9HqEgio7v/7vi4FI131A6gxuxrbfb8Zsf4besen4r46QgVSWR0998dH5ei8Y7aAdSYfa3Nnt/sGL9t3eNTET8doSKJjO7+u+PjUjTeUTuAGrO/xcZzQc7s8zta9/VVET8doSKJjO7+u+PjUjTeUTuAc+C5AJ26r6+K+OkIFUlkdPffHR+XovGO2gHUmP0ODc8FObPP72jd11dF/HSEiiQyuvvvjo9L0XhH7QBqzL7WZs9vdozftu7xqYifjlCRREZ3/93xcSka76gdQI3Z19rs+c2O8dvWPT4V8dMRKpLI6O6/Oz4uReMdtQM4B54L0Kn7+qqIn45QkURGd//d8XEpGu+oHcA58FyATt3XV0X8dISKJDK6+++Oj0vReEftAGrM/kvuPBfkzD6/o3VfXxXx0xEqksjo7r87Pi5F4x21A6gx+1qbPb/ZMX7busenIn46QkUSGd39d8fHpWi8o3YANWa/Q8NzQc7s8zta9/VVET8doSKJjO7+u+PjUjTeUTuAc+C5AJ26r6+K+OkIFUlkdPffHR+XovGO2gGcA88F6NR9fVXET0eoSCKju//u+LgUjXfUDqDG7G+x8VyQM/v8jtZ9fVXET0eoSCKju3+Jz8bGxsY234anY/y2dY9PRfx0hIokMrr7744/u3uff9Rf1A6gxux3aHguyJl9fkfrvr4q4qcjVCSR0d1/d/zZ3fv8o/6idgDnwHMBOnVfXxXx0xEqksjo7r87/uzuff5Rf1E7gHPguQCduq+vivjpCBVJZHT33x1/dvc+/6i/qB1AjdnX2uz5zY7x29Y9PhXx0xEqksjo7r87/uzuff5Rf1E7gBqzr7XZ85sd47ete3wq4qcjVCSR0d1/d/zZ3fv8o/6idgA1Zv8ld54Lcmaf39G6r6+K+OkIFUlkdPffHX929z7/qL+oHcA58FyATt3XV0X8dISKJDK6+++OP7t7n3/UX9QOoMbsd2h4LkCn7uurIn46QkUSGd39L8V/8+bN437Z5Hvd9+LFC3fkr0XtW96/f//Y14cPH3zTIs3N5iekfx9HY7969erTPmvp/DtF/UXtT2HHSHT0EZG5sdeSePfu3WMuti3y1GsM8Easg1vMnh/2rfv6qoifjlCRREZ3/z6+vqgq/T4q0KL2LbcWcHK8kOOX8nv+/PmnY7Vd9sm5ef78u0X9Re1P4Yujjj4iS0WaFNV+X+Sp1xjgjVgHt5g9P+xb9/VVET8doSKJjO7+fXx5UbV3q+QFVos62bRdf5bNt9sCSmNpkSabL9RuLeAs7csWaBJLYsrP2r5WYPrz7xb1F7U/hS+SbB92Dv0+HS/5qnc3bSwdUz1e2etDjrE/Sz9+ny3uZL50vxbq0uZzArJ4CxVn1n19VcRPR6hIIqO7/7X4+oIZFUBaoK3dAdP2tTtg4qkFnBYWwr7ga182J1vMqW+//Xb1/LtE/UXtT7FWwPliXdhjdcxkDP1xQo7VMbWxbAzdZ4s0/aqPsW32GpDY/rpbugaBI+p4LgBU9/VVET8doSKJjO7+o/j6gukLNC3w9PG+XdkXfj3eF2pPKeD8nT55/K0F3MuXLx/evn17sa9bNN5R+1P4As6Og4yP9KnFtb0zJts9Czh9S9xu2q4o4FCFO3A4s+7rqyJ+OkJFEhnd/fv48oKshZDQF3ZbDNkXUfuiHRVwyr8I2wJu6UXck8f7GPYOnzwmegtVCjcp4O5t6XysqP0p/Bz4Qtb/LqF3rwJO+CLez5ufd+CpOtZapdnzw751X18V8dMRKpLI6O7fx7cvymKpANKv/oVf99uY+r181Rfn7IvwWjGxVEBq/7bA07dO5eu9+fH2ovansGMvtFiS8ZIxsfMoha8eq8XVrQWcxrJxry3gNIY+Vr/qPyqy1w6gOtZapdnzw751X18V8dMRKpLI6O6/O/6MRtx5U9F4R+0AzoHnAnTqvr4q4qcjVCSR0d1/d/zZyF23EXfeVDTeUTuAc+C5AJ26r6+K+OkIFUlkdPffHX82I+++iWi8o3YANfgQA86s+/qqiJ+OUJFERnf/3fFnMuqDC1Y03lE7gBqzr7XZ88O+dV9fFfHTESqSyOjuvzv+LEb8yZAl0XhH7QBqcAcOZ9Z9fVXET0eoSCKju//u+DOY4c6bisY7ah9JPnE6c37AkbDW0Kn7+qqIn45QkURGd//d8Wcg5zjygwtWNN5R+yj2T4IA6Md6Q6fu66sifjpCRRIZ3f13x5/BDG+dqmi8o/Z70bttsvk/ruzpfv1bbvK9/L02+Sp/v81+b4+3f9tNHwPcC2+h4sy6r6+K+OkIFUlkdPdvX5iPus0kyidqvwfJwf9vHPYP61q26NI/zCvH2f/31n5vz0//uLL/nyGAe5hhrW2ZPT/sW/f1VRE/HaEiiYzu/rvj41I03lH7PdxSwNk7dbJtFXD+bVgKOIzEHTicWff1VRE/HaEiiYzu/rvj41I03lH7PdxSwC297blWwGmbooAD1s3wXIDj6r6+KuKnI1QkkdHdf3d8XIrGO2q/B/3/SYUWb2sFnP2P66UQk5+3Cjj9v05ln8ajgAM+N8NzAY6r+/qqiJ+OUJFERnf/3fFxKRrvrfaPHz8+fPPNN343gCfYWmszmD0/7Fv39VURPx2hIomM7v674+NSNN5L7VK4ff31149tS+0Abjf7Wpo9P+xb9/VVET8doSKJjO7+u+PjUjTevl2KN9n37NkzCjigEB9iwJl1X18V8dMRKpLI6O6/Oz4uReMt7V999dVnRZvdABwfax2duq+vivjpCBVJZHT33x0fl6LxlvYvv/zys6KN7T4bzoM7cDiz7uurIn46QkUSGd39d8fHpWi8pZ07cEC/2dfS7Plh37qvr4r46QgVSWR0998dH5ei8dZ2+d03LeT8BiBv9rU0e37Yt+7rqyJ+OkJFEhnd/XfHx6VovJfapZh7/fo1BRxwIqx1dOq+viripyNUJJHR3X93fFyKxnurnb8DB5zH1nMBkNV9fVXET0eoSCKju//u+LgUjXfUDqAGH2LAmXVfXxXx0xEqksjo7r87Pi5F4x21A6gx+1qbPT/sW/f1VRE/HaEiiYzu/rvj41I03lE7gBrcgcOZdV9fFfHTESqSyOjuvzs+Lsl4RxvG8XPhN+BeuN7Qqfv6qoifjlCRREZ3/93xAQC347kZnbqvr4r46QgVSWR0998dH9tmfxsHOKrZ1x7PzejUfX1VxE9HqEgio7v/7vjYxvgDY8y+9mbPD/vWfX1VxE9HqEgio7v/7vjYNvtdAOCoZl97PDejU/f1VRE/HaEiiYzu/rvjAwBux3MzOnVfXxXx0xEqksjo7r87PgDgdjw3o1P39VURPx2hIomM7v6742Mb4w+MMfvamz0/7Fv39VURPx2hIokM6Z+NjY2NjY2NrXLrVBE/HaEiCWDN7L9IDRwVaw/oU1E7pSNUJAEAAHAWFbVTOkJFEsAa7gIAY7D2gD4VtVM6QkUSwBquL2AM1h7Qp2J9pSNUJAGs4foCxmDtAX0q1lc6QkUSwBrexgHGYO0BfSpqp3SEiiQAAADOoqJ2SkeoSAJYw10AYAzWHtCnonZKR6hIAljD9QWMwdoD+lSsr3SEiiSANVxfwBisPaBPxfpKR6hIAgAA4Cwqaqd0hIokAAAAzqKidkpHqEgCWMMvUgNjsPaAPhW1UzpCRRLAGq4vYAzWHtCnYn2lI1QkAazhLgAwBmsP6FNRO6UjSBJsbGxsbGxsbGzXb1n5CAAAALgrCjhMjbdxgDFYe8DcKOAwtYrbzABux9oD5sYKxdS4CwCMwdoD5kYBBwAAsDMUcAAAADtDAYep8Xs4wBisPWBurFBMjd/DAcZg7QFzo4ADAADYGQo4AACAnaGAAwAA2BkKOAAAgJ2hgAMAANgZCjgAAICdoYADAADYGQo4AACAnaGAAwAA2BkKOAAAgJ2hgAMAANgZCjgAAICdoYADAADYGQo4AACAnaGAAwAA2BkKOAAAgJ35f74+lvMYBzx3AAAAAElFTkSuQmCC>

[image4]: <data:image/png;base64,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>

[image5]: <data:image/png;base64,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F77wBdoFQ2nvwP3rv/7r3X9q+Jd/+Zd7y0noAGDdrjIDEpX7rD2+/vWv303cicPI/uEf/uGNxO7bv/3bT7/6q796b5kldSR2AEACt2s+geNOHGZjd+Ws33t/93d/d/r7v/97kjgA09tVRkCCcp9P4JTEcRcOs7DPyv37v//7vWW/+Iu/ePq2b/u203/+53/eW/7f//3f9+YBYHS7yphI4O5rEzjuxAGnN77wYM+/93u/9/TXf/3Xb9yZ++ijj+7NA8AodpUJkJjct5TAcScOuO8//uM/Tu+8887dlyE8S+b++I//mLtzAIa0q4yJBO6+tQTO34kjkQOW/e3f/u3px37sx95I7H73d3/39I//+I/3lgHA0ewqYyKBuy9K4HwSB+B83/md33l677337i2zt1q5UwfgSHb16n9JMvLs2bN781b2xYsX95Zd6qHyjx8/fv385cuXr/fBnlv99thSGR2b38ZDHkrglMRxFw4439tvv333o8LeV77ylbux1P6UCQDs1fkZ0wZ6JHC23J77bdnPEfgEzx4tkWrrs+U2PXny5I1tGJ+g2Tq2XSVyNm9UTutaParfltm81WHzqkf7pXl71PbO0e4ngMt86UtfOv3SL/3SG196+LVf+7XTX/7lX76xHABubVev/JckImsJnN+G1vHJlbH1nj9//no9UQLXlvOUbFlSZnHbjpIyv02VXboDp/1U4ueXad2lutdc0m4AzvdDP/RDpz/4gz944+1V+xmT9huvALClXb3yX5KILCVwdodLiZC2pbc3NVmSZessvd2pBE6xpSRPSZs92nqWyPm3RVXPOQmclbdtKRlUGe7AAfv11a9+9fQd3/Edd/89wvvGN75x+uY3v3lvGQBcy65e+S9JRNrkSgmR5z+j5lUSOH/nTfOqxyeVJHDAmOwHht999903fmT4J37iJ06//du/fW8ZAFzLrl75L01E/J01scSnXabPoCnpqiRwlqT5O25+Hd3ts/psMlpXidtaAme07yRwwPH84R/+4elv/uZv2sUAcBW7euUnEflfltAtJY9raDcAAOayq1f+mRMR/+3ZS+6+mZnbDQCAGe3qlZ9EJId2AwBgLrt65ScRyaHdAACYy65e+W+RiOhtS5v0ZYKjuUW7AQCA29nVK/8tEhH7Rqj4nwQ5klu0GwAAuJ1dvfLfIhHxCZx9eUA/+2HfArX9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>

[image6]: <data:image/png;base64,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>

[image7]: <data:image/png;base64,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>