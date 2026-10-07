package com.cck.util;

import java.util.HashMap;
import java.util.Map;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;

public class JagoIbftAdditionalData {

	public static final String TRF_UNKNOWN = "9";
	public static final String TRF_TYPE_ISS_BENE = "1";
	public static final String TRF_TYPE_ISS_ONLY = "2";
	public static final String TRF_TYPE_BENE_ONLY = "3";

	public static final String SWITCH_INDICATOR_D_OR_C = "0";
	public static final String SWITCH_INDICATOR_DC = "1";

	public static final String DC_INDICATOR_DB = "1";
	public static final String DC_INDICATOR_CR = "2";
	public static final int L_TRF_INDICATOR = 1;

	public static final String XFER_IND = "00";
	public static final String BUS_APPL_ID = "01";
	public static final String TRF_IND = "02";
	public static final String ISS_CBC = "03";
	public static final String ISS_NAME = "04";
	public static final String DEST_CBC = "05";
	public static final String DEST_NAME = "06";
	public static final String REF = "07";
	public static final String ACQ_SRC_ACCT_NUM = "08";
	public static final String ACQ_ISS_ACCT_NUM = "09";
	public static final String X_SWITCH_IND = "10";
	public static final String DBCR_IND = "11";
	public static final String OFFUS_FLG = "12";

	public static final int TYPE_VISA = 1;
	public static final int TYPE_LOCAL = 2;

	private int type;

	private Map<String, String> map;

	public JagoIbftAdditionalData() {
		this.map = new HashMap<>();
		this.type = TYPE_VISA;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public void put(String tag, String value) {
		this.map.put(tag, value);
	}

	public String get(String tag) {
		return this.map.get(tag);
	}

	public String getIssName() {
		return get(ISS_NAME);
	}

	public String getDestName() {
		return get(DEST_NAME);
	}

	public String getRef() {
		return get(REF);
	}

	public String getFormattedDestCbc() {
		return getFormattedCbc(this.map.get(DEST_CBC));
	}

	public String getFormattedIssCbc() {
		return getFormattedCbc(this.map.get(ISS_CBC));
	}

	private String getFormattedCbc(String cbc) {
		if (cbc == null)
			return null;
		cbc = cbc.trim();
		cbc = ISOUtil.zeroUnPad(cbc);
		if (cbc.length() > 3)
			return cbc;
		try {
			return ISOUtil.zeropad(cbc, 3);
		} catch (Exception e) {
			return null;
		}
	}

	private int unpackField(String raw, int idx) {
		String key = null;
		try {
			key = raw.substring(idx, idx + 2);
		} catch (Exception e) {
			// invalid format
			return -1;
		}
		idx += 2;
		int valueLength = 0;
		try {
			valueLength = Integer.parseInt(raw.substring(idx, idx + 3));
			idx += 3;
		} catch (Exception e) {
			// invalid format
			return -1;
		}
		if (valueLength > 0) {
			String value = "";
			try {
				value = raw.substring(idx, idx + valueLength);
			} catch (StringIndexOutOfBoundsException ide) {
				return -1;
			}

			this.map.put(key, value);

		}
		return idx + valueLength;
	}

	public void unpack(String raw) throws ISOException {
		if (raw == null || raw.isEmpty())
			return;

		int length = raw.length();
		int idx = 0;
		try {
			this.type = Integer.parseInt(raw.substring(idx, idx + 1));
		} catch (NumberFormatException ne) {
			throw new ISOException("Invalid data format", ne);
		}
		idx++;
		while (idx < length) {
			idx = unpackField(raw, idx);
			if (idx == -1)
				break;
		}
	}

	private void packField(StringBuilder sb, String tag) {
		String value = this.map.get(tag);
		if (value == null)
			return;

		sb.append(tag);
		sb.append(ISOUtil.zeropad(value.length(), 3));
		sb.append(value);
	}

	public String pack() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.type);
		this.map.forEach((k, v) -> {
			packField(sb, k);
		});

		return sb.toString();
	}

	@Override
	public String toString() {
		return "IbftAdditionalData [map=" + map + "]";
	}

}