package com.tempmonitor.activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.tempmonitor.R;
import com.tempmonitor.adapters.DevicesA;
import com.tempmonitor.pojos.DeviceI;

import java.util.ArrayList;
import java.util.Set;

public class Main extends AppCompatActivity {

    View view;
    TextView title, nodevs;
    RecyclerView list;
    DevicesA devicesA;
    Button scan;
    boolean isDevicesfound;
    ImageButton bt;
    final int ENABLE_BT = 100, LOC_PER = 101;
    Set<BluetoothDevice> bluetoothDevices;
    BluetoothAdapter bluetoothAdapter;
    LinearLayoutManager llm;
    ArrayList<DeviceI> deviceIS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        view = findViewById(R.id.ma_toolbar);
        title = view.findViewById(R.id.tb_title);
        title.setText(getResources().getString(R.string.app_name));
        bt = view.findViewById(R.id.tb_timer);
        bt.setImageResource(R.drawable.bluetooth);
        bt.setVisibility(View.VISIBLE);
        list = findViewById(R.id.ma_list);
        nodevs = findViewById(R.id.ma_nodevs);
        scan = findViewById(R.id.ma_scan);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            Toast.makeText(getApplicationContext(), R.string.bleunsupported, Toast.LENGTH_SHORT).show();
        } else if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if(bluetoothAdapter == null){
                Toast.makeText(getApplicationContext(), R.string.bleunsupported, Toast.LENGTH_SHORT).show();
            } else if (!bluetoothAdapter.isEnabled()) {
                enableBT();
            } else {
                getpaireddevs();
            }
        }

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!bluetoothAdapter.isEnabled()){
                    enableBT();
                } else {
                    Toast.makeText(getApplicationContext(), "Bluetooth already enabled", Toast.LENGTH_SHORT).show();
                }
            }
        });

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLocPermission();
            }
        });

    }

    public void enableBT(){
        Intent in = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(in, ENABLE_BT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                getpaireddevs();
            } else {
                Toast.makeText(getApplicationContext(), R.string.bluetoothnotenabled, Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    void getpaireddevs() {
        deviceIS = new ArrayList<>();
        llm = new LinearLayoutManager(getApplicationContext());

        list.setHasFixedSize(true);
        devicesA = new DevicesA(Main.this, deviceIS);
        list.setLayoutManager(llm);
        list.setAdapter(devicesA);
        
        bluetoothDevices = bluetoothAdapter.getBondedDevices();
        if (bluetoothDevices.size() > 0) {
            for (BluetoothDevice bd : bluetoothDevices) {
                deviceIS.add(new DeviceI(bd));
            }
            devicesA.notifyDataSetChanged();
        } else {
            Toast.makeText(getApplicationContext(), R.string.nopaireddevs, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isDevicesfound){
            unregisterReceiver(foundDevices);
        }
    }

    private BroadcastReceiver foundDevices = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            assert action != null;
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                deviceIS.add(new DeviceI(device));
                devicesA.notifyDataSetChanged();
            }
        }
    };

    public void discoverDevs() {
        deviceIS.clear();
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
            bluetoothAdapter.startDiscovery();
        } else {
            bluetoothAdapter.startDiscovery();
        }
        IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(foundDevices, discoverDevicesIntent);
    }

    void checkLocPermission(){
        if (ActivityCompat.checkSelfPermission(Main.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(Main.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Main.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, LOC_PER);
        } else {
            discoverDevs();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == LOC_PER){
            if(grantResults[0] == 0){
                checkLocPermission();
            } else {
                Toast.makeText(getApplicationContext(), "Need location permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        bluetoothAdapter.cancelDiscovery();
    }

    public void toConnect(int i){
        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
        }
        Intent in = new Intent(getApplicationContext(), ConnectedDevice.class);
        DeviceI btDev = new DeviceI(deviceIS.get(i).getBluetoothDevice());
        in.putExtra("Obj", btDev);
        startActivity(in);
    }
}
