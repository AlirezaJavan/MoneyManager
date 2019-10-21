package com.javanapps.moneymanager.core.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.javanapps.moneymanager.core.model.Category
import com.javanapps.moneymanager.core.model.TransactionType

/** Room entity for a category. A name is unique per [type] (expense vs income lists are separate). */
@Entity(
    tableName = "categories",
    indices = [Index(value = ["name", "type"], unique = true)],
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: TransactionType,
)

fun CategoryEntity.asExternalModel(): Category = Category(id = id, name = name, type = type)

fun Category.asEntity(): CategoryEntity = CategoryEntity(id = id, name = name, type = type)
