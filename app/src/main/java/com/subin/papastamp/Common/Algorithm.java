package com.subin.papastamp.Common;

/**
 Aes encryption
 */
import javax.crypto.Cipher;

import javax.crypto.spec.IvParameterSpec;

import javax.crypto.spec.SecretKeySpec;



import android.util.Base64;


public class Algorithm {
	public String Decrypt(String text, String key) {
		byte[] results = null;
		try {

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

			byte[] keyBytes= new byte[16];

			byte[] b= key.getBytes("UTF-8");

			int len= b.length;

			if (len > keyBytes.length) len = keyBytes.length;

			System.arraycopy(b, 0, keyBytes, 0, len);

			SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

			IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);

			cipher.init(Cipher.DECRYPT_MODE,keySpec,ivSpec);

			results = cipher.doFinal(Base64.decode(text, 0));

			return new String(results,"UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}



	public String Encrypt(String text, String key) {
		byte[] results = null;
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			byte[] keyBytes = new byte[16];
			byte[] b = key.getBytes("UTF-8");

			int len = b.length;
			if (len > keyBytes.length) len = keyBytes.length;

			System.arraycopy(b, 0, keyBytes, 0, len);

			SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

			IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);

			cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
			results = cipher.doFinal(text.getBytes("UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return Base64.encodeToString(results, Base64.NO_WRAP);
	}
}