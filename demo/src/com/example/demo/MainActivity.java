package com.example.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.IActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.NetworkPolicyManager;
import android.net.NetworkTemplate;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.TextView;

import com.example.DataUsageSummary;
import com.example.net.AppItem;
import com.example.net.NetworkPolicyEditor;
import com.example.net.UidDetailProvider;
import com.example.util.ActivityController;
import com.example.util.AppManager;
import com.example.util.CpuManager;
import com.example.util.DataManager;
import com.example.util.DeviceManager;
import com.example.util.LocationUtil;
import com.example.util.MemManager;
import com.example.util.MyBatteryManager;
import com.example.util.NetManager;
import com.example.util.OnStatusCompleted;
import com.example.util.OnStorageMeasurementCompleted;
import com.example.util.SmsObserver;
import com.example.util.StorageUtil;
import com.google.android.collect.Lists;

@SuppressLint("NewApi")
public class MainActivity extends Activity implements OnStatusCompleted, OnStorageMeasurementCompleted {

	private final static String TAG = "lujiangfei";
	private Context mContext;
	private TextView snTv;
	private TextView modelTv;
	private TextView romlTv;
	private TextView currenteyTv;
	private TextView sdTotalTv;
	private TextView sdusdTv;
	private TextView percentTv;
	private TextView audioSizeTv;
	private TextView videoSizeTv;
	private TextView photoSizeTv;
	private TextView otherSizeTv;
	private TextView appSizeTv;
	private TextView memTv;
	private TextView dataTv;
	private TextView simTv;

	private final int EY_WHAT = 0x110;
	private final int APP_LIST_WHAT = 0x111;

	private DeviceManager deviceManager;
	private StorageUtil storageUtil;
	private AppManager appManager;
	private MemManager memManager;
	private LocationUtil locationUtil;
	private MyBatteryManager batteryManager;
	private NetworkPolicyEditor mPolicyEditor;

	private INetworkManagementService mNetworkService;
	private NetworkPolicyManager mPolicyManager;
	private SubscriptionManager mSubscriptionManager;

	private List<SubscriptionInfo> mSubInfoList;
	private Map<Integer, String> mMobileTagMap;
	private NetworkTemplate mTemplate;
	private UidDetailProvider mUidDetailProvider;

	private INetworkStatsSession mStatsSession;
	private INetworkStatsService mStatsService;
	private ArrayList<AppItem> mItems = Lists.newArrayList();

	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EY_WHAT:
				currenteyTv.setText(msg.obj.toString());

				break;
			case APP_LIST_WHAT:
				appSizeTv.setText(msg.obj.toString());

				break;

			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initUI();
		initData();

		CusomeReceiver receiver = new CusomeReceiver();
		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		// 静态注册无效
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_HEADSET_PLUG);
		registerReceiver(receiver, filter);

		IActivityManager default1 = ActivityManagerNative.getDefault();
		try {
			default1.setActivityController(new ActivityController());
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		DataUsageSummary fragment = new DataUsageSummary();
		fragmentTransaction.add(R.id.widget_frame, fragment);

		fragmentTransaction.commit();

	}

	private void initData() {
		mContext = getApplicationContext();
		deviceManager = new DeviceManager(mContext);
		storageUtil = new StorageUtil(mContext, this);
		appManager = new AppManager(mContext, this);
		memManager = new MemManager(mContext);
		locationUtil = new LocationUtil(mContext);

		snTv.setText(deviceManager.getDeviceId());// 设备号
		modelTv.setText(deviceManager.getModel());// 机型
		romlTv.setText(deviceManager.getRomVersion());// rom版本
		// String simOperatorName = deviceManager.getSimOperatorName();
		// Log.d(TAG, simOperatorName);

		storageUtil.measure();

		sdTotalTv.setText(Formatter.formatFileSize(mContext, storageUtil.getSDTotalSize()));
		sdusdTv.setText(Formatter.formatFileSize(mContext, storageUtil.getSDAvailableSize()));
		long used = storageUtil.getRomAvailableSize();
		long total = storageUtil.getRomTotalSize();
		percentTv.setText(used * 100 / total + "%");

		batteryManager = new MyBatteryManager(mContext);
		batteryManager.setHandler(handler, EY_WHAT);

		Long[] result = storageUtil.getImageSize();

		photoSizeTv.setText(Formatter.formatFileSize(mContext, result[0]) + "   " + result[1]);
		result = storageUtil.getAudioSize();
		audioSizeTv.setText(Formatter.formatFileSize(mContext, result[0]) + "   " + result[1]);
		result = storageUtil.getVideoSize();
		videoSizeTv.setText(Formatter.formatFileSize(mContext, result[0]) + "   " + result[1]);
		result = storageUtil.getOtherSize();
		otherSizeTv.setText(Formatter.formatFileSize(mContext, result[0]) + "   " + result[1]);

		appManager.doAppSize();

		memTv.setText(memManager.getAverMem());

		long wifidata = DataManager.getWifiData();
		long mobileData = DataManager.getMobileData();
		dataTv.setText(Formatter.formatFileSize(mContext, wifidata) + "   "
				+ Formatter.formatFileSize(mContext, mobileData));

		simTv.setText(deviceManager.hasSDCard() + "  " + deviceManager.getSimCount() + "  "
				+ deviceManager.getLocalLaunage() + "  " + deviceManager.getFontSize());

		double[] locationMsg = locationUtil.getLocationMsg();
		Log.d(TAG, "locationMsg:" + locationMsg);

		NetManager manager = new NetManager(mContext);
		int netType = manager.getNetType();
		Log.d(TAG, "-1,无网络；0，手机；１,wifi：" + netType);

		// 短信
		getContentResolver().registerContentObserver(Uri.parse("content://sms／"), true,
				new SmsObserver(mContext, new Handler()));
		// 彩信
		getContentResolver().registerContentObserver(Uri.parse("content://mms／"), true,
				new SmsObserver(mContext, new Handler()));

		mNetworkService = INetworkManagementService.Stub.asInterface(ServiceManager
				.getService(Context.NETWORKMANAGEMENT_SERVICE));
		mStatsService = INetworkStatsService.Stub.asInterface(ServiceManager
				.getService(Context.NETWORK_STATS_SERVICE));
		mPolicyManager = NetworkPolicyManager.from(mContext);
		mSubscriptionManager = SubscriptionManager.from(mContext);

		mPolicyEditor = new NetworkPolicyEditor(mPolicyManager);
		mPolicyEditor.read();
		mSubInfoList = mSubscriptionManager.getActiveSubscriptionInfoList();

		try {
			mStatsSession = mStatsService.openSession();
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		DataManager dataManager = new DataManager(mContext);
		// Map<String, Long> pkgWifiData =
		// dataManager.getPkgMobileData();
		Map<String, Long> pkgWifiData = dataManager.getPkgWifiData();
		for (Map.Entry<String, Long> entry : pkgWifiData.entrySet()) {
			Log.d(TAG,
					"key= " + entry.getKey() + " and value= "
							+ Formatter.formatFileSize(mContext, entry.getValue()));
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		CpuManager.getCpuRate();
	}

	private void initUI() {
		snTv = (TextView) findViewById(R.id.sn);
		modelTv = (TextView) findViewById(R.id.model);
		romlTv = (TextView) findViewById(R.id.romverison);
		currenteyTv = (TextView) findViewById(R.id.currentey);
		sdTotalTv = (TextView) findViewById(R.id.totalSD);
		sdusdTv = (TextView) findViewById(R.id.usedSD);
		percentTv = (TextView) findViewById(R.id.percent);
		photoSizeTv = (TextView) findViewById(R.id.photosize);
		videoSizeTv = (TextView) findViewById(R.id.videosize);
		audioSizeTv = (TextView) findViewById(R.id.audiosize);
		otherSizeTv = (TextView) findViewById(R.id.othersize);
		appSizeTv = (TextView) findViewById(R.id.appsize);
		memTv = (TextView) findViewById(R.id.memPercent);
		dataTv = (TextView) findViewById(R.id.data);
		simTv = (TextView) findViewById(R.id.sim);
	}

	private void sendMsg(int what, Object obj) {
		Message message = Message.obtain();
		message.what = what;
		message.obj = obj;
		handler.sendMessage(message);

	}

	public class CusomeReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
				int current = intent.getExtras().getInt("level");// 获得当前电量
				int total = intent.getExtras().getInt("scale");// 获得总电量
				// sendMsg(EY_WHAT, current + "%");
			} else if (Intent.ACTION_SCREEN_ON.equals(action)) {
				Log.d(TAG, "ACTION_SCREEN_ON:" + System.currentTimeMillis());
			} else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
				Log.d(TAG, "ACTION_SCREEN_OFF:" + System.currentTimeMillis());
			} else if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
				int state2 = intent.getIntExtra("state", -1);
				if (state2 == 0) {
					Log.d(TAG, "state:拔出耳机" + state2);
				} else if (state2 == 1) {
					Log.d(TAG, "state:插耳机" + state2);
				} else {
					Log.d(TAG, "state:error状态" + state2);
				}
			}

		}
	}

	@Override
	public void packageStatusCompleted(long size, int count) {

		sendMsg(APP_LIST_WHAT, Formatter.formatFileSize(mContext, size) + "  " + count);
	}

	@Override
	public void onSMCompleted(HashMap<String, ArrayList<Long>> map) {
	}

}
