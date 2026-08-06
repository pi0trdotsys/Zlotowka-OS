package pl.nullpointerstudio.zlotowka.notifications

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import pl.nullpointerstudio.zlotowka.R
import pl.nullpointerstudio.zlotowka.ZlotowkaApp
import pl.nullpointerstudio.zlotowka.domain.toPln
import pl.nullpointerstudio.zlotowka.ui.nav.Destinations
import kotlin.random.Random

/**
 * Krótka, zachęcająca do oszczędzania wskazówka — uruchamiana raz dziennie (rano) przez
 * [NotificationScheduler]. Część wiadomości personalizujemy realnym streakiem/Pulsem,
 * żeby nie było to oderwane od tego, co użytkownik faktycznie robi.
 */
class MotivationalTipWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = ZlotowkaApp.from(applicationContext)
        val settings = app.settingsRepository.settings.first()
        if (!settings.motivationalTipsEnabled) return Result.success()

        if (!NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) {
            return Result.success()
        }

        val snapshot = app.repository.motivationSnapshot.first()

        // Część rano personalizujemy realnymi danymi zamiast ogólnej ciekawostki.
        val personalized = buildList {
            if (snapshot.streakDays > 0) {
                add("🔥 Jesteś w serii ${snapshot.streakDays} dni bez wydatku impulsowego — nie przerywaj jej dziś!")
            }
            val badge = snapshot.nextBadge
            if (badge != null && badge.second <= 3) {
                add("🏅 Jeszcze ${badge.second} dni i odblokujesz odznakę „${badge.first.name}”.")
            }
            if (snapshot.score >= 80) {
                add("⚡ Twój Puls oszczędzania to ${snapshot.score}/100 — świetna forma, tak trzymaj!")
            }
            if (snapshot.hasIncomePlan && snapshot.dailyLeftMinor > 0) {
                add("💸 Masz dziś do wydania ${snapshot.dailyLeftMinor.toPln()} zgodnie z planem. Rozsądnie to rozłóż!")
            }
        }

        val pool = if (personalized.isNotEmpty() && Random.nextInt(100) < 50) personalized else GENERIC_TIPS
        val message = pool.random()

        val notification = NotificationCompat.Builder(applicationContext, NotificationChannels.CHANNEL_MOTIVATION)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Motywacja na dziś")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .setContentIntent(
                NotificationChannels.buildContentIntent(
                    applicationContext,
                    Destinations.DASHBOARD,
                    NotificationChannels.motivationNotificationId(),
                ),
            )
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(NotificationChannels.motivationNotificationId(), notification)

        return Result.success()
    }

    private companion object {
        val GENERIC_TIPS = listOf(
            "💡 Zasada 24 godzin: przy większym zakupie odczekaj dzień — impuls zwykle mija.",
            "🧾 Zapisuj wydatek od razu po zapłacie — łatwiej niż odtwarzać z pamięci wieczorem.",
            "☕ Domowa kawa zamiast tej na mieście to nawet kilkaset złotych oszczędności miesięcznie.",
            "🛒 Idź na zakupy z listą — impulsowe dokładki w koszyku to najczęstszy wyciek budżetu.",
            "📉 Małe, regularne oszczędności biją jednorazowe wielkie postanowienia.",
            "🎯 Nazwij swój cel konkretnie — „Bieszczady we wrześniu” motywuje bardziej niż „oszczędności”.",
            "🔁 Ustaw automatyczny przelew na cel zaraz po wypłacie — odkładaj, zanim wydasz.",
            "🧊 Zamroź impuls: dodaj coś do koszyka i wróć do niego jutro.",
            "📱 Sprawdź subskrypcje — czy na pewno korzystasz ze wszystkich, za które płacisz?",
            "🍽️ Gotowanie w domu zamiast jedzenia na mieście to jedna z największych dźwigni budżetu.",
            "🪙 Każda odłożona złotówka to głos na Twoją przyszłość, nie na dzisiejszy impuls.",
            "📊 Raz w tygodniu zerknij na Porównania — świadomość wzorców to połowa sukcesu.",
        )
    }
}
