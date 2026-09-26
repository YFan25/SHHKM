package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.navigation.Screen
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
  isChinese: Boolean,
  currentUser: String,
  userRole: String,
  userId: String,
  onNavigateToRoute: (String) -> Unit,
  onChangePassword: suspend (userId: String, newPassword: String) -> Boolean = { _, _ -> true },
  onLogout: () -> Unit
) {
  val isAdmin = userRole.lowercase() == "admin"
  var showChangePasswordDialog by remember { mutableStateOf(false) }
  var passwordChangeSuccessMessage by remember { mutableStateOf<String?>(null) }

  val coroutineScope = rememberCoroutineScope()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
  ) {
    // Current User Profile Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(16.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = currentUser.take(1).uppercase(),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
          )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = currentUser,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = if (isAdmin) MaterialTheme.colorScheme.primary
              else MaterialTheme.colorScheme.secondary
            ) {
              Text(
                text = if (isAdmin) (if (isChinese) "管理员 (Admin)" else "Administrator")
                else (if (isChinese) "业务员工 (Staff)" else "Sales Staff"),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimary
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
              Text(
                text = if (isChinese) "状态: 正常激活" else "Status: Active",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }
        }
      }
    }

    if (passwordChangeSuccessMessage != null) {
      Spacer(modifier = Modifier.height(12.dp))
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Success",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = passwordChangeSuccessMessage ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Medium
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Admin Dedicated Section
    if (isAdmin) {
      Text(
        text = if (isChinese) "管理员管理中心 (Admin Control Hub)" else "Admin Control Hub",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
      )

      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column {
          SettingsMenuItem(
            icon = Icons.Default.People,
            title = if (isChinese) "用户管理 (User Management)" else "User Management",
            subtitle = if (isChinese) "增删改查员工、重置密码、冻结激活" else "Manage users, reset password, activate",
            onClick = { onNavigateToRoute(Screen.UserManagement.route) }
          )
          HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.2f))
          SettingsMenuItem(
            icon = Icons.Default.Business,
            title = if (isChinese) "公司资料管理 (Company Settings)" else "Company Settings",
            subtitle = if (isChinese) "Company 1 & 2 名称、Logo、前缀、银行" else "Company 1 & 2 profiles, logos, bank info",
            onClick = { onNavigateToRoute(Screen.CompanySettings.route) }
          )
          HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.2f))
          SettingsMenuItem(
            icon = Icons.Default.Inventory,
            title = if (isChinese) "产品主数据管理 (Product Master)" else "Product Master",
            subtitle = if (isChinese) "新增商品、条码、包装规格 (无价格)" else "Add/edit products & packing size (No price)",
            onClick = { onNavigateToRoute(Screen.ProductManagement.route) }
          )
          HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.2f))
          SettingsMenuItem(
            icon = Icons.Default.FileDownload,
            title = if (isChinese) "Excel 批量导入产品 (Excel Import)" else "Excel Import",
            subtitle = if (isChinese) "品名、条码、包装规格批量录入 (无价格)" else "Batch import products from spreadsheet",
            onClick = { onNavigateToRoute(Screen.ExcelImport.route) }
          )
          HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.2f))
          SettingsMenuItem(
            icon = Icons.Default.AddPhotoAlternate,
            title = if (isChinese) "条形码图片批量上传 (Batch Images)" else "Batch Image Upload",
            subtitle = if (isChinese) "按条码自动匹配商品图片" else "Match images by barcode filename",
            onClick = { onNavigateToRoute(Screen.BatchImageUpload.route) }
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
    }

    // General Security Settings
    Text(
      text = if (isChinese) "账户安全与凭据" else "Account Security & Credentials",
      style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(bottom = 8.dp)
    )

    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(14.dp)
    ) {
      Column {
        SettingsMenuItem(
          icon = Icons.Default.LockReset,
          title = if (isChinese) "修改登录密码" else "Change Password",
          subtitle = if (isChinese) "更新安全散列密码 (SHA-256)" else "Update secure hashed password",
          onClick = { showChangePasswordDialog = true }
        )
      }
    }

    Spacer(modifier = Modifier.height(28.dp))

    // Logout Button
    OutlinedButton(
      onClick = onLogout,
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .testTag("logout_button"),
      shape = RoundedCornerShape(12.dp)
    ) {
      Icon(
        imageVector = Icons.AutoMirrored.Filled.Logout,
        contentDescription = "Logout",
        tint = MaterialTheme.colorScheme.error,
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = if (isChinese) "退出登录 (Sign Out)" else "Sign Out",
        color = MaterialTheme.colorScheme.error,
        fontWeight = FontWeight.SemiBold
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = "SHHKM APP v1.0.0 (Phase 3 Authentication Ready)",
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
      modifier = Modifier.align(Alignment.CenterHorizontally)
    )
  }

  // Change Password Dialog
  if (showChangePasswordDialog) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var dialogError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
      onDismissRequest = { showChangePasswordDialog = false },
      title = {
        Text(
          text = if (isChinese) "修改登录密码" else "Change Password",
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column {
          Text(
            text = if (isChinese) "密码将以 SHA-256 盐值加密持久化保存。" else "Password will be stored using salted SHA-256 hash.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = newPassword,
            onValueChange = {
              newPassword = it
              dialogError = null
            },
            label = { Text(if (isChinese) "新密码" else "New Password") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
              IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                  imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                  contentDescription = null
                )
              }
            },
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
              confirmPassword = it
              dialogError = null
            },
            label = { Text(if (isChinese) "确认新密码" else "Confirm Password") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
          )

          if (dialogError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = dialogError ?: "",
              color = MaterialTheme.colorScheme.error,
              style = MaterialTheme.typography.bodySmall
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newPassword.isBlank()) {
              dialogError = if (isChinese) "密码不能为空" else "Password cannot be empty"
              return@Button
            }
            if (newPassword != confirmPassword) {
              dialogError = if (isChinese) "两次输入密码不一致" else "Passwords do not match"
              return@Button
            }
            coroutineScope.launch {
              val success = onChangePassword(userId, newPassword)
              if (success) {
                showChangePasswordDialog = false
                passwordChangeSuccessMessage = if (isChinese) "密码已成功更新！" else "Password updated successfully!"
              }
            }
          }
        ) {
          Text(if (isChinese) "确认更新" else "Update")
        }
      },
      dismissButton = {
        TextButton(onClick = { showChangePasswordDialog = false }) {
          Text(if (isChinese) "取消" else "Cancel")
        }
      }
    )
  }
}

@Composable
private fun SettingsMenuItem(
  icon: ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(40.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(imageVector = icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
    }
    Spacer(modifier = Modifier.width(14.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
      Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Arrow", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
  }
}
