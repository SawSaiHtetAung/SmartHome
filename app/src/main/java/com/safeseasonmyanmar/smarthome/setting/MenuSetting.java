package com.safeseasonmyanmar.smarthome.setting;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.safeseasonmyanmar.smarthome.DataProcessing;
import com.safeseasonmyanmar.smarthome.R;

import java.util.Objects;

public class MenuSetting extends Fragment implements View.OnClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //Set Resource
    SettingTextView textView, index;

    //Set resources
    ConstraintLayout accountMenu;
    TextView comMenu, serverMenu, syncMenu, setupMenu, imgMenu,dRegMenu, mRegMenu, genMenu, diagMenu
            , resetMenu, updateMenu, aboutMenu;
    TextView[] button = new TextView[12];

    int buttonID = 0, tempButtonID = 20;;

    public MenuSetting() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        textView.passDataFromFragment("Setting");
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            textView = (SettingTextView) getActivity();
            index = (SettingTextView) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + "need implement");
        }
    }

    public static MenuSetting newInstance(String param1, String param2) {
        MenuSetting fragment = new MenuSetting();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_menu_setting, container, false);

        accountMenu = view.findViewById(R.id.accountMenu);
        button[0]  = comMenu    = view.findViewById(R.id.comMenu);
        button[1]  = serverMenu = view.findViewById(R.id.serverMenu);
        button[2]  = syncMenu   = view.findViewById(R.id.syncMenu);
        button[3]  = setupMenu  = view.findViewById(R.id.setupMenu);
        button[4]  = imgMenu    = view.findViewById(R.id.bkImageMenu);
        button[5]  = dRegMenu   = view.findViewById(R.id.divRegMenu);
        button[6]  = mRegMenu   = view.findViewById(R.id.modRegMenu);
        button[7]  = genMenu    = view.findViewById(R.id.genMenu);
        button[8]  = diagMenu   = view.findViewById(R.id.diagnosticMenu);
        button[9]  = resetMenu  = view.findViewById(R.id.factoryDefaultMenu);
        button[10] = updateMenu = view.findViewById(R.id.updateMenu);
        button[11] = aboutMenu  = view.findViewById(R.id.aboutMenu);

        //Set click listener
        for (int i=0;i<12;i++)
            button[i].setOnClickListener(this);
        accountMenu.setOnClickListener(this);

        return view;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.comMenu:
                buttonID = 1;
                break;
            case R.id.serverMenu:
                buttonID = 2;
                break;
            case R.id.syncMenu:
                buttonID = 3;
                break;
            case R.id.setupMenu:
                buttonID = 4;
                break;
            case R.id.bkImageMenu:
                buttonID = 5;
                break;
            case R.id.divRegMenu:
                buttonID = 6;
                break;
            case R.id.modRegMenu:
                buttonID = 7;
                break;
            case R.id.genMenu:
                buttonID = 8;
                break;
            case R.id.diagnosticMenu:
                buttonID = 9;
                break;
            case R.id.factoryDefaultMenu:
                buttonID = 10;
                break;
            case R.id.updateMenu:
                buttonID = 11;
                break;
            case R.id.aboutMenu:
                buttonID = 12;
                break;
            case R.id.accountMenu:
                buttonID = 20;
                break;
            default:
                buttonID = 99;
                break;
        }

        if (buttonID == 20){
            textView.passDataFromFragment(requireActivity().getResources().getStringArray(R.array.menuString)[12]);
        } else if (buttonID <= 12){
            textView.passDataFromFragment(requireActivity().getResources().getStringArray(R.array.menuString)[buttonID-1]);
        }
        index.passFragmentIndex(buttonID);
    }
}