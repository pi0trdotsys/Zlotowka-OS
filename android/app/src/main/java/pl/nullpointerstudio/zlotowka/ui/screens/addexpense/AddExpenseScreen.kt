@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package pl.nullpointerstudio.zlotowka.ui.screens.addexpense

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import pl.nullpointerstudio.zlotowka.ui.theme.PillShape
import pl.nullpointerstudio.zlotowka.ui.theme.Surface
import pl.nullpointerstudio.zlotowka.ui.theme.TextMuted
import pl.nullpointerstudio.zlotowka.ui.theme.TextPrimary
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Ekran "Dodaj / edytuj wpis" — obsługuje zarówno wydatki, jak i dochody (przełącznik u góry),
 * dowolną datę wsteczną (nie w przyszłości) oraz edycję/usuwanie istniejącej transakcji, gdy
 * [transactionId] jest podany (patrz Destinations.editExpense).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(transactionId: String? = null, onDone: () -> Unit) {
    val context = LocalContext.current
    val app = remember(context) { ZlotowkaApp.from(context) }
    val viewModel: AddExpenseViewModel = viewModel(
        factory = viewModelFactory { initializer { AddExpenseViewModel(app.repository) } },
    )
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    val isEditMode = transactionId != null
    val existingTransaction = if (transactionId != null) {
        uiState.transactions.firstOrNull { it.id == transactionId }
    } else {
        null
    }

    var amountText by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.BLIK) }
    var note by remember { mutableStateOf("") }
    var isIncome by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var timeOfDay by remember { mutableStateOf(LocalTime.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.categories) {
        if (selectedCategoryId == null) {
            selectedCategoryId = uiState.categories.firstOrNull()?.id
        }
    }

    // Gdy edytujemy istniejący wpis, uzupełniamy formularz po jego wczytaniu (dane ładują się async).
    LaunchedEffect(existingTransaction?.id) {
        val tx = existingTransaction ?: return@LaunchedEffect
        amountText = minorToInputText(tx.amountMinor)
        selectedCategoryId = tx.categoryId
        selectedMethod = tx.method
        note = tx.note ?: ""
        isIncome = tx.amountMinor > 0
        val zoned = Instant.ofEpochMilli(tx.timestamp).atZone(ZoneId.systemDefault())
        selectedDate = zoned.toLocalDate()
        timeOfDay = zoned.toLocalTime()
    }

    // W trybie edycji dane transakcji ładują się asynchronicznie — dopóki jej nie znajdziemy,
    // nie renderujemy formularza (unikamy migotania z pustymi/domyślnymi wartościami).
    if (isEditMode && existingTransaction == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "Wczytywanie…", color = TextMuted)
        }
        return
    }

    val amountMinorAbs = parseAmountToMinor(amountText)
    val signedAmountMinor = if (isIncome) amountMinorAbs else -amountMinorAbs
    val alert = if (!isIncome) selectedCategoryId?.let { repeatedExpenseAlert(uiState.transactions, it) } else null
    val selectedCategory = uiState.categories.firstOrNull { it.id == selectedCategoryId }
    val accent = if (isIncome) Lime else Coral

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            selectableDates = PastOrPresentSelectableDates,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Anuluj") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Na pewno usunąć ten wpis?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    val id = transactionId ?: return@TextButton
                    scope.launch {
                        viewModel.deleteTransaction(id)
                        onDone()
                    }
                }) { Text("Usuń", color = Coral) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Anuluj") }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 24.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Pill(
                text = "Wydatek",
                accent = Coral,
                filled = !isIncome,
                modifier = Modifier.clickable { isIncome = false },
            )
            Pill(
                text = "Dochód",
                accent = Lime,
                filled = isIncome,
                modifier = Modifier.clickable { isIncome = true },
            )
        }

        SectionLabel(text = "Kwota", modifier = Modifier.padding(top = 20.dp))
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
            suffix = { Text("zł", color = accent, fontSize = 16.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accent,
                unfocusedBorderColor = accent.copy(alpha = 0.4f),
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
            ),
        )

        SectionLabel(text = "Data", modifier = Modifier.padding(top = 20.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = formatSelectedDate(selectedDate), color = TextPrimary, fontSize = 15.sp)
            Pill(
                text = "Zmień",
                accent = Cyan,
                modifier = Modifier.clickable { showDatePicker = true },
            )
        }

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

        if (!isIncome && alert != null && selectedCategory != null) {
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
            text = when {
                isEditMode -> "Zapisz zmiany"
                isIncome -> "Zapisz dochód"
                else -> "Zapisz wydatek"
            },
            modifier = Modifier.padding(top = 24.dp),
            onClick = {
                val categoryId = selectedCategoryId ?: return@PrimaryPillButton
                if (amountMinorAbs <= 0L) return@PrimaryPillButton
                val title = note.ifBlank { selectedCategory?.label ?: if (isIncome) "Dochód" else "Wydatek" }
                val timestamp = buildTimestamp(selectedDate, timeOfDay)
                scope.launch {
                    viewModel.saveTransaction(
                        transactionId = transactionId,
                        title = title,
                        categoryId = categoryId,
                        amountMinor = signedAmountMinor,
                        method = selectedMethod,
                        note = note.ifBlank { null },
                        timestamp = timestamp,
                    )
                    onDone()
                }
            },
        )

        if (isEditMode) {
            DangerPillButton(
                text = if (isIncome) "Usuń dochód" else "Usuń wydatek",
                modifier = Modifier.padding(top = 12.dp),
                onClick = { showDeleteConfirm = true },
            )
        }
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
        horizontalAlignment = Alignment.CenterHorizontally,
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

/** Przycisk akcji niszczącej (usunięcie wpisu) — pigułka w stylu Pill, ale w tonacji Coral. */
@Composable
private fun DangerPillButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(PillShape)
            .background(Coral.copy(alpha = 0.08f))
            .border(1.dp, Coral.copy(alpha = 0.5f), PillShape)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, color = Coral, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

/** Parsuje wpisany tekst kwoty ("34,90" lub "34.90") na grosze. Nieprawidłowy wpis => 0. */
private fun parseAmountToMinor(input: String): Long {
    val normalized = input.trim().replace(',', '.')
    if (normalized.isEmpty()) return 0L
    val value = normalized.toDoubleOrNull() ?: return 0L
    return Math.round(value * 100)
}

/** Formatuje kwotę (grosze, wartość bezwzględna) do tekstu edytowalnego w polu "Kwota", np. "34,90". */
private fun minorToInputText(minor: Long): String {
    val value = kotlin.math.abs(minor) / 100.0
    return String.format(Locale.US, "%.2f", value).replace('.', ',')
}

/** "Dziś" dla bieżącej daty, w przeciwnym razie dd.MM.yyyy — spójnie z formatDeadline w GoalDetailScreen. */
private fun formatSelectedDate(date: LocalDate): String {
    if (date == LocalDate.now()) return "Dziś"
    return date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale("pl", "PL")))
}

/** Łączy wybraną datę z zachowaną godziną (bieżącą dla nowego wpisu, oryginalną przy edycji). */
private fun buildTimestamp(date: LocalDate, time: LocalTime): Long =
    LocalDateTime.of(date, time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

/** Nie pozwala wybrać daty w przyszłości — wydatki/dochody można dodawać tylko wstecz lub dziś. */
private object PastOrPresentSelectableDates : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        val pickedDate = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()
        return !pickedDate.isAfter(LocalDate.now(ZoneOffset.UTC))
    }

    override fun isSelectableYear(year: Int): Boolean = year <= LocalDate.now(ZoneOffset.UTC).year
}
