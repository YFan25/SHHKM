package com.example.navigation

sealed class Screen(val route: String, val titleEn: String, val titleZh: String) {
  object Login : Screen("login", "Sign In", "系统登录")
  object Home : Screen("home", "Home", "首页")
  object Catalogue : Screen("catalogue", "Products", "产品目录")
  object ProductDetail : Screen("product_detail/{productId}", "Product Details", "产品详情") {
    fun createRoute(productId: String) = "product_detail/$productId"
  }
  object ShareList : Screen("share_list", "Share List", "分享清单")
  object QuotationCreate : Screen("quotation_create", "New Quotation", "制作报价单")
  object QuotationPreview : Screen("quotation_preview/{quotationId}", "Preview Quotation", "报价单预览") {
    fun createRoute(quotationId: String) = "quotation_preview/$quotationId"
  }
  object QuotationHistory : Screen("quotation_history", "Quotations", "历史报价单")
  object Settings : Screen("settings", "Settings", "系统设置")
  object UserManagement : Screen("user_management", "User Management", "用户管理")
  object CompanySettings : Screen("company_settings", "Company Profiles", "公司资料设置")
  object ProductManagement : Screen("product_management", "Product Master", "产品主数据管理")
  object ExcelImport : Screen("excel_import", "Excel Import", "Excel 批量导入")
  object BatchImageUpload : Screen("batch_image_upload", "Batch Images", "条形码图片批传")
}
