package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Product

@Entity(tableName = "products")
data class ProductEntity(
  @PrimaryKey val id: String,
  val name: String,
  val barcode: String,
  val packingSize: String, // Stored as raw free text
  val categoryId: String,
  val categoryName: String,
  val imageUrl: String = "",
  val description: String = "",
  val status: String = "active",
  val createdAt: Long,
  val updatedAt: Long
) {
  fun toModel(): Product = Product(
    id = id,
    name = name,
    barcode = barcode,
    packingSize = packingSize,
    categoryId = categoryId,
    categoryName = categoryName,
    imageUrl = imageUrl,
    description = description,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt
  )

  companion object {
    fun fromModel(model: Product): ProductEntity = ProductEntity(
      id = model.id,
      name = model.name,
      barcode = model.barcode,
      packingSize = model.packingSize,
      categoryId = model.categoryId,
      categoryName = model.categoryName,
      imageUrl = model.imageUrl,
      description = model.description,
      status = model.status,
      createdAt = model.createdAt,
      updatedAt = model.updatedAt
    )
  }
}
