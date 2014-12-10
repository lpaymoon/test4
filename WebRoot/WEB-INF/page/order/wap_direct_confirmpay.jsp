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
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/static/css/wap_direct.css">
<script type="text/JavaScript">
<!--
function MM_swapImgRestore() { //v3.0
  var i,x,a=document.MM_sr; for(i=0;a&&i<a.length&&(x=a[i])&&x.oSrc;i++) x.src=x.oSrc;
}

function MM_preloadImages() { //v3.0
  var d=document; if(d.images){ if(!d.MM_p) d.MM_p=new Array();
    var i,j=d.MM_p.length,a=MM_preloadImages.arguments; for(i=0; i<a.length; i++)
    if (a[i].indexOf("#")!=0){ d.MM_p[j]=new Image; d.MM_p[j++].src=a[i];}}
}

function MM_findObj(n, d) { //v4.01
  var p,i,x;  if(!d) d=document; if((p=n.indexOf("?"))>0&&parent.frames.length) {
    d=parent.frames[n.substring(p+1)].document; n=n.substring(0,p);}
  if(!(x=d[n])&&d.all) x=d.all[n]; for (i=0;!x&&i<d.forms.length;i++) x=d.forms[i][n];
  for(i=0;!x&&d.layers&&i<d.layers.length;i++) x=MM_findObj(n,d.layers[i].document);
  if(!x && d.getElementById) x=d.getElementById(n); return x;
}

function MM_swapImage() { //v3.0
  var i,j=0,x,a=MM_swapImage.arguments; document.MM_sr=new Array; for(i=0;i<(a.length-2);i+=3)
   if ((x=MM_findObj(a[i]))!=null){document.MM_sr[j++]=x; if(!x.oSrc) x.oSrc=x.src; x.src=a[i+2];}
}
//-->
</script>
</head>

<body onload="MM_preloadImages('${pageContext.request.contextPath}/static/images/wap_direct/back2.jpg')" style="background:#d1e4f7; ">
<div id="MENU">
       <span><b>确认支付</b></span>
</div>
<div id="logod" class="t_l"><img src="${pageContext.request.contextPath}/static/images/wap_direct/logo.jpg"  class="ml10"/></div>
<div id="MAIN">
  <div class="maininfo">
		  <div class="box">
		    <div class="box1"><b></b><span></span></div>				   
		    <div class="pt10"> 
			    <ul>
				<li class="t_l">商品名称：${order.goodsName}</li>
				<li class="t_l">应付金额：<b class="red">${order.amount4dollar}元</b></li>
				<li class="t_l">支付方式：手机话费购买</li>
				<li class="t_l">支付手机：${mobileId}</li>
				</ul>
			</div>
		    <div class="box2"><b></b><span></span></div>
			<div class="blank"></div>	
		  </div> 
		  
		  <div class="box mt10 mbt10">
		    <div class="box1"><b></b><span></span></div>				   
		    <div class="pt10">
            <c:if test="${retCode == '0000'}">
				<ul class="bline">
				<li class="t_l" style="padding-bottom:20px; "><span class="org" >${retMsg}</span></li>	
				</ul>
			</c:if>
			  <div class="posa">
					<ul class="mt10">
					
					<form method="post" action="${pageContext.request.contextPath}/pay/wapDirectPay.do" name="hfForm1">		
								<li class="t_l">
								<input name="captcha" type="text" class="inputb f_l" value=""  maxlength="11"/>
								<c:if test="${retCode != '0000'}">
									<div class="blank"></div><div class="mt10"  id="ERROR"><p class="ml10">${retMsg}</p></div>
								</c:if>
								</li>
								<li class="t_l"><input type="submit" value="确认支付" class="buttona mt10 mbt10" /></li>
					</form>
					</ul>
					<ul>
					<form method="post" action="${pageContext.request.contextPath}/pay/wapDirectCaptcha.do" name="hfForm2">
						<li style="position:absolute; top:0; right:3%; width:28%;">
				          	<input type="hidden" name="merId" value="${order.merId}"/>
  							<input type="hidden" name="mobileId" value="${mobileId}"/>
							<input name="button" type="submit" class="buttonb f_r" value="重新获取" />
						</li>
					</form>					
				    </ul>
			 </div>	 
			</div>							
		    <div class="box2"><b></b><span></span></div>			
		  </div> 
  </div>
</div>
<div id="FOOT" class="mt20 pt20 line"><img src="${pageContext.request.contextPath}/static/images/wap_direct/logo2.jpg" /></div>
</body>
</html>
