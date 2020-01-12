package com.example.client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
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

public class MainActivity extends AppCompatActivity
{
    private Button button1, button2, button3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button1 = findViewById(R.id.button_register);
        button2 = findViewById(R.id.button_login);
        button3 = findViewById(R.id.button);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        button1.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, Register.class);
                startActivity(intent);
            }
        });
        button1.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, Register.class);
                startActivity(intent);
            }
        });

        button2.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                //Intent intent = new Intent();
                //intent.setClass(MainActivity.this, Login.class);
                //startActivity(intent);
                String username="";
                EditText editText1 =(EditText)findViewById(R.id.edit_username);
                username = editText1.getText().toString();
                Location.username = username;
                String pwd="";
                EditText editText2 =(EditText)findViewById(R.id.edit_pwd);
                pwd = editText2.getText().toString();
                //editText2.setText(username.toCharArray(), 0, username.length());
                JSONObject msg = new JSONObject();
                //System.out.println("json");
                try
                {
                    msg.put("username", username);
                    msg.put("password", pwd);
                    msg.put("type","login");
                }
                catch (org.json.JSONException e)
                {
                    e.printStackTrace();
                }
                System.out.println(msg.toString());
                try
                {
                    System.out.println("enter try process");
                    Socket socket = new Socket("13.56.251.5", 40002);
                    //socket.setSoTimeout(10000);//设置读操作超时时间30 s
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
                    System.out.println((sb.toString()));
                    //System.out.println(sb);
                    if (sb.toString().equals("wrong"))
                    {
                        String warn = "wrong username or password";
                        EditText editText3 =(EditText)findViewById(R.id.edit_warn);
                        editText3.setText(warn.toCharArray(), 0, warn.length());
                        System.out.println("login fail");
                    }
                    else
                    {
                        Intent intent = new Intent();
                        intent.setClass(MainActivity.this, Login.class);
                        startActivity(intent);
                        System.out.println("login success");

                    }
                    os.close();
                    is.close();
                    socket.close();
                }
                catch(UnknownHostException e)
                {
                    e.printStackTrace();
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }
}
