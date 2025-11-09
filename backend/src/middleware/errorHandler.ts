import { Request, Response, NextFunction } from 'express';

/**
 * Custom application error class
 */
export class AppError extends Error {
  statusCode: number;

  constructor(message: string, statusCode: number) {
    super(message);
    this.statusCode = statusCode;
    this.name = 'AppError';

    // Capture stack trace excluding constructor
    Error.captureStackTrace(this, this.constructor);
  }
}

/**
 * Error handler middleware
 */
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

  // Log error for debugging
  console.error('Error:', {
    message: err.message,
    statusCode,
    stack: err.stack,
    timestamp: new Date().toISOString(),
    path: req.path,
    method: req.method
  });

  // Send response
  res.status(statusCode).json({
    error: err.name || 'Error',
    message,
    statusCode,
    ...(process.env.NODE_ENV === 'development' && { stack: err.stack, details: err })
  });
};

/**
 * 404 handler for routes that don't exist
 */
export const notFoundHandler = (req: Request, _res: Response, next: NextFunction): void => {
  const error = new AppError(`Route ${req.originalUrl} not found`, 404);
  next(error);
};

/**
 * Safely extract a route parameter
 * Ensures only whitelisted parameters are allowed
 */
const allowedParams = ['id', 'userId', 'postId']; // <-- Adjust to your route params

export function requireParam(req: Request, paramName: string): string {
  if (!allowedParams.includes(paramName)) {
    throw new AppError(`Invalid parameter name: ${paramName}`, 400);
  }

  const value = req.params[paramName];
  if (!value) {
    throw new AppError(`Missing required parameter: ${paramName}`, 400);
  }

  return value;
}

/**
 * Async error wrapper for route handlers
 * Ensures errors in async functions are passed to errorHandler
 */
export const asyncHandler = (
  fn: (req: Request, res: Response, next: NextFunction) => Promise<void>
): ((req: Request, res: Response, next: NextFunction) => void) => {
  return (req: Request, res: Response, next: NextFunction): void => {
    void (async (): Promise<void> => {
      try {
        await fn(req, res, next);
      } catch (error) {
        next(error);
      }
    })();
  };
};
