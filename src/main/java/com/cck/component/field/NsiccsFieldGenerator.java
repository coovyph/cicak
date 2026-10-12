package com.cck.component.field;

import java.util.Map;
import java.util.Random;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.security.SecureDESKey;
import org.springframework.stereotype.Component;

import com.cck.component.BinManager;
import com.cck.component.KeyStoreManager;
import com.cck.tlv.EmvFields;
import com.cck.util.CryptoUtil;
import com.cck.util.CryptoCckException;
import com.cck.util.EMVTag;

@Component("fieldNI")
public class NsiccsFieldGenerator extends BaseChipFieldGenerator{
	public static final String F_MKAC = "mkac";
	public static final String DEFAULT_CARD_VERIFICATION_RESULT = "020000";

	public static final String TERMINAL_VERIFICATION_RESULT = "8000048000";
	public static final String DEFAULT_TERMINAL_COUNTRY_CODE = "0360";


	public NsiccsFieldGenerator(Random random,KeyStoreManager km,BinManager bm) {
		super(random,km,bm);
	}
	

	@Override
	protected void calculateAcquirerArqc(EmvFields emvFields, ISOMsg msg, Map<String, Object> valMap)
			throws FieldGeneratorException {
		SecureDESKey mkac =retrieveImkAc(msg,valMap.get(F_MKAC));
		String pan = extractPan(msg);
		String panSeqNum = extractPanSequenceNo(msg,emvFields);

		byte[] arqc = null;
		try{
			arqc = CryptoUtil.constructNSICCSArqc(pan, panSeqNum, mkac.getKeyBytes(), emvFields);
		}catch(CryptoCckException ce) {
			throw new FieldGeneratorException("Cannot construct nsiccs arqc ",ce);
		}
		emvFields.setField(EMVTag._9F26_APPLICATION_CRYPTOGRAM, ISOUtil.hexString(arqc));
		
	}


}
