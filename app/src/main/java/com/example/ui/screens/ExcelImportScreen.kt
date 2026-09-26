package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

enum class BarcodeConflictStrategy {
  UPDATE, // Update existing product's name, packing size, category
  SKIP    // Keep existing product as is, ignore this row
}

data class ParsedImportRow(
  val rowIndex: Int,
  val name: String,
  val barcode: String,
  val packingSize: String,
  val category: String,
  val isExisting: Boolean,
  val isValid: Boolean,
  val errorMessage: String? = null
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExcelImportScreen(
  isChinese: Boolean,
  existingProducts: List<Product>,
  onBatchImport: (productsToInsertOrUpdate: List<Product>) -> Unit
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  var conflictStrategy by remember { mutableStateOf(BarcodeConflictStrategy.UPDATE) }
  var parsedRows by remember { mutableStateOf<List<ParsedImportRow>>(emptyList()) }
  var importSummaryMessage by remember { mutableStateOf<String?>(null) }
  var showTemplateDialog by remember { mutableStateOf(false) }
  var isProcessing by remember { mutableStateOf(false) }

  // Document picker launcher for CSV or text spreadsheets
  val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
  ) { uri: Uri? ->
    if (uri != null) {
      coroutineScope.launch {
        isProcessing = true
        try {
          val parsed = parseCsvUri(context, uri, existingProducts)
          parsedRows = parsed
          importSummaryMessage = null
          if (parsed.isEmpty()) {
            Toast.makeText(context, if (isChinese) "未解析到有效数据行" else "No valid data rows found", Toast.LENGTH_SHORT).show()
          } else {
            Toast.makeText(context, if (isChinese) "成功读取 ${parsed.size} 行数据" else "Loaded ${parsed.size} rows", Toast.LENGTH_SHORT).show()
          }
        } catch (e: Exception) {
          Toast.makeText(context, (if (isChinese) "文件读取失败: " else "Failed to parse file: ") + e.localizedMessage, Toast.LENGTH_LONG).show()
        } finally {
          isProcessing = false
        }
      }
    }
  }

  // Pre-calculated stats
  val totalRows = parsedRows.size
  val validRows = parsedRows.filter { it.isValid }
  val newRows = validRows.filter { !it.isExisting }
  val existingRows = validRows.filter { it.isExisting }
  val invalidRows = parsedRows.filter { !it.isValid }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(16.dp)
  ) {
    // Top Title & Architecture Banner
    Text(
      text = if (isChinese) "Excel / CSV 批量导入产品 (Batch Import)" else "Excel / CSV Batch Import",
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onBackground
    )

    Spacer(modifier = Modifier.height(4.dp))

    // Zero-Price Architecture Banner
    Surface(
      shape = RoundedCornerShape(10.dp),
      color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (isChinese) {
            "导入规则：仅需【品名、条码、包装规格、分类】4列。产品库严格保持零价格(Zero-Price)，两公司共用同一产品库！"
          } else {
            "Required columns: Product Name, Barcode, Packing Size, Category. Strictly No-Price architecture."
          },
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onPrimaryContainer,
          fontWeight = FontWeight.Medium
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // File Actions Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(14.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (isChinese) "导入数据源 (Data Source)" else "Import Source",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )

          TextButton(onClick = { showTemplateDialog = true }) {
            Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(if (isChinese) "查看/复制标准模版" else "View Template")
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // File Picker Button
          Button(
            onClick = {
              filePickerLauncher.launch(arrayOf("*/*"))
            },
            modifier = Modifier
              .weight(1f)
              .height(46.dp)
              .testTag("select_file_button"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary
            )
          ) {
            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isChinese) "选择 .csv / 文件" else "Choose File",
              fontWeight = FontWeight.SemiBold
            )
          }

          // One-Click Demo Sample Button
          OutlinedButton(
            onClick = {
              val sampleDemoRows = generateSampleImportData(existingProducts)
              parsedRows = sampleDemoRows
              importSummaryMessage = null
              Toast.makeText(
                context,
                if (isChinese) "已载入 6 款标准快消产品示例数据" else "Loaded 6 demo FMCG products",
                Toast.LENGTH_SHORT
              ).show()
            },
            modifier = Modifier
              .weight(1f)
              .height(46.dp)
              .testTag("load_sample_data_button"),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isChinese) "一键载入示例数据" else "Load Demo",
              fontWeight = FontWeight.Medium
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Duplicate Barcode Conflict Strategy
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(14.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.QrCode, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (isChinese) "若条形码在产品库中已存在：" else "When barcode already exists in database:",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { conflictStrategy = BarcodeConflictStrategy.UPDATE }
          ) {
            RadioButton(
              selected = conflictStrategy == BarcodeConflictStrategy.UPDATE,
              onClick = { conflictStrategy = BarcodeConflictStrategy.UPDATE }
            )
            Text(
              text = if (isChinese) "覆盖更新现有资料" else "Update existing product",
              fontSize = 13.sp,
              fontWeight = if (conflictStrategy == BarcodeConflictStrategy.UPDATE) FontWeight.Bold else FontWeight.Normal
            )
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { conflictStrategy = BarcodeConflictStrategy.SKIP }
          ) {
            RadioButton(
              selected = conflictStrategy == BarcodeConflictStrategy.SKIP,
              onClick = { conflictStrategy = BarcodeConflictStrategy.SKIP }
            )
            Text(
              text = if (isChinese) "跳过保留原样" else "Skip row",
              fontSize = 13.sp,
              fontWeight = if (conflictStrategy == BarcodeConflictStrategy.SKIP) FontWeight.Bold else FontWeight.Normal
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Success Notification Banner
    AnimatedVisibility(visible = importSummaryMessage != null) {
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp)
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = importSummaryMessage ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }

    // Pre-Import Verification Summary & Table
    if (parsedRows.isNotEmpty()) {
      // Statistics Bar
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (isChinese) "待导入预览 (共 $totalRows 行)" else "Preview ($totalRows rows)",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
          )

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
              text = "${if (isChinese) "新增" else "New"}: ${newRows.size}",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = "${if (isChinese) "重复" else "Dup"}: ${existingRows.size}",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.secondary
            )
            if (invalidRows.isNotEmpty()) {
              Text(
                text = "${if (isChinese) "异常" else "Err"}: ${invalidRows.size}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Parsed Rows List
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        itemsIndexed(parsedRows) { index, row ->
          ImportRowCard(
            row = row,
            conflictStrategy = conflictStrategy,
            isChinese = isChinese
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Action Buttons (Confirm Import or Clear)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedButton(
          onClick = {
            parsedRows = emptyList()
            importSummaryMessage = null
          },
          modifier = Modifier
            .weight(0.7f)
            .height(50.dp),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(if (isChinese) "清空" else "Clear")
        }

        val importableCount = if (conflictStrategy == BarcodeConflictStrategy.UPDATE) {
          validRows.size
        } else {
          newRows.size
        }

        Button(
          onClick = {
            if (importableCount == 0) {
              Toast.makeText(
                context,
                if (isChinese) "没有可导入的产品（已被跳过或存在校验错误）" else "No importable products",
                Toast.LENGTH_SHORT
              ).show()
              return@Button
            }

            val now = System.currentTimeMillis()
            val existingMap = existingProducts.associateBy { it.barcode }

            val productsToSave = mutableListOf<Product>()
            var newlyAddedCount = 0
            var updatedCount = 0

            for (row in validRows) {
              val existing = existingMap[row.barcode]
              if (existing != null) {
                if (conflictStrategy == BarcodeConflictStrategy.UPDATE) {
                  productsToSave.add(
                    existing.copy(
                      name = row.name,
                      packingSize = row.packingSize,
                      categoryId = "cat_${row.category.lowercase().replace(" ", "_")}",
                      categoryName = row.category,
                      updatedAt = now
                    )
                  )
                  updatedCount++
                } // else skip
              } else {
                productsToSave.add(
                  Product(
                    id = "prd_imp_${System.currentTimeMillis()}_${row.barcode}",
                    name = row.name,
                    barcode = row.barcode,
                    packingSize = row.packingSize,
                    categoryId = "cat_${row.category.lowercase().replace(" ", "_")}",
                    categoryName = row.category,
                    imageUrl = "",
                    description = "",
                    status = "active",
                    createdAt = now,
                    updatedAt = now
                  )
                )
                newlyAddedCount++
              }
            }

            onBatchImport(productsToSave)

            importSummaryMessage = if (isChinese) {
              "导入成功！已新增 $newlyAddedCount 款，更新 $updatedCount 款产品至共享主库！"
            } else {
              "Import complete: $newlyAddedCount added, $updatedCount updated."
            }

            parsedRows = emptyList()
            Toast.makeText(context, importSummaryMessage, Toast.LENGTH_LONG).show()
          },
          modifier = Modifier
            .weight(1.3f)
            .height(50.dp)
            .testTag("confirm_import_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
          )
        ) {
          Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = (if (isChinese) "确认批量入库 " else "Confirm Import ") + "($importableCount)",
            fontWeight = FontWeight.Bold
          )
        }
      }
    } else {
      // Empty Placeholder View
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(24.dp)
        ) {
          Icon(
            imageVector = Icons.Default.TableChart,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            modifier = Modifier.size(64.dp)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = if (isChinese) "暂无待导入数据" else "No Data Loaded",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = if (isChinese) {
              "点击上方「选择 .csv / 文件」或「一键载入示例数据」进行校验与预览"
            } else {
              "Choose a CSV file or tap 'Load Demo' to preview import items"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }

  // Template Help Dialog
  if (showTemplateDialog) {
    val templateCsvText = "Product Name,Barcode,Packing Size,Category\n" +
      "Crispy Butter Cookies,9556100112233,10g x 20pcs x 12box,Biscuits\n" +
      "White Coffee 3-in-1,9556100223344,40g x 15s x 20bags,Beverages\n" +
      "Original Potato Chips,9556100334455,60g x 24 cans,Snacks"

    AlertDialog(
      onDismissRequest = { showTemplateDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.width(8.dp))
          Text(if (isChinese) "Excel / CSV 标准字段规范" else "CSV Template Format", fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = if (isChinese) {
              "标准 CSV 表头（不区分大小写，支持中英文）：\n• Product Name (品名)\n• Barcode (条形码)\n• Packing Size (包装规格 - 原样 Free Text)\n• Category (分类)\n\n严格禁忌：请勿包含 Price（价格）列！"
            } else {
              "Required 4 columns:\n• Product Name\n• Barcode\n• Packing Size (Raw Text)\n• Category\n\nNo price column allowed."
            },
            style = MaterialTheme.typography.bodySmall
          )

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = templateCsvText,
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp,
              modifier = Modifier.padding(10.dp)
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("CSV Template", templateCsvText)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, if (isChinese) "已复制模版至剪贴板" else "Template copied to clipboard", Toast.LENGTH_SHORT).show()
            showTemplateDialog = false
          }
        ) {
          Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(if (isChinese) "复制模版" else "Copy Template")
        }
      },
      dismissButton = {
        TextButton(onClick = { showTemplateDialog = false }) {
          Text(if (isChinese) "关闭" else "Close")
        }
      }
    )
  }
}

