package com.example.data.repository

import com.example.data.local.dao.QuotationDao
import com.example.data.local.entity.QuotationEntity
import com.example.data.model.Quotation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class QuotationRepository(private val quotationDao: QuotationDao) {

  val allQuotations: Flow<List<Quotation>> = quotationDao.getAllQuotations().map { list ->
    list.map { it.toModel() }
  }

  fun getQuotationsByCompany(companyId: String): Flow<List<Quotation>> {
    return quotationDao.getQuotationsByCompany(companyId).map { list ->
      list.map { it.toModel() }
    }
  }

  suspend fun getQuotationById(id: String): Quotation? {
    return quotationDao.getQuotationById(id)?.toModel()
  }

  suspend fun getQuotationByNumber(number: String): Quotation? {
    return quotationDao.getQuotationByNumber(number)?.toModel()
  }

  suspend fun saveQuotation(quotation: Quotation) {
    quotationDao.insertQuotation(QuotationEntity.fromModel(quotation))
  }

  suspend fun deleteQuotation(id: String) {
    quotationDao.deleteQuotation(id)
  }
}
