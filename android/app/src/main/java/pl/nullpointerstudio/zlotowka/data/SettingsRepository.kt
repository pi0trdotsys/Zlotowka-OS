package pl.nullpointerstudio.zlotowka.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

private val Context.dataStore by preferencesDataStore(name = "zlotowka_settings")

data class NotificationSettings(
    val dailySummaryEnabled: Boolean = true,
    val weeklyReportEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val dailySummaryHour: Int = 21,
    val weeklyReportDayOfWeek: Int = Calendar.MONDAY,
    val weeklyReportHour: Int = 9,
)

/** Preferencje powiadomień — czytane przez ekran ustawień i workery WorkManager. */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val DAILY_ENABLED = booleanPreferencesKey("daily_summary_enabled")
        val WEEKLY_ENABLED = booleanPreferencesKey("weekly_report_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val DAILY_HOUR = intPreferencesKey("daily_summary_hour")
        val WEEKLY_DAY = intPreferencesKey("weekly_report_day")
        val WEEKLY_HOUR = intPreferencesKey("weekly_report_hour")
    }

    val settings: Flow<NotificationSettings> = context.dataStore.data.map { prefs ->
        NotificationSettings(
            dailySummaryEnabled = prefs[Keys.DAILY_ENABLED] ?: true,
            weeklyReportEnabled = prefs[Keys.WEEKLY_ENABLED] ?: true,
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            dailySummaryHour = prefs[Keys.DAILY_HOUR] ?: 21,
            weeklyReportDayOfWeek = prefs[Keys.WEEKLY_DAY] ?: Calendar.MONDAY,
            weeklyReportHour = prefs[Keys.WEEKLY_HOUR] ?: 9,
        )
    }

    suspend fun setDailySummaryEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DAILY_ENABLED] = enabled }
    }

    suspend fun setWeeklyReportEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.WEEKLY_ENABLED] = enabled }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }
}
