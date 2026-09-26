package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
  @Query("SELECT * FROM users ORDER BY createdAt ASC")
  fun getAllUsers(): Flow<List<UserEntity>>

  @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
  suspend fun getUserByUsername(username: String): UserEntity?

  @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
  suspend fun getUserById(userId: String): UserEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUser(user: UserEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUsers(users: List<UserEntity>)

  @Update
  suspend fun updateUser(user: UserEntity)

  @Query("UPDATE users SET status = :status WHERE id = :userId")
  suspend fun updateUserStatus(userId: String, status: String)

  @Query("UPDATE users SET lastLogin = :timestamp WHERE id = :userId")
  suspend fun updateLastLogin(userId: String, timestamp: Long)

  @Query("UPDATE users SET passwordHash = :passwordHash WHERE id = :userId")
  suspend fun updatePasswordHash(userId: String, passwordHash: String)

  @Query("DELETE FROM users WHERE id = :userId")
  suspend fun deleteUser(userId: String)

  @Query("SELECT COUNT(*) FROM users")
  suspend fun getUserCount(): Int
}
