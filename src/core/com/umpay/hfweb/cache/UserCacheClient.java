package com.umpay.hfweb.cache;

import java.util.Date;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;

import com.umpay.hfweb.util.ObjectUtil;

public class UserCacheClient extends CommonEcc{
	private static Logger logger = Logger.getLogger(UserCacheClient.class);
	public static final String CACHE_NAME = "userCache";
	public UserCacheClient(String cacheName) {
		super(cacheName);
	}
	
	/*
	 * ********************************************
	 * method name   : checkUserOrdersLtd 
	 * description   : 检查用户在单位时间的下单次数，若用户在缓存有效期内下n笔订单，则返回下单失败
	 * @return       : void
	 * @param        : mobileId 下订单手机号
	 * @param        : userOrderV 用户下订单次数阀值
	 * @return       : 返回码 0000为正常可下单，1170为首次达到上限，1171为超过上限
	 * modified      : zhangwl ,  Aug 4, 2011
	 * @see          : 
	 * *******************************************
	 */
	public String checkUserOrdersLtd(String mobileId,String userOrderVCfg){
		//下订单手机号
		//String mobileId = webInfo.getMobileId();
				
		//获取用户下单缓存		
		Cache cache = cacheManager.getCache(CACHE_NAME);
		//获取用户下单缓存对象（不修改缓存的统计信息）
		Element element = cache.getQuiet(mobileId);
		
		//用户上次下单的时间
		long lastTime = 0;
		//用户下单次数
		long orderTimes = 0;
				
		if(element==null){
			//如果缓存不存在，则新增缓存，此时lastTime=0,orderTimes=0
			cache.put(new Element(mobileId,mobileId));
		}else{
			//如果缓存存在
			//1、当上次访问时间为0时，说明用户是第二次访问，则上次访问时间为缓存的创建时间
			//2、当上次访问时间不为0是，说明用户是大于二次的访问，则上次访问时间为缓存的上次访问时间
			lastTime = element.getLastAccessTime();
			if(lastTime==0){
				lastTime = element.getCreationTime();
			}
			//重新获取用户下单缓存，主要是更新了缓存的统计信息，并且获取下单次数
			element = cache.get(mobileId);
			orderTimes = element.getHitCount();
		}
		
		logger.info("mobileid["+mobileId+"] 上次下单时间["+new Date(lastTime)+"] 下单累计次数["+orderTimes+"]");
		//logger.info("mobileid["+mobileId+"] 用户下单次数缓存数量["+cache.getSize()+"]");
		
		//下面为具体的控制规则
		
		//String _userOrderV = getLocalMsg("UserOrderV");
		//获取用户下订单次数阀值，默认为10
		int userOrderV = 5;
		try{
			userOrderV = Integer.valueOf(userOrderVCfg);
		}catch(Exception e){
			logger.error(ObjectUtil.handlerException(e, "userOrderV"));
		}
		
		//计算两笔订单的时间价格=系数*下单次数的立方
		long interval = userOrderV * orderTimes * orderTimes * orderTimes*1000;
		
		if(lastTime!=0&&(System.currentTimeMillis()-lastTime) < interval){
			logger.info("mobileid["+mobileId+"] 当前下单时间["+new Date()+"] 上次下单时间["+new Date(lastTime)+"] 下单累计次数["+orderTimes+"] 交易间隔时间["+interval+"]被屏蔽");
			if(orderTimes==userOrderV){
				//throw new Exception("1170");
				return "1170";
			}else{
				//throw new Exception("1171");
				return "1171";
			}	
		}
		return com.umpay.hfweb.dict.DataDict.SUCCESS_RET_CODE;
		
	}
	
}
