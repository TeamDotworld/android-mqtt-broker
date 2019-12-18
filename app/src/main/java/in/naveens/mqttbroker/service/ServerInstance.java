package in.naveens.mqttbroker.service;

import android.util.Log;

import io.moquette.broker.Server;

public class ServerInstance {
    public static final String TAG = "ServerInstance";
    private static final Object INSTANCE_LOCK = new Object();
    private static Server serverInstance = null;


    public static Server getServerInstance() {
        try {
            if (serverInstance == null) {
                synchronized (INSTANCE_LOCK) {
                    if (serverInstance == null) {
                        serverInstance = new Server();
                        Server server = serverInstance;
                        return server;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return serverInstance;
    }
}
