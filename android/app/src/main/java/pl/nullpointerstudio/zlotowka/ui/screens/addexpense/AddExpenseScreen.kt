package pl.nullpointerstudio.zlotowka.ui.screens.addexpense

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.initializer
import kotlinx.coroutines.launch
import pl.nullpointerstudio.zlotowka.ZlotowkaApp
import pl.nullpointerstudio.zlotowka.data.CategoryEntity
import pl.nullpointerstudio.zlotowka.data.PaymentMethod
import pl.nullpointerstudio.zlotowka.domain.repeatedExpenseAlert
import pl.nullpointerstudio.zlotowka.domain.toPln
import pl.nullpointerstudio.zlotowka.ui.components.Pill
import pl.nullpointerstudio.zlotowka.ui.components.PrimaryPillButton
import pl.nullpointerstudio.zlotowka.ui.components.SectionLabel
import pl.nullpointerstudio.zlotowka.ui.theme.Coral
import pl.nullpointerstudio.zlotowka.ui.theme.Cyan
import pl.nullpointerstudio.zlotowka.ui.theme.Lime
import pl.nullpointerstudio.zlotowka.ui.theme.Surface
import pl.nullpointerstudio.zlotowka.ui.theme.TextMuted
import pl.nullpointerstudio.zlotowka.ui.theme.TextPrimary

@Composable
fun AddExpenseScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val app = remember(context) { ZlotowkaApp.from(context) }
    val viewModel: AddExpenseViewModel = viewModel(
        factory = viewModelFactory { initializer { AddExpenseViewModel(app.repository) } },
    )
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var amountText by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.BLIK) }
    var note by remember { mutableStateOf("") }

    LaunchedEffect(uiState.categories) {
        if (selectedCategoryId == null) {
            selectedCategoryId = uiState.categories.firstOrNull()?.id
        }
    }

    val amountMinor = parseAmountToMinor(amountText)
    val alert = selectedCategoryId?.let { repeatedExpenseAlert(uiState.transactions, it) }
    val selectedCategory = uiState.categories.firstOrNull { it.id == selectedCategoryId }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 24.dp),
    ) {
        SectionLabel(text = "Kwota")
        OutlinedTextField(
            value = amountText,
            onValueChange = { input ->
                if (input.count { it == ',' || it == '.' } <= 1) amountText = input
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            placeholder = { Text("0,00", fontSize = 32.sp, color = TextMuted) },
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 32.sp, color = TextPrimary),
            suffix = { Text("zł", color = Lime, fontSize = 16.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Lime,
                unfocusedBorderColor = Lime.copy(alpha = 0.4f),
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
            ),
        )

        SectionLabel(text = "Kategoria", modifier = Modifier.padding(top = 20.dp))
        Column(
            modifier = Modifier.padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            uiState.categories.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    row.forEach { category ->
                        CategoryCell(
                            category = category,
                            selected = category.id == selectedCategoryId,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedCategoryId = category.id },
                        )
                    }
                    repeat(3 - row.size) { Column(modifier = Modifier.weight(1f)) {} }
                }
            }
        }

        SectionLabel(text = "Opis (opcjonalnie)", modifier = Modifier.padding(top = 20.dp))
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            placeholder = { Text("np. Lunch na mieście", color = TextMuted, fontSize = 13.sp) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Cyan,
                unfocusedBorderColor = Cyan.copy(alpha = 0.3f),
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
            ),
        )

        Row(
            modifier = Modifier.padding(top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PaymentMethodChip("BLIK", selectedMethod == PaymentMethod.BLIK) { selectedMethod = PaymentMethod.BLIK }
            PaymentMethodChip("Karta", selectedMethod == PaymentMethod.CARD) { selectedMethod = PaymentMethod.CARD }
            PaymentMethodChip("Gotówka", selectedMethod == PaymentMethod.CASH) { selectedMethod = PaymentMethod.CASH }
        }

        if (alert != null && selectedCategory != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Coral.copy(alpha = 0.1f))
                    .border(1.dp, Coral.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(12.dp),
            ) {
                Text(
                    text = "Uwaga: to ${alert.occurrencesThisWeek}. wydatek w kategorii „${selectedCategory.label}” " +
                        "w tym tygodniu. Ograniczenie zostawiłoby ci ok. ${alert.potentialSavingsMinor.toPln()}.",
                    color = Coral,
                    fontSize = 12.sp,
                )
            }
        }

        PrimaryPillButton(
            text = "Zapisz wydatek",
            modifier = Modifier.padding(top = 24.dp),
            onClick = {
                val categoryId = selectedCategoryId ?: return@PrimaryPillButton
                if (amountMinor <= 0L) return@PrimaryPillButton
                val title = note.ifBlank { selectedCategory?.label ?: "Wydatek" }
                scope.launch {
                    viewModel.saveExpense(
                        title = title,
                        categoryId = categoryId,
                        amountMinor = amountMinor,
                        method = selectedMethod,
                        note = note.ifBlank { null },
                    )
                    onDone()
                }
            },
        )
    }
}

@Composable
private fun CategoryCell(
    category: CategoryEntity,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) Lime.copy(alpha = 0.12f) else Surface)
            .border(
                1.dp,
                if (selected) Lime.copy(alpha = 0.5f) else androidx.compose.ui.graphics.Color(0x17FFFFFF),
                RoundedCornerShape(14.dp),
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
    ) {
        Text(text = category.emoji, fontSize = 18.sp)
        Text(
            text = category.label,
            color = TextMuted,
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun PaymentMethodChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Pill(
        text = label,
        accent = if (selected) Cyan else TextMuted,
        filled = selected,
        modifier = Modifier.clickable(onClick = onClick),
    )
}

/** Parsuje wpisany tekst kwoty ("34,90" lub "34.90") na grosze. Nieprawidłowy wpis => 0. */
private fun parseAmountToMinor(input: String): Long {
    val normalized = input.trim().replace(',', '.')
    if (normalized.isEmpty()) return 0L
    val value = normalized.toDoubleOrNull() ?: return 0L
    return Math.round(value * 100)
}
