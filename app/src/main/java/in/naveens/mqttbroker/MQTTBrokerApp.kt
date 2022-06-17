package `in`.naveens.mqttbroker

import `in`.naveens.mqttbroker.utils.AppPreferences
import android.app.Application

class MQTTBrokerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppPreferences.init(this)
        instance = this
    }

    companion object {
        lateinit var instance: MQTTBrokerApp
    }
}