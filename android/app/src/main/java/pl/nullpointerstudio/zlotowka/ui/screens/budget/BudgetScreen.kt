package pl.nullpointerstudio.zlotowka.ui.screens.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.initializer
import pl.nullpointerstudio.zlotowka.ZlotowkaApp
import pl.nullpointerstudio.zlotowka.data.CategoryEntity
import pl.nullpointerstudio.zlotowka.domain.CutSuggestion
import pl.nullpointerstudio.zlotowka.domain.toPln
import pl.nullpointerstudio.zlotowka.notifications.NotificationChannels
import pl.nullpointerstudio.zlotowka.ui.components.AmountText
import pl.nullpointerstudio.zlotowka.ui.components.Pill
import pl.nullpointerstudio.zlotowka.ui.components.ProgressBar
import pl.nullpointerstudio.zlotowka.ui.components.SectionLabel
import pl.nullpointerstudio.zlotowka.ui.components.SurfaceCard
import pl.nullpointerstudio.zlotowka.ui.theme.ColorTone
import pl.nullpointerstudio.zlotowka.ui.theme.Coral
import pl.nullpointerstudio.zlotowka.ui.theme.Cyan
import pl.nullpointerstudio.zlotowka.ui.theme.Lime
import pl.nullpointerstudio.zlotowka.ui.theme.Surface
import pl.nullpointerstudio.zlotowka.ui.theme.TextMuted
import pl.nullpointerstudio.zlotowka.ui.theme.TextPrimary
import pl.nullpointerstudio.zlotowka.ui.theme.toColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun BudgetScreen(onOpenComparisons: () -> Unit) {
    val context = LocalContext.current
    val app = remember(context) { ZlotowkaApp.from(context) }
    val viewModel: BudgetViewModel = viewModel(
        factory = viewModelFactory { initializer { BudgetViewModel(app.repository) } },
    )
    val uiState by viewModel.uiState.collectAsState()
    val snapshot = uiState.snapshot
    var showIncomeDialog by remember { mutableStateOf(false) }
    var incomeInput by remember { mutableStateOf("") }

    if (showIncomeDialog) {
        AlertDialog(
            onDismissRequest = { showIncomeDialog = false },
            title = { Text("Szacowane miesięczne zarobki") },
            text = {
                OutlinedTextField(
                    value = incomeInput,
                    onValueChange = { input -> if (input.count { it == ',' || it == '.' } <= 1) incomeInput = input },
                    placeholder = { Text("np. 6000") },
                    suffix = { Text("zł") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Lime,
                        unfocusedBorderColor = Lime.copy(alpha = 0.4f),
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = Surface,
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setEstimatedIncome(parseAmountToMinor(incomeInput))
                    showIncomeDialog = false
                }) { Text("Zapisz", color = Lime) }
            },
            dismissButton = {
                TextButton(onClick = { showIncomeDialog = false }) { Text("Anuluj") }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 24.dp),
    ) {
        SectionLabel(text = "Twój plan")
        SurfaceCard(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (snapshot != null && snapshot.hasIncomePlan) {
                    PlanRow(label = "Szacowane zarobki", value = snapshot.estimatedIncomeMinor.toPln())
                    PlanRow(label = "Odłożysz na cele", value = "${snapshot.totalGoalContribMinor.toPln()}/mies.")
                    PlanRow(
                        label = "Do wydania",
                        value = "${snapshot.dailyBudgetForRestOfMonthMinor.toPln()}/dzień",
                        valueColor = Lime,
                    )
                } else {
                    Text(
                        text = "Wpisz szacowane zarobki, żeby zobaczyć ile możesz wydawać dziennie po odłożeniu na cele.",
                        color = TextMuted,
                        fontSize = 12.sp,
                    )
                }
                Pill(
                    text = if (snapshot?.hasIncomePlan == true) "Edytuj zarobki" else "Ustaw zarobki",
                    accent = Lime,
                    filled = true,
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .clickable {
                            incomeInput = ((snapshot?.estimatedIncomeMinor ?: 0L) / 100.0).let {
                                if (it == 0.0) "" else it.toString().removeSuffix(".0")
                            }
                            showIncomeDialog = true
                        },
                )
            }
        }

        SectionLabel(text = "${currentFullMonthYear()} · plan miesięczny", modifier = Modifier.padding(top = 20.dp))

        Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.Bottom) {
            AmountText(minor = snapshot?.monthLeftMinor ?: 0L, fontSize = 34.sp)
            Text(
                text = "  wolne",
                color = TextMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }

        val usedPct = if (snapshot != null && snapshot.monthBudgetMinor > 0) {
            ((snapshot.monthSpentMinor.toDouble() / snapshot.monthBudgetMinor) * 100).roundToInt()
        } else 0
        val overUsed = usedPct > 90

        Text(
            text = "Wykorzystano $usedPct% z ${(snapshot?.monthBudgetMinor ?: 0L).toPln()} · " +
                "zostały ${uiState.daysLeftInMonth} dni.",
            color = TextMuted,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 6.dp),
        )

        ProgressBar(
            progress = usedPct / 100f,
            modifier = Modifier.padding(top = 10.dp),
            height = 8.dp,
            color = if (overUsed) Coral else Lime,
        )

        Row(
            modifier = Modifier.padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatChip(
                label = "W limicie",
                value = "${uiState.safeCategoryCount}/${uiState.categories.count { it.monthlyBudgetMinor > 0 }}",
                color = Lime,
                modifier = Modifier.weight(1f),
            )
            StatChip(
                label = "Przekroczone",
                value = "${uiState.overCategoryCount}",
                color = Coral,
                modifier = Modifier.weight(1f),
            )
            StatChip(
                label = "Dzienny luz",
                value = (snapshot?.dailyBudgetForRestOfMonthMinor ?: 0L).toPln(),
                color = Cyan,
                modifier = Modifier.weight(1f),
            )
        }

        Column(
            modifier = Modifier.padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            uiState.categories.take(4).forEach { category ->
                CompactCategoryRow(category, uiState.spentByCategory[category.id] ?: 0L)
            }
        }

        val suggestion = uiState.bestSuggestion
        val mainGoal = uiState.mainGoal
        if (suggestion != null && mainGoal != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Lime.copy(alpha = 0.1f))
                    .border(1.dp, Lime.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(12.dp),
            ) {
                SectionLabel(text = "Zachęta", color = Lime)
                Text(
                    text = "Zejdź z ${suggestion.label} o ${suggestion.cutMinor.toPln()}, a domkniesz miesiąc na plusie — " +
                        "to szybciej o ${suggestion.weeksSaved} tyg. w „${mainGoal.label}”.",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }

        Row(
            modifier = Modifier.padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Pill(
                text = "Odłóż resztę → cel",
                accent = Cyan,
                filled = true,
                modifier = Modifier.clickable(enabled = mainGoal != null && (snapshot?.monthLeftMinor ?: 0L) > 0) {
                    val goal = mainGoal ?: return@clickable
                    val amount = snapshot?.monthLeftMinor ?: return@clickable
                    viewModel.setAsideRemainder(goal.id, amount) { unlocked ->
                        unlocked.forEach { milestone ->
                            NotificationChannels.notifyMilestoneUnlocked(context, goal.label, milestone)
                        }
                    }
                },
            )
            Pill(
                text = "Puls ${snapshot?.score ?: 0}/100 · 🔥 ${snapshot?.streakDays ?: 0} dni",
                accent = TextMuted,
            )
        }

        Row(
            modifier = Modifier
                .padding(top = 10.dp)
                .clickable(onClick = onOpenComparisons),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Pill(text = "⇄ Porównania", accent = Cyan)
        }
    }
}

