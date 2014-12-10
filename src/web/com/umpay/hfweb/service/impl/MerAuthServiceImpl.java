package com.umpay.hfweb.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;

import com.umpay.hfweb.cache.AbstractCacheFactory;
import com.umpay.hfweb.cache.CommonEcc;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.Advertisement;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.service.MerAuthService;
import com.umpay.hfweb.util.MessageUtil;
import com.umpay.hfweb.util.ObjectUtil;
import com.umpay.hfweb.util.SessionThreadLocal;
import com.umpay.hfbusi.HFBusiDict;

/**
 * ******************  类说明  *********************
 * class       :  MerAuthServiceImpl
 * @author     :  yangwr
 * @version    :  1.0  
 * description :  商户接入访问控制
 * @see        :                        
 * ***********************************************
 */
public class MerAuthServiceImpl implements MerAuthService{
	private static final Logger logger = Logger.getLogger(MerAuthServiceImpl.class);
	private static final String Separator = new String("[,]");
	private MessageSource sysconfSource;
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
	public boolean canAccess(String funCode,String merId){
		boolean accFlag = false;
		String accMers = ObjectUtil.trim(MessageUtil.getLocalProperty(sysconfSource,funCode+".ACCESS"));
		if(!ObjectUtil.isEmpty(accMers)){
			//仅允许登记的商户可通过
			if(!accMers.toUpperCase().equals("ALL")){
				String[] mers = accMers.split(Separator);
				for (String merIdTemp : mers){
					if(merId.equals(merIdTemp)){
						accFlag = true;
						break;
					}
				}
				
			}else{
				//设为ALL，权限无限制
				accFlag = true;
			}
		}else{
			//未登记可通过商户，权限无限制
			accFlag = true;
		}
		//检查是否有禁止访问的配置
		if(accFlag){
			String denyMers = ObjectUtil.trim(MessageUtil.getLocalProperty(sysconfSource,funCode+".DENY"));
			if(!ObjectUtil.isEmpty(denyMers)){
				//登记的商户不允许访问
				if(!denyMers.toUpperCase().equals("ALL")){
					String[] mers = denyMers.split(Separator);
					for (String merIdTemp : mers){
						if(merId.equals(merIdTemp)){
							accFlag = false;
							break;
						}
					}
					
				}else{
					//设为ALL，都不允许访问
					accFlag = false;
				}
			}
		}
		
		//未设置禁止访问，不做处理
		return accFlag;
	}
	/**
	 * <pre>
	 * 查找商户展示哪一套广告
	 * 配置文件中配置举例：
	 * AdSum=10
	 * Ads.1=9996,9997
	 * Ads.10=9998,9999
	 * </pre>
	 * @param merId 商户号
	 * @return
	 */
	public int getAdByMerId(String merId){
		//广告总套数
		String adSum = ObjectUtil.trim(MessageUtil.getLocalProperty(sysconfSource,"AdSum"));
		if(!ObjectUtil.isEmpty(adSum)){
			//查找商户对应的广告
			for(int i=1;i<=Integer.valueOf(adSum);i++){
				String merIds = ObjectUtil.trim(MessageUtil.getLocalProperty(sysconfSource,"Ad.MerId."+String.valueOf(i)));
				if(!ObjectUtil.isEmpty(merIds)){
					if(merIds.indexOf(merId)!=-1){
						return i;
					}
				}
			}
		}
		return 0;
	}
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
	public List<Advertisement> getAds(int adNum){
		String ads = ObjectUtil.trim(MessageUtil.getLocalProperty(sysconfSource,"Ad.Set."+String.valueOf(adNum)));
		if(!ObjectUtil.isEmpty(ads)){
			List<Advertisement> adList=new ArrayList<Advertisement>();
			String[] adStrs=ads.split(",");
			for (String ad : adStrs) {
				String[] adAttri=ad.split("\\|");
				Advertisement adv=new Advertisement();
				adv.setAdName(adAttri[0]);
				if(adAttri.length>1){
					adv.setAdUrl(adAttri[1]);
				}
				adList.add(adv);
			}
			return adList;
		}
		else{
			return null;
		}
	}
	
	/**
	 * <pre>
	 * 更多商户接入访问控制
	 * </pre>
	 * @param funCode 功能码
	 * @param merId 商户号
	 * @param reqInfo 其他请求信息
	 * @return
	 */
	public MpspMessage accessCheck(String funCode,String merId,MpspMessage reqInfo){
		MpspMessage rtnMap = new MpspMessage();
		rtnMap.setRetCode0000();
		String checkIPFlag =  ObjectUtil.trim(reqInfo.getStr(DataDict.NET_CLIENTIP+"#FLAG"));
		if(checkIPFlag.equalsIgnoreCase("true")){//默认不执行,需要执行的话，reqInfo中设置校验标志为真
			String reqIP = ObjectUtil.trim(reqInfo.get(DataDict.NET_CLIENTIP));
			MpspMessage rs = checkIP(merId,reqIP);
			if(!rs.isRetCode0000()){
				ObjectUtil.logInfo(logger, "未通过IP校验 merId:%s,requestIP:%s,retCode:%s",merId,reqIP,rs.getStr(HFBusiDict.MEMO));
				return rs;
			}
			ObjectUtil.logInfo(logger, "已通过IP校验 merId:%s,requestIP:%s",merId,reqIP);
		}
		return rtnMap;
	}
	/**
	 * 校验IP控制
	 * @param funCode
	 * @param merId
	 * @param ip
	 * @return
	 */
	private MpspMessage checkIP(String merId,String ip){
		MpspMessage rtnMap = new MpspMessage();
		//QDHFZF.9996.IP=10.10.38.214,10.10.73.21
		String accIPs = ObjectUtil.trim(MessageUtil.getLocalProperty(sysconfSource,"IP."+merId));
		//1315:商户请求的IP地址未通过校验
		rtnMap.setRetCode("1315");
		rtnMap.put(HFBusiDict.MEMO, "商户请求的IP地址未通过校验");
		if(!ObjectUtil.isEmpty(accIPs)){
			//仅允许登记的商户可通过
			if(!accIPs.toUpperCase().equals("ALL")){
				String[] ips = accIPs.split(Separator);
				for (String ipTemp : ips){
					if(ip.equals(ipTemp)){
						rtnMap.setRetCode0000();
						break;
					}
				}
				
			}else{
				//设为ALL，权限无限制
				rtnMap.setRetCode0000();
			}
		}else{
			//1314:系统没有为商户配置可以发起交易的IP
			rtnMap.setRetCode("1314");
			rtnMap.put(HFBusiDict.MEMO, "系统没有为商户配置可以发起交易的IP");
		}
		return rtnMap;
	}
	
	public String checkChannelPayRate(String mobileId){
		//支付手机号			
		//获取直接支付缓存		
		Cache cache = ((CommonEcc)AbstractCacheFactory.getInstance().getCacheClient("channelPayCache")).getCache();
		//获取直接支付缓存对象（不修改缓存的统计信息）
		Element element = cache.getQuiet(mobileId);
		
		//用户上次下单的时间
		long lastTime = 0;
		//用户直接支付次数
		long payTimes = 0;
				
		if(element==null){
			//如果缓存不存在，则新增缓存，此时lastTime=0,orderTimes=0
			cache.put(new Element(mobileId,mobileId));
		}else{
			//如果缓存存在
			//1、当上次访问时间为0时，说明用户是第二次访问，则上次访问时间为缓存的创建时间
			//2、当上次访问时间不为0是，说明用户是大于二次的访问，则上次访问时间为缓存的上次访问时间
			lastTime = element.getLastAccessTime();
			if(lastTime==0){
				lastTime = element.getCreationTime();
			}
			//重新获取用户下单缓存，主要是更新了缓存的统计信息，并且获取下单次数
			element = cache.get(mobileId);
			payTimes = element.getHitCount();
		}
		
		logger.info("mobileid["+mobileId+"] 上次支付时间["+new Date(lastTime)+"] 支付累计次数["+payTimes+"]");
		//logger.info("mobileid["+mobileId+"] 用户下单次数缓存数量["+cache.getSize()+"]");
		String userPayVConfig = ObjectUtil.trim(MessageUtil.getLocalProperty(sysconfSource,"UserPayV.HFZJZF"));
		if(ObjectUtil.isEmpty(userPayVConfig)){
			userPayVConfig = "1";//默认为1
		}
		//下面为具体的控制规则
		int userPayV = 1;
		try{
			userPayV = Integer.valueOf(userPayVConfig);
		}catch(Exception e){
			String rpid = ObjectUtil.trim(SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
			logger.error(ObjectUtil.handlerException(e, rpid));
		}
		logger.info("获取到系数为:"+userPayV);
		//计算两笔订单的时间价格=系数*下单次数的立方
		long interval = userPayV * payTimes * payTimes * 1000;
		
		if(lastTime!=0&&(System.currentTimeMillis()-lastTime) < interval){
			logger.info("mobileid["+mobileId+"] 当前直接支付时间["+new Date()+"] 上次直接时间["+new Date(lastTime)+"] 支付累计次数["+payTimes+"] 交易间隔时间["+interval+"]被屏蔽");
			if(payTimes==userPayV){
				return "1316";
			}else{
				return "1317";
			}
		}
		return DataDict.SUCCESS_RET_CODE;
	}
	
	public void setSysconfSource(MessageSource sysconfSource) {
		this.sysconfSource = sysconfSource;
	}
}