package com.example.client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Alarm extends AppCompatActivity {
    private Button button1, button2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        String warn = "Fire alarm at "+ Location.location + "!!!" ;
        EditText editText =(EditText)findViewById(R.id.edit_alarm);
        editText.setText(warn.toCharArray(), 0, warn.length());
        button1 = findViewById(R.id.button2);
        button2 = findViewById(R.id.button1);


        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(Alarm.this, Login.class);
                startActivity(intent);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                JSONObject msg = new JSONObject();
                    try {
                        msg.put("deviceID", Location.deviceID);
                        msg.put("type", "closeRing");
                    } catch (org.json.JSONException e) {
                        e.printStackTrace();
                    }
                    System.out.println(msg);

                    try {
                        Socket socket = new Socket("13.56.251.5", 40002);
                        System.out.println("socket established");
                        OutputStream os = socket.getOutputStream();
                        //写入要发送给服务器的数据
                        os.write(msg.toString().getBytes());
                        os.flush();
                        socket.shutdownOutput();
                        os.close();
                        socket.close();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

        });


    }


}
