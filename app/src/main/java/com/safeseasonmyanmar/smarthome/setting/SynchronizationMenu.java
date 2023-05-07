package com.safeseasonmyanmar.smarthome.setting;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.safeseasonmyanmar.smarthome.DataProcessing;
import com.safeseasonmyanmar.smarthome.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SynchronizationMenu#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SynchronizationMenu extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    ArrayAdapter<String> adapter;
    private ArrayList<String> title = new ArrayList<>();
    private final Byte[] outData = {(byte) 0xFF};
    DataProcessing processing;
    Runnable runnable;
    Handler handler = new Handler();
    private int holdingNumberOfLights;
    private int holdingNumberOfSwitches;
    private int holdingNumberOfENV;

    public SynchronizationMenu() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SynchronizationMenu.
     */
    // TODO: Rename and change types and number of parameters
    public static SynchronizationMenu newInstance(String param1, String param2) {
        SynchronizationMenu fragment = new SynchronizationMenu();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(syncReceiver, new IntentFilter("DataNotification"));
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(syncReceiver);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Data save information")
                .setMessage("All data will be appeared on next time \nDo you want to use now?")
                .setPositiveButton("OK", (dialog, which) -> {

                    //Set temporary holding value to Data Processing value
                    DataProcessing.numberOfLights   = holdingNumberOfLights;
                    DataProcessing.numberOfSwitches = holdingNumberOfSwitches;
                    DataProcessing.numberOfENV      = holdingNumberOfENV;

                })
                .setNegativeButton("NO", (dialog, which) -> {
                })
                .show();
        super.onPause();
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
        View view = inflater.inflate(R.layout.fragment_synchronization_menu, container, false);

        Button onSync = view.findViewById(R.id.syncButton);
        ListView list = view.findViewById(R.id.syncListView);
        processing = new DataProcessing();
        adapter = new ArrayAdapter<>(view.getContext(),R.layout.spinner_item,title);
        list.setAdapter(adapter);

        onSync.setOnClickListener(vButton -> {
            if(DataProcessing.inDemoMode){
                //ToDo in demo mode data processing
            } else {
                //Getting time from device
                Byte[] rData = new Byte[7];
                String[] ids = TimeZone.getAvailableIDs(23400000);
                if (ids.length == 0)
                    System.exit(0);
                SimpleTimeZone pdt = new SimpleTimeZone(23400000, ids[0]);
                Calendar calendar = new GregorianCalendar(pdt);

                rData[0] = (byte) (calendar.get(Calendar.SECOND) + 2);
                rData[1] = (byte) calendar.get(Calendar.MINUTE);
                rData[2] = (byte) calendar.get(Calendar.HOUR);
                rData[3] = (byte) calendar.get(Calendar.DATE);
                rData[4] = (byte) (calendar.get(Calendar.MONTH) + 1);
                int year = calendar.get(Calendar.YEAR);
                rData[5] = (byte) (year - 2000);
                rData[6] = (byte) calendar.get(Calendar.DAY_OF_WEEK);

                sendSerialData(vButton.getContext(), DataProcessing.userID, DataProcessing.FUNC_CUR_TIME, DataProcessing.loginCode, rData);
                title.add("Time synchronization start");
                adapter.notifyDataSetChanged();
            }
        });
        return view;
    }

    public BroadcastReceiver syncReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (DataProcessing.func){
                case DataProcessing.FUNC_CUR_TIME:
                    if (DataProcessing.inData[0] == DataProcessing.onSuccess)
                        title.add("Time synchronization is succeed");
                    else if (DataProcessing.inData[0] == DataProcessing.onFail)
                        title.add("Time module is error");
                    else
                        title.add("Unknown Error!");
                    sendSerialData(context, DataProcessing.userID, DataProcessing.FUNC_GETLIGHT_NO, DataProcessing.loginCode, outData);
                    title.add("Getting lighting data from device");
                    break;
                case DataProcessing.FUNC_GETLIGHT_NO:
                    title.add("" + DataProcessing.inData[0] + " numbers of lighting found");
                    title.add("" + DataProcessing.inData[1] + " numbers of switch found");
                    holdingNumberOfLights   = DataProcessing.inData[0]; //Temporary holding for lights
                    holdingNumberOfSwitches = DataProcessing.inData[1]; //Temporary holding for switches
                    adapter.notifyDataSetChanged();
                    sendSerialData(context,DataProcessing.userID, DataProcessing.FUNC_GETENV_NO, DataProcessing.loginCode, outData);
                    title.add("Getting environment sensor data from device");
                    adapter.notifyDataSetChanged();
                    break;
                case DataProcessing.FUNC_GETENV_NO:
                    title.add("" + DataProcessing.inData[0] + " numbers of environment sensor found");
                    holdingNumberOfENV = DataProcessing.inData[0];  //Temporary holding for ENV
                    adapter.notifyDataSetChanged();

                    //Write synchronous value to local storage
                    SharedPreferences storage = getActivity().getSharedPreferences("preSyncValue", Context.MODE_PRIVATE);
                    SharedPreferences.Editor storageEditor = storage.edit();
                    storageEditor.putInt("numberOfLights", holdingNumberOfLights);
                    storageEditor.putInt("numberOfSwitches", holdingNumberOfSwitches);
                    storageEditor.putInt("numberOfENV", holdingNumberOfENV);
                    storageEditor.apply();
                    break;
            }
        }
    };

    void sendSerialData(Context mComtext, int userID, Byte func, long TSID, Byte[] data){
        runnable = () -> processing.serialSend(mComtext,userID,func,TSID,data);
        handler.postDelayed(runnable, 2000);
    }
}