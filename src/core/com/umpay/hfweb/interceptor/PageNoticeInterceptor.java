/** *****************  JAVA头文件说明  ****************
 * file name  :  AdInterceptor.java
 * owner      :  xu
 * copyright  :  UMPAY
 * description:  
 * modified   :  2012-12-13
 * *************************************************/ 

package com.umpay.hfweb.interceptor;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.umpay.hfbusi.HFBusiDict;
import com.umpay.hfweb.action.param.OrderParam;
import com.umpay.hfweb.model.MpspMessage;
import com.umpay.hfweb.util.MessageUtil;
import com.umpay.hfweb.util.ObjectUtil;


/** ******************  类说明  *********************
 * class       :  PageNoticeInterceptor
 * @author     :  lizhen
 * description :  拦截URL，展示页面公告
 * ************************************************/
public class PageNoticeInterceptor extends HandlerInterceptorAdapter {
	public static Logger log = Logger.getLogger(PageNoticeInterceptor.class);
	private MessageSource sysconfSource;
	
	public void setSysconfSource(MessageSource sysconfSource) {
		this.sysconfSource = sysconfSource;
	}

	public void postHandle(HttpServletRequest request,HttpServletResponse response, Object handler,ModelAndView modelAndView) throws Exception {
		if(modelAndView.hasView()){
			String view = modelAndView.getViewName();
			//只在order/web_xe_confirmpay展示页面公告
			if("order/web_xe_payresult".equals(view)){
				//获取银行id
				HttpSession session = request.getSession();
				OrderParam orderParam = (OrderParam)session.getAttribute("orderParam");
				String bankId = orderParam.getBankId();
				//判断是否展示页面公告
				Map<String, Object> pageNotice = new HashMap<String, Object>();
				try{
					if(isShowingPageNotice(bankId)){
						String content = ObjectUtil.trim(MessageUtil.getLocalProperty(sysconfSource,bankId+".notice.content"));
						pageNotice.put("showNotice", "true");
						pageNotice.put("noticeContent", content);
						request.setAttribute("pageNotice", pageNotice);
					}
				}catch (ParseException e){
					log.info("配置文件时间格式不正确");
				}
			}
		}
	}
	
	private boolean isShowingPageNotice(String bankId) throws ParseException{
		
		boolean isShowing = true;
		boolean isDayShowing = true;
		boolean isMLShowing = true;
		boolean isMFShowing = true;
		
		//总是显示
		String alwaysShow = ObjectUtil.trim(MessageUtil.getLocalProperty(sysconfSource,bankId+".alwaysShowing"));
		if(!alwaysShow.equals("")){
			boolean show=Boolean.valueOf(alwaysShow);
			//alwaysShowing的优先级最高，为true时不再考虑设定的时间
			if(show==true){
				log.info("一直展示页面公告alwaysShow["+alwaysShow+"]");
				return true;
			}
		}
		
		//当日显示
		String startTime = ObjectUtil.trim(MessageUtil.getLocalProperty(sysconfSource,bankId+".show.startTime"));
		String endTime = ObjectUtil.trim(MessageUtil.getLocalProperty(sysconfSource,bankId+".show.endTime"));
		//日周期显示
		String dStartTime = ObjectUtil.trim(MessageUtil.getLocalProperty(sysconfSource,bankId+".showDay.startTime"));
		String dEndTime = ObjectUtil.trim(MessageUtil.getLocalProperty(sysconfSource,bankId+".showDay.endTime"));
		//月末最后一天显示
		String mLStartTime = ObjectUtil.trim(MessageUtil.getLocalProperty(sysconfSource,bankId+".showMonthLastDay.startTime"));
		String mLEndTime = ObjectUtil.trim(MessageUtil.getLocalProperty(sysconfSource,bankId+".showMonthLastDay.endTime"));
		//月初第一天显示
		String mFStartTime = ObjectUtil.trim(MessageUtil.getLocalProperty(sysconfSource,bankId+".showMonthFirstDay.startTime"));
		String mFEndTime = ObjectUtil.trim(MessageUtil.getLocalProperty(sysconfSource,bankId+".showMonthFirstDay.endTime"));
		
		if(ObjectUtil.isEmpty(startTime) || ObjectUtil.isEmpty(endTime)){
			isShowing=false;
			log.info("当日展示页面公告时间配置不完整，不进行判断 startTime["+startTime+"]endTime["+endTime+"]");
		}
		if(ObjectUtil.isEmpty(dStartTime) || ObjectUtil.isEmpty(dEndTime)){
			isDayShowing=false;
			log.info("日周期展示页面公告时间配置不完整，不进行判断 startTime["+startTime+"]endTime["+endTime+"]");
		}
		if(ObjectUtil.isEmpty(mLStartTime) || ObjectUtil.isEmpty(mLEndTime)){
			isMLShowing=false;
			log.info("月末最后一天展示页面公告时间配置不完整，不进行判断 startTime["+startTime+"]endTime["+endTime+"]");
		}
		if(ObjectUtil.isEmpty(mFStartTime) || ObjectUtil.isEmpty(mFEndTime)){
			isMFShowing=false;
			log.info("月初第一天展示页面公告时间配置不完整，不进行判断 startTime["+startTime+"]endTime["+endTime+"]");
		}
		
		if(!isShowing && !isDayShowing && !isMLShowing && !isMFShowing){
			log.info("未配置展示页面公告时间，不展示页面公告");
			return false;
		}
		
		DateFormat fm = new SimpleDateFormat ("yyyyMMddHHmmss");
		Date date = new Date();
		Date stime = new Date();
		Date etime = new Date();
		//当前时间满足设定的其中一种即可
		if(isShowing){
			log.info("进行当日展示页面公告时间判断，bankId["+bankId+"] startTime["+startTime+"] endTime["+endTime+"]！");
			stime = fm.parse(startTime);
    		etime  = fm.parse(endTime);
	    	if (date.after(stime) && date.before(etime)) {
	    		log.info("bankId["+bankId+"] startTime["+startTime+"] endTime["+endTime+"] 展示页面公告");
				return true;
			}
		}
		log.info("当前时间不满足当日展示页面公告时间isShowing["+isShowing+"]");
		if(isDayShowing){
			log.info("进行日周期展示页面公告时间判断，bankId["+bankId+"] startTime["+dStartTime+"] endTime["+dEndTime+"]！");
			DateFormat fm2 = new SimpleDateFormat ("yyyyMMdd");
	    	String timeStr = fm2.format(date);
	    	dStartTime = timeStr+dStartTime;
	    	dEndTime =  timeStr+dEndTime;
	    	Date _startTime2 = new Date();
	    	Date _endTime2   = new Date();
	    	boolean isDoubleCheck = false;
    		stime = fm.parse(dStartTime);
	    	etime   = fm.parse(dEndTime);
	    	if(etime.before(stime)){
	    		String dayBegin = timeStr+"000000";
	    		_startTime2 =  fm.parse(dayBegin);
	    		_endTime2 = etime;
	    		
	    		String dayEnd = timeStr+"235959";
	    		etime = fm.parse(dayEnd);
	    		isDoubleCheck = true;
	    	}
	    	if (date.after(stime) && date.before(etime)) {
	    		log.info("bankId["+bankId+"] dStartTime["+stime+"] dEndTime["+etime+"] 展示页面公告！");		
				return true;
			}
	    	if(isDoubleCheck&&date.after(_startTime2) && date.before(_endTime2)){
	    		log.info("bankId["+bankId+"] dStartTime["+_startTime2+"] dEndTime["+_endTime2+"] 展示页面公告！");		
				return true;
	    	}
		}
		log.info("当前时间不满足日周期展示页面公告时间isDayShowing["+isDayShowing+"]");
		if(isMLShowing){
			//判断是否是月末最后一天
			log.info("进行月末最后一天展示页面公告时间判断，bankId["+bankId+"] mLStartTime["+mLStartTime+"] mLEndTime["+mLEndTime+"]！");
			Calendar cal = Calendar.getInstance();
			int today = cal.get(Calendar.DAY_OF_MONTH);
			int monthLastDay=cal.getActualMaximum(Calendar.DAY_OF_MONTH); 
			if(today==monthLastDay){
				DateFormat fm2 = new SimpleDateFormat ("yyyyMMdd");
		    	String timeStr = fm2.format(date);
		    	mLStartTime = timeStr+mLStartTime;
		    	mLEndTime =  timeStr+mLEndTime;
	    		stime = fm.parse(mLStartTime);
		    	etime   = fm.parse(mLEndTime);
		    	if (date.after(stime) && date.before(etime)) {
		    		log.info("bankId["+bankId+"] mLStartTime["+mLStartTime+"] mLEndTime["+mLEndTime+"] 展示页面公告！");		
		    		return true;
				}
			}
		}
		log.info("当前时间不满足月末最后一天展示页面公告时间isMLShowing["+isMLShowing+"]");
		if(isMFShowing){
			//判断是否是月初第一天
			log.info("进行月初第一天展示页面公告时间判断，bankId["+bankId+"] mFStartTime["+mFStartTime+"] mFEndTime["+mFEndTime+"]！");
			Calendar cal = Calendar.getInstance();
			int today = cal.get(Calendar.DAY_OF_MONTH);
			if(today==1){
				DateFormat fm2 = new SimpleDateFormat ("yyyyMMdd");
		    	String timeStr = fm2.format(date);
		    	mFStartTime = timeStr+mFStartTime;
		    	mFEndTime = timeStr+mFEndTime;
	    		stime = fm.parse(mFStartTime);
		    	etime = fm.parse(mFEndTime);
		    	if (date.after(stime) && date.before(etime)) {
		    		log.info("bankId["+bankId+"] mFStartTime["+mFStartTime+"] mFEndTime["+mFEndTime+"] 展示页面公告！");		
					return true;
				}
			}
		}
		log.info("当前时间不满足月初第一天展示页面公告时间isMFShowing["+isMFShowing+"]");
		log.info("展示页面公告时间判断通过 OK.");
		return false;
	}

}
