package pl.nullpointerstudio.zlotowka.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import pl.nullpointerstudio.zlotowka.R
import pl.nullpointerstudio.zlotowka.domain.Milestone
import pl.nullpointerstudio.zlotowka.ui.MainActivity
import pl.nullpointerstudio.zlotowka.ui.nav.Destinations

/**
 * Rejestracja kanałów powiadomień (API 26+) oraz proste akcje wysyłki — dzielone
 * przez workery podsumowania dnia/tygodnia i odblokowania kamieni milowych.
 */
object NotificationChannels {

    const val CHANNEL_DAILY = "daily_summary"
    const val CHANNEL_WEEKLY = "weekly_report"
    const val CHANNEL_MILESTONE = "milestones"

    private const val NOTIF_ID_DAILY = 1001
    private const val NOTIF_ID_WEEKLY = 1002
    private const val NOTIF_ID_MILESTONE_BASE = 2000
    private const val NOTIF_ID_TEST = 999

    private fun coinSoundUri(context: Context): Uri =
        Uri.parse("android.resource://${context.packageName}/${R.raw.notif_coin}")

    private fun audioAttributes(): AudioAttributes =
        AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

    fun ensureCreated(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val soundUri = coinSoundUri(context)
        val attrs = audioAttributes()

        val daily = NotificationChannel(
            CHANNEL_DAILY,
            context.getString(R.string.notif_channel_daily_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.notif_channel_daily_desc)
            setSound(soundUri, attrs)
        }

        val weekly = NotificationChannel(
            CHANNEL_WEEKLY,
            context.getString(R.string.notif_channel_weekly_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.notif_channel_weekly_desc)
            setSound(soundUri, attrs)
        }

        val milestone = NotificationChannel(
            CHANNEL_MILESTONE,
            context.getString(R.string.notif_channel_milestone_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.notif_channel_milestone_desc)
            setSound(soundUri, attrs)
        }

        manager.createNotificationChannels(listOf(daily, weekly, milestone))
    }

    private fun contentIntent(context: Context, route: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(Destinations.EXTRA_ROUTE, route)
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    fun sendTestNotification(context: Context) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        val notification = NotificationCompat.Builder(context, CHANNEL_DAILY)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Testowe powiadomienie")
            .setContentText("Tak będą wyglądać Twoje powiadomienia 🔔")
            .setAutoCancel(true)
            .setContentIntent(contentIntent(context, Destinations.DASHBOARD, NOTIF_ID_TEST))
            .build()

        NotificationManagerCompat.from(context).notify(NOTIF_ID_TEST, notification)
    }

    fun notifyMilestoneUnlocked(context: Context, goalLabel: String, milestone: Milestone) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        val notification = NotificationCompat.Builder(context, CHANNEL_MILESTONE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Odznaka odblokowana! 🏅")
            .setContentText("${milestone.reward} — $goalLabel")
            .setAutoCancel(true)
            .setContentIntent(
                contentIntent(context, Destinations.GOALS, NOTIF_ID_MILESTONE_BASE + milestone.pct),
            )
            .build()

        NotificationManagerCompat.from(context).notify(NOTIF_ID_MILESTONE_BASE + milestone.pct, notification)
    }

    internal fun dailyNotificationId(): Int = NOTIF_ID_DAILY
    internal fun weeklyNotificationId(): Int = NOTIF_ID_WEEKLY

    internal fun buildContentIntent(context: Context, route: String, requestCode: Int): PendingIntent =
        contentIntent(context, route, requestCode)
}
