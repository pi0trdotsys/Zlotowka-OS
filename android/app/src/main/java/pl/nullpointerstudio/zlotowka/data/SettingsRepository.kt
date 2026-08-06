package pl.nullpointerstudio.zlotowka.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

private val Context.dataStore by preferencesDataStore(name = "zlotowka_settings")

data class NotificationSettings(
    val dailySummaryEnabled: Boolean = true,
    val weeklyReportEnabled: Boolean = true,
    val motivationalTipsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val dailySummaryHour: Int = 21,
    val weeklyReportDayOfWeek: Int = Calendar.MONDAY,
    val weeklyReportHour: Int = 9,
)

/** Plan budżetowy oparty o szacowane zarobki — patrz [pl.nullpointerstudio.zlotowka.domain.buildMotivationSnapshot]. */
data class BudgetPlanSettings(
    val estimatedIncomeMinor: Long = 0L,
)

/** Preferencje powiadomień i plan budżetowy — czytane przez ekrany i workery WorkManager. */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val DAILY_ENABLED = booleanPreferencesKey("daily_summary_enabled")
        val WEEKLY_ENABLED = booleanPreferencesKey("weekly_report_enabled")
        val MOTIVATION_ENABLED = booleanPreferencesKey("motivational_tips_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val DAILY_HOUR = intPreferencesKey("daily_summary_hour")
        val WEEKLY_DAY = intPreferencesKey("weekly_report_day")
        val WEEKLY_HOUR = intPreferencesKey("weekly_report_hour")
        val ESTIMATED_INCOME = longPreferencesKey("estimated_income_minor")
    }

    val settings: Flow<NotificationSettings> = context.dataStore.data.map { prefs ->
        NotificationSettings(
            dailySummaryEnabled = prefs[Keys.DAILY_ENABLED] ?: true,
            weeklyReportEnabled = prefs[Keys.WEEKLY_ENABLED] ?: true,
            motivationalTipsEnabled = prefs[Keys.MOTIVATION_ENABLED] ?: true,
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            dailySummaryHour = prefs[Keys.DAILY_HOUR] ?: 21,
            weeklyReportDayOfWeek = prefs[Keys.WEEKLY_DAY] ?: Calendar.MONDAY,
            weeklyReportHour = prefs[Keys.WEEKLY_HOUR] ?: 9,
        )
    }

    val budgetPlan: Flow<BudgetPlanSettings> = context.dataStore.data.map { prefs ->
        BudgetPlanSettings(estimatedIncomeMinor = prefs[Keys.ESTIMATED_INCOME] ?: 0L)
    }

    suspend fun setDailySummaryEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DAILY_ENABLED] = enabled }
    }

    suspend fun setWeeklyReportEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.WEEKLY_ENABLED] = enabled }
    }

    suspend fun setMotivationalTipsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.MOTIVATION_ENABLED] = enabled }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun setEstimatedIncome(estimatedIncomeMinor: Long) {
        context.dataStore.edit { it[Keys.ESTIMATED_INCOME] = estimatedIncomeMinor.coerceAtLeast(0L) }
    }
}
