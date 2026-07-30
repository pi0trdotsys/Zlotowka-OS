package pl.nullpointerstudio.zlotowka.notifications

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import pl.nullpointerstudio.zlotowka.R
import pl.nullpointerstudio.zlotowka.ZlotowkaApp
import pl.nullpointerstudio.zlotowka.domain.currentMonthExpenseByCategory
import pl.nullpointerstudio.zlotowka.domain.suggestionsForGoal
import pl.nullpointerstudio.zlotowka.domain.toPln
import pl.nullpointerstudio.zlotowka.domain.weekOverWeek
import pl.nullpointerstudio.zlotowka.ui.nav.Destinations
import kotlin.math.abs

/** Cotygodniowy raport wydatków z sugestią cięcia — uruchamiany cyklicznie przez [NotificationScheduler]. */
class WeeklyReportWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = ZlotowkaApp.from(applicationContext)
        val settings = app.settingsRepository.settings.first()
        if (!settings.weeklyReportEnabled) return Result.success()

        if (!NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) {
            return Result.success()
        }

        val categories = app.repository.categories.first()
        val transactions = app.repository.transactions.first()
        val goals = app.repository.goals.first()

        val comparison = weekOverWeek(transactions)
        val deltaText = abs(comparison.deltaExpenseMinor).toPln()
        val weekLine = if (comparison.improved) {
            "− wydałeś o $deltaText mniej niż tydzień temu 🎉"
        } else {
            "+ o $deltaText więcej niż tydzień temu"
        }

        val topGoal = goals.minByOrNull { it.priority }
        val suggestion = topGoal?.let { goal ->
            suggestionsForGoal(goal, categories, currentMonthExpenseByCategory(transactions), limit = 1).firstOrNull()
        }

        val bodyBuilder = StringBuilder(weekLine)
        if (suggestion != null) {
            bodyBuilder.append('\n')
            bodyBuilder.append(
                "Sugestia: ogranicz ${suggestion.label} o ${suggestion.cutMinor.toPln()} — " +
                    "przyspieszysz cel o ${suggestion.weeksSaved} tyg.",
            )
        }
        val body = bodyBuilder.toString()

        val notification = NotificationCompat.Builder(applicationContext, NotificationChannels.CHANNEL_WEEKLY)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Raport tygodnia")
            .setContentText(weekLine)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(
                NotificationChannels.buildContentIntent(
                    applicationContext,
                    Destinations.BUDGET,
                    NotificationChannels.weeklyNotificationId(),
                ),
            )
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(NotificationChannels.weeklyNotificationId(), notification)

        return Result.success()
    }
}
