package pl.nullpointerstudio.zlotowka.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import pl.nullpointerstudio.zlotowka.ZlotowkaApp
import pl.nullpointerstudio.zlotowka.data.PaymentMethod

/**
 * Chip "10"/"20"/"50" w widgecie "Puls" — dopisuje szybki wydatek kartą bez otwierania
 * aplikacji, a potem odświeża widget natychmiast, żeby liczby się zgadzały.
 */
class QuickAddAction : ActionCallback {

    companion object {
        val AMOUNT_KEY = ActionParameters.Key<Long>("amount_minor")
    }

    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val amount = parameters[AMOUNT_KEY] ?: return

        ZlotowkaApp.from(context).repository.addTransaction(
            title = "Szybki wydatek",
            categoryId = "jedzenie",
            amountMinor = -amount,
            method = PaymentMethod.CARD,
        )

        BudgetWidget().updateAll(context)
    }
}
