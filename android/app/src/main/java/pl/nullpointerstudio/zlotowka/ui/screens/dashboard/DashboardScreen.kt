package pl.nullpointerstudio.zlotowka.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.initializer
import pl.nullpointerstudio.zlotowka.ZlotowkaApp
import pl.nullpointerstudio.zlotowka.data.CategoryEntity
import pl.nullpointerstudio.zlotowka.data.PaymentMethod
import pl.nullpointerstudio.zlotowka.data.TransactionEntity
import pl.nullpointerstudio.zlotowka.domain.DayFlow
import pl.nullpointerstudio.zlotowka.domain.MotivationSnapshot
import pl.nullpointerstudio.zlotowka.domain.TransactionFilter
import pl.nullpointerstudio.zlotowka.domain.flowScales
import pl.nullpointerstudio.zlotowka.domain.toPln
import pl.nullpointerstudio.zlotowka.domain.toPlnShort
import pl.nullpointerstudio.zlotowka.domain.weekFlowTotals
import pl.nullpointerstudio.zlotowka.ui.components.AmountText
import pl.nullpointerstudio.zlotowka.ui.components.Pill
import pl.nullpointerstudio.zlotowka.ui.components.ProgressBar
import pl.nullpointerstudio.zlotowka.ui.components.SectionLabel
import pl.nullpointerstudio.zlotowka.ui.components.SurfaceCard
import pl.nullpointerstudio.zlotowka.ui.mascot.Mascot
import pl.nullpointerstudio.zlotowka.ui.nav.Destinations
import pl.nullpointerstudio.zlotowka.ui.theme.BorderOnDark
import pl.nullpointerstudio.zlotowka.ui.theme.Coral
import pl.nullpointerstudio.zlotowka.ui.theme.Cyan
import pl.nullpointerstudio.zlotowka.ui.theme.Lime
import pl.nullpointerstudio.zlotowka.ui.theme.Surface2
import pl.nullpointerstudio.zlotowka.ui.theme.TextMuted
import pl.nullpointerstudio.zlotowka.ui.theme.TextPrimary
import java.util.Calendar
import kotlin.math.abs

