package pl.nullpointerstudio.zlotowka.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.nullpointerstudio.zlotowka.domain.toPln
import pl.nullpointerstudio.zlotowka.ui.theme.AmountTextStyle
import pl.nullpointerstudio.zlotowka.ui.theme.BorderOnDark
import pl.nullpointerstudio.zlotowka.ui.theme.Lime
import pl.nullpointerstudio.zlotowka.ui.theme.OnLime
import pl.nullpointerstudio.zlotowka.ui.theme.PillShape
import pl.nullpointerstudio.zlotowka.ui.theme.Surface
import pl.nullpointerstudio.zlotowka.ui.theme.Surface2
import pl.nullpointerstudio.zlotowka.ui.theme.TextMuted

/** Kwota — ZAWSZE monospace z tabular figures, żeby cyfry nie „skakały" przy animacji licznika. */
@Composable
fun AmountText(
    minor: Long,
    modifier: Modifier = Modifier,
    withSign: Boolean = false,
    fontSize: TextUnit = 22.sp,
    color: Color = MaterialTheme.colorScheme.onBackground,
) {
    Text(
        text = minor.toPln(withSign),
        modifier = modifier,
        style = AmountTextStyle.copy(fontSize = fontSize, color = color),
    )
}

/** Etykieta sekcji: małe litery rozstrzelone, uppercase, wyciszony kolor. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, color: Color = TextMuted) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = TextStyle(
            fontSize = 11.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Medium,
            color = color,
        ),
    )
}

/** Karta w stylu makiety: surface + subtelna obwódka + duże zaokrąglenie. */
@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = Surface,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, BorderOnDark, shape),
        content = content,
    )
}

/** Cienki pasek postępu z animowaną szerokością — jak w makiecie (h-1.5/h-2 rounded-full). */
@Composable
fun ProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp,
    color: Color = Lime,
    trackColor: Color = Surface2,
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(650),
        label = "progress",
    )
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(999.dp))
            .background(trackColor),
    ) {
        val fullWidth = maxWidth
        val animatedWidth by animateDpAsState(targetValue = fullWidth * animated, animationSpec = tween(650), label = "progressWidth")
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(animatedWidth)
                .clip(RoundedCornerShape(999.dp))
                .background(color),
        )
    }
}

/** Pigułka (chip) — metody płatności, tagi, CTA drugorzędne. */
@Composable
fun Pill(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color = TextMuted,
    filled: Boolean = false,
    onSurfaceTint: Color = Surface,
) {
    Box(
        modifier = modifier
            .clip(PillShape)
            .then(
                if (filled) Modifier.background(accent.copy(alpha = 0.14f))
                else Modifier.background(onSurfaceTint),
            )
            .border(1.dp, accent.copy(alpha = if (filled) 0.45f else 0.3f), PillShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(text = text, color = accent, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

/** Delikatna neonowa poświata pod limonkowymi elementami (odpowiednik --shadow-neon z CSS). */
fun Modifier.neonGlow(color: Color, elevation: Dp = 14.dp, shape: Shape = PillShape): Modifier = this.shadow(
    elevation = elevation,
    shape = shape,
    ambientColor = color.copy(alpha = 0.55f),
    spotColor = color.copy(alpha = 0.55f),
)

/** Główny CTA — limonkowa pigułka z neonową poświatą, jak przycisk "Zapisz wydatek". */
@Composable
fun PrimaryPillButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .neonGlow(Lime)
            .clip(PillShape)
            .background(Lime)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, color = OnLime, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}
