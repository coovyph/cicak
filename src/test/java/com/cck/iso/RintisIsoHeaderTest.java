package com.cck.iso;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class RintisIsoHeaderTest {
	@Test
	public void testPack() {
		RintisISOHeader header = new RintisISOHeader();
		header.setBase24Header("TST");

		header.setProductIndicator("01");
		header.setReleaseNumber("60");
		header.setStatus("000");
		header.setOriginCode("7");
		header.setResponderCode("7");
		
		assertEquals("TST016000077", new String(header.pack()));
	
	}
	
	@Test
	public void testPack2() {
		RintisISOHeader header = new RintisISOHeader();
		header.setProductIndicator("01");
		header.setReleaseNumber("60");
		header.setStatus("000");
		header.setOriginCode("7");
		header.setResponderCode("7");
		
		assertEquals("ISO016000077", new String(header.pack()));
	
	}
	
	@Test
	public void testPack3() {
		Map<String,String> map = new HashMap<>();
		map.put(RintisISOHeader.H_B24_HEADER, "IS5");
		map.put(RintisISOHeader.H_PRODUCT_INDICATOR, "55");
		map.put(RintisISOHeader.H_RELEASE_NUMBER, "60");
		map.put(RintisISOHeader.H_STATUS, "012");
		map.put(RintisISOHeader.H_ORIGIN_CODE, "7");
		map.put(RintisISOHeader.H_RESPONDER_CODE, "7");
		
		RintisISOHeader header = new RintisISOHeader();
		header.setValue(map);
		
		assertEquals("IS5", header.getBase24Header());

		assertEquals("IS5556001277", new String(header.pack()));
	}
	
	@Test
	public void testUnpack() {
		RintisISOHeader header = new RintisISOHeader();
		header.unpack("ISO016000077".getBytes());
		
		assertEquals("ISO",header.getBase24Header());
		assertEquals("01",header.getProductIndicator());
		assertEquals("60",header.getReleaseNumber());
		assertEquals("000",header.getStatus());
		assertEquals("7",header.getOriginCode());
		assertEquals("7",header.getResponderCode());
	}
	
	@Test
	public void testUnpack2() {
		RintisISOHeader header = new RintisISOHeader();
		header.unpack("ISO016000077".getBytes());
		
		assertEquals("ISO",header.getBase24Header());
		assertEquals("01",header.getProductIndicator());
	}
}
