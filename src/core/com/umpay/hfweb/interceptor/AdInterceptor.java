/** *****************  JAVA头文件说明  ****************
 * file name  :  AdInterceptor.java
 * owner      :  xu
 * copyright  :  UMPAY
 * description:  
 * modified   :  2012-12-13
 * *************************************************/ 

package com.umpay.hfweb.interceptor;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.bs.mpsp.util.StringUtil;
import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.param.OrderParam;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.service.MessageService;
import com.umpay.hfweb.util.SpringContextUtil;


/** ******************  类说明  *********************
 * class       :  AdInterceptor
 * @author     :  xuhuafeng
 * description :  拦截URL，添加广告
 * ************************************************/

public class AdInterceptor extends HandlerInterceptorAdapter {
	public static Logger log = Logger.getLogger(AdInterceptor.class);

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		//获取session中的订单参数
		OrderParam sessionParam = (OrderParam)request.getSession().getAttribute("orderPageParam");
		if(sessionParam == null){
			sessionParam = (OrderParam)request.getSession().getAttribute("orderParam");
		}
		if(sessionParam != null){
			Map<String, Object> adMesaage = new HashMap<String, Object>();
			adMesaage.put("sessionParam", sessionParam);
			request.setAttribute("adMesaage", adMesaage);
		}
		return super.preHandle(request, response, handler);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		HttpSession session = request.getSession();
		Map<String, Object> adMesaage2 = new HashMap<String, Object>();
		String num = "-1";
		String merId = "";
		String goodsId = "";
		String bankId = "";
		if(modelAndView.hasView()){
			String view = modelAndView.getViewName();
			if("order/web_confirmPayNoMbl".equals(view) || "order/web_mw_confirmpay".equals(view) || "order/web_xe_confirmpay".equals(view)){  //下单到确认页面
				num = "1";
			}else if("order/web_mw_payresult".equals(view) || "order/web_xe_payresult".equals(view)){  //到二次确认页面
				num = "2";
			}else if("order/web_mw_tomer".equals(view) || "order/web_xe_tomer".equals(view)){  //到支付成功页面
				num = "3";
			}else if("web_error".equals(view)){  //错误页面
				String uri = request.getRequestURI();
				if(uri.endsWith("/page.do")){  //下单到确认页面
					num = "1";
				}else if(uri.endsWith("/page2.do") || uri.endsWith("/saveOrder.do")){  //到二次确认页面
					num = "2";
				}else if(uri.endsWith("/sms.do") || uri.endsWith("/payResult.do")){  //到支付成功页面
					num = "3";
				}
			}
			if(!"-1".equals(num)){
				OrderParam sessionParam = new OrderParam();
				log.info("在"+view+"页面添加广告");
				Map<String, Object> adMesaage = (Map<String, Object>) request.getAttribute("adMesaage"); //存放广告的相关信息
				if(adMesaage == null){
					sessionParam = (OrderParam)session.getAttribute("orderPageParam");
					if(sessionParam == null){
						sessionParam = (OrderParam)session.getAttribute("orderParam");
					}
				}else{
					sessionParam = (OrderParam)adMesaage.get("sessionParam");
				}
				if(sessionParam == null){
					String url = (String) session.getAttribute("errorRetUrl"); //获取session中的URL
					if(url != null){
						String params = url.substring( url.indexOf("?") + 1 ); 
						String[] param = params.split("&"); //截串，取参数
						for(int i=0;i<param.length;i++){
							String[] oneParam = param[i].split("=");
							if(oneParam.length == 2){
								if(DataDict.MER_REQ_MERID.equals(oneParam[0])){
									merId = oneParam[1];
									continue;
								}
								if(DataDict.MER_REQ_GOODSID.equals(oneParam[0])){
									goodsId = oneParam[1];
									continue;
								}
								if(DataDict.MER_REQ_BANKID.equals(oneParam[0])){
									bankId = oneParam[1];
									continue;
								}
							}
						}
						if(bankId == "" || bankId == null){
							try{
								MpspMessage checkTradeResp = (MpspMessage) session.getAttribute("tradeMessage");
								bankId = (String)checkTradeResp.get(HFBusiDict.BANKID);
							}catch (Exception e) {
								log.info("从session中获取省份失败");
							}
						}
					}
				}else{
					merId = sessionParam.getMerId();
					goodsId = sessionParam.getGoodsId();
					bankId = sessionParam.getBankId();
				}
				adMesaage2.put("showAd", "false");  //默认不显示广告
				String adver = getAdMessage(num, merId, goodsId, bankId);
				if(adver != null){
					String[] ad = adver.split(",");
					if(ad.length == 2){
						adMesaage2.put("adSrc", ad[0]);
						adMesaage2.put("adHref", ad[1]);
						adMesaage2.put("showAd", "true");
					}
				}
				request.setAttribute("adMesaage", adMesaage2);
			}
		}
	}
	/**
	 * 从4个维度匹配广告
	 */
	private String getAdMessage(String num, String merId, String goodsId, String bankId){
		MessageService messageService = (MessageService) SpringContextUtil.getBean("messageService");
		merId = StringUtil.trim(merId);
		goodsId = StringUtil.trim(goodsId);
		bankId = StringUtil.trim(bankId);
		if("".equals(merId) || merId == null){
			merId = "*";
		}
		if("".equals(goodsId) || goodsId == null){
			goodsId = "*";
		}
		if("".equals(bankId) || bankId == null){
			bankId = "*";
		}
		String key = getKey(num, merId, goodsId, bankId);
		String adMesaage = messageService.getSystemParam(key);
		//逐级向上模糊匹配
		if("".equals(adMesaage)){
			if(!"*".equals(bankId)){  //bankId != "*"
				key = getKey(num, merId, goodsId, "*");
				adMesaage = messageService.getSystemParam(key);
				if("".equals(adMesaage) && !"*".equals(goodsId)){
					key = getKey(num, merId, "*", bankId);
					adMesaage = messageService.getSystemParam(key);
				}
				if("".equals(adMesaage) && !"*".equals(merId)){
					key = getKey(num, "*", "*", bankId);
					adMesaage = messageService.getSystemParam(key);
				}
			}
			if("".equals(adMesaage)){
				key = getKey(num, merId, "*", "*");
				adMesaage = messageService.getSystemParam(key);
			}
			if("".equals(adMesaage)){
				key = getKey(num, "*", "*", "*");
				adMesaage = messageService.getSystemParam(key);
			}
			if("".equals(adMesaage)){
				key = getKey("*", "*", "*", "*");
				adMesaage = messageService.getSystemParam(key);
			}
		}
		if("".equals(adMesaage)){
			log.info("键值["+getKey(num, merId, goodsId, bankId)+"]没有匹配到广告");
		}else{
			log.info("初始键值["+getKey(num, merId, goodsId, bankId)+"],最终匹配到信息的键值["+key+"],获取的广告信息为:"+adMesaage);
		}
		return adMesaage;
	}
	
	private String getKey(String num, String merId, String goodsId, String bankId){
		return "ADMESSAGE."+num + "." + merId + "." + goodsId + "." + bankId;
	}

}
