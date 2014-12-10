package com.umpay.hfweb.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;
import com.umpay.hfweb.model.SIAP;


/**
 * Http共通处理类
 * @author wuwenjie 下午01:22:22
 */
@SuppressWarnings("unchecked")
public class HttpUtil {
	private static Logger log = Logger.getLogger(HttpUtil.class);
	private static XStream xStream = null;
	
	public HttpUtil() {
		if(xStream == null){
			xStream = new XStream();
			xStream.alias("SIAP", SIAP.class);
		}
	}
	/**
	 * Parse the http request and retrieve all form fields . the map key is the
	 * field name and the value is field value or field values list if more than
	 * one value exist.
	 * 
	 * @param request
	 * @return
	 * @throws WBankHttpException
	 */
	public static Map<String,String> parseRequestParam(HttpServletRequest request){
		Map fieldMap = new HashMap();
		// common post
		Enumeration names = request.getParameterNames();
		while (names.hasMoreElements()) {
			String name = (String) names.nextElement();
			String values = request.getParameter(name);
			if(null!=values)values = values.trim();
			fieldMap.put(name, values);
		}
		return fieldMap;
	}
	
	public static Map<String,Object> parseRequestAttr(HttpServletRequest request){
		Map fieldMap = new HashMap();
		// common post
		Enumeration names = request.getAttributeNames();
		while (names.hasMoreElements()) {
			String name = (String) names.nextElement();
			Object values = request.getAttribute(name);
			fieldMap.put(name, values);
		}
		return fieldMap;
	}

	public static String mapToRequestParameter(Map map) {
		StringBuffer sb = new StringBuffer("");
		Set set = map.keySet();
		for (Iterator iter = set.iterator(); iter.hasNext();) {
			Object name = iter.next();
			if (name != null) {
				String key = (String) name;
				if (key.trim() != "") {
					Object value = map.get(key);
					if (value == null) {
						value = "";
					}
					sb.append(key);
					sb.append("=");
					sb.append(value);
					sb.append("&");
				}
			}
		}
		if(sb.length() > 0){
			int index = sb.lastIndexOf("&");
			if(sb.length()-1 == index){
				return sb.substring(0, sb.length()-1).toString();
			}
		}
		return sb.toString();
	}

	/**
	 * 获取用户请求IP地址
	 * 
	 * @param request
	 * @return
	 */
	public static String getCustIp(HttpServletRequest request){
		String ipAddress = null;    
	     //ipAddress = this.getRequest().getRemoteAddr();    
	     ipAddress = request.getHeader("x-forwarded-for");    
	     if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {    
	      ipAddress = request.getHeader("Proxy-Client-IP");    
	     }    
	     if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {    
	         ipAddress = request.getHeader("WL-Proxy-Client-IP");    
	     }    
	     if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {    
	      	ipAddress = request.getRemoteAddr();    
	     if(ipAddress.equals("127.0.0.1")){    
	       //根据网卡取本机配置的IP    
	        InetAddress inet=null;    
	    	try {    
	     		inet = InetAddress.getLocalHost();    
	    	} catch (Exception e) {    
	     		e.printStackTrace();    
	    	}    
	    	ipAddress= inet.getHostAddress();    
	      }    
	             
	     }    
	   
	     //对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割    
	     if(ipAddress!=null && ipAddress.length()>15){ //"***.***.***.***".length() = 15    
	         if(ipAddress.indexOf(",")>0){    
	             ipAddress = ipAddress.substring(0,ipAddress.indexOf(","));    
	         }    
	     }    
		log.info("获取用户请求IP成功:" + ipAddress);
		return ipAddress;
	}
	
	/**
	 * <b>方法描述：</b>用于向基于Http协议的3.0平台发送同步Post请求。
	 * <P>
	 * <b>参数说明：</b>参数名 String urlstr 请求地址。 Map<String,Object> reqMap 请求报文，将接口定义参数放入map就行。
	 * <P>
	 * <b>返回说明：</b>返回的是一个Map对象，可以按照接口定义，直接使用get获取返回值。
	 * <P>
	 */
	public synchronized Map<String, Object> sendRequestForPost(String urlstr, Map<String, String> reqData)
			throws Exception {
		return (Map<String, Object>) sendRequest("POST", urlstr, reqData);
	}

	/**
	 * <b>方法描述：</b>用于向基于Http协议的3.0平台发送同步Post请求。
	 * <P>
	 * <b>参数说明：</b>参数名 String urlstr 请求地址。 SIAP siap 请求报文，将接口定义参数放入SIAP就行。
	 * <P>
	 * <b>返回说明：</b>返回的是一个SIAP对象，可以按照接口定义，直接使用get获取返回值。
	 * <P>
	 */
	public synchronized SIAP sendRequestForPost(String urlstr, SIAP reqData) throws Exception {
		return (SIAP) sendRequest("POST", urlstr, reqData);
	}
	
