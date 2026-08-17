package pl.nullpointerstudio.zlotowka.widget

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import kotlinx.coroutines.flow.first
import pl.nullpointerstudio.zlotowka.ZlotowkaApp
import pl.nullpointerstudio.zlotowka.domain.weeklyFlowSeries

/**
 * Widget na ekran główny — skaluje się do 4 rozmiarów (2x1, 2x2, 4x1, 4x2), z osobnym,
 * maksymalnie funkcjonalnym layoutem dla każdego z nich (patrz [WidgetContent]). Ten sam
 * [pl.nullpointerstudio.zlotowka.domain.MotivationSnapshot] co Pulpit, żeby liczby się nigdy
 * nie rozjechały między powierzchniami.
 */
class BudgetWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(110.dp, 40.dp), // 2x1 — minimalny pasek
            DpSize(110.dp, 110.dp), // 2x2 — Puls
            DpSize(250.dp, 40.dp), // 4x1 — skondensowany pasek dnia
            DpSize(250.dp, 110.dp), // 4x2 — pełny pasek dnia
        ),
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = ZlotowkaApp.from(context).repository
        val snapshot = repo.motivationSnapshot.first()
        val weeklyFlow = weeklyFlowSeries(repo.transactions.first())

        provideContent {
            val prefs = currentState<Preferences>()
            val style2x1 = prefs[WIDGET_2X1_STYLE_KEY] ?: 0
            WidgetContent(snapshot, weeklyFlow, style2x1)
        }
    }
}
