package com.umpay.hfweb.action.wx;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.sun.jmx.snmp.Timestamp;
import com.umpay.hfweb.util.DateUtil;

import net.sf.json.JSONObject;

import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.WxOrderBaseAction;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.ObjectUtil;


/** ******************  类说明  *********************
 * class       :  WxMwUserLtdAction
 * @author     :  xuwei
 * @version    :  1.0  
 * description :  手机号梦网月交易额统计action
 * @see        :                        
 * ************************************************/   
public class WxMwUserLtdAction extends WxOrderBaseAction {

	@Override
	protected void processBussiness(RequestMsg requestMsg, ResponseMsg respMap) {
		requestMsg.setFunCode(getFunCode());
		//检查请求参数
		logInfo("校验请求参数");
		MpspMessage checkParamResp = checkService.doCheck(requestMsg);
		if(!checkParamResp.isRetCode0000()){
			respMap.setRetCode(checkParamResp.getRetCode());
			logInfo("ParamCheck Result Failed[RetCode]:%s:%s",respMap.getRetCode(),(String)checkParamResp.get("message"));
			responseEorr(requestMsg,respMap);
			return;
		}
	
		//调用资源层查询手机号码全网月交易累计信息
		logInfo("调用资源层查询手机号月全网交易累计.....");
//		MpspMessage querInf=restService.getMwUserLtd(requestMsg);
		MpspMessage querInf=restService.getHfUserLtd(requestMsg);
		
		if(!querInf.isRetCode0000()){
			logInfo("QueryUserinf Result Failed[RetCode]:%s:%s", querInf.getRetCode(), "查询用户信息失败");
			respMap.setRetCode(querInf.getRetCode());
			responseEorr(requestMsg,respMap);
			return;
		}
		logInfo("查询用户信息成功");
	
		responseSuccess(querInf,respMap);
	}
	private void responseSuccess(MpspMessage message,ResponseMsg respMap){
		String modtime  =  ObjectUtil.trim(message.getStr(HFBusiDict.MODTIME));
		String daypayed =  ObjectUtil.trim(message.getStr(HFBusiDict.DAYPAYED));//日消费累计
		String monthpayed = ObjectUtil.trim(message.getStr(HFBusiDict.MONTHPAYED));//月消费累计
		
		String xemodtime  =  ObjectUtil.trim(message.getStr("xemodtime"));
		String xedaypayed =  ObjectUtil.trim(message.getStr("xedaypayed"));//省网日消费累计
		String xemonthpayed = ObjectUtil.trim(message.getStr("xemonthpayed"));//省网月消费累计
		respMap.setRetCode0000();
		//根据modtime判断是否是当月的消费累计
		if(!modtime.equals("")){
			SimpleDateFormat fmn = new SimpleDateFormat ("yyyyMM"); 
			SimpleDateFormat fmm = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss"); 
		
			Calendar calendar = Calendar.getInstance();
			String curMonth=fmn.format(calendar.getTime());//获取当期月份
			Date modTime;
			try {
				modTime = fmm.parse(modtime);
			} catch (ParseException e) {
				logInfo("modtime 格式转换异常:%s", modtime);
				modTime=calendar.getTime();//转换异常，取默认当天时间
			} 
		    String modMonth=fmn.format(modTime);//格式化交易记录更新月份201406
		    logInfo("month %s", modMonth+"-"+curMonth);
		    if(!modMonth.equals(curMonth)){//当月还无交易累计
		    	daypayed="0";
		    	monthpayed="0";
		    }
		}
		
		if(!xemodtime.equals("")){
			SimpleDateFormat fmn = new SimpleDateFormat ("yyyyMM"); 
			SimpleDateFormat fmm = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss"); 
		
			Calendar calendar = Calendar.getInstance();
			String curMonth=fmn.format(calendar.getTime());//获取当期月份
			Date modTime;
			try {
				modTime = fmm.parse(xemodtime);
			} catch (ParseException e) {
				logInfo("xemodtime 格式转换异常:%s", xemodtime);
				modTime=calendar.getTime();//转换异常，取默认当天时间
			} 
		    String modMonth=fmn.format(modTime);//格式化交易记录更新月份201406
		    logInfo("xemonth %s", modMonth+"-"+curMonth);
		    if(!modMonth.equals(curMonth)){//当月还无交易累计
		    	xedaypayed="0";
		    	xemonthpayed="0";
		    }
		}
		
		Map <String,String> map = new HashMap<String,String>();
		map.put("retCode", DataDict.SUCCESS_RET_CODE);
		map.put("retMsg", "查询成功!");
		map.put("daypayed",daypayed);
		map.put("monthpayed",monthpayed);
		map.put("xedaypayed",xedaypayed);
		map.put("xemonthpayed",xemonthpayed);

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
	protected void responseEorr(RequestMsg requestMsg, ResponseMsg respMap) {
		String retCode = ObjectUtil.trim(respMap.getRetCode());
		String retMsg = getRetMessage(respMap);
		Map <String,String> map = new HashMap<String,String>();
		map.put(HFBusiDict.RETCODE, retCode);
		map.put(HFBusiDict.RETMSG, retMsg);
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
		return DataDict.FUNCODE_WX_MWUSERLTD;
	}
	@Override
	protected String createTransRpid(HttpServletRequest request) {
		return getRpid4DES(request,false);//非加密数据
	}

}
