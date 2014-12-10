package com.umpay.hfweb.action.command;

import java.util.Map;

import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.util.ObjectUtil;
/**
 * 后台直连处理参数处理类
 * @Title: DirectOrderCmd.java
 * @Package com.umpay.hfweb.action.command
 * @Description: TODO(添加描述)
 * @author yangwr
 * @date Nov 1, 2011 7:57:19 PM
 * @version V1.0
 */
public class DirectOrderCmd extends AbstractOrderCmd{
	public DirectOrderCmd(){
		
	}
	public DirectOrderCmd(Map<String,Object> reqMap) {
		super(reqMap);
	}
	
	/**
	 * @Title: getPayPlain
	 * @Description: 获取后台直连下单时，请求信息的明文串
	 * @param @param payFlag
	 * @param @return    String
	 * @return String    
	 * @throws
	 */
	public String getPlainText(){
		StringBuffer buffer = new StringBuffer();
		buffer.append(DataDict.MER_REQ_MERID).append("=");
		if(!ObjectUtil.isEmpty(expand) && expand.startsWith("MICPAY")){
			buffer.append("7517");
		}else{
			buffer.append(merId);
		}
		buffer.append("&");
		if (goodsId != null){
			buffer.append(DataDict.MER_REQ_GOODSID).append("=").append(ObjectUtil.trim(goodsId)).append("&");
		}
		buffer.append(DataDict.MER_REQ_MOBILEID).append("=").append(mobileId).append("&");
		buffer.append(DataDict.MER_REQ_ORDERID).append("=").append(orderId).append("&");
		buffer.append(DataDict.MER_REQ_MERDATE).append("=").append(merDate).append("&");
		buffer.append(DataDict.MER_REQ_AMOUNT).append("=").append(amount).append("&");
		buffer.append(DataDict.MER_REQ_AMTTYPE).append("=").append(amtType).append("&");
		buffer.append(DataDict.MER_REQ_BANKTYPE).append("=").append(bankType).append("&");
		if (notifyUrl != null){
			buffer.append(DataDict.MER_REQ_NOTIFYURL).append("=").append(notifyUrl).append("&");
		}
		if (merPriv != null){
			buffer.append(DataDict.MER_REQ_MERPRIV).append("=").append(merPriv).append("&");
		}
		if (expand != null){
			buffer.append(DataDict.MER_REQ_EXPAND).append("=").append(expand).append("&");
		}
		buffer.append(DataDict.MER_REQ_VERSION).append("=").append(version);
		
		return buffer.toString();
	}
}
