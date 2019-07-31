package com.tempmonitor.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.tempmonitor.R;
import com.tempmonitor.activities.ConnectedDevice;

import java.util.Objects;

public class BuzzAlert extends Dialog {

    private Context context;
    private ConnectedDevice connectedDevice;
    public String msg;

    public BuzzAlert(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getWindow()).setBackgroundDrawableResource(R.drawable.rc_white);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.buzz_alert);

        TextView msgtxt = findViewById(R.id.ba_msg);

        msgtxt.setText(msg);

        findViewById(R.id.ba_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectedDevice = (ConnectedDevice) context;
                connectedDevice.stopBuzzing();
                dismiss();
            }
        });
    }

}
