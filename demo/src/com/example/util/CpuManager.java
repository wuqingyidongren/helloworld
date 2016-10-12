package com.example.util;

import android.util.Log;

import com.android.internal.os.ProcessCpuTracker;

public class CpuManager {

	private static final String TAG = "lujiangfei";

	public static float getCpuRate() {
		ProcessCpuTracker cpuTracker = new ProcessCpuTracker(true);
		cpuTracker.init();
		float totalCpuPercent = cpuTracker.getTotalCpuPercent();
		Log.d(TAG, "taaaaaaaaaaaaaaa=====================otalCpuPercent=" + totalCpuPercent);
		return totalCpuPercent;
	}

}
