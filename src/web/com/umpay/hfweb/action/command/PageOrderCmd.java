package com.umpay.hfweb.action.command;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.util.ObjectUtil;
/**
 * ******************  类说明  *********************
 * class       :  PageOrderCmd
 * @author     :  zhaoyan 
 * description :  页面支付相关输入参数
 * @see        :  
 * @version    :  1.0                   
 * ***********************************************
 */
public class PageOrderCmd extends AbstractOrderCmd{
	//商品信息 3.0
	protected String goodsName;
	//通道号
	protected String gateId;
	//页面返回地址
	protected String retUrl;
	/**
	 * 构造方法，组装数据
	 * @param reqMap
	 */
	public PageOrderCmd(Map<String,Object> reqMap) {
		super(reqMap);
		this.goodsName = (String)reqMap.get(DataDict.MER_REQ_GOODSINF);
		this.gateId = (String)reqMap.get(DataDict.MER_REQ_GATEID);
		this.retUrl = (String)reqMap.get(DataDict.MER_REQ_RETURL);
	}
	public PageOrderCmd(PageOrderCmd cmd, String mobileId){
		this.merId = cmd.getMerId();
		this.goodsId = cmd.getGoodsId();
		this.mobileId = cmd.getMobileId();
		this.orderId = cmd.getOrderId();
		this.merDate = cmd.getMerDate();
		this.amount = cmd.getAmount();
		this.amtType = cmd.getAmtType();
		this.bankType = cmd.getBankType();
		this.notifyUrl = cmd.getNotifyUrl();
		this.merPriv = cmd.getMerPriv();
		this.expand = cmd.getExpand();
		this.version = cmd.getVersion();
		this.sign = cmd.getSign();
		
		this.goodsName = cmd.getGoodsName();
		this.gateId = cmd.gateId;
		this.retUrl = cmd.getRetUrl();
		this.mobileId = mobileId;
	}
	/**
	 * ********************************************
	 * 获取页面下单时，请求信息的明文串
	 * method name   : getPlainText 
	 * modified      : zhaoyan ,  Nov 3, 2011
	 * @see          : @see com.umpay.hfweb.action.command.AbstractOrderCmd#getPlainText()
	 * *******************************************
	 */
	public String getPlainText(){
		StringBuffer buffer = new StringBuffer();
		buffer.append(DataDict.MER_REQ_MERID).append("=").append(merId).append("&");
		if (goodsId != null){
			buffer.append(DataDict.MER_REQ_GOODSID).append("=").append(goodsId).append("&");
		}
		if (goodsName != null){
			buffer.append(DataDict.MER_REQ_GOODSINF).append("=").append(goodsName).append("&");
		}
		if (mobileId != null ){
			buffer.append(DataDict.MER_REQ_MOBILEID).append("=").append(mobileId).append("&");
		}
		buffer.append(DataDict.MER_REQ_ORDERID).append("=").append(orderId).append("&");
		buffer.append(DataDict.MER_REQ_MERDATE).append("=").append(merDate).append("&");
		buffer.append(DataDict.MER_REQ_AMOUNT).append("=").append(amount).append("&");
		buffer.append(DataDict.MER_REQ_AMTTYPE).append("=").append(amtType).append("&");
		if (bankType != null){
			buffer.append(DataDict.MER_REQ_BANKTYPE).append("=").append(bankType).append("&");
		}
		if(gateId != null){
			buffer.append(DataDict.MER_REQ_GATEID).append("=").append(ObjectUtil.trim(gateId)).append("&");
		}
		buffer.append(DataDict.MER_REQ_RETURL).append("=").append(retUrl).append("&");
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
	/**
	 * *****************  方法说明  *****************
	 * method name   :  getUrlEncodedPlainText
	 * @param		 :  @param charset
	 * @param		 :  @return
	 * @param		 :  @throws UnsupportedEncodingException
	 * @return		 :  String
	 * @author       :  LiZhen 2014-6-25 下午3:27:48
	 * description   :  生成编码后的明文串
	 * @see          :  
	 * **********************************************
	 */
	public String getUrlEncodedPlainText(String charset) throws UnsupportedEncodingException{
		StringBuffer buffer = new StringBuffer();
		buffer.append(DataDict.MER_REQ_MERID).append("=").append(URLEncoder.encode(merId, charset)).append("&");
		if (goodsId != null){
			buffer.append(DataDict.MER_REQ_GOODSID).append("=").append(URLEncoder.encode(goodsId, charset)).append("&");
		}
		if (goodsName != null){
			buffer.append(DataDict.MER_REQ_GOODSINF).append("=").append(URLEncoder.encode(goodsName, charset)).append("&");
		}
		if (mobileId != null ){
			buffer.append(DataDict.MER_REQ_MOBILEID).append("=").append(URLEncoder.encode(mobileId, charset)).append("&");
		}
		buffer.append(DataDict.MER_REQ_ORDERID).append("=").append(URLEncoder.encode(orderId, charset)).append("&");
		buffer.append(DataDict.MER_REQ_MERDATE).append("=").append(URLEncoder.encode(merDate, charset)).append("&");
		buffer.append(DataDict.MER_REQ_AMOUNT).append("=").append(URLEncoder.encode(amount, charset)).append("&");
		buffer.append(DataDict.MER_REQ_AMTTYPE).append("=").append(URLEncoder.encode(amtType, charset)).append("&");
		if (bankType != null){
			buffer.append(DataDict.MER_REQ_BANKTYPE).append("=").append(URLEncoder.encode(bankType, charset)).append("&");
		}
		if(gateId != null){
			buffer.append(DataDict.MER_REQ_GATEID).append("=").append(URLEncoder.encode(ObjectUtil.trim(gateId), charset)).append("&");
		}
		buffer.append(DataDict.MER_REQ_RETURL).append("=").append(URLEncoder.encode(retUrl, charset)).append("&");
		if (notifyUrl != null){
			buffer.append(DataDict.MER_REQ_NOTIFYURL).append("=").append(URLEncoder.encode(notifyUrl, charset)).append("&");
		}
		if (merPriv != null){
			buffer.append(DataDict.MER_REQ_MERPRIV).append("=").append(URLEncoder.encode(merPriv, charset)).append("&");
		}
		if (expand != null){
			buffer.append(DataDict.MER_REQ_EXPAND).append("=").append(URLEncoder.encode(expand, charset)).append("&");
		}
		buffer.append(DataDict.MER_REQ_VERSION).append("=").append(URLEncoder.encode(version, charset));
		return buffer.toString();
	}
	public String getGoodsName() {
		return goodsName;
	}
	public String getGateId() {
		return gateId;
	}
	public String getRetUrl() {
		return retUrl;
	}

}
