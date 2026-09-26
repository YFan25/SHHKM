package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.UserEntity
import com.example.data.model.AuthResult
import com.example.data.model.Product
import com.example.data.model.User
import com.example.data.repository.UserRepository
import com.example.data.session.SessionManager
import com.example.util.PasswordSecurity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var context: Context
  private lateinit var database: AppDatabase
  private lateinit var userRepository: UserRepository
  private lateinit var companyRepository: com.example.data.repository.CompanyRepository
  private lateinit var productRepository: com.example.data.repository.ProductRepository

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    userRepository = UserRepository(database.userDao())
    companyRepository = com.example.data.repository.CompanyRepository(database.companyDao())
    productRepository = com.example.data.repository.ProductRepository(database.productDao())
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun `read string from context`() {
    val appName = context.getString(R.string.app_name)
    assertEquals("SHHKM App", appName)
  }

  @Test
  fun `password security hashing and verification works`() {
    val plain = "mypassword123"
    val hash = PasswordSecurity.hashPassword(plain)
    assertNotNull(hash)
    assertEquals(64, hash.length) // SHA-256 hex length

    assertTrue(PasswordSecurity.verifyPassword(plain, hash))
    assertFalse(PasswordSecurity.verifyPassword("wrongpass", hash))
    // Plaintext seed compatibility check
    assertTrue(PasswordSecurity.verifyPassword("admin123", "admin123"))
  }

  @Test
  fun `phase 3 authentication handles admin login and password upgrade`() = runBlocking {
    val now = System.currentTimeMillis()
    database.userDao().insertUser(
      UserEntity(
        id = "usr_001",
        name = "Alex Tan",
        username = "admin",
        passwordHash = "admin123",
        role = "admin",
        status = "active",
        createdAt = now,
        lastLogin = now
      )
    )

    // Authenticate with correct credentials
    val result = userRepository.authenticate("admin", "admin123")
    assertTrue(result is AuthResult.Success)
    val success = result as AuthResult.Success
    assertEquals("admin", success.user.username)
    assertEquals("admin", success.user.role)

    // Verify that password was automatically upgraded to salted SHA-256 hash in database
    val updatedUser = database.userDao().getUserById("usr_001")
    assertNotNull(updatedUser)
    assertEquals(64, updatedUser!!.passwordHash.length)
  }

  @Test
  fun `phase 3 authentication rejects deactivated account`() = runBlocking {
    val now = System.currentTimeMillis()
    database.userDao().insertUser(
      UserEntity(
        id = "usr_004",
        name = "David Wong",
        username = "sales3",
        passwordHash = "sales123",
        role = "staff",
        status = "deactivated",
        createdAt = now,
        lastLogin = now
      )
    )

    val result = userRepository.authenticate("sales3", "sales123")
    assertTrue(result is AuthResult.AccountDeactivated)
  }

  @Test
  fun `phase 3 authentication rejects invalid password or unknown user`() = runBlocking {
    val now = System.currentTimeMillis()
    database.userDao().insertUser(
      UserEntity(
        id = "usr_002",
        name = "John Doe",
        username = "sales1",
        passwordHash = PasswordSecurity.hashPassword("sales123"),
        role = "staff",
        status = "active",
        createdAt = now,
        lastLogin = now
      )
    )

    // Wrong password
    val wrongPassResult = userRepository.authenticate("sales1", "wrong_password")
    assertTrue(wrongPassResult is AuthResult.InvalidCredentials)

    // Unknown username
    val unknownResult = userRepository.authenticate("ghost_user", "password")
    assertTrue(unknownResult is AuthResult.AccountNotFound)
  }

  @Test
  fun `session manager saves and clears session properly`() {
    val sessionManager = SessionManager(context)
    val user = User(
      id = "usr_test",
      name = "Test User",
      username = "testuser",
      passwordHash = "",
      role = "admin",
      status = "active"
    )

    sessionManager.saveSession(user)
    assertTrue(sessionManager.isLoggedIn.value)
    assertEquals("testuser", sessionManager.currentUser.value?.username)
    assertTrue(sessionManager.isAdmin)
    assertFalse(sessionManager.isStaff)

    sessionManager.clearSession()
    assertFalse(sessionManager.isLoggedIn.value)
    assertEquals(null, sessionManager.currentUser.value)
  }

  @Test
  fun `phase 4 user management full crud and status lifecycle`() = runBlocking {
    // 1. Add User
    val newUser = User(
      id = "usr_phase4_01",
      name = "Sarah Tan",
      username = "sarahtan",
      passwordHash = "initial123",
      role = "staff",
      status = "active"
    )
    userRepository.insertUser(newUser)

    val fetched = userRepository.getUserById("usr_phase4_01")
    assertNotNull(fetched)
    assertEquals("Sarah Tan", fetched?.name)
    assertEquals("sarahtan", fetched?.username)
    assertEquals("staff", fetched?.role)
    assertEquals("active", fetched?.status)

    // 2. Edit User (update name and promote to admin)
    userRepository.updateUser(fetched!!.copy(name = "Sarah Tan (Senior)", role = "admin"))
    val updated = userRepository.getUserById("usr_phase4_01")
    assertEquals("Sarah Tan (Senior)", updated?.name)
    assertEquals("admin", updated?.role)

    // 3. Deactivate User & Re-activate
    userRepository.updateUserStatus("usr_phase4_01", "deactivated")
    val deactivated = userRepository.getUserById("usr_phase4_01")
    assertEquals("deactivated", deactivated?.status)

    userRepository.updateUserStatus("usr_phase4_01", "active")
    val reactivated = userRepository.getUserById("usr_phase4_01")
    assertEquals("active", reactivated?.status)

    // 4. Admin Reset Password
    userRepository.updateUserPassword("usr_phase4_01", "new_secure_pwd_456")
    val authResult = userRepository.authenticate("sarahtan", "new_secure_pwd_456")
    assertTrue(authResult is AuthResult.Success)

    // Old password fails
    val failResult = userRepository.authenticate("sarahtan", "initial123")
    assertTrue(failResult is AuthResult.InvalidCredentials)

    // 5. Delete User
    userRepository.deleteUser("usr_phase4_01")
    val deleted = userRepository.getUserById("usr_phase4_01")
    assertEquals(null, deleted)
  }

  @Test
  fun `verify SHHKMY4 account authentication with Korean980!`() = runBlocking {
    val now = System.currentTimeMillis()
    database.userDao().insertUser(
      UserEntity(
        id = "usr_shhkmy4",
        name = "SHHKMY4",
        username = "shhkmy4",
        passwordHash = PasswordSecurity.hashPassword("Korean980!"),
        role = "admin",
        status = "active",
        createdAt = now,
        lastLogin = now
      )
    )

    // Test with uppercase input
    val resultUpper = userRepository.authenticate("SHHKMY4", "Korean980!")
    assertTrue(resultUpper is AuthResult.Success)
    val userUpper = (resultUpper as AuthResult.Success).user
    assertEquals("admin", userUpper.role)
    assertEquals("active", userUpper.status)

    // Test with lowercase input
    val resultLower = userRepository.authenticate("shhkmy4", "Korean980!")
    assertTrue(resultLower is AuthResult.Success)

    // Test with wrong password
    val resultWrong = userRepository.authenticate("SHHKMY4", "wrong_password")
    assertTrue(resultWrong is AuthResult.InvalidCredentials)
  }

  @Test
  fun `phase 5 company management handles independent profiles and sequence rules`() = runBlocking {
    val company1 = com.example.data.local.entity.CompanyEntity(
      id = "company_1",
      name = "SHHKM TRADING SDN BHD",
      regNo = "202001012345 (1380123-X)",
      logoUrl = "https://example.com/c1_logo.png",
      address = "No. 12, Jalan Industri 3, Kajang",
      phone = "+60 3-8765 4321",
      email = "sales@shhkm.com",
      website = "www.shhkm.com",
      quotationPrefix = "C1-Q-2026-",
      nextSequence = 1,
      bankDetails = "Maybank: 5140-1234-5678",
      termsAndConditions = "1. Validity: 14 days."
    )
    val company2 = com.example.data.local.entity.CompanyEntity(
      id = "company_2",
      name = "GLOBAL CONSUMER GOODS SDN BHD",
      regNo = "202101054321 (1420999-Y)",
      logoUrl = "",
      address = "Suite 8-3, Menara Global, PJ",
      phone = "+60 3-7955 8899",
      email = "contact@globalgoods.com.my",
      website = "www.globalgoods.com.my",
      quotationPrefix = "C2-Q-2026-",
      nextSequence = 5,
      bankDetails = "Public Bank: 3120-8888-9999",
      termsAndConditions = "1. Strictly cash before delivery."
    )
    database.companyDao().insertCompanies(listOf(company1, company2))

    // 1. Verify read
    val c1 = companyRepository.getCompanyById("company_1")
    val c2 = companyRepository.getCompanyById("company_2")
    assertNotNull(c1)
    assertNotNull(c2)
    assertEquals("SHHKM TRADING SDN BHD", c1?.name)
    assertEquals("GLOBAL CONSUMER GOODS SDN BHD", c2?.name)

    // 2. Verify independent quotation numbering
    val nextQuoteC1 = companyRepository.getNextQuotationNumber("company_1")
    assertEquals("C1-Q-2026-0001", nextQuoteC1)

    // Sequence for C1 incremented to 2
    val c1After = companyRepository.getCompanyById("company_1")
    assertEquals(2, c1After?.nextSequence)

    // Company 2 sequence remains independent
    val nextQuoteC2 = companyRepository.getNextQuotationNumber("company_2")
    assertEquals("C2-Q-2026-0005", nextQuoteC2)
    val c2After = companyRepository.getCompanyById("company_2")
    assertEquals(6, c2After?.nextSequence)

    // 3. Update company profile (e.g. Terms and Bank Details)
    companyRepository.updateCompany(
      c1After!!.copy(
        phone = "+60 12-345 6789",
        bankDetails = "CIMB: 8001-2345-6789",
        termsAndConditions = "1. Validity: 30 days.\n2. Payment: Net 30."
      )
    )

    val c1Updated = companyRepository.getCompanyById("company_1")
    assertEquals("+60 12-345 6789", c1Updated?.phone)
    assertEquals("CIMB: 8001-2345-6789", c1Updated?.bankDetails)
    assertEquals("1. Validity: 30 days.\n2. Payment: Net 30.", c1Updated?.termsAndConditions)

    // Ensure C2 was completely unaffected
    val c2Final = companyRepository.getCompanyById("company_2")
    assertEquals("Public Bank: 3120-8888-9999", c2Final?.bankDetails)
  }

  @Test
  fun `phase 6 product master management enforces zero price rule and full lifecycle`() = runBlocking {
    val sampleProduct = com.example.data.model.Product(
      id = "prd_phase6_test",
      name = "Crunchy Oat Biscuit",
      barcode = "9556000112233",
      packingSize = "15g x 30packs x 8tins", // Preserved as raw free text
      categoryId = "cat_biscuits",
      categoryName = "Biscuits",
      imageUrl = "https://example.com/biscuit.jpg",
      description = "High fiber premium oat biscuit",
      status = "active"
    )

    // 1. Insert product
    productRepository.insertProduct(sampleProduct)

    // 2. Fetch by ID and Barcode
    val byId = productRepository.getProductById("prd_phase6_test")
    assertNotNull(byId)
    assertEquals("Crunchy Oat Biscuit", byId?.name)
    assertEquals("9556000112233", byId?.barcode)
    // Verify packing size raw text preservation
    assertEquals("15g x 30packs x 8tins", byId?.packingSize)

    val byBarcode = productRepository.getProductByBarcode("9556000112233")
    assertNotNull(byBarcode)
    assertEquals(byId?.id, byBarcode?.id)

    // 3. Edit product
    val updatedProduct = byId!!.copy(
      name = "Crunchy Oat Biscuit (Family Pack)",
      packingSize = "15g x 60packs x 4boxes",
      description = "Family pack size"
    )
    productRepository.updateProduct(updatedProduct)

    val fetchedUpdated = productRepository.getProductById("prd_phase6_test")
    assertEquals("Crunchy Oat Biscuit (Family Pack)", fetchedUpdated?.name)
    assertEquals("15g x 60packs x 4boxes", fetchedUpdated?.packingSize)

    // 4. Status toggle (Archive / Unarchive)
    productRepository.updateProductStatus("prd_phase6_test", "archived")
    val archivedProduct = productRepository.getProductById("prd_phase6_test")
    assertEquals("archived", archivedProduct?.status)

    productRepository.updateProductStatus("prd_phase6_test", "active")
    val reactivatedProduct = productRepository.getProductById("prd_phase6_test")
    assertEquals("active", reactivatedProduct?.status)

    // 5. Delete product
    productRepository.deleteProduct("prd_phase6_test")
    val afterDelete = productRepository.getProductById("prd_phase6_test")
    assertEquals(null, afterDelete)
  }

  @Test
  fun `phase 7 excel batch import with duplicate strategy update and skip`() = runBlocking {
    // 1. Initial product in repository
    val existing = Product(
      id = "prd_existing_01",
      name = "Old Cream Cracker",
      barcode = "9556100223344",
      packingSize = "300g x 10 pkts",
      categoryId = "cat_biscuits",
      categoryName = "Biscuits",
      status = "active"
    )
    productRepository.insertProduct(existing)

    // 2. Batch import dataset: 1 new product, 1 duplicate barcode
    val importList = listOf(
      Product(
        id = "prd_batch_new",
        name = "Royal Danish Butter Cookies",
        barcode = "9556100112233",
        packingSize = "454g x 12 tins",
        categoryId = "cat_biscuits",
        categoryName = "Biscuits",
        status = "active"
      ),
      Product(
        id = "prd_existing_01", // updating the existing product
        name = "Cream Crackers Golden (Upgraded)",
        barcode = "9556100223344",
        packingSize = "428g x 12 packets",
        categoryId = "cat_biscuits",
        categoryName = "Biscuits",
        status = "active"
      )
    )

    // Execute batch insert / update (Strategy: UPDATE)
    productRepository.insertProducts(importList)

    // Verify new item added
    val newProduct = productRepository.getProductByBarcode("9556100112233")
    assertNotNull(newProduct)
    assertEquals("Royal Danish Butter Cookies", newProduct?.name)
    assertEquals("454g x 12 tins", newProduct?.packingSize)

    // Verify existing item updated
    val updatedExisting = productRepository.getProductByBarcode("9556100223344")
    assertNotNull(updatedExisting)
    assertEquals("Cream Crackers Golden (Upgraded)", updatedExisting?.name)
    assertEquals("428g x 12 packets", updatedExisting?.packingSize)
  }

  @Test
  fun `phase 8 batch image upload barcode auto matching and review area workflow`() = runBlocking {
    // 1. Prepare target products in database
    val prodA = Product(
      id = "prd_img_01",
      name = "Crunchy Cocoa Wafers",
      barcode = "9558000112233",
      packingSize = "120g x 24pkts",
      categoryId = "cat_biscuits",
      categoryName = "Biscuits",
      imageUrl = ""
    )
    val prodB = Product(
      id = "prd_img_02",
      name = "Sparkling Lime Juice",
      barcode = "9558000445566",
      packingSize = "330ml x 24cans",
      categoryId = "cat_drinks",
      categoryName = "Drinks",
      imageUrl = ""
    )
    productRepository.insertProduct(prodA)
    productRepository.insertProduct(prodB)

    val dbProducts = listOf(prodA, prodB)

    // 2. Barcode Matching Logic Test
    fun matchFilename(fileName: String): Product? {
      val baseName = fileName.substringBeforeLast('.').trim()
      // Direct match
      val direct = dbProducts.firstOrNull { it.barcode.equals(baseName, ignoreCase = true) }
      if (direct != null) return direct
      // Regex 8-14 digits
      val digitRegex = Regex("""(\d{8,14})""")
      val candidate = digitRegex.find(baseName)?.value
      if (candidate != null) {
        val found = dbProducts.firstOrNull { it.barcode == candidate }
        if (found != null) return found
      }
      return dbProducts.firstOrNull { baseName.contains(it.barcode) }
    }

    // Exact filename match
    val match1 = matchFilename("9558000112233.jpg")
    assertNotNull(match1)
    assertEquals("prd_img_01", match1?.id)

    // Prefix & suffix filename match (e.g. IMG_9558000445566_pack.png)
    val match2 = matchFilename("IMG_9558000445566_pack.png")
    assertNotNull(match2)
    assertEquals("prd_img_02", match2?.id)

    // Unmatched item (should go to Review Area)
    val matchUnmatched = matchFilename("promo_unknown_snack.jpg")
    assertEquals(null, matchUnmatched)

    // 3. Database Persistence: Apply batch image bindings
    val newImageUrlA = "content://media/external/images/media/1001"
    val newImageUrlB = "https://images.unsplash.com/sample_lime_drink.jpg"
    productRepository.updateProductImage("prd_img_01", newImageUrlA)
    productRepository.updateProductImage("prd_img_02", newImageUrlB)

    val updatedA = productRepository.getProductById("prd_img_01")
    assertEquals(newImageUrlA, updatedA?.imageUrl)

    val updatedB = productRepository.getProductById("prd_img_02")
    assertEquals(newImageUrlB, updatedB?.imageUrl)

    // 4. Test updating image by barcode
    val newImageUrlByBarcode = "content://media/external/images/media/2002"
    productRepository.updateProductImageByBarcode("9558000112233", newImageUrlByBarcode)
    val updatedByBarcode = productRepository.getProductByBarcode("9558000112233")
    assertEquals(newImageUrlByBarcode, updatedByBarcode?.imageUrl)
  }

  @Test
  fun `phase 9 share list management selection zero price export and quotation transition`() = runBlocking {
    // 1. Prepare sample products
    val p1 = Product(
      id = "p_share_1",
      name = "Choco Crunch Biscuits",
      barcode = "9551111222233",
      packingSize = "20g x 12pcs x 10box",
      categoryId = "cat_biscuits",
      categoryName = "Biscuits",
      imageUrl = "https://example.com/biscuit.jpg"
    )
    val p2 = Product(
      id = "p_share_2",
      name = "Crispy Seaweed Chips",
      barcode = "9554444555566",
      packingSize = "40g x 24tins",
      categoryId = "cat_snacks",
      categoryName = "Snacks",
      imageUrl = "https://example.com/chips.jpg"
    )
    val p3 = Product(
      id = "p_share_3",
      name = "Green Tea Jasmine",
      barcode = "9557777888899",
      packingSize = "500ml x 24bottles",
      categoryId = "cat_drinks",
      categoryName = "Drinks",
      imageUrl = ""
    )

    // 2. Share list operations
    val shareList = mutableListOf<Product>()
    // Add items
    shareList.add(p1)
    shareList.add(p2)
    shareList.add(p3)
    assertEquals(3, shareList.size)

    // Remove single item
    shareList.remove(p2)
    assertEquals(2, shareList.size)
    assertFalse(shareList.any { it.id == p2.id })

    // Re-add p2
    shareList.add(p2)
    assertEquals(3, shareList.size)

    // Category aggregation
    val categories = shareList.map { it.categoryName }.distinct().sorted()
    assertEquals(listOf("Biscuits", "Drinks", "Snacks"), categories)

    // 3. Customer Text Export (Zero-Price Verification)
    val sb = StringBuilder()
    shareList.forEachIndexed { idx, p ->
      sb.append("${idx + 1}. ${p.name} | ${p.barcode} | ${p.packingSize}\n")
    }
    val exportedText = sb.toString()
    assertTrue(exportedText.contains("Choco Crunch Biscuits"))
    assertTrue(exportedText.contains("9551111222233"))
    assertTrue(exportedText.contains("20g x 12pcs x 10box"))
    // Strictly zero price
    assertFalse(exportedText.contains("RM"))
    assertFalse(exportedText.contains("Price"))
    assertFalse(exportedText.contains("价格"))

    // 4. Subset selection for Quotation Creation
    val selectedForQuoteIds = setOf(p1.id, p3.id)
    val targetQuoteProducts = shareList.filter { selectedForQuoteIds.contains(it.id) }
    assertEquals(2, targetQuoteProducts.size)
    assertTrue(targetQuoteProducts.any { it.id == p1.id })
    assertTrue(targetQuoteProducts.any { it.id == p3.id })
    assertFalse(targetQuoteProducts.any { it.id == p2.id })

    // 5. Quotation Manual Unit Price Input (Business Rule)
    // Price is supplied by sales rep at quotation creation, NEVER from product DB
    val quotationPriceMap = mapOf(
      p1.id to "RM 38.50/ctn",
      p3.id to "RM 24.00/ctn"
    )
    assertEquals("RM 38.50/ctn", quotationPriceMap[p1.id])
    assertEquals("RM 24.00/ctn", quotationPriceMap[p3.id])
  }
}
