package com.safeseasonmyanmar.smarthome.subdisplay;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.slider.Slider;
import com.safeseasonmyanmar.smarthome.R;

import java.util.ArrayList;

public class LightConsoleAdaptor extends BaseAdapter {

    //Set display resources
    LayoutInflater mLayoutInflater;
    ArrayList<String> lightTitle;
    ArrayList<String> lightValue;
    ArrayList<Boolean> status;
    ArrayList<Integer> dimming;
    UpdateLighting mUpdateLighting;

    public LightConsoleAdaptor(Context mContext,ArrayList<String> lightTitle,ArrayList<String> lightValue,
                               ArrayList<Boolean> status, ArrayList<Integer> dim){
        this.mLayoutInflater    = LayoutInflater.from(mContext);
        this.lightTitle         = lightTitle;
        this.lightValue         = lightValue;
        this.status             = status;
        this.dimming            = dim;
    }

    public void setLightingClickListener(UpdateLighting updateLighting){
        this.mUpdateLighting = updateLighting;
    }

    @Override
    public int getCount() {
        return lightTitle.size();
    }

    @Override
    public Object getItem(int position) {
        return lightTitle.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row;
        LightConsoleViewHolder viewHolder;
        if (convertView == null){
            row = mLayoutInflater.inflate(R.layout.lightconsole_adaptor, null);
            viewHolder = new LightConsoleViewHolder();
            viewHolder.title = row.findViewById(R.id.lightConsoleTitle);
            viewHolder.value = row.findViewById(R.id.lightConsoleValue);
            viewHolder.dimmerSlide = row.findViewById(R.id.lightConsoleSlider);
            viewHolder.lightBtn = row.findViewById(R.id.lightConsoleBtn);

            row.setTag(viewHolder);
        } else {
            row = convertView;
            viewHolder = (LightConsoleViewHolder) row.getTag();
        }

        viewHolder.title.setText(lightTitle.get(position));
        viewHolder.value.setText(lightValue.get(position));
        if (status.get(position)){
            viewHolder.lightBtn.setImageResource(R.drawable.ico_lighton);
        } else {
            viewHolder.lightBtn.setImageResource(R.drawable.ico_light_off);
        }
        viewHolder.lightBtn.setOnClickListener(v -> {
            if (mUpdateLighting != null){
                mUpdateLighting.onLightingClickListener(!status.get(position),dimming.get(position), position);
            }
        });
        viewHolder.dimmerSlide.setValue(dimming.get(position));
        viewHolder.dimmerSlide.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                if (mUpdateLighting != null){
                    status.set(position, true);
                    mUpdateLighting.onLightingClickListener(status.get(position), (int) slider.getValue(), position);
                }
                viewHolder.value.setText("Sending data.....");
            }
        });
        viewHolder.dimmerSlide.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser)
                viewHolder.value.setText("Setting up to " + (int) value + "%");
        });
        return row;
    }
}

class LightConsoleViewHolder{
    TextView title;
    TextView value;
    ImageView lightBtn;
    Slider dimmerSlide;
}
