package pl.nullpointerstudio.zlotowka.widget

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import pl.nullpointerstudio.zlotowka.domain.DayFlow
import pl.nullpointerstudio.zlotowka.domain.MotivationSnapshot
import pl.nullpointerstudio.zlotowka.domain.flowScales
import pl.nullpointerstudio.zlotowka.domain.toPln
import pl.nullpointerstudio.zlotowka.domain.toPlnShort
import pl.nullpointerstudio.zlotowka.domain.weekFlowTotals
import pl.nullpointerstudio.zlotowka.ui.MainActivity
import pl.nullpointerstudio.zlotowka.ui.nav.Destinations
import pl.nullpointerstudio.zlotowka.ui.theme.Coral
import pl.nullpointerstudio.zlotowka.ui.theme.Cyan
import pl.nullpointerstudio.zlotowka.ui.theme.Lime
import pl.nullpointerstudio.zlotowka.ui.theme.OnLime
import pl.nullpointerstudio.zlotowka.ui.theme.Surface
import pl.nullpointerstudio.zlotowka.ui.theme.Surface2
import pl.nullpointerstudio.zlotowka.ui.theme.TextMuted
import pl.nullpointerstudio.zlotowka.ui.theme.TextPrimary

/** Progi rozmiaru — cztery kubełki: 2x1 / 2x2 / 4x1 / 4x2, każdy z osobnym, maks. funkcjonalnym layoutem. */
private val WIDE_BREAKPOINT = 180.dp
private val TALL_BREAKPOINT = 70.dp

/** Cztery warianty stylu widgetu 2x1 — mirror Widget2x1Balance/Left/Spark/QuickAdd z Widget.tsx. Cykl przez „⟳". */
const val WIDGET_2X1_STYLE_COUNT = 4

private val CardBackground = ColorProvider(Surface)
private val TrackBackground = ColorProvider(Surface2)
private val LimeProvider = ColorProvider(Lime)
private val CoralProvider = ColorProvider(Coral)
private val CyanProvider = ColorProvider(Cyan)
private val OnLimeProvider = ColorProvider(OnLime)
private val TextPrimaryProvider = ColorProvider(TextPrimary)
private val TextMutedProvider = ColorProvider(TextMuted)

@Composable
fun WidgetContent(snapshot: MotivationSnapshot, weeklyFlow: List<DayFlow>, style2x1: Int) {
    val size = LocalSize.current
    val isWide = size.width >= WIDE_BREAKPOINT
    val isTall = size.height >= TALL_BREAKPOINT

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(CardBackground)
            .cornerRadius(if (isTall) 24.dp else 16.dp)
            .padding(if (isTall) 14.dp else 8.dp),
    ) {
        when {
            isWide && isTall -> WideBudgetStrip(snapshot, weeklyFlow)
            isWide && !isTall -> CompactWideStrip(snapshot)
            !isWide && isTall -> SmallPulse(snapshot)
            else -> Minimal2x1(snapshot, weeklyFlow, style2x1)
        }
    }
}

/** 4x2 — "Pasek dnia": ile zostało dziś, szybki dodaj, histogram wpływy/wydatki 7 dni, streak. */
@Composable
private fun WideBudgetStrip(snapshot: MotivationSnapshot, weeklyFlow: List<DayFlow>) {
    val context = LocalContext.current

    Column(modifier = GlanceModifier.fillMaxSize()) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically,
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = "DZIŚ ZOSTAŁO",
                    style = TextStyle(color = TextMutedProvider, fontSize = 10.sp, fontWeight = FontWeight.Medium),
                )
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = snapshot.dailyLeftMinor.toPln(),
                    style = TextStyle(color = TextPrimaryProvider, fontSize = 22.sp, fontWeight = FontWeight.Bold),
                )
            }
            QuickAddButton(context, size = 28.dp, fontSize = 16.sp)
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        if (weeklyFlow.isNotEmpty()) {
            DayFlowHistogramRow(weeklyFlow, barMax = 18.dp, modifier = GlanceModifier.fillMaxWidth())
        }

        Spacer(modifier = GlanceModifier.height(6.dp))

        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Text(
                text = "−${snapshot.spentTodayMinor.toPln()} dziś",
                modifier = GlanceModifier.defaultWeight(),
                style = TextStyle(color = TextMutedProvider, fontSize = 10.sp),
            )
            Text(
                text = "🔥 ${snapshot.streakDays} dni",
                style = TextStyle(color = LimeProvider, fontSize = 10.sp, fontWeight = FontWeight.Medium),
            )
        }
    }
}

