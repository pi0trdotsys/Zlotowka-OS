package pl.nullpointerstudio.zlotowka.ui.screens.comparisons

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.initializer
import pl.nullpointerstudio.zlotowka.ZlotowkaApp
import pl.nullpointerstudio.zlotowka.domain.CategoryComparison
import pl.nullpointerstudio.zlotowka.domain.DaySpend
import pl.nullpointerstudio.zlotowka.domain.PeriodComparison
import pl.nullpointerstudio.zlotowka.domain.WeekdayAverage
import pl.nullpointerstudio.zlotowka.domain.toPln
import pl.nullpointerstudio.zlotowka.ui.components.AmountText
import pl.nullpointerstudio.zlotowka.ui.components.Pill
import pl.nullpointerstudio.zlotowka.ui.components.SectionLabel
import pl.nullpointerstudio.zlotowka.ui.components.SurfaceCard
import pl.nullpointerstudio.zlotowka.ui.theme.Coral
import pl.nullpointerstudio.zlotowka.ui.theme.Cyan
import pl.nullpointerstudio.zlotowka.ui.theme.Lime
import pl.nullpointerstudio.zlotowka.ui.theme.OnLime
import pl.nullpointerstudio.zlotowka.ui.theme.Surface2
import pl.nullpointerstudio.zlotowka.ui.theme.TextMuted
import pl.nullpointerstudio.zlotowka.ui.theme.TextPrimary
import kotlin.math.abs

private enum class ComparisonTab { WEEK, MONTH }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparisonsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = remember(context) { ZlotowkaApp.from(context) }
    val viewModel: ComparisonsViewModel = viewModel(
        factory = viewModelFactory { initializer { ComparisonsViewModel(app.repository) } },
    )
    val uiState by viewModel.uiState.collectAsState()

    var selectedTab by remember { mutableStateOf(ComparisonTab.WEEK) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Porównania", color = TextPrimary, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Wstecz",
                            tint = TextPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = TextPrimary,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            TabToggle(selectedTab, onSelect = { selectedTab = it })

            if (uiState.loading) {
                Text("Ładowanie danych…", color = TextMuted, fontSize = 13.sp)
                return@Column
            }

            when (selectedTab) {
                ComparisonTab.WEEK -> {
                    uiState.weekOverWeek?.let { WeekOverWeekCard(it) }
                    if (uiState.weeklySeries.isNotEmpty()) {
                        WeeklyBarsCard(uiState.weeklySeries)
                    }
                }
                ComparisonTab.MONTH -> {
                    uiState.monthOverMonth?.let { MonthOverMonthCard(it) }
                    if (uiState.categoryComparisons.isNotEmpty()) {
                        CategoryComparisonsCard(uiState.categoryComparisons)
                    }
                }
            }

            uiState.priciestWeekday?.let { PriciestWeekdayCard(it) }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun TabToggle(selected: ComparisonTab, onSelect: (ComparisonTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(999.dp))
            .background(Surface2),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        TabOption("Tydzień", selected == ComparisonTab.WEEK, Modifier.weight(1f)) { onSelect(ComparisonTab.WEEK) }
        TabOption("Miesiąc", selected == ComparisonTab.MONTH, Modifier.weight(1f)) { onSelect(ComparisonTab.MONTH) }
    }
}

@Composable
private fun TabOption(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) Lime else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (selected) OnLime else TextMuted,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun DeltaBadge(deltaMinor: Long, deltaPct: Int?, improved: Boolean) {
    val color = if (improved) Lime else Coral
    val arrow = if (deltaMinor <= 0) "↓" else "↑"
    val pctText = deltaPct?.let { " (${if (it >= 0) "+" else ""}$it%)" } ?: ""
    Text(
        text = "$arrow ${abs(deltaMinor).toPln()}$pctText",
        color = color,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
    )
}

@Composable
private fun PeriodComparisonBody(comparison: PeriodComparison) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                SectionLabel(comparison.currentLabel)
                AmountText(minor = comparison.currentExpenseMinor, fontSize = 26.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                SectionLabel(comparison.previousLabel)
                AmountText(minor = comparison.previousExpenseMinor, fontSize = 18.sp, color = TextMuted)
            }
        }
        DeltaBadge(comparison.deltaExpenseMinor, comparison.deltaExpensePct, comparison.improved)
    }
}

@Composable
private fun WeekOverWeekCard(comparison: PeriodComparison) {
    SurfaceCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            SectionLabel("Tydzień do tygodnia", color = TextMuted)
            Spacer(modifier = Modifier.height(12.dp))
            PeriodComparisonBody(comparison)
        }
    }
}

@Composable
private fun MonthOverMonthCard(comparison: PeriodComparison) {
    SurfaceCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            SectionLabel("Miesiąc do miesiąca", color = TextMuted)
            Spacer(modifier = Modifier.height(12.dp))
            PeriodComparisonBody(comparison)
        }
    }
}

@Composable
private fun WeeklyBarsCard(series: List<DaySpend>) {
    val maxValue = series.maxOfOrNull { it.totalMinor }?.coerceAtLeast(1L) ?: 1L
    val outlierIndex = series.indices.maxByOrNull { series[it].totalMinor }
    SurfaceCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            SectionLabel("Wydatki dzień po dniu", color = TextMuted)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                series.forEachIndexed { index, day ->
                    val fraction = (day.totalMinor.toFloat() / maxValue.toFloat()).coerceIn(0.03f, 1f)
                    val barColor = when {
                        day.isToday -> Cyan
                        outlierIndex == index && day.totalMinor > 0 -> Coral
                        else -> Surface2
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp * fraction)
                                .clip(RoundedCornerShape(6.dp))
                                .background(barColor),
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(day.dayLabel, color = TextMuted, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryComparisonsCard(categories: List<CategoryComparison>) {
    SurfaceCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            SectionLabel("Kategorie: ten miesiąc vs poprzedni", color = TextMuted)
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                categories.forEach { category -> CategoryComparisonRow(category) }
            }
        }
    }
}

@Composable
private fun CategoryComparisonRow(category: CategoryComparison) {
    val improved = category.deltaMinor <= 0
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Surface2),
                contentAlignment = Alignment.Center,
            ) {
                Text(category.icon, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(category.label, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(
                    "${category.previousMinor.toPln()} → ${category.currentMinor.toPln()}",
                    color = TextMuted,
                    fontSize = 12.sp,
                )
            }
        }
        DeltaBadge(category.deltaMinor, category.deltaPct, improved)
    }
}

@Composable
private fun PriciestWeekdayCard(weekday: WeekdayAverage) {
    SurfaceCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            SectionLabel("Wgląd", color = TextMuted)
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Pill(text = weekday.dayLabel, accent = Coral, filled = true)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Twój najdroższy dzień tygodnia to ${weekday.dayLabel}, średnio ${weekday.averageMinor.toPln()}.",
                    color = TextPrimary,
                    fontSize = 13.sp,
                )
            }
        }
    }
}
