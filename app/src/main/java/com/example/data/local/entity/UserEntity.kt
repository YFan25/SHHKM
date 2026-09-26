package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.User

@Entity(tableName = "users")
data class UserEntity(
  @PrimaryKey val id: String,
  val name: String,
  val username: String,
  val passwordHash: String,
  val role: String,
  val status: String,
  val createdAt: Long,
  val lastLogin: Long
) {
  fun toModel(): User = User(
    id = id,
    name = name,
    username = username,
    passwordHash = passwordHash,
    role = role,
    status = status,
    createdAt = createdAt,
    lastLogin = lastLogin
  )

  companion object {
    fun fromModel(model: User): UserEntity = UserEntity(
      id = model.id,
      name = model.name,
      username = model.username,
      passwordHash = model.passwordHash,
      role = model.role,
      status = model.status,
      createdAt = model.createdAt,
      lastLogin = model.lastLogin
    )
  }
}
