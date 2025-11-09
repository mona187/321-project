# Test Coverage Explanation

## Code That Cannot Be Covered by API Tests

### `src/server.ts` (0% coverage - Lines 1-129)

**Why it cannot be covered:**
- This is the application entry point that starts the HTTP server
- Contains process-level event handlers (`unhandledRejection`, `uncaughtException`, `SIGTERM`, `SIGINT`)
- Calls `server.listen()` which starts the actual server process
- Contains background task initialization with `setInterval`
- This code is meant to be executed directly when the application starts, not tested via API calls

**Explanation:**
- Testing this would require starting an actual server process, which is not feasible in unit/integration tests
- Process event handlers are triggered by system signals and cannot be reliably tested via API calls
- Background tasks (`setInterval`) run continuously and are not triggered by API requests
- The code is framework/infrastructure code that is standard in Node.js applications

**Percentage of total codebase:** ~2-3% (129 lines out of ~4000+ lines)

---

### `src/app.ts` Line 26 (Health Check Endpoint)

**Status**: ⚠️ **NOT COVERED** - Health check endpoint `GET /health`
- **Why it's not covered**: Tests don't currently hit this endpoint
- **Can it be covered**: ✅ **YES** - This is testable via API test: `GET /health`
- **Recommendation**: Add a test for the health check endpoint if needed, but it's a simple endpoint that returns status
- **Percentage of total codebase:** <0.1% (simple health check endpoint)

---

### `errorHandler.ts` requireParam Defensive Checks (Lines 22-23, 27-28, 33-34, 39-40, 44-45)

**Status**: ✅ **NOW COVERED** - Tests added in `with-mocks/errorHandler.mock.test.ts`:
- **Lines 22-23**: `paramName` validation - Tested with empty string, null, and non-string values
- **Lines 27-28**: Whitelist check - Tested with param name not in `ALLOWED_PARAMS`
- **Lines 33-34**: Missing parameter check - Tested when paramName is not in `req.params`
- **Lines 39-40**: Falsy value check - Tested with empty string and null values
- **Lines 44-45**: Type check - Tested with non-string value

**Explanation:**
- These are defensive programming checks (defense in depth) to prevent programming errors
- They protect against internal bugs (e.g., developer passing wrong param name), not API misuse
- Tests directly call `requireParam` with invalid inputs to exercise all defensive paths

---

### `auth.middleware.ts` optionalAuth Catch Block (Lines 111-113)

**Status**: ⚠️ **PARTIALLY COVERED** - Test added in `no-mocks/auth.test.ts` to attempt coverage via API
- **Lines 111-113**: Outer catch block - Test attempts to trigger via edge cases, but may not always execute
- **Lines 104-106**: Inner catch block - ✅ **COVERED** - Test with invalid token triggers this path

**Why the outer catch block is difficult to trigger via API:**
- The inner try-catch (lines 97-106) catches all errors from `jwt.verify`
- Express always provides `req.headers` as an object, so accessing `authorization` is safe
- `authHeader.substring(7)` is safe because we check `startsWith('Bearer ')` first
- `process.env` access is safe
- `next()` rarely throws in Express

**Explanation:**
- The outer catch block would only be triggered if:
  - `req.headers` is not an object (nearly impossible with Express)
  - `next()` throws an error (extremely rare)
- This is defensive programming for edge cases that are nearly impossible to trigger via normal API requests
- Test verifies middleware continues to work correctly, even if the catch block isn't always executed

**Percentage of total codebase:** <0.1% (extremely rare error path)

---

### `authService.ts` Uncovered Lines

**Line 89**: Profile picture conversion check in `findOrCreateUser`
- **Status**: ✅ **NOW COVERED** - Tests added in `no-mocks/auth.test.ts`:
  - **True branch**: When `googleData.picture` exists - Covered by tests that provide a picture
  - **False branch (else)**: When `googleData.picture` is missing/undefined - Test added to cover this path

**Line 90**: Profile picture Base64 conversion call
- **Status**: ✅ **COVERED** - Tested via API when Google provides a picture URL that gets converted to Base64

