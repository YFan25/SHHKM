package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
  @Query("SELECT * FROM products WHERE status = 'active' ORDER BY createdAt DESC")
  fun getAllActiveProducts(): Flow<List<ProductEntity>>

  @Query("SELECT * FROM products ORDER BY createdAt DESC")
  fun getAllProducts(): Flow<List<ProductEntity>>

  @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
  suspend fun getProductById(productId: String): ProductEntity?

  @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
  suspend fun getProductByBarcode(barcode: String): ProductEntity?

  @Query("""
    SELECT * FROM products 
    WHERE status = 'active' 
      AND (:categoryId IS NULL OR categoryId = :categoryId)
      AND (name LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%')
    ORDER BY createdAt DESC
  """)
  fun searchProducts(query: String, categoryId: String?): Flow<List<ProductEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProduct(product: ProductEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProducts(products: List<ProductEntity>)

  @Update
  suspend fun updateProduct(product: ProductEntity)

  @Query("DELETE FROM products WHERE id = :productId")
  suspend fun deleteProduct(productId: String)

  @Query("UPDATE products SET status = :status, updatedAt = :updatedAt WHERE id = :productId")
  suspend fun updateProductStatus(productId: String, status: String, updatedAt: Long = System.currentTimeMillis())

  @Query("UPDATE products SET imageUrl = :imageUrl, updatedAt = :updatedAt WHERE id = :productId")
  suspend fun updateProductImage(productId: String, imageUrl: String, updatedAt: Long = System.currentTimeMillis())

  @Query("UPDATE products SET imageUrl = :imageUrl, updatedAt = :updatedAt WHERE barcode = :barcode")
  suspend fun updateProductImageByBarcode(barcode: String, imageUrl: String, updatedAt: Long = System.currentTimeMillis())

  @Query("SELECT COUNT(*) FROM products WHERE status = 'active'")
  suspend fun getProductCount(): Int
}
