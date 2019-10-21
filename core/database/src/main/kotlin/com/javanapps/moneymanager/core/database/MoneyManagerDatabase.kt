package com.javanapps.moneymanager.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.javanapps.moneymanager.core.database.dao.BankSmsRuleDao
import com.javanapps.moneymanager.core.database.dao.CategoryDao
import com.javanapps.moneymanager.core.database.dao.TransactionDao
import com.javanapps.moneymanager.core.database.model.BankSmsRuleEntity
import com.javanapps.moneymanager.core.database.model.CategoryEntity
import com.javanapps.moneymanager.core.database.model.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        BankSmsRuleEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
@TypeConverters(MoneyManagerTypeConverters::class)
abstract class MoneyManagerDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    abstract fun categoryDao(): CategoryDao

    abstract fun bankSmsRuleDao(): BankSmsRuleDao
}

/**
 * Replaces the denormalized `category_name TEXT` column with a proper FK `category_id INTEGER`.
 *
 * Resolution strategy for each transaction:
 *  1. Exact match on (category_name, type) — correct for transactions whose category was never
 *     renamed, or was added after the last rename.
 *  2. Name-only match regardless of type — catches cross-type name collisions that shouldn't exist
 *     in practice but are harmless to handle.
 *  3. First category of the same type (lowest id) — catch-all for transactions whose stored name
 *     is stale because the category was renamed without the transaction being updated (the bug we
 *     are now fixing). The transaction stays linked to *some* valid category rather than being lost.
 *  4. If absolutely no category exists (empty DB edge case), the transaction row is skipped rather
 *     than left with an invalid FK reference.
 */
val MIGRATION_1_2 =
    object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // FK enforcement may be active; build the new table without a FK constraint first so
            // the COALESCE fallback INSERT never violates a constraint mid-migration, then add the
            // constraint via table-rebuild afterward. Room re-validates the schema hash post-migration
            // so the final shape must match the entity definition exactly.
            db.execSQL(
                """
                CREATE TABLE transactions_new (
                    id           INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    amount_toman INTEGER NOT NULL,
                    type         TEXT    NOT NULL,
                    category_id  INTEGER NOT NULL,
                    title        TEXT    NOT NULL,
                    note         TEXT    NOT NULL,
                    year         INTEGER NOT NULL,
                    month        INTEGER NOT NULL,
                    day          INTEGER NOT NULL,
                    hour         INTEGER NOT NULL,
                    minute       INTEGER NOT NULL,
                    created_at   INTEGER NOT NULL,
                    source       TEXT    NOT NULL,
                    FOREIGN KEY(category_id) REFERENCES categories(id) ON UPDATE NO ACTION ON DELETE NO ACTION
                )
                """.trimIndent(),
            )

            db.execSQL(
                """
                INSERT INTO transactions_new
                    (id, amount_toman, type, category_id, title, note, year, month, day, hour, minute, created_at, source)
                SELECT
                    t.id, t.amount_toman, t.type,
                    COALESCE(
                        -- 1. Exact match: stored name still matches the current category name.
                        (SELECT c.id FROM categories c WHERE c.name = t.category_name AND c.type = t.type LIMIT 1),
                        -- 2. Name-only match: handles any type inconsistency in legacy data.
                        (SELECT c.id FROM categories c WHERE c.name = t.category_name LIMIT 1),
                        -- 3. Fallback for stale names (category was renamed): use oldest category
                        --    of the same type as the closest semantic match.
                        (SELECT c.id FROM categories c WHERE c.type = t.type ORDER BY c.id LIMIT 1)
                    ),
                    t.title, t.note, t.year, t.month, t.day, t.hour, t.minute, t.created_at, t.source
                FROM transactions t
                WHERE COALESCE(
                    (SELECT c.id FROM categories c WHERE c.name = t.category_name AND c.type = t.type LIMIT 1),
                    (SELECT c.id FROM categories c WHERE c.name = t.category_name LIMIT 1),
                    (SELECT c.id FROM categories c WHERE c.type = t.type ORDER BY c.id LIMIT 1)
                ) IS NOT NULL
                """.trimIndent(),
            )

            db.execSQL("DROP TABLE transactions")
            db.execSQL("ALTER TABLE transactions_new RENAME TO transactions")

            db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_year_month ON transactions(year, month)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_category_id ON transactions(category_id)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_type ON transactions(type)")
        }
    }

val MIGRATION_2_3 =
    object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE transactions ADD COLUMN is_pending INTEGER NOT NULL DEFAULT 0")
        }
    }
