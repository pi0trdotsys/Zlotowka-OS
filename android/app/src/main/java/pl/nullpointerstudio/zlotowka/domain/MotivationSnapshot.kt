package pl.nullpointerstudio.zlotowka.domain

import pl.nullpointerstudio.zlotowka.data.CategoryEntity
import pl.nullpointerstudio.zlotowka.data.GoalEntity
import pl.nullpointerstudio.zlotowka.data.TransactionEntity
import kotlin.math.max
import kotlin.math.roundToLong

/**
 * Jedno miejsce prawdy o stanie motywacyjnym — używane przez Pulpit, widget na ekranie
 * głównym i powiadomienia, żeby liczby nigdy się nie rozjechały między powierzchniami.
 *
 * Budżet miesięczny/dzienny liczymy od 2026-08 wg planu: szacowane zarobki minus to, co
 * chcesz odłożyć na cele (suma `monthlyContribMinor` wszystkich celów) — a nie suma limitów
 * kategorii (te nadal służą do śledzenia "gdzie idą pieniądze" na ekranach Kategorie/Budżet).
 */
data class MotivationSnapshot(
    val score: Int,
    val streakDays: Int,
    val dailyLeftMinor: Long,
    val spentTodayMinor: Long,
    val monthBudgetMinor: Long,
    val monthSpentMinor: Long,
    val monthLeftMinor: Long,
    val dailyBudgetForRestOfMonthMinor: Long,
    val nextBadge: Pair<Badge, Int>?,
    val mascotMood: MascotMood,
    /** Szacowane miesięczne zarobki wpisane w Ustawieniach/na ekranie Budżet (grosze). */
    val estimatedIncomeMinor: Long = 0L,
    /** Suma `monthlyContribMinor` ze wszystkich celów — ile miesięcznie chcesz odłożyć. */
    val totalGoalContribMinor: Long = 0L,
    /** true, gdy użytkownik już wpisał szacowane zarobki — inaczej dzienny budżet nie ma sensu. */
    val hasIncomePlan: Boolean = false,
)

enum class MascotMood { THRIVING, HAPPY, NEUTRAL, WORRIED, ALARMED }

fun mascotMoodFor(score: Int): MascotMood = when {
    score >= 85 -> MascotMood.THRIVING
    score >= 65 -> MascotMood.HAPPY
    score >= 45 -> MascotMood.NEUTRAL
    score >= 25 -> MascotMood.WORRIED
    else -> MascotMood.ALARMED
}

fun buildMotivationSnapshot(
    categories: List<CategoryEntity>,
    transactions: List<TransactionEntity>,
    goals: List<GoalEntity> = emptyList(),
    estimatedIncomeMinor: Long = 0L,
    now: Long = System.currentTimeMillis(),
): MotivationSnapshot {
    val impulseIds = categories.filter { it.isImpulse }.map { it.id }.toHashSet()
    val streak = computeStreakDays(transactions, impulseIds, now)
    val spentByCategory = currentMonthExpenseByCategory(transactions, now)
    val monthTotals = currentMonthTotals(transactions, now)

    val totalGoalContrib = goals.sumOf { it.monthlyContribMinor }
    // Budżet = szacowane zarobki minus to, co ma pójść na cele — czyli ile realnie można wydać.
    val monthBudget = max(0L, estimatedIncomeMinor - totalGoalContrib)

    val adherence = budgetAdherence(categories, spentByCategory)
    val rate = savingsRate(monthTotals.incomeMinor, monthTotals.expenseMinor)
    val factor = streakFactor(streak)
    val score = savingScore(SavingScoreInputs(adherence, rate, factor))

    val today = weeklySpendSeries(transactions, now).lastOrNull { it.isToday }
    val spentToday = today?.totalMinor ?: 0L

    val monthLeft = max(0L, monthBudget - monthTotals.expenseMinor)
    val daysLeftInMonth = max(1, monthTotals.daysInMonth - monthTotals.dayOfMonth + 1)
    val dailyBudgetRest = (monthLeft.toDouble() / daysLeftInMonth).roundToLong()

    return MotivationSnapshot(
        score = score,
        streakDays = streak,
        dailyLeftMinor = max(0L, dailyBudgetRest - spentToday),
        spentTodayMinor = spentToday,
        monthBudgetMinor = monthBudget,
        monthSpentMinor = monthTotals.expenseMinor,
        monthLeftMinor = monthLeft,
        dailyBudgetForRestOfMonthMinor = dailyBudgetRest,
        nextBadge = nextBadge(streak),
        mascotMood = mascotMoodFor(score),
        estimatedIncomeMinor = estimatedIncomeMinor,
        totalGoalContribMinor = totalGoalContrib,
        hasIncomePlan = estimatedIncomeMinor > 0L,
    )
}
