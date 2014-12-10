package com.umpay.hfweb.action.wx;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.WxOrderBaseAction;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.ObjectUtil;


/** ******************  类说明  *********************
 * class       :  WxQueryOrderAction
 * @author     :  panxingwu
 * @version    :  1.0  
 * description :  获取订单信息
 * @see        :                        
 * ************************************************/   
public class WxQueryOrderAction extends WxOrderBaseAction {
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
		//查询订单信息
		logInfo("查询订单信息...");
		MpspMessage queryOrderResp = restService.queryMerOrder(requestMsg.getStr(DataDict.MER_REQ_MERID),requestMsg.getStr(DataDict.MER_REQ_MERDATE),requestMsg.getStr(DataDict.MER_REQ_ORDERID));
		
		//查询用户手机信息（归属地）20130328 panxingwu add(服务器按地区主动给用户推送短信需求新增)
		String mobileid = (String) queryOrderResp.getStr(HFBusiDict.MOBILEID);
		MpspMessage mobileidInfRs=new MpspMessage();
		if(mobileid!=null&&!"".equals(mobileid)){
			logInfo("从订单表获取到手机号，调用资源层查询用户手机归属地");
			mobileidInfRs = restService.queryMobileidInf(mobileid);
			mobileidInfRs.put(HFBusiDict.MOBILEID,mobileid);
			queryOrderResp.putAll(mobileidInfRs);
		}
		if(!queryOrderResp.isRetCode0000()){
			logInfo("WxQueryOrder Result Failed[RetCode]:%s:%s", queryOrderResp.getRetCode(), "查询订单失败");
			respMap.putAll(mobileidInfRs);
			respMap.put(HFBusiDict.MOBILEID, mobileid);
			respMap.setRetCode(queryOrderResp.getRetCode());
			responseEorr(requestMsg,respMap);
			return;
		}
		responseSuccess(queryOrderResp,requestMsg,respMap);
	}
	private void responseSuccess(MpspMessage queryOrderResp,
			RequestMsg requestMsg, ResponseMsg respMap) {
		respMap.setRetCode0000();
		String retCode = DataDict.SUCCESS_RET_CODE; 
		String orderState  = ObjectUtil.trim(queryOrderResp.getStr(HFBusiDict.ORDERSTATE));
		String sign = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_SIGN));
		String reserved = queryOrderResp.getStr(HFBusiDict.RESERVED);
		String payRetCode = "";
		if(reserved.length()>=4){
			String payCode = queryOrderResp.getStr(HFBusiDict.RESERVED).substring(4);
			if("1840".equals(payCode)||"1931".equals(payCode)||"1901".equals(payCode)||"1911".equals(payCode)){
				retCode="1403";//余额不足返回码，供话付宝使用，话付宝接收到此码后跳转到充值话费页面
			}
			payRetCode="8602"+ObjectUtil.trim(queryOrderResp.getStr(HFBusiDict.RESERVED).substring(4));
		}
		logInfo("支付状态码:%s", payRetCode);
		Map <String,String> map = new HashMap<String,String>();
		map.put(HFBusiDict.RETCODE,retCode);
		map.put(HFBusiDict.RETMSG, messageService.getMessage(payRetCode));//返回支付信息（支付失败的详细信息）
		map.put(DataDict.MER_REQ_VERSION, "1.0");
		map.put("orderState", orderState);
		map.put(DataDict.MER_REQ_SIGN, sign);
		map.put("mobileid",queryOrderResp.getStr("mobileid")==null?"":queryOrderResp.getStr("mobileid"));
		map.put(HFBusiDict.PROVCODE, queryOrderResp.getStr(HFBusiDict.PROVCODE)==null?"":queryOrderResp.getStr(HFBusiDict.PROVCODE));
		map.put("provname", queryOrderResp.getStr("provname")==null?"":queryOrderResp.getStr("provname"));
		map.put(HFBusiDict.AREACODE, queryOrderResp.getStr(HFBusiDict.AREACODE)==null?"":queryOrderResp.getStr(HFBusiDict.AREACODE));
		map.put("areaname", queryOrderResp.getStr("areaname")==null?"":queryOrderResp.getStr("areaname"));
		String jsonStr = JSONObject.fromObject(map).toString();
		//加密
		byte[] data=null;
		try {
			logInfo("返回客户端的数据:%s", jsonStr);
			data = encryptor.encyptString(jsonStr);
		} catch (Exception e) {
			logInfo("加密返回信息的时候出现异常:", e);
		}
		respMap.setDirectByteMsg(data);
		
	}
	@Override
	protected void responseEorr(RequestMsg requestMsg, ResponseMsg respMap) {
		
		String retCode = respMap.getStr(DataDict.RET_CODE);
		String retMsg = getRetMessage(respMap);
		String sign = requestMsg.getStr(DataDict.MER_REQ_SIGN);
		
		Map<String,String> map = new HashMap<String,String>();
		map.put(DataDict.RET_CODE, retCode);
		map.put(DataDict.RET_MSG, retMsg);
		map.put(DataDict.MER_REQ_VERSION, "1.0");
		map.put(HFBusiDict.PROVCODE, respMap.getStr(HFBusiDict.PROVCODE)==null?"":respMap.getStr(HFBusiDict.PROVCODE));
		map.put("mobileid",respMap.getStr("mobileid")==null?"":respMap.getStr("mobileid"));
		map.put("provname", respMap.getStr("provname")==null?"":respMap.getStr("provname"));
		map.put(HFBusiDict.AREACODE, respMap.getStr(HFBusiDict.AREACODE)==null?"":respMap.getStr(HFBusiDict.AREACODE));
		map.put("areaname", respMap.getStr("areaname")==null?"":respMap.getStr("areaname"));
		map.put("orderState", "");
		map.put(DataDict.MER_REQ_SIGN, sign);
		String jsonStr = JSONObject.fromObject(map).toString();
		//加密
		byte[] data=null;
		try {
			logInfo("返回客户端的数据:%s", jsonStr);
			data = encryptor.encyptString(jsonStr);
		} catch (Exception e) {
			logInfo("加密返回信息的时候出现异常:", e);
		}
		respMap.setDirectByteMsg(data);

	}
	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_WX_WXDDCX;
	}

}
