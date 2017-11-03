package com.example.yuusuke.myrobotcontroller;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;
import android.bluetooth.BluetoothAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nifty.cloud.mb.core.DoneCallback;
import com.nifty.cloud.mb.core.NCMB;
import com.nifty.cloud.mb.core.NCMBException;
import com.nifty.cloud.mb.core.NCMBObject;

import app.akexorcist.bluetotohspp.library.BluetoothSPP.BluetoothConnectionListener;
import app.akexorcist.bluetotohspp.library.BluetoothSPP.OnDataReceivedListener;

public class MainActivity extends AppCompatActivity{
    BluetoothSPP bt = null;  //bluetooth
    View inputView;  //Dialogレイアウトの取得用変数
    TextView textView;
    EditText editName;


    //たまビュー
    ImageView tamaV;
    private int preDx, preDy, newDx, newDy;

    int defX,defY;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tamaV = (ImageView) findViewById(R.id.tama);
        //Dialogレイアウト呼び出し
        LayoutInflater inflater = LayoutInflater.from(this);
        inputView = inflater.inflate(R.layout.result_dialog,null);

        textView = (TextView)inputView.findViewById(R.id.text);
        editName = (EditText)inputView.findViewById(R.id.editText);

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
        tamaSetup();
    }

    // メニュー作成
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    // メニューアイテム選択イベント
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_setting:
                // 接続ボタンの処理
                Intent intent = new Intent(this,robotSettingActivity.class);
                startActivity(intent);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public void connect(View v){
        if(bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
            bt.disconnect();
        } else {
            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        bt.stopService();
    }

    public void onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if(!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
            }
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    /*public void btn(View view) {

        switch (view.getId()){
            //前進するとき
            case R.id.front:
                bt.send("0001", false);
                break;
            //止まるとき
            case R.id.stop:
                bt.send("0005", false);
                break;
            //左回転するとき
            case R.id.left:
                bt.send("0003", false);
                break;
            //右回転するとき
            case R.id.right:
                bt.send("0004", false);
                break;
            //後ろに行くとき
            case R.id.back:
                bt.send("0002", false);
                break;
        }
    }*/

    void tamaSetup(){
        preDx = preDy = newDx = newDy = 0;
        defX=tamaV.getWidth();
        defY=tamaV.getHeight();
        tamaV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // x,y 位置取得
                newDx = (int) event.getRawX();
                newDy = (int) event.getRawY();

                switch (event.getAction()) {
                    // タッチダウンでdragされた
                    case MotionEvent.ACTION_MOVE:
                        // ACTION_MOVEでの位置
                        int dx = tamaV.getLeft() + (newDx - preDx);
                        int dy = tamaV.getTop() + (newDy - preDy);

                        // 画像の位置を設定する
                        //右回転
                        if (dx > 450) {
                            dx = 450;
                            bt.send("0004", false);
                        }
                        //左回転
                        else if (dx < 250) {
                            dx = 250;
                            bt.send("0003", false);
                        }
                        //前進
                        if (dy < 0) {
                            dy = 0;
                            bt.send("0001", false);
                        }
                        //後進
                        else if (dy > 200) {
                            dy = 200;
                            bt.send("0002", false);
                        }
                        tamaV.layout(dx, dy, dx + tamaV.getWidth(), dy + tamaV.getHeight());

                        Log.d("onTouch", "ACTION_MOVE: dx=" + dx + ", dy=" + dy + "," + newDx + "," + newDy);

                        break;

                    //指が離れた時
                    case MotionEvent.ACTION_UP:
                        tamaV.layout(350, 80, 350+tamaV.getWidth(), 80+tamaV.getHeight());
                        bt.send("0005", false);
                        break;
                }

                // タッチした位置を古い位置とする
                preDx = newDx;
                preDy = newDy;

                return true;
            }
        });

    }
}
