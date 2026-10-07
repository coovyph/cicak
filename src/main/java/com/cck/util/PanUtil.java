package com.cck.util;

public class PanUtil {
	public static final String TRACK2_SEP_1 = "=";
	public static final String TRACK2_SEP_2 = "D";

	public static final String extractPanFromTrack2(String track2) {
		if(track2==null)return null;
		int idx = track2.indexOf(TRACK2_SEP_1);
		if(idx==-1) {
			idx = track2.indexOf(TRACK2_SEP_2);
		}
		if(idx==-1)return track2;

		return track2.substring(0,idx);
	}
}
