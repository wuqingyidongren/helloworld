package com.example.util;

import android.app.IActivityController;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

public class ActivityController extends IActivityController.Stub {
	
	private  final String TAG = "lujiangfei";
	@Override
	public boolean activityResuming(String pkg) throws RemoteException {
		Log.d(TAG,"activityResuming--pkg="+pkg);

		return true;
	}

	@Override
	public boolean activityStarting(Intent intent, String pkg)
			throws RemoteException {
		Log.d(TAG,"activityStarting--pkg="+pkg);
		return true;
	}

	@Override
	public boolean appCrashed(String processName, int pid, String shortMsg,
			String longMsg, long timeMillis, String stackTrace)
			throws RemoteException {
		Log.d(TAG,"appCrashed--stackTrace="+stackTrace);

		return true;
	}

	@Override
	public int appEarlyNotResponding(String processName, int pid,
			String annotation) throws RemoteException {
		Log.d(TAG,"appEarlyNotResponding--appEarlyNotResponding=");

		return 0;
	}

	@Override
	public int appNotResponding(String processName, int pid, String processStats)
			throws RemoteException {
		Log.d(TAG,"appNotResponding--appNotResponding=");

		return 0;
	}

	@Override
	public int systemNotResponding(String arg0) throws RemoteException {
		Log.d(TAG,"systemNotResponding--systemNotResponding=");

		return 0;
	}

}
