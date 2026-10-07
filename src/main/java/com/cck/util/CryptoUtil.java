package com.cck.util;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;
import org.jpos.security.CipherMode;
import org.jpos.security.jceadapter.JCEHandlerException;
import org.jpos.tlv.TLVList;

/**
 * NSICCS Crypto 
  1. Format the pan data mengunakan Option A:
     a. gabungin informasi pan + pansequence number, DE 2 + DE 23 (5F34)
        pastikan minimal nomor kartu panjang 14, jika kurang dari 14 tambahkan padding 0 di depan
        pan data = pan + pan sequence number, di convert  BCD menjadi bytes
        check di method preparePANPSN
     b. Jika pan + pan sequence number > 8 bytes, maka ambil 8 bytes terakhir saja.
        check di method formatPANPSNOptionA
     Hasil dari langkah ini akan menghasilkan pansn bytes

  2. Derive IMKAC key, ini standard common derive yang pertama: 
     1. generate leftbytes dengan cara encrypt pansn 8 bytes dengan imkac
     2. generate rightbytes dengan cara pansn di xor dengan FFFFFFFFFFFFFFFF. kemudian
        di encrypt dengan IMKAC

     3. commonDeriveImkAc = lefbytes + rightbytes, gabungan kedua set bytes tersebut, kemudian
        di adjust odd parity

  3. Derive lagi commonDeriveImkAc dari step 2. 
     1. Buat 8 bytes array namakan skl. isi 2 bytes terakhir dengan ATC. kemudian byte ke 3 diisi dengan 0xF0.
        darimana byte magic 0xF0 ga tahu juga.
        kemudian encrypt skl dengan commonDeriveImkAc
     2. Buat 8 bytes array namakan skr. isi 2 bytes terakhir dengan ATC. kemudian byte ke 3 diisi dengan 0x0F.
        darimana byte magic 0x0F ga tahu juga.
        kemudian encrypt skr dengan commonDeriveImkAc
     3. set deriveKey = skl + skr, gabungan kedua set bytes tersebut, kemudian
        di adjust odd parity

  4. Construct authentication data, auth_data adalah gabungan dari data-data ICC , lihat saja di code
     kemudian di padding dengan 80 dan 0. Secara hexstring authdata mesti kelipatan 16

  5. Construct ARQC berdasarkan algoritma MAC ISO/IEC 9797-1 Alg 3
    dengan input auth_data dan key = deriveKey




 */
public class CryptoUtil {

	static final String ALG_DES = "DES";
	static final String ALG_TRIPLE_DES = "DESede";
	static final String DES_MODE_ECB = "ECB";
	static final String DES_MODE_CBC = "CBC";
	static final String DES_NO_PADDING = "NoPadding";

	private static final String HEX_PAD = "80";
	private static final String HEX_NEXT_PAD = "0";

	static final byte[] fPaddingBlock = ISOUtil.hex2byte("FFFFFFFFFFFFFFFF");

	private CryptoUtil() {
		//do nothing
	}

	public static final byte[] constructNSICCSArqc(String pan,String panSequenceNo,
			byte[] imkAc,TLVList tlvList)throws CryptoUtilException 
	{
		byte[] atc = tlvList.getValue(EMVTag._9F36_APPLICATION_TRANSACTION_COUNTER);
		//MKD Option : Option A
		byte[] panpsn = formatPANPSNOptionA(pan, panSequenceNo);
		Key mkac = deriveICCMasterKey(new SecretKeySpec(normalizeKeyBytes(imkAc), ALG_TRIPLE_DES), panpsn);

		//SKD Method : EMV_CSKD
		mkac = deriveCommonSK_AC(mkac,atc);

		//a authData
		byte[] authData = createNsiccsCrytogramAuthData(tlvList); 

		return calculateMACISO9797Alg3(mkac, authData);
	}


	public static final byte[] constructVchipArqc(String pan,String panSequenceNo,
			byte[] imkAc,TLVList tlvList)throws CryptoUtilException 
	{

		//MKD Option : Option A
		byte[] panpsn = formatPANPSNOptionA(pan, panSequenceNo);
		Key mkac = deriveICCMasterKey(new SecretKeySpec(normalizeKeyBytes(imkAc), ALG_TRIPLE_DES), panpsn);


		//a authData
		byte[] authData = createVchipAuthData(tlvList); 


		return calculateMACISO9797Alg3(mkac, authData);
	}

