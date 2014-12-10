package com.umpay.hfweb.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.StringReader;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Hashtable;
import java.util.Map;

import sun.misc.BASE64Encoder;

import com.umpay.hfweb.exception.SignEncException;
import com.umpay.hfweb.service.MessageService;

public class SignEnc {
	private static Map<String, String> merSignMap = new Hashtable<String, String>();
	
	public static String sign(String merId,String plain, MessageService messageService) throws SignEncException {
		// get private key
		PrivateKey pk = Config.getMerPrivateKey(merId, messageService);
		if(pk == null)
			throw new SignEncException("私钥加载失败");
		// get the signature
		Signature sig = null;
		byte sb[] = null;
		try {
			sig = Signature.getInstance("SHA1withRSA");
			sig.initSign(pk);
			sig.update(plain.getBytes("gb2312"));
			sb = sig.sign();
		} catch (Exception e) {
			throw new SignEncException("签名出错");
		}
		// generate base64 signature
		String b64Str = null;
		try {
			sun.misc.BASE64Encoder base64 = new sun.misc.BASE64Encoder();
			b64Str = base64.encode(sb);
			// return b64Str;
		} catch (Exception e) {
			throw new SignEncException("base64编码出错");
		}
		// delete the char of return and new line
		try {
			BufferedReader br = new BufferedReader(new StringReader(b64Str));
			String tmpStr = "";
			String tmpStr1 = "";
			while ((tmpStr = br.readLine()) != null) {
				tmpStr1 += tmpStr;
			}
			b64Str = tmpStr1;
			
			merSignMap.put(merId, b64Str);
			return b64Str;
		} catch (Exception e) {
			throw new SignEncException("编码出错");
		}
	}

	public static boolean verify(String merId , String plain, String sign, MessageService messageService)
			throws SignEncException {
		X509Certificate cert = Config.getUmpayPublicKey(merId, messageService);
		if(cert == null)
			throw new SignEncException("公钥加载失败");
		try {
			sun.misc.BASE64Decoder base64 = new sun.misc.BASE64Decoder();
			byte[] signed = base64.decodeBuffer(sign);
			Signature sig = Signature.getInstance("SHA1withRSA");
			sig.initVerify(cert);
			sig.update(plain.getBytes());
			return sig.verify(signed);
		} catch (Exception e) {
			throw new SignEncException("验签过程出错");
		}
	}

	public static void main(String[] args) throws Exception {
		CertificateFactory certificatefactory=CertificateFactory.getInstance("X.509");
		 FileInputStream bais=new FileInputStream("D:/cert/kfk.cer");
		 X509Certificate Cert = (X509Certificate)certificatefactory.generateCertificate(bais);
		        PublicKey pk = Cert.getPublicKey();
		        BASE64Encoder bse=new BASE64Encoder();
		        System.out.println(pk.getAlgorithm());
		        System.out.println(new String(pk.getEncoded()));
		        System.out.println("pk:"+bse.encode(pk.getEncoded()));
	}
}
