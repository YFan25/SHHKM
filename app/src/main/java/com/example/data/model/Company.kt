package com.example.data.model

data class Company(
  val id: String,                 // "company_1" | "company_2"
  val name: String,
  val regNo: String,
  val logoUrl: String = "",
  val address: String,
  val phone: String,
  val email: String,
  val website: String,
  val quotationPrefix: String,    // "C1-Q-2026-" | "C2-Q-2026-"
  val nextSequence: Int = 1,
  val bankDetails: String,
  val termsAndConditions: String
)