	private static final byte[] createMchipAuthData(TLVList tlvList) {
		String tranAmountAuthorised = tlvList.getString(EMVTag._9F02_AMOUNT_AUTHORIZED_NUMERIC);

		String tranAmountOther = tlvList.getString(EMVTag._9F03_AMOUNT_OTHER_NUMERIC);
		if(tranAmountOther==null) tranAmountOther = "000000000000";

		String terminalCountryCode = tlvList.getString(EMVTag._9F1A_TERMINAL_COUNTRY_CODE);
		String tvr = tlvList.getString(EMVTag._95_TERMINAL_VERIFICATION_RESULTS);
		String tranCurrencyCode = tlvList.getString(EMVTag._5F2A_TRANSACTION_CURRENCY_CODE);
		String tranDateLocal = tlvList.getString(EMVTag._9A_TRANSACTION_DATE);
		String tranType=tlvList.getString(EMVTag._9C_TRANSACTION_TYPE);

		String 	unpredictableNumber = tlvList.getString(EMVTag._9F37_UNPREDICTABLE_NUMBER);
		String 	aip = tlvList.getString(EMVTag._82_APPLICATION_INTERCHANGE_PROFILE);
		String atc = tlvList.getString(EMVTag._9F36_APPLICATION_TRANSACTION_COUNTER);
		String iad = tlvList.getString(EMVTag._9F10_ISSUER_APPLICATION_DATA);

		StringBuilder sb = new StringBuilder();
		sb.append(tranAmountAuthorised);
		sb.append(tranAmountOther);
		sb.append(terminalCountryCode);
		sb.append(tvr);
		sb.append(tranCurrencyCode);
		sb.append(tranDateLocal);
		sb.append(tranType);
		sb.append(unpredictableNumber);
		sb.append(aip);
		sb.append(atc);

		if(iad!=null) {
			sb.append(iad.substring(4,16));
		}
		sb.append(80);

		return ISOUtil.hex2byte(sb.toString());
	}

	public static final byte[] constructMchipArqc(String pan,String panSequenceNo,
			byte[] imkAc,TLVList tlvList)throws CryptoUtilException {
		byte[] atc = tlvList.getValue(EMVTag._9F36_APPLICATION_TRANSACTION_COUNTER);
		byte[] upn = tlvList.getValue(EMVTag._9F37_UNPREDICTABLE_NUMBER);

		//MKD Option : Option A
		byte[] panpsn = formatPANPSNOptionA(pan, panSequenceNo);
		Key mkac = deriveICCMasterKey(new SecretKeySpec(normalizeKeyBytes(imkAc), ALG_TRIPLE_DES), panpsn);

		//SKD Method : MCHIP
		mkac = deriveSK_MK(mkac,atc,upn);

		byte[] authData = createMchipAuthData(tlvList); 

		return calculateMACISO9797Alg3(mkac, authData);
	}


	private static final byte[] createVchipAuthData(TLVList tlvList) {
		String tranAmountAuthorised = tlvList.getString(EMVTag._9F02_AMOUNT_AUTHORIZED_NUMERIC);
		String tranAmountOther = tlvList.getString(EMVTag._9F03_AMOUNT_OTHER_NUMERIC);
		String terminalCountryCode = tlvList.getString(EMVTag._9F1A_TERMINAL_COUNTRY_CODE);
		String tranCurrencyCode = tlvList.getString(EMVTag._5F2A_TRANSACTION_CURRENCY_CODE);
		String tvr = tlvList.getString(EMVTag._95_TERMINAL_VERIFICATION_RESULTS);
		String tranDateLocal = tlvList.getString(EMVTag._9A_TRANSACTION_DATE);
		String tranType=tlvList.getString(EMVTag._9C_TRANSACTION_TYPE);
		String unpredictableNumber = tlvList.getString(EMVTag._9F37_UNPREDICTABLE_NUMBER);
		String aip = tlvList.getString(EMVTag._82_APPLICATION_INTERCHANGE_PROFILE);
		String atc = tlvList.getString(EMVTag._9F36_APPLICATION_TRANSACTION_COUNTER);
		String iad = tlvList.getString(EMVTag._9F10_ISSUER_APPLICATION_DATA);

		StringBuilder sb = new StringBuilder();
		sb.append(tranAmountAuthorised);
		sb.append(tranAmountOther);
		sb.append(terminalCountryCode);
		sb.append(tvr);
		sb.append(tranCurrencyCode);
		sb.append(tranDateLocal);
		sb.append(tranType);
		sb.append(unpredictableNumber);
		sb.append(aip);
		sb.append(atc);
		if(iad!=null && "0A".equals(iad.substring(4,6))) {
			//CVN 10 (0A) only use the CVR
			sb.append(iad.substring(6));
		}else {
			sb.append(iad);
		}
		return ISOUtil.hex2byte(sb.toString());
	} 

