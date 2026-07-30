package pl.nullpointerstudio.zlotowka.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Po restarcie urządzenia WorkManager traci zaplanowane okresowe zadania — odtwarzamy je tutaj. */
class BootRescheduleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            NotificationScheduler.scheduleAll(context)
        }
    }
}
