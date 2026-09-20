package com.mohna.ops

/**
 * Mohna Ops System Configuration
 * Provides Supabase PostgREST endpoints, authentication headers,
 * company metadata, and operational constants.
 */
object AppConfig {
    // Supabase PostgREST API Base Configuration
    // Users can override via environment or direct setup
    const val DEFAULT_SUPABASE_URL = "https://your-project.supabase.co"
    const val DEFAULT_SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.dummy_anon_key"

    // Company & Corporate Metadata for Official Reports and Slips
    const val COMPANY_NAME = "MOHNA EXPRESS"
    const val COMPANY_FULL_NAME = "Mohna Express Logistics & Ultra-Fast Delivery Network Private Limited"
    const val COMPANY_CIN = "U63090DL2024PTC128940"
    const val COMPANY_GSTIN = "10AAECM8762N1ZQ"
    const val COMPANY_HQ = "Patna - Jamalpur Expressway Hub"
    const val COMPANY_EMAIL = "corporate@mohnaexpress.com"
    const val DISPATCHER_NAME = "Amarjeet Kumar"
    const val DISPATCHER_TITLE = "Warehouse Dispatcher / Operations Head"
    const val DIRECTOR_TITLE = "Executive Director / Authorized Signatory"

    // Operational SLA & Delivery Guarantees
    const val DEFAULT_ETA_MINUTES = 20
    const val LATE_CASHBACK_AMOUNT = 25

    // Live GPS Telemetry Broadcast Interval (milliseconds)
    const val TELEMETRY_INTERVAL_MS = 10_000L

    // Notification Channel IDs
    const val TELEMETRY_CHANNEL_ID = "mohna_telemetry_channel"
    const val TELEMETRY_NOTIFICATION_ID = 1001
}
