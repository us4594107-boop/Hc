package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.PayrollScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RequestsScreen
import com.example.ui.screens.SignInScreen
import com.example.ui.screens.TrainingScreen

const val ROUTE_HOME = "home"
const val ROUTE_ATTENDANCE = "attendance"
const val ROUTE_REQUESTS = "requests"
const val ROUTE_PAYROLL = "payroll"
const val ROUTE_PROFILE = "profile"
const val ROUTE_TRAINING = "training"

private data class NavTab(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val mainTabs = listOf(
    NavTab(ROUTE_HOME, R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
    NavTab(ROUTE_ATTENDANCE, R.string.nav_attendance, Icons.Filled.EventNote, Icons.Outlined.EventNote, "nav_attendance"),
    NavTab(ROUTE_REQUESTS, R.string.nav_requests, Icons.Filled.Assignment, Icons.Outlined.Assignment, "nav_requests"),
    NavTab(ROUTE_PAYROLL, R.string.nav_payroll, Icons.Filled.Payments, Icons.Outlined.Payments, "nav_payroll"),
    NavTab(ROUTE_PROFILE, R.string.nav_profile, Icons.Filled.Person, Icons.Outlined.Person, "nav_profile")
)

@Composable
fun AlfaoneApp(
    viewModel: AlfaoneViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessages.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    when {
        uiState.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        uiState.employee == null || uiState.employee?.isSignedIn == false -> {
            SignInScreen(
                onSignIn = { nik, pin, onError ->
                    viewModel.signIn(nik, pin, onError)
                }
            )
        }

        else -> {
            MainAuthenticatedShell(
                uiState = uiState,
                snackbarHostState = snackbarHostState,
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun MainAuthenticatedShell(
    uiState: AlfaoneUiState,
    snackbarHostState: SnackbarHostState,
    viewModel: AlfaoneViewModel
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: ROUTE_HOME
    val showBottomOrRail = currentRoute != ROUTE_TRAINING
    var draftRequestCategory by remember { mutableStateOf<String?>(null) }

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (showBottomOrRail && !useRail) {
                    NavigationBar {
                        mainTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            val label = stringResource(tab.labelRes)
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = { Text(label) },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (showBottomOrRail && useRail) {
                    NavigationRail {
                        mainTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            val label = stringResource(tab.labelRes)
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = { Text(label) },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = ROUTE_HOME,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(ROUTE_HOME) {
                        DashboardScreen(
                            uiState = uiState,
                            onClockIn = { mode, loc -> viewModel.clockIn(mode, loc) },
                            onStartBreak = { viewModel.startBreak() },
                            onEndBreak = { viewModel.endBreak() },
                            onClockOut = { loc -> viewModel.clockOut(loc) },
                            onNavigateToAttendance = { navigateToTab(ROUTE_ATTENDANCE) },
                            onNavigateToRequests = { category ->
                                draftRequestCategory = category
                                navigateToTab(ROUTE_REQUESTS)
                            },
                            onNavigateToPayroll = { navigateToTab(ROUTE_PAYROLL) },
                            onNavigateToTraining = { navController.navigate(ROUTE_TRAINING) },
                            onMarkAnnouncementRead = { id -> viewModel.markAnnouncementRead(id) }
                        )
                    }

                    composable(ROUTE_ATTENDANCE) {
                        AttendanceScreen(
                            uiState = uiState,
                            onSubmitCorrection = { dateKey, mode, note ->
                                viewModel.submitAttendanceCorrection(dateKey, mode, note)
                            }
                        )
                    }

                    composable(ROUTE_REQUESTS) {
                        RequestsScreen(
                            uiState = uiState,
                            initialDraftCategory = draftRequestCategory,
                            onConsumeDraftCategory = { draftRequestCategory = null },
                            onSubmitRequest = { cat, title, start, end, dur, reason, uri ->
                                viewModel.submitLeaveRequest(cat, title, start, end, dur, reason, uri)
                            },
                            onCancelRequest = { id -> viewModel.cancelLeaveRequest(id) }
                        )
                    }

                    composable(ROUTE_PAYROLL) {
                        PayrollScreen(
                            uiState = uiState,
                            onVerifyPinForSalary = { pin, onInvalid ->
                                viewModel.toggleSalaryReveal(pin, onInvalid)
                            },
                            onHideSalary = { viewModel.toggleSalaryReveal() }
                        )
                    }

                    composable(ROUTE_PROFILE) {
                        ProfileScreen(
                            uiState = uiState,
                            onOpenTraining = { navController.navigate(ROUTE_TRAINING) },
                            onChangePin = { oldPin, newPin, onSuccess, onError ->
                                viewModel.changePin(oldPin, newPin, onSuccess, onError)
                            },
                            onSignOut = { viewModel.signOut() }
                        )
                    }

                    composable(ROUTE_TRAINING) {
                        TrainingScreen(
                            uiState = uiState,
                            onBack = { navController.popBackStack() },
                            onCompleteModule = { id -> viewModel.completeTrainingModule(id) }
                        )
                    }
                }
            }
        }
    }
}
