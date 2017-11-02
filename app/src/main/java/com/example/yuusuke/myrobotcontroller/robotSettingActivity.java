package com.example.yuusuke.myrobotcontroller;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import static android.widget.Toast.LENGTH_SHORT;

public class robotSettingActivity extends AppCompatActivity {


    SharedPreferences pref;
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
        // メソッド呼び出し
        association();
    }

    // xmlと関連付けしたいものをまとめた
    void association(){
        //xmlとの関連付け
        frontLeft = (EditText)findViewById(R.id.editText1);
        frontRight = (EditText)findViewById(R.id.editText2);

        backLeft = (EditText)findViewById(R.id.editText3);
        backRight = (EditText)findViewById(R.id.editText4);

        rotationLeft = (EditText)findViewById(R.id.editText5);
        rotationRight = (EditText)findViewById(R.id.editText6);
    }
    @Override
    public void onResume(){
        super.onResume();
        // 保存ファイル名とmodeを指定 今回だと data という名前で、 このアプリ以外アクセスが出来ない設定
        pref = getSharedPreferences("data",MODE_PRIVATE);
        // 値を取得
        String frontLeftStr = pref.getString("frontLeft","100");
        String frontRightStr = pref.getString("frontRight","100");
        String backLeftStr = pref.getString("backLeft","100");
        String backRightStr = pref.getString("backRight","100");
        String rotationLeftStr = pref.getString("rotationLeft","100");
        String rotationRightStr = pref.getString("rotationRight","100");

        // editTextに文字列を貼り付け
        frontLeft.setText(frontLeftStr);
        frontRight.setText(frontRightStr);
        backLeft.setText(backLeftStr);
        backRight.setText(backRightStr);
        rotationLeft.setText(rotationLeftStr);
        rotationRight.setText(rotationRightStr);
    }

    public void save(View v){
        String errorText = null;
        if (!errorCheck(frontLeft.getText().toString())){
            errorText = "前進時の左";
        }
        else if (!errorCheck(frontRight.getText().toString())){
            errorText = "前進時の右";
        }
        else if (!errorCheck(backLeft.getText().toString())){
            errorText = "後退時の左";
        }
        else if (!errorCheck(backRight.getText().toString())){
            errorText = "後退時の右";
        }
        else if (!errorCheck(rotationLeft.getText().toString())){
            errorText = "回転時の左";
        }
        else if (!errorCheck(rotationRight.getText().toString())){
            errorText = "回転時の右";
        }
        if (errorText != null){
            Toast.makeText(this,errorText+"値が0~255の範囲内にありません",LENGTH_SHORT).show();
        }
        // 保存して良い時
        else{
            SharedPreferences.Editor editor;
            // SharedPreferencesに書くときに使う Editor の使用準備
            editor = pref.edit();
            // 書き込みデータを指定 key と 書き込みたいデータ
            editor.putString("frontLeft",textShap(frontLeft));
            editor.putString("frontRight",textShap(frontRight));
            editor.putString("backLeft",textShap(backLeft));
            editor.putString("backRight",textShap(backRight));
            editor.putString("rotationLeft",textShap(rotationLeft));
            editor.putString("rotationRight",textShap(rotationRight));
            // これをしないと書き込まれないので注意
            editor.apply();
            Toast.makeText(this, "保存しました", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    boolean errorCheck(String numStr){
        if (numStr.equals("")){
            return false;
        }
        // 文字列型を数字に変換
        int num = Integer.parseInt(numStr);
        // 値が 0以上 かつ 255以下の時
        if (num >=0 && num <= 255){
            // trueを返す
            return true;
        }
        // falseを返す
        return false;
    }

    // editTextに入っている数字を3桁に整形する
    String textShap(EditText editText){
        // ここでeditTextの数字で、0が先頭にあったら削除している
        String num = Integer.valueOf(editText.getText().toString()).toString();
        if (num.length() == 1){
            num = "0"+"0"+num;
        }else if (num.length() == 2){
            num = "0"+num;
        }
        return num;
    }
}