@Composable
fun DashboardScreen(onNavigate: (String) -> Unit, onOpenTransaction: (String) -> Unit) {
    val context = LocalContext.current
    val app = remember(context) { ZlotowkaApp.from(context) }
    val viewModel: DashboardViewModel = viewModel(
        factory = viewModelFactory { initializer { DashboardViewModel(app.repository) } },
    )
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionLabel(text = "Zostało do końca ${currentMonthGenitive()}")
            IconButton(onClick = { onNavigate(Destinations.SETTINGS) }) {
                Icon(Icons.Filled.Settings, contentDescription = "Ustawienia", tint = TextMuted)
            }
        }

        val snapshot = uiState.snapshot
        Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.Bottom) {
            AmountText(minor = snapshot?.monthLeftMinor ?: 0L, fontSize = 38.sp)
        }
        if (snapshot != null) {
            val deltaAbs = abs(uiState.dailyRateDeltaMinor).toPln()
            val moreOrLess = if (uiState.dailyRateDeltaMinor > 0) "więcej" else "mniej"
            Text(
                text = "To ${snapshot.dailyBudgetForRestOfMonthMinor.toPln()}/dzień — o $deltaAbs $moreOrLess niż w poprzednim miesiącu.",
                color = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        if (snapshot != null) {
            PulseCard(snapshot = snapshot, modifier = Modifier.padding(top = 20.dp))
        }

        FilterChips(
            selected = uiState.filter,
            onSelect = viewModel::setFilter,
            modifier = Modifier.padding(top = 20.dp),
        )

        if (uiState.weeklyFlow.isNotEmpty()) {
            FlowHistogram(
                weeklyFlow = uiState.weeklyFlow,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        Column(modifier = Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (uiState.filteredTransactions.isEmpty()) {
                Text(
                    text = "Brak transakcji do pokazania.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            } else {
                uiState.filteredTransactions.forEach { tx ->
                    val category = uiState.categories.firstOrNull { it.id == tx.categoryId }
                    RecentTransactionRow(tx, category, onClick = { onOpenTransaction(tx.id) })
                }
            }
        }
    }
}

@Composable
private fun FilterChips(
    selected: TransactionFilter,
    onSelect: (TransactionFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            label = "Wszystko",
            isSelected = selected == TransactionFilter.ALL,
            onClick = { onSelect(TransactionFilter.ALL) },
        )
        FilterChip(
            label = "Wydatki",
            isSelected = selected == TransactionFilter.EXPENSE,
            onClick = { onSelect(TransactionFilter.EXPENSE) },
        )
        FilterChip(
            label = "Dochody",
            isSelected = selected == TransactionFilter.INCOME,
            onClick = { onSelect(TransactionFilter.INCOME) },
        )
    }
}

@Composable
private fun FilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(modifier = Modifier.clickable(onClick = onClick)) {
        Pill(
            text = label,
            accent = if (isSelected) Lime else TextMuted,
            filled = isSelected,
        )
    }
}

@Composable
private fun PulseCard(snapshot: MotivationSnapshot, modifier: Modifier = Modifier) {
    SurfaceCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        SectionLabel(text = "Puls oszczędzania")
                        Text("${snapshot.score}/100", color = Lime, fontSize = 13.sp)
                    }
                    ProgressBar(
                        progress = snapshot.score / 100f,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                    val badge = snapshot.nextBadge
                    val streakText = if (badge != null) {
                        "🔥 ${snapshot.streakDays} dni bez wydatku impulsowego. Jeszcze ${badge.second} i odblokujesz odznakę „${badge.first.name}”."
                    } else {
                        "🔥 ${snapshot.streakDays} dni bez wydatku impulsowego. Wszystkie odznaki odblokowane!"
                    }
                    Text(
                        text = streakText,
                        color = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
                Mascot(mood = snapshot.mascotMood, size = 56.dp, modifier = Modifier.padding(start = 12.dp))
            }
        }
    }
}

/**
 * Histogram przepływu — mirror 1:1 FlowHistogram z Screens.tsx: wpływy nad osią,
 * wydatki pod osią, osobne skale dla każdego kierunku (żeby jedna wypłata nie
 * spłaszczyła wszystkich słupków wydatków), oś dnia dzisiejszego na cyan.
 */
