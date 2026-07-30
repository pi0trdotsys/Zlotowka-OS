package pl.nullpointerstudio.zlotowka.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/** Cykliczne odświeżenie widgetu (co ~30 min), niezależne od interakcji z aplikacją. */
class WidgetUpdateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        BudgetWidget().updateAll(applicationContext)
        return Result.success()
    }
}
