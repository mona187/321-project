package com.example.cpen_321.data.model

import com.google.gson.annotations.SerializedName

/**
 * Credibility log entry
 */
data class CredibilityLog(
    @SerializedName("logId")
    val logId: String,

    @SerializedName("userId")
    val userId: String,

    @SerializedName("action")
    val action: CredibilityAction,

    @SerializedName("scoreChange")
    val scoreChange: Int,

    @SerializedName("groupId")
    val groupId: String? = null,

    @SerializedName("roomId")
    val roomId: String? = null,

    @SerializedName("previousScore")
    val previousScore: Double,

    @SerializedName("newScore")
    val newScore: Double,

    @SerializedName("notes")
    val notes: String? = null,

    @SerializedName("createdAt")
    val createdAt: String
)

/**
 * Credibility action enum
 */
enum class CredibilityAction {
    @SerializedName("no_show")
    NO_SHOW,

    @SerializedName("late_cancel")
    LATE_CANCEL,

    @SerializedName("left_group_early")
    LEFT_GROUP_EARLY,

    @SerializedName("completed_meetup")
    COMPLETED_MEETUP,

    @SerializedName("positive_review")
    POSITIVE_REVIEW,

    @SerializedName("negative_review")
    NEGATIVE_REVIEW
}

/**
 * Credibility statistics
 */
data class CredibilityStats(
    @SerializedName("currentScore")
    val currentScore: Double,

    @SerializedName("totalLogs")
    val totalLogs: Int,

    @SerializedName("positiveActions")
    val positiveActions: Int,

    @SerializedName("negativeActions")
    val negativeActions: Int,

    @SerializedName("recentTrend")
    val recentTrend: String // "improving", "stable", "declining"
)