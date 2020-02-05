package org.komparator.security;

import java.io.*;
import java.security.*;
import javax.crypto.*;

import java.util.*;

public class CryptoUtil {
	
	/**
	 * Asymmetric cipher: combination of algorithm, block processing, and
	 * padding.
	 */
	private static final String ASYM_CIPHER = "RSA/ECB/PKCS1Padding";
	
	

	public byte[] asymCipher(byte[] plainBytes, PublicKey publicKey) throws NoSuchAlgorithmException, 
	NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		// get an RSA cipher object
		Cipher cipher = Cipher.getInstance(ASYM_CIPHER);

		// encrypt the plain text using the public key
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] cipherBytes = cipher.doFinal(plainBytes);
		
		return cipherBytes;
	}
	
	public byte[] asymDecipher(byte[] cipherBytes, PrivateKey privateKey) throws NoSuchAlgorithmException, 
	NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		// get an RSA cipher object
		Cipher cipher = Cipher.getInstance(ASYM_CIPHER);
		
		// decipher the ciphered digest using the private key
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		byte[] decipheredBytes = cipher.doFinal(cipherBytes);
		
		return decipheredBytes;
	}
    // TODO add security helper methods

}
