package com.example.data.model

sealed class AuthResult {
  data class Success(val user: User) : AuthResult()
  object InvalidCredentials : AuthResult()
  data class AccountDeactivated(val message: String = "Account has been deactivated. Please contact your administrator.") : AuthResult()
  object AccountNotFound : AuthResult()
  data class Error(val message: String) : AuthResult()
}
