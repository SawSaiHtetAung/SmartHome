package com.safeseasonmyanmar.smarthome.TCPConnection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.safeseasonmyanmar.smarthome.DataProcessing;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class ConnectionTask extends AsyncTask<Boolean, Byte, Integer> {
    //Server connection information
    @SuppressLint("StaticFieldLeak")
    private final Context mContext;
    Socket socket;
    String  serverAddress;
    int     port;
    int     dCount = 0, lResult;
    byte[]  lData = new byte[40];
    HandleMsg handleMsg;

    public ConnectionTask(Context mContext, String serverAddress, int port) {
        this.mContext       = mContext;
        this.serverAddress  = serverAddress;
        this.port           = port;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        handleMsg = new HandleMsg(mContext);
    }

    @Override
    protected Integer doInBackground(Boolean... booleans) {
        //Start connection
        if (socket == null)
            socket = new Socket();

        SocketAddress remoteAddress = new InetSocketAddress(serverAddress, port);
        try {
            socket.connect(remoteAddress);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Connection", "Can't connect to server");
            return DataProcessing.NOT_CONNECT_SERVER;
        }

        Log.v("Connection", "Connected to server");
        Message message = new Message();
        message.what    = DataProcessing.CONNECTED;
        handleMsg.sendMessageDelayed(message, 1000);
        SocketHandler.setSocket(socket);

        //Waiting incoming message
        try {
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            byte[] rData = new byte[100];
            do{
                lResult = inputStream.read(rData);      //Read data from socket
                for(int i=0; i<lResult; i++)
                    publishProgress(rData[i]);
            } while (lResult != -1);                    //If socket is disconnected, exit loop
        } catch (IOException e) {
            e.printStackTrace();
        }
        return DataProcessing.SERVER_DISCONNECTED;      //If connection is terminate
    }

    @Override
    protected void onProgressUpdate(Byte... values) {
        super.onProgressUpdate(values);

        if(dCount < lResult){                           //Write to buffer
            lData[dCount] = values[0];
            dCount++;
        }

        if (dCount == lResult){
            dCount = 0; //Set to initial state
            if (lData[0] == DataProcessing.inHeader) {
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
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            } else {
                Log.e("Serial Data", "Unknown serial data");
            }
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        Intent intent = new Intent("ServerStatus");
        intent.putExtra("Reason", integer);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }
}
