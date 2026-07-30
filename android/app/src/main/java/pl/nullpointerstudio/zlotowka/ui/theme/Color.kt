package pl.nullpointerstudio.zlotowka.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * ZŁOTÓWKA OS — design tokens.
 * 1:1 z src/styles.css makiety: głęboka grafitowa noc + neonowa limonka jako akcent.
 */

val Background = Color(0xFF14161A)
val Surface = Color(0xFF1D2026)
val Surface2 = Color(0xFF262A31)

val Lime = Color(0xFFC8F751)
val Cyan = Color(0xFF6FE3E1)
val Coral = Color(0xFFF2704E)
val Amber = Color(0xFFF0B34A)
val Violet = Color(0xFFB48CF2)

val TextPrimary = Color(0xFFF4F5F7)
val TextMuted = Color(0xFF9AA1AC)

val OnLime = Color(0xFF14210C)
val OnCyan = Color(0xFF0C2323)

val BorderOnDark = Color(0x17FFFFFF)
val InputOnDark = Color(0x1FFFFFFF)

/** Tony kategorii — mapowanie 1:1 z `Category["tone"]` w mock.ts. */
enum class ColorTone { Lime, Cyan, Coral, Amber, Violet, Muted }

fun ColorTone.toColor(): Color = when (this) {
    ColorTone.Lime -> Lime
    ColorTone.Cyan -> Cyan
    ColorTone.Coral -> Coral
    ColorTone.Amber -> Amber
    ColorTone.Violet -> Violet
    ColorTone.Muted -> TextMuted
}
