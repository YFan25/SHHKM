package com.example.data.model

data class User(
  val id: String,
  val name: String,
  val username: String,
  val passwordHash: String,
  val role: String,       // "admin" | "staff"
  val status: String,     // "active" | "deactivated"
  val createdAt: Long = System.currentTimeMillis(),
  val lastLogin: Long = System.currentTimeMillis()
)
