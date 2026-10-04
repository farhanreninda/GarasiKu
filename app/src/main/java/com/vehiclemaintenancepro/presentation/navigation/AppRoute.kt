package com.vehiclemaintenancepro.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.vehiclemaintenancepro.R

sealed class AppRoute(
    val route: String,
    @param:StringRes val labelRes: Int,
    @param:StringRes val contentDescriptionRes: Int,
    val icon: ImageVector,
) {
    data object Dashboard : AppRoute(
        route = "dashboard",
        labelRes = R.string.nav_dashboard,
        contentDescriptionRes = R.string.content_desc_dashboard,
        icon = Icons.Rounded.Home,
    )

    data object Vehicles : AppRoute(
        route = "vehicles",
        labelRes = R.string.nav_vehicles,
        contentDescriptionRes = R.string.content_desc_vehicles,
        icon = Icons.Rounded.DirectionsCar,
    )

    data object Service : AppRoute(
        route = "service",
        labelRes = R.string.nav_service,
        contentDescriptionRes = R.string.content_desc_service,
        icon = Icons.Rounded.Build,
    )

    data object Statistics : AppRoute(
        route = "statistics",
        labelRes = R.string.nav_statistics,
        contentDescriptionRes = R.string.content_desc_statistics,
        icon = Icons.Rounded.BarChart,
    )

    data object Settings : AppRoute(
        route = "settings",
        labelRes = R.string.nav_settings,
        contentDescriptionRes = R.string.content_desc_settings,
        icon = Icons.Rounded.Person,
    )

    data object Notifications : AppRoute(
        route = "notifications",
        labelRes = R.string.nav_notifications,
        contentDescriptionRes = R.string.content_desc_notifications,
        icon = Icons.Rounded.Notifications,
    )
}

val bottomNavRoutes = listOf(
    AppRoute.Dashboard,
    AppRoute.Vehicles,
    AppRoute.Service,
    AppRoute.Statistics,
    AppRoute.Settings,
)
