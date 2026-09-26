package com.example.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Product
import kotlinx.coroutines.launch

/**
 * Image Item Representation in Batch Upload Flow
 */
data class BatchImageItem(
  val id: String = java.util.UUID.randomUUID().toString(),
  val uri: Uri? = null,
  val previewUrl: String = "",
  val fileName: String,
  val extractedBarcode: String = "",
  val matchedProduct: Product? = null,
  val isManuallyLinked: Boolean = false,
  val isApplied: Boolean = false
)

@Composable
fun BatchImageUploadScreen(
  isChinese: Boolean,
  products: List<Product>,
  onApplyMatches: (Map<String, String>) -> Unit = {}
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  // State: List of imported batch images
  val imageItems = remember { mutableStateListOf<BatchImageItem>() }
  var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Matched, 2: Review Needed
  var itemForManualLink by remember { mutableStateOf<BatchImageItem?>(null) }
  var isApplying by remember { mutableStateOf(false) }

  // Automatic barcode matcher
  fun matchImageToProduct(fileName: String, uri: Uri?, previewUrl: String): BatchImageItem {
    // 1. Strip file extension
    val baseName = fileName.substringBeforeLast('.').trim()

    // 2. Direct exact barcode match
    val directMatch = products.firstOrNull { it.barcode.equals(baseName, ignoreCase = true) }
    if (directMatch != null) {
      return BatchImageItem(
        uri = uri,
        previewUrl = previewUrl.ifEmpty { uri?.toString() ?: "" },
        fileName = fileName,
        extractedBarcode = directMatch.barcode,
        matchedProduct = directMatch
      )
    }

    // 3. Extract continuous 8-14 digits (EAN/UPC standard)
    val digitRegex = Regex("""(\d{8,14})""")
    val digitMatches = digitRegex.findAll(baseName).map { it.value }.toList()
    for (candidate in digitMatches) {
      val productMatch = products.firstOrNull { it.barcode == candidate }
      if (productMatch != null) {
        return BatchImageItem(
          uri = uri,
          previewUrl = previewUrl.ifEmpty { uri?.toString() ?: "" },
          fileName = fileName,
          extractedBarcode = candidate,
          matchedProduct = productMatch
        )
      }
    }

    // 4. Fallback search: Check if product barcode is contained in the filename
    val substringMatch = products.firstOrNull { baseName.contains(it.barcode) }
    if (substringMatch != null) {
      return BatchImageItem(
        uri = uri,
        previewUrl = previewUrl.ifEmpty { uri?.toString() ?: "" },
        fileName = fileName,
        extractedBarcode = substringMatch.barcode,
        matchedProduct = substringMatch
      )
    }

    // 5. Unmatched -> Goes to Review Area
    val fallbackBarcode = digitMatches.firstOrNull() ?: ""
    return BatchImageItem(
      uri = uri,
      previewUrl = previewUrl.ifEmpty { uri?.toString() ?: "" },
      fileName = fileName,
      extractedBarcode = fallbackBarcode,
      matchedProduct = null
    )
  }

  // Helper to get filename from Uri
  fun getFileNameFromUri(uri: Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
      val cursor = context.contentResolver.query(uri, null, null, null, null)
      cursor?.use {
        if (it.moveToFirst()) {
          val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
          if (nameIndex != -1) {
            result = it.getString(nameIndex)
          }
        }
      }
    }
    if (result == null) {
      result = uri.path
      val cut = result?.lastIndexOf('/') ?: -1
      if (cut != -1) {
        result = result?.substring(cut + 1)
      }
    }
    return result ?: "image_${System.currentTimeMillis()}.jpg"
  }

  // 1. Android Photo Picker Launcher (Modern Zero-Permission PickMultipleVisualMedia)
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 50)
  ) { uris ->
    if (uris.isNotEmpty()) {
      var autoMatchedCount = 0
      uris.forEach { uri ->
        val fileName = getFileNameFromUri(uri)
        val item = matchImageToProduct(fileName, uri, uri.toString())
        if (item.matchedProduct != null) autoMatchedCount++
        imageItems.add(item)
      }
      val msg = if (isChinese) {
        "已载入 ${uris.size} 张图片：自动匹配 $autoMatchedCount 张，未对齐 ${uris.size - autoMatchedCount} 张已归入待处理区"
      } else {
        "Loaded ${uris.size} images: $autoMatchedCount auto-matched, ${uris.size - autoMatchedCount} in Review Area"
      }
      Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }
  }

  // 2. Sample Demo Images Loader (Crucial for streaming emulator testing)
  fun loadDemoSampleImages() {
    val sampleData = listOf(
      Pair("9551234567890.jpg", "https://images.unsplash.com/photo-1558961363-fa8fdf82db35?w=500&auto=format&fit=crop&q=60"),
      Pair("9551234567891.png", "https://images.unsplash.com/photo-1590080875515-8a3a8dc5735e?w=500&auto=format&fit=crop&q=60"),
      Pair("9551234567892.jpg", "https://images.unsplash.com/photo-1566478989037-eec170784d0b?w=500&auto=format&fit=crop&q=60"),
      Pair("9551234567893_tea.jpeg", "https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=500&auto=format&fit=crop&q=60"),
      Pair("unmatched_seasonal_biscuit_box.jpg", "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=500&auto=format&fit=crop&q=60"),
      Pair("promo_beverage_99887766.png", "https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?w=500&auto=format&fit=crop&q=60")
    )

    imageItems.clear()
    var autoMatchedCount = 0
    sampleData.forEach { (fileName, url) ->
      val item = matchImageToProduct(fileName, null, url)
      if (item.matchedProduct != null) autoMatchedCount++
      imageItems.add(item)
    }

    val msg = if (isChinese) {
      "已载入 6 张测试图片：自动匹配 $autoMatchedCount 张，未对齐 ${sampleData.size - autoMatchedCount} 张已归入待处理区"
    } else {
      "Loaded 6 demo images: $autoMatchedCount auto-matched, ${sampleData.size - autoMatchedCount} in Review Area"
    }
    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
  }

  // Computations
  val totalCount = imageItems.size
  val matchedItems = imageItems.filter { it.matchedProduct != null }
  val unmatchedItems = imageItems.filter { it.matchedProduct == null }
  val matchedCount = matchedItems.size
  val unmatchedCount = unmatchedItems.size
  val unappliedMatchedCount = matchedItems.count { !it.isApplied }

  val displayedItems = when (selectedTab) {
    1 -> matchedItems
    2 -> unmatchedItems
    else -> imageItems
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    // 1. Header Card with Title and Instructions
    Surface(
      color = MaterialTheme.colorScheme.surface,
      shadowElevation = 2.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.CloudUpload,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(24.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (isChinese) "条形码图片批量上传与自动匹配" else "Batch Image Upload & Matching",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (isChinese) "Phase 8: 文件名对齐条形码 · 未匹配归入待处理区" else "Phase 8: Auto Barcode Alignment · Review Area",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Matching Logic Badge
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (isChinese) "自动规则：图片文件名与产品条形码一致 (如 9551234567890.jpg) 即可瞬间秒级绑定！"
              else "Auto Logic: Filename containing barcode (e.g. 9551234567890.jpg) auto-links instantly!",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action Buttons Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = {
              photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            },
            modifier = Modifier
              .weight(1f)
              .testTag("pick_images_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
          ) {
            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isChinese) "选择手机图片" else "Pick Photos", maxLines = 1)
          }

          OutlinedButton(
            onClick = { loadDemoSampleImages() },
            modifier = Modifier
              .weight(1f)
              .testTag("load_sample_images_button")
          ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isChinese) "载入测试图包" else "Load Demo", maxLines = 1)
          }

          if (imageItems.isNotEmpty()) {
            IconButton(
              onClick = {
                imageItems.clear()
                Toast.makeText(context, if (isChinese) "已清空已选图片" else "Cleared images", Toast.LENGTH_SHORT).show()
              },
              modifier = Modifier.size(40.dp)
            ) {
              Icon(Icons.Default.Delete, contentDescription = "Clear", tint = MaterialTheme.colorScheme.error)
            }
          }
        }
      }
    }

    // 2. Metrics & KPI Strip
    if (imageItems.isNotEmpty()) {
      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // All
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = MaterialTheme.colorScheme.surface
            ) {
              Text(
                text = "${if (isChinese) "总计" else "Total"}: $totalCount",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
              )
            }
            // Matched
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = MaterialTheme.colorScheme.primaryContainer
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "${if (isChinese) "已匹配" else "Matched"}: $matchedCount",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
              }
            }
            // Review Needed
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = if (unmatchedCount > 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = if (unmatchedCount > 0) Icons.Default.HelpOutline else Icons.Default.Check,
                  contentDescription = null,
                  tint = if (unmatchedCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "${if (isChinese) "待处理" else "Review"}: $unmatchedCount",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                  color = if (unmatchedCount > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          // Batch Apply Button
          if (unappliedMatchedCount > 0) {
            Button(
              onClick = {
                coroutineScope.launch {
                  isApplying = true
                  val matchMap = matchedItems
                    .filter { !it.isApplied && it.matchedProduct != null }
                    .associate { it.matchedProduct!!.id to it.previewUrl }
                  
                  onApplyMatches(matchMap)
                  // Mark as applied
                  for (i in imageItems.indices) {
                    val item = imageItems[i]
                    if (item.matchedProduct != null) {
                      imageItems[i] = item.copy(isApplied = true)
                    }
                  }
                  isApplying = false
                  val successMsg = if (isChinese) {
                    "成功将 ${matchMap.size} 张商品图片批量同步并写入数据库！"
                  } else {
                    "Successfully applied & synced ${matchMap.size} product images!"
                  }
                  Toast.makeText(context, successMsg, Toast.LENGTH_LONG).show()
                }
              },
              enabled = !isApplying,
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
              modifier = Modifier.testTag("apply_all_matched_button")
            ) {
              if (isApplying) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onSecondary, strokeWidth = 2.dp)
              } else {
                Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (isChinese) "批量保存 ($unappliedMatchedCount)" else "Apply ($unappliedMatchedCount)",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }

      // Filter Tabs
      ScrollableTabRow(
        selectedTabIndex = selectedTab,
        edgePadding = 16.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = { Text("${if (isChinese) "全部图片" else "All"} ($totalCount)") }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = { Text("${if (isChinese) "已匹配成功" else "Auto Matched"} ($matchedCount)") }
        )
        Tab(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          text = { Text("${if (isChinese) "待处理区" else "Review Needed"} ($unmatchedCount)") }
        )
      }
    }

    // 3. Body: List or Empty State
    if (imageItems.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Default.CloudUpload,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            modifier = Modifier.size(72.dp)
          )
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = if (isChinese) "暂无待处理图片" else "No Images Selected",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = if (isChinese) "点击下方按钮批量选择手机图片，或一键载入测试图包快速体验自动对齐！"
            else "Pick images from device or load demo barcode images to test auto-matching!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
          Spacer(modifier = Modifier.height(20.dp))
          Button(
            onClick = { loadDemoSampleImages() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
          ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isChinese) "载入 6 张测试图片体验" else "Load 6 Demo Barcode Images")
          }
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("batch_image_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                text = if (isChinese) "该分类下暂无图片" else "No images in this tab",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        items(displayedItems, key = { it.id }) { item ->
          BatchImageItemCard(
            item = item,
            isChinese = isChinese,
            onManualLinkClick = { itemForManualLink = item },
            onRemove = { imageItems.remove(item) },
            onSaveSingle = {
              if (item.matchedProduct != null) {
                coroutineScope.launch {
                  onApplyMatches(mapOf(item.matchedProduct.id to item.previewUrl))
                  val idx = imageItems.indexOfFirst { it.id == item.id }
                  if (idx != -1) {
                    imageItems[idx] = item.copy(isApplied = true)
                  }
                  Toast.makeText(
                    context,
                    if (isChinese) "已将图片保存至 ${item.matchedProduct.name}" else "Saved image to ${item.matchedProduct.name}",
                    Toast.LENGTH_SHORT
                  ).show()
                }
              }
            }
          )
        }
      }
    }
  }

  // 4. Manual Link Dialog (待处理区手动关联弹窗)
  if (itemForManualLink != null) {
    val targetItem = itemForManualLink!!
    ManualLinkDialog(
      item = targetItem,
      products = products,
      isChinese = isChinese,
      onDismiss = { itemForManualLink = null },
      onProductSelected = { selectedProduct ->
        val index = imageItems.indexOfFirst { it.id == targetItem.id }
        if (index != -1) {
          imageItems[index] = targetItem.copy(
            matchedProduct = selectedProduct,
            extractedBarcode = selectedProduct.barcode,
            isManuallyLinked = true,
            isApplied = false
          )
        }
        itemForManualLink = null
        Toast.makeText(
          context,
          if (isChinese) "已手动关联至：${selectedProduct.name} (${selectedProduct.barcode})"
          else "Linked to: ${selectedProduct.name}",
          Toast.LENGTH_SHORT
        ).show()
      }
    )
  }
}

/**
 * Individual Image Item Card
 */
@Composable
fun BatchImageItemCard(
  item: BatchImageItem,
  isChinese: Boolean,
  onManualLinkClick: () -> Unit,
  onRemove: () -> Unit,
  onSaveSingle: () -> Unit
) {
  val isMatched = item.matchedProduct != null

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("batch_image_card_${item.id}"),
    colors = CardDefaults.cardColors(
      containerColor = if (isMatched) MaterialTheme.colorScheme.surface
      else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
    ),
    shape = RoundedCornerShape(12.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // 1. Image Thumbnail
      Box(
        modifier = Modifier
          .size(80.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
      ) {
        if (item.previewUrl.isNotEmpty()) {
          AsyncImage(
            model = item.previewUrl,
            contentDescription = item.fileName,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
          )
        } else {
          Icon(
            imageVector = Icons.Default.Image,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(36.dp)
          )
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      // 2. Info Center Column
      Column(modifier = Modifier.weight(1f)) {
        // Status Tag Row
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (isMatched) {
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = if (item.isManuallyLinked) MaterialTheme.colorScheme.tertiaryContainer
              else MaterialTheme.colorScheme.primaryContainer
            ) {
              Text(
                text = if (item.isManuallyLinked) (if (isChinese) "手动关联" else "Manual Link")
                else (if (isChinese) "✓ 条码对齐" else "✓ Auto Matched"),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (item.isManuallyLinked) MaterialTheme.colorScheme.onTertiaryContainer
                else MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
            if (item.isApplied) {
              Spacer(modifier = Modifier.width(6.dp))
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
              ) {
                Text(
                  text = if (isChinese) "已同步入库" else "Synced",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSecondaryContainer,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
          } else {
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = MaterialTheme.colorScheme.errorContainer
            ) {
              Text(
                text = if (isChinese) "⚠️ 待处理 (未对齐)" else "⚠️ Review Needed",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // File Name
        Text(
          text = item.fileName,
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        // Matched Product Details or Unmatched Prompt
        if (isMatched) {
          val product = item.matchedProduct!!
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = product.name,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = "${if (isChinese) "条码" else "Barcode"}: ${product.barcode} · ${product.packingSize}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        } else {
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = if (isChinese) "文件名无匹配条码，请点击关联" else "Barcode not found, tap Link",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
          )
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      // 3. Action Buttons
      Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        if (!isMatched) {
          Button(
            onClick = onManualLinkClick,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier.testTag("link_product_button_${item.id}")
          ) {
            Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(if (isChinese) "关联商品" else "Link", fontSize = 12.sp)
          }
        } else {
          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = onManualLinkClick,
              modifier = Modifier.size(32.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Re-link",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
              )
            }
            if (!item.isApplied) {
              IconButton(
                onClick = onSaveSingle,
                modifier = Modifier.size(32.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = "Save Single",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          }
        }

        IconButton(
          onClick = onRemove,
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Clear,
            contentDescription = "Remove",
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}

/**
 * Manual Link Product Selection Dialog
 */
@Composable
fun ManualLinkDialog(
  item: BatchImageItem,
  products: List<Product>,
  isChinese: Boolean,
  onDismiss: () -> Unit,
  onProductSelected: (Product) -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf<String?>(null) }

  // Extract unique categories
  val categories = remember(products) {
    products.map { it.categoryName }.distinct().sorted()
  }

  // Filter products by search query and category
  val filteredProducts = remember(products, searchQuery, selectedCategory) {
    products.filter { product ->
      val matchesCategory = selectedCategory == null || product.categoryName == selectedCategory
      val matchesQuery = searchQuery.isBlank() ||
          product.name.contains(searchQuery, ignoreCase = true) ||
          product.barcode.contains(searchQuery, ignoreCase = true) ||
          product.packingSize.contains(searchQuery, ignoreCase = true)
      matchesCategory && matchesQuery
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (isChinese) "手动关联商品到此图片" else "Link Image to Product",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 500.dp)
      ) {
        // Target Image Info
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
              contentAlignment = Alignment.Center
            ) {
              if (item.previewUrl.isNotEmpty()) {
                AsyncImage(
                  model = item.previewUrl,
                  contentDescription = item.fileName,
                  modifier = Modifier.fillMaxSize(),
                  contentScale = ContentScale.Crop
                )
              } else {
                Icon(Icons.Default.Image, contentDescription = null)
              }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = item.fileName,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = if (isChinese) "请在下方检索并选择要绑定的目标商品：" else "Search and choose the target product:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Input
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          label = { Text(if (isChinese) "输入品名或条形码搜索" else "Search Name or Barcode") },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { searchQuery = "" }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear")
              }
            }
          },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("manual_link_search_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Category Filter Chips
        if (categories.isNotEmpty()) {
          ScrollableTabRow(
            selectedTabIndex = if (selectedCategory == null) 0 else categories.indexOf(selectedCategory) + 1,
            edgePadding = 0.dp,
            divider = {},
            modifier = Modifier.fillMaxWidth()
          ) {
            Tab(
              selected = selectedCategory == null,
              onClick = { selectedCategory = null },
              text = { Text(if (isChinese) "全部" else "All") }
            )
            categories.forEach { cat ->
              Tab(
                selected = selectedCategory == cat,
                onClick = { selectedCategory = cat },
                text = { Text(cat) }
              )
            }
          }
          Spacer(modifier = Modifier.height(8.dp))
        }

        // Product Candidates List
        Text(
          text = "${if (isChinese) "可选商品" else "Products"} (${filteredProducts.size})",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          if (filteredProducts.isEmpty()) {
            item {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(24.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = if (isChinese) "未找到匹配商品" else "No matching products found",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          items(filteredProducts, key = { it.id }) { product ->
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onProductSelected(product) }
                .testTag("manual_link_product_${product.id}"),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
              shape = RoundedCornerShape(8.dp)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                      shape = RoundedCornerShape(4.dp),
                      color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                      Text(
                        text = product.barcode,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                      )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = product.packingSize,
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }

                Button(
                  onClick = { onProductSelected(product) },
                  colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  modifier = Modifier.height(32.dp)
                ) {
                  Text(if (isChinese) "选择" else "Select", fontSize = 11.sp)
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {},
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(if (isChinese) "取消" else "Cancel")
      }
    }
  )
}
