package com.umpay.hfweb.util;

import java.util.HashMap;
import java.util.Map;

import net.sf.ehcache.Element;

import org.apache.log4j.Logger;

import com.umpay.hfweb.cache.AbstractCacheFactory;
import com.umpay.hfweb.cache.CommonEcc;


/** ******************  类说明  *********************
 * class       :  CommonUtil
 * @author     :  panxingwu
 * @version    :  1.0  
 * description :  通用工具类，封装一些可复用的方法
 * @see        :                        
 * ************************************************/   
public class CommonUtil {
	protected static Logger logger = Logger.getLogger(CommonUtil.class);
	private static final String Sended_SMS_Count= "intSendedCount";
	/** ********************************************
	 * method name   : timesLimit 
	 * description   : 交易次数限制，现在在缓存指定的周期内允许交易的最大次数
	 * @return       : boolean
	 * @param        : @param key		 :缓存对象的key
	 * @param        : @param cacheName  :缓存名称
	 * @param        : @param maxTimes   :最大限制次数
	 * modified      : panxingwu ,  2013-7-10  下午3:58:15
	 * @see          : 
	 * ********************************************/      
	public static Map<String,Object> timesLimit(String cacheName,String key,long maxTimes){
		boolean flag = true;
		String retMsg = "";
		CommonEcc cache = (CommonEcc)AbstractCacheFactory.getInstance().getCacheClient(cacheName);
		Element element = cache.getElementByDefaultMap(key,Sended_SMS_Count,new Integer(0));
		int lifeTime = element.getTimeToIdle();
		int hours = lifeTime/60/60; 
		logger.info("缓存时间:"+lifeTime);
		Map<String,Object> cacheInfo = (Map<String,Object>)element.getValue();
		Integer intSendedCount = (Integer)cacheInfo.get(Sended_SMS_Count);
		if(intSendedCount==0) intSendedCount=1;
		logger.info("当前为第"+intSendedCount+"次交易");
		if(intSendedCount>maxTimes){
			retMsg=hours+"小时内最多只能交易"+maxTimes+"次";
			flag=false;
		}
		intSendedCount++;
		cacheInfo.put(Sended_SMS_Count, intSendedCount);
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("flag", flag);
		return map;
	}
	/**
	 * *****************  方法说明  *****************
	 * method name   :  isQRCodeAccess
	 * @param		 :  @param sign
	 * @param		 :  @param merId
	 * @param		 :  @return
	 * @return		 :  boolean
	 * @author       :  LiZhen 2014-6-24 下午8:48:35
	 * description   :  根据配置文件判断二维码支付是否打开
	 * @see          :  
	 * **********************************************
	 */
	public static boolean isQRCodeAccess(String sign,String merId){
		if(sign!=null && "".equals(sign) && merId!=null){
			return false;
		}else if("ALL".equals(sign)){
			return true;
		}else if(sign.indexOf(merId)!=-1){
			return true;
		}else{
			return false;
		}
	}
}
