package com.safeseasonmyanmar.smarthome.TCPConnection;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.safeseasonmyanmar.smarthome.DataProcessing;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MQTTConnectionTask extends AsyncTask<Boolean, byte[], Integer> {
    Context context;
    String serverAddress;
    int port;
    MqttAndroidClient client;
    String tag = "MQTT Connection";
    HandleMsg handleMsg;
    byte[] lData = new byte[40];

    public MQTTConnectionTask(Context context, String serverAddress, int port) {
        this.context = context;
        this.serverAddress = serverAddress;
        this.port = port;
        this.client = new MqttAndroidClient(context,"tcp://" + serverAddress + ":" + port,
                DataProcessing.clientID);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        handleMsg = new HandleMsg(context);
    }

    @Override
    protected Integer doInBackground(Boolean... booleans) {
        try {
            Log.v(tag,"Connecting to :" + client.getServerURI());
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.v(tag,"On success connection");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(tag,"On failure connection");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
            return DataProcessing.NOT_CONNECT_SERVER;
        }

        while (!client.isConnected());

        Message message = new Message();
        message.what    = DataProcessing.CONNECTED;
        handleMsg.sendMessageDelayed(message, 1000);

        try {
            IMqttToken subToken = client.subscribe(DataProcessing.subscribeID, 0);
            MQTTHandler.setClient(client);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.v(tag,"On success subscribe");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.v(tag,"On failure subscribe");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
            return DataProcessing.NOT_CONNECT_SERVER;
        }

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                onProgressUpdate(mqttMessage.getPayload());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });

        while (client.isConnected());
        return DataProcessing.SERVER_DISCONNECTED;
    }

    @Override
    protected void onProgressUpdate(byte[]... values) {
        super.onProgressUpdate(values);

        System.arraycopy(values[0], 0, lData, 0, values[0].length);

        if (lData[0] == DataProcessing.inHeader){
            DataProcessing.func         = lData[1];
            DataProcessing.TSID_MSB     = lData[2];
            DataProcessing.TSID_LSB     = lData[3];
            DataProcessing.dataLen      = lData[4];
            System.arraycopy(lData,5,DataProcessing.inData,0,DataProcessing.dataLen);
            DataProcessing.checkSum_MSB = lData[6+DataProcessing.dataLen];
            DataProcessing.checkSum_LSB = lData[7+DataProcessing.dataLen];

            //ToDo serial data incoming notification
            long TSID = (lData[3] & 0xFF) | ((lData[2] & 0xFF) << 8);
            Intent intent = new Intent("DataNotification");
            intent.putExtra("loginCode", TSID);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        Intent intent = new Intent("ServerStatus");
        intent.putExtra("Reason", integer);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
