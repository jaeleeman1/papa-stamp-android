package com.subin.papastamp.common;

/**
 Aes encryption
 */
import javax.crypto.Cipher;

import javax.crypto.spec.IvParameterSpec;

import javax.crypto.spec.SecretKeySpec;



import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;

public class Algorithm {
	public static final String DEFAULT_CODING = "utf-8";
	private static final String TAG = "[UserManager] ";

	public static String encrypt(String content, String key) throws Exception {
		byte[] input = content.getBytes(DEFAULT_CODING);

		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] thedigest = md.digest(key.getBytes(DEFAULT_CODING));

		Log.d(TAG, "content: " + content);
		Log.d(TAG, "key: " + key);

		SecretKeySpec skc = new SecretKeySpec(thedigest, "AES");

		//String byteToStr = new String(thedigest, 0, thedigest.length);

		StringBuilder sb = new StringBuilder();
		for(final byte b: thedigest)
			sb.append(String.format("%02x ", b&0xff));

		Log.d(TAG, "thedigest: " + sb.toString());

		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, skc);

		byte[] cipherText = new byte[cipher.getOutputSize(input.length)];
		int ctLength = cipher.update(input, 0, input.length, cipherText, 0);
		ctLength += cipher.doFinal(cipherText, ctLength);

		return parseByte2HexStr(cipherText);
	}

	private static String decrypt(String encrypted, String seed) throws Exception {
		byte[] keyb = seed.getBytes(DEFAULT_CODING);
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] thedigest = md.digest(keyb);
		SecretKeySpec skey = new SecretKeySpec(thedigest, "AES");
		Cipher dcipher = Cipher.getInstance("AES");
		dcipher.init(Cipher.DECRYPT_MODE, skey);

		byte[] clearbyte = dcipher.doFinal(toByte(encrypted));
		return new String(clearbyte);
	}

	private static byte[] toByte(String hexString) {
		int len = hexString.length() / 2;
		byte[] result = new byte[len];
		for (int i = 0; i < len; i++) {
			result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
		}
		return result;
	}

	private static String parseByte2HexStr(byte buf[]) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < buf.length; i++) {
			String hex = Integer.toHexString(buf[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			sb.append(hex);
		}
		return sb.toString();
	}
}