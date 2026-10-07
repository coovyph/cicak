package com.cck.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jpos.security.SMException;
import org.jpos.security.jceadapter.GeckoSecurityModule;
import org.jpos.util.NameRegistrar;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cck.component.BinManager;
import com.cck.component.KeyStoreManager;
import com.cck.component.field.ICCFieldGenerator;
import com.cck.component.field.ISOFieldGenerator;
import com.cck.component.field.PinblockFieldGenerator;
import com.cck.component.field.TlvLLFieldGenerator;
import com.cck.dto.CckBody;
import com.cck.dto.CckHeaders;
import com.cck.dto.CckMsg;
import com.cck.mock.AppContextMock;
import com.cck.mock.QMUXMock;
import com.cck.util.CicakDateUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class CallServiceImplTest {
	private static AppContextMock ctx;
	private ObjectMapper objectMapper = new ObjectMapper();

	@InjectMocks
	private static CallServiceImpl callServiceImpl;

	@InjectMocks
	private static GeneratorRegistrarServiceImpl registrar;

	@BeforeAll
	public static void prepare()throws SMException {
		GeckoSecurityModule gsm = new GeckoSecurityModule("resources/lmk");
		KeyStoreManager ksm = new KeyStoreManager("resources/key.prop");
		ksm.reloadKey();

		BinManager bm = new BinManager("resources/bin.prop");
		bm.reload();


		registrar = new GeneratorRegistrarServiceImpl();
		registrar.setApplicationContext(ctx);

		ctx = new AppContextMock();
		ctx.register("fieldF",new ISOFieldGenerator(registrar));
		ctx.register("fieldP",new PinblockFieldGenerator(gsm,ksm));
		ctx.register("fieldI",new ICCFieldGenerator(gsm, ksm, bm));
		ctx.register("fieldTLV2",new TlvLLFieldGenerator(registrar));


		callServiceImpl = new CallServiceImpl(registrar);

		QMUXMock muckMock = new QMUXMock(true);
		NameRegistrar.register("mux.TEST_mux", muckMock);

	}

	@AfterAll
	public static void cleanUp() {
		NameRegistrar.unregister("mux.TEST_mux");
	}

	private CckMsg constructCckMsg() {
		CckMsg cckMsg = new CckMsg();
		cckMsg.setMti("0200");

		CckHeaders cckHeaders = new CckHeaders();
		cckHeaders.put("interchange", "TEST");

		CckBody cckBody = new CckBody();
		cckBody.put("f2", "5393710500000060");
		cckBody.put("f3", "301000");
		cckBody.put("f4", "000000000000");
		cckBody.put("f7", CicakDateUtil.formatGmtDateTime(LocalDateTime.now()));
		cckBody.put("f11", "123456");
		cckBody.put("f12", CicakDateUtil.formatLocalTime(LocalDateTime.now()));
		cckBody.put("f13", CicakDateUtil.formatLocalDate(LocalDate.now()));
		cckBody.put("f15", CicakDateUtil.formatLocalDate(LocalDate.now()));
		cckBody.put("f18", "6011");
		cckBody.put("f22", "051");
		cckBody.put("f25", "00");
		cckBody.put("f32", "126");
		cckBody.put("f33", "360001");
		cckBody.put("f35", "5393710500000060=26122010000067301");
		cckBody.put("f37","12345678912");
		cckBody.put("f41", "10000001");
		cckBody.put("f42", "ATM JAG10000001");
		cckBody.put("f43", "GATOT SUBROTO                    JBR IDN");
		cckBody.put("f49", "360");
		cckBody.put("p52", "123456");


		Map<String, String> map = new HashMap<String, String>();
		cckBody.put("i55", map);

		map.put("format", "B");
		map.put("v82", "7400");
		map.put("v9F27", "80");
		map.put("v9F09", "0101");
		map.put("v5F34", "00");
		map.put("v9F10", "0101A0008000006A6303F80000000000000000000000000000000000");
		map.put("v9A",  "130525");
		map.put("v84", "A0000006021010");
		map.put("v9F36", "0001");


		Map<String,Object> map48 = new LinkedHashMap<String, Object>();
		map48.put("f1", "T");
		map48.put("tlv2_02", "s");

		cckBody.put("cmp_48", map48);

		cckMsg.setBody(cckBody);
		cckMsg.setHeaders(cckHeaders);

		return cckMsg;
	}

	@Test
	public void testCallNormal() throws Exception{
		CckMsg cckMsg = constructCckMsg();



		CckMsg rspMsg = callServiceImpl.call(cckMsg);

		System.out.println(this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rspMsg));


	}
}
