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

@Component("fieldMI")
public class MchipIccFieldGenerator extends BaseChipFieldGenerator{


	public static final String MCHIP_AID = "A0000000041010";
	public static final String MCHIP_IAD = "010103250000DAC1";


	public MchipIccFieldGenerator(Random random,KeyStoreManager km,BinManager bm) {
		super(random,km,bm);
	}



	@Override
	protected void prepareEmvFields(EmvFields emvFields, ISOMsg msg) {
		super.prepareEmvFields(emvFields, msg);
		emvFields.setField(EmvFields._9F10_ISSUER_APPLICATION_DATA,MCHIP_IAD);
		emvFields.setField(EmvFields._84_DF_NAME, MCHIP_AID);
	}


	@Override
	protected void calculateAcquirerArqc(EmvFields emvFields, ISOMsg msg, Map<String, Object> valMap)
			throws FieldGeneratorException {
		SecureDESKey mkac = retrieveImkAc(msg, valMap.get(F_MKAC));


		String pan = extractPan(msg);
		String panSeqNum = extractPanSequenceNo(msg, emvFields);

		try {
			byte[] arqc = CryptoUtil.constructMchipArqc(pan, panSeqNum, mkac.getKeyBytes(), emvFields);
			emvFields.setField(EmvFields._9F26_APPLICATION_CRYPTOGRAM, arqc);
		}catch(CryptoCckException ce) {
			throw new FieldGeneratorException("Cannot generate MCHIP arqc",ce);
		}


	}

}
