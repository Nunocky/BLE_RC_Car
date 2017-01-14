package org.nunocky.bleshieldstudy01;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableField;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.nunocky.bleshieldstudy01.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    final int REQUEST_ENABLE_BT = 0;

    public final ObservableField<String> mStatusStr = new ObservableField<>();
    public boolean isScanning;

    public ArrayList<BluetoothDevice> deviceList = new ArrayList<>();
    //    private BluetoothAdapter mBluetoothAdapter;
    //    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothDeviceListAdapter mListAdapter;

//    private BluetoothGatt bluetoothGatt;

    private BLEScanSupport mBLEScanSupport;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setUi(this);

        binding.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onListItemClicked(view, position);
            }
        });

        mStatusStr.set("initialized.");
        isScanning = false;
    }

    @Override
    protected void onResume() {
        BluetoothAdapter mBluetoothAdapter;
        BluetoothLeScanner mBluetoothLeScanner;

        super.onResume();

        mListAdapter = new BluetoothDeviceListAdapter(this, deviceList);

        ListView lv = (ListView) findViewById(R.id.listView);
        lv.setAdapter(mListAdapter);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        // mBluetoothLeScannerの初期化
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (mBluetoothLeScanner == null) {
            Toast.makeText(this, "Scanner initialize failed", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mBLEScanSupport = new BLEScanSupport(this, deviceList, mBluetoothLeScanner);
    }

    public void onScanButtonClicked(View view) {
        Log.d(TAG, "onScanButtonClicked");
        if (isScanning) {
            mBLEScanSupport.stopScan();
        } else {
            mBLEScanSupport.scan(null, null, true, new BLEScanSupport.Callback() {
                @Override
                void onScanStarted() {
                    Log.d(TAG, "onScanStarted");
                    mStatusStr.set("SCANNING");
                    isScanning = true;
                    deviceList.clear();
                    mListAdapter.notifyDataSetChanged();
                }

                @Override
                void onScanStopped() {
                    Log.d(TAG, "onScanStopped");
                    mStatusStr.set("IDLE");
                    isScanning = false;
                }

                @Override
                void onDeviceFound(BluetoothDevice device) {
                    Log.d(TAG, "onDeviceFound: ");
                    final String name = device.getName();
                    if (name != null) {
                        Log.d(TAG, "found " + name);
                    }
                    mListAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    public void onListItemClicked(View view, int position) {
        mBLEScanSupport.stopScan();

        BluetoothDevice dev = deviceList.get(position);

        Log.d(TAG, "onItemClick: " + dev.getName());
        // devを次のアクティビティへ送る
        Intent intent = new Intent(this, CommunicationActivity.class);
        intent.putExtra("device", dev);
        startActivity(intent);
    }


}
