package mahroo.noohi.android_homework3


import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class NetworkChangeService : Service() {
    private val networkChangeReceiver = NetworkChangeReceiver()
    private var notification: NotificationCompat.Builder? = null

    companion object {
        const val ACTION_NETWORK_CHANGE = "com.example.android.ACTION_NETWORK_CHANGE"
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        registerReceiver(
            networkChangeReceiver,
            IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        )

        notification = NotificationCompat.Builder(this, "service_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Internet Access")
            .setContentText("Connection Status: Disconnected")

        startForeground(1, notification!!.build())

        return START_STICKY
    }

    override fun onDestroy() {
        unregisterReceiver(networkChangeReceiver)
        super.onDestroy()
    }

    inner class NetworkChangeReceiver : BroadcastReceiver() {
        private var previousConnectedState = false

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != "android.net.conn.CONNECTIVITY_CHANGE") {
                return
            }

            val isConnected = isInternetConnected(context)
            if (isConnected != previousConnectedState) {
                logInternetStatus(context, isConnected)
                saveInternetStatusToFile(context, isConnected)
                updateNotification(isConnected)
                sendBroadCast(isConnected)
                previousConnectedState = isConnected
            }
        }

        private fun isInternetConnected(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

    private fun logInternetStatus(context: Context, isConnected: Boolean) {
        val timestamp = System.currentTimeMillis()
        val formattedTimestamp =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))

        val status = if (isConnected) "Connected" else "Not Connected"

        val jsonObject = JSONObject().apply {
            put("type", "Internet")
            put("time", formattedTimestamp)
            put("value", status)
        }

        Log.i("InternetReceiver", jsonObject.toString())
    }

    private fun saveInternetStatusToFile(context: Context, isConnected: Boolean) {
        val timestamp = System.currentTimeMillis()
        val formattedTimestamp =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))

        val status = if (isConnected) "Connected" else "Not Connected"

        val jsonObject = JSONObject().apply {
            put("type", "Internet")
            put("time", formattedTimestamp)
            put("value", status)
        }
        val logEntry = jsonObject.toString() + "\n"

        try {
            val file = File(context.filesDir, "internet_status.txt")
            FileWriter(file, true).use { writer ->
                writer.write(logEntry)
            }
        } catch (e: IOException) {
            Log.e("InternetReceiver", "Error saving Internet status to file: ${e.message}")
        }
    }

    private fun sendBroadCast(isConnected: Boolean) {
        val connectionStatusIntent = Intent(ACTION_NETWORK_CHANGE)
        connectionStatusIntent.putExtra("CONNECTION_STATUS", isConnected)
        sendBroadcast(connectionStatusIntent)
    }

    private fun updateNotification(isConnected: Boolean) {
        val statusText = if (isConnected) "Connected" else "Disconnected"
        notification?.setContentText("Connection Status: $statusText")
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification?.build())
    }
}