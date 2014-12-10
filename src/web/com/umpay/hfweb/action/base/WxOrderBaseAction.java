package com.umpay.hfweb.action.base;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.exception.BusinessException;
import com.umpay.hfweb.exception.WebBusiException;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.Encryptor;
import com.umpay.hfweb.util.HttpUtil;
import com.umpay.hfweb.util.ObjectUtil;
import com.umpay.hfweb.util.SequenceUtil;
import com.umpay.hfweb.util.SessionThreadLocal;


/** ******************  类说明  *********************
 * class       :  WxOrderBaseAction
 * @author     :  panxingwu
 * @version    :  1.0  
 * description :  无线接入商户下单基础类
 * @see        :                        
 * ************************************************/   
public abstract class WxOrderBaseAction extends BaseAbstractAction {
	protected Encryptor encryptor;
	@Override
	protected String processBussiness(HttpServletRequest request,
			HttpServletResponse response, Map<String, Object> modelMap)throws BusinessException {
			ResponseMsg respMap = new ResponseMsg(modelMap);
			RequestMsg reMsg = new RequestMsg();
			reMsg.put(DataDict.NET_CLIENTIP, request.getRemoteAddr());
			//第一步先执行createTransRpid以后将request的数据解密并放入Attribute中
			Enumeration requestNames = request.getAttributeNames();
			while(requestNames.hasMoreElements()){
				String name = (String) requestNames.nextElement();
				String value = String.valueOf(request.getAttribute(name));
				reMsg.put(name,value);
			}
			logInfo("请求数据为:%s", reMsg.getWrappedMap().toString());
			try{
				processBussiness(reMsg,respMap);
			}catch (Exception e) {
				logError(e);
				respMap.setRetCode(DataDict.SYSTEM_ERROR_CODE);
				responseEorr(reMsg,respMap);
			}
			try {
				byte[] responseMsg = (byte[])respMap.getDiretByteMsg();
				logInfo("WxOrderMsg:%s",responseMsg);
				response.setCharacterEncoding("UTF-8");
				response.getOutputStream().write(responseMsg);
				response.flushBuffer();
				return "";
			} catch (IOException e) {
				throw new WebBusiException(DataDict.SYSTEM_ERROR_CODE,e);
			}
	}

	protected abstract void responseEorr(RequestMsg requestMsg, ResponseMsg respMap);
	protected abstract void processBussiness(RequestMsg requestMsg, ResponseMsg respMap);
	
	/**
	 * ********************************************
	 * method name   : createTransRpid 
	 * modified      : panxingwu ,  2012-4-11
	 * description   : 重载此方法是因为有些请求的request是经过加密的不能直接取得merid
	 * *******************************************
	 */
	@Override
	protected String createTransRpid(HttpServletRequest request) {
		return getRpid4DES(request,true);
	}
	/**
	 * ********************************************
	 * method name   : decryptData 
	 * description   : 解密数据
	 * @return       : void
	 * @param        : @param requestMsg
	 * @param        : @param request
	 * modified      : panxingwu ,  2012-4-11  下午6:19:15
	 * @see          : 
	 * *******************************************
	 */
	protected RequestMsg decryptData(HttpServletRequest request,boolean isDES){
		RequestMsg reqMsg = new RequestMsg();
		reqMsg.putAllParam(HttpUtil.parseRequestParam(request));
		Map<String,Object> map = null;
		if(isDES){
			logInfo("加密数据，需要解密");
			//获取request请求包体中的加密数据
			byte[] data = getDataFromInputStream(request);
			logInfo("从request中获取的字节流为:"+data+"长度为:"+data.length);
			//获取密钥
			String key = messageService.getSystemParam("WXCLIENT.DES.KEY");
			logInfo("加密解密的公共密钥为:%s", key);
			//解密data
			encryptor = new Encryptor(key);
			logInfo("DES的信息:", encryptor.toString());
			String deString="";
			try {
				deString = encryptor.decryptString(data);
				logInfo("解密后的数据为:%s", deString);
			} catch (Exception e) {
				logInfo("解密数据异常:%s",e);
				deString="{}";//防止json转换异常
			}finally{
				if(deString==null||"".equals(deString)||!deString.startsWith("{")){
					deString="{}";
				}
			}
			//解析JSON为Map对象
			map = jsonToMap(deString);
			reqMsg.putAllAttr(map);
		}else{
			try {
				logInfo("非加密数据直接解析");
				byte[] dataBytes = getDataFromInputStream(request);
				String dataStr = new String(dataBytes,"UTF-8");
				if(dataStr==null||"".equals(dataStr)||!dataStr.startsWith("{")){
					dataStr="{}";
				}
				logInfo("解析出的数据为：%s", dataStr);
				logInfo("开始将数据解析为json格式......");
				map = this.jsonToMap(dataStr);
				reqMsg.putAllAttr(map);
			} catch (UnsupportedEncodingException e) {
				logInfo("将参数转换为JSON格式异常");
				e.printStackTrace();
			}
		}
		for (String key : map.keySet()) {
			request.setAttribute(key, map.get(key));
		}
		return reqMsg;
	}

