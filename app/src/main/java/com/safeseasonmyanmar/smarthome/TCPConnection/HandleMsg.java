package com.safeseasonmyanmar.smarthome.TCPConnection;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.safeseasonmyanmar.smarthome.DataProcessing;


public class HandleMsg extends Handler {

    private Context mContext;
    private String TAG = "Handler";

    public HandleMsg(Context context) {
        this.mContext = context;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        switch (msg.what){
            //This function is notified to all data is ready
            case DataProcessing.CONNECTED:
                Log.d(TAG, "handleMessage: Server is connected");
                Intent intent = new Intent("DataReady");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                break;
            //Notified that serial data in incoming
            case DataProcessing.NOTIFIED_DATA_INCOMING:
                //ToDo notified data message
                break;
        }
    }
}
