package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.QuotationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuotationDao {
  @Query("SELECT * FROM quotations ORDER BY createdDate DESC")
  fun getAllQuotations(): Flow<List<QuotationEntity>>

  @Query("SELECT * FROM quotations WHERE companyId = :companyId ORDER BY createdDate DESC")
  fun getQuotationsByCompany(companyId: String): Flow<List<QuotationEntity>>

  @Query("SELECT * FROM quotations WHERE id = :id LIMIT 1")
  suspend fun getQuotationById(id: String): QuotationEntity?

  @Query("SELECT * FROM quotations WHERE quotationNumber = :quotationNumber LIMIT 1")
  suspend fun getQuotationByNumber(quotationNumber: String): QuotationEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertQuotation(quotation: QuotationEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertQuotations(quotations: List<QuotationEntity>)

  @Update
  suspend fun updateQuotation(quotation: QuotationEntity)

  @Query("DELETE FROM quotations WHERE id = :id")
  suspend fun deleteQuotation(id: String)

  @Query("SELECT COUNT(*) FROM quotations")
  suspend fun getQuotationCount(): Int
}
