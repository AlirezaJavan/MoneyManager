package com.javanapps.moneymanager.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.javanapps.moneymanager.core.model.Transaction
import com.javanapps.moneymanager.core.model.TransactionSource
import com.javanapps.moneymanager.core.model.TransactionType

/**
 * Room entity for a transaction. Linked to its category by [categoryId] (FK → categories.id) so that
 * renaming a category automatically reflects in all read queries via JOIN — no denormalized text copy.
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [
        Index(value = ["year", "month"]),
        Index(value = ["category_id"]),
        Index(value = ["type"]),
    ],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "amount_toman")
    val amountToman: Long,
    val type: TransactionType,
    @ColumnInfo(name = "category_id")
    val categoryId: Long,
    val title: String,
    val note: String,
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int,
    @ColumnInfo(name = "created_at")
    val createdAtEpochMillis: Long,
    val source: TransactionSource,
    @ColumnInfo(name = "is_pending", defaultValue = "0")
    val isPending: Boolean = false,
)

fun Transaction.asEntity(categoryId: Long): TransactionEntity =
    TransactionEntity(
        id = id,
        amountToman = amountToman,
        type = type,
        categoryId = categoryId,
        title = title,
        note = note,
        year = date.year,
        month = date.month,
        day = date.day,
        hour = date.hour,
        minute = date.minute,
        createdAtEpochMillis = createdAtEpochMillis,
        source = source,
        isPending = isPending,
    )