**Lines 112-114**: Profile picture update condition branches
- **Status**: ✅ **NOW COVERED** - Tests added in `no-mocks/auth.test.ts`:
  - Line 112: `(!user.profilePicture || user.profilePicture === '')` - Both branches covered:
    - `!user.profilePicture` (undefined/null) - Test with `$unset { profilePicture: '' }`
    - `user.profilePicture === ''` (empty string) - Test with `profilePicture: ''`
  - Line 113: Update path - Covered when user has empty/undefined profile picture and Google provides one

**Line 115**: Profile picture keep existing branch
- **Status**: ✅ **NOW COVERED** - Test added in `no-mocks/auth.test.ts`:
  - **Branch**: `else if (user.profilePicture && user.profilePicture !== '')` 
  - **Test**: When user has existing custom profile picture and Google provides one, we keep the existing custom picture
  - This ensures the branch where we don't update the profile picture is covered

**Lines 71-72**: `verifyGoogleToken` catch block
- **Why it cannot be covered via API tests**: This catch block is covered by a **direct service test** (not API test)
- The test at `auth.test.ts` line 1755 calls `authService.verifyGoogleToken()` directly
- **Status**: Covered by direct service test, but not via API tests (<0.1% of codebase)

**Note**: Dead code methods (`verifyToken`, `updateFCMToken`, `deleteAccount`) have been removed from `authService.ts` as they were never called from controllers/middleware.

**Percentage of total codebase:** <1% (mostly testable, some defensive code)

---

### `groupService.ts` Uncovered Lines

**Line 15**: Group not found check in `getGroupStatus`
- **Status**: ✅ **NOW COVERED** - Test added to simulate race condition where group is deleted between `getGroupByUserId` and `getGroupStatus` calls
- The test uses a spy to simulate the race condition scenario

**Line 127**: `'Selected Restaurant'` fallback in `voteForRestaurant`
- **Status**: ✅ **NOW COVERED** - Test added in `with-mocks/group.mock.test.ts` to test the fallback when `group.restaurant?.name` is undefined
- The test votes without providing a restaurant object, triggering the fallback `'Selected Restaurant'` when emitting socket events and sending notifications

**Line 248**: Error catch block in `leaveGroup` (when restaurant auto-selected)
- **Status**: ✅ **NOW COVERED** - Test added in `with-mocks/group.mock.test.ts` to simulate `emitRestaurantSelected` throwing an error
- The test creates a 2-member group where both have voted, then one member leaves, triggering auto-selection and the catch block when `emitRestaurantSelected` throws

**Line 169**: Group not found check in `leaveGroup`
- **Why it cannot be covered**: This error path should be testable via API endpoint `POST /api/group/:groupId/leave`
- **Status**: Should be covered - can be tested via API

**Lines 282-302**: `closeGroup` method
- **Why it cannot be covered**: This method is only called internally by `checkExpiredGroups` (line 352)
- There is no API endpoint that calls this method
- It's used to disband groups when they expire without votes
- **Status**: Unreachable via API tests (only called by background task)

**Lines 307-366**: Background task `checkExpiredGroups`
- **Why it cannot be covered**: This is a background task that runs on a schedule (via `setInterval` in `server.ts` line 70)
- It checks for expired groups and automatically disbands them or selects winning restaurant based on votes
- **Cannot be tested via API because:**
  1. **No API endpoint calls it**: There is no route/endpoint that invokes `checkExpiredGroups()` - it's only called by `setInterval` in `server.ts`
  2. **Time-triggered, not request-triggered**: The function runs automatically every 2 minutes via `setInterval`, not in response to HTTP requests
  3. **Asynchronous background execution**: It runs independently of API requests, processing expired groups in the background
  4. **Testing would require**: 
     - Manipulating system time (not feasible in API tests)
     - Waiting for actual group expiration (not practical for test suite)
     - Directly calling the service method (violates API-only testing requirement)
- **Explanation**: This is infrastructure/background task code similar to `server.ts` entry point and process handlers
- **Status**: Unreachable via API tests (background task, <1% of codebase)

**Percentage of total codebase:** ~1-2% (background task code + testable error paths)

---

### `matchingService.ts` Uncovered Lines

**Lines 69-70**: Edge case in room matching algorithm
- **Status**: ✅ **NOW COVERED** - Test added in `no-mocks/matching.test.ts` to test the scenario where rooms exist but best match score < MINIMUM_MATCH_SCORE (30)
- The test creates a room with specific preferences, then tries to join with preferences that give a score < 30, triggering the "no good match found" path

