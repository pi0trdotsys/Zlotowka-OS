package pl.nullpointerstudio.zlotowka.ui.mascot

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import pl.nullpointerstudio.zlotowka.domain.MascotMood
import pl.nullpointerstudio.zlotowka.ui.theme.Background
import pl.nullpointerstudio.zlotowka.ui.theme.Cyan
import pl.nullpointerstudio.zlotowka.ui.theme.Coral
import pl.nullpointerstudio.zlotowka.ui.theme.Lime
import pl.nullpointerstudio.zlotowka.ui.theme.OnLime
import pl.nullpointerstudio.zlotowka.ui.theme.ZlotowkaTheme

/**
 * "Grosik" — maskotka złotówki (moneta z antenką i błyskawicą "zł" na piersi), ten sam
 * charakter co statyczne [pl.nullpointerstudio.zlotowka.R.drawable.ic_splash_mascot] /
 * `ic_launcher_foreground`, ale przerysowany natywnie na Canvas/DrawScope, żeby dało się go
 * animować w zależności od aktualnego nastroju użytkownika ([MascotMood]).
 *
 * Zawsze delikatnie "oddycha" i co jakiś czas mruga, niezależnie od nastroju; dodatkowo
 * każdy nastrój ma własny akcent ruchu/koloru (patrz opis przy [MascotMood]).
 */
@Composable
fun Mascot(
    mood: MascotMood,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
) {
    val transition = rememberInfiniteTransition(label = "mascotIdle")

    // Oddech — zawsze aktywny, niezależnie od nastroju, ~2s pełen cykl.
    val breathe by transition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathe",
    )

    // Mruganie — oczy zwężają się do cienkiej kreski na chwilę, mniej więcej co 4s.
    val eyeOpen by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4000
                1f at 0
                1f at 3650
                0.06f at 3800
                1f at 3950
                1f at 4000
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "eyeOpen",
    )

    // Fala napędzająca akcent nastroju: podskok (HAPPY) / drżenie (WORRIED, ALARMED).
    val moodWave by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (mood) {
                    MascotMood.HAPPY -> 900
                    MascotMood.WORRIED -> 420
                    MascotMood.ALARMED -> 220
                    MascotMood.THRIVING, MascotMood.NEUTRAL -> 2000
                },
                easing = if (mood == MascotMood.WORRIED || mood == MascotMood.ALARMED) {
                    LinearEasing
                } else {
                    FastOutSlowInEasing
                },
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "moodWave",
    )

    // Puls poświaty/koloru czubka antenki: THRIVING (miga) i ALARMED (limonka -> koral).
    val glowPulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (mood == MascotMood.ALARMED) 350 else 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glowPulse",
    )

    // Kąt iskierek krążących koło czubka antenki (tylko THRIVING).
    val sparkleAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "sparkleAngle",
    )

    val bodyColor = Lime
    val rimColor = OnLime.copy(alpha = 0.42f)
    val markColor = Background
    val eyeColor = Background
    val mouthColor = Background
    val antennaTipColor = if (mood == MascotMood.ALARMED) lerp(Cyan, Coral, glowPulse) else Cyan
    val sparkleColors = listOf(Lime, Cyan)

    Canvas(modifier = modifier.size(size)) {
        val s = this.size.minDimension
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f

        val bounceOffset = if (mood == MascotMood.HAPPY) -s * 0.05f * moodWave else 0f
        val shakeAmplitude = when (mood) {
            MascotMood.WORRIED -> s * 0.012f
            MascotMood.ALARMED -> s * 0.02f
            else -> 0f
        }
        val shakeOffset = shakeAmplitude * (moodWave * 2f - 1f)

        translate(left = shakeOffset, top = bounceOffset) {
            scale(scale = breathe, pivot = Offset(cx, cy)) {
                drawGrosik(
                    s = s,
                    cx = cx,
                    cy = cy,
                    mood = mood,
                    eyeOpen = eyeOpen,
                    bodyColor = bodyColor,
                    rimColor = rimColor,
                    markColor = markColor,
                    eyeColor = eyeColor,
                    mouthColor = mouthColor,
                    antennaTipColor = antennaTipColor,
                    glowPulse = glowPulse,
                    sparkleAngle = sparkleAngle,
                    sparkleColors = sparkleColors,
                )
            }
        }
    }
}

