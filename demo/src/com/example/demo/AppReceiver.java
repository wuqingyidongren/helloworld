package com.example.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.util.Log;

public class AppReceiver extends BroadcastReceiver {
	private final static String TAG = "lujiangfei";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.d(TAG, "AppReceiver action = " + action);
		String packageName = intent.getData().getSchemeSpecificPart();
		PackageInfo packageInfo = null;
		try {
			 packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
		} catch (NameNotFoundException e) {
 			e.printStackTrace();
		}
		 
		switch (action) {

		case Intent.ACTION_PACKAGE_REPLACED:
			Log.d(TAG, "ACTION_PACKAGE_REPLACED:" + packageInfo.packageName);
			Log.d(TAG, "ACTION_PACKAGE_REPLACED:" + packageInfo.versionName);
			Log.d(TAG, "ACTION_PACKAGE_REPLACED:" + packageInfo.applicationInfo.loadLabel(context.getPackageManager()));

			break;
		case Intent.ACTION_PACKAGE_REMOVED:
			Log.d(TAG, "ACTION_PACKAGE_REMOVED:" + packageName);
 			break;
		case Intent.ACTION_PACKAGE_ADDED:
			Log.d(TAG, "ACTION_PACKAGE_ADDED:" + packageInfo.packageName);
			Log.d(TAG, "ACTION_PACKAGE_ADDED:" + packageInfo.versionName);
			Log.d(TAG, "ACTION_PACKAGE_ADDED:" + packageInfo.applicationInfo.loadLabel(context.getPackageManager()));
			break;
		default:
			break;
		}
	}

}
