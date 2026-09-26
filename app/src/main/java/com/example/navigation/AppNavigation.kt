package com.example.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.SHHKMApplication
import com.example.data.model.Product
import com.example.data.model.Quotation
import com.example.ui.components.AppScaffold
import com.example.ui.screens.BatchImageUploadScreen
import com.example.ui.screens.CatalogueScreen
import com.example.ui.screens.CompanyManagementScreen
import com.example.ui.screens.ExcelImportScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.ProductManagementScreen
import com.example.ui.screens.QuotationCreateScreen
import com.example.ui.screens.QuotationHistoryScreen
import com.example.ui.screens.QuotationPreviewScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ShareListScreen
import com.example.ui.screens.UserManagementScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
  navController: NavHostController = rememberNavController()
) {
  val context = LocalContext.current
  val application = context.applicationContext as SHHKMApplication
  val coroutineScope = rememberCoroutineScope()
  val sessionManager = application.sessionManager

  // Reactive Session State (Phase 3)
  val sessionUser by sessionManager.currentUser.collectAsStateWithLifecycle()
  val isLoggedIn by sessionManager.isLoggedIn.collectAsStateWithLifecycle()

  // Reactive Room Database Flows
  val products by application.productRepository.activeProducts.collectAsStateWithLifecycle(initialValue = emptyList())
  val allMasterProducts by application.productRepository.allProducts.collectAsStateWithLifecycle(initialValue = emptyList())
  val categories by application.categoryRepository.activeCategories.collectAsStateWithLifecycle(initialValue = emptyList())
  val quotations by application.quotationRepository.allQuotations.collectAsStateWithLifecycle(initialValue = emptyList())
  val companies by application.companyRepository.allCompanies.collectAsStateWithLifecycle(initialValue = emptyList())
  val users by application.userRepository.allUsers.collectAsStateWithLifecycle(initialValue = emptyList())

  var isChinese by remember { mutableStateOf(true) }

  // Current session parameters derived reactively
  val currentUser = sessionUser?.username ?: "admin"
  val currentUserId = sessionUser?.id ?: "usr_001"
  val userRole = sessionUser?.role ?: "admin"
  val isAdmin = userRole.equals("admin", ignoreCase = true)

  // Verify session integrity on app launch
  LaunchedEffect(Unit) {
    if (sessionManager.isLoggedIn.value) {
      sessionManager.restoreSessionWithVerification(application.userRepository)
    }
  }

  // Shared List of Products selected for Customer Sharing (STRICTLY NO PRICE)
  val shareList = remember { mutableStateListOf<Product>() }
  var quotationTargetProducts by remember { mutableStateOf<List<Product>?>(null) }

  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentDestination = navBackStackEntry?.destination?.route

  // Determine Dynamic Header Title
  val currentTitle = when {
    currentDestination == Screen.Home.route -> if (isChinese) "SHHKM 业务总览" else "Dashboard"
    currentDestination == Screen.Catalogue.route -> if (isChinese) "产品目录 (共用库)" else "Product Catalogue"
    currentDestination?.startsWith("product_detail") == true -> if (isChinese) "产品详情" else "Product Details"
    currentDestination == Screen.ShareList.route -> if (isChinese) "分享清单 (无价格)" else "Share List"
    currentDestination == Screen.QuotationCreate.route -> if (isChinese) "制作报价单" else "Create Quotation"
    currentDestination?.startsWith("quotation_preview") == true -> if (isChinese) "报价单预览" else "Quotation Preview"
    currentDestination == Screen.QuotationHistory.route -> if (isChinese) "历史报价单" else "Quotations"
    currentDestination == Screen.Settings.route -> if (isChinese) "系统设置" else "Settings"
    currentDestination == Screen.UserManagement.route -> if (isChinese) "用户管理" else "User Management"
    currentDestination == Screen.CompanySettings.route -> if (isChinese) "公司资料配置" else "Company Settings"
    currentDestination == Screen.ProductManagement.route -> if (isChinese) "产品主数据管理" else "Product Master"
    currentDestination == Screen.ExcelImport.route -> if (isChinese) "Excel 批量导入" else "Excel Import"
    currentDestination == Screen.BatchImageUpload.route -> if (isChinese) "条形码图片批传" else "Batch Images"
    else -> "SHHKM APP"
  }

  val topLevelRoutes = listOf(Screen.Login.route, Screen.Home.route)
  val canNavigateBack = currentDestination !in topLevelRoutes && navController.previousBackStackEntry != null

  if (!isLoggedIn || currentDestination == Screen.Login.route) {
    LoginScreen(
      isChinese = isChinese,
      onLoginAttempt = { username, passwordAttempt ->
        application.userRepository.authenticate(username, passwordAttempt)
      },
      onLoginSuccess = { user ->
        sessionManager.saveSession(user)
      },
      onToggleLanguage = { isChinese = !isChinese }
    )
  } else {
    AppScaffold(
      currentRoute = currentDestination,
      title = currentTitle,
      canNavigateBack = canNavigateBack,
      onNavigateBack = { navController.popBackStack() },
      onNavigateToRoute = { route ->
        // Admin route guard
        val adminRoutes = listOf(
          Screen.UserManagement.route,
          Screen.CompanySettings.route,
          Screen.ProductManagement.route,
          Screen.ExcelImport.route,
          Screen.BatchImageUpload.route
        )
        if (route in adminRoutes && !isAdmin) {
          // If staff tries to navigate to admin routes, direct to Settings
          navController.navigate(Screen.Settings.route) {
            launchSingleTop = true
          }
        } else {
          navController.navigate(route) {
            popUpTo(Screen.Home.route) { saveState = true }
            launchSingleTop = true
            restoreState = true
          }
        }
      },
      shareListCount = shareList.size,
      isChinese = isChinese,
      onToggleLanguage = { isChinese = !isChinese }
    ) {
      NavHost(
        navController = navController,
        startDestination = Screen.Home.route
      ) {
        // 1. Home
        composable(Screen.Home.route) {
          HomeScreen(
            isChinese = isChinese,
            currentUser = currentUser,
            userRole = userRole,
            shareListCount = shareList.size,
            productCount = products.size,
            categoryCount = categories.size,
            quotationCount = quotations.size,
            categories = categories.map { it.name },
            onNavigateToRoute = { route -> navController.navigate(route) },
            onSearchSubmitted = {
              navController.navigate(Screen.Catalogue.route)
            }
          )
        }

        // 2. Product Catalogue
        composable(Screen.Catalogue.route) {
          CatalogueScreen(
            isChinese = isChinese,
            products = products,
            categories = categories.map { it.name },
            shareListProductIds = shareList.map { it.id }.toSet(),
            onToggleShareList = { product ->
              if (shareList.any { it.id == product.id }) {
                shareList.removeAll { it.id == product.id }
              } else {
                shareList.add(product)
              }
            },
            onProductClick = { productId ->
              navController.navigate(Screen.ProductDetail.createRoute(productId))
            }
          )
        }

        // 3. Product Detail
        composable(
          route = Screen.ProductDetail.route,
          arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
          val productId = backStackEntry.arguments?.getString("productId") ?: ""
          val product = products.find { it.id == productId }
          val isInShare = shareList.any { it.id == productId }
          ProductDetailScreen(
            product = product,
            isChinese = isChinese,
            isInShareList = isInShare,
            onToggleShareList = {
              product?.let { p ->
                if (isInShare) {
                  shareList.removeAll { it.id == p.id }
                } else {
                  shareList.add(p)
                }
              }
            },
            onCreateQuotation = {
              product?.let { quotationTargetProducts = listOf(it) }
              navController.navigate(Screen.QuotationCreate.route)
            }
          )
        }

        // 4. Share List (Strictly No Price)
        composable(Screen.ShareList.route) {
          ShareListScreen(
            isChinese = isChinese,
            items = shareList,
            onRemoveItem = { item -> shareList.remove(item) },
            onClearAll = { shareList.clear() },
            onCreateQuotation = { selectedProducts ->
              quotationTargetProducts = selectedProducts
              navController.navigate(Screen.QuotationCreate.route)
            },
            onGoToCatalogue = {
              navController.navigate(Screen.Catalogue.route)
            }
          )
        }

        // 5. Quotation Create (Manual Price Key-in, Strictly No Quantity, No Price in Product DB)
        composable(Screen.QuotationCreate.route) {
          val productsForQuote = quotationTargetProducts ?: if (shareList.isNotEmpty()) shareList else products.take(2)
          QuotationCreateScreen(
            isChinese = isChinese,
            initialProducts = productsForQuote,
            companies = companies,
            currentUserName = currentUser,
            currentUserId = currentUserId,
            onSaveQuotation = { newQuote ->
              coroutineScope.launch {
                application.quotationRepository.saveQuotation(newQuote)
                navController.navigate(Screen.QuotationPreview.createRoute(newQuote.id))
              }
            }
          )
        }

        // 6. Quotation Preview
        composable(
          route = Screen.QuotationPreview.route,
          arguments = listOf(navArgument("quotationId") { type = NavType.StringType })
        ) { backStackEntry ->
          val quotationId = backStackEntry.arguments?.getString("quotationId") ?: ""
          val quotation = quotations.find { it.id == quotationId || it.quotationNumber == quotationId }
          val company = companies.find { it.id == quotation?.companyId }
          QuotationPreviewScreen(
            quotation = quotation,
            company = company,
            isChinese = isChinese,
            onNavigateBack = { navController.popBackStack() }
          )
        }

        // 7. Quotation History
        composable(Screen.QuotationHistory.route) {
          QuotationHistoryScreen(
            isChinese = isChinese,
            quotations = quotations,
            onViewQuotation = { quoteId ->
              navController.navigate(Screen.QuotationPreview.createRoute(quoteId))
            },
            onCreateNewQuotation = {
              navController.navigate(Screen.QuotationCreate.route)
            }
          )
        }

        // 8. Settings
        composable(Screen.Settings.route) {
          SettingsScreen(
            isChinese = isChinese,
            currentUser = currentUser,
            userRole = userRole,
            userId = currentUserId,
            onNavigateToRoute = { route -> navController.navigate(route) },
            onChangePassword = { uid, newPwd ->
              application.userRepository.updateUserPassword(uid, newPwd)
              true
            },
            onLogout = {
              sessionManager.clearSession()
              navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
              }
            }
          )
        }

        // 9. User Management (Admin Only)
        composable(Screen.UserManagement.route) {
          if (isAdmin) {
            UserManagementScreen(
              isChinese = isChinese,
              users = users,
              currentUserId = currentUserId,
              onToggleUserStatus = { userId, status ->
                coroutineScope.launch {
                  application.userRepository.updateUserStatus(userId, status)
                }
              },
              onAddUser = { newUser ->
                coroutineScope.launch {
                  application.userRepository.insertUser(newUser)
                }
              },
              onUpdateUser = { updatedUser ->
                coroutineScope.launch {
                  application.userRepository.updateUser(updatedUser)
                }
              },
              onResetPassword = { userId, newPassword ->
                coroutineScope.launch {
                  application.userRepository.updateUserPassword(userId, newPassword)
                }
              },
              onDeleteUser = { userId ->
                coroutineScope.launch {
                  application.userRepository.deleteUser(userId)
                }
              }
            )
          } else {
            // Guard
            SettingsScreen(
              isChinese = isChinese,
              currentUser = currentUser,
              userRole = userRole,
              userId = currentUserId,
              onNavigateToRoute = { route -> navController.navigate(route) },
              onLogout = { sessionManager.clearSession() }
            )
          }
        }

        // 10. Company Settings (Admin Only)
        composable(Screen.CompanySettings.route) {
          CompanyManagementScreen(
            isChinese = isChinese,
            companies = companies,
            onUpdateCompany = { updatedCompany ->
              coroutineScope.launch {
                application.companyRepository.updateCompany(updatedCompany)
              }
            }
          )
        }

        // 11. Product Management (Admin Only)
        composable(Screen.ProductManagement.route) {
          ProductManagementScreen(
            isChinese = isChinese,
            products = allMasterProducts,
            categories = categories,
            onAddProduct = { newProduct ->
              coroutineScope.launch {
                application.productRepository.insertProduct(newProduct)
              }
            },
            onUpdateProduct = { updatedProduct ->
              coroutineScope.launch {
                application.productRepository.updateProduct(updatedProduct)
              }
            },
            onDeleteProduct = { productId ->
              coroutineScope.launch {
                application.productRepository.deleteProduct(productId)
              }
            },
            onToggleStatus = { productId, nextStatus ->
              coroutineScope.launch {
                application.productRepository.updateProductStatus(productId, nextStatus)
              }
            }
          )
        }

        // 12. Excel Import (Admin Only)
        composable(Screen.ExcelImport.route) {
          ExcelImportScreen(
            isChinese = isChinese,
            existingProducts = allMasterProducts,
            onBatchImport = { productsToInsertOrUpdate ->
              coroutineScope.launch {
                application.productRepository.insertProducts(productsToInsertOrUpdate)
              }
            }
          )
        }

        // 13. Batch Image Upload (Admin Only)
        composable(Screen.BatchImageUpload.route) {
          BatchImageUploadScreen(
            isChinese = isChinese,
            products = allMasterProducts,
            onApplyMatches = { matchMap ->
              coroutineScope.launch {
                matchMap.forEach { (productId, imageUrl) ->
                  application.productRepository.updateProductImage(productId, imageUrl)
                }
              }
            }
          )
        }
      }
    }
  }
}