/** Rysuje Grosika na przekazanej [DrawScope] — geometria jako ułamki wymiaru [s]. */
private fun DrawScope.drawGrosik(
    s: Float,
    cx: Float,
    cy: Float,
    mood: MascotMood,
    eyeOpen: Float,
    bodyColor: Color,
    rimColor: Color,
    markColor: Color,
    eyeColor: Color,
    mouthColor: Color,
    antennaTipColor: Color,
    glowPulse: Float,
    sparkleAngle: Float,
    sparkleColors: List<Color>,
) {
    val bodyCenter = Offset(cx, cy + s * 0.02f)
    val bodyRadius = s * 0.32f

    // Stopki.
    val footRx = s * 0.045f
    val footRy = s * 0.035f
    val footY = bodyCenter.y + bodyRadius * 0.92f
    for (side in intArrayOf(-1, 1)) {
        drawOval(
            color = rimColor,
            topLeft = Offset(bodyCenter.x + side * s * 0.155f - footRx, footY - footRy),
            size = Size(footRx * 2, footRy * 2),
        )
    }

    // Ciało (moneta) + wewnętrzna obwódka.
    drawCircle(color = bodyColor, radius = bodyRadius, center = bodyCenter)
    drawCircle(
        color = rimColor,
        radius = bodyRadius * 0.72f,
        center = bodyCenter,
        style = Stroke(width = s * 0.016f),
    )

    // Znaczek "zł" (błyskawica) na piersi.
    val markW = bodyRadius * 0.62f
    val markH = bodyRadius * 0.9f
    val mox = bodyCenter.x
    val moy = bodyCenter.y + bodyRadius * 0.06f
    val markPath = Path().apply {
        moveTo(mox - markW * 0.1f, moy - markH * 0.5f)
        lineTo(mox + markW * 0.5f, moy - markH * 0.5f)
        lineTo(mox - markW * 0.05f, moy + markH * 0.05f)
        lineTo(mox + markW * 0.45f, moy + markH * 0.05f)
        lineTo(mox - markW * 0.5f, moy + markH * 0.5f)
        lineTo(mox - markW * 0.05f, moy + markH * 0.05f)
        lineTo(mox - markW * 0.55f, moy + markH * 0.05f)
        close()
    }
    drawPath(markPath, color = markColor)

    // Oczy — przy mruganiu spłaszczają się w cienką kreskę.
    val eyeY = bodyCenter.y - bodyRadius * 0.34f
    val eyeOffsetX = bodyRadius * 0.36f
    val eyeR = bodyRadius * 0.135f
    for (side in intArrayOf(-1, 1)) {
        val ex = bodyCenter.x + side * eyeOffsetX
        val halfH = (eyeR * eyeOpen).coerceAtLeast(s * 0.004f)
        drawOval(
            color = eyeColor,
            topLeft = Offset(ex - eyeR, eyeY - halfH),
            size = Size(eyeR * 2, halfH * 2),
        )
    }

    // Usta — kształt zależny od nastroju.
    val mouthCenter = Offset(bodyCenter.x, bodyCenter.y - bodyRadius * 0.02f)
    val mouthW = bodyRadius * 0.62f
    when (mood) {
        MascotMood.THRIVING -> drawArc(
            color = mouthColor,
            startAngle = 12f,
            sweepAngle = 156f,
            useCenter = false,
            topLeft = Offset(mouthCenter.x - mouthW / 2, mouthCenter.y - mouthW / 2),
            size = Size(mouthW, mouthW),
            style = Stroke(width = mouthW * 0.22f, cap = StrokeCap.Round),
        )
        MascotMood.HAPPY -> drawArc(
            color = mouthColor,
            startAngle = 24f,
            sweepAngle = 132f,
            useCenter = false,
            topLeft = Offset(mouthCenter.x - mouthW / 2, mouthCenter.y - mouthW / 2),
            size = Size(mouthW, mouthW),
            style = Stroke(width = mouthW * 0.20f, cap = StrokeCap.Round),
        )
        MascotMood.NEUTRAL -> drawArc(
            color = mouthColor,
            startAngle = 36f,
            sweepAngle = 108f,
            useCenter = false,
            topLeft = Offset(mouthCenter.x - mouthW / 2, mouthCenter.y - mouthW / 2),
            size = Size(mouthW, mouthW),
            style = Stroke(width = mouthW * 0.18f, cap = StrokeCap.Round),
        )
        MascotMood.WORRIED -> drawLine(
            color = mouthColor,
            start = Offset(mouthCenter.x - mouthW * 0.42f, mouthCenter.y),
            end = Offset(mouthCenter.x + mouthW * 0.42f, mouthCenter.y),
            strokeWidth = mouthW * 0.17f,
            cap = StrokeCap.Round,
        )
        MascotMood.ALARMED -> drawCircle(
            color = mouthColor,
            radius = mouthW * 0.22f,
            center = mouthCenter,
        )
    }

    // Antenka — lekko opada przy WORRIED.
    val antennaBase = Offset(bodyCenter.x, bodyCenter.y - bodyRadius * 0.98f)
    val droop = if (mood == MascotMood.WORRIED) s * 0.05f else 0f
    val antennaTip = Offset(antennaBase.x + droop, antennaBase.y - s * 0.16f)
    drawLine(
        color = rimColor,
        start = antennaBase,
        end = antennaTip,
        strokeWidth = s * 0.014f,
        cap = StrokeCap.Round,
    )
    if (mood == MascotMood.THRIVING) {
        drawCircle(
            color = antennaTipColor.copy(alpha = 0.28f + glowPulse * 0.25f),
            radius = s * (0.045f + glowPulse * 0.02f),
            center = antennaTip,
        )
    }
    drawCircle(color = antennaTipColor, radius = s * 0.028f, center = antennaTip)

    // Iskierki orbitujące koło antenki (tylko THRIVING).
    if (mood == MascotMood.THRIVING) {
        val orbitR = s * 0.09f
        val angles = floatArrayOf(0f, 120f, 240f)
        for (i in angles.indices) {
            val angleRad = ((sparkleAngle + angles[i]) * (PI / 180.0)).toFloat()
            val sx = antennaTip.x + orbitR * cos(angleRad)
            val sy = antennaTip.y + orbitR * sin(angleRad) * 0.6f
            drawCircle(
                color = sparkleColors[i % sparkleColors.size].copy(alpha = 0.85f),
                radius = s * 0.014f,
                center = Offset(sx, sy),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MascotPreview() {
    ZlotowkaTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                for (mood in MascotMood.entries) {
                    Mascot(mood = mood, size = 72.dp)
                }
            }
        }
    }
}
