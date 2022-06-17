package `in`.naveens.mqttbroker.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Pair

object AppPreferences {
    private const val NAME = "MQTT Broker"
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences
    private val IS_FIRST_RUN_PREF = Pair("is_first_time", true)
    private val MQTT_PORT = Pair("mqtt_port1", "1883")
    private val MQTT_AUTH_STATUS = Pair("mqtt_auth_status", false)
    private val MQTT_BROKER_STATUS = Pair("mqtt_broker_status", false)
    private val MQTT_USER_NAME = Pair("mqtt_username", "admin")
    private val MQTT_PASSWORD = Pair("mqtt_password", null)
    private val MQTT_HOST = Pair("mqtt_host", null)

    fun init(context: Context) {
        preferences = context.getSharedPreferences(NAME, MODE)
    }


    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    var firstRun: Boolean
        get() = preferences.getBoolean(IS_FIRST_RUN_PREF.first, IS_FIRST_RUN_PREF.second)
        set(value) = preferences.edit {
            it.putBoolean(IS_FIRST_RUN_PREF.first, value)
        }

    var mqttPort: String?
        get() = preferences.getString(MQTT_PORT.first, MQTT_PORT.second)
        set(value) = preferences.edit {
            it.putString(MQTT_PORT.first, value)
        }

    var mqttUserName: String?
        get() = preferences.getString(MQTT_USER_NAME.first, MQTT_USER_NAME.second)
        set(value) = preferences.edit {
            it.putString(MQTT_USER_NAME.first, value)
        }
    var mqttPassword: String?
        get() = preferences.getString(MQTT_PASSWORD.first, MQTT_PASSWORD.second)
        set(value) = preferences.edit {
            it.putString(MQTT_PASSWORD.first, value)
        }

    var mqttAuthStatus: Boolean
        get() = preferences.getBoolean(MQTT_AUTH_STATUS.first, MQTT_AUTH_STATUS.second)
        set(value) = preferences.edit {
            it.putBoolean(MQTT_AUTH_STATUS.first, value)
        }
    var mqttBrokerStatus: Boolean
        get() = preferences.getBoolean(MQTT_BROKER_STATUS.first, MQTT_BROKER_STATUS.second)
        set(value) = preferences.edit {
            it.putBoolean(MQTT_BROKER_STATUS.first, value)
        }
    var mqttHost: String?
        get() = preferences.getString(MQTT_HOST.first, MQTT_HOST.second)
        set(value) = preferences.edit {
            it.putString(MQTT_HOST.first, value)
        }
}
