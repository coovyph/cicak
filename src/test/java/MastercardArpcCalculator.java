import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.Key;

public class MastercardArpcCalculator {

    // Helper to turn Hex Strings into byte arrays
    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                 + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }

    // Helper to turn byte arrays into Hex Strings
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.append("").toString();
    }

    /**
     * Derives Card's Unique Derivation Key (UDK) using EMV Option A
     */
    public static byte[] deriveUdkOptionA(byte[] imkAc, String pan, String panSeqNo) throws GeneralSecurityException {
        // Step 1: Prepare the input data (PAN + PSN) padded and aligned to 16 digits
        String fullPanPsn = pan + panSeqNo;
        if (fullPanPsn.length() < 16) {
            fullPanPsn = String.format("%016d", Long.parseLong(fullPanPsn));
        } else if (fullPanPsn.length() > 16) {
            fullPanPsn = fullPanPsn.substring(fullPanPsn.length() - 16);
        }
        
        byte[] dataLeft = hexToBytes(fullPanPsn);
        
        // Invert the data bytes for the right half derivation step
        byte[] dataRight = new byte[8];
        for (int i = 0; i < 8; i++) {
            dataRight[i] = (byte) (~dataLeft[i] & 0xFF);
        }

        // Use Triple DES to encrypt the data blocks with the IMK AC
        SecretKeySpec secretKey = new SecretKeySpec(normalizeKeyBytes(imkAc), "DESede");
        Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] udkLeft = cipher.doFinal(dataLeft);
        byte[] udkRight = cipher.doFinal(dataRight);

        // Combine into a 16-byte UDK
        byte[] udk = new byte[16];
        System.arraycopy(udkLeft, 0, udk, 0, 8);
        System.arraycopy(udkRight, 0, udk, 8, 8);
        return udk;
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

	private static Key normalizeDesKey(Key key) {
		byte[] newKeyBytes = normalizeKeyBytes(key.getEncoded());
		return new SecretKeySpec(newKeyBytes, key.getAlgorithm());
	}

    /**
     * Calculates ARPC Method 1: 3DES(SessionKey)[ARQC ^ (ARC || 00...00)]
     */
    public static byte[] calculateArpcMethod1(byte[] sessionKey, byte[] arqc, String arc) throws GeneralSecurityException {
        // Create an 8-byte array block initialized with the 2-character response code (e.g. "00")
        byte[] arcBlock = new byte[8];
        byte[] arcBytes = arc.getBytes(); // "00" becomes [0x30, 0x30]
        System.arraycopy(arcBytes, 0, arcBlock, 0, Math.min(arcBytes.length, 8));

        // XOR the incoming ARQC with the padded Response Code block
        byte[] xorInput = new byte[8];
        for (int i = 0; i < 8; i++) {
            xorInput[i] = (byte) (arqc[i] ^ arcBlock[i]);
        }

        // Encrypt the XOR output using Triple DES with the derived Session Key
        SecretKeySpec secretKey = new SecretKeySpec(normalizeKeyBytes(sessionKey), "DESede");
        Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        return cipher.doFinal(xorInput);
    }

    public static void main(String[] args) {
        try {
            // Inputs provided
            String pan = "5393710501122434";
            String panSeqNo = "01";
            byte[] imkAc = hexToBytes("404043434545464649494A4A4C4C4F4F");
            byte[] arqc = hexToBytes("07F403E5D65A23C1");
            
            // Standard Authorization Response Code: "00" (Approved)
            String responseCode = "00";

            System.out.println("--- Starting ARPC Computation ---");
            
            // Phase 1: Derive the UDK (Card Unique Derivation Key)
            byte[] udk = deriveUdkOptionA(imkAc, pan, panSeqNo);
            System.out.println("Derived UDK:  " + bytesToHex(udk));

            // Phase 2: Session Key derivation (For CVN10, Session Key == UDK)
            byte[] sessionKey = udk;

            // Phase 3: Compute the ARPC
            byte[] arpc = calculateArpcMethod1(sessionKey, arqc, responseCode);
            System.out.println("Generated ARPC: " + bytesToHex(arpc));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
