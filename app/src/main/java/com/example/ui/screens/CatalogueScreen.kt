package com.example.ui.screens

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Product

@Composable
fun CatalogueScreen(
  isChinese: Boolean,
  initialQuery: String = "",
  products: List<Product>,
  categories: List<String>,
  shareListProductIds: Set<String>,
  onToggleShareList: (Product) -> Unit,
  onProductClick: (String) -> Unit
) {
  var searchQuery by remember { mutableStateOf(initialQuery) }
  var isGridView by remember { mutableStateOf(true) }
  var selectedCategory by remember { mutableStateOf("All") }

  val filteredProducts = products.filter { item ->
    val matchesCategory = selectedCategory == "All" || item.categoryName == selectedCategory
    val matchesSearch = searchQuery.isBlank() ||
      item.name.contains(searchQuery, ignoreCase = true) ||
      item.barcode.contains(searchQuery, ignoreCase = true)
    matchesCategory && matchesSearch
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    // 1. Search and View Toggle Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = {
          Text(
            if (isChinese) "搜产品名称或条码..." else "Search product name or barcode...",
            fontSize = 14.sp
          )
        },
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
              Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
            }
          }
        },
        singleLine = true,
        modifier = Modifier
          .weight(1f)
          .testTag("catalogue_search_bar"),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = MaterialTheme.colorScheme.primary,
          unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
      )

      Spacer(modifier = Modifier.width(8.dp))

      Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable { isGridView = !isGridView }
      ) {
        IconButton(onClick = { isGridView = !isGridView }) {
          Icon(
            imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
            contentDescription = "Toggle Grid/List",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // 2. Category Filter Chips
    val categoryList = listOf("All") + categories
    LazyRow(
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.padding(bottom = 10.dp)
    ) {
      items(categoryList) { cat ->
        FilterChip(
          selected = selectedCategory == cat,
          onClick = { selectedCategory = cat },
          label = { Text(cat) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
          ),
          shape = RoundedCornerShape(8.dp)
        )
      }
    }

    // Shared Database Notice
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = if (isChinese) "共 ${filteredProducts.size} 件商品 (Company 1 & 2 共用产品库)"
        else "${filteredProducts.size} Products (Shared Database)",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Text(
        text = if (isChinese) "数据库不含价格" else "Price Free Database",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.secondary
      )
    }

    // 3. Product Grid or List
    LazyVerticalGrid(
      columns = if (isGridView) GridCells.Fixed(2) else GridCells.Fixed(1),
      contentPadding = PaddingValues(16.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier
        .fillMaxSize()
        .testTag("product_grid")
    ) {
      items(filteredProducts, key = { it.id }) { product ->
        val isInShareList = shareListProductIds.contains(product.id)
        ProductCatalogueCard(
          product = product,
          isGridView = isGridView,
          isChinese = isChinese,
          isInShareList = isInShareList,
          onCardClick = { onProductClick(product.id) },
          onToggleShare = { onToggleShareList(product) }
        )
      }
    }
  }
}

@Composable
fun ProductCatalogueCard(
  product: Product,
  isGridView: Boolean,
  isChinese: Boolean,
  isInShareList: Boolean,
  onCardClick: () -> Unit,
  onToggleShare: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onCardClick)
      .testTag("product_card_${product.id}"),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    shape = RoundedCornerShape(14.dp)
  ) {
    if (isGridView) {
      // Grid Card (Image prominent at top)
      Column(modifier = Modifier.padding(12.dp)) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(10.dp))
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
              contentDescription = "Placeholder",
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
              modifier = Modifier.size(48.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = product.name,
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = "${if (isChinese) "条码: " else "Barcode: "}${product.barcode}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
          text = "${if (isChinese) "规格: " else "Packing: "}${product.packingSize}",
          style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
          color = MaterialTheme.colorScheme.primary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
          onClick = onToggleShare,
          modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .testTag("toggle_share_${product.id}"),
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isInShareList) MaterialTheme.colorScheme.secondary
            else MaterialTheme.colorScheme.primaryContainer,
            contentColor = if (isInShareList) MaterialTheme.colorScheme.onSecondary
            else MaterialTheme.colorScheme.onPrimaryContainer
          ),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Icon(
            imageVector = if (isInShareList) Icons.Default.Check else Icons.Default.Add,
            contentDescription = "Share Action",
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (isInShareList) {
              if (isChinese) "已加入清单" else "In Share List"
            } else {
              if (isChinese) "+ 加入清单" else "+ Add Share"
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    } else {
      // List Card
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Inventory2,
            contentDescription = "Icon",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(36.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = product.name,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "${if (isChinese) "条码: " else "Barcode: "}${product.barcode}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "${if (isChinese) "规格: " else "Packing: "}${product.packingSize}",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.primary
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
          onClick = onToggleShare,
          modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
              if (isInShareList) MaterialTheme.colorScheme.secondary
              else MaterialTheme.colorScheme.primaryContainer
            )
        ) {
          Icon(
            imageVector = if (isInShareList) Icons.Default.Check else Icons.Default.Add,
            contentDescription = "Toggle Share",
            tint = if (isInShareList) MaterialTheme.colorScheme.onSecondary
            else MaterialTheme.colorScheme.onPrimaryContainer
          )
        }
      }
    }
  }
}
