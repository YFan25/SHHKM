package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Category

@Entity(tableName = "categories")
data class CategoryEntity(
  @PrimaryKey val id: String,
  val name: String,
  val sortOrder: Int = 0,
  val status: String = "active",
  val createdAt: Long
) {
  fun toModel(): Category = Category(
    id = id,
    name = name,
    sortOrder = sortOrder,
    status = status,
    createdAt = createdAt
  )

  companion object {
    fun fromModel(model: Category): CategoryEntity = CategoryEntity(
      id = model.id,
      name = model.name,
      sortOrder = model.sortOrder,
      status = model.status,
      createdAt = model.createdAt
    )
  }
}
