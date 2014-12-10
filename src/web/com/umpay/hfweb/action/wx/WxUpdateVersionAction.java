package com.umpay.hfweb.action.wx;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import com.umpay.hfweb.action.base.WxOrderBaseAction;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.ObjectUtil;


/** ******************  类说明  *********************
 * class       :  WxUpdateVersionAction
 * @author     :  panxingwu
 * @version    :  1.0  
 * description :  手机客户端更新版本
 * @see        :                        
 * ************************************************/   
public class WxUpdateVersionAction  extends WxOrderBaseAction {
	private static final String FAT_APK="0";//富客户端
	private static final String THIN_APK="1";//瘦客户端
	private static final String SDK="2";//SDK
	
	@Override
	protected void processBussiness(RequestMsg requestMsg, ResponseMsg respMap) {
		requestMsg.setFunCode(getFunCode());
		//检查请求参数
		logInfo("校验请求参数");
		MpspMessage checkParamResp = checkService.doCheck(requestMsg);
		if(!checkParamResp.isRetCode0000()){
			respMap.setRetCode(checkParamResp.getRetCode());
			logInfo("ParamCheck Result Failed[RetCode]:%s:%s",respMap.getRetCode(),(String)checkParamResp.get("message"));
			responseSuccess_N(respMap);
			return;
		}
		logInfo("参数校验通过");
		long versionCode = 1;
		try {
			versionCode = Long.valueOf(requestMsg.getStr("versionCode"));
		} catch (NumberFormatException e) {
			logError(e);
			versionCode = 1;
		}
		String clienttype = ObjectUtil.trim(requestMsg.getStr("clientType"));//客户端类型 0:胖    1:瘦     2:SDK 
		String function = requestMsg.getStr("function");//功能码（客户端强制升级需求新增） 20130328 潘兴武
		String clientName = requestMsg.getStr("clientName");
		int type = Integer.parseInt(ObjectUtil.trim(requestMsg.getStr("type")));//更新方式：0更新富客户端，1更新瘦客户端，2直接下载富客户端，3直接下载瘦客户端
		
		//验证是否需要限制升级
		boolean isLimit = updateLimit(clienttype,versionCode,type);
		if(isLimit){
			logInfo("升级被限制，不提示客户端升级!");
			//被限制升级，不提示客户端升级
			responseSuccess_N(respMap);
			return;
		}
		
		//潘兴武 改造于20130524（将配置从配置文件移到数据库中） 
		boolean forceFlag = false;//强制升级标识（为了兼容之前发布的客户端版本，顾判断此字段是否有值）
		if(function!=null&&!function.equals("")){
			logInfo("传入强制更新标识，需要验证是否强制更新");
			forceFlag = true;
		}
		String clientType="";//在配置库中 0：富客户端  1：瘦客户端
		if(type==0||type==2){
			clientType="0";
		}else{
			clientType="1";
		}
		MpspMessage configRs =  restService.queryClientConf(clientName,clientType);
		if(!configRs.isRetCode0000()){
			logInfo("查询配置信息失败,请检查是否有可用版本！");
			responseSuccess_N(respMap);
			return;
		}
		long usingVersion = Long.valueOf(configRs.getStr("versioncode"));//当前线上正在使用的版本
		long forceVersion = Long.valueOf(configRs.getStr("forceversion").replace(".", ""));//需要强制升级的最低版本
		String conf_clientName = configRs.getStr("clientName");//配置的客户端名称
		String url = configRs.getStr("downloadurl");//下载地址
		String description = configRs.getStr("description");//升级描述信息
		logInfo("获取配置成功,当前最新版本：%s,强制升级最低版本：%s,客户端名称:%s,下载地址:%s,描述信息:%s",usingVersion,forceVersion,conf_clientName,url,description);
		
		if(type==0||type==1){
			if(!"".equals(url)&&(versionCode<usingVersion)&&(clientName.equals(conf_clientName))){
				logInfo("有新版本，可以更新，最新客户端版本：version=%s", usingVersion);
				respMap.put("upFlag","1");
				if(forceFlag){
					logInfo("需要强制升级");
					if(versionCode<=forceVersion) respMap.put("upFlag", "2");
				}
				responseSuccess_y(url,description,respMap);
			}else{
				logInfo("版本不需要更新，传入版本：%s,当前最新版本：%s",versionCode,usingVersion);
				responseSuccess_N(respMap);
			}
		}else{
			logInfo("直接下载客户端");
			if("".equals(url)){
				responseSuccess_N(respMap);//下载地址未配置，不提供下载
			}else{
				respMap.put("upFlag","1");
				responseSuccess_y(url,description,respMap);
			}
		}
	}
	@Override
	protected void responseEorr(RequestMsg requestMsg, ResponseMsg respMap) {
	}
	private void responseSuccess_y(String url,String description,ResponseMsg respMap){
		respMap.setRetCode0000();
		Map<String,String> map = new HashMap<String,String>();
		map.put("retCode",respMap.getStr("upFlag"));
		map.put("updateInfo", description);
		map.put("URL", url);
		String jsonStr = JSONObject.fromObject(map).toString();
		byte[] by = null;
		try {
			logInfo("返回客户端的信息：%s", jsonStr);
			by = jsonStr.getBytes("UTF-8");
		} catch (Exception e) {
			logInfo("返回数据时出现异常:%s", e);
		}
		respMap.setDirectByteMsg(by);
	}
	private void responseSuccess_N(ResponseMsg respMap){
		respMap.setRetCode0000();
		Map<String,String> map = new HashMap<String,String>();
		map.put("retCode","0");
		map.put("updateInfo", "");
		map.put("URL", "");
		String jsonStr = JSONObject.fromObject(map).toString();
		byte[] by = null;
		try {
			logInfo("返回客户端的信息：%s", jsonStr);
			by = jsonStr.getBytes("UTF-8");
		} catch (Exception e) {
			logInfo("返回数据时出现异常:%s", e);
		}
		respMap.setDirectByteMsg(by);
	}
	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_WX_JCGX;
	}
	/**
	 * ********************************************
	 * method name   : createTransRpid 
	 * modified      : panxingwu 
	 * description   : 重载此方法，客户端检查更新的请求数据不需要加密
	 * *******************************************
	 */
	@Override
	protected String createTransRpid(HttpServletRequest request) {
		return getRpid4DES(request, false);
	}
	/** ********************************************
	 * method name   : updateLimit 
	 * description   : 升级限制规则
	 * @return       : boolean
	 * @param        : @param type
	 * @param        : @param merid
	 * @param        : @return
	 * modified      : panxingwu ,  2013-8-8  下午4:05:49
	 * @see          : 
	 * ********************************************/      
	private boolean updateLimit(String clientType,long version,int type){
		if("".equals(clientType)) return false;//如果没有传入客户端类型，则不限制（为了兼容已经发布的版本）
		if(type==2||type==3){
			if(clientType.equals(SDK)){
				//SDK升级到胖版的限制
				String limitVersion = ObjectUtil.trim(messageService.getSystemParam("clientUpdate.limitVersion"));
				if(!"".equals(limitVersion)){
					long limit = Long.valueOf(limitVersion);
					if(version>=limit){
						logInfo("SDK升级到富客户端被限制!");
						return true;
					}
				}
			}else if(clientType.equals(THIN_APK)){
				//瘦版升级到胖版的限制
				String limitVersion = ObjectUtil.trim(messageService.getSystemParam("thinUpdate4Fat.limitVersion"));
				if(!"".equals(limitVersion)){
					long limit = Long.valueOf(limitVersion);
					if(version>=limit){
						logInfo("瘦客户端升级到富客户端被限制!");
						return true;
					}
				}
			}
		}else{
			if(clientType.equals(FAT_APK)){
				//低胖版升级到高胖版的限制
				String limitVersion = ObjectUtil.trim(messageService.getSystemParam("fatUpdate.limitVersion"));
				if(!"".equals(limitVersion)){
					long limit = Long.valueOf(limitVersion);
					if(version>=limit){
						logInfo("富客户端升级被限制");
						return true;
					}
				}
			}else if(clientType.equals(THIN_APK)){
				//低瘦版升级到高瘦版的限制
				String limitVersion = ObjectUtil.trim(messageService.getSystemParam("thinUpdate.limitVersion"));
				if(!"".equals(limitVersion)){
					long limit = Long.valueOf(limitVersion);
					if(version>=limit){
						logInfo("瘦客户端升级被限制!");
						return true;
					}
				}
			}
		}
		return false;
	}
}
