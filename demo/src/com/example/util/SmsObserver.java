package com.example.util;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

public class SmsObserver extends ContentObserver {

	private Context context;
	private final String TAG = "lujiangfei";
	private static final String[] SMS_PROJECTION = new String[] { "address",
			"person", "date", "type", "body", };

	public SmsObserver(Context context, Handler handler) {
		super(handler);
		this.context = context;
		Log.i(TAG, "My Oberver on create");
	}

	public void onChange(boolean selfChange) {
		Log.i(TAG, "sms onChange###### ");
		// 发送短信：content://sms/outbox
		// 接收短信：content://sms/inbox

   /*     _id：短信序号，如100 　　
	     * 　　thread_id：对话的序号，如100，与同一个手机号互发的短信，其序号是相同的 　　
	     * 　　address：发件人地址，即手机号，如+8613811810000 　　
	     * 　　person：发件人，如果发件人在通讯录中则为具体姓名，陌生人为null 　　
	     * 　　date：日期，long型，如1256539465022，可以对日期显示格式进行设置 　　
	     * 　　protocol：协议0SMS_RPOTO短信，1MMS_PROTO彩信
	     * 　　read：是否阅读0未读，1已读 　　
	     * 　　status：短信状态-1接收，0complete,64pending,128failed 　　
	     * 　　type：短信类型1是接收到的，2是已发出 　　 　　
	     * 　　body：短信具体内容 　　
	     * 　　service_center：短信服务中心号码编号，如+8613800755500*/
		// 查询all短信,
		Cursor cursor = context.getContentResolver().query(
				Uri.parse("content://sms/"), null, "read = 0", null, null);

		while (cursor.moveToNext()) {
			StringBuilder sb = new StringBuilder();
			sb.append("address="
					+ cursor.getString(cursor.getColumnIndex("address")));
			sb.append(", body="
					+ cursor.getString(cursor.getColumnIndex("body")));
			sb.append(", date="
					+ cursor.getString(cursor.getColumnIndex("date")));
			sb.append(", type="
					+ cursor.getString(cursor.getColumnIndex("type")));
			sb.append(", person="
					+ cursor.getString(cursor.getColumnIndex("person")));

			Log.i(TAG, sb.toString());

		}
		cursor.close();
		
		
		// 查询all彩信,
		Cursor cursor2 = context.getContentResolver().query(
				Uri.parse("content://mms/"), null, "read = 0", null, null);

		while (cursor2.moveToNext()) {
			StringBuilder sb = new StringBuilder();
			sb.append("address="
					+ cursor.getString(cursor.getColumnIndex("address")));
			sb.append(", body="
					+ cursor.getString(cursor.getColumnIndex("body")));
			sb.append(", date="
					+ cursor.getString(cursor.getColumnIndex("date")));
			sb.append(", type="
					+ cursor.getString(cursor.getColumnIndex("type")));
			sb.append(", person="
					+ cursor.getString(cursor.getColumnIndex("person")));

			Log.i(TAG, sb.toString());

		}
		cursor2.close();
	}

}