**Lines 104-106**: Fallback branches in `joinMatching` matching preferences
- **Status**: ✅ **NOW COVERED** - Tests added in `no-mocks/matching.test.ts`:
  - Line 104: `user.preference` fallback when `preferences.cuisine` is undefined
  - Line 105: `user.budget ?? 50` fallback when `preferences.budget` is undefined and `user.budget` is null
  - Line 106: `user.radiusKm ?? 5` fallback when `preferences.radiusKm` is undefined and `user.radiusKm` is null

**Line 121**: Null fallback for cuisine when cuisines array is empty
- **Status**: ✅ **NOW COVERED** - Test added in `no-mocks/matching.test.ts` to test `matchingPreferences.cuisines[0] || null` when cuisines array is empty

**Lines 336-343**: `getRoomUsers` method
- **Status**: ✅ **NOW COVERED** - Tests added in `no-mocks/matching.test.ts`:
  - Success path (line 342) via GET `/api/matching/users/:roomId`
  - Error path (lines 338-339) when room not found

**Line 169**: Error handling in `leaveRoom`
- **Why it cannot be covered**: This is the catch block in `leaveRoom` method
- **Status**: ⚠️ **NOT COVERED** - Should be testable via API endpoint `POST /api/matching/leave/:roomId` by triggering an error
- **Can it be covered**: ✅ **YES** - Can be tested by mocking database operations to throw errors

**Lines 260-304**: `createGroupFromRoom` private method
- **Why it cannot be covered**: This is a private method called internally when a room becomes full (reaches MAX_MEMBERS)
- **Status**: ⚠️ **NOT COVERED** - Should be testable via API by creating a scenario where a room reaches maximum capacity
- **Can it be covered**: ✅ **YES** - Can be tested by joining enough users to fill a room (MAX_MEMBERS = 10)
- **Note**: This requires coordinating multiple users joining the same room, which may be complex but is testable

**Lines 349-384**: Background task `checkExpiredRooms`
- **Why it cannot be covered**: This is a background task that runs on a schedule (via `setInterval` in `server.ts` line 59)
- It checks for expired rooms and automatically expires/disbands them or creates groups
- Cannot be tested via API because:
  1. It's triggered by time, not API requests
  2. It runs asynchronously in the background via `setInterval`
  3. There is no API endpoint that calls this method
  4. Testing would require calling the service method directly (not an API test)
- **Explanation**: This is infrastructure/background task code similar to `server.ts` and `groupService.checkExpiredGroups`
- **Status**: Unreachable via API tests (background task, <0.5% of codebase)

**Percentage of total codebase:** ~1-2% (background task code + testable paths)

---

### `notificationService.ts` Uncovered Lines (Lines 12-28, 54-55, 66, 76, 87-97, 107-116)

**Why these lines cannot be covered:**
- These are error handling paths in the notification service
- Some represent Firebase initialization failures or network errors
- Some are catch blocks for notification sending failures
- **Note**: Many of these should be testable via API tests with proper mocking of Firebase/notification services
- However, some error paths (e.g., Firebase SDK initialization failures) are extremely difficult to trigger via API tests

**Status**: Mix of testable and difficult-to-test error paths

**Percentage of total codebase:** ~1-2% (error handling paths)

---

### `restaurantService.ts` Branch Coverage (Lines 34, 82, 119-133)

**Line 34**: Default parameter `radius: number = 5000`
- **Why it cannot be covered**: This is a default parameter that's always provided in API calls
- The branch where `radius` is undefined cannot be reached via API because Express validates request parameters
- **Status**: Unreachable defensive code (<0.1% of codebase)

**Line 82**: Error handling branch in `searchRestaurants`
- **Why it cannot be covered**: This is the catch block that returns mock data on error
- Should be testable by mocking axios to throw an error
- **Status**: Should be covered - can be tested via API with mocks

**Lines 119-133**: Error handling branch in `getRestaurantDetails`
- **Why it cannot be covered**: This is the catch block that returns mock data on error
- Should be testable by mocking axios to throw an error
- **Status**: Should be covered - can be tested via API with mocks

**Percentage of total codebase:** <0.5% (mostly testable with mocks)

---

### `userService.ts` Uncovered Lines

**Lines 149-151**: `contactNumber`, `budget`, `radiusKm` updates in `updateUserSettings`
- **Status**: ✅ **NOW COVERED** - Test added in `no-mocks/user.test.ts` to test updating these fields via POST /api/user/settings

