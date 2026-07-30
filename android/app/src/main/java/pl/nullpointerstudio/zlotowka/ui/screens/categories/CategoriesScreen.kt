package pl.nullpointerstudio.zlotowka.ui.screens.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.initializer
import pl.nullpointerstudio.zlotowka.ZlotowkaApp
import pl.nullpointerstudio.zlotowka.data.CategoryEntity
import pl.nullpointerstudio.zlotowka.domain.toPln
import pl.nullpointerstudio.zlotowka.ui.components.ProgressBar
import pl.nullpointerstudio.zlotowka.ui.components.SectionLabel
import pl.nullpointerstudio.zlotowka.ui.components.SurfaceCard
import pl.nullpointerstudio.zlotowka.ui.theme.Coral
import pl.nullpointerstudio.zlotowka.ui.theme.ColorTone
import pl.nullpointerstudio.zlotowka.ui.theme.TextMuted
import pl.nullpointerstudio.zlotowka.ui.theme.TextPrimary
import pl.nullpointerstudio.zlotowka.ui.theme.toColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun CategoriesScreen() {
    val context = LocalContext.current
    val app = remember(context) { ZlotowkaApp.from(context) }
    val viewModel: CategoriesViewModel = viewModel(
        factory = viewModelFactory { initializer { CategoriesViewModel(app.repository) } },
    )
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 24.dp),
    ) {
        SectionLabel(text = "${currentFullMonthYear()} · wydano")
        Text(
            text = "${uiState.totalSpentMinor.toPln()}",
            color = TextPrimary,
            fontSize = 30.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 4.dp),
        )

        Column(
            modifier = Modifier.padding(top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            uiState.categories.forEach { category ->
                CategoryRow(category = category, spentMinor = uiState.spentByCategory[category.id] ?: 0L)
            }
        }
    }
}

@Composable
private fun CategoryRow(category: CategoryEntity, spentMinor: Long) {
    val budget = category.monthlyBudgetMinor
    val over = budget > 0 && spentMinor > budget
    val pct = if (budget > 0) min(150, ((spentMinor.toDouble() / budget) * 100).roundToInt()) else 0
    val tone = category.colorToken.toColorTone().toColor()
    val amountColor = if (over) Coral else tone

    SurfaceCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Text(text = category.emoji, fontSize = 14.sp)
                Text(
                    text = category.label,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                )
                Text(text = spentMinor.toPln(), color = amountColor, fontSize = 12.sp)
            }
            ProgressBar(
                progress = min(100, pct) / 100f,
                modifier = Modifier.padding(top = 8.dp),
                height = 4.dp,
                color = if (over) Coral else tone,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = "limit ${budget.toPln()}", color = TextMuted, fontSize = 10.sp)
                Text(text = "$pct%", color = if (over) Coral else TextMuted, fontSize = 10.sp)
            }
        }
    }
}

private fun String.toColorTone(): ColorTone =
    ColorTone.entries.firstOrNull { it.name.equals(this, ignoreCase = true) } ?: ColorTone.Muted

private fun currentFullMonthYear(now: Long = System.currentTimeMillis()): String {
    val fmt = SimpleDateFormat("LLLL yyyy", Locale("pl", "PL"))
    return fmt.format(Date(now)).replaceFirstChar { it.uppercase() }
}
