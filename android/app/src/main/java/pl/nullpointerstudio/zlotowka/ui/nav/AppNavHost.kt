package pl.nullpointerstudio.zlotowka.ui.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import pl.nullpointerstudio.zlotowka.ui.screens.addexpense.AddExpenseScreen
import pl.nullpointerstudio.zlotowka.ui.screens.budget.BudgetScreen
import pl.nullpointerstudio.zlotowka.ui.screens.categories.CategoriesScreen
import pl.nullpointerstudio.zlotowka.ui.screens.comparisons.ComparisonsScreen
import pl.nullpointerstudio.zlotowka.ui.screens.dashboard.DashboardScreen
import pl.nullpointerstudio.zlotowka.ui.screens.goaldetail.GoalDetailScreen
import pl.nullpointerstudio.zlotowka.ui.screens.goals.GoalsScreen
import pl.nullpointerstudio.zlotowka.ui.screens.settings.SettingsScreen
import pl.nullpointerstudio.zlotowka.ui.theme.Lime
import pl.nullpointerstudio.zlotowka.ui.theme.Surface
import pl.nullpointerstudio.zlotowka.ui.theme.TextMuted
import pl.nullpointerstudio.zlotowka.ui.theme.TextPrimary

/** Trasy widoczne z dolnym paskiem nawigacji — pozostałe (szczegóły celu, porównania, ustawienia) chowają go. */
private val BOTTOM_BAR_ROUTES = setOf(
    Destinations.DASHBOARD,
    Destinations.ADD_EXPENSE,
    Destinations.CATEGORIES,
    Destinations.BUDGET,
    Destinations.GOALS,
)

@Composable
fun AppNavHost(navController: NavHostController, startRoute: String? = null) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute != null && BOTTOM_BAR_ROUTES.contains(currentRoute)

    LaunchedEffect(startRoute) {
        if (startRoute != null && startRoute != Destinations.DASHBOARD) {
            navController.navigate(startRoute)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(currentRoute = currentRoute, navController = navController)
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destinations.DASHBOARD,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Destinations.DASHBOARD) {
                DashboardScreen(onNavigate = navController::navigate)
            }
            composable(Destinations.ADD_EXPENSE) {
                AddExpenseScreen(
                    onDone = {
                        navController.navigate(Destinations.DASHBOARD) {
                            popUpTo(Destinations.DASHBOARD) { inclusive = true }
                        }
                    },
                )
            }
            composable(Destinations.CATEGORIES) { CategoriesScreen() }
            composable(Destinations.BUDGET) {
                BudgetScreen(onOpenComparisons = { navController.navigate(Destinations.COMPARISONS) })
            }
            composable(Destinations.GOALS) {
                GoalsScreen(onOpenGoal = { id -> navController.navigate(Destinations.goalDetail(id)) })
            }
            composable(
                route = Destinations.GOAL_DETAIL_PATTERN,
                arguments = listOf(navArgument("goalId") { type = NavType.StringType }),
            ) { entry ->
                val goalId = entry.arguments?.getString("goalId") ?: return@composable
                GoalDetailScreen(goalId = goalId, onBack = { navController.popBackStack() })
            }
            composable(Destinations.COMPARISONS) {
                ComparisonsScreen(onBack = { navController.popBackStack() })
            }
            composable(Destinations.SETTINGS) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

/** Dolny pasek — mirror 1:1 TabBar z PhoneFrame.tsx: 5 pozycji, aktywna w limonce, reszta wyciszona. */
@Composable
private fun BottomNavBar(currentRoute: String?, navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface)
            .padding(horizontal = 4.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        BOTTOM_NAV_ITEMS.forEach { item ->
            val selected = currentRoute == item.route
            val interactionSource = remember { MutableInteractionSource() }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {
                            if (!selected) {
                                navController.navigate(item.route) {
                                    popUpTo(Destinations.DASHBOARD) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = item.icon,
                    color = if (selected) Lime else TextMuted,
                    fontSize = 18.sp,
                )
                Text(
                    text = item.label.uppercase(),
                    color = if (selected) TextPrimary else TextMuted,
                    fontSize = 9.sp,
                    letterSpacing = 1.4.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}
