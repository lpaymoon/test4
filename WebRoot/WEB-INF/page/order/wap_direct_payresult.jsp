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
			    <ul class="mt20 mbt20">
					<li class="">
					<c:choose>
		  		      <c:when test="${retCode=='0000'}">
					  	<b class="t_l oisok">成功支付</b>
					  </c:when>
					  <c:otherwise>
					  	<b class="t_l oisno">支付失败</b>
					  </c:otherwise>
					</c:choose>
					</li>
				</ul>
				<ul><li class="iline t_l">${retMsg}</li></ul>
			</div>
		    <div class="box2"><b></b><span></span></div>
			<div class="blank"></div>	
		  </div> 
		  <p class="mt20">请返回商户查询购买商品信息</p>
		  <p class="mt20"><input type="button" value="返回商户" class="buttona mt10" onclick="window.location.href='${wholeRetUrl}'" /></p>  
  </div>
</div>
<div id="FOOT" class="mt20 pt20 line"><img src="${pageContext.request.contextPath}/static/images/wap_direct/logo2.jpg" /></div>
</body>
</html>
