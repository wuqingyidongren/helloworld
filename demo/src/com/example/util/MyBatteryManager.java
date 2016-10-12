package com.example.util;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.L)
public class MyBatteryManager {

	private static BatteryManager batteryManager;

	private static Context mContext;

	public int current = 0;

	public int total = 0;
	
	private final int BATTERY_CHANGED_WHAT = 0x111111;
	
	private CusomeReceiver receiver;
	
	private Handler mHandler ;
	private int what;

	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BATTERY_CHANGED_WHAT:
				mContext.unregisterReceiver(receiver);

			default:
				break;
			}
		};
	};
	
	public void setHandler(Handler handle,int what){
		this.mHandler = handle;
		this.what = what;
	}

	private class CusomeReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
				current = intent.getExtras().getInt("level");// 获得当前电量
				total = intent.getExtras().getInt("scale");// 获得总电量
				Log.d("lujiangfei", "current ey = "+current);
				handler.sendEmptyMessage(BATTERY_CHANGED_WHAT);
				
				Message message = new Message();
				message.obj = current+"%";
				message.what = what;
				mHandler.sendMessage(message);
			}
		}
	}

	public MyBatteryManager(Context context) {
		mContext = context;
		batteryManager = (BatteryManager) mContext
				.getSystemService(Context.BATTERY_SERVICE);
	    receiver = new CusomeReceiver();
		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		mContext.registerReceiver(receiver, filter);
	}

	/**
	 * 获取当前的手机电量
	 * 
	 * @return
	 */
	public static int getCurrBattery() {
		return batteryManager
				.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
	}
}
