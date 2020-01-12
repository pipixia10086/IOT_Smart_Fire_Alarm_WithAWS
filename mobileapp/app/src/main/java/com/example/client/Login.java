package com.example.client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.MyMQTT.MyMqttClient;

public class Login extends AppCompatActivity {
    private Button button1,button2;
    private static MyMqttClient myMqttClient =null;
    private static String un = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        button1 = findViewById(R.id.button3);
        button1.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(Login.this, MainActivity.class);
                startActivity(intent);
            }
        });
        button2 = findViewById(R.id.button_scan);
        button2.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(Login.this, Scan.class);
                startActivity(intent);
            }
        });
        if(myMqttClient ==null) {
            Login.myMqttClient = new MyMqttClient(
                    "110", "13.56.251.5:1883", "TCP", Login.this);
        }
        myMqttClient.Connect();
        if (un == null)
        {
            if(Location.username != null){
                System.out.println(Location.username);
                System.out.println(myMqttClient);
                myMqttClient.subTopic(Location.username);
                un = Location.username;
            }
        }
        else if(!un.equals(Location.username)){
            if(Location.username != null){
                myMqttClient.subTopic(Location.username);
                un = Location.username;
                if (un != null){
                    myMqttClient.cleanTopic(un);
                }
            }
        }

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while(true) {
//                   // System.out.println(Location.location);
//                    if(Location.location!=null) {
//                        System.out.println(Location.location);
//                        Intent intent = new Intent();
//                        intent.setClass(Login.this, Alarm.class);
//                        startActivity(intent);
////                        String warn = Location.location;
////                        EditText editText =(EditText)findViewById(R.id.editText10);
////                        System.out.println(1);
////
////                        editText.setText(warn.toCharArray(), 0, warn.length());
////                        System.out.println(2);
//
//                        break;
//                    }
//                    try{
//                        Thread.sleep(1000);
//                    }catch (Exception e) {
//                    }
//                }
//            }
//        }).start();
    }


}
