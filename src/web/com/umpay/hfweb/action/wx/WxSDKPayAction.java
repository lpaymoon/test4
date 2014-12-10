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
 * class       :  WxSDKPayAction
 * @author     :  xuwei
 * @version    :  1.0  
 * description :  12580 SDK客户端接入商户支付
 * @see        :                        
 * ************************************************/   
public class WxSDKPayAction extends WxOrderBaseAction {
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
		
		
		String IMEI = ObjectUtil.trim(requestMsg.getStr(HFBusiDict.IMEI));//手机串号
		String IMSI = ObjectUtil.trim(requestMsg.getStr(HFBusiDict.IMSI));//手机SIM 识别码
		String sign = ObjectUtil.trim(requestMsg.getStr(HFBusiDict.SIGN));
		String sdkSign = ObjectUtil.trim(requestMsg.getStr("sdkSign"));
		String sdkSignStr = messageService.getSystemParam("sdkSign");
		logInfo("验证客户端身份...");
	    if(!sdkSign.equals(sdkSignStr)){
	        	logInfo("客户端身份验证不通过");
	        	respMap.setRetCode("1322");
				responseEorr(requestMsg,respMap);
				return;
	     }
	    logInfo("客户端身份验证通过");
		//1-9查询手机绑定关系
		MpspMessage queryWxBindResp = restService.queryWxSDKBind(IMEI,IMSI);
		if(!queryWxBindResp.isRetCode0000()){
			logInfo("queryWxBindResp Result Failed[RetCode]:%s:%s", queryWxBindResp.getRetCode(), "查询绑定关系失败");
			respMap.setRetCode(queryWxBindResp.getRetCode());
			responseEorr(requestMsg,respMap);
			return;
		}
		logInfo("queryWxBindResp Result Success[RetCode]:0000:查询手机绑定关系表成功, mobileId="+queryWxBindResp.getStr(HFBusiDict.MOBILEID));
		requestMsg.put(HFBusiDict.MOBILEID, queryWxBindResp.getStr(HFBusiDict.MOBILEID));
		String publicKey=ObjectUtil.trim(queryWxBindResp.getStr(HFBusiDict.PUBLICKEY));
		if("".equals(publicKey)){
			//公钥不存在的返回码采用和解密异常一致的返回码，因为这两种情况客户端都需要重新同步公钥，故采用统一返回码，方便客户端处理。
			logInfo("公钥不存在");
			respMap.setRetCode("1323");
			responseEorr(requestMsg,respMap);
			return;
		}
		try {
			String isCheck = messageService.getSystemParam("isSdkCheck");
			if("1".equals(isCheck)){//只有开通了验签功能才走验签步骤
				//从字符串获取公钥
				String pubKey = RSAUtils.getPublicKey(publicKey);
				//使用公钥解密数据
		        byte[] decodedData = RSAUtils.decryptByPublicKey(getEncodedData(sign), pubKey);
		        String target = new String(decodedData);
		        String unSignStr = getUnsignStr(requestMsg);
		        logInfo("原串:%s,签名串:%s",unSignStr,target);
		        if(!unSignStr.equals(target)){
		        	logInfo("请求数据非法，验签失败");
		        	respMap.setRetCode("1321");
					responseEorr(requestMsg,respMap);
					return;
		        }
			}
			logInfo("代码签名验证通过");
		} catch (Exception e) {
			e.printStackTrace();
			logInfo("异常:%s",e.getMessage());
			respMap.setRetCode("1323");
			responseEorr(requestMsg,respMap);
			return;
		}
		//调用支付流程
		MpspMessage SDKpayResp = tradeService.SDKPay(requestMsg);
		if(!SDKpayResp.isRetCode0000()&&!SDKpayResp.getRetCode().equals("86011571")){
			logInfo("SDKpayResp Result Failed[RetCode]:%s:%s", SDKpayResp.getRetCode(), "支付失败");
			respMap.setRetCode(SDKpayResp.getRetCode());
			respMap.put(HFBusiDict.RETMSG, SDKpayResp.get(HFBusiDict.RETMSG));
			responseEorr(requestMsg,respMap);
			return;
		}
		logInfo("SDKpayResp Result Failed[RetCode]:%s:%s", SDKpayResp.getRetCode(), "支付成功");
		//下发短信
		smsService.pushPayOkSms(SDKpayResp.getWrappedMap());
		//20140220 liujilong start 去除限额短信提示,注掉以下代码
		//		String merId=SDKpayResp.getStr(HFBusiDict.MERID);	
		//		String mobileId=SDKpayResp.getStr(HFBusiDict.MOBILEID);
		//		String send_promotion=ObjectUtil.trim(SDKpayResp.getStr("send_mt_promotion"));
		//		if(send_promotion.equals("true")){ //下发限额提示短信
		//			String smsContent=SDKpayResp.getStr("promotion_msg");
		//			 smsService.pushSms(merId, mobileId, smsContent);
		//		}
		//20140220 liujilong end
		
		responseSuccess(SDKpayResp,requestMsg,respMap);
		
	//	
		
	}
	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_WX_SDKPAY;
	}
	private void responseSuccess(MpspMessage SDKpayResp,RequestMsg requestMsg, ResponseMsg respMap){
		respMap.setRetCode0000();
		
		Map <String,String> map = new HashMap<String,String>();
		map.put(DataDict.MER_REQ_MERID, SDKpayResp.getStr(HFBusiDict.MERID));
		map.put(DataDict.MER_REQ_GOODSID, SDKpayResp.getStr(HFBusiDict.GOODSID));
		map.put(DataDict.MER_REQ_PORDERID, SDKpayResp.getStr(HFBusiDict.PORDERID));
		map.put(DataDict.MER_REQ_MOBILEID, SDKpayResp.getStr(HFBusiDict.MOBILEID));
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
		map.put(HFBusiDict.RETCODE, retCode);
		map.put(HFBusiDict.RETMSG, retMsg);
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
	
	/**
     * 校验系统类型
     * @return
     */
    private boolean checkIsTest(){
    	boolean rnt = false;
		// 0:生产系统 其他：测试系统
		String type = messageService.getSystemParam("SystemType");
		if (!type.equals("0")) {
			rnt = true;
		}
		return rnt;
    }
    
    private boolean orderLimit(){
    	return false;
    }
	@Override
	protected String createTransRpid(HttpServletRequest request) {
		return getRpid4DES(request,false);//非加密数据
	}
	
	private String getUnsignStr(RequestMsg requestMsg){
		StringBuffer unSignStr = new StringBuffer();
		String IMEI = ObjectUtil.trim(requestMsg.getStr(HFBusiDict.IMEI));
		String IMSI = ObjectUtil.trim(requestMsg.getStr(HFBusiDict.IMSI));
		String porderId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_PORDERID));
		if(!"".equals(IMEI))
			unSignStr.append(IMEI);
		if(!"".equals(IMSI))
			unSignStr.append("&"+IMSI);
		if(!"".equals(porderId))
			unSignStr.append("&"+porderId);
			unSignStr.append("&rgz");
		return RSAUtils.getMD5String(unSignStr.toString());
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
//		String temp = signStr.substring(1);
//		String sign = temp.substring(0, temp.length()-1);
//		String[] data = sign.split(",");
//		logInfo("数组为:%s", data.toString());
//		byte[] bytes = new byte[data.length];
//		for (int i = 0; i < data.length; i++) {
//			bytes[i] = Byte.valueOf(data[i]);
//		}
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
