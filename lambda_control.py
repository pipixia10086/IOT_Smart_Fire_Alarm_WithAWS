#!/usr/bin/env python
# coding: utf-8

# In[7]:


from AWSIoTPythonSDK.MQTTLib import AWSIoTMQTTClient
# import pymysql
import sys
import time
import socket               # 导入 socket 模块
import json
# import numpy as np
# import sklearn
import joblib

REGION = 'us-west-1b'
low_power = 1000

#数据库信息
rds_host  = "final.c7mtgoufa4yi.us-west-1.rds.amazonaws.com"
name = "admin"
password = "12345678"
db_name = "sensors"


predictflag = 0
predict = -1
# Max Norm
# center:  cook, normal ,fire
centroids = [[0.77634159, 0.58050692, 0.98477186, 0.93912053],
 [0.91315325, 0.77987498, 0.91070243, 0.93131321],
 [0.87689573, 0.74146738, 0.92988549, 0.3668408]]



maximum = [18420, 12611, 26102, 58686]

# 连接mqtt
iot_id = "0"
mqqt_endpoint = "a2a1zfem06d51g-ats.iot.us-west-1.amazonaws.com"
mqqt_port = 8883
mqqt_led_message_topic = "iot/cmd"

def initIOT():
    myMQTTClient = AWSIoTMQTTClient(iot_id)
    myMQTTClient.configureEndpoint(mqqt_endpoint, mqqt_port)
    myMQTTClient.configureCredentials(
        ("Amazon_Root_CA_1.pem"),
        ("690ce7c017-private.pem.key"),
        ("690ce7c017-certificate.pem.crt"))

    #myMQTTClient connection configuration
    myMQTTClient.configureAutoReconnectBackoffTime(1, 32, 20)
    myMQTTClient.configureOfflinePublishQueueing(-1)  # Infinite offline Publish queueing
    myMQTTClient.configureDrainingFrequency(2)  # Draining: 2 Hz
    myMQTTClient.configureConnectDisconnectTimeout(10)  # 10 sec
    myMQTTClient.configureMQTTOperationTimeout(5)  # 5 sec
    myMQTTClient.connect()
    return myMQTTClient


# 通知server给user发信息
def sendmsg(msg):
    s = socket.socket()         # 创建 socket 对象
    host = "ec2-18-217-209-65.us-east-2.compute.amazonaws.com"
    port = 10000
    msg = json.dumps(msg)
    s.connect((host, port))
    print(s.send(msg.encode()))
    s.close()


def main(event, context):
    """
    This function fetches content from mysql RDS instance
    """
    now = int(round(time.time()*1000))
    Time = time.strftime('%Y-%m-%d %H:%M:%S',time.localtime(now/1000))
    
    if 'CO' in event:
        predictflag = 1
        CO = event['CO']
        Smoke = event['Smoke']
        T = event['T']
        Flame = event['Flame']

    # 低电量预警
    if 'Power' in event:
        Power = event['Power']
        Msg = {"type": "LowBattery",
        "deviceID": "0080000004018895",
        "data": '00',
        "time": Time
        }
#        sendmsg(Msg)
        

    # sensor data存入数据库
#    result = []
#    conn = pymysql.connect(rds_host, user=name, passwd=password, db=db_name, connect_timeout=2)
#    with conn.cursor() as cur:
#        cur.execute("""insert into sensor (Time, CO, Smoke, T, Flame) values( '%s', %d, %d, %d, %d)""" 
#        % (Time, int(CO), int(Smoke), int(T), int(Flame)))
#        cur.execute("""select * from sensor""")
#        conn.commit()
#        cur.close()
#        for row in cur:
#            result.append(list(row))
#        print('Data from RDS...')
#        print(result)
        
        
    # 预测
    mindistance = float('inf')
    if predictflag:
        data = [float(CO)/maximum[0], float(Smoke)/maximum[1], float(T)/maximum[2], float(Flame)/maximum[3]]
        for label, center in enumerate(centroids):
            distance = sum([pow(x-y, 2) for x,y in zip(center, data)])
            if distance < mindistance:
                mindistance = distance
                predict = label
            
#        model = joblib.load('my_model.pkl')
#        predict = model.predict([])
    # 火灾报警 0: normal 1:火灾 2: 做饭
    if predict == 2:
        msg = {
        "deviceID": "0080000004018895",
        "cmd": 'open',
        }
        msg = json.dumps(msg)
        myMQTTClient = initIOT()
        myMQTTClient.publish(mqqt_led_message_topic, msg, 1)
        
        Msg = {"type": "FireWarining",
            "deviceID": "0080000004018895",
            "data": 'room1',
            "time": Time
        }
#        sendmsg(Msg)
        
        
        
        