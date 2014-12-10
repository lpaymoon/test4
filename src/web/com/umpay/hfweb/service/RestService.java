package com.umpay.hfweb.service;

import java.util.Map;

import com.umpay.hfweb.action.command.AbstractOrderCmd;
import com.umpay.hfweb.action.command.H5verifyCodePayCmd;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.model.RequestMsg;

/** ******************  类说明  *********************
 * class       :  RestService
 * @author     :  panxingwu
 * @version    :  1.0  
 * description :  
 * @see        :                        
 * ************************************************/   
public interface RestService {
	
	/**
	 * ********************************************
	 * method name   : getMobileSeg 
	 * description   : 获取手机号号段信息
	 * @return       : MpspMessage
	 * @param        : mobileId
	 * modified      : yangwr ,  Nov 4, 2011  4:59:15 PM
	 * @see          : 
	 * *******************************************
	 */
	public MpspMessage getMobileSeg(String mobileId);

	/**
	 * ********************************************
	 * method name   : cancelMonthUserState 
	 * description   : 取消包月订购关系
	 * @return       : MpspMessage
	 * @param        : @param merId
	 * @param        : @param mobileId
	 * @param        : @param goodsId
	 * @param        : @return
	 * modified      : yangwr ,  Nov 10, 2011  2:54:12 PM
	 * @see          : 
	 * *******************************************
	 */
	MpspMessage cancelMonthUserState(String merId,String mobileId,String goodsId);
	
	/**
	 * ********************************************
	 * method name   : checkMerInfo 
	 * description   : 检查商户信息
	 * @return       : MpspMessage
	 * @param        : merId 商户号
	 * modified      : yangwr ,  Nov 2, 2011  9:25:34 PM
	 * @see          : 
	 * *******************************************
	 */
	public MpspMessage queryMerInfo(String merId);
	
	/**
	 * ********************************************
	 * method name   : checkSign 
	 * description   : 验签处理
	 * @return       : MpspMessage
	 * @param        : @param merId 商户号
	 * @param        : @param plainText 原文
	 * @param        : @param signedText 加签后文本
	 * @param        : @return
	 * modified      : yangwr ,  Nov 2, 2011  9:17:35 PM
	 * @see          : 
	 * *******************************************
	 */
	public MpspMessage checkSign(String merId, String plainText, String signedText);

	/**
	 * ********************************************
	 * method name   : checkTrade 
	 * description   : 调用资源层交易鉴权服务
	 * @return       : MpspMessage
	 * @param        : @param mobileId
	 * @param        : @param merId
	 * @param        : @param goodsId
	 * @param        : @return
	 * modified      : zhaoyan ,  Nov 2, 2011 3:41:01 PM
	 * @see          : 
	 * *******************************************
	 */
	public MpspMessage checkTrade(String mobileId, String merId, String goodsId);
	/**
	 * ********************************************
	 * method name   : queryMonthUserState 
	 * description   : 调用资源层话费包月用户关系定制状态服务
	 * @return       : MpspMessage
	 * @param        : @param merId
	 * @param        : @param mobileId
	 * @param        : @param goodsId
	 * @param        : @return
	 * modified      : yangwr ,  Nov 10, 2011  2:53:31 PM
	 * @see          : 
	 * *******************************************
	 */
	public MpspMessage queryMonthUserState(String merId,String mobileId,String goodsId);
	/**
	 * ********************************************
	 * method name   : queryMerOrder 
	 * description   : 调用资源层商户查询订单服务
	 * @return       : MpspMessage
	 * @param        : @param merId
	 * @param        : @param merDate
	 * @param        : @param orderId
	 * @param        : @return
	 * modified      : yangwr ,  Nov 10, 2011  3:38:16 PM
	 * @see          : 
	 * *******************************************
	 */
	public MpspMessage queryMerOrder(String merId, String merDate, String orderId );


	/** ********************************************
	 * method name   : queryWXPlatOrder 
	 * description   : 调用资源层查询平台临时订单服务
	 * @return       : MpspMessage
	 * @param        : @param platOrderId
	 * @param        : @return
	 * modified      : LiuJiLong ,  2012-11-24 下午05:01:53
	 * @see          : 
	 * ********************************************/      
	public MpspMessage queryWXPlatOrder(String  platOrderId);
	
	/** ********************************************
	 * method name   : saveWXPlatOrder 
	 * description   : 调用资源层查询平台临时订单服务
	 * @return       : MpspMessage
	 * @param        : @param platOrderId
	 * @param        : @return
	 * modified      : LiuJiLong ,  2012-11-24 下午05:01:53
	 * @see          : 
	 * ********************************************/      
	public MpspMessage saveWXPlatOrder(MpspMessage message);
	/**
	 * ********************************************
	 * method name   : queryMerGoodsInfo 
	 * description   : 调用资源层查询商户商品信息服务
	 * @return       : MpspMessage
	 * @param        : @param merId
	 * @param        : @param goodsId
	 * @param        : @return
	 * modified      : zhaoyan ,  Nov 6, 2011 11:46:07  AM
	 * @see          : 
	 * *******************************************
	 */
	public MpspMessage queryMerGoodsInfo(String merId, String goodsId);
	/**
	 * ********************************************
	 * method name   : transacl 
	 * description   : 交易屏蔽校验
	 * @return       : MpspMessage
	 * @param        : @param message 鉴权结果
	 * @param        : @param cmd     原始请求
	 * *******************************************
	 */
	public MpspMessage transacl(MpspMessage message, AbstractOrderCmd cmd);
	/**
	 * ********************************************
	 * method name   : getOrderInfo 
	 * description   : 调用资源层查询话费订单信息服务
	 * @return       : MpspMessage
	 * @param        : @param mobileId
	 * @param        : @param porderId
	 * @param        : @return
	 * modified      : zhaoyan ,  Nov 7, 2011 4:43:46 PM
	 * @see          : 废弃（一期没有用到）
	 * *******************************************
	 */
//	public MpspMessage queryOrderByMobileId(String mobileId, String porderId);
	
	/**
	 * ********************************************
	 * method name   : queryMerReferInf 
	 * description   : 调用资源层查询商户报备信息
	 * @return       : MpspMessage
	 * @param        : merid 商户号
	 * @param		 : goodsid 商品号
	 * *******************************************
	 */
	public MpspMessage queryMerReferInf(String merid, String goodsid) ;
	/** ********************************************
	 * method name   : createWxOrder 
	 * description   : 无线接入商户下单
	 * @return       : MpspMessage
	 * @param        : @param message
	 * @param        : @param cmd
	 * @param        : @return
	 * modified      : panxingwu ,  2012-3-12  下午4:36:27
	 * @see          : 
	 * ********************************************/      
	public MpspMessage createWxOrder(MpspMessage message);

	public MpspMessage createSDkLxWxOrder(MpspMessage message);
/**
	 * ********************************************
	 * method name   : getGoodsBank 
	 * description   : 获取商品银行列表
	 * @return       : MpspMessage
	 * @param        : @param merid
	 * @param        : @param goodsid
	 * @param        : @return
	 * modified      : panxingwu ,  2012-4-11  下午8:37:07
	 * @see          : 
	 * *******************************************
	 */
	public MpspMessage getGoodsBank(String merid, String goodsid);
	/**
	 * ********************************************
	 * method name   : wxStatistics 
	 * description   : 统计无线客户端装机量
	 * @return       : MpspMessage
	 * @param        : @param message
	 * @param        : @return
	 * modified      : panxingwu ,  2012-6-11 上午10:22:54
	 * @see          : 
	 * *******************************************
	 */
	public MpspMessage addWxUser(MpspMessage message);

	/** ********************************************
	 * method name   : getSmsRandomKey 
	 * description   : 获取短信随机验证码
	 * @return       : void
	 * @param        : 
	 * modified      : panxingwu ,  2012-11-21  上午11:16:21
	 * @see          : 
	 * ********************************************/      
	public MpspMessage getSmsRandomKey(String mobileid);

	/** ********************************************
	 * method name   : checkRandomKey 
	 * description   : 动态密码验证
	 * @return       : void
	 * @param        : @param mobileid
	 * modified      : panxingwu ,  2012-11-27  下午4:22:23
	 * @see          : 
	 * ********************************************/      
	public MpspMessage checkRandomKey(MpspMessage message);

	public MpspMessage getHistoryTrans(RequestMsg requestMsg);

	/**
	 * ********************************************
	 * method name   : wxStatistics 
	 * description   : 保存订单
	 * @return       : MpspMessage
	 * @param        : @param message
	 * @param        : @return
	 * modified      : panxingwu ,  2012-11-22 上午10:22:54
	 * @see          : 
	 * *******************************************
	 */
	public MpspMessage createOrder(Map<String, String> reqMap);
	/**
	 * ********************************************
	 * method name   : wxStatistics 
	 * description   : 保存订单
	 * @return       : MpspMessage
	 * @param        : @param message
	 * @param        : @return
	 * modified      : panxingwu ,  2012-11-22 下午14:08:20
	 * @see          : 
	 * *******************************************
	 */
	public MpspMessage updateOrder(Map<String, String> reqMap);

	public MpspMessage getRandomVerifyTimes(String mobileid);

	/**
	 * ********************************************
	 * method name   : wxStatistics 
	 * description   : 查询手机号段信息（话付宝推送短信需求新增）
	 * @return       : MpspMessage
	 * @param        : @param message
	 * @param        : @return
	 * modified      : panxingwu
	 * @see          : 
	 * *******************************************
	 */
	public MpspMessage queryMobileidInf(String mobileid);

	/**
	 * ********************************************
	 * method name   : getWxUserSeg 
	 * description   : 跟据IMEI查询用户手机号码和归属地
	 * @return       : MpspMessage
	 * @param        : @param message
	 * @param        : @return
	 * modified      : panxingwu
	 * @see          : 
	 * *******************************************
	 */
	public MpspMessage getWxUserSeg(RequestMsg requestMsg);

	/** ********************************************
	 * method name   : checkChnlSign 
	 * description   : 渠道验签
	 * @return       : MpspMessage
	 * @param        : @param requestMsg
	 * @param        : @return
	 * modified      : panxingwu ,  2013-3-22  上午9:44:31
	 * @see          : 
	 * ********************************************/      
	public MpspMessage checkChnlSign(String chnlid,String signStr,String unSignStr);

	/** 
	* @Title		: queryQDOrder 
	* @Description	: 查询渠道订单信息
	* @param 		：@param requestMsg
	* @param 		：@return
	* @return 		：MpspMessage 
	* @throws 
	*/
	public MpspMessage queryQDOrder(RequestMsg requestMsg);

	/** 
	* @Title		: addWxUserReplyInf 
	* @Description	: 新增用户反馈信息
	* @param 		：@param requestMsg
	* @param 		：@return
	* @return 		：MpspMessage 
	* @throws 
	*/
	public MpspMessage addWxUserReplyInf(RequestMsg requestMsg);

	/** 
	* @Title		: queryClientConf 
	* @Description	: 获取客户端配置信息
	* @param 		：@param clientName
	* @param 		：@param clientType
	* @param 		：@return
	* @return 		：MpspMessage 
	* @throws 
	*/
	public MpspMessage queryClientConf(String clientName, String clientType);

	/** ********************************************
	 * method name   : getWxOrderVerifyCode 
	 * description   : 获取订单验证码
	 * @return       : MpspMessage
	 * @param        : @param requestMsg
	 * @param        : @return
	 * modified      : panxingwu ,  2013-9-30  上午10:58:55
	 * @see          : 
	 * ********************************************/      
	public MpspMessage getWxOrderVerifyCode(RequestMsg requestMsg);

	/** ********************************************
	 * method name   : checkVerifyCode 
	 * description   : 校验订单验证码是否正确
	 * @return       : MpspMessage
	 * @param        : @param requestMsg
	 * @param        : @return
	 * modified      : panxingwu ,  2013-9-30  上午11:32:11
	 * @see          : 
	 * ********************************************/      
	public MpspMessage checkVerifyCode(RequestMsg requestMsg);
	
	/** ********************************************
	 * method name   : queryWxSDKBind 
	 * description   : 查询无线订单手机绑定关系
	 * @return       : MpspMessage
	 * @param        : @param requestMsg
	 * @param        : @return
	 * modified      : panxingwu ,  2013-9-30  上午11:32:11
	 * @see          : 
	 * ********************************************/   
	public MpspMessage queryWxSDKBind(String iMEI, String iMSI);
	/** ********************************************
	 * method name   : updateSDKpbKey 
	 * description   : 同步更新SDK 公钥
	 * @return       : MpspMessage
	 * @param        : @param requestMsg
	 * @param        : @return
	 * modified      : panxingwu ,  2013-9-30  上午11:32:11
	 * @see          : 
	 * ********************************************/   
	public MpspMessage updateSDKpbKey(RequestMsg requestMsg);

	/** 
	* @Title      : recordClientUser  
	* @param      : @param requestMsg
	* @param      : @return 
	* @return     : MpspMessage 
	* @throws 
	* @Description:  统计终端信息
	*/
	public MpspMessage recordClientUser(RequestMsg requestMsg);

	public MpspMessage getClientUser(RequestMsg requestMsg);
	/** ********************************************
	 * method name   : queryUserMonthlyServiceInfo 
	 * description   : 查询海南彩票包月订购关系
	 * @return       : MpspMessage
	 * @param        : @param mobileId
	 * @param        : @param merId
	 * @param        : @return
	 * modified      : zhuoyangyang ,  2013-12-19  下午04:17:34
	 * @see          : 
	 * ********************************************/   
	
	public MpspMessage queryUserMonthlyServiceInfo(String mobileId,
			String merId);
	
	/** 
	* @Title      : getMwUserLtd  
	* @param      : @param requestMsg
	* @param      : @return 
	* @return     : MpspMessage 
	* @throws 
	* @Description:  统计全网用户交易累计信息
	*/
	public MpspMessage getMwUserLtd(RequestMsg requestMsg);
	/** 
	* @Title      : getHfUserLtd  
	* @param      : @param requestMsg
	* @param      : @return 
	* @return     : MpspMessage 
	* @throws 
	* @Description:  统计用户交易累计信息
	*/
	public MpspMessage getHfUserLtd(RequestMsg requestMsg);

	public MpspMessage queryWxOrder(MpspMessage message);
	/** 
	* @Title      : queryChnlGoodInf  
	* @param      : @param requestMsg
	* @param      : @return 
	* @return     : MpspMessage 
	* @throws 
	* @Description:  查询渠道商品信息
	*/
	public MpspMessage queryChnlGoodInf(String channlid,String merid,String goodsid);
}
