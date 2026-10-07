package com.cck.component.field;

import java.util.Map;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.security.EncryptedPIN;
import org.jpos.security.SMAdapter;
import org.jpos.security.SecureDESKey;
import org.jpos.security.jceadapter.GeckoSecurityModule;
import org.springframework.stereotype.Component;

import com.cck.component.KeyStoreManager;

import lombok.RequiredArgsConstructor;

@Component("fieldP")
@RequiredArgsConstructor
public class PinblockFieldGenerator extends AFieldValueGenerator{


	private final GeckoSecurityModule sm;
	private final KeyStoreManager ksm;




	private Object constructPinBlock(SecureDESKey kwk,String id,String pan,String clearPin)throws FieldGeneratorException {

		boolean isBinary = clearPin.startsWith("0x");

		if(isBinary) {
			clearPin = clearPin.substring(2);	
		}
		try{
			EncryptedPIN pinUndlerLMK =  sm.encryptPIN(clearPin, pan);
			EncryptedPIN pinUnderKwk  = sm.exportPIN(pinUndlerLMK, kwk, SMAdapter.FORMAT00);
			if(isBinary) {
				return pinUnderKwk.getPINBlock();
			}else {
				return ISOUtil.hexString(pinUnderKwk.getPINBlock());
			}
		}catch(Exception e) {
			throw new FieldGeneratorException("Cannot construct pinblock",e);
		}



	}



	protected String constructZpkKeyName(String interchangeName) {
		StringBuilder sb = new StringBuilder();

		sb.append(SMAdapter.TYPE_ZPK);
		sb.append("-");
		sb.append(interchangeName);
		sb.append("-acq");

		return sb.toString();
	}





	@Override
	public Object constructMsgValue(ISOMsg msg, String interchangeName, Map<String, Object> msgBody, FieldDefinition fd)
			throws FieldGeneratorException {
		Object objClearPin = msgBody.get(fd.getKey());
		if(!(objClearPin instanceof String)){
			throw new FieldGeneratorException("Invalid value for id " + fd.getKey());
		}

		String clearPin = (String)objClearPin;
		String zpkName = constructZpkKeyName(interchangeName);

		if(zpkName == null) {
			throw new FieldGeneratorException("ZPK name is not valid");
		}

		SecureDESKey kwk = ksm.getKey(zpkName);
		if(kwk==null) {
			throw new FieldGeneratorException("working key is not found for zpk '" + zpkName  + "', please do keychange first");
		}

		String pan = extractPan(msg);

		return constructPinBlock(kwk,fd.getFieldId(),pan,clearPin);
	}

}
