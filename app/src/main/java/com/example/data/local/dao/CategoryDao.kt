package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
  @Query("SELECT * FROM categories WHERE status = 'active' ORDER BY sortOrder ASC")
  fun getAllActiveCategories(): Flow<List<CategoryEntity>>

  @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
  fun getAllCategories(): Flow<List<CategoryEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCategory(category: CategoryEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCategories(categories: List<CategoryEntity>)

  @Update
  suspend fun updateCategory(category: CategoryEntity)

  @Query("DELETE FROM categories WHERE id = :categoryId")
  suspend fun deleteCategory(categoryId: String)

  @Query("SELECT COUNT(*) FROM categories")
  suspend fun getCategoryCount(): Int
}
