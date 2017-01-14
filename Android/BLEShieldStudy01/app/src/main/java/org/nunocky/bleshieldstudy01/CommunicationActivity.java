package org.nunocky.bleshieldstudy01;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.UUID;

public class CommunicationActivity extends AppCompatActivity {

    private static final String TAG = "CommunicationActivity";
//    private BluetoothAdapter mBluetoothAdapter;
//    private BluetoothLeScanner mBluetoothLeScanner;


    private static final String UUID_UART_RX = "713d0003-503e-4c75-ba94-3148f18d941e";
    private static final String UUID_UART_TX = "713d0002-503e-4c75-ba94-3148f18d941e";

    private BluetoothGatt bluetoothGatt;
    private List<BluetoothGattService> serviceList;
    private BluetoothGattCharacteristic characteristic_uart_tx = null; // TODO 送信用 characteristicのフィールド

    public int speed = 127;
    public int direction = 1;

//    ActivityCommunicationBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CustomCanvas canvas = new CustomCanvas(this);
        setContentView(canvas);

        //    setContentView(R.layout.activity_communication);
//        binding = DataBindingUtil.setContentView(this, R.layout.activity_communication);
//        //Toolbar toolbar = (Toolbar) findViewById(binding.toolbar);
//        setSupportActionBar(binding.toolbar);
//
//        binding.radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                if (checkedId == R.id.radioButton1) {
//                    direction = -1;
//                } else if (checkedId == R.id.radioButton3) {
//                    direction = 1;
//                } else {
//                    direction = 0;
//                }
//                sendData();
//            }
//        });
//
//        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                speed = progress;
//                binding.textView.setText(new Integer(speed).toString());
//                sendData();
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        if (intent != null) {
            BluetoothDevice dev = intent.getParcelableExtra("device");
            connect(this, dev);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        disconnect();
        finish();
    }

    //==================================================================================
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            // 接続成功し、サービス取得
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                bluetoothGatt = gatt;
                discoverService();
            }

            switch (newState) {
                case BluetoothProfile.STATE_CONNECTING:
                    Log.d(TAG, "onConnectionStateChange: STATE_CONNECTING");
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.d(TAG, "onConnectionStateChange: STATE_DISCONNECTED");
                    //isConnected = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CommunicationActivity.this, "disconnected", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                    break;
                case BluetoothProfile.STATE_DISCONNECTING:
                    Log.d(TAG, "onConnectionStateChange: STATE_DISCONNECTING");
                    break;
                case BluetoothProfile.STATE_CONNECTED:
                default:
                    Log.d(TAG, "onConnectionStateChange: STATE_CONNECTED");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CommunicationActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                        }
                    });
                    //isConnected = true;
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            serviceList = gatt.getServices();

            for (BluetoothGattService s : serviceList) {
                if (characteristic_uart_tx == null) {
                    characteristic_uart_tx = s.getCharacteristic(UUID.fromString(UUID_UART_RX));
                    if (characteristic_uart_tx != null) {
                        Log.d(TAG, "UART RX found! (ﾟ∀ﾟ)");
                    }
                }

                // サービス一覧を取得したり探したりする処理
                // あとキャラクタリスティクスを取得したり探したりしてもよい
                Log.d(TAG, "onServicesDiscovered: ");
                for (BluetoothGattCharacteristic c : s.getCharacteristics()) {
                    Log.d(TAG, "onServicesDiscovered: Characteristic " + c.getUuid().toString());
                }
            }
        }
    };

    // Gattへの接続要求
    private void connect(Context context, BluetoothDevice device) {
        bluetoothGatt = device.connectGatt(context, false, mGattCallback);
        bluetoothGatt.connect();
    }

    private void disconnect() {
        bluetoothGatt.disconnect();
    }

    // サービス取得要求
    private void discoverService() {
        if (bluetoothGatt != null) {
            bluetoothGatt.discoverServices();
        }
    }

    private void sendData() {
        if (characteristic_uart_tx == null) {
            return;
        }
//        Log.d(TAG, "direction " + direction);
//        Log.d(TAG, "speed " + speed);

        byte[] bb = new byte[2];
        bb[0] = (byte) (direction & 0xff);
        bb[1] = (byte) (speed & 0xff);

//        String s = String.format("%02x, %02x", bb[0], bb[1]);
//        Log.d(TAG, s);

        characteristic_uart_tx.setValue(bb);
        bluetoothGatt.writeCharacteristic(characteristic_uart_tx);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ControlEvent event) {
        direction = event.direction;
        speed = event.speed;
        sendData();
    }
}
