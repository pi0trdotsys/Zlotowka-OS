package pl.nullpointerstudio.zlotowka.domain

import pl.nullpointerstudio.zlotowka.data.CategoryEntity
import pl.nullpointerstudio.zlotowka.data.TransactionEntity
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Rozbudowany system porównań: tydzień/tydzień, miesiąc/miesiąc, kategoria/kategoria
 * i analiza "najdroższego dnia tygodnia". Wszystko liczone z realnych transakcji,
 * bez trzymania zdenormalizowanych sum.
 */

private const val MS_PER_DAY = 24L * 60 * 60 * 1000

internal fun calendarAt(epochMillis: Long): Calendar =
    Calendar.getInstance().apply {
        firstDayOfWeek = Calendar.MONDAY
        timeInMillis = epochMillis
    }

internal fun startOfDay(cal: Calendar): Calendar = (cal.clone() as Calendar).apply {
    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
}

internal fun startOfWeek(epochMillis: Long): Long {
    val cal = startOfDay(calendarAt(epochMillis))
    // cofnij do poniedziałku
    val dow = cal.get(Calendar.DAY_OF_WEEK) // SUNDAY=1..SATURDAY=7
    val daysSinceMonday = ((dow - Calendar.MONDAY) + 7) % 7
    cal.add(Calendar.DAY_OF_YEAR, -daysSinceMonday)
    return cal.timeInMillis
}

internal fun startOfMonth(epochMillis: Long): Long {
    val cal = startOfDay(calendarAt(epochMillis))
    cal.set(Calendar.DAY_OF_MONTH, 1)
    return cal.timeInMillis
}

data class PeriodComparison(
    val currentLabel: String,
    val previousLabel: String,
    val currentExpenseMinor: Long,
    val previousExpenseMinor: Long,
    val currentIncomeMinor: Long,
    val previousIncomeMinor: Long,
) {
    val deltaExpenseMinor: Long get() = currentExpenseMinor - previousExpenseMinor
    val deltaExpensePct: Int? get() = if (previousExpenseMinor == 0L) null else
        ((deltaExpenseMinor.toDouble() / previousExpenseMinor) * 100).roundToInt()
    val improved: Boolean get() = deltaExpenseMinor <= 0
}

internal fun sumExpense(txs: List<TransactionEntity>, from: Long, to: Long): Long =
    txs.filter { it.timestamp in from until to && it.amountMinor < 0 }.sumOf { -it.amountMinor }

internal fun sumIncome(txs: List<TransactionEntity>, from: Long, to: Long): Long =
    txs.filter { it.timestamp in from until to && it.amountMinor > 0 }.sumOf { it.amountMinor }

fun weekOverWeek(transactions: List<TransactionEntity>, now: Long = System.currentTimeMillis()): PeriodComparison {
    val curStart = startOfWeek(now)
    val prevStart = curStart - 7 * MS_PER_DAY
    val curEnd = curStart + 7 * MS_PER_DAY
    return PeriodComparison(
        currentLabel = "Ten tydzień",
        previousLabel = "Poprzedni tydzień",
        currentExpenseMinor = sumExpense(transactions, curStart, curEnd),
        previousExpenseMinor = sumExpense(transactions, prevStart, curStart),
        currentIncomeMinor = sumIncome(transactions, curStart, curEnd),
        previousIncomeMinor = sumIncome(transactions, prevStart, curStart),
    )
}

