// This checks userGuard file
// tests/unit/controller-type-guards.test.ts

/**
 * Controller Type Guard Tests
 * 
 * These tests specifically cover the ensureAuthenticated type guards
 * in controller methods. These guards are defensive checks that should
 * never trigger in production (auth middleware runs first), but we test
 * them for 100% code coverage.
 */

import { Response, NextFunction } from 'express';
import { AuthRequest } from '../../src/types';
import { userController } from '../../src/controllers/user.controller';
import { groupController } from '../../src/controllers/group.controller';
import { matchingController } from '../../src/controllers/matching.controller';

describe('User Controller Type Guards', () => {
  let req: Partial<AuthRequest>;
  let res: Partial<Response>;
  let next: NextFunction;

  beforeEach(() => {
    req = {
      headers: {},
      body: {},
      params: {},
      user: undefined // Simulate missing user (middleware failure)
    };
    
    res = {
      status: jest.fn().mockReturnThis(),
      json: jest.fn().mockReturnThis(),
    };
    
    next = jest.fn();
  });

  test('getUserSettings should return 401 when req.user is undefined', async () => {
    await userController.getUserSettings(
      req as AuthRequest,
      res as Response,
      next
    );

    expect(res.status).toHaveBeenCalledWith(401);
    expect(res.json).toHaveBeenCalledWith({
      Status: 401,
      Message: { error: 'Unauthorized - User not authenticated' },
      Body: null
    });
    expect(next).not.toHaveBeenCalled();
  });

  test('createUserProfile should return 401 when req.user is undefined', async () => {
    req.body = { name: 'Test User' };

    await userController.createUserProfile(
      req as AuthRequest,
      res as Response,
      next
    );

    expect(res.status).toHaveBeenCalledWith(401);
    expect(res.json).toHaveBeenCalledWith({
      Status: 401,
      Message: { error: 'Unauthorized - User not authenticated' },
      Body: null
    });
    expect(next).not.toHaveBeenCalled();
  });

  test('updateUserSettings should return 401 when req.user is undefined', async () => {
    req.body = { name: 'Updated Name' };

    await userController.updateUserSettings(
      req as AuthRequest,
      res as Response,
      next
    );

    expect(res.status).toHaveBeenCalledWith(401);
    expect(res.json).toHaveBeenCalledWith({
      Status: 401,
      Message: { error: 'Unauthorized - User not authenticated' },
      Body: null
    });
    expect(next).not.toHaveBeenCalled();
  });

  test('updateUserProfile should return 401 when req.user is undefined', async () => {
    req.body = { name: 'Updated Name' };

    await userController.updateUserProfile(
      req as AuthRequest,
      res as Response,
      next
    );

    expect(res.status).toHaveBeenCalledWith(401);
    expect(res.json).toHaveBeenCalledWith({
      Status: 401,
      Message: { error: 'Unauthorized - User not authenticated' },
      Body: null
    });
    expect(next).not.toHaveBeenCalled();
  });

  test('deleteUser should return 401 when req.user is undefined', async () => {
    req.params = { userId: 'some-user-id' };

    await userController.deleteUser(
      req as AuthRequest,
      res as Response,
      next
    );

    expect(res.status).toHaveBeenCalledWith(401);
    expect(res.json).toHaveBeenCalledWith({
      Status: 401,
      Message: { error: 'Unauthorized - User not authenticated' },
      Body: null
    });
    expect(next).not.toHaveBeenCalled();
  });
});

describe('Group Controller Type Guards', () => {
  let req: Partial<AuthRequest>;
  let res: Partial<Response>;
  let next: NextFunction;

  beforeEach(() => {
    req = {
      headers: {},
      body: {},
      params: {},
      user: undefined // Simulate missing user
    };
    
    res = {
      status: jest.fn().mockReturnThis(),
      json: jest.fn().mockReturnThis(),
    };
    
    next = jest.fn();
  });

  test('getGroupStatus should return 401 when req.user is undefined', async () => {
    await groupController.getGroupStatus(
      req as AuthRequest,
      res as Response,
      next
    );

    expect(res.status).toHaveBeenCalledWith(401);
    expect(res.json).toHaveBeenCalledWith({
      Status: 401,
      Message: { error: 'Unauthorized - User not authenticated' },
      Body: null
    });
    expect(next).not.toHaveBeenCalled();
  });

  test('voteForRestaurant should return 401 when req.user is undefined', async () => {
    req.params = { groupId: 'group-123' };
    req.body = { restaurantID: 'rest-123' };

    await groupController.voteForRestaurant(
      req as AuthRequest,
      res as Response,
      next
    );

    expect(res.status).toHaveBeenCalledWith(401);
    expect(res.json).toHaveBeenCalledWith({
      Status: 401,
      Message: { error: 'Unauthorized - User not authenticated' },
      Body: null
    });
    expect(next).not.toHaveBeenCalled();
  });

  test('leaveGroup should return 401 when req.user is undefined', async () => {
    req.params = { groupId: 'group-123' };

    await groupController.leaveGroup(
      req as AuthRequest,
      res as Response,
      next
    );

    expect(res.status).toHaveBeenCalledWith(401);
    expect(res.json).toHaveBeenCalledWith({
      Status: 401,
      Message: { error: 'Unauthorized - User not authenticated' },
      Body: null
    });
    expect(next).not.toHaveBeenCalled();
  });
});

describe('Matching Controller Type Guards', () => {
  let req: Partial<AuthRequest>;
  let res: Partial<Response>;
  let next: NextFunction;

  beforeEach(() => {
    req = {
      headers: {},
      body: {},
      params: {},
      user: undefined // Simulate missing user
    };
    
    res = {
      status: jest.fn().mockReturnThis(),
      json: jest.fn().mockReturnThis(),
    };
    
    next = jest.fn();
  });

  test('joinMatching should return 401 when req.user is undefined', async () => {
    req.body = {
      cuisine: 'Italian',
      budget: '$$$',
      radiusKm: 5
    };

    await matchingController.joinMatching(
      req as AuthRequest,
      res as Response,
      next
    );

    expect(res.status).toHaveBeenCalledWith(401);
    expect(res.json).toHaveBeenCalledWith({
      Status: 401,
      Message: { error: 'Unauthorized - User not authenticated' },
      Body: null
    });
    expect(next).not.toHaveBeenCalled();
  });

  test('leaveRoom should return 401 when req.user is undefined', async () => {
    req.params = { roomId: 'room-123' };

    await matchingController.leaveRoom(
      req as AuthRequest,
      res as Response,
      next
    );

    expect(res.status).toHaveBeenCalledWith(401);
    expect(res.json).toHaveBeenCalledWith({
      Status: 401,
      Message: { error: 'Unauthorized - User not authenticated' },
      Body: null
    });
    expect(next).not.toHaveBeenCalled();
  });
});