package pl.nullpointerstudio.zlotowka

import android.app.Application
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import pl.nullpointerstudio.zlotowka.data.AppDatabase
import pl.nullpointerstudio.zlotowka.data.BudgetRepository
import pl.nullpointerstudio.zlotowka.data.SettingsRepository
import pl.nullpointerstudio.zlotowka.notifications.NotificationChannels
import pl.nullpointerstudio.zlotowka.notifications.NotificationScheduler

/** Kompozycyjny root aplikacji: baza, repozytoria, kanały powiadomień. Bez Hilt — lekki ręczny DI. */
class ZlotowkaApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var repository: BudgetRepository
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.get(this)
        repository = BudgetRepository(db)
        settingsRepository = SettingsRepository(this)

        NotificationChannels.ensureCreated(this)

        applicationScope.launch {
            repository.seedIfNeeded()
        }
        NotificationScheduler.scheduleAll(this)
    }

    companion object {
        fun from(context: Context): ZlotowkaApp = context.applicationContext as ZlotowkaApp
    }
}
