package pl.nullpointerstudio.zlotowka.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.nullpointerstudio.zlotowka.domain.MascotMood
import pl.nullpointerstudio.zlotowka.ui.mascot.Mascot
import pl.nullpointerstudio.zlotowka.ui.theme.Background
import pl.nullpointerstudio.zlotowka.ui.theme.Lime
import pl.nullpointerstudio.zlotowka.ui.theme.TextMuted
import pl.nullpointerstudio.zlotowka.ui.theme.TextPrimary
import pl.nullpointerstudio.zlotowka.ui.theme.ZlotowkaTheme

/** Losowo (deterministycznie na bieżącą sekundę) dobrana ciekawostka o złotówce pod maskotką. */
private val PLN_FACTS = listOf(
    "Grosz to 1/100 złotego — stąd ksywka Grosik.",
    "Symbol „zł” to skrót od „złoty” — waluty Polski od 1919 roku.",
    "W 1995 roku denominacja wymieniła 10 000 starych złotych na 1 nowy.",
    "Na monetach 5 zł od 1995 roku widnieje orzeł w koronie.",
    "Nazwa „złoty” pochodzi od złotych dukatów używanych w dawnej Polsce.",
    "Pierwsze polskie banknoty złotowe wydrukowano w 1924 roku.",
    "Przed złotym w obiegu były marki polskie — zdewaluowane przez hiperinflację lat 20.",
    "Na banknocie 20 zł znajduje się Kazimierz III Wielki.",
    "Skrót „PLN” to międzynarodowy kod ISO 4217 dla złotego po denominacji 1995 roku.",
    "Stary kod złotego (sprzed 1995) to „PLZ” — dziś już nieużywany.",
    "Moneta 2 zł z 1995 roku to jedna z pierwszych monet nowego złotego.",
    "Grosz jako nazwa pochodzi od łacińskiego „grossus” — „gruby” (dawniej grubsza moneta).",
    "Polska nie należy do strefy euro — złoty pozostaje walutą narodową od ponad 100 lat.",
    "Zasada 50/30/20: 50% budżetu na potrzeby, 30% na chcenia, 20% na oszczędności.",
    "Metoda koperty: fizyczny (lub cyfrowy) podział budżetu na kategorie ogranicza przepłacanie.",
    "Automatyczne odkładanie „zaraz po wypłacie” działa lepiej niż odkładanie „tego, co zostanie”.",
)

/**
 * Ekran powitalny — animowane wejście maskotki, wordmark "ZŁOTÓWKA OS" i ciekawostka o PLN.
 * Sam znika po ~1.8s wywołując [onFinished]. Zakłada, że [ZlotowkaTheme] jest już zaaplikowany
 * przez wywołującego (np. MainActivity) — tutaj się w niego NIE opakowuje.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val factIndex = remember { (System.currentTimeMillis() / 1000 % PLN_FACTS.size).toInt() }

    val entranceScale = remember { Animatable(0.72f) }
    val entranceAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            entranceScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
            )
        }
        launch {
            entranceAlpha.animateTo(targetValue = 1f, animationSpec = tween(600))
        }
        delay(1800)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp),
        ) {
            Mascot(
                mood = MascotMood.HAPPY,
                size = 120.dp,
                modifier = Modifier
                    .scale(entranceScale.value)
                    .alpha(entranceAlpha.value),
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = TextPrimary)) { append("ZŁOTÓWKA ") }
                    withStyle(SpanStyle(color = Lime)) { append("OS") }
                },
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.alpha(entranceAlpha.value),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = PLN_FACTS[factIndex],
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(entranceAlpha.value),
            )
        }

        Text(
            text = "NullPointer Studio".uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    ZlotowkaTheme {
        SplashScreen(onFinished = {})
    }
}