	private static final byte[] createNsiccsCrytogramAuthData(TLVList tlvList) {
		String tranAmountAuthorised = tlvList.getString(EMVTag._9F02_AMOUNT_AUTHORIZED_NUMERIC);
		String tranAmountOther = tlvList.getString(EMVTag._9F03_AMOUNT_OTHER_NUMERIC);
		String terminalCountryCode = tlvList.getString(EMVTag._9F1A_TERMINAL_COUNTRY_CODE);
		String tranCurrencyCode = tlvList.getString(EMVTag._5F2A_TRANSACTION_CURRENCY_CODE);
		String tvr = tlvList.getString(EMVTag._95_TERMINAL_VERIFICATION_RESULTS);
		String tranDateLocal = tlvList.getString(EMVTag._9A_TRANSACTION_DATE);
		String tranType=tlvList.getString(EMVTag._9C_TRANSACTION_TYPE);
		String unpredictableNumber = tlvList.getString(EMVTag._9F37_UNPREDICTABLE_NUMBER);
		String aip = tlvList.getString(EMVTag._82_APPLICATION_INTERCHANGE_PROFILE);
		String atc = tlvList.getString(EMVTag._9F36_APPLICATION_TRANSACTION_COUNTER);
		String iad = tlvList.getString(EMVTag._9F10_ISSUER_APPLICATION_DATA);

		StringBuilder sb = new StringBuilder();
		sb.append(tranAmountAuthorised);
		sb.append(tranAmountOther);
		sb.append(terminalCountryCode);
		sb.append(tvr);
		sb.append(tranCurrencyCode);
		sb.append(tranDateLocal);
		sb.append(tranType);
		sb.append(unpredictableNumber);
		sb.append(aip);
		sb.append(atc);
		sb.append(iad);
		sb.append(HEX_PAD);

		String data = sb.toString();
		int length = data.length();
		if(length%16!=0) {
			int multiplier = (length/16) + 1;
			int padLength = (multiplier*16)-length;
			for(int i=0;i<padLength;i++) {
				sb.append(HEX_NEXT_PAD);
			}
		}
		return ISOUtil.hex2byte(sb.toString());
	} 
	/**
	 * Prepare 8-bytes data from PAN and PAN Sequence Number (Option A)
	 * <ul>
	 * <li> Prepare Application PAN and PAN Sequence Number by {@see #preparePANPSN}
	 * <li> Select first 16 digits
	 * </ul>
	 * @param pan application primary account number
	 * @param psn PAN Sequence Number
	 * @return 8-bytes representing first 16 digits
	 */
	private static byte[] formatPANPSNOptionA(String pan, String psn){
		if ( pan.length() < 14 )
			try {
				pan = ISOUtil.zeropad(pan, 14);
			} catch( ISOException ex ) {} //NOPMD: ISOException condition is checked before.
		byte[] b = preparePANPSN(pan, psn);
		return Arrays.copyOfRange(b, b.length-8, b.length);
	}

