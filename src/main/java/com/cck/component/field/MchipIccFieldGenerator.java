package com.cck.component.field;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.security.SecureDESKey;
import org.jpos.security.jceadapter.GeckoSecurityModule;
import org.jpos.tlv.TLVList;
import org.springframework.stereotype.Component;

import com.cck.component.BinManager;
import com.cck.component.KeyStoreManager;
import com.cck.util.MastercardUtility;
import com.cck.util.EMVTag;

import lombok.extern.slf4j.Slf4j;

@Component("fieldM")
@Slf4j
public class MchipIccFieldGenerator extends ICCFieldGenerator{

	public static final String F_MKAC = "mkac";



	public static final String DEFAULT_CARD_VERIFICATION_RESULT = "410302";

	public static final String TERMINAL_VERIFICATION_RESULT = "8000048000";
	public static final String DEFAULT_TERMINAL_COUNTRY_CODE = "0360";

	public static final String MCHIP_AID = "A0000000041010";
	public static final String MCHIP_IAD = "010103250000DAC1";

	

	public static final DateTimeFormatter LOCAL_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");
	

	public MchipIccFieldGenerator(GeckoSecurityModule sm,KeyStoreManager km,BinManager bm) {
		super(sm,km,bm);
	}



	private void calculateAcquirerCryptogram(TLVList tlvList,ISOMsg msg, Map valMap) throws FieldGeneratorException{

		SecureDESKey mkac = retrieveImkAc(msg, valMap.get(F_MKAC));


		String pan = extractPan(msg);
		String panSeqNum = msg.getString(23);
		if(panSeqNum==null) {
			panSeqNum  = "00";
		}else {
			if(panSeqNum.length()>2) {
				panSeqNum = panSeqNum.substring(panSeqNum.length()-2);
			}
		}
		
		try {
			MastercardUtility.calculateCryptogram(this.sm, mkac, tlvList, pan, panSeqNum);
		}catch(ISOException e) {
			throw new FieldGeneratorException(e);
		}
	}

	private void prepareICCTag(TLVList tlvList, ISOMsg msg) {
		String transactionAmount =  msg.getString(4);
		if(transactionAmount==null)transactionAmount = "000000000000";
		try {
			tlvList.append(EMVTag._9F02_AMOUNT_AUTHORIZED_NUMERIC,ISOUtil.zeropad(transactionAmount,12));
		}catch(ISOException ioe) {
			//should never happens
		}
		tlvList.append(EMVTag._9F03_AMOUNT_OTHER_NUMERIC,"000000000000");


		String processingCode = msg.getString(3);
		if(processingCode!=null) {
			String tranType = processingCode.substring(0,2);
			tlvList.append(EMVTag._9C_TRANSACTION_TYPE,tranType);
		}


		String tranCurrencyCode = msg.getString(49);
		if(tranCurrencyCode!=null) {
			tlvList.append(EMVTag._5F2A_TRANSACTION_CURRENCY_CODE,tranCurrencyCode);
		}


		tlvList.append(EMVTag._9F37_UNPREDICTABLE_NUMBER,constructUnpredictableNumber());
		tlvList.append(EMVTag._95_TERMINAL_VERIFICATION_RESULTS,TERMINAL_VERIFICATION_RESULT);
		tlvList.append(EMVTag._9F34_CVM_RESULTS,DEFAULT_CARD_VERIFICATION_RESULT);

		tlvList.append(EMVTag._9A_TRANSACTION_DATE,LOCAL_DATETIME_FORMATTER.format(LocalDateTime.now()));
		tlvList.append(EMVTag._84_DF_NAME,MCHIP_AID);
		tlvList.append(EMVTag._9F27_CRYPTOGRAM_INFORMATION_DATA,"80");
		tlvList.append(EMVTag._82_APPLICATION_INTERCHANGE_PROFILE,"5800");
		tlvList.append(EMVTag._9F10_ISSUER_APPLICATION_DATA,MCHIP_IAD);
	}


	@Override
	public Object constructMsgValue(ISOMsg msg, String interchangeName, Map<String, Object> msgBody, FieldDefinition fd)
			throws FieldGeneratorException {

		Object objValue = msgBody.get(fd.getKey());

		if(!(objValue instanceof Map)) {
			throw new FieldGeneratorException("Invalid icc parameter value for " + fd.getKey());
		}

		Map<String, Object> fieldMap = (Map<String, Object>)objValue;

		TLVList tlvList = new TLVList();
		prepareICCTag(tlvList,msg);

		
		prepareIccTagOverride(tlvList, fieldMap);
		
		//tlvList.dump(System.out, "");
		calculateAcquirerCryptogram(tlvList,msg,fieldMap);


		Object format = fieldMap.get("format");

		if(format!=null && "B".equalsIgnoreCase(format.toString())) {
			return tlvList.pack();
		}else {
			return ISOUtil.hexString(tlvList.pack());
		}
	}

}
