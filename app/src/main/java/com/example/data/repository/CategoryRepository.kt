package com.example.data.repository

import com.example.data.local.dao.CategoryDao
import com.example.data.local.entity.CategoryEntity
import com.example.data.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepository(private val categoryDao: CategoryDao) {

  val activeCategories: Flow<List<Category>> = categoryDao.getAllActiveCategories().map { list ->
    list.map { it.toModel() }
  }

  suspend fun insertCategory(category: Category) {
    categoryDao.insertCategory(CategoryEntity.fromModel(category))
  }

  suspend fun updateCategory(category: Category) {
    categoryDao.updateCategory(CategoryEntity.fromModel(category))
  }

  suspend fun deleteCategory(categoryId: String) {
    categoryDao.deleteCategory(categoryId)
  }
}
