package pl.nullpointerstudio.zlotowka.ui.screens.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pl.nullpointerstudio.zlotowka.data.BudgetRepository
import pl.nullpointerstudio.zlotowka.data.CategoryEntity
import pl.nullpointerstudio.zlotowka.data.CategoryKind

/** Stan formularza kategorii — w trybie edycji wypełniany istniejącą kategorią po jej wczytaniu. */
data class CategoryFormUiState(
    val existing: CategoryEntity? = null,
    val loading: Boolean = false,
)

/**
 * ViewModel formularza "Nowa kategoria" / "Edytuj kategorię".
 * [categoryId] == null → tryb tworzenia; w przeciwnym razie tryb edycji istniejącej kategorii.
 */
class CategoryFormViewModel(
    private val repository: BudgetRepository,
    private val categoryId: String?,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryFormUiState(loading = categoryId != null))
    val uiState: StateFlow<CategoryFormUiState> = _uiState.asStateFlow()

    init {
        val id = categoryId
        if (id != null) {
            viewModelScope.launch {
                val existing = repository.categories.first().firstOrNull { it.id == id }
                _uiState.value = CategoryFormUiState(existing = existing, loading = false)
            }
        }
    }

    /** Zapisuje formularz — tworzy nową kategorię albo aktualizuje istniejącą (zachowując jej sortOrder). */
    suspend fun save(
        label: String,
        emoji: String,
        colorToken: String,
        monthlyBudgetMinor: Long,
        isImpulse: Boolean,
        kind: CategoryKind,
    ) {
        val id = categoryId
        if (id == null) {
            repository.addCategory(
                label = label,
                emoji = emoji,
                colorToken = colorToken,
                monthlyBudgetMinor = monthlyBudgetMinor,
                isImpulse = isImpulse,
                kind = kind,
            )
        } else {
            val originalSortOrder = uiState.value.existing?.sortOrder ?: 0
            repository.updateCategory(
                CategoryEntity(
                    id = id,
                    label = label,
                    emoji = emoji,
                    colorToken = colorToken,
                    monthlyBudgetMinor = monthlyBudgetMinor,
                    sortOrder = originalSortOrder,
                    isImpulse = isImpulse,
                    kind = kind,
                ),
            )
        }
    }

    /** Dostępne wyłącznie w trybie edycji. */
    suspend fun delete() {
        val id = requireNotNull(categoryId) { "delete() dostępne tylko w trybie edycji istniejącej kategorii" }
        repository.deleteCategory(id)
    }
}
