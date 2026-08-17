package pl.nullpointerstudio.zlotowka.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class PaymentMethod { BLIK, CARD, CASH, TRANSFER }

enum class ContributionSource { MANUAL, AUTO, ROUNDUP, CHALLENGE, CUT }

/** Kategorie wydatków i dochodów to osobne listy — sposoby zarabiania różnią się od sposobów wydawania. */
enum class CategoryKind { EXPENSE, INCOME }

/** Kwoty zawsze w groszach jako Long — nigdy Double. Wydatek < 0, dochód > 0. */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val categoryId: String,
    val amountMinor: Long,
    val timestamp: Long,
    val method: PaymentMethod,
    val note: String? = null,
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val label: String,
    val emoji: String,
    /** "lime" | "cyan" | "coral" | "amber" | "violet" | "muted" */
    val colorToken: String,
    val monthlyBudgetMinor: Long,
    val sortOrder: Int = 0,
    /** kategorie "impulsowe" napędzają serię (streak) w Pulsie oszczędzania */
    val isImpulse: Boolean = false,
    /** Wydatek czy dochód — osobne listy kategorii, bo sposoby zarabiania różnią się od wydawania. */
    val kind: CategoryKind = CategoryKind.EXPENSE,
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    val label: String,
    val targetMinor: Long,
    val savedMinor: Long,
    val deadline: Long,
    /** realne tempo odkładania, grosze/miesiąc */
    val monthlyContribMinor: Long,
    /** 1 = cel główny, wyżej = dalszy priorytet */
    val priority: Int,
)

@Entity(tableName = "goal_milestones", primaryKeys = ["goalId", "pct"])
data class MilestoneEntity(
    val goalId: String,
    val pct: Int, // 25 | 50 | 75 | 100
    val reward: String,
    val unlockedAt: Long? = null,
)

@Entity(tableName = "goal_contributions")
data class ContributionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val goalId: String,
    val amountMinor: Long, // > 0 wpłata, < 0 wypłata z celu
    val timestamp: Long,
    val source: ContributionSource,
    val note: String? = null,
)
