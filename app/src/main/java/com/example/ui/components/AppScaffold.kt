package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
  currentRoute: String?,
  title: String,
  canNavigateBack: Boolean,
  onNavigateBack: () -> Unit,
  onNavigateToRoute: (String) -> Unit,
  shareListCount: Int = 0,
  isChinese: Boolean = true,
  onToggleLanguage: () -> Unit = {},
  actions: @Composable RowScope.() -> Unit = {},
  content: @Composable (Modifier) -> Unit
) {
  // Routes where the bottom navigation bar should be visible
  val bottomBarRoutes = listOf(
    Screen.Home.route,
    Screen.Catalogue.route,
    Screen.ShareList.route,
    Screen.QuotationHistory.route,
    Screen.Settings.route
  )
  val showBottomBar = currentRoute in bottomBarRoutes

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
          )
        },
        navigationIcon = {
          if (canNavigateBack) {
            IconButton(
              onClick = onNavigateBack,
              modifier = Modifier.testTag("nav_back_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        },
        actions = {
          IconButton(
            onClick = onToggleLanguage,
            modifier = Modifier.testTag("toggle_language_button")
          ) {
            Icon(
              imageVector = Icons.Filled.Language,
              contentDescription = "Toggle Language",
              tint = MaterialTheme.colorScheme.primary
            )
          }
          actions()
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    bottomBar = {
      if (showBottomBar) {
        NavigationBar(
          containerColor = MaterialTheme.colorScheme.surface,
          tonalElevation = 8.dp
        ) {
          // Home
          NavigationBarItem(
            selected = currentRoute == Screen.Home.route,
            onClick = { onNavigateToRoute(Screen.Home.route) },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text(if (isChinese) "首页" else "Home") },
            modifier = Modifier.testTag("nav_item_home"),
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = MaterialTheme.colorScheme.primary,
              indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
          )
          // Products / Catalogue
          NavigationBarItem(
            selected = currentRoute == Screen.Catalogue.route,
            onClick = { onNavigateToRoute(Screen.Catalogue.route) },
            icon = { Icon(Icons.Filled.Inventory2, contentDescription = "Catalogue") },
            label = { Text(if (isChinese) "产品目录" else "Products") },
            modifier = Modifier.testTag("nav_item_products"),
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = MaterialTheme.colorScheme.primary,
              indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
          )
          // Share List (with badge)
          NavigationBarItem(
            selected = currentRoute == Screen.ShareList.route,
            onClick = { onNavigateToRoute(Screen.ShareList.route) },
            icon = {
              BadgedBox(badge = {
                if (shareListCount > 0) {
                  Badge { Text("$shareListCount") }
                }
              }) {
                Icon(Icons.Filled.Share, contentDescription = "Share List")
              }
            },
            label = { Text(if (isChinese) "分享清单" else "Share") },
            modifier = Modifier.testTag("nav_item_share_list"),
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = MaterialTheme.colorScheme.primary,
              indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
          )
          // Quotations
          NavigationBarItem(
            selected = currentRoute == Screen.QuotationHistory.route,
            onClick = { onNavigateToRoute(Screen.QuotationHistory.route) },
            icon = { Icon(Icons.Filled.ReceiptLong, contentDescription = "Quotations") },
            label = { Text(if (isChinese) "报价单" else "Quotations") },
            modifier = Modifier.testTag("nav_item_quotations"),
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = MaterialTheme.colorScheme.primary,
              indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
          )
          // Settings / More
          NavigationBarItem(
            selected = currentRoute == Screen.Settings.route,
            onClick = { onNavigateToRoute(Screen.Settings.route) },
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
            label = { Text(if (isChinese) "设置" else "Settings") },
            modifier = Modifier.testTag("nav_item_settings"),
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = MaterialTheme.colorScheme.primary,
              indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
          )
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      content(Modifier)
    }
  }
}
