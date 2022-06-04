package com.example.ceshi;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SignUpActivity extends Activity {
    EditText username2, pwd1,pwd2;
    Mysql mysql;

    SQLiteDatabase db;
    SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        //关联用户名 密码 确认密码 注册按钮
        Button mbtnconfirmregister = findViewById(R.id.btn_confirmregister);
        username2=findViewById(R.id.ed_3);
        pwd1=findViewById(R.id.ed_4);
        pwd2=findViewById(R.id.ed_5);

        //建立数据库
        mysql=new Mysql(this,"Userinfo",null,1);
        db=mysql.getReadableDatabase();
        sp=this.getSharedPreferences("userinfo", MODE_PRIVATE);


        //立即注册按钮监听
        mbtnconfirmregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strNewUsername=username2.getText().toString().trim();
                String strNewPassward1=pwd1.getText().toString().trim();
                String strNewPassward2=pwd2.getText().toString().trim();
                boolean flag=true;//判断用户是否已存在的标志
                //粗略检查格式
                Cursor cursor=db.query("logins",new String[]{"usname"},
                        null,null,null,null,null);
                if(strNewUsername.length()>10){
                    Toast.makeText(SignUpActivity.this,
                            "用户名长度必须小于10字符！",
                            Toast.LENGTH_SHORT).show();
                }
                else if(strNewUsername.length()<2){
                    Toast.makeText(SignUpActivity.this,"用户名长度不能小于2字符",Toast.LENGTH_SHORT).show();
                }
                else if(strNewPassward1.length()>16){
                    Toast.makeText(SignUpActivity.this,
                            "密码长度必须小于16字符！",
                            Toast.LENGTH_SHORT).show();

                }
                else if(strNewPassward1.length()<3){
                    Toast .makeText(SignUpActivity.this,"密码长度不能小于3字符!",Toast.LENGTH_SHORT).show();
                }
                else if(!strNewPassward2.equals(strNewPassward1)){
                    Toast.makeText(SignUpActivity.this,"两次密码输入不一致！",Toast.LENGTH_SHORT).show();

                }
                else{
                    while(cursor.moveToNext()){
                        if(cursor.getString(0).equals(strNewUsername)){
                            flag=false;
                            Toast.makeText(SignUpActivity.this,"用户名已注册！",Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                    if(flag==true){
                        ContentValues cv=new ContentValues();
                        cv.put("usname",strNewUsername);
                        cv.put("uspwd",strNewPassward1);
                        db.insert("logins",null,cv);
                        SharedPreferences.Editor editor=sp.edit();
                        editor.putString("usname",strNewUsername);
                        editor.putString("uspwd",strNewPassward1);
                        Toast.makeText(SignUpActivity.this,"注册成功！",Toast.LENGTH_SHORT).show();
                        //跳转到登录界面
                        Intent intent =new Intent(SignUpActivity.this,MainActivity.class);
                        startActivity(intent);
                    }

                }

            }
        });
    }
}
