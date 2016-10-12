package com.example.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.os.StatFs;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.util.Log;
import android.util.SparseLongArray;

import com.android.settingslib.deviceinfo.StorageMeasurement;
import com.android.settingslib.deviceinfo.StorageMeasurement.MeasurementDetails;
import com.android.settingslib.deviceinfo.StorageMeasurement.MeasurementReceiver;
import com.google.android.collect.Sets;

@TargetApi(23)
public class StorageUtil {

	private Context mContext;
	private long otherCount = 0;
	private long otherSize = 0;

	public static final String AUDIO = "audio";
	public static final String IMAGE = "image";
	public static final String VIDEO = "video";
	public static final String APP = "app";
	public static final String OTHER = "other";

	private VolumeInfo mVolume;
	private VolumeInfo mSharedVolume;
	private UserManager mUserManager = null;
	private StorageManager mStorageManager = null;
	private StorageMeasurement mMeasure = null;
	private String mVolumeId;

	private OnStorageMeasurementCompleted completed;

	public StorageUtil(Context mContext, OnStorageMeasurementCompleted completed) {
		this.mContext = mContext;
		this.completed = completed;
		mUserManager = mContext.getSystemService(UserManager.class);
		mStorageManager = mContext.getSystemService(StorageManager.class);

		// mVolumeId = VolumeInfo.ID_EMULATED_INTERNAL;
		mVolumeId = VolumeInfo.ID_PRIVATE_INTERNAL;
		mVolume = mStorageManager.findVolumeById(mVolumeId);

		mSharedVolume = mStorageManager.findEmulatedForPrivate(mVolume);

		mMeasure = new StorageMeasurement(mContext, mVolume, mSharedVolume);
		mMeasure.setReceiver(mReceiver);

	}

	public String getAverMem() {

		return null;
	}

	/**
	 * 获得SD总大小
	 * 
	 * @return
	 */
	public long getSDTotalSize() {
		File path = Environment.getExternalStorageDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return blockSize * totalBlocks;

	}

	/**
	 * 获得SD可用大小
	 * 
	 * @return
	 */
	public long getSDAvailableSize() {
		File path = Environment.getExternalStorageDirectory();
		Log.d("lujiangfei", "sd path:" + path);
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return blockSize * availableBlocks;
	}

	/**
	 * 获得机身内存总大小
	 * 
	 * @return
	 */
	public long getRomTotalSize() {
		File path = Environment.getDataDirectory();
		Log.d("lujiangfei", "rom path:" + path);

		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return blockSize * totalBlocks;

	}

	/**
	 * 获得机身可用内存
	 * 
	 * @return
	 */
	public long getRomAvailableSize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return blockSize * availableBlocks;
	}

	public Long[] getImageSize() {
		long size = 0;
		long count = 0;
		String str[] = { MediaStore.Images.Media.SIZE };
		Cursor cursor = mContext.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, str,
				null, null, null);
		while (cursor.moveToNext()) {
			size += cursor.getLong(0);
			count++;
		}

		Cursor cursor2 = mContext.getContentResolver().query(MediaStore.Images.Media.INTERNAL_CONTENT_URI, str,
				null, null, null);
		while (cursor2.moveToNext()) {
			size += cursor2.getLong(0);
			count++;
		}
		cursor.close();
		cursor2.close();

		return new Long[] { size, count };
	}

	public Long[] getAudioSize() {
		long size = 0;
		long count = 0;
		String str[] = { MediaStore.Audio.Media.SIZE, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA };
		String selection = " is_music != 0";// +MediaStore.Audio.Media.;
		Cursor cursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, str,
				selection, null, null);
		while (cursor.moveToNext()) {
			String tilte = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
			Log.d("lujiangfei", "===================" + tilte);
			size += cursor.getLong(0);
			count++;
		}
		cursor.close();

		Cursor cursor2 = mContext.getContentResolver().query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, str,
				selection, null, null);
		while (cursor2.moveToNext()) {
			String tilte = cursor2.getString(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
			Log.d("lujiangfei", "==========a=========" + tilte);
			size += cursor2.getLong(0);
			count++;
		}
		cursor2.close();
		return new Long[] { size, count };
	}

	public Long[] getVideoSize() {
		long size = 0;
		long count = 0;
		String str[] = { MediaStore.Video.Media.SIZE };
		Cursor cursor = mContext.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, str,
				null, null, null);
		while (cursor.moveToNext()) {
			size += cursor.getLong(0);
			count++;
		}

		cursor.close();
		Cursor cursor2 = mContext.getContentResolver().query(MediaStore.Video.Media.INTERNAL_CONTENT_URI, str,
				null, null, null);

		while (cursor2.moveToNext()) {
			size += cursor2.getLong(0);
			count++;
		}
		cursor2.close();
		return new Long[] { size, count };
	}

	private final MeasurementReceiver mReceiver = new MeasurementReceiver() {
		@Override
		public void onDetailsChanged(MeasurementDetails details) {
			updateDetails(details);
		}
	};

	private void updateDetails(MeasurementDetails details) {
		HashMap<String, ArrayList<Long>> bigMap = new HashMap<>();
		ArrayList<Long> arrayList = new ArrayList<>();
		SparseLongArray miscSize = details.miscSize;
		otherSize = miscSize.get(mContext.getUserId());
		arrayList.add(0, otherSize);
		arrayList.add(1, 0L);
		bigMap.put(OTHER, arrayList);
		arrayList.clear();

		Log.d("lujiangfei", "other=" + Formatter.formatFileSize(mContext, otherSize));

		SparseLongArray apps = details.appsSize;
		long appSize = apps.get(mContext.getUserId());
		arrayList.add(0, appSize);
		arrayList.add(1, 0L);
		bigMap.put(APP, arrayList);
		arrayList.clear();

		Log.d("lujiangfei", "APP=" + Formatter.formatFileSize(mContext, appSize) + apps.size());

		Long[] videos = totalValues(details, mContext.getUserId(), Environment.DIRECTORY_MOVIES);
		arrayList.add(0, videos[0]);
		arrayList.add(1, videos[1]);
		bigMap.put(VIDEO, arrayList);
		arrayList.clear();
		Log.d("lujiangfei", "VIDEO=" + Formatter.formatFileSize(mContext, videos[0]) + ";count=" + videos[1]);

		Long[] audio = totalValues(details, mContext.getUserId(), Environment.DIRECTORY_MUSIC,
				Environment.DIRECTORY_ALARMS, Environment.DIRECTORY_NOTIFICATIONS,
				Environment.DIRECTORY_RINGTONES, Environment.DIRECTORY_PODCASTS);
		arrayList.add(0, audio[0]);
		arrayList.add(1, audio[1]);
		bigMap.put(AUDIO, arrayList);
		arrayList.clear();
		Log.d("lujiangfei", "AUDIO=" + Formatter.formatFileSize(mContext, audio[0]) + ";count=" + audio[1]);

		Long[] images = totalValues(details, mContext.getUserId(), Environment.DIRECTORY_DCIM,
				Environment.DIRECTORY_MOVIES, Environment.DIRECTORY_PICTURES);
		arrayList.add(0, images[0]);
		arrayList.add(1, images[1]);
		bigMap.put(IMAGE, arrayList);
		arrayList.clear();
		Log.d("lujiangfei", "IMAGE=" + Formatter.formatFileSize(mContext, images[0]) + ";count=" + images[1]);

		completed.onSMCompleted(bigMap);

	}

	public void measure() {
		mMeasure.measure();
	}

	public Long[] getOtherSize() {

		long size = 0;
		Set<String> sMeasureMediaTypes = Sets.newHashSet(Environment.DIRECTORY_DCIM,
				Environment.DIRECTORY_MOVIES, Environment.DIRECTORY_PICTURES,
				Environment.DIRECTORY_MUSIC, Environment.DIRECTORY_ALARMS,
				Environment.DIRECTORY_NOTIFICATIONS, Environment.DIRECTORY_RINGTONES,
				Environment.DIRECTORY_PODCASTS, Environment.DIRECTORY_DOWNLOADS,
				Environment.DIRECTORY_ANDROID);
		for (String name : sMeasureMediaTypes) {
			size += getTotalSizeOfFilesInDir(new File("/sdcard/" + name));
		}

		return new Long[] { size, otherCount };
	}

	private Long[] totalValues(MeasurementDetails details, int userId, String... keys) {
		long total = 0;
		long count = 0;
		HashMap<String, Long> map = details.mediaSize.get(userId);
		if (map != null) {
			for (String key : keys) {
				if (map.containsKey(key)) {
					total += map.get(key);
					count++;
				}
			}
		} else {
			Log.w("lujiangfei", "MeasurementDetails mediaSize array does not have key for user " + userId);
		}
		return new Long[] { total, count };
	}

	private long getTotalSizeOfFilesInDir(final File file) {
		if (file.isFile()) {
			otherCount++;
			return file.length();
		}
		final File[] children = file.listFiles();
		long total = 0;
		if (children != null)
			for (final File child : children)
				total += getTotalSizeOfFilesInDir(child);
		return total;
	}

}
