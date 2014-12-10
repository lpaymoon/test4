package com.umpay.hfweb.action.wx;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.ehcache.Element;
import net.sf.json.JSONObject;

import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.WxOrderBaseAction;
import com.umpay.hfweb.cache.AbstractCacheFactory;
import com.umpay.hfweb.cache.CommonEcc;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;


/** ******************  类说明  *********************
 * class       :  WxSmsRandomKeyAction
 * @author     :  panxingwu
 * @version    :  1.0  
 * description :  动态验证码下发action
 * @see        :                        
 * ************************************************/   
public class WxSmsRandomKeyAction extends WxOrderBaseAction{
	private static final String Sended_SMS_Count= "intSendedCount";
	@Override
	protected void processBussiness(RequestMsg requestMsg, ResponseMsg respMap) {
		requestMsg.setFunCode(getFunCode());
		//1.检查请求参数
		logInfo("校验请求参数");
		MpspMessage checkParamResp = checkService.doCheck(requestMsg);
		if(!checkParamResp.isRetCode0000()){
			respMap.setRetCode(checkParamResp.getRetCode());
			logInfo("ParamCheck Result Failed[RetCode]:%s:%s",respMap.getRetCode(),(String)checkParamResp.get("message"));
			responseEorr(requestMsg,respMap);
			return;
		}
		
		String mobileid = requestMsg.getStr(DataDict.MER_REQ_MOBILEID);
		
		//2.做缓存，用于限制获取动态码的次数
		CommonEcc ecc = (CommonEcc)AbstractCacheFactory.getInstance().getCacheClient("randomVerify");
		String key = mobileid;
		Element cache = ecc.getElementByDefaultMap(key,Sended_SMS_Count,new Integer(0));
		Map<String,Object> cacheInfo = (Map<String,Object>)cache.getValue();
		Integer intSendedCount = (Integer)cacheInfo.get(Sended_SMS_Count);
		Integer maxNum = Integer.parseInt(messageService.getSystemParam("WXSMSRANDOM.MAX"));
		logInfo("缓存次数为:%s,配置最大参数为%s", intSendedCount,maxNum);
		//3.调用资源层生成动态验证码
		if(intSendedCount<maxNum){
			MpspMessage message = restService.getRandomVerifyTimes(mobileid);
			if("0000".equals(message.getStr(HFBusiDict.RETCODE))){
				int verifytimes = Integer.parseInt(message.getStr(HFBusiDict.VERIFYTIMES)==null?"0":message.getStr(HFBusiDict.VERIFYTIMES));
				String lastModtime = message.getStr(HFBusiDict.MODTIME);
				SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date date = null;
				try {
					date= sf.parse(lastModtime);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				if(verifytimes!=0&&verifytimes%maxNum==0){
					logInfo("上次修改时间:%s", date.toString());
					logInfo("系统当前时间减去上次修改时间:%s", System.currentTimeMillis()-date.getTime());
					if(System.currentTimeMillis()-date.getTime()<24*60*60*1000){
						cacheInfo.put(Sended_SMS_Count,maxNum);
						logInfo("超过24小时内请求获取验证码的次数限制");
						respMap.setRetCode("86011401");
						responseEorr(requestMsg,respMap);//超过动态验证的次数
						return;
					}
				}
			}else if(!"86001108".equals(message.getStr(HFBusiDict.RETCODE))){
				logInfo("获取动态验证码失败");
				respMap.setRetCode(message.getStr(HFBusiDict.RETCODE));
				responseEorr(requestMsg,respMap);
				return;
			}
			MpspMessage resultMap = restService.getSmsRandomKey(mobileid);
			logInfo("资源层返回信息:%s", resultMap.getWrappedMap());
			String retCode = resultMap.getStr(HFBusiDict.RETCODE);
			if(!"0000".equals(retCode)){
				logInfo("获取动态验证码失败");
				respMap.setRetCode(retCode);
				responseEorr(requestMsg,respMap);
				return;
			}
			intSendedCount++;
			cacheInfo.put(Sended_SMS_Count,intSendedCount);
			//4.给用户手机下发动态验证码	
			String smsContent = "您本次使用的动态密码是："+resultMap.getStr(HFBusiDict.RANDOMKEY)+"【话付宝交易记录查询】";
			smsService.pushSms("3121", mobileid, smsContent);
			responseSuccess(respMap);
		}else{
			logInfo("超过24小时内请求获取验证码的次数限制");
			respMap.setRetCode("86011401");
			responseEorr(requestMsg,respMap);//超过动态验证的次数
		}
	}
	
	@Override
	protected void responseEorr(RequestMsg requestMsg, ResponseMsg respMap) {
		Map <String,String> map = new HashMap<String,String>();
		map.put("retCode", respMap.getRetCode());
		map.put("retMsg",getRetMessage(respMap));
		String jsonStr = JSONObject.fromObject(map).toString();
		//加密
		byte[] data=null;
		try {
			logInfo("返回客户端的数据为:%s", jsonStr);
			data = jsonStr.getBytes("UTF-8");
		} catch (Exception e) {
			logInfo("加密返回信息时出现异常:%s", e);
		}
		respMap.setDirectByteMsg(data);
		
	}
	private void responseSuccess(ResponseMsg respMap){
		respMap.setRetCode0000();
		Map <String,String> map = new HashMap<String,String>();
		map.put("retCode", DataDict.SUCCESS_RET_CODE);
		map.put("retMsg",getRetMessage(respMap));
		String jsonStr = JSONObject.fromObject(map).toString();
		//加密
		byte[] data=null;
		try {
			logInfo("返回客户端的数据为:%s", jsonStr);
			data = jsonStr.getBytes("UTF-8");
		} catch (Exception e) {
			logInfo("加密返回信息时出现异常:%s", e);
		}
		respMap.setDirectByteMsg(data);
	}
	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_WX_SMSVERIFY;
	}
	@Override
	protected String createTransRpid(HttpServletRequest request) {
		return getRpid4DES(request, false);
	}
}
