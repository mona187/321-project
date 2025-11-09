# Back-end Test Specification

## 2.1. Locations of Back-end Tests and Instructions to Run

### 2.1.1. API Test Coverage Table

| Interface | Describe Group Location, No Mocks | Describe Group Location, With Mocks | Mocked Components |
|-----------|-----------------------------------|-----------------------------------|-------------------|
| POST /api/auth/signup | `backend/tests/no-mocks/auth.test.ts#L72` | N/A | N/A (No mocks - tests validation and business logic) |
| POST /api/auth/signin | `backend/tests/no-mocks/auth.test.ts#L196` | N/A | N/A (No mocks - tests validation and business logic) |
| POST /api/auth/google | `backend/tests/no-mocks/auth.test.ts#L237` | N/A | N/A (No mocks - tests validation) |
| POST /api/auth/logout | `backend/tests/no-mocks/auth.test.ts#L464` | `backend/tests/with-mocks/auth.mock.test.ts#L48` | User Model (findById, save), Database operations |
| GET /api/auth/verify | `backend/tests/no-mocks/auth.test.ts#L724` | `backend/tests/with-mocks/auth.mock.test.ts#L130` | User Model (findById), Database operations |
| POST /api/auth/fcm-token | `backend/tests/no-mocks/auth.test.ts#L801` | `backend/tests/with-mocks/auth.mock.test.ts#L189` | User Model (findById, save), Database operations |
| DELETE /api/auth/account | `backend/tests/no-mocks/auth.test.ts#L879` | `backend/tests/with-mocks/auth.mock.test.ts#L275` | User Model (findById, findByIdAndDelete), Database operations |
| GET /api/user/profile/:ids | `backend/tests/no-mocks/user.test.ts#L41` | `backend/tests/with-mocks/user.mock.test.ts#L25` | User Model (find), Database queries, Axios (for profile picture conversion) |
| GET /api/user/settings | `backend/tests/no-mocks/user.test.ts#L147` | `backend/tests/with-mocks/user.mock.test.ts#L133` | User Model (findById, save), Database operations |
| POST /api/user/profile | `backend/tests/no-mocks/user.test.ts#L260` | `backend/tests/with-mocks/user.mock.test.ts#L227` | User Model (findById, save), Database write operations |
| POST /api/user/settings | `backend/tests/no-mocks/user.test.ts#L347` | `backend/tests/with-mocks/user.mock.test.ts#L339` | User Model (findById, save), Database operations |
| PUT /api/user/profile | `backend/tests/no-mocks/user.test.ts#L410` | `backend/tests/with-mocks/user.mock.test.ts#L453` | User Model (findById, save), Database operations |
| DELETE /api/user/:userId | `backend/tests/no-mocks/user.test.ts#L467` | `backend/tests/with-mocks/user.mock.test.ts#L558` | User Model (findById, findByIdAndDelete), Database operations |
| POST /api/matching/join | `backend/tests/no-mocks/matching.test.ts#L109` | `backend/tests/with-mocks/matching.mock.test.ts#L80` | Room Model, User Model, Group Model, SocketManager, NotificationService |
| POST /api/matching/join/:roomId | `backend/tests/no-mocks/matching.test.ts#L294` | N/A | N/A (Endpoint returns 501 - Not Implemented) |
| PUT /api/matching/leave/:roomId | `backend/tests/no-mocks/matching.test.ts#L330` | `backend/tests/with-mocks/matching.mock.test.ts#L386` | Room Model, User Model, SocketManager, NotificationService |
| GET /api/matching/status/:roomId | `backend/tests/no-mocks/matching.test.ts#L453` | `backend/tests/with-mocks/matching.mock.test.ts#L624` | Room Model (findById), Database queries |
| GET /api/matching/users/:roomId | `backend/tests/no-mocks/matching.test.ts#L534` | `backend/tests/with-mocks/matching.mock.test.ts#L735` | Room Model (findById), Database queries |
| GET /api/group/status | `backend/tests/no-mocks/group.test.ts#L141` | `backend/tests/with-mocks/group.mock.test.ts#L86` | User Model (findById), Group Model (findById), SocketManager, NotificationService |
| POST /api/group/vote/:groupId | `backend/tests/no-mocks/group.test.ts#L230` | `backend/tests/with-mocks/group.mock.test.ts#L173` | Group Model (findById, save), SocketManager, NotificationService |
| POST /api/group/leave/:groupId | `backend/tests/no-mocks/group.test.ts#L493` | `backend/tests/with-mocks/group.mock.test.ts#L405` | Group Model (findById, findByIdAndDelete), User Model (findById, save), SocketManager, NotificationService |
| GET /api/restaurant/search | `backend/tests/no-mocks/restaurant.test.ts#L62` | `backend/tests/with-mocks/restaurant.mock.test.ts#L74` | Axios (Google Places API), External HTTP requests |
| GET /api/restaurant/:restaurantId | `backend/tests/no-mocks/restaurant.test.ts#L197` | `backend/tests/with-mocks/restaurant.mock.test.ts#L396` | Axios (Google Places API), External HTTP requests |
| POST /api/restaurant/recommendations/:groupId | `backend/tests/no-mocks/restaurant.test.ts#L259` | `backend/tests/with-mocks/restaurant.mock.test.ts#L519` | Axios (Google Places API), External HTTP requests, Group Model |

### 2.1.2. Commit Hash

The tests are designed to run on the latest commit in the `main` branch. To get the current commit hash:

```bash
git rev-parse HEAD
```

**Note:** The reviewer should use the commit hash from the `main` branch at the time of review.

### 2.1.3. Instructions to Run Tests

#### Prerequisites

1. **Node.js**: Version 18 or higher
2. **MongoDB**: A running MongoDB instance (local or remote)
3. **Environment Variables**: Create a `.env.test` file in the `backend/` directory with:
   ```
   MONGODB_URI=mongodb://localhost:27017/feastfriends_test
   JWT_SECRET=test-jwt-secret-key
   NODE_ENV=test
   GOOGLE_PLACES_API_KEY=test-key (optional for tests)
   ```

#### Running Tests

Navigate to the `backend/` directory:

```bash
cd backend
```

**Run all tests (with and without mocks):**
```bash
npm test
```

**Run tests without mocks only:**
```bash
npm run test:no-mocks
```

**Run tests with mocks only:**
```bash
npm run test:with-mocks
```

**Run tests with coverage (all tests):**
```bash
npm run test:coverage
```

**Run tests with coverage (no mocks only):**
```bash
npm run test:coverage:no-mocks
```

**Run tests with coverage (with mocks only):**
```bash
npm run test:coverage:with-mocks
```

**Run tests in watch mode:**
```bash
npm run test:watch
```

**Run tests with verbose output:**
```bash
npm run test:verbose
```

#### Test Structure

- **No Mocks Tests**: Located in `backend/tests/no-mocks/`
  - Test controllable scenarios: validation, business logic, real database operations
  - Use real MongoDB connection
  - Test actual service integrations

- **With Mocks Tests**: Located in `backend/tests/with-mocks/`
  - Test uncontrollable failures: database errors, network timeouts, external API failures
  - Mock external dependencies (database models, HTTP clients, services)
  - Focus on error handling and edge cases

#### Important Notes

1. **Database**: Tests use a separate test database. Ensure MongoDB is running before executing tests.
2. **Test Isolation**: Tests run sequentially (`maxWorkers: 1` in jest.config.ts) to prevent database conflicts.
3. **Cleanup**: Tests automatically clean up test data after execution.
4. **Timeout**: Tests have a 30-second timeout (configured in `tests/setup.ts`).

## 2.2. GitHub Actions Workflow for Back-end Tests

The back-end tests are run via GitHub Actions. The workflow file is located at:

**`.github/workflows/test.yml`**

### Workflow Configuration

The workflow:
1. Checks out the repository
2. Sets up Node.js 20
3. Starts a MongoDB service container
4. Installs dependencies (`npm ci`)
5. Runs tests without mocks (`npm run test:no-mocks`)
6. Runs tests with mocks (`npm run test:with-mocks`)
7. Generates combined coverage report (`npm run test:coverage`)
8. Uploads coverage reports as artifacts

The workflow triggers on:
- Push to `main` or `develop` branches (when backend files change)
- Pull requests to `main` or `develop` branches (when backend files change)

## 2.3. Coverage Reports - No Mocking

To generate coverage reports for tests without mocking:

```bash
cd backend
npm run test:coverage:no-mocks
```

The coverage report will be generated in:
- **HTML**: `backend/coverage/index.html`
- **LCOV**: `backend/coverage/lcov.info`
- **Text**: Displayed in terminal

**Expected Coverage:**
- High coverage (80%+) for most files
- Less than 100% due to missing error cases (which are tested in with-mocks tests)
- Focus on happy paths and business logic

## 2.4. Coverage Reports - With Mocking

To generate coverage reports for tests with mocking:

```bash
cd backend
npm run test:coverage:with-mocks
```

The coverage report will be generated in the same location as above.

**Expected Coverage:**
- Lower coverage than no-mocks tests
- Focus on error handling paths
- Tests edge cases and failure scenarios
- Completes coverage gaps from no-mocks tests

## 2.5. Coverage Reports - Combined (No Mocks + With Mocks)

To generate combined coverage reports:

```bash
cd backend
npm run test:coverage
```

This runs both test suites and generates a combined coverage report.

**Expected Coverage:**
- **High coverage (90%+)** for most files
- **Less than 100% coverage** is expected due to:
  1. **Error Handler Edge Cases**: Some extremely rare error combinations may not be fully covered
  2. **External Service Integration**: Some Google OAuth token verification paths require actual Google API responses
  3. **Socket.IO Real-time Events**: Some socket event handlers are difficult to test in isolation
  4. **Background Tasks**: Some scheduled background tasks (expired rooms/groups) are tested but may have timing edge cases
  5. **Firebase Initialization**: Firebase Admin SDK initialization failures are tested but some edge cases may remain
  6. **Type Definitions**: Type definition files (`.d.ts`) are excluded from coverage
  7. **Unused Code Paths**: Some defensive code paths that are extremely unlikely to execute in production

### Files with Lower Coverage (and reasons):

- `src/config/firebase.ts`: Some Firebase initialization edge cases
- `src/utils/socketManager.ts`: Some socket event handlers require actual socket connections
- `src/services/credibilityService.ts`: Not fully implemented (excluded from coverage)
- `src/models/CredibilityLog.ts`: Not fully implemented (excluded from coverage)

## Additional Notes

- All tests use Jest as the testing framework
- Tests are written in TypeScript
- Supertest is used for HTTP endpoint testing
- Test helpers are located in `backend/tests/helpers/`
- Test setup and configuration is in `backend/tests/setup.ts`
- Jest configuration is in `backend/jest.config.ts`

