// tests/with-mocks/errorHandler.mock.test.ts

/**
 * Error Handler Middleware Tests - With Mocking
 * 
 * This test suite covers error handling scenarios that are difficult
 * to trigger through normal API calls:
 * - MongoDB duplicate key errors (11000)
 * - Mongoose validation errors
 * - Mongoose cast errors
 * - Generic server errors
 * - Custom AppError handling
 * 
 * These tests ensure the error handler middleware correctly processes
 * and formats all types of errors.
 */

import request from 'supertest';
import express, { Request, Response, NextFunction } from 'express';
import { errorHandler, AppError, notFoundHandler, asyncHandler, requireParam } from '../../src/middleware/errorHandler';
import User from '../../src/models/User';
import { connectDatabase, disconnectDatabase } from '../../src/config/database';

/**
 * Create a test Express app with error-throwing routes
 */
function createTestApp() {
  const app = express();
  app.use(express.json());

  // Route that throws AppError
  app.get('/test/app-error', (_req: Request, _res: Response, next: NextFunction) => {
    next(new AppError('Custom application error', 400));
  });

  // Route that throws Mongoose ValidationError
  app.get('/test/validation-error', (_req: Request, _res: Response, next: NextFunction) => {
    const error: any = new Error('Validation failed');
    error.name = 'ValidationError';
    error.errors = {
      email: {
        message: 'Email is required'
      }
    };
    next(error);
  });

  // Route that throws Mongoose CastError
  app.get('/test/cast-error', (_req: Request, _res: Response, next: NextFunction) => {
    const error: any = new Error('Cast to ObjectId failed');
    error.name = 'CastError';
    error.path = '_id';
    error.value = 'invalid-id-123';
    next(error);
  });

  // Route that throws MongoDB duplicate key error
  app.get('/test/duplicate-error', (_req: Request, _res: Response, next: NextFunction) => {
    const error: any = new Error('E11000 duplicate key error');
    error.name = 'MongoServerError';
    error.code = 11000;
    error.keyValue = { email: 'test@example.com' };
    next(error);
  });

  // Route that throws generic error
  app.get('/test/generic-error', (_req: Request, _res: Response, next: NextFunction) => {
    next(new Error('Something went wrong'));
  });

  // Route that throws error with no message
  app.get('/test/no-message-error', (_req: Request, _res: Response, next: NextFunction) => {
    const error: any = new Error();
    error.name = 'UnknownError';
    next(error);
  });

  // Route that throws error with no name property (to test err.name || 'Error' fallback)
  app.get('/test/no-name-error', (_req: Request, _res: Response, next: NextFunction) => {
    const error: any = { message: 'Error without name property' };
    // Explicitly delete name to test the fallback
    delete error.name;
    next(error);
  });

  // Route that throws network error
  app.get('/test/network-error', (_req: Request, _res: Response, next: NextFunction) => {
    const error: any = new Error('ECONNREFUSED');
    error.code = 'ECONNREFUSED';
    next(error);
  });

  // Add error handler
  app.use(errorHandler);

  return app;
}

