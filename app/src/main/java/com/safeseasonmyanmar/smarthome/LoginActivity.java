package com.safeseasonmyanmar.smarthome;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.safeseasonmyanmar.smarthome.TCPConnection.ConnectionTask;
import com.safeseasonmyanmar.smarthome.TCPConnection.MQTTConnectionTask;
import com.safeseasonmyanmar.smarthome.TCPConnection.MQTTHandler;
import com.safeseasonmyanmar.smarthome.TCPConnection.SocketHandler;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private View mContentView;
    Button bConnect;
    EditText server, email, password;
    DataProcessing processing;
    ArrayList<Byte> inputData = new ArrayList<>();
    ConnectionTask connectionTask;
    MQTTConnectionTask mqttConnectionTask;
    private ProgressDialog dialog;
    private SharedPreferences.Editor loginPrefsEditor;

    @Override
    protected void onPostResume() {
        super.onPostResume();
        hideSystemUI();
        LocalBroadcastManager.getInstance(this).registerReceiver(loginReceiver,
                new IntentFilter("DataNotification"));
        LocalBroadcastManager.getInstance(this).registerReceiver(dataReady,
                new IntentFilter("DataReady"));
        LocalBroadcastManager.getInstance(this).registerReceiver(serverStatus,
                new IntentFilter("ServerStatus"));
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus)
            hideSystemUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(loginReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(dataReady);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(serverStatus);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //Force white mode
        mContentView = findViewById(R.id.login_fullscreen);

        //Permit for network binding thread
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //Set resources
        processing = new DataProcessing();
        bConnect = findViewById(R.id.loginButton);
        server   = findViewById(R.id.inputServer);
        email    = findViewById(R.id.inputEmail);
        password = findViewById(R.id.inputPassword);

        SharedPreferences loginPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        //Set preference data to server edit text box
        server.setText(loginPreferences.getString("serverIP", ""));

        bConnect.setOnClickListener(view -> {
            String ipAddress = server.getText().toString();    //Set IP address for server
            int    port;         //Set port for server

            if (server.getText().toString().equals("DEMO")){
                DataProcessing.inDemoMode = true;
                Intent main = new Intent(this, MainActivity.class);
                startActivity(main);
                LoginActivity.this.finish();
            }

            loginPrefsEditor.putString("serverIP", ipAddress);
            loginPrefsEditor.apply();

            dialog = ProgressDialog.show(LoginActivity.this, "", "Connecting to server", true);

            //Try to extract server ip address and port
            if (ipAddress.toUpperCase(Locale.ROOT).startsWith("ID")){
                DataProcessing.lenMode = false;
                ipAddress = "broker.hivemq.com";
                port = 1883;
            } else {
                DataProcessing.lenMode = true;
                try {
                    URL url = new URL("http://" + ipAddress);
                    ipAddress = url.getHost();
                    port = url.getPort();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Invalid format", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            //Data collecting
            inputData.clear();
            byte[] md5Array;
            String passwordHash = password.getText().toString();

            try {
                java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
                md5Array = md.digest(passwordHash.getBytes(StandardCharsets.UTF_8));
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return;
            }

            //User data loading
            String username = email.getText().toString();
            byte user = (byte) 0xFE;
            if (!username.matches("")){
                if (TextUtils.isDigitsOnly(username))
                    user = (byte) (Integer.parseInt(username) & 0xFF);
            }

            DataProcessing.userID = user;
            if (md5Array.length == 16){
                inputData.add((user));
                for(int i=0; i<16; i++)
                    inputData.add(md5Array[i]);
            }

            //Make connection
            if (port == -1)  //If port not defined set to default value
                port = 8899;
            if (DataProcessing.lenMode) {
                connectionTask = new ConnectionTask(view.getContext(), ipAddress, port);
                connectionTask.execute();
            } else {
                mqttConnectionTask = new MQTTConnectionTask(view.getContext(), ipAddress, port);
                mqttConnectionTask.execute();
            }
        });
    }

    //Wait for receiving message from device
    BroadcastReceiver loginReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long TSID;
            TSID = intent.getLongExtra("loginCode", 0);
            if (TSID > 4096 && TSID < 65280){
                dialog.dismiss();
                DataProcessing.loginCode = TSID;
                Intent main = new Intent(context, MainActivity.class);
                startActivity(main);
                LoginActivity.this.finish();
            } else {
                switch ((int) TSID){
                    case 49:
                        email.setError("User have already LINK!");
                        break;
                    case 17:
                        email.setError("User ID not found!");
                        break;
                    case 33:
                        password.setError("Incorrect password!");
                        break;
                }

                //Set to initial state
                if (DataProcessing.lenMode) {
                    Socket socket = SocketHandler.getSocket();
                    if (socket != null) {
                        if (socket.isConnected()) {
                            try {
                                socket.close();
                                socket = null;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    SocketHandler.setSocket(socket);
                    connectionTask = null;
                } else {
                    MqttAndroidClient client = MQTTHandler.getClient();
                    if (client != null){
                        if (client.isConnected()){
                            try {
                                client.disconnect();
                            } catch (MqttException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    MQTTHandler.setClient(client);
                    mqttConnectionTask = null;
                }
            }
        }
    };

    BroadcastReceiver dataReady = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Byte[] rawData = new Byte[inputData.size()];

            for (int i=0; i<inputData.size(); i++)
                rawData[i] = inputData.get(i);

            processing.serialSend(context,255, DataProcessing.FUNC_LOGIN, 0, rawData);
        }
    };

    BroadcastReceiver serverStatus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int reason;
            reason = intent.getIntExtra("Reason", 99);

            switch (reason){
                case DataProcessing.NOT_CONNECT_SERVER:
                    dialog.setMessage("Server not found");
                    dismissDiag();
                    break;
                case DataProcessing.SERVER_DISCONNECTED:
                    dialog.setMessage("Disconnected from server");
                    dismissDiag();
                    break;
                case DataProcessing.SEND_DATA:
                    dialog.setMessage("Sending data to server");
                    break;
                case DataProcessing.SEND_FAIL:
                    dialog.setMessage("Error sending message");
                    dismissDiag();
                    break;
                default:
                    dismissDiag();
                    break;
            }

        }
    };

    private void dismissDiag(){
        final Handler handler = new Handler();
        if (dialog != null)
            handler.postDelayed(() -> dialog.dismiss(), 2000);
    }

    private void hideSystemUI(){
        if (Build.VERSION.SDK_INT < 30){
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        }
    }
}