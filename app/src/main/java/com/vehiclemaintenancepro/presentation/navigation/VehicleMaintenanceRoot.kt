package com.vehiclemaintenancepro.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.LocalIndication
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.vehiclemaintenancepro.presentation.screen.dashboard.DashboardViewModel
import com.vehiclemaintenancepro.presentation.screen.vehicles.VehiclesViewModel
import com.vehiclemaintenancepro.presentation.screen.service.ServiceViewModel
import com.vehiclemaintenancepro.presentation.screen.statistics.StatisticsViewModel
import com.vehiclemaintenancepro.presentation.screen.settings.SettingsViewModel
import com.vehiclemaintenancepro.presentation.screen.notifications.NotificationsViewModel
import com.vehiclemaintenancepro.R
import com.vehiclemaintenancepro.presentation.screen.dashboard.DashboardRoute
import com.vehiclemaintenancepro.core.design.MotionTokens
import com.vehiclemaintenancepro.presentation.screen.notifications.NotificationsRoute
import com.vehiclemaintenancepro.presentation.screen.service.ServiceRoute
import com.vehiclemaintenancepro.presentation.screen.settings.SettingsRoute
import com.vehiclemaintenancepro.presentation.screen.statistics.StatisticsRoute
import com.vehiclemaintenancepro.presentation.screen.vehicles.AddVehicleDialogRoute
import com.vehiclemaintenancepro.presentation.screen.vehicles.VehiclesRoute
import com.vehiclemaintenancepro.presentation.component.AppTopBar

@Composable
fun VehicleMaintenanceRoot(
    navController: NavHostController = rememberNavController(),
) {
    val dashboardViewModel: DashboardViewModel = hiltViewModel()
    val vehiclesViewModel: VehiclesViewModel = hiltViewModel()
    val serviceViewModel: ServiceViewModel = hiltViewModel()
    val statisticsViewModel: StatisticsViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val notificationsViewModel: NotificationsViewModel = hiltViewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    var selectedRoute by rememberSaveable { mutableStateOf(AppRoute.Dashboard.route) }
    var showAddVehicleDialog by rememberSaveable { mutableStateOf(false) }
    var addReminderRequest by rememberSaveable { mutableIntStateOf(0) }
    var addActivityRequest by rememberSaveable { mutableIntStateOf(0) }
    var odometerRequest by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(currentDestination?.route) {
        currentDestination?.route
            ?.takeIf { route -> bottomNavRoutes.any { it.route == route } }
            ?.let { selectedRoute = it }
    }

    fun openRoute(route: String) {
        if (bottomNavRoutes.any { it.route == route }) {
            selectedRoute = route
        }
        if (bottomNavRoutes.any { it.route == route }) {
            navController.navigateSingleTopTo(route)
        } else {
            navController.navigate(route) { launchSingleTop = true }
        }
    }

    fun openAddVehicle() {
        vehiclesViewModel.clearNotice()
        showAddVehicleDialog = true
    }

    fun openAddReminder() {
        addReminderRequest += 1
        openRoute(AppRoute.Service.route)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = if (currentDestination?.route == AppRoute.Notifications.route) "Pengingat"
                    else stringResource(bottomNavRoutes.firstOrNull { it.route == selectedRoute }?.labelRes ?: R.string.nav_dashboard),
                onNotifications = { openRoute(AppRoute.Notifications.route) },
                onProfile = { openRoute(AppRoute.Settings.route) },
                onBack = if (currentDestination?.route == AppRoute.Notifications.route) ({ navController.popBackStack(); Unit }) else null,
            )
        },
        bottomBar = {
            VehicleMaintenanceBottomBar(
                currentRoute = selectedRoute,
                onNavigate = ::openRoute,
            )
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.Dashboard.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
        ) {
            composable(AppRoute.Dashboard.route) {
                DashboardRoute(
                    viewModel = dashboardViewModel,
                    onAddVehicle = ::openAddVehicle,
                    onOpenService = { openRoute(AppRoute.Service.route) },
                    onOpenNotifications = { openRoute(AppRoute.Notifications.route) },
                    onOpenStatistics = { openRoute(AppRoute.Statistics.route) },
                    onOpenProfile = { odometerRequest += 1; openRoute(AppRoute.Settings.route) },
                    onRecordMaintenance = {
                        addActivityRequest += 1
                        openRoute(AppRoute.Service.route)
                    },
                )
            }
            composable(AppRoute.Notifications.route) {
                NotificationsRoute(
                    viewModel = notificationsViewModel,
                    onBack = { navController.popBackStack() },
                    onOpenService = { openRoute(AppRoute.Service.route) },
                )
            }
            composable(AppRoute.Vehicles.route) {
                VehiclesRoute(viewModel = vehiclesViewModel)
            }
            composable(AppRoute.Service.route) {
                ServiceRoute(
                    viewModel = serviceViewModel,
                    addReminderRequest = addReminderRequest,
                    addActivityRequest = addActivityRequest,
                    onActivityRequestConsumed = { addActivityRequest = 0 },
                    onReminderRequestConsumed = { addReminderRequest = 0 },
                    onAddVehicle = ::openAddVehicle,
                )
            }
            composable(AppRoute.Statistics.route) {
                StatisticsRoute(
                    viewModel = statisticsViewModel,
                    onOpenVehicles = { openRoute(AppRoute.Vehicles.route) },
                    onOpenService = { vehicleId ->
                        if (vehicleId == null) openRoute(AppRoute.Service.route)
                        else vehiclesViewModel.setActiveVehicle(vehicleId) { openRoute(AppRoute.Service.route) }
                    },
                    onAddVehicle = ::openAddVehicle,
                )
            }
            composable(AppRoute.Settings.route) {
                SettingsRoute(viewModel = settingsViewModel, onOpenVehicles = { openRoute(AppRoute.Vehicles.route) }, onOpenService = { openRoute(AppRoute.Service.route) },
                    odometerRequest = odometerRequest, onOdometerRequestConsumed = { odometerRequest = 0 })
            }
        }
    }

    if (showAddVehicleDialog) {
        AddVehicleDialogRoute(
            viewModel = vehiclesViewModel,
            onDismiss = { showAddVehicleDialog = false },
        )
    }
}

@Composable
private fun VehicleMaintenanceBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
    ) {
        val rows = if (LocalDensity.current.fontScale > 1.6f && maxWidth < 600.dp) bottomNavRoutes.chunked(3) else listOf(bottomNavRoutes)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 76.dp),
            shape = RoundedCornerShape(0.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Column {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                rows.forEach { destinations ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    destinations.forEach { destination ->
                        val selected = currentRoute == destination.route
                        val interactionSource = remember { MutableInteractionSource() }
                        val itemColor = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 60.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .selectable(
                                    selected = selected,
                                    role = Role.Tab,
                                    interactionSource = interactionSource,
                                    indication = LocalIndication.current,
                                    onClick = { onNavigate(destination.route) },
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                        ) {
                            Box(
                                modifier = Modifier.size(width = 52.dp, height = 32.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = if (selected) destination.selectedIcon else destination.icon,
                                    contentDescription = stringResource(destination.contentDescriptionRes),
                                    modifier = Modifier.size(22.dp),
                                    tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else itemColor,
                                )
                            }
                            Text(stringResource(destination.labelRes), style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = itemColor, textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 2.dp))
                        }
                    }
                }
                }
            }
        }
    }
}

private fun NavHostController.navigateSingleTopTo(route: String) {
    if (currentDestination?.route == route) return

    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = route != AppRoute.Dashboard.route
    }
}
