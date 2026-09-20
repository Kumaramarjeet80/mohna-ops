# ⚡ Mohna Ops (Unified Admin & Rider Hub)

**Mohna Ops** is a 100% production-ready, feature-complete native Android application in Kotlin and Jetpack Compose.
It unifies two core logistics portals into a single seamless APK:
1. **Mohna Express - Rider Partner Hub**
2. **Admin Dashboard - Mohna Express Control Hub**

Backend: Shared Supabase PostgreSQL REST / PostgREST architecture.

---

## 🚀 Key Features

### 1. Dual-Theme & Role-Based Access Control (RBAC)
- **Rider Theme (Deep Dark Slate)**: Background `#0F172A`, Surface `#1E293B`, Borders `#334155`, Accents `#38BDF8` & `#0284C7`, Status `#22C55E` & `#EF4444`.
- **Admin Theme (Clean Industrial Light)**: Background `#F1F5F9`, Surface `#FFFFFF`, Borders `#CBD5E1`, Primary `#0F172A`, Accent `#2563EB`.
- **Super Admin Dynamic Switcher**: Instant top-bar toggle allowing instant switching between 🛵 Rider Mode and 🛠️ Admin Mode with live theme re-composition.
- **Root Authentication Gateway**: Email/password sign-in and 6-digit OTP verification signup.

### 2. Rider Partner Hub
- **Live Metrics Grid**: Available Dispatches count, Completed Today count, and real-time calculated On-Time Guarantee Rate (%).
- **Continuous High-Speed Camera Scanning (CameraX + ML Kit Barcode)**:
  - **Bulk Warehouse Pickup (Batch Scanner)**: Scans parcel stickers continuously without closing the viewfinder. Parses `MOHNA_PARCEL_LABEL`, verifies tokens, auto-accepts orders, increments batch pill counter, and plays high-pitch chime.
  - **Quick Doorstep Handover (2-Step Verification)**:
    - *Step 1*: Scans parcel QR label, pauses camera, and presents itemized inspection card (items, price, quantity, customer phone/address, assigned rider details).
    - *Step 2*: Scans dynamic customer handover QR (`MOHNA_DELIVERY_HANDOVER`), authenticates both tokens, marks delivered, and credits ₹25 wallet cashback if delivered past SLA.
- **AudioTrack Synthesis (`SoundManager.kt`)**: Pure PCM tone generator producing 587.33Hz $\rightarrow$ 880Hz success chime and 220Hz $\rightarrow$ 110Hz error buzzer without external audio files.
- **Live GPS Telemetry Service (`RiderLocationService.kt`)**: Android Foreground Service broadcasting GPS coordinates every 10 seconds to `riders.live_lat` and `riders.live_lng`.
- **Fallback 4-Digit Handover PIN**: Manual PIN verification dialog with customer email resend.
- **Direct Navigation & Dialing**: Google Maps routing and customer phone calling intents.

### 3. Admin Control Hub (10 Workspaces)
1. **🧾 Orders & Live Countdowns**: Synchronized live countdown tickers (Ticking cyan, On-Time green, Late red), Google Maps route launcher, and dispatch slips.
2. **📦 Dark Store Picker & Packing Hub**: Packing queue showing items to pack with **"📦 Mark Packed & Ready"** action.
3. **👥 Users Database**: Customer accounts, wallet balances, registered & login GPS, block/unblock actions, and **"➕ Add New Customer" Dialog**.
4. **🛵 Rider Fleet & Verification**: Fleet roster with live GPS telemetry, **"✔ Verify & Activate"**, **"🚫 Block"**, and **"➕ Add New Rider" Dialog** with pre-verification options.
5. **⭐ Customer Reviews & Feedback**: Ratings, comments, inline comment editing, and deletion.
6. **📊 Reports (CSV & PDF)**: One-click CSV and formal Executive Audit PDF generation with company header, metadata, verified seal stamp, and dual signature blocks.
7. **🎟️ Coupons**: Promo code creation with scoped delivery zones and delete actions.
8. **📦 Products & Stock**: Full catalog CRUD with retail/wholesale prices, MRP, stock quantities, and scope filtering.
9. **🏷️ Categories**: Scoped categories with automatic slug generation.
10. **🛠️ Delivery Zones**: Boundaries, delivery minutes SLA, and late cashback penalty management.
- **Master Date Filter & Omnisearch**: Universal filtering across 24h, 7d, 30d, all-time, and live text search.

### 4. Document & Label Printing
- **A4 Dispatch Docket**: Packing manifest with item breakdown and warehouse checklist.
- **8-per-A4 Parcel Labels**: Standard 2FA shipping labels with scannable QR codes rendered via ZXing.
- **Executive Audit PDFs**: Multi-column reports with official corporate headers, verified seal stamp, and authorized signature lines.

---

## 🛠️ Tech Stack
- **Kotlin**: 1.9.23
- **Android Target SDK**: 34 (Android 14)
- **Jetpack Compose**: Material3, Icons-Extended, Navigation-Compose
- **CameraX**: Camera2, Lifecycle, View (`1.3.2`)
- **Google ML Kit**: Barcode Scanning (`17.2.0`)
- **ZXing Core**: Local 2FA QR code bitmap generation (`3.5.3`)
- **Networking**: Retrofit2 (`2.11.0`) + OkHttp3 (`4.12.0`)
- **Location**: Google Play Services Location (`21.2.0`)
- **Image Loading**: Coil Compose (`2.6.0`)
- **Document Engine**: Android `PdfDocument` & `PrintManager`
- **Gradle**: 8.7

---

## 📦 Building the APK
```bash
./gradlew assembleDebug
```
The compiled APK will be output to:
`app/build/outputs/apk/debug/app-debug.apk`
