package mahroo.noohi.android_homework3



import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //start My Service
        startService(Intent(this, NetworkChangeService::class.java))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "service_channel",
                "My Notification",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // start My Worker
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
           MainContent()
        }
    }
}


@Composable
fun MainContent() {
    val context = LocalContext.current
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        ConnectionStatus()
        Spacer(modifier = Modifier.height(16.dp))
        mergeFilesScreen(context = context)
        Spacer(modifier = Modifier.height(16.dp))
        LogList(context = context)
    }
}


@Composable
fun ConnectionStatus() {
    val context = LocalContext.current

    val isConnected = remember { mutableStateOf(false) }

    val bcr = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != "com.example.android.ACTION_NETWORK_CHANGE") {
                return
            }
            val isConnectedValue = intent.getBooleanExtra("CONNECTION_STATUS", false)
            isConnected.value = isConnectedValue
        }
    }

    val filter = IntentFilter("com.example.android.ACTION_NETWORK_CHANGE")

    DisposableEffect(Unit) {
        ContextCompat.registerReceiver(context, bcr, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        onDispose {
            context.unregisterReceiver(bcr)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Internet Access:",
            fontSize = 35.sp,
            color = Color.Black
        )
        Text(
            text = if (isConnected.value) "Connected" else "Disconnected",
            fontWeight = FontWeight.SemiBold,
            fontSize = 25.sp,
            color = if (isConnected.value) Color.Green else Color.Red
        )
    }
}



private fun mergeFilesScreen(context: Context) {
    val file1Content = readFileContent(context, "AirplaneBluetoothStatus.txt")
    val file2Content = readFileContent(context, "internet_status.txt")
    val mergedContent = file1Content + "\n" + file2Content
    writeFileContent(context, "final.txt", mergedContent)
}

private fun readFileContent(context: Context, fileName: String): String {
    val file = File(context.filesDir, fileName)
    return file.readText()
}

private fun writeFileContent(context: Context, fileName: String, content: String) {
    val file = File(context.filesDir, fileName)
    file.writeText(content)
}


@Composable
fun LogList(context: Context) {
    val logs = remember { readLogs(context) }
    val sortedLogs = remember { sortLogsByTime(logs) }

    LazyColumn {
        items(logs) { log ->
            Text(text = log)
        }
    }
}

private fun readLogs(context: Context): List<String> {
    val file = File(context.filesDir, "final.txt")
    return if (file.exists()) {
        file.readLines().reversed() // Read logs and reverse the list
    } else {
        emptyList()
    }
}


private fun sortLogsByTime(logs: List<String>): List<String> {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    return logs.sortedByDescending {
        try {
            val timeString = it.substringBefore(" - ")
            dateFormat.parse(timeString)
        } catch (e: Exception) {
            null
        }
    }
}
