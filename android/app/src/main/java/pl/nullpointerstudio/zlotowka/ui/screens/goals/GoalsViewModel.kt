package pl.nullpointerstudio.zlotowka.ui.screens.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.nullpointerstudio.zlotowka.data.BudgetRepository
import pl.nullpointerstudio.zlotowka.data.ContributionSource
import pl.nullpointerstudio.zlotowka.data.GoalEntity
import pl.nullpointerstudio.zlotowka.domain.CutSuggestion
import pl.nullpointerstudio.zlotowka.domain.Milestone
import pl.nullpointerstudio.zlotowka.domain.MotivationSnapshot
import pl.nullpointerstudio.zlotowka.domain.currentMonthExpenseByCategory
import pl.nullpointerstudio.zlotowka.domain.milestonesFor
import pl.nullpointerstudio.zlotowka.domain.suggestionsForGoal

/** Stan ekranu Cele — mirror ScreenGoals z Screens.tsx. */
data class GoalsUiState(
    val sortedGoals: List<GoalEntity> = emptyList(),
    val mainGoal: GoalEntity? = null,
    val mainGoalMilestones: List<Milestone> = emptyList(),
    val suggestions: List<CutSuggestion> = emptyList(),
    val snapshot: MotivationSnapshot? = null,
    val loading: Boolean = true,
)

class GoalsViewModel(private val repository: BudgetRepository) : ViewModel() {

    val uiState: StateFlow<GoalsUiState> = combine(
        repository.goals,
        repository.categories,
        repository.transactions,
        repository.motivationSnapshot,
    ) { goals, categories, transactions, snapshot ->
        val sorted = goals.sortedBy { it.priority }
        val mainGoal = sorted.firstOrNull()
        val spentByCategory = currentMonthExpenseByCategory(transactions)
        GoalsUiState(
            sortedGoals = sorted,
            mainGoal = mainGoal,
            mainGoalMilestones = mainGoal?.let { milestonesFor(it) } ?: emptyList(),
            suggestions = mainGoal?.let { suggestionsForGoal(it, categories, spentByCategory, limit = 3) } ?: emptyList(),
            snapshot = snapshot,
            loading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GoalsUiState(),
    )

    fun applyCutSuggestion(categoryId: String, cutMinor: Long, goalId: String) {
        viewModelScope.launch { repository.applyCutSuggestion(categoryId, cutMinor, goalId) }
    }

    fun addContribution(goalId: String, amountMinor: Long, onUnlocked: (List<Milestone>) -> Unit) {
        viewModelScope.launch {
            val unlocked = repository.addContribution(goalId, amountMinor, ContributionSource.MANUAL)
            onUnlocked(unlocked)
        }
    }
}
