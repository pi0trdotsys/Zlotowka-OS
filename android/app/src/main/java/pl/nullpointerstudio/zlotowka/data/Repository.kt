package pl.nullpointerstudio.zlotowka.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import pl.nullpointerstudio.zlotowka.domain.Milestone
import pl.nullpointerstudio.zlotowka.domain.MotivationSnapshot
import pl.nullpointerstudio.zlotowka.domain.buildMotivationSnapshot
import pl.nullpointerstudio.zlotowka.domain.goalPct
import pl.nullpointerstudio.zlotowka.domain.milestonesFor
import java.util.UUID
import kotlin.math.max

/** Kategoria "kosza" — wybrane kategorie usuwa się bezpiecznie, przypisując ich transakcje tutaj. */
const val FALLBACK_CATEGORY_ID = "inne"

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

    /**
     * Aplikacja jest do wpisywania WŁASNYCH danych — zasiewamy wyłącznie domyślną listę kategorii
     * (żeby ekran dodawania wydatku miał z czego wybierać), bez żadnych fikcyjnych transakcji/celów.
     */
    suspend fun seedIfNeeded() {
        if (db.categoryDao().count() == 0) {
            db.categoryDao().insertAll(SeedData.categories())
        }
    }

    // ---------- Transakcje (wydatki i dochody) ----------

    suspend fun addTransaction(
        title: String,
        categoryId: String,
        amountMinor: Long,
        method: PaymentMethod,
        note: String? = null,
        timestamp: Long = System.currentTimeMillis(),
    ) {
        db.transactionDao().insert(
            TransactionEntity(
                title = title,
                categoryId = categoryId,
                amountMinor = amountMinor,
                timestamp = timestamp,
                method = method,
                note = note,
            ),
        )
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        db.transactionDao().update(transaction)
    }

    suspend fun deleteTransaction(id: String) {
        db.transactionDao().delete(id)
    }

    // ---------- Kategorie ----------

    /** Dodaje nową kategorię zdefiniowaną przez użytkownika; zwraca jej id. */
    suspend fun addCategory(
        label: String,
        emoji: String,
        colorToken: String,
        monthlyBudgetMinor: Long,
        isImpulse: Boolean,
    ): String {
        val nextSort = db.categoryDao().maxSortOrder() + 1
        val id = UUID.randomUUID().toString()
        db.categoryDao().insert(
            CategoryEntity(
                id = id,
                label = label,
                emoji = emoji,
                colorToken = colorToken,
                monthlyBudgetMinor = monthlyBudgetMinor,
                sortOrder = nextSort,
                isImpulse = isImpulse,
            ),
        )
        return id
    }

    suspend fun updateCategory(category: CategoryEntity) {
        db.categoryDao().update(category)
    }

    /** Usuwa kategorię; jej dotychczasowe transakcje trafiają do kategorii "Inne" (jeśli inna niż usuwana). */
    suspend fun deleteCategory(id: String) {
        if (db.categoryDao().count() <= 1) return // zawsze zostaw co najmniej jedną kategorię
        if (id != FALLBACK_CATEGORY_ID && db.categoryDao().observeAll().first().any { it.id == FALLBACK_CATEGORY_ID }) {
            db.transactionDao().reassignCategory(id, FALLBACK_CATEGORY_ID)
        }
        db.categoryDao().delete(id)
    }

    // ---------- Cele ----------

    /** Dodaje nowy cel oszczędnościowy (z zerowym stanem odłożonych środków) i jego kamienie milowe; zwraca id. */
    suspend fun addGoal(
        label: String,
        targetMinor: Long,
        deadline: Long,
        monthlyContribMinor: Long,
        makeMain: Boolean,
    ): String {
        val id = UUID.randomUUID().toString()
        val priority = if (makeMain) {
            db.goalDao().minPriority() - 1
        } else {
            db.goalDao().maxPriority() + 1
        }
        val goal = GoalEntity(
            id = id,
            label = label,
            targetMinor = targetMinor,
            savedMinor = 0L,
            deadline = deadline,
            monthlyContribMinor = monthlyContribMinor,
            priority = priority,
        )
        db.goalDao().insert(goal)
        db.milestoneDao().insertAll(
            milestonesFor(goal).map { m ->
                MilestoneEntity(goalId = id, pct = m.pct, reward = m.reward, unlockedAt = null)
            },
        )
        return id
    }

    /** Aktualizuje dane celu (etykieta/kwota/termin/tempo) — nie rusza odłożonej kwoty ani priorytetu. */
    suspend fun updateGoal(
        goalId: String,
        label: String,
        targetMinor: Long,
        deadline: Long,
        monthlyContribMinor: Long,
    ) {
        val goal = db.goalDao().observeAll().first().firstOrNull { it.id == goalId } ?: return
        db.goalDao().update(
            goal.copy(
                label = label,
                targetMinor = targetMinor,
                deadline = deadline,
                monthlyContribMinor = monthlyContribMinor,
            ),
        )
    }

    suspend fun setMainGoal(goalId: String) {
        val goal = db.goalDao().observeAll().first().firstOrNull { it.id == goalId } ?: return
        val newPriority = db.goalDao().minPriority() - 1
        db.goalDao().update(goal.copy(priority = newPriority))
    }

    suspend fun deleteGoal(id: String) {
        db.contributionDao().deleteForGoal(id)
        db.milestoneDao().deleteForGoal(id)
        db.goalDao().delete(id)
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
