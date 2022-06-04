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


public class FanActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subpage_fan);
        final int[] flag = {0};

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
                Intent intent =new Intent(FanActivity.this,maininterface.class);
                startActivity(intent);
            }
        });

    }
}
