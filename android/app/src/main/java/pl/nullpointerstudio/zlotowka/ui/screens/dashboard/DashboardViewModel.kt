package pl.nullpointerstudio.zlotowka.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import pl.nullpointerstudio.zlotowka.data.BudgetRepository
import pl.nullpointerstudio.zlotowka.data.CategoryEntity
import pl.nullpointerstudio.zlotowka.data.TransactionEntity
import pl.nullpointerstudio.zlotowka.domain.DaySpend
import pl.nullpointerstudio.zlotowka.domain.MotivationSnapshot
import pl.nullpointerstudio.zlotowka.domain.currentMonthTotals
import pl.nullpointerstudio.zlotowka.domain.monthOverMonth
import pl.nullpointerstudio.zlotowka.domain.weeklySpendSeries
import java.util.Calendar

/** Stan ekranu Pulpit — mirror ScreenDashboard z Screens.tsx. */
data class DashboardUiState(
    val snapshot: MotivationSnapshot? = null,
    val weeklySpend: List<DaySpend> = emptyList(),
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    /** Delta dziennej średniej wydatków vs poprzedni miesiąc, grosze; > 0 = wydajesz więcej/dzień. */
    val dailyRateDeltaMinor: Long = 0L,
    val loading: Boolean = true,
)

class DashboardViewModel(private val repository: BudgetRepository) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.categories,
        repository.transactions,
        repository.motivationSnapshot,
    ) { categories, transactions, snapshot ->
        val monthTotals = currentMonthTotals(transactions)
        val comparison = monthOverMonth(transactions)
        val prevDaysInMonth = daysInPreviousCalendarMonth()
        val currentDailyAvg = if (monthTotals.dayOfMonth > 0) monthTotals.expenseMinor / monthTotals.dayOfMonth else 0L
        val prevDailyAvg = if (prevDaysInMonth > 0) comparison.previousExpenseMinor / prevDaysInMonth else 0L

        DashboardUiState(
            snapshot = snapshot,
            weeklySpend = weeklySpendSeries(transactions),
            recentTransactions = transactions.sortedByDescending { it.timestamp }.take(3),
            categories = categories,
            dailyRateDeltaMinor = currentDailyAvg - prevDailyAvg,
            loading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(),
    )
}

private fun daysInPreviousCalendarMonth(now: Long = System.currentTimeMillis()): Int {
    val cal = Calendar.getInstance()
    cal.timeInMillis = now
    cal.add(Calendar.MONTH, -1)
    return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
}
