package pl.nullpointerstudio.zlotowka.ui.screens.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import pl.nullpointerstudio.zlotowka.data.BudgetRepository
import pl.nullpointerstudio.zlotowka.data.GoalEntity

/** Stan ekranu formularza celu (nowy/edycja) — mirror ScreenGoalForm z Screens.tsx. */
data class GoalFormUiState(
    val existingGoal: GoalEntity? = null,
    val isFirstGoal: Boolean = false,
    val loading: Boolean = true,
)

class GoalFormViewModel(
    private val repository: BudgetRepository,
    private val goalId: String?,
) : ViewModel() {

    val uiState: StateFlow<GoalFormUiState> = repository.goals.map { goals ->
        GoalFormUiState(
            existingGoal = goalId?.let { id -> goals.firstOrNull { it.id == id } },
            isFirstGoal = goals.isEmpty(),
            loading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GoalFormUiState(),
    )

    suspend fun save(
        label: String,
        targetMinor: Long,
        deadline: Long,
        monthlyContribMinor: Long,
        makeMain: Boolean,
    ) {
        if (goalId == null) {
            repository.addGoal(
                label = label,
                targetMinor = targetMinor,
                deadline = deadline,
                monthlyContribMinor = monthlyContribMinor,
                makeMain = makeMain,
            )
        } else {
            repository.updateGoal(
                goalId = goalId,
                label = label,
                targetMinor = targetMinor,
                deadline = deadline,
                monthlyContribMinor = monthlyContribMinor,
            )
            if (makeMain) {
                repository.setMainGoal(goalId)
            }
        }
    }

    suspend fun delete() {
        repository.deleteGoal(goalId!!)
    }
}
