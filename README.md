# MEDSAFE Mobile — Android Foundation (Milestone 1)

> **Important Safety Notice:**
> *"This application helps organize medicine expiry information. It does not provide medical advice. Always verify the original packaging and consult a pharmacist or healthcare professional if you are unsure whether a medicine can be used."*

---

## 1. Project Purpose & Philosophy

**MEDSAFE Mobile** is an independent Android application engineered to help individuals, families, and caregivers track medication batches, monitor expiry windows, and prevent accidental consumption of expired medications.

The project is built on the following core principles:
- **100% Offline-First:** Operates without any internet connection.
- **Privacy by Default:** Zero analytics, zero cloud databases, zero account registrations. All data remains securely stored on the local device.
- **Batch-Centric:** Medicines can have multiple batches, each with its own expiry date, quantity, and storage location.
- **Independent from Desktop:** While inspired by Windows MEDSAFE, the Android edition is a completely separate codebase with mobile-first UX.

---

## 2. Technology Stack

- **Language:** Kotlin 1.9.24
- **UI Framework:** Jetpack Compose (BOM 2024.05.00)
- **Design System:** Material 3 (Material Design tokens, custom accessible color schemes, and dynamic system theme support)
- **Database:** AndroidX Room 2.6.1 with KSP (SQLite with relational entities and cascade deletion)
- **Preferences:** AndroidX DataStore Preferences 1.1.1
- **Navigation:** Navigation Compose 2.7.7
- **Concurrency & State:** Kotlin Coroutines 1.8.1 & StateFlow
- **Architecture:** Clean MVVM + Repository Pattern + Domain Use Cases
- **Build System:** Gradle 8.7 with Kotlin DSL

---

## 3. Architecture & Project Layout

MEDSAFE Mobile strictly enforces separation of concerns across clean layers:

```
MEDSAFE-Mobile/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/shivdhikshith/medsafe/
│       │   │   ├── MainActivity.kt                // Root Activity with dynamic theme binding
│       │   │   ├── MedSafeApplication.kt          // App entry & AppContainer DI graph
│       │   │   │
│       │   │   ├── presentation/                  // FRONTEND / PRESENTATION LAYER
│       │   │   │   ├── navigation/                // Screen routes, BottomNavItem, NavGraph
│       │   │   │   ├── theme/                     // Color palette, Typography, Shapes, MedSafeTheme
│       │   │   │   ├── components/                // Status badges, cards, buttons, empty states
│       │   │   │   └── common/                    // UI state helpers
│       │   │   │
│       │   │   ├── feature/                       // FEATURE MODULES (UI + ViewModels)
│       │   │   │   ├── dashboard/                 // Overview metrics & urgent expiry cards
│       │   │   │   ├── inventory/                 // Filtered & sorted medicine list
│       │   │   │   ├── medicine/                  // Add medicine form & detail management
│       │   │   │   ├── summary/                   // Personal organizational medicine notes
│       │   │   │   ├── reminders/                 // Medication reminder schedule foundation
│       │   │   │   ├── scanner/                   // Smart scanner architectural boundary
│       │   │   │   ├── photos/                    // Photo Picker gallery integration
│       │   │   │   ├── settings/                  // Alert thresholds, notifications, theme
│       │   │   │   ├── about/                     // App info and safety disclaimer
│       │   │   │   └── more/                      // Navigation hub for settings/about
│       │   │   │
│       │   │   ├── domain/                        // BUSINESS LOGIC LAYER (Pure Kotlin)
│       │   │   │   ├── model/                     // Medicine, Batch, ExpiryStatus, Settings
│       │   │   │   ├── repository/                // Repository interfaces
│       │   │   │   └── usecase/                   // Expiry calculator, validation, CRUD
│       │   │   │
│       │   │   ├── data/                          // DATA & PERSISTENCE LAYER
│       │   │   │   ├── local/
│       │   │   │   │   ├── database/              // MedSafeDatabase (Room)
│       │   │   │   │   ├── entity/                // MedicineEntity, BatchEntity, etc.
│       │   │   │   │   ├── dao/                   // MedicineDao, BatchDao, ReminderDao
│       │   │   │   │   └── relation/              // MedicineWithBatchesRelation
│       │   │   │   ├── mapper/                    // Entity <-> Domain mappers
│       │   │   │   ├── repository/                // MedicineRepositoryImpl, SettingsRepoImpl
│       │   │   │   └── preferences/               // UserPreferencesDataStore
│       │   │   │
│       │   │   └── core/                          // CORE INFRASTRUCTURE
│       │   │       ├── common/                    // Constants, Resource, DispatcherProvider
│       │   │       ├── utils/                     // DateUtils, ExpiryCalculator
│       │   │       ├── notifications/             // NotificationHelper, Channels
│       │   │       ├── permissions/               // PermissionUtils
│       │   │       ├── files/                     // FileUtils
│       │   │       └── extensions/                // DateExtensions, StringExtensions
│       │   │
│       │   └── res/                               // Android resources, drawables, strings
│       └── test/                                  // UNIT TESTS
│           ├── core/                              // ExpiryCalculator, DateUtils tests
│           ├── domain/                            // Use Case & business logic tests
│           ├── data/                              // Entity mappers & test doubles
│           └── feature/                           // ViewModel state collection tests
│
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

---

## 4. Key Architectural Guarantees

1. **Unidirectional Data Flow:**
   ```
   Composable UI ➔ ViewModel ➔ Use Case ➔ Repository Interface ➔ Repository Impl ➔ Room DAO ➔ SQLite
   ```
2. **Room Entities Never Reach UI:**
   Room entities (`MedicineEntity`, `BatchEntity`) are mapped to clean domain models (`Medicine`, `Batch`) before reaching ViewModels or UI components.
3. **Medical Safety Distinction:**
   The `VALID` status is strictly rendered as **"Expiry date not reached"**, never *"Safe to use"*.
4. **Clean Input Validation:**
   Input validation and atomic transactions take place inside use cases (`AddMedicineWithBatchUseCase`, `ValidateMedicineInputUseCase`), keeping UI screens declarative.

---

## 5. Getting Started

### Prerequisites
- Android Studio Iguana, Jellyfish, Koala, or newer
- JDK 17+ (e.g. Microsoft OpenJDK 17 or Eclipse Temurin 17)
- Android SDK Platform 34 & Build Tools 34.0.0

### Open in Android Studio
1. Launch Android Studio.
2. Select **Open** and choose the `MEDSAFE-Mobile` directory.
3. Allow Gradle to sync dependencies.

### Build and Test Commands

#### Run Unit Tests
```powershell
./gradlew test
```

#### Build Debug APK
```powershell
./gradlew assembleDebug
```
The resulting debug APK is located at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 6. Milestone Roadmap

- **M1 (Current):** Foundation, Clean Architecture, Room Database, Repositories, Domain Models, Use Cases, ViewModels, Navigation, Dashboard, Inventory, Add Medicine, Medicine Detail foundation, Summary foundation, Settings, About, Unit Tests, Debug APK.
- **M2:** Alarms & scheduled exact reminders via `AlarmManager` and background notifications.
- **M3:** Photo capture via CameraX and multi-photo attachment.
- **M4:** Medicine Summary expansion & PDF/JSON export.
- **M5:** QR & Barcode scanning via Google ML Kit.
- **M6:** OCR package scanning for expiry date extraction.
- **M7:** Encrypted local backup and restore.
- **M8:** Privacy-preserving mobile shelf-life analytics.
- **M9:** Optional encrypted local Windows ↔ Android data transfer.
