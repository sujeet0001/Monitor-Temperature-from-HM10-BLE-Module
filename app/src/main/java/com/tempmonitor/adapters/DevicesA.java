package com.tempmonitor.adapters;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tempmonitor.R;
import com.tempmonitor.activities.Main;
import com.tempmonitor.pojos.DeviceI;

import java.util.ArrayList;

public class DevicesA extends RecyclerView.Adapter<DevicesA.DevicesH> {

    private ArrayList<DeviceI> deviceIS;
    private AppCompatActivity context;

    public DevicesA(AppCompatActivity context, ArrayList<DeviceI> deviceIS) {
        this.deviceIS = deviceIS;
        this.context = context;
    }

    @NonNull
    @Override
    public DevicesH onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new DevicesH(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.dev_item, viewGroup, false),
                context, deviceIS);
    }

    @Override
    public void onBindViewHolder(@NonNull DevicesH devicesH, int i) {
        DeviceI deviceI = deviceIS.get(i);
        devicesH.name.setText(deviceI.getBluetoothDevice().getName());
        devicesH.id.setText(deviceI.getBluetoothDevice().getAddress());
    }

    @Override
    public int getItemCount() {
        return deviceIS.size();
    }

    class DevicesH extends RecyclerView.ViewHolder {

        AppCompatActivity context;
        ArrayList<DeviceI> deviceIS;
        TextView name, id, connect;

        DevicesH(@NonNull View itemView, final AppCompatActivity context, final ArrayList<DeviceI> deviceIS) {
            super(itemView);
            this.context = context;
            this.deviceIS = deviceIS;

            name = itemView.findViewById(R.id.di_name);
            id = itemView.findViewById(R.id.di_id);
            connect = itemView.findViewById(R.id.di_connect);

            connect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Main main = (Main) context;
                    main.toConnect(getAdapterPosition());
                }
            });

        }
    }

}
