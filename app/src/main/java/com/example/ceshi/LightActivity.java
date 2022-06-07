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


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
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


//import android.os.Bundle;
//import android.os.Message;
//import android.view.View;
import android.widget.CompoundButton;
//import android.widget.ImageView;
import android.widget.Switch;
//import android.widget.TextView;



public class LightActivity extends Activity {

    private  Switch switch_voice;
    private  Switch switch_light;
    private  ImageView image_led;
    private  int led_auto=0;
    private  int led_auto_voice=0;
    private  int led_auto_light=0;
    private  int led_flag=0;
    private  int light_value=0;

    private Button light_state_button;
    private TextView light_state_text;
    private TextView light_intensity_text;
    private TextView led_flag_text;

    //mqtt参数配置
    private String host = "tcp://120.24.88.132:1883";
    private String userName = "wkb";
    private String passWord = "123456";
    private String mqtt_id = "2473775327_light"; //定义成自己的QQ号  切记！不然会掉线！！！
    private String mqtt_sub_topic = "light_mode"; //为了保证你不受到别人的消息  哈哈
    private String mqtt_pub_topic = "light_mode_ESP"; //为了保证你不受到别人的消息  哈哈  自己QQ好后面加 _PC
    private ScheduledExecutorService scheduler;
    private MqttClient client;
    private MqttConnectOptions options;
    private Handler handler;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subpage_light);
        final int[] flag = {0};

        //button_switch监听
        Button mbtn_switch = findViewById(R.id.btn_open5);
        Button mbtn_back1=findViewById(R.id.btn_back1);
        mbtn_switch.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override

            public void onClick(View v) {
                //开关状态改变
                if (flag[0] == 0) {
                    mbtn_switch.setBackground(getResources().getDrawable(R.drawable.switch3));



                } else {
                    mbtn_switch.setBackground(getResources().getDrawable(R.drawable.switch1));
                }
                flag[0] = (flag[0] + 1) % 2;

            }
        });

        //返回主界面监听
        mbtn_back1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //跳转到主界面
                Intent intent =new Intent(LightActivity.this,maininterface.class);
                startActivity(intent);
            }
        });

        //控件id配置
        switch_voice=findViewById(R.id.switch_voice);
        switch_light=findViewById(R.id.switch_light);
        image_led=findViewById(R.id.image_led);
        light_state_button = findViewById(R.id.light_state_button);
        light_state_text = findViewById(R.id.light_state_text);
        light_intensity_text = findViewById(R.id.light_intensity_text);
        led_flag_text = findViewById(R.id.led_flag_text);

        //switch 点击事件

        switch_voice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    led_auto_voice=1;
                    led_auto=1;
                    Toast.makeText(LightActivity.this,"开启声控模式" ,Toast.LENGTH_SHORT).show();
                    if (led_auto_light==1)
                    {
                        publishmessageplus(mqtt_pub_topic,"{\"led_auto\":1,\"led_auto_voice\":1,\"led_auto_light\":1}");
                    }
                    else
                    {
                        publishmessageplus(mqtt_pub_topic,"{\"led_auto\":1,\"led_auto_voice\":1,\"led_auto_light\":0}");
                    }
                }
                else
                {
                    led_auto_voice=0;
                    if(led_auto_light==1)
                    {
                        led_auto=1;
                        publishmessageplus(mqtt_pub_topic,"{\"led_auto\":1,\"led_auto_voice\":0,\"led_auto_light\":1}");
                        Toast.makeText(LightActivity.this,"关闭声控模式" ,Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        led_auto=0;
                        publishmessageplus(mqtt_pub_topic,"{\"led_auto\":0,\"led_auto_voice\":0,\"led_auto_light\":0}");
                        Toast.makeText(LightActivity.this,"手动遥控模式" ,Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        switch_light.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    led_auto_light=1;
                    led_auto=1;
                    Toast.makeText(LightActivity.this,"开启光控模式" ,Toast.LENGTH_SHORT).show();
                    if (led_auto_voice==1)
                    {
                        publishmessageplus(mqtt_pub_topic,"{\"led_auto\":1,\"led_auto_voice\":1,\"led_auto_light\":1}");

                    }
                    else
                    {
                        publishmessageplus(mqtt_pub_topic,"{\"led_auto\":1,\"led_auto_voice\":0,\"led_auto_light\":1}");
                    }
                }
                else
                {
                    led_auto_light=0;
                    if(led_auto_voice==1)
                    {
                        led_auto=1;
                        publishmessageplus(mqtt_pub_topic,"{\"led_auto\":1,\"led_auto_voice\":1,\"led_auto_light\":0}");
                        Toast.makeText(LightActivity.this,"关闭光控模式" ,Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        led_auto=0;
                        publishmessageplus(mqtt_pub_topic,"{\"led_auto\":0,\"led_auto_voice\":0,\"led_auto_light\":0}");
                        Toast.makeText(LightActivity.this,"手动遥控模式" ,Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
        image_led.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(led_auto==0)
                {
                    if(led_flag==0)
                    {
                        publishmessageplus(mqtt_pub_topic,"{\"led_auto\":0,\"led_auto_voice\":0,\"led_auto_light\":0,\"set_led\":1}");
                        Toast.makeText(LightActivity.this,"灯光已关闭" ,Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        publishmessageplus(mqtt_pub_topic,"{\"led_auto\":0,\"led_auto_voice\":0,\"led_auto_light\":0,\"set_led\":0}");
                        Toast.makeText(LightActivity.this,"灯光已关闭" ,Toast.LENGTH_SHORT).show();

                    }
                }
            }
        });


//***************************************************************************
        //mqtt
        Mqtt_init();
        startReconnect();
        handler = new Handler() {
            @SuppressLint("SetTextI18n")
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 1: //开机校验更新回传
                        break;
                    case 2:  // 反馈回传

                        break;
                    case 3:  //MQTT 收到消息回传   UTF8Buffer msg=new UTF8Buffer(object.toString());
                        String light_val = msg.obj.toString().substring(msg.obj.toString().indexOf("light_value\":")+13,msg.obj.toString().indexOf(","));
                        String led_val = msg.obj.toString().substring(msg.obj.toString().indexOf("led_flag\":")+10,msg.obj.toString().indexOf("}"));
                        //显示室内光强
                        light_intensity_text.setText(light_val);
                        led_flag_text.setText(led_val);
                        //将光强度string 转为float 方便接下来进行统计
                          light_value= Integer.parseInt(light_val);
                          led_flag=Integer.parseInt(led_val);//更新灯光状态

                        //Toast.makeText(MainActivity.this,T_val ,Toast.LENGTH_SHORT).show();

                        break;
                    case 30:  //连接失败
//                        Toast.makeText(LightActivity.this,"连接失败" ,Toast.LENGTH_SHORT).show();
                        break;
                    case 31:   //连接成功
//                        Toast.makeText(LightActivity.this,"连接成功" ,Toast.LENGTH_SHORT).show();
                        light_state_text.setText("在线");
                        light_state_button.setBackground(getResources().getDrawable(R.drawable.shape_circle_green));
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
                    Message msg = new Message();
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
