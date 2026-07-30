package pl.nullpointerstudio.zlotowka.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Wejście systemowe dla widgetu Glance. Poza standardowym cyklem życia AppWidget, planuje
 * cykliczne odświeżanie co ~30 min przez WorkManager — natychmiastowe odświeżenie po zapisie
 * transakcji obsługuje [QuickAddAction] / repozytorium wywołując `BudgetWidget().updateAll(...)`
 * bezpośrednio.
 */
class BudgetWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = BudgetWidget()

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        enqueuePeriodicRefresh(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        enqueuePeriodicRefresh(context)
    }

    private fun enqueuePeriodicRefresh(context: Context) {
        val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(30, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            REFRESH_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    companion object {
        private const val REFRESH_WORK_NAME = "budget_widget_refresh_work"
    }
}