/** Wiersz mini-słupków: wpływy nad osią, wydatki pod osią, każdy kierunek z własną skalą. */
@Composable
private fun DayFlowHistogramRow(weeklyFlow: List<DayFlow>, barMax: Dp, modifier: GlanceModifier = GlanceModifier) {
    val scales = flowScales(weeklyFlow)
    Row(modifier = modifier) {
        weeklyFlow.forEach { day ->
            val inFraction = (day.incomeMinor.toFloat() / scales.incomeMax.toFloat()).coerceIn(0f, 1f)
            val outFraction = (day.expenseMinor.toFloat() / scales.expenseMax.toFloat()).coerceIn(0f, 1f)
            Column(
                modifier = GlanceModifier.defaultWeight(),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
            ) {
                Box(
                    modifier = GlanceModifier.fillMaxWidth().height(barMax),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    if (day.incomeMinor > 0) {
                        Box(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .height(barHeight(barMax, inFraction))
                                .cornerRadius(2.dp)
                                .background(LimeProvider),
                        ) {}
                    }
                }
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(if (day.isToday) CyanProvider else TrackBackground),
                ) {}
                Box(modifier = GlanceModifier.fillMaxWidth().height(barMax)) {
                    if (day.expenseMinor > 0) {
                        Box(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .height(barHeight(barMax, outFraction))
                                .cornerRadius(2.dp)
                                .background(CoralProvider),
                        ) {}
                    }
                }
            }
        }
    }
}

private fun barHeight(max: Dp, fraction: Float): Dp {
    if (fraction <= 0f) return 0.dp
    val h = max * fraction
    return if (h < 2.dp) 2.dp else h
}

/** 4x1 — skondensowany pasek dnia w jednej linii: kwota, cienki pasek postępu, szybki dodaj. */
@Composable
private fun CompactWideStrip(snapshot: MotivationSnapshot) {
    val context = LocalContext.current
    val progress = monthProgress(snapshot)

    Row(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
                Text(
                    text = snapshot.dailyLeftMinor.toPln(),
                    style = TextStyle(color = TextPrimaryProvider, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                )
                Spacer(modifier = GlanceModifier.width(6.dp))
                Text(
                    text = "dziś",
                    style = TextStyle(color = TextMutedProvider, fontSize = 9.sp),
                )
            }
            Spacer(modifier = GlanceModifier.height(4.dp))
            LinearProgressIndicator(
                modifier = GlanceModifier.fillMaxWidth().height(3.dp),
                progress = progress,
                color = LimeProvider,
                backgroundColor = TrackBackground,
            )
        }
        Spacer(modifier = GlanceModifier.width(8.dp))
        QuickAddButton(context, size = 24.dp, fontSize = 14.sp)
    }
}

/** 2x2 — "Puls": wynik motywacyjny + trzy chipy szybkiego BLIK-a (10/20/50 zł). */
@Composable
private fun SmallPulse(snapshot: MotivationSnapshot) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        Text(
            text = "PULS",
            style = TextStyle(color = TextMutedProvider, fontSize = 10.sp, fontWeight = FontWeight.Medium),
        )
        Spacer(modifier = GlanceModifier.height(2.dp))
        Text(
            text = "${snapshot.score}",
            style = TextStyle(color = LimeProvider, fontSize = 30.sp, fontWeight = FontWeight.Bold),
        )
        Text(
            text = "na 100 pkt · 🔥${snapshot.streakDays}",
            style = TextStyle(color = TextMutedProvider, fontSize = 10.sp),
        )

        Spacer(modifier = GlanceModifier.defaultWeight())

        Row(modifier = GlanceModifier.fillMaxWidth()) {
            QuickAddChip("10", amountMinor = 1_000L, modifier = GlanceModifier.defaultWeight())
            Spacer(modifier = GlanceModifier.width(6.dp))
            QuickAddChip("20", amountMinor = 2_000L, modifier = GlanceModifier.defaultWeight())
            Spacer(modifier = GlanceModifier.width(6.dp))
            QuickAddChip("50", amountMinor = 5_000L, modifier = GlanceModifier.defaultWeight())
        }
    }
}

