package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.Product

@Composable
fun ProductDetailScreen(
  product: Product?,
  isChinese: Boolean,
  isInShareList: Boolean,
  onToggleShareList: () -> Unit,
  onCreateQuotation: () -> Unit
) {
  val context = LocalContext.current

  if (product == null) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text(if (isChinese) "未找到该产品" else "Product not found")
    }
    return
  }

  fun copyBarcode() {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Barcode", product.barcode)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, if (isChinese) "条码已复制到剪贴板" else "Barcode copied to clipboard", Toast.LENGTH_SHORT).show()
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
  ) {
    // 1. Hero Image Container
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(240.dp)
        .clip(RoundedCornerShape(16.dp))
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
          tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
          modifier = Modifier.size(80.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 2. Main Product Info Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        // Category Badge
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = MaterialTheme.colorScheme.primaryContainer,
          modifier = Modifier.padding(bottom = 8.dp)
        ) {
          Text(
            text = product.categoryName,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
        }

        // Product Name
        Text(
          text = product.name,
          style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(12.dp))

        // Barcode Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(
              text = if (isChinese) "产品条形码 (Barcode)" else "Barcode",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = product.barcode,
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
          }
          IconButton(onClick = { copyBarcode() }) {
            Icon(
              imageVector = Icons.Default.ContentCopy,
              contentDescription = "Copy Barcode",
              tint = MaterialTheme.colorScheme.primary
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Packing Size Row
        Column {
          Text(
            text = if (isChinese) "包装规格 (Packing Size - 原样文本)" else "Packing Size (Free Text)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = product.packingSize,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Strict Separation Alert
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = if (isChinese) "ℹ️ 产品库不设价格。价格仅在制作报价单时由员工手动输入。"
            else "ℹ️ No price in database. Price is manually keyed in when creating a quotation.",
            modifier = Modifier.padding(10.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // 3. Action Buttons
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      OutlinedButton(
        onClick = onToggleShareList,
        modifier = Modifier
          .weight(1f)
          .height(50.dp)
          .testTag("detail_share_button"),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(
          imageVector = if (isInShareList) Icons.Default.Check else Icons.Default.Share,
          contentDescription = "Share",
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (isInShareList) {
            if (isChinese) "已在清单中" else "In Share List"
          } else {
            if (isChinese) "加入分享清单" else "Add to Share"
          }
        )
      }

      Button(
        onClick = onCreateQuotation,
        modifier = Modifier
          .weight(1f)
          .height(50.dp)
          .testTag("detail_quotation_button"),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
      ) {
        Icon(
          imageVector = Icons.Default.ReceiptLong,
          contentDescription = "Quote",
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = if (isChinese) "制作报价单" else "Create Quote")
      }
    }
  }
}
