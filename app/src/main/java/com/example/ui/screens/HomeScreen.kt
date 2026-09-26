package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.navigation.Screen

@Composable
fun HomeScreen(
  isChinese: Boolean,
  currentUser: String,
  userRole: String,
  shareListCount: Int,
  productCount: Int = 6,
  categoryCount: Int = 6,
  quotationCount: Int = 2,
  categories: List<String> = listOf("Biscuits", "Snacks", "Drinks", "Candy", "Instant Food", "Others"),
  onNavigateToRoute: (String) -> Unit,
  onSearchSubmitted: (String) -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  val sampleCategories = listOf("All") + categories
  var selectedCategory by remember { mutableStateOf("All") }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
  ) {
    // 1. Header: Company Branding & Greeting
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(48.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = painterResource(id = R.drawable.app_logo_icon_1790321246982),
          contentDescription = "Logo",
          modifier = Modifier.size(38.dp)
        )
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "SHHKM TRADING",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = if (isChinese) "欢迎回来, $currentUser (${if (userRole == "admin") "管理员" else "业务员"})"
          else "Welcome, $currentUser (${userRole.replaceFirstChar { it.uppercase() }})",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // 2. MOST PROMINENT SEARCH BAR (Mandatory Requirement)
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Text(
          text = if (isChinese) "🔍 快速搜索产品 (品名 / 条形码)" else "🔍 Search Products (Name / Barcode)",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = {
            Text(
              if (isChinese) "输入产品名称或扫描条码号 (如 955123...)"
              else "Enter name or barcode number..."
            )
          },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Search Icon",
              tint = MaterialTheme.colorScheme.primary
            )
          },
          trailingIcon = {
            if (searchQuery.isNotBlank()) {
              Surface(
                onClick = {
                  onSearchSubmitted(searchQuery)
                  onNavigateToRoute(Screen.Catalogue.route)
                },
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(end = 4.dp)
              ) {
                Text(
                  text = if (isChinese) "查找" else "Go",
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                  style = MaterialTheme.typography.labelMedium
                )
              }
            }
          },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("home_search_input"),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
          )
        )
      }
    }

    // 3. Category Quick Filter Chips
    Text(
      text = if (isChinese) "产品分类" else "Categories",
      style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onBackground,
      modifier = Modifier.padding(bottom = 8.dp)
    )
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      contentPadding = PaddingValues(bottom = 16.dp)
    ) {
      items(sampleCategories) { category ->
        val selected = selectedCategory == category
        FilterChip(
          selected = selected,
          onClick = {
            selectedCategory = category
            onNavigateToRoute(Screen.Catalogue.route)
          },
          label = { Text(category) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
          ),
          shape = RoundedCornerShape(8.dp)
        )
      }
    }

    // 4. Quick Action Grid / Cards
    Text(
      text = if (isChinese) "核心功能" else "Core Modules",
      style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onBackground,
      modifier = Modifier.padding(bottom = 12.dp)
    )

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      HomeFeatureCard(
        modifier = Modifier.weight(1f),
        title = if (isChinese) "全部产品" else "Catalogue",
        subtitle = if (isChinese) "两公司共用同一产品库" else "Shared Database (No Price)",
        icon = Icons.Default.Inventory2,
        accentColor = MaterialTheme.colorScheme.primary,
        onClick = { onNavigateToRoute(Screen.Catalogue.route) }
      )
      HomeFeatureCard(
        modifier = Modifier.weight(1f),
        title = if (isChinese) "分享清单" else "Share List",
        subtitle = if (isChinese) "发给客户选品 (无价格)" else "Customer Share (No Price)",
        icon = Icons.Default.Share,
        accentColor = MaterialTheme.colorScheme.secondary,
        badgeCount = shareListCount,
        onClick = { onNavigateToRoute(Screen.ShareList.route) }
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      HomeFeatureCard(
        modifier = Modifier.weight(1f),
        title = if (isChinese) "报价单" else "Quotations",
        subtitle = if (isChinese) "Company 1 / 2 独立开单" else "C1 / C2 Independent Quotes",
        icon = Icons.Default.ReceiptLong,
        accentColor = MaterialTheme.colorScheme.tertiary,
        onClick = { onNavigateToRoute(Screen.QuotationHistory.route) }
      )
      HomeFeatureCard(
        modifier = Modifier.weight(1f),
        title = if (isChinese) "系统设置" else "Settings",
        subtitle = if (isChinese) "用户/公司/导入管理" else "User / Company / Import",
        icon = Icons.Default.Settings,
        accentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        onClick = { onNavigateToRoute(Screen.Settings.route) }
      )
    }

    Spacer(modifier = Modifier.height(20.dp))

    // 5. Dashboard Summary
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
      shape = RoundedCornerShape(14.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = if (isChinese) "📊 数据总览" else "📊 System Summary",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          StatItem(title = if (isChinese) "产品总数" else "Products", value = "$productCount")
          StatItem(title = if (isChinese) "产品分类" else "Categories", value = "$categoryCount")
          StatItem(title = if (isChinese) "待发选品" else "In Share List", value = "$shareListCount")
          StatItem(title = if (isChinese) "近期报价" else "Quotations", value = "$quotationCount")
        }
      }
    }
  }
}

@Composable
private fun HomeFeatureCard(
  modifier: Modifier = Modifier,
  title: String,
  subtitle: String,
  icon: ImageVector,
  accentColor: androidx.compose.ui.graphics.Color,
  badgeCount: Int = 0,
  onClick: () -> Unit
) {
  Card(
    modifier = modifier
      .clickable(onClick = onClick)
      .height(124.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    shape = RoundedCornerShape(14.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(14.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(accentColor.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = title,
            tint = accentColor,
            modifier = Modifier.size(20.dp)
          )
        }
        if (badgeCount > 0) {
          Badge { Text("$badgeCount") }
        } else {
          Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Arrow",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(18.dp)
          )
        }
      }
      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1
        )
      }
    }
  }
}

@Composable
private fun StatItem(title: String, value: String) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = value,
      style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.primary
    )
    Text(
      text = title,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}
