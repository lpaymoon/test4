package com.umpay.hfweb.common;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.util.ObjectUtil;
import com.umpay.hfweb.util.SessionThreadLocal;
import com.umpay.loadstrategy.base.LoadStrategyInf;

/**
 * ******************  类说明  *********************
 * class       :  RestConnPool
 * @author     :  yangwr
 * @version    :  1.0  
 * description :  资源层通信控制
 * @see        :                        
 * ***********************************************
 */  
public class RestConnPool {
	private static final Logger log = Logger.getLogger(RestConnPool.class);
	
	private LoadStrategyInf loadStrategy;

    public void setLoadStrategy(LoadStrategyInf loadStrategy) {
		this.loadStrategy = loadStrategy;
	}

	private ConcurrentMap<String, AtomicInteger> counter = new ConcurrentHashMap<String, AtomicInteger>();
    private int  sendQsize=100;
	private static final String sendQName = "sendQ";
	private HttpClientControler httpClientCtrl;
    
	public RestConnPool(int size,HttpClientControler httpClientCtrl){
		this.sendQsize = size;
		this.httpClientCtrl = httpClientCtrl;
	}
	
	/**
	 * ********************************************
	 * method name   : getRestSrvPath 
	 * description   : 获取资源层服务路径
	 * @return       : String
	 * modified      : yangwr ,  Nov 4, 2011  11:11:03 AM
	 * @see          : 
	 * *******************************************
	 */
	private String getRestSrvPath(){
		/*
		 * liujilong 20130905 加入策略负载组件
		 */
		String srvURL =  loadStrategy.lookup();
			//messageService.getSystemParam(DataDict.REST_SRV_URL);
		if(srvURL.endsWith("/")){
			srvURL = srvURL.substring(0, srvURL.length()-1); 
		}
		return srvURL;
	}
	
	/** *****************  方法说明  *****************
	 * method name   :  finish
	 * @param		 :  @param retCode
	 * @param		 :  @param retCodeBussi
	 * @param		 :  @param responseMsgGlobal
	 * @return		 :  void
	 * @author       :  LiuJiLong 2013-9-10 下午03:13:39
	 * description   :  对请求返回数据的反馈处理  
	 * @see          :  
	 * ***********************************************/
	private void finish(String url, String retCode, MpspMessage responseMsgGlobal) {
		loadStrategy.finish(url, retCode);
	}
	
	public MpspMessage doGet(String url, Map<String,String> map){
		long beginTime = System.currentTimeMillis();
		MpspMessage message = null;
		String urlPre = null;
		try{
			Map<String,Object> onLockRes = onLock();
			if(onLockRes!=null){
				message = new MpspMessage();
				message.setRetCodeSysError();
				message.setRetCodeBussi((String)onLockRes.get(HFBusiDict.RETCODE));
			}else{
				urlPre = getRestSrvPath();
				message = getHttpRest( "http://" + urlPre + "/hfrestbusi" + url, map);
			}
			logInfo("RestConnPool doGet Result %s:%s",message.getRetCode(),message.getRetCodeBussi());
			return message;
		}finally{
			try{
				finish(urlPre, message.getRetCode(), message);
			}catch(Exception e){}
			offLock();
			logInfo("RestControl doGet useTime:"+(System.currentTimeMillis()-beginTime));
		}
	}
	
	public MpspMessage doPost(String url, Map<String,String> map){
		long beginTime = System.currentTimeMillis();
		MpspMessage message = null;
		String urlPre = null;
		try{
			Map<String,Object> onLockRes = onLock();
			if(onLockRes!=null){
				message = new MpspMessage();
				message.setRetCodeSysError();
				message.setRetCodeBussi((String)onLockRes.get(HFBusiDict.RETCODE));
			}else{
				urlPre = getRestSrvPath();
				message = postHttpRest("http://" + urlPre + "/hfrestbusi" + url, map);
			}
			logInfo("RestConnPool doPost Result %s:%s",message.getRetCode(),message.getRetCodeBussi());
			return message;
		} finally{
			try{
				finish(urlPre, message.getRetCode(), message);
			}catch(Exception e){}
			offLock();
			log.info("RestControl doPost useTime--rpid[" + getRpid() + "]--UseTime:"+(System.currentTimeMillis()-beginTime)+"--URL:"+url);
		}
	}
	/**
	 * 处理http请求
	 * @param urlstr 请求的URL
	 * @param m 参数Map
	 * @return 以Map方式返回结果
	 * @throws IOException 
	 * @throws HttpException 
	 */
	private MpspMessage postHttpRest(String urlstr, Map<String,String> paramMap){
		MpspMessage respMessage = new MpspMessage();
		paramMap.put("x-accept-charset", "UTF-8");
		PostMethod postMethod = new PostMethod(urlstr);
		postMethod.getParams().setContentCharset("UTF-8");
		Object result = httpClientCtrl.getHttpResPost_Form(paramMap,postMethod);
		if(result == null){
			logInfo("rest未返回信息");
			respMessage.setRetCode(DataDict.SYSTEM_ERROR_CODE);
			respMessage.setRetCodeBussi(DataDict.SYSTEM_ERROR_REST_NULL);
		}else if(result instanceof Exception){
			Exception e = (Exception)result;
			logInfo("rest服务通信异常");
			logInfo(ObjectUtil.handlerException(e, getRpid()));
			respMessage.setRetCode(DataDict.SYSTEM_ERROR_CODE);
			respMessage.setRetCodeBussi(DataDict.SYSTEM_ERROR_REST_ERROR);
		}else if(!(result instanceof Map)){
			logInfo("rest返回格式异常");
			respMessage.setRetCode(DataDict.SYSTEM_ERROR_CODE);
			respMessage.setRetCodeBussi(DataDict.SYSTEM_ERROR_REST_ERROR_TYPE);
		}else{
			Map<String,Object> retMap = (Map<String,Object>)result;
			respMessage.getWrappedMap().putAll(retMap);
			respMessage.setRetCode(ObjectUtil.trim(retMap.get(HFBusiDict.RETCODE)));
			respMessage.setRetCodeBussi(ObjectUtil.trim(retMap.get(HFBusiDict.RETCODE)));
		}
		return respMessage;
	}
	
	/**
	 * 处理http请求Get
	 * @param urlstr 请求的URL
	 * @param paramMap 参数Map
	 * @return 以Map方式返回结果
	 * @throws IOException 
	 * @throws HttpException 
	 */
	private MpspMessage getHttpRest(String urlstr, Map<String,String> paramMap){
		MpspMessage respMessage = new MpspMessage();
		paramMap.put("x-accept-charset", "UTF-8");
		GetMethod getMethod = new GetMethod(urlstr);
		getMethod.getParams().setContentCharset("UTF-8");
		Object result = httpClientCtrl.getHttpResGet_Form(paramMap,getMethod);
		if(result == null){
			logInfo("rest未返回信息");
			respMessage.setRetCode(DataDict.SYSTEM_ERROR_CODE);
			respMessage.setRetCodeBussi(DataDict.SYSTEM_ERROR_REST_NULL);
		}else if(result instanceof Exception){
			Exception e = (Exception)result;
			logInfo("rest服务通信异常");
			logInfo(ObjectUtil.handlerException(e, getRpid()));
			respMessage.setRetCode(DataDict.SYSTEM_ERROR_CODE);
			respMessage.setRetCodeBussi(DataDict.SYSTEM_ERROR_REST_ERROR);
		}else if(!(result instanceof Map)){
			logInfo("rest返回格式异常");
			respMessage.setRetCode(DataDict.SYSTEM_ERROR_CODE);
			respMessage.setRetCodeBussi(DataDict.SYSTEM_ERROR_REST_ERROR_TYPE);
		}else{
			Map<String,Object> retMap = (Map<String,Object>)result;
			respMessage.getWrappedMap().putAll(retMap);
			respMessage.setRetCode(ObjectUtil.trim(retMap.get(HFBusiDict.RETCODE)));
			respMessage.setRetCodeBussi(ObjectUtil.trim(retMap.get(HFBusiDict.RETCODE)));
		}
		return respMessage;
	}
	
	private Map<String,Object> onLock(){      
    	Integer count = 1;
    	AtomicInteger cache = counter.get(sendQName);
    	if(cache==null){//计数器不存在的情况
    		//尝试新增计数器
    		cache = counter.putIfAbsent(sendQName, new AtomicInteger(1));
    		if(cache!=null){//已经存在计数器
    			count=cache.incrementAndGet();
    		}
    	}else{
    		count=cache.incrementAndGet();
    	}
    	if(count>sendQsize){
    		log.info("rest返回发送队列满载信息 maxsize["+sendQsize+"] count["+count+"]");
    		Map<String,Object> resMap = new HashMap<String, Object>();
    		resMap.put(HFBusiDict.RETCODE, "1300");
    		return resMap;
    	}   		
    	log.info("入rest发送队列 当前队列size["+count+"] end");
    	return null;
    }
	
	 private void offLock(){
	    int count = counter.get(sendQName).decrementAndGet();
	    log.info("出rest发送队列 当前队列size["+count+"] end");
	 }
	 
	public int getSendQsize() {
		return sendQsize;
	}
	
	private String getRpid(){
		return ObjectUtil.trim(SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
	}
	
	public void logInfo(String message,Object... args){
		String rpid = SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID);
		String funCode = SessionThreadLocal.getSessionValue(DataDict.FUNCODE);
		log.info(String.format("%s,%s,%s",funCode,rpid,String.format(message,args)));
	}

	public void setHttpClientCtrl(HttpClientControler httpClientCtrl) {
		this.httpClientCtrl = httpClientCtrl;
	}


//	public void setSendQsize(int sendQsize) {
//		this.sendQsize = sendQsize;
//	}
}
