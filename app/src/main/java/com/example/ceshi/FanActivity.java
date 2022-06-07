package com.example.ceshi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FanActivity extends Activity {

    //建立物联网连接部分
//    private String host = "tcp://192.168.1.4:1883";
    private String host = "tcp://120.24.88.132:1883";
    private String userName = "haleyiot";
    private String passWord = "654321";
    private String mqtt_id = "testiot_fan"; //定义成自己的QQ号  切记！不然会掉线！！！
    private String mqtt_sub_topic = "testiot"; //为了保证你不受到别人的消息  哈哈
    private String mqtt_pub_topic = "testiot_PC"; //为了保证你不受到别人的消息  哈哈  自己QQ好后面加 _PC

    private ScheduledExecutorService scheduler;
    private MqttClient client;
    private MqttConnectOptions options;
    private Handler handler;

    //关联按钮
    private ImageButton fan_control;
    private TextView fan_state_text;
    private Button fan_state_button;
    private ImageView fan_unlock;
    private TextView fan_unlock_text;
    private ImageButton fan_control_1;
    private ImageButton fan_control_2;
    private ImageButton fan_control_3;


    @SuppressLint("HandlerLeak")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subpage_fan);
        final int[] flag = {0};
        final int[] fan_flag = {0};
        final int[] fan_lock_flag = {0};

        fan_state_button = findViewById(R.id.fan_state_button);
        fan_state_text = findViewById(R.id.fan_state_text);
        fan_control = findViewById(R.id.fan_control);
        fan_unlock = findViewById(R.id.fan_unlock);
        fan_unlock_text = findViewById(R.id.fan_unlock_text);
        fan_control_1 = findViewById(R.id.fan_control_1);
        fan_control_2 = findViewById(R.id.fan_control_2);
        fan_control_3 = findViewById(R.id.fan_control_3);

        //button_switch监听
        Button mbtn_switch = findViewById(R.id.btn_open4);
        Button mbtn_back1=findViewById(R.id.btn_back1);
        mbtn_switch.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override

            public void onClick(View v) {
                //开关状态改变
                if (flag[0] == 0) {
                    mbtn_switch.setBackground(getResources().getDrawable(R.drawable.switch3));
                    fan_lock_flag[0] = 1;
                    fan_unlock.setBackground(getResources().getDrawable(R.drawable.lock));
                    fan_unlock_text.setText("界面已锁定");



                } else {
                    mbtn_switch.setBackground(getResources().getDrawable(R.drawable.switch1));
                    fan_lock_flag[0] = 0;
                    fan_unlock.setBackground(getResources().getDrawable(R.drawable.unlock));
                    fan_unlock_text.setText("界面已解锁");
                }
                flag[0] = (flag[0] + 1) % 2;

            }
        });

        //返回主界面监听
        mbtn_back1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //跳转到主界面
                Intent intent =new Intent(FanActivity.this,maininterface.class);
                startActivity(intent);
            }
        });


        Mqtt_init();
        startReconnect();

        fan_control.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fan_lock_flag[0] == 0) {
                    if (fan_flag[0] == 0) {
                        publishmessageplus(mqtt_pub_topic, "{\"set_fan\":1}");
                        fan_flag[0] = 1;
                        Toast.makeText(FanActivity.this, "风扇已打开", Toast.LENGTH_SHORT).show(); //context:当前的活动,text:内容,LENGTH_SHORT:显示的时长（short）
                    } else if (fan_flag[0] == 1) {
                        publishmessageplus(mqtt_pub_topic, "{\"set_fan\":0}");
                        fan_flag[0] = 0;
                        Toast.makeText(FanActivity.this, "风扇已关闭", Toast.LENGTH_SHORT).show(); //context:当前的活动,text:内容,LENGTH_SHORT:显示的时长（short）
                    }
                }
            }
        });

        fan_control_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fan_lock_flag[0] == 0 && fan_flag[0] == 1){
                    publishmessageplus(mqtt_pub_topic, "{\"set_fan\":1}");
                }
            }
        });

        fan_control_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fan_lock_flag[0] == 0 && fan_flag[0] == 1){
                    publishmessageplus(mqtt_pub_topic, "{\"set_fan\":2}");
                }
            }
        });

        fan_control_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fan_lock_flag[0] == 0 && fan_flag[0] == 1){
                    publishmessageplus(mqtt_pub_topic, "{\"set_fan\":3}");
                }
            }
        });

        handler = new Handler() {
            @SuppressLint("SetTextI18n")
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 1: //开机校验更新回传
                        break;
                    case 2:  // 反馈回传

                        break;
                    case 3:


                    case 30:  //连接失败
//                        Toast.makeText(MainActivity.this,"连接失败" ,Toast.LENGTH_SHORT).show();
//                        fan_state_text.setText("离线");
//                        fan_state_button.setBackground(getResources().getDrawable(R.drawable.shape_circle_red));
                        break;
                    case 31:   //连接成功
//                        Toast.makeText(FanActivity.this,"连接成功" ,Toast.LENGTH_SHORT).show();
                        fan_state_text.setText("在线");
                        fan_state_button.setBackground(getResources().getDrawable(R.drawable.shape_circle_green));
                        try {
                            client.subscribe(mqtt_sub_topic,1);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
            }
        };


    }

    private void Mqtt_init()
    {
        try {
            //host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
            client = new MqttClient(host, mqtt_id,
                    new MemoryPersistence());
            //MQTT的连接设置
            options = new MqttConnectOptions();
            //设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            options.setCleanSession(false);
            //设置连接的用户名
            options.setUserName(userName);
            //设置连接的密码
            options.setPassword(passWord.toCharArray());
            // 设置超时时间 单位为秒
            options.setConnectionTimeout(10);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            options.setKeepAliveInterval(20);
            //设置回调
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    //连接丢失后，一般在这里面进行重连
                    System.out.println("connectionLost----------");
                    //startReconnect();
                }
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    //publish后会执行到这里
                    System.out.println("deliveryComplete---------"
                            + token.isComplete());
                }
                @Override
                public void messageArrived(String topicName, MqttMessage message)
                        throws Exception {
                    //subscribe后得到的消息会执行到这里面
                    System.out.println("messageArrived----------");

                    //封装message包
                    Message msg = new Message();
                    //发送message到handler
                    msg.what = 3;   //收到消息标志位
                    msg.obj = topicName + "---" + message.toString();
                    handler.sendMessage(msg);    // hander 回传
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void Mqtt_connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(!(client.isConnected()) )  //如果还未连接
                    {
                        client.connect(options);
                        Message msg = new Message();
                        msg.what = 31;
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = new Message();
                    msg.what = 30;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }
    private void startReconnect() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (!client.isConnected()) {
                    Mqtt_connect();
                }
            }
        }, 0 * 1000, 10 * 1000, TimeUnit.MILLISECONDS);
    }
    private void publishmessageplus(String topic,String message2)
    {
        if (client == null || !client.isConnected()) {
            return;
        }
        MqttMessage message = new MqttMessage();
        message.setPayload(message2.getBytes());
        try {
            client.publish(topic,message);
        } catch (MqttException e) {

            e.printStackTrace();
        }
    }
}
