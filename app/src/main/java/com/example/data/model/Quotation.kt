package com.example.data.model

data class QuotationItem(
  val productId: String,
  val productName: String,
  val barcode: String,
  val packingSize: String,
  val imageUrl: String = "",
  val manualPrice: String        // Price entered by employee for this quotation only
  // NOTE: STRICTLY NO QUANTITY / QTY FIELD!
)

data class Quotation(
  val id: String,
  val quotationNumber: String,   // E.g. "C1-Q-2026-0001" or "C2-Q-2026-0001"
  val companyId: String,         // "company_1" or "company_2"
  val companyName: String,
  val createdDate: Long = System.currentTimeMillis(),
  val createdByUserId: String,
  val createdByName: String,
  val items: List<QuotationItem>,
  val remarks: String = "",
  val status: String = "active"  // "active" | "archived"
)
