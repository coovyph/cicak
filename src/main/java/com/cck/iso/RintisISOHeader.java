package com.cck.iso;

import java.io.PrintStream;
import java.util.Map;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOHeader;
import org.jpos.iso.ISOUtil;
import org.jpos.util.Loggeable;

import lombok.Data;

@Data
public class RintisISOHeader implements ISOHeader, Loggeable {
	private static final long serialVersionUID = 1L;

	public static final String BASE24_HEADER = "ISO";
	public static final String H_B24_HEADER = "b24-header";
	public static final String H_PRODUCT_INDICATOR = "prod-ind";
	public static final String H_RELEASE_NUMBER = "rel-num";
	public static final String H_STATUS = "status";
	public static final String H_ORIGIN_CODE = "origin-code";
	public static final String H_RESPONDER_CODE ="responder-code";

	public static final int L_B24_HEADER = 3;
	public static final int L_PRODUCT_INDICATOR = 2;
	public static final int L_RELEASE_NUMBER = 2;
	public static final int L_STATUS = 3;
	public static final int L_ORIGINATOR_CODE = 1;
	public static final int L_RESPONDER_CODE = 1;

	public static final int L_MAX_L_ENGTH = L_B24_HEADER + L_PRODUCT_INDICATOR + 
			L_RELEASE_NUMBER + L_STATUS + 
			L_ORIGINATOR_CODE + L_RESPONDER_CODE;

	private String base24Header = "ISO";
	private String productIndicator;
	private String releaseNumber;
	private String status;
	private String originCode;
	private String responderCode;

	private int packDetail(byte[] rslt, String value, int offset, int lgth) {
		if (value == null) {
			value = "";
		} 
		try {
			value = ISOUtil.padleft(value, lgth, ' ');
		}catch(ISOException e) {
			//should not happen
		}
		System.arraycopy(value.getBytes(), 0, rslt, offset, lgth);
		return offset+lgth;

	}
	
	public void setValue(Map<String,String> map) {
		setBase24Header(map.get(H_B24_HEADER));
		setProductIndicator(map.get(H_PRODUCT_INDICATOR));
		setReleaseNumber(map.get(H_RELEASE_NUMBER));
		setStatus(map.get(H_STATUS));
		setOriginCode(map.get(H_ORIGIN_CODE));
		setResponderCode(map.get(H_RESPONDER_CODE));
	}

	public byte[] pack() {
		byte[] rslt = new byte[L_MAX_L_ENGTH];
		int offset = 0;
		offset = packDetail(rslt, this.base24Header, offset, L_B24_HEADER);
		offset = packDetail(rslt, this.productIndicator, offset, L_PRODUCT_INDICATOR);
		offset = packDetail(rslt, this.releaseNumber, offset, L_RELEASE_NUMBER);
		offset = packDetail(rslt, this.status, offset, L_STATUS);
		offset = packDetail(rslt, this.originCode, offset, L_ORIGINATOR_CODE);
		packDetail(rslt, this.responderCode, offset, L_RESPONDER_CODE);

		return rslt;
	}




	public int unpack(byte[] b) {
		if(b.length<L_MAX_L_ENGTH) return 0;
		int offset = 0;

		setBase24Header(new String(b,offset,L_B24_HEADER));
		offset+=L_B24_HEADER;

		setProductIndicator(new String(b,offset,L_PRODUCT_INDICATOR));
		offset+=L_PRODUCT_INDICATOR;
		
		setReleaseNumber(new String(b,offset,L_RELEASE_NUMBER));
		offset+=L_RELEASE_NUMBER;

		setStatus(new String(b,offset,L_STATUS));
		offset+=L_STATUS;
		
		setOriginCode(new String(b,offset,L_ORIGINATOR_CODE));
		offset+=L_ORIGINATOR_CODE;
		
		setResponderCode(new String(b,offset,L_RESPONDER_CODE));
		offset+=L_RESPONDER_CODE;
		
		return offset;
	}


	public void setDestination(String dst) {}

	public String getDestination() {
		return null;
	}

	public void setSource(String src) {}

	public String getSource() {
		return null;
	}

	public int getLength() {
		return L_MAX_L_ENGTH;
	}

	public void swapDirection() {}

	public Object clone() {
		return null;
	}

	private String formatDump(String ident, String label, String value) {
		StringBuilder sb = new StringBuilder();
		sb.append(ident);
		sb.append(String.format("%1$-25s", new Object[] { label }));
		sb.append(": ");
		sb.append(value);
		sb.append("\n");
		return sb.toString();
	}

	public void dump(PrintStream p, String indent) {
		p.print(formatDump(indent,"B24 Header", this.base24Header));
		p.print(formatDump(indent,"Product Indicator", this.productIndicator));
		p.print(formatDump(indent,"Release Number", this.releaseNumber));
		p.print(formatDump(indent,"Status", this.status));
		p.print(formatDump(indent,"Origin Code", this.originCode));
		p.print(formatDump(indent,"Responder Code", this.responderCode));

	}
}