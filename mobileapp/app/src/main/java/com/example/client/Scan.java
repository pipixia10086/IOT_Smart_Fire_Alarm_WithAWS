package com.example.client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Scan extends AppCompatActivity{
    //private Button button1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        //button1 = findViewById(R.id.button_backtolog);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建IntentIntegrator对象
                IntentIntegrator intentIntegrator = new IntentIntegrator(Scan.this);
                // 开始扫描
                intentIntegrator.initiateScan();
            }
        });
        findViewById(R.id.button_backtolog).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(Scan.this, Login.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String location = "";
                EditText editText = (EditText) findViewById(R.id.editText1);
                location = editText.getText().toString();


                EditText editText3 =(EditText)findViewById(R.id.editText);
                Location.deviceID = editText3.getText().toString();

                JSONObject msg = new JSONObject();
                if (location != " " && Location.deviceID != null) {
                    try {
                        msg.put("deviceID", Location.deviceID);
                        msg.put("locate", location);
                        msg.put("type", "addDevice");
                        msg.put("username",Location.username);
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
                        InputStream is = socket.getInputStream();
                        //解析服务器返回的数据
                        InputStreamReader reader = new InputStreamReader(is);
                        BufferedReader bufReader = new BufferedReader(reader);
                        String s = "";
                        StringBuffer sb = new StringBuffer();
                        while ((s = bufReader.readLine()) != null) {
                            sb.append(s);
                        }
                        if (sb.toString().equals("OK")) {
                            String warn = "Add success";
                            EditText editText2 =(EditText)findViewById(R.id.editText3);
                            editText2.setText(warn.toCharArray(), 0, warn.length());
                            System.out.println("register fail");
                        }
                        else{
                            String warn = "Add fails";
                            EditText editText2 =(EditText)findViewById(R.id.editText3);
                            editText2.setText(warn.toCharArray(), 0, warn.length());
                            System.out.println("register fail");
                        }

                        os.close();
                        is.close();
                        socket.close();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

//        new Thread(new Runnable() {
//            @Override
//            public void run()
//            {
//                findViewById(R.id.button_backtolog).setOnClickListener(new View.OnClickListener() {
//                    public void onClick(View view) {
//                        Intent intent = new Intent();
//                        intent.setClass(Scan.this, Login.class);
//                        startActivity(intent);
//                    }
//                });
//            }
//        }).start();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 获取解析结果
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "取消扫描", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "the content is:" + result.getContents(), Toast.LENGTH_LONG).show();
                Location.deviceID = result.getContents();
                EditText editText =(EditText)findViewById(R.id.editText);
                editText.setText(Location.deviceID.toCharArray(), 0, Location.deviceID.length());
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
        }
}



