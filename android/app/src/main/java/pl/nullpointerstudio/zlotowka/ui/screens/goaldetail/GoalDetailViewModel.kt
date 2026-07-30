package pl.nullpointerstudio.zlotowka.ui.screens.goaldetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.nullpointerstudio.zlotowka.data.BudgetRepository
import pl.nullpointerstudio.zlotowka.data.ContributionEntity
import pl.nullpointerstudio.zlotowka.data.ContributionSource
import pl.nullpointerstudio.zlotowka.data.GoalEntity
import pl.nullpointerstudio.zlotowka.domain.GoalForecast
import pl.nullpointerstudio.zlotowka.domain.Milestone
import pl.nullpointerstudio.zlotowka.domain.MonthlyBucket
import pl.nullpointerstudio.zlotowka.domain.goalEta
import pl.nullpointerstudio.zlotowka.domain.goalEtaFromHistory
import pl.nullpointerstudio.zlotowka.domain.monthlyContribSeries

/** Stan ekranu Szczegóły celu — mirror ScreenGoalDetail z Screens.tsx. */
data class GoalDetailUiState(
    val goal: GoalEntity? = null,
    val history: List<ContributionEntity> = emptyList(),
    val monthlySeries: List<MonthlyBucket> = emptyList(),
    val forecast: GoalForecast? = null,
    val declaredEta: String = "—",
    val loading: Boolean = true,
)

class GoalDetailViewModel(
    private val repository: BudgetRepository,
    private val goalId: String,
) : ViewModel() {

    val uiState: StateFlow<GoalDetailUiState> = combine(
        repository.goals,
        repository.contributionsForGoal(goalId),
    ) { goals, contributions ->
        val goal = goals.firstOrNull { it.id == goalId }
        val sortedHistory = contributions.sortedByDescending { it.timestamp }
        GoalDetailUiState(
            goal = goal,
            history = sortedHistory,
            monthlySeries = monthlyContribSeries(contributions),
            forecast = goal?.let { goalEtaFromHistory(it, contributions) },
            declaredEta = goal?.let { goalEta(it) } ?: "—",
            loading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GoalDetailUiState(),
    )

    fun addContribution(amountMinor: Long, onUnlocked: (List<Milestone>) -> Unit) {
        viewModelScope.launch {
            val unlocked = repository.addContribution(goalId, amountMinor, ContributionSource.MANUAL)
            onUnlocked(unlocked)
        }
    }
}
