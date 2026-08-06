package pl.nullpointerstudio.zlotowka.notifications

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import pl.nullpointerstudio.zlotowka.R
import pl.nullpointerstudio.zlotowka.ZlotowkaApp
import pl.nullpointerstudio.zlotowka.domain.hasLoggedToday
import pl.nullpointerstudio.zlotowka.ui.nav.Destinations

/**
 * Przypomnienie w środku dnia, żeby zalogować dzisiejsze wydatki/dochody — tylko gdy
 * naprawdę jeszcze nic dziś nie zapisano (żeby nie spamować, gdy użytkownik i tak na bieżąco
 * wpisuje wpisy). Uruchamiane cyklicznie przez [NotificationScheduler].
 */
class LoggingReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = ZlotowkaApp.from(applicationContext)
        val settings = app.settingsRepository.settings.first()
        if (!settings.dailySummaryEnabled) return Result.success()

        val transactions = app.repository.transactions.first()
        if (hasLoggedToday(transactions)) return Result.success()

        if (!NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) {
            return Result.success()
        }

        val title = "Nie zapomnij zalogować dzisiejszych wydatków"
        val body = "Nie zalogowałeś dziś jeszcze żadnego wydatku ani dochodu. Dodaj go, zanim zapomnisz 📝"

        val notification = NotificationCompat.Builder(applicationContext, NotificationChannels.CHANNEL_DAILY)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(
                NotificationChannels.buildContentIntent(
                    applicationContext,
                    Destinations.ADD_EXPENSE,
                    NotificationChannels.loggingReminderNotificationId(),
                ),
            )
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(NotificationChannels.loggingReminderNotificationId(), notification)

        return Result.success()
    }
}
