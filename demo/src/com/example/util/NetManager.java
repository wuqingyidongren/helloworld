package com.example.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetManager {

	private ConnectivityManager connectivityManager;
	private Context context;

	public NetManager(Context context) {
		this.context = context;
		connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	/**
	 * -1,无网络；0，手机；１,wifi
	 * 
	 * @return
	 */
	public int getNetType() {
		int result = -1;
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo == null) {
			result = 0;
		} else {
			result = networkInfo.getType();
		}
		return result;
	}

	/**
	 * wifi网络是否连接
	 * 
	 * @return
	 */
	public boolean isWifiConnected() {
		boolean result = false;
		NetworkInfo info = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 		return info.isConnected();
	}
	
	/**
	 * 手机网络是否连接
	 * 
	 * @return
	 */
	public boolean isMobileConnected() {
		boolean result = false;
		NetworkInfo info = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		return info.isConnected();
	}
}