/**
 * 2x1 — cztery warianty jednowierszowe wg [style2x1] (0..3), mirror Widget2x1Balance/Left/Spark/QuickAdd
 * z Widget.tsx. Całość klika w Pulpit (poza chipami QuickAdd), a mały „⟳" w rogu cyklicznie zmienia wariant —
 * odpowiednik konfiguracji WidgetStyle z dokumentacji, bez osobnej Activity konfiguracyjnej.
 */
@Composable
private fun Minimal2x1(snapshot: MotivationSnapshot, weeklyFlow: List<DayFlow>, style2x1: Int) {
    val context = LocalContext.current
    Box(modifier = GlanceModifier.fillMaxSize()) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
        ) {
            when (style2x1 % WIDGET_2X1_STYLE_COUNT) {
                0 -> Widget2x1Balance(weeklyFlow)
                1 -> Widget2x1Left(snapshot)
                2 -> Widget2x1Spark(weeklyFlow)
                else -> Widget2x1QuickAdd(context)
            }
        }
        StyleCycleButton(modifier = GlanceModifier.padding(2.dp))
    }
}

/** 2x1 A — „Dzisiejszy bilans": wpłynęło vs wydane + wynik dnia. */
@Composable
private fun Widget2x1Balance(weeklyFlow: List<DayFlow>) {
    val today = weeklyFlow.lastOrNull { it.isToday } ?: weeklyFlow.lastOrNull()
    val balance = (today?.incomeMinor ?: 0L) - (today?.expenseMinor ?: 0L)
    Row(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = "BILANS DNIA",
                style = TextStyle(color = TextMutedProvider, fontSize = 8.sp, fontWeight = FontWeight.Medium),
            )
            Text(
                text = balance.toPln(withSign = true),
                style = TextStyle(
                    color = if (balance >= 0) LimeProvider else CoralProvider,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                ),
                maxLines = 1,
            )
        }
        Column(horizontalAlignment = Alignment.Horizontal.End) {
            Text(
                text = "+${(today?.incomeMinor ?: 0L).toPlnShort()}",
                style = TextStyle(color = LimeProvider, fontSize = 8.sp),
            )
            Text(
                text = "−${(today?.expenseMinor ?: 0L).toPlnShort()}",
                style = TextStyle(color = CoralProvider, fontSize = 8.sp),
            )
        }
    }
}

