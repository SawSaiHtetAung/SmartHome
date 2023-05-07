package com.safeseasonmyanmar.smarthome.subdisplay;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.safeseasonmyanmar.smarthome.R;
import com.safeseasonmyanmar.smarthome.ui.LogsIndication;

import java.util.ArrayList;

public class LogDisplayAdaptor extends BaseAdapter {
    LayoutInflater mLayoutInflater;

    ArrayList<String> timeTitle;
    ArrayList<String>  detailText;
    ArrayList<Integer> indication;


    public LogDisplayAdaptor(Context context, ArrayList<String> timeTitle, ArrayList<String> detailText, ArrayList<Integer> indication) {
        mLayoutInflater = LayoutInflater.from(context);
        this.timeTitle = timeTitle;
        this.detailText = detailText;
        this.indication = indication;
    }

    @Override
    public int getCount() {
        return timeTitle.size();
    }

    @Override
    public Object getItem(int position) {
        return timeTitle.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row;
        LogListViewHolder logListViewHolder;
        if (convertView == null){
            row = mLayoutInflater.inflate(R.layout.logview_adaptor, null);
            logListViewHolder = new LogListViewHolder();

            logListViewHolder.tagTimeTitle = row.findViewById(R.id.tagTimeLog);
            logListViewHolder.tagDetailText = row.findViewById(R.id.tagDetailLog);
            logListViewHolder.view = row.findViewById(R.id.graphIndication);

            row.setTag(logListViewHolder);
        } else {
            row = convertView;
            logListViewHolder = (LogListViewHolder) row.getTag();
        }

        logListViewHolder.tagTimeTitle.setText(timeTitle.get(position));
        logListViewHolder.tagDetailText.setText(detailText.get(position));
        logListViewHolder.view.setCircleColor(indication.get(position));

        return row;
    }
}

class LogListViewHolder{
    TextView tagTimeTitle;
    TextView tagDetailText;
    LogsIndication view;
}
