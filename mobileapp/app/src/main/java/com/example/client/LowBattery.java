package com.example.client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

public class LowBattery extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_low_battery);

        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(LowBattery.this, Login.class);
                startActivity(intent);
            }
        });

        String warn = "Device: " + Location.deviceID + "" +
                "\nLocation: " + Location.location;
        warn += "\nLOW battery, please replace it!";
        TextView editText =(TextView)findViewById(R.id.editText4);
        editText.setText(warn.toCharArray(), 0, warn.length());

    }
}
