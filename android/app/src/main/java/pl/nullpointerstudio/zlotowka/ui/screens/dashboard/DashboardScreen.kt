package pl.nullpointerstudio.zlotowka.ui.screens.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.initializer
import pl.nullpointerstudio.zlotowka.ZlotowkaApp
import pl.nullpointerstudio.zlotowka.data.CategoryEntity
import pl.nullpointerstudio.zlotowka.data.PaymentMethod
import pl.nullpointerstudio.zlotowka.data.TransactionEntity
import pl.nullpointerstudio.zlotowka.domain.DaySpend
import pl.nullpointerstudio.zlotowka.domain.MotivationSnapshot
import pl.nullpointerstudio.zlotowka.domain.toPln
import pl.nullpointerstudio.zlotowka.ui.components.AmountText
import pl.nullpointerstudio.zlotowka.ui.components.ProgressBar
import pl.nullpointerstudio.zlotowka.ui.components.SectionLabel
import pl.nullpointerstudio.zlotowka.ui.components.SurfaceCard
import pl.nullpointerstudio.zlotowka.ui.mascot.Mascot
import pl.nullpointerstudio.zlotowka.ui.nav.Destinations
import pl.nullpointerstudio.zlotowka.ui.theme.Coral
import pl.nullpointerstudio.zlotowka.ui.theme.Lime
import pl.nullpointerstudio.zlotowka.ui.theme.Surface
import pl.nullpointerstudio.zlotowka.ui.theme.Surface2
import pl.nullpointerstudio.zlotowka.ui.theme.TextMuted
import pl.nullpointerstudio.zlotowka.ui.theme.TextPrimary
import java.util.Calendar
import kotlin.math.abs

@Composable
fun DashboardScreen(onNavigate: (String) -> Unit) {
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

        if (uiState.weeklySpend.isNotEmpty()) {
            WeeklyBars(weeklySpend = uiState.weeklySpend, modifier = Modifier.padding(top = 20.dp))
        }

        Column(modifier = Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            uiState.recentTransactions.forEach { tx ->
                val category = uiState.categories.firstOrNull { it.id == tx.categoryId }
                RecentTransactionRow(tx, category)
            }
        }
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

@Composable
private fun WeeklyBars(weeklySpend: List<DaySpend>, modifier: Modifier = Modifier) {
    val max = weeklySpend.maxOfOrNull { it.totalMinor }?.coerceAtLeast(1L) ?: 1L
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        weeklySpend.forEach { day ->
            val fraction = (day.totalMinor.toFloat() / max.toFloat()).coerceIn(0.03f, 1f)
            val isMax = day.totalMinor == max
            val barColor = when {
                isMax -> Coral
                day.isToday -> Lime
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
                        .height(70.dp * fraction)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(barColor),
                )
                Text(
                    text = day.dayLabel,
                    color = TextMuted,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun RecentTransactionRow(tx: TransactionEntity, category: CategoryEntity?) {
    SurfaceCard(modifier = Modifier.fillMaxWidth()) {
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
