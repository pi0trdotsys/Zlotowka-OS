package pl.nullpointerstudio.zlotowka.domain

import pl.nullpointerstudio.zlotowka.data.TransactionEntity

/**
 * Przytyk kontekstowy przy dodawaniu wydatku: jeśli w bieżącym tygodniu są już >= [threshold]
 * wydatków w tej samej kategorii, pokazujemy alternatywny koszt (ile zostałoby, gdyby ich nie było).
 */
data class RepeatedExpenseAlert(
    val occurrencesThisWeek: Int,
    val potentialSavingsMinor: Long,
)

fun repeatedExpenseAlert(
    transactions: List<TransactionEntity>,
    categoryId: String,
    now: Long = System.currentTimeMillis(),
    threshold: Int = 3,
): RepeatedExpenseAlert? {
    val weekStart = startOfWeek(now)
    val weekEnd = weekStart + 7L * 24 * 60 * 60 * 1000
    val matches = transactions.filter {
        it.categoryId == categoryId && it.amountMinor < 0 && it.timestamp in weekStart until weekEnd
    }
    if (matches.size < threshold) return null
    return RepeatedExpenseAlert(
        occurrencesThisWeek = matches.size,
        potentialSavingsMinor = matches.sumOf { -it.amountMinor },
    )
}
