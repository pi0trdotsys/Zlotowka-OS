package pl.nullpointerstudio.zlotowka.domain

import pl.nullpointerstudio.zlotowka.data.CategoryEntity
import pl.nullpointerstudio.zlotowka.data.ContributionEntity
import pl.nullpointerstudio.zlotowka.data.GoalEntity
import java.util.Calendar
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Szybkie dorzucenia do celu (grosze) — identyczne z quickTopUps w mock.ts. */
val QUICK_TOP_UPS_MINOR = listOf(1_000L, 2_000L, 5_000L)

data class Milestone(val pct: Int, val reward: String, val unlocked: Boolean, val unlockedAt: Long? = null)

data class CutSuggestion(
    val categoryId: String,
    val icon: String,
    val label: String,
    val cutMinor: Long,
    val weeksSaved: Int,
    val hint: String,
)

data class GoalForecast(val eta: String, val rateMinor: Long, val drift: Long)

private val MILESTONE_REWARDS = mapOf(
    25 to "Pierwsza ćwiartka",
    50 to "Półmetek",
    75 to "Ostatnia prosta",
    100 to "Cel domknięty",
)
val MILESTONE_THRESHOLDS = listOf(25, 50, 75, 100)

fun goalPct(goal: GoalEntity): Int {
    if (goal.targetMinor <= 0) return 0
    return min(100, ((goal.savedMinor.toDouble() / goal.targetMinor) * 100).roundToInt())
}

fun milestonesFor(goal: GoalEntity): List<Milestone> {
    val pct = goalPct(goal)
    return MILESTONE_THRESHOLDS.map { p ->
        Milestone(pct = p, reward = MILESTONE_REWARDS.getValue(p), unlocked = pct >= p)
    }
}

/** Ile miesięcy do celu przy obecnym (lub hipotetycznym) tempie odkładania. */
fun monthsToGoal(goal: GoalEntity, extraMonthlyMinor: Long = 0L): Double {
    val rate = goal.monthlyContribMinor + extraMonthlyMinor
    if (rate <= 0) return Double.POSITIVE_INFINITY
    return max(0.0, (goal.targetMinor - goal.savedMinor).toDouble() / rate)
}

/** Szacowana data osiągnięcia celu, np. "paź 2026". */
fun goalEta(goal: GoalEntity, extraMonthlyMinor: Long = 0L, from: Long = System.currentTimeMillis()): String {
    val months = monthsToGoal(goal, extraMonthlyMinor)
    if (!months.isFinite()) return "—"
    val cal = Calendar.getInstance()
    cal.timeInMillis = from
    cal.add(Calendar.MONTH, ceil(months).toInt())
    return formatMonthYear(cal.timeInMillis)
}

/**
 * Sugestie „co zmniejszyć": kategorie z największym przekroczeniem / udziałem.
 * Cięcie = 100% nadwyżki ponad limit, a gdy w limicie — 15% wydatku (zaokrąglone do pełnych złotych).
 */
fun suggestionsForGoal(
    goal: GoalEntity,
    categories: List<CategoryEntity>,
    spentByCategory: Map<String, Long>,
    limit: Int = 3,
): List<CutSuggestion> {
    val baseMonths = monthsToGoal(goal)
    return categories
        .filter { it.monthlyBudgetMinor > 0 }
        .map { c ->
            val spent = spentByCategory[c.id] ?: 0L
            val over = max(0L, spent - c.monthlyBudgetMinor)
            val cutMinor = if (over > 0) roundToNearest100(over) else roundToNearest100((spent * 0.15).toLong())
            val weeksSaved = max(
                1,
                ((baseMonths - monthsToGoal(goal, cutMinor)) * 4.345).roundToInt(),
            )
            CutSuggestion(
                categoryId = c.id,
                icon = c.emoji,
                label = c.label,
                cutMinor = cutMinor,
                weeksSaved = weeksSaved,
                hint = if (over > 0) "przekroczone o ${over.toPln()}" else "−15% miesięcznie",
            )
        }
        .sortedByDescending { it.cutMinor }
        .take(limit)
}

private fun roundToNearest100(value: Long): Long = ((value + 50) / 100) * 100

/** Średnie realne tempo z ostatnich `months` miesięcy (grosze/mies.). */
fun actualMonthlyRate(contributions: List<ContributionEntity>, months: Int = 2): Long {
    if (contributions.isEmpty()) return 0L
    val sum = contributions.sumOf { it.amountMinor }
    return sum / months
}

/** Prognoza oparta na realnym tempie wpłat, a nie na deklarowanym. */
fun goalEtaFromHistory(goal: GoalEntity, contributions: List<ContributionEntity>): GoalForecast {
    val rate = actualMonthlyRate(contributions)
    val declared = goal.monthlyContribMinor
    val eta = if (rate > 0) goalEta(goal.copy(monthlyContribMinor = rate)) else "—"
    return GoalForecast(eta = eta, rateMinor = rate, drift = rate - declared)
}

data class MonthlyBucket(val label: String, val totalMinor: Long)

/** Miesięczne sumy wpłat — do mini-wykresu w szczegółach celu. */
fun monthlyContribSeries(contributions: List<ContributionEntity>): List<MonthlyBucket> {
    val buckets = linkedMapOf<String, Long>()
    val sorted = contributions.sortedBy { it.timestamp }
    val keyFmt = java.text.SimpleDateFormat("yyyy-MM")
    for (c in sorted) {
        val key = keyFmt.format(java.util.Date(c.timestamp))
        buckets[key] = (buckets[key] ?: 0L) + c.amountMinor
    }
    return buckets.entries.sortedBy { it.key }.map { (key, total) ->
        val date = keyFmt.parse(key) ?: java.util.Date()
        MonthlyBucket(label = formatMonthYear(date.time).substringBefore(" "), totalMinor = total)
    }
}
