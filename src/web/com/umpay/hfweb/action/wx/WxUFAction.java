package com.umpay.hfweb.action.wx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;
import com.umpay.api.common.ReqData;
import com.umpay.api.exception.ReqDataException;
import com.umpay.api.paygate.v40.Mer2Plat_v40;
import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.WxOrderBaseAction;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.ObjectUtil;
import com.umpay.hfweb.util.SessionThreadLocal;


/** ******************  类说明  *********************
 * class       :  WxOrderToUFAction
 * @author     :  panxingwu
 * @version    :  1.0  
 * description :  向U付下单
 * ************************************************/   
public class WxUFAction extends WxOrderBaseAction {
	@Override
	protected void processBussiness(RequestMsg requestMsg, ResponseMsg respMap) {
		requestMsg.setFunCode(getFunCode());
		// 1-检查请求参数
		logInfo("检验请求参数");
		MpspMessage checkParamResp = checkService.doCheck(requestMsg);
		Map<String,String> resultMap = new HashMap<String,String>();//U付返回结果
		if(!checkParamResp.isRetCode0000()){
			respMap.setRetCode(checkParamResp.getRetCode());
			logInfo("ParamCheck Result Failed[RetCode]:%s:%s",respMap.getRetCode(),(String)checkParamResp.get("message"));
			responseEorr(requestMsg,respMap);
			return;
		}
		
		//2-向U付服务器下订单
		try {
			Map<String,String> map = new HashMap<String,String>();
			//必输字段
		    map.put("order_id",requestMsg.getStr(DataDict.MER_REQ_ORDERID));   
		    map.put("mer_date",requestMsg.getStr(DataDict.MER_REQ_MERDATE));   
		    map.put("amount",requestMsg.getStr(DataDict.MER_REQ_AMOUNT)); 
		    map.put("mer_id",requestMsg.getStr(DataDict.MER_REQ_MERID));  
		    map.put("goods_id",requestMsg.getStr(DataDict.MER_REQ_GOODSID));
		    map.put("service","pay_req");   
		    map.put("charset","UTF-8");   
		    map.put("sign_type","RSA");   
		    map.put("version","4.0");  
		    map.put("amt_type","RMB");   
		    //可选字段
		    map.put("goods_inf",requestMsg.getStr(DataDict.MER_REQ_GOODSINF)==null?"":requestMsg.getStr(DataDict.MER_REQ_GOODSINF));   
		    map.put("media_id",requestMsg.getStr(DataDict.MER_REQ_MOBILEID)==null?"":requestMsg.getStr(DataDict.MER_REQ_MOBILEID)); 
		    map.put("mer_priv",requestMsg.getStr(DataDict.MER_REQ_MERPRIV)==null?"":requestMsg.getStr(DataDict.MER_REQ_MERPRIV));
		    map.put("expand",requestMsg.getStr(DataDict.MER_REQ_EXPAND)==null?"":requestMsg.getStr(DataDict.MER_REQ_EXPAND));
		    map.put("notify_url",messageService.getSystemParam("WXNOTIFYURL"));//后台通知地址
		    ReqData reqDate = Mer2Plat_v40.ReqDataByGet(map);//组装数据和生成签名
			String reqUrl = reqDate.getUrl();//直接向U付请求此URL即可完成下单
			logInfo("向U付下单的URL：%s",reqUrl);

			//访问reqUrl执行下单操作
			BufferedReader reader=null;
			HttpURLConnection connection=null;
			String sTotalString = "";//U付服务器返回信息（HTML格式）
			try {
				URL url = new URL(reqUrl);
		        connection = (HttpURLConnection)url.openConnection(); 
		        connection.setConnectTimeout(5 * 60 * 1000);
		        connection.setReadTimeout(5 * 60 * 1000);
		        connection.setRequestMethod("GET");
	            connection.connect(); 
	            int rCode = connection.getResponseCode();
	            if(rCode == HttpURLConnection.HTTP_OK){
		            InputStream urlStream = connection.getInputStream(); 
		            reader = new BufferedReader(new InputStreamReader(urlStream,"utf-8")); 
		            String sCurrentLine = ""; 
		            while((sCurrentLine = reader.readLine()) != null) 
		            { 
		                sTotalString+=sCurrentLine; 
		            }
		            sTotalString = sTotalString.substring(sTotalString.indexOf("CONTENT=\"")+9, sTotalString.lastIndexOf("\">"));
		            String[] result = sTotalString.split("&");
		            resultMap = arrayToMap(result);
	            }else{
	            	resultMap.put("ret_code", "9999");
	            	logInfo("请求U付服务器失败，HTTP响应码：%s", rCode);
	            }
			}catch (Exception e) {
				e.printStackTrace();
			}finally{
				try {
					if(reader!=null){
						reader.close();
					}
						connection.disconnect();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} catch (ReqDataException e) {
				e.printStackTrace();
			}
			logInfo("U付服务器返回信息为:%s",resultMap);
			String retCode = resultMap.get("ret_code");
			if(!"0000".equals(retCode)){
				if(retCode==null||retCode.equals("")){
					retCode="9999";
				}
				respMap.setRetCode(retCode);
				responseEorr(requestMsg,respMap);
				return;
			}
			//3-通信账户订单保存
			Map<String,String> reqMap = new HashMap<String,String>();
			Calendar calendar = Calendar.getInstance();
			long now = calendar.getTimeInMillis();
			long future = now + 24*60*60*1000;
			reqMap.put(HFBusiDict.ORDERID,requestMsg.getStr(DataDict.MER_REQ_ORDERID));   
			reqMap.put(HFBusiDict.ORDERDATE,requestMsg.getStr(DataDict.MER_REQ_MERDATE));   
			reqMap.put(HFBusiDict.AMOUNT,requestMsg.getStr(DataDict.MER_REQ_AMOUNT)); 
			reqMap.put(HFBusiDict.MERID,requestMsg.getStr(DataDict.MER_REQ_MERID));  
			reqMap.put(HFBusiDict.GOODSID,requestMsg.getStr(DataDict.MER_REQ_GOODSID));
			reqMap.put(HFBusiDict.RPID,SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
			reqMap.put(HFBusiDict.ORIGAMOUNT,requestMsg.getStr(DataDict.MER_REQ_AMOUNT));
			reqMap.put(HFBusiDict.BUSINESSTYPE, "0206");//U付支付流程
			reqMap.put(HFBusiDict.MOBILEID, "13800000000");
			reqMap.put(HFBusiDict.EXPIRETIME,new Timestamp(future).toString());
			reqMap.put(HFBusiDict.BANKID,"XE010000");
			reqMap.put(HFBusiDict.ACCESSTYPE,"W");
			reqMap.put(HFBusiDict.VERIFYCODE,"8");
			reqMap.put(HFBusiDict.AMTTYPE,"01");// 01：人民币 02：移动话费 03：移动积分
			reqMap.put(HFBusiDict.VERSION,"3.0");
			reqMap.put(HFBusiDict.BUSINESSTYPE, DataDict.BUSI_WX_UF);//业务区分，U付支付(0206)
			restService.createOrder(reqMap);
			//4-返回请求结果
			responseSuccess(resultMap,respMap);
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
			data = encryptor.encyptString(jsonStr);
		} catch (Exception e) {
			logInfo("加密返回信息时出现异常:%s", e);
		}
		respMap.setDirectByteMsg(data);
	}
	
	private void responseSuccess(Map<String,String> resultMap,ResponseMsg respMap){
		respMap.setRetCode0000();
		Map <String,String> map = new HashMap<String,String>();
		map.put("retMsg", resultMap.get("ret_msg"));
		map.put("trade_state", resultMap.get("trade_state"));
		map.put("trade_no", resultMap.get("trade_no"));
		map.put("retCode", "0000");
		String jsonStr = JSONObject.fromObject(map).toString();
		//加密
		byte[] data=null;
		try {
			logInfo("返回客户端的数据为:%s", jsonStr);
			data = encryptor.encyptString(jsonStr);
		} catch (Exception e) {
			logInfo("加密返回信息时出现异常:%s", e);
		}
		respMap.setDirectByteMsg(data);
	}
	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_WXUFZF;
	}
	/**
	 * ********************************************
	 * method name   : arrayToMap 
	 * description   : 数组转换成Map
	 * @return       : Map<String,String>
	 * @param        : @param array
	 * @param        : @return
	 * modified      : panxingwu ,  2012-10-24  下午2:32:18
	 * @see          : 
	 * *******************************************
	 */
	private Map<String,String> arrayToMap(String[] array){
		Map<String,String> map = new HashMap<String,String>();
		for (int i = 0; i < array.length; i++) {
			String[] arr = array[i].split("=");
			map.put(arr[0], arr[1]);
		}
		return map;
	}
}
