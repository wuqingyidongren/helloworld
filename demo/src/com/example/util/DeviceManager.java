package com.example.util;

import java.util.Locale;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Environment;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;

public class DeviceManager {

	private static Context mContext;

	private static TelephonyManager mTelephonyManager;

	public DeviceManager(Context context) {
		mContext = context;
		mTelephonyManager = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
	}

	/**
	 * 设备号
	 * 
	 * @return
	 */
	public static String getDeviceId() {
		return mTelephonyManager.getDeviceId();
	}

	/**
	 * \ 手机型号
	 * 
	 * @return
	 */
	public static String getModel() {
		return android.os.Build.MODEL;
	}

	/**
	 * \ rom版本
	 * 
	 * @return
	 */
	public static String getRomVersion() {
		return android.os.Build.DISPLAY;
	}

	/**
	 * \ 协议版本
	 * 
	 * @return
	 */
	public static String getProtocalVersion() {
		return null;
	}

	public static boolean hasSDCard() {
		return Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState());
	}

	/**
	 * SIM卡的数量
	 * 
	 * @return
	 */

	public static int getSimCount() {
		int simCount = mTelephonyManager.getSimCount();
		int count = 0;

		for (int i = 0; i < simCount; i++) {
			if (mTelephonyManager.getSimState(i) != TelephonyManager.SIM_STATE_ABSENT) {
				count++;
			}
		}
		return count;
	}

	public static float getFontSize() {
		Configuration mCurConfig = new Configuration();
		try {
			mCurConfig.updateFrom(ActivityManagerNative.getDefault()
					.getConfiguration());
		} catch (RemoteException e) {
			Log.w("lujiangfei", "Unable to retrieve font size");
		}
		//返回值｛0.85:小，1.0：正常，1.15:大，1.3:超大｝
		return mCurConfig.fontScale;

	}

	public static String getLocalLaunage() {
		Locale l = Locale.getDefault();
		String language = l.getLanguage();
		String country = l.getCountry().toLowerCase();
		if ("zh".equals(language)) {
			if ("cn".equals(country)) {
				language = "中文简体";
			} else if ("tw".equals(country)) {
				language = "中文繁体";
			}
		} else if ("en".equals(language)) {
			language = "英文";
		} else {
			language = "其他";
		}
		return language;
	}

	/**
	 * 手机运营商
	 * 
	 * @return
	 */
	public static String getSimOperatorName() {
		String operator = mTelephonyManager.getSubscriberId();
 
		if (operator != null) {
			if (operator.startsWith("46000") || operator.startsWith("46002")
					|| operator.startsWith("46007")) {
				return "中国移动";
			} else if (operator.startsWith("46001")) {
				return "中国联通";
			} else if (operator.startsWith("46003")) {
				return "中国电信";
			} else {
				return "未知";
			}
		} else {
			return null;
		}
	}
}