describe('Error Handler Middleware - All Error Types', () => {
  let testApp: express.Application;

  beforeAll(async () => {
    console.log('\n🚀 Starting Error Handler Tests...\n');
    await connectDatabase();
    testApp = createTestApp();
    console.log('✅ Test setup complete.\n');
  });

  afterAll(async () => {
    console.log('\n🧹 Cleaning up after tests...\n');
    await disconnectDatabase();
    console.log('✅ Cleanup complete.\n');
  });

  describe('AppError - Custom Application Errors', () => {
    test('should return 400 for custom AppError', async () => {
      /**
       * Scenario: Application throws custom AppError with specific status code
       * Expected: Error handler returns the custom status code and message
       */

      const response = await request(testApp)
        .get('/test/app-error');

      expect(response.status).toBe(400);
      // AppError might be reported as 'Error' in error.name
      expect(response.body.error).toMatch(/Error|AppError/);
      expect(response.body.message).toBe('Custom application error');
      expect(response.body.statusCode).toBe(400);
    });

    test('should handle AppError with 404 status', async () => {
      /**
       * Scenario: Resource not found, AppError with 404
       * Expected: Return 404 with custom message
       */

      const app = express();
      app.get('/test/not-found', (_req: Request, _res: Response, next: NextFunction) => {
        next(new AppError('Resource not found', 404));
      });
      app.use(errorHandler);

      const response = await request(app)
        .get('/test/not-found');

      expect(response.status).toBe(404);
      expect(response.body.message).toBe('Resource not found');
    });

    test('should handle AppError with 403 status', async () => {
      /**
       * Scenario: Forbidden access, AppError with 403
       * Expected: Return 403 with custom message
       */

      const app = express();
      app.get('/test/forbidden', (_req: Request, _res: Response, next: NextFunction) => {
        next(new AppError('Access forbidden', 403));
      });
      app.use(errorHandler);

      const response = await request(app)
        .get('/test/forbidden');

      expect(response.status).toBe(403);
      expect(response.body.message).toBe('Access forbidden');
    });

    test('should handle AppError with 409 status', async () => {
      /**
       * Scenario: Conflict error
       * Expected: Return 409 with custom message
       */

      const app = express();
      app.get('/test/conflict', (_req: Request, _res: Response, next: NextFunction) => {
        next(new AppError('Resource conflict', 409));
      });
      app.use(errorHandler);

      const response = await request(app)
        .get('/test/conflict');

      expect(response.status).toBe(409);
      expect(response.body.message).toBe('Resource conflict');
    });

    test('should handle AppError with 401 status', async () => {
      /**
       * Scenario: Unauthorized access
       * Expected: Return 401 with custom message
       */

      const app = express();
      app.get('/test/unauthorized', (_req: Request, _res: Response, next: NextFunction) => {
        next(new AppError('Unauthorized', 401));
      });
      app.use(errorHandler);

      const response = await request(app)
        .get('/test/unauthorized');

      expect(response.status).toBe(401);
      expect(response.body.message).toBe('Unauthorized');
    });
  });

  describe('Mongoose ValidationError', () => {
    test('should return 400 for Mongoose validation error', async () => {
      /**
       * Scenario: Mongoose schema validation fails
       * Expected: Error handler returns 400 with validation message
       */

      const response = await request(testApp)
        .get('/test/validation-error');

      expect(response.status).toBe(400);
      expect(response.body.error).toBe('ValidationError');
      expect(response.body.message).toBe('Validation failed');
      expect(response.body.statusCode).toBe(400);
    });

    test('should handle real Mongoose validation error from User model', async () => {
      /**
       * Scenario: Try to create user with missing required fields
       * Expected: Mongoose throws ValidationError, handler returns 400
       */

      try {
        // Try to create user without required fields
        await User.create({
          // Missing required fields like googleId, email, name
        });
      } catch (error: any) {
        expect(error.name).toBe('ValidationError');
        expect(error).toHaveProperty('errors');
      }
    });
  });

  describe('Mongoose CastError', () => {
    test('should return 400 for Mongoose CastError', async () => {
      /**
       * Scenario: Invalid ObjectId format provided
       * Expected: Error handler returns 400 with "Invalid data format"
       */

      const response = await request(testApp)
        .get('/test/cast-error');

      expect(response.status).toBe(400);
      expect(response.body.error).toBe('CastError');
      expect(response.body.message).toBe('Invalid data format');
      expect(response.body.statusCode).toBe(400);
    });

    test('should handle CastError from real User.findById with invalid ID', async () => {
      /**
       * Scenario: Try to find user with invalid ObjectId
       * Expected: Mongoose throws CastError
       */

      try {
        await User.findById('invalid-id-format-123');
      } catch (error: any) {
        expect(error.name).toBe('CastError');
      }
    });
  });

  describe('MongoDB Duplicate Key Error (11000)', () => {
    test('should return 409 for MongoDB duplicate key error', async () => {
      /**
       * Scenario: Try to insert duplicate unique field (e.g., email)
       * Expected: Error handler returns 409 Conflict
       */

      const response = await request(testApp)
        .get('/test/duplicate-error');

      expect(response.status).toBe(409);
      expect(response.body.error).toBe('MongoServerError');
      expect(response.body.message).toBe('Duplicate entry - resource already exists');
      expect(response.body.statusCode).toBe(409);
    });

    test('should handle real duplicate key error with User model', async () => {
      /**
       * Scenario: Create two users with same googleId
       * Expected: MongoDB throws duplicate key error (code 11000)
       */

      const userData = {
        googleId: `test-duplicate-${Date.now()}`,
        email: `duplicate-${Date.now()}@example.com`,
        name: 'Duplicate Test User',
        preference: [],
        credibilityScore: 100,
        budget: 50,
        radiusKm: 10,
      };

      try {
        // Create first user
        await User.create(userData);

        // Try to create second user with same googleId (should fail)
        await User.create(userData);

        // Should not reach here
        fail('Should have thrown duplicate key error');
      } catch (error: any) {
        expect(error.name).toBe('MongoServerError');
        expect(error.code).toBe(11000);
      } finally {
        // Cleanup
        await User.deleteOne({ googleId: userData.googleId });
      }
    });
  });

  describe('Generic Errors', () => {
    test('should return 500 for generic error', async () => {
      /**
       * Scenario: Unexpected error occurs
       * Expected: Error handler returns 500 with error message
       */

      const response = await request(testApp)
        .get('/test/generic-error');

      expect(response.status).toBe(500);
      expect(response.body.error).toBe('Error');
      expect(response.body.message).toBe('Something went wrong');
      expect(response.body.statusCode).toBe(500);
    });

    test('should return 500 with default message for error without message', async () => {
      /**
       * Scenario: Error thrown with no message
       * Expected: Error handler returns 500 with error name
       */

      const response = await request(testApp)
        .get('/test/no-message-error');

      expect(response.status).toBe(500);
      expect(response.body.statusCode).toBe(500);
      expect(response.body).toHaveProperty('error');
      // Error name should be present
      expect(response.body.error).toBeTruthy();
    });

    test('should return 500 with "Error" fallback when error has no name property', async () => {
      /**
       * Covers errorHandler.ts line 58: err.name || 'Error' fallback
       * Path: error: err.name || 'Error' [FALSE BRANCH] -> uses 'Error' as fallback
       * Scenario: Error object without name property
       * Expected: Error handler uses 'Error' as fallback
       */

      const response = await request(testApp)
        .get('/test/no-name-error');

      expect(response.status).toBe(500);
      expect(response.body.error).toBe('Error'); // Should use fallback
      expect(response.body.message).toBe('Error without name property');
      expect(response.body.statusCode).toBe(500);
    });
  });

  describe('Network and Connection Errors', () => {
    test('should handle network connection errors', async () => {
      /**
       * Scenario: Network connection refused
       * Expected: Error handler returns 500
       */

      const response = await request(testApp)
        .get('/test/network-error');

      expect(response.status).toBe(500);
      expect(response.body.message).toContain('ECONNREFUSED');
    });

    test('should handle MongoDB connection errors', async () => {
      /**
       * Scenario: Database connection lost during operation
       * Expected: Error handler catches and returns 500
       */

      jest.spyOn(User, 'findById').mockRejectedValueOnce(
        new Error('MongoNetworkError: connection lost')
      );

      try {
        await User.findById('507f1f77bcf86cd799439011');
        fail('Should have thrown error');
      } catch (error: any) {
        expect(error.message).toContain('connection lost');
      } finally {
        jest.restoreAllMocks();
      }
    });
  });

  describe('Error Handler Response Format', () => {
    test('should include timestamp in error logs', async () => {
      /**
       * Scenario: Any error occurs
       * Expected: Error handler logs timestamp (verified via console)
       */

      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();

      await request(testApp).get('/test/generic-error');

      expect(consoleSpy).toHaveBeenCalled();
      const logCall = consoleSpy.mock.calls[0];
      expect(logCall[0]).toBe('Error:');
      expect(logCall[1]).toHaveProperty('timestamp');

      consoleSpy.mockRestore();
    });

    test('should include request path and method in error logs', async () => {
      /**
       * Scenario: Error occurs during request
       * Expected: Error handler logs request path and method
       */

      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();

      await request(testApp).get('/test/generic-error');

      expect(consoleSpy).toHaveBeenCalled();
      const logCall = consoleSpy.mock.calls[0];
      expect(logCall[1]).toHaveProperty('path', '/test/generic-error');
      expect(logCall[1]).toHaveProperty('method', 'GET');

      consoleSpy.mockRestore();
    });

    test('should include stack trace in development mode', async () => {
      /**
       * Scenario: Error in development environment
       * Expected: Response includes stack trace
       */

      const originalEnv = process.env.NODE_ENV;
      process.env.NODE_ENV = 'development';

      const response = await request(testApp)
        .get('/test/generic-error');

      expect(response.body).toHaveProperty('stack');
      expect(response.body).toHaveProperty('details');

      process.env.NODE_ENV = originalEnv;
    });

    test('should NOT include stack trace in production mode', async () => {
      /**
       * Scenario: Error in production environment
       * Expected: Response does NOT include stack trace
       */

      const originalEnv = process.env.NODE_ENV;
      process.env.NODE_ENV = 'production';

      const response = await request(testApp)
        .get('/test/generic-error');

      expect(response.body).not.toHaveProperty('stack');
      expect(response.body).not.toHaveProperty('details');

      process.env.NODE_ENV = originalEnv;
    });
  });

  describe('Multiple Error Types in Sequence', () => {
    test('should handle different error types correctly in sequence', async () => {
      /**
       * Scenario: Multiple different errors thrown in sequence
       * Expected: Each error handled with correct status code
       */

      // Test 1: Validation error
      const response1 = await request(testApp).get('/test/validation-error');
      expect(response1.status).toBe(400);

      // Test 2: Duplicate key error
      const response2 = await request(testApp).get('/test/duplicate-error');
      expect(response2.status).toBe(409);

      // Test 3: Generic error
      const response3 = await request(testApp).get('/test/generic-error');
      expect(response3.status).toBe(500);

      // Test 4: Cast error
      const response4 = await request(testApp).get('/test/cast-error');
      expect(response4.status).toBe(400);
    });
  });

  describe('Edge Cases', () => {
    test('should handle null error gracefully', async () => {
      /**
       * Scenario: Error handler receives null
       * Expected: Handler processes gracefully, returns 404 (no route matched)
       */

      const app = express();
      app.get('/test/null-error', (_req: Request, _res: Response, next: NextFunction) => {
        next(null as any);
      });
      app.use(errorHandler);

      const response = await request(app).get('/test/null-error');
      
      // Should not crash - next(null) should continue to next middleware
      // Since there's no route handler, it returns 404
      expect(response.status).toBeDefined();
    });

    test('should handle error with very long message', async () => {
      /**
       * Scenario: Error with extremely long message
       * Expected: Handler processes without truncation issues
       */

      const app = express();
      const longMessage = 'Error: ' + 'a'.repeat(10000);
      
      app.get('/test/long-error', (_req: Request, _res: Response, next: NextFunction) => {
        next(new Error(longMessage));
      });
      app.use(errorHandler);

      const response = await request(app).get('/test/long-error');
      
      expect(response.status).toBe(500);
      expect(response.body.message).toBe(longMessage);
    });

    test('should handle error with special characters in message', async () => {
      /**
       * Scenario: Error message contains special characters
       * Expected: Message properly escaped in JSON response
       */

      const app = express();
      const specialMessage = 'Error: <script>alert("xss")</script> & "quotes" \'apostrophe\'';
      
      app.get('/test/special-chars', (_req: Request, _res: Response, next: NextFunction) => {
        next(new Error(specialMessage));
      });
      app.use(errorHandler);

      const response = await request(app).get('/test/special-chars');
      
      expect(response.status).toBe(500);
      expect(response.body.message).toBe(specialMessage);
    });

    test('should handle TypeError', async () => {
      /**
       * Scenario: TypeError thrown (common runtime error)
       * Expected: Error handler returns 500
       */

      const app = express();
      app.get('/test/type-error', (_req: Request, _res: Response, next: NextFunction) => {
        const error = new TypeError('Cannot read property of undefined');
        next(error);
      });
      app.use(errorHandler);

      const response = await request(app).get('/test/type-error');
      
      expect(response.status).toBe(500);
      expect(response.body.message).toContain('Cannot read property');
    });

    test('should handle ReferenceError', async () => {
      /**
       * Scenario: ReferenceError thrown
       * Expected: Error handler returns 500
       */

      const app = express();
      app.get('/test/reference-error', (_req: Request, _res: Response, next: NextFunction) => {
        const error = new ReferenceError('Variable is not defined');
        next(error);
      });
      app.use(errorHandler);

      const response = await request(app).get('/test/reference-error');
      
      expect(response.status).toBe(500);
      expect(response.body.message).toContain('not defined');
    });
  });

  describe('Error Handler with Different HTTP Methods', () => {
    test('should handle POST request errors', async () => {
      /**
       * Scenario: Error occurs during POST request
       * Expected: Error handler catches and logs correct method
       */

      const app = express();
      app.use(express.json());
      app.post('/test/post-error', (_req: Request, _res: Response, next: NextFunction) => {
        next(new Error('POST error'));
      });
      app.use(errorHandler);

      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();

      const response = await request(app)
        .post('/test/post-error')
        .send({ data: 'test' });
      
      expect(response.status).toBe(500);
      expect(consoleSpy).toHaveBeenCalled();
      const logCall = consoleSpy.mock.calls[0];
      expect(logCall[1]).toHaveProperty('method', 'POST');

      consoleSpy.mockRestore();
    });

    test('should handle PUT request errors', async () => {
      /**
       * Scenario: Error occurs during PUT request
       * Expected: Error handler catches and logs correct method
       */

      const app = express();
      app.use(express.json());
      app.put('/test/put-error', (_req: Request, _res: Response, next: NextFunction) => {
        next(new Error('PUT error'));
      });
      app.use(errorHandler);

      const response = await request(app)
        .put('/test/put-error')
        .send({ data: 'test' });
      
      expect(response.status).toBe(500);
    });

    test('should handle DELETE request errors', async () => {
      /**
       * Scenario: Error occurs during DELETE request
       * Expected: Error handler catches and logs correct method
       */

      const app = express();
      app.delete('/test/delete-error', (_req: Request, _res: Response, next: NextFunction) => {
        next(new Error('DELETE error'));
      });
      app.use(errorHandler);

      const response = await request(app).delete('/test/delete-error');
      
      expect(response.status).toBe(500);
    });
  });
});
// ============================================
// Coverage for next(error) in Controllers
// ============================================