**Lines 188-190**: `contactNumber`, `budget`, `radiusKm` updates in `updateUserProfile`
- **Status**: ✅ **NOW COVERED** - Test added in `no-mocks/user.test.ts` to test updating these fields via PUT /api/user/profile

**Line 101**: Direct `profilePicture` assignment in `createUserProfile`
- **Status**: ✅ **NOW COVERED** - Test added in `no-mocks/user.test.ts` to test direct profilePicture assignment via POST /api/user/profile
- Note: `createUserProfile` does NOT convert Google URLs to Base64 (unlike `updateUserSettings`/`updateUserProfile`)

**Line 12**: Early return in `convertGoogleProfilePictureToBase64`
- **Status**: ✅ **NOW COVERED** - Test added in `no-mocks/user.test.ts` to test early return for non-Google URLs

**Lines 22-29**: Successful Base64 conversion in `convertGoogleProfilePictureToBase64`
- **Status**: ✅ **NOW COVERED** - Tests added in `no-mocks/user.test.ts` to test:
  - Successful conversion path (lines 22-23, 26-29) via PUT /api/user/profile and POST /api/user/settings
  - Content-type fallback (line 24) when header is missing

**Lines 30-33**: Error handling in `convertGoogleProfilePictureToBase64`
- **Why it cannot be covered**: These are catch blocks for network/timeout errors when fetching profile picture
- Should be testable by mocking axios to throw errors
- **Status**: Should be covered - can be tested via API with mocks

**Lines 185-186**: Profile picture conversion in `updateProfile`
- **Why it cannot be covered**: This calls `convertGoogleProfilePictureToBase64` when profile picture is updated
- Should be testable via API endpoint `PUT /api/user/profile` with a Google profile picture URL
- **Status**: Should be covered - can be tested via API

**Percentage of total codebase:** <0.5% (testable via API)

---

### `socketManager.ts` Branch Coverage (Lines 18, 171)

**Line 18**: Singleton pattern check `if (!SocketManager.instance)`
- **Why it cannot be covered**: This is a singleton pattern that only creates one instance
- Once the instance is created, this branch is never executed again
- **Status**: Unreachable after first initialization (<0.1% of codebase)

**Line 171**: Socket iteration in `emitToUser`
- **Why it cannot be covered**: This iterates through all connected sockets to find a user's socket
- The branch where no socket is found for a user may not be covered if all tests have active socket connections
- **Status**: Could be covered with specific test setup (<0.1% of codebase)

**Percentage of total codebase:** <0.2% (singleton pattern + edge case)

---

## Summary

**Total Unreachable Code (Cannot be covered via API tests):**
- `server.ts`: ~2-3% (entry point, process handlers, background tasks)
- Background tasks (`checkExpiredRooms`, `checkExpiredGroups`): ~1-2% (scheduled tasks)
- Defensive checks (`requireParam`, `authMiddleware`, `errorHandler`): ~1% (unreachable defensive code)
- **Total: ~4-6% of codebase**

**Potentially Testable Code (Should be covered but may need additional tests):**
- Service method error paths: ~2-3%
- Error handling branches: ~1-2%
- **Total: ~3-5% of codebase**

**Current Coverage:** 87.25% statements, 90.47% branches, 82.24% functions, 86.84% lines

**Justification Summary for Uncovered Code:**

1. **Unreachable via API Tests (~4-6% of codebase):**
   - `server.ts` (0%): Entry point, process handlers, background tasks
   - Background tasks: `checkExpiredGroups` (lines 307-366), `checkExpiredRooms` (lines 349-384)
   - Defensive code: Singleton patterns, unreachable error paths
   - `optionalAuth` outer catch block (lines 111-113): Extremely rare error path

2. **Testable but Not Yet Covered (~3-5% of codebase):**
   - Error handling branches in services (restaurantService, matchingService)
   - Edge cases (authService profile picture branches, matchingService createGroupFromRoom)
   - Health check endpoint (app.ts line 26)
   - Some notification service error paths

**Target Coverage (excluding unreachable code):** ~95-96% is achievable with comprehensive API tests

---

## Code That Can Be Covered (To Be Tested)

All other uncovered code should be testable via API tests (with or without mocks) and will be covered.
