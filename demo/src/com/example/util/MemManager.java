package com.example.util;

import java.io.InputStream;
import java.text.NumberFormat;

import android.content.Context;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;

import com.android.internal.app.IProcessStats;
import com.android.internal.app.ProcessStats;

public class MemManager {

	public static final String TAG = "lujiangfei";

	private Context mContext;

	private IProcessStats mProcessStats = null;

	private ParcelFileDescriptor pfd = null;

	private ProcessStats mStats = null;

	private static final long DURATION_QUANTUM = ProcessStats.COMMIT_PERIOD / 2;

	private long mDuration = 24 * 60 * 60 * 1000;// 一天

	public MemManager(Context mContext) {
		this.mContext = mContext;
		mProcessStats = IProcessStats.Stub.asInterface(ServiceManager.getService(ProcessStats.SERVICE_NAME));
		try {
			pfd = mProcessStats.getStatsOverTime(mDuration - DURATION_QUANTUM);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		mStats = new ProcessStats(false);
		InputStream is = new ParcelFileDescriptor.AutoCloseInputStream(pfd);
		mStats.read(is);
	}

	public MemInfo getMemInfo() {
		long now = SystemClock.uptimeMillis();
		long memTotalTime = ProcessStats.dumpSingleTime(null, null, mStats.mMemFactorDurations,
				mStats.mMemFactor, mStats.mStartTime, now);
		ProcessStats.TotalMemoryUseCollection totalMem = new ProcessStats.TotalMemoryUseCollection(
				ProcessStats.ALL_SCREEN_ADJ, ProcessStats.ALL_MEM_ADJ);
		mStats.computeTotalMemoryUse(totalMem, now);
		MemInfo memInfo = new MemInfo(mContext, totalMem, memTotalTime);
		return memInfo;
	}

	public String getAverMem() {
		MemInfo memInfo = getMemInfo();
		return formatPercentage((long) memInfo.realUsedRam, (long) memInfo.realTotalRam);
	}

	private String formatPercentage(long amount, long total) {
		return formatPercentage(((double) amount) / total);
	}

	private String formatPercentage(double percentage) {
		return NumberFormat.getPercentInstance().format(percentage);
	}

}
