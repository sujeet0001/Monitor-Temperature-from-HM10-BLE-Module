package com.tempmonitor.activities;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.tempmonitor.R;
import com.tempmonitor.dialogs.BuzzAlert;
import com.tempmonitor.dialogs.SetThreshold;
import com.tempmonitor.pojos.DeviceI;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class ConnectedDevice extends AppCompatActivity {

    BluetoothDevice bluetoothDevice = null;
    DeviceI btDev;
    View toolbar;
    Button startListening, setthreshold;
    LinearLayout lay;
    TextView title, name, tvtemp, minthres, maxthres;
    float min = 0, max = 0;
    Switch aSwitch;
    MediaPlayer mp;
    ProgressBar progress;
    BuzzAlert buzzAlert;
    BluetoothGatt gatt;
    boolean rchdThrshld, isListening, bzmx = false, bzmn = false, alarm = true;
    SetThreshold setThreshold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connected_device);

        toolbar = findViewById(R.id.cd_toolbar);
        title = toolbar.findViewById(R.id.tb_title);
        title.setText(R.string.cnndev);

        setthreshold = findViewById(R.id.cd_setthreshold);

        name = findViewById(R.id.cd_devname);
        startListening = findViewById(R.id.cd_listen);
        tvtemp = findViewById(R.id.cd_temp);
        lay = findViewById(R.id.cd_lay);
        aSwitch = findViewById(R.id.cd_switch);

        minthres = findViewById(R.id.cd_minthres);
        maxthres = findViewById(R.id.cd_maxthres);

        setThreshold = new SetThreshold(ConnectedDevice.this);

        btDev = getIntent().getParcelableExtra("Obj");
        bluetoothDevice = btDev.getBluetoothDevice();

        name.setText(bluetoothDevice.getName());

        progress = findViewById(R.id.cd_progress);

        setthreshold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setThreshold.show();
            }
        });

        findViewById(R.id.cd_alarm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(getApplicationContext(), ChooseAlarm.class));
            }
        });

        startListening.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (min == 0 && max == 0) {
                    Toast.makeText(getApplicationContext(), "Thresholds not set", Toast.LENGTH_LONG).show();
                } else {
                    if (isListening) {
                        if (gatt != null) {
                            gatt.close();
                            gatt = null;
                        }
                        onBackPressed();
                    } else {
                        progress.setVisibility(View.VISIBLE);
                        connectToDev();
                    }
                }
            }
        });

        aSwitch.setChecked(true);

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    alarm = false;
                } else {
                    alarm = true;
                }
            }
        });
    }

    public void setValues(float min, float max) {
        this.min = min;
        this.max = max;
        if (lay.getVisibility() != View.VISIBLE) {
            lay.setVisibility(View.VISIBLE);
        }
        String mn, mx;
        mn = min + " " + getResources().getString(R.string.degree);
        minthres.setText(mn);
        mx = max + " " + getResources().getString(R.string.degree);
        maxthres.setText(mx);
        Toast.makeText(getApplicationContext(), "Thresholds set successfully", Toast.LENGTH_LONG).show();
        rchdThrshld = false;
        bzmx = false;
        bzmn = false;

    }

    void connectToDev() {
        if(mp == null){
            mp = MediaPlayer.create(this, R.raw.bell);
            mp.setLooping(true);
        }
        if (gatt == null) {
            gatt = bluetoothDevice.connectGatt(this, true, gattCallback);
        }
    }

    public void stopBuzzing() {
        mp.pause();
    }

    @Override
    protected void onDestroy() {
        mp.stop();
        super.onDestroy();
    }

    BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            isListening = true;
            handler.sendEmptyMessage(1);
            gatt.readCharacteristic(gatt.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"))
                    .getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")));
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            gatt.readCharacteristic(characteristic);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            try {
                handler.sendMessage(Message.obtain(null, 0, new String(characteristic.getValue(), StandardCharsets.UTF_8).substring(4)));
            } catch (IndexOutOfBoundsException i) {
                //
            }

            gatt.setCharacteristicNotification(characteristic, true);
            BluetoothGattDescriptor desc = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
            desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(desc);

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            try {
                handler.sendMessage(Message.obtain(null, 0, new String(characteristic.getValue(), StandardCharsets.UTF_8).substring(4)));
            } catch (IndexOutOfBoundsException i) {
                //
            }
        }
    };

    void buzz(float num) {
        if (alarm) {
            if (!rchdThrshld) {
                if (num > min) {
                    rchdThrshld = true;
                }
            } else {
                if (num < min && !bzmn) {
                    bzmn = true;
                    mp.start();
                    buzzAlert = new BuzzAlert(ConnectedDevice.this);
                    buzzAlert.msg = getResources().getString(R.string.minthrrchd);
                    buzzAlert.show();
                } else if (num > max && !bzmx) {
                    bzmx = true;
                    mp.start();
                    buzzAlert = new BuzzAlert(ConnectedDevice.this);
                    buzzAlert.msg = getResources().getString(R.string.maxthrrchd);
                    buzzAlert.show();
                } else if (num > min && num < max) {
                    if (bzmn) {
                        bzmn = false;
                    }
                    if (bzmx) {
                        bzmx = false;
                    }
                }
            }
        }
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    progress.setVisibility(View.GONE);
                    startListening.setText(getString(R.string.stoplistening));
                    startListening.setBackground(getResources().getDrawable(R.drawable.rc_red));
                    break;
                default:
                    String characteristic;
                    characteristic = (String) msg.obj;
                    tvtemp.setText(characteristic);
                    try {
                        buzz(Float.valueOf(characteristic));
                    } catch (NumberFormatException nfe) {
                        //
                    }
                    break;
            }
        }
    };

    @Override
    public void finish() {
        super.finish();
        mp.stop();
    }
}