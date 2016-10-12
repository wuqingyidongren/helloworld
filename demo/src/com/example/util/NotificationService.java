package com.example.util;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class NotificationService extends NotificationListenerService {

	/**
	 * 收到消息
	 */
	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {
		super.onNotificationPosted(sbn);
		Notification mNotification = sbn.getNotification();
		Bundle bundle = mNotification.extras;
		// 内容标题、内容、副内容
		String contentTitle = bundle.getString(Notification.EXTRA_TITLE);

		String contentText = bundle.getString(Notification.EXTRA_TEXT);

		String contentSubtext = bundle.getString(Notification.EXTRA_SUB_TEXT);

		Log.d("lujiangfei", "notify msg:  contentTitle=" + contentTitle
				+ " +contentText+" + contentText + " ,contentSubtext="
				+ contentSubtext);
	}

	@Override
	public void onNotificationRemoved(StatusBarNotification sbn) {
		super.onNotificationRemoved(sbn);
		Log.d("lujiangfei", "onNotificationRemoved");
				 
	}

}
