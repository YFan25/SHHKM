package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserManagementScreen(
  isChinese: Boolean,
  users: List<User>,
  currentUserId: String = "",
  onToggleUserStatus: (String, String) -> Unit,
  onAddUser: (User) -> Unit,
  onUpdateUser: (User) -> Unit = {},
  onResetPassword: (String, String) -> Unit = { _, _ -> },
  onDeleteUser: (String) -> Unit = {}
) {
  val context = LocalContext.current

  // Filter & Search states
  var searchQuery by remember { mutableStateOf("") }
  var selectedFilter by remember { mutableStateOf("all") } // "all", "active", "deactivated", "admin", "staff"

  // Dialog States
  var showAddDialog by remember { mutableStateOf(false) }
  var editingUser by remember { mutableStateOf<User?>(null) }
  var resettingPasswordUser by remember { mutableStateOf<User?>(null) }
  var deletingUser by remember { mutableStateOf<User?>(null) }

  // Filter users based on query and chip
  val filteredUsers = users.filter { user ->
    val matchesSearch = user.name.contains(searchQuery, ignoreCase = true) ||
      user.username.contains(searchQuery, ignoreCase = true)
    val matchesFilter = when (selectedFilter) {
      "active" -> user.status == "active"
      "deactivated" -> user.status != "active"
      "admin" -> user.role.equals("admin", ignoreCase = true)
      "staff" -> user.role.equals("staff", ignoreCase = true)
      else -> true
    }
    matchesSearch && matchesFilter
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(16.dp)
  ) {
    // Top Bar Header & Action
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = if (isChinese) "用户管理 (User Management)" else "User Management",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = if (isChinese) "共 ${users.size} 个账户 (${users.count { it.status == "active" }} 活跃)" else "Total ${users.size} users (${users.count { it.status == "active" }} active)",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Button(
        onClick = { showAddDialog = true },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier.testTag("add_user_button")
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add User", modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (isChinese) "新增员工" else "Add User",
          fontWeight = FontWeight.SemiBold
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Search Input Field
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("user_search_input"),
      placeholder = { Text(if (isChinese) "搜索姓名或登录用户名..." else "Search by name or username...") },
      leadingIcon = {
        Icon(
          imageVector = Icons.Default.Search,
          contentDescription = "Search",
          tint = MaterialTheme.colorScheme.primary
        )
      },
      trailingIcon = {
        if (searchQuery.isNotEmpty()) {
          IconButton(onClick = { searchQuery = "" }) {
            Icon(Icons.Default.Close, contentDescription = "Clear Search")
          }
        }
      },
      singleLine = true,
      shape = RoundedCornerShape(12.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
      )
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Quick Filter Chips
    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      FilterChip(
        selected = selectedFilter == "all",
        onClick = { selectedFilter = "all" },
        label = { Text(if (isChinese) "全部 (${users.size})" else "All (${users.size})") },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
          selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
      )
      FilterChip(
        selected = selectedFilter == "active",
        onClick = { selectedFilter = "active" },
        label = { Text(if (isChinese) "激活中 (${users.count { it.status == "active" }})" else "Active (${users.count { it.status == "active" }})") },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
          selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
      )
      FilterChip(
        selected = selectedFilter == "deactivated",
        onClick = { selectedFilter = "deactivated" },
        label = { Text(if (isChinese) "已冻结 (${users.count { it.status != "active" }})" else "Deactivated (${users.count { it.status != "active" }})") },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
          selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
        )
      )
      FilterChip(
        selected = selectedFilter == "admin",
        onClick = { selectedFilter = "admin" },
        label = { Text(if (isChinese) "管理员 (${users.count { it.role.equals("admin", true) }})" else "Admin (${users.count { it.role.equals("admin", true) }})") }
      )
      FilterChip(
        selected = selectedFilter == "staff",
        onClick = { selectedFilter = "staff" },
        label = { Text(if (isChinese) "员工 (${users.count { it.role.equals("staff", true) }})" else "Staff (${users.count { it.role.equals("staff", true) }})") }
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // User Cards List
    if (filteredUsers.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.FilterList,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(48.dp)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = if (isChinese) "未找到符合条件的用户" else "No users found",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(filteredUsers, key = { it.id }) { user ->
          UserCardItem(
            user = user,
            isChinese = isChinese,
            isCurrentLoggedUser = user.id == currentUserId,
            onEdit = { editingUser = user },
            onResetPassword = { resettingPasswordUser = user },
            onToggleStatus = {
              val nextStatus = if (user.status == "active") "deactivated" else "active"
              onToggleUserStatus(user.id, nextStatus)
              Toast.makeText(
                context,
                if (isChinese) "用户状态已更新" else "User status updated",
                Toast.LENGTH_SHORT
              ).show()
            },
            onDelete = { deletingUser = user }
          )
        }
      }
    }
  }

  // 1. Add User Dialog
  if (showAddDialog) {
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("staff") } // "staff" or "admin"
    var passwordVisible by remember { mutableStateOf(false) }
    var dialogError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
      onDismissRequest = { showAddDialog = false },
      title = {
        Text(
          text = if (isChinese) "新增内部业务用户" else "Add New User",
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          OutlinedTextField(
            value = name,
            onValueChange = { name = it; dialogError = null },
            label = { Text(if (isChinese) "员工姓名 (Full Name)" else "Full Name") },
            placeholder = { Text("e.g. Ken Lee") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = username,
            onValueChange = { username = it.trim().lowercase(); dialogError = null },
            label = { Text(if (isChinese) "登录用户名 (Username)" else "Username") },
            placeholder = { Text("e.g. sales2") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = password,
            onValueChange = { password = it; dialogError = null },
            label = { Text(if (isChinese) "初始登录密码" else "Initial Password") },
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

          // Role Selection
          Text(
            text = if (isChinese) "分配权限角色：" else "Select Role:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              RadioButton(
                selected = role == "staff",
                onClick = { role = "staff" }
              )
              Text(if (isChinese) "业务员工 (Staff)" else "Staff", fontSize = 14.sp)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              RadioButton(
                selected = role == "admin",
                onClick = { role = "admin" }
              )
              Text(if (isChinese) "管理员 (Admin)" else "Admin", fontSize = 14.sp)
            }
          }

          if (dialogError != null) {
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
            if (name.isBlank() || username.isBlank() || password.isBlank()) {
              dialogError = if (isChinese) "请完整填写姓名、用户名与密码" else "Please fill all fields"
              return@Button
            }
            if (users.any { it.username.equals(username, ignoreCase = true) }) {
              dialogError = if (isChinese) "用户名已存在，请使用其他用户名" else "Username already exists"
              return@Button
            }

            onAddUser(
              User(
                id = "usr_${System.currentTimeMillis()}",
                name = name.trim(),
                username = username.trim().lowercase(),
                passwordHash = password,
                role = role,
                status = "active",
                createdAt = System.currentTimeMillis(),
                lastLogin = System.currentTimeMillis()
              )
            )
            showAddDialog = false
            Toast.makeText(
              context,
              if (isChinese) "用户添加成功！" else "User created successfully!",
              Toast.LENGTH_SHORT
            ).show()
          }
        ) {
          Text(if (isChinese) "确认创建" else "Create")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddDialog = false }) {
          Text(if (isChinese) "取消" else "Cancel")
        }
      }
    )
  }

  // 2. Edit User Dialog
  editingUser?.let { targetUser ->
    var editName by remember { mutableStateOf(targetUser.name) }
    var editUsername by remember { mutableStateOf(targetUser.username) }
    var editRole by remember { mutableStateOf(targetUser.role) }
    var editError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
      onDismissRequest = { editingUser = null },
      title = {
        Text(
          text = if (isChinese) "编辑用户资料" else "Edit User Profile",
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          OutlinedTextField(
            value = editName,
            onValueChange = { editName = it; editError = null },
            label = { Text(if (isChinese) "姓名" else "Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = editUsername,
            onValueChange = { editUsername = it.trim().lowercase(); editError = null },
            label = { Text(if (isChinese) "用户名" else "Username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          Text(
            text = if (isChinese) "用户角色权限：" else "User Role:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              RadioButton(
                selected = editRole == "staff",
                onClick = { editRole = "staff" }
              )
              Text(if (isChinese) "业务员工 (Staff)" else "Staff", fontSize = 14.sp)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              RadioButton(
                selected = editRole == "admin",
                onClick = { editRole = "admin" }
              )
              Text(if (isChinese) "管理员 (Admin)" else "Admin", fontSize = 14.sp)
            }
          }

          if (editError != null) {
            Text(
              text = editError ?: "",
              color = MaterialTheme.colorScheme.error,
              style = MaterialTheme.typography.bodySmall
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (editName.isBlank() || editUsername.isBlank()) {
              editError = if (isChinese) "姓名和用户名不能为空" else "Name and username cannot be empty"
              return@Button
            }
            if (users.any { it.id != targetUser.id && it.username.equals(editUsername, ignoreCase = true) }) {
              editError = if (isChinese) "该用户名已被其他员工占用" else "Username already in use by another user"
              return@Button
            }

            onUpdateUser(
              targetUser.copy(
                name = editName.trim(),
                username = editUsername.trim().lowercase(),
                role = editRole
              )
            )
            editingUser = null
            Toast.makeText(
              context,
              if (isChinese) "资料更新成功！" else "User updated successfully!",
              Toast.LENGTH_SHORT
            ).show()
          }
        ) {
          Text(if (isChinese) "保存修改" else "Save Changes")
        }
      },
      dismissButton = {
        TextButton(onClick = { editingUser = null }) {
          Text(if (isChinese) "取消" else "Cancel")
        }
      }
    )
  }

  // 3. Reset Password Dialog (Admin Authority)
  resettingPasswordUser?.let { targetUser ->
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var resetError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
      onDismissRequest = { resettingPasswordUser = null },
      title = {
        Text(
          text = if (isChinese) "重置员工登录密码" else "Reset User Password",
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = if (isChinese) "正在为 ${targetUser.name} (@${targetUser.username}) 重置密码。" else "Resetting password for ${targetUser.name} (@${targetUser.username}).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it; resetError = null },
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

          OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; resetError = null },
            label = { Text(if (isChinese) "再次确认新密码" else "Confirm New Password") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
          )

          if (resetError != null) {
            Text(
              text = resetError ?: "",
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
              resetError = if (isChinese) "新密码不能为空" else "Password cannot be empty"
              return@Button
            }
            if (newPassword != confirmPassword) {
              resetError = if (isChinese) "两次输入的密码不一致" else "Passwords do not match"
              return@Button
            }

            onResetPassword(targetUser.id, newPassword)
            resettingPasswordUser = null
            Toast.makeText(
              context,
              if (isChinese) "密码已成功重置！" else "Password reset successfully!",
              Toast.LENGTH_SHORT
            ).show()
          }
        ) {
          Text(if (isChinese) "确认重置" else "Reset")
        }
      },
      dismissButton = {
        TextButton(onClick = { resettingPasswordUser = null }) {
          Text(if (isChinese) "取消" else "Cancel")
        }
      }
    )
  }

  // 4. Delete Confirmation Dialog
  deletingUser?.let { targetUser ->
    val isSelf = targetUser.id == currentUserId

    AlertDialog(
      onDismissRequest = { deletingUser = null },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
          Spacer(modifier = Modifier.width(8.dp))
          Text(if (isChinese) "删除员工账号" else "Delete User", fontWeight = FontWeight.Bold)
        }
      },
      text = {
        if (isSelf) {
          Text(
            text = if (isChinese) "无法删除当前正在登录的管理员账号。" else "You cannot delete your own logged-in account.",
            color = MaterialTheme.colorScheme.error
          )
        } else {
          Text(
            text = if (isChinese) {
              "确定要永久删除员工 \"${targetUser.name}\" (@${targetUser.username}) 吗？\n注意：建议优先使用「冻结」，以便完整保留该员工历史开具的报价单记录。"
            } else {
              "Are you sure you want to delete \"${targetUser.name}\" (@${targetUser.username})?\nNote: Deactivating is recommended to retain quotation history."
            }
          )
        }
      },
      confirmButton = {
        if (!isSelf) {
          Button(
            onClick = {
              onDeleteUser(targetUser.id)
              deletingUser = null
              Toast.makeText(
                context,
                if (isChinese) "用户已删除" else "User deleted",
                Toast.LENGTH_SHORT
              ).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
          ) {
            Text(if (isChinese) "确认删除" else "Delete")
          }
        }
      },
      dismissButton = {
        TextButton(onClick = { deletingUser = null }) {
          Text(if (isChinese) "取消" else "Cancel")
        }
      }
    )
  }
}

