package com.example.yuusuke.myrobotcontroller;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

public class robotSettingActivity extends AppCompatActivity {


    SharedPreferences pref;
    SharedPreferences.Editor editor;

    // 前進の時
    EditText frontLeft;
    EditText frontRight;
    // 後退の時
    EditText backLeft;
    EditText backRight;
    // 回転の時
    EditText rotationLeft;
    EditText rotationRight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot_setting);
        //xmlとの関連付け
        frontLeft = (EditText)findViewById(R.id.editText1);
        frontRight = (EditText)findViewById(R.id.editText2);

        backLeft = (EditText)findViewById(R.id.editText3);
        backRight = (EditText)findViewById(R.id.editText4);

        rotationLeft = (EditText)findViewById(R.id.editText5);
        rotationRight = (EditText)findViewById(R.id.editText6);



    }


}