	/** ********************************************
	 * method name   : getDataFromInputStream 
	 * description   : 从包体中获取请求数据
	 * @return       : String
	 * @param        : @param request
	 * @param        : @return
	 * modified      : panxingwu ,  2012-3-14  下午2:59:12
	 * @see          : 
	 * ********************************************/      
	protected static byte[] getDataFromInputStream(HttpServletRequest request){
		InputStream in = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			request.setCharacterEncoding("UTF-8");
			in = request.getInputStream();
		    int b=0;
		    while((b = in.read())!=-1){
		    	 baos.write(b);
		    }
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				if(in!=null) in.close();
				if(baos!=null) baos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return baos.toByteArray();
	}
	
	/**
	 * ********************************************
	 * method name   : getRpid4DES 
	 * description   : 根据merid生成rpid
	 * @return       : String
	 * modified      : panxingwu ,  2012-4-18  下午4:18:00
	 * @see          : 
	 * *******************************************
	 */
	protected String getRpid4DES(HttpServletRequest request,boolean isDES){
		RequestMsg reqMsg = decryptData(request,isDES);
		String rpid = "";
		String merId = ObjectUtil.trim(reqMsg.getStr(DataDict.MER_REQ_MERID));
		if(!isVer3()){
			merId = ObjectUtil.trim(reqMsg.getStr(DataDict.MER_REQ_MERID_V2));
		}
		if(merId==null||"".equals(merId)) merId="9999";
		if(ObjectUtil.isNotEmpty(merId)){
			String sid = request.getSession().getId();
			String prefix = "0";
			if(!ObjectUtil.isEmpty(sid) && sid.length() > 1){
				prefix = sid.substring(0, 1);
			}
			SequenceUtil su = SequenceUtil.getInstance();
			rpid = "W" + ObjectUtil.trim(merId) + prefix + SequenceUtil.formatSequence(su.getSequence4File("hfWebBusi.rpid"), 10);
			SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID, rpid);
		}
		return rpid;
	}
	
	/**
	 * ********************************************
	 * 根据retCode获得配置文件中对应的retMsg
	 * *******************************************
	 */
	protected String getRetMessage(ResponseMsg responseMsg) {
		String retMsg = "";
		String retCode = responseMsg.getRetCode();
		String retCodeBussi = responseMsg.getRetCodeBussi();
		if(!ObjectUtil.isEmpty(retCodeBussi)){
			retMsg = messageService.getMessage(retCodeBussi);
		}
		if(ObjectUtil.isEmpty(retMsg)){
			retMsg = messageService.getMessage(retCode);
		}
		return retMsg;
	};
	/** ********************************************
	 * method name   : updateRequestMsg 
	 * description   : 将json格式的字符串转换成map
	 * @return       : Map
	 * modified      : panxingwu ,  2012-3-10  下午5:23:02
	 * @see          : 
	 * ********************************************/      
	protected static Map<String,Object> jsonToMap(String data){
		//转换为json格式
		JSONObject jsonObject=JSONObject.fromObject(data); 
		Iterator keys=jsonObject.keys(); 
		Map<String,Object> map = new HashMap<String,Object>();
		while(keys.hasNext()){
			String key=(String) keys.next(); 
			String value=jsonObject.get(key).toString(); 
			if(value.startsWith("{")&&value.endsWith("}")){ 
				map.put(key, jsonToMap(value)); 
			}else{ 
				map.put(key, value); 
			} 
		}
		return map;
	}

	@Override
	protected void initMap4Log(HttpServletRequest request,Map<String, Object> mpspMap) {
		super.initMap4Log(request, mpspMap);
		Enumeration requestNames = request.getAttributeNames();
		while(requestNames.hasMoreElements()){
			String name = (String) requestNames.nextElement();
			String value = String.valueOf(request.getAttribute(name));
			mpspMap.put(name,value);
		}
	}
	
}
