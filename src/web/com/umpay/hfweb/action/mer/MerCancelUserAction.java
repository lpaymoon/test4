package com.umpay.hfweb.action.mer;

import com.umpay.hfweb.action.base.DirectCommonAction;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.ObjectUtil;

/**
 * ******************  类说明  *********************
 * class       :  CancelUserAction
 * @author     :  yangwr
 * @version    :  1.0  
 * description :  商户取消包月定制关系 V3.0（暂不开发该接口）
 * @see        :                        
 * ***********************************************
 */
public class MerCancelUserAction extends DirectCommonAction{

	@Override
	public String getFunCode() {
		return DataDict.FUNCODE_MER_CANCEL_USER;
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
//		//签名
//		String sign = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_SIGN));
		String content = "merId="+merId+"&goodsId="+goodsId+"&mobileId="+mobileId+"&version="+version;
		return content;
	}

	@Override
	protected void responseEorr2Mer(RequestMsg requestMsg,
			ResponseMsg responseMsg) {
		//商户号
		String merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID));
		String retMsg = responseMsg.getRetCode()+"|"+merId;
		String sign="";
		String memo = messageService.getMessage(responseMsg.getRetCode());
		String meta = retMsg + "|" + sign + "|" + memo;
		responseMsg.setDirectResMsg(meta);
	}

	@Override
	protected void responseSuccess2Mer(RequestMsg requestMsg,
			ResponseMsg responseMsg) {
		//商户号
		String merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID));
		String retMsg = responseMsg.getRetCode()+"|"+merId;
		String sign=platSign(retMsg);
		String meta = retMsg + "|" + sign + "|" + getRetMessage(responseMsg);
		responseMsg.setDirectResMsg(meta);
		
	}

	@Override
	protected MpspMessage mainProcess(RequestMsg requestMsg) {
		//商户号
		String merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID));
		//商品号
		String goodsId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_GOODSID));
		//手机号
		String mobileId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MOBILEID));
		//取消包月定制关系
		MpspMessage cancelResp = restService.cancelMonthUserState(merId,goodsId,mobileId);
		return cancelResp;
	}

	@Override
	protected MpspMessage specialCheck(RequestMsg requestMsg) {
		// do nothing
		return null;
	}

}
