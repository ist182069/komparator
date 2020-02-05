package org.komparator.security;

import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.crypto.*;
import java.util.*;

import org.junit.*;

import static javax.xml.bind.DatatypeConverter.printHexBinary;
import static org.junit.Assert.*;
//nao gosto muito disto
public class CryptoUtilTest extends BaseTest {
	
	/** CryptoUtil class. */
	private CryptoUtil cryptoUtil = new CryptoUtil();
	/** Plain text to digest. */
	private final String plainText = "This is the plain text!";
	/** Plain text bytes. */
	private final byte[] plainBytes = plainText.getBytes();

	/** certificates from the src/test/resources directory. */
	final static String CA_CERTIFICATE = "ca.cer";
	final static String CERTIFICATE = "example.cer";
	
	/** keyStore and its attributes from the src/test/resources directory. */
	final static String KEYSTORE = "example.jks";
	final static String KEYSTORE_PASSWORD = "1nsecure";

	final static String KEY_ALIAS = "example";
	final static String KEY_PASSWORD = "ins3cur3";

	
    // one-time initialization and clean-up
    @BeforeClass
    public static void oneTimeSetUp() {
        // runs once before all tests in the suite
    }

    @AfterClass
    public static void oneTimeTearDown() {
        // runs once after all tests in the suite
    }

    // members

    // initialization and clean-up for each test
    @Before
    public void setUp() {
        // runs before each test
    }

    @After
    public void tearDown() {
        // runs after each test
    }

    // tests. Ir buscar ao cert-util? Ou fazer a mao?
    @Test
    public void sucessChiperDecipher() throws Exception {
    	// get the certificate from resources
    	Certificate cert = CertUtil.getX509CertificateFromResource(CERTIFICATE);

		// get public key from the example certificate
		PublicKey publicKey = cert.getPublicKey();
		
		System.out.print("Text: ");
		System.out.println(plainText);
		System.out.print("Bytes: ");
		System.out.println(printHexBinary(plainBytes));
		
		// cipher the plainBytes attribute
		System.out.println("Ciphering  with public key...");
		byte[] cipheredBytes = cryptoUtil.asymCipher(plainBytes, publicKey);
		
		System.out.println("Ciphered bytes:");
		System.out.println(printHexBinary(cipheredBytes));
		
		
		// get private key from the example KeyStore
		PrivateKey privateKey = CertUtil.getPrivateKeyFromKeyStoreResource(KEYSTORE, KEYSTORE_PASSWORD.toCharArray(), KEY_ALIAS, KEY_PASSWORD.toCharArray());
		
		System.out.println("Deciphering  with private key...");
		byte[] decipheredBytes = cryptoUtil.asymDecipher(cipheredBytes, privateKey);
		System.out.println("Deciphered bytes:");
		System.out.println(printHexBinary(decipheredBytes));

		System.out.print("Text: ");
		String newPlainText = new String(decipheredBytes);
		System.out.println(newPlainText);

		assertEquals(plainText, newPlainText);
    }
}
