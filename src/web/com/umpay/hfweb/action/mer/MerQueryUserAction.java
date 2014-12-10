package com.umpay.hfweb.action.mer;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.base.DirectCommonAction;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.ObjectUtil;
/**
 * ******************  类说明  *********************
 * class       :  MerQueryUserAction
 * @author     :  yangwr
 * @version    :  1.0  
 * description :  商户查询包月订购关系 V3.0
 * @see        :                        
 * ***********************************************
 */
public class MerQueryUserAction extends DirectCommonAction{
	
	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_MER_QUERY_MONTHSUB;
	}

	@Override
	protected String getPlainText(RequestMsg requestMsg) {
		//商户号
		String merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID));
		//商品号
		String goodsId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_GOODSID));
		//手机号
		String mobileId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MOBILEID));
		//版本号
		String version = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_VERSION));
		String plainText = new StringBuffer("merId=").append(merId).append("&goodsId=")
						.append(goodsId).append("&mobileId=").append(mobileId).append("&version=")
						.append(version).toString();
		return plainText;
	}
	@Override
	protected MpspMessage specialCheck(RequestMsg requestMsg) {
//		MpspMessage respMsg = new MpspMessage();
//		//版本号
//		String version = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_VERSION));
//		if(!version.equals("3.0")){
//			respMsg.setRetCode("1112");
//			return respMsg;
//		}
//		respMsg.setRetCode0000();
		return null;
	}
	
	@Override
	protected MpspMessage mainProcess(RequestMsg requestMsg) {
		//商户号
		String merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID));
		//商品号
		String goodsId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_GOODSID));
		//手机号
		String mobileId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MOBILEID));
		//查询商户包月信息
		MpspMessage checkMerMonthSubResp = restService.queryMonthUserState(merId,mobileId,goodsId);
		return checkMerMonthSubResp;
	}
	@Override
	protected void responseEorr2Mer(RequestMsg requestMsg,
			ResponseMsg responseMsg) {
		String retCode = responseMsg.getRetCode();
		String busiCode = responseMsg.getRetCodeBussi();
		if("86001400".equals(busiCode)){
			MpspMessage infoMsg = responseMsg.getInfoMsg();
			StringBuffer sb = new StringBuffer();
			sb.append(ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID))).append("|");
			sb.append(ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_GOODSID))).append("|");
			sb.append(ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MOBILEID))).append("|");
			sb.append(ObjectUtil.trim(infoMsg.get(HFBusiDict.STATE))).append("|");
			Timestamp tsp = (Timestamp)infoMsg.get(HFBusiDict.ORDERTIME);
			String orderTime = "";
			if(tsp != null){
				try {
					SimpleDateFormat dateFormat2 = new SimpleDateFormat ("yyyyMMddHHmmss");
					orderTime = dateFormat2.format(tsp);
				} catch (Exception e) {
					logInfo("定购时间解析失败");
				}
			}
			sb.append(orderTime).append("|");
			sb.append(ObjectUtil.trim(infoMsg.get(HFBusiDict.ENDDATE))).append("|");
			sb.append("0001").append("|");
			sb.append("3.0");
			String sign = platSign(sb.toString());
			sb.append("|").append(sign);
			responseMsg.setDirectResMsg(sb.toString());
		}else{
			String retMsg = getRetMessage(responseMsg);
			//String meta = retCode + "|||||||" + retMsg;
			String meta = "||||||" + retCode + "||"  + retMsg;
			responseMsg.setDirectResMsg(meta);
		}
	}
	@Override
	protected void responseSuccess2Mer(RequestMsg requestMsg,
			ResponseMsg responseMsg) {
		MpspMessage infoMsg = responseMsg.getInfoMsg();
		StringBuffer sb = new StringBuffer();
		String retCode = responseMsg.getRetCode();
		String state = ObjectUtil.trim(infoMsg.get(HFBusiDict.STATE));
		//原版本实现对照 数据库状态--TradeLogicService返回码--返回商户码
		//0--1201--0001
		//2--1206--0000
		//3--1203--0003
		//4--1204--0004
		//5 6 7--1205--0005
		//else -- 1207--1207
		if(state.equals("0")){
			retCode = "0001";
		}else if(state.equals("2")){
			retCode = "0000";
		}else if(state.equals("3")){
			retCode = "0003";
		}else if(state.equals("4")){
			retCode = "0004";
		}else{
			retCode = "0005";
		}
		sb.append(ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID))).append("|");
		sb.append(ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_GOODSID))).append("|");
		sb.append(ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MOBILEID))).append("|");
		sb.append(state).append("|");
		Timestamp tsp = (Timestamp)infoMsg.get(HFBusiDict.ORDERTIME);
		String orderTime = "";
		try {
			SimpleDateFormat dateFormat2 = new SimpleDateFormat ("yyyyMMddHHmmss");
			orderTime = dateFormat2.format(tsp);
		} catch (Exception e) {
			logInfo("定购时间解析失败");
		}
		sb.append(orderTime).append("|");
		sb.append(ObjectUtil.trim(infoMsg.get(HFBusiDict.ENDDATE))).append("|");
		sb.append(retCode).append("|");
		sb.append("3.0");
		String sign = platSign(sb.toString());
		sb.append("|").append(sign);
		responseMsg.setDirectResMsg(sb.toString());
	}
}
