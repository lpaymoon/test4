<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" isELIgnored="false"%>
<%@ taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta content="text/html; charset=utf-8" http-equiv="Content-Type" />
<meta name="viewport" content="initial-scale=1.0,minimum-scale=1.0,maximum-scale=2.0,user-scalable=no"/>
<title>中国移动通信账户支付</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/wapstyle_mw_2014.css" type="text/css" />
<script type="text/javascript" src="${pageContext.request.contextPath}/static/js/forward.js"></script>
</head>
<body>
<div class="contain">
  <div class="top_logo"><img src="${pageContext.request.contextPath}/static/images/newpage_2014/logo_wap_mw.gif" /></div>
  <div class="ctn_txt">
  
    <div class="dis_title"><img src="${pageContext.request.contextPath}/static/images/newpage_2014/dis.png" align="absmiddle"><span>支付失败</span></div>
    <dl class="ctn_nr2">
      <dt>${errorMessage }！</dt>
      <dt>支付服务客服：4006125880</dt>
    </dl>
  </div>
  
  <c:if test="${errorRetUrl!=null && errorRetUrl!=''}">
      <div class="btn" ><a href="${errorRetUrl}" class="red_btn round">返回商家</a></div>
  </c:if>
</div>
</body>
</html>
