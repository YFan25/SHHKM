package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Unarchive
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.Product

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductManagementScreen(
  isChinese: Boolean,
  products: List<Product>,
  categories: List<Category>,
  onAddProduct: (Product) -> Unit,
  onUpdateProduct: (Product) -> Unit,
  onDeleteProduct: (String) -> Unit,
  onToggleStatus: (String, String) -> Unit
) {
  val context = LocalContext.current

  // Filter States
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("All") }
  var selectedStatus by remember { mutableStateOf("All") } // "All", "active", "archived"

  // Dialog States
  var showAddDialog by remember { mutableStateOf(false) }
  var editingProduct by remember { mutableStateOf<Product?>(null) }
  var deletingProduct by remember { mutableStateOf<Product?>(null) }

  // Available unique categories
  val availableCategories = listOf("All") + (categories.map { it.name } + products.map { it.categoryName }).distinct()

  // Filtered List
  val filteredProducts = products.filter { product ->
    val matchesSearch = product.name.contains(searchQuery, ignoreCase = true) ||
      product.barcode.contains(searchQuery, ignoreCase = true) ||
      product.packingSize.contains(searchQuery, ignoreCase = true)

    val matchesCategory = selectedCategory == "All" ||
      product.categoryName.equals(selectedCategory, ignoreCase = true)

    val matchesStatus = when (selectedStatus) {
      "active" -> product.status == "active"
      "archived" -> product.status != "active"
      else -> true
    }

    matchesSearch && matchesCategory && matchesStatus
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(16.dp)
  ) {
    // Top Bar & Action
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = if (isChinese) "产品主数据管理" else "Product Master Data",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = if (isChinese) "共 ${products.size} 款产品 (${products.count { it.status == "active" }} 在售)" else "Total ${products.size} items (${products.count { it.status == "active" }} active)",
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
        modifier = Modifier.testTag("add_product_button")
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Product", modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (isChinese) "新增产品" else "Add Product",
          fontWeight = FontWeight.SemiBold
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Business Rules Architecture Badge
    Surface(
      shape = RoundedCornerShape(10.dp),
      color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Info,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.secondary,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (isChinese) {
            "Company 1 与 Company 2 共用商品库 • 数据库严格零价格(Zero-Price)"
          } else {
            "Shared product catalog (Company 1 & 2) • Zero-Price architecture"
          },
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSecondaryContainer,
          fontWeight = FontWeight.Medium
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Search Input Field
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("product_search_input"),
      placeholder = { Text(if (isChinese) "输入品名、条形码或包装规格搜索..." else "Search by product name, barcode or packing...") },
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

    Spacer(modifier = Modifier.height(8.dp))

    // Filter Chips: Status & Category
    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      // Status Chips
      FilterChip(
        selected = selectedStatus == "All",
        onClick = { selectedStatus = "All" },
        label = { Text(if (isChinese) "全部状态" else "All Status") },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
          selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
      )
      FilterChip(
        selected = selectedStatus == "active",
        onClick = { selectedStatus = "active" },
        label = { Text(if (isChinese) "在售中 (${products.count { it.status == "active" }})" else "Active (${products.count { it.status == "active" }})") },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
          selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
      )
      FilterChip(
        selected = selectedStatus == "archived",
        onClick = { selectedStatus = "archived" },
        label = { Text(if (isChinese) "已封存 (${products.count { it.status != "active" }})" else "Archived (${products.count { it.status != "active" }})") },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
          selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
        )
      )

      // Category Chips
      availableCategories.take(5).forEach { cat ->
        FilterChip(
          selected = selectedCategory == cat,
          onClick = { selectedCategory = cat },
          label = { Text(cat) }
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Product List
    if (filteredProducts.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.Inventory2,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(52.dp)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = if (isChinese) "未找到符合条件的产品" else "No matching products found",
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
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(filteredProducts, key = { it.id }) { product ->
          ProductManagementItem(
            product = product,
            isChinese = isChinese,
            onEdit = { editingProduct = product },
            onToggleStatus = {
              val next = if (product.status == "active") "archived" else "active"
              onToggleStatus(product.id, next)
              Toast.makeText(
                context,
                if (isChinese) "产品状态已切换" else "Product status updated",
                Toast.LENGTH_SHORT
              ).show()
            },
            onDelete = { deletingProduct = product }
          )
        }
      }
    }
  }

  // 1. Add Product Dialog
  if (showAddDialog) {
    ProductFormDialog(
      isChinese = isChinese,
      dialogTitle = if (isChinese) "手工录入新产品" else "Add New Product Master",
      existingProduct = null,
      existingBarcodes = products.map { it.barcode },
      categories = categories.map { it.name },
      onDismiss = { showAddDialog = false },
      onConfirm = { newProduct ->
        onAddProduct(newProduct)
        showAddDialog = false
        Toast.makeText(
          context,
          if (isChinese) "新产品已存入共用数据库！" else "Product created successfully!",
          Toast.LENGTH_SHORT
        ).show()
      }
    )
  }

  // 2. Edit Product Dialog
  editingProduct?.let { targetProduct ->
    ProductFormDialog(
      isChinese = isChinese,
      dialogTitle = if (isChinese) "编辑产品资料" else "Edit Product Master",
      existingProduct = targetProduct,
      existingBarcodes = products.filter { it.id != targetProduct.id }.map { it.barcode },
      categories = categories.map { it.name },
      onDismiss = { editingProduct = null },
      onConfirm = { updatedProduct ->
        onUpdateProduct(updatedProduct)
        editingProduct = null
        Toast.makeText(
          context,
          if (isChinese) "产品信息已更新！" else "Product updated successfully!",
          Toast.LENGTH_SHORT
        ).show()
      }
    )
  }

  // 3. Delete Confirmation Dialog
  deletingProduct?.let { targetProduct ->
    AlertDialog(
      onDismissRequest = { deletingProduct = null },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
          Spacer(modifier = Modifier.width(8.dp))
          Text(if (isChinese) "删除产品档案" else "Delete Product", fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Text(
          text = if (isChinese) {
            "确定要永久删除产品「${targetProduct.name}」(${targetProduct.barcode}) 吗？\n\n注意：如果历史报价单中曾包含此产品，强烈建议优先使用「封存 (Archive)」，以确保历史业务单据的可溯性与完整性。"
          } else {
            "Are you sure you want to delete \"${targetProduct.name}\" (${targetProduct.barcode})?\n\nNote: If this product exists in historical quotations, deactivating/archiving is recommended to preserve quotation records."
          }
        )
      },
      confirmButton = {
        Button(
          onClick = {
            onDeleteProduct(targetProduct.id)
            deletingProduct = null
            Toast.makeText(
              context,
              if (isChinese) "产品已删除" else "Product deleted",
              Toast.LENGTH_SHORT
            ).show()
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text(if (isChinese) "确认删除" else "Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { deletingProduct = null }) {
          Text(if (isChinese) "取消" else "Cancel")
        }
      }
    )
  }
}

@Composable
private fun ProductManagementItem(
  product: Product,
  isChinese: Boolean,
  onEdit: () -> Unit,
  onToggleStatus: () -> Unit,
  onDelete: () -> Unit
) {
  val isActive = product.status == "active"

  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(14.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = product.name,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(6.dp))

          // Barcode & Category Badges
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.QrCode,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = product.barcode,
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(6.dp),
              color = MaterialTheme.colorScheme.surfaceVariant
            ) {
              Text(
                text = product.categoryName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Packing Size (Raw Free Text)
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
          ) {
            Text(
              text = (if (isChinese) "包装规格: " else "Packing: ") + product.packingSize,
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
              color = MaterialTheme.colorScheme.secondary,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }
        }

        // Status Badge & Action Controls
        Column(horizontalAlignment = Alignment.End) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (isActive) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
          ) {
            Text(
              text = if (isActive) (if (isChinese) "在售" else "Active")
              else (if (isChinese) "已封存" else "Archived"),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
      Spacer(modifier = Modifier.height(4.dp))

      // Bottom Actions Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          // Edit
          IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
          }

          // Toggle Status (Archive / Unarchive)
          IconButton(onClick = onToggleStatus, modifier = Modifier.size(34.dp)) {
            Icon(
              imageVector = if (isActive) Icons.Default.Archive else Icons.Default.Unarchive,
              contentDescription = "Toggle Status",
              modifier = Modifier.size(16.dp),
              tint = if (isActive) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
            )
          }

          // Delete
          IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProductFormDialog(
  isChinese: Boolean,
  dialogTitle: String,
  existingProduct: Product?,
  existingBarcodes: List<String>,
  categories: List<String>,
  onDismiss: () -> Unit,
  onConfirm: (Product) -> Unit
) {
  var name by remember { mutableStateOf(existingProduct?.name ?: "") }
  var barcode by remember { mutableStateOf(existingProduct?.barcode ?: "") }
  var packingSize by remember { mutableStateOf(existingProduct?.packingSize ?: "") }
  var categoryName by remember { mutableStateOf(existingProduct?.categoryName ?: (categories.firstOrNull() ?: "Biscuits")) }
  var status by remember { mutableStateOf(existingProduct?.status ?: "active") }
  var formError by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(text = dialogTitle, fontWeight = FontWeight.Bold)
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Zero-price architecture reminder
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ) {
          Text(
            text = if (isChinese) {
              "重要铁律：产品主库绝无价格字段，两公司共用同一产品库。单价仅在制作报价单时现场指定。"
            } else {
              "Strict Rule: No price field in master catalog. Prices are quoted during quotation creation."
            },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(8.dp)
          )
        }

        // Product Name
        OutlinedTextField(
          value = name,
          onValueChange = { name = it; formError = null },
          label = { Text(if (isChinese) "产品名称 (Product Name) *" else "Product Name *") },
          placeholder = { Text("e.g. Crispy Butter Cookies") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("product_name_input")
        )

        // Barcode
        OutlinedTextField(
          value = barcode,
          onValueChange = { barcode = it.trim(); formError = null },
          label = { Text(if (isChinese) "条形码 (Barcode) *" else "Barcode *") },
          placeholder = { Text("e.g. 9556123456789") },
          leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("product_barcode_input")
        )

        // Packing Size (Raw Text)
        OutlinedTextField(
          value = packingSize,
          onValueChange = { packingSize = it; formError = null },
          label = { Text(if (isChinese) "包装规格 (Packing Size - 原样文本) *" else "Packing Size (Raw Text) *") },
          placeholder = { Text("e.g. 10g x 20pcs x 12box") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("product_packing_input")
        )

        // Category Selection & Quick Suggestion Chips
        Text(
          text = if (isChinese) "产品分类：" else "Category:",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold
        )

        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          val popularCategories = (categories + listOf("Biscuits", "Beverages", "Snacks", "Confectionery", "Instant Food")).distinct()
          popularCategories.take(6).forEach { cat ->
            FilterChip(
              selected = categoryName.equals(cat, ignoreCase = true),
              onClick = { categoryName = cat },
              label = { Text(cat, fontSize = 12.sp) }
            )
          }
        }

        OutlinedTextField(
          value = categoryName,
          onValueChange = { categoryName = it },
          label = { Text(if (isChinese) "自定义分类名称" else "Custom Category Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        // Status Selection
        Text(
          text = if (isChinese) "产品状态：" else "Status:",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = status == "active", onClick = { status = "active" })
            Text(if (isChinese) "在售 (Active)" else "Active", fontSize = 14.sp)
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = status == "archived", onClick = { status = "archived" })
            Text(if (isChinese) "封存 (Archived)" else "Archived", fontSize = 14.sp)
          }
        }

        if (formError != null) {
          Text(
            text = formError ?: "",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isBlank() || barcode.isBlank() || packingSize.isBlank()) {
            formError = if (isChinese) "品名、条形码与包装规格均为必填项" else "Name, barcode and packing size are required"
            return@Button
          }

          if (existingBarcodes.any { it.equals(barcode.trim(), ignoreCase = true) }) {
            formError = if (isChinese) "该条形码已存在，请勿重复录入" else "Barcode already exists"
            return@Button
          }

          val now = System.currentTimeMillis()
          val productResult = Product(
            id = existingProduct?.id ?: "prd_${System.currentTimeMillis()}",
            name = name.trim(),
            barcode = barcode.trim(),
            packingSize = packingSize.trim(),
            categoryId = "cat_${categoryName.lowercase().replace(" ", "_")}",
            categoryName = categoryName.trim(),
            imageUrl = "",
            description = "",
            status = status,
            createdAt = existingProduct?.createdAt ?: now,
            updatedAt = now
          )

          onConfirm(productResult)
        }
      ) {
        Text(if (isChinese) "确认保存" else "Save")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(if (isChinese) "取消" else "Cancel")
      }
    }
  )
}
