package pl.nullpointerstudio.zlotowka.notifications

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import pl.nullpointerstudio.zlotowka.R
import pl.nullpointerstudio.zlotowka.ZlotowkaApp
import pl.nullpointerstudio.zlotowka.domain.buildMotivationSnapshot
import pl.nullpointerstudio.zlotowka.domain.toPln
import pl.nullpointerstudio.zlotowka.ui.nav.Destinations

/** Codzienne podsumowanie wydatków — uruchamiane cyklicznie przez [NotificationScheduler]. */
class DailySummaryWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = ZlotowkaApp.from(applicationContext)
        val settings = app.settingsRepository.settings.first()
        if (!settings.dailySummaryEnabled) return Result.success()

        if (!NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) {
            return Result.success()
        }

        val categories = app.repository.categories.first()
        val transactions = app.repository.transactions.first()
        val snapshot = buildMotivationSnapshot(categories, transactions)

        val title = "Dziś wydano ${snapshot.spentTodayMinor.toPln()}"
        val body = "Zostało ${snapshot.dailyLeftMinor.toPln()} do jutra · Puls ${snapshot.score}/100 · 🔥 ${snapshot.streakDays} dni"

        val notification = NotificationCompat.Builder(applicationContext, NotificationChannels.CHANNEL_DAILY)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(
                NotificationChannels.buildContentIntent(
                    applicationContext,
                    Destinations.DASHBOARD,
                    NotificationChannels.dailyNotificationId(),
                ),
            )
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(NotificationChannels.dailyNotificationId(), notification)

        return Result.success()
    }
}
