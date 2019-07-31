package com.tempmonitor.pojos;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

public class DeviceI implements Parcelable {

    private BluetoothDevice bluetoothDevice;

    public DeviceI(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    private DeviceI(Parcel in) {
        bluetoothDevice = in.readParcelable(BluetoothDevice.class.getClassLoader());
    }

    public static final Creator<DeviceI> CREATOR = new Creator<DeviceI>() {
        @Override
        public DeviceI createFromParcel(Parcel in) {
            return new DeviceI(in);
        }

        @Override
        public DeviceI[] newArray(int size) {
            return new DeviceI[size];
        }
    };

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(bluetoothDevice, flags);
    }
}
