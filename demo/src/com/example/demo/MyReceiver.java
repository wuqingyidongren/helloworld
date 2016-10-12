package com.example.demo;

import java.util.List;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.os.BatteryManager;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.example.util.ActivityController;
import com.example.util.NetManager;

public class MyReceiver extends BroadcastReceiver {

	private final static String TAG = "lujiangfei";
	
	private NetManager manager;

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		manager = new NetManager(context);
		Log.d(TAG, "MyReceiver action = " + action);

		switch (action) {
		case Intent.ACTION_BOOT_COMPLETED:

			// 系统开机后拦截app的启动

			IActivityManager default1 = ActivityManagerNative.getDefault();
			try {
				default1.setActivityController(new ActivityController());
			} catch (RemoteException e) {
				e.printStackTrace();
			}

			Log.d(TAG, "开机：" + System.currentTimeMillis());
			int netType = manager.getNetType();
			Log.d(TAG, "-1,无网络；0，手机；１,wifi：" + netType);


			List<PackageInfo> installedPackages = context.getPackageManager()
					.getInstalledPackages(0);
			for (PackageInfo packageInfo : installedPackages) {
				Log.d(TAG, "packageName:" + packageInfo.packageName);
				Log.d(TAG, "versionName:" + packageInfo.versionName);
				Log.d(TAG, "packageCode:" + packageInfo.versionCode);
				Log.d(TAG,
						"applable:"
								+ packageInfo.applicationInfo.loadLabel(context
										.getPackageManager()));
			}
			
			break;
		case Intent.ACTION_SHUTDOWN:
			Log.d(TAG, "关机：" + System.currentTimeMillis());
			break;
		case Intent.ACTION_POWER_CONNECTED:
			Log.d(TAG,
					"ACTION_POWER_CONNECTED:" + "开始充电，电量是："
							+ intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0));
			break;
		case Intent.ACTION_POWER_DISCONNECTED:
			Log.d(TAG,
					"ACTION_POWER_DISCONNECTED:"
							+ "断开充电，电量是："
							+ intent.getIntExtra(BatteryManager.EXTRA_LEVEL,
									100));
			break;

		case BluetoothDevice.ACTION_BOND_STATE_CHANGED:

			BluetoothDevice device = intent
					.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			int bondState = device.getBondState();
			if (BluetoothDevice.BOND_BONDING == bondState) {
				Log.d(TAG, "BluetoothDevice正在配对" + device.getName());
			} else if (BluetoothDevice.BOND_BONDED == bondState) {
				Log.d(TAG, "BluetoothDevice完成配对" + device.getName());
			} else if (BluetoothDevice.BOND_NONE == bondState) {
				Log.d(TAG, "BluetoothDevice取消配对" + device.getName());
			}
			break;

		case "android.intent.action.SIM_STATE_CHANGED":// SIM卡插拔
			 
			Log.i(TAG, " SIM_STATE_CHANGED:" + System.currentTimeMillis());
  
			break;
		case Intent.ACTION_NEW_OUTGOING_CALL:// 拨号
			String phoneNumber = intent
			.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
			Log.i(TAG, "call OUT:" + phoneNumber);
			
			break;

		case TelephonyManager.ACTION_PHONE_STATE_CHANGED:// 来电
			TelephonyManager tManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			int callState = tManager.getCallState();
			if (callState == TelephonyManager.CALL_STATE_RINGING) {
				String phoneNumber2 = intent.getStringExtra("incoming_number");
				Log.i(TAG, "call In　响铃:" + phoneNumber2);
			} else if (callState == TelephonyManager.CALL_STATE_OFFHOOK) {
				Log.i(TAG, "call In 111　接通＝＝＝");
			}else if(callState == TelephonyManager.CALL_STATE_IDLE){
				Log.i(TAG, "call In 122＝ 挂断＝＝");
			}

			break;

		case ConnectivityManager.CONNECTIVITY_ACTION:// 网络变化
			NetManager netManager = new NetManager(context);
			if (netManager.isMobileConnected()) {
				Log.i(TAG, "手机流量－－－");
			} else if (netManager.isWifiConnected()) {
				Log.i(TAG, "wifi----");
			} else {
				Log.i(TAG, "no net connected ---");
			}
			break;
		default:
			break;
		}
	}
}
