package com.umpay.hfweb.cache;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;
import org.springframework.util.SystemPropertyUtils;

public abstract class AbstractCacheFactory {

	private static Logger log = Logger.getLogger(AbstractCacheFactory.class);
	private static AbstractCacheFactory instance;

	public static AbstractCacheFactory getInstance(){
		if(instance == null){
			synchronized(AbstractCacheFactory.class){
				long startTime = System.currentTimeMillis();
				try {
					String factory = SystemPropertyUtils.resolvePlaceholders("${uniform.cache.factory}");
					if(StringUtils.hasLength(factory)){
							instance = (AbstractCacheFactory)forName(factory).newInstance();
					}
					
					log.info("platform use "+factory+" to cache object");
				} catch (Exception e) {
					log.warn("platform fail to load cache factory,see system property uniform.cache.factory");
				}
				if(instance == null){
					log.info("platform uses the default cache(Ehcache) to cache object");
					instance = new CommonEccFactory();
				}
				long endTime = System.currentTimeMillis();
				log.info("platform load cache class use time :"+(endTime-startTime));
			}
		}
		return instance;
	}
	
	public abstract AbstractCacheClient getCacheClient(String cacheName);
	public abstract AbstractCacheClient getUserCacheClient();
	
	public static Class<?> forName(String name) throws ClassNotFoundException{
		ClassLoader cl = null;
		try {
			cl = Thread.currentThread().getContextClassLoader();
		}
		catch (Throwable ex) {
			// Cannot access thread context ClassLoader - falling back to system class loader...
		}
		if (cl == null) {
			// No thread context class loader -> use class loader of this class.
			cl = AbstractCacheFactory.class.getClassLoader();
		}
		return cl.loadClass(name);
	}
	
	public static void main(String[] args){
		System.out.println(AbstractCacheFactory.getInstance().getCacheClient("**Cache"));
	}

}
