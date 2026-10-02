package com.shivdhikshith.medsafe

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.shivdhikshith.medsafe.core.common.DefaultDispatcherProvider
import com.shivdhikshith.medsafe.core.notifications.AndroidReminderScheduler
import com.shivdhikshith.medsafe.core.notifications.ExpiryNotificationWorker
import com.shivdhikshith.medsafe.core.notifications.NotificationChannels
import com.shivdhikshith.medsafe.core.notifications.NotificationHelper
import com.shivdhikshith.medsafe.core.notifications.ReminderScheduler
import com.shivdhikshith.medsafe.data.local.database.MedSafeDatabase
import com.shivdhikshith.medsafe.data.preferences.UserPreferencesDataStore
import com.shivdhikshith.medsafe.data.repository.AlertHistoryRepositoryImpl
import com.shivdhikshith.medsafe.data.repository.MedicineRepositoryImpl
import com.shivdhikshith.medsafe.data.repository.ReminderRepositoryImpl
import com.shivdhikshith.medsafe.data.repository.SettingsRepositoryImpl
import com.shivdhikshith.medsafe.core.files.PhotoStorageManager
import com.shivdhikshith.medsafe.data.repository.PhotoRepositoryImpl
import com.shivdhikshith.medsafe.domain.repository.AlertHistoryRepository
import com.shivdhikshith.medsafe.domain.repository.MedicineRepository
import com.shivdhikshith.medsafe.domain.repository.PhotoRepository
import com.shivdhikshith.medsafe.domain.repository.ReminderRepository
import com.shivdhikshith.medsafe.domain.repository.SettingsRepository
import com.shivdhikshith.medsafe.domain.usecase.AddBatchUseCase
import com.shivdhikshith.medsafe.domain.usecase.AddMedicineWithBatchUseCase
import com.shivdhikshith.medsafe.domain.usecase.AddPhotoUseCase
import com.shivdhikshith.medsafe.domain.usecase.CalculateExpiryStatusUseCase
import com.shivdhikshith.medsafe.domain.usecase.CheckAndNotifyExpiryUseCase
import com.shivdhikshith.medsafe.domain.usecase.DeleteBatchUseCase
import com.shivdhikshith.medsafe.domain.usecase.DeleteMedicineUseCase
import com.shivdhikshith.medsafe.domain.usecase.DeletePhotoUseCase
import com.shivdhikshith.medsafe.domain.usecase.GetDashboardSummaryUseCase
import com.shivdhikshith.medsafe.domain.usecase.GetMedicineDetailUseCase
import com.shivdhikshith.medsafe.domain.usecase.ObserveInventoryUseCase
import com.shivdhikshith.medsafe.domain.usecase.ObservePhotosUseCase
import com.shivdhikshith.medsafe.domain.usecase.ObserveSettingsUseCase
import com.shivdhikshith.medsafe.domain.usecase.UpdateBatchStatusUseCase
import com.shivdhikshith.medsafe.domain.usecase.UpdateBatchUseCase
import com.shivdhikshith.medsafe.domain.usecase.UpdateMedicineUseCase
import com.shivdhikshith.medsafe.domain.usecase.UpdateSettingsUseCase
import com.shivdhikshith.medsafe.domain.usecase.ValidateMedicineInputUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MedSafeApplication : Application() {

    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val checkMutex = Mutex()

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "MedSafeApplication onCreate starting")
        NotificationChannels.createNotificationChannels(this)
        container = AppContainer(this)

        // Schedule daily periodic background expiry check
        ExpiryNotificationWorker.schedulePeriodicWork(this)

        // Detect when app enters foreground / is reopened
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            private var startedActivityCount = 0

            override fun onActivityStarted(activity: Activity) {
                startedActivityCount++
                if (startedActivityCount == 1) {
                    Log.d(TAG, "App foregrounded/reopened (startedCount=1) -> triggering expiry check")
                    triggerExpiryCheck()
                }
            }

            override fun onActivityStopped(activity: Activity) {
                startedActivityCount = (startedActivityCount - 1).coerceAtLeast(0)
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })

        // Run non-blocking startup check for expiring batches
        triggerExpiryCheck()

        // One-time cleanup of legacy alert records from previous deleted channel on Pixel 8 emulator/devices
        val prefs = getSharedPreferences("medsafe_app_meta", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("migrated_channel_v2", false)) {
            applicationScope.launch(Dispatchers.IO) {
                try {
                    container.database.alertHistoryDao().deleteAllAlerts()
                    prefs.edit().putBoolean("migrated_channel_v2", true).apply()
                    Log.i(TAG, "Migrated to expiry notification channel v2; cleared legacy alert history")
                    triggerExpiryCheck()
                } catch (e: Exception) {
                    Log.e(TAG, "Error during v2 channel migration: ${e.message}", e)
                }
            }
        }
    }

    /**
     * Executes automatic expiry evaluation safely in the application scope.
     * Synchronized via Mutex to prevent duplicate concurrent runs.
     */
    fun triggerExpiryCheck() {
        applicationScope.launch {
            checkMutex.withLock {
                try {
                    Log.d(TAG, "Executing automatic expiry check...")
                    val result = container.checkAndNotifyExpiryUseCase()
                    Log.d(
                        TAG,
                        "Automatic expiry check complete: batchesChecked=${result.batchesChecked}, " +
                                "sent=${result.notificationsSent}, duplicates=${result.duplicatesSuppressed}, errors=${result.errorsCount}"
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error executing automatic expiry check: ${e.message}", e)
                }
            }
        }
    }

    companion object {
        private const val TAG = "MedSafeApp"
    }
}

