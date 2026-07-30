package pl.nullpointerstudio.zlotowka.domain

import pl.nullpointerstudio.zlotowka.data.CategoryEntity
import pl.nullpointerstudio.zlotowka.data.TransactionEntity
import java.util.Calendar
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Puls oszczędzania (0..100), liczony na podstawie:
 *  0.4 * przestrzeganie limitów kategorii
 *  0.3 * stopa oszczędzania (dochody - wydatki) / dochody
 *  0.3 * czynnik serii (streak / 30, max 1)
 */
data class SavingScoreInputs(
    val budgetAdherence: Double,
    val savingsRate: Double,
    val streakFactor: Double,
)

fun budgetAdherence(categories: List<CategoryEntity>, spentByCategory: Map<String, Long>): Double {
    val withBudget = categories.filter { it.monthlyBudgetMinor > 0 }
    if (withBudget.isEmpty()) return 1.0
    val inLimit = withBudget.count { (spentByCategory[it.id] ?: 0L) <= it.monthlyBudgetMinor }
    return inLimit.toDouble() / withBudget.size
}

fun savingsRate(incomeMinor: Long, expenseMinor: Long): Double {
    if (incomeMinor <= 0) return 0.0
    val rate = (incomeMinor - expenseMinor).toDouble() / incomeMinor
    return rate.coerceIn(0.0, 1.0)
}

fun streakFactor(streakDays: Int): Double = min(streakDays / 30.0, 1.0)

fun savingScore(inputs: SavingScoreInputs): Int {
    val raw = 0.4 * inputs.budgetAdherence + 0.3 * inputs.savingsRate + 0.3 * inputs.streakFactor
    return (raw * 100).roundToInt().coerceIn(0, 100)
}

data class Badge(val days: Int, val name: String)

val STREAK_BADGES = listOf(
    Badge(3, "Pierwszy Grosz"),
    Badge(7, "Oszczędny"),
    Badge(14, "Twarda Waluta"),
    Badge(30, "Żelazny Budżet"),
)

/** Najbliższa nieodblokowana odznaka i ile dni do niej brakuje; null = wszystkie odblokowane. */
fun nextBadge(streakDays: Int): Pair<Badge, Int>? {
    val next = STREAK_BADGES.firstOrNull { it.days > streakDays } ?: return null
    return next to (next.days - streakDays)
}

fun unlockedBadges(streakDays: Int): List<Badge> = STREAK_BADGES.filter { streakDays >= it.days }

/**
 * Seria (streak): liczba kolejnych dni (licząc wstecz od dziś) bez wydatku
 * w kategorii oznaczonej jako "impulsowa". Przerywa się w dniu z takim wydatkiem.
 */
fun computeStreakDays(
    transactions: List<TransactionEntity>,
    impulseCategoryIds: Set<String>,
    now: Long = System.currentTimeMillis(),
    maxLookbackDays: Int = 90,
): Int {
    if (impulseCategoryIds.isEmpty()) return 0
    val impulseDays = transactions
        .asSequence()
        .filter { it.amountMinor < 0 && it.categoryId in impulseCategoryIds }
        .map { dayKey(it.timestamp) }
        .toHashSet()

    var streak = 0
    val cal = Calendar.getInstance()
    cal.timeInMillis = now
    repeat(maxLookbackDays) {
        val key = dayKey(cal.timeInMillis)
        if (key in impulseDays) return streak
        streak++
        cal.add(Calendar.DAY_OF_YEAR, -1)
    }
    return streak
}

private fun dayKey(epochMillis: Long): Int {
    val cal = Calendar.getInstance()
    cal.timeInMillis = epochMillis
    return cal.get(Calendar.YEAR) * 1000 + cal.get(Calendar.DAY_OF_YEAR)
}
