@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package pl.nullpointerstudio.zlotowka.ui.screens.goaldetail

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.initializer
import pl.nullpointerstudio.zlotowka.ZlotowkaApp
import pl.nullpointerstudio.zlotowka.data.ContributionEntity
import pl.nullpointerstudio.zlotowka.data.ContributionSource
import pl.nullpointerstudio.zlotowka.domain.QUICK_TOP_UPS_MINOR
import pl.nullpointerstudio.zlotowka.domain.goalPct
import pl.nullpointerstudio.zlotowka.domain.toPln
import pl.nullpointerstudio.zlotowka.notifications.NotificationChannels
import pl.nullpointerstudio.zlotowka.ui.components.AmountText
import pl.nullpointerstudio.zlotowka.ui.components.Pill
import pl.nullpointerstudio.zlotowka.ui.components.ProgressBar
import pl.nullpointerstudio.zlotowka.ui.components.SectionLabel
import pl.nullpointerstudio.zlotowka.ui.components.SurfaceCard
import pl.nullpointerstudio.zlotowka.ui.theme.Coral
import pl.nullpointerstudio.zlotowka.ui.theme.Lime
import pl.nullpointerstudio.zlotowka.ui.theme.TextMuted
import pl.nullpointerstudio.zlotowka.ui.theme.TextPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun GoalDetailScreen(goalId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val app = remember(context) { ZlotowkaApp.from(context) }
    val viewModel: GoalDetailViewModel = viewModel(
        factory = viewModelFactory { initializer { GoalDetailViewModel(app.repository, goalId) } },
    )
    val uiState by viewModel.uiState.collectAsState()
    val goal = uiState.goal

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(text = goal?.label ?: "Szczegóły celu", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = TextPrimary,
                ),
            )
        },
    ) { padding ->
        if (goal == null) return@Scaffold
        val pct = goalPct(goal)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = goal.label, color = TextPrimary, fontSize = 14.sp)
                Text(text = "$pct%", color = Lime, fontSize = 12.sp)
            }
            AmountText(minor = goal.savedMinor, fontSize = 30.sp, color = Lime, modifier = Modifier.padding(top = 6.dp))
            Text(
                text = "z ${goal.targetMinor.toPln()} · termin ${formatDeadline(goal.deadline)}",
                color = TextMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
            ProgressBar(progress = pct / 100f, modifier = Modifier.padding(top = 10.dp), height = 8.dp)

            val forecast = uiState.forecast
            if (forecast != null) {
                val faster = forecast.drift > 0
                SurfaceCard(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionLabel(text = "Prognoza ukończenia")
                        Text(text = forecast.eta, color = TextPrimary, fontSize = 18.sp, modifier = Modifier.padding(top = 6.dp))
                        Text(
                            text = "Realne tempo ${forecast.rateMinor.toPln()}/mies. · plan ${goal.monthlyContribMinor.toPln()} (${uiState.declaredEta})",
                            color = TextMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        Text(
                            text = if (faster) {
                                "Wpłacasz o ${forecast.drift.toPln()} więcej niż zakładałeś — prognoza przyspiesza."
                            } else {
                                "Wpłacasz o ${abs(forecast.drift).toPln()} mniej niż plan — prognoza się cofa."
                            },
                            color = if (faster) Lime else Coral,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }

            if (uiState.monthlySeries.isNotEmpty()) {
                SectionLabel(text = "Wpłaty miesięcznie", modifier = Modifier.padding(top = 20.dp))
                val maxSeries = uiState.monthlySeries.maxOfOrNull { abs(it.totalMinor) }?.coerceAtLeast(1L) ?: 1L
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .height(80.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    uiState.monthlySeries.forEach { bucket ->
                        val fraction = (abs(bucket.totalMinor).toFloat() / maxSeries.toFloat()).coerceIn(0.05f, 1f)
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp * fraction)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(if (bucket.totalMinor < 0) Coral else Lime),
                            )
                            Text(bucket.label, color = TextMuted, fontSize = 9.sp, modifier = Modifier.padding(top = 6.dp))
                        }
                    }
                }
            }

            SectionLabel(text = "Historia wpłat", modifier = Modifier.padding(top = 20.dp))
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                uiState.history.forEach { contribution -> ContributionRow(contribution) }
            }

            Row(
                modifier = Modifier.padding(top = 16.dp),
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
                            viewModel.addContribution(amount) { unlocked ->
                                unlocked.forEach { milestone ->
                                    NotificationChannels.notifyMilestoneUnlocked(context, goal.label, milestone)
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
private fun ContributionRow(contribution: ContributionEntity) {
    SurfaceCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = if (contribution.amountMinor < 0) "↩" else "↑", fontSize = 15.sp, color = TextPrimary)
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(text = contribution.note ?: sourceLabel(contribution.source), color = TextPrimary, fontSize = 12.sp, maxLines = 1)
                Text(
                    text = "${formatContributionDate(contribution.timestamp)} · ${sourceLabel(contribution.source)}",
                    color = TextMuted,
                    fontSize = 10.sp,
                )
            }
            AmountText(
                minor = contribution.amountMinor,
                withSign = true,
                fontSize = 13.sp,
                color = if (contribution.amountMinor < 0) Coral else Lime,
            )
        }
    }
}

private fun sourceLabel(source: ContributionSource): String = when (source) {
    ContributionSource.MANUAL -> "Ręcznie"
    ContributionSource.AUTO -> "Auto"
    ContributionSource.ROUNDUP -> "Zaokrąglenie"
    ContributionSource.CHALLENGE -> "Wyzwanie"
    ContributionSource.CUT -> "Cięcie"
}

private fun formatContributionDate(epochMillis: Long): String =
    SimpleDateFormat("yyyy-MM-dd", Locale("pl", "PL")).format(Date(epochMillis))

private fun formatDeadline(epochMillis: Long): String =
    SimpleDateFormat("dd.MM.yyyy", Locale("pl", "PL")).format(Date(epochMillis))
