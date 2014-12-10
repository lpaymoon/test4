package com.umpay.hfweb.service.impl;

import java.util.HashMap;
import java.util.Map;

import net.sf.ehcache.Element;

import org.apache.log4j.Logger;

import com.bs.mpsp.util.DateTimeUtil;
import com.bs2.core.ext.Service4QObj;
import com.bs2.mpsp.XmlMobile;
import com.umpay.api.util.StringUtil;
import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.param.SmsParam;
import com.umpay.hfweb.cache.AbstractCacheFactory;
import com.umpay.hfweb.cache.CommonEcc;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.service.MessageService;
import com.umpay.hfweb.service.SmsService;
import com.umpay.hfweb.util.ObjectUtil;
import com.umpay.hfweb.util.SessionThreadLocal;
import com.umpay.hfweb.util.SystemHelper;

public class SmsServiceImpl implements SmsService{
	public static Logger logger = Logger.getLogger(SmsServiceImpl.class);
	private static final String Sended_SMS_Count= "intSendedCount";
	private MessageService messageService;
	private Service4QObj smsQueue;
	private Service4QObj alarmQueue;

	/** sms标签 funCode*/
	private static final String SMS_TAG_FUNCODE = "funCode";
	/** sms标签 subCode*/
	private static final String SMS_TAG_SUBCODE = "subCode";
	/** sms标签 content*/
	private static final String SMS_TAG_CONTENT = "content";
	/** sms标签 calling*/
	private static final String SMS_TAG_CALLING = "calling";
	/** sms标签 rpid*/
	private static final String SMS_TAG_RPID = "rpid";
	/** sms标签 ISNOTIFY*/
	private static final String SMS_TAG_ISNOTIFY = "ISNOTIFY";
	/** sms标签 ISTEST*/
	private static final String SMS_TAG_ISTEST = "ISTEST";
	/** sms标签 MERID*/
	private static final String SMS_TAG_MERID = "merId";
	
	public boolean pushCaptchaSms(Map<String,String> smsMap){
		String funCode = messageService.getSystemParam("Sms.FunCode.QRZF");
		if(ObjectUtil.isEmpty(funCode)){
			throw new IllegalArgumentException("Sms.FunCode.QRZF is empty!");
		}
		smsMap.put(SMS_TAG_FUNCODE, funCode);
		smsMap.put(SMS_TAG_RPID, getRpid());
		smsMap.put(SMS_TAG_ISNOTIFY, "TRUE");
		smsMap.put(SMS_TAG_CALLING, smsMap.get(HFBusiDict.MOBILEID));
		
		if(checkIsTest()){
			smsMap.put(SMS_TAG_ISTEST,"TRUE");
		}
		smsQueue.putJob(smsMap);
		logInfo("PushSms In Queue Success---下发验证码短信入队列成功");
		return true;
	}
	
	/**
	 * 下发常规短信
	 * @param info
	 */
	public boolean pushSms(String merId,String mobileId,String smsContent){
		Map<String,String> paramMap = new HashMap<String,String>();
		String funCode = messageService.getSystemParam("Sms.Funcode.ZJXF");
		if(ObjectUtil.isEmpty(funCode)){
			throw new IllegalArgumentException("Sms.Funcode.ZJXF is empty!");
		}
		paramMap.put(SMS_TAG_FUNCODE, funCode);
		paramMap.put(SMS_TAG_SUBCODE, "1000");
		paramMap.put(SMS_TAG_CONTENT, smsContent);
		paramMap.put(SMS_TAG_CALLING, mobileId);
		paramMap.put(SMS_TAG_RPID, getRpid());
		paramMap.put(SMS_TAG_ISNOTIFY, "TRUE");
		paramMap.put(SMS_TAG_MERID, merId);
		if(checkIsTest()){
			paramMap.put(SMS_TAG_ISTEST,"TRUE");
		}
		smsQueue.putJob(paramMap);
		logInfo("PushSms In Queue Success---下发常规短信入队列成功");
		return true;
	}

	/**
	 * ********************************************
	 * method name   : sendUserOrderLtdSms 
	 * description   : 发送用户第一次达到下单频率阀值提醒短信
	 * @return       : boolean
	 * @param        : @param mobileId 手机号
	 * @see          : 
	 * *******************************************
	 */
    public boolean sendUserOrderLtdSms(String mobileId){
		String funCode = messageService.getSystemParam("Sms.Funcode.ZJXF");
		if(ObjectUtil.isEmpty(funCode)){
			throw new IllegalArgumentException("Sms.Funcode.ZJXF is empty!");
		}
		Map<String,String> paramMap = new HashMap<String,String>();
		paramMap.put(SMS_TAG_FUNCODE, funCode);
		paramMap.put(SMS_TAG_SUBCODE, "1001");
		paramMap.put(SMS_TAG_CONTENT, "");
		paramMap.put(SMS_TAG_CALLING, mobileId);
		paramMap.put(SMS_TAG_RPID, getRpid());
		paramMap.put(SMS_TAG_ISNOTIFY, "TRUE");
		paramMap.put(SMS_TAG_MERID, "");
		if(checkIsTest()){
			paramMap.put(SMS_TAG_ISTEST,"TRUE");
		}
		smsQueue.putJob(paramMap);
		logInfo("sendUserOrderLtdSms In Queue Success---下单频率短信入队列成功");
		return true;
    }
	
	public boolean pushPaySms(Map<String, String> smsMap){
		Map<String, String> map = new HashMap<String, String>();
		map.put(HFBusiDict.CALLED, smsMap.get(HFBusiDict.CALLED));
		map.put(HFBusiDict.GOODSNAME, smsMap.get(HFBusiDict.GOODSNAME));
		map.put(HFBusiDict.BANKID, smsMap.get(HFBusiDict.BANKID));
		map.put(HFBusiDict.GOODSID, smsMap.get(HFBusiDict.GOODSID));
		map.put(HFBusiDict.MERID, smsMap.get(HFBusiDict.MERID));
		map.put(HFBusiDict.SERVTYPE, smsMap.get(HFBusiDict.SERVTYPE));
		map.put(HFBusiDict.CALLING, smsMap.get(HFBusiDict.CALLING));
		String servMonth = smsMap.get(HFBusiDict.SERVMONTH);
		if(ObjectUtil.isNotEmpty(servMonth)){
			if(servMonth.equals("1")){
				map.put(HFBusiDict.SERVMONTH, "");
			}else{
				map.put(HFBusiDict.SERVMONTH, servMonth);
			}
		}
		map.put(HFBusiDict.AMOUNT, smsMap.get(HFBusiDict.AMOUNT));
		map.put(HFBusiDict.VERIFYCODE, smsMap.get(HFBusiDict.VERIFYCODE));
		map.put(HFBusiDict.PORDERID, smsMap.get(HFBusiDict.PORDERID));
		String funCode = messageService.getSystemParam("Sms.FunCode.QRZF");
		if(ObjectUtil.isEmpty(funCode)){
			throw new IllegalArgumentException("Sms.Funcode.QRZF is empty!");
		}
		long dt = DateTimeUtil.currentDateTime();
		map.put(HFBusiDict.REQDATE, DateTimeUtil.getDateString(dt));
		map.put(HFBusiDict.REQTIME, DateTimeUtil.getTimeString(dt));
		map.put(XmlMobile.FUNCODE, funCode);
		map.put(SMS_TAG_ISNOTIFY, "TRUE");
		map.put(HFBusiDict.RPID, getRpid());
		if(!checkIsTest()){
			map.put("ISTEST", "FALSE");
		}else{
			map.put("ISTEST", "TRUE");
		}
		smsQueue.putJob(map);
		logInfo("pushPaySms In Queue Success--r-二次确认短信入队列成功");
		return true;
	}
	public static void main(String[] args) {
		System.out.println("1.4.24.34".split(".").length);
	}
	/**
	 * ********************************************
	 * method name   : alarm 
	 * description   : 报警功能
	 * @return       : void
	 * @param        : @param out
	 * modified      : lingling ,  Nov 17, 2011  7:46:50 PM
	 * @see          : 
	 * *******************************************
	 */
	public void alarm(Map<String,Object> out){
		try{
			//获取报警配置信息
			//需要进行报警的返回码
			String alarmRetCode =  messageService.getSystemParam("sys.alarm.retcode", "");
			//需要报警的业务处理时间
			String alarmSpendTime = messageService.getSystemParam("sys.alarm.spendtime", "5000");
			//业务返回码
			String retCode = (String)out.get(DataDict.RET_CODE);
			
			//1、验证返回码是否需要报警
			if(alarmRetCode.indexOf(retCode)!=-1){
				alarmNew(out);
				//alarm(retCode,out);
			}
			//业务处理时间
			try{
				String strSpendTime = (String) out.get(DataDict.REQ_USE_TIME);
				if(ObjectUtil.isEmpty(strSpendTime)){
					return;
				}
				long spendTime = Long.parseLong(strSpendTime);
				long _spendTime = Long.parseLong(alarmSpendTime);
				if(spendTime>_spendTime){
					logInfo("执行业务超时报警");
					alarmNew(out);
					//alarm(DataDict.SYSTEM_ALARM_TIMEOUT,out);
				}
			}catch(Exception e){
				logInfo("业务处理超时报警出异常:"+e.getMessage());
			}

		}catch(Exception e){
			//报警异常
			logInfo(ObjectUtil.handlerException(e, SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID)));
		}
	}

	/** *****************  方法说明  *****************
	 * method name   :  alarmNew
	 * @param		 :  @param out
	 * @return		 :  void
	 * @author       :  LiuJiLong 2014-4-2 上午09:18:31
	 * description   :  增加报警中心的报警方式 
	 * @see          :  
	 * ***********************************************/
	private void alarmNew(Map<String, Object> out) {
		String reIp = (String) out.get("serverIp");
		if(reIp!=null&&reIp.matches("\\d+.\\d+.\\d+.\\d+")&&reIp.split("\\.").length==4){
			out.put(HFBusiDict.ADDRESS, reIp.split("\\.")[3]);
		}else{
			out.put(HFBusiDict.ADDRESS, reIp);
		}
		out.put(HFBusiDict.SERVICEID, "WEB");
		out.put("tradeIp", SessionThreadLocal.getSessionValue("tradeIp"));
		alarmQueue.putJob(out);
	}
	
	/**
	 * ********************************************
	 * method name   : alarm 
	 * description   : 根据返回码进行报警
	 * @return       : void
	 * @param        : @param retCode
	 * modified      : lingling ,  Nov 17, 2011  8:21:14 PM
	 * @see          : 
	 * *******************************************
	 */
	private void alarm(String retCode,Map<String,Object> out) throws Exception{
		
		//警报缓存的tags
		String alarmTags = messageService.getSystemParam("sys.alarm.tags","");
		//报警累计次数
		String alarmTimes = messageService.getSystemParam("sys.alarm.times","3");
		int times = Integer.parseInt(alarmTimes);
		//警报在一定时间内达到报警次数才报警
		String alarmInterval = messageService.getSystemParam("sys.alarm.interval","2000");
		long interval = Integer.parseInt(alarmInterval);
		
		//同一类型报警在一个缓存的生命周期内下发的最大短信条数
		String maxSms = messageService.getSystemParam("sys.alarm.maxsms","5");
		long intMaxSms = Integer.parseInt(maxSms);
		
		String[] tagsStr = alarmTags.split(",");
		//没有配置报警标签时退出
		if(tagsStr.length==0){
			logInfo("没有配置报警,报警退出");
			return;
		}
		
		//报警标签配置方式为tag1.tag2,tag1.tag3,...
		for(String tagArray : tagsStr){
			String[] tagArr = tagArray.split("[.]");
			if(tagArr.length==0){
				continue;
			}
			StringBuffer sb= new StringBuffer();
			sb.append(retCode);
			for(String tag:tagArr){
				sb.append(".");
				Object value = out.get(tag);
				sb.append(value);				
			}
			String key = sb.toString();
			CommonEcc ecc = (CommonEcc)AbstractCacheFactory.getInstance().getCacheClient("alarm");
			Element cache = ecc.getElementByDefaultMap(key,Sended_SMS_Count,new Integer(0));
			long inter = System.currentTimeMillis() - cache.getLastUpdateTime();
			
			//更改cache的更新时间，用于计算上次报警时间
			cache.updateUpdateStatistics();
			
			//累计次数超限并且间隔时间过小报警
			//增加一个同类型报警，已发送短信条数的限制，超过限制将不再发短信
			long hitCount = cache.getHitCount();
			Map<String,Object> cacheInfo = (Map<String,Object>)cache.getValue();
			Integer intSendedCount = (Integer)cacheInfo.get(Sended_SMS_Count);
			if(intSendedCount < intMaxSms && times < hitCount && interval > inter){	
				logInfo(String.format("key[%s] out[%s] 下发报警短信", key,out));
				String machineFlag = "";//机器标识
				try{
					machineFlag = SystemHelper.getSystemLocalIp().getHostAddress();
					if(ObjectUtil.isEmpty(machineFlag)){
						machineFlag = SystemHelper.getSystemHostName();
					}
				}catch(Exception e){
					e.printStackTrace();
					try{
						machineFlag = SystemHelper.getSystemHostName();
					}catch(Exception ee){
						ee.printStackTrace();
					}
				}
				//短信前缀
				StringBuffer smsContent = new StringBuffer();
				smsContent.append("HFWEBBUSI(");
				smsContent.append(machineFlag);
				smsContent.append("):").append(key);
				String strMobiles = messageService.getSystemParam("sys.alarm.mobiles");
				if(!ObjectUtil.isEmpty(strMobiles)){
					logInfo("执行报警短信下发");
					this.pushSms("",strMobiles,smsContent.toString());
				}else{
					logInfo("告警手机号列表中为空");
				}
				intSendedCount++;
				cacheInfo.put(Sended_SMS_Count, intSendedCount);				
			}else{
				logInfo("报警条件未满 累计次数,间隔时间,已发条数 alarm-not-send-sms:key=%s times %s:%s,interval %s:%s,sendedCount %s:%s",key,times,hitCount,interval,inter,intMaxSms,intSendedCount);
			}
		}
	}
	public SmsParam genSmsInfo(String servType, String servMonth, String goodsId, String porderId) {
		SmsParam smsParam = new SmsParam();
		String smsFrom = "";
		String smsSub = messageService.getSystemParam("smsPrex");
		//短信解析做加4处理
//		boolean test = checkIsTest();
//		if (test){
//			smsFrom += "4";
//		}
		if (ObjectUtil.isNotEmpty(servType) && !servType.equals("2")) {
			smsFrom += "8" + goodsId;
		} else {
			smsFrom += "2" + porderId;
		}
		smsSub += smsFrom;
		smsParam.setSmsFrom(smsFrom);
		smsParam.setSmsSub(smsSub);
		smsParam.setSendCount(1);
		smsParam.setServType(servType);
		smsParam.setServMonth(servMonth);
		return smsParam;
	}
	 /**
     * 校验系统类型
     * @return
     */
    private boolean checkIsTest(){
    	boolean rnt = false;
		// 0:生产系统 其他：测试系统
		String type = messageService.getSystemParam("SystemType");
		if (!type.equals("0")) {
			rnt = true;
		}
		return rnt;
    }
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}

	public void setAlarmQueue(Service4QObj alarmQueue) {
		this.alarmQueue = alarmQueue;
	}
	public void setSmsQueue(Service4QObj smsQueue) {
		this.smsQueue = smsQueue;
	}
	private String getRpid(){
		String rpid = ObjectUtil.trim(SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
		return rpid;
	}
	private void logInfo(String message,Object... args){
		String rpid = SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID);
		String funCode = SessionThreadLocal.getSessionValue(DataDict.FUNCODE);
		logger.info(String.format("%s,%s,%s",funCode,rpid,String.format(message,args)));
	}
	public SmsParam genSmsInfoSpecial(String servType, String servMonth, String goodsId, String porderId,String merId){
		SmsParam smsParam = genSmsInfo(servType,servMonth,goodsId,porderId);
		String smsSub = messageService.getSystemParam("smsPrex");
		String smsFrom = "1" + porderId;
		smsSub += smsFrom;
		smsParam.setSmsSub(smsSub);
		smsParam.setSmsFrom(smsFrom);
		return smsParam;
	}

	public boolean  pushPayOkSms(Map<String, Object> smsMap) {
		
		Map<String, String> map = new HashMap<String, String>();
		map.put(HFBusiDict.GOODSDESC, StringUtil.trim(smsMap.get(HFBusiDict.GOODSDESC)));
		map.put(HFBusiDict.CALLED, StringUtil.trim(smsMap.get(HFBusiDict.CALLED)));
		map.put(HFBusiDict.GOODSNAME, StringUtil.trim(smsMap.get(HFBusiDict.GOODSNAME)));
		map.put(HFBusiDict.BANKID, StringUtil.trim(smsMap.get(HFBusiDict.BANKID)));
		map.put(HFBusiDict.GOODSID, StringUtil.trim(smsMap.get(HFBusiDict.GOODSID)));
		map.put(HFBusiDict.MERID, StringUtil.trim(smsMap.get(HFBusiDict.MERID)));
		map.put(HFBusiDict.SERVTYPE, StringUtil.trim(smsMap.get(HFBusiDict.SERVTYPE)));
		map.put(HFBusiDict.CALLING, StringUtil.trim(smsMap.get(HFBusiDict.MOBILEID)));
//		map.put(HFBusiDict.RPID, StringUtil.trim(smsMap.get(HFBusiDict.RPID)));
		map.put(HFBusiDict.PORDERID, StringUtil.trim(smsMap.get(HFBusiDict.PORDERID)));
		map.put(HFBusiDict.AMOUNT, StringUtil.trim(smsMap.get(HFBusiDict.AMOUNT)));
		map.put(HFBusiDict.CUSPHONE, StringUtil.trim(smsMap.get(HFBusiDict.CUSPHONE)));
		map.put(HFBusiDict.PUSHINF, StringUtil.trim(smsMap.get(HFBusiDict.PUSHINF)));
		map.put(HFBusiDict.MTNUM, StringUtil.trim(smsMap.get(HFBusiDict.MTNUM)));
//		map.put(HFBusiDict.SERVICEID, StringUtil.trim(smsMap.get(HFBusiDict.SERVICEID)));
		map.put(HFBusiDict.CHANNELSRC, StringUtil.trim(smsMap.get(HFBusiDict.CHANNELSRC)));
		map.put(HFBusiDict.RETCODE, StringUtil.trim(smsMap.get(HFBusiDict.RETCODE)));
		map.put(HFBusiDict.SEQID, "1"+StringUtil.trim(smsMap.get(HFBusiDict.PORDERID)));
		map.put(HFBusiDict.MONTHPAYLMT, StringUtil.trim(smsMap.get(HFBusiDict.MONTHPAYLMT)) );//月限制额
		map.put(HFBusiDict.MONTHPAYED, StringUtil.trim(smsMap.get(HFBusiDict.MONTHPAYED)) );//月消费额
		//新增
		map.put(HFBusiDict.FEECODE, StringUtil.trim(smsMap.get(HFBusiDict.FEECODE)));
		map.put(HFBusiDict.FEETYPE, StringUtil.trim(smsMap.get(HFBusiDict.FEETYPE)));
		map.put(HFBusiDict.SERVICEID, StringUtil.trim(smsMap.get(HFBusiDict.SERVICEID)));
		
		long dt = DateTimeUtil.currentDateTime();
		map.put(HFBusiDict.REQDATE, DateTimeUtil.getDateString(dt));
		map.put(HFBusiDict.REQTIME, DateTimeUtil.getTimeString(dt));
		map.put(XmlMobile.FUNCODE, "HFDXZF");
		map.put(SMS_TAG_ISNOTIFY, "TRUE");
		map.put(HFBusiDict.RPID, getRpid());
		if(!checkIsTest()){
			map.put("ISTEST", "FALSE");
		}else{
			map.put("ISTEST", "TRUE");
		}
		smsQueue.putJob(map);
		logInfo("pushPayOkSms In Queue Success--r7-支付成功短信入队列成功");
		return true;
		
	}

	public boolean pushRandomKeySms(Map<String, Object> smsMap) {
		// TODO Auto-generated method stub
		Map<String,String> paramMap = new HashMap<String,String>();
        
		paramMap.put(SMS_TAG_FUNCODE, "HFYZMQR");
		paramMap.put(HFBusiDict.SUBNO, StringUtil.trim(smsMap.get(HFBusiDict.MERID)));
		paramMap.put(HFBusiDict.VERIFYCODE, StringUtil.trim(smsMap.get(HFBusiDict.VERIFYCODE)));
		paramMap.put(HFBusiDict.AMOUNT, StringUtil.trim(smsMap.get(HFBusiDict.AMOUNT)));
		paramMap.put(HFBusiDict.GOODSNAME, StringUtil.trim(smsMap.get(HFBusiDict.GOODSNAME)));
		paramMap.put(HFBusiDict.BANKID, StringUtil.trim(smsMap.get(HFBusiDict.BANKID)));
		paramMap.put(HFBusiDict.SERVTYPE, StringUtil.trim(smsMap.get(HFBusiDict.SERVTYPE)));
		paramMap.put(SMS_TAG_CALLING, StringUtil.trim(smsMap.get(HFBusiDict.MOBILEID)));
		paramMap.put(SMS_TAG_ISNOTIFY, "TRUE");
		paramMap.put(HFBusiDict.MERID, StringUtil.trim(smsMap.get(HFBusiDict.MERID)));
		paramMap.put(HFBusiDict.GOODSID, StringUtil.trim(smsMap.get(HFBusiDict.GOODSID)));
		if(checkIsTest()){
			paramMap.put(SMS_TAG_ISTEST,"TRUE");
		}
		paramMap.put(HFBusiDict.RPID, getRpid());
		smsQueue.putJob(paramMap);
		logInfo("PushSms In Queue Success---下发常规短信入队列成功");
		return true;
	}
}
