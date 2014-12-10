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
import com.umpay.hfweb.util.*;


/** ******************  类说明  *********************
 * class       :  R8PayAction
 * @author     :  panxingwu
 * @version    :  1.0  
 * description :  R8支付流程
 * @see        :                        
 * ************************************************/   
public class R8PayAction extends WxOrderBaseAction {
	@Override
	protected void processBussiness(RequestMsg requestMsg, ResponseMsg respMap) {
		requestMsg.setFunCode(getFunCode());
		// 1-检查请求参数
		logInfo("检验请求参数");
		MpspMessage checkParamResp = checkService.doCheck(requestMsg);
		if(!checkParamResp.isRetCode0000()){
			respMap.setRetCode(checkParamResp.getRetCode());
			logInfo("ParamCheck Result Failed[RetCode]:%s:%s",respMap.getRetCode(),(String)checkParamResp.get("message"));
			responseEorr(requestMsg,respMap);
			return;
		}
		logInfo("参数校验通过");
		
		// 2-调用支付流程
		MpspMessage SDKpayResp = tradeService.SDKPay(requestMsg);
		if(!SDKpayResp.isRetCode0000()&&!SDKpayResp.getRetCode().equals("86011571")){
			logInfo("SDKpayResp Result Failed[RetCode]:%s:%s", SDKpayResp.getRetCode(), "支付失败");
			respMap.setRetCode(SDKpayResp.getRetCode());
			respMap.put(HFBusiDict.RETMSG, SDKpayResp.get(HFBusiDict.RETMSG));
			responseEorr(requestMsg,respMap);
			return;
		}
		logInfo("SDKpayResp Result Failed[RetCode]:%s:%s", SDKpayResp.getRetCode(), "支付成功");
		// 3-下发短信
		smsService.pushPayOkSms(SDKpayResp.getWrappedMap());
		String merId=SDKpayResp.getStr(HFBusiDict.MERID);	
		String mobileId=SDKpayResp.getStr(HFBusiDict.MOBILEID);
		String send_promotion=ObjectUtil.trim(SDKpayResp.getStr("send_mt_promotion"));
		if(send_promotion.equals("true")){ //下发限额提示短信
			String smsContent=SDKpayResp.getStr("promotion_msg");
			 smsService.pushSms(merId, mobileId, smsContent);
		}
		responseSuccess(SDKpayResp,requestMsg,respMap);
	}
	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_WX_R8PAY;
	}
	private void responseSuccess(MpspMessage SDKpayResp,RequestMsg requestMsg, ResponseMsg respMap){
		respMap.setRetCode0000();
		
		Map <String,String> map = new HashMap<String,String>();
		map.put("retCode", DataDict.SUCCESS_RET_CODE);
		map.put("retMsg", SDKpayResp.getStr(DataDict.RET_MSG));

		String jsonStr = JSONObject.fromObject(map).toString();
		//加密
		byte[] data=null;
		try {
			logInfo("返回客户端的数据:%s",jsonStr);
			//data = encryptor.encyptString(jsonStr);
			data = jsonStr.getBytes("UTF-8");
		} catch (Exception e) {
			logInfo("加密返回信息出现异常:%s", e);
		}
		respMap.setDirectByteMsg(data);
	}

	@Override
	protected void responseEorr(RequestMsg requestMsg, ResponseMsg respMap) {
		
		String retCode = ObjectUtil.trim(respMap.getRetCode());
		String retMsg =  getRetMessage(respMap);		
		Map <String,String> map = new HashMap<String,String>();
		map.put("retCode", retCode);
		map.put("retMsg", retMsg);
		String jsonStr = JSONObject.fromObject(map).toString();
		//加密
		byte[] data=null;
		try {
			logInfo("返回客户端的数据为:%s", jsonStr);
		//	data = encryptor.encyptString(jsonStr);
			data = jsonStr.getBytes("UTF-8");
		} catch (Exception e) {
			logInfo("加密返回信息时出现异常:%s", e);
		}
		respMap.setDirectByteMsg(data);
	}
	@Override
	protected String createTransRpid(HttpServletRequest request) {
		return getRpid4DES(request,false);//非加密数据
	}
	
	public byte[] getEncodedData(String signStr){
		//base64解码
		signStr = signStr.replace("\\", "");
		byte[] bytes=null;
		try {
			bytes = Base64Utils.decode(signStr);
		} catch (Exception e) {
			logInfo("base64解码失败");
			e.printStackTrace();
		}
		logInfo("解析出来的数组:%s",bytes);
		return bytes;
	}
//	public static void main(String[] args) throws Exception {
//		String pubKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC03ms1fGeEu43ed6NKGRsxqmefRg9kWwoxRVxCa7sbB7+gBSxdX/cha0/DWbHavxYqjexSbuNNuOOCZYUBcVZIxaIa9jsUC3+myYAss8zxdsaCp1YZSdGLFOYqTTEt7GwIXbHn2EXW/FwE1JOYzg44I2GC5SpF/nSpNFXg0vTprwIDAQAB";
//		String str = "XMlWZNGPD3inZv977udd3MYynEEtL/wkEL5pAWMW82yRYRdgTisZMRgmN6xgEZgwszXTvhPAVTsjzEqavJ8EizyWYIDk+MhnGvtZyGK5nNpruejPBKrSVldUaIcPe4vGOO60xkfUxub0MBVvfebhCEUMyNkjksqjKfhz71e88ic=";
//		byte[] bytes = Base64Utils.decode(str);
//        byte[] decodedData = RSAUtils.decryptByPublicKey(bytes, pubKey);
//        String target = new String(decodedData);
//        System.out.println(target);
//        System.out.println("8f7d480786bd2874d9916ab41348999d");
//	}
}
