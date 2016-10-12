package com.example.util;

import static android.net.NetworkPolicyManager.POLICY_REJECT_METERED_BACKGROUND;
import static android.net.NetworkTemplate.buildTemplateMobileAll;
import static android.net.NetworkTemplate.buildTemplateWifiWildcard;
import static android.net.TrafficStats.UID_REMOVED;
import static android.net.TrafficStats.UID_TETHERING;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.UserInfo;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.NetworkPolicy;
import android.net.NetworkPolicyManager;
import android.net.NetworkStats;
import android.net.NetworkTemplate;
import android.net.TrafficStats;
import android.os.INetworkManagementService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.SparseArray;

import com.example.net.AppItem;
import com.example.net.NetworkPolicyEditor;
import com.example.net.UidDetail;
import com.example.net.UidDetailProvider;

@SuppressLint("NewApi")
public class DataManager {

	private final String TAG = "lujiangfei";
	private Context mContext;
	private INetworkManagementService mNetworkService;
	private INetworkStatsService mStatsService;
	private NetworkPolicyManager mPolicyManager;
	private SubscriptionManager mSubscriptionManager;
	private NetworkPolicyEditor mPolicyEditor;
	private List<SubscriptionInfo> mSubInfoList;
	private INetworkStatsSession mStatsSession;
	private NetworkTemplate mTemplate;
	private UidDetailProvider mUidDetailProvider;

	private Map<String, Long> mWifiMap = new HashMap<>();
	private Map<String, Long> mMobileMap = new HashMap<>();
	private TelephonyManager mTelephonyManager;

