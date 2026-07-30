package pl.nullpointerstudio.zlotowka.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import pl.nullpointerstudio.zlotowka.ui.nav.AppNavHost
import pl.nullpointerstudio.zlotowka.ui.nav.Destinations
import pl.nullpointerstudio.zlotowka.ui.splash.SplashScreen
import pl.nullpointerstudio.zlotowka.ui.theme.ZlotowkaTheme

/** Punkt wejścia: instaluje splash systemowy, potem pokazuje własny SplashScreen (mascot), a na końcu graf nawigacji. */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // installSplashScreen() MUSI być wywołane przed super.onCreate()/setContent, zgodnie z dokumentacją core-splashscreen.
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            ZlotowkaTheme {
                var showSplash by remember { mutableStateOf(true) }
                if (showSplash) {
                    SplashScreen(onFinished = { showSplash = false })
                } else {
                    val navController = rememberNavController()
                    AppNavHost(
                        navController = navController,
                        startRoute = intent.getStringExtra(Destinations.EXTRA_ROUTE),
                    )
                }
            }
        }
    }
}
