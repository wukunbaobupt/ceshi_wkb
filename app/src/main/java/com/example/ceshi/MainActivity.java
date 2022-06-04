package com.example.ceshi;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
    EditText username, pwd;
    Mysql mysql;
    SQLiteDatabase db;
    SharedPreferences sp1,sp2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        username = findViewById(R.id.ed_1);
        pwd = findViewById(R.id.ed_2);
        Button mBtnEditText = (Button) findViewById(R.id.btn_edittext);
        Button mBtnregister = (Button) findViewById(R.id.btn_register);
        //关联用户名 密码 两个按钮

        sp1=this.getSharedPreferences("useinfo", MODE_PRIVATE);
        sp2=this.getSharedPreferences("username", MODE_PRIVATE);

        //将输入的用户名和密码临时存储，等待后续比对数据库

        username.setText((sp1.getString("usname",null)));
        pwd.setText(sp1.getString("uspwd",null));
        mysql = new Mysql(this,"Userinfo",null,1);
        db=mysql.getReadableDatabase();
        //取数据库



        mBtnEditText.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String name=username.getText().toString();
                String passward=pwd.getText().toString();
            //获取用户名和密码

                Cursor cursor=db.query("logins",new String[]{"usname","uspwd"},"usname=? and uspwd=?",new String[]{name,passward},null,null,null);

                //查询用户名和密码存在于数据库
                int flag=cursor.getCount();
                //查询出来的记录项条数
                if(flag!=0){
                    Toast.makeText(MainActivity.this,"登录成功！",Toast.LENGTH_SHORT).show();

                    //若查询出来不为零，则跳转到主界面

                    Intent intent=new Intent(MainActivity.this,maininterface.class);
                    startActivity(intent);

                }else{
                    if(name.equals("")){
                        Toast.makeText(MainActivity.this,"请输入用户名！",Toast.LENGTH_LONG).show();
                    }
                    else if(passward.equals("")){
                        Toast.makeText(MainActivity.this,"请输入密码！",Toast.LENGTH_LONG).show();
                    }
                    else{

                    Toast.makeText(MainActivity.this,"用户名或密码错误！",Toast.LENGTH_LONG).show();
                }
                }
            }

        });

        mBtnregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到注册界面
                Intent intent=new Intent(MainActivity.this,SignUpActivity.class);
                startActivity(intent);
            }
        });
}

}

