package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Company
import com.example.data.model.Product
import com.example.data.model.Quotation
import com.example.data.model.QuotationItem
import com.example.ui.theme.Company1Color
import com.example.ui.theme.Company2Color
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun QuotationCreateScreen(
  isChinese: Boolean,
  initialProducts: List<Product>,
  companies: List<Company>,
  currentUserName: String,
  currentUserId: String,
  onSaveQuotation: (Quotation) -> Unit
) {
  val context = LocalContext.current
  var selectedCompanyId by remember { mutableStateOf("company_1") }

  val company1 = companies.find { it.id == "company_1" }
  val company2 = companies.find { it.id == "company_2" }
  val currentCompany = if (selectedCompanyId == "company_1") company1 else company2

  val generatedQuoteNo = currentCompany?.let {
    "${it.quotationPrefix}${String.format("%04d", it.nextSequence)}"
  } ?: (if (selectedCompanyId == "company_1") "C1-Q-2026-0002" else "C2-Q-2026-0002")

  val priceMap = remember {
    mutableStateMapOf<String, String>().apply {
      initialProducts.forEach { put(it.id, "RM 45.00/ctn") }
    }
  }

  var remarks by remember(currentCompany) {
    mutableStateOf(currentCompany?.termsAndConditions ?: "1. Validity: 14 days.\n2. Goods sold are non-refundable.")
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
  ) {
    // 1. Company Selection Card
    Text(
      text = if (isChinese) "第一步：选择开单公司 (Company Selection)" else "Step 1: Select Company",
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(8.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      CompanySelectCard(
        modifier = Modifier.weight(1f),
        companyName = company1?.name ?: "Company 1 (SHHKM)",
        prefix = company1?.quotationPrefix ?: "C1-Q-2026-",
        accentColor = Company1Color,
        isSelected = selectedCompanyId == "company_1",
        onSelect = { selectedCompanyId = "company_1" }
      )
      CompanySelectCard(
        modifier = Modifier.weight(1f),
        companyName = company2?.name ?: "Company 2 (GLOBAL)",
        prefix = company2?.quotationPrefix ?: "C2-Q-2026-",
        accentColor = Company2Color,
        isSelected = selectedCompanyId == "company_2",
        onSelect = { selectedCompanyId = "company_2" }
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Quotation Number preview banner
    Surface(
      shape = RoundedCornerShape(10.dp),
      color = MaterialTheme.colorScheme.surfaceVariant,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = if (isChinese) "独立报价编号 (云端/本地自增)" else "Independent Quotation Number",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = generatedQuoteNo,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = if (selectedCompanyId == "company_1") Company1Color else Company2Color
          )
        }
        Text(
          text = if (isChinese) "两公司流水号互不混淆" else "Independent Sequence",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.secondary
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // 2. Manual Price Input for Each Product
    Text(
      text = if (isChinese) "第二步：逐项填写报价 (Manual Price Input)" else "Step 2: Key In Price per Item",
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onBackground
    )
    Text(
      text = if (isChinese) "注意：价格只归属于本份报价单，绝对不会写回产品数据库"
      else "Notice: Price belongs strictly to this quotation and is never saved to product database",
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.secondary
    )

    Spacer(modifier = Modifier.height(12.dp))

    initialProducts.forEachIndexed { index, product ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "${index + 1}.",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.width(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = product.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "${if (isChinese) "条码: " else "Barcode: "}${product.barcode}  |  ${if (isChinese) "规格: " else "Packing: "}${product.packingSize}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Price Manual Key-in Box (No quantity allowed)
          OutlinedTextField(
            value = priceMap[product.id] ?: "",
            onValueChange = { priceMap[product.id] = it },
            label = { Text(if (isChinese) "手动输入报价 (Price)" else "Key In Price") },
            placeholder = { Text("例如: RM 48.00/ctn 或 RM 15.50") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("quotation_price_input_${product.id}"),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = MaterialTheme.colorScheme.primary,
              unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 3. Remarks
    Text(
      text = if (isChinese) "第三步：条款与备注 (Remarks / Terms)" else "Step 3: Remarks & Terms",
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
      value = remarks,
      onValueChange = { remarks = it },
      modifier = Modifier
        .fillMaxWidth()
        .height(100.dp)
        .testTag("quotation_remarks_input"),
      shape = RoundedCornerShape(10.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline
      )
    )

    Spacer(modifier = Modifier.height(24.dp))

    // 4. Save & Preview Button
    Button(
      onClick = {
        val quotationItems = initialProducts.map { p ->
          QuotationItem(
            productId = p.id,
            productName = p.name,
            barcode = p.barcode,
            packingSize = p.packingSize,
            imageUrl = p.imageUrl,
            manualPrice = priceMap[p.id] ?: "RM 0.00"
          )
        }
        val newQuote = Quotation(
          id = "qtn_${System.currentTimeMillis()}",
          quotationNumber = generatedQuoteNo,
          companyId = selectedCompanyId,
          companyName = currentCompany?.name ?: "SHHKM TRADING SDN BHD",
          createdDate = System.currentTimeMillis(),
          createdByUserId = currentUserId,
          createdByName = currentUserName,
          items = quotationItems,
          remarks = remarks,
          status = "active"
        )
        onSaveQuotation(newQuote)
      },
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .testTag("quotation_preview_button"),
      shape = RoundedCornerShape(12.dp),
      colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
      Icon(Icons.Default.ReceiptLong, contentDescription = "Preview", modifier = Modifier.size(20.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = if (isChinese) "保存并预览正式报价单" else "Save & Preview Quotation",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
      )
    }
  }
}

@Composable
private fun CompanySelectCard(
  modifier: Modifier = Modifier,
  companyName: String,
  prefix: String,
  accentColor: androidx.compose.ui.graphics.Color,
  isSelected: Boolean,
  onSelect: () -> Unit
) {
  Card(
    modifier = modifier
      .clickable(onClick = onSelect)
      .border(
        width = if (isSelected) 2.dp else 1.dp,
        color = if (isSelected) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp)
      ),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected) accentColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
    ),
    shape = RoundedCornerShape(12.dp)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Business,
          contentDescription = companyName,
          tint = accentColor,
          modifier = Modifier.size(24.dp)
        )
        RadioButton(
          selected = isSelected,
          onClick = onSelect,
          modifier = Modifier.size(24.dp)
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = companyName,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1
      )
      Text(
        text = "Prefix: $prefix",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
fun QuotationPreviewScreen(
  quotation: Quotation?,
  company: Company?,
  isChinese: Boolean,
  onNavigateBack: () -> Unit
) {
  val context = LocalContext.current

  if (quotation == null) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text(if (isChinese) "未找到该报价单" else "Quotation not found")
    }
    return
  }

  val isCompany1 = quotation.companyId == "company_1"
  val companyDisplayName = company?.name ?: quotation.companyName
  val regNo = company?.regNo ?: "202001012345 (1380123-X)"
  val address = company?.address ?: "No. 12, Jalan Industri 3, 43000 Selangor, Malaysia"
  val phone = company?.phone ?: "+60 3-8765 4321"
  val email = company?.email ?: "sales@shhkm.com"
  val bankDetails = company?.bankDetails ?: "Maybank: 5140-1234-5678 (SHHKM TRADING SDN BHD)"
  val dateFormatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(quotation.createdDate))

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(12.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        // Company Letterhead Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = companyDisplayName,
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
              color = if (isCompany1) Company1Color else Company2Color
            )
            Text(
              text = "Reg: $regNo",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "$address\nTel: $phone | Email: $email",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = (if (isCompany1) Company1Color else Company2Color).copy(alpha = 0.15f)
          ) {
            Text(
              text = if (isChinese) "OFFICIAL QUOTATION\n正式报价单" else "OFFICIAL\nQUOTATION",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
              color = if (isCompany1) Company1Color else Company2Color,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = DividerDefaults.color)
        Spacer(modifier = Modifier.height(12.dp))

        // Quotation Details Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(
              text = if (isChinese) "报价单号 (Quotation No):" else "Quotation No:",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = quotation.quotationNumber,
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
          }
          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = if (isChinese) "日期 (Date):" else "Date:",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = dateFormatted,
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Table Header (Product + Packing Size + Price | STRICTLY NO QUANTITY)
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(8.dp)
          ) {
            Text(text = "#", modifier = Modifier.width(28.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(text = if (isChinese) "产品名称 / 条码" else "Product / Barcode", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(text = if (isChinese) "包装规格" else "Packing Size", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(text = if (isChinese) "报价 (Price)" else "Price", modifier = Modifier.width(90.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }

        // Product Items Rows
        quotation.items.forEachIndexed { idx, item ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 10.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(text = "${idx + 1}", modifier = Modifier.width(28.dp), fontSize = 13.sp)
            Column(modifier = Modifier.weight(1f)) {
              Text(text = item.productName, fontSize = 13.sp, fontWeight = FontWeight.Medium)
              Text(text = item.barcode, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(text = item.packingSize, modifier = Modifier.weight(1f), fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
            Text(text = item.manualPrice, modifier = Modifier.width(90.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold)
          }
          HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.2f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bank Details
        Text(
          text = if (isChinese) "银行账号 (Bank Details):" else "Bank Details:",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = bankDetails, style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(8.dp))

        // Terms
        Text(
          text = if (isChinese) "条款 (Terms & Conditions):" else "Terms & Conditions:",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = quotation.remarks, style = MaterialTheme.typography.bodySmall)
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(
      text = if (isChinese) "导出与分享方式" else "Export & Share Options",
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(10.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
      Button(
        onClick = { Toast.makeText(context, if (isChinese) "已生成 PDF 报价单并下载" else "PDF Downloaded", Toast.LENGTH_SHORT).show() },
        modifier = Modifier.weight(1f).height(48.dp),
        shape = RoundedCornerShape(10.dp)
      ) {
        Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF", modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("PDF")
      }
      OutlinedButton(
        onClick = { Toast.makeText(context, if (isChinese) "已导出 Excel 报价单" else "Excel Exported", Toast.LENGTH_SHORT).show() },
        modifier = Modifier.weight(1f).height(48.dp),
        shape = RoundedCornerShape(10.dp)
      ) {
        Icon(Icons.Default.TableChart, contentDescription = "Excel", modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Excel")
      }
      OutlinedButton(
        onClick = { Toast.makeText(context, if (isChinese) "唤起系统打印服务" else "Print dialog", Toast.LENGTH_SHORT).show() },
        modifier = Modifier.weight(1f).height(48.dp),
        shape = RoundedCornerShape(10.dp)
      ) {
        Icon(Icons.Default.Print, contentDescription = "Print", modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(if (isChinese) "打印" else "Print")
      }
    }
  }
}

@Composable
fun QuotationHistoryScreen(
  isChinese: Boolean,
  quotations: List<Quotation>,
  onViewQuotation: (String) -> Unit,
  onCreateNewQuotation: () -> Unit
) {
  var selectedTab by remember { mutableStateOf(0) }
  val tabs = listOf(
    if (isChinese) "全部公司" else "All Companies",
    "Company 1",
    "Company 2"
  )

  val filteredHistory = quotations.filter {
    when (selectedTab) {
      1 -> it.companyId == "company_1"
      2 -> it.companyId == "company_2"
      else -> true
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = MaterialTheme.colorScheme.surface
    ) {
      tabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedTab == index,
          onClick = { selectedTab = index },
          text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
        )
      }
    }

    if (filteredHistory.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(if (isChinese) "暂无报价单记录" else "No quotations found")
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(filteredHistory) { item ->
          val isC1 = item.companyId == "company_1"
          val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(item.createdDate))
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onViewQuotation(item.id) },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isC1) Company1Color.copy(alpha = 0.15f)
                    else Company2Color.copy(alpha = 0.15f)
                  ) {
                    Text(
                      text = if (isC1) "Company 1" else "Company 2",
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                      color = if (isC1) Company1Color else Company2Color
                    )
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = item.quotationNumber,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "${if (isChinese) "开单人: " else "By: "}${item.createdByName}  |  ${if (isChinese) "日期: " else "Date: "}$dateStr",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "${if (isChinese) "包含商品: " else "Items: "}${item.items.size} ${if (isChinese) "个品项" else "items"}",
                  style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                  color = MaterialTheme.colorScheme.primary
                )
              }
              Icon(Icons.Default.ChevronRight, contentDescription = "View", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
      }
    }
  }
}
