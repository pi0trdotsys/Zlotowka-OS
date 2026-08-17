package pl.nullpointerstudio.zlotowka.ui.screens.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import pl.nullpointerstudio.zlotowka.data.BudgetRepository
import pl.nullpointerstudio.zlotowka.data.CategoryEntity
import pl.nullpointerstudio.zlotowka.domain.currentMonthExpenseByCategory
import pl.nullpointerstudio.zlotowka.domain.currentMonthIncomeByCategory

/** Stan ekranu Kategorie — mirror ScreenCategories z Screens.tsx, z osobnymi listami wydatków i dochodów. */
data class CategoriesUiState(
    val categories: List<CategoryEntity> = emptyList(),
    val spentByCategory: Map<String, Long> = emptyMap(),
    val receivedByCategory: Map<String, Long> = emptyMap(),
    val totalSpentMinor: Long = 0L,
    val totalReceivedMinor: Long = 0L,
    val loading: Boolean = true,
)

class CategoriesViewModel(private val repository: BudgetRepository) : ViewModel() {

    val uiState: StateFlow<CategoriesUiState> = combine(
        repository.categories,
        repository.transactions,
    ) { categories, transactions ->
        val spentByCategory = currentMonthExpenseByCategory(transactions)
        val receivedByCategory = currentMonthIncomeByCategory(transactions)
        CategoriesUiState(
            categories = categories.sortedBy { it.sortOrder },
            spentByCategory = spentByCategory,
            receivedByCategory = receivedByCategory,
            totalSpentMinor = spentByCategory.values.sum(),
            totalReceivedMinor = receivedByCategory.values.sum(),
            loading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CategoriesUiState(),
    )
}
