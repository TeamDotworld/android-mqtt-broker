package `in`.naveens.mqttbroker.service

import android.util.Log
import io.moquette.broker.Server
import java.io.IOException
import java.net.BindException
import java.util.Properties
import java.util.concurrent.Callable

class MQTTBroker(private var config: Properties?) : Callable<Boolean> {

    private val TAG = MQTTBroker::class.java.name

    companion object {
        private var server: Server? = null
        fun getServer(): Server? {
            return server
        }
    }

    fun stopServer() {
        server?.stopServer()
    }

    override fun call(): Boolean {
        try {
            server = ServerInstance().getServerInstance()
            Log.d(TAG, "call: ${config}")
            Log.d(TAG, "call: ${config?.get("password_file")}")
            server?.startServer(config)
            Log.d(TAG, "MQTT Broker Started")
            return true
        } catch (e: BindException) {
            Log.e(TAG, "Address already in use. Unable to bind.")
            Log.e(TAG, "Error : " + e.message)
            throw BindException(e.localizedMessage)
        } catch (e: IOException) {
            Log.e(TAG, "Error : " + e.message)
        }
        return false
    }
}