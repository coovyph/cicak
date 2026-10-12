package com.cck.component.field;

import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.security.SecureDESKey;

import com.cck.component.BinManager;
import com.cck.component.KeyStoreManager;
import com.cck.tlv.EmvFields;
import com.cck.util.EMVTag;
import com.cck.util.StringUtils;
import com.cck.util.StringUtilsException;

import lombok.RequiredArgsConstructor;

/**
 * Base Field generator for ICC, since VISA, NPG and MCHIP
 * share some identical componen
 */
@RequiredArgsConstructor
public abstract class BaseChipFieldGenerator extends AFieldValueGenerator{
	public static final String F_MKAC = "mkac";


	public static final String DEFAULT_CARD_VERIFICATION_RESULT = "020000";

	public static final String TERMINAL_VERIFICATION_RESULT = "8000048000";
	public static final String DEFAULT_TERMINAL_COUNTRY_CODE = "0360";

	private final Random random;
	private final KeyStoreManager km;
	private final BinManager bm;


	private String getImklKeyNameFromPan(ISOMsg msg) {
		String pan = extractPan(msg);
		if(pan==null)return null;


		String imkAcName = this.bm.getImkAcKeyName(pan);
		if(imkAcName == null)return KeyStoreManager.DEFAULT_IMK_AC;
		return imkAcName;
	}
	
	protected String extractPanSequenceNo(ISOMsg msg,EmvFields emvFields) {
		String panSeqNo = emvFields.getString(EMVTag._5F34_APPLICATION_PAN_SEQ_NR);
		if(panSeqNo == null) {
			panSeqNo = msg.getString(23);
		}
		
		if(panSeqNo!=null && panSeqNo.length()>2) {
			panSeqNo = panSeqNo.substring(panSeqNo.length()-2);
		}
		return panSeqNo;
	}

	protected SecureDESKey retrieveImkAc(ISOMsg msg, Object name)throws FieldGeneratorException {
		String imkKeyName;
		if(name==null) {
			imkKeyName = getImklKeyNameFromPan(msg);			
		}else {
			imkKeyName = name.toString();
		}

		if(imkKeyName == null) {
			throw new FieldGeneratorException(F_MKAC + " field is not set and no IMK-AC for this pan");
		}


		SecureDESKey desKey = km.getKey(imkKeyName);
		if(desKey==null) {
			throw new FieldGeneratorException("Cannot find " + F_MKAC +  " key '" + imkKeyName	 + "'");
		}
		return desKey;
	}



	protected String constructUnpredictableNumber(){
		byte[] result = new byte[4];
		for(int i=0;i<4;i++) {
			result[i] = (byte)this.random.nextInt(255);
		}
		return ISOUtil.hexString(result);
	}

	private int extractEmvTag(Object val) {
		if(val==null)return -1;
		String key = val.toString().toUpperCase();
		if(!key.startsWith("V"))return -1;
		String id = key.substring(1);
		try {
			return Integer.parseInt(id, 16);
		}catch(Exception ioe) {

		}
		return -1;
	}

	protected void prepareIccTagOverride(EmvFields emvFields,Map<String,Object> valMap) {
		valMap.forEach((k,v)->{
			int emvTag = extractEmvTag(k);
			if(emvTag!=-1 && v!=null) {
				emvFields.setField(emvTag,v.toString());
			}
		});

		//override with default value if is not exists
		appendIfEmpty(emvFields, EMVTag._9F1A_TERMINAL_COUNTRY_CODE, DEFAULT_TERMINAL_COUNTRY_CODE);
		appendIfEmpty(emvFields,EMVTag._9F36_APPLICATION_TRANSACTION_COUNTER, "0000");
	}

	private void appendIfEmpty(EmvFields fields,int tag,String value) {
		byte[] currentValue = fields.getValue(tag);
		if(currentValue == null) {
			fields.setField(tag, value);
		}
	}

	protected void prepareEmvFields(EmvFields emvFields, ISOMsg msg) {
		String transactionAmount =  msg.getString(4);
		if(transactionAmount==null)transactionAmount = "000000000000";
		try {
			emvFields.setField(EmvFields._9F02_AMOUNT_AUTHORIZED_NUMERIC, StringUtils.padleft(transactionAmount, 12, '0'));
		}catch(StringUtilsException se) {
			//do nothing
		}
		emvFields.setField(EmvFields._9F03_AMOUNT_OTHER_NUMERIC,"000000000000");


		String processingCode = msg.getString(3);
		if(processingCode!=null) {
			String tranType = processingCode.substring(0,2);
			emvFields.setField(EMVTag._9C_TRANSACTION_TYPE,tranType);
		}

		String pan = extractPan(msg);
		if(pan!=null) {
			emvFields.setField(EMVTag._5A_APPLICATION_PAN,pan);
		}

		String tranCurrencyCode = msg.getString(49);
		if(tranCurrencyCode!=null) {
			emvFields.setField(EMVTag._5F2A_TRANSACTION_CURRENCY_CODE,tranCurrencyCode);
		}

		String panSequenceNo = msg.getString(23);
		if(panSequenceNo!=null) {
			if(panSequenceNo.length()>2) panSequenceNo = panSequenceNo.substring(panSequenceNo.length()-2);
			emvFields.setField(EMVTag._5F34_APPLICATION_PAN_SEQ_NR,panSequenceNo);
		}

		emvFields.setField(EMVTag._9F37_UNPREDICTABLE_NUMBER,constructUnpredictableNumber());
		emvFields.setField(EMVTag._95_TERMINAL_VERIFICATION_RESULTS,TERMINAL_VERIFICATION_RESULT);
		emvFields.setField(EMVTag._9F34_CVM_RESULTS,DEFAULT_CARD_VERIFICATION_RESULT);

	}

	protected void ommitEmv(EmvFields emvFields,Map<String, Object> valMap) {
		Object objOmit = valMap.get("ommit");
		if(!(objOmit instanceof String))return;

		StringTokenizer tokenz = new StringTokenizer((String)objOmit);

		tokenz.asIterator().forEachRemaining(v->emvFields.remove(v.toString()));
	}

	public Object constructMsgValue(ISOMsg msg, String interchangeName, Map<String, Object> msgBody, FieldDefinition fd)
			throws FieldGeneratorException {
		Object val = msgBody.get(fd.getKey());
		if(!(val instanceof Map)) {
			throw new FieldGeneratorException("Invalid icc parameter value for " + fd.getKey());
		}

		@SuppressWarnings("unchecked")
		Map<String,Object> valMap = (Map<String,Object>)val;

		EmvFields emvFields = new EmvFields();
		prepareEmvFields(emvFields, msg);
		prepareIccTagOverride(emvFields, valMap);

		byte[] arqc = emvFields.getValue(EmvFields._9F26_APPLICATION_CRYPTOGRAM);
		if(arqc==null)calculateAcquirerArqc(emvFields,msg,valMap);

		ommitEmv(emvFields, valMap);

		Object format = valMap.get("format");

		if(format!=null && "B".equalsIgnoreCase(format.toString())) {
			return  emvFields.pack();
		}else {
			return ISOUtil.hexString(emvFields.pack());
		}
	}


	protected abstract void calculateAcquirerArqc(EmvFields emvFields,ISOMsg msg,Map<String,Object> valMap)throws FieldGeneratorException;
}
