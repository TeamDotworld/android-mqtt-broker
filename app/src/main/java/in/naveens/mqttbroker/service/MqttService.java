package in.naveens.mqttbroker.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import org.apache.log4j.chainsaw.Main;

import java.io.File;
import java.util.Properties;
import java.util.concurrent.FutureTask;

import in.naveens.mqttbroker.MainActivity;
import in.naveens.mqttbroker.R;
import in.naveens.mqttbroker.Utils;
import in.naveens.mqttbroker.model.MqttSettings;
import io.moquette.BrokerConstants;

public class MqttService extends Service {

    private static final String TAG = MqttService.class.getSimpleName();
    public static final String CHANNEL_ID = "MQTTBrokerNotificationChannel";

    IBinder binder;
    SharedPreferences sharedPreferences;
    MQTTBroker mqttBroker;
    private Thread thread;


    public MqttService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Received start id " + startId + " : " + intent);

        startForeground(1, getNotification());

        try {
            mqttBroker = new MQTTBroker(getApplicationContext(), getConfig());
            FutureTask<Boolean> futureTask = new FutureTask<>(mqttBroker);
            if (thread == null || !thread.isAlive()) {
                thread = new Thread(futureTask);
                thread.setName("MQTT Server");
                thread.start();
                if (futureTask.get()) {
                    Toast.makeText(this, "MQTT Broker Service started", Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            stopSelf();
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
                        .setSmallIcon(R.drawable.ic_server)
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
        //props.setProperty(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME, this.getExternalFilesDir(null).getAbsolutePath() + File.separator + BrokerConstants.DEFAULT_MOQUETTE_STORE_H2_DB_FILENAME);
        props.setProperty(BrokerConstants.PORT_PROPERTY_NAME, sharedPreferences.getString(getString(R.string.mqtt_port), "1883"));
        props.setProperty(BrokerConstants.NEED_CLIENT_AUTH, String.valueOf(sharedPreferences.getBoolean(getString(R.string.mqtt_auth_status), false)));
        props.setProperty(BrokerConstants.HOST_PROPERTY_NAME, Utils.getIPAddress(true));
        props.setProperty(BrokerConstants.WEB_SOCKET_PORT_PROPERTY_NAME, String.valueOf(BrokerConstants.WEBSOCKET_PORT));
        return props;
    }

    @Override
    public void onDestroy() {
        try {
            new Thread(() -> {
                MQTTBroker.getServer().stopServer();
                thread.interrupt();
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
