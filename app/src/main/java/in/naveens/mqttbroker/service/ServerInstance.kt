package `in`.naveens.mqttbroker.service

import android.util.Log
import io.moquette.broker.Server
open class ServerInstance {
    private val TAG = ServerInstance::class.java.name
    private var serverInstance: Server? = null

    fun getServerInstance(): Server? {
        try {
            if (serverInstance == null) {
                synchronized(this) {
                    if (serverInstance == null) {
                        serverInstance = Server()
                        return serverInstance
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message)
        }
        return serverInstance
    }
}