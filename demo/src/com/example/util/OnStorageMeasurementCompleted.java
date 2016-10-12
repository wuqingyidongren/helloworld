package com.example.util;

import java.util.ArrayList;
import java.util.HashMap;


public interface OnStorageMeasurementCompleted {

	void onSMCompleted(HashMap<String, ArrayList<Long>> map);
}
