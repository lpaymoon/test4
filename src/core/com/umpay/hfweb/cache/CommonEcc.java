package com.umpay.hfweb.cache;

import java.util.HashMap;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;


public class CommonEcc extends AbstractCacheClient{
	protected CacheManager cacheManager;

	public CacheManager getCacheManager() {
//		WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
//		cacheManager = (CacheManager)wac.getBean("cacheManager");
		if(cacheManager == null){
			cacheManager = (CacheManager)com.umpay.hfweb.util.SpringContextUtil.getBean("cacheManager");
		}
		return cacheManager;
	}

	private String cacheName=null;
	public CommonEcc(String cacheName){
		this.cacheName = cacheName;
		getCacheManager();
	}
	
	/**
	 * Retrieve a key from the server, using a specific hash.
	 *
	 *  If the data was compressed or serialized when compressed, it will automatically<br/>
	 *  be decompressed or serialized, as appropriate. (Inclusive or)<br/>
	 *<br/>
	 *  Non-serialized data will be returned as a string, so explicit conversion to<br/>
	 *  numeric types will be necessary, if desired<br/>
	 *
	 * @param key key where data is stored
	 * @return the object that was previously stored, or null if it was not previously stored
	 */
	public Object get(Object key) {
		Element e = getElement(key);
		if(e == null){
			return null;
		}
		return e.getValue();
	}
	/**
	 * Stores data on the server; only the key and the value are specified.
	 *
	 * @param key key to store data under
	 * @param value value to store
	 * @return true, if the data was successfully stored
	 */
	public void put(Object key, Object value) {
		Cache cache = getCacheManager().getCache(cacheName);
		cache.put(new Element(key,value));
	}
	
	public Element getElement(Object key){
		Cache cache = getCacheManager().getCache(cacheName);
		Element e = cache.get(key);
		if(e == null){
			e = new Element(key,key);
			cache.put(e);
		}
		return e;
	}
	
	public Element getElementByDefaultMap(Object key,String subKey,Object subValue){
		Cache cache = getCacheManager().getCache(cacheName);
		Element e = cache.get(key);
		if(e == null){
			Map<String,Object> cacheInfo = new HashMap<String,Object>();
			cacheInfo.put(subKey, subValue);
			e = new Element(key,cacheInfo);
			cache.put(e);
		}
		return e;
	}

	@Override
	public boolean remove(Object key) {
		Cache cache = getCacheManager().getCache(cacheName);
		return cache.remove(key);
	}

	@Override
	public Object replace(Object key, Object value) {
		Cache cache = getCacheManager().getCache(cacheName);
		Element e = cache.get(key);
		if(e != null){
			return cache.replace(new Element(key,value));
		}else{
			cache.put(new Element(key,value));
		}
		return null;
	}
	
	public Cache getCache(){
		Cache cache = getCacheManager().getCache(cacheName);
		return cache;
	}

}
