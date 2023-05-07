package com.safeseasonmyanmar.smarthome.setting;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;
import com.safeseasonmyanmar.smarthome.R;
import com.safeseasonmyanmar.smarthome.setting.setupWizard.WizardDeviceSelected;
import com.safeseasonmyanmar.smarthome.setting.setupWizard.WizardFloorSelected;
import com.safeseasonmyanmar.smarthome.setting.setupWizard.WizardSync;
import com.safeseasonmyanmar.smarthome.setting.setupWizard.WizardWelcome;

public class SetupWizardMenu extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //Set resources
    MaterialButton next, cancel;

    //Data processing
    public int BACKSTACK_COUNT     = 0;

    public SetupWizardMenu() {
        // Required empty public constructor
    }

    public static SetupWizardMenu newInstance(String param1, String param2) {
        SetupWizardMenu fragment = new SetupWizardMenu();
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
        View view = inflater.inflate(R.layout.fragment_setup_wizard_menu, container, false);

        //Set resources
        next = view.findViewById(R.id.wizardAgreed);
        cancel = view.findViewById(R.id.wizardCancel);

        next.setOnClickListener(v -> {
            BACKSTACK_COUNT++;

            if (BACKSTACK_COUNT > 3)
                BACKSTACK_COUNT = 3;
            setupWizard();
        });

        cancel.setOnClickListener(v -> {
            BACKSTACK_COUNT--;
            if (BACKSTACK_COUNT < 1) {
                cancel.setText("CANCEL");
                next.setText("AGREED");
                BACKSTACK_COUNT = 0;
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.replace(R.id.wizardFragment, new WizardWelcome(), "Wizard").commitAllowingStateLoss();
            } else
                setupWizard();
        });

        return view;
    }

    private void setupWizard() {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

        switch (BACKSTACK_COUNT){
            case 1:
                WizardFloorSelected fragment = new WizardFloorSelected();
                transaction.replace(R.id.wizardFragment, fragment, "Wizard");
                transaction.commitAllowingStateLoss();
                next.setText("NEXT");
                break;
            case 2:
                if (WizardFloorSelected.dataValidationCheck()){
                    WizardDeviceSelected deviceFragment = new WizardDeviceSelected();
                    transaction.replace(R.id.wizardFragment, deviceFragment, "wizard");
                    transaction.commitAllowingStateLoss();
                } else
                    BACKSTACK_COUNT--;
                break;
            case 3:
                WizardSync zoneSelected = new WizardSync();
                transaction.replace(R.id.wizardFragment, zoneSelected, "wizard");
                transaction.commitAllowingStateLoss();
                break;
        }
        cancel.setText("BACK");
    }
}