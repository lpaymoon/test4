package com.umpay.hfweb.service.impl;

import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;

import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.service.CheckService;
import com.umpay.hfweb.util.CheckDataUtil;
import com.umpay.hfweb.util.MessageUtil;
import com.umpay.hfweb.util.ObjectUtil;

/**
 * ******************  类说明  *********************
 * class       :  DefaultCheckServiceImpl
 * @author     :  yangwr
 * @version    :  1.0  
 * description :  检验输入参数
 * @see        :                        
 * ***********************************************
 */
public class DefaultCheckServiceImpl implements CheckService{
	public static Logger log = Logger.getLogger(DefaultCheckServiceImpl.class);
	private MessageSource validateSource;
	private static final String Separator = new String("[,]");
	
	/**
	 * ********************************************
	 * method name   : doCheck 
	 * modified      : yangwr ,  Nov 2, 2011
	 * @see          : @see com.umpay.hfweb.service.CheckService#doCheck(com.umpay.hfweb.model.MpspMessage)
	 * *******************************************
	 */
	public MpspMessage doCheck(MpspMessage paraMessage){
		MpspMessage rtnMap = new MpspMessage();
		rtnMap.setRetCode0000();
		String funCode = (String) paraMessage.get(DataDict.FUNCODE);
		log.info("FUNCODE为："+funCode);
		// 获取该业务的请求数据列表
		String keys = this.getLocalProperty("FUNCODE."+funCode);
		log.info("获得的配置key为："+keys);
		boolean isPass = false;
		if(ObjectUtil.isEmpty(keys)){
			log.debug("未找到请求数据keys配置文件！目前校验通过！");
			rtnMap.put(DataDict.RET_CODE, DataDict.SYSTEM_ERROR_CODE);
			rtnMap.put("message", "未找到请求数据keys配置文件");
			isPass = false;
		}else{
			String[] keys2Check = keys.split(Separator);
			String v = null;// 需要校验的字段值
			// 检查每个字段的格式是否正确
			for (String key : keys2Check){
				if (key.startsWith("[") && key.endsWith("]")){
					// 非必须校验字段
					key = key.substring(1, key.length() - 1);
					v = (String) paraMessage.get(key);
					if (ObjectUtil.isEmpty(v)){
						// 非必须校验字段，未填写不校验，校验通过
						continue;
					}
				} else {
					// 必须校验字段，不能为空
					v = (String)paraMessage.get(key);
					// 判断请求数据是否为空
					if (ObjectUtil.isEmpty(v))
					{
						log.debug("请求数据不足，" + key + "字段不存在");
						//1111:参数不全
						rtnMap.put(DataDict.RET_CODE, "1111");
						rtnMap.put("message", "请求数据不足，" + key + "字段不存在");
						return rtnMap;// 请求数据不足
					}
				}
				
				String regTag = this.getLocalProperty("REGEXP."+key);
				if (ObjectUtil.isEmpty(regTag))
				{
					log.debug("未配置输入参数的校验规则信息！目前校验通过！");
					continue;
				}
				String[] regs = regTag.split("[@]");
				
				boolean isOk = false;
				for (String reg : regs){
					// 检查字段格式
					if (CheckDataUtil.check(v, reg))
					{
						isOk = true;
						break;
					} 
				}
				if (!isOk){
					log.debug(key + "字段校验未通过，数据值：" + v + "，校验正则式：" + regTag);
					String ret = this.getLocalProperty("RETCODE."+key);
					if (ObjectUtil.isEmpty(ret)){
						//1112:参数不正确
						rtnMap.put("retCode", "1112");
					}else{
						rtnMap.put(DataDict.RET_CODE, ret);
					}
					rtnMap.put("message", key + "字段校验未通过，数据值：" + v + "，校验正则式：" + regTag);
					return rtnMap;
				}
			}
			isPass = true;
		}
		if (isPass){
			log.debug("数据校验通过！");
		} else {
			log.debug("数据校验未通过！");
		}
		return rtnMap;
	}
	
	/**
	 * 获得配置信息
	 * 
	 * @param key
	 * @return
	 */
	public String getLocalProperty(String key) {
		String rtn = "";
		rtn = MessageUtil.getLocalProperty(validateSource,key);
		log.debug("getLocalMsg() key[" + key + "] localMsg[" + rtn + "]");
		return rtn;
	}

	public void setValidateSource(MessageSource validateSource) {
		this.validateSource = validateSource;
	}
	
}
