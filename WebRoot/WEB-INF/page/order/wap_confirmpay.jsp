<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" isELIgnored="false"%>
<%@ taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=2.0, user-scalable=no"/>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>中国移动通信账户支付</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/wapstyle_2014.css" type="text/css">
<script type="text/javascript" src="${pageContext.request.contextPath}/static/js/forward.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/static/js/google.js"></script>
<script type="text/javascript">
function tjdd(){
    document.getElementById('btnTjdd').disabled=true;
    document.forms['hfForm'].submit();
}
</script>
</head>
<body>
<form method="post" action="${pageContext.request.contextPath}/pay/wapSaveOrder.do" id="hfForm" name="hfForm">
<div class="contain">
  <div class="top_logo"><img src="${pageContext.request.contextPath}/static/images/newpage_2014/logo_wap_xe.gif" /></div>
  <div class="ctn_txt">
  
    <div class="ctn_title">话费支付</div>
    <dl class="ctn_nr">
      <c:if test="${order.goodsId!=null && order.goodsId!=''}">
        <dt>商品名称：${order.goodsName }</dt>
      </c:if>
      <dt>商品价格：<span class="cor_red">${order.amount4dollar }
        <c:choose>
          <c:when test="${order.servType == '2'}">元/次</c:when>
          <c:when test="${order.servType == '3'}">元/<c:if test="${order.servMonth != '1'}">${order.servMonth}</c:if>月</c:when>
        </c:choose>
      </span><span class="font_size">（不含通信费）</span></dt>
      <dt>客服电话：${order.cusPhone }</dt>
      <dt>支付手机：${order.mobileId }</dt>
    </dl>
  </div>
  
  <div class="btn" >
    <input type="hidden" name="bankId" id="bankId" value="${order.bankId }" />
    <input id="btnTjdd" type="button" onclick="tjdd()" value="提交订单" class="red_btn round"/>
  </div>
   <div class="zs_font">
    <dl>
     <dt>温馨提示：</dt>
     <dt>1、中国移动通信账户支付是中国移动电子商务的支付方式之一。10658008是中国移动通信账户支付专用短信端口号码</dt>
     <dt>2、本服务支持移动全球通、动感地带、神州行等品牌的用户，用户使用中国移动通信账户支付时无免费试用；支付成功，即刻扣费；商品价格不含通信费</dt>
     <dt>3、业务使用中会产生0.1元/条（或按照您参与的运营商话费套餐标准）短信通信费，具体请咨询10086</dt>
     <dt>4、部分省份“赠送”“返还”的话费不可用于购买商品。如：北京、黑龙江等</dt>
     <dt>5、部分商品可用话费购买的数量有限，若购买失败，请选择其他支付方式</dt>
     <dt>6、中国移动通信账户支付客服电话：4006125880（只收市话费，无长途话费）</dt>
    </dl>
   </div>
</div>
</form>
</body>
</html>
