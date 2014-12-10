package com.umpay.hfweb.action.mer;

import com.umpay.hfweb.action.base.DirectCommonAction;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
import com.umpay.hfweb.model.ResponseMsg;
import com.umpay.hfweb.util.ObjectUtil;
/**
 * ******************  类说明  *********************
 * class       :  CancelUserV2Action
 * @author     :  yangwr
 * @version    :  1.0  
 * description :  商户取消包月定制关系 V2.0
 * @see        :                        
 * ***********************************************
 */
public class MerCancelUserV2Action extends DirectCommonAction{

	@Override
	public String getFunCode() {
		return DataDict.FUNCODE_MER_CANCEL_USER_V2;
	}

	@Override
	protected String getPlainText(RequestMsg requestMsg) {
		//商户号V2接口
		String merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID_V2));
		//商品号V2接口
		String goodsId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_GOODSID_V2));
		//手机号V2接口
		String mobileId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MOBILEID_V2));
		//随机码V2接口
		String rdpwd = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_RDPWD_V2));
		String content = "SPID="+merId+"&GOODSID="+goodsId+"&MOBILEID="+mobileId+"&RDPWD="+rdpwd;
		return content;
	}

	@Override
	protected void responseSuccess2Mer(RequestMsg requestMsg,
			ResponseMsg responseMsg) {
		//商户号V2接口
		String merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID_V2));
		String retMsg = responseMsg.getRetCode()+"|"+merId;
		String sign=platSign(retMsg);
		String memo = messageService.getMessage(responseMsg.getRetCode());
		String meta = retMsg + "|" + sign + "|" + memo; //TODO confirm memo
		responseMsg.setDirectResMsg(meta);
	}

	@Override
	protected void responseEorr2Mer(RequestMsg requestMsg,
			ResponseMsg responseMsg) {
		//商户号V2接口
		String merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID_V2));
		String retMsg = responseMsg.getRetCode()+"|"+merId;
		String sign = "";
		String memo = getRetMessage(responseMsg);
		String meta = retMsg + "|" + sign + "|" + memo;
		responseMsg.setDirectResMsg(meta);
	}
	@Override
	 protected boolean isVer3(){
			return false;
		}
	@Override
	protected MpspMessage mainProcess(RequestMsg requestMsg) {
		//商户号
		String merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID_V2));
		//商品号
		String goodsId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_GOODSID_V2));
		//手机号
		String mobileId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MOBILEID_V2));
		//取消包月定制关系
		MpspMessage cancelResp = restService.cancelMonthUserState(merId,mobileId,goodsId);
		return cancelResp;
	}

	@Override
	protected MpspMessage specialCheck(RequestMsg requestMsg) {
		// do nothing
		return null;
	}

}
