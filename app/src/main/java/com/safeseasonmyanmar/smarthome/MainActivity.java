package com.safeseasonmyanmar.smarthome;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;
import com.safeseasonmyanmar.smarthome.subdisplay.ClimateConsole;
import com.safeseasonmyanmar.smarthome.subdisplay.DeviceConsole;
import com.safeseasonmyanmar.smarthome.subdisplay.EnergyConsole;
import com.safeseasonmyanmar.smarthome.subdisplay.InformationDisplay;
import com.safeseasonmyanmar.smarthome.subdisplay.LightConsole;
import com.safeseasonmyanmar.smarthome.subdisplay.SecurityConsole;

import static com.safeseasonmyanmar.smarthome.R.color.secondary_background;
import static com.safeseasonmyanmar.smarthome.R.color.white;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private View mContentView;
    MaterialCardView securityBT,climateBT,lightBT,deviceBT,energyBT;
    ImageView setting, exit;

    MaterialCardView[] button = new MaterialCardView[5];
    int buttonID = 6, tempButtonID = 6;
    boolean checkDouble = false;

    //Data handling
    private final Handler setCheckSystem = new Handler();
    private Runnable checkSystemRun;
    DataProcessing dataProcessing;

    @Override
    protected void onPostResume() {
        super.onPostResume();
        hideSystemUI();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus)
            hideSystemUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mainReceiver, new IntentFilter("DataNotification"));
        checkSystem();
    }

    @Override
    protected void onPause() {
        super.onPause();
        setCheckSystem.removeCallbacks(checkSystemRun);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mainReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContentView = findViewById(R.id.fullscreen_content);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        dataProcessing = new DataProcessing();

        //Declare resources
        button[0] = securityBT  = findViewById(R.id.securityBtn);
        button[1] = climateBT   = findViewById(R.id.climateBtn);
        button[2] = lightBT     = findViewById(R.id.lightBtn);
        button[3] = deviceBT    = findViewById(R.id.deviceBtn);
        button[4] = energyBT    = findViewById(R.id.energyBtn);
        setting = findViewById(R.id.settingBtn);
        exit    = findViewById(R.id.exitBtn);

        //Assign click identifier
        securityBT.setOnClickListener(this);
        climateBT.setOnClickListener(this);
        lightBT.setOnClickListener(this);
        deviceBT.setOnClickListener(this);
        energyBT.setOnClickListener(this);

        setting.setOnClickListener(view -> {
            view.startAnimation(new AlphaAnimation(1f, 0.4f));
            Intent setting = new Intent(view.getContext(), SettingActivity.class);
            startActivity(setting);
        });

        exit.setOnClickListener(view -> {
            view.startAnimation(new AlphaAnimation(1f, 0.4f));
            //ToDo the application finished
        });

        getStoragePreference();
    }

    private void hideSystemUI(){
        if (Build.VERSION.SDK_INT < 30){
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        } else {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.securityBtn:
                buttonID = 1;
                break;
            case R.id.climateBtn:
                buttonID = 2;
                break;
            case R.id.lightBtn:
                buttonID = 3;
                break;
            case R.id.deviceBtn:
                buttonID = 4;
                break;
            case R.id.energyBtn:
                buttonID = 5;
                break;
            default:
                buttonID = 9;
                break;
        }

        //Test which button need to transit
        if (tempButtonID == buttonID && !checkDouble){
            checkDouble = true;

            //ToDo button default state code here
            for (int i= 0; i<5; i++) {
                setButtonLayout(button[i],false,view);
            }
            tempButtonID = 6;
            fragmentTransaction(6);
            return;
        } else {
            checkDouble = false;

            //ToDo button transition code here
            for (int i=1; i<6; i++){
                setButtonLayout(button[i-1], buttonID == i,view);
            }
            tempButtonID = buttonID;
        }
        fragmentTransaction(buttonID);
    }

    private void setButtonLayout(MaterialCardView cardView,boolean setButton,View getView){
        float pixels = getView.getContext().getResources().getDisplayMetrics().density;
        if (setButton){
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (100 * pixels + 0.5f));
            params.gravity = Gravity.END;
            params.topMargin = (int) (10 * pixels + 0.5f);
            cardView.setLayoutParams(params);
            cardView.setBackground(ContextCompat.getDrawable(getView.getContext(), secondary_background));
        } else {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int) (100 * pixels + 0.5f));
            params.gravity = Gravity.END;
            params.topMargin = (int) (10 * pixels + 0.5f);
            cardView.setLayoutParams(params);
            cardView.setBackground(ContextCompat.getDrawable(getView.getContext(), white));
        }
    }

    private void fragmentTransaction(int layerIndex){
        if (layerIndex < 1 || layerIndex > 6)
            return;

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment prevMain = getSupportFragmentManager().findFragmentByTag("consoleFragmentLayer");
        if (prevMain != null)
            transaction.remove(prevMain);

        switch (layerIndex){
            case 1:
                SecurityConsole securityConsole = new SecurityConsole();
                transaction.replace(R.id.consoleFragment, securityConsole, "consoleFragmentLayer");
                break;
            case 2:
                ClimateConsole climateConsole = new ClimateConsole();
                transaction.replace(R.id.consoleFragment, climateConsole,"consoleFragmentLayer");
                break;
            case 3:
                LightConsole lightConsole = new LightConsole();
                transaction.replace(R.id.consoleFragment, lightConsole,"consoleFragmentLayer");
                break;
            case 4:
                DeviceConsole deviceConsole = new DeviceConsole();
                transaction.replace(R.id.consoleFragment, deviceConsole,"consoleFragmentLayer");
                break;
            case 5:
                EnergyConsole energyConsole = new EnergyConsole();
                transaction.replace(R.id.consoleFragment, energyConsole, "consoleFragmentLayer");
                break;
            case 6:
                InformationDisplay informationConsole = new InformationDisplay();
                transaction.replace(R.id.consoleFragment, informationConsole, "consoleFragmentLayer");
                break;
            default:
                return;
        }
        transaction.setReorderingAllowed(true);
        transaction.commitNow();
    }

    /**
     * This function will set the pre-value from local storage
     */
    private void getStoragePreference(){
        SharedPreferences registerStorage = getSharedPreferences("preSyncValue", MODE_PRIVATE);

        DataProcessing.numberOfLights   = registerStorage.getInt("numberOfLights", 0);
        DataProcessing.numberOfSwitches = registerStorage.getInt("numberOfSwitches", 0);
        DataProcessing.numberOfENV      = registerStorage.getInt("numberOfENV", 0);
    }

    private void checkSystem(){
        checkSystemRun = new Runnable() {
            @Override
            public void run() {
                Byte[] lData = {(byte) 0xFF};
                setCheckSystem.postDelayed(this, 45000);
                dataProcessing.serialSend(getBaseContext(),DataProcessing.userID,DataProcessing.FUNC_PING,
                        DataProcessing.loginCode,lData);
            }
        };
        setCheckSystem.postDelayed(checkSystemRun, 2000);
    }

    public BroadcastReceiver mainReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int function = DataProcessing.func;

            switch (function){
                case DataProcessing.FUNC_PING:
                    if (DataProcessing.inData[0] == DataProcessing.onSuccess)
                        Toast.makeText(context, "Ping successfully reply", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };
}