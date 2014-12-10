<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" isELIgnored="false"%>
<%@ taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="Cache-Control" content="no-cache">
<meta name="viewport" content="initial-scale=1.0,minimum-scale=1.0,maximum-scale=2.0,user-scalable=no">
<meta name="MobileOptimized" content="240">
<title>移动支付</title>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/static/css/wapstyle_2014.css">
<script type="text/JavaScript">
var wait = 10; //设置秒数(单位秒) 
var secs = 0;          
for(var i=1;i<=wait;i++) 
{ 
 window.setTimeout("sTimer("+i+")",i*1000); 
} 
function sTimer(num) 
{ 
 if(document.getElementById("mustInput").value=="1"){
   document.getElementById("btnHQyzm").disabled=false; 
   return;
 }
 if(num==wait) 
 { 
  document.getElementById("btnHQyzm").value="重新获取"; 
  document.getElementById("btnHQyzm").disabled=false; 
 } 
 else 
 { 
  secs=wait-num; 
  document.getElementById("btnHQyzm").value="重新获取 ("+secs+")"; 
 } 
} 

function submitFrom1(){
    document.forms['hfForm1'].submit();
}
function submitFrom2(){
    document.getElementById('btnQrzf').disabled=true;
    document.forms['hfForm2'].submit();
}
</script>
</head>

<body>
<input type="hidden" name="mustInput" id="mustInput" value="${mustInput}"/>
<div class="contain">
  <div class="top_logo"><img src="${pageContext.request.contextPath}/static/images/newpage_2014/logo_wap_xe.gif" /></div>
  <div class="ctn_txt">
    <div class="ctn_title">话费支付</div>
    <form method="post" action="${pageContext.request.contextPath}/wap/wapVerifyCodePay.do" id="hfForm1" name="hfForm1">
    <dl class="ctn_nr">
      <dt>商品名称：${order.goodsName}</dt>
      <dt>商品价格：<span class="cor_red">${order.amount4dollar}
        <c:choose>
          <c:when test="${order.servType == '2'}">元/次</c:when>
          <c:when test="${order.servType == '3'}">元/<c:if test="${order.servMonth != '1'}">${order.servMonth}</c:if>月</c:when>
          <c:otherwise>元</c:otherwise>
        </c:choose>
      </span><span class="font_size">（不含通信费）</span></dt>
      <c:if test="${order.cusPhone != null}">
        <dt>客服电话：${order.cusPhone}</dt>
      </c:if>
      <dt>支付手机：${mobileId}</dt>
      <dt>验证码：
        <input name="captcha"  class="yzm_sty" type="text" maxlength="11"/>
        <input id="btnHQyzm" type="button" onclick="submitFrom2()" value="重新获取" disabled="true" class="yzm_btn"/>
      </dt>
    </dl>
    </form>
  </div>
  
  <form method="post" action="${pageContext.request.contextPath}/wap/wapVerifyCode.do" id="hfForm2" name="hfForm2">
    <input type="hidden" name="merId" value="${order.merId}"/>
    <input type="hidden" name="mobileId" value="${mobileId}"/>
  </form>
  
  <div class="sp_ms round">${retMsg}</div>
  <div class="btn" >
    <input id="btnQrzf" type="button" onclick="submitFrom1()" value="确认支付" class="red_btn round"/>
  </div>
  <c:if test="${order.orderState!=2}">
    <div class="btn" ><a href="${wholeRetUrl }" class="blue_btn round">返回商家</a></div>
  </c:if>
  <div class="zs_font">*10658008是中国移动通信账户支付专用短信特服号码</div>
  
</div>
</body>
</html>
