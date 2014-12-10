package com.umpay.hfweb.action.wx;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;
import javax.servlet.http.HttpServletRequest;

import com.bs.mpsp.util.StringUtil;
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
 * description :  手机客户端装机量统计
 * @see        :                        
 * ************************************************/   
public class ClientUserAction extends WxOrderBaseAction {

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
		// 入无线临时订单表
		String isLxSDKZf = StringUtil.trim(requestMsg.getStr("isLxSDKZf"));
		if ("true".equals(isLxSDKZf)) {
			// 只有是离线sdk的才入无线临时订单表
			MpspMessage createWxOrderResp = restService.createSDkLxWxOrder(requestMsg);
			if(!createWxOrderResp.isRetCode0000()){
				logInfo("createSDkLxWxOrder Result Failed[RetCode]:%s:%s", createWxOrderResp.getRetCode(), "保存SDK离线无线接入商户订单失败");
				respMap.setRetCode(createWxOrderResp.getRetCode());
				responseEorr(requestMsg,respMap);
//				return;
			}
			logInfo("createSDkLxWxOrder Result Success[RetCode]:0000:SDK离线保存用户信息至订单表成功");
		}

		MpspMessage addRes = restService.recordClientUser(requestMsg);
		//调用资源层查询用户手机号码和手机归属地信息   20130401  panxignwu  
		logInfo("调用资源层查询用户手机号和归属地.....");
		MpspMessage wxSegInfRs = new MpspMessage();
		wxSegInfRs=restService.getWxUserSeg(requestMsg);
		if(!addRes.isRetCode0000()){
			logInfo("wxStatistics Result Failed[RetCode]:%s:%s", addRes.getRetCode(), "终端信息统计失败");
			respMap.setRetCode("0000");
			respMap.putAll(wxSegInfRs);
			responseEorr(requestMsg,respMap);
			return;
		}
		logInfo("wxStatistics Result Success[RetCode]:0000:统计成功");
		
		responseSuccess(addRes,respMap,wxSegInfRs);
	}
	@Override
	protected void responseEorr(RequestMsg requestMsg, ResponseMsg respMap) {
		String retCode = ObjectUtil.trim(respMap.getRetCode());
		String retMsg = getRetMessage(respMap);
		Map<String,String> map = new HashMap<String,String>();
		map.put(HFBusiDict.RETCODE, retCode);
		map.put(HFBusiDict.RETMSG, retMsg);
		map.put("cid", requestMsg.getStr(DataDict.WX_REQ_CLIENTID));
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
		map.put("cid", mpsp.getStr(HFBusiDict.CLIENTID));
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
		return DataDict.FUNCODE_WX_ZJL;
	}
	@Override
	protected String createTransRpid(HttpServletRequest request) {
		return getRpid4DES(request,false);//非加密数据
	}
}
