package `in`.naveens.mqttbroker.utils

import android.app.ActivityManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*

object Utils {

    const val TAG = "Utils"
    val MQTT_STATUS_ON_OR_OFF="mqtt_updates"
    val NETWORK_BROADCAST_ACTION="network_change"

    fun generatePassword(): String {
        val leftLimit = 48 // numeral '0'
        val rightLimit = 122 // letter 'z'
        val targetStringLength = 10
        val random = Random()
        return random.ints(leftLimit, rightLimit + 1)
            .filter { i: Int -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97) }
            .limit(targetStringLength.toLong())
            .collect(
                { StringBuilder() },
                { obj: java.lang.StringBuilder, i: Int -> obj.appendCodePoint(i) }) { obj: java.lang.StringBuilder, charSequence: java.lang.StringBuilder? ->
                obj.append(
                    charSequence
                )
            }
            .toString()
    }

    fun isMyServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager =
            context.getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    fun getIPAddress(useIPv4: Boolean): String {
        try {
            val interfaces: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs: List<InetAddress> = Collections.list(intf.inetAddresses)
                Log.d(TAG, "getIPAddress: $addrs")
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr = addr.hostAddress
                        if (sAddr != null) {
                            val isIPv4 = sAddr.indexOf(':') < 0
                            if (useIPv4) {
                                if (isIPv4) return sAddr
                            } else {
                                if (!isIPv4) {
                                    val delim = sAddr.indexOf('%') // drop ip6 zone suffix
                                    return if (delim < 0) sAddr.uppercase() else sAddr.substring(
                                        0,
                                        delim
                                    ).uppercase()
                                }
                            }
                        }
                    }
                }
            }
        } catch (ignored: Exception) {
        } // for now eat exceptions
        return ""
    }

    fun networkType(context: Context) : Type{
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        Log.d("MqttService", "networkType: ${netInfo?.typeName}")
        if (netInfo != null){
            return Type.valueOf(netInfo.typeName)
        }
        return Type.NONE
    }

    enum class Type{
        WIFI,
        MOBILE,
        ETHERNET,
        NONE
    }

    val networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .build()
}