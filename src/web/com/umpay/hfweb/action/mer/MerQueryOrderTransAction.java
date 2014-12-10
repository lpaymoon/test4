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
 * class       :  MerQueryOrderTransAction
 * @author     :  zhaoYan 
 * description :  商户查询订单以及交易返回码
 * @see        :  2012-11-21 新增
 * @version    :  1.0                   
 * ***********************************************
 */
public class MerQueryOrderTransAction extends DirectCommonAction{

	@Override
	protected String getFunCode() {
		return DataDict.FUNCODE_MER_QUERY_ORDERTRANS;
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
		//商户日期
		String merDate = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERDATE));
		//订单号
		String orderId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_ORDERID));

		StringBuffer buffer = new StringBuffer();
		buffer.append("merId=").append(merId);
		buffer.append("&goodsId=").append(goodsId);
		buffer.append("&orderId=").append(orderId);
		buffer.append("&merDate=").append(merDate);
		buffer.append("&mobileId=").append(mobileId);
		buffer.append("&version=").append(version);
		
		return buffer.toString();
	}
	
	@Override
	protected MpspMessage specialCheck(RequestMsg requestMsg) {
		return null;
	}

	@Override
	protected MpspMessage mainProcess(RequestMsg requestMsg) {
		//商户号
		String merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID));
		//商户日期
		String merDate = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERDATE));
		//订单号
		String orderId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_ORDERID));
		//5-查询商户订单信息
		MpspMessage checkMerQueryOrderResp = restService.queryMerOrder(merId, merDate, orderId);
		return checkMerQueryOrderResp;
	}

	@Override
	protected void responseSuccess2Mer(RequestMsg requestMsg,
			ResponseMsg responseMsg) {
		//商户号
		String merId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERID));
		//商品号
		String goodsId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_GOODSID));
		//手机号
		String mobileId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MOBILEID));
		//版本号
//		String version = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_VERSION));
		//商户日期
		String merDate = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERDATE));
		//订单号
		String orderId = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_ORDERID));

		MpspMessage infoMsg = responseMsg.getInfoMsg();
		
		String orderState = ObjectUtil.trim(infoMsg.get(HFBusiDict.ORDERSTATE));
		String payRetCode = ObjectUtil.trim(infoMsg.get(HFBusiDict.RESERVED));//交易失败返回码
		//0：开始 1：支付中 2：成功 3：失败
		if("0".equals(orderState)){
			//do nothing
		}else if("2".equals(orderState)){
			orderState = "1";
		}else{
			orderState = "-1";
		}
		
		if(payRetCode.length() > 4){
			//错误码截取后四位
			payRetCode = payRetCode.substring(4);
		}
		StringBuffer sb = new StringBuffer();
		sb.append(merId).append("|");
		sb.append(goodsId).append("|");
		sb.append(orderId).append("|");
		sb.append(merDate).append("|");
		sb.append(ObjectUtil.trim(infoMsg.get(HFBusiDict.PLATDATE))).append("|"); //平台时间
		sb.append(ObjectUtil.trim(infoMsg.get(HFBusiDict.AMOUNT))).append("|");
		sb.append(ObjectUtil.trim(infoMsg.get(HFBusiDict.AMTTYPE))).append("|");
		sb.append("3").append("|");
		sb.append(mobileId).append("|");
		//20111206 zhaoy 添加一条“|”
		sb.append("").append("|");
		sb.append(ObjectUtil.trim(infoMsg.get(HFBusiDict.TRANSTYPE))).append("|");
		sb.append(orderState).append("|");
		sb.append(payRetCode).append("|");//2012-11-21 交易返回码 成功时为0000， 错误时返回交易错误码(4位)
		sb.append(ObjectUtil.trim(infoMsg.get(HFBusiDict.PLATDATE))).append("|"); //平台时间
		sb.append("1").append("|");
		sb.append(ObjectUtil.trim(infoMsg.get(HFBusiDict.MERPRIV))).append("|");
		sb.append(DataDict.SUCCESS_RET_CODE).append("|");
		sb.append("3.0");
		String sign = platSign(sb.toString());
		sb.append("|").append(sign);
		responseMsg.setDirectResMsg(sb.toString());
	}
	@Override
	protected void responseEorr2Mer(RequestMsg requestMsg,ResponseMsg responseMsg) {
		//商户日期
		String merDate = ObjectUtil.trim(requestMsg.get(DataDict.MER_REQ_MERDATE));
		String retCode = responseMsg.getRetCode();
		String retMsg = getRetMessage(responseMsg);
		//由于增加reserved字段，因此增加一个 "|" 2012-11-21
		StringBuffer meta = new StringBuffer("|||").append(merDate).append("|||||||||||||").append(retCode).append("|3.0|").append(retMsg);
		responseMsg.setDirectResMsg(meta.toString());
	}

}
