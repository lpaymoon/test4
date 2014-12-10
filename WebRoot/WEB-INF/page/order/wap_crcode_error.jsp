<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" isELIgnored="false"%>
<%@ taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta content="text/html; charset=utf-8" http-equiv="Content-Type" />
<meta name="viewport" content="initial-scale=1.0,minimum-scale=1.0,maximum-scale=2.0,user-scalable=no"/>
<title>购买失败</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/wap_yzm_style.css" type="text/css" />
<script type="text/javascript" src="${pageContext.request.contextPath}/static/js/forward.js"></script>
</head>
<body style=" background-color:#f5f5f5;>
<div class="contain">
       <div class="ctn_bg02">
       <div style=" height:20px;"></div>
       <div class="tip_img">
           <img src="${pageContext.request.contextPath}/static/images/wap_yzm/pay_dis.png" style="float:left;" /><span style="float:left; margin:0px 0px 0px 10px; height:60px; color:#d33e3e; line-height:60px;">购买失败</span>
        </div>
        <div class="clr"></div>
        
        <div class="buy_ctn">
          <dl>
            <dt>${errorMessage }</dt>
            <dt>客服电话：<span class="blue_color">4006-125880</span></dt>
          </dl>
        </div>
         <div style=" height:20px;"></div>
      </div>
     <div class="ts_btn"> <a href="${wholeRetUrl}" class="red_btn round"><input name="" type="button"  class="qr_bt" value="返回"/></a></div>
     <div class="btm_img"><img src="${pageContext.request.contextPath}/static/images/wap_yzm/btm_logo.png"/></div>
</div>
</body>
</html>
