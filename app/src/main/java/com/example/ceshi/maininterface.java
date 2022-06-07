package com.example.ceshi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class maininterface extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface_main);

        //关联按钮
        Button mbtntemp = findViewById(R.id.btn_temp_wet);
        Button mbtnsecure=findViewById(R.id.btn_secure);
        Button mbtnfan=findViewById(R.id.btn_fan);
        Button mbtnlight=findViewById(R.id.btn_light);
        Button mbtnback=findViewById(R.id.btn_back2);

        //温湿度按钮监听
        mbtntemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到温度界面
                Intent intent=new Intent(maininterface.this,TempActivity.class);
                startActivity(intent);
            }
        });


        //智能安保按钮监听
        mbtnsecure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到界面
                Intent intent=new Intent(maininterface.this,SecureActivity.class);
                startActivity(intent);
            }
        });

        //风扇按钮监听
        mbtnfan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到界面
                Intent intent=new Intent(maininterface.this,FanActivity.class);
                startActivity(intent);
            }
        });

        //灯光按钮监听
        mbtnlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到界面
                Intent intent=new Intent(maininterface.this,LightActivity.class);
                startActivity(intent);
            }
        });


        //返回登录界面
        mbtnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(maininterface.this,MainActivity.class);
                startActivity(intent);
            }
        });


    }
}