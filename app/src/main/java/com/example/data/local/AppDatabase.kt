package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.converters.AppTypeConverters
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.CompanyDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.QuotationDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.CompanyEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.QuotationEntity
import com.example.data.local.entity.UserEntity
import com.example.data.model.QuotationItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [
    UserEntity::class,
    ProductEntity::class,
    CategoryEntity::class,
    CompanyEntity::class,
    QuotationEntity::class
  ],
  version = 1,
  exportSchema = false
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

  abstract fun userDao(): UserDao
  abstract fun productDao(): ProductDao
  abstract fun categoryDao(): CategoryDao
  abstract fun companyDao(): CompanyDao
  abstract fun quotationDao(): QuotationDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "shhkm_business.db"
        )
          .addCallback(DatabasePrepopulateCallback())
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }

    private class DatabasePrepopulateCallback : RoomDatabase.Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        // Coroutine to seed default initial master data
        CoroutineScope(Dispatchers.IO).launch {
          INSTANCE?.let { database ->
            seedInitialData(database)
          }
        }
      }
    }

    suspend fun seedInitialData(database: AppDatabase) {
      val now = System.currentTimeMillis()

      // 1. Seed Initial Users (Username / Password based)
      val initialUsers = listOf(
        UserEntity(
          id = "usr_shhkmy4",
          name = "SHHKMY4",
          username = "shhkmy4",
          passwordHash = com.example.util.PasswordSecurity.hashPassword("Korean980!"),
          role = "admin",
          status = "active",
          createdAt = now,
          lastLogin = now
        ),
        UserEntity(
          id = "usr_001",
          name = "Alex Tan",
          username = "admin",
          passwordHash = "admin123", // In Phase 3 hashed securely
          role = "admin",
          status = "active",
          createdAt = now,
          lastLogin = now
        ),
        UserEntity(
          id = "usr_002",
          name = "John Doe",
          username = "sales1",
          passwordHash = "sales123",
          role = "staff",
          status = "active",
          createdAt = now,
          lastLogin = now
        ),
        UserEntity(
          id = "usr_003",
          name = "Mary Lim",
          username = "sales2",
          passwordHash = "sales123",
          role = "staff",
          status = "active",
          createdAt = now,
          lastLogin = now
        ),
        UserEntity(
          id = "usr_004",
          name = "David Wong",
          username = "sales3",
          passwordHash = "sales123",
          role = "staff",
          status = "deactivated", // Preserves history even when deactivated
          createdAt = now,
          lastLogin = now
        )
      )
      database.userDao().insertUsers(initialUsers)

      // 2. Seed Default Categories
      val initialCategories = listOf(
        CategoryEntity(id = "cat_biscuits", name = "Biscuits", sortOrder = 1, status = "active", createdAt = now),
        CategoryEntity(id = "cat_snacks", name = "Snacks", sortOrder = 2, status = "active", createdAt = now),
        CategoryEntity(id = "cat_drinks", name = "Drinks", sortOrder = 3, status = "active", createdAt = now),
        CategoryEntity(id = "cat_candy", name = "Candy", sortOrder = 4, status = "active", createdAt = now),
        CategoryEntity(id = "cat_instant", name = "Instant Food", sortOrder = 5, status = "active", createdAt = now),
        CategoryEntity(id = "cat_others", name = "Others", sortOrder = 6, status = "active", createdAt = now)
      )
      database.categoryDao().insertCategories(initialCategories)

      // 3. Seed Company 1 & Company 2 (Shared Product DB, Independent Quotation & Sequence)
      val initialCompanies = listOf(
        CompanyEntity(
          id = "company_1",
          name = "SHHKM TRADING SDN BHD",
          regNo = "202001012345 (1380123-X)",
          logoUrl = "",
          address = "No. 12, Jalan Industri 3, Kawasan Perindustrian, 43000 Kajang, Selangor",
          phone = "+60 3-8765 4321",
          email = "sales@shhkm.com",
          website = "www.shhkm.com",
          quotationPrefix = "C1-Q-2026-",
          nextSequence = 2, // Starts at 0001, next is 0002
          bankDetails = "Maybank: 5140-1234-5678 (SHHKM TRADING SDN BHD)",
          termsAndConditions = "1. Validity: 14 days.\n2. Payment: 30 days.\n3. Goods sold are non-refundable."
        ),
        CompanyEntity(
          id = "company_2",
          name = "GLOBAL CONSUMER GOODS SDN BHD",
          regNo = "202101054321 (1420999-Y)",
          logoUrl = "",
          address = "Suite 8-3, Menara Global, Persiaran Barat, 46050 Petaling Jaya, Selangor",
          phone = "+60 3-7955 8899",
          email = "contact@globalgoods.com.my",
          website = "www.globalgoods.com.my",
          quotationPrefix = "C2-Q-2026-",
          nextSequence = 2,
          bankDetails = "Public Bank: 3120-8888-9999 (GLOBAL CONSUMER GOODS SDN BHD)",
          termsAndConditions = "1. Quotation valid for 14 days.\n2. Goods sold are non-returnable.\n3. Payment strictly before delivery."
        )
      )
      database.companyDao().insertCompanies(initialCompanies)

      // 4. Seed Products (CRITICAL: STRICTLY NO PRICE FIELD!)
      val initialProducts = listOf(
        ProductEntity(
          id = "prd_101",
          name = "Crispy Chocolate Biscuit",
          barcode = "9551234567890",
          packingSize = "10g x 20pcs x 12box",
          categoryId = "cat_biscuits",
          categoryName = "Biscuits",
          description = "Premium cocoa chocolate crunch biscuit for retail and bulk distribution.",
          createdAt = now,
          updatedAt = now
        ),
        ProductEntity(
          id = "prd_102",
          name = "Butter Caramel Cookies",
          barcode = "9551234567891",
          packingSize = "50g x 24pcs",
          categoryId = "cat_biscuits",
          categoryName = "Biscuits",
          description = "Rich golden butter cookies with salted caramel glaze.",
          createdAt = now,
          updatedAt = now
        ),
        ProductEntity(
          id = "prd_103",
          name = "Sea Salt Potato Crisps",
          barcode = "9551234567892",
          packingSize = "80g x 18tins",
          categoryId = "cat_snacks",
          categoryName = "Snacks",
          description = "Crispy potato chips made with organic farm potatoes.",
          createdAt = now,
          updatedAt = now
        ),
        ProductEntity(
          id = "prd_104",
          name = "Sparkling Lemon Green Tea",
          barcode = "9551234567893",
          packingSize = "320ml x 24cans",
          categoryId = "cat_drinks",
          categoryName = "Drinks",
          description = "Refreshing sparkling tea beverage with natural lemon extract.",
          createdAt = now,
          updatedAt = now
        ),
        ProductEntity(
          id = "prd_105",
          name = "Assorted Fruity Gummy Candy",
          barcode = "9551234567894",
          packingSize = "15g x 30packs x 8bags",
          categoryId = "cat_candy",
          categoryName = "Candy",
          description = "Assorted strawberry, grape, and mango chewy soft candies.",
          createdAt = now,
          updatedAt = now
        ),
        ProductEntity(
          id = "prd_106",
          name = "Instant Tom Yam Seafood Noodles",
          barcode = "9551234567895",
          packingSize = "85g x 5packs x 12bags",
          categoryId = "cat_instant",
          categoryName = "Instant Food",
          description = "Spicy and sour Thai authentic tom yam soup noodles.",
          createdAt = now,
          updatedAt = now
        )
      )
      database.productDao().insertProducts(initialProducts)

      // 5. Seed Initial Sample Quotations (Holds manual price, strictly no quantity!)
      val initialQuotations = listOf(
        QuotationEntity(
          id = "qtn_c1_0001",
          quotationNumber = "C1-Q-2026-0001",
          companyId = "company_1",
          companyName = "SHHKM TRADING SDN BHD",
          createdDate = now - 86400000L,
          createdByUserId = "usr_001",
          createdByName = "Alex Tan",
          items = listOf(
            QuotationItem(
              productId = "prd_101",
              productName = "Crispy Chocolate Biscuit",
              barcode = "9551234567890",
              packingSize = "10g x 20pcs x 12box",
              imageUrl = "",
              manualPrice = "RM 45.00/ctn"
            ),
            QuotationItem(
              productId = "prd_102",
              productName = "Butter Caramel Cookies",
              barcode = "9551234567891",
              packingSize = "50g x 24pcs",
              imageUrl = "",
              manualPrice = "RM 38.50/ctn"
            )
          ),
          remarks = "1. Quotation valid for 14 days.\n2. Delivery within 3 days upon confirmation.",
          status = "active"
        ),
        QuotationEntity(
          id = "qtn_c2_0001",
          quotationNumber = "C2-Q-2026-0001",
          companyId = "company_2",
          companyName = "GLOBAL CONSUMER GOODS SDN BHD",
          createdDate = now - 172800000L,
          createdByUserId = "usr_002",
          createdByName = "John Doe",
          items = listOf(
            QuotationItem(
              productId = "prd_103",
              productName = "Sea Salt Potato Crisps",
              barcode = "9551234567892",
              packingSize = "80g x 18tins",
              imageUrl = "",
              manualPrice = "RM 52.00/ctn"
            )
          ),
          remarks = "1. Cash on delivery.\n2. Price valid for current shipment.",
          status = "active"
        )
      )
      database.quotationDao().insertQuotations(initialQuotations)
    }
  }
}
