package com.example.net;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseBooleanArray;

@SuppressLint("NewApi")
public class AppItem implements Comparable<AppItem>, Parcelable {
	public static final int CATEGORY_USER = 0;
	public static final int CATEGORY_APP_TITLE = 1;
	public static final int CATEGORY_APP = 2;

	public final int key;
	public boolean restricted;
	public int category;

	public SparseBooleanArray uids = new SparseBooleanArray();
	public long total;

	public AppItem() {
		this.key = 0;
	}

	public AppItem(int key) {
		this.key = key;
	}

	public AppItem(Parcel parcel) {
		key = parcel.readInt();
		uids = parcel.readSparseBooleanArray();
		total = parcel.readLong();
	}

	public void addUid(int uid) {
		uids.put(uid, true);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(key);
		dest.writeSparseBooleanArray(uids);
		dest.writeLong(total);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public int compareTo(AppItem another) {
		int comparison = Integer.compare(category, another.category);
		if (comparison == 0) {
			comparison = Long.compare(another.total, total);
		}
		return comparison;
	}

	public static final Creator<AppItem> CREATOR = new Creator<AppItem>() {
		@Override
		public AppItem createFromParcel(Parcel in) {
			return new AppItem(in);
		}

		@Override
		public AppItem[] newArray(int size) {
			return new AppItem[size];
		}
	};
}
