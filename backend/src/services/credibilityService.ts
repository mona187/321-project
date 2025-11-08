import User from '../models/User';
import CredibilityLog, { CredibilityAction } from '../models/CredibilityLog';

export class CredibilityService {
  /**
   * Update user's credibility score
   */
  async updateCredibilityScore(
    userId: string,
    action: CredibilityAction,
    groupId?: string,
    roomId?: string,
    notes?: string
  ): Promise<{
    previousScore: number;
    newScore: number;
    scoreChange: number;
  }> {
    const user = await User.findById(userId);

    if (!user) {
      throw new Error('User not found');
    }

    const previousScore = user.credibilityScore;
    
    // Get score change using switch statement to avoid insecure dynamic property access
    let scoreChange: number;
    switch (action) {
      case CredibilityAction.NO_SHOW:
        scoreChange = -15;
        break;
      case CredibilityAction.LATE_CANCEL:
        scoreChange = -10;
        break;
      case CredibilityAction.LEFT_GROUP_EARLY:
        scoreChange = -5;
        break;
      case CredibilityAction.COMPLETED_MEETUP:
        scoreChange = 5;
        break;
      case CredibilityAction.POSITIVE_REVIEW:
        scoreChange = 3;
        break;
      case CredibilityAction.NEGATIVE_REVIEW:
        scoreChange = -8;
        break;
      default:
        throw new Error(`Invalid credibility action: ${String(action)}`);
    }
    
    let newScore = previousScore + scoreChange;

    // Clamp score between 0 and 100
    newScore = Math.max(0, Math.min(100, newScore));

    // Update user score
    user.credibilityScore = newScore;
    await user.save();

    // Log the change
    await CredibilityLog.create({
      userId,
      action,
      scoreChange,
      groupId,
      roomId,
      previousScore,
      newScore,
      notes,
    });

    console.log(
      `📊 Credibility updated for user ${userId}: ${previousScore} → ${newScore} (${action})`
    );

    return {
      previousScore,
      newScore,
      scoreChange,
    };
  }

  /**
   * Record a completed meetup (positive action)
   */
  async recordCompletedMeetup(
    userId: string,
    groupId: string
  ): Promise<void> {
    await this.updateCredibilityScore(
      userId,
      CredibilityAction.COMPLETED_MEETUP,
      groupId,
      undefined,
      'User completed meetup'
    );
  }

  /**
   * Record a no-show (negative action)
   */
  async recordNoShow(
    userId: string,
    groupId: string
  ): Promise<void> {
    await this.updateCredibilityScore(
      userId,
      CredibilityAction.NO_SHOW,
      groupId,
      undefined,
      'User did not show up'
    );
  }

  /**
   * Record leaving group early (negative action)
   */
  async recordLeftGroupEarly(
    userId: string,
    groupId: string
  ): Promise<void> {
    await this.updateCredibilityScore(
      userId,
      CredibilityAction.LEFT_GROUP_EARLY,
      groupId,
      undefined,
      'User left group before restaurant selected'
    );
  }

  /**
   * Record late cancellation (negative action)
   */
  async recordLateCancellation(
    userId: string,
    roomId: string
  ): Promise<void> {
    await this.updateCredibilityScore(
      userId,
      CredibilityAction.LATE_CANCEL,
      undefined,
      roomId,
      'User canceled late'
    );
  }

  /**
   * Get credibility logs for a user
   */
  async getUserCredibilityLogs(
    userId: string,
    limit: number = 20
  ): Promise<unknown[]> {
    const logs = await CredibilityLog.findByUserId(userId, limit);
    return logs;
  }

  /**
   * Get credibility statistics for a user
   */
  async getUserCredibilityStats(userId: string): Promise<{
    currentScore: number;
    totalLogs: number;
    positiveActions: number;
    negativeActions: number;
    recentTrend: string;
  }> {
    const user = await User.findById(userId);

    if (!user) {
      throw new Error('User not found');
    }

    const logs = await CredibilityLog.findByUserId(userId, 100);

    const positiveActions = logs.filter(log => log.scoreChange > 0).length;
    const negativeActions = logs.filter(log => log.scoreChange < 0).length;

    // Calculate recent trend (last 10 actions)
    const recentLogs = logs.slice(0, 10);
    const recentChange = recentLogs.reduce((sum, log) => sum + log.scoreChange, 0);
    
    let recentTrend = 'stable';
    if (recentChange > 5) recentTrend = 'improving';
    if (recentChange < -5) recentTrend = 'declining';

    return {
      currentScore: user.credibilityScore,
      totalLogs: logs.length,
      positiveActions,
      negativeActions,
      recentTrend,
    };
  }

  /**
   * Check if user meets minimum credibility requirement
   */
  isCredibilityAcceptable(score: number, minimumRequired: number = 50): boolean {
    return score >= minimumRequired;
  }

  /**
   * Restore credibility score (admin function or after appeal)
   */
  async restoreCredibilityScore(
    userId: string,
    amount: number,
    notes: string
  ): Promise<void> {
    const user = await User.findById(userId);

    if (!user) {
      throw new Error('User not found');
    }

    const previousScore = user.credibilityScore;
    const newScore = Math.min(100, previousScore + amount);

    user.credibilityScore = newScore;
    await user.save();

    // Log the restoration
    await CredibilityLog.create({
      userId,
      action: CredibilityAction.POSITIVE_REVIEW, // Use as a generic positive action
      scoreChange: amount,
      previousScore,
      newScore,
      notes: `Manual restoration: ${notes}`,
    });

    console.log(`✅ Restored ${amount} points to user ${userId}: ${previousScore} → ${newScore}`);
  }
}

export default new CredibilityService();