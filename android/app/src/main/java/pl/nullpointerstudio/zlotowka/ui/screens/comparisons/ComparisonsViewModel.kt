package pl.nullpointerstudio.zlotowka.ui.screens.comparisons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import pl.nullpointerstudio.zlotowka.data.BudgetRepository
import pl.nullpointerstudio.zlotowka.domain.CategoryComparison
import pl.nullpointerstudio.zlotowka.domain.DaySpend
import pl.nullpointerstudio.zlotowka.domain.PeriodComparison
import pl.nullpointerstudio.zlotowka.domain.WeekdayAverage
import pl.nullpointerstudio.zlotowka.domain.categoryMonthOverMonth
import pl.nullpointerstudio.zlotowka.domain.monthOverMonth
import pl.nullpointerstudio.zlotowka.domain.priciestWeekday
import pl.nullpointerstudio.zlotowka.domain.weekOverWeek
import pl.nullpointerstudio.zlotowka.domain.weeklySpendSeries

/** Stan ekranu "Porównania" — rozbudowany system porównań: tydzień/tydzień, miesiąc/miesiąc,
 * kategoria/kategoria i najdroższy dzień tygodnia. */
data class ComparisonsUiState(
    val weekOverWeek: PeriodComparison?,
    val monthOverMonth: PeriodComparison?,
    val categoryComparisons: List<CategoryComparison>,
    val weeklySeries: List<DaySpend>,
    val priciestWeekday: WeekdayAverage?,
    val loading: Boolean = true,
)

class ComparisonsViewModel(private val repository: BudgetRepository) : ViewModel() {

    val uiState: StateFlow<ComparisonsUiState> = combine(
        repository.transactions,
        repository.categories,
    ) { transactions, categories ->
        ComparisonsUiState(
            weekOverWeek = weekOverWeek(transactions),
            monthOverMonth = monthOverMonth(transactions),
            categoryComparisons = categoryMonthOverMonth(transactions, categories),
            weeklySeries = weeklySpendSeries(transactions),
            priciestWeekday = priciestWeekday(transactions),
            loading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ComparisonsUiState(
            weekOverWeek = null,
            monthOverMonth = null,
            categoryComparisons = emptyList(),
            weeklySeries = emptyList(),
            priciestWeekday = null,
            loading = true,
        ),
    )
}
