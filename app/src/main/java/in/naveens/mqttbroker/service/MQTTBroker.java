package in.naveens.mqttbroker.service;

import android.util.Log;

import java.io.IOException;
import java.net.BindException;
import java.util.Properties;
import java.util.concurrent.Callable;

import io.moquette.broker.Server;

public class MQTTBroker implements Callable<Boolean> {

    private static final String TAG = MQTTBroker.class.getName();

    private static Server server;
    private Properties config;

    public MQTTBroker(Properties config) {
        this.config = config;
    }

    public static Server getServer() {
        return server;
    }

    public void stopServer() {
        server.stopServer();
    }

    @Override
    public Boolean call() throws BindException {

        try {
            server = ServerInstance.getServerInstance();
            server.startServer(config);
            Log.d(TAG, "MQTT Broker Started");
            return true;
        } catch (BindException e) {
            Log.e(TAG, "Address already in use. Unable to bind.");
            Log.e(TAG, "Error : " + e.getMessage());
            throw new BindException(e.getLocalizedMessage());
        } catch (IOException e) {
            Log.e(TAG, "Error : " + e.getMessage());
        }
        return false;
    }
}
