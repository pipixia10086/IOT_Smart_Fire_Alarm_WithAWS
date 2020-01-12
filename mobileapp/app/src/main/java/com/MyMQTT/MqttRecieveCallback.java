package com.MyMQTT;

import android.app.Activity;
import android.content.Intent;
import android.widget.EditText;

import com.example.client.Alarm;
import com.example.client.Location;
import com.example.client.Login;
import com.example.client.LowBattery;
import com.example.client.MainActivity;
import com.example.client.R;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class MqttRecieveCallback implements MqttCallback{
    private Activity activity;
    private  MyMqttClient mqttconn;
    public MqttRecieveCallback(MyMqttClient mqttconn, Activity activity){
        this.activity=activity;
        this.mqttconn = mqttconn;
    }
    @Override
    public  void connectionLost(Throwable cause) {
        System.out.println("lost connection , auto reconnection");
        mqttconn.reConnect();
    }
    @Override
    public void messageArrived(String topic, MqttMessage message){
        String RawData = new String(message.getPayload());
        System.out.println("Client 接受消息内容 ： " + RawData);
        try {
            JSONObject jsonData = new JSONObject(RawData);
            String type = jsonData.getString("type");
            if(type.equals("FireWarning"))
            {
                Location.location = jsonData.getString("locate");
                Location.deviceID = jsonData.getString("deviceID");
                Intent intent = new Intent();
                intent.setClass(activity, Alarm.class);
                activity.startActivity(intent);
            }else if (type.equals("LowBattery"))
            {
                Location.location = jsonData.getString("locate");
                Location.deviceID = jsonData.getString("deviceID");
                Intent intent = new Intent();
                intent.setClass(activity, LowBattery.class);
                activity.startActivity(intent);
            }else
            {
                System.out.println("Invalid message " + jsonData);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token){

    }
}
