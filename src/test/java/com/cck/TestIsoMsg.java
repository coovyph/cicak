package com.cck;

import org.jpos.iso.ISOHeader;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.GenericPackager;
import org.junit.jupiter.api.Test;

public class TestIsoMsg {

	@Test
	public void testIsoRintis()throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("49534F30313630303030373730323130");
		sb.append("46323341433430314145393138303030");
		sb.append("30303030303030303134303030303030");
		sb.append("31363534333431363137343239303637");
		sb.append("34313331323030303030303030303030");
		sb.append("30303030303330363033353931333030");
		sb.append("30323735313035393039303330363033");
		sb.append("30363033303636303131303531303331");
		sb.append("34373036333630303032333435343334");
		sb.append("31363137343239303637343144323930");
		sb.append("39323031303030303039353930313133");
		sb.append("39333838202020202020303030303030");
		sb.append("31325052494D412041544D2043484950");
		sb.append("20203235323030303030303030303030");
		sb.append("30303030303030303030303030303034");
		sb.append("54303031333630303335303131323737");
		sb.append("3138313232373032313503");

		String rawData = sb.toString();
		byte[] rawBytes = ISOUtil.hex2byte(rawData);

		ISOMsg msg = new ISOMsg();
		ISOPackager packager = new GenericPackager("resources/cfg/iso87Rintis.xml");

		msg.setPackager(packager);
		msg.unpack(rawBytes);

		msg.dump(System.out, "");

		ISOHeader isoHeader = msg.getISOHeader();
		
		System.out.println(new String(isoHeader.pack()));
		System.out.println(new String(msg.getHeader()));
	}
	public void testIsoPack()throws Exception {
		ISOMsg msg = new ISOMsg();
		msg.setMTI("0200");
		//msg.set("7","0625092100");
		msg.set("7.1","0625");
		msg.set("7.2","092100");

		msg.setPackager(new GenericPackager("resources/cfg/iso87D12.xml"));

		byte[] bytes = msg.pack();
		System.out.println(new String(bytes));
	}

	public static void main(String[] args) throws Exception{
		new TestIsoMsg().testIsoPack();
	}
}
