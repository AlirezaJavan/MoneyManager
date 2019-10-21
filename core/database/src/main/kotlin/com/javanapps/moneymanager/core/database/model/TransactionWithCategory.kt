package com.javanapps.moneymanager.core.database.model

import androidx.room.ColumnInfo
import com.javanapps.moneymanager.core.model.ShamsiDate
import com.javanapps.moneymanager.core.model.Transaction
import com.javanapps.moneymanager.core.model.TransactionSource
import com.javanapps.moneymanager.core.model.TransactionType

/** Result type for DAO queries that JOIN transactions with categories to resolve the category name. */
data class TransactionWithCategory(
    val id: Long,
    @ColumnInfo(name = "amount_toman") val amountToman: Long,
    val type: TransactionType,
    @ColumnInfo(name = "category_id") val categoryId: Long,
    @ColumnInfo(name = "category_name") val categoryName: String,
    val title: String,
    val note: String,
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int,
    @ColumnInfo(name = "created_at") val createdAtEpochMillis: Long,
    val source: TransactionSource,
    @ColumnInfo(name = "is_pending") val isPending: Boolean,
)

fun TransactionWithCategory.asExternalModel(): Transaction =
    Transaction(
        id = id,
        amountToman = amountToman,
        type = type,
        categoryName = categoryName,
        title = title,
        note = note,
        date = ShamsiDate(year = year, month = month, day = day, hour = hour, minute = minute),
        createdAtEpochMillis = createdAtEpochMillis,
        source = source,
        isPending = isPending,
    )
