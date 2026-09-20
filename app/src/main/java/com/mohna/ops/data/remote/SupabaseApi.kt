package com.mohna.ops.data.remote

import com.mohna.ops.AppConfig
import com.mohna.ops.data.model.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface SupabaseApi {

    // Orders
    @GET("rest/v1/orders?select=*")
    suspend fun getOrders(): Response<List<Order>>

    @POST("rest/v1/orders")
    suspend fun createOrder(@Body order: Order): Response<List<Order>>

    @PATCH("rest/v1/orders")
    suspend fun updateOrderStatus(
        @Query("orderId") orderIdQuery: String,
        @Body updates: Map<String, Any>
    ): Response<List<Order>>

    // Riders
    @GET("rest/v1/riders?select=*")
    suspend fun getRiders(): Response<List<Rider>>

    @POST("rest/v1/riders")
    suspend fun createRider(@Body rider: Rider): Response<List<Rider>>

    @PATCH("rest/v1/riders")
    suspend fun updateRiderStatus(
        @Query("id") idQuery: String,
        @Body updates: Map<String, Any>
    ): Response<List<Rider>>

    @PATCH("rest/v1/riders")
    suspend fun updateRiderLocation(
        @Query("email") emailQuery: String,
        @Body locationUpdate: Map<String, Any>
    ): Response<List<Rider>>

    // Users
    @GET("rest/v1/users?select=*")
    suspend fun getUsers(): Response<List<User>>

    @POST("rest/v1/users")
    suspend fun createUser(@Body user: User): Response<List<User>>

    @PATCH("rest/v1/users")
    suspend fun updateUser(
        @Query("id") idQuery: String,
        @Body updates: Map<String, Any>
    ): Response<List<User>>

    // Products
    @GET("rest/v1/products?select=*")
    suspend fun getProducts(): Response<List<Product>>

    @POST("rest/v1/products")
    suspend fun createProduct(@Body product: Product): Response<List<Product>>

    @DELETE("rest/v1/products")
    suspend fun deleteProduct(@Query("id") idQuery: String): Response<Unit>

    // Categories
    @GET("rest/v1/categories?select=*")
    suspend fun getCategories(): Response<List<Category>>

    @POST("rest/v1/categories")
    suspend fun createCategory(@Body category: Category): Response<List<Category>>

    @DELETE("rest/v1/categories")
    suspend fun deleteCategory(@Query("id") idQuery: String): Response<Unit>

    // Coupons
    @GET("rest/v1/coupons?select=*")
    suspend fun getCoupons(): Response<List<Coupon>>

    @POST("rest/v1/coupons")
    suspend fun createCoupon(@Body coupon: Coupon): Response<List<Coupon>>

    @DELETE("rest/v1/coupons")
    suspend fun deleteCoupon(@Query("id") idQuery: String): Response<Unit>

    // Delivery Zones
    @GET("rest/v1/delivery_zones?select=*")
    suspend fun getDeliveryZones(): Response<List<DeliveryZone>>

    @POST("rest/v1/delivery_zones")
    suspend fun saveDeliveryZone(@Body zone: DeliveryZone): Response<List<DeliveryZone>>

    @DELETE("rest/v1/delivery_zones")
    suspend fun deleteDeliveryZone(@Query("id") idQuery: String): Response<Unit>

    // Reviews
    @GET("rest/v1/reviews?select=*")
    suspend fun getReviews(): Response<List<Review>>

    @PATCH("rest/v1/reviews")
    suspend fun updateReview(
        @Query("id") idQuery: String,
        @Body updates: Map<String, Any>
    ): Response<List<Review>>

    @DELETE("rest/v1/reviews")
    suspend fun deleteReview(@Query("id") idQuery: String): Response<Unit>

    // RPCs
    @POST("rest/v1/rpc/assign_rider_to_order")
    suspend fun rpcAssignRider(@Body payload: Map<String, Any>): Response<Map<String, Any>>

    @POST("rest/v1/rpc/verify_two_factor_delivery")
    suspend fun rpcVerifyTwoFactor(@Body payload: Map<String, Any>): Response<Map<String, Any>>

    @POST("rest/v1/rpc/send_delivery_otp")
    suspend fun rpcSendDeliveryOtp(@Body payload: Map<String, Any>): Response<Map<String, Any>>

    @POST("rest/v1/rpc/verify_delivery_otp")
    suspend fun rpcVerifyDeliveryOtp(@Body payload: Map<String, Any>): Response<Map<String, Any>>
}

object RetrofitClient {
    private var supabaseUrl: String = AppConfig.DEFAULT_SUPABASE_URL
    private var supabaseAnonKey: String = AppConfig.DEFAULT_SUPABASE_ANON_KEY

    fun setCredentials(url: String, anonKey: String) {
        supabaseUrl = if (url.endsWith("/")) url else "$url/"
        supabaseAnonKey = anonKey
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()
            .header("apikey", supabaseAnonKey)
            .header("Authorization", "Bearer $supabaseAnonKey")
            .header("Content-Type", "application/json")
            .header("Prefer", "return=representation")
        val request = requestBuilder.build()
        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val api: SupabaseApi by lazy {
        Retrofit.Builder()
            .baseUrl(if (supabaseUrl.endsWith("/")) supabaseUrl else "$supabaseUrl/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SupabaseApi::class.java)
    }
}
