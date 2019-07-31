package com.tempmonitor.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tempmonitor.R;
import com.tempmonitor.activities.ConnectedDevice;

public class SetThreshold extends Dialog {

    private EditText min, max;
    private Context context;
    private float x, n;
    private String mn, mx;

    public SetThreshold(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(R.drawable.rc_white);
        setContentView(R.layout.set_threshold);

        min = findViewById(R.id.st_min);
        max = findViewById(R.id.st_max);
        Button done = findViewById(R.id.st_done);

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mn = min.getText().toString();
                mx = max.getText().toString();

                if(mn.isEmpty() || mx.isEmpty()){
                    Toast.makeText(context, "Please set both thresholds", Toast.LENGTH_LONG).show();
                } else {
                    x = Float.valueOf(mx);
                    n = Float.valueOf(mn);
                    if(n > x){
                        Toast.makeText(context, "Min cant be greater than Max", Toast.LENGTH_LONG).show();
                    } else if(n == x){
                        Toast.makeText(context, "Min cant be equal to Max", Toast.LENGTH_LONG).show();
                    } else {
                        ConnectedDevice cd = (ConnectedDevice) context;
                        cd.setValues(n, x);
                        dismiss();
                    }
                }
            }
        });

    }
}
