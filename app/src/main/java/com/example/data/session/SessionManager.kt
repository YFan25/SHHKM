package com.example.data.session

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.User
import com.example.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(context: Context) {
  private val prefs: SharedPreferences =
    context.applicationContext.getSharedPreferences("shhkm_auth_session", Context.MODE_PRIVATE)

  private val _currentUser = MutableStateFlow<User?>(null)
  val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

  private val _isLoggedIn = MutableStateFlow(false)
  val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

  init {
    // Read cached session upon initialization
    val storedId = prefs.getString(KEY_USER_ID, null)
    val storedUsername = prefs.getString(KEY_USERNAME, null)
    val storedName = prefs.getString(KEY_NAME, null)
    val storedRole = prefs.getString(KEY_ROLE, null)
    val loggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    if (loggedIn && !storedId.isNullOrBlank() && !storedUsername.isNullOrBlank()) {
      _currentUser.value = User(
        id = storedId,
        name = storedName ?: storedUsername,
        username = storedUsername,
        passwordHash = "",
        role = storedRole ?: "staff",
        status = "active",
        createdAt = 0L,
        lastLogin = prefs.getLong(KEY_LOGIN_TIMESTAMP, System.currentTimeMillis())
      )
      _isLoggedIn.value = true
    }
  }

  fun saveSession(user: User) {
    val now = System.currentTimeMillis()
    prefs.edit()
      .putString(KEY_USER_ID, user.id)
      .putString(KEY_USERNAME, user.username)
      .putString(KEY_NAME, user.name)
      .putString(KEY_ROLE, user.role)
      .putBoolean(KEY_IS_LOGGED_IN, true)
      .putLong(KEY_LOGIN_TIMESTAMP, now)
      .apply()

    _currentUser.value = user.copy(lastLogin = now)
    _isLoggedIn.value = true
  }

  fun clearSession() {
    prefs.edit().clear().apply()
    _currentUser.value = null
    _isLoggedIn.value = false
  }

  suspend fun restoreSessionWithVerification(userRepository: UserRepository): Boolean {
    val storedUserId = prefs.getString(KEY_USER_ID, null) ?: return false
    val liveUser = userRepository.getUserById(storedUserId)
    if (liveUser != null && liveUser.status == "active") {
      _currentUser.value = liveUser
      _isLoggedIn.value = true
      return true
    } else {
      // User deleted or deactivated, invalidate session
      clearSession()
      return false
    }
  }

  val isAdmin: Boolean
    get() = _currentUser.value?.role.equals("admin", ignoreCase = true)

  val isStaff: Boolean
    get() = _currentUser.value?.role.equals("staff", ignoreCase = true)

  companion object {
    private const val KEY_USER_ID = "key_user_id"
    private const val KEY_USERNAME = "key_username"
    private const val KEY_NAME = "key_name"
    private const val KEY_ROLE = "key_role"
    private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
    private const val KEY_LOGIN_TIMESTAMP = "key_login_timestamp"

    @Volatile
    private var INSTANCE: SessionManager? = null

    fun getInstance(context: Context): SessionManager {
      return INSTANCE ?: synchronized(this) {
        val instance = SessionManager(context.applicationContext)
        INSTANCE = instance
        instance
      }
    }
  }
}