class AppContainer(private val application: Application) {
    private val dispatchers = DefaultDispatcherProvider()
    val database: MedSafeDatabase by lazy { MedSafeDatabase.getInstance(application) }
    val preferencesDataStore: UserPreferencesDataStore by lazy { UserPreferencesDataStore(application) }

    val notificationHelper: NotificationHelper by lazy {
        NotificationHelper(application)
    }

    val reminderScheduler: ReminderScheduler by lazy {
        AndroidReminderScheduler(application)
    }

    val medicineRepository: MedicineRepository by lazy {
        MedicineRepositoryImpl(database, dispatchers)
    }

    val alertHistoryRepository: AlertHistoryRepository by lazy {
        AlertHistoryRepositoryImpl(database.alertHistoryDao(), dispatchers)
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(preferencesDataStore, dispatchers)
    }

    val reminderRepository: ReminderRepository by lazy {
        ReminderRepositoryImpl(database.reminderDao(), dispatchers)
    }

    val photoStorageManager: PhotoStorageManager by lazy {
        PhotoStorageManager(application, dispatchers)
    }

    val photoRepository: PhotoRepository by lazy {
        PhotoRepositoryImpl(database.photoDao(), dispatchers)
    }

    val addPhotoUseCase by lazy {
        AddPhotoUseCase(photoRepository, photoStorageManager)
    }

    val observePhotosUseCase by lazy {
        ObservePhotosUseCase(photoRepository)
    }

    val deletePhotoUseCase by lazy {
        DeletePhotoUseCase(photoRepository, photoStorageManager)
    }

    val calculateExpiryStatusUseCase by lazy { CalculateExpiryStatusUseCase() }
    val validateMedicineInputUseCase by lazy { ValidateMedicineInputUseCase() }

    val checkAndNotifyExpiryUseCase by lazy {
        CheckAndNotifyExpiryUseCase(
            medicineRepository = medicineRepository,
            alertHistoryRepository = alertHistoryRepository,
            settingsRepository = settingsRepository,
            notificationHelper = notificationHelper
        )
    }

    val addMedicineWithBatchUseCase by lazy {
        AddMedicineWithBatchUseCase(
            medicineRepository = medicineRepository,
            settingsRepository = settingsRepository,
            validateInputUseCase = validateMedicineInputUseCase,
            calculateExpiryStatusUseCase = calculateExpiryStatusUseCase
        )
    }

    val getDashboardSummaryUseCase by lazy {
        GetDashboardSummaryUseCase(
            medicineRepository = medicineRepository,
            settingsRepository = settingsRepository,
            calculateExpiryStatusUseCase = calculateExpiryStatusUseCase
        )
    }

    val observeInventoryUseCase by lazy {
        ObserveInventoryUseCase(
            medicineRepository = medicineRepository,
            settingsRepository = settingsRepository,
            calculateExpiryStatusUseCase = calculateExpiryStatusUseCase
        )
    }

    val getMedicineDetailUseCase by lazy {
        GetMedicineDetailUseCase(
            medicineRepository = medicineRepository,
            settingsRepository = settingsRepository,
            calculateExpiryStatusUseCase = calculateExpiryStatusUseCase
        )
    }

    val updateBatchStatusUseCase by lazy {
        UpdateBatchStatusUseCase(medicineRepository)
    }

    val updateMedicineUseCase by lazy {
        UpdateMedicineUseCase(medicineRepository)
    }

    val addBatchUseCase by lazy {
        AddBatchUseCase(medicineRepository, settingsRepository, calculateExpiryStatusUseCase)
    }

    val updateBatchUseCase by lazy {
        UpdateBatchUseCase(medicineRepository, settingsRepository, calculateExpiryStatusUseCase)
    }

    val deleteBatchUseCase by lazy {
        DeleteBatchUseCase(medicineRepository)
    }

    val deleteMedicineUseCase by lazy {
        DeleteMedicineUseCase(medicineRepository)
    }

    val observeSettingsUseCase by lazy {
        ObserveSettingsUseCase(settingsRepository)
    }

    val updateSettingsUseCase by lazy {
        UpdateSettingsUseCase(settingsRepository)
    }

    val dataManagementUseCases by lazy {
        com.shivdhikshith.medsafe.domain.usecase.DataManagementUseCases(
            medicineRepository = medicineRepository,
            reminderRepository = reminderRepository,
            database = database,
            settingsRepository = settingsRepository,
            alertHistoryRepository = alertHistoryRepository,
            photoRepository = photoRepository
        )
    }
}
