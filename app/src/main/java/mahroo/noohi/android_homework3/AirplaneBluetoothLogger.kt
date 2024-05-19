package mahroo.noohi.android_homework3



import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Context.BLUETOOTH_SERVICE
import android.provider.Settings
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit




class AirplaneBluetoothLogger(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val bluetoothEnabled = isBluetoothEnabled(applicationContext)
        val airplaneModeStatus = isAirplaneModeOn()
        val currentTimeMillis = System.currentTimeMillis()
        Log.i("worker_airplane", "Status: ${if (airplaneModeStatus) "On" else "Off"}")
        Log.i("worker_bluetooth", "Status: ${if (bluetoothEnabled) "On" else "Off"}")

        saveLogToFile(applicationContext, "worker_airplane", currentTimeMillis, if (airplaneModeStatus) "On" else "Off")
        saveLogToFile(applicationContext, "worker_bluetooth", currentTimeMillis, if (bluetoothEnabled) "On" else "Off")

        val periodicWorkRequest = PeriodicWorkRequestBuilder<AirplaneBluetoothLogger>(
            repeatInterval = 2,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setInitialDelay(2, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "loggerPeriodicWorkRequest",
            ExistingPeriodicWorkPolicy.REPLACE,
            periodicWorkRequest
        )



        return Result.success()
    }

    private fun isAirplaneModeOn(): Boolean {
        return Settings.Global.getInt(
            applicationContext.contentResolver,
            Settings.Global.AIRPLANE_MODE_ON, 0
        ) != 0
    }

    private fun isBluetoothEnabled(context: Context): Boolean {
        val bluetoothManager = context.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager?
        val bluetoothAdapter = bluetoothManager?.adapter
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled
    }


    private fun saveLogToFile(context: Context, type: String, timestamp: Long, value: String) {
        val formattedTimestamp =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))

        val jsonObject = JSONObject().apply {
            put("type", type)
            put("time", formattedTimestamp)
            put("value", value)
        }
        val logEntry = jsonObject.toString() + "\n"

        try {
            val file = File(context.filesDir, "AirplaneBluetoothStatus.txt")
            FileWriter(file, true).use { writer ->
                writer.write(logEntry)
            }
        } catch (e: IOException) {
            Log.e("LogWorker", "Error saving log entry to file: ${e.message}")
        }
    }
}