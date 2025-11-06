import { Request, Response, NextFunction } from 'express';

// Custom error class
export class AppError extends Error {
  statusCode: number;

  constructor(message: string, statusCode: number) {
    super(message);
    this.statusCode = statusCode;

    Error.captureStackTrace(this, this.constructor);
  }
}

// Error handler middleware
export const errorHandler = (
  err: Error | AppError,
  req: Request,
  res: Response,
  _next: NextFunction
): void => {
  // Default error values
  let statusCode = 500;
  let message = 'Internal Server Error';

  // Check if it's our custom AppError
  if (err instanceof AppError) {
    statusCode = err.statusCode;
    message = err.message;
  } else if (err.name === 'ValidationError') {
    // Mongoose validation error
    statusCode = 400;
    message = err.message;
  } else if (err.name === 'CastError') {
    // Mongoose cast error (invalid ObjectId, etc.)
    statusCode = 400;
    message = 'Invalid data format';
  } else if (err.name === 'MongoServerError' && 'code' in err && (err as { code: number }).code === 11000) {
    // MongoDB duplicate key error
    statusCode = 409;
    message = 'Duplicate entry - resource already exists';
  } else if (err.message) {
    message = err.message;
  }

  // Log error for debugging (in production, you might want to use a logging service)
  console.error('Error:', {
    message: err.message,
    statusCode,
    stack: err.stack,
    timestamp: new Date().toISOString(),
    path: req.path,
    method: req.method
  });

  // Send error response
  res.status(statusCode).json({
    error: err.name || 'Error',
    message: message,
    statusCode: statusCode,
    ...(process.env.NODE_ENV === 'development' && {
      stack: err.stack,
      details: err
    })
  });
};

// 404 handler for routes that don't exist
export const notFoundHandler = (
  req: Request,
  _res: Response,
  next: NextFunction
): void => {
  const error = new AppError(
    `Route ${req.originalUrl} not found`,
    404
  );
  next(error);
};

// Async error wrapper - wraps async route handlers to catch errors
// This wrapper ensures async route handlers don't return promises to Express
// Type signature explicitly shows it accepts a promise-returning function but returns a void function
export const asyncHandler = (
  fn: (req: Request, res: Response, next: NextFunction) => Promise<void>
): ((req: Request, res: Response, next: NextFunction) => void) => {
  return (req: Request, res: Response, next: NextFunction): void => {
    // Execute async function in IIFE to prevent promise return
    // Explicitly void the IIFE to satisfy strict linters that check for promise returns
    const promise = (async (): Promise<void> => {
      try {
        await fn(req, res, next);
      } catch (error) {
        next(error);
      }
    })();
    // Explicitly void the promise to prevent it from being returned
    void promise;
  };
};