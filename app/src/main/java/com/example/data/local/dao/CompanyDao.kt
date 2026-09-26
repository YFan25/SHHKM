package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CompanyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {
  @Query("SELECT * FROM companies ORDER BY id ASC")
  fun getAllCompanies(): Flow<List<CompanyEntity>>

  @Query("SELECT * FROM companies WHERE id = :companyId LIMIT 1")
  suspend fun getCompanyById(companyId: String): CompanyEntity?

  @Query("SELECT * FROM companies WHERE id = :companyId LIMIT 1")
  fun getCompanyFlow(companyId: String): Flow<CompanyEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCompany(company: CompanyEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCompanies(companies: List<CompanyEntity>)

  @Update
  suspend fun updateCompany(company: CompanyEntity)

  @Query("UPDATE companies SET nextSequence = nextSequence + 1 WHERE id = :companyId")
  suspend fun incrementSequence(companyId: String)
}
