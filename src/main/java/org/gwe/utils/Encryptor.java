/*
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gwe.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * @author Marco Ruiz
 * @since Mar 1, 2008
 */
public class Encryptor {

	private static Log log = LogFactory.getLog(Encryptor.class);

	public static byte[] createMD5Hash(String key) {
		return createMD5Hash(key.getBytes());
	}

	public static byte[] createMD5Hash(byte[] key) {
//		MessageDigest longHash = MessageDigest.getInstance("SHA");
		MessageDigest longHash;
        try {
	        longHash = MessageDigest.getInstance("MD5");
			longHash.update(key);
			return longHash.digest();
        } catch (NoSuchAlgorithmException e) {
        	log.fatal("Security not available because basic infrastructure to provide it is not available.", e);
        }
        return null;
	}

	private byte[] md5Hash;
	
	public Encryptor(String password) {
		this(createMD5Hash(password));
	}
	
	public Encryptor(byte[] md5Bytes) {
		// throw exception if MD5 bytes length is different than 16 
//		if (md5Bytes.length != 16) throw new 
		md5Hash = md5Bytes;
	}
	
	public byte[] encrypt(byte[] target) throws Exception {
		Cipher cipher = createCipher(Cipher.ENCRYPT_MODE);

		byte[] encryptedText = cipher.doFinal(target);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		new BASE64Encoder().encode(encryptedText, bos);
		byte[] encodedEncryptedText = bos.toByteArray();
//		byte[] encodedEncryptedText = encryptedText;

		byte[] result = encodedEncryptedText;
		return result;
	}

	public byte[] decrypt(byte[] target) throws Exception {
		Cipher cipher = createCipher(Cipher.DECRYPT_MODE);

		byte[] decodedText = new BASE64Decoder().decodeBuffer(new ByteArrayInputStream(target));
//		byte[] decodedText = target;
		byte[] result = cipher.doFinal(decodedText);

		return result;
	}
/*
	public String encrypt(byte[] target) throws Exception {
		Cipher cipher = createCipher(Cipher.ENCRYPT_MODE);

		byte[] encryptedText = cipher.doFinal(target);
		String encodedEncryptedText = new BASE64Encoder().encode(encryptedText); // Base64.encodeBase64(encryptedText);

		return encodedEncryptedText;
	}

	public String decrypt(byte[] target) throws Exception {
		Cipher cipher = createCipher(Cipher.DECRYPT_MODE);

		byte[] decodedText = new BASE64Decoder().decodeBuffer(new ByteArrayInputStream(target)); // Base64.decodeBase64(target);
//		byte[] decodedText = target;
		byte[] result = cipher.doFinal(decodedText);

		return new String(result);
	}
*/
	private Cipher createCipher(int mode) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		SecretKey sk = new SecretKeySpec(md5Hash, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(mode, sk);
		return cipher;
	}

	public static void main(String[] args) throws Exception {
		test("user/password", "");
		test("user/password", ".This is my message");
		test("user/password", "This is my message-");
		test("user/password", "This is my messagee");
		test("user/password", "This is my message ");
		test("userpassword", "This is other message");
		test("user=password", "This is my message");
		test("user/password", "This is my message ");
		test("user/password", "This is other message, and it is far longer than the other ones. The $ ~~ 513443:;[]{} Its encrypted version should be longer as well, if not then it may just be a hashing function and not an actual encryptor");
		
		String pwd = "my-password";
		System.out.println("pwd: " + pwd);
		
		byte[] pwdHash = Encryptor.createMD5Hash(pwd);
		System.out.println("pwdHash: " + bytesToHex(pwdHash));
		
		String daemonPwd = pwd + "daemon-location";
		Encryptor enc = new Encryptor(daemonPwd);
		System.out.println("daemonPwd: " + daemonPwd);
		
		System.out.println("daemonPwdHash: " + bytesToHex(enc.md5Hash));
/*		
		String encPwdHash = enc.encrypt(pwdHash);
		System.out.println("encPwdHash: " + bytesToHex(encPwdHash));
		
		String decPwdHash = enc.decrypt(encPwdHash);
		System.out.println("decPwdHash: " + bytesToHex(decPwdHash));
*/	
	}
	
	public static void test(String key, String message) throws Exception {
		System.out.println("Key: " + key);
		System.out.println("Message: " + message);
		Encryptor encrypter = new Encryptor(key);
		byte[] enc = encrypter.encrypt(message.getBytes());
		System.out.println("Encrypted: " + bytesToHex(enc));
		System.out.println("Decrypted: " + encrypter.decrypt(enc) + "\n");

		System.out.println("Key Hash: " + bytesToHex(encrypter.md5Hash));
/*
		String keyEnc = encrypter.encrypt(encrypter.md5Hash);
		System.out.println("Password encrypted with itself: " + bytesToHex(keyEnc));
		System.out.println("Password decrypted with itself: " + bytesToHex(encrypter.decrypt(keyEnc)) + "\n");
*/
	}

	public static String bytesToHex(String str) {
		return bytesToHex(str.getBytes());
	}

	public static String bytesToHex(byte[] b) {
		char hexDigit[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		StringBuffer buf = new StringBuffer();
		for (int j = 0; j < b.length; j++) {
			buf.append(hexDigit[(b[j] >> 4) & 0x0f]);
			buf.append(hexDigit[b[j] & 0x0f]);
		}
		return buf.toString();
	}
}
