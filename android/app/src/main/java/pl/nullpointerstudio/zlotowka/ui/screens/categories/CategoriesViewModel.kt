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

/** Stan ekranu Kategorie — mirror ScreenCategories z Screens.tsx. */
data class CategoriesUiState(
    val categories: List<CategoryEntity> = emptyList(),
    val spentByCategory: Map<String, Long> = emptyMap(),
    val totalSpentMinor: Long = 0L,
    val loading: Boolean = true,
)

class CategoriesViewModel(private val repository: BudgetRepository) : ViewModel() {

    val uiState: StateFlow<CategoriesUiState> = combine(
        repository.categories,
        repository.transactions,
    ) { categories, transactions ->
        val spentByCategory = currentMonthExpenseByCategory(transactions)
        CategoriesUiState(
            categories = categories.sortedBy { it.sortOrder },
            spentByCategory = spentByCategory,
            totalSpentMinor = spentByCategory.values.sum(),
            loading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CategoriesUiState(),
    )
}
