package com.umpay.hfweb.cache;

public abstract class AbstractCacheClient {

	public abstract Object get(Object key);
	public abstract void put(Object key, Object value);
	public abstract boolean remove(Object key);
	public abstract Object replace(Object key, Object value); 
}
