package com.javanapps.moneymanager.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.javanapps.moneymanager.core.database.model.CategorySum
import com.javanapps.moneymanager.core.database.model.DaySum
import com.javanapps.moneymanager.core.database.model.MonthSum
import com.javanapps.moneymanager.core.database.model.TransactionEntity
import com.javanapps.moneymanager.core.database.model.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

private const val TX_JOIN_COLS = """
    t.id, t.amount_toman, t.type, t.category_id, c.name AS category_name,
    t.title, t.note, t.year, t.month, t.day, t.hour, t.minute, t.created_at, t.source, t.is_pending
"""

@Dao
interface TransactionDao {
    @Upsert
    suspend fun upsert(transaction: TransactionEntity): Long

    @Upsert
    suspend fun upsertAll(transactions: List<TransactionEntity>)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query(
        """
        SELECT $TX_JOIN_COLS
        FROM transactions t JOIN categories c ON c.id = t.category_id
        WHERE t.id = :id
        """,
    )
    suspend fun getById(id: Long): TransactionWithCategory?

    @Query(
        """
        SELECT $TX_JOIN_COLS
        FROM transactions t JOIN categories c ON c.id = t.category_id
        WHERE t.year = :year AND t.month = :month
        ORDER BY t.day DESC, t.created_at DESC
        """,
    )
    fun observeByMonth(
        year: Int,
        month: Int,
    ): Flow<List<TransactionWithCategory>>

    @Query(
        """
        SELECT $TX_JOIN_COLS
        FROM transactions t JOIN categories c ON c.id = t.category_id
        WHERE t.is_pending = 1
        ORDER BY t.created_at DESC
        """,
    )
    fun observePending(): Flow<List<TransactionWithCategory>>

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun count(): Int

    // --- Filtered search ---
    @Query(
        """
        SELECT $TX_JOIN_COLS
        FROM transactions t JOIN categories c ON c.id = t.category_id
        WHERE (:fromOrdinal IS NULL OR (t.year * 12 + t.month - 1) >= :fromOrdinal)
          AND (:toOrdinal IS NULL OR (t.year * 12 + t.month - 1) <= :toOrdinal)
          AND (:type IS NULL OR t.type = :type)
          AND (:titleQuery IS NULL OR t.title LIKE '%' || :titleQuery || '%')
          AND (:categoryQuery IS NULL OR c.name LIKE '%' || :categoryQuery || '%')
        ORDER BY t.year DESC, t.month DESC, t.day DESC, t.created_at DESC
        """,
    )
    fun observeFiltered(
        fromOrdinal: Int?,
        toOrdinal: Int?,
        type: String?,
        titleQuery: String?,
        categoryQuery: String?,
    ): Flow<List<TransactionWithCategory>>

    // --- Aggregations ---
    @Query(
        "SELECT COALESCE(SUM(amount_toman), 0) FROM transactions " +
            "WHERE year = :year AND month = :month AND type = :type",
    )
    fun observeMonthTotal(
        year: Int,
        month: Int,
        type: String,
    ): Flow<Long>

    @Query(
        """
        SELECT c.name AS categoryName, SUM(t.amount_toman) AS total
        FROM transactions t JOIN categories c ON c.id = t.category_id
        WHERE t.year = :year AND t.month = :month AND t.type = :type
        GROUP BY t.category_id
        ORDER BY total DESC
        """,
    )
    fun observeCategoryBreakdown(
        year: Int,
        month: Int,
        type: String,
    ): Flow<List<CategorySum>>

    @Query(
        "SELECT day AS day, SUM(amount_toman) AS total FROM transactions " +
            "WHERE year = :year AND month = :month AND type = :type " +
            "GROUP BY day ORDER BY day",
    )
    fun observeDailyTotals(
        year: Int,
        month: Int,
        type: String,
    ): Flow<List<DaySum>>

    @Query(
        """
        SELECT year AS year, month AS month, SUM(amount_toman) AS total FROM transactions
        WHERE type = :type AND (year * 12 + month - 1) BETWEEN :fromOrdinal AND :toOrdinal
        GROUP BY year, month ORDER BY year, month
        """,
    )
    fun observeMonthlyTotals(
        type: String,
        fromOrdinal: Int,
        toOrdinal: Int,
    ): Flow<List<MonthSum>>

    // --- Category maintenance — all keyed by category_id for referential integrity ---
    @Query(
        "SELECT COUNT(*) FROM transactions WHERE category_id = " +
            "(SELECT id FROM categories WHERE name = :name AND type = :type LIMIT 1)",
    )
    suspend fun countByCategory(
        name: String,
        type: String,
    ): Int

    @Query(
        "SELECT COUNT(*) FROM transactions WHERE category_id = " +
            "(SELECT id FROM categories WHERE name = :name AND type = :type LIMIT 1)",
    )
    fun observeCountByCategory(
        name: String,
        type: String,
    ): Flow<Int>

    @Query(
        """
        UPDATE transactions
        SET category_id = (SELECT id FROM categories WHERE name = :newName AND type = :type LIMIT 1)
        WHERE category_id = (SELECT id FROM categories WHERE name = :oldName AND type = :type LIMIT 1)
        """,
    )
    suspend fun reassignCategory(
        oldName: String,
        newName: String,
        type: String,
    )

    @Query(
        "DELETE FROM transactions WHERE category_id = " +
            "(SELECT id FROM categories WHERE name = :name AND type = :type LIMIT 1)",
    )
    suspend fun deleteByCategory(
        name: String,
        type: String,
    )
}
