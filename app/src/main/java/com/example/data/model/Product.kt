package com.example.data.model

/**
 * Product Master Model
 *
 * CRITICAL BUSINESS RULE:
 * 1. Product Database NEVER contains any Price field.
 * 2. Shared by Company 1 and Company 2 without distinction.
 * 3. Packing size is preserved as raw free text (e.g. "10g x 20pcs x 12box").
 */
data class Product(
  val id: String,
  val name: String,
  val barcode: String,
  val packingSize: String, // Raw free text (never auto-split or formatted)
  val categoryId: String,
  val categoryName: String,
  val imageUrl: String = "",
  val description: String = "",
  val status: String = "active", // "active" | "archived"
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)
