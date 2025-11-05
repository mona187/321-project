package com.example.cpen_321.e2e.utils

/**
 * Screen element constants matching actual implementations
 */
object ScreenObjects {

    // Common
    const val GO_BACK = "Go Back"
    const val LOADING = "Loading"

    // Home Screen
    const val WELCOME_PREFIX = "Welcome"
    const val START_MATCHMAKING = "Start Matchmaking"
    const val VIEW_ACTIVE_GROUP = "View Active Group"
    const val CURRENT_GROUPS = "Current Groups"
    const val CREDIBILITY_SCORE_PREFIX = "Credibility Score:"

    // ProfileConfigScreen
    const val PROFILE_BUTTON = "Profile"
    const val PREFERENCES_BUTTON = "Preferences"
    const val CREDIBILITY_SCORE_BUTTON = "Credibility Score"
    const val LOGOUT_BUTTON = "Logout"
    const val DELETE_ACCOUNT = "Delete Account"

    // ProfileScreen
    const val CHANGE_PROFILE_PICTURE = "Change Profile Picture"
    const val NAME_LABEL = "Name:"
    const val BIO_LABEL = "Bio:"
    const val PHONE_NUMBER_LABEL = "Phone Number:"
    const val SAVE_PROFILE = "Save Profile"
    const val PHONE_ERROR_MIN = "Phone number must be at least 10 digits"
    const val PHONE_ERROR_DIGITS = "Phone number must contain only digits"
    const val PHONE_ERROR_MAX = "Phone number must be no more than 15 digits"
    const val PROCESSING = "Processing..."

    // PreferencesScreen
    const val PREFERENCES_TITLE = "Preferences (Select)"
    const val MAX_MONEY_PREFIX = "Max amount of money to spend:"
    const val SEARCH_RADIUS_PREFIX = "Search radius:"
    const val SAVE_PREFERENCES = "Save Preferences"
    const val LOADING_PREFERENCES = "Loading preferences..."

    // Cuisine Options
    const val SUSHI = "Sushi"
    const val ITALIAN = "Italian"
    const val PIZZA = "Pizza"
    const val JAPANESE = "Japanese"
    const val EUROPEAN = "European"
    const val KOREAN = "Korean"
    const val MIDDLE_EASTERN = "Middle Eastern"
    const val CHINESE = "Chinese"

    // Success/Error Messages
    const val SETTINGS_UPDATED = "Settings updated successfully"
    const val CONNECTION_ERROR = "try again"
    const val IMAGE_SELECTED = "Image selected - click Save to apply changes"
    const val FAILED_TO_LOAD_IMAGE = "Failed to load image"

    // WaitingRoomScreen
    const val WAITING_ROOM_TITLE = "Waiting Room"
    const val FINDING_GROUP = "Finding your perfect dining group..."
    const val LEAVE_ROOM = "Leave Waiting Room"
    const val TIME_REMAINING = "Time remaining"
    const val FINISHING_SOON = "Finishing soon!"
    const val MEMBERS_IN_ROOM = "members in room"
    const val GROUP_READY = "Group Ready!"
    const val PREPARING_GROUP = "Preparing your group..."
    const val LOADING_MEMBERS = "Loading members..."

    // Leave Dialog
    const val LEAVE_CONFIRM_TITLE = "Leave Waiting Room?"
    const val STAY_BUTTON = "Stay"
    const val LEAVE_BUTTON = "Leave"

    // Room Failure Dialog
    const val UNABLE_TO_CREATE_GROUP = "Unable to Create Group"
    const val TIMER_EXPIRED = "The waiting room timer expired"
    const val TRY_AGAIN = "Try Again"

    // VoteRestaurantScreen
    const val VOTE_RESTAURANT_TITLE = "Restaurant Voting"
    const val SELECT_RESTAURANT = "Select a restaurant to vote"
    const val RESTAURANT_OPTION = "Restaurant Option" // ADD THIS
    const val LOCATION_PERMISSION = "Location permission required"
    const val GRANT_PERMISSION = "Grant Permission"
    const val LOADING_RESTAURANTS = "Loading restaurants..."
    const val NO_RESTAURANTS = "No restaurants found"
    const val VOTE_RECORDED = "Vote recorded"
    const val CHANGE_VOTE = "Change vote"

    // ViewGroupsScreen / GroupScreen
    const val NO_ACTIVE_GROUPS = "No active groups"
    const val GROUP_TITLE = "Group"
    const val LEAVE_GROUP = "Leave Group"
    const val LEAVE_GROUP_CONFIRM = "Are you sure you want to leave this group?"
    const val RESTAURANT_NAME_PREFIX = "Restaurant:"
    const val LOCATION_PREFIX = "Location:"
    const val MEMBER_PREFIX = "Member"
    const val CLOSE_GROUP = "Close Group"
    const val GO_TO_HOME = "Go to Home"
    const val VIEW_DETAILS = "View Details" // ADD THIS

    // Semantic descriptions
    const val PROFILE_PICTURE_DESCRIPTION = "Profile Picture"
    const val TIMER_DESCRIPTION = "Timer"
    const val DEFAULT_AVATAR_DESCRIPTION = "Default avatar"

    // ContentDescriptions for testing
    const val BUDGET_SLIDER = "Budget Slider"
    const val RADIUS_SLIDER = "Radius Slider"
}