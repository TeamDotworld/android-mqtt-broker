package in.naveens.mqttbroker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;


import in.naveens.mqttbroker.service.MqttService;

public class MainActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        boolean firstBoot = sharedPref.getBoolean(getString(R.string.isFirst), true);
        if (firstBoot) {
            Log.d(TAG, "Initializing Shared Preferences");
            initSharedPrefs();
        }
        setIP();
        Log.d(TAG, "Logging Preference Listener");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();

    }

    private void initSharedPrefs() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.isFirst), false);
        editor.commit();


        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(this).edit();
        prefs.putString(getString(R.string.mqtt_host), Utils.getIPAddress(true));
        prefs.putString(getString(R.string.mqtt_port), "1883");
        prefs.putBoolean(getString(R.string.mqtt_auth_status), false);
        prefs.putString(getString(R.string.mqtt_username), "admin");
        prefs.putString(getString(R.string.mqtt_password), Utils.generatePassword());
        prefs.commit();
    }


    private void setIP() {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(this).edit();
        prefs.putString(getString(R.string.mqtt_host), Utils.getIPAddress(true));
        prefs.apply();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "Preference changed. " + key);
        try {
            switch (key) {
                case "mqtt_broker_status":
                    Log.d(TAG, "Server status changed");
                    boolean status = sharedPreferences.getBoolean(key, false);
                    Log.d(TAG, "Start Server?" + status);
                    if (status) {
                        Log.d(TAG, "Starting Server");
                        startService();
                    } else {
                        Log.d(TAG, "Stopping Server");
                        stopService();
                    }
                    break;
                case "mqtt_auth_status":
                    Log.d(TAG, "Restarting mqtt service");
                    new Thread(() -> {
                        stopService();
                        startService();
                    }).start();
                    Toast.makeText(this, "MQTT broker restarted with updated config", Toast.LENGTH_LONG).show();
                    break;
                case "mqtt_password":
                case "mqtt_username":
                case "mqtt_port1":
                    View contextView = findViewById(android.R.id.content);
                    Snackbar.make(contextView, "You need to restart the server for applying the config changes", Snackbar.LENGTH_LONG)
                            .show();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startService() {
        Log.d(TAG, "Starting MQTT Service");
        Intent serviceIntent = new Intent(this, MqttService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void stopService() {
        Log.d(TAG, "Stopping MQTT Service");
        Intent serviceIntent = new Intent(this, MqttService.class);
        stopService(serviceIntent);
    }
}
