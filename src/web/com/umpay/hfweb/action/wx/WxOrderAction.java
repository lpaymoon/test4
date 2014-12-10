package com.umpay.hfweb.action.wx;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bs.mpsp.util.StringUtil;
import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.WxOrderBaseAction;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.DateUtil;
import com.umpay.hfweb.util.ObjectUtil;


/** ******************  类说明  *********************
 * class       :  WxOrderAction
 * @author     :  panxingwu
 * @version    :  1.0  
 * description :  无线接入商户下单
 * @see        :                        
 * ************************************************/   
public class WxOrderAction extends WxOrderBaseAction {
	private static final Log log = LogFactory.getLog(WxOrderAction.class);
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
		
		//参数转换：由于客户端做了大的升级，传入的参数有调整，为了兼容版本，需要做相关参数转化
		String versionCode =  ObjectUtil.trim(requestMsg.get(DataDict.WX_REQ_VERSIONCODE));
		String versionName =  ObjectUtil.trim(requestMsg.get(DataDict.WX_REQ_VERSIONNAME));
		if(!versionCode.contains(".")){
			versionCode = versionName;
		}
		
		//获取支付客户端选择配置
		String mer = requestMsg.get(DataDict.MER_REQ_MERID).toString();
		String apkMers = ObjectUtil.trim(messageService.getSystemParam("apkMers"));
		String payChnl = "SDK";//默认使用SDK支付
		if(apkMers.contains(mer)||apkMers.contains("ALL"))payChnl="APK";
		respMap.put("payChnl", payChnl);
		
		//客户端ID校验
		String clientId = requestMsg.getStr(DataDict.WX_REQ_CLIENTID);
		String platType = requestMsg.getStr(DataDict.WX_REQ_PLATTYPE);
		logInfo("versionCode=%s",versionCode);
		if("2".equals(platType)&&getIntVersion(versionCode)==10112){
			if(clientId==null||clientId.equals("")){
				logInfo("客户端ID为空，交易终止");
				respMap.setRetCode("1405");
				responseEorr(requestMsg,respMap);
				return;
			}
			
			if (!clientId.contains("null")) {
				MpspMessage checkCIDRs = restService.getClientUser(requestMsg);
				if(!checkCIDRs.isRetCode0000()){
					logInfo("查询客户端信息失败，交易终止");
					respMap.setRetCode("1406");
					responseEorr(requestMsg,respMap);
					return;
				}
			}
		}
		
//		String IMEI = ObjectUtil.trim(requestMsg.getStr(HFBusiDict.IMEI));//手机串号
		long goodsPrice = Long.valueOf(requestMsg.getStr(DataDict.MER_REQ_AMOUNT));
		String merDate = requestMsg.getStr(DataDict.MER_REQ_MERDATE);
//		//分商户限制IMEI
//		String IMEILimitMer = ObjectUtil.trim(messageService.getSystemParam("IMEI.limitMer"));
//		String regex = ObjectUtil.trim(messageService.getSystemParam("IMEI.limitSeg"));
//		if(IMEILimitMer.contains(mer)){
//			if(!IMEI.matches(regex)){
//				logInfo("下单失败,[IMEI=%s]非法,商户：%s", IMEI,mer);
//				respMap.setRetCode("1301");
//				responseEorr(requestMsg,respMap);
//				return;
//			}
//		}
				
		// 2-下单时间校验
		if(!DateUtil.verifyOrderDate(merDate)){
			respMap.setRetCode("1310");
			logInfo("请求日期超出当前日期前后一天！ merDate=%s", merDate);
			responseEorr(requestMsg,respMap);
			return;
		}
		
//		//4-下单次数限制(防刷交易)
//		String maxTimes = messageService.getSystemParam("wxOrderMaxNum");
//		String reqIP = ObjectUtil.trim(requestMsg.get(DataDict.NET_CLIENTIP));
//		
//		if(!(maxTimes==null||"".equals(maxTimes))){//只有配置了下单次数才做以下校验
//			logInfo("请求数据:%s",requestMsg.getWrappedMap());
//			logInfo("下单次数校验开始，IP:%s,IMEI:%s", reqIP,IMEI);
//			//4-1 IP限制
//			Map<String,Object> IpLimit = CommonUtil.timesLimit("wxOrderTimesCache", reqIP, Long.valueOf(maxTimes));
//			boolean ipFlag = (Boolean) IpLimit.get("flag");
//			//4-2 IMEI限制
//			Map<String,Object> result = CommonUtil.timesLimit("wxOrderTimesCache", IMEI, Long.valueOf(maxTimes));
//			boolean flag = (Boolean) result.get("flag");
//			if(!flag||!ipFlag){
//				logInfo("下单次数校验未通过");
//				respMap.setRetCode("1402");
//				responseEorr(requestMsg,respMap);
//				return;
//			}
//		} 
		
		//5-交易鉴权
		MpspMessage checkMerGoodsResp = restService.queryMerGoodsInfo(requestMsg.get(DataDict.MER_REQ_MERID).toString(),requestMsg.get(DataDict.MER_REQ_GOODSID).toString());
		if(!checkMerGoodsResp.isRetCode0000()){
			logInfo("queryMerGoodsInfo Result Failed[RetCode]:%s:%s", checkMerGoodsResp.getRetCode(), "交易鉴权失败[获取商户商品信息失败]");
			respMap.setRetCode(checkMerGoodsResp.getRetCode());
			responseEorr(requestMsg,respMap);
			return;
		}
		logInfo("交易鉴权成功");
		
		String pricemode = checkMerGoodsResp.getStr(HFBusiDict.PRICEMODE);
		if("0".equals(pricemode)){
			logInfo("定价商品,校验金额开始");
			//如果价格模式是固定价格则获取商品银行信息
			String merid = checkMerGoodsResp.getStr(HFBusiDict.MERID);
			String goodsid = checkMerGoodsResp.getStr(HFBusiDict.GOODSID);
			MpspMessage goodsBankResp = restService.getGoodsBank(merid, goodsid);
			if(!goodsBankResp.isRetCode0000()){
				logInfo("queryMerGoodsInfo Result Failed[RetCode]:%s:%s", goodsBankResp.getRetCode(), "获取商品银行失败");
				respMap.setRetCode(goodsBankResp.getRetCode());
				responseEorr(requestMsg,respMap);
				return;
			}
			logInfo("获取商品银行成功");
			List<Map<String, Object>> rs = (List<Map<String, Object>>) goodsBankResp.get(HFBusiDict.GOODSBANKS);
			Map<String,Object> map = rs.get(0);
			long amount = Long.valueOf(requestMsg.getStr(DataDict.MER_REQ_AMOUNT));
			long price = Long.valueOf(map.get(HFBusiDict.AMOUNT).toString());//制定价格
			if(price!=amount){
				logInfo("定价商品，金额不符，制定价格为：%s,输入价格为:%s",price,amount);
				respMap.setRetCode("1404");
				responseEorr(requestMsg,respMap);
				return;
			}
			logInfo("金额校验通过");
			goodsPrice = price;
		}
		
