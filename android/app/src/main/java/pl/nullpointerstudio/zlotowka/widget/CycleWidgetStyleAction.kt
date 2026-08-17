package pl.nullpointerstudio.zlotowka.widget

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState

/** Klucz stylu 2x1 w stanie widgetu — osobny na instancję, więc dwa widgety 2x1 mogą pokazywać co innego. */
val WIDGET_2X1_STYLE_KEY: Preferences.Key<Int> = intPreferencesKey("widget_2x1_style")

/**
 * Tap na „⟳" w rogu widgetu 2x1 — cyklicznie zmienia wariant (Bilans/Zostało/Spark/QuickAdd) tylko
 * dla TEJ instancji widgetu, bez otwierania aplikacji i bez osobnej Activity konfiguracyjnej.
 */
class CycleWidgetStyleAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        updateAppWidgetState(context, glanceId) { prefs ->
            val current = prefs[WIDGET_2X1_STYLE_KEY] ?: 0
            prefs[WIDGET_2X1_STYLE_KEY] = (current + 1) % WIDGET_2X1_STYLE_COUNT
        }
        BudgetWidget().update(context, glanceId)
    }
}
