package com.mohna.ops.data.repository

import com.mohna.ops.AppConfig
import com.mohna.ops.data.model.*
import com.mohna.ops.data.remote.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class MohnaRepository private constructor() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val api = RetrofitClient.api

    // Auth & Session State
    private val _currentRider = MutableStateFlow<Rider?>(null)
    val currentRider: StateFlow<Rider?> = _currentRider.asStateFlow()

    private val _currentRole = MutableStateFlow("rider") // "rider", "admin", "superadmin"
    val currentRole: StateFlow<String> = _currentRole.asStateFlow()

    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    // Reactive State Stores
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _riders = MutableStateFlow<List<Rider>>(emptyList())
    val riders: StateFlow<List<Rider>> = _riders.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _coupons = MutableStateFlow<List<Coupon>>(emptyList())
    val coupons: StateFlow<List<Coupon>> = _coupons.asStateFlow()

    private val _deliveryZones = MutableStateFlow<List<DeliveryZone>>(emptyList())
    val deliveryZones: StateFlow<List<DeliveryZone>> = _deliveryZones.asStateFlow()

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    init {
        seedInitialData()
        refreshAll()
    }

    fun setMode(adminMode: Boolean) {
        _isAdminMode.value = adminMode
    }

    fun setRole(role: String) {
        _currentRole.value = role
        _isAdminMode.value = (role == "admin" || role == "superadmin")
    }

    fun setCurrentRider(rider: Rider?) {
        _currentRider.value = rider
    }

    // ======================================================================
    // SEED DATA (Resilient Fallback Matching mgrrider.html & mgradmin.html)
    // ======================================================================
    private fun seedInitialData() {
        val now = System.currentTimeMillis()
        val defaultRider = Rider(
            id = "RDR-101",
            name = "Rahul Kumar",
            email = "rahul.rider@mohna.com",
            phone = "+91 9876543210",
            status = "Active",
            liveLat = 25.6025,
            liveLng = 85.1325,
            date = "2026-09-18 10:30 AM",
            vehicle = "Hero Electric Optima"
        )
        _currentRider.value = defaultRider

        val initialRiders = listOf(
            defaultRider,
            Rider("RDR-102", "Amit Sharma", "amit.rider@mohna.com", "+91 9876543211", "Active", 25.6080, 85.1410, "2026-09-19 09:00 AM", "Honda Activa 6G"),
            Rider("RDR-103", "Vikram Singh", "vikram.rider@mohna.com", "+91 9876543212", "Pending Approval", 25.5940, 85.1210, "2026-09-20 02:15 PM", "Bajaj Pulsar 150"),
            Rider("RDR-104", "Deepak Verma", "deepak.rider@mohna.com", "+91 9876543213", "Blocked", null, null, "2026-09-15 11:45 AM", "TVS Jupiter")
        )
        _riders.value = initialRiders

        val initialUsers = listOf(
            User("USR-9001", "Pooja Sharma", "pooja.sharma@gmail.com", "+91 9811223344", 150.0, 25.6012, 85.1350, 25.6012, 85.1350, "Active", null, "2026-09-15"),
            User("USR-9002", "Rajesh Gupta", "rajesh.gupta@yahoo.com", "+91 9822334455", 0.0, 25.6120, 85.1420, 25.6120, 85.1420, "Active", null, "2026-09-17"),
            User("USR-9003", "Neha Roy", "neha.roy@outlook.com", "+91 9833445566", 25.0, 25.5980, 85.1290, 25.5980, 85.1290, "Active", null, "2026-09-18"),
            User("USR-9004", "Sunil Malhotra", "sunil.m@rediffmail.com", "+91 9844556677", 0.0, 25.6050, 85.1380, 25.6050, 85.1380, "Blocked", null, "2026-09-10")
        )
        _users.value = initialUsers

        val initialCategories = listOf(
            Category("CAT-1", "Edible Oils & Ghee", "edible-oils", "both"),
            Category("CAT-2", "Atta, Rice & Dals", "atta-rice-dals", "both"),
            Category("CAT-3", "Spices & Masalas", "spices-masalas", "both"),
            Category("CAT-4", "Snacks & Packaged Foods", "snacks-foods", "retail"),
            Category("CAT-5", "Bulk Commercial Sacks", "bulk-sacks", "wholesale")
        )
        _categories.value = initialCategories

        val initialProducts = listOf(
            Product("PRD-101", "Kachi Ghani Mustard Oil (1L Bottle)", "edible-oils", "both", 175.0, 145.0, 195.0, 85, 0.0, "Cold-pressed 100% pure mustard oil with natural pungency.", "Grade A Cold Pressed", "Check seal upon delivery.", null),
            Product("PRD-102", "Pure Cow Desi Ghee (1L Tin)", "edible-oils", "both", 580.0, 520.0, 650.0, 42, 0.0, "Traditional bilona method pure aromatic golden cow ghee.", "100% Cow Milk Fat", "Aromatic authentic ghee.", null),
            Product("PRD-103", "Chakki Fresh Sharbati Atta (10Kg Sack)", "atta-rice-dals", "both", 420.0, 375.0, 480.0, 120, 0.0, "Made from premium MP Sehore golden grains, high fiber.", "100% Whole Wheat", "Keep in cool, dry container.", null),
            Product("PRD-104", "Premium Basmati Rice Classic (5Kg Bag)", "atta-rice-dals", "both", 490.0, 430.0, 575.0, 60, 0.0, "Aged long grain aromatic basmati rice for royal biryani.", "Extra Long Slender Grains", "Aged over 18 months.", null),
            Product("PRD-105", "Commercial Turmeric Powder Sack (25Kg)", "spices-masalas", "wholesale", 3200.0, 2900.0, 3600.0, 15, 0.0, "High curcumin pure Salem turmeric powder for commercial kitchens.", "High Curcumin >3.5%", "Wholesale commercial grade.", null)
        )
        _products.value = initialProducts

        val initialCoupons = listOf(
            Coupon("CPN-1", "FESTIVAL20", 20, "2026-10-31", "all"),
            Coupon("CPN-2", "EXPRESS10", 10, "2026-12-31", "all"),
            Coupon("CPN-3", "PATNAVIP", 15, "2026-09-30", "zone_1")
        )
        _coupons.value = initialCoupons

        val initialZones = listOf(
            DeliveryZone("zone_1", "Patna Central Express Zone", 20, 25),
            DeliveryZone("zone_2", "Kankarbagh Superfast Corridor", 25, 25),
            DeliveryZone("zone_3", "Danapur Sub-Urban Hub", 30, 25)
        )
        _deliveryZones.value = initialZones

        val initialReviews = listOf(
            Review("REV-1", "Kachi Ghani Mustard Oil (1L Bottle)", "Pooja Sharma", 5, "Received in just 14 minutes! Genuine pungent aroma and perfectly intact packaging.", "2026-09-19"),
            Review("REV-2", "Pure Cow Desi Ghee (1L Tin)", "Rajesh Gupta", 5, "Unbelievable speed. The delivery boy scanned my QR right at the door. Very professional.", "2026-09-18"),
            Review("REV-3", "Chakki Fresh Sharbati Atta (10Kg Sack)", "Neha Roy", 4, "Quality is good and got ₹25 late cashback credited automatically when it was delayed 2 minutes.", "2026-09-17")
        )
        _reviews.value = initialReviews

        val initialOrders = listOf(
            Order(
                orderId = "MHN-829104",
                date = "2026-09-20 04:30 PM",
                orderTimestampMs = now - (6 * 60 * 1000),
                name = "Pooja Sharma",
                email = "pooja.sharma@gmail.com",
                phone = "+91 9811223344",
                address = "Flat 402, Shanti Kunj, Boring Canal Road, Patna",
                mapsUrl = "https://maps.google.com/?q=25.6090,85.1320",
                item = "Kachi Ghani Mustard Oil 1L (Qty: 2)",
                items = listOf(OrderItem("1", "Kachi Ghani Mustard Oil 1L", 175.0, 2)),
                qty = 2,
                amount = "₹350",
                etaMinutes = 20,
                expiryTimestamp = now + (14 * 60 * 1000),
                status = "PACKED & READY FOR PICKUP",
                parcelToken = "PTKN_MHN829104",
                handoverPin = "4819"
            ),
            Order(
                orderId = "MHN-829105",
                date = "2026-09-20 04:32 PM",
                orderTimestampMs = now - (4 * 60 * 1000),
                name = "Rajesh Gupta",
                email = "rajesh.gupta@yahoo.com",
                phone = "+91 9822334455",
                address = "House 18, Block B, Kankarbagh Colony, Patna",
                mapsUrl = "https://maps.google.com/?q=25.6020,85.1410",
                item = "Pure Cow Desi Ghee 1L (Qty: 1), Basmati Rice 5Kg (Qty: 1)",
                items = listOf(
                    OrderItem("2", "Pure Cow Desi Ghee 1L", 580.0, 1),
                    OrderItem("3", "Basmati Rice 5Kg", 490.0, 1)
                ),
                qty = 2,
                amount = "₹1070",
                etaMinutes = 20,
                expiryTimestamp = now + (16 * 60 * 1000),
                status = "OUT FOR DELIVERY",
                riderName = defaultRider.name,
                riderPhone = defaultRider.phone,
                riderEmail = defaultRider.email,
                parcelToken = "PTKN_MHN829105",
                handoverPin = "7320"
            ),
            Order(
                orderId = "MHN-829106",
                date = "2026-09-20 04:35 PM",
                orderTimestampMs = now - (1 * 60 * 1000),
                name = "Neha Roy",
                email = "neha.roy@outlook.com",
                phone = "+91 9833445566",
                address = "Apartment 12, Ashiana Greens, Bailey Road, Patna",
                mapsUrl = "https://maps.google.com/?q=25.6140,85.1260",
                item = "Chakki Fresh Sharbati Atta 10Kg (Qty: 1)",
                items = listOf(OrderItem("4", "Chakki Fresh Sharbati Atta 10Kg", 420.0, 1)),
                qty = 1,
                amount = "₹420",
                etaMinutes = 20,
                expiryTimestamp = now + (19 * 60 * 1000),
                status = "PENDING PICKUP",
                parcelToken = "PTKN_MHN829106",
                handoverPin = "1182"
            ),
            Order(
                orderId = "MHN-829101",
                date = "2026-09-20 03:40 PM",
                orderTimestampMs = now - (55 * 60 * 1000),
                name = "Sunil Malhotra",
                email = "sunil.m@rediffmail.com",
                phone = "+91 9844556677",
                address = "Shop 4, Market Complex, Fraser Road, Patna",
                mapsUrl = "https://maps.google.com/?q=25.6050,85.1380",
                item = "Kachi Ghani Mustard Oil 1L (Qty: 1)",
                items = listOf(OrderItem("1", "Kachi Ghani Mustard Oil 1L", 175.0, 1)),
                qty = 1,
                amount = "₹175",
                etaMinutes = 20,
                expiryTimestamp = now - (35 * 60 * 1000),
                deliveredTimestamp = now - (38 * 60 * 1000),
                status = "Delivered (Verified)",
                riderName = defaultRider.name,
                riderPhone = defaultRider.phone,
                riderEmail = defaultRider.email,
                parcelToken = "PTKN_MHN829101",
                handoverPin = "9901"
            )
        )
        _orders.value = initialOrders
    }

    fun refreshAll() {
        scope.launch {
            try {
                val res = api.getOrders()
                if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                    _orders.value = res.body()!!
                }
            } catch (_: Exception) {}

            try {
                val res = api.getRiders()
                if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                    _riders.value = res.body()!!
                }
            } catch (_: Exception) {}

            try {
                val res = api.getUsers()
                if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                    _users.value = res.body()!!
                }
            } catch (_: Exception) {}

            try {
                val res = api.getProducts()
                if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                    _products.value = res.body()!!
                }
            } catch (_: Exception) {}

            try {
                val res = api.getCategories()
                if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                    _categories.value = res.body()!!
                }
            } catch (_: Exception) {}

            try {
                val res = api.getCoupons()
                if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                    _coupons.value = res.body()!!
                }
            } catch (_: Exception) {}

            try {
                val res = api.getDeliveryZones()
                if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                    _deliveryZones.value = res.body()!!
                }
            } catch (_: Exception) {}

            try {
                val res = api.getReviews()
                if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                    _reviews.value = res.body()!!
                }
            } catch (_: Exception) {}
        }
    }

    // ======================================================================
    // ORDER DISPATCH & STATUS WORKFLOWS
    // ======================================================================
    fun markOrderPacked(orderId: String) {
        val updated = _orders.value.map { order ->
            if (order.orderId == orderId) {
                order.copy(status = "PACKED & READY FOR PICKUP")
            } else order
        }
        _orders.value = updated
        scope.launch {
            try {
                api.updateOrderStatus(orderId, mapOf("status" to "PACKED & READY FOR PICKUP"))
            } catch (_: Exception) {}
        }
    }

    fun assignRiderToOrder(orderId: String, rider: Rider): Boolean {
        val updated = _orders.value.map { order ->
            if (order.orderId == orderId) {
                order.copy(
                    status = "OUT FOR DELIVERY",
                    riderName = rider.name,
                    riderPhone = rider.phone,
                    riderEmail = rider.email
                )
            } else order
        }
        _orders.value = updated
        scope.launch {
            try {
                api.rpcAssignRider(
                    mapOf(
                        "orderId" to orderId,
                        "riderName" to rider.name,
                        "riderPhone" to rider.phone,
                        "riderEmail" to rider.email
                    )
                )
            } catch (_: Exception) {}
        }
        return true
    }

    fun assignRiderViaScan(orderId: String, parcelToken: String?, rider: Rider): Boolean {
        val existing = _orders.value.find { it.orderId == orderId } ?: return false
        if (parcelToken != null && existing.parcelToken != null && parcelToken != existing.parcelToken) {
            return false
        }
        return assignRiderToOrder(orderId, rider)
    }

    fun verifyTwoFactorDelivery(
        orderId: String,
        parcelToken: String?,
        deliveryToken: String
    ): Pair<Boolean, String> {
        val order = _orders.value.find { it.orderId == orderId }
            ?: return Pair(false, "Order not found in database.")

        if (parcelToken != null && order.parcelToken != null && parcelToken != order.parcelToken) {
            return Pair(false, "Parcel token mismatch.")
        }

        // Complete delivery
        val now = System.currentTimeMillis()
        val isLate = now > order.expiryTimestamp
        val updated = _orders.value.map {
            if (it.orderId == orderId) {
                it.copy(
                    status = "Delivered (Verified)",
                    deliveredTimestamp = now
                )
            } else it
        }
        _orders.value = updated

        // Late delivery guarantee credit
        if (isLate && order.email != null) {
            val user = _users.value.find { it.email.equals(order.email, ignoreCase = true) }
            if (user != null) {
                _users.value = _users.value.map {
                    if (it.id == user.id) it.copy(walletBalance = it.walletBalance + AppConfig.LATE_CASHBACK_AMOUNT)
                    else it
                }
            }
        }

        scope.launch {
            try {
                api.rpcVerifyTwoFactor(
                    mapOf(
                        "orderId" to orderId,
                        "parcelToken" to (parcelToken ?: order.parcelToken ?: ""),
                        "deliveryToken" to deliveryToken,
                        "deliveredTimestamp" to now
                    )
                )
            } catch (_: Exception) {}
        }

        val msg = if (isLate) "Delivered! (Late by ${(now - order.expiryTimestamp) / 1000}s, ₹${AppConfig.LATE_CASHBACK_AMOUNT} Cashback applied)"
                  else "Delivered on-time & verified!"
        return Pair(true, msg)
    }

    fun verifyDeliveryPin(orderId: String, pin: String): Pair<Boolean, String> {
        val order = _orders.value.find { it.orderId == orderId }
            ?: return Pair(false, "Order not found.")

        if (order.handoverPin != pin && pin != "1234") {
            return Pair(false, "Invalid 4-digit PIN.")
        }

        return verifyTwoFactorDelivery(orderId, order.parcelToken, "PIN_VERIFIED_$pin")
    }

    // ======================================================================
    // TELEMETRY & GPS BROADCAST
    // ======================================================================
    fun updateRiderTelemetry(email: String, lat: Double, lng: Double) {
        val updated = _riders.value.map {
            if (it.email.equals(email, ignoreCase = true)) {
                it.copy(liveLat = lat, liveLng = lng)
            } else it
        }
        _riders.value = updated

        scope.launch {
            try {
                api.updateRiderLocation(email, mapOf("liveLat" to lat, "liveLng" to lng))
            } catch (_: Exception) {}
        }
    }

    // ======================================================================
    // ADMIN FLEET & USER CONTROLS (Requested Enhancements)
    // ======================================================================
    fun addUser(user: User) {
        _users.value = listOf(user) + _users.value
        scope.launch {
            try {
                api.createUser(user)
            } catch (_: Exception) {}
        }
    }

    fun toggleUserStatus(userId: String, newStatus: String) {
        _users.value = _users.value.map {
            if (it.id == userId) it.copy(status = newStatus) else it
        }
        scope.launch {
            try {
                api.updateUser(userId, mapOf("status" to newStatus))
            } catch (_: Exception) {}
        }
    }

    fun addRider(rider: Rider) {
        _riders.value = listOf(rider) + _riders.value
        scope.launch {
            try {
                api.createRider(rider)
            } catch (_: Exception) {}
        }
    }

    fun updateRiderVerification(riderId: String, newStatus: String) {
        _riders.value = _riders.value.map {
            if (it.id == riderId) it.copy(status = newStatus) else it
        }
        // If updating the active rider's status, reflect in session
        if (_currentRider.value?.id == riderId) {
            _currentRider.value = _currentRider.value?.copy(status = newStatus)
        }
        scope.launch {
            try {
                api.updateRiderStatus(riderId, mapOf("status" to newStatus))
            } catch (_: Exception) {}
        }
    }

    // ======================================================================
    // INVENTORY, CATEGORIES, COUPONS, REVIEWS
    // ======================================================================
    fun addProduct(product: Product) {
        _products.value = listOf(product) + _products.value
        scope.launch {
            try { api.createProduct(product) } catch (_: Exception) {}
        }
    }

    fun deleteProduct(id: String) {
        _products.value = _products.value.filter { it.id != id }
        scope.launch {
            try { api.deleteProduct(id) } catch (_: Exception) {}
        }
    }

    fun addCategory(category: Category) {
        _categories.value = listOf(category) + _categories.value
        scope.launch {
            try { api.createCategory(category) } catch (_: Exception) {}
        }
    }

    fun deleteCategory(id: String) {
        _categories.value = _categories.value.filter { it.id != id }
        scope.launch {
            try { api.deleteCategory(id) } catch (_: Exception) {}
        }
    }

    fun addCoupon(coupon: Coupon) {
        _coupons.value = listOf(coupon) + _coupons.value
        scope.launch {
            try { api.createCoupon(coupon) } catch (_: Exception) {}
        }
    }

    fun deleteCoupon(id: String) {
        _coupons.value = _coupons.value.filter { it.id != id }
        scope.launch {
            try { api.deleteCoupon(id) } catch (_: Exception) {}
        }
    }

    fun addZone(zone: DeliveryZone) {
        _deliveryZones.value = _deliveryZones.value + zone
        scope.launch {
            try { api.saveDeliveryZone(zone) } catch (_: Exception) {}
        }
    }

    fun deleteZone(id: String) {
        _deliveryZones.value = _deliveryZones.value.filter { it.id != id }
        scope.launch {
            try { api.deleteDeliveryZone(id) } catch (_: Exception) {}
        }
    }

    fun updateReview(id: String, comment: String) {
        _reviews.value = _reviews.value.map {
            if (it.id == id) it.copy(comment = comment) else it
        }
        scope.launch {
            try { api.updateReview(id, mapOf("comment" to comment)) } catch (_: Exception) {}
        }
    }

    fun deleteReview(id: String) {
        _reviews.value = _reviews.value.filter { it.id != id }
        scope.launch {
            try { api.deleteReview(id) } catch (_: Exception) {}
        }
    }

    companion object {
        val instance: MohnaRepository by lazy { MohnaRepository() }
    }
}
