package com.umpay.hfweb.model;

import java.util.Map;

/**
 * ******************  类说明  *********************
 * class       :  ResponseMsg
 * @author     :  yangwr
 * @version    :  1.0  
 * description :  针对Http响应消息
 * @see        :                        
 * ***********************************************
 */
public class ResponseMsg extends MpspMessage{
	
	private static final String RESPONSE_MSG = "directResMsg";
	
	private MpspMessage infoMsg;

	public ResponseMsg(){
		super();
	}
	public ResponseMsg(Map<String,Object> map){
		super(map);
	}
	public String getDirectResMsg() {
		return (String)mpspMap.get(RESPONSE_MSG);
	}
	public void setDirectResMsg(String directResMsg) {
		mpspMap.put(RESPONSE_MSG, directResMsg);
	}
	public MpspMessage getInfoMsg() {
		return infoMsg;
	}
	public void setInfoMsg(MpspMessage infoMsg) {
		this.infoMsg = infoMsg;
	}
	public byte[] getDiretByteMsg(){
		return (byte[]) mpspMap.get(RESPONSE_MSG);
	}
	public void setDirectByteMsg(byte[] data){
		mpspMap.put(RESPONSE_MSG, data);
	}
	
}
