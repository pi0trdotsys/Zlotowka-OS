package pl.nullpointerstudio.zlotowka.ui.screens.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.nullpointerstudio.zlotowka.data.BudgetRepository
import pl.nullpointerstudio.zlotowka.data.CategoryEntity
import pl.nullpointerstudio.zlotowka.data.ContributionSource
import pl.nullpointerstudio.zlotowka.data.GoalEntity
import pl.nullpointerstudio.zlotowka.domain.CutSuggestion
import pl.nullpointerstudio.zlotowka.domain.Milestone
import pl.nullpointerstudio.zlotowka.domain.MotivationSnapshot
import pl.nullpointerstudio.zlotowka.domain.currentMonthExpenseByCategory
import pl.nullpointerstudio.zlotowka.domain.currentMonthTotals
import pl.nullpointerstudio.zlotowka.domain.suggestionsForGoal
import kotlin.math.max

/** Stan ekranu Budżet — mirror ScreenBudget z Screens.tsx. */
data class BudgetUiState(
    val categories: List<CategoryEntity> = emptyList(),
    val spentByCategory: Map<String, Long> = emptyMap(),
    val snapshot: MotivationSnapshot? = null,
    val safeCategoryCount: Int = 0,
    val overCategoryCount: Int = 0,
    val mainGoal: GoalEntity? = null,
    val bestSuggestion: CutSuggestion? = null,
    val daysLeftInMonth: Int = 1,
    val loading: Boolean = true,
)

class BudgetViewModel(private val repository: BudgetRepository) : ViewModel() {

    val uiState: StateFlow<BudgetUiState> = combine(
        repository.categories,
        repository.transactions,
        repository.goals,
        repository.motivationSnapshot,
    ) { categories, transactions, goals, snapshot ->
        val spentByCategory = currentMonthExpenseByCategory(transactions)
        val withBudget = categories.filter { it.monthlyBudgetMinor > 0 }
        val safeCount = withBudget.count { (spentByCategory[it.id] ?: 0L).toDouble() / it.monthlyBudgetMinor <= 0.75 }
        val overCount = withBudget.count { (spentByCategory[it.id] ?: 0L) > it.monthlyBudgetMinor }
        val mainGoal = goals.filter { it.priority > 0 }.minByOrNull { it.priority }
        val bestSuggestion = mainGoal?.let {
            suggestionsForGoal(it, categories, spentByCategory, limit = 1).firstOrNull()
        }
        val monthTotals = currentMonthTotals(transactions)
        val daysLeft = max(1, monthTotals.daysInMonth - monthTotals.dayOfMonth + 1)

        BudgetUiState(
            categories = categories.sortedBy { it.sortOrder },
            spentByCategory = spentByCategory,
            snapshot = snapshot,
            safeCategoryCount = safeCount,
            overCategoryCount = overCount,
            mainGoal = mainGoal,
            bestSuggestion = bestSuggestion,
            daysLeftInMonth = daysLeft,
            loading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BudgetUiState(),
    )

    /** Odkłada resztę wolnego budżetu na cel główny; zwraca ewentualne nowo odblokowane kamienie milowe. */
    fun setAsideRemainder(goalId: String, amountMinor: Long, onUnlocked: (List<Milestone>) -> Unit) {
        if (amountMinor <= 0L) return
        viewModelScope.launch {
            val unlocked = repository.addContribution(goalId, amountMinor, ContributionSource.MANUAL)
            onUnlocked(unlocked)
        }
    }
}
