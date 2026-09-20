package com.mohna.ops.data.model

import com.google.gson.annotations.SerializedName

/**
 * Parcel QR code scan payload format (MOHNA_PARCEL_LABEL)
 */
data class ParcelQrPayload(
    @SerializedName("type") val type: String = "MOHNA_PARCEL_LABEL",
    @SerializedName("orderId") val orderId: String,
    @SerializedName("parcelToken") val parcelToken: String? = null
)

/**
 * Customer Handover QR code scan payload format (MOHNA_DELIVERY_HANDOVER)
 */
data class CustomerQrPayload(
    @SerializedName("type") val type: String = "MOHNA_DELIVERY_HANDOVER",
    @SerializedName("orderId") val orderId: String,
    @SerializedName("deliveryToken") val deliveryToken: String
)

/**
 * Order item representation
 */
data class OrderItem(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Double,
    @SerializedName("qty") val qty: Int
)

/**
 * Master Order Data Class
 */
data class Order(
    @SerializedName("orderId") val orderId: String,
    @SerializedName("date") val date: String = "",
    @SerializedName("orderTimestampMs") val orderTimestampMs: Long = System.currentTimeMillis(),
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone") val phone: String,
    @SerializedName("address") val address: String,
    @SerializedName("mapsUrl") val mapsUrl: String = "",
    @SerializedName("item") val item: String,
    @SerializedName("items") val items: List<OrderItem> = emptyList(),
    @SerializedName("qty") val qty: Int = 1,
    @SerializedName("amount") val amount: String,
    @SerializedName("etaMinutes") val etaMinutes: Int = 20,
    @SerializedName("expiryTimestamp") val expiryTimestamp: Long = 0L,
    @SerializedName("deliveredTimestamp") val deliveredTimestamp: Long? = null,
    @SerializedName("status") val status: String = "PACKED & READY FOR PICKUP",
    @SerializedName("riderName") val riderName: String? = null,
    @SerializedName("riderPhone") val riderPhone: String? = null,
    @SerializedName("riderEmail") val riderEmail: String? = null,
    @SerializedName("parcelToken") val parcelToken: String? = null,
    @SerializedName("receiptPdfUrl") val receiptPdfUrl: String? = null,
    @SerializedName("handoverPin") val handoverPin: String = "1234"
)

/**
 * Rider Partner Profile
 */
data class Rider(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("status") val status: String = "Active", // "Active", "Pending Approval", "Blocked"
    @SerializedName("liveLat") val liveLat: Double? = null,
    @SerializedName("liveLng") val liveLng: Double? = null,
    @SerializedName("date") val date: String = "",
    @SerializedName("vehicle") val vehicle: String? = "Motorcycle"
)

/**
 * Customer / User Account
 */
data class User(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("walletBalance") val walletBalance: Double = 0.0,
    @SerializedName("regLat") val regLat: Double? = 25.6000,
    @SerializedName("regLng") val regLng: Double? = 85.1300,
    @SerializedName("loginLat") val loginLat: Double? = 25.6000,
    @SerializedName("loginLng") val loginLng: Double? = 85.1300,
    @SerializedName("status") val status: String = "Active", // "Active", "Blocked"
    @SerializedName("avatar") val avatar: String? = null,
    @SerializedName("date") val date: String = ""
)

/**
 * Product & Inventory Stock
 */
data class Product(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String,
    @SerializedName("scope") val scope: String = "both", // "both", "retail", "wholesale"
    @SerializedName("retailPrice") val retailPrice: Double,
    @SerializedName("wholesalePrice") val wholesalePrice: Double,
    @SerializedName("mrp") val mrp: Double,
    @SerializedName("stockQty") val stockQty: Int,
    @SerializedName("deliveryFee") val deliveryFee: Double = 0.0,
    @SerializedName("description") val description: String? = null,
    @SerializedName("specifications") val specifications: String? = null,
    @SerializedName("terms") val terms: String? = null,
    @SerializedName("img") val img: String? = null
)

/**
 * Promotional Coupon
 */
data class Coupon(
    @SerializedName("id") val id: String,
    @SerializedName("code") val code: String,
    @SerializedName("discountPercent") val discountPercent: Int,
    @SerializedName("validUntil") val validUntil: String,
    @SerializedName("zoneScope") val zoneScope: String = "all"
)

/**
 * Scoped Category
 */
data class Category(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("scope") val scope: String = "both" // "both", "retail", "wholesale"
)

/**
 * Delivery Zone definition
 */
data class DeliveryZone(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("deliveryMinutes") val deliveryMinutes: Int = 20,
    @SerializedName("lateCashback") val lateCashback: Int = 25,
    @SerializedName("points") val points: List<Pair<Double, Double>> = emptyList()
)

/**
 * Customer Review / Feedback
 */
data class Review(
    @SerializedName("id") val id: String,
    @SerializedName("productName") val productName: String,
    @SerializedName("customerName") val customerName: String,
    @SerializedName("rating") val rating: Int,
    @SerializedName("comment") val comment: String,
    @SerializedName("date") val date: String
)
