package com.umpay.hfweb.model;

import java.util.Map;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.util.ObjectUtil;

/**
 * 只对Http请求消息
 * @Title: RequestMsg.java
 * @Package com.umpay.hfweb.model
 * @Description: TODO(添加描述)
 * @author yangwr
 * @date Nov 1, 2011 10:32:26 PM
 * @version V1.0
 */
public class RequestMsg extends MpspMessage{

	public void putAllParam(Map<String,String> map){
		mpspMap.putAll(map);
	}
	public void putAllAttr(Map<String,Object> map){
		mpspMap.putAll(map);
	}
	public String getFunCode(){
		return ObjectUtil.trim(mpspMap.get(DataDict.FUNCODE));
	}
	public void setFunCode(String funCode){
		mpspMap.put(DataDict.FUNCODE, funCode);
	}
	public String getRpid(){
		return ObjectUtil.trim(mpspMap.get(DataDict.REQ_MER_RPID));
	}
	public void setRpid(String rpid){
		mpspMap.put(DataDict.REQ_MER_RPID, rpid);
	}
	
}
