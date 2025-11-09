import { Request, Response, NextFunction } from 'express';

// Custom error class
export class AppError extends Error {
  statusCode: number;

  constructor(message: string, statusCode: number) {
    super(message);
    this.statusCode = statusCode;
    this.name = 'AppError';

    Error.captureStackTrace(this, this.constructor);
  }
}

// Allowed route params whitelist for safety
const ALLOWED_PARAMS = ['id', 'userId', 'roomId', 'groupId', 'restaurantId', 'ids']; // All valid route parameters

// Helper function to safely extract required route parameters
export function requireParam(req: Request, paramName: string): string {
  // Validate paramName is a safe string
  if (!paramName || typeof paramName !== 'string' || paramName.length === 0) {
    throw new AppError('Invalid parameter name', 400);
  }
  
  // Whitelist check to prevent object injection
  if (!ALLOWED_PARAMS.includes(paramName)) {
    throw new AppError(`Invalid parameter requested: ${paramName}`, 400);
  }

  // Use Object.prototype.hasOwnProperty.call() to safely check for parameter
  // This prevents prototype pollution attacks
  if (!Object.prototype.hasOwnProperty.call(req.params, paramName)) {
    throw new AppError(`Missing required parameter: ${paramName}`, 400);
  }

  // Safely access the parameter value using hasOwnProperty check
  const value = req.params[paramName];
  if (!value) {
    throw new AppError(`Missing required parameter: ${paramName}`, 400);
  }
  
  // Ensure value is a string to prevent object injection
  if (typeof value !== 'string') {
    throw new AppError(`Invalid parameter type for: ${paramName}`, 400);
  }

  return value;
}

// Async error wrapper
export const asyncHandler = (
  fn: (req: Request, res: Response, next: NextFunction) => Promise<void>
): ((req: Request, res: Response, next: NextFunction) => void) => {
  return (req: Request, res: Response, next: NextFunction): void => {
    void (async () => {
      try {
        await fn(req, res, next);
      } catch (error) {
        next(error);
      }
    })();
  };
};

// Error handler middleware
export const errorHandler = (
  err: Error | AppError,
  req: Request,
  res: Response,
  _next: NextFunction
): void => {
  let statusCode = 500;
  let message = 'Internal Server Error';

  if (err instanceof AppError) {
    statusCode = err.statusCode;
    message = err.message;
  } else if (err.name === 'ValidationError') {
    statusCode = 400;
    message = err.message;
  } else if (err.name === 'CastError') {
    statusCode = 400;
    message = 'Invalid data format';
  } else if (err.name === 'MongoServerError' && 'code' in err && (err as { code: number }).code === 11000) {
    statusCode = 409;
    message = 'Duplicate entry - resource already exists';
  } else if (err.message) {
    message = err.message;
  }

  console.error('Error:', {
    message: err.message,
    statusCode,
    stack: err.stack,
    timestamp: new Date().toISOString(),
    path: req.path,
    method: req.method
  });

  res.status(statusCode).json({
    error: err.name || 'Error',
    message,
    statusCode,
    ...(process.env.NODE_ENV === 'development' && {
      stack: err.stack,
      details: err
    })
  });
};

// 404 handler
export const notFoundHandler = (
  req: Request,
  _res: Response,
  next: NextFunction
): void => {
  const error = new AppError(`Route ${req.originalUrl} not found`, 404);
  next(error);
};
