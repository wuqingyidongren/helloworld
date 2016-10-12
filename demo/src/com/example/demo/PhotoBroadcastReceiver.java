package com.example.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.util.Log;

public class PhotoBroadcastReceiver extends BroadcastReceiver {

	private final static String TAG = "lujiangfei";

	@Override
	public void onReceive(Context context, Intent intent) {

		String action = intent.getAction();
		Log.d(TAG, "PhotoBroadcastReceiver action = " + action);

		Uri uri = intent.getData();
		Log.d(TAG, "PhotoBroadcastReceiver data = " + uri);
		Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
		// 拍照类型, 1照片16x9, 2照片4 x 3，3照片1 x 1, 4全景照片，5视频，

		if (cursor != null) {
			cursor.moveToFirst();

			if (Camera.ACTION_NEW_PICTURE.equals(action)) {
				String path = cursor.getString(cursor.getColumnIndex("_data"));
				Bitmap decodeFile = BitmapFactory.decodeFile(path);
				int width = decodeFile.getWidth();
				int height = decodeFile.getHeight();
				if (width == height) {
					Log.d(TAG, "PhotoBroadcastReceiver　w:h=1:1");
				} else if (width * 3 == height * 4) {
					Log.d(TAG, "PhotoBroadcastReceiver　w:h=4:3");
				} else if (width * 9 == height * 16) {
					Log.d(TAG, "PhotoBroadcastReceiver　w:h=16:9");
				}

			} else if (Camera.ACTION_NEW_VIDEO.equals(action)) {
				Log.d(TAG, "PhotoBroadcastReceiver　ACTION_NEW_VIDEO");
			}

		}

	}

}
