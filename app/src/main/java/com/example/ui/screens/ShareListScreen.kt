package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareListScreen(
  isChinese: Boolean,
  items: List<Product>,
  onRemoveItem: (Product) -> Unit,
  onClearAll: () -> Unit,
  onCreateQuotation: (List<Product>) -> Unit,
  onGoToCatalogue: () -> Unit
) {
  val context = LocalContext.current

  // State: Selected product IDs for sharing / quotation creation (default: all checked)
  val selectedProductIds = remember(items) {
    mutableStateListOf<String>().apply {
      addAll(items.map { it.id })
    }
  }

  // State: Filter/search within share list
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
  var showClearConfirmDialog by remember { mutableStateOf(false) }

  // Unique categories in the share list
  val uniqueCategories = remember(items) {
    items.map { it.categoryName }.distinct().sorted()
  }

  // Filtered items
  val displayedItems = remember(items, searchQuery, selectedCategoryFilter) {
    items.filter { product ->
      val matchesCategory = selectedCategoryFilter == null || product.categoryName == selectedCategoryFilter
      val matchesQuery = searchQuery.isBlank() ||
          product.name.contains(searchQuery, ignoreCase = true) ||
          product.barcode.contains(searchQuery, ignoreCase = true) ||
          product.packingSize.contains(searchQuery, ignoreCase = true)
      matchesCategory && matchesQuery
    }
  }

  // Items currently selected by user checkboxes
  val activeSelectedProducts = remember(items, selectedProductIds) {
    items.filter { selectedProductIds.contains(it.id) }
  }

  // Generate Customer-Facing Clean Text (Strictly Zero Price!)
  fun generateCustomerText(productsToShare: List<Product>): String {
    val sb = StringBuilder()
    sb.append(if (isChinese) "【SHHKM 贸易 · 精选商品推荐清单】\n" else "【SHHKM Trading · Product Recommendation List】\n")
    sb.append(if (isChinese) "📅 推荐时间: ${java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())}\n"
    else "📅 Date: ${java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())}\n")
    sb.append(if (isChinese) "ℹ️ 正式报价根据采购订量及公司条款另行提供\n"
    else "ℹ️ Official quotation will be issued based on order volume and terms\n")
    sb.append("------------------------------------------\n\n")

    productsToShare.forEachIndexed { index, product ->
      sb.append("${index + 1}. *${product.name}*\n")
      sb.append("   • ${if (isChinese) "条形码" else "Barcode"}: ${product.barcode}\n")
      sb.append("   • ${if (isChinese) "包装规格" else "Packing Size"}: ${product.packingSize}\n")
      sb.append("   • ${if (isChinese) "商品分类" else "Category"}: ${product.categoryName}\n\n")
    }

    sb.append("------------------------------------------\n")
    sb.append(if (isChinese) "共推荐 ${productsToShare.size} 件商品，如需定制报价单请随时联系销售人员！"
    else "Total ${productsToShare.size} products. Contact sales representative for official quotation!")
    return sb.toString()
  }

  fun shareViaWhatsApp() {
    val targetProducts = if (activeSelectedProducts.isNotEmpty()) activeSelectedProducts else items
    val text = generateCustomerText(targetProducts)
    val sendIntent = Intent().apply {
      action = Intent.ACTION_SEND
      putExtra(Intent.EXTRA_TEXT, text)
      type = "text/plain"
      setPackage("com.whatsapp")
    }
    try {
      context.startActivity(sendIntent)
    } catch (_: Exception) {
      val fallback = Intent.createChooser(
        Intent().apply {
          action = Intent.ACTION_SEND
          putExtra(Intent.EXTRA_TEXT, text)
          type = "text/plain"
        },
        if (isChinese) "分享产品清单给客户" else "Share Product List"
      )
      context.startActivity(fallback)
    }
  }

  fun copyToClipboard() {
    val targetProducts = if (activeSelectedProducts.isNotEmpty()) activeSelectedProducts else items
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Products", generateCustomerText(targetProducts)))
    Toast.makeText(
      context,
      if (isChinese) "已复制 ${targetProducts.size} 件商品清单 (不含价格)" else "Copied ${targetProducts.size} items (No Price)",
      Toast.LENGTH_SHORT
    ).show()
  }

  fun shareGeneral() {
    val targetProducts = if (activeSelectedProducts.isNotEmpty()) activeSelectedProducts else items
    val text = generateCustomerText(targetProducts)
    val shareIntent = Intent.createChooser(
      Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
      },
      if (isChinese) "分享推荐清单" else "Share Recommendation List"
    )
    context.startActivity(shareIntent)
  }

  if (items.isEmpty()) {
    // Empty State Screen
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(32.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Box(
          modifier = Modifier
            .size(88.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Share,
            contentDescription = "Empty Share List",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(44.dp)
          )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
          text = if (isChinese) "分享清单目前是空的" else "Share List is Empty",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = if (isChinese) "在「产品目录」中点击 [+ 加入清单] 即可将商品快速归集到这里，支持一键客户分享或生成正式报价单。"
          else "Browse Catalogue and tap [+ Add Share] to gather products here for customer sharing or quoting.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
          onClick = onGoToCatalogue,
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
          modifier = Modifier.testTag("go_to_catalogue_button")
        ) {
          Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(if (isChinese) "前往产品目录挑选" else "Browse Catalogue")
        }
      }
    }
  } else {
    // Non-Empty Content
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ) {
      // 1. Top Bar & Zero Price Guarantee Banner
      Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (isChinese) "待发客户清单 (${items.size} 件商品)" else "Customer Share List (${items.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
              )
              Text(
                text = if (isChinese) "已勾选 ${activeSelectedProducts.size} 件用于分享与制作报价"
                else "${activeSelectedProducts.size} selected for sharing & quote",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
              )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              OutlinedButton(
                onClick = onGoToCatalogue,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(34.dp)
              ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isChinese) "继续加选" else "Add More", fontSize = 12.sp)
              }

              Spacer(modifier = Modifier.width(8.dp))

              IconButton(
                onClick = { showClearConfirmDialog = true },
                modifier = Modifier.size(34.dp)
              ) {
                Icon(
                  Icons.Default.Delete,
                  contentDescription = "Clear All",
                  tint = MaterialTheme.colorScheme.error,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Strict Business Rule Notice
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (isChinese) "🛡️ 铁律保障：分享清单完全剥离价格，对外发送 100% 安全；报价价格在进入报价单时由员工针对客户手动键入。"
                else "🛡️ Zero-Price Guarantee: Share list strictly contains no price. Price is keyed in when creating a quote.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Search & Select All Controls
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Select All Checkbox
            val isAllSelected = selectedProductIds.size == items.size
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clickable {
                  if (isAllSelected) {
                    selectedProductIds.clear()
                  } else {
                    selectedProductIds.clear()
                    selectedProductIds.addAll(items.map { it.id })
                  }
                }
                .padding(end = 8.dp)
            ) {
              Checkbox(
                checked = isAllSelected,
                onCheckedChange = { checked ->
                  selectedProductIds.clear()
                  if (checked) {
                    selectedProductIds.addAll(items.map { it.id })
                  }
                },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.size(32.dp)
              )
              Spacer(modifier = Modifier.width(2.dp))
              Text(
                text = if (isChinese) "全选" else "All",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
              )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Search Bar
            OutlinedTextField(
              value = searchQuery,
              onValueChange = { searchQuery = it },
              placeholder = { Text(if (isChinese) "清单内快速搜索..." else "Search list...") },
              leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
              trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                  IconButton(onClick = { searchQuery = "" }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                  }
                }
              },
              singleLine = true,
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("share_list_search_input")
            )
          }

          // Category Filter Chips
          if (uniqueCategories.size > 1) {
            Spacer(modifier = Modifier.height(8.dp))
            ScrollableTabRow(
              selectedTabIndex = if (selectedCategoryFilter == null) 0 else uniqueCategories.indexOf(selectedCategoryFilter) + 1,
              edgePadding = 0.dp,
              divider = {},
              modifier = Modifier.fillMaxWidth()
            ) {
              Tab(
                selected = selectedCategoryFilter == null,
                onClick = { selectedCategoryFilter = null },
                text = { Text("${if (isChinese) "全部" else "All"} (${items.size})") }
              )
              uniqueCategories.forEach { category ->
                val catCount = items.count { it.categoryName == category }
                Tab(
                  selected = selectedCategoryFilter == category,
                  onClick = { selectedCategoryFilter = category },
                  text = { Text("$category ($catCount)") }
                )
              }
            }
          }
        }
      }

      // 2. Items List
      LazyColumn(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
          .testTag("share_items_list"),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        if (displayedItems.isEmpty()) {
          item {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = if (isChinese) "没有找到匹配的商品" else "No matching items",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        itemsIndexed(displayedItems, key = { _, item -> item.id }) { index, product ->
          val isChecked = selectedProductIds.contains(product.id)

          Card(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("share_item_card_${product.id}"),
            colors = CardDefaults.cardColors(
              containerColor = if (isChecked) MaterialTheme.colorScheme.surface
              else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isChecked) 2.dp else 0.dp),
            border = if (isChecked) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer) else null
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Selection Checkbox
              Checkbox(
                checked = isChecked,
                onCheckedChange = { checked ->
                  if (checked) {
                    if (!selectedProductIds.contains(product.id)) selectedProductIds.add(product.id)
                  } else {
                    selectedProductIds.remove(product.id)
                  }
                },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.size(32.dp)
              )

              // Index Badge
              Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(24.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              Spacer(modifier = Modifier.width(8.dp))

              // Product Image (AsyncImage with Fallback)
              Box(
                modifier = Modifier
                  .size(60.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
              ) {
                if (product.imageUrl.isNotEmpty()) {
                  AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                  )
                } else {
                  Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = product.name,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(30.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.width(10.dp))

              // Info Column
              Column(modifier = Modifier.weight(1f)) {
                // Category Chip
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = MaterialTheme.colorScheme.primaryContainer
                ) {
                  Text(
                    text = product.categoryName,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                  )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = product.name,
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "${if (isChinese) "条码: " else "Barcode: "}${product.barcode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "•",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = product.packingSize,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }

              // Remove Button
              IconButton(
                onClick = {
                  onRemoveItem(product)
                  selectedProductIds.remove(product.id)
                  Toast.makeText(
                    context,
                    if (isChinese) "已移出清单: ${product.name}" else "Removed: ${product.name}",
                    Toast.LENGTH_SHORT
                  ).show()
                },
                modifier = Modifier.size(32.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Delete,
                  contentDescription = "Remove",
                  tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }

      // 3. Bottom Action Bar (Customer Sharing & Quotation Creation)
      Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          // Share Channels Row
          Text(
            text = if (isChinese) "📤 发送推荐清单给客户 (绝对不含价格)" else "📤 Send to Customer (Strictly No Price)",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(6.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            ShareChannelButton(
              modifier = Modifier.weight(1f),
              title = "WhatsApp",
              icon = Icons.Default.Share,
              color = MaterialTheme.colorScheme.primary,
              onClick = { shareViaWhatsApp() }
            )
            ShareChannelButton(
              modifier = Modifier.weight(1f),
              title = if (isChinese) "复制文本" else "Copy Text",
              icon = Icons.Default.ContentCopy,
              color = MaterialTheme.colorScheme.secondary,
              onClick = { copyToClipboard() }
            )
            ShareChannelButton(
              modifier = Modifier.weight(1f),
              title = if (isChinese) "系统分享" else "Share Sheet",
              icon = Icons.Default.Share,
              color = MaterialTheme.colorScheme.tertiary,
              onClick = { shareGeneral() }
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Make Quotation Primary Button
          val quotationProducts = if (activeSelectedProducts.isNotEmpty()) activeSelectedProducts else items
          Button(
            onClick = {
              onCreateQuotation(quotationProducts)
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("create_quotation_from_share_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
          ) {
            Icon(
              imageVector = Icons.Default.ReceiptLong,
              contentDescription = "Quotation",
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (isChinese) "制作正式报价单 (${quotationProducts.size} 件商品 · 手动填价)"
              else "Create Official Quotation (${quotationProducts.size} items · Key Price)",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp)
            )
          }
        }
      }
    }
  }

  // Clear Confirmation Dialog
  if (showClearConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showClearConfirmDialog = false },
      title = {
        Text(if (isChinese) "确认清空分享清单？" else "Clear Share List?")
      },
      text = {
        Text(if (isChinese) "此操作将从当前清单中移除全部 ${items.size} 件商品，不会删除产品主数据。"
        else "This will clear all ${items.size} items from current share list. Master product data is not affected.")
      },
      confirmButton = {
        Button(
          onClick = {
            onClearAll()
            selectedProductIds.clear()
            showClearConfirmDialog = false
            Toast.makeText(context, if (isChinese) "已清空分享清单" else "Cleared share list", Toast.LENGTH_SHORT).show()
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text(if (isChinese) "确认清空" else "Clear All")
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearConfirmDialog = false }) {
          Text(if (isChinese) "取消" else "Cancel")
        }
      }
    )
  }
}

@Composable
private fun ShareChannelButton(
  modifier: Modifier = Modifier,
  title: String,
  icon: ImageVector,
  color: androidx.compose.ui.graphics.Color,
  onClick: () -> Unit
) {
  OutlinedButton(
    onClick = onClick,
    modifier = modifier.height(42.dp),
    shape = RoundedCornerShape(8.dp),
    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
  ) {
    Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(16.dp), tint = color)
    Spacer(modifier = Modifier.width(4.dp))
    Text(text = title, fontSize = 12.sp, color = color, maxLines = 1)
  }
}
