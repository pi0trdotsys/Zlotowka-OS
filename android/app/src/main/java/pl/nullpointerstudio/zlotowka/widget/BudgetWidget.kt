package pl.nullpointerstudio.zlotowka.widget

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import kotlinx.coroutines.flow.first
import pl.nullpointerstudio.zlotowka.ZlotowkaApp

/**
 * "Pasek dnia" / "Puls" — widget na ekranie głównym. Responsywny: szeroki layout (4x2) pokazuje
 * dzienny budżet z szybkim dodawaniem wydatku, wąski (2x2) pokazuje Puls oszczędzania z chipami
 * szybkiego BLIK-a. Ten sam [pl.nullpointerstudio.zlotowka.domain.MotivationSnapshot] co Pulpit,
 * żeby liczby się nigdy nie rozjechały.
 */
class BudgetWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(180.dp, 110.dp),
            DpSize(330.dp, 110.dp),
        ),
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = ZlotowkaApp.from(context).repository
        val snapshot = repo.motivationSnapshot.first()

        provideContent {
            WidgetContent(snapshot)
        }
    }
}
