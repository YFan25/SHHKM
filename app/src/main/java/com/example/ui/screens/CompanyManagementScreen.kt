package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Company
import com.example.ui.theme.Company1Color
import com.example.ui.theme.Company2Color

@Composable
fun CompanyManagementScreen(
  isChinese: Boolean,
  companies: List<Company>,
  onUpdateCompany: (Company) -> Unit
) {
  val context = LocalContext.current
  var selectedTab by remember { mutableIntStateOf(0) }
  val targetCompanyId = if (selectedTab == 0) "company_1" else "company_2"
  val company = companies.find { it.id == targetCompanyId }

  // Form Fields synced when active company changes
  var name by remember(company) { mutableStateOf(company?.name ?: "") }
  var regNo by remember(company) { mutableStateOf(company?.regNo ?: "") }
  var logoUrl by remember(company) { mutableStateOf(company?.logoUrl ?: "") }
  var address by remember(company) { mutableStateOf(company?.address ?: "") }
  var phone by remember(company) { mutableStateOf(company?.phone ?: "") }
  var email by remember(company) { mutableStateOf(company?.email ?: "") }
  var website by remember(company) { mutableStateOf(company?.website ?: "") }
  var quotationPrefix by remember(company) { mutableStateOf(company?.quotationPrefix ?: "") }
  var nextSequence by remember(company) { mutableStateOf(company?.nextSequence?.toString() ?: "1") }
  var bankDetails by remember(company) { mutableStateOf(company?.bankDetails ?: "") }
  var termsAndConditions by remember(company) { mutableStateOf(company?.termsAndConditions ?: "") }

  var showPreview by remember { mutableStateOf(true) }
  var saveSuccessMessage by remember { mutableStateOf<String?>(null) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val activeBrandColor = if (selectedTab == 0) Company1Color else Company2Color

  val formattedSeq = try {
    val num = nextSequence.toIntOrNull() ?: 1
    String.format("%04d", num)
  } catch (e: Exception) {
    "0001"
  }
  val sampleQuotationNo = "$quotationPrefix$formattedSeq"

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    // Header & Company Tabs (Company 1 vs Company 2)
    Surface(
      color = MaterialTheme.colorScheme.surface,
      shadowElevation = 3.dp
    ) {
      Column {
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = MaterialTheme.colorScheme.surface,
          indicator = { tabPositions ->
            Box(
              Modifier
                .tabIndicatorOffset(tabPositions[selectedTab])
                .height(3.dp)
                .background(activeBrandColor)
            )
          }
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = {
              selectedTab = 0
              saveSuccessMessage = null
              errorMessage = null
            },
            modifier = Modifier.testTag("tab_company_1"),
            text = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Company1Color)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Company 1 (SHHKM)",
                  fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                  color = if (selectedTab == 0) Company1Color else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          )

          Tab(
            selected = selectedTab == 1,
            onClick = {
              selectedTab = 1
              saveSuccessMessage = null
              errorMessage = null
            },
            modifier = Modifier.testTag("tab_company_2"),
            text = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Company2Color)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Company 2 (GLOBAL)",
                  fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                  color = if (selectedTab == 1) Company2Color else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          )
        }
      }
    }

    // Scrollable Configuration Form
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(16.dp)
    ) {
      // Notification Banners
      AnimatedVisibility(visible = saveSuccessMessage != null) {
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
              text = saveSuccessMessage ?: "",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              fontWeight = FontWeight.Medium
            )
          }
        }
      }

      if (errorMessage != null) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.errorContainer,
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
        ) {
          Text(
            text = errorMessage ?: "",
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall
          )
        }
      }

      // Live Quotation Letterhead Preview Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp)
          .border(1.dp, activeBrandColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Preview, contentDescription = null, tint = activeBrandColor, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (isChinese) "报价单抬头实时效果预览" else "Quotation Header Live Preview",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = activeBrandColor
              )
            }
            TextButton(onClick = { showPreview = !showPreview }) {
              Text(if (showPreview) (if (isChinese) "收起" else "Collapse") else (if (isChinese) "展开" else "Expand"))
            }
          }

          if (showPreview) {
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Mock Quotation Letterhead Layout
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.Top
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = name.ifBlank { if (selectedTab == 0) "SHHKM TRADING SDN BHD" else "GLOBAL CONSUMER GOODS SDN BHD" },
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
                if (regNo.isNotBlank()) {
                  Text(
                    text = "Reg No: $regNo",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                if (address.isNotBlank()) {
                  Text(text = address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                  text = "Tel: ${phone.ifBlank { "-" }}  •  Email: ${email.ifBlank { "-" }}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (website.isNotBlank()) {
                  Text(text = "Web: $website", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
              }

              // Quotation No Box
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = activeBrandColor.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, activeBrandColor.copy(alpha = 0.4f)),
                modifier = Modifier.padding(start = 12.dp)
              ) {
                Column(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                  horizontalAlignment = Alignment.End
                ) {
                  Text(
                    text = if (isChinese) "下一份报价单号" else "Next Quote #",
                    style = MaterialTheme.typography.labelSmall,
                    color = activeBrandColor,
                    fontWeight = FontWeight.SemiBold
                  )
                  Text(
                    text = sampleQuotationNo,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = activeBrandColor
                  )
                }
              }
            }
          }
        }
      }

      // SECTION 1: Company Profile & Registration
      FormSectionHeader(
        title = if (isChinese) "1. 公司核心注册资料" else "1. Company Profile & Registration",
        icon = Icons.Default.Business,
        brandColor = activeBrandColor
      )

      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          OutlinedTextField(
            value = name,
            onValueChange = { name = it; errorMessage = null },
            label = { Text(if (isChinese) "公司全称 (Company Legal Name) *" else "Company Name *") },
            placeholder = { Text("e.g. SHHKM TRADING SDN BHD") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("company_name_input")
          )

          OutlinedTextField(
            value = regNo,
            onValueChange = { regNo = it },
            label = { Text(if (isChinese) "商业注册号 (Registration No / SSM)" else "Registration Number") },
            placeholder = { Text("e.g. 202001012345 (1380123-X)") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("company_reg_no_input")
          )

          OutlinedTextField(
            value = logoUrl,
            onValueChange = { logoUrl = it },
            label = { Text(if (isChinese) "公司 Logo 图片链接 (Logo URL)" else "Company Logo URL") },
            placeholder = { Text("https://example.com/logo.png") },
            leadingIcon = { Icon(Icons.Default.Image, contentDescription = null, tint = activeBrandColor) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // SECTION 2: Contact Information
      FormSectionHeader(
        title = if (isChinese) "2. 联络与官方通讯" else "2. Contact & Communication",
        icon = Icons.Default.LocationOn,
        brandColor = activeBrandColor
      )

      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text(if (isChinese) "办公/发票地址 (Full Address)" else "Address") },
            placeholder = { Text("No. 12, Jalan Industri...") },
            minLines = 2,
            maxLines = 3,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("company_address_input")
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedTextField(
              value = phone,
              onValueChange = { phone = it },
              label = { Text(if (isChinese) "电话 / WhatsApp" else "Phone / WhatsApp") },
              placeholder = { Text("+60 3-8765 4321") },
              leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = activeBrandColor) },
              singleLine = true,
              modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
              value = email,
              onValueChange = { email = it },
              label = { Text(if (isChinese) "业务邮箱" else "Email") },
              placeholder = { Text("sales@domain.com") },
              leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = activeBrandColor) },
              singleLine = true,
              modifier = Modifier.weight(1f)
            )
          }

          OutlinedTextField(
            value = website,
            onValueChange = { website = it },
            label = { Text(if (isChinese) "公司官网" else "Official Website") },
            placeholder = { Text("www.shhkm.com") },
            leadingIcon = { Icon(Icons.Default.Language, contentDescription = null, tint = activeBrandColor) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // SECTION 3: Quotation Sequencing Rules
      FormSectionHeader(
        title = if (isChinese) "3. 独立报价单编号生成规则" else "3. Quotation Sequence Rules",
        icon = Icons.Default.Numbers,
        brandColor = activeBrandColor
      )

      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(
            text = if (isChinese) {
              "Company 1 与 Company 2 的报价单前缀与流水号完全独立，开单时系统将根据此规则自动自增。"
            } else {
              "Company 1 and Company 2 maintain independent sequence counters for quotation numbering."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedTextField(
              value = quotationPrefix,
              onValueChange = { quotationPrefix = it; errorMessage = null },
              label = { Text(if (isChinese) "单号前缀 (Prefix) *" else "Quotation Prefix *") },
              placeholder = { Text("e.g. C1-Q-2026-") },
              singleLine = true,
              modifier = Modifier
                .weight(1.3f)
                .testTag("quotation_prefix_input")
            )

            OutlinedTextField(
              value = nextSequence,
              onValueChange = { nextSequence = it.filter { ch -> ch.isDigit() } },
              label = { Text(if (isChinese) "当前序号 (Seq)" else "Next Sequence") },
              placeholder = { Text("1") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              modifier = Modifier
                .weight(0.7f)
                .testTag("quotation_seq_input")
            )
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = activeBrandColor.copy(alpha = 0.08f)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (isChinese) "开单自动生成格式: " else "Format: ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = sampleQuotationNo,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = activeBrandColor
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // SECTION 4: Bank Details for Payment
      FormSectionHeader(
        title = if (isChinese) "4. 银行收款账户 (Bank Details)" else "4. Bank Payment Information",
        icon = Icons.Default.AccountBalance,
        brandColor = activeBrandColor
      )

      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = if (isChinese) "将打印在 PDF 报价单与分享文本底部，供客户对公转账汇款：" else "Printed on quotations for customer remittances:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = bankDetails,
            onValueChange = { bankDetails = it },
            label = { Text(if (isChinese) "银行户名、开户行与账号" else "Bank Name, Account & Branch") },
            placeholder = { Text("Maybank: 5140-1234-5678 (SHHKM TRADING SDN BHD)") },
            minLines = 3,
            maxLines = 5,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("bank_details_input")
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // SECTION 5: Terms & Conditions
      FormSectionHeader(
        title = if (isChinese) "5. 报价单标准条款与细则 (Terms & Conditions)" else "5. Quotation Terms & Conditions",
        icon = Icons.Default.Description,
        brandColor = activeBrandColor
      )

      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = if (isChinese) "各公司专属的法务与贸易条款（如报价有效期、结款周期、退换声明）：" else "Company specific trade terms and conditions:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = termsAndConditions,
            onValueChange = { termsAndConditions = it },
            label = { Text(if (isChinese) "条款与细则文本" else "Terms and Conditions") },
            placeholder = { Text("1. Validity: 14 days.\n2. Payment: 30 days.\n3. Goods sold are non-refundable.") },
            minLines = 4,
            maxLines = 8,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("terms_conditions_input")
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Save Button
      Button(
        onClick = {
          if (name.isBlank()) {
            errorMessage = if (isChinese) "公司名称不能为空" else "Company name cannot be empty"
            return@Button
          }
          if (quotationPrefix.isBlank()) {
            errorMessage = if (isChinese) "报价单前缀不能为空" else "Quotation prefix cannot be empty"
            return@Button
          }

          val seqInt = nextSequence.toIntOrNull() ?: 1
          val updatedCompany = Company(
            id = targetCompanyId,
            name = name.trim(),
            regNo = regNo.trim(),
            logoUrl = logoUrl.trim(),
            address = address.trim(),
            phone = phone.trim(),
            email = email.trim(),
            website = website.trim(),
            quotationPrefix = quotationPrefix.trim(),
            nextSequence = seqInt,
            bankDetails = bankDetails.trim(),
            termsAndConditions = termsAndConditions.trim()
          )

          onUpdateCompany(updatedCompany)
          saveSuccessMessage = if (isChinese) {
            "已成功保存 [${if (selectedTab == 0) "Company 1" else "Company 2"}] 的最新资料与配置！"
          } else {
            "Saved settings for ${if (selectedTab == 0) "Company 1" else "Company 2"} successfully!"
          }
          errorMessage = null
          Toast.makeText(context, saveSuccessMessage, Toast.LENGTH_SHORT).show()
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("save_company_button"),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = activeBrandColor,
          contentColor = Color.White
        )
      ) {
        Icon(Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (isChinese) "保存当前公司资料与报价单规则" else "Save Company & Quotation Rules",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun FormSectionHeader(
  title: String,
  icon: ImageVector,
  brandColor: Color
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.padding(bottom = 8.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = brandColor,
      modifier = Modifier.size(18.dp)
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = title,
      style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}