@Composable
private fun PlanRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color = TextPrimary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, color = TextMuted, fontSize = 12.sp)
        Text(text = value, color = valueColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

/** Parsuje wpisaną kwotę ("6000" lub "6000,50") na grosze. Nieprawidłowy wpis => 0. */
private fun parseAmountToMinor(input: String): Long {
    val normalized = input.trim().replace(',', '.')
    if (normalized.isEmpty()) return 0L
    val value = normalized.toDoubleOrNull() ?: return 0L
    return Math.round(value * 100)
}

@Composable
private fun StatChip(label: String, value: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    SurfaceCard(modifier = modifier) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)) {
            Text(text = label.uppercase(), color = TextMuted, fontSize = 9.sp, letterSpacing = 1.sp)
            Text(text = value, color = color, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun CompactCategoryRow(category: CategoryEntity, spentMinor: Long) {
    val budget = category.monthlyBudgetMinor
    val over = budget > 0 && spentMinor > budget
    val pct = if (budget > 0) ((spentMinor.toDouble() / budget) * 100).roundToInt() else 0
    val tone = category.colorToken.toColorTone().toColor()

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = category.emoji, fontSize = 13.sp)
        Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = category.label, color = TextPrimary, fontSize = 11.sp)
                Text(
                    text = if (over) "+${(spentMinor - budget).toPln()}" else (budget - spentMinor).toPln(),
                    color = if (over) Coral else tone,
                    fontSize = 11.sp,
                )
            }
            ProgressBar(
                progress = (pct.coerceIn(0, 100)) / 100f,
                modifier = Modifier.padding(top = 4.dp),
                height = 4.dp,
                color = if (over) Coral else tone,
            )
        }
    }
}

private fun String.toColorTone(): ColorTone =
    ColorTone.entries.firstOrNull { it.name.equals(this, ignoreCase = true) } ?: ColorTone.Muted

private fun currentFullMonthYear(now: Long = System.currentTimeMillis()): String {
    val fmt = SimpleDateFormat("LLLL yyyy", Locale("pl", "PL"))
    return fmt.format(Date(now)).replaceFirstChar { it.uppercase() }
}
