package com.umpay.hfweb.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;

public class HFMerReferCache extends CommonEcc {
	
	private static Logger logger = Logger.getLogger(HFMerReferCache.class);
	public static final String CACHE_NAME = "hfMerReferCache";

	public HFMerReferCache(String cacheName) {
		super(cacheName);
	}

	/**
	 * 根据key获取备案的缓存对象
	 * @param key ： 格式  merid_goodsid
	 * @return 备案url的Map集合<'refer', refer>
	 */
	public Object getMerReferFromCache(String key){
		logger.info("key[" + key + "]获取备案信息缓存对象");
		Cache cache = this.cacheManager.getCache(CACHE_NAME);
		Element element = cache.get(key);
		
		return element == null ? null : element.getValue();
	}
	
	public void removeAll(){
		logger.info("清空备案信息缓存");
		Cache cache = this.cacheManager.getCache(CACHE_NAME);
		cache.removeAll();
	}
}
