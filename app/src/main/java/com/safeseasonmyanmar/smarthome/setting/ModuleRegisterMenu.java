package com.safeseasonmyanmar.smarthome.setting;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.material.button.MaterialButton;
import com.safeseasonmyanmar.smarthome.R;
import com.safeseasonmyanmar.smarthome.setting.setupWizard.WizardDeviceSelectedAdaptor;

import java.util.ArrayList;

public class ModuleRegisterMenu extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    //Set image resources
    //Set resources
    ListView listView;
    MaterialButton scanBtn;

    ArrayList<String> deviceList = new ArrayList<>();
    ArrayAdapter<CharSequence> floorAdaptor;
    ArrayAdapter<CharSequence> zoneAdaptor;
    WizardDeviceSelectedAdaptor adaptor;

    public ModuleRegisterMenu() {
        // Required empty public constructor
    }

        public static ModuleRegisterMenu newInstance(String param1, String param2) {
        ModuleRegisterMenu fragment = new ModuleRegisterMenu();
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
        View view = inflater.inflate(R.layout.fragment_module_register_menu, container, false);

        listView = view.findViewById(R.id.moduleDeviceList);
        scanBtn = view.findViewById(R.id.moduleSyncButton);

        for (int i=0; i<10; i++){
            deviceList.add("ML-"+i);
        }

        floorAdaptor = ArrayAdapter.createFromResource(getContext(),R.array.floorSelected, R.layout.spinner_item);
        zoneAdaptor = ArrayAdapter.createFromResource(getContext(),R.array.zoneSelected, R.layout.spinner_item);
        adaptor = new WizardDeviceSelectedAdaptor(getContext(),deviceList,floorAdaptor,zoneAdaptor);
        listView.setAdapter(adaptor);
        return view;
    }
}