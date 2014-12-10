package com.umpay.hfweb.service;

import java.util.List;

import com.umpay.hfweb.model.Advertisement;
import com.umpay.hfweb.model.MpspMessage;

public interface MerAuthService {

	/**
	 * <pre>
	 * 商户接入访问控制
	 * 配置文件中配置举例：后台直连下单
	 * HTXD.ACCESS=ALL
	 * HTXD.DENY=ALL or HTXD.DENY=9996
	 * </pre>
	 * @param funCode 功能码
	 * @param merId 商户号
	 * @return
	 */
	public boolean canAccess(String funCode,String merId);
	/**
	 * <pre>
	 * 查找商户展示哪一套广告，没有配置则返回0
	 * 配置文件中配置举例：
	 * AdSum=10
	 * Ads.1=9996,9997
	 * Ads.10=9998,9999
	 * </pre>
	 * @param merId 商户号
	 * @return
	 */
	public int getAdByMerId(String merId);
	/**
	 * <pre>
	 * 查找广告套件中的文件名称
	 * 配置文件中配置举例：
	 * Ad.Set.1=1-1.gif,1-2.jpg,1-3.jpg
	 * Ad.Set.2=2-1.jpg,2-2.jpg,2-3.jpg
	 * Ad.Set.3=3-1.gif,3-2.gif,3-3.gif
	 * </pre>
	 * @param merId 商户号
	 * @return
	 */
	public List<Advertisement> getAds(int adNum);
	/**
	 * <pre>
	 * 更多商户接入访问控制
	 * </pre>
	 * @param funCode 功能码
	 * @param merId 商户号
	 * @param reqInfo 其他请求信息
	 * @return
	 */
	public MpspMessage accessCheck(String funCode,String merId,MpspMessage reqInfo);
	/**
	 * 直接支付频率限制
	 * @param mobileId 发起支付的手机号
	 * @return
	 */
	public String checkChannelPayRate(String mobileId);
}
