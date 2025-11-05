package com.example.cpen_321.e2e

import com.example.cpen_321.e2e.group.LeaveRematchE2ETest
import com.example.cpen_321.e2e.group.ViewGroupE2ETest
import com.example.cpen_321.e2e.group.ViewRestaurantE2ETest
import com.example.cpen_321.e2e.group.VoteRestaurantE2ETest
import com.example.cpen_321.e2e.matchmaking.ExitWaitingRoomE2ETest
import com.example.cpen_321.e2e.matchmaking.JoinWaitingRoomE2ETest
import com.example.cpen_321.e2e.profile.AddProfileInformationE2ETest
import com.example.cpen_321.e2e.profile.SetPreferencesE2ETest
import com.example.cpen_321.e2e.profile.UpdateProfileInformationE2ETest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    SetPreferencesE2ETest::class,
    AddProfileInformationE2ETest::class,
    UpdateProfileInformationE2ETest::class,
    JoinWaitingRoomE2ETest::class,
    ExitWaitingRoomE2ETest::class,
    ViewRestaurantE2ETest::class,
    VoteRestaurantE2ETest::class,
    ViewGroupE2ETest::class,
    LeaveRematchE2ETest::class
)
class E2ETestSuite