	/**
	 * Format bytes representing Application PAN and
	 * PAN Sequence Number in BCD format.
	 * <p>
	 * Concatenate from left to right decimal digits of PAN and
	 * PAN Sequence Number digits. If {@code psn} is not present, it is
	 * replaced by a "00" digits. If the result is less than 16 digits long,
	 * pad it to the left with hexadecimal zeros in order to obtain an
	 * 16-digit number. If the Application PAN has an odd number of decimal
	 * digits then concatenate a "0" padding digit to the left thereby
	 * ensuring that the result is an even number of digits.
	 *
	 * @param pan application primary account number
	 * @param psn PAN Sequence Number
	 * @return up to 11 bytes representing Application PAN
	 */
	private static byte[] preparePANPSN(String pan, String psn){
		if (psn == null || psn.isEmpty())
			psn = "00";
		String ret = pan + psn;
		//convert digits to bytes and padd with "0"
		//to left for ensure even number of digits
		return ISOUtil.hex2byte(ret);
	}

	/**
	 * derive ICC master key 
	 * the imk is clear IMK
	 * @param imk
	 * @param panpsn
	 * @return
	 * @throws CryptoUtilException
	 */
	private static Key deriveICCMasterKey(Key imk, byte[] panpsn)
			throws CryptoUtilException {

		byte[] l = Arrays.copyOfRange(panpsn, 0, 8);
		//left part of derived key
		l =  encryptECB(l,imk);

		byte[] r = Arrays.copyOfRange(panpsn, 0, 8);
		//inverse clear right part of key
		r = xor(r, fPaddingBlock);
		//right part of derived key
		r = encryptECB(r,imk);


		//derived key
		byte[] mk = ISOUtil.concat(l,r);
		//fix DES parity of key 
		//i dont have real explanation of this algorithm yet
		adjustDESParity(mk);

		//form JCE Tripple-DES Key
		return new SecretKeySpec(normalizeKeyBytes(mk), ALG_TRIPLE_DES);
	}


	private static Key deriveCommonSK_AC(Key mkac, byte[] atc) throws CryptoUtilException {

		byte[] r = new byte[8];
		System.arraycopy(atc, atc.length-2, r, 0, 2);

		return deriveCommonSK_SM(mkac, r);
	}

	private static Key deriveSK_MK(Key mkac, byte[] atc, byte[] upn) throws CryptoUtilException {

		byte[] r = new byte[8];
		System.arraycopy(atc, atc.length-2, r, 0, 2);
		System.arraycopy(upn, upn.length-4, r, 4, 4);

		return deriveCommonSK_SM(mkac, r);
	}

	/**
	 * Common Session Key Derivation Method for secure messaging.
	 * <p>
	 * The diversification value is the <em>RAND</em>, which is ARQC
	 * incremeted by 1 (with overflow) after each script command
	 * for that same ATC value.
	 * Described in EMV v4.2 Book 2, Annex A1.3.1 Common Session Key
	 * Derivation Option for secure messaging.
	 *
	 * @param mksm unique ICC Master Key for Secure Messaging
	 * @param rand Application Cryptogram as diversification value
	 * @return derived 16-bytes Session Key with adjusted DES parity
	 * @throws JCEHandlerException
	 */
	private static Key deriveCommonSK_SM(Key mksm, byte[] rand) throws CryptoUtilException {
		byte[] rl = Arrays.copyOf(rand,8);

		rl[2] = (byte)0xf0;
		byte[] skl = encryptECB(rl, mksm);

		byte[] rr = Arrays.copyOf(rand,8);
		rr[2] = (byte)0x0f;
		byte[] skr = encryptECB(rr, mksm);


		adjustDESParity(skl);
		adjustDESParity(skr);

		byte[] result = concat(skl, skr);

		return new SecretKeySpec(result, ALG_TRIPLE_DES);
	}


	public static final byte[] encryptECB(byte[] data,Key key)throws CryptoUtilException{
		return doCryptStuff(data, key,Cipher.ENCRYPT_MODE , CipherMode.ECB, null);
	}

	public static final byte[] decryptECB(byte[] data,Key key)throws CryptoUtilException{
		return doCryptStuff(data, key,Cipher.DECRYPT_MODE , CipherMode.ECB, null);
	}


	private static byte[] normalizeKeyBytes(byte[] encoded) {		
		if(encoded.length == 16) {
			byte[] newEncoded = new byte[24];
			System.arraycopy(encoded, 0, newEncoded, 0, 16);
			System.arraycopy(encoded, 0, newEncoded, 16, 8);

			return newEncoded;
		}

		return encoded;
	}

	private static byte[] doCryptStuff(byte[] data, Key key, int direction
			,CipherMode cipherMode, byte[] iv) throws CryptoUtilException{
		byte[] result;
		StringBuilder sbTransformation = new StringBuilder();
		sbTransformation.append(key.getAlgorithm());

		if(ALG_TRIPLE_DES.startsWith(key.getAlgorithm())) {
			sbTransformation.append("/");
			sbTransformation.append(cipherMode.toString());
			sbTransformation.append("/");
			sbTransformation.append(DES_NO_PADDING);
		}

		AlgorithmParameterSpec aps = null;
		try {
			//Just trust the java library for JCE provider
			Cipher c1 = Cipher.getInstance(sbTransformation.toString());
			if (cipherMode != CipherMode.ECB)
				aps = new IvParameterSpec(iv);
			c1.init(direction, key, aps);
			result = c1.doFinal(data);
			if (cipherMode != CipherMode.ECB)
				System.arraycopy(result, result.length-8, iv, 0, iv.length);
		}catch(NoSuchPaddingException | NoSuchAlgorithmException | 
				BadPaddingException | InvalidKeyException |
				InvalidAlgorithmParameterException| IllegalBlockSizeException e) {
			e.printStackTrace();
			throw new CryptoUtilException(e);
		}
		return result;
	}

	public static byte[] xor (byte[] op1, byte[] op2) {
		byte[] result;
		// Use the smallest array
		if (op2.length > op1.length) {
			result = new byte[op1.length];
		}
		else {
			result = new byte[op2.length];
		}
		for (int i = 0; i < result.length; i++) {
			result[i] = (byte)(op1[i] ^ op2[i]);
		}
		return  result;
	}


	/**
	 * Calculate MAC according to ISO/IEC 9797-1 Alg 3
	 * @param key DES double length key
	 * @param d data to calculate MAC on it
	 * @return 8 byte of mac value
	 * @throws JCEHandlerException
	 */
	private static byte[] calculateMACISO9797Alg3(Key key, byte[] d) throws CryptoUtilException {
		Key skl = new SecretKeySpec(Arrays.copyOfRange(key.getEncoded(), 0, 8), ALG_DES) ;
		Key skr = new SecretKeySpec(Arrays.copyOfRange(key.getEncoded(), 8, 16), ALG_DES) ; 

		if (d.length%8 != 0) {
			//Padding with 0x00 bytes
			byte[] t = new byte[d.length - d.length%8 + 8];
			System.arraycopy(d, 0, t, 0, d.length);
			d = t;
		}
		//MAC_CBC alg 3
		byte[] y_i = ISOUtil.hex2byte("0000000000000000");
		byte[] yi  = new byte[8];

		for ( int i=0;i<d.length;i+=8){
			System.arraycopy(d, i, yi, 0, yi.length);
			y_i = encryptECB(ISOUtil.xor(yi, y_i), skl);
		}
		y_i = decryptECB(y_i, skr);
		y_i = encryptECB(y_i, skl);
		return y_i;
	}

	/**
	 * DES Keys use the LSB as the odd parity bit.  This method can
	 * be used enforce correct parity.
	 *
	 * @param bytes the byte array to set the odd parity on.
	 */
	public static final void adjustDESParity (byte[] bytes) {
		for (int i = 0; i < bytes.length; i++) {
			int b = bytes[i];
			bytes[i] = (byte)(b & 0xfe | (b >> 1 ^ b >> 2 ^ b >> 3 ^ b >> 4 ^ b >> 5 ^ b >> 6 ^ b >> 7 ^ 0x01) & 0x01);
		}
	}

	/**
	 * concat 2 array of bytes
	 * @param array1
	 * @param array2
	 * @return
	 */
	public static final byte[] concat (byte[] array1, byte[] array2) {
		byte[] concatArray = new byte[array1.length + array2.length];
		System.arraycopy(array1, 0, concatArray, 0, array1.length);
		System.arraycopy(array2, 0, concatArray, array1.length, array2.length);
		return  concatArray;
	}

}