	@SuppressLint("NewApi")
	public DataManager(Context context) {
		this.mContext = context;
		mNetworkService = INetworkManagementService.Stub.asInterface(ServiceManager
				.getService(Context.NETWORKMANAGEMENT_SERVICE));
		mStatsService = INetworkStatsService.Stub.asInterface(ServiceManager.getService(Context.NETWORK_STATS_SERVICE));
		mPolicyManager = NetworkPolicyManager.from(mContext);
		mSubscriptionManager = SubscriptionManager.from(mContext);
		mUidDetailProvider = new UidDetailProvider(mContext);
		mTelephonyManager = TelephonyManager.from(context);

		mPolicyEditor = new NetworkPolicyEditor(mPolicyManager);
		mPolicyEditor.read();
		mSubInfoList = mSubscriptionManager.getActiveSubscriptionInfoList();

		try {
			mStatsSession = mStatsService.openSession();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public Map<String, Long> getPkgWifiData() {
		mTemplate = buildTemplateWifiWildcard();
		updatePolicy(true);
		return mWifiMap;
	}

	public Map<String, Long> getPkgMobileData() {
		mTemplate = buildTemplateMobileAll(getActiveSubscriberId(mContext, getSubId("mobile1")));
		mTemplate = NetworkTemplate.normalize(mTemplate, mTelephonyManager.getMergedSubscriberIds());
		updatePolicy(true);
		return mMobileMap;
	}

	private String getActiveSubscriberId(Context context, int subId) {
		String retVal = mTelephonyManager.getSubscriberId(subId);
		return retVal;
	}

	private int getSubId(String currentTab) {
		SubscriptionManager mSubscriptionManager = SubscriptionManager.from(mContext);
		List<SubscriptionInfo> mSubInfoList = mSubscriptionManager.getActiveSubscriptionInfoList();
		Map<Integer, String> mMobileTagMap = initMobileTabTag(mSubInfoList);

		if (mMobileTagMap != null) {
			Set<Integer> set = mMobileTagMap.keySet();
			for (Integer subId : set) {
				if (mMobileTagMap.get(subId).equals(currentTab)) {
					return subId;
				}
			}
		}
		return -1;
	}

	private Map<Integer, String> initMobileTabTag(List<SubscriptionInfo> subInfoList) {
		Map<Integer, String> map = null;
		if (subInfoList != null) {
			String mobileTag;
			map = new HashMap<Integer, String>();
			for (SubscriptionInfo subInfo : subInfoList) {
				mobileTag = "mobile" + String.valueOf(subInfo.getSubscriptionId());
				map.put(subInfo.getSubscriptionId(), mobileTag);
			}
		}
		return map;
	}

	private void updatePolicy(boolean refreshCycle) {
		NetworkPolicy policy = mPolicyEditor.getPolicy(mTemplate);
		updateCycleList(policy);
	}

	private void updateCycleList(NetworkPolicy policy) {//TODO 时间的处理

		try {
			NetworkStats data = mStatsSession.getSummaryForAllUid(mTemplate, 1472428800000L,
					System.currentTimeMillis(), true);
			int[] restrictedUids = mPolicyManager.getUidsWithPolicy(POLICY_REJECT_METERED_BACKGROUND);
			bindStats(data, restrictedUids);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void bindStats(NetworkStats stats, int[] restrictedUids) {

		final UserManager um = (UserManager) mContext.getSystemService(Context.USER_SERVICE);
		final int currentUserId = ActivityManager.getCurrentUser();
		final List<UserHandle> profiles = um.getUserProfiles();
		final SparseArray<AppItem> knownItems = new SparseArray<AppItem>();

		NetworkStats.Entry entry = null;
		final int size = stats != null ? stats.size() : 0;
		for (int i = 0; i < size; i++) {
			entry = stats.getValues(i, entry);

			final int uid = entry.uid;
			final int collapseKey;
			final int category;
			final int userId = UserHandle.getUserId(uid);
			if (UserHandle.isApp(uid)) {
				if (profiles.contains(new UserHandle(userId))) {
					if (userId != currentUserId) {
						// Add to a managed user item.
						final int managedKey = UidDetailProvider.buildKeyForUser(userId);
						accumulate(managedKey, knownItems, entry, AppItem.CATEGORY_USER);
					}
					// Add to app item.
					collapseKey = uid;
					category = AppItem.CATEGORY_APP;
				} else {
					// If it is a removed user add it to the removed users' key
					final UserInfo info = um.getUserInfo(userId);
					if (info == null) {
						collapseKey = UID_REMOVED;
						category = AppItem.CATEGORY_APP;
					} else {
						// Add to other user item.
						collapseKey = UidDetailProvider.buildKeyForUser(userId);
						category = AppItem.CATEGORY_USER;
					}
				}
			} else if (uid == UID_REMOVED || uid == UID_TETHERING) {
				collapseKey = uid;
				category = AppItem.CATEGORY_APP;
			} else {
				collapseKey = android.os.Process.SYSTEM_UID;
				category = AppItem.CATEGORY_APP;
			}
			accumulate(collapseKey, knownItems, entry, category);
		}

		final int restrictedUidsMax = restrictedUids.length;
		for (int i = 0; i < restrictedUidsMax; ++i) {
			final int uid = restrictedUids[i];
			if (!profiles.contains(new UserHandle(UserHandle.getUserId(uid)))) {
				continue;
			}

			AppItem item = knownItems.get(uid);
			if (item == null) {
				item = new AppItem(uid);
				item.total = -1;
				knownItems.put(item.key, item);
			}
			item.restricted = true;
		}

		AppItem title = new AppItem();
		title.category = AppItem.CATEGORY_APP_TITLE;

	}

	private void accumulate(int collapseKey, SparseArray<AppItem> knownItems, NetworkStats.Entry entry, int itemCategory) {
		int uid = entry.uid;
		AppItem item = knownItems.get(collapseKey);
		if (item == null) {
			item = new AppItem(collapseKey);
			item.category = itemCategory;
		}
		item.addUid(uid);
		item.total += entry.rxBytes + entry.txBytes;
		UidDetail cachedDetail = mUidDetailProvider.getUidDetail(item.key, true);
		String label = cachedDetail.label.toString();
		Long size = mWifiMap.get(label);
		if (size == null) {
			mWifiMap.put(label, item.total);
//			mMobileMap.put(label, item.total);
		} else {
			mWifiMap.put(label, item.total  + size);
// 			mMobileMap.put(label, item.total + size);
		}

	}

	public static long getMobileData() {
		long upload = TrafficStats.getMobileTxBytes();// 获取手机3g/2g网络上传的总流量
		long download = TrafficStats.getMobileRxBytes();// 手机2g/3g下载的总流量
		return upload + download;
	}

	public static long getTotalData() {
		long upload = TrafficStats.getTotalTxBytes();// 手机全部网络接口
		// 包括wifi，3g、2g上传的总流量
		long download = TrafficStats.getTotalRxBytes(); // 手机全部网络接口
		// 包括wifi，3g、2g下载的总流量
		return upload + download;
	}

	public static long getWifiData() {
		return getTotalData() - getMobileData();
	}
}
