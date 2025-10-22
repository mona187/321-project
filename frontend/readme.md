## frontend subdirectory




## Frontend folder structure:
Note: file structure is incomplete, add files if needed

- com.example.cpen_321
    - data
        - local
            - TokenManager.kt
        - model
            - Group.kt
            - Match.kt
            - Restaurant.kt
            - User.kt
        - network
            - api
                - AuthAPI
                - MatchAPI
                - RetrofitClient
                - UserAPI.kt
            - dto
                - ApiResponse.kt
                - AuthModels.kt
            - interceptors
                - AuthInterceptor
        - repository
            - AuthRepository
            - AuthRepositoryImpl
            - MatchRepository
            - MatchRepositoryImpl
    - di
        - NetworkModule
        - RepositoryModule
    - ui
        - components
            - BottomAppBar.kt
        - navigation
            - NavGraph.kt
            - NavigationStateManager
            - NavRoutes
        - screens
            - AuthScreen.kt
            - CredibilityScreen.kt
            - GroupScreen.kt
            - HomeScreen.kt
            - PreferencesScreen.kt
            - ProfileConfigScreen.kt
            - ProfileScreen.kt
            - ViewGroupsScreen.kt
            - VoteRestaurantScreen.kt
            - WaitingRoomScreen.kt
        - theme
            - Color.kt
            - Spacing.kt
            - Theme.kt
            - Type.kt
        - viewmodels
            - AuthViewModel.kt
            - MatchViewModel.kt
    - utils
        - JsonUtils
        - SocketManager
    - MainActivity.kt
    - MyApplication






## Summary of ViewModels:
    - AuthViewModel
        - Google sign-in
        - Token verification
        - Logout
        - Socket connection management
        - FCM token updates
        - Account deletion

    - MatchViewModel
        - Join matching pool
        - Real-time room updates via Socket.IO
        - Room member management
        - Timer for room expiration
        - Group ready notifications

    - UserViewModel
        - Load/update user settings
        - Load/update user profile
        - Cuisine preference management
        - Budget and radius settings
        - Local preference caching

    - GroupViewModel
        - Load group status
        - Vote for restaurants
        - Real-time vote updates via Socket.IO
        - Group member tracking
        - Restaurant selection notifications
        - Leave group

    - RestaurantViewModel
        - Search restaurants by location
        - Get restaurant details
        - Get group recommendations
        - Restaurant selection