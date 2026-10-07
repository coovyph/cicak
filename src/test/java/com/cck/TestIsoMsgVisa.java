package com.cck;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.GenericPackager;
import org.junit.jupiter.api.Test;

import com.cck.iso.VISAHeader;

public class TestIsoMsgVisa {
	@Test
	public void test() throws Exception{
		String rawMessage = "16010200AF00000000000000000000000000000000000430E22024810AC0000200000042000000001040345678901234560000000707020615005956084001200806400552F5F1F8F8F0F2F0F0F5F9F5F6F0F0E3C5D9D4C9C4F0F1D4D9C3C86DC9C44040404040404040058000000002010000595607070206130000040055200000000000F0F0F0F0F0F0F0F0F4F8F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0";
		byte[] rawBytes = ISOUtil.hex2byte(rawMessage);
		
		VISAHeader visaHeader = new VISAHeader();
		int next = visaHeader.unpack(rawBytes);
		
		GenericPackager gp = new GenericPackager("resources/cfg/iso87Visa.xml");
		
		ISOMsg msg=  new ISOMsg();
		msg.setPackager(gp);
		
		byte[] nextBytes = new byte[rawBytes.length-next];
		System.arraycopy(rawBytes, next, nextBytes, 0, nextBytes.length);
		msg.unpack(nextBytes);
		
		visaHeader.dump(System.out, "");
		msg.dump(System.out, "");

	}
}
