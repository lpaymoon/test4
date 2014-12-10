package com.umpay.hfweb.service;

import com.umpay.hfweb.action.command.AbstractOrderCmd;
import com.umpay.hfweb.action.command.ChannelPayCmd;
import com.umpay.hfweb.action.command.ChannelRevokeCmd;
import com.umpay.hfweb.action.command.DirectOrderCmd;
import com.umpay.hfweb.action.command.PageOrderCmd;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;
/**
 * 
 * @Title: TradeService.java
 * @Package com.umpay.hfweb.service
 * @Description: 业务层相关处理
 * @author yangwr
 * @date Nov 1, 2011 10:53:25 PM
 * @version V1.0
 */
public interface TradeService {
	/**
	 * ********************************************
	 * method name   : saveOrder 
	 * description   : 调用业务层订单保存服务
	 * @return       : MpspMessage
	 * @param        : @param orderReqMsg
	 * @param        : @param cmd
	 * @param        : @return
	 * modified      : zhaoyan ,  Nov 10, 2011 2:31:09 PM
	 * @see          : 
	 * *******************************************
	 */
	public MpspMessage saveOrder(String bankId, MpspMessage message, AbstractOrderCmd cmd);
	
	
	/**
	 * ********************************************
	 * method name   : queryBalance 
	 * description   : 查询商户余额
	 * @return       : MpspMessage
	 * @param        : @param merId
	 * @param        : @param mobileId
	 * @param        : @return
	 * modified      : yangwr ,  Nov 4, 2011  4:19:02 PM
	 * @see          : 
	 * *******************************************
	 */
	public MpspMessage queryBalance(String merId,String mobileId);
	
	/**
	 * ********************************************
	 * method name   : queryBalance 
	 * description   : 查询用户可支付额度
	 * @return       : MpspMessage
	 * @param        : @param merId
	 * @param        : @param mobileId
	 * @param        : @return
	 * modified      : yangwr ,  Nov 4, 2011  4:19:02 PM
	 * @see          : 
	 * *******************************************
	 */
	public MpspMessage queryPayment(String merId,String mobileId);
	
	/**
	 * ********************************************
	 * method name   : channelPay 
	 * description   : 渠道支付
	 * @param        : cmd merId
	 * @return       : 
	 * *******************************************
	 */
	public MpspMessage channelPay(ChannelPayCmd cmd);
	/**
	 * ********************************************
	 * method name   : channelRevoke 
	 * description   : 渠道冲正
	 * @param        : cmd merId
	 * @return       : 
	 * *******************************************
	 */
	public MpspMessage channelRevoke(ChannelRevokeCmd cmd);
	/**
	 * 异步下单接口，江西web后台下单接口
	 * 注意：web页面下单如果也需要接入江西小额的话，合并该接口到saveOrder方法
	 * @param bankId
	 * @param message
	 * @param cmd
	 * @return
	 */
	public MpspMessage asynOrder(String bankId, MpspMessage message, AbstractOrderCmd cmd);
	
	public MpspMessage wxHfOrderSave(MpspMessage message);
	
	/**
	 * ********************************************
	 * method name   : preAuthPay 
	 * description   : 预授权支付接口
	 * @param        : MpspMessage message
	 * @return       : 
	 * *******************************************
	 */
	public MpspMessage preAuthPay(MpspMessage message);
	/**
	 * ********************************************
	 * method name   : wxUFNotifyService 
	 * description   : 商户结果通知
	 * @return       : MpspMessage
	 * @param        : @param requestMsg
	 * @param        : @return
	 * modified      : panxingwu ,  2012-10-24  下午4:41:19
	 * @see          : 
	 * *******************************************
	 */
	public MpspMessage wxUFNotifyService(MpspMessage message);


	/** ********************************************
	 * method name   : queryMobileInfo 
	 * description   : 商户查询手机号信息接口
	 * @return       : MpspMessage
	 * @param        : @param merId 商户号
	 * @param        : @param mobileId 手机号
	 * @param        : @return
	 * modified      : LiuJiLong ,  2012-12-12 下午09:06:59
	 * @see          : 
	 * ********************************************/      
	public MpspMessage queryMobileInfo(String merId, String mobileId);

	/**
	 * ********************************************
	 * method name   : specialMerPay 
	 * description   : 特殊渠道支付接口（福建12580买Q币提出）
	 * @return       : MpspMessage
	 * @param        : @param requestMsg
	 * @param        : @return
	 * modified      : panxingwu ,  2013-1-23  下午1:46:52
	 * @see          : 
	 * *******************************************
	 */
	public MpspMessage specialMerPay(RequestMsg requestMsg);

	/**
	 * ********************************************
	 * method name   : specialMerPay 
	 * description   : 渠道下单
	 * @return       : MpspMessage
	 * @param        : @param requestMsg
	 * @param        : @return
	 * modified      : panxingwu ,  2013-03-13  下午1:46:52
	 * @see          : 
	 * *******************************************
	 */
	public MpspMessage qdOrderCreate(RequestMsg requestMsg);


	/** 
	* @Title		: wxQueryUserInf 
	* @Description	: 话付宝查询用户手机信息（余额，日可用额度等）
	* @param 		：@param requestMsg
	* @param 		：@return
	* @return 		：MpspMessage 
	* @throws 
	*/
	public MpspMessage wxQueryUserInf(RequestMsg requestMsg);
	/**
	 * ********************************************
	 * method name   : wapDirectPay 
	 * description   : WAP直接支付
	 * @param isCommon 
	 * @param        : cmd
	 * @param        : mobileId
	 * @return       :  
	 * *******************************************
	 */
	public MpspMessage wapDirectPay(PageOrderCmd cmd,String mobileId, boolean isCommon);

	/**
	 * ********************************************
	 * method name   : SDKPay 
	 * description   : 12580 SDK客户端支付流程
	 * @param isCommon 
	 * @param        : requestMsg
	 * @return       :  
	 * *******************************************
	 */
	public MpspMessage SDKPay(RequestMsg requestMsg);
	/** ********************************************
	 * method name   : sendMonthlyServiceOrder 
	 * description   : 向业务层发起包月用户关系订购
	 * @return       : MpspMessage
	 * @param        : @param cmd
	 * @param        : @param state
	 * @param        : @return
	 * add           : zhuoyangyang ,  2013-12-19  下午04:41:48
	 * @see          : 
	 * ********************************************/   
	
	public MpspMessage sendMonthlyServiceOrder(DirectOrderCmd cmd);
	/**
	 * ********************************************
	 * method name   : QdVerifyPay 
	 * description   : 渠道验证码支付
	 * @param        : requestMsg
	 * @return       :  
	 * *******************************************
	 */
	public MpspMessage qdVerifyPay(RequestMsg requestMsg,MpspMessage message);
	/**
	 * ********************************************
	 * method name   : qdVerifyOrder 
	 * description   : 渠道下单
	 * @return       : MpspMessage
	 * @param        : @param requestMsg
	 * @param        : @return
	 * modified      : panxingwu ,  2013-03-13  下午1:46:52
	 * @see          : 
	 * *******************************************
	 */
	public MpspMessage qdVerifyOrder(RequestMsg requestMsg);


	public MpspMessage sdkQuickPay(RequestMsg requestMsg);
}
