<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" isELIgnored="false"%>
<%@ taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=2.0, user-scalable=no"/>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>中国移动通信账户支付</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/wapstyle_2014.css" type="text/css" />
<script type="text/javascript" src="${pageContext.request.contextPath}/static/js/forward.js"></script>
<script type="text/javascript">
function gmwc(){
    document.getElementById('btnGmwc').disabled=true;
    document.forms['hfForm'].submit();
}
function fhsj(){
    document.getElementById('retMer').disabled=true;
    document.getElementById("submitBtn").value="retMer"; 
    document.forms['hfForm'].submit();
}
</script>
</head>
<body>
<form name="hfForm" method="post" action="${pageContext.request.contextPath}/pay/wapPayResult.do">
<input type="hidden" id="submitBtn" name="submitBtn"/>
<div class="contain">
  <div class="top_logo"><img src="${pageContext.request.contextPath}/static/images/newpage_2014/logo_wap_xe.gif" /></div>
  <div class="ctn_txt">
    <dl class="ctn_nr3">
      <dt>${order.nextDirect }</dt>
    </dl>
  </div>
  
  <div class="btn" ><input id="btnGmwc" type="button" onclick="gmwc()" value="购买完成" class="red_btn round"/></div>
  <c:if test="${order.orderState!=2}">
    <div class="btn" ><input id="retMer" type="button" onclick="fhsj()" value="返回商家" class="blue_btn round"/></div>
  </c:if>
  <c:if test="${sessionScope.smsParam.sendCount <= 2 && order.bankId!='XE791000'}">
    <div class="btn"><a href="${pageContext.request.contextPath}/pay/wapSms.do" class="font_btn">没收到支付确认短信，重新接收</a></div>
  </c:if>
  <c:if test="${order.bankId=='XE791000'}">
    <div class="btn"><a class="font_btn">如1分钟内未成功接收短信，请重新下订单订购</a></div>
  </c:if>
 <div class="zs_font">*如未收到短信请咨询中国移动通信账户支付客服中心：4006125880（只收市话费，无长途话费）</div>
</div>
</form>
</body>
</html>