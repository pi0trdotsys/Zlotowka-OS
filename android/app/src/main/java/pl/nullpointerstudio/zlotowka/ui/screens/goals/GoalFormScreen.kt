@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package pl.nullpointerstudio.zlotowka.ui.screens.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.initializer
import kotlinx.coroutines.launch
import pl.nullpointerstudio.zlotowka.ZlotowkaApp
import pl.nullpointerstudio.zlotowka.ui.components.Pill
import pl.nullpointerstudio.zlotowka.ui.components.PrimaryPillButton
import pl.nullpointerstudio.zlotowka.ui.components.SectionLabel
import pl.nullpointerstudio.zlotowka.ui.theme.Coral
import pl.nullpointerstudio.zlotowka.ui.theme.Lime
import pl.nullpointerstudio.zlotowka.ui.theme.Surface
import pl.nullpointerstudio.zlotowka.ui.theme.TextMuted
import pl.nullpointerstudio.zlotowka.ui.theme.TextPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Domyślny termin nowego celu — 6 miesięcy od dziś. */
private fun defaultDeadline(): Long {
    val cal = Calendar.getInstance()
    cal.add(Calendar.MONTH, 6)
    return cal.timeInMillis
}

/** Start dzisiejszego dnia w UTC — DatePickerDialog przekazuje daty w SelectableDates jako UTC millis. */
private fun startOfTodayUtc(): Long {
    val cal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

/**
 * Material3 DatePicker reprezentuje dzień jako północ UTC, a nasz `deadline` to zwykły
 * epoch millis w lokalnej strefie (jak wszędzie indziej w aplikacji, np. `formatDeadline`).
 * Te dwie konwersje zamieniają dzień kalendarzowy między reprezentacjami, żeby data pokazywana
 * w pickerze i data zapisywana w `GoalEntity.deadline` zawsze wskazywały ten sam dzień.
 */
private fun localMillisToUtcPickerMillis(localMillis: Long): Long {
    val local = Calendar.getInstance()
    local.timeInMillis = localMillis
    val utc = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
    utc.clear()
    utc.set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    return utc.timeInMillis
}

private fun utcPickerMillisToLocalMillis(utcMillis: Long): Long {
    val utc = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
    utc.timeInMillis = utcMillis
    val local = Calendar.getInstance()
    local.clear()
    local.set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH))
    return local.timeInMillis
}

private fun formatDeadline(epochMillis: Long): String =
    SimpleDateFormat("dd.MM.yyyy", Locale("pl", "PL")).format(Date(epochMillis))

/** Parsuje wpisany tekst kwoty ("34,90" lub "34.90") na grosze. Nieprawidłowy wpis => 0. */
private fun parseAmountToMinor(input: String): Long {
    val normalized = input.trim().replace(',', '.')
    if (normalized.isEmpty()) return 0L
    val value = normalized.toDoubleOrNull() ?: return 0L
    return Math.round(value * 100)
}

