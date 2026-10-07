package com.cck.component.field;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;


public class FieldDefinitionTest {
	@Test
	public void testFieldDefinitionNormal() {
		FieldDefinition fd = new FieldDefinition();
		try {
			fd.parse("f2");
		}catch(FieldDefinitionException fde) {
			assertFalse("Does not expect error",true);
		}
		assertEquals("F", fd.getSuffix());
		assertEquals("2",fd.getFieldId());

	}
	
	@Test
	public void testFieldDefinitionWithUnderline() {
		FieldDefinition fd = new FieldDefinition();
		try {
			fd.parse("f_2");
		}catch(FieldDefinitionException fde) {
			assertFalse("Does not expect error",true);
		}
		assertEquals("F", fd.getSuffix());
		assertEquals("2",fd.getFieldId());

	}
	
	@Test
	public void testFieldDefinitionMoreThanOneSuffix() {
		FieldDefinition fd = new FieldDefinition();
		try {
			fd.parse("f_2");
		}catch(FieldDefinitionException fde) {
			assertFalse("Does not expect error",true);
		}
		assertEquals("F", fd.getSuffix());
		assertEquals("2",fd.getFieldId());

	}
	
	@Test
	public void testFieldDefinitionInvalidField() {
		FieldDefinition fd = new FieldDefinition();
		try {
			fd.parse("f");
			assertFalse("Does not expect parsing success",true);
		}catch(FieldDefinitionException fde) {
			assertTrue("Expect  error",true);
			return;
		}

	}
	
	@Test
	public void testFieldDefinitionInvalidField2() {
		FieldDefinition fd = new FieldDefinition();
		try {
			fd.parse("f_");
			assertFalse("Does not expect parsing success",true);
		}catch(FieldDefinitionException fde) {
			assertTrue("Expect  error",true);
			return;
		}


	}
}
