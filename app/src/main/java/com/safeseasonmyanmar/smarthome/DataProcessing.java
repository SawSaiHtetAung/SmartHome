package com.safeseasonmyanmar.smarthome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.safeseasonmyanmar.smarthome.TCPConnection.MQTTHandler;
import com.safeseasonmyanmar.smarthome.TCPConnection.SocketHandler;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class DataProcessing {

    public DataProcessing() {
    }

    //Unpackage information
    public static final byte inHeader = 0x16;
    public static byte userID;
    public static byte func;
    public static byte TSID_MSB;
    public static byte TSID_LSB;
    public static byte dataLen;
    public static byte[] inData = new byte[32];
    public static byte checkSum_MSB;
    public static byte checkSum_LSB;
    public static long loginCode;

    //Message handler what
    public static final int NOTIFIED_DATA_INCOMING  = 201;
    public static final int CONNECTED               = 101;
    public static final int NOT_CONNECT_SERVER      = 219;
    public static final int SERVER_DISCONNECTED     = 229;
    public static final int SEND_DATA               = 231;
    public static final int SEND_FAIL               = 232;

    //Declare function
    public static final byte FUNC_LOGIN             = 0x03;
    public static final byte FUNC_PING              = 0x06;
    public static final byte FUNC_CUR_TIME          = 0x08;
    public static final byte FUNC_SETLIGHT_CMD      = 0x11;
    public static final byte FUNC_SETLIGHT_LOC      = 0x12;
    public static final byte FUNC_GETLIGHT_NO       = 0x20;
    public static final byte FUNC_GETLIGHT_CMD      = 0X21;
    public static final byte FUNC_GETENV_NO         = 0x40;
    public static final byte FUNC_GETTEMP_CUR       = 0x41;
    public static final byte FUNC_GETHUMI_CUR       = 0x43;
    public static final byte FUNC_GETAQI_CUR        = 0x45;

    //For MQTT connection
    public static final String clientID = "ID12345";
    public static final String subscribeID = "CDC10226";
    public static final String publicID  = "NDC10226";

    //Temporary buffer data
    public static boolean lenMode = true;
    public static boolean inDemoMode = false;
    public static final byte onSuccess  = 0x55;
    public static final byte onFail     = (byte) 0xAA;

    //For Lighting
    public static int numberOfLights    = 0;
    public static int numberOfSwitches  = 0;
    public static int numberOfENV       = 0;

    //Data handler
    private Runnable run;
    Handler handler = new Handler();
    private int retrieval;
    private volatile boolean isActivated = false;


    public void serialSend(Context mComtext, int userID, Byte func, long TSID, Byte[] data){
        ArrayList<Byte> iData = new ArrayList<>();
        byte[] bytes  = longToBytes(TSID);
        int length = data.length;
        Socket socket = null;
        MqttAndroidClient client = null;
        if (lenMode) {
            socket = SocketHandler.getSocket();
            if (socket == null)
                return;
        } else {
            client = MQTTHandler.getClient();
            if (client == null)
                return;
        }
        retrieval = 0;

        if ( length <= 0 || length > 32)
            return;

        //Register broadcast receiver
        LocalBroadcastManager.getInstance(mComtext).registerReceiver(receiver,
                new IntentFilter("DataNotification"));
        while (isActivated);

        //Create message to broadcast receiver
        Intent intent = new Intent("ServerStatus");
        intent.putExtra("Reason", SEND_DATA);
        LocalBroadcastManager.getInstance(mComtext).sendBroadcast(intent);

        //Packaging
        iData.add(inHeader);
        iData.add((byte) userID);
        iData.add(func);
        iData.add(bytes[6]);
        iData.add(bytes[7]);
        iData.add((byte) length);
        iData.addAll(Arrays.asList(data).subList(0, length));
        short checkSum = CalculateCheckSum(iData);
        iData.add((byte) (checkSum & 0xFF));
        iData.add((byte) ((checkSum >> 8) & 0xFF));

        //Send data to server
        int size = iData.size();
        byte[] sendData = new byte[size];

        for (int i=0; i<size; i++)
            sendData[i] = iData.get(i);

        //Before sending command to device, make sure clear interrupt
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (handler.hasCallbacks(run))
                handler.removeCallbacks(run);
        }
        if (lenMode) {
            lenSendData(mComtext, socket, sendData);
        } else {
            mqttSendData(mComtext, client, sendData);
        }
    }

    /**
     * This function will post
     * @param socket
     * @param sendData
     */
    private void lenSendData(Context context, Socket socket, byte[] sendData){
        run = new Runnable() {
            @Override
            public void run() {
                isActivated = true;
                if (retrieval < 3)
                    handler.postDelayed(this, 8000L *(retrieval+1));
                else{
                    //Create message to broadcast receiver
                    Intent intent = new Intent("ServerStatus");
                    intent.putExtra("Reason", SEND_FAIL);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                }
                retrieval++;

                try {
                    DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                    outputStream.write(sendData);
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        handler.postDelayed(run, 500);
    }

    private void mqttSendData(Context context, MqttAndroidClient client, byte[] sendData){
        run = new Runnable() {
            @Override
            public void run() {
                isActivated = true;
                if (retrieval < 3)
                    handler.postDelayed(this, 8000L *(retrieval+1));
                else{
                    //Create message to broadcast receiver
                    Intent intent = new Intent("ServerStatus");
                    intent.putExtra("Reason", SEND_FAIL);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                }
                retrieval++;

                MqttMessage message = new MqttMessage(sendData);
                try {
                    client.publish(DataProcessing.publicID, message);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        };
        handler.postDelayed(run, 500);
    }

    BroadcastReceiver receiver =  new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            isActivated = false;
            handler.removeCallbacks(run);
            LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
        }
    };

    /**
     *
     * @param value
     * @return
     */
    public byte[] longToBytes(long value){
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(value);
        return buffer.array();
    }

    /**
     *
     * @param data
     * @return
     */
    short CalculateCheckSum(ArrayList<Byte> data){
        short checkSum = 0;
        for (int i=0; i < data.size(); i++)
            checkSum += (short) data.get(i) & 0xFF;
        return checkSum;
    }
}
