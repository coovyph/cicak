package com.cck.participant;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.jpos.core.ConfigurationException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.security.jceadapter.GeckoSecurityModule;
import org.jpos.transaction.Context;
import org.jpos.transaction.TransactionParticipant;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;
import org.jpos.util.SimpleLogListener;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cck.component.BinManager;
import com.cck.component.KeyStoreManager;
import com.cck.mock.AppContextMock;
import com.cck.service.impl.Q2Service;
import com.cck.util.ISOContextUtil;

@ExtendWith(MockitoExtension.class)
public class ArpcGenParticipantTest {
	private static AppContextMock ctx;
	@BeforeAll
	public static final void prepare()throws Exception {
		GeckoSecurityModule gsm = new GeckoSecurityModule("resources/lmk");

		Logger logger = new Logger();
		logger.addListener(new SimpleLogListener());
		gsm.setLogger(logger,"gsm");

		BinManager bm = new BinManager("resources/bin.prop");
		bm.reload();
		KeyStoreManager ksm = new KeyStoreManager("resources/key.prop");
		ksm.reloadKey();

		ctx = new AppContextMock();
		ctx.register(gsm);
		ctx.register(bm);
		ctx.register(ksm);

		NameRegistrar.register(Q2Service.Q2_CONTEXT, ctx);
	}

	@AfterAll
	public static final void cleanUp()throws Exception {		
		NameRegistrar.unregister(Q2Service.Q2_CONTEXT);
		ctx = null;
	}


	private ISOMsg constructIsoMsg() {
		ISOMsg msg = new ISOMsg();
		try {
			msg.setMTI("0200");
		}catch(ISOException isoe) {
			assertFalse("setg MTI ISOException is not expected",false);
		}
		msg.set(2,"5393710500000060");
		msg.set(3,"301000");
		msg.set(4,"000000000000");
		msg.set(22,"051");
		msg.set(35,"5393710500000060=26122010000067301");
		msg.set(55, ISOUtil.hex2byte("82025400950500000000005F2A0203605F3401009A032211089C01309F02060000000000009F03060000000000009F101C0101A000800000EDB26B8D00000000000000000000000000000000009F1A0203609F2608589A8D9683ADD65D9F360201439F37040FF753179F2701808407A0000006021010"));

		return msg;
	}

	@Test
	@Order(1)
	public void testNormal() {
		ArpcGenParticipant genParticipant = new ArpcGenParticipant();
		Logger logger = new Logger();
		logger.addListener(new SimpleLogListener());
		genParticipant.setLogger(logger, "arpc-gen");


		SimpleConfiguration cfg = new SimpleConfiguration();
		cfg.put("pem", "05");
		try {
			genParticipant.setConfiguration(cfg);
		}catch(ConfigurationException cfe) {
			assertFalse("ConfigurationException is not expected",false);
		}

		ISOMsg msg = constructIsoMsg();
		Context ctx = new Context();
		ISOContextUtil.setInIsoMsg(ctx, msg);

		int result = genParticipant.prepare(1, ctx);
		assertEquals(TransactionParticipant.PREPARED,result);

		ISOMsg rspMsg = ISOContextUtil.getOutIsoMsg(ctx);

		assertEquals("910A7EDA35498566A2AA3030",ISOUtil.hexString(rspMsg.getBytes(55)));
		rspMsg.dump(System.out, "");
	}

	@Test
	public void testNotIccPEM() {
		ArpcGenParticipant genParticipant = new ArpcGenParticipant();
		Logger logger = new Logger();
		logger.addListener(new SimpleLogListener());
		genParticipant.setLogger(logger, "arpc-gen");


		SimpleConfiguration cfg = new SimpleConfiguration();
		cfg.put("pem", "05");
		try {
			genParticipant.setConfiguration(cfg);
		}catch(ConfigurationException cfe) {
			assertFalse("ConfigurationException is not expected",false);
		}

		ISOMsg msg = constructIsoMsg();
		msg.unset(55);
		msg.set(22,"121");

		Context ctx = new Context();
		ISOContextUtil.setInIsoMsg(ctx, msg);

		int result = genParticipant.prepare(1, ctx);
		assertEquals(TransactionParticipant.PREPARED,result);

		ISOMsg rspMsg = ISOContextUtil.getOutIsoMsg(ctx);

		assertEquals("00",rspMsg.getString(39));
		rspMsg.dump(System.out, "");
	}

	@Test
	public void testNoICC() {
		ArpcGenParticipant genParticipant = new ArpcGenParticipant();
		Logger logger = new Logger();
		logger.addListener(new SimpleLogListener());
		genParticipant.setLogger(logger, "arpc-gen");


		SimpleConfiguration cfg = new SimpleConfiguration();
		cfg.put("pem", "05");
		try {
			genParticipant.setConfiguration(cfg);
		}catch(ConfigurationException cfe) {
			assertFalse("ConfigurationException is not expected",false);
		}

		ISOMsg msg = constructIsoMsg();
		msg.unset(55);

		Context ctx = new Context();
		ISOContextUtil.setInIsoMsg(ctx, msg);

		int result = genParticipant.prepare(1, ctx);
		assertEquals(TransactionParticipant.ABORTED,result);

		ISOMsg rspMsg = ISOContextUtil.getOutIsoMsg(ctx);

		assertEquals("96",rspMsg.getString(39));
		rspMsg.dump(System.out, "");
	}


	@Test
	public void testNoImkAc() {
		ArpcGenParticipant genParticipant = new ArpcGenParticipant();
		Logger logger = new Logger();
		logger.addListener(new SimpleLogListener());
		genParticipant.setLogger(logger, "arpc-gen");


		SimpleConfiguration cfg = new SimpleConfiguration();
		cfg.put("pem", "05");
		try {
			genParticipant.setConfiguration(cfg);
		}catch(ConfigurationException cfe) {
			assertFalse("ConfigurationException is not expected",false);
		}

		ISOMsg msg =  constructIsoMsg();
		msg.set(2,"1234567890123456");
		Context ctx = new Context();
		ISOContextUtil.setInIsoMsg(ctx, msg);

		int result = genParticipant.prepare(1, ctx);
		assertEquals(TransactionParticipant.ABORTED,result);

		ISOMsg rspMsg = ISOContextUtil.getOutIsoMsg(ctx);

		assertEquals("96",rspMsg.getString(39));
		rspMsg.dump(System.out, "");
	}

	@Test
	public void testNoBinManager() {
		ArpcGenParticipant genParticipant = new ArpcGenParticipant();
		Logger logger = new Logger();
		logger.addListener(new SimpleLogListener());
		genParticipant.setLogger(logger, "arpc-gen");


		SimpleConfiguration cfg = new SimpleConfiguration();
		cfg.put("pem", "05");
		try {
			genParticipant.setConfiguration(cfg);
		}catch(ConfigurationException cfe) {
			assertFalse("ConfigurationException is not expected",false);
		}

		ISOMsg msg =constructIsoMsg();
		Context context = new Context();
		ISOContextUtil.setInIsoMsg(context, msg);

		BinManager bm = ctx.getBean(BinManager.class);
		ctx.unregister(BinManager.class);

		int result = genParticipant.prepare(1, context);

		ctx.register(bm);

		assertEquals(TransactionParticipant.ABORTED,result);

		ISOMsg rspMsg = ISOContextUtil.getOutIsoMsg(context);

		assertEquals("96",rspMsg.getString(39));
		rspMsg.dump(System.out, "");
	}



	@Test
	public void testNoKeystoreManager() {
		ArpcGenParticipant genParticipant = new ArpcGenParticipant();
		Logger logger = new Logger();
		logger.addListener(new SimpleLogListener());
		genParticipant.setLogger(logger, "arpc-gen");


		SimpleConfiguration cfg = new SimpleConfiguration();
		cfg.put("pem", "05");
		try {
			genParticipant.setConfiguration(cfg);
		}catch(ConfigurationException cfe) {
			assertFalse("ConfigurationException is not expected",false);
		}

		ISOMsg msg = constructIsoMsg();
		Context context = new Context();
		ISOContextUtil.setInIsoMsg(context, msg);

		KeyStoreManager ksm = ctx.getBean(KeyStoreManager.class);
		ctx.unregister(KeyStoreManager.class);

		int result = genParticipant.prepare(1, context);

		ctx.register(ksm);

		assertEquals(TransactionParticipant.ABORTED,result);

		ISOMsg rspMsg = ISOContextUtil.getOutIsoMsg(context);

		assertEquals("96",rspMsg.getString(39));
		rspMsg.dump(System.out, "");
	}

	@Test
	public void testNoSecurityModule() {
		ArpcGenParticipant genParticipant = new ArpcGenParticipant();
		Logger logger = new Logger();
		logger.addListener(new SimpleLogListener());
		genParticipant.setLogger(logger, "arpc-gen");


		SimpleConfiguration cfg = new SimpleConfiguration();
		cfg.put("pem", "05");
		try {
			genParticipant.setConfiguration(cfg);
		}catch(ConfigurationException cfe) {
			assertFalse("ConfigurationException is not expected",false);
		}

		ISOMsg msg = constructIsoMsg();
		Context context = new Context();
		ISOContextUtil.setInIsoMsg(context, msg);

		GeckoSecurityModule gsm = ctx.getBean(GeckoSecurityModule.class);
		ctx.unregister(GeckoSecurityModule.class);

		int result = genParticipant.prepare(1, context);

		ctx.register(gsm);

		assertEquals(TransactionParticipant.ABORTED,result);

		ISOMsg rspMsg = ISOContextUtil.getOutIsoMsg(context);

		assertEquals("96",rspMsg.getString(39));
		rspMsg.dump(System.out, "");
	}
}
