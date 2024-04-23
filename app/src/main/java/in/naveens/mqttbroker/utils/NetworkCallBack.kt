package `in`.naveens.mqttbroker.utils

import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import `in`.naveens.mqttbroker.MQTTBrokerApp

class NetworkCallBack : ConnectivityManager.NetworkCallback() {

    private val TAG = "NetworkCallBack"

    // Got network connection
    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        val intent = Intent(Utils.NETWORK_BROADCAST_ACTION)
        intent.putExtra("status", true)
        MQTTBrokerApp.instance.sendBroadcast(intent)
    }

    // Lost network connection
    override fun onLost(network: Network) {
        super.onLost(network)
        val intent = Intent(Utils.NETWORK_BROADCAST_ACTION)
        intent.putExtra("status", false)
        MQTTBrokerApp.instance.sendBroadcast(intent)
    }

}