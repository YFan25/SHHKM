package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Quotation
import com.example.data.model.QuotationItem

@Entity(tableName = "quotations")
data class QuotationEntity(
  @PrimaryKey val id: String,
  val quotationNumber: String,
  val companyId: String,
  val companyName: String,
  val createdDate: Long,
  val createdByUserId: String,
  val createdByName: String,
  val items: List<QuotationItem>,
  val remarks: String,
  val status: String
) {
  fun toModel(): Quotation = Quotation(
    id = id,
    quotationNumber = quotationNumber,
    companyId = companyId,
    companyName = companyName,
    createdDate = createdDate,
    createdByUserId = createdByUserId,
    createdByName = createdByName,
    items = items,
    remarks = remarks,
    status = status
  )

  companion object {
    fun fromModel(model: Quotation): QuotationEntity = QuotationEntity(
      id = model.id,
      quotationNumber = model.quotationNumber,
      companyId = model.companyId,
      companyName = model.companyName,
      createdDate = model.createdDate,
      createdByUserId = model.createdByUserId,
      createdByName = model.createdByName,
      items = model.items,
      remarks = model.remarks,
      status = model.status
    )
  }
}
