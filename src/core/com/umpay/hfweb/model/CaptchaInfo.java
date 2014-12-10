package com.umpay.hfweb.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Random;

public class CaptchaInfo implements Serializable {
	
	
	/**
	 * 验证码
	 */
	private static final long serialVersionUID = 7089847930039907257L;
	
	String captcha;//验证码
	
	int errorTimes;//当前验证码已经输入错误的次数，验证码换了则归零
	
	int sendTimes;//发送的是第几条验证码
	
	Timestamp modTime;//验证码更换的时间
	
	Timestamp inTime;
	
	public CaptchaInfo(){
		this.captcha=getRandom();
		this.errorTimes=0;
		this.sendTimes=1;
		Timestamp now=new Timestamp(System.currentTimeMillis());
		this.modTime=now;
		this.inTime=now;
	}
	/**
	 * ********************************************
	 * method name   : regenerateCaptcha 
	 * description   : 重新生成验证码，输入错误次数归零
	 * @return       : void
	 * @param        : 
	 * modified      : LiZhen ,  2013-6-3  下午01:00:28
	 * @see          : 
	 * *******************************************
	 */
	public void regenerateCaptcha(){
		this.captcha=getRandom();
		this.errorTimes=0;
		this.sendTimes=this.sendTimes+1;
		Timestamp now=new Timestamp(System.currentTimeMillis());
		this.modTime=now;
	}
	/**
	 * ********************************************
	 * method name   : isMoreThanInterval 
	 * description   : 指定的时间减去修改时间是否大于指定的间隔
	 * @return       : boolean
	 * @param        : @param t 指定的时间
	 * @param        : @param sentInterval 指定的间隔  单位为秒 默认为3秒
	 * @param        : @return
	 * modified      : LiZhen ,  2013-6-3  下午01:32:45
	 * @see          : 
	 * *******************************************
	 */
	public boolean isMoreThanInterval(Timestamp t,String sentInterval){
		int interval=5;
		try{
			interval=Integer.valueOf(sentInterval);
		}catch(NumberFormatException e){
			e.printStackTrace();
			interval=5;
		}
		if(t.getTime()-this.modTime.getTime()>interval*1000){
			return true;
		}else{
			return false;
		}
		
	}
	/**
	 * ********************************************
	 * method name   : isLessThanMaxSentTimes 
	 * description   : 发送次数是否小于指定的最大次数maxSentTimes，
	 * @return       : boolean
	 * @param        : @param maxSentTimes 指定的最大次数 默认为3次
	 * @param        : @return
	 * modified      : LiZhen ,  2013-6-3  上午10:22:38
	 * @see          : 
	 * *******************************************
	 */
	public boolean isLessThanMaxSentTimes(String maxSentTimes){
		int times=3;
		try{
			times=Integer.valueOf(maxSentTimes);
		}catch(NumberFormatException e){
			e.printStackTrace();
			times=3;
		}
		return (this.getSendTimes()<times) ? true : false;
	}
	/**
	 * ********************************************
	 * method name   : isExpired 
	 * description   : 查看验证码是否过期
	 * @return       : boolean
	 * @param        : @param t 需判断的时间
	 * @param        : @param validTime 指定的有效时间，默认为300秒
	 * @param        : @return
	 * modified      : LiZhen ,  2013-6-5  下午05:14:33
	 * @see          : 
	 * *******************************************
	 */
	public boolean isExpired(Timestamp t,String validTime){
		int vt=300;//默认五分钟
		try{
			vt=Integer.valueOf(validTime);
		}catch(NumberFormatException e){
			e.printStackTrace();
			vt=300;
		}
		return (t.getTime()-this.getModTime().getTime()>vt*1000) ? true : false;
	}
	/**
	 * ********************************************
	 * method name   : isExpired 
	 * description   : 查看错误次数是否少于指定的最大错误次数
	 * @return       : boolean
	 * @param        : @param maxErrorTimes 指定的最大错误次数，默认最多错3次
	 * @param        : @return
	 * modified      : LiZhen ,  2013-6-5  下午05:14:22
	 * @see          : 
	 * *******************************************
	 */
	public boolean isLessThanMaxErrorTimes(String maxErrorTimes){
		int met=3;
		try{
			met=Integer.valueOf(maxErrorTimes);
		}catch(NumberFormatException e){
			e.printStackTrace();
			met=3;
		}
		return (this.getErrorTimes()<met) ? true : false;
	}
	protected String getRandom(){
		Random random = new Random();
		String sRand="";
		for (int i=0;i<6;i++){
		    String rand=String.valueOf(random.nextInt(10));
		    sRand+=rand;
		}
		return sRand;
	}
	public String getCaptcha() {
		return captcha;
	}
	public void setCaptcha(String captcha) {
		this.captcha = captcha;
	}
	public int getErrorTimes() {
		return errorTimes;
	}
	public void setErrorTimes(int errorTimes) {
		this.errorTimes = errorTimes;
	}
	public int getSendTimes() {
		return sendTimes;
	}
	public void setSendTimes(int sendTimes) {
		this.sendTimes = sendTimes;
	}
	public Timestamp getModTime() {
		return modTime;
	}
	public void setModTime(Timestamp modTime) {
		this.modTime = modTime;
	}
	public Timestamp getInTime() {
		return inTime;
	}
	public void setInTime(Timestamp inTime) {
		this.inTime = inTime;
	}
}