@Composable
private fun UserCardItem(
  user: User,
  isChinese: Boolean,
  isCurrentLoggedUser: Boolean,
  onEdit: () -> Unit,
  onResetPassword: () -> Unit,
  onToggleStatus: () -> Unit,
  onDelete: () -> Unit
) {
  val isActive = user.status == "active"
  val isAdmin = user.role.equals("admin", ignoreCase = true)
  val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(16.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Top row: Avatar, Name, Username, Badges
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(
              if (isAdmin) MaterialTheme.colorScheme.primaryContainer
              else MaterialTheme.colorScheme.secondaryContainer
            ),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = user.name.take(1).uppercase(),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = if (isAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = user.name,
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            if (isCurrentLoggedUser) {
              Spacer(modifier = Modifier.width(6.dp))
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
              ) {
                Text(
                  text = if (isChinese) "当前自己" else "You",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
              }
            }
          }

          Text(
            text = "@${user.username}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Status Badge & Role Badge
        Column(horizontalAlignment = Alignment.End) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (isAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
          ) {
            Text(
              text = if (isAdmin) "ADMIN" else "STAFF",
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onPrimary
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            else MaterialTheme.colorScheme.errorContainer
          ) {
            Text(
              text = if (isActive) (if (isChinese) "激活中" else "Active")
              else (if (isChinese) "已冻结" else "Deactivated"),
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              style = MaterialTheme.typography.labelSmall,
              color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
      Spacer(modifier = Modifier.height(8.dp))

      // Bottom Row: Last login & Actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (user.lastLogin > 0) {
            (if (isChinese) "最后登录: " else "Last: ") + dateFormat.format(Date(user.lastLogin))
          } else {
            if (isChinese) "从未登录" else "Never logged in"
          },
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          // Edit
          IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
          }

          // Reset Password
          IconButton(onClick = onResetPassword, modifier = Modifier.size(34.dp)) {
            Icon(Icons.Default.LockReset, contentDescription = "Reset Password", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
          }

          // Toggle Status (Activate / Deactivate)
          OutlinedButton(
            onClick = onToggleStatus,
            shape = RoundedCornerShape(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
            modifier = Modifier.height(30.dp)
          ) {
            Text(
              text = if (isActive) (if (isChinese) "冻结" else "Deactivate") else (if (isChinese) "激活" else "Activate"),
              fontSize = 11.sp,
              color = if (isActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
          }

          // Delete
          if (!isCurrentLoggedUser) {
            IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
              Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
            }
          }
        }
      }
    }
  }
}
