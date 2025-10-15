import { Request, Response, NextFunction } from 'express';
import { AuthService } from '../services/authService';

const authService = new AuthService();

export class AuthController {
  async googleAuth(req: Request, res: Response, next: NextFunction) {
    try {
      const { idToken } = req.body;

      if (!idToken) {
        res.status(400).json({ error: 'ID token is required' });
        return;
      }

      const result = await authService.authenticateUser(idToken);

      res.status(200).json({
        message: 'Authentication successful',
        token: result.token,
        user: result.user,
        isNewUser: result.isNewUser,
      });
    } catch (error) {
      next(error);
    }
  }
}