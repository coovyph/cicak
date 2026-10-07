package com.cck;


import java.util.HashMap;
import java.util.Map;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.MUX;
import org.jpos.security.jceadapter.GeckoSecurityModule;
import org.jpos.util.NameRegistrar;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import com.cck.bean.AInterchange;
import com.cck.component.field.ICCFieldGenerator;
import com.cck.component.field.ISOFieldGenerator;
import com.cck.dto.CckMsg;

public class TestIccFieldGenerator {

	private void prepareInterchange() {
		AInterchange interchange = new AInterchange() {



			@Override
			public String getName() {
				// TODO Auto-generated method stub
				return "ich";
			}
		
			@Override
			public boolean doSignOn(MUX mux) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean doSignOff(MUX mux) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean doKeyChange(MUX mux) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			protected boolean doEchoTest(MUX mux) {
				// TODO Auto-generated method stub
				return false;
			}
		};

		interchange.init();
	}

	private CckMsg prepareTestMsg() {
		CckMsg cckMsg = new CckMsg();
		cckMsg.setMti("0200");
		cckMsg.putBodyValue("f2", "5393710500000060");
		cckMsg.putBodyValue("f3","311000");
		cckMsg.putBodyValue("f35", "5393710500000060D26122010000067301");
		cckMsg.putBodyValue("f49", "360");
		cckMsg.putBodyValue("i55", constructIccDataRequest());

		cckMsg.setInterchange("ich");
		return cckMsg;
	}

	private Map<String,String> constructIccDataRequest(){
		Map<String, String> ficc = new HashMap<>();
		ficc.put("V82", "7400");
		ficc.put("V85", "8000040000");
		ficc.put("V5F2A","0360");
		ficc.put("V5F34","00");
		ficc.put("V9A","220715");

		ficc.put("V9F02","000000000000");
		ficc.put("V9F03","000000000000");
		ficc.put("V9F10","0601A000800000B65FC2FC0000000000000000000000000000000000");
		ficc.put("V9F1A","0360");
		ficc.put("V9F26","114B283785DB3988");
		ficc.put("V9F36","00EC");
		ficc.put("V9F37","A3E54EB4");

		ficc.put("mkac","BCAD");



		return ficc;
	}

	private void prepareSecurityModule() {
		GeckoSecurityModule sm =  null;
		try{
			sm = new GeckoSecurityModule("resources/lmk");
		}catch(Exception e) {
			e.printStackTrace();
		}
		sm.setName("sm");

		NameRegistrar.register("sm", sm);

	}

	@Test
	public void testIssuerManager()throws Exception {
		try {
			prepareInterchange();
			prepareSecurityModule();
			GeckoSecurityModule gsm = new GeckoSecurityModule("resources/lmk");
			/*ICCFieldGenerator iccFieldGenerator = new ICCFieldGenerator(gsm);


			ISOMsg msg = new ISOMsg();
			CckMsg cckMsg = prepareTestMsg();

			iccFieldGenerator.setMsgValue(msg, cckMsg,"i55");


			ISOFieldGenerator fgenerator = new ISOFieldGenerator();
			fgenerator.setMsgValue(msg, cckMsg, "f2");
			fgenerator.setMsgValue(msg, cckMsg, "f3");

			msg.dump(System.out, "");
			*/

		}catch(Exception e) {
			e.printStackTrace();
		}
		//assertEquals(expected.toString(), actual==null?null:actual.toString());
	}


	public static void main(String[] args) {
		Result result = JUnitCore.runClasses(TestIccFieldGenerator.class);

		for (Failure failure : result.getFailures()) {
			System.out.println("Fail : " + failure.toString());
			failure.getException().printStackTrace();
		}
		System.out.println("Result Success ? " + result.wasSuccessful());

	}
}
