package com.example.util;

import java.util.List;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class LocationUtil {

	private Context mContext;

	private LocationManager mLocationManager;

	private String provider;

	private Location location;

	public LocationUtil(Context context) {
		mContext = context;
		mLocationManager = (LocationManager) mContext
				.getSystemService(Context.LOCATION_SERVICE);
	}

	/**
	 * 获取经纬度，服务端处理
	 * 
	 * @return
	 */
	public double[] getLocationMsg() {
		provider = judgeProvider(mLocationManager);
		if (provider != null) {
			location = mLocationManager.getLastKnownLocation(provider);
			Log.d("lujiangfei", "getLocationMsga11:" + (location == null));

			if (location != null) {
				double latitude = location.getLatitude();// 纬度
				double longitude = location.getLongitude();// 经度
				return new double[] { longitude, latitude };
			}
		}
		return null;
	}

	// 获取Location Provider
	private String getProvider() {
		// 构建位置查询条件
		Criteria criteria = new Criteria();
		// 查询精度：高
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		// 是否查询海拨：否
		criteria.setAltitudeRequired(false);
		// 是否查询方位角 : 否
		criteria.setBearingRequired(false);
		// 是否允许付费：是
		criteria.setCostAllowed(false);
		// 电量要求：低
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		// 返回最合适的符合条件的provider，第2个参数为true说明 , 如果只有一个provider是有效的,则返回当前provider
		return mLocationManager.getBestProvider(criteria, true);
	}

	/**
	 * 判断是哪一种provider提供位置，，
	 * 
	 * @param locationManager
	 * @return
	 */
	public String judgeProvider(LocationManager locationManager) {
		List<String> prodiverlist = locationManager.getProviders(true);
		if (prodiverlist.contains(LocationManager.NETWORK_PROVIDER)) {
			return LocationManager.NETWORK_PROVIDER;
		} else if (prodiverlist.contains(LocationManager.GPS_PROVIDER)) {
			return LocationManager.GPS_PROVIDER;
		}
		return null;
	}

}
