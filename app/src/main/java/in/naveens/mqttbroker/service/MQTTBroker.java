package in.naveens.mqttbroker.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.util.Properties;
import java.util.concurrent.Callable;

import io.moquette.broker.Server;

public class MQTTBroker implements Callable<Boolean> {

    private static final String TAG = "MQTTBrokerThread";

    private Context context;
    private static Server server;
    private Properties config;

    private SharedPreferences sharedPreferences;

    public MQTTBroker(Context context, Properties config) {
        this.config = config;
        this.context = context;
    }

    public static Server getServer() {
        return server;
    }

    public void stopServer() {
        server.stopServer();
    }

    @Override
    public Boolean call() throws Exception {

        try {
            server = ServerInstance.getServerInstance();
            server.startServer(config);
            Log.d(TAG, "MQTT Broker Started");
            return true;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            throw new Exception(e.getLocalizedMessage());
        }
    }
}