@Composable
private fun FlowHistogram(weeklyFlow: List<DayFlow>, modifier: Modifier = Modifier) {
    val scales = remember(weeklyFlow) { flowScales(weeklyFlow) }
    val totals = remember(weeklyFlow) { weekFlowTotals(weeklyFlow) }
    val worstExpense = remember(weeklyFlow) { weeklyFlow.maxOfOrNull { it.expenseMinor } ?: 0L }
    val today = weeklyFlow.lastOrNull { it.isToday } ?: weeklyFlow.last()
    val barMax = 56.dp

    SurfaceCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionLabel(text = "Przepływ")
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Lime)) { append("+${totals.incomeMinor.toPlnShort()}") }
                        withStyle(SpanStyle(color = TextMuted)) { append(" · ") }
                        withStyle(SpanStyle(color = Coral)) { append("−${totals.expenseMinor.toPlnShort()}") }
                    },
                    fontSize = 12.sp,
                )
            }

            Row(
                modifier = Modifier.padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                LegendDot(color = Lime, label = "wpływy")
                LegendDot(color = Coral, label = "wydatki")
                Text(
                    text = "ostatnie 7 dni",
                    color = TextMuted,
                    fontSize = 9.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End,
                )
            }

            Row(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                weeklyFlow.forEach { day ->
                    val inFraction = (day.incomeMinor.toFloat() / scales.incomeMax.toFloat()).coerceIn(0f, 1f)
                    val outFraction = (day.expenseMinor.toFloat() / scales.expenseMax.toFloat()).coerceIn(0f, 1f)
                    val isWorst = day.expenseMinor > 0 && day.expenseMinor == worstExpense

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // wpływy — nad osią
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(barMax),
                            contentAlignment = Alignment.BottomCenter,
                        ) {
                            if (day.incomeMinor > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height((barMax * inFraction).coerceAtLeast(3.dp))
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(Lime.copy(alpha = 0.9f)),
                                )
                            }
                        }
                        // oś zera
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(if (day.isToday) Cyan else BorderOnDark),
                        )
                        // wydatki — pod osią
                        Box(modifier = Modifier.fillMaxWidth().height(barMax)) {
                            if (day.expenseMinor > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height((barMax * outFraction).coerceAtLeast(3.dp))
                                        .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                                        .background(if (isWorst) Coral else Coral.copy(alpha = 0.55f)),
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .then(
                                    if (day.isToday) Modifier.background(Cyan.copy(alpha = 0.15f)) else Modifier,
                                )
                                .padding(horizontal = 5.dp, vertical = 1.dp),
                        ) {
                            Text(
                                text = day.dayLabel,
                                color = if (day.isToday) Cyan else TextMuted,
                                fontSize = 9.sp,
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
                    .background(BorderOnDark)
                    .height(1.dp),
            ) {}

            Row(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FlowStatChip(
                    label = "Bilans tygodnia",
                    valueMinor = totals.balanceMinor,
                    modifier = Modifier.weight(1f),
                )
                FlowStatChip(
                    label = "Dziś",
                    valueMinor = today.incomeMinor - today.expenseMinor,
                    modifier = Modifier.weight(1f),
                )
            }

            Text(
                text = "Skale góra/dół są niezależne — dzień wypłaty nie spłaszcza wydatków.",
                color = TextMuted,
                fontSize = 9.sp,
                lineHeight = 12.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(width = 10.dp, height = 6.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(color),
        )
        Text(text = label, color = TextMuted, fontSize = 9.sp, modifier = Modifier.padding(start = 4.dp))
    }
}

@Composable
private fun FlowStatChip(label: String, valueMinor: Long, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Surface2.copy(alpha = 0.6f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        Column {
            Text(text = label, color = TextMuted, fontSize = 10.sp)
            Text(
                text = valueMinor.toPln(withSign = true),
                color = if (valueMinor >= 0) Lime else Coral,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun RecentTransactionRow(tx: TransactionEntity, category: CategoryEntity?, onClick: () -> Unit) {
    SurfaceCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = category?.emoji ?: "💸", fontSize = 16.sp)
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(text = tx.title, color = TextPrimary, fontSize = 12.sp, maxLines = 1)
                Text(text = methodLabel(tx.method), color = TextMuted, fontSize = 10.sp)
            }
            AmountText(
                minor = tx.amountMinor,
                withSign = true,
                fontSize = 13.sp,
                color = if (tx.amountMinor < 0) TextPrimary else Lime,
            )
        }
    }
}

private fun methodLabel(method: PaymentMethod): String = when (method) {
    PaymentMethod.BLIK -> "BLIK"
    PaymentMethod.CARD -> "Karta"
    PaymentMethod.CASH -> "Gotówka"
    PaymentMethod.TRANSFER -> "Przelew"
}

private val POLISH_MONTH_GENITIVE = listOf(
    "stycznia", "lutego", "marca", "kwietnia", "maja", "czerwca",
    "lipca", "sierpnia", "września", "października", "listopada", "grudnia",
)

private fun currentMonthGenitive(now: Long = System.currentTimeMillis()): String {
    val cal = Calendar.getInstance()
    cal.timeInMillis = now
    return POLISH_MONTH_GENITIVE[cal.get(Calendar.MONTH)]
}
