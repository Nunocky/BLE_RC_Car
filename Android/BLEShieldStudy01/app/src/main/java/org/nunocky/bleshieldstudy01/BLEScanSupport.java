package org.nunocky.bleshieldstudy01;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BLEScanSupport {
    private static final String TAG = "BLEScanSupport";

    // BLEスキャンのタイムアウト時間
    private static final long SCAN_PERIOD = 10000;

    private boolean isScanning;

    private Handler mHandler = new Handler();

    private ArrayList<BluetoothDevice> deviceList;
    //     private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanCallback mScanCallback;
    private Callback mCallback;

    private BLEScanSupport() {

    }

    BLEScanSupport(Context context, ArrayList<BluetoothDevice> ary, BluetoothLeScanner scanner) {
        deviceList = ary;
        mBluetoothLeScanner = scanner;
    }

    // ScanCallbackの初期化
    private ScanCallback initCallbacks() {
        return new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);

                if (result != null && result.getDevice() != null) {
                    if (isAdded(result.getDevice())) {
                        // No add
                    } else {
                        saveDevice(result.getDevice());
                    }
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }
        };
    }

    // スキャン実施
    void scan(@Nullable List<ScanFilter> filters, @Nullable ScanSettings settings,
              boolean enable, @Nullable Callback callback) {

        mScanCallback = initCallbacks();
        mCallback = callback;

        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isScanning = false;
                    mBluetoothLeScanner.stopScan(mScanCallback);
                    if (mCallback != null) {
                        mCallback.onScanStopped();
                    }
                }
            }, SCAN_PERIOD);

            isScanning = true;
            if (filters != null) {
                // スキャンフィルタを設定するならこちら
                mBluetoothLeScanner.startScan(filters, settings, mScanCallback);
            } else {
                mBluetoothLeScanner.startScan(mScanCallback);
                if (mCallback != null) {
                    mCallback.onScanStarted();
                }
            }
        } else {
            isScanning = false;
            mBluetoothLeScanner.stopScan(mScanCallback);
            if (mCallback != null) {
                mCallback.onScanStopped();
            }
        }
    }

    // スキャン停止
    void stopScan() {
        if (mBluetoothLeScanner != null) {
            mBluetoothLeScanner.stopScan(mScanCallback);
            if (mCallback != null) {
                mCallback.onScanStopped();
            }
        }
    }

    // スキャンしたデバイスのリスト保存
    void saveDevice(BluetoothDevice device) {
        if (deviceList == null) {
            deviceList = new ArrayList<>();
        }

        if (device.getName() == null)
            return;

        deviceList.add(device);

        if (mCallback != null) {
            mCallback.onDeviceFound(device);
        }
    }

    // スキャンしたデバイスがリストに追加済みかどうかの確認
    private boolean isAdded(BluetoothDevice device) {

        if (device.getName() == null)
            return true;

        if (deviceList != null && deviceList.size() > 0) {
            return deviceList.contains(device);
//            for (BluetoothDevice dev : deviceList) {
//                if (dev.getAddress().equals(device.getAddress()))
//                    return true;
//            }
        }

        return false;
    }

    static abstract class Callback {
        abstract void onScanStarted();

        abstract void onScanStopped();

        abstract void onDeviceFound(BluetoothDevice device);
    }


}
