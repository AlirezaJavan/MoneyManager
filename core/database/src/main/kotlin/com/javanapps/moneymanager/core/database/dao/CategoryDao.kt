package com.javanapps.moneymanager.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.javanapps.moneymanager.core.database.model.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name")
    fun observeByType(type: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY type, name")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Upsert
    suspend fun upsert(category: CategoryEntity): Long

    /** Seeds default categories without overwriting existing ones. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnoringConflicts(categories: List<CategoryEntity>)

    @Query("SELECT COUNT(*) FROM categories WHERE name = :name AND type = :type")
    suspend fun countByNameAndType(
        name: String,
        type: String,
    ): Int

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int

    @Query("UPDATE categories SET name = :newName WHERE id = :id")
    suspend fun rename(
        id: Long,
        newName: String,
    )

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT id FROM categories WHERE name = :name AND type = :type LIMIT 1")
    suspend fun getIdByNameAndType(
        name: String,
        type: String,
    ): Long?

    /** Returns the id of the oldest category of [type], used as a last-resort fallback. */
    @Query("SELECT id FROM categories WHERE type = :type ORDER BY id LIMIT 1")
    suspend fun getFirstIdByType(type: String): Long?
}
