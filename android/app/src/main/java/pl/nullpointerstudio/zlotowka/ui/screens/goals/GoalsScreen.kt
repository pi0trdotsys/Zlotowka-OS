package pl.nullpointerstudio.zlotowka.ui.screens.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.initializer
import pl.nullpointerstudio.zlotowka.ZlotowkaApp
import pl.nullpointerstudio.zlotowka.data.GoalEntity
import pl.nullpointerstudio.zlotowka.domain.CutSuggestion
import pl.nullpointerstudio.zlotowka.domain.Milestone
import pl.nullpointerstudio.zlotowka.domain.QUICK_TOP_UPS_MINOR
import pl.nullpointerstudio.zlotowka.domain.goalEta
import pl.nullpointerstudio.zlotowka.domain.goalPct
import pl.nullpointerstudio.zlotowka.domain.toPln
import pl.nullpointerstudio.zlotowka.notifications.NotificationChannels
import pl.nullpointerstudio.zlotowka.ui.components.AmountText
import pl.nullpointerstudio.zlotowka.ui.components.Pill
import pl.nullpointerstudio.zlotowka.ui.components.ProgressBar
import pl.nullpointerstudio.zlotowka.ui.components.SectionLabel
import pl.nullpointerstudio.zlotowka.ui.components.SurfaceCard
import pl.nullpointerstudio.zlotowka.ui.theme.Cyan
import pl.nullpointerstudio.zlotowka.ui.theme.Lime
import pl.nullpointerstudio.zlotowka.ui.theme.Surface
import pl.nullpointerstudio.zlotowka.ui.theme.Surface2
import pl.nullpointerstudio.zlotowka.ui.theme.TextMuted
import pl.nullpointerstudio.zlotowka.ui.theme.TextPrimary
import kotlin.math.min

@Composable
fun GoalsScreen(onOpenGoal: (String) -> Unit) {
    val context = LocalContext.current
    val app = remember(context) { ZlotowkaApp.from(context) }
    val viewModel: GoalsViewModel = viewModel(
        factory = viewModelFactory { initializer { GoalsViewModel(app.repository) } },
    )
    val uiState by viewModel.uiState.collectAsState()
    val mainGoal = uiState.mainGoal

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 24.dp),
    ) {
        if (mainGoal != null) {
            val pct = goalPct(mainGoal)
            SectionLabel(text = "Cel główny")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = mainGoal.label, color = TextPrimary, fontSize = 14.sp)
                Text(text = "$pct%", color = Lime, fontSize = 12.sp)
            }
            AmountText(minor = mainGoal.savedMinor, fontSize = 32.sp, color = Lime, modifier = Modifier.padding(top = 6.dp))
            Text(
                text = "z ${mainGoal.targetMinor.toPln()} · brakuje ${(mainGoal.targetMinor - mainGoal.savedMinor).toPln()}",
                color = TextMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
            ProgressBar(progress = pct / 100f, modifier = Modifier.padding(top = 10.dp), height = 8.dp)
            Text(
                text = "Przy ${mainGoal.monthlyContribMinor.toPln()}/mies. dojdziesz do ${goalEta(mainGoal)}.",
                color = TextMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 8.dp),
            )

            SectionLabel(text = "Mikro-nagrody", modifier = Modifier.padding(top = 20.dp))
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                uiState.mainGoalMilestones.forEach { milestone ->
                    MilestoneCell(milestone, modifier = Modifier.weight(1f))
                }
            }

            SectionLabel(text = "Co zmniejszyć, by przyspieszyć", modifier = Modifier.padding(top = 20.dp))
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                uiState.suggestions.forEach { suggestion ->
                    SuggestionRow(
                        suggestion = suggestion,
                        onApply = { viewModel.applyCutSuggestion(suggestion.categoryId, suggestion.cutMinor, mainGoal.id) },
                    )
                }
            }
        }

        SectionLabel(text = "Wszystkie cele", modifier = Modifier.padding(top = 20.dp))
        Column(
            modifier = Modifier.padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            uiState.sortedGoals.forEach { goal ->
                GoalSummaryCard(goal = goal, onClick = { onOpenGoal(goal.id) })
            }
        }

        if (mainGoal != null) {
            val streakDays = uiState.snapshot?.streakDays ?: 0
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Cyan.copy(alpha = 0.1f))
                    .border(1.dp, Cyan.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(16.dp),
            ) {
                SectionLabel(text = "Wyzwanie tygodnia", color = Cyan)
                Text(
                    text = "Nie zamawiaj jedzenia przez 7 dni → +180 zł do „${mainGoal.label}”.",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    repeat(7) { i ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (i < min(7, streakDays)) Cyan else Surface2),
                        )
                    }
                }
            }

            uiState.suggestions.firstOrNull()?.let { best ->
                Text(
                    text = "Tnij ${best.label.lowercase()} — cel wpada już ${goalEta(mainGoal, best.cutMinor)}.",
                    color = TextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            Row(
                modifier = Modifier.padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(text = "DORZUĆ", color = TextMuted, fontSize = 10.sp, letterSpacing = 1.sp)
                QUICK_TOP_UPS_MINOR.forEach { amount ->
                    Pill(
                        text = "+${amount.toPln()}",
                        accent = Lime,
                        filled = true,
                        modifier = Modifier.clickable {
                            viewModel.addContribution(mainGoal.id, amount) { unlocked ->
                                unlocked.forEach { milestone ->
                                    NotificationChannels.notifyMilestoneUnlocked(context, mainGoal.label, milestone)
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun MilestoneCell(milestone: Milestone, modifier: Modifier = Modifier) {
    val color = if (milestone.unlocked) Lime else TextMuted
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (milestone.unlocked) Lime.copy(alpha = 0.1f) else Surface)
            .border(1.dp, if (milestone.unlocked) Lime.copy(alpha = 0.5f) else androidx.compose.ui.graphics.Color(0x17FFFFFF), RoundedCornerShape(14.dp))
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = if (milestone.unlocked) "◆" else "◇", color = color, fontSize = 14.sp)
        Text(text = "${milestone.pct}%", color = TextMuted, fontSize = 9.sp, modifier = Modifier.padding(top = 4.dp))
        Text(
            text = milestone.reward,
            color = if (milestone.unlocked) TextPrimary else TextMuted,
            fontSize = 8.sp,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
private fun SuggestionRow(suggestion: CutSuggestion, onApply: () -> Unit) {
    SurfaceCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = suggestion.icon, fontSize = 16.sp)
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(text = "${suggestion.label} −${suggestion.cutMinor.toPln()}/mies.", color = TextPrimary, fontSize = 12.sp)
                Text(
                    text = "${suggestion.hint} · szybciej o ${suggestion.weeksSaved} tyg.",
                    color = TextMuted,
                    fontSize = 10.sp,
                )
            }
            Pill(text = "Zastosuj", accent = Cyan, filled = true, modifier = Modifier.clickable(onClick = onApply))
        }
    }
}

@Composable
private fun GoalSummaryCard(goal: GoalEntity, onClick: () -> Unit) {
    val pct = goalPct(goal)
    SurfaceCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = goal.label, color = TextPrimary, fontSize = 14.sp)
                Text(text = "$pct%", color = Lime, fontSize = 12.sp)
            }
            ProgressBar(progress = pct / 100f, modifier = Modifier.padding(top = 10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = goal.savedMinor.toPln(), color = TextMuted, fontSize = 10.sp)
                Text(text = "z ${goal.targetMinor.toPln()} · ${goalEta(goal)}", color = TextMuted, fontSize = 10.sp)
            }
        }
    }
}
