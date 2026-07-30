package pl.nullpointerstudio.zlotowka.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Planowanie cyklicznych workerów powiadomień. Harmonogram jest "na sztywno" (21:00 / pon 9:00)
 * przy pierwszym zaplanowaniu — sam worker sprawdza w locie, czy użytkownik ma powiadomienia
 * włączone i o której godzinie faktycznie chce je widzieć (DataStore jest asynchroniczne,
 * więc nie da się tego odczytać synchronicznie w miejscu planowania).
 */
object NotificationScheduler {

    private const val DAILY_WORK_NAME = "daily_summary_work"
    private const val WEEKLY_WORK_NAME = "weekly_report_work"

    private const val DEFAULT_DAILY_HOUR = 21
    private const val DEFAULT_WEEKLY_DAY = Calendar.MONDAY
    private const val DEFAULT_WEEKLY_HOUR = 9

    fun scheduleAll(context: Context) {
        val workManager = WorkManager.getInstance(context)

        val dailyDelayMs = delayUntilNextHour(DEFAULT_DAILY_HOUR)
        val dailyRequest = PeriodicWorkRequestBuilder<DailySummaryWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(dailyDelayMs, TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            DAILY_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            dailyRequest,
        )

        val weeklyDelayMs = delayUntilNextDayAndHour(DEFAULT_WEEKLY_DAY, DEFAULT_WEEKLY_HOUR)
        val weeklyRequest = PeriodicWorkRequestBuilder<WeeklyReportWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(weeklyDelayMs, TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            WEEKLY_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            weeklyRequest,
        )
    }

    /** Wywoływane przez ekran ustawień po zmianie godziny podsumowania dnia. */
    fun rescheduleDaily(context: Context, hour: Int) {
        val delayMs = delayUntilNextHour(hour)
        val request = PeriodicWorkRequestBuilder<DailySummaryWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    /** Wywoływane przez ekran ustawień po zmianie dnia/godziny raportu tygodniowego. */
    fun rescheduleWeekly(context: Context, dayOfWeek: Int, hour: Int) {
        val delayMs = delayUntilNextDayAndHour(dayOfWeek, hour)
        val request = PeriodicWorkRequestBuilder<WeeklyReportWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WEEKLY_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    private fun delayUntilNextHour(hour: Int): Long {
        val now = Calendar.getInstance()
        val target = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!target.after(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }

    private fun delayUntilNextDayAndHour(dayOfWeek: Int, hour: Int): Long {
        val now = Calendar.getInstance()
        val target = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        while (target.get(Calendar.DAY_OF_WEEK) != dayOfWeek || !target.after(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }
}
