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

public class SecureActivity extends Activity {

    //建立物联网连接部分
//    private String host = "tcp://192.168.1.4:1883";
    private String host = "tcp://120.24.88.132:1883";
    private String userName = "haleyiot";
    private String passWord = "654321";
    private String mqtt_id = "testiot_secure"; //定义成自己的QQ号  切记！不然会掉线！！！
    private String mqtt_sub_topic = "testiot"; //为了保证你不受到别人的消息  哈哈
    private String mqtt_pub_topic = "testiot_PC"; //为了保证你不受到别人的消息  哈哈  自己QQ好后面加 _PC

    private ScheduledExecutorService scheduler;
    private MqttClient client;
    private MqttConnectOptions options;
    private Handler handler;

    //关联按钮
    private ImageView secure_unlock;
    private TextView secure_unlock_text;
    private ImageButton door_control;
    private ImageButton alarm_control;
    private Button secure_state_button;
    private TextView secure_state_text;
    private Button sonic_text;
    private Button pir_text;
    private Button monitor_text;

    @SuppressLint("HandlerLeak")


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subpage_secure);
        final int[] flag = {0};
        final int[] secure_lock_flag = {0};
        final int[] door_flag = {0};
        final int[] alarm_flag = {0};

        secure_unlock = findViewById(R.id.secure_unlock);
        secure_unlock_text = findViewById(R.id.secure_unlock_text);
        door_control = findViewById(R.id.door_control);
        alarm_control = findViewById(R.id.alarm_control);
        secure_state_button = findViewById(R.id.secure_state_button);
        secure_state_text = findViewById(R.id.secure_state_text);
        sonic_text = findViewById(R.id.sonic_text);
        pir_text = findViewById(R.id.pir_text);
        monitor_text = findViewById(R.id.monitor_text);

        //button_switch监听
        Button mbtn_switch = findViewById(R.id.btn_open3);
        Button mbtn_back1=findViewById(R.id.btn_back1);
        mbtn_switch.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override

            public void onClick(View v) {
                //开关状态改变
                if (flag[0] == 0) {
                    mbtn_switch.setBackground(getResources().getDrawable(R.drawable.switch3));
                    secure_lock_flag[0] = 1;
                    secure_unlock.setBackground(getResources().getDrawable(R.drawable.lock));
                    secure_unlock_text.setText("界面已锁定");



                } else {
                    mbtn_switch.setBackground(getResources().getDrawable(R.drawable.switch1));
                    secure_lock_flag[0] = 0;
                    secure_unlock.setBackground(getResources().getDrawable(R.drawable.unlock));
                    secure_unlock_text.setText("界面已解锁");
                }
                flag[0] = (flag[0] + 1) % 2;

            }
        });

        //返回主界面监听
        mbtn_back1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //跳转到主界面
                Intent intent =new Intent(SecureActivity.this,maininterface.class);
                startActivity(intent);
            }
        });

        Mqtt_init();
        startReconnect();

        door_control.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(secure_lock_flag[0] == 0){
                    if (door_flag[0] == 0) {
                        publishmessageplus(mqtt_pub_topic, "{\"set_door\":1}");
                        door_flag[0] = 1;
                        Toast.makeText(SecureActivity.this, "门已打开", Toast.LENGTH_SHORT).show();
                        door_control.setBackground(getResources().getDrawable(R.drawable.dooropen));
                    } else if (door_flag[0] == 1) {
                        publishmessageplus(mqtt_pub_topic, "{\"set_door\":0}");
                        door_flag[0] = 0;
                        Toast.makeText(SecureActivity.this, "门已关闭", Toast.LENGTH_SHORT).show();
                        door_control.setBackground(getResources().getDrawable(R.drawable.doorclose));
                    }
                }
            }
        });

        alarm_control.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(secure_lock_flag[0] == 0){
                    if (alarm_flag[0] == 0) {
                        publishmessageplus(mqtt_pub_topic, "{\"set_alarm\":1}");
                        alarm_flag[0] = 1;
                        Toast.makeText(SecureActivity.this, "警报已打开", Toast.LENGTH_SHORT).show();
                        alarm_control.setBackground(getResources().getDrawable(R.drawable.alarmon));
                    } else if (alarm_flag[0] == 1) {
                        publishmessageplus(mqtt_pub_topic, "{\"set_alarm\":0}");
                        alarm_flag[0] = 0;
                        Toast.makeText(SecureActivity.this, "警报已关闭", Toast.LENGTH_SHORT).show();
                        alarm_control.setBackground(getResources().getDrawable(R.drawable.alarmoff));
                    }
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
                        if(msg.obj.toString().contains("PIR_sensor"))
                        {
                            String PIR_val = msg.obj.toString().substring(msg.obj.toString().indexOf("PIR_sensor") + 11, msg.obj.toString().indexOf("}"));
                            pir_text.setText(PIR_val);
                        }

                        //收到超声波传感器的消息
                        else if(msg.obj.toString().contains("SONIC_sensor"))
                        {
                            String SONIC_val = msg.obj.toString().substring(msg.obj.toString().indexOf("SONIC_sensor") + 13, msg.obj.toString().indexOf("}"));
                            sonic_text.setText(SONIC_val);
                            break;
                        }

                        //收到警报判决程序的消息
                        else if(msg.obj.toString().contains("ALARM_sensor"))
                        {
                            String ALARM_val = msg.obj.toString().substring(msg.obj.toString().indexOf("ALARM_sensor") + 13, msg.obj.toString().indexOf("}"));
                            monitor_text.setText(ALARM_val);
                            break;
                        }


                    case 30:  //连接失败
//                        Toast.makeText(MainActivity.this,"连接失败" ,Toast.LENGTH_SHORT).show();
//                        secure_state_text.setText("离线");
//                        secure_state_button.setBackground(getResources().getDrawable(R.drawable.shape_circle_red));
                        break;
                    case 31:   //连接成功
//                        Toast.makeText(FanActivity.this,"连接成功" ,Toast.LENGTH_SHORT).show();
                        secure_state_text.setText("在线");
                        secure_state_button.setBackground(getResources().getDrawable(R.drawable.shape_circle_green));
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
