package com.umpay.hfweb.util;

import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.DESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.modes.PaddedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;


public class Encryptor {

	private BufferedBlockCipher cipher;
	private KeyParameter key;
	
	public Encryptor(byte[] key) {
		cipher = new PaddedBlockCipher(new CBCBlockCipher(new DESEngine()));
		this.key = new KeyParameter(key);
	}
	
	public Encryptor(String key) {
		this(key.getBytes());
	}
	
	private byte [] callCipher(byte[] data) throws DataLengthException, IllegalStateException, InvalidCipherTextException {
		int size = cipher.getOutputSize(data.length);
		byte [] result = new byte[size];
		int olen = cipher.processBytes(data, 0, data.length, result, 0);
		olen += cipher.doFinal(result, olen);
		if (olen < size) {
			byte[] tmp = new byte[olen];
			System.arraycopy(result, 0, tmp, 0, olen);
			result = tmp;
		}
		return result;
	}
	
	public synchronized byte [] encrypt(byte[] data) throws 
		DataLengthException, IllegalStateException, InvalidCipherTextException {
		if (data == null || data.length == 0) {
			return new byte[0];
		}
		cipher.init(true, key);
		return callCipher(data);
	}
	
	public synchronized byte [] decrypt(byte[] data) throws
		DataLengthException, IllegalStateException, InvalidCipherTextException {
		if (data == null || data.length == 0) {
			return new byte[0];
		}
		cipher.init(false, key);
		return callCipher(data);
	}
	
	public byte[] encyptString(String data) throws Exception{
		if (data == null || data.length() == 0) {
			return new byte[0];
		}
		return encrypt(data.getBytes("UTF-8"));
	}
	
	public String decryptString(byte[] data){
		String str = "";
		if (data == null || data.length == 0) {
			return "";
		}
		try {
			str = new String(decrypt(data),"UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return str;
	}
	
//	public static void main(String[] args) {
//		try {
//			String msg = "通信账户";
//			String key = "umpay123";
//			Encryptor encryptor = new Encryptor(key);
//			byte[] encBytes = encryptor.encyptString(msg);
//			String enString = new String(encBytes);
//			System.out.println("enString: " + enString);
//			System.out.println(enString);
//
//			String deString = encryptor.decryptString(enString.getBytes());
//			//String deString = new String(enString.getBytes());
//			System.out.println("deString: " + deString);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
}
