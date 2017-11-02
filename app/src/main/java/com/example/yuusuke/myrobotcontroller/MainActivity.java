package com.example.yuusuke.myrobotcontroller;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;
import android.bluetooth.BluetoothAdapter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import app.akexorcist.bluetotohspp.library.BluetoothSPP.BluetoothConnectionListener;
import app.akexorcist.bluetotohspp.library.BluetoothSPP.OnDataReceivedListener;
public class MainActivity extends AppCompatActivity {

    BluetoothSPP bt;

    // 速度の値が入る変数
    // 前進の時
    String frontLeftStr;
    String frontRightStr;
    // 後退の時
    String backLeftStr;
    String backRightStr;
    // 回転の時
    String rotationLeftStr;
    String rotationRightStr;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bt = new BluetoothSPP(this);

        if(!bt.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }

        bt.setOnDataReceivedListener(new OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

        bt.setBluetoothConnectionListener(new BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                Toast.makeText(getApplicationContext()
                        , "接続 to " + name + "\n" + address
                        , Toast.LENGTH_SHORT).show();
            }

            public void onDeviceDisconnected() {
                Toast.makeText(getApplicationContext()
                        , "接続が切れました", Toast.LENGTH_SHORT).show();
            }

            public void onDeviceConnectionFailed() {
                Toast.makeText(getApplicationContext()
                        , "接続できません", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnConnect = (Button)findViewById(R.id.connect);
        btnConnect.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                if(bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bt.disconnect();
                } else {
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_settings:
                Log.d("test", "Settings Selected.");
                Intent intent = new Intent(this,robotSettingActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_help:
                Log.d("test", "Help selected.");
                break;
        }
        return true;
    }



    public void onDestroy() {
        super.onDestroy();
        bt.stopService();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if(!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
                setup();
            }
        }
    }
    @Override
    public void onResume(){
        super.onResume();
        SharedPreferences pref;
        SharedPreferences.Editor editor;
        // 保存ファイル名とmodeを指定 今回だと data という名前で、 このアプリ以外アクセスが出来ない設定
        pref = getSharedPreferences("data",MODE_PRIVATE);
        // SharedPreferencesに書くときに使う Editor の使用準備
        editor = pref.edit();

        // 値を取得
        frontLeftStr = pref.getString("frontLeft","000");
        frontRightStr = pref.getString("frontRight","000");
        backLeftStr = pref.getString("backLeft","000");
        backRightStr = pref.getString("backRight","000");
        rotationLeftStr = pref.getString("rotationLeft","000");
        rotationRightStr = pref.getString("rotationRight","000");
    }



    public void setup() {
//        Button btnSend = (Button)findViewById(R.id.btnSend);
//        btnSend.setOnClickListener(new OnClickListener(){
//            public void onClick(View v){
//                bt.send("0001", true);
//            }
//        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
                //setup();
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    public void btn(View view) {
        //バイブレーションの宣言
        //Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        //20ミリセックバイブする
        //vibrator.vibrate(50);

        switch (view.getId()){
            //前進するとき
            case R.id.front:
                bt.send("0001"+frontLeftStr+frontRightStr, false);
                break;
            //止まるとき
            case R.id.stop:
                bt.send("0005"+"000"+"000", false);
                break;
            //左回転するとき
            case R.id.left:
                bt.send("0003"+rotationLeftStr+rotationRightStr, false);
                break;
            //右回転するとき
            case R.id.right:
                bt.send("0004"+rotationLeftStr+rotationRightStr, false);
                break;
            //後ろに行くとき
            case R.id.back:
                bt.send("0002"+backLeftStr+backRightStr, false);
                break;


        }
    }

}
