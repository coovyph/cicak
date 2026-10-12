import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;

public class MastercardHsmArpcCalculator {

    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                 + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
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

    /**
     * Phase 1: Derive Card's Unique Derivation Key (UDK) using EMV Option A
     */
    public static byte[] deriveUdkOptionA(byte[] imkAc, String pan, String panSeqNo) throws GeneralSecurityException {
        String fullPanPsn = pan + panSeqNo;
        if (fullPanPsn.length() < 16) {
            fullPanPsn = String.format("%016d", Long.parseLong(fullPanPsn));
        } else if (fullPanPsn.length() > 16) {
            fullPanPsn = fullPanPsn.substring(fullPanPsn.length() - 16);
        }
        
        byte[] dataLeft = hexToBytes(fullPanPsn);
        byte[] dataRight = new byte[8];
        for (int i = 0; i < 8; i++) {
            dataRight[i] = (byte) (~dataLeft[i] & 0xFF);
        }

        SecretKeySpec secretKey = new SecretKeySpec(normalizeKeyBytes(imkAc), "DESede");
        Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] udkLeft = cipher.doFinal(dataLeft);
        byte[] udkRight = cipher.doFinal(dataRight);

        byte[] udk = new byte[16];
        System.arraycopy(udkLeft, 0, udk, 0, 8);
        System.arraycopy(udkRight, 0, udk, 8, 8);
        return udk;
    }

    /**
     * Phase 2: Mastercard Proprietary SKD Session Key Derivation
     * Derived using ATC (Tag 9F36) and Unpredictable Number (Tag 9F37)
     */
    public static byte[] deriveMastercardSkdSessionKey(byte[] udk, String atcHex, String unpredictableNumberHex) throws GeneralSecurityException {
        // Build diversification blocks for Left and Right halves of the Session Key
        // Commonly: SK_L handles ATC + "0000" + UN or variant pads depending on specific M/Chip version rules
        byte[] rBlockLeft = hexToBytes(atcHex + "0000" + unpredictableNumberHex.substring(0, 8));
        byte[] rBlockRight = hexToBytes(atcHex + "0000" + unpredictableNumberHex.substring(0, 8));
        
        // Apply variant inversion or F0/0F masks matching standard M/Chip diversification
        rBlockLeft[2] ^= (byte) 0xF0;
        rBlockRight[2] ^= (byte) 0x0F;

        SecretKeySpec secretKey = new SecretKeySpec(normalizeKeyBytes(udk), "DESede");
        Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] skLeft = cipher.doFinal(rBlockLeft);
        byte[] skRight = cipher.doFinal(rBlockRight);

        byte[] sessionKey = new byte[16];
        System.arraycopy(skLeft, 0, sessionKey, 0, 8);
        System.arraycopy(skRight, 0, sessionKey, 8, 8);
        return sessionKey;
    }

    /**
     * Phase 3: Calculate ARPC (XOR with binary Response Code block and Encrypt)
     */
    public static byte[] calculateArpc(byte[] sessionKey, byte[] arqc, String arcHex) throws GeneralSecurityException {
        // Hex formatting for Response Code block (e.g. "0000000000000000")
        byte[] arcBlock = hexToBytes(arcHex.replace(" ", "") + "0000000000000000".substring(arcHex.length()));

        byte[] xorInput = new byte[8];
        for (int i = 0; i < 8; i++) {
            xorInput[i] = (byte) (arqc[i] ^ arcBlock[i]);
        }

        SecretKeySpec secretKey = new SecretKeySpec(normalizeKeyBytes(sessionKey), "DESede");
        Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        return cipher.doFinal(xorInput);
    }

    public static void main(String[] args) {
        try {
            String pan = "5393710501122434";
            String panSeqNo = "01";
            byte[] imkAc = hexToBytes("404043434545464649494A4A4C4C4F4F");
            byte[] arqc = hexToBytes("07F403E5D65A23C1");
            
            String atc = "0614";
            String unpredictableNumber = "32922789";
            String responseCodeHex = "0000"; // Binary/Hex format response rather than ASCII

            // 1. UDK Derivation
            byte[] udk = deriveUdkOptionA(imkAc, pan, panSeqNo);
            
            // 2. Dynamic Session Key Derivation (Mastercard SKD)
            byte[] sessionKey = deriveMastercardSkdSessionKey(udk, atc, unpredictableNumber);
            
            // 3. ARPC Cryptogram Generation
            byte[] arpc = calculateArpc(sessionKey, arqc, responseCodeHex);
            
            System.out.println("HSM Targeted ARPC: " + bytesToHex(arpc));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