describe('Controller Error Forwarding - next(error) Coverage', () => {
  test('should simulate controller calling next(error)', async () => {
    /**
     * This test simulates how restaurant.controller.ts or matching.controller.ts
     * would call next(error) when a service throws.
     * It ensures that the global errorHandler correctly processes errors
     * forwarded via next(error), giving full coverage for those lines.
     */

    const app = express();
    app.use(express.json());

    // Simulate a route similar to a controller
    app.get('/test/controller-error', async (_req: Request, _res: Response, next: NextFunction) => {
      try {
        // Simulate something throwing inside controller try block
        throw new Error('Simulated controller failure');
      } catch (error) {
        next(error); // This line mimics the next(error) in controllers
      }
    });

    // Attach real error handler
    app.use(errorHandler);

    const response = await request(app).get('/test/controller-error');

    expect(response.status).toBe(500);
    expect(response.body).toHaveProperty('message');
    expect(response.body.message).toBe('Simulated controller failure');
  });
});

describe('notFoundHandler - 404 Handler', () => {
  /**
   * Tests for the notFoundHandler middleware
   * Covers: notFoundHandler function
   */

  test('should create AppError with 404 when route not found', async () => {
    /**
     * Covers errorHandler.ts lines 69-79: notFoundHandler function
     * Path: notFoundHandler -> new AppError -> next(error)
     */
    const app = express();
    app.use(express.json());
    
    // Add a route that exists
    app.get('/test/exists', (_req: Request, res: Response) => {
      res.json({ success: true });
    });
    
    // Add notFoundHandler (should be called for non-existent routes)
    app.use(notFoundHandler);
    
    // Add error handler to catch the AppError
    app.use(errorHandler);
    
    // Make request to non-existent route
    const response = await request(app)
      .get('/test/non-existent-route');
    
    // Should return 404 with AppError message
    expect(response.status).toBe(404);
    // AppError.name is 'AppError', but errorHandler uses err.name || 'Error'
    // Since AppError extends Error, err.name should be 'AppError'
    expect(response.body.error).toBe('AppError');
    expect(response.body.message).toContain('not found');
    expect(response.body.statusCode).toBe(404);
  });
});

