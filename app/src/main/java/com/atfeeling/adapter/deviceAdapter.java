package com.atfeeling.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.atfeeling.beans.device;
import com.atfeeling.test.R;
import java.util.ArrayList;

/**
 * Created by anxulei on 2018/3/11.
 */


public class deviceAdapter extends BaseAdapter {

        private Context context;
        private LayoutInflater inflater;
        public ArrayList<device> arr;
        public deviceAdapter(Context context) {
            super();
            this.context = context;
            inflater = LayoutInflater.from(context);
            arr = new ArrayList<device>();
        }
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return arr.size();
        }
        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return arg0;
        }
        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return arg0;
        }
        @Override
        public View getView(final int position, View view, ViewGroup arg2) {
            // TODO Auto-generated method stub
            if(view == null){
                view = inflater.inflate(R.layout.listview_item, null);
            }

            TextView name =(TextView) view.findViewById(R.id.name);
            TextView deviceId =(TextView) view.findViewById(R.id.deviceId);
            TextView rssi =(TextView) view.findViewById(R.id.rssi);
            TextView state =(TextView) view.findViewById(R.id.state);
            TextView electricity =(TextView) view.findViewById(R.id.electricity);
            TextView isOpen =(TextView) view.findViewById(R.id.isOpen);

            name.setText(arr.get(position).getName());
            deviceId.setText(arr.get(position).getDeviceId());
            rssi.setText(arr.get(position).getRssi());
            state.setText(arr.get(position).getState());
            electricity.setText(arr.get(position).getElectricity());
            isOpen.setText(arr.get(position).getIsOpen());


            return view;
        }
    }