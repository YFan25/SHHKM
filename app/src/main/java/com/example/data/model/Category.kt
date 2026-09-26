package com.example.data.model

data class Category(
  val id: String,
  val name: String,
  val sortOrder: Int = 0,
  val status: String = "active",
  val createdAt: Long = System.currentTimeMillis()
)
