package com.umpay.hfweb.cache;

import java.util.Hashtable;
import java.util.Map;

public class CommonEccFactory extends AbstractCacheFactory{

	private Map<String,AbstractCacheClient> eccMap = new Hashtable<String,AbstractCacheClient>();
	//保证不在包外的类中实例化
	CommonEccFactory(){
		
	}
	@Override
	public AbstractCacheClient getCacheClient(String cacheName) {
		if (!eccMap.containsKey(cacheName)) {
			AbstractCacheClient dao = createCacheClient(cacheName);
			eccMap.put(cacheName, dao);
		}
		return  eccMap.get(cacheName);
	}
	
	public AbstractCacheClient createCacheClient(String cacheName){
		if(cacheName.equals("userCache")){
			return new UserCacheClient(cacheName);
		}
		// 商户渠道备案信息缓存   added by FanXiangChi at 2012-02-16
		if(HFMerReferCache.CACHE_NAME.equals(cacheName)){
			return new HFMerReferCache(cacheName);
		}
		return new CommonEcc(cacheName);
	}
	@Override
	public AbstractCacheClient getUserCacheClient() {
		return getCacheClient("userCache");
	}
}
