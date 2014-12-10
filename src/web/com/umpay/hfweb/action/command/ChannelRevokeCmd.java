package com.umpay.hfweb.action.command;

import java.util.Map;

import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.util.ObjectUtil;
/**
 * 渠道冲正处理参数处理类
 * @author yangwr
 */
public class ChannelRevokeCmd extends AbstractOrderCmd{
	public ChannelRevokeCmd(){
		
	}
	public ChannelRevokeCmd(Map<String,Object> reqMap) {
		super(reqMap);
		notifyUrl = null;//渠道冲正请求接口中，没有该参数
		merPriv = null;//渠道冲正请求接口中，没有该参数
		expand = null;//渠道冲正请求接口中，没有该参数
	}
	
	/**
	 * 获取请求信息的明文串
	 * @return String
	 */
	public String getPlainText(){
		StringBuffer buffer = new StringBuffer();
		buffer.append(DataDict.MER_REQ_MERID).append("=").append(merId).append("&");
		buffer.append(DataDict.MER_REQ_GOODSID).append("=").append(ObjectUtil.trim(goodsId)).append("&");
		buffer.append(DataDict.MER_REQ_MOBILEID).append("=").append(mobileId).append("&");
		buffer.append(DataDict.MER_REQ_ORDERID).append("=").append(orderId).append("&");
		buffer.append(DataDict.MER_REQ_MERDATE).append("=").append(merDate).append("&");
		buffer.append(DataDict.MER_REQ_AMOUNT).append("=").append(amount).append("&");
		buffer.append(DataDict.MER_REQ_AMTTYPE).append("=").append(amtType).append("&");
		buffer.append(DataDict.MER_REQ_BANKTYPE).append("=").append(bankType).append("&");
//		if (notifyUrl != null){
//			buffer.append(DataDict.MER_REQ_NOTIFYURL).append("=").append(notifyUrl).append("&");
//		}
//		if (merPriv != null){//TODO test 如果入参中没有商户私有信息时，merPriv是否为null
//			buffer.append(DataDict.MER_REQ_MERPRIV).append("=").append(merPriv).append("&");
//		}
//		if (expand != null){
//			buffer.append(DataDict.MER_REQ_EXPAND).append("=").append(expand).append("&");
//		}
		buffer.append(DataDict.MER_REQ_VERSION).append("=").append(version);
		
		return buffer.toString();
	}
}
