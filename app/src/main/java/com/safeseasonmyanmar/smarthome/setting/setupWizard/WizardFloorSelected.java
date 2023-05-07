package com.safeseasonmyanmar.smarthome.setting.setupWizard;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.safeseasonmyanmar.smarthome.R;

import java.util.ArrayList;


public class WizardFloorSelected extends Fragment implements UpdateFloorSelected{

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    //Set resources
    TextView floorInd;
    WizardFloorSelectedAdaptor adaptor;
    public static int  levelSelected = 1;
    static ArrayList<Integer> selectedLevelID = new ArrayList<>();

    public WizardFloorSelected() {
        // Required empty public constructor
    }

    public static WizardFloorSelected newInstance(String param1, String param2) {
        WizardFloorSelected fragment = new WizardFloorSelected();
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
        View view = inflater.inflate(R.layout.fragment_wizard_floor_selected, container, false);

        ListView listView = view.findViewById(R.id.floorsList);
        ImageView increase = view.findViewById(R.id.floorIncrease);
        ImageView decrease = view.findViewById(R.id.floorDecrease);
        floorInd = view.findViewById(R.id.floorInd);
        ArrayAdapter<CharSequence> adapterList = ArrayAdapter.createFromResource(getContext(),
                R.array.floorSelected, R.layout.spinner_item);

        //Set list view adaptor
        adaptor = new WizardFloorSelectedAdaptor(getContext(),adapterList);
        adaptor.setButtonListener(this);
        listView.setAdapter(adaptor);
        floorInd.setText(""+levelSelected);

        //Set default value
        if (selectedLevelID.isEmpty())
            selectedLevelID.add(0);

        increase.setOnClickListener(v -> {
            levelSelected++;
            if (levelSelected > 6)
                levelSelected = 6;
            else {
                selectedLevelID.add(0);
            }
            adapterList.notifyDataSetChanged();
            floorInd.setText(""+levelSelected);
        });

        decrease.setOnClickListener(v -> {
            levelSelected--;
            if (levelSelected < 1)
                levelSelected = 1;
            else {
                selectedLevelID.remove(0);
            }
            adapterList.notifyDataSetChanged();
            floorInd.setText(""+levelSelected);
        });

        return view;
    }

    @Override
    public void onActionClickListener(int position, int id) {
        selectedLevelID.set(position,id);
    }

    public static boolean dataValidationCheck(){
        for (int i=0; i<selectedLevelID.size(); i++){
            for (int n=0; n< selectedLevelID.size(); n++){
                if (selectedLevelID.get(i) == selectedLevelID.get(n) && i != n)
                    return false;
            }
        }
        return true;
    }
}

interface UpdateFloorSelected{
    void onActionClickListener(int position, int id);
}