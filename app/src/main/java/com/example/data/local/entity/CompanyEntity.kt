package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Company

@Entity(tableName = "companies")
data class CompanyEntity(
  @PrimaryKey val id: String, // "company_1" or "company_2"
  val name: String,
  val regNo: String,
  val logoUrl: String = "",
  val address: String,
  val phone: String,
  val email: String,
  val website: String,
  val quotationPrefix: String,
  val nextSequence: Int,
  val bankDetails: String,
  val termsAndConditions: String
) {
  fun toModel(): Company = Company(
    id = id,
    name = name,
    regNo = regNo,
    logoUrl = logoUrl,
    address = address,
    phone = phone,
    email = email,
    website = website,
    quotationPrefix = quotationPrefix,
    nextSequence = nextSequence,
    bankDetails = bankDetails,
    termsAndConditions = termsAndConditions
  )

  companion object {
    fun fromModel(model: Company): CompanyEntity = CompanyEntity(
      id = model.id,
      name = model.name,
      regNo = model.regNo,
      logoUrl = model.logoUrl,
      address = model.address,
      phone = model.phone,
      email = model.email,
      website = model.website,
      quotationPrefix = model.quotationPrefix,
      nextSequence = model.nextSequence,
      bankDetails = model.bankDetails,
      termsAndConditions = model.termsAndConditions
    )
  }
}
