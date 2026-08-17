@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package pl.nullpointerstudio.zlotowka.ui.screens.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.launch
import pl.nullpointerstudio.zlotowka.ZlotowkaApp
import pl.nullpointerstudio.zlotowka.data.CategoryKind
import pl.nullpointerstudio.zlotowka.ui.components.Pill
import pl.nullpointerstudio.zlotowka.ui.components.PrimaryPillButton
import pl.nullpointerstudio.zlotowka.ui.components.SectionLabel
import pl.nullpointerstudio.zlotowka.ui.components.SurfaceCard
import pl.nullpointerstudio.zlotowka.ui.theme.BorderOnDark
import pl.nullpointerstudio.zlotowka.ui.theme.ColorTone
import pl.nullpointerstudio.zlotowka.ui.theme.Coral
import pl.nullpointerstudio.zlotowka.ui.theme.Lime
import pl.nullpointerstudio.zlotowka.ui.theme.PillShape
import pl.nullpointerstudio.zlotowka.ui.theme.Surface
import pl.nullpointerstudio.zlotowka.ui.theme.TextMuted
import pl.nullpointerstudio.zlotowka.ui.theme.TextPrimary
import pl.nullpointerstudio.zlotowka.ui.theme.toColor

private const val DEFAULT_EMOJI = "🏷️"

/**
 * Kurowana lista emoji do wyboru ikony kategorii — zamiast [androidx.emoji2.emojipicker.EmojiPickerView],
 * które przy pierwszym otwarciu potrafi zablokować główny wątek na tyle długo, że system pokazuje
 * "aplikacja nie odpowiada" (zwłaszcza na emulatorze). Ten picker jest w 100% w Compose, bez ryzyka ANR.
 */
private val EMOJI_CHOICES = listOf(
    "🍔", "🍕", "🍜", "🍣", "🍱", "🥗", "🍿", "🍩", "🍫", "🍷", "🍺", "🍹", "☕", "🧋", "🥤",
    "🚗", "🚕", "🚌", "🚈", "🚲", "✈️", "🚀", "⛽", "🅿️", "🚕",
    "🏠", "🏡", "🏢", "🛋️", "🛏️", "🚿", "🧹", "🧺", "💡", "🔌",
    "🛍️", "🛒", "👗", "👟", "👜", "💄", "💅", "💇",
    "🎮", "🎲", "🎬", "🎧", "🎤", "🎨", "🖼️", "📚", "🎓",
    "💊", "🩺", "🏋️", "🧘", "🦷", "🧴", "🧼",
    "💻", "📱", "⌚", "🖨️", "📷", "🔋", "🔧", "🧰",
    "💰", "💳", "🏦", "📈", "📉", "🧾", "🎁", "🎉", "🎂",
    "🐶", "🐱", "🐦", "🐟", "🌱", "🌸", "🌍",
    "⚽", "🏀", "🎾", "🏈", "⛳", "🏊", "🚴",
    "📝", "✏️", "📌", "📅", "⏰", "🔔", "❤️", "⭐", "✨", "🔥", "💧", "☀️", "🌙",
    "🧳", "🗺️", "🏖️", "⛰️", "🎡", "🏷️",
)

/** Formularz "Nowa kategoria" / "Edytuj kategorię" — pełny CRUD kategorii wydatków. */
@Composable
fun CategoryFormScreen(categoryId: String? = null, onDone: () -> Unit) {
    val context = LocalContext.current
    val app = remember(context) { ZlotowkaApp.from(context) }
    val viewModel: CategoryFormViewModel = viewModel(
        factory = viewModelFactory { initializer { CategoryFormViewModel(app.repository, categoryId) } },
    )
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val isEditMode = categoryId != null

    var initialized by remember { mutableStateOf(false) }
    var label by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf(DEFAULT_EMOJI) }
    var tone by remember { mutableStateOf(ColorTone.Lime) }
    var budgetText by remember { mutableStateOf("") }
    var isImpulse by remember { mutableStateOf(false) }
    var kind by remember { mutableStateOf(CategoryKind.EXPENSE) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.existing) {
        val existing = uiState.existing
        if (!initialized && existing != null) {
            label = existing.label
            emoji = existing.emoji
            tone = existing.colorToken.toColorTone()
            budgetText = minorToAmountText(existing.monthlyBudgetMinor)
            isImpulse = existing.isImpulse
            kind = existing.kind
            initialized = true
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Edytuj kategorię" else "Nowa kategoria",
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
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
        ) {
            if (!isEditMode) {
                SectionLabel(text = "Rodzaj", modifier = Modifier.padding(top = 16.dp))
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Pill(
                        text = "Wydatek",
                        accent = Coral,
                        filled = kind == CategoryKind.EXPENSE,
                        modifier = Modifier.clickable { kind = CategoryKind.EXPENSE },
                    )
                    Pill(
                        text = "Dochód",
                        accent = Lime,
                        filled = kind == CategoryKind.INCOME,
                        modifier = Modifier.clickable { kind = CategoryKind.INCOME },
                    )
                }
            } else {
                SectionLabel(text = "Rodzaj", modifier = Modifier.padding(top = 16.dp))
                Text(
                    text = if (kind == CategoryKind.INCOME) "Dochód" else "Wydatek",
                    color = if (kind == CategoryKind.INCOME) Lime else Coral,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            SectionLabel(text = "Emoji", modifier = Modifier.padding(top = 24.dp))
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Surface)
                    .border(1.dp, BorderOnDark, RoundedCornerShape(18.dp))
                    .clickable { showEmojiPicker = true },
                contentAlignment = Alignment.Center,
            ) {
                Text(text = emoji, fontSize = 32.sp)
            }

            SectionLabel(text = "Nazwa", modifier = Modifier.padding(top = 24.dp))
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                placeholder = { Text("np. Jedzenie", color = TextMuted) },
                singleLine = true,
                textStyle = TextStyle(fontSize = 16.sp, color = TextPrimary),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Lime,
                    unfocusedBorderColor = Lime.copy(alpha = 0.4f),
                    focusedContainerColor = Surface,
                    unfocusedContainerColor = Surface,
                ),
            )

            SectionLabel(text = "Kolor", modifier = Modifier.padding(top = 24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                ColorTone.entries.forEach { candidate ->
                    val selected = candidate == tone
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(candidate.toColor())
                            .border(
                                width = if (selected) 3.dp else 1.dp,
                                color = if (selected) TextPrimary else BorderOnDark,
                                shape = CircleShape,
                            )
                            .clickable { tone = candidate },
                    )
                }
            }

            // Miesięczny budżet i seria "impulsowa" mają sens tylko dla wydatków — dochodom się ich nie limituje.
            if (kind == CategoryKind.EXPENSE) {
                SectionLabel(text = "Miesięczny budżet", modifier = Modifier.padding(top = 24.dp))
                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { input ->
                        if (input.count { it == ',' || it == '.' } <= 1) budgetText = input
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    placeholder = { Text("0,00", color = TextMuted) },
                    suffix = { Text("zł", color = Lime, fontSize = 14.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = TextStyle(fontSize = 16.sp, color = TextPrimary),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Lime,
                        unfocusedBorderColor = Lime.copy(alpha = 0.4f),
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = Surface,
                    ),
                )

                SurfaceCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Kategoria impulsowa",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = "Wydatki w tej kategorii przerywają serię dni bez impulsu.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                        Switch(
                            checked = isImpulse,
                            onCheckedChange = { isImpulse = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = Lime, checkedThumbColor = Surface),
                        )
                    }
                }
            }

            PrimaryPillButton(
                text = if (isEditMode) "Zapisz zmiany" else "Zapisz kategorię",
                modifier = Modifier.padding(top = 28.dp),
                onClick = {
                    if (label.isNotBlank()) {
                        val budgetMinor = if (kind == CategoryKind.EXPENSE) parseAmountToMinor(budgetText) else 0L
                        val savedLabel = label.trim()
                        val savedEmoji = emoji
                        val savedTone = tone
                        val savedImpulse = kind == CategoryKind.EXPENSE && isImpulse
                        val savedKind = kind
                        scope.launch {
                            viewModel.save(
                                label = savedLabel,
                                emoji = savedEmoji,
                                colorToken = savedTone.name.lowercase(),
                                monthlyBudgetMinor = budgetMinor,
                                isImpulse = savedImpulse,
                                kind = savedKind,
                            )
                            onDone()
                        }
                    }
                },
            )

            if (isEditMode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .clip(PillShape)
                        .border(1.dp, Coral.copy(alpha = 0.5f), PillShape)
                        .clickable { showDeleteConfirm = true }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "Usuń kategorię", color = Coral, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }
        }
    }

    if (showEmojiPicker) {
        Dialog(onDismissRequest = { showEmojiPicker = false }) {
            SurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionLabel(text = "Wybierz emoji")
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp)
                            .padding(top = 8.dp),
                    ) {
                        items(EMOJI_CHOICES) { candidate ->
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (candidate == emoji) Lime.copy(alpha = 0.18f) else Surface)
                                    .clickable {
                                        emoji = candidate
                                        showEmojiPicker = false
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(text = candidate, fontSize = 22.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(text = "Usunąć kategorię?", color = TextPrimary) },
            text = {
                val fallbackLabel = if (kind == CategoryKind.INCOME) "Inne wpływy" else "Inne"
                Text(
                    text = "Dotychczasowe transakcje z tej kategorii zostaną przeniesione do kategorii \"$fallbackLabel\".",
                    color = TextMuted,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        scope.launch {
                            viewModel.delete()
                            onDone()
                        }
                    },
                ) {
                    Text(text = "Usuń", color = Coral, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(text = "Anuluj", color = TextMuted)
                }
            },
            containerColor = Surface,
        )
    }
}

private fun String.toColorTone(): ColorTone =
    ColorTone.entries.firstOrNull { it.name.equals(this, ignoreCase = true) } ?: ColorTone.Muted

/** Parsuje wpisany tekst kwoty ("34,90" lub "34.90") na grosze. Nieprawidłowy wpis => 0. */
private fun parseAmountToMinor(input: String): Long {
    val normalized = input.trim().replace(',', '.')
    if (normalized.isEmpty()) return 0L
    val value = normalized.toDoubleOrNull() ?: return 0L
    return Math.round(value * 100)
}

/** Odwrotność [parseAmountToMinor] — do prefillowania pola przy edycji istniejącej kategorii. */
private fun minorToAmountText(minor: Long): String {
    if (minor <= 0) return ""
    val whole = minor / 100
    val frac = minor % 100
    return "$whole,${frac.toString().padStart(2, '0')}"
}
