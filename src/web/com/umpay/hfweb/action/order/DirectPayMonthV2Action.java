package com.umpay.hfweb.action.order;

import com.umpay.hfweb.action.command.DirectMonthOrderV2Cmd;
import com.umpay.hfweb.action.command.DirectOrderCmd;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.ObjectUtil;

/**
 * ******************  类说明  *********************
 * class       :  DirectPayMonthV2Action
 * @author     :  yangwr
 * @version    :  1.0  
 * description :  全网包月下单 V2.0（订单号=手机号+商品号）
 * @see        :                        
 * ***********************************************
 */
public class DirectPayMonthV2Action extends DirectPayAction{
	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_DIRECT_HTXD_MONTH_V2;
	}
	protected  DirectOrderCmd populateCommand(RequestMsg requestMsg){
		return new DirectMonthOrderV2Cmd(requestMsg.getWrappedMap());
	}
	
	protected void responseSuccess2Mer(RequestMsg requestMsg,final ResponseMsg responseMsg){
		String retCode = DataDict.SUCCESS_RET_CODE;
		responseMsg.setRetCode0000();
		
		String merId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MERID_V2));
		String goodsId = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_GOODSID_V2));
		String merDate = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_DATETIME_V2));
		String rdpwd = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_RDPWD_V2));
		
		
		StringBuffer sb = new StringBuffer();
		sb.append(retCode).append("|");
		sb.append(merDate).append("|");
		sb.append(rdpwd).append("|");
		sb.append(merId).append("|");
		sb.append(goodsId).append("|");
		sb.append("下订单成功");
		String sign = super.platSign(sb.toString());
		sb.append("|").append(sign);
		
		responseMsg.setDirectResMsg(sb.toString());
	}
	
	@Override
	protected void responseEorr2Mer(RequestMsg requestMsg,ResponseMsg responseMsg) {
		String retCode = responseMsg.getRetCode();
		String merDate = ObjectUtil.trim(requestMsg.getStr(DataDict.MER_REQ_MERDATE));
		StringBuffer sb = new StringBuffer();
		sb.append(retCode).append("|");
		sb.append(merDate);
		sb.append("|||||");
		sb.append(getRetMessage(responseMsg));
		responseMsg.setDirectResMsg(sb.toString());
	}
	@Override
	public void processBussiness(RequestMsg requestMsg,
			ResponseMsg responseMsg) {
		super.processBussiness(requestMsg, responseMsg);
	}
	protected boolean isVer3(){
		return false;
	}
}
