package com.umpay.hfweb.action.wx;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.WxOrderBaseAction;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.ObjectUtil;


/** ******************  类说明  *********************
 * class       :  WxStatisticsAction
 * @author     :  panxingwu
 * @version    :  1.0  
 * description :  手机客户端装机量统计action
 * @see        :                        
 * ************************************************/   
public class WxUserAction extends WxOrderBaseAction {

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
		MpspMessage wxtjMpsp = restService.addWxUser(requestMsg);
		
		String IMEI = ObjectUtil.trim(requestMsg.getStr(HFBusiDict.IMEI));
		MpspMessage wxSegInfRs = new MpspMessage();
		//调用资源层查询用户手机号码和手机归属地信息   20130401  panxignwu  
		logInfo("调用资源层查询用户手机号和归属地.....");
		if(IMEI!=null&&!"".equals(IMEI)){
			wxSegInfRs=restService.getWxUserSeg(requestMsg);
		}
		
		if(!wxtjMpsp.isRetCode0000()){
			logInfo("wxStatistics Result Failed[RetCode]:%s:%s", wxtjMpsp.getRetCode(), "无线装机量统计失败");
			respMap.setRetCode(wxtjMpsp.getRetCode());
			respMap.putAll(wxSegInfRs);
			responseEorr(requestMsg,respMap);
			return;
		}
		logInfo("wxStatistics Result Success[RetCode]:0000:统计成功");
		responseSuccess(wxtjMpsp,respMap,wxSegInfRs);
	}
	@Override
	protected void responseEorr(RequestMsg requestMsg, ResponseMsg respMap) {
		String retCode = ObjectUtil.trim(respMap.getRetCode());
		String retMsg = getRetMessage(respMap);
		Map<String,String> map = new HashMap<String,String>();
		map.put(HFBusiDict.RETCODE, retCode);
		map.put(HFBusiDict.RETMSG, retMsg);
		map.put("mobileid", respMap.getStr(HFBusiDict.MOBILEID)==null?"":respMap.getStr(HFBusiDict.MOBILEID));
		map.put("provcode", respMap.getStr(HFBusiDict.PROVCODE)==null?"":respMap.getStr(HFBusiDict.PROVCODE));
		map.put("areacode", respMap.getStr(HFBusiDict.AREACODE)==null?"":respMap.getStr(HFBusiDict.AREACODE));
		map.put("provname", respMap.getStr(HFBusiDict.PROVNAME)==null?"":respMap.getStr(HFBusiDict.PROVNAME));
		map.put("areaname", respMap.getStr("areaname")==null?"":respMap.getStr("areaname"));
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
	private void responseSuccess(MpspMessage mpsp,ResponseMsg respMap, MpspMessage wxSegInfRs){
		respMap.setRetCode0000();
		Map<String,String> map = new HashMap<String,String>();
		map.put("retCode", DataDict.SUCCESS_RET_CODE);
		map.put("retMsg", mpsp.getStr(HFBusiDict.RETMSG));
		map.put("mobileid", wxSegInfRs.getStr(HFBusiDict.MOBILEID)==null?"":wxSegInfRs.getStr(HFBusiDict.MOBILEID));
		map.put("provcode", wxSegInfRs.getStr(HFBusiDict.PROVCODE)==null?"":wxSegInfRs.getStr(HFBusiDict.PROVCODE));
		map.put("areacode", wxSegInfRs.getStr(HFBusiDict.AREACODE)==null?"":wxSegInfRs.getStr(HFBusiDict.AREACODE));
		map.put("provname", wxSegInfRs.getStr(HFBusiDict.PROVNAME)==null?"":wxSegInfRs.getStr(HFBusiDict.PROVNAME));
		map.put("areaname", wxSegInfRs.getStr("areaname")==null?"":wxSegInfRs.getStr("areaname"));
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
		return DataDict.FUNCODE_WX_YHXX;
	}
	@Override
	protected String createTransRpid(HttpServletRequest request) {
		return getRpid4DES(request,false);//非加密数据
	}

}
