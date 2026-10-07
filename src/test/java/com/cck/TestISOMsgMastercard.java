package com.cck;

import org.jpos.iso.ISOBasePackager;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.util.LogListener;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;

public class TestISOMsgMastercard {
	public static void main(String[] args) throws Exception{
		byte[] dataBytes = ISOUtil.hex2byte("f0f1f0f0723c4641a8c19a08f1f6f5f5f7f6f9f2f0f0f6f1f8f9f9f4f5f8f0f0f0f0f0f0f0f0f0f0f1f0f2f0f0f0f0f0f0f5f2f6f1f0f2f7f0f3f8f9f5f6f9f4f1f7f2f7f0f3f0f5f2f6f2f6f0f6f5f5f4f1f0f5f1f0f0f1f0f6f0f6f0f2f7f9f6f1f0f6f0f2f7f9f6f0f3f7f5f5f7f6f9f2f0f0f6f1f8f9f9f4f5f87ef2f6f0f6f2f2f6f0f0f0f0f0f0f9f8f0f0f0f0f0f0f5f2f6f0f0f0f3f1f5f3f5c4f2c2d4f0f7f9f4f8f8f5f0f0f0f4f9f2f7f6f0404040f0f2f0d9f3f7f1f5f0f5f1f1f0f0f0f0f0f9f9f9f9f9f8f3f6f0d276eb5b5c711918f9f7f0f1f1f0f0f0f0f1f0f0f0f0f0f0f1f1f49f260833a9713b0c922d149f2701809f3704413ee6c39f36020361950500000480009a032505269c01009f02060000102000005f2a0203609f1a020360820274009f03060000000000009f101c0101a0000000000088620900000000000000000000000000000000008407a0000006021010f0f1f1f0f0f0f0f0f0f0f0f0f0f5");
		ISOBasePackager p = new GenericPackager("resources/cfg/iso87master.xml");

		ISOMsg msg = new ISOMsg();
		msg.setPackager(p);

		Logger logger = new Logger();
		LogListener logListener = new SimpleLogListener();
		logger.addListener(logListener);

		//p.setLogger(logger, "test");
		try {
			msg.unpack(dataBytes);
		}catch(Exception e) {}
		msg.dump(System.out, "");
		
	}
}
