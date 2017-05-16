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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nifty.cloud.mb.core.DoneCallback;
import com.nifty.cloud.mb.core.NCMB;
import com.nifty.cloud.mb.core.NCMBException;
import com.nifty.cloud.mb.core.NCMBObject;

import app.akexorcist.bluetotohspp.library.BluetoothSPP.BluetoothConnectionListener;
import app.akexorcist.bluetotohspp.library.BluetoothSPP.OnDataReceivedListener;

public class MainActivity extends AppCompatActivity implements OnClickListener {
    BluetoothSPP bt = null;  //bluetooth
    Chronometer chronometer;
    Button startButton;
    Button stopButton;
    Button clearButton;
    AlertDialog alertDialog;
    View inputView;  //Dialogレイアウトの取得用変数
    TextView textView;
    EditText editName;
    NCMBObject obj;

    long startTime=0;			// スタートした時の時間
    long stopTime=0;			// ストップした時の時間
    long start_stop_time =0;		// スタートとストップの差の時間
    long time=0;				// 現在のクロノメータータイムとstartstopTimeの差


    // 状態の判断材料
    boolean flag=false; 		// trueにすると起動すぐに内部でカウント開始されてしまう
    boolean startFlag=true;		// スタートボタンの状態
    boolean stopFlag=false;		// ストップボタンの状態

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chronometer=(Chronometer)findViewById(R.id.chronometer);
        chronometer.setOnClickListener(this);

        startButton = (Button)findViewById(R.id.timerStart);
        startButton.setOnClickListener(this);

        stopButton = (Button)findViewById(R.id.timerstop);
        stopButton.setOnClickListener(this);

        clearButton = (Button)findViewById(R.id.timerClear);
        clearButton.setOnClickListener(this);

        //Dialogレイアウト呼び出し
        LayoutInflater inflater = LayoutInflater.from(this);
        inputView = inflater.inflate(R.layout.result_dialog,null);

        textView = (TextView)inputView.findViewById(R.id.text);
        editName = (EditText)inputView.findViewById(R.id.editText);

        setDialog();  //Dialogの設定

        //mBaas初期化
        NCMB.initialize(this.getApplicationContext(),"33c9c7da896342f619f12945a88d3f02d600a05bd22e222bc1d1280512e806b1"
                ,"eda878b816c799a03e098cd20db19e1e4453e4f2f9e45cae90a0655717a6a4e2");


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
            case R.id.connectTest:
                // 接続ボタンの処理

                if(bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bt.disconnect();
                } else {
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
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
                setup();
            }
        }
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
    }


    @Override
    public void onClick(View view) {
        switch(view.getId()){
            // スタートボタンを押した時(2パターン)
            case R.id.timerStart:
                if(!flag & startFlag){		// 最初にボタンが押された時
                    chronometer.setBase(SystemClock.elapsedRealtime());	// 0にする
                    chronometer.start();								// クロノメーターをスタート
                    startTime=SystemClock.elapsedRealtime();			// スタート時間(計算時に必要)
                    flag=true;											// flagを有効にする

                }else if(flag & startFlag){			// 2回目以降にボタンが押された時
                    start_stop_time =stopTime-startTime;	// スタートとストップの時間の差を計算
                    time=SystemClock.elapsedRealtime()- start_stop_time;	// スタートから上の差を引いた数を計算
                    chronometer.setBase(time);	// セットする
                    startTime=time;				// timeをスタートする時間に代入する
                    chronometer.start();		// クロノメーターをスタート
                }

                startFlag=false;									// startFlagを無効にする
                stopFlag=true;										// stopFlagを有効にする

                break;

            // ストップボタンを押した時
            case R.id.timerstop:
                if(stopFlag){
                    chronometer.stop();						// クロノメーターをストップ
                    stopTime=SystemClock.elapsedRealtime();	// ストップした時間を取得
                    startFlag=true;							// startFlagを有効にする
                    stopFlag=false;							// stopFlagを有効にする

                    alertDialog.setMessage(chronometer.getText().toString());

                    // ダイアログ表示
                    alertDialog.show();
                }
                break;

            // クリアボタンを押した時
            case R.id.timerClear:
                chronometer.stop();						// クロノメーターをストップ
                chronometer.setBase(SystemClock.elapsedRealtime());	// 0にする
                flag=false;								// フラグを無効にする
                startFlag=true;							// startFlagを有効にする
                stopFlag=true;							// stopFlagを有効にする
                break;
        }
    }

    public void setDialog(){
        if(alertDialog == null) {
            alertDialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("結果")
                    .setView(inputView)
                    .setPositiveButton(
                            "ランキング登録",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // ランキング登録 ボタンクリック処理
                                    Toast.makeText(MainActivity.this, "アップロードしました", Toast.LENGTH_SHORT).show();

                                    //Cloudにデータ送信

                                    //保存するインスタンスを作成
                                    obj = new NCMBObject("GameScore");

                                    //値を設定
                                    obj.put("Name", editName.getText().toString());
                                    obj.put("Time", chronometer.getText().toString());

                                    //保存を実施
                                    obj.saveInBackground(new DoneCallback() {
                                        @Override
                                        public void done(NCMBException e) {
                                            if (e != null) {
                                                //保存が失敗した場合の処理
                                                Log.d("NCMB", "保存に失敗しました。エラー:" + e.getMessage());
                                            } else {
                                                //保存が成功した場合の処理
                                                Log.d("NCMB", "保存に成功しました。");
                                            }
                                        }
                                    });
                                }
                            }
                    )

                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .create();
        }
    }
}