fun monthOverMonth(transactions: List<TransactionEntity>, now: Long = System.currentTimeMillis()): PeriodComparison {
    val curStart = startOfMonth(now)
    val curCal = calendarAt(curStart)
    val prevCal = (curCal.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
    val nextCal = (curCal.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
    val prevStart = prevCal.timeInMillis
    val curEnd = nextCal.timeInMillis
    val monthFmt = java.text.SimpleDateFormat("LLLL", Locale("pl", "PL"))
    return PeriodComparison(
        currentLabel = monthFmt.format(curCal.time).replaceFirstChar { it.uppercase() },
        previousLabel = monthFmt.format(prevCal.time).replaceFirstChar { it.uppercase() },
        currentExpenseMinor = sumExpense(transactions, curStart, curEnd),
        previousExpenseMinor = sumExpense(transactions, prevStart, curStart),
        currentIncomeMinor = sumIncome(transactions, curStart, curEnd),
        previousIncomeMinor = sumIncome(transactions, prevStart, curStart),
    )
}

data class CategoryComparison(
    val categoryId: String,
    val label: String,
    val icon: String,
    val currentMinor: Long,
    val previousMinor: Long,
) {
    val deltaMinor: Long get() = currentMinor - previousMinor
    val deltaPct: Int? get() = if (previousMinor == 0L) null else
        ((deltaMinor.toDouble() / previousMinor) * 100).roundToInt()
}

/** Wydatki wg kategorii: bieżący miesiąc vs poprzedni, posortowane po wielkości zmiany. */
fun categoryMonthOverMonth(
    transactions: List<TransactionEntity>,
    categories: List<CategoryEntity>,
    now: Long = System.currentTimeMillis(),
): List<CategoryComparison> {
    val curStart = startOfMonth(now)
    val curCal = calendarAt(curStart)
    val prevStart = (curCal.clone() as Calendar).apply { add(Calendar.MONTH, -1) }.timeInMillis
    val curEnd = (curCal.clone() as Calendar).apply { add(Calendar.MONTH, 1) }.timeInMillis

    fun expenseByCategory(from: Long, to: Long): Map<String, Long> =
        transactions.asSequence()
            .filter { it.timestamp in from until to && it.amountMinor < 0 }
            .groupBy { it.categoryId }
            .mapValues { (_, v) -> v.sumOf { -it.amountMinor } }

    val current = expenseByCategory(curStart, curEnd)
    val previous = expenseByCategory(prevStart, curStart)

    return categories
        .filter { it.monthlyBudgetMinor > 0 }
        .map { c ->
            CategoryComparison(
                categoryId = c.id,
                label = c.label,
                icon = c.emoji,
                currentMinor = current[c.id] ?: 0L,
                previousMinor = previous[c.id] ?: 0L,
            )
        }
        .sortedByDescending { kotlin.math.abs(it.deltaMinor) }
}

data class DaySpend(val dayLabel: String, val totalMinor: Long, val isToday: Boolean)

private val PL_DAY_LABELS = listOf("Pn", "Wt", "Śr", "Cz", "Pt", "So", "Nd")

/** Wydatki dzień po dniu za ostatnie 7 dni (Pn..Nd wg kalendarza, jak wykres w Pulpicie). */
fun weeklySpendSeries(transactions: List<TransactionEntity>, now: Long = System.currentTimeMillis()): List<DaySpend> {
    val weekStart = startOfWeek(now)
    val todayKey = startOfDay(calendarAt(now)).timeInMillis
    return (0 until 7).map { i ->
        val dayStart = weekStart + i * MS_PER_DAY
        val dayEnd = dayStart + MS_PER_DAY
        DaySpend(
            dayLabel = PL_DAY_LABELS[i],
            totalMinor = sumExpense(transactions, dayStart, dayEnd),
            isToday = dayStart == todayKey,
        )
    }
}

data class WeekdayAverage(val dayLabel: String, val averageMinor: Long)

/** Średni wydatek per dzień tygodnia z całej historii — który dzień jest "najdroższy". */
fun averageSpendByWeekday(transactions: List<TransactionEntity>): List<WeekdayAverage> {
    val totals = LongArray(7)
    val counts = IntArray(7)
    for (t in transactions) {
        if (t.amountMinor >= 0) continue
        val cal = calendarAt(t.timestamp)
        val dow = cal.get(Calendar.DAY_OF_WEEK)
        val idx = ((dow - Calendar.MONDAY) + 7) % 7
        totals[idx] += -t.amountMinor
        counts[idx] += 1
    }
    return (0 until 7).map { i ->
        val avg = if (counts[i] == 0) 0L else totals[i] / counts[i]
        WeekdayAverage(PL_DAY_LABELS[i], avg)
    }
}

fun priciestWeekday(transactions: List<TransactionEntity>): WeekdayAverage? =
    averageSpendByWeekday(transactions).filter { it.averageMinor > 0 }.maxByOrNull { it.averageMinor }

/** Wydatki wg kategorii w bieżącym miesiącu kalendarzowym (do puli oszczędzania i sugestii cięć). */
fun currentMonthExpenseByCategory(transactions: List<TransactionEntity>, now: Long = System.currentTimeMillis()): Map<String, Long> {
    val curStart = startOfMonth(now)
    val curEnd = (calendarAt(curStart).clone() as Calendar).apply { add(Calendar.MONTH, 1) }.timeInMillis
    return transactions.asSequence()
        .filter { it.timestamp in curStart until curEnd && it.amountMinor < 0 }
        .groupBy { it.categoryId }
        .mapValues { (_, v) -> v.sumOf { -it.amountMinor } }
}

data class MonthTotals(val incomeMinor: Long, val expenseMinor: Long, val daysInMonth: Int, val dayOfMonth: Int)

fun currentMonthTotals(transactions: List<TransactionEntity>, now: Long = System.currentTimeMillis()): MonthTotals {
    val curStart = startOfMonth(now)
    val cal = calendarAt(curStart)
    val curEnd = (cal.clone() as Calendar).apply { add(Calendar.MONTH, 1) }.timeInMillis
    val dayOfMonth = calendarAt(now).get(Calendar.DAY_OF_MONTH)
    val daysInMonth = (cal.clone() as Calendar).getActualMaximum(Calendar.DAY_OF_MONTH)
    return MonthTotals(
        incomeMinor = sumIncome(transactions, curStart, curEnd),
        expenseMinor = sumExpense(transactions, curStart, curEnd),
        daysInMonth = daysInMonth,
        dayOfMonth = dayOfMonth,
    )
}
