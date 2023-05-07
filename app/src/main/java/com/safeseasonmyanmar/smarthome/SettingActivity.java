package com.safeseasonmyanmar.smarthome;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.safeseasonmyanmar.smarthome.setting.AboutUsMenu;
import com.safeseasonmyanmar.smarthome.setting.AccountMenu;
import com.safeseasonmyanmar.smarthome.setting.ConnectionMenu;
import com.safeseasonmyanmar.smarthome.setting.DeviceRegisterMenu;
import com.safeseasonmyanmar.smarthome.setting.DiagnosticTestMenu;
import com.safeseasonmyanmar.smarthome.setting.FactoryDefaultMenu;
import com.safeseasonmyanmar.smarthome.setting.GeneralMenu;
import com.safeseasonmyanmar.smarthome.setting.LoadImageMenu;
import com.safeseasonmyanmar.smarthome.setting.ModuleRegisterMenu;
import com.safeseasonmyanmar.smarthome.setting.ServerSettingMenu;
import com.safeseasonmyanmar.smarthome.setting.SettingTextView;
import com.safeseasonmyanmar.smarthome.setting.SetupWizardMenu;
import com.safeseasonmyanmar.smarthome.setting.SynchronizationMenu;
import com.safeseasonmyanmar.smarthome.setting.UpdateMenu;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class SettingActivity extends AppCompatActivity implements SettingTextView {

    private Window mContentView;
    TextView subText, subPreviousText;
    ImageView backMenu;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        //Set resources
        mContentView = getWindow();
        subText = findViewById(R.id.subSettingText);
        subPreviousText = findViewById(R.id.subPreviousText);
        backMenu = findViewById(R.id.backMenu);

        //Blur image
        float radius = 5f;
        BlurView view = findViewById(R.id.blurView);
        View decorView = getWindow().getDecorView();
        ViewGroup rootView = decorView.findViewById(android.R.id.content);
        Drawable windowBackground = decorView.getBackground();

        view.setupWith(rootView)
                .setFrameClearDrawable(windowBackground)
                .setBlurAlgorithm(new RenderScriptBlur(this))
                .setBlurRadius(radius)
                .setHasFixedTransformationMatrix(true);

        backMenu.setOnClickListener(v -> onBackPressed());
    }

    private void hideSystemUI(){
        WindowInsetsControllerCompat windowInsetsController =
                ViewCompat.getWindowInsetsController(mContentView.getDecorView());
        if (windowInsetsController == null) {
            return;
        }
        // Configure the behavior of the hidden system bars
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        // Hide both the status bar and the navigation bar
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0){
            fm.popBackStack();
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public void passDataFromFragment(String string) {
        if (string.equals("Setting")){
            subPreviousText.setVisibility(View.INVISIBLE);
            subText.setText("Setting");
        }else{
            subPreviousText.setVisibility(View.VISIBLE);
            subPreviousText.setText("Setting");
            subText.setText(string);
        }
    }

    @Override
    public void passFragmentIndex(Integer integer) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        switch (integer){
            case 1:
                ConnectionMenu connectionMenu = new ConnectionMenu();
                transaction.replace(R.id.settingFragment,connectionMenu, "menuItems");
                break;
            case 2:
                ServerSettingMenu serverSettingMenu = new ServerSettingMenu();
                transaction.replace(R.id.settingFragment,serverSettingMenu,"menuItems");
                break;
            case 3:
                SynchronizationMenu synchronizationMenu = new SynchronizationMenu();
                transaction.replace(R.id.settingFragment,synchronizationMenu,"menuItems");
                break;
            case 4:
                SetupWizardMenu setupWizardMenu = new SetupWizardMenu();
                transaction.replace(R.id.settingFragment,setupWizardMenu,"menuItems");
                break;
            case 5:
                LoadImageMenu loadImageMenu = new LoadImageMenu();
                transaction.replace(R.id.settingFragment,loadImageMenu,"menuItems");
                break;
            case 6:
                DeviceRegisterMenu deviceRegisterMenu = new DeviceRegisterMenu();
                transaction.replace(R.id.settingFragment,deviceRegisterMenu,"menuItems");
                break;
            case 7:
                ModuleRegisterMenu moduleRegisterMenu = new ModuleRegisterMenu();
                transaction.replace(R.id.settingFragment,moduleRegisterMenu,"menuItems");
                break;
            case 8:
                GeneralMenu generalMenu = new GeneralMenu();
                transaction.replace(R.id.settingFragment,generalMenu,"menuItems");
                break;
            case 9:
                DiagnosticTestMenu diagnosticTestMenu = new DiagnosticTestMenu();
                transaction.replace(R.id.settingFragment,diagnosticTestMenu,"menuItems");
                break;
            case 10:
                FactoryDefaultMenu factoryDefaultMenu = new FactoryDefaultMenu();
                transaction.replace(R.id.settingFragment,factoryDefaultMenu,"menuItems");
                break;
            case 11:
                UpdateMenu updateMenu = new UpdateMenu();
                transaction.replace(R.id.settingFragment,updateMenu,"menuItems");
                break;
            case 12:
                AboutUsMenu aboutUsMenu = new AboutUsMenu();
                transaction.replace(R.id.settingFragment,aboutUsMenu,"menuItems");
                break;
            case 20:
                AccountMenu accountMenu = new AccountMenu();
                transaction.replace(R.id.settingFragment, accountMenu,"menuItems");
                break;
            default:
                return;
        }
        transaction.addToBackStack(null);
        transaction.setReorderingAllowed(true);
        transaction.commit();
    }
}