package com.example.data.repository

import com.example.data.local.dao.ProductDao
import com.example.data.local.entity.ProductEntity
import com.example.data.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepository(private val productDao: ProductDao) {

  val activeProducts: Flow<List<Product>> = productDao.getAllActiveProducts().map { list ->
    list.map { it.toModel() }
  }

  val allProducts: Flow<List<Product>> = productDao.getAllProducts().map { list ->
    list.map { it.toModel() }
  }

  fun searchProducts(query: String, categoryId: String?): Flow<List<Product>> {
    val cleanCategory = if (categoryId == "All" || categoryId.isNullOrBlank()) null else categoryId
    return productDao.searchProducts(query.trim(), cleanCategory).map { list ->
      list.map { it.toModel() }
    }
  }

  suspend fun getProductById(productId: String): Product? {
    return productDao.getProductById(productId)?.toModel()
  }

  suspend fun getProductByBarcode(barcode: String): Product? {
    return productDao.getProductByBarcode(barcode.trim())?.toModel()
  }

  suspend fun insertProduct(product: Product) {
    productDao.insertProduct(ProductEntity.fromModel(product))
  }

  suspend fun insertProducts(products: List<Product>) {
    productDao.insertProducts(products.map { ProductEntity.fromModel(it) })
  }

  suspend fun updateProduct(product: Product) {
    productDao.updateProduct(ProductEntity.fromModel(product))
  }

  suspend fun updateProductStatus(productId: String, status: String) {
    productDao.updateProductStatus(productId, status)
  }

  suspend fun updateProductImage(productId: String, imageUrl: String) {
    productDao.updateProductImage(productId, imageUrl)
  }

  suspend fun updateProductImageByBarcode(barcode: String, imageUrl: String) {
    productDao.updateProductImageByBarcode(barcode.trim(), imageUrl)
  }

  suspend fun deleteProduct(productId: String) {
    productDao.deleteProduct(productId)
  }
}
