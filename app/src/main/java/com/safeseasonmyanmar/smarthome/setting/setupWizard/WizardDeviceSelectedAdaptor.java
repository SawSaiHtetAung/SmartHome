package com.safeseasonmyanmar.smarthome.setting.setupWizard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.safeseasonmyanmar.smarthome.R;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;

public class WizardDeviceSelectedAdaptor extends BaseAdapter {
    LayoutInflater mLayoutInflater;
    ArrayList<String> title;
    ArrayAdapter<CharSequence> floorAdaptor;
    ArrayAdapter<CharSequence> zoneAdaptor;
    boolean[] checkedStates;

    public WizardDeviceSelectedAdaptor(Context context, ArrayList<String> title, ArrayAdapter<CharSequence> floor,
                                       ArrayAdapter<CharSequence> zone) {
        mLayoutInflater = LayoutInflater.from(context);
        this.title = title;
        this.floorAdaptor = floor;
        this.zoneAdaptor = zone;
        checkedStates = new boolean[title.size()];
    }

    @Override
    public int getCount() {
        return title.size();
    }

    @Override
    public Object getItem(int position) {
        return title.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row;
        final DeviceListViewHolder listViewHolder;
        if (convertView == null){
            row = mLayoutInflater.inflate(R.layout.wizard_device_adaptor, null);
            listViewHolder = new DeviceListViewHolder();
            listViewHolder.title = row.findViewById(R.id.deviceListTitle);
            listViewHolder.checkBox = row.findViewById(R.id.deviceListSelect);
            listViewHolder.floor = row.findViewById(R.id.wizardFloorList);
            listViewHolder.zone = row.findViewById(R.id.wizardZoneList);
            listViewHolder.layout = row.findViewById(R.id.wizardZoneSelected);

            row.setTag(listViewHolder);
        } else {
            row = convertView;
            listViewHolder = (DeviceListViewHolder) row.getTag();
        }

        listViewHolder.layout.setExpanded(checkedStates[position]);
        listViewHolder.floor.setAdapter(floorAdaptor);
        listViewHolder.zone.setAdapter(zoneAdaptor);
        listViewHolder.title.setText(title.get(position));
        listViewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkedStates[position] = !checkedStates[position];
                listViewHolder.layout.setExpanded(checkedStates[position]);
            }
        });

        listViewHolder.checkBox.setChecked(checkedStates[position]);

        return row;
    }

    static class DeviceListViewHolder{
        TextView title;
        MaterialCheckBox checkBox;
        Spinner floor, zone;
        ExpandableLayout layout;
    }
}
