package com.safeseasonmyanmar.smarthome.subdisplay;

import static java.lang.Thread.sleep;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.safeseasonmyanmar.smarthome.DataProcessing;
import com.safeseasonmyanmar.smarthome.R;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class ClimateConsole extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    ArrayList<Float> temperature    = new ArrayList<>();
    ArrayList<Float> humidity       = new ArrayList<>();
    ArrayList<Float> airQuality     = new ArrayList<>();
    private String tag = "climateConsole";

    //Data processing
    ClimateConsoleAdaptor adaptor;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Future runningTask;
    private final Handler getClimateStatus = new Handler();
    private Runnable setSchedule;
    DataProcessing processing;

    //Climate processing
    private int numberOfENVSensor;
    private final Byte[] outData = new Byte[1];
    private int curPosition = 0;

    public ClimateConsole() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!DataProcessing.inDemoMode){
            runningTask = executorService.submit(postProcessing);
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(climateReceiver,
                    new IntentFilter("DataNotification"));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!DataProcessing.inDemoMode){
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(climateReceiver);
            runningTask.cancel(true);
            getClimateStatus.removeCallbacks(setSchedule);
        }
    }

    public static ClimateConsole newInstance(String param1, String param2) {
        ClimateConsole fragment = new ClimateConsole();
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
        View view = inflater.inflate(R.layout.fragment_climate_console, container, false);
        processing = new DataProcessing();

        if (DataProcessing.inDemoMode){
            //Todo DEMO mode display
            temperature.add((float) 36.6);
            humidity.add((float) 61.3);
            airQuality.add((float) 0.12);
        } else {
            numberOfENVSensor = DataProcessing.numberOfENV;
            setScheduleRun();
        }

        //Define resource
        ListView listView = view.findViewById(R.id.climateListsView);
        adaptor = new ClimateConsoleAdaptor(inflater.getContext(),temperature,humidity,airQuality);
        listView.setAdapter(adaptor);

        return view;
    }

    private final Runnable postProcessing = new Runnable() {
        @Override
        public void run() {
            if (runningTask.isCancelled())
                return;
            if (temperature.size() >= numberOfENVSensor)
                return;
            for (int i=0; i<numberOfENVSensor; i++){
                temperature.add(0.f);
                humidity.add(0.f);
                airQuality.add(0.f);
            }
            adaptor.notifyDataSetChanged();
        }
    };

    private void setScheduleRun(){
        setSchedule = () -> {
            Toast.makeText(requireContext(), "Getting climate status", Toast.LENGTH_SHORT).show();
            //Todo lighting status command
            curPosition = getClimateCMD(requireContext(), numberOfENVSensor-1);
        };
        getClimateStatus.postDelayed(setSchedule, 2000);
    }

    private int getClimateCMD(Context mContext, int position) {
        //ToDo lighting send code here
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            outData[0] = (byte) position;
            processing.serialSend(mContext, DataProcessing.userID, DataProcessing.FUNC_GETTEMP_CUR,
                    DataProcessing.loginCode, outData);
        }, 300);
        return (position-1);
    }

    private void sendClimateCMD(Context mContext, Byte function, Byte[] rData){
        final Handler handler = new Handler();
        handler.postDelayed(() ->
                processing.serialSend(mContext, DataProcessing.userID, function, DataProcessing.loginCode, rData), 2000);
    }

    public BroadcastReceiver climateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int function = DataProcessing.func;

            switch (function){
                case DataProcessing.FUNC_GETTEMP_CUR:
                    //ToDo get temperature function
                    int temperatureID = DataProcessing.inData[0] & 0xFF;
                    if (temperatureID <= numberOfENVSensor){
                        long holding = DataProcessing.inData[1] & 0xFF;
                        holding = (holding << 8) | (DataProcessing.inData[2] & 0xFF);
                        if (holding > 0x8000)
                            temperature.set(temperatureID, (holding & 0x7FFF) / 10.0f);
                        else
                            temperature.set(temperatureID, holding / 10.0f);

                        outData[0] = (byte) (curPosition + 1);
                        sendClimateCMD(context, DataProcessing.FUNC_GETHUMI_CUR, outData);
                    }
                    break;
                case DataProcessing.FUNC_GETHUMI_CUR:
                    //ToDo get humidity function
                    int humidityID = DataProcessing.inData[0] & 0xFF;
                    if (humidityID <= numberOfENVSensor){
                        long holding = DataProcessing.inData[1] & 0xFF;
                        holding = (holding << 8) | (DataProcessing.inData[2] & 0xFF);
                        humidity.set(humidityID, holding / 10.0f);

                        outData[0] = (byte) (curPosition + 1);
                        sendClimateCMD(context, DataProcessing.FUNC_GETAQI_CUR, outData);
                    }
                    break;
                case DataProcessing.FUNC_GETAQI_CUR:
                    //ToDo get air quantity
                    int aqiID = DataProcessing.inData[0] & 0xFF;
                    if (aqiID <= numberOfENVSensor){
                        long holding = DataProcessing.inData[1] & 0xFF;
                        holding = (holding << 8) | (DataProcessing.inData[2] & 0xFF);
                        airQuality.set(aqiID, holding / 10.0f);
                    }
                    if (curPosition < 0){
                        adaptor.notifyDataSetChanged();
                        getClimateStatus.postDelayed(setSchedule, 60000);
                    } else {
                        curPosition = getClimateCMD(context, curPosition);
                    }
                    Log.v(tag,"Incoming ID " + aqiID);
                    break;
                default:
                    Toast.makeText(context, "Unknown function " + function, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}