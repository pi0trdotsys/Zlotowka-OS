package pl.nullpointerstudio.zlotowka.widget

import android.content.Intent
import androidx.compose.runtime.Composable
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
import pl.nullpointerstudio.zlotowka.domain.MotivationSnapshot
import pl.nullpointerstudio.zlotowka.domain.toPln
import pl.nullpointerstudio.zlotowka.ui.MainActivity
import pl.nullpointerstudio.zlotowka.ui.nav.Destinations
import pl.nullpointerstudio.zlotowka.ui.theme.Lime
import pl.nullpointerstudio.zlotowka.ui.theme.OnLime
import pl.nullpointerstudio.zlotowka.ui.theme.Surface
import pl.nullpointerstudio.zlotowka.ui.theme.Surface2
import pl.nullpointerstudio.zlotowka.ui.theme.TextMuted
import pl.nullpointerstudio.zlotowka.ui.theme.TextPrimary

/** Próg szerokości, od którego renderujemy szeroki layout "Pasek dnia" zamiast wąskiego "Puls". */
private val WIDE_BREAKPOINT = 300.dp

private val CardBackground = ColorProvider(Surface)
private val TrackBackground = ColorProvider(Surface2)
private val LimeProvider = ColorProvider(Lime)
private val OnLimeProvider = ColorProvider(OnLime)
private val TextPrimaryProvider = ColorProvider(TextPrimary)
private val TextMutedProvider = ColorProvider(TextMuted)

@Composable
fun WidgetContent(snapshot: MotivationSnapshot) {
    val size = LocalSize.current
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(CardBackground)
            .cornerRadius(24.dp)
            .padding(14.dp),
    ) {
        if (size.width >= WIDE_BREAKPOINT) {
            WideBudgetStrip(snapshot)
        } else {
            SmallPulse(snapshot)
        }
    }
}

/** "Pasek dnia" (4x2) — ile zostało dziś, szybki dodaj wydatek, pasek postępu miesiąca. */
@Composable
private fun WideBudgetStrip(snapshot: MotivationSnapshot) {
    val context = LocalContext.current
    val progress = if (snapshot.monthBudgetMinor > 0) {
        (snapshot.monthSpentMinor.toFloat() / snapshot.monthBudgetMinor.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Column(modifier = GlanceModifier.fillMaxSize()) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically,
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = "DZIŚ ZOSTAŁO",
                    style = TextStyle(
                        color = TextMutedProvider,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                )
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = snapshot.dailyLeftMinor.toPln(),
                    style = TextStyle(
                        color = TextPrimaryProvider,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
            Box(
                modifier = GlanceModifier
                    .size(30.dp)
                    .cornerRadius(15.dp)
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
                    style = TextStyle(
                        color = OnLimeProvider,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(10.dp))

        LinearProgressIndicator(
            modifier = GlanceModifier.fillMaxWidth().height(4.dp),
            progress = progress,
            color = LimeProvider,
            backgroundColor = TrackBackground,
        )

        Spacer(modifier = GlanceModifier.height(8.dp))

        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Text(
                text = "Wydano dziś ${snapshot.spentTodayMinor.toPln()}",
                modifier = GlanceModifier.defaultWeight(),
                style = TextStyle(color = TextMutedProvider, fontSize = 10.sp),
            )
            Text(
                text = "🔥 ${snapshot.streakDays} dni",
                style = TextStyle(
                    color = LimeProvider,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
    }
}

/** "Puls" (2x2) — wynik motywacyjny + trzy chipy szybkiego BLIK-a (10/20/50 zł). */
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
            text = "na 100 pkt",
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
