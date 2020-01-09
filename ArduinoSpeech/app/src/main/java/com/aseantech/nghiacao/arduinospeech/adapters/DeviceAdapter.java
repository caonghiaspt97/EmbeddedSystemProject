package com.aseantech.nghiacao.arduinospeech.adapters;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aseantech.nghiacao.arduinospeech.R;

import java.util.List;

public class DeviceAdapter extends BaseAdapter {
    Context context;
    int layout;
    List<BluetoothDevice> listDevices;

    public DeviceAdapter(Context context, int layout, List<BluetoothDevice> listDevices) {
        this.context = context;
        this.layout = layout;
        this.listDevices = listDevices;
    }

    @Override
    public int getCount() {
        return listDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(layout,null);

        //bind data
        TextView tvDeviceName = convertView.findViewById(R.id.tv_device_name);
        TextView tvMAC = convertView.findViewById(R.id.tv_MAC);
        ImageView imgDeviceType = convertView.findViewById(R.id.img_device_type);

        //assign data
        tvDeviceName.setText(listDevices.get(i).getName());
        tvMAC.setText(listDevices.get(i).getAddress());

        if(listDevices.get(i).getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.PHONE){
            imgDeviceType.setImageResource(R.drawable.phone);
        } else if(listDevices.get(i).getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.COMPUTER){
            imgDeviceType.setImageResource(R.drawable.desktop);
        } else if(listDevices.get(i).getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.AUDIO_VIDEO){
            imgDeviceType.setImageResource(R.drawable.media);
        } else {
            imgDeviceType.setImageResource(R.drawable.bluetooth);
        }

        return convertView;
    }
}
