package com.safeseasonmyanmar.smarthome.subdisplay;

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
import java.util.BitSet;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class LightConsole extends Fragment implements UpdateLighting{

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    ArrayList<String> lightTitle    = new ArrayList<>();
    ArrayList<String> lightValue    = new ArrayList<>();
    ArrayList<Integer> dimming      = new ArrayList<>();
    ArrayList<Boolean> status       = new ArrayList<>();
    ArrayList<Boolean> dimmingStatus= new ArrayList<>();
    private String tag = "lightConsole";
    private Boolean holdingStatus;
    private int holdingDimming;
    private int holdingLightID;

    //Data processing
    LightConsoleAdaptor adaptor;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Future runningTask;
    private final Handler getLightStatus = new Handler();
    private Runnable setSchedule;
    DataProcessing processing;

    //Lighting processing
    private int numberOfLights;
    private final Byte[] outData = new Byte[1];
    private int curPosition = 0;

    public LightConsole() {
        // Required empty public constructor
    }

    public static LightConsole newInstance(String param1, String param2) {
        LightConsole fragment = new LightConsole();
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
    public void onResume() {
        super.onResume();
        //If demo mode is off
        if (!DataProcessing.inDemoMode) {
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(lightReceiver,
                    new IntentFilter("DataNotification"));
            runningTask = executorService.submit(postProcessing);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(lightReceiver);
        runningTask.cancel(true);
        getLightStatus.removeCallbacks(setSchedule);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_light_console, container, false);
        processing = new DataProcessing();

        if (DataProcessing.inDemoMode){
            //Todo DEMO mode display
        } else {
            numberOfLights = DataProcessing.numberOfLights;
            setScheduleRun();
        }

        ListView listView = view.findViewById(R.id.lightListsView);
        adaptor = new LightConsoleAdaptor(inflater.getContext(),lightTitle,lightValue,status,dimming);
        adaptor.setLightingClickListener(this);
        listView.setAdapter(adaptor);

        return view;
    }

    private final Runnable postProcessing = () -> {
        if (runningTask.isCancelled())
            return;
        if (status.size() >= numberOfLights)
            return;

        for (int i = 0; i< numberOfLights; i++){
            lightTitle.add("Loading Module.......");
            lightValue.add("Loading.......");
            status.add(false);
            dimmingStatus.add(false);
            dimming.add(0);
        }
        adaptor.notifyDataSetChanged();
    };

    private void setScheduleRun(){
        setSchedule = () -> {
            Toast.makeText(requireContext(), "Getting lighting status", Toast.LENGTH_SHORT).show();
            //Todo lighting status command
            curPosition = getLightingCMD(requireContext(),numberOfLights-1);
        };
        getLightStatus.postDelayed(setSchedule, 2000); //Initial time wait 2s to send command
    }

    private int getLightingCMD(Context mContext,int position) {
        //ToDo lighting send code here
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            outData[0] = (byte) position;
            processing.serialSend(mContext, DataProcessing.userID, DataProcessing.FUNC_GETLIGHT_CMD,
                    DataProcessing.loginCode, outData);
        }, 300);
        return (position-1);
    }

    public BroadcastReceiver lightReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int function = DataProcessing.func;

            switch (function){
                case DataProcessing.FUNC_GETLIGHT_CMD:
                    //ToDo get lighting function
                    BitSet getLight;
                    getLight = convert(DataProcessing.inData[1]);
                    int lightID = DataProcessing.inData[0] & 0xFF;
                    if (lightID <= numberOfLights){
                        dimmingStatus.set(lightID, getLight.get(6));
                        status.set(lightID, getLight.get(7));
                        dimming.set(lightID, (DataProcessing.inData[1] & 0x3E) * 2);
                    }
                    if (curPosition < 0){
                        updateLighting();
                        getLightStatus.postDelayed(setSchedule, 60000);
                    } else {
                        curPosition = getLightingCMD(requireContext(),curPosition);
                    }
                    Log.v(tag,"Incoming ID " + lightID);
                    break;
                case DataProcessing.FUNC_SETLIGHT_CMD:
                    //ToDo upDate lighting command
                    if (DataProcessing.inData[0] == DataProcessing.onSuccess){
                        status.set(holdingLightID, holdingStatus);
                        dimming.set(holdingLightID, holdingDimming);
                        updateLighting();
                    } else {
                        Toast.makeText(context, "Didn't response from device! retrying", Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    Toast.makeText(context, "Unknown function " + function, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    /**
     * This function set the lighting status on User Interface
     */
    private void updateLighting(){
        Log.v(tag,"Start updating lighting status");
        for(int i=0; i<numberOfLights; i++){
            lightTitle.set(i, "Ground Floor. Lighting No."+(i+1));
            lightValue.set(i, "Dimming percent is "+ dimming.get(i) +"%");
        }
        adaptor.notifyDataSetChanged();
    }


    //Long to BitSet convert
    public static BitSet convert(long value){
        BitSet bits = new BitSet(16);
        int index = 0;
        while (value != 0L){
            if (value % 2L != 0)
                bits.set(index);
            ++index;
            value = value >>> 1;
        }
        return bits;
    }

    //Bitset to Long convert
    public static long convert(BitSet bits){
        long value = 0L;
        for (int i=0; i < bits.length(); ++i)
            value += bits.get(i) ? (1L << i) : 0L;
        return value;
    }

    @Override
    public void onLightingClickListener(Boolean lightStatus, int percent, int lightID) {
        Byte rData[] = new Byte[2];
        int bytePercent;
        rData[0] = (byte) (lightID & 0xFF);
        if (lightStatus)
            rData[1] = (byte) 0b11000000;
        else
            rData[1] = (byte) 0b01000000;
        bytePercent = percent/2;
        rData[1] = (byte) (rData[1] | bytePercent);
        processing.serialSend(requireContext(),DataProcessing.userID,
                DataProcessing.FUNC_SETLIGHT_CMD, DataProcessing.loginCode, rData);

        //Temporary holding data for lighting status
        holdingStatus = lightStatus;
        holdingLightID = lightID;
        holdingDimming = percent;
    }
}

interface UpdateLighting{
    void onLightingClickListener(Boolean lightStatus, int percent, int lightID);
}