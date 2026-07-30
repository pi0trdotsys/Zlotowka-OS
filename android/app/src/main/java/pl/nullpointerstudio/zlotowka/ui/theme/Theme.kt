package pl.nullpointerstudio.zlotowka.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ZlotowkaColorScheme = darkColorScheme(
    primary = Lime,
    onPrimary = OnLime,
    secondary = Cyan,
    onSecondary = OnCyan,
    tertiary = Violet,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = Surface2,
    onSurfaceVariant = TextMuted,
    error = Coral,
    onError = Color(0xFF2A0D06),
    outline = BorderOnDark,
    outlineVariant = InputOnDark,
)

/** Aplikacja jest zawsze dark (future-minimalizm) — niezależnie od motywu systemu. */
@Composable
fun ZlotowkaTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = Background.toArgb()
            window.navigationBarColor = Background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }
    MaterialTheme(
        colorScheme = ZlotowkaColorScheme,
        typography = ZlotowkaTypography,
        shapes = ZlotowkaShapes,
        content = content,
    )
}
