package com.example.data.local.converters

import androidx.room.TypeConverter
import com.example.data.model.QuotationItem
import org.json.JSONArray
import org.json.JSONObject

class AppTypeConverters {

  @TypeConverter
  fun fromQuotationItemList(items: List<QuotationItem>?): String {
    if (items == null) return "[]"
    val jsonArray = JSONArray()
    for (item in items) {
      val obj = JSONObject()
      obj.put("productId", item.productId)
      obj.put("productName", item.productName)
      obj.put("barcode", item.barcode)
      obj.put("packingSize", item.packingSize)
      obj.put("imageUrl", item.imageUrl)
      obj.put("manualPrice", item.manualPrice)
      jsonArray.put(obj)
    }
    return jsonArray.toString()
  }

  @TypeConverter
  fun toQuotationItemList(data: String?): List<QuotationItem> {
    if (data.isNullOrEmpty()) return emptyList()
    val list = mutableListOf<QuotationItem>()
    try {
      val jsonArray = JSONArray(data)
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        list.add(
          QuotationItem(
            productId = obj.optString("productId"),
            productName = obj.optString("productName"),
            barcode = obj.optString("barcode"),
            packingSize = obj.optString("packingSize"),
            imageUrl = obj.optString("imageUrl"),
            manualPrice = obj.optString("manualPrice")
          )
        )
      }
    } catch (_: Exception) {
      // Return empty if parsing error
    }
    return list
  }
}
