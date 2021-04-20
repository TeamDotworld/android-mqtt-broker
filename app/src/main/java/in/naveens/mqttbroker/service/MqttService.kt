package `in`.naveens.mqttbroker.service

import `in`.naveens.mqttbroker.MainActivity
import `in`.naveens.mqttbroker.R
import `in`.naveens.mqttbroker.utils.AppPreferences
import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import io.moquette.BrokerConstants
import org.apache.commons.codec.digest.DigestUtils
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask

open class MqttService : Service() {
    private val TAG = MqttService::class.java.simpleName
    val CHANNEL_ID = "MQTTBrokerNotificationChannel"

    var mqttBroker: MQTTBroker? = null
    private var thread: Thread? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(TAG, "Received start id $startId : $intent")
        startForeground(1, getNotification())
        try {
            updateIP()
        } catch (e: Exception) {
            Log.e(TAG, "Error : " + e.message)
        }
        try {
            val props = getConfig()
            Log.d(TAG, "onStartCommand: props $props")
            mqttBroker = MQTTBroker(props)
            val futureTask = FutureTask(mqttBroker)
            if (thread == null || thread?.isAlive != true) {
                thread = Thread(futureTask)
                thread?.name = "MQTT Server"
                thread?.start()
                if (futureTask.get()) {
                    Toast.makeText(this, "MQTT Broker Service started", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Unable to start MQTT Broker", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: ExecutionException) {
            Log.e(TAG, "Error kotlin : " + e.message)
            e.printStackTrace()
            Toast.makeText(this, "Try using another port. Address already in use", Toast.LENGTH_SHORT).show()
            stopSelf()
            return START_NOT_STICKY
        } catch (e: Exception) {
            e.printStackTrace()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun getNotification(): Notification? {
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        return Notification.Builder(this, CHANNEL_ID)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_message))
                .setSmallIcon(R.drawable.ic_mqtt_sq)
                .setContentIntent(pendingIntent)
                .setTicker(getText(R.string.ticker_text))
                .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun getConfig(): Properties {
        val props = Properties()
        props.setProperty(BrokerConstants.PORT_PROPERTY_NAME, AppPreferences.mqttPort)
        val auth = AppPreferences.mqttAuthStatus
        props.setProperty(BrokerConstants.NEED_CLIENT_AUTH, auth.toString())
        if (auth) {
            val username =AppPreferences.mqttUserName
            val password = AppPreferences.mqttPassword
            if (password != null) {
                val sha256hex = DigestUtils.sha256Hex(password)
                val filename = "password.conf"
                val fileContents = "$username:$sha256hex"
                try {
                    openFileOutput(filename, MODE_PRIVATE).use { fos ->
                        fos.write(fileContents.toByteArray())
                        val file = File(filesDir, filename)
                        props.setProperty(BrokerConstants.PASSWORD_FILE_PROPERTY_NAME, file.absolutePath)
                    }
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(this, "Unable to generate auth file", Toast.LENGTH_SHORT).show()
            }
        }
        props.setProperty(BrokerConstants.HOST_PROPERTY_NAME, `in`.naveens.mqttbroker.utils.Utils.getIPAddress(true))
        props.setProperty(BrokerConstants.WEB_SOCKET_PORT_PROPERTY_NAME, BrokerConstants.WEBSOCKET_PORT.toString())
        return props
    }

    private fun updateIP() {
        AppPreferences.mqttHost=`in`.naveens.mqttbroker.utils.Utils.getIPAddress(true)
    }

    override fun onDestroy() {
        if (thread != null && MQTTBroker.getServer() != null) {
            try {
                Log.d(TAG, "Trying to stop mqtt server")
                mqttBroker?.stopServer()
                thread?.interrupt()
                Toast.makeText(this, "MQTT Broker Service stopped", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e(TAG, e.message)
            }
        } else {
            Log.d(TAG, "Server is not running")
        }
        super.onDestroy()
    }
}