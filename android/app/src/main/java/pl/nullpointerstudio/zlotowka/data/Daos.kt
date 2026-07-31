package pl.nullpointerstudio.zlotowka.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tx: TransactionEntity)

    @Update
    suspend fun update(tx: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE transactions SET categoryId = :fallbackCategoryId WHERE categoryId = :categoryId")
    suspend fun reassignCategory(categoryId: String, fallbackCategoryId: String)

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun count(): Int
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Update
    suspend fun update(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM categories")
    suspend fun maxSortOrder(): Int
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY priority ASC")
    fun observeAll(): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: GoalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(goals: List<GoalEntity>)

    @Update
    suspend fun update(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT COUNT(*) FROM goals")
    suspend fun count(): Int

    @Query("SELECT COALESCE(MIN(priority), 1) FROM goals")
    suspend fun minPriority(): Int

    @Query("SELECT COALESCE(MAX(priority), 0) FROM goals")
    suspend fun maxPriority(): Int
}

@Dao
interface MilestoneDao {
    @Query("SELECT * FROM goal_milestones WHERE goalId = :goalId ORDER BY pct ASC")
    fun observeForGoal(goalId: String): Flow<List<MilestoneEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(milestones: List<MilestoneEntity>)

    @Update
    suspend fun update(milestone: MilestoneEntity)

    @Query("DELETE FROM goal_milestones WHERE goalId = :goalId")
    suspend fun deleteForGoal(goalId: String)

    @Query("SELECT COUNT(*) FROM goal_milestones")
    suspend fun count(): Int
}

@Dao
interface ContributionDao {
    @Query("SELECT * FROM goal_contributions ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<ContributionEntity>>

    @Query("SELECT * FROM goal_contributions WHERE goalId = :goalId ORDER BY timestamp DESC")
    fun observeForGoal(goalId: String): Flow<List<ContributionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contribution: ContributionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contributions: List<ContributionEntity>)

    @Query("DELETE FROM goal_contributions WHERE goalId = :goalId")
    suspend fun deleteForGoal(goalId: String)
}
