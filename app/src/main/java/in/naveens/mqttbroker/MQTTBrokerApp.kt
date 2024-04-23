package `in`.naveens.mqttbroker

import android.app.Application
import `in`.naveens.mqttbroker.utils.AppPreferences

class MQTTBrokerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppPreferences.init(this)
        instance = this
    }

    companion object {
        lateinit var instance: MQTTBrokerApp
            private set
    }
}