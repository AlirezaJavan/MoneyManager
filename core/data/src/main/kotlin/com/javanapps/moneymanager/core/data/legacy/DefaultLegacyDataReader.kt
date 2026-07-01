package com.javanapps.moneymanager.core.data.legacy

import android.database.sqlite.SQLiteDatabase
import com.javanapps.moneymanager.core.model.Category
import com.javanapps.moneymanager.core.model.MigrationData
import com.javanapps.moneymanager.core.model.Transaction
import com.javanapps.moneymanager.core.model.TransactionSource
import com.javanapps.moneymanager.core.model.TransactionType
import io.github.alirezajavan.shamsipicker.calendar.ShamsiCalendar
import io.github.alirezajavan.shamsipicker.model.ShamsiDate
import java.io.File
import javax.inject.Inject
import kotlin.math.abs

/**
 * Reads the legacy hand-rolled SQLite schema:
 * - `TBL2`: signed REAL amount (Toman), Shamsi y/m/d + hour/minute strings, category, title.
 * - `Cats`: `cat_name`, `cat_type` (0 = expense, 1 = income).
 *
 * Robust to missing tables/columns (returns whatever it can read).
 */
internal class DefaultLegacyDataReader
    @Inject
    constructor() : LegacyDataReader {
        override fun read(databaseFile: File): MigrationData {
            val db =
                SQLiteDatabase.openDatabase(
                    databaseFile.path,
                    null,
                    SQLiteDatabase.OPEN_READONLY,
                )
            return db.use { database ->
                MigrationData(
                    transactions = database.readTransactions(),
                    categories = database.readCategories(),
                )
            }
        }

        private fun SQLiteDatabase.readTransactions(): List<Transaction> {
            val result = mutableListOf<Transaction>()
            runCatching {
                rawQuery("SELECT * FROM TBL2", null).use { c ->
                    val amountIdx = c.getColumnIndex("Kharj2_amount")
                    val categoryIdx = c.getColumnIndex("Kharj2_Category")
                    val locationIdx = c.getColumnIndex("Kharj2_Location")
                    val saatIdx = c.getColumnIndex("Kharj2_saat")
                    val daghigheIdx = c.getColumnIndex("Kharj2_daghighe")
                    val yearIdx = c.getColumnIndex("Kharj2_year")
                    val monthIdx = c.getColumnIndex("Kharj2_month")
                    val dayIdx = c.getColumnIndex("Kharj2_day")
                    val nameIdx = c.getColumnIndex("Kharj2_Name")
                    while (c.moveToNext()) {
                        val signed = c.getDouble(amountIdx)
                        val date =
                            ShamsiDate(
                                year = c.getInt(yearIdx),
                                month = c.getInt(monthIdx).coerceIn(1, 12),
                                day = c.getInt(dayIdx).coerceIn(1, 31),
                                hour = c.stringOrNull(saatIdx)?.toIntOrNull()?.coerceIn(0, 23) ?: 0,
                                minute = c.stringOrNull(daghigheIdx)?.toIntOrNull()?.coerceIn(0, 59) ?: 0,
                            )
                        result +=
                            Transaction(
                                id = Transaction.NO_ID,
                                amountToman = abs(signed).toLong(),
                                type = TransactionType.fromSignedAmount(signed),
                                categoryName = c.stringOrNull(categoryIdx).orEmpty(),
                                title = c.stringOrNull(nameIdx).orEmpty(),
                                note = c.stringOrNull(locationIdx).orEmpty(),
                                date = date,
                                createdAtEpochMillis =
                                    runCatching { ShamsiCalendar.toEpochMillis(date) }
                                        .getOrDefault(0L),
                                source = TransactionSource.LEGACY_IMPORT,
                            )
                    }
                }
            }
            return result
        }

        private fun SQLiteDatabase.readCategories(): List<Category> {
            val result = mutableListOf<Category>()
            runCatching {
                rawQuery("SELECT * FROM Cats", null).use { c ->
                    val nameIdx = c.getColumnIndex("cat_name")
                    val typeIdx = c.getColumnIndex("cat_type")
                    while (c.moveToNext()) {
                        val name = c.stringOrNull(nameIdx)?.takeIf { it.isNotBlank() } ?: continue
                        result +=
                            Category(
                                id = Category.NO_ID,
                                name = name,
                                type = TransactionType.fromLegacyCategoryType(c.getInt(typeIdx)),
                            )
                    }
                }
            }
            return result
        }

        private fun android.database.Cursor.stringOrNull(index: Int): String? = if (index >= 0 && !isNull(index)) getString(index) else null
    }
