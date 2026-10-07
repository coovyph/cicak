package com.cck;

import org.jpos.core.SimpleConfiguration;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.security.jceadapter.GeckoSecurityModule;
import org.jpos.transaction.Context;
import org.jpos.transaction.TransactionParticipant;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;
import org.jpos.util.SimpleLogListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.cck.participant.ArpcGenParticipant;
import com.cck.participant.ArqcValidateParticipant;
import com.cck.util.ISOContextUtil;


public class TestArqcValidateParticipant {

	GeckoSecurityModule sm  = null;
	@Before
	public void initialize()throws Exception {
		sm = new GeckoSecurityModule("resources/lmk");
		NameRegistrar.register("sm", sm);
	}

	@After
	public void release() {
		NameRegistrar.unregister("sm");
	}

	@Test
	public void test()throws Exception {
		ArqcValidateParticipant participant = new ArqcValidateParticipant();
		ArpcGenParticipant arpcParticipant = new ArpcGenParticipant();
		SimpleConfiguration cfg = new SimpleConfiguration();
		cfg.put("sm", "sm");
		cfg.put("mk-ac", "378313A393B6A119E5341D86B5A838C5");
		cfg.put("mk-ac-kcv", "B2EFCB");
		cfg.put("pem", "05");

		Logger logger = new Logger();
		logger.addListener(new SimpleLogListener());
		participant.setLogger(logger,"Q2");
		arpcParticipant.setLogger(logger, "Q2");

		participant.setConfiguration(cfg);
		arpcParticipant.setConfiguration(cfg);
		
		ISOMsg msg = new ISOMsg();
		msg.set(2,"4624368800015330");
		msg.setMTI("0200");
		msg.set(22,"051");
		msg.set(55,ISOUtil.hex2byte("9F34030200009F3303E0F0C89F03060000000000009F02060000000000008407A00000060210105F340101820254009F2701809F2608424C65874DC322A65F2A0203609F1E0830383230383837379F1A0203609C01309A032208039F4104000000329F101C0101A0008000005876BFB50000000000000000000000000000000000950542C00480009F0902008C9F37049C992A9C9F360207C69F350122"));
		
		Context ctx = new Context();
		ISOContextUtil.setInIsoMsg(ctx, msg);
		int result = participant.prepare(1, ctx);
		
		ISOMsg rsp = ISOContextUtil.getOutIsoMsg(ctx);
		
		arpcParticipant.prepare(1, ctx);
		
		System.out.println(rsp.getString(39));
		System.out.println(result==TransactionParticipant.ABORTED ? "ABORTED" : "NOT-ABORT");
	}

}