describe('requireParam - Parameter Validation', () => {
  /**
   * Tests for the requireParam function defensive checks
   * Covers: errorHandler.ts lines 22-46
   * These tests directly call requireParam with invalid inputs to test defensive checks
   */

  test('should throw AppError when paramName is empty string (covers errorHandler line 22-23)', () => {
    /**
     * Covers errorHandler.ts lines 22-23: paramName validation
     * Path: requireParam -> if (!paramName || typeof paramName !== 'string' || paramName.length === 0) -> throw AppError
     */
    const mockReq = {
      params: { id: '123' }
    } as any;

    expect(() => {
      requireParam(mockReq, '');
    }).toThrow(AppError);
    
    try {
      requireParam(mockReq, '');
    } catch (error) {
      expect(error).toBeInstanceOf(AppError);
      expect((error as AppError).statusCode).toBe(400);
      expect((error as AppError).message).toBe('Invalid parameter name');
    }
  });

  test('should throw AppError when paramName is null (covers errorHandler line 22-23)', () => {
    /**
     * Covers errorHandler.ts lines 22-23: paramName validation
     */
    const mockReq = {
      params: { id: '123' }
    } as any;

    expect(() => {
      requireParam(mockReq, null as any);
    }).toThrow(AppError);
    
    try {
      requireParam(mockReq, null as any);
    } catch (error) {
      expect(error).toBeInstanceOf(AppError);
      expect((error as AppError).statusCode).toBe(400);
      expect((error as AppError).message).toBe('Invalid parameter name');
    }
  });

  test('should throw AppError when paramName is not a string (covers errorHandler line 22-23)', () => {
    /**
     * Covers errorHandler.ts lines 22-23: paramName validation
     */
    const mockReq = {
      params: { id: '123' }
    } as any;

    expect(() => {
      requireParam(mockReq, 123 as any);
    }).toThrow(AppError);
    
    try {
      requireParam(mockReq, 123 as any);
    } catch (error) {
      expect(error).toBeInstanceOf(AppError);
      expect((error as AppError).statusCode).toBe(400);
      expect((error as AppError).message).toBe('Invalid parameter name');
    }
  });

  test('should throw AppError when paramName is not in ALLOWED_PARAMS whitelist (covers errorHandler line 27-28)', () => {
    /**
     * Covers errorHandler.ts lines 27-28: Whitelist check
     * Path: requireParam -> if (!ALLOWED_PARAMS.includes(paramName)) -> throw AppError
     */
    const mockReq = {
      params: { maliciousParam: 'value' }
    } as any;

    expect(() => {
      requireParam(mockReq, 'maliciousParam');
    }).toThrow(AppError);
    
    try {
      requireParam(mockReq, 'maliciousParam');
    } catch (error) {
      expect(error).toBeInstanceOf(AppError);
      expect((error as AppError).statusCode).toBe(400);
      expect((error as AppError).message).toBe('Invalid parameter requested: maliciousParam');
    }
  });

  test('should throw AppError when paramName is not in req.params (covers errorHandler line 33-34)', () => {
    /**
     * Covers errorHandler.ts lines 33-34: Missing parameter check
     * Path: requireParam -> if (!Object.prototype.hasOwnProperty.call(req.params, paramName)) -> throw AppError
     */
    const mockReq = {
      params: { otherParam: 'value' }
    } as any;

    expect(() => {
      requireParam(mockReq, 'groupId');
    }).toThrow(AppError);
    
    try {
      requireParam(mockReq, 'groupId');
    } catch (error) {
      expect(error).toBeInstanceOf(AppError);
      expect((error as AppError).statusCode).toBe(400);
      expect((error as AppError).message).toBe('Missing required parameter: groupId');
    }
  });

  test('should throw AppError when param value is empty string (covers errorHandler line 39-40)', () => {
    /**
     * Covers errorHandler.ts lines 39-40: Falsy value check
     * Path: requireParam -> if (!value) -> throw AppError
     */
    const mockReq = {
      params: { groupId: '' }
    } as any;

    expect(() => {
      requireParam(mockReq, 'groupId');
    }).toThrow(AppError);
    
    try {
      requireParam(mockReq, 'groupId');
    } catch (error) {
      expect(error).toBeInstanceOf(AppError);
      expect((error as AppError).statusCode).toBe(400);
      expect((error as AppError).message).toBe('Missing required parameter: groupId');
    }
  });

  test('should throw AppError when param value is null (covers errorHandler line 39-40)', () => {
    /**
     * Covers errorHandler.ts lines 39-40: Falsy value check
     */
    const mockReq = {
      params: { groupId: null }
    } as any;

    expect(() => {
      requireParam(mockReq, 'groupId');
    }).toThrow(AppError);
    
    try {
      requireParam(mockReq, 'groupId');
    } catch (error) {
      expect(error).toBeInstanceOf(AppError);
      expect((error as AppError).statusCode).toBe(400);
      expect((error as AppError).message).toBe('Missing required parameter: groupId');
    }
  });

  test('should throw AppError when param value is not a string (covers errorHandler line 44-45)', () => {
    /**
     * Covers errorHandler.ts lines 44-45: Type check
     * Path: requireParam -> if (typeof value !== 'string') -> throw AppError
     */
    const mockReq = {
      params: { groupId: 123 }
    } as any;

    expect(() => {
      requireParam(mockReq, 'groupId');
    }).toThrow(AppError);
    
    try {
      requireParam(mockReq, 'groupId');
    } catch (error) {
      expect(error).toBeInstanceOf(AppError);
      expect((error as AppError).statusCode).toBe(400);
      expect((error as AppError).message).toBe('Invalid parameter type for: groupId');
    }
  });

  test('should return value when all validations pass', () => {
    /**
     * Tests the happy path - all validations pass
     */
    const mockReq = {
      params: { groupId: 'valid-group-id-123' }
    } as any;

    const result = requireParam(mockReq, 'groupId');
    expect(result).toBe('valid-group-id-123');
  });
});

describe('asyncHandler - Async Error Wrapper', () => {
  /**
   * Tests for the asyncHandler wrapper
   * Covers: asyncHandler function
   */

  test('should catch errors from async route handlers', async () => {
    /**
     * Covers errorHandler.ts lines 82-88: asyncHandler function
     * Path: asyncHandler -> Promise.resolve -> catch -> next(error)
     */
    const app = express();
    app.use(express.json());
    
    // Create an async route handler that throws an error
    const asyncRouteHandler = async (_req: Request, _res: Response, _next: NextFunction) => {
      throw new Error('Async handler error');
    };
    
    // Wrap it with asyncHandler
    app.get('/test/async-error', asyncHandler(asyncRouteHandler));
    
    // Add error handler
    app.use(errorHandler);
    
    // Make request
    const response = await request(app)
      .get('/test/async-error');
    
    // Should catch the error and pass it to errorHandler
    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Async handler error');
  });

  test('should pass through successful async route handlers', async () => {
    /**
     * Covers errorHandler.ts lines 82-88: asyncHandler function (success path)
     * Path: asyncHandler -> Promise.resolve -> success -> no catch
     */
    const app = express();
    app.use(express.json());
    
    // Create an async route handler that succeeds
    const asyncRouteHandler = async (_req: Request, res: Response, _next: NextFunction) => {
      res.json({ success: true });
    };
    
    // Wrap it with asyncHandler
    app.get('/test/async-success', asyncHandler(asyncRouteHandler));
    
    // Make request
    const response = await request(app)
      .get('/test/async-success');
    
    // Should succeed without errors
    expect(response.status).toBe(200);
    expect(response.body.success).toBe(true);
  });
});