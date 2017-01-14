package org.nunocky.bleshieldstudy01;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class BluetoothDeviceListAdapter extends BaseAdapter {
    @SuppressWarnings("unused")
    private static final String TAG = "BLEListAdapter";

    private ArrayList<BluetoothDevice> mDeviceList;
    private LayoutInflater inflater;

    BluetoothDeviceListAdapter(Context context, @NonNull ArrayList<BluetoothDevice> ary) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDeviceList = ary;
    }

    @Override
    public int getCount() {
        return mDeviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDeviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final BluetoothDevice dev = (BluetoothDevice) getItem(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listitem, null);
        }
        TextView tv1 = (TextView) convertView.findViewById(R.id.text1);
        TextView tv2 = (TextView) convertView.findViewById(R.id.text2);
        tv1.setText(dev.getName());
        tv2.setText(dev.getAddress());
        return convertView;
    }

}
