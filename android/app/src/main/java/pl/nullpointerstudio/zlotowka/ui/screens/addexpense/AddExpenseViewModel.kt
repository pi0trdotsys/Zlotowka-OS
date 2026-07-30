package pl.nullpointerstudio.zlotowka.ui.screens.addexpense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import pl.nullpointerstudio.zlotowka.data.BudgetRepository
import pl.nullpointerstudio.zlotowka.data.CategoryEntity
import pl.nullpointerstudio.zlotowka.data.PaymentMethod
import pl.nullpointerstudio.zlotowka.data.TransactionEntity

/** Stan ekranu "Nowy wydatek" — mirror ScreenAdd z Screens.tsx. */
data class AddExpenseUiState(
    val categories: List<CategoryEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),
    val loading: Boolean = true,
)

class AddExpenseViewModel(private val repository: BudgetRepository) : ViewModel() {

    val uiState: StateFlow<AddExpenseUiState> = combine(
        repository.categories,
        repository.transactions,
    ) { categories, transactions ->
        AddExpenseUiState(
            categories = categories.sortedBy { it.sortOrder },
            transactions = transactions,
            loading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AddExpenseUiState(),
    )

    suspend fun saveExpense(
        title: String,
        categoryId: String,
        amountMinor: Long,
        method: PaymentMethod,
        note: String?,
    ) {
        repository.addTransaction(
            title = title,
            categoryId = categoryId,
            amountMinor = -amountMinor,
            method = method,
            note = note,
        )
    }
}
