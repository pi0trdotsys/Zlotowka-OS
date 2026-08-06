@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package pl.nullpointerstudio.zlotowka.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pl.nullpointerstudio.zlotowka.BuildConfig
import pl.nullpointerstudio.zlotowka.ZlotowkaApp
import pl.nullpointerstudio.zlotowka.data.NotificationSettings
import pl.nullpointerstudio.zlotowka.notifications.NotificationChannels
import pl.nullpointerstudio.zlotowka.ui.components.PrimaryPillButton
import pl.nullpointerstudio.zlotowka.ui.components.SectionLabel
import pl.nullpointerstudio.zlotowka.ui.components.SurfaceCard
import pl.nullpointerstudio.zlotowka.ui.mascot.Mascot
import pl.nullpointerstudio.zlotowka.domain.MascotMood
import pl.nullpointerstudio.zlotowka.ui.theme.Lime
import pl.nullpointerstudio.zlotowka.ui.theme.TextMuted
import pl.nullpointerstudio.zlotowka.ui.theme.TextPrimary

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = remember(context) { ZlotowkaApp.from(context) }
    val settings by app.settingsRepository.settings.collectAsState(initial = NotificationSettings())
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Ustawienia", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            SectionLabel(text = "Powiadomienia", modifier = Modifier.padding(top = 12.dp))

            SurfaceCard(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Column(modifier = Modifier.padding(4.dp)) {
                    SettingsToggleRow(
                        label = "Codzienne podsumowanie",
                        checked = settings.dailySummaryEnabled,
                        onCheckedChange = { enabled ->
                            scope.launch { app.settingsRepository.setDailySummaryEnabled(enabled) }
                        },
                    )
                    SettingsToggleRow(
                        label = "Raport tygodniowy",
                        checked = settings.weeklyReportEnabled,
                        onCheckedChange = { enabled ->
                            scope.launch { app.settingsRepository.setWeeklyReportEnabled(enabled) }
                        },
                    )
                    SettingsToggleRow(
                        label = "Motywacyjne wskazówki",
                        checked = settings.motivationalTipsEnabled,
                        onCheckedChange = { enabled ->
                            scope.launch { app.settingsRepository.setMotivationalTipsEnabled(enabled) }
                        },
                    )
                    SettingsToggleRow(
                        label = "Dźwięk powiadomień",
                        checked = settings.soundEnabled,
                        onCheckedChange = { enabled ->
                            scope.launch { app.settingsRepository.setSoundEnabled(enabled) }
                        },
                    )
                }
            }

            PrimaryPillButton(
                text = "Testuj powiadomienie",
                modifier = Modifier.padding(top = 20.dp),
                onClick = { NotificationChannels.sendTestNotification(context) },
            )

            SectionLabel(text = "O aplikacji", modifier = Modifier.padding(top = 32.dp))
            SurfaceCard(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Mascot(mood = MascotMood.HAPPY, size = 72.dp)
                    Text(
                        text = "ZŁOTÓWKA OS",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                    Text(
                        text = "Wersja ${BuildConfig.VERSION_NAME}",
                        color = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Text(
                        text = "NULLPOINTER STUDIO",
                        color = TextMuted,
                        fontSize = 10.sp,
                        letterSpacing = 3.sp,
                        modifier = Modifier.padding(top = 20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, color = TextPrimary, fontSize = 13.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = Lime),
        )
    }
}
