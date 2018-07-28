package com.example.yuusuke.myrobotcontroller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothSPP.BluetoothConnectionListener;
import app.akexorcist.bluetotohspp.library.BluetoothSPP.OnDataReceivedListener;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class MainActivity extends AppCompatActivity {
    private BluetoothSPP bluetoothSPP = null;  //bluetooth

    // 速度の値が入る変数
    // 前進の時
    private String frontLeftStr, frontRightStr;
    // 後退の時
    private String backLeftStr, backRightStr;
    // 回転の時
    private String rotationLeftStr, rotationRightStr;

    //たまビュー
    private ImageView tamaV;
    private int preDx, preDy, newDx, newDy;

    private int VIEW_HEIGHT, VIEW_WIDTH;
    private FrameLayout frameLayout;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tamaV = (ImageView) findViewById(R.id.tama);

        bluetoothSPP = new BluetoothSPP(this);

        if (!bluetoothSPP.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }

        bluetoothSPP.setOnDataReceivedListener(new OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

        bluetoothSPP.setBluetoothConnectionListener(new BluetoothConnectionListener() {
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

        frameLayout = (FrameLayout) findViewById(R.id.frame_layout);

    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences pref;
        // 保存ファイル名とmodeを指定 今回だと data という名前で、 このアプリ以外アクセスが出来ない設定
        pref = getSharedPreferences("data", MODE_PRIVATE);

        // 値を取得
        frontLeftStr = pref.getString("frontLeft", "100");
        frontRightStr = pref.getString("frontRight", "100");
        backLeftStr = pref.getString("backLeft", "100");
        backRightStr = pref.getString("backRight", "100");
        rotationLeftStr = pref.getString("rotationLeft", "100");
        rotationRightStr = pref.getString("rotationRight", "100");
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
                intent = new Intent(this, robotSettingActivity.class);
                startActivity(intent);
                break;
            case R.id.item_connect:
                //接続
                if (bluetoothSPP.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bluetoothSPP.disconnect();
                } else {
                    intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public void onDestroy() {
        super.onDestroy();
        bluetoothSPP.stopService();
    }

    public void onStart() {
        super.onStart();
        if (!bluetoothSPP.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!bluetoothSPP.isServiceAvailable()) {
                bluetoothSPP.setupService();
                bluetoothSPP.startService(BluetoothState.DEVICE_OTHER);
            }
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bluetoothSPP.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bluetoothSPP.setupService();
                bluetoothSPP.startService(BluetoothState.DEVICE_OTHER);
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    void tamaSetup() {
        preDx = preDy = newDx = newDy = 0;

        tamaV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // x,y 位置取得
                newDx = (int) event.getRawX();
                newDy = (int) event.getRawY();

                VIEW_HEIGHT = frameLayout.getHeight() + tamaV.getHeight();
                VIEW_WIDTH = frameLayout.getWidth() + tamaV.getWidth();

                switch (event.getAction()) {
                    // タッチダウンでdragされた
                    case MotionEvent.ACTION_MOVE:
                        // ACTION_MOVEでの位置
                        int dx = tamaV.getLeft() + (newDx - preDx);
                        int dy = tamaV.getTop() + (newDy - preDy);

                        // 画像の位置を設定する
                        //右回転
                        if (dx > (VIEW_WIDTH / 2) - tamaV.getWidth() / 2) {
                            dx = (VIEW_WIDTH / 2) - tamaV.getWidth() / 2;
                            bluetoothSPP.send("4" + rotationLeftStr + rotationRightStr, false);
                        }
                        //左回転
                        if (dx < (VIEW_WIDTH / 2) - tamaV.getWidth() - tamaV.getWidth() / 2) {
                            dx = (VIEW_WIDTH / 2) - tamaV.getWidth() - tamaV.getWidth() / 2;
                            bluetoothSPP.send("3" + rotationLeftStr + rotationRightStr, false);
                        }
                        //前進
                        if (dy < (VIEW_HEIGHT / 2) - tamaV.getHeight() - tamaV.getHeight() / 2) {
                            dy = (VIEW_HEIGHT / 2) - tamaV.getHeight() - tamaV.getHeight() / 2;
                            bluetoothSPP.send("1" + frontLeftStr + frontRightStr, false);
                        }
                        //後進
                        if (dy > (VIEW_HEIGHT / 2) - tamaV.getHeight() / 2) {
                            dy = (VIEW_HEIGHT / 2) - tamaV.getHeight() / 2;
                            bluetoothSPP.send("2" + backLeftStr + backRightStr, false);
                        }
                        tamaV.layout(dx, dy, dx + tamaV.getWidth(), dy + tamaV.getHeight());

                        Log.d("onTouch", "ACTION_MOVE: dx=" + dx + ", dy=" + dy + "," + newDx + "," + newDy);

                        break;

                    //指が離れた時
                    case MotionEvent.ACTION_UP:
                        //中心は（VIEW_WIDTH /2, VIEW_HEIGHT /2）である
                        tamaV.layout((VIEW_WIDTH / 2) - tamaV.getWidth(), (VIEW_HEIGHT / 2) - tamaV.getHeight(), VIEW_WIDTH / 2, VIEW_HEIGHT / 2);
                        bluetoothSPP.send("5" + "000" + "000", false);
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
