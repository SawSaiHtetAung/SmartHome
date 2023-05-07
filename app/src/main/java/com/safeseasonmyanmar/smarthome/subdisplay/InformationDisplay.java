package com.safeseasonmyanmar.smarthome.subdisplay;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.safeseasonmyanmar.smarthome.R;

import java.util.ArrayList;

public class InformationDisplay extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private String mParam1;
    private String mParam2;

    //Adding logsView graphic
    final int COLOR_WANING = Color.parseColor("#ffc107");
    final int COLOR_ERROR = Color.parseColor("#FF4348");
    final int COLOR_INFORMATION = Color.parseColor("#02a9f4");
    final int COLOR_AUTOMATION= Color.parseColor("#80C884");

    //Set resource
    private LogDisplayAdaptor logDisplayAdaptor;
    ArrayList<String> listTitle = new ArrayList<>();
    ArrayList<String> listDetail = new ArrayList<>();
    ArrayList<Integer> indication = new ArrayList<>();

    public InformationDisplay() {
        // Required empty public constructor
    }

    public static InformationDisplay newInstance(String param1, String param2) {
        InformationDisplay fragment = new InformationDisplay();
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
        View view = inflater.inflate(R.layout.fragment_information_display, container, false);

        //Set default test
        listTitle.add("Today, 8:20 PM");
        listDetail.add("Home theatre mode id ON");
        indication.add(COLOR_AUTOMATION);

        listTitle.add("Today, 6:30 PM");
        listDetail.add("locker room light is activated, but there is no activity within 30 minute." +
                " Suggest to turn off light");
        indication.add(COLOR_WANING);

        listTitle.add("Today, 5:18 PM");
        listDetail.add("Living room Air-Com temperature is set to 26°C");
        indication.add(COLOR_ERROR);


        //Set the log list adaptor
        ListView listView = view.findViewById(R.id.climateListsView);
        listView.setDivider(null);
        logDisplayAdaptor = new LogDisplayAdaptor(inflater.getContext(), listTitle, listDetail, indication);
        listView.setAdapter(logDisplayAdaptor);

        return view;
    }
}