/**
 * 
 * Description: 配置公钥，私钥
 * Copyright: Copyright (c) 2008
 * Company:联动优势
 * @author 任水
 * @version 1.0
 * @date May 7, 2009
 */
package com.umpay.hfweb.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import com.umpay.hfweb.service.MessageService;

public class Config {
	
	private static Map<String, PrivateKey> merPrivateKeyMap = new HashMap<String, PrivateKey>();
	
	private static Map<String, X509Certificate> umpayPublicKeyMap = new HashMap<String, X509Certificate>();
	
	private static Map<String, Long> lastModMap = new HashMap<String, Long>(); // 证书的更新
	
	
	/**
	 * @param merid
	 * @return
	 */
	public static PrivateKey getMerPrivateKey(String merid, MessageService messageService) {
		
		// 商户私有
		String merPrivateKeyPath = messageService.getSystemParam(merid+".merPrivateKeyPath");
		// 未配置
		if("".equals(ObjectUtil.trim(merPrivateKeyPath))){
			return null;
		}
		File f = new File(merPrivateKeyPath);
		if (!f.exists()){
			return null;
		}
		
		PrivateKey merPrivateKey = merPrivateKeyMap.get(merid);
		if(merPrivateKey != null && !ObjectUtil.needReloadFile(merPrivateKeyPath, lastModMap)){
			return merPrivateKey;
		}
			
		byte[] kb = null;
		FileInputStream fis = null;
		try {
			kb = new byte[(int) (f.length())];
			fis = new FileInputStream(f);
			fis.read(kb);
		} catch (Exception e) {
			return null;
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e) {
				} finally {
					fis = null;
				}
			}
		}
		// get the private key
		PKCS8EncodedKeySpec peks = null;
		KeyFactory kf = null;
		try {
			peks = new PKCS8EncodedKeySpec(kb);
			kf = KeyFactory.getInstance("RSA");
			merPrivateKey = kf.generatePrivate(peks);
		} catch (Exception e) {
			return null;
		}
		
		merPrivateKeyMap.put(merid, merPrivateKey);
		return merPrivateKey;
	}

	public static X509Certificate getUmpayPublicKey(String merid, MessageService messageService) {
		String umpayPublicKeyPath = messageService.getSystemParam("mer.public.umpayPublicKeyPath");
		// 未配置
		if("".equals(ObjectUtil.trim(umpayPublicKeyPath))){
			return null;
		}
		File f = new File(umpayPublicKeyPath);
		if (!f.exists()){
			return null;
		}
		
		X509Certificate umpayPublicKey = umpayPublicKeyMap.get("umpayPublicKey");
		if(umpayPublicKey != null && !ObjectUtil.needReloadFile(umpayPublicKeyPath, lastModMap)){
			return umpayPublicKey;
		}
		FileInputStream fis = null;
		byte[] cb = null;
		try {
			cb = new byte[(int) (f.length())];
			fis = new FileInputStream(f);
			fis.read(cb);
		} catch (Exception e) {
			return null;
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e) {
				} finally {
					fis = null;
				}
			}
		}
		// load the cert
		ByteArrayInputStream bais = new ByteArrayInputStream(cb);
		try {
		CertificateFactory cf = null;
		cf = CertificateFactory.getInstance("X.509");
		umpayPublicKey = (X509Certificate) cf.generateCertificate(bais);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}finally{
			if(bais != null){
				try{
					bais.close();
				}catch(Exception e){
					
				}finally{
					bais = null;
				}
			}
		}
		umpayPublicKeyMap.put("umpayPublicKey", umpayPublicKey);
		return umpayPublicKey;
	}
}
