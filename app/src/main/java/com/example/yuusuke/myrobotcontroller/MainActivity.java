package com.example.yuusuke.myrobotcontroller;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;
import android.bluetooth.BluetoothAdapter;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import app.akexorcist.bluetotohspp.library.BluetoothSPP.BluetoothConnectionListener;
import app.akexorcist.bluetotohspp.library.BluetoothSPP.OnDataReceivedListener;

public class MainActivity extends AppCompatActivity{
    BluetoothSPP bt = null;  //bluetooth
    View inputView;  //Dialogレイアウトの取得用変数
    TextView textView;
    EditText editName;

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

    //たまビュー
    ImageView tamaV;
    private int preDx, preDy, newDx, newDy;

    int defX,defY;

    boolean tamaFrontFlag =false;
    boolean tamaBackFlag =false;
    boolean tamaLeftFlag =false;
    boolean tamaRightFlag =false;

    boolean rotateMode = true;  //true : その場で回転  false : 片方のタイヤを軸に回転

    int VIEW_HEIGHT;
    int VIEW_WIDTH;
    FrameLayout fL;

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

        fL=(FrameLayout) findViewById(R.id.frame_layout);

    }

    @Override
    public void onResume(){
        super.onResume();
        SharedPreferences pref;
        // 保存ファイル名とmodeを指定 今回だと data という名前で、 このアプリ以外アクセスが出来ない設定
        pref = getSharedPreferences("data",MODE_PRIVATE);

        // 値を取得
        frontLeftStr = pref.getString("frontLeft","100");
        frontRightStr = pref.getString("frontRight","100");
        backLeftStr = pref.getString("backLeft","100");
        backRightStr = pref.getString("backRight","100");
        rotationLeftStr = pref.getString("rotationLeft","100");
        rotationRightStr = pref.getString("rotationRight","100");
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
        Intent intent;
        switch (item.getItemId()) {
            case R.id.item_setting:
                // 設定
                intent = new Intent(this,robotSettingActivity.class);
                startActivity(intent);
                break;
            case R.id.item_connect:
                //接続
                if(bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bt.disconnect();
                } else {
                    intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
                break;

            case R.id.rotateMode:
                if(rotateMode){
                    Toast.makeText(this, "片方軸回転モード", Toast.LENGTH_SHORT).show();
                    rotateMode = false;
                }else{
                    Toast.makeText(this, "中心軸回転モード", Toast.LENGTH_SHORT).show();
                    rotateMode = true;
                }
                break;
        }
        return super.onOptionsItemSelected(item);
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

                VIEW_HEIGHT =fL.getHeight()+tamaV.getHeight();
                VIEW_WIDTH =fL.getWidth()+tamaV.getWidth();

                switch (event.getAction()) {
                    // タッチダウンでdragされた
                    case MotionEvent.ACTION_MOVE:
                        // ACTION_MOVEでの位置
                        int dx = tamaV.getLeft() + (newDx - preDx);
                        int dy = tamaV.getTop() + (newDy - preDy);

                        // 画像の位置を設定する
                        //右回転
                        if (dx > (VIEW_WIDTH /2)-tamaV.getWidth()/2) {
                            dx = (VIEW_WIDTH /2)-tamaV.getWidth()/2;
                            if(rotateMode) {
                                bt.send("0004"+rotationLeftStr+rotationRightStr, false);
                            }else{
                                bt.send("0007"+rotationLeftStr+rotationRightStr, false);
                            }
                            tamaRightFlag =true;
                        }
                        else{
                            tamaRightFlag =false;
                        }
                        //左回転
                        if (dx < (VIEW_WIDTH /2)-tamaV.getWidth()-tamaV.getWidth()/2) {
                            dx = (VIEW_WIDTH /2)-tamaV.getWidth()-tamaV.getWidth()/2;
                            if(rotateMode){
                                bt.send("0003"+rotationLeftStr+rotationRightStr, false);
                            }else{
                                bt.send("0006"+rotationLeftStr+rotationRightStr, false);
                            }
                            tamaLeftFlag = true;
                        }
                        else{
                            tamaLeftFlag = false;
                        }
                        //前進
                        if (dy < (VIEW_HEIGHT /2)-tamaV.getHeight()-tamaV.getHeight()/2) {
                            dy = (VIEW_HEIGHT /2)-tamaV.getHeight()-tamaV.getHeight()/2;
                            //if(!tamaFrontFlag){
                                bt.send("0001"+frontLeftStr+frontRightStr, false);
                            //}
                            tamaFrontFlag = true;
                        }
                        else{
                            tamaFrontFlag = false;
                        }
                        //後進
                        if (dy > (VIEW_HEIGHT /2)-tamaV.getHeight()/2) {
                            dy = (VIEW_HEIGHT /2)-tamaV.getHeight()/2;
                            //if (!tamaBackFlag){
                                bt.send("0002"+backLeftStr+backRightStr, false);
                            //}
                            tamaBackFlag = true;
                        }
                        else{
                            tamaBackFlag = false;
                        }
                        tamaV.layout(dx, dy, dx + tamaV.getWidth(), dy + tamaV.getHeight());

                        Log.d("onTouch", "ACTION_MOVE: dx=" + dx + ", dy=" + dy + "," + newDx + "," + newDy);

                        break;

                    //指が離れた時
                    case MotionEvent.ACTION_UP:
                        //tamaV.layout(350, 80, 350+tamaV.getWidth(), 80+tamaV.getHeight());
                        //中心は（VIEW_WIDTH /2, VIEW_HEIGHT /2）である
                        tamaV.layout((VIEW_WIDTH /2)-tamaV.getWidth(), (VIEW_HEIGHT /2)-tamaV.getHeight(),VIEW_WIDTH /2, VIEW_HEIGHT /2);
                        //tamaV.layout(0, 0, 0+tamaV.getWidth(), 0+tamaV.getHeight());
                        Log.d("AAAA",String.valueOf(VIEW_WIDTH/2)+","+String.valueOf(VIEW_HEIGHT/2));
                        Log.d("AAAA",String.valueOf((VIEW_WIDTH /2)-tamaV.getWidth())+","+String.valueOf((VIEW_HEIGHT /2)-tamaV.getHeight()));
                        bt.send("0005"+"000"+"000", false);
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
