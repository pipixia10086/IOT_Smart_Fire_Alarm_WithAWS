package com.MyMQTT;




import javax.net.ssl.SSLSocketFactory;


//class TestSSL {
////    private  MqttConnectOptions options = null;
//
//    public static void test(){
//        MqttConnectOptions options = new MqttConnectOptions();
//        String serverUrl = "ssl://a2a1zfem06d51g-ats.iot.us-west-1.amazonaws.com:8883";
//        options.setCleanSession(true);
//        options.setConnectionTimeout(10);
//        options.setKeepAliveInterval(10);
//        MqttClient mqttClient = null;
//        try {
//            options.setSocketFactory(SSLUtil.getSocketFactory());
//            mqttClient = new MqttClient(serverUrl, "mainserver", null);
//            MqttRecieveCallback mqttRecieveCallback = new MqttRecieveCallback();
//            mqttClient.setCallback(mqttRecieveCallback);
//            mqttClient.connect(options);
//            MqttTopic topic = mqttClient.getTopic("iot/cmd");
//            MqttMessage mqttMessage = new MqttMessage();
//            mqttMessage.setQos(1);
//            mqttMessage.setPayload("test From Main Server".getBytes());
//            MqttDeliveryToken publish = topic.publish(mqttMessage);
//            if(!publish.isComplete()){
//                System.out.println("Complete");
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }
//
//}

class SSLUtil {
    static SSLSocketFactory getSocketFactory() throws Exception {
        return null;
    }
}