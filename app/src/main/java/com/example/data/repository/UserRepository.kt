package com.example.data.repository

import com.example.data.local.dao.UserDao
import com.example.data.local.entity.UserEntity
import com.example.data.model.AuthResult
import com.example.data.model.User
import com.example.util.PasswordSecurity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepository(private val userDao: UserDao) {

  val allUsers: Flow<List<User>> = userDao.getAllUsers().map { list ->
    list.map { it.toModel() }
  }

  /**
   * Phase 3 Authentication:
   * - Validates Username & Password
   * - Validates Account Status (active vs deactivated)
   * - Secure password hash verification
   * - Automatically hashes seed/legacy plaintext passwords on successful verification
   * - Records lastLogin timestamp
   */
  suspend fun authenticate(username: String, passwordAttempt: String): AuthResult {
    val cleanUsername = username.trim().lowercase()
    if (cleanUsername.isBlank() || passwordAttempt.isBlank()) {
      return AuthResult.InvalidCredentials
    }

    val userEntity = userDao.getUserByUsername(cleanUsername) ?: return AuthResult.AccountNotFound

    // Check account active/deactivated status
    if (!userEntity.status.equals("active", ignoreCase = true)) {
      return AuthResult.AccountDeactivated("This account has been deactivated. Please contact your administrator.")
    }

    // Secure password verification
    val isPasswordValid = PasswordSecurity.verifyPassword(passwordAttempt, userEntity.passwordHash)
    if (!isPasswordValid) {
      return AuthResult.InvalidCredentials
    }

    val now = System.currentTimeMillis()
    // Upgrade plain or outdated hash to modern salted SHA-256 hash if needed
    val secureHash = PasswordSecurity.hashPassword(passwordAttempt)
    if (userEntity.passwordHash != secureHash) {
      userDao.updatePasswordHash(userEntity.id, secureHash)
    }

    // Update lastLogin timestamp
    userDao.updateLastLogin(userEntity.id, now)

    return AuthResult.Success(userEntity.toModel().copy(lastLogin = now, passwordHash = secureHash))
  }

  suspend fun getUserById(userId: String): User? {
    return userDao.getUserById(userId)?.toModel()
  }

  suspend fun getUserByUsername(username: String): User? {
    return userDao.getUserByUsername(username.trim().lowercase())?.toModel()
  }

  suspend fun insertUser(user: User) {
    // Ensure password is saved in hashed form
    val secureHash = if (user.passwordHash.length == 64) user.passwordHash else PasswordSecurity.hashPassword(user.passwordHash)
    val entity = UserEntity.fromModel(user.copy(passwordHash = secureHash))
    userDao.insertUser(entity)
  }

  suspend fun updateUser(user: User) {
    val existing = userDao.getUserById(user.id)
    val hash = if (user.passwordHash.isNotBlank()) {
      if (user.passwordHash.length == 64) user.passwordHash else PasswordSecurity.hashPassword(user.passwordHash)
    } else {
      existing?.passwordHash ?: ""
    }
    val entity = UserEntity.fromModel(user.copy(passwordHash = hash))
    userDao.updateUser(entity)
  }

  suspend fun updateUserStatus(userId: String, status: String) {
    userDao.updateUserStatus(userId, status)
  }

  suspend fun updateUserPassword(userId: String, newPlainPassword: String) {
    val hash = PasswordSecurity.hashPassword(newPlainPassword)
    userDao.updatePasswordHash(userId, hash)
  }

  suspend fun deleteUser(userId: String) {
    userDao.deleteUser(userId)
  }
}