/** 2x1 B — „Zostało dziś" z cienkim paskiem dziennego limitu. */
@Composable
private fun Widget2x1Left(snapshot: MotivationSnapshot) {
    val fraction = if (snapshot.dailyBudgetForRestOfMonthMinor > 0) {
        (snapshot.spentTodayMinor.toFloat() / snapshot.dailyBudgetForRestOfMonthMinor.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    Column(modifier = GlanceModifier.fillMaxSize()) {
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Text(
                text = "ZOSTAŁO DZIŚ",
                modifier = GlanceModifier.defaultWeight(),
                style = TextStyle(color = TextMutedProvider, fontSize = 8.sp, fontWeight = FontWeight.Medium),
            )
            Text(
                text = "z ${snapshot.dailyBudgetForRestOfMonthMinor.toPlnShort()}",
                style = TextStyle(color = TextMutedProvider, fontSize = 8.sp),
            )
        }
        Text(
            text = snapshot.dailyLeftMinor.toPln(),
            style = TextStyle(color = TextPrimaryProvider, fontSize = 14.sp, fontWeight = FontWeight.Bold),
            maxLines = 1,
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        LinearProgressIndicator(
            modifier = GlanceModifier.fillMaxWidth().height(3.dp),
            progress = (1f - fraction).coerceIn(0f, 1f),
            color = LimeProvider,
            backgroundColor = TrackBackground,
        )
    }
}

/** 2x1 C — mikro-histogram 7 dni (wpływy nad osią, wydatki pod). */
@Composable
private fun Widget2x1Spark(weeklyFlow: List<DayFlow>) {
    val totals = weekFlowTotals(weeklyFlow)
    Row(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        if (weeklyFlow.isNotEmpty()) {
            DayFlowHistogramRow(weeklyFlow, barMax = 12.dp, modifier = GlanceModifier.defaultWeight().fillMaxHeight())
        }
        Spacer(modifier = GlanceModifier.width(6.dp))
        Column(horizontalAlignment = Alignment.Horizontal.End) {
            Text(text = "7 dni", style = TextStyle(color = TextMutedProvider, fontSize = 8.sp))
            Text(
                text = totals.balanceMinor.let { if (it >= 0) "+" else "−" } + kotlin.math.abs(totals.balanceMinor).toPlnShort(),
                style = TextStyle(
                    color = if (totals.balanceMinor >= 0) LimeProvider else CoralProvider,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
    }
}

/** 2x1 D — szybkie dodanie wydatku (BLIK-owe kwoty). */
@Composable
private fun Widget2x1QuickAdd(context: android.content.Context) {
    Row(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        Box(
            modifier = GlanceModifier
                .size(22.dp)
                .cornerRadius(11.dp)
                .background(LimeProvider)
                .clickable(
                    actionStartActivity(
                        Intent(context, MainActivity::class.java)
                            .putExtra(Destinations.EXTRA_ROUTE, Destinations.ADD_EXPENSE),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "＋", style = TextStyle(color = OnLimeProvider, fontSize = 13.sp, fontWeight = FontWeight.Bold))
        }
        Spacer(modifier = GlanceModifier.width(6.dp))
        QuickAddChip("10", amountMinor = 1_000L, modifier = GlanceModifier.defaultWeight())
        Spacer(modifier = GlanceModifier.width(4.dp))
        QuickAddChip("20", amountMinor = 2_000L, modifier = GlanceModifier.defaultWeight())
        Spacer(modifier = GlanceModifier.width(4.dp))
        QuickAddChip("50", amountMinor = 5_000L, modifier = GlanceModifier.defaultWeight())
    }
}

/** Mały „⟳" w rogu 2x1 — cyklicznie zmienia wariant widgetu, nie otwiera aplikacji. */
@Composable
private fun StyleCycleButton(modifier: GlanceModifier = GlanceModifier) {
    Box(
        modifier = modifier
            .size(14.dp)
            .cornerRadius(7.dp)
            .background(TrackBackground)
            .clickable(actionRunCallback<CycleWidgetStyleAction>()),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "⟳", style = TextStyle(color = TextMutedProvider, fontSize = 8.sp))
    }
}

@Composable
private fun QuickAddButton(context: android.content.Context, size: Dp, fontSize: TextUnit) {
    Box(
        modifier = GlanceModifier
            .size(size)
            .cornerRadius(size / 2)
            .background(LimeProvider)
            .clickable(
                actionStartActivity(
                    Intent(context, MainActivity::class.java)
                        .putExtra(Destinations.EXTRA_ROUTE, Destinations.ADD_EXPENSE),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "+",
            style = TextStyle(color = OnLimeProvider, fontSize = fontSize, fontWeight = FontWeight.Bold),
        )
    }
}

@Composable
private fun QuickAddChip(label: String, amountMinor: Long, modifier: GlanceModifier) {
    Box(
        modifier = modifier
            .background(TrackBackground)
            .cornerRadius(8.dp)
            .padding(start = 0.dp, top = 6.dp, end = 0.dp, bottom = 6.dp)
            .clickable(
                actionRunCallback<QuickAddAction>(
                    actionParametersOf(QuickAddAction.AMOUNT_KEY to amountMinor),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = TextStyle(
                color = TextPrimaryProvider,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            ),
        )
    }
}

private fun monthProgress(snapshot: MotivationSnapshot): Float =
    if (snapshot.monthBudgetMinor > 0) {
        (snapshot.monthSpentMinor.toFloat() / snapshot.monthBudgetMinor.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