		//加入配置，看是否需要进行快捷支付的判断  并且不支持以前发出去的sdk，所以plattpye为4才可以快捷支付
		String doQuickPayFlag = StringUtil.trim(messageService.getSystemParam("doQuickPayFlag"));// true打开 false关闭
		String supportQuickPayList = StringUtil.trim(messageService.getSystemParam("supportQuickPayList"));//支持快捷支付的plattpye列表
		boolean isQuickPay = false;
		// 0612综合支付小额验证码流程，不允许走快捷支付流程
		if ("true".equals(doQuickPayFlag) && supportQuickPayList.contains(","+platType+",") &&!"0612".equals(requestMsg.getStr(HFBusiDict.BUSINESSTYPE))) {
			/**
			 * 加入快捷支付判断的逻辑：
			 * 如果IMEI,IMSI,ICCID都不为空的话，查询无线订单表，查询24小时之内的交易，如果有成功的，则直接进行快捷支付流程。
			 */
			String IMEI = StringUtil.trim(requestMsg.getStr(DataDict.WX_REQ_IMEI));
			String IMSI = StringUtil.trim(requestMsg.getStr(DataDict.WX_REQ_IMSI));
			String ICCID = StringUtil.trim(requestMsg.getStr(DataDict.WX_REQ_ICCID));
			if (!"".equals(IMEI) && !"".equals(IMSI) && !"".equals(ICCID)) {
				MpspMessage queryWxOrderResp = restService.queryWxOrder(requestMsg);
				//  order by modtime desc fetch first 1 row only
				if (!queryWxOrderResp.isRetCode0000()) {
					logInfo("queryWxOrderResp Result Failed[RetCode]:%s:%s", queryWxOrderResp.getRetCode(), "根据三码查询无线订单表失败");
//					respMap.setRetCode(queryWxOrderResp.getRetCode());
//					responseEorr(requestMsg,respMap);
				} else {
					logInfo("queryWxOrderResp Result Succ[RetCode]:%s:%s", queryWxOrderResp.getRetCode(), "根据三码查询无线订单表成功");
					// 如果查询成功，才进行下一步的操作
					Map<String,Object> rs= (Map<String, Object>) queryWxOrderResp.get("qryOrder");
					if (rs == null) {
						log.info("根据三码查询无线订单表的结果为空！不进行快捷支付。");
					} else {
						// 看是否有最近24小时内支付成功且三码都有的交易
						Timestamp modTime = (Timestamp) (rs.get("modtime"));//2014-07-17 18:35:20.496488
			            // 毫秒ms
						long curTime = (new Date()).getTime();
			            long diff = curTime - modTime.getTime();
			            log.info("modTime.getTime():" + modTime.getTime() +";curTime.getTime():"+curTime +";diff:"+diff);
			            long diffHours = diff / (60 * 60 * 1000);// 0-23 ok
			            if ((diffHours < 24) && ((Integer) (rs).get("orderstate") == 2)){
				            String orderid = (String) (rs).get("orderid");//订单号
				            String orderdate = (String) (rs).get("orderdate");//订单日期
				            String merid = (String) (rs).get("merid");//商户号
				            log.info("根据当前下单请求的IMEI,IMSI,ICCID查到在24小时内，最后一笔交易是成功的，merid:"+merid+
				            		";orderid:"+orderid+";orderdate:"+orderdate+"。相差在：" +(diffHours+1) + "小时内。");
				            //取出来payPhoneNum看是否合法 然后如果有传入来的获取到的手机号，就对比一下，如果没有就不对比
				            String payPhoneNum = (String) (rs).get("payphonenum");//支付成功的手机号码
				            String getPhoneNum = StringUtil.trim(requestMsg.getStr("mobileNo"));//sdk传过来的获取到的手机号，有可能为空
				            if (payPhoneNum.matches("^1\\d{10}$")) {
								if (!"".equals(getPhoneNum) && !getPhoneNum.equals(payPhoneNum)) {
						            isQuickPay = false;
								} else {
						            isQuickPay = true;
								}
								requestMsg.put("payPhoneNum", payPhoneNum);
							}
						}
//			            log.info("根据当前下单请求的IMEI,IMSI,ICCID查到在24小时内，最后一笔交易是失败的！不能使用快捷支付");
					}
				}
			}
		}
		
		//6-保存订单表
		String maxTimes = messageService.getSystemParam("wxOrderMaxNum");
		requestMsg.put(HFBusiDict.MAX, maxTimes);
		requestMsg.put(HFBusiDict.EXPIRETIME, checkMerGoodsResp.get(HFBusiDict.EXPIRETIME));
		MpspMessage createWxOrderResp = restService.createWxOrder(requestMsg);
		if(!createWxOrderResp.isRetCode0000()){
			logInfo("createWxOrder Result Failed[RetCode]:%s:%s", createWxOrderResp.getRetCode(), "保存无线接入商户订单失败");
			respMap.setRetCode(createWxOrderResp.getRetCode());
			responseEorr(requestMsg,respMap);
			return;
		}
		logInfo("WxOrderSave Result Success[RetCode]:0000:下单成功");
		requestMsg.put(HFBusiDict.PORDERID,createWxOrderResp.get(HFBusiDict.PORDERID));
		
		if ("true".equals(doQuickPayFlag) && isQuickPay == true) {
	        log.info("调用快捷支付流程进行支付。");
	        requestMsg.put(HFBusiDict.BUSINESSTYPE, DataDict.BUSI_WX_KJZF);
			MpspMessage quickPayResp = tradeService.sdkQuickPay(requestMsg);
			//0000 1599算成功，其他的都算失败  但是1599不发短信
			if(quickPayResp.isRetCode0000()){
				logInfo("快捷支付成功[RetCode]:%s。",quickPayResp.getRetCode());
				// 下发短信
				smsService.pushPayOkSms(quickPayResp.getWrappedMap());
				responseKJZFSuccess(checkMerGoodsResp,requestMsg,respMap,goodsPrice);
			} else if("86011599".equals(quickPayResp.getRetCode())){
				logInfo("快捷支付成功[RetCode]异步充值:%s。",quickPayResp.getRetCode());
		        responseKJZFSuccess(checkMerGoodsResp,requestMsg,respMap,goodsPrice);
			} else {
				logInfo("快捷支付失败[RetCode]:%s。",quickPayResp.getRetCode());
				respMap.setRetCode(quickPayResp.getRetCode());
		        responseKJZFFail(checkMerGoodsResp,requestMsg,respMap,goodsPrice);
			}
		} else {
			//如果没有快捷支付，就用其他的支付方式
			responseSuccess(checkMerGoodsResp,requestMsg,respMap,goodsPrice);
		}
		
	}
	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_WX_WXXD;
	}
	private void responseSuccess(MpspMessage mpspMessage,RequestMsg requestMsg, ResponseMsg respMap,long price){
		respMap.setRetCode0000();
		String version = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_VERSION));
		String sign = ObjectUtil.trim(requestMsg.getStr(HFBusiDict.SIGN));
		String amount = String.valueOf(price);
		String merName = ObjectUtil.trim(mpspMessage.getStr(HFBusiDict.MERNAME));
		String goodsName = ObjectUtil.trim(mpspMessage.getStr(HFBusiDict.GOODSNAME));
		String cusphone = ObjectUtil.trim(mpspMessage.getStr(HFBusiDict.CUSPHONE));//客服电话
		String servmonth = ObjectUtil.trim(mpspMessage.getStr(HFBusiDict.SERVMONTH));//服务月份
		String servtype = ObjectUtil.trim(mpspMessage.getStr(HFBusiDict.SERVTYPE));//服务类型
		String payChnl = ObjectUtil.trim(respMap.getStr("payChnl"));//支付方式，APK和SDK
		String versionCode=ObjectUtil.trim(requestMsg.getStr("versionCode"));
		String clientVersion = messageService.getSystemParam("R4DriectPay.clientVersion");
//		String R4MerList = messageService.getSystemParam("R4DriectPay.MerList");
//		String xiaoAmt = messageService.getSystemParam("R4DriectPay.Amt.Xiao");
		String merId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MERID));
		boolean flag1=checkSDKversion(versionCode,clientVersion);//检查客户端版本是否支持直接支付
		
//		boolean flag=false;	
//		if(R4MerList.contains(merId)||R4MerList.contains("ALL")){
//			flag=true;
//		}
//		int xiao = 200;
//		if(!ObjectUtil.isEmpty(xiaoAmt)){
//			try{
//				 xiao = Integer.parseInt(xiaoAmt);
//			}catch(Exception e){
//				 xiao = 200;
//			}
//		}
		long amt = Long.valueOf(StringUtil.trim((String)amount));
	
		if(servmonth==null||"".equals(servmonth)) servmonth="0";
		
		Map <String,String> map = new HashMap<String,String>();
		map.put(DataDict.MER_REQ_VERSION, version);
		map.put(DataDict.MER_REQ_SIGN,sign);
		map.put(DataDict.MER_REQ_AMOUNT,amount);
		map.put(DataDict.MER_REQ_MERNAME,merName);
		map.put("goodsName", goodsName);
		map.put("cusPhone", cusphone);
		map.put("servMonth", servmonth);
		map.put("servType", servtype);
		map.put("retCode", DataDict.SUCCESS_RET_CODE);
		map.put("retMsg", mpspMessage.getStr(HFBusiDict.RETMSG));
		map.put("payChnl", payChnl);
	//	logInfo("******flag1="+flag1+"--flag="+flag+"--xiao="+xiao);

		String R4DirectRule = "";
		//多维度判断是否需要二次确认 add by chenwei 20140630
		String R4DirectRuleKey = StringUtil.trim(messageService.getSystemParam("R4DirectRuleKey"));
		String[] R4DirectRuleKeys = R4DirectRuleKey.split(";");
		Map<String, String> map4Rule = new HashMap<String, String>();
		String iccid = StringUtil.trim(requestMsg.getStr("iccid"));
		String provCode = "*";
		if (!"".equals(iccid) && iccid.length()>10) {
			provCode = iccid.substring(8, 10);
		}
		provCode = matchProv(provCode);
		map4Rule.put(HFBusiDict.PROVCODE, provCode);
		map4Rule.put(HFBusiDict.MERID, merId);
		try {
			//根据省份、商户获取最优匹配规则对应的限制条件
			R4DirectRule = findCondition(R4DirectRuleKeys, "R4DirectRule", map4Rule);
			logInfo("匹配后获得的限制规则为 R4DirectRule["+R4DirectRule+"]");
		} catch (Exception e) {
			logInfo("匹配下单频率控制规则时异常e：", e);
		}
		if(flag1 == true && amt <= Integer.valueOf(R4DirectRule)){
			map.put("isDirectPay", "1");//直接支付
		}else{
			map.put("isDirectPay", "0");//需要二次确认
		}
		
		//判断是否为测试系统
		boolean test = checkIsTest();
		String smsFrom = "";
		String smsPrex = messageService.getSystemParam("smsPrex");
		if (test){
			smsFrom += "4";
		}
