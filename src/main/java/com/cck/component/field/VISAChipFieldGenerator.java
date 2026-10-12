package com.cck.component.field;

import java.util.Map;
import java.util.Random;

import org.jpos.iso.ISOMsg;
import org.jpos.security.SecureDESKey;
import org.springframework.stereotype.Component;

import com.cck.component.BinManager;
import com.cck.component.KeyStoreManager;
import com.cck.tlv.EmvFields;
import com.cck.util.CryptoUtil;
import com.cck.util.CryptoCckException;

@Component("fieldVI")
public class VISAChipFieldGenerator extends BaseChipFieldGenerator{


	public VISAChipFieldGenerator(Random random,KeyStoreManager ikm,BinManager ibm) {
		super(random,ikm,ibm);
	}


	@Override
	protected void calculateAcquirerArqc(EmvFields emvFields, ISOMsg msg, Map<String, Object> valMap)throws FieldGeneratorException {
		SecureDESKey mkac = retrieveImkAc(msg,valMap.get(F_MKAC));
		String pan = extractPan(msg);
		String panSeqNum = extractPanSequenceNo(msg, emvFields);

		byte[] arqc = null;

		try{
			arqc = CryptoUtil.constructVchipArqc(pan, panSeqNum, mkac.getKeyBytes(), emvFields);
		}catch(CryptoCckException e) {
			throw new FieldGeneratorException("Exception when generating arqc",e);
		}

		emvFields.setField(EmvFields._9F26_APPLICATION_CRYPTOGRAM,arqc);
	}




}
