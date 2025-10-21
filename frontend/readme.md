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