//		String merId = (String)requestMsg.get(DataDict.MER_REQ_MERID);
		String mers = messageService.getSystemParam("special.called.mer.list","");
		String mo2Called = smsPrex+smsFrom+"2"+requestMsg.getStr(HFBusiDict.PORDERID);
//		if(mers.toUpperCase().contains("ALL") || mers.contains(merId)){
//			mo2Called = smsPrex+smsFrom+"1"+requestMsg.getStr(HFBusiDict.PORDERID);
//		}
		String mo1Called = smsPrex+smsFrom+"1015"+mpspMessage.getStr(HFBusiDict.MERID);
		map.put("mo1Called", mo1Called);
		map.put("mo1Msg", requestMsg.getStr(DataDict.MER_REQ_GOODSID)+"#"+requestMsg.getStr(DataDict.MER_REQ_ORDERID)+"#"+requestMsg.getStr(DataDict.MER_REQ_MERDATE)+"#"+requestMsg.getStr(HFBusiDict.PORDERID));
		map.put("mo2Called", mo2Called);
		map.put("mo2Msg", "8");
		String jsonStr = JSONObject.fromObject(map).toString();
		//加密
		byte[] data=null;
		try {
			logInfo("返回客户端的数据:%s",jsonStr);
			data = encryptor.encyptString(jsonStr);
		} catch (Exception e) {
			logInfo("加密返回信息出现异常:%s", e);
		}
		respMap.setDirectByteMsg(data);
	}

	private void responseKJZFSuccess(MpspMessage mpspMessage,RequestMsg requestMsg, ResponseMsg respMap,long price){
		respMap.setRetCode0000();
		Map <String,String> map = new HashMap<String,String>();
		map.put("retCode", DataDict.SUCCESS_RET_CODE);
		//加入判断是否为快捷支付的标志
		map.put("KJZFflag", "0");
		String jsonStr = JSONObject.fromObject(map).toString();
		//加密
		byte[] data=null;
		try {
			logInfo("返回客户端的数据:%s",jsonStr);
			data = encryptor.encyptString(jsonStr);
		} catch (Exception e) {
			logInfo("加密返回信息出现异常:%s", e);
		}
		respMap.setDirectByteMsg(data);
	}

	private void responseKJZFFail(MpspMessage mpspMessage,RequestMsg requestMsg, ResponseMsg respMap,long price){
		String merId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MERID));
		String goodsId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_GOODSID));
		String orderId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_ORDERID));
		String merDate = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MERDATE));
		String payChnl = ObjectUtil.trim(respMap.getStr("payChnl"));//支付方式，APK和SDK
		String retCode = ObjectUtil.trim(respMap.getRetCode());
		String retMsg =  getRetMessage(respMap);
		
		Map <String,String> map = new HashMap<String,String>();
		map.put(HFBusiDict.MERID, merId);
		map.put(HFBusiDict.GOODSID,goodsId);
		map.put(HFBusiDict.ORDERID, orderId);
		map.put(DataDict.MER_REQ_MERDATE,merDate);
		map.put(HFBusiDict.RETCODE, retCode);
		map.put(HFBusiDict.RETMSG, retMsg);
		map.put("payChnl", payChnl);
		//加入判断是否为快捷支付的标志
		map.put("KJZFflag", "1");
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

	private String matchProv(String provCode) {
		String resProvCode = "*";
		if ("01".equals(provCode)) {//北京
			resProvCode = "010";
		} else if ("02".equals(provCode)) {//天津
			resProvCode = "022";
		} else if ("03".equals(provCode)) {//河北
			resProvCode = "311";
		} else if ("04".equals(provCode)) {//山西
			resProvCode = "351";
		} else if ("05".equals(provCode)) {//内蒙古
			resProvCode = "471";
		} else if ("06".equals(provCode)) {//辽宁
			resProvCode = "024";
		} else if ("07".equals(provCode)) {//吉林
			resProvCode = "431";
		} else if ("08".equals(provCode)) {//黑龙江
			resProvCode = "451";
		} else if ("09".equals(provCode)) {//上海
			resProvCode = "021";
		} else if ("10".equals(provCode)) {//江苏
			resProvCode = "025";
		} else if ("11".equals(provCode)) {//浙江
			resProvCode = "571";
		} else if ("12".equals(provCode)) {//安徽
			resProvCode = "551";
		} else if ("13".equals(provCode)) {//福建
			resProvCode = "591";
		} else if ("14".equals(provCode)) {//江西
			resProvCode = "791";
		} else if ("15".equals(provCode)) {//山东
			resProvCode = "531";
		} else if ("16".equals(provCode)) {//河南
			resProvCode = "371";
		} else if ("17".equals(provCode)) {//湖北
			resProvCode = "027";
		} else if ("18".equals(provCode)) {//湖南
			resProvCode = "731";
		} else if ("19".equals(provCode)) {//广东
			resProvCode = "020";
		} else if ("20".equals(provCode)) {//广西
			resProvCode = "771";
		} else if ("21".equals(provCode)) {//海南
			resProvCode = "898";
		} else if ("22".equals(provCode)) {//四川
			resProvCode = "028";
		} else if ("23".equals(provCode)) {//贵州
			resProvCode = "851";
		} else if ("24".equals(provCode)) {//云南
			resProvCode = "871";
		} else if ("25".equals(provCode)) {//西藏
			resProvCode = "891";
		} else if ("26".equals(provCode)) {//陕西
			resProvCode = "029";
		} else if ("27".equals(provCode)) {//甘肃
			resProvCode = "931";
		} else if ("28".equals(provCode)) {//青海
			resProvCode = "971";
		} else if ("29".equals(provCode)) {//宁夏
			resProvCode = "951";
		} else if ("30".equals(provCode)) {//新疆
			resProvCode = "991";
		} else if ("31".equals(provCode)) {//重庆
			resProvCode = "023";
		}
		return resProvCode;
	}
	@Override
	protected void responseEorr(RequestMsg requestMsg, ResponseMsg respMap) {
		String merId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MERID));
		String goodsId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_GOODSID));
		String orderId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_ORDERID));
		String merDate = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MERDATE));
		String payChnl = ObjectUtil.trim(respMap.getStr("payChnl"));//支付方式，APK和SDK
		String retCode = ObjectUtil.trim(respMap.getRetCode());
		String retMsg =  getRetMessage(respMap);
		
		Map <String,String> map = new HashMap<String,String>();
		map.put(HFBusiDict.MERID, merId);
		map.put(HFBusiDict.GOODSID,goodsId);
		map.put(HFBusiDict.ORDERID, orderId);
		map.put(DataDict.MER_REQ_MERDATE,merDate);
		map.put(HFBusiDict.RETCODE, retCode);
		map.put(HFBusiDict.RETMSG, retMsg);
		map.put("payChnl", payChnl);
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
    
    
	/**
	 * 判断话付宝版本号是否满足直接支付条件
	 * @param clientVersion
	 * @param r4Pay_ClientVersion
	 * @return
	 */
	
	private boolean  checkSDKversion(String clientVersion,String config_version) {
		if(config_version.contains(clientVersion)){
			return true;
		}
		
//		//logInfo("*************clientVersion"+clientVersion+"--config_Version:+"+config_version);
//		String[] sl=clientVersion.split("\\.");
//		if(sl.length==3){
//			if(sl[1].length()==1){ 
//				sl[0]=sl[0]+"0"+sl[1];
//			}else{
//				sl[0]=sl[0]+sl[1];
//			}
//			if(sl[2].length()==1){
//				sl[0]=sl[0]+"0"+sl[2];
//			}else{
//				sl[0]=sl[0]+sl[2];
//			}
//	//		logInfo("*************sl[0]"+sl[0]+"--config_Version:+"+config_version);
//			int version=0;
//			int verson_config=1;
//			try{
//			   version=Integer.parseInt(sl[0]);
//			   verson_config=Integer.parseInt(config_version);
//			}catch(Exception e){
//				logInfo("客户端版本号解析失败 ：clientVersion["+clientVersion+"]config_version [" +config_version+"]");
//				return false;
//			}	
//			if(version>=verson_config){
//				logInfo("客户端版本号满足直接支付条件：clientVersion["+clientVersion+"]config_version [" +config_version+"]");
//			    return true;
//			}
//		}		
		return false;
	}
	
	public static int getIntVersion(String versionCode){
		String version = "";
		if(versionCode.contains(".")){
			String[] arrs = versionCode.split("[.]");
			for (int i = 0; i < arrs.length; i++) {
				int temp = Integer.parseInt(arrs[i]);
				if(temp<=9&&temp>=0&&arrs[i].length()==1){
					version = version+"0"+arrs[i];
				}else{
					version = version+arrs[i];
				}
			}
		}else{
			version = versionCode;
		}
		if("".equals(version)) version="0";
		return Integer.parseInt(version);
	}
	
	/**
	 * *****************  方法说明  *****************
	 * method name   :  findCondition
	 * @param  :  @param keys
	 * @param  :  @param funcode
	 * @param  :  @param msg
	 * @param  :  @return
	 * @param  :  @throws Exception
	 * @return  :  String
	 * @author       :  zhaoYan 2013-9-23 下午12:18:38 
	 * @Description  :  根据匹配条件，获取配置信息(有优先级关系)
	 * @see          : 
	 * @throws       :
	 * **********************************************
	 */
	private String findCondition(String[] keys, String funcode,
			Map<String, String> msg) throws Exception {
		//首先判断配置模板获取标签，如果为空，则退出
		int limitLen = keys.length;
		if (limitLen == 1 && keys[0].trim().length() == 0) {
			log.debug("功能码:" + funcode
					+ " 没有配置模板获取标签，请检查配置文件!");
			return null;
		}
		StringBuilder limitStr = new StringBuilder();
		for (int i = 0; i < limitLen; i++) {
			//确定参数个数，用二进制位数表示
			limitStr.append("1");
		}
		//将获取的标签长度，由2进制改为10进制
		int limit = Integer.parseInt(limitStr.toString(), 2);
		log.info("二进制limitStr:" + limitStr + " 转为十进制 limit:" + limit);
		StringBuilder baseCaseStr = new StringBuilder();
		String[] values = new String[limitLen];
		StringBuilder tags = new StringBuilder();
		for (int i = 0; i < limitLen; i++) {
			tags.append(keys[i]).append(".");
			String value = msg.get(keys[i]);
			values[i] = value;
			if (value == null) {
				baseCaseStr.append("0");
				log.debug("功能码:" + funcode + " 模板查找key [无]:" + keys[i]
						+ "-->" + value);
			} else {
				baseCaseStr.append("1");
				log.debug("功能码:" + funcode + " 模板查找key [有]:" + keys[i]
						+ "-->" + value);
			}
		}

		int baseCase = Integer.parseInt(baseCaseStr.toString(), 2);
		Set<Integer> used = new HashSet<Integer>();
		// 遍历所有情况
		for (int i = limit; i >= 0; i--) {
			int x = i & baseCase;
			log.debug("第:" + i + " 次查找！ 相与后的键图:" + x + " baseCase:"
			 +
			 baseCase);
			if (used.contains(x)) {
				continue;
			}
			used.add(x);
			StringBuilder condition = new StringBuilder();
			for (int n = limitLen - 1; n >= 0; n--) {
				if ((x & 1) == 1) {
					condition.insert(0, values[n] + ".");
				} else {
					condition.insert(0, "*.");
				}
				x >>= 1;
			}
			if (condition.length() > 0){
				condition.deleteCharAt(condition.length() - 1);
			}
			String catchedStr =  StringUtil.trim(messageService.getSystemParam(funcode+"."+condition.toString()));
			if ("".equals(catchedStr)) {
				log.debug(">>功能码:" + funcode + " 条件:" + condition
				 + " 没有找到模板配置，继续下一个");
				continue;
			}
			log.info("查找的键值:" + condition + " 模板:" + catchedStr);
			return catchedStr;
		}
		return null;
	}
}