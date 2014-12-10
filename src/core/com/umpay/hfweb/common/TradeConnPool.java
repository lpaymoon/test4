package com.umpay.hfweb.common;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

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
public class TradeConnPool {
	private static final Logger log = Logger.getLogger(TradeConnPool.class);

    private ConcurrentMap<String, AtomicInteger> counter = new ConcurrentHashMap<String, AtomicInteger>();
    private int  sendQsize=100;
	private static final String sendQName = "sendQ";
	private HttpClientControler httpClientCtrl;
	private LoadStrategyInf loadStrategy;

    
	public TradeConnPool(int size,HttpClientControler httpClientCtrl){
		this.sendQsize = size;
		this.httpClientCtrl = httpClientCtrl;
	}
	/**
	 * ********************************************
	 * method name   : getRestSrvPath 
	 * description   : 获取资源服务路径
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
		
	public MpspMessage doPost(String url, Map<String,String> map){
		long beginTime = System.currentTimeMillis();
		MpspMessage message = null;
		String urlPre = getRestSrvPath();
		try{
			Map<String,Object> onLockRes = onLock();
			if(onLockRes!=null){
				message = new MpspMessage();
				message.setRetCode((String)onLockRes.get(DataDict.RET_CODE));
			}else{
				long time2 = System.currentTimeMillis();
				message = getHttpRes("http://" + urlPre + "/hftradebusi" +url, map);
				logInfo("TradeControl getHttpRes useTime:"+(System.currentTimeMillis()-time2));
			}
			try{
				SessionThreadLocal.setSessionValue("tradeIp", urlPre.split("\\.")[3].split(":")[0]);
			}catch(Exception e){}
			return message;
		}finally{
			try{
				finish(urlPre, message.getRetCode(), message);
			}catch(Exception e){}
			offLock();
			logInfo("TradeControl doPost useTime:"+(System.currentTimeMillis()-beginTime));
		}
	}
	
	private  MpspMessage getHttpRes(String urlstr, Map<String,String> paramMap){
		MpspMessage respMessage = new MpspMessage();
		PostMethod post = new PostMethod(urlstr);
		post.getParams().setContentCharset("GBK");
		Map<String,String> retMap = (Map<String,String>)httpClientCtrl.getHttpResPost_XML(paramMap,post);
		if(retMap == null){
			logInfo("Trade未返回信息");
			respMessage.setRetCode(DataDict.SYSTEM_ERROR_CODE);
			respMessage.setRetCodeBussi(DataDict.SYSTEM_ERROR_TRADE_NULL);
		}else{
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
    		log.info("trade返回发送队列满载信息 maxsize["+sendQsize+"] count["+count+"]");
    		Map<String,Object> resMap = new HashMap<String, Object>();
    		resMap.put(DataDict.RET_CODE, "1306");
    		return resMap;
    	}   		
    	log.info("入trade发送队列 当前队列size["+count+"] end");
    	return null;
    }
	
	 private void offLock(){
	    int count = counter.get(sendQName).decrementAndGet();
	    log.info("出trade发送队列 当前队列size["+count+"] end");
	 }
	 
	public int getSendQsize() {
		return sendQsize;
	}
	public void logInfo(String message,Object... args){
		String rpid = SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID);
		String funCode = SessionThreadLocal.getSessionValue(DataDict.FUNCODE);
		log.info(String.format("%s,%s,%s",funCode,rpid,String.format(message,args)));
	}

	private String getRpid(){
		return ObjectUtil.trim(SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
	}
	public void setLoadStrategy(LoadStrategyInf loadStrategy) {
		this.loadStrategy = loadStrategy;
	}
}
