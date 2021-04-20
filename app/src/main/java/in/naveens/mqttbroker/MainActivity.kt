package `in`.naveens.mqttbroker
import `in`.naveens.mqttbroker.service.MqttService
import `in`.naveens.mqttbroker.utils.AppPreferences
import `in`.naveens.mqttbroker.utils.Utils.Companion.generatePassword
import `in`.naveens.mqttbroker.utils.Utils.Companion.getIPAddress
import `in`.naveens.mqttbroker.utils.Utils.Companion.isMyServiceRunning
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.*
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity(), OnSharedPreferenceChangeListener {

    private  var TAG=MainActivity::class.java.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AppPreferences.firstRun){
            initSharedPrefs()
            Log.d(TAG, "onCreate: its frist time run")
        }
        setIP()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        setContentView(R.layout.settings_activity)

        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.settings, SettingsFregment())
                    .commit()

        } else {
            title = savedInstanceState.getCharSequence(TAG)
        }

    }

    class SettingsFregment() :PreferenceFragmentCompat(){
        lateinit var mPreferences: SharedPreferences;
        var host: EditTextPreference? = null
        var port: EditTextPreference? = null
        var username: EditTextPreference? = null
        var password: EditTextPreference? = null
        var auth_enable: SwitchPreferenceCompat? = null

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            mPreferences = this.requireActivity().getSharedPreferences("sos_mdm", MODE_PRIVATE)
            host=findPreference(getString(R.string.mqtt_host))
            port=findPreference(getString(R.string.mqtt_port))
            username=findPreference(getString(R.string.mqtt_username))
            password=findPreference(getString(R.string.mqtt_password))
            auth_enable=findPreference(getString(R.string.mqtt_auth_status))
        }

        override fun onResume() {
            if (host != null){
                host?.text = AppPreferences.mqttHost
                port?.text = AppPreferences.mqttPort
                password?.text = AppPreferences.mqttPassword
                username?.text = AppPreferences.mqttUserName
                auth_enable?.isChecked= AppPreferences.mqttAuthStatus
            }
            super.onResume()
        }


    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        Log.d(TAG, "Preference changed. $key")
        try {
            when (key) {
                getString(R.string.mqtt_broker_status) -> {
                    Log.d(TAG, "Server status changed")
                    val status = sharedPreferences.getBoolean(key, false)
                    Log.d(TAG, "Start Server?$status")
                    if (status) {
                        Log.d(TAG, "Starting Server")
                        startService()
                    } else {
                        Log.d(TAG, "Stopping Server")
                        stopService()
                    }
                }
                getString(R.string.mqtt_auth_status) -> {
                    Log.d(TAG, "Restarting mqtt service")
                    Log.d(TAG, "onSharedPreferenceChanged: "+sharedPreferences.getBoolean(key, false))
                    val status = sharedPreferences.getBoolean(key, false)
                    AppPreferences.mqttAuthStatus=status

                    Thread {
                        stopService()
                        startService()
                    }.start()
                    Toast.makeText(this, "MQTT broker restarted with updated config", Toast.LENGTH_LONG).show()
                }
                getString(R.string.mqtt_password), getString(R.string.mqtt_username), getString(R.string.mqtt_port)->
                     {
                    val contextView = findViewById<View>(android.R.id.content)
                    Snackbar.make(contextView, "You need to restart the server for applying the config changes", Snackbar.LENGTH_LONG)
                            .show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "onSharedPreferenceChanged: $e", )
        }
    }
    private fun initSharedPrefs() {
        AppPreferences.mqttHost = getIPAddress(true)
        AppPreferences.mqttPort="1883"
        AppPreferences.mqttAuthStatus=false
        AppPreferences.mqttUserName="admin"
        AppPreferences.mqttPassword= generatePassword()
        AppPreferences.firstRun=false

    }
    private fun startService() {
        Log.d(TAG, "Starting MQTT Service")
        val serviceIntent = Intent(this, MqttService::class.java)
        if (isMyServiceRunning(this,MqttService::class.java)){
            stopService(serviceIntent)
        }
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun stopService() {
        Log.d(TAG, "Stopping MQTT Service")
        val serviceIntent = Intent(this, MqttService::class.java)
        stopService(serviceIntent)
    }
    private fun setIP() {
        AppPreferences. mqttHost = getIPAddress(true)
    }


}
