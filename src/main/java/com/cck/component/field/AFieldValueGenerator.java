package com.cck.component.field;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;

import com.cck.util.PanUtil;

public abstract class AFieldValueGenerator implements IFieldValueGenerator{
	

	protected String extractPan(ISOMsg msg) {
		String pan = msg.getString(2);
		if(pan==null) {
			pan = PanUtil.extractPanFromTrack2(msg.getString(35));
		}
		return pan;
	}


	protected Object formatValue(String value) {

		if(value.startsWith("0x")) {
			return ISOUtil.hex2byte(value.substring(2));

		}else {
			return value;
		}
	}


}
