package com.example.util;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageStats;
import android.os.RemoteException;
import android.util.Log;

@SuppressLint("NewApi")
public class AppManager {

	private Context mContext;

	private OnStatusCompleted completed;

	private int allAppCount = 0;
	private long allAppSize = 0;
	private int index = 0;

	public AppManager(Context mContext, OnStatusCompleted completed) {
		this.mContext = mContext;
		this.completed = completed;
	}

	public void doAppSize() {
		List<PackageInfo> installedPackages = getAllApp();
		allAppCount = installedPackages.size();

		for (int i = 0; i < installedPackages.size(); i++) {
			PackageInfo packageInfo = installedPackages.get(i);

			String packageName = packageInfo.packageName;
			mContext.getPackageManager().getPackageSizeInfo(packageName, new PkgSizeObserver());
		}
	}

	public AppManager(Context mContext) {
		this.mContext = mContext;
	}

	public List<PackageInfo> getAllApp() {
		return mContext.getPackageManager().getInstalledPackages(0);
	}

	private List<PackageInfo> deleteSysApp(List<PackageInfo> installedPackages) {
		int count = installedPackages.size();
		List<PackageInfo> list = new ArrayList<>(installedPackages);
		for (int i = 0; i < count; i++) {
			PackageInfo packageInfo = installedPackages.get(i);
			if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
				list.remove(i);
			}
		}
		return list;
	}

	public class PkgSizeObserver extends IPackageStatsObserver.Stub {

		@Override
		public void onGetStatsCompleted(PackageStats packageStats, boolean succeeded) throws RemoteException {
			long cacheSize = packageStats.cacheSize;
			long codeSize = packageStats.codeSize;
			long dataSize = packageStats.dataSize;
			long externalCacheSize = packageStats.externalCacheSize;
			long externalCodeSize = packageStats.externalCodeSize;
			long externalDataSize = packageStats.externalDataSize;
			long externalMediaSize = packageStats.externalMediaSize;
			long externalObbSize = packageStats.externalObbSize;
			long totalSize = cacheSize + codeSize + dataSize + externalDataSize + externalCacheSize + externalCodeSize
					+ externalObbSize + externalMediaSize;
			allAppSize += totalSize;
			if (index == allAppCount - 1) {
				completed.packageStatusCompleted(allAppSize, allAppCount);
				index = 0;
			}
			index++;
		}
	}

}
