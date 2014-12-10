package com.umpay.hfweb.model;

import java.util.HashMap;
import java.util.Map;

import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.util.ObjectUtil;

public class MpspMessage {
	protected Map<String,Object> mpspMap;
	
	public MpspMessage(){
		if(mpspMap == null){
			mpspMap = new HashMap<String,Object>();
			mpspMap.put(DataDict.RET_CODE, DataDict.SYSTEM_ERROR_CODE);
		}
	}
	public MpspMessage(Map<String,Object> map){
		mpspMap = map;
		mpspMap.put(DataDict.RET_CODE, DataDict.SYSTEM_ERROR_CODE);
	}
	
	public String getStr(String key){
		Object value = get(key);
		if(value == null){
			return null;
		}else{
			if(value instanceof String){
				return (String)value;
			}else{
				return value.toString();
			}
		}
	}
	
	public void putAll(MpspMessage message){
		mpspMap.putAll(message.getWrappedMap());
	}
	
	public Object get(String key){
		return mpspMap.get(key);
	}
	
	public Map<String,Object> getWrappedMap(){
		return mpspMap;
	}
	
	public Object put(String key ,Object value){
		return mpspMap.put(key, value);
	}
	
	public String getRetCode(){
		return ObjectUtil.trim(mpspMap.get(DataDict.RET_CODE));
	}
	
	public boolean isRetCode0000(){
		return ObjectUtil.trim(mpspMap.get(DataDict.RET_CODE)).equals(DataDict.SUCCESS_RET_CODE);
	}
	
	public void setRetCode(String retCode){
		this.put(DataDict.RET_CODE, retCode);
	}
	public void setRetCode0000(){
		setRetCode(DataDict.SUCCESS_RET_CODE);
	}
	
	public boolean isRetCodeSysError(){
		return ObjectUtil.trim(mpspMap.get(DataDict.RET_CODE)).equals(DataDict.SYSTEM_ERROR_CODE);
	}
	
	public void setRetCodeSysError(){
		setRetCode(DataDict.SYSTEM_ERROR_CODE);
	}
	
	public void setRetCodeBussi(String retCode){
		this.put(DataDict.RET_CODE_BUSSI, retCode);
	}
	
	public String getRetCodeBussi(){
		return ObjectUtil.trim(mpspMap.get(DataDict.RET_CODE_BUSSI));
	}
}
