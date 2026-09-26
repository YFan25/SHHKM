package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.CategoryRepository
import com.example.data.repository.CompanyRepository
import com.example.data.repository.ProductRepository
import com.example.data.repository.QuotationRepository
import com.example.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SHHKMApplication : Application() {

  val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
  val sessionManager: com.example.data.session.SessionManager by lazy { com.example.data.session.SessionManager.getInstance(this) }
  val userRepository: UserRepository by lazy { UserRepository(database.userDao()) }
  val productRepository: ProductRepository by lazy { ProductRepository(database.productDao()) }
  val categoryRepository: CategoryRepository by lazy { CategoryRepository(database.categoryDao()) }
  val companyRepository: CompanyRepository by lazy { CompanyRepository(database.companyDao()) }
  val quotationRepository: QuotationRepository by lazy { QuotationRepository(database.quotationDao()) }

  override fun onCreate() {
    super.onCreate()
    // Trigger eager pre-population on first launch
    CoroutineScope(Dispatchers.IO).launch {
      if (database.userDao().getUserCount() == 0) {
        AppDatabase.seedInitialData(database)
      }
      // Ensure primary master user SHHKMY4 is always initialized and active
      val userDao = database.userDao()
      val existing = userDao.getUserByUsername("shhkmy4")
      val targetHash = com.example.util.PasswordSecurity.hashPassword("Korean980!")
      if (existing == null) {
        userDao.insertUser(
          com.example.data.local.entity.UserEntity(
            id = "usr_shhkmy4",
            name = "SHHKMY4",
            username = "shhkmy4",
            passwordHash = targetHash,
            role = "admin",
            status = "active",
            createdAt = System.currentTimeMillis(),
            lastLogin = System.currentTimeMillis()
          )
        )
      } else {
        userDao.updatePasswordHash(existing.id, targetHash)
        userDao.updateUserStatus(existing.id, "active")
      }
    }
  }
}
