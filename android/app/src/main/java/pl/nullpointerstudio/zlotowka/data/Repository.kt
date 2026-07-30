package pl.nullpointerstudio.zlotowka.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import pl.nullpointerstudio.zlotowka.domain.Milestone
import pl.nullpointerstudio.zlotowka.domain.MotivationSnapshot
import pl.nullpointerstudio.zlotowka.domain.buildMotivationSnapshot
import pl.nullpointerstudio.zlotowka.domain.goalPct
import kotlin.math.max

/** Jedyne wejście do danych aplikacji — Compose, widget Glance i workery powiadomień korzystają z tego samego repo. */
class BudgetRepository(private val db: AppDatabase) {

    val transactions: Flow<List<TransactionEntity>> = db.transactionDao().observeAll()
    val categories: Flow<List<CategoryEntity>> = db.categoryDao().observeAll()
    val goals: Flow<List<GoalEntity>> = db.goalDao().observeAll()
    val contributions: Flow<List<ContributionEntity>> = db.contributionDao().observeAll()

    val motivationSnapshot: Flow<MotivationSnapshot> =
        combine(categories, transactions) { cats, txs -> buildMotivationSnapshot(cats, txs) }

    fun contributionsForGoal(goalId: String): Flow<List<ContributionEntity>> =
        db.contributionDao().observeForGoal(goalId)

    fun milestonesForGoal(goalId: String): Flow<List<MilestoneEntity>> =
        db.milestoneDao().observeForGoal(goalId)

    suspend fun seedIfNeeded() {
        if (db.categoryDao().count() == 0) {
            db.categoryDao().insertAll(SeedData.categories())
        }
        if (db.transactionDao().observeAll().first().isEmpty()) {
            SeedData.transactions().forEach { db.transactionDao().insert(it) }
        }
        if (db.goalDao().count() == 0) {
            val goals = SeedData.goals()
            db.goalDao().insertAll(goals)
            db.contributionDao().insertAll(SeedData.contributions())
            db.milestoneDao().insertAll(SeedData.milestones(goals))
        }
    }

    suspend fun addTransaction(
        title: String,
        categoryId: String,
        amountMinor: Long,
        method: PaymentMethod,
        note: String? = null,
    ) {
        db.transactionDao().insert(
            TransactionEntity(
                title = title,
                categoryId = categoryId,
                amountMinor = amountMinor,
                timestamp = System.currentTimeMillis(),
                method = method,
                note = note,
            ),
        )
    }

    /** Dodaje wpłatę/wypłatę do celu i odblokowuje ewentualne nowe kamienie milowe. */
    suspend fun addContribution(
        goalId: String,
        amountMinor: Long,
        source: ContributionSource,
        note: String? = null,
    ): List<Milestone> {
        db.contributionDao().insert(
            ContributionEntity(goalId = goalId, amountMinor = amountMinor, timestamp = System.currentTimeMillis(), source = source, note = note),
        )
        val goal = db.goalDao().observeAll().first().first { it.id == goalId }
        val updated = goal.copy(savedMinor = max(0L, goal.savedMinor + amountMinor))
        db.goalDao().update(updated)
        return unlockMilestones(updated)
    }

    private suspend fun unlockMilestones(goal: GoalEntity): List<Milestone> {
        val pct = goalPct(goal)
        val existing = db.milestoneDao().observeForGoal(goal.id).first()
        val newlyUnlocked = mutableListOf<Milestone>()
        val now = System.currentTimeMillis()
        for (e in existing) {
            if (e.unlockedAt == null && pct >= e.pct) {
                db.milestoneDao().update(e.copy(unlockedAt = now))
                newlyUnlocked += Milestone(e.pct, e.reward, unlocked = true, unlockedAt = now)
            }
        }
        return newlyUnlocked
    }

    /** Akcja "Zastosuj" z sugestii cięć: obniża limit kategorii i podnosi tempo odkładania celu o tę samą kwotę. */
    suspend fun applyCutSuggestion(categoryId: String, cutMinor: Long, goalId: String) {
        val category = db.categoryDao().observeAll().first().first { it.id == categoryId }
        db.categoryDao().update(category.copy(monthlyBudgetMinor = max(0L, category.monthlyBudgetMinor - cutMinor)))
        val goal = db.goalDao().observeAll().first().first { it.id == goalId }
        db.goalDao().update(goal.copy(monthlyContribMinor = goal.monthlyContribMinor + cutMinor))
    }
}
