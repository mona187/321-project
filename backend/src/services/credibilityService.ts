import { User } from '../models/User';
import { Group } from '../models/Group';
import { CredibilityLog } from '../models/CredibilityLog';
import { AppError } from '../middleware/errorHandler';

export class CredibilityService {
  private readonly CHECK_IN_SCORE_CHANGE = 0;
  private readonly NO_SHOW_SCORE_CHANGE = -0.5;
  private readonly MAX_SCORE = 5.0;
  private readonly MIN_SCORE = 0.0;

  async checkIn(userId: string, groupId: string) {
    const user = await User.findById(userId);
    const group = await Group.findOne({ groupId });

    if (!user) {
      throw new AppError(404, 'User not found');
    }

    if (!group) {
      throw new AppError(404, 'Group not found');
    }

    if (!group.users.includes(userId)) {
      throw new AppError(400, 'User not part of this group');
    }

    // Check if already checked in
    const existingLog = await CredibilityLog.findOne({ userId, groupId });
    if (existingLog) {
      throw new AppError(400, 'User already checked in');
    }

    // Create check-in log
    await CredibilityLog.create({
      userId,
      groupId,
      checkedIn: true,
      timestamp: new Date(),
      scoreChange: this.CHECK_IN_SCORE_CHANGE,
    });

    // Maintain score (no change for checking in, but prevents decrease)
    return {
      message: 'Check-in successful',
      credibilityScore: user.credibilityScore,
    };
  }

  async processNoShows(groupId: string) {
    const group = await Group.findOne({ groupId });

    if (!group) {
      throw new AppError(404, 'Group not found');
    }

    // Find users who didn't check in
    const checkedInUsers = await CredibilityLog.find({
      groupId,
      checkedIn: true,
    }).distinct('userId');

    const noShowUsers = group.users.filter(
      (userId) => !checkedInUsers.includes(userId)
    );

    // Decrease credibility for no-shows
    for (const userId of noShowUsers) {
      const user = await User.findById(userId);
      if (user) {
        const newScore = Math.max(
          this.MIN_SCORE,
          user.credibilityScore + this.NO_SHOW_SCORE_CHANGE
        );

        await User.findByIdAndUpdate(userId, {
          $set: { credibilityScore: newScore },
        });

        await CredibilityLog.create({
          userId,
          groupId,
          checkedIn: false,
          timestamp: new Date(),
          scoreChange: this.NO_SHOW_SCORE_CHANGE,
        });
      }
    }

    return {
      message: 'No-show processing complete',
      noShowCount: noShowUsers.length,
    };
  }

  async getUserCredibilityHistory(userId: string) {
    const logs = await CredibilityLog.find({ userId })
      .sort({ timestamp: -1 })
      .limit(20)
      .populate('groupId', 'restaurant completionTime');

    const user = await User.findById(userId, 'credibilityScore');

    return {
      currentScore: user?.credibilityScore,
      history: logs,
    };
  }

  async calculatePriorityScore(userId: string): Promise<number> {
    const user = await User.findById(userId);
    if (!user) {
      return 0;
    }

    // Higher credibility = higher priority
    return user.credibilityScore * 10;
  }
}