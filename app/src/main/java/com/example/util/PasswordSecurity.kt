package com.example.util

import java.security.MessageDigest

object PasswordSecurity {
  private const val SALT = "SHHKM_SALT_2026_SECURE_AUTH"

  /**
   * Hashes a plaintext password using SHA-256 with a secure application salt.
   */
  fun hashPassword(password: String): String {
    val md = MessageDigest.getInstance("SHA-256")
    val saltedInput = "$SALT:$password"
    val digest = md.digest(saltedInput.toByteArray(Charsets.UTF_8))
    return digest.joinToString("") { "%02x".format(it) }
  }

  /**
   * Verifies if the entered plaintext password matches the stored password hash.
   * Also supports seamless fallback/upgrade for initial seed data.
   */
  fun verifyPassword(plainPassword: String, storedHash: String): Boolean {
    if (storedHash.isBlank()) return false
    // 1. Direct match with current SHA-256 hash
    if (hashPassword(plainPassword) == storedHash) return true
    // 2. Compatibility fallback for plain seed passwords
    return plainPassword == storedHash
  }
}
