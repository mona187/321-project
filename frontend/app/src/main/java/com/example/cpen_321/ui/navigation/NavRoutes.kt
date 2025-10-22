object NavRoutes {
    const val AUTH = "auth"
    const val HOME = "home"
    const val WAITING_ROOM = "waiting_room"
    const val GROUP = "group"
    const val VOTE_RESTAURANT = "vote_restaurant"
    const val PROFILE_CONFIG = "profile_config"
    const val PROFILE = "profile"
    const val PREFERENCES = "preferences"
    const val CREDIBILITY_SCORE = "credibility_score"
    const val VIEW_GROUPS = "view_groups"

    // Navigation helpers with parameters
    fun groupWithId(groupId: String) = "group/$groupId"
    fun voteRestaurantWithId(groupId: String) = "vote_restaurant/$groupId"
}