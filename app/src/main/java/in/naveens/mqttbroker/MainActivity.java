package in.naveens.mqttbroker;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.log4j.chainsaw.Main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import in.naveens.mqttbroker.model.MqttSettings;
import in.naveens.mqttbroker.service.MqttService;
import io.moquette.BrokerConstants;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int WRITE_EXT_STORAGE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        boolean isFirst = sharedPref.getBoolean(getString(R.string.isFirst), true);
        if (isFirst) {
            Log.d(TAG, "Initializing Shared Preferences");
            initSharedPrefs();
        }

        Log.d(TAG, "Logging Preference Listener");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();

        this.checkStorage();
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
        prefs.putString(getString(R.string.mqtt_password), "admin");
        prefs.commit();

    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        Log.d(TAG, "Permission Granted");
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            Toast.makeText(this, "Ahaaaaan", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @AfterPermissionGranted(WRITE_EXT_STORAGE)
    private void checkStorage() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            File file = Environment.getExternalStorageDirectory();
            if (file.isDirectory()) {
                Log.d(TAG, "Path is a directory");
            } else {
                Log.d(TAG, "Path is a file");
            }
        } else {
            EasyPermissions.requestPermissions(this, "App requires storage permission for starting broker",
                    WRITE_EXT_STORAGE, perms);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "Preference changed. " + key);
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
