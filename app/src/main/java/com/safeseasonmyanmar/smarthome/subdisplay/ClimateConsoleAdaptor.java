package com.safeseasonmyanmar.smarthome.subdisplay;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.safeseasonmyanmar.smarthome.R;

import java.util.ArrayList;

public class ClimateConsoleAdaptor extends BaseAdapter {

    LayoutInflater layoutInflater;
    ArrayList<Float> temperature ;
    ArrayList<Float> humidity;
    ArrayList<Float> airQuality;

    public ClimateConsoleAdaptor(Context mContext, ArrayList<Float> getTemperature, ArrayList<Float> getHumidity,
                                 ArrayList<Float> getAirQuality){
        this.layoutInflater = LayoutInflater.from(mContext);
        this.temperature = getTemperature;
        this.humidity = getHumidity;
        this.airQuality = getAirQuality;
    }

    @Override
    public int getCount() {
        return temperature.size();
    }

    @Override
    public Object getItem(int position) {
        return temperature.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row;
        ClimateConsoleViewHolder viewHolder;
        if (convertView == null){
            row = layoutInflater.inflate(R.layout.climateconsole_adaptor, null);
            viewHolder = new ClimateConsoleViewHolder();
            //ToDo child view tag
            viewHolder.temperatureValue = row.findViewById(R.id.climateTempValue);
            viewHolder.humidityValue    = row.findViewById(R.id.climateHumiValue);
            viewHolder.airQualityValue  = row.findViewById(R.id.climateAQIValue);

            row.setTag(viewHolder);
        } else {
            row = convertView;
            viewHolder = (ClimateConsoleViewHolder) row.getTag();
        }
        //ToDo child view tagged display
        if (temperature.get(position) == 0)
            viewHolder.temperatureValue.setText("__._ \u2103");
        else
            viewHolder.temperatureValue.setText(temperature.get(position) + " \u2103");

        if (humidity.get(position) == 0)
            viewHolder.humidityValue.setText("--.- %");
        else
            viewHolder.humidityValue.setText(humidity.get(position) + " %");

        if (airQuality.get(position) == 0)
            viewHolder.airQualityValue.setText("--.- PPM");
        else
            viewHolder.airQualityValue.setText(airQuality.get(position) + " PPM");

        return row;
    }
}

class ClimateConsoleViewHolder{
    TextView temperatureValue;
    TextView humidityValue;
    TextView airQualityValue;
}
