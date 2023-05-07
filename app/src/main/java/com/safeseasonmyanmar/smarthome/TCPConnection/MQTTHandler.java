package com.safeseasonmyanmar.smarthome.TCPConnection;

import org.eclipse.paho.android.service.MqttAndroidClient;

public class MQTTHandler {
    private static MqttAndroidClient mqttAndroidClient;

    public static synchronized MqttAndroidClient getClient(){
        return mqttAndroidClient;
    }

    public static synchronized void setClient(MqttAndroidClient mqttAndroidClient){
        MQTTHandler.mqttAndroidClient = mqttAndroidClient;
    }
}