@Composable
fun GoalFormScreen(goalId: String? = null, onDone: () -> Unit) {
    val context = LocalContext.current
    val app = remember(context) { ZlotowkaApp.from(context) }
    val viewModel: GoalFormViewModel = viewModel(
        factory = viewModelFactory { initializer { GoalFormViewModel(app.repository, goalId) } },
    )
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val isEditing = goalId != null

    var label by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }
    var monthlyContribText by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf(defaultDeadline()) }
    var makeMain by remember { mutableStateOf(false) }
    var prefilled by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.existingGoal, uiState.isFirstGoal, uiState.loading) {
        if (uiState.loading || prefilled) return@LaunchedEffect
        val goal = uiState.existingGoal
        if (goal != null) {
            label = goal.label
            targetText = (goal.targetMinor / 100.0).let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() }
            monthlyContribText = (goal.monthlyContribMinor / 100.0).let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() }
            deadline = goal.deadline
            prefilled = true
        } else if (!isEditing) {
            makeMain = uiState.isFirstGoal
            prefilled = true
        }
    }

    val targetMinor = parseAmountToMinor(targetText)
    val canSave = label.isNotBlank() && targetMinor > 0L

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Edytuj cel" else "Nowy cel",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = TextPrimary,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 24.dp),
        ) {
            SectionLabel(text = "Nazwa celu")
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                placeholder = { Text("np. Wyjazd w Bieszczady", color = TextMuted, fontSize = 13.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Lime,
                    unfocusedBorderColor = Lime.copy(alpha = 0.4f),
                    focusedContainerColor = Surface,
                    unfocusedContainerColor = Surface,
                ),
            )

            SectionLabel(text = "Kwota docelowa", modifier = Modifier.padding(top = 20.dp))
            OutlinedTextField(
                value = targetText,
                onValueChange = { input ->
                    if (input.count { it == ',' || it == '.' } <= 1) targetText = input
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                placeholder = { Text("0,00", fontSize = 24.sp, color = TextMuted) },
                textStyle = TextStyle(fontSize = 24.sp, color = TextPrimary),
                suffix = { Text("zł", color = Lime, fontSize = 14.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Lime,
                    unfocusedBorderColor = Lime.copy(alpha = 0.4f),
                    focusedContainerColor = Surface,
                    unfocusedContainerColor = Surface,
                ),
            )

            SectionLabel(text = "Termin", modifier = Modifier.padding(top = 20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = formatDeadline(deadline), color = TextPrimary, fontSize = 14.sp)
                Pill(
                    text = "Wybierz",
                    accent = Lime,
                    filled = true,
                    modifier = Modifier.clickable { showDatePicker = true },
                )
            }

            SectionLabel(text = "Miesięczna wpłata (opcjonalnie)", modifier = Modifier.padding(top = 20.dp))
            OutlinedTextField(
                value = monthlyContribText,
                onValueChange = { input ->
                    if (input.count { it == ',' || it == '.' } <= 1) monthlyContribText = input
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                placeholder = { Text("0,00", fontSize = 18.sp, color = TextMuted) },
                textStyle = TextStyle(fontSize = 18.sp, color = TextPrimary),
                suffix = { Text("zł/mies.", color = TextMuted, fontSize = 12.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Lime,
                    unfocusedBorderColor = Lime.copy(alpha = 0.4f),
                    focusedContainerColor = Surface,
                    unfocusedContainerColor = Surface,
                ),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Ustaw jako cel główny", color = TextPrimary, fontSize = 13.sp)
                Switch(
                    checked = makeMain,
                    onCheckedChange = { makeMain = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = Lime, checkedThumbColor = Surface),
                )
            }

            PrimaryPillButton(
                text = if (isEditing) "Zapisz zmiany" else "Zapisz cel",
                modifier = Modifier.padding(top = 24.dp),
                onClick = {
                    if (!canSave) return@PrimaryPillButton
                    val monthlyMinor = parseAmountToMinor(monthlyContribText)
                    scope.launch {
                        viewModel.save(
                            label = label.trim(),
                            targetMinor = targetMinor,
                            deadline = deadline,
                            monthlyContribMinor = monthlyMinor,
                            makeMain = makeMain,
                        )
                        onDone()
                    }
                },
            )

            if (isEditing) {
                TextButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                ) {
                    Text(text = "Usuń cel", color = Coral, fontSize = 14.sp)
                }
            }
        }
    }

    if (showDatePicker) {
        val today = startOfTodayUtc()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = localMillisToUtcPickerMillis(deadline),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= today
            },
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { deadline = utcPickerMillisToLocalMillis(it) }
                    showDatePicker = false
                }) {
                    Text("OK", color = Lime)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Anuluj", color = TextMuted)
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Usunąć cel?") },
            text = { Text("Na pewno usunąć ten cel? Historia wpłat też zostanie usunięta.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    scope.launch {
                        viewModel.delete()
                        onDone()
                    }
                }) {
                    Text("Usuń", color = Coral)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Anuluj")
                }
            },
        )
    }
}
