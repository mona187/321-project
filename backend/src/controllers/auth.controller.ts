import { Request, Response, NextFunction } from 'express';
import { AuthService } from '../services/authService';

const authService = new AuthService();

export class AuthController {
  async googleAuth(req: Request, res: Response, next: NextFunction) {
    try {
      const { idToken } = req.body;
      const isSignup = req.path.includes('/signup');

      if (!idToken) {
        res.status(400).json({ 
          error: 'ID token is required',
          message: 'Please provide a valid Google ID token'
        });
        return;
      }

      const result = await authService.authenticateUser(idToken, isSignup);

      if (isSignup) {
        // For signup, return success message without token or user data
        res.status(201).json({
          message: result.message || 'Account created successfully! Please sign in to continue.',
          data: null // No data for signup - user needs to sign in separately
        });
      } else {
        // For signin, return token and user data
        res.status(200).json({
          message: 'Sign in successful',
          data: {
            token: result.token,
            user: result.user,
          }
        });
      }
    } catch (error: any) {
      // Make error messages more visible and clear
      const statusCode = error.statusCode || 500;
      const message = error.message || 'Authentication failed';
      
      res.status(statusCode).json({
        error: message,
        message: message,
        status: statusCode
      });
    }
  }
}