package com.example.util;

import android.app.ActivityManager;
import android.content.Context;
import android.text.format.Formatter;
import android.util.Log;

import com.android.internal.app.ProcessStats;
import com.android.internal.app.ProcessStats.TotalMemoryUseCollection;
import com.android.internal.util.MemInfoReader;

public class MemInfo {

	final String TAG = "lujiangfei";
	public double realUsedRam;
	public double realFreeRam;
	public double realTotalRam;
	public long baseCacheRam;

	public double[] mMemStateWeights = new double[ProcessStats.STATE_COUNT];
	public double freeWeight;
	public double usedWeight;
	public double weightToRam;
	public double totalRam;
	public double totalScale;
	public long memTotalTime;

	public MemInfo(Context context,
			ProcessStats.TotalMemoryUseCollection totalMem, long memTotalTime) {

		this.memTotalTime = memTotalTime;
		calculateWeightInfo(context, totalMem, memTotalTime);

		double usedRam = (usedWeight * 1024) / memTotalTime;
		double freeRam = (freeWeight * 1024) / memTotalTime;
		totalRam = usedRam + freeRam;
		totalScale = realTotalRam / totalRam;
		weightToRam = totalScale / memTotalTime * 1024;

		realUsedRam = usedRam * totalScale;
		realFreeRam = freeRam * totalScale;
		Log.i(TAG,
				"Scaled Used RAM: "
						+ Formatter.formatShortFileSize(context,
								(long) realUsedRam));
		Log.i(TAG,
				"Scaled Free RAM: "
						+ Formatter.formatShortFileSize(context,
								(long) realFreeRam));

		Log.i(TAG,
				"Adj Scaled Used RAM: "
						+ Formatter.formatShortFileSize(context,
								(long) realUsedRam));
		Log.i(TAG,
				"Adj Scaled Free RAM: "
						+ Formatter.formatShortFileSize(context,
								(long) realFreeRam));

		ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
		((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
				.getMemoryInfo(memInfo);
		if (memInfo.hiddenAppThreshold >= realFreeRam) {
			realUsedRam = freeRam;
			realFreeRam = 0;
			baseCacheRam = (long) realFreeRam;
		} else {
			realUsedRam += memInfo.hiddenAppThreshold;
			realFreeRam -= memInfo.hiddenAppThreshold;
			baseCacheRam = memInfo.hiddenAppThreshold;
		}
	}

	private void calculateWeightInfo(Context context,
			TotalMemoryUseCollection totalMem, long memTotalTime) {
		MemInfoReader memReader = new MemInfoReader();
		memReader.readMemInfo();
		realTotalRam = memReader.getTotalSize();
		freeWeight = totalMem.sysMemFreeWeight + totalMem.sysMemCachedWeight;
		usedWeight = totalMem.sysMemKernelWeight + totalMem.sysMemNativeWeight
				+ totalMem.sysMemZRamWeight;
		for (int i = 0; i < ProcessStats.STATE_COUNT; i++) {
			if (i == ProcessStats.STATE_SERVICE_RESTARTING) {
				mMemStateWeights[i] = 0;
			} else {
				mMemStateWeights[i] = totalMem.processStateWeight[i];
				if (i >= ProcessStats.STATE_HOME) {
					freeWeight += totalMem.processStateWeight[i];
				} else {
					usedWeight += totalMem.processStateWeight[i];
				}
			}
		}

		Log.i(TAG,
				"Used RAM: "
						+ Formatter.formatShortFileSize(context,
								(long) ((usedWeight * 1024) / memTotalTime)));
		Log.i(TAG,
				"Free RAM: "
						+ Formatter.formatShortFileSize(context,
								(long) ((freeWeight * 1024) / memTotalTime)));
		Log.i(TAG,
				"Total RAM: "
						+ Formatter
								.formatShortFileSize(
										context,
										(long) (((freeWeight + usedWeight) * 1024) / memTotalTime)));

	}
}
