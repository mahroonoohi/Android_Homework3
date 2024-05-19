package mahroo.noohi.android_hw3_worker


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val periodicWorkRequest = PeriodicWorkRequestBuilder<AirplaneBluetoothLogger>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.SECONDS
        ).build()
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "loggerPeriodicWorkRequest",
            ExistingPeriodicWorkPolicy.REPLACE,
            periodicWorkRequest
        )
        setContent {
            Text(text = "Good Luck")
        }
    }
}
