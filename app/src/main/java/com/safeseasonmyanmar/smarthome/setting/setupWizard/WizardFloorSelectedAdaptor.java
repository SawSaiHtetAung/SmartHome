package com.safeseasonmyanmar.smarthome.setting.setupWizard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Spinner;

import com.safeseasonmyanmar.smarthome.R;

public class WizardFloorSelectedAdaptor extends BaseAdapter {
    LayoutInflater mLayoutInflater;
    ArrayAdapter<CharSequence> level;
    UpdateFloorSelected updateFloorSelected;

    public WizardFloorSelectedAdaptor(Context context, ArrayAdapter<CharSequence> level) {
        mLayoutInflater = LayoutInflater.from(context);
        this.level = level;
    }

    public void setButtonListener(UpdateFloorSelected updateInterface){
        this.updateFloorSelected = updateInterface;
    }

    @Override
    public int getCount() {
        return WizardFloorSelected.levelSelected;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row;
        ListViewHolder listViewHolder;
        if (convertView == null){
            row = mLayoutInflater.inflate(R.layout.wizard_floor_selected_adaptor, null);
            listViewHolder = new ListViewHolder();
            listViewHolder.spinner = row.findViewById(R.id.floorSelectedLists);

            row.setTag(listViewHolder);
        } else {
            row = convertView;
            listViewHolder = (ListViewHolder) row.getTag();
        }

        listViewHolder.spinner.setAdapter(level);
        listViewHolder.spinner.setSelection(position);
        listViewHolder.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int idPosition, long id) {
                if (updateFloorSelected != null)
                    updateFloorSelected.onActionClickListener(position, idPosition);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return row;
    }

    class ListViewHolder{
        Spinner spinner;
    }
}
