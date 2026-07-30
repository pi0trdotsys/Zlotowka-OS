package pl.nullpointerstudio.zlotowka.data

import pl.nullpointerstudio.zlotowka.domain.milestonesFor
import java.util.Calendar

/**
 * Dane startowe — przełożenie 1:1 danych z makiety (src/data/mock.ts) na encje Room.
 * Znaczniki czasu liczone względem "teraz", żeby demo zawsze wyglądało świeżo,
 * z zachowaniem tych samych przesunięć dni co w makiecie (referencyjne "dziś" = 2026-07-30).
 */
object SeedData {

    private fun daysAgo(days: Int, hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -days)
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun daysFromNow(days: Int): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, days)
        return cal.timeInMillis
    }

    fun categories(): List<CategoryEntity> = listOf(
        CategoryEntity("jedzenie", "Jedzenie", "🥦", "lime", 120_000, 0, isImpulse = true),
        CategoryEntity("transport", "Transport", "🚇", "cyan", 40_000, 1),
        CategoryEntity("mieszkanie", "Mieszkanie", "🏠", "violet", 230_000, 2),
        CategoryEntity("rozrywka", "Rozrywka", "🎧", "coral", 50_000, 3, isImpulse = true),
        CategoryEntity("zdrowie", "Zdrowie", "💊", "amber", 30_000, 4),
        CategoryEntity("subskrypcje", "Subskrypcje", "📺", "muted", 15_000, 5),
        CategoryEntity("inne", "Inne", "💰", "muted", 0, 6),
    )

    fun transactions(): List<TransactionEntity> = listOf(
        TransactionEntity(title = "Żabka — kawa i drożdżówka", categoryId = "jedzenie", amountMinor = -1490, timestamp = daysAgo(0, 8, 12), method = PaymentMethod.BLIK),
        TransactionEntity(title = "Bilet miesięczny ZTM", categoryId = "transport", amountMinor = -11000, timestamp = daysAgo(1, 18, 40), method = PaymentMethod.CARD),
        TransactionEntity(title = "Wypłata", categoryId = "inne", amountMinor = 812000, timestamp = daysAgo(2, 9, 0), method = PaymentMethod.TRANSFER),
        TransactionEntity(title = "Spotify Family", categoryId = "subskrypcje", amountMinor = -2999, timestamp = daysAgo(3, 12, 5), method = PaymentMethod.CARD),
        TransactionEntity(title = "Biedronka — duże zakupy", categoryId = "jedzenie", amountMinor = -23784, timestamp = daysAgo(4, 17, 22), method = PaymentMethod.BLIK),
        TransactionEntity(title = "Kino Helios", categoryId = "rozrywka", amountMinor = -4800, timestamp = daysAgo(5, 20, 10), method = PaymentMethod.CASH),
        TransactionEntity(title = "Żabka — lunch na mieście", categoryId = "jedzenie", amountMinor = -3490, timestamp = daysAgo(6, 13, 10), method = PaymentMethod.BLIK),
        TransactionEntity(title = "Apteka", categoryId = "zdrowie", amountMinor = -9900, timestamp = daysAgo(8, 10, 30), method = PaymentMethod.CARD),
        TransactionEntity(title = "Bolt do pracy", categoryId = "transport", amountMinor = -1830, timestamp = daysAgo(9, 8, 5), method = PaymentMethod.BLIK),
        TransactionEntity(title = "Czynsz — lipiec", categoryId = "mieszkanie", amountMinor = -230000, timestamp = daysAgo(10, 9, 0), method = PaymentMethod.TRANSFER),
    )

    fun goals(): List<GoalEntity> = listOf(
        GoalEntity("g1", "Poduszka bezpieczeństwa", targetMinor = 1_500_000, savedMinor = 962_000, deadline = daysFromNow(154), monthlyContribMinor = 90_000, priority = 2),
        GoalEntity("g2", "Wyjazd w Bieszczady", targetMinor = 350_000, savedMinor = 128_000, deadline = daysFromNow(47), monthlyContribMinor = 55_000, priority = 1),
    )

    fun milestones(goals: List<GoalEntity>): List<MilestoneEntity> = goals.flatMap { g ->
        milestonesFor(g).map { m ->
            MilestoneEntity(
                goalId = g.id,
                pct = m.pct,
                reward = m.reward,
                unlockedAt = if (m.unlocked) System.currentTimeMillis() else null,
            )
        }
    }

    fun contributions(): List<ContributionEntity> = listOf(
        ContributionEntity(goalId = "g2", amountMinor = 18000, timestamp = daysAgo(2, 9, 5), source = ContributionSource.AUTO, note = "Stałe zlecenie po wypłacie"),
        ContributionEntity(goalId = "g2", amountMinor = 4500, timestamp = daysAgo(6, 20, 31), source = ContributionSource.CHALLENGE, note = "Tydzień bez dowozu"),
        ContributionEntity(goalId = "g2", amountMinor = 1230, timestamp = daysAgo(9, 11, 48), source = ContributionSource.ROUNDUP, note = "Reszty z 14 transakcji"),
        ContributionEntity(goalId = "g2", amountMinor = -6000, timestamp = daysAgo(14, 17, 2), source = ContributionSource.MANUAL, note = "Wypłata na serwis roweru"),
        ContributionEntity(goalId = "g2", amountMinor = 9000, timestamp = daysAgo(20, 8, 15), source = ContributionSource.CUT, note = "Rozrywka −90 zł"),
        ContributionEntity(goalId = "g2", amountMinor = 18000, timestamp = daysAgo(32, 9, 4), source = ContributionSource.AUTO, note = "Stałe zlecenie po wypłacie"),
        ContributionEntity(goalId = "g1", amountMinor = 30000, timestamp = daysAgo(2, 9, 5), source = ContributionSource.AUTO, note = "Stałe zlecenie po wypłacie"),
        ContributionEntity(goalId = "g1", amountMinor = 7500, timestamp = daysAgo(18, 13, 20), source = ContributionSource.MANUAL),
    )
}
