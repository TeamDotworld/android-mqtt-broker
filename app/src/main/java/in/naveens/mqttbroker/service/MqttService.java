package in.naveens.mqttbroker.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import org.apache.log4j.chainsaw.Main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import in.naveens.mqttbroker.MainActivity;
import in.naveens.mqttbroker.R;
import in.naveens.mqttbroker.Utils;
import io.moquette.BrokerConstants;

public class MqttService extends Service {

    private static final String TAG = MqttService.class.getSimpleName();
    public static final String CHANNEL_ID = "MQTTBrokerNotificationChannel";

    SharedPreferences sharedPreferences;
    MQTTBroker mqttBroker;
    private Thread thread;

    public MqttService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Received start id " + startId + " : " + intent);

        startForeground(1, getNotification());
        try {
            updateIP();
        } catch (Exception e) {
            Log.e(TAG, "Error : " + e.getMessage());
        }
        try {
            Properties props = getConfig();
            mqttBroker = new MQTTBroker(props);
            FutureTask<Boolean> futureTask = new FutureTask<>(mqttBroker);
            if (thread == null || !thread.isAlive()) {
                thread = new Thread(futureTask);
                thread.setName("MQTT Server");
                thread.start();
                if (futureTask.get()) {
                    Toast.makeText(this, "MQTT Broker Service started", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Unable to start MQTT Broker", Toast.LENGTH_LONG).show();
                }
            }
        } catch (ExecutionException e) {
            Log.e(TAG, "Error : " + e.getMessage());
            Toast.makeText(this, "Try using another port. Address already in use", Toast.LENGTH_SHORT).show();
            stopSelf();
            return START_NOT_STICKY;
        } catch (Exception e) {
            e.printStackTrace();
            return START_NOT_STICKY;
        }

        return START_STICKY;

    }

    private Notification getNotification() {
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification =
                new Notification.Builder(this, CHANNEL_ID)
                        .setContentTitle(getText(R.string.notification_title))
                        .setContentText(getText(R.string.notification_message))
                        .setSmallIcon(R.drawable.ic_mqtt_sq)
                        .setContentIntent(pendingIntent)
                        .setTicker(getText(R.string.ticker_text))
                        .build();
        return notification;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    public Properties getConfig() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Properties props = new Properties();
        props.setProperty(BrokerConstants.PORT_PROPERTY_NAME, sharedPreferences.getString(getString(R.string.mqtt_port), "1883"));

        boolean auth = sharedPreferences.getBoolean(getString(R.string.mqtt_auth_status), false);
        props.setProperty(BrokerConstants.NEED_CLIENT_AUTH, String.valueOf(auth));
        if (auth) {
            String username = sharedPreferences.getString(getString(R.string.mqtt_username), "admin");
            String password = sharedPreferences.getString(getString(R.string.mqtt_password), null);
            if (password != null) {
                String sha256hex = org.apache.commons.codec.digest.DigestUtils.sha256Hex(password);
                String filename = "password.conf";
                String fileContents = username + ":" + sha256hex;
                try (FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE)) {
                    fos.write(fileContents.getBytes());
                    File file = new File(getFilesDir(), filename);
                    props.setProperty(BrokerConstants.PASSWORD_FILE_PROPERTY_NAME, file.getAbsolutePath());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "Unable to generate auth file", Toast.LENGTH_SHORT).show();
            }

        }
        props.setProperty(BrokerConstants.HOST_PROPERTY_NAME, Utils.getIPAddress(true));
        props.setProperty(BrokerConstants.WEB_SOCKET_PORT_PROPERTY_NAME, String.valueOf(BrokerConstants.WEBSOCKET_PORT));
        return props;
    }

    protected void updateIP() {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(this).edit();
        prefs.putString(getString(R.string.mqtt_host), Utils.getIPAddress(true));
        prefs.apply();
    }

    @Override
    public void onDestroy() {
        if (thread != null && MQTTBroker.getServer() != null) {
            try {
                Log.d(TAG, "Trying to stop mqtt server");
                mqttBroker.stopServer();
                thread.interrupt();
                Toast.makeText(this, "MQTT Broker Service stopped", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        } else {
            Log.d(TAG, "Server is not running");
        }
        super.onDestroy();
    }
}
