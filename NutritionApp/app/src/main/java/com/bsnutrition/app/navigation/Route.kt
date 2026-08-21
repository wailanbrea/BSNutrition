package com.bsnutrition.app.navigation

import kotlinx.serialization.Serializable

sealed interface Route {

    @Serializable
    data object Login : Route

    @Serializable
    data object Register : Route

    @Serializable
    data object Main : Route
}

sealed interface TopLevelRoute {
    @Serializable
    data object Home : TopLevelRoute

    @Serializable
    data object Diary : TopLevelRoute

    @Serializable
    data object Add : TopLevelRoute

    @Serializable
    data object Progress : TopLevelRoute

    @Serializable
    data object More : TopLevelRoute
}
