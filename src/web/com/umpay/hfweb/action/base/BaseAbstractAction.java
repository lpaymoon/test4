package com.umpay.hfweb.action.base;

import java.io.File;
import java.security.PrivateKey;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.bs.mpsp.util.FileUtil;
import com.bs.mpsp.util.SignUtil;
import com.bs.utils.Base64;
import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.command.PageOrderCmd;
import com.umpay.hfweb.action.param.OrderParam;
import com.umpay.hfweb.common.LogTemplateHandler;
import com.umpay.hfweb.common.LoggerManager;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.exception.BusinessException;
import com.umpay.hfweb.exception.WebBusiException;
import com.umpay.hfweb.service.CheckService;
import com.umpay.hfweb.service.MerAuthService;
import com.umpay.hfweb.service.MessageService;
import com.umpay.hfweb.service.RestService;
import com.umpay.hfweb.service.SmsService;
import com.umpay.hfweb.service.TradeService;
import com.umpay.hfweb.util.ObjectUtil;
import com.umpay.hfweb.util.SequenceUtil;
import com.umpay.hfweb.util.SessionThreadLocal;
public abstract class BaseAbstractAction extends AbstractController{
	/**简要日志*/
	private static final Logger log_ = LoggerManager.getMpspLogger();
	protected Logger logger = Logger.getLogger(getClass());
	protected RestService restService;
	protected TradeService tradeService;
	protected MessageService messageService;
	protected CheckService checkService;
	protected MerAuthService merAuthService;
	protected SmsService smsService;
	private LogTemplateHandler mpspLogHandler;
	protected final String ERROR_PAGE = "web_error";
	protected final String ERROR_WAP = "wap_error";
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws WebBusiException {
			long beginTime = System.currentTimeMillis();
			String pgView = null; 
			Map<String,Object> modelMap = new HashMap<String,Object>();
			Map<String,Object> logInfo = new HashMap<String, Object>();
			try{
				//1-1生成RPID
				createTransRpid(request);
				//1-2 设置功能码
				SessionThreadLocal.setSessionValue(DataDict.FUNCODE, getFunCode());
				logInfo("Bussiness Process Start");
				//2-日志预处理
				initMap4Log(request,logInfo);
				String blackUrl = ObjectUtil.trim(messageService.getSystemParam("requestBlackUrl"));
				logInfo("[配置文件中所有的黑名单:]%s",blackUrl);
				boolean blackRefererFlag = false;
				if(!"".equals(blackUrl)){
					String referer = ObjectUtil.trim((String)logInfo.get(DataDict.NET_REFERER));
					if(!"".equals(referer)){
						String urls[] =  blackUrl.split(",");
						for(String url : urls){
							if(referer.indexOf(url) > -1){
								logInfo("[配置文件中存在和request Referer存在相同的信息:]%s",url);
								blackRefererFlag = true;
								break;
							}		
						}
					}
				}
				if(blackRefererFlag){
					logInfo("请求的referer在黑名单中，请求业务受理拒绝");
					pgView = "";	
				}else{
					logInfo("请求的referer不在黑名单中或无需做黑名单处理，请求业务受理接受");
					//3-业务处理
					pgView = processBussiness(request,response,modelMap);
				}
				//4-后处理(业务完成后再处理)
				afterProcess(modelMap, logInfo);
			}catch(Exception e){
				WebBusiException we = new WebBusiException(DataDict.SYSTEM_ERROR_CODE,e);
				we.setMessage(messageService.getMessage(DataDict.SYSTEM_ERROR_CODE));
				we.setMessageDetail(messageService.getMessageDetail(DataDict.SYSTEM_ERROR_CODE));
				we.setFunCode(getFunCode());
				logInfo.put(DataDict.RET_CODE, we.getCode());
				logInfo.put(DataDict.RET_MSG, filterSpecialChar(we.getMessage()));
				logError(we);
				throw we;
			}finally{
				long useTime = System.currentTimeMillis() - beginTime;
				logInfo.put(DataDict.REQ_USE_TIME, String.valueOf(useTime));
				//简要日志
				log_.info(mpspLogHandler.createLog(logInfo));
				logInfo("Bussiness Process End [RetCode]:%s useTime:%s",logInfo.get(DataDict.RET_CODE),useTime);
				//系统报警检查
				smsService.alarm(logInfo);
			}
			//非页面跳转
			if(ObjectUtil.isEmpty(pgView)){
				logInfo("非本系统页面间正常跳转");
				return null;
			}
			return new ModelAndView(pgView,modelMap);
	}
	/**
	 * ********************************************
	 * method name   : processBussiness 
	 * description   : 业务处理
	 * @return       : String
	 * @param        : @param request
	 * @param        : @param response
	 * @param        : @param modelMap
	 * @see          : 
	 * *******************************************
	 */
	protected abstract String processBussiness(HttpServletRequest request,
			HttpServletResponse response,Map<String,Object> modelMap)throws BusinessException;
	/**
	 * ********************************************
	 * method name   : getFunCode 
	 * description   : 具体业务实现的功能码
	 * @return       : String
	 * *******************************************
	 */
	protected abstract String getFunCode();
	
	/**
	 * ********************************************
	 * method name   : isVer3 
	 * description   : 说明接入版本,默认3.0
	 * @return       : boolean
	 * *******************************************
	 */
	protected boolean isVer3(){
		return true;
	}
		
	/**
	 * ********************************************
	 * method name   : getTransRpid 
	 * description   : 生成rpId,并存储到ThreadLocal中
	 * @return       : String
	 * @param        : @param merId
	 * *******************************************
	 */
	protected String createTransRpid(HttpServletRequest request) {
		String rpid = "";
		String merId = ObjectUtil.trim(request.getParameter(DataDict.MER_REQ_MERID));
		if(merId==null||"".equals(merId)){
			merId = ObjectUtil.trim(request.getParameter("mer_id"));//U付平台用mer_id
		}
		if(!isVer3()){
			merId = ObjectUtil.trim(request.getParameter(DataDict.MER_REQ_MERID_V2));
		}
		if(ObjectUtil.isNotEmpty(merId)){
			String sid = request.getSession().getId();
			String prefix = "0";
			if(!ObjectUtil.isEmpty(sid) && sid.length() > 1){
				prefix = sid.substring(0, 1);
			}
			SequenceUtil su = SequenceUtil.getInstance();
			rpid = "W" + ObjectUtil.trim(merId) + prefix + SequenceUtil.formatSequence(su.getSequence4File("hfWebBusi.rpid"), 10);
			request.getSession().setAttribute(DataDict.REQ_MER_RPID,rpid);

			SessionThreadLocal.setSessionValue(DataDict.REQ_MER_RPID, rpid);
		}
		return rpid;
	}
	/**
	 * ********************************************
	 * method name   : createMap4Log 
	 * description   : 创建存储日志信息的Map
	 * @return       : Map<String,String>
	 * @param        : @param request
	 * @param        : @return
	 * modified      : yangwr ,  Nov 17, 2011  4:27:21 PM
	 * @see          : 
	 * *******************************************
	 */
	@SuppressWarnings("unchecked")
	protected void initMap4Log(HttpServletRequest request,Map<String,Object> mpspMap){
		String rpid = SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID);
		mpspMap.put(DataDict.FUNCODE, getFunCode());
		mpspMap.put(DataDict.REQ_MER_RPID, rpid);
		String sessionId = request.getSession(true).getId();
		mpspMap.put("sessionId",sessionId);
		String ipAddr = request.getHeader("X-Real-IP");
		String clientIp = "";
		if(null == ipAddr){
			mpspMap.put("IP",request.getRemoteAddr());
			clientIp = request.getRemoteAddr();
		}else{
			mpspMap.put("IP",ipAddr);
			clientIp = ipAddr;
		}
		
		mpspMap.put(DataDict.NET_CLIENTIP, clientIp);
		Integer clientPort = request.getRemotePort();
		mpspMap.put(DataDict.NET_CLIENTPORT, clientPort==null?"":clientPort.toString());
		mpspMap.put(DataDict.NET_USERIP, request.getRemoteAddr());
		String serverIp = request.getLocalAddr();
		mpspMap.put(DataDict.NET_SERVERIP, serverIp);
		Integer serverPort = request.getLocalPort();
		mpspMap.put(DataDict.NET_SERVERPORT, serverPort==null?"":serverPort.toString());
		//Thread.currentThread().setName(sessionId);
		String referer = ObjectUtil.trim(request.getHeader("Referer"));
		String referers[] = referer.split("[?]");
		mpspMap.put(DataDict.NET_REFERER, referers[0]);
		//mpspMap.put(DataDict.REQ_TIME, getReqTime());
		StringBuffer reqData = new StringBuffer();
		Enumeration requestNames = request.getParameterNames();
		while(requestNames.hasMoreElements()){
			String name = (String) requestNames.nextElement();
			String value = request.getParameter(name);
			reqData.append(name).append("=").append(value).append(",");
			mpspMap.put(name,value);
		}
		logInfo("[request data:]%s",reqData.toString());
		logInfo("[request Referer:]%s",referer);
		
		if(getFunCode().equals(DataDict.FUNCODE_PAGE_XDNOMBL)){
			OrderParam order = (OrderParam)request.getSession().getAttribute("orderPageParam");
			if(order != null){
				mpspMap.putAll(order.getLogData());
			}
		}else if(getFunCode().equals(DataDict.FUNCODE_PAGE_ORDSV) || getFunCode().equals(DataDict.FUNCODE_WAP_ORDSV)){
			PageOrderCmd cmd = (PageOrderCmd)request.getSession().getAttribute(DataDict.FUNCODE_ORDER_PARAM);
			if(cmd != null){
				mpspMap.putAll(cmd.getLogData());
			}
		}else if(getFunCode().equals(DataDict.FUNCODE_PAGE_NOTIFY) || getFunCode().equals(DataDict.FUNCODE_WAP_NOTIFY)|| getFunCode().equals(DataDict.FUNCODE_PAGE_SMSRESEND) || getFunCode().equals(DataDict.FUNCODE_WAP_SMSRESEND)){
			OrderParam cmd = (OrderParam)request.getSession().getAttribute("orderParam");
			if(cmd != null){
				mpspMap.putAll(cmd.getLogData());
			}
		}

		//2.0参数到3.0转换(merId,merDate,orderId,goodsId,amount)
		if(!isVer3()){
			String merId2 = (String)mpspMap.get(DataDict.MER_REQ_MERID_V2);
			if(ObjectUtil.isNotEmpty(merId2)){
				mpspMap.put(DataDict.MER_REQ_MERID, merId2);
			}
			String merDate = (String)mpspMap.get(DataDict.MER_REQ_DATETIME_V2);
			if(ObjectUtil.isNotEmpty(merDate)){
				mpspMap.put(DataDict.MER_REQ_MERDATE, merDate);
			}
			//
			String orderId = (String)mpspMap.get(DataDict.MER_REQ_ORDERID_V2);
			if(ObjectUtil.isNotEmpty(orderId)){
				mpspMap.put(DataDict.MER_REQ_ORDERID, orderId);
			}
			String goodsId =  (String)mpspMap.get(DataDict.MER_REQ_GOODSID_V2);
			if(ObjectUtil.isNotEmpty(goodsId)){
				mpspMap.put(DataDict.MER_REQ_GOODSID, goodsId);
			}
			String amount =  (String)mpspMap.get(DataDict.MER_REQ_AMOUNT_V2);
			if(ObjectUtil.isNotEmpty(goodsId)){
				mpspMap.put(DataDict.MER_REQ_AMOUNT, amount);
			}
			String mobileId =  (String)mpspMap.get(DataDict.MER_REQ_MOBILEID_V2);
			if(ObjectUtil.isNotEmpty(mobileId)){
				mpspMap.put(DataDict.MER_REQ_MOBILEID, mobileId);
			}
		}
	}
	/**
	 * ********************************************
	 * method name   : afterProcess 
	 * description   : 业务完成后再处理(包括但不限于日志Map信息整理)
	 * @return       : void
	 * @param        : @param modelMap 业务数据Map
	 * @param        : @param logInfo 日志Map信息
	 * *******************************************
	 */
	private void afterProcess(Map<String,Object> modelMap,Map<String,Object> logInfo){
		//日志中记录业务处理结果
		String retCode = "";
		String retMsg = "";
		if(modelMap!=null&&modelMap.size()!=0){
			String retCodeWeb = ObjectUtil.trim(modelMap.get(DataDict.RET_CODE));
			String retCodeBussi = ObjectUtil.trim(modelMap.get(DataDict.RET_CODE_BUSSI));
			if(!ObjectUtil.isEmpty(retCodeBussi)){
				retCode = retCodeBussi;
				retMsg = filterSpecialChar(messageService.getMessage(retCode));
			}else{
				retCode = retCodeWeb;
				if(!retCode.equals(DataDict.SUCCESS_RET_CODE) && retCode.length() == 4){
					retCode = "8602"+retCode;
				}
				retMsg = filterSpecialChar(messageService.getMessage(retCodeWeb));
			}
			//begin modify by zhaoYan 2013-11-27  为配合数据转化率分析系统处理日志，现增加三个字段于简要日志中
			String bankId = ObjectUtil.trim(modelMap.get(HFBusiDict.BANKID));
			if(!"".equals(bankId)){
				logInfo.put(HFBusiDict.BANKID, bankId);
			}
			String provcode = ObjectUtil.trim(modelMap.get(HFBusiDict.PROVCODE));
			if(!"".equals(provcode)){
				logInfo.put(HFBusiDict.PROVCODE, provcode);
			}
			String areacode = ObjectUtil.trim(modelMap.get(HFBusiDict.AREACODE));
			if(!"".equals(areacode)){
				logInfo.put(HFBusiDict.AREACODE, areacode);
			}
			//end modify by zhaoYan 2013-11-27  为配合数据转化率分析系统处理日志，现增加三个字段于简要日志中
			String orderId = ObjectUtil.trim(modelMap.get(HFBusiDict.ORDERID));
			if(!"".equals(orderId)){
				logInfo.put("orderId", orderId);
			}
			String merDate = ObjectUtil.trim(modelMap.get(HFBusiDict.ORDERDATE));
			if(!"".equals(merDate)){
				logInfo.put("merDate", merDate);
			}//end modify by zhuoYangYang 20140429 
		}else{
			retCode="86021180";
			retMsg = filterSpecialChar(messageService.getMessage(retCode));
		}
		logInfo.put(DataDict.RET_CODE, retCode);
		logInfo.put(DataDict.RET_MSG, retMsg);
	}
	
	/**
	 * ********************************************
	 * method name   : logInfo 
	 * description   : 打印业务日志
	 * @return       : void
	 * @param        : @param message 带模板%s的模板
	 * @param        : @param args    可变长参数
	 * *******************************************
	 */
	public void logInfo(String message,Object... args){
		String rpid = SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID);
		String funCode = SessionThreadLocal.getSessionValue(DataDict.FUNCODE);
		logger.info(String.format("%s,%s,%s",funCode,rpid,String.format(message,args)));
	}
	
	public void logError(Exception e){
		String rpid = SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID);
		logger.error(ObjectUtil.handlerException(e, rpid));
	}
	
	
	
	/**
	 * ********************************************
	 * method name   : isLevel2Mer 
	 * description   : 判断是否为二级商户 是则返回true
	 * @return       : boolean
	 * @param        : @param merId        : 
	 * *******************************************
	 */
	protected boolean isLevel2Mer(String merId){
		logger.info("判断是否为二级商户号：" + merId);
		String level2MeridList = messageService.getSystemParam("level2MeridList");
	    // 判断是否是二级商户的交易  如果是为true
	    boolean isLevel2 = level2MeridList.indexOf(merId) != -1;
	    return isLevel2;

	}
	/**
	 * @Title: platSign
	 * @Description: 平台签名
	 * @param @param content
	 * @param @return    设定文件
	 * @return String    返回类型
	 * @throws WebBusiException 
	 * @throws
	 */
	public String platSign(String content){
		String keyFile = messageService.getSystemParam(DataDict.PLATEFORM_KEY);
		File file = new File(keyFile);
		if(!file.exists()){
			throw new IllegalArgumentException("platform key file is not exists");
		}
		byte[] key = FileUtil.getFileContent(keyFile);
		PrivateKey pk = SignUtil.genPrivateKey(key);
		byte[] signData = SignUtil.sign(pk, content.getBytes());
		String sign = Base64.encode(signData);
		return sign;

	}

	/**
	 * ********************************************
	 * method name   : filterSpecialChar 
	 * description   : 过滤特殊字符
	 * @return       : String
	 * @param        : @param msg
	 * *******************************************
	 */
	private String filterSpecialChar(String msg){
		if(msg != null && msg.contains(",")){
			msg = org.springframework.util.StringUtils.replace(msg, ",", "-");
		}
		return msg;
	}

	public void setRestService(RestService restService) {
		this.restService = restService;
	}

	public void setTradeService(TradeService tradeService) {
		this.tradeService = tradeService;
	}

	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}

	public void setCheckService(CheckService checkService) {
		this.checkService = checkService;
	}

	public void setMerAuthService(MerAuthService merAuthService) {
		this.merAuthService = merAuthService;
	}

	public void setSmsService(SmsService smsService) {
		this.smsService = smsService;
	}
	public void setMpspLogHandler(LogTemplateHandler handler) {
		this.mpspLogHandler = handler;
	}
	
}
