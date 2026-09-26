package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AuthResult
import com.example.data.model.User
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
  isChinese: Boolean,
  onLoginAttempt: suspend (username: String, passwordAttempt: String) -> AuthResult,
  onLoginSuccess: (user: User) -> Unit,
  onToggleLanguage: () -> Unit
) {
  var username by remember { mutableStateOf("SHHKMY4") }
  var password by remember { mutableStateOf("Korean980!") }
  var passwordVisible by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var isDeactivatedError by remember { mutableStateOf(false) }
  var isLoading by remember { mutableStateOf(false) }

  val coroutineScope = rememberCoroutineScope()

  fun performLogin() {
    val cleanUsername = username.trim()
    if (cleanUsername.isBlank() || password.isBlank()) {
      errorMessage = if (isChinese) "请输入用户名与密码" else "Please enter username and password"
      isDeactivatedError = false
      return
    }

    isLoading = true
    errorMessage = null
    isDeactivatedError = false

    coroutineScope.launch {
      val result = onLoginAttempt(cleanUsername, password)
      isLoading = false
      when (result) {
        is AuthResult.Success -> {
          onLoginSuccess(result.user)
        }
        is AuthResult.AccountDeactivated -> {
          isDeactivatedError = true
          errorMessage = if (isChinese) {
            "该账号已被停用或冻结，无法登录。请联系系统管理员。"
          } else {
            "This account is deactivated. Please contact your administrator."
          }
        }
        is AuthResult.InvalidCredentials -> {
          isDeactivatedError = false
          errorMessage = if (isChinese) {
            "用户名或密码错误，请核对后重试。"
          } else {
            "Invalid username or password. Please try again."
          }
        }
        is AuthResult.AccountNotFound -> {
          isDeactivatedError = false
          errorMessage = if (isChinese) {
            "用户不存在，请确认用户名输入是否正确。"
          } else {
            "Account not found. Please verify your username."
          }
        }
        is AuthResult.Error -> {
          isDeactivatedError = false
          errorMessage = result.message
        }
      }
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(20.dp),
    contentAlignment = Alignment.Center
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 440.dp)
        .verticalScroll(rememberScrollState()),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
      shape = RoundedCornerShape(24.dp)
    ) {
      Column(
        modifier = Modifier.padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Top row: Language switch and Phase 3 Auth indicator
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
          ) {
            Text(
              text = if (isChinese) "安全认证 (Phase 3)" else "Secure Auth (Phase 3)",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              fontWeight = FontWeight.Medium
            )
          }

          TextButton(onClick = onToggleLanguage) {
            Text(
              text = if (isChinese) "English" else "中文",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // App Logo & Branding
        Box(
          modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center
        ) {
          Image(
            painter = painterResource(id = R.drawable.app_logo_icon_1790321246982),
            contentDescription = "SHHKM Logo",
            modifier = Modifier.size(58.dp)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "SHHKM APP",
          style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          ),
          color = MaterialTheme.colorScheme.onSurface
        )

        Text(
          text = if (isChinese) "企业内部业务与报价系统" else "Internal Business & Quotation System",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Error message banner
        AnimatedVisibility(
          visible = errorMessage != null,
          enter = fadeIn(),
          exit = fadeOut()
        ) {
          errorMessage?.let { msg ->
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .testTag("error_banner"),
              shape = RoundedCornerShape(12.dp),
              color = if (isDeactivatedError) {
                MaterialTheme.colorScheme.errorContainer
              } else {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f)
              }
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = if (isDeactivatedError) Icons.Default.Warning else Icons.Default.Info,
                  contentDescription = "Error Icon",
                  tint = MaterialTheme.colorScheme.error,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = msg,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onErrorContainer,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }
        }

        // Username Field (ONLY Username & Password)
        OutlinedTextField(
          value = username,
          onValueChange = {
            username = it
            errorMessage = null
            isDeactivatedError = false
          },
          label = { Text(if (isChinese) "用户名 (Username)" else "Username") },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = "Username Icon",
              tint = MaterialTheme.colorScheme.primary
            )
          },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("username_input"),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
          ),
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next
          )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        OutlinedTextField(
          value = password,
          onValueChange = {
            password = it
            errorMessage = null
            isDeactivatedError = false
          },
          label = { Text(if (isChinese) "密码 (Password)" else "Password") },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = "Password Icon",
              tint = MaterialTheme.colorScheme.primary
            )
          },
          trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
              Icon(
                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = if (passwordVisible) "Hide password" else "Show password"
              )
            }
          },
          singleLine = true,
          visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("password_input"),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
          ),
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
          ),
          keyboardActions = KeyboardActions(onDone = { performLogin() })
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Login Button
        Button(
          onClick = { performLogin() },
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("login_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
          ),
          enabled = !isLoading
        ) {
          if (isLoading) {
            CircularProgressIndicator(
              modifier = Modifier.size(22.dp),
              color = MaterialTheme.colorScheme.onPrimary,
              strokeWidth = 2.dp
            )
          } else {
            Text(
              text = if (isChinese) "登 录" else "Sign In",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
          }
        }
      }
    }
  }
}
