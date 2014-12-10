<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" isELIgnored="false"%>
<%@ taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!-- 系统升级更新信息提示  -->
<c:if test="${notice!=null && notice!=''}">
   <div class="sysam">
     <div id="SYSTEMALERT"><span>${notice}</span></div>
   </div>
</c:if>
<header class="header">
  <h1 title="手机支付通信账户收银台">手机支付通信账户收银台</h1>
</header>
<article class="main">
<header class="mh">
<h1>来自<c:choose>
	<c:when test="${order.merName!=null && order.merName!=''}">${order.merName }</c:when>
	<c:otherwise>商户</c:otherwise>
</c:choose>的（通信账户）小额支付交易</h1>
<span class="mhl"></span><span class="mhr"></span>
</header>
<div class="mmb">