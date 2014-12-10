package com.umpay.hfweb.util;

import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import org.apache.log4j.Logger;

import com.umpay.hfweb.dict.DataDict;


public class ObjectUtil {

	public static String trim(String str){
		if(str == null){
			return "";
		}else{
			return str.trim();
		}
	}
	
	public static String trim(Object obj){
		if(obj == null){
			return "";
		}else{
			return obj.toString();
		}
	}
    public static String unifyUrl(String srvURL){
    	if(srvURL.endsWith("/")){
			srvURL = srvURL.substring(0, srvURL.length()-1); 
		}
    	return srvURL;
     }
	public static boolean isEmpty(String str){
		if(str == null){
			return true;
		}else{
			if(str.trim().length() <= 0){
				return true;
			}
		}
		return false;
	}
	
	public static boolean isNotEmpty(String str){
		return !isEmpty(str);
	}
	/**
	 * 异常处理函数
	 */
	public static String handlerException(Throwable e,String prefix) {
		StringBuffer sbe = new StringBuffer();	
		StringWriter strWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(strWriter);
		e.printStackTrace(printWriter);
		printWriter.flush();
		LineNumberReader reader = new LineNumberReader(new StringReader(strWriter.toString()));
		try {
			String errInfo = reader.readLine();
			//sbe.append("\r\n");
			while(errInfo != null){
				sbe.append(prefix).append(":").append(errInfo).append("\r\n");
				errInfo = reader.readLine();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		} finally{
			try {
				reader.close();
				printWriter.close();
				strWriter.close();
				reader = null;
				strWriter = null;
				printWriter = null;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return sbe.toString();
	}
	
	public static void logInfo(Logger log,String message,Object... args){
		String rpid = SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID);
		String funCode = SessionThreadLocal.getSessionValue(DataDict.FUNCODE);
		log.info(String.format("%s,%s,%s",funCode,rpid,String.format(message,args)));
	}
	public static void logWarn(Logger log,String message,Object... args){
		String rpid = SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID);
		String funCode = SessionThreadLocal.getSessionValue(DataDict.FUNCODE);
		log.warn(String.format("%s,%s,%s",funCode,rpid,String.format(message,args)));
	}
	
	/**
	 * 判断文件是否有更新, 使用此方法的前提是文件必须存在[有可判断的意义]
	 * @param filePath ： Map里存的key  一般指文件路径
	 * @param map 存放文件时间的Map<filePath, lastModifyTime>
	 * @return true || false
	 */
	public static boolean needReloadFile(String filePath, Map<String, Long> map){
		File file = new File(filePath);

		Long fileLastMod = file.lastModified();
		// 取出map中的最后修改时间  如果不存在  直接put后返回
		Long lastMod = map.get(filePath);
		if(lastMod == null){
			map.put(filePath, fileLastMod);
			return true;
		}
		
		if(fileLastMod.longValue() != lastMod.longValue()){
			map.put(filePath, fileLastMod);
			return true;
		}
		return false;
	}
}
