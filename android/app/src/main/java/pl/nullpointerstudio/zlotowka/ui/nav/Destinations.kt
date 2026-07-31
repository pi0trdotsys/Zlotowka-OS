package pl.nullpointerstudio.zlotowka.ui.nav

/** Jedno źródło prawdy dla tras nawigacji — używane przez ekrany, widget (deep link) i powiadomienia. */
object Destinations {
    const val DASHBOARD = "dashboard"
    const val ADD_EXPENSE = "add"
    const val EDIT_EXPENSE_PATTERN = "edit_expense/{txId}"
    const val CATEGORIES = "categories"
    const val CATEGORY_FORM_NEW = "category_form"
    const val CATEGORY_FORM_EDIT_PATTERN = "category_form/{categoryId}"
    const val BUDGET = "budget"
    const val GOALS = "goals"
    const val GOAL_FORM_NEW = "goal_form"
    const val GOAL_FORM_EDIT_PATTERN = "goal_form/{goalId}"
    const val GOAL_DETAIL_PATTERN = "goal_detail/{goalId}"
    const val COMPARISONS = "comparisons"
    const val SETTINGS = "settings"

    fun editExpense(txId: String) = "edit_expense/$txId"
    fun categoryFormEdit(categoryId: String) = "category_form/$categoryId"
    fun goalFormEdit(goalId: String) = "goal_form/$goalId"
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