@Composable
private fun ImportRowCard(
  row: ParsedImportRow,
  conflictStrategy: BarcodeConflictStrategy,
  isChinese: Boolean
) {
  val badgeColor = when {
    !row.isValid -> MaterialTheme.colorScheme.error
    !row.isExisting -> MaterialTheme.colorScheme.primary
    conflictStrategy == BarcodeConflictStrategy.UPDATE -> MaterialTheme.colorScheme.secondary
    else -> MaterialTheme.colorScheme.outline
  }

  val badgeText = when {
    !row.isValid -> (if (isChinese) "格式错误" else "Invalid")
    !row.isExisting -> (if (isChinese) "[NEW] 新增" else "[NEW] Insert")
    conflictStrategy == BarcodeConflictStrategy.UPDATE -> (if (isChinese) "[UPDATE] 覆盖更新" else "[UPDATE] Overwrite")
    else -> (if (isChinese) "[SKIP] 跳过" else "[SKIP] Ignore")
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(10.dp),
    border = if (!row.isValid) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)) else null
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "#${row.rowIndex}  ${row.name.ifBlank { if (isChinese) "(品名缺失)" else "(Missing name)" }}",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          color = if (row.name.isBlank()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.weight(1f)
        )

        Surface(
          shape = RoundedCornerShape(4.dp),
          color = badgeColor.copy(alpha = 0.15f)
        ) {
          Text(
            text = badgeText,
            color = badgeColor,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "条码: ${row.barcode.ifBlank { "(空)" }}",
          style = MaterialTheme.typography.bodySmall,
          color = if (row.barcode.isBlank()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
          text = "•",
          color = MaterialTheme.colorScheme.outlineVariant
        )

        Text(
          text = "规格: ${row.packingSize.ifBlank { "(空)" }}",
          style = MaterialTheme.typography.bodySmall,
          color = if (row.packingSize.isBlank()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
        )

        Text(
          text = "•",
          color = MaterialTheme.colorScheme.outlineVariant
        )

        Text(
          text = row.category,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      if (row.errorMessage != null) {
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "错误: ${row.errorMessage}",
          color = MaterialTheme.colorScheme.error,
          fontSize = 11.sp
        )
      }
    }
  }
}

/**
 * Parses a CSV URI into ParsedImportRow models with column mapping and validation.
 */
private suspend fun parseCsvUri(
  context: Context,
  uri: Uri,
  existingProducts: List<Product>
): List<ParsedImportRow> = withContext(Dispatchers.IO) {
  val existingBarcodes = existingProducts.map { it.barcode }.toSet()
  val result = mutableListOf<ParsedImportRow>()

  context.contentResolver.openInputStream(uri)?.use { inputStream ->
    val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
    val lines = reader.readLines().filter { it.isNotBlank() }

    if (lines.isEmpty()) return@withContext emptyList()

    // Parse header to find column indices
    val headerLine = lines[0]
    val delimiter = if (headerLine.contains("\t")) "\t" else ","
    val headerColumns = splitCsvLine(headerLine, delimiter).map { it.trim().lowercase() }

    var nameIdx = headerColumns.indexOfFirst { it.contains("name") || it.contains("品名") || it.contains("产品") }
    var barcodeIdx = headerColumns.indexOfFirst { it.contains("barcode") || it.contains("条码") || it.contains("条形码") || it == "code" }
    var packingIdx = headerColumns.indexOfFirst { it.contains("packing") || it.contains("规格") || it.contains("size") }
    var catIdx = headerColumns.indexOfFirst { it.contains("category") || it.contains("分类") || it.contains("类别") }

    // Fallback default index positions if headers are standard order [Name, Barcode, Packing, Category]
    if (nameIdx == -1 && headerColumns.isNotEmpty()) nameIdx = 0
    if (barcodeIdx == -1 && headerColumns.size > 1) barcodeIdx = 1
    if (packingIdx == -1 && headerColumns.size > 2) packingIdx = 2
    if (catIdx == -1 && headerColumns.size > 3) catIdx = 3

    // Process data rows
    for (i in 1 until lines.size) {
      val line = lines[i]
      val tokens = splitCsvLine(line, delimiter)

      val name = if (nameIdx in tokens.indices) tokens[nameIdx].trim() else ""
      val barcode = if (barcodeIdx in tokens.indices) tokens[barcodeIdx].trim() else ""
      val packingSize = if (packingIdx in tokens.indices) tokens[packingIdx].trim() else ""
      val category = if (catIdx in tokens.indices) tokens[catIdx].trim().ifBlank { "General" } else "General"

      val isExisting = existingBarcodes.contains(barcode)
      val isValid = name.isNotBlank() && barcode.isNotBlank() && packingSize.isNotBlank()
      val errorMsg = when {
        name.isBlank() -> "产品名称不可为空"
        barcode.isBlank() -> "条形码不可为空"
        packingSize.isBlank() -> "包装规格不可为空"
        else -> null
      }

      result.add(
        ParsedImportRow(
          rowIndex = i,
          name = name,
          barcode = barcode,
          packingSize = packingSize,
          category = category,
          isExisting = isExisting,
          isValid = isValid,
          errorMessage = errorMsg
        )
      )
    }
  }

  result
}

/**
 * Splits CSV string with quote preservation.
 */
private fun splitCsvLine(line: String, delimiter: String): List<String> {
  val tokens = mutableListOf<String>()
  val sb = StringBuilder()
  var inQuotes = false

  var i = 0
  while (i < line.length) {
    val c = line[i]
    if (c == '\"') {
      inQuotes = !inQuotes
    } else if (!inQuotes && line.startsWith(delimiter, i)) {
      tokens.add(sb.toString().trim())
      sb.clear()
      i += delimiter.length - 1
    } else {
      sb.append(c)
    }
    i++
  }
  tokens.add(sb.toString().trim())
  return tokens
}

/**
 * Generates sample demo items for testing without external files.
 */
private fun generateSampleImportData(existingProducts: List<Product>): List<ParsedImportRow> {
  val existingBarcodes = existingProducts.map { it.barcode }.toSet()

  val sampleProducts = listOf(
    Triple("Royal Danish Butter Cookies", "9556100112233", "454g x 12 tins") to "Biscuits",
    Triple("Cream Crackers Golden", "9556100223344", "428g x 12 packets") to "Biscuits",
    Triple("Roasted White Coffee 3-in-1", "9556100334455", "40g x 15 sachets x 20 bags") to "Beverages",
    Triple("Crispy Seaweed Roll Original", "9556100445566", "3g x 12 rolls x 10 boxes") to "Snacks",
    Triple("Chewy Fruit Gummies Assorted", "9556100556677", "100g x 24 bags") to "Confectionery",
    Triple("Instant Beef Noodles Bowl", "9556100667788", "110g x 12 bowls") to "Instant Food"
  )

  return sampleProducts.mapIndexed { idx, item ->
    val (info, cat) = item
    val (name, barcode, packing) = info
    val isExisting = existingBarcodes.contains(barcode)

    ParsedImportRow(
      rowIndex = idx + 1,
      name = name,
      barcode = barcode,
      packingSize = packing,
      category = cat,
      isExisting = isExisting,
      isValid = true,
      errorMessage = null
    )
  }
}