	private synchronized Object sendRequestForHttp(String method, String urlstr, Object request) throws Exception{
		HttpURLConnection conn = null;
		InputStreamReader reader = null;
		Object rtn = null;
		String reqXml = null;
		log.info("HttpClientControler***发送" + method + "请求：" + urlstr);
		try {
			URL url = new URL(urlstr);
			
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5 * 60 * 1000);
			conn.setReadTimeout(5 * 60 * 1000);
			conn.setRequestMethod(method);
			
			if (request == null){
				throw new Exception("请求参数为空。");
			}
				
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Encoding", "UTF-8");
			conn.setRequestProperty("Content-Type", "text/xml");
			
			// 判断传入请求数据的类型，目前只处理map和SIAP
			if (request.getClass().equals(SIAP.class)) {
				String xml = xStream.toXML(request);
				String xmlHeader = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
//				reqXml = "reqXML=" + xmlHeader + "\r\n" + xml.replaceAll("SIAPHeader", "SIAP-Header").replaceAll("SIAPBody", "SIAP-Body");
				reqXml = xmlHeader + xml.replaceAll("SIAPHeader", "SIAP-Header").replaceAll("SIAPBody", "SIAP-Body");
				log.info("HttpClientControler***请求数据：" + reqXml.trim());
				conn.getOutputStream().write(reqXml.getBytes("UTF-8"));
			} else {
				reqXml = xStream.toXML(request);
				log.info("HttpClientControler***请求数据：" + reqXml);
				conn.getOutputStream().write(reqXml.getBytes("UTF-8"));
			}
			conn.getOutputStream().flush();
			conn.getOutputStream().close();
			int rCode = conn.getResponseCode();
			if (rCode == HttpURLConnection.HTTP_OK) {
				if (conn.getInputStream() != null) {
					if (request.getClass().equals(SIAP.class)) {
						String returnInterBOSSXml = inputStream2String(conn.getInputStream());
						log.info("HttpClientControler***返回的原始报文：" + returnInterBOSSXml);
						rtn = xStream.fromXML(returnInterBOSSXml);
					} else {
						reader = new InputStreamReader(conn.getInputStream(), "UTF-8");
						rtn = (Map<String, Object>) xStream.fromXML(reader);
					}
				}
				log.info("HttpClientControler***Http请求成功。");
			} else {
				throw new Exception("http请求失败");
			}

		} catch (Exception e) {
			throw new Exception("http请求失败", e);
		} finally {
			try {
				reader.close();
				conn.disconnect();
			} catch (Exception e) {
			}
		}
		return rtn;
	}
	
	private synchronized Object sendRequestForHttps(String method, String urlstr, Object request) throws Exception{
		HttpsURLConnection conn = null;
		InputStreamReader reader = null;
		Object rtn = null;
		String reqXml = null;
		log.info("HttpClientControler***发送" + method + "请求：" + urlstr);
		try {
			URL url = new URL(urlstr);
			
			conn = (HttpsURLConnection) url.openConnection();
			conn.setConnectTimeout(5 * 60 * 1000);
			conn.setReadTimeout(5 * 60 * 1000);
			conn.setRequestMethod(method);
			
			conn.setHostnameVerifier(new TrustAnyHostnameVerifier());
			if (request == null){
				throw new Exception("请求参数为空。");
			}
				
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Encoding", "UTF-8");
			conn.setRequestProperty("Content-Type", "text/xml");
			
			// 判断传入请求数据的类型，目前只处理map和SIAP
			if (request.getClass().equals(SIAP.class)) {
				String xml = xStream.toXML(request);
				String xmlHeader = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
//				reqXml = "reqXML=" + xmlHeader + "\r\n" + xml.replaceAll("SIAPHeader", "SIAP-Header").replaceAll("SIAPBody", "SIAP-Body");
				reqXml = xmlHeader + xml.replaceAll("SIAPHeader", "SIAP-Header").replaceAll("SIAPBody", "SIAP-Body").replaceAll("__", "_");
				log.info("HttpClientControler***请求数据：" + reqXml.trim());
				conn.getOutputStream().write(reqXml.getBytes("UTF-8"));
			} else {
				reqXml = xStream.toXML(request);
				log.info("HttpClientControler***请求数据：" + reqXml);
				conn.getOutputStream().write(reqXml.getBytes("UTF-8"));
			}
			conn.getOutputStream().flush();
			conn.getOutputStream().close();
			int rCode = conn.getResponseCode();
			if (rCode == HttpURLConnection.HTTP_OK) {
				if (conn.getInputStream() != null) {
					if (request.getClass().equals(SIAP.class)) {
						String returnInterBOSSXml = inputStream2String(conn.getInputStream());
						log.info("HttpClientControler***返回的原始报文：" + returnInterBOSSXml);
						returnInterBOSSXml = returnInterBOSSXml.replaceAll("SIAP-Header", "SIAPHeader").replaceAll("SIAP-Body", "SIAPBody");
						rtn = xStream.fromXML(returnInterBOSSXml);
					} else {
						reader = new InputStreamReader(conn.getInputStream(), "UTF-8");
						rtn = (Map<String, Object>) xStream.fromXML(reader);
					}
				}
				log.info("HttpClientControler***Http请求成功。");
			} else {
				throw new Exception("http请求失败");
			}

		} catch (Exception e) {
			throw new Exception("http请求失败", e);
		} finally {
			try {
				reader.close();
				conn.disconnect();
			} catch (Exception e) {
			}
		}
		return rtn;
	}
	
	

	private synchronized Object sendRequest(String method, String urlstr, Object request) throws Exception {
		if(ObjectUtil.trim(urlstr).toLowerCase().startsWith("https")){
			return this.sendRequestForHttps(method, urlstr, request);
		}
		if(ObjectUtil.trim(urlstr).toLowerCase().startsWith("http")){
			return this.sendRequestForHttp(method, urlstr, request);
		}
		throw new Exception("请求的URL必须以http或者https开头!");
	}
	
	public static synchronized String inputStream2String(InputStream is) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int i = -1;
		try {
			while ((i = is.read()) != -1) {
				baos.write(i);
			}
		} catch (IOException e) {
			throw new Exception("读取InputStream异常...", e);
		}
			return baos.toString("UTF-8");
	}
	
}
