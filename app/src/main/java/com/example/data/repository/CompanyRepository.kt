package com.example.data.repository

import com.example.data.local.dao.CompanyDao
import com.example.data.local.entity.CompanyEntity
import com.example.data.model.Company
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CompanyRepository(private val companyDao: CompanyDao) {

  val allCompanies: Flow<List<Company>> = companyDao.getAllCompanies().map { list ->
    list.map { it.toModel() }
  }

  suspend fun getCompanyById(companyId: String): Company? {
    return companyDao.getCompanyById(companyId)?.toModel()
  }

  fun getCompanyFlow(companyId: String): Flow<Company?> {
    return companyDao.getCompanyFlow(companyId).map { it?.toModel() }
  }

  suspend fun updateCompany(company: Company) {
    companyDao.updateCompany(CompanyEntity.fromModel(company))
  }

  /**
   * Generates next independent quotation number for Company 1 or Company 2,
   * e.g. "C1-Q-2026-0001" vs "C2-Q-2026-0001" and increments the sequence.
   */
  suspend fun getNextQuotationNumber(companyId: String): String {
    val company = companyDao.getCompanyById(companyId)?.toModel()
      ?: return if (companyId == "company_1") "C1-Q-2026-0001" else "C2-Q-2026-0001"
    val seqFormatted = String.format("%04d", company.nextSequence)
    val quoteNo = "${company.quotationPrefix}$seqFormatted"
    companyDao.incrementSequence(companyId)
    return quoteNo
  }
}
