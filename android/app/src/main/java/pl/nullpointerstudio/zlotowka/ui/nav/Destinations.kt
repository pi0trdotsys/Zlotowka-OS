package pl.nullpointerstudio.zlotowka.ui.nav

/** Jedno źródło prawdy dla tras nawigacji — używane przez ekrany, widget (deep link) i powiadomienia. */
object Destinations {
    const val DASHBOARD = "dashboard"
    const val ADD_EXPENSE = "add"
    const val CATEGORIES = "categories"
    const val BUDGET = "budget"
    const val GOALS = "goals"
    const val GOAL_DETAIL_PATTERN = "goal_detail/{goalId}"
    const val COMPARISONS = "comparisons"
    const val SETTINGS = "settings"

    fun goalDetail(goalId: String) = "goal_detail/$goalId"

    /** Klucz ekstra w Intencie startującym MainActivity z widgetu/powiadomienia. */
    const val EXTRA_ROUTE = "route"
}

data class BottomNavItem(val route: String, val label: String, val icon: String)

val BOTTOM_NAV_ITEMS = listOf(
    BottomNavItem(Destinations.DASHBOARD, "Pulpit", "◎"),
    BottomNavItem(Destinations.ADD_EXPENSE, "Dodaj", "＋"),
    BottomNavItem(Destinations.CATEGORIES, "Kategorie", "▤"),
    BottomNavItem(Destinations.BUDGET, "Budżet", "◈"),
    BottomNavItem(Destinations.GOALS, "Cele", "◆"),
)
