package com.umpay.hfweb.action.mer;

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
 * description :  商户查询包月订购关系 V2.0
 * @see        :                        
 * ***********************************************
 */
public class MerQueryUserV2Action extends DirectCommonAction{
	
	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_MER_QUERY_MONTHSUB_V2;
	}
    @Override
    protected boolean isVer3(){
		return false;
	}
	@Override
	protected String getPlainText(RequestMsg requestMsg) {
		StringBuffer sb = new StringBuffer();
		sb.append("SPID=");
		sb.append(ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MERID_V2)));
		sb.append("&GOODSID=");
		sb.append(ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_GOODSID_V2)));
		sb.append("&MOBILEID=");
		sb.append(ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MOBILEID_V2)));
		sb.append("&RDPWD=");
		sb.append(ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_RDPWD_V2)));
		return sb.toString();
	}
	@Override
	protected MpspMessage specialCheck(RequestMsg requestMsg) {
		//空处理
		return null;
	}
	
	@Override
	protected MpspMessage mainProcess(RequestMsg requestMsg) {
		//商户号
		String merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID_V2));
		//商品号
		String goodsId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_GOODSID_V2));
		//手机号
		String mobileId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MOBILEID_V2));
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
			//商户号
			String merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID_V2));
			StringBuffer sb = new StringBuffer();
			sb.append("0001").append("|");
			sb.append(merId);
			String sign=platSign(sb.toString());
			sb.append("|").append(sign);
			responseMsg.setDirectResMsg(sb.toString());
		}else{
			String retMsg = getRetMessage(responseMsg);		
			String meta = retCode + "||" + retMsg;
			responseMsg.setDirectResMsg(meta);
		}

	}
	@Override
	protected void responseSuccess2Mer(RequestMsg requestMsg,
			ResponseMsg responseMsg) {
		MpspMessage infoMsg = responseMsg.getInfoMsg();
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
		//商户号
		String merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID_V2));
		StringBuffer sb = new StringBuffer();
		sb.append(retCode).append("|");
		sb.append(merId);
		String sign=platSign(sb.toString());
		sb.append("|").append(sign);
		responseMsg.setDirectResMsg(sb.toString());
	}
}
