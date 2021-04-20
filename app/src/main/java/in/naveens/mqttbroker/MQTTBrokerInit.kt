package `in`.naveens.mqttbroker

import `in`.naveens.mqttbroker.utils.AppPreferences
import android.app.Application

class MQTTBrokerInit : Application() {
    override fun onCreate() {
        super.onCreate()
        AppPreferences.init(this)
    }
}