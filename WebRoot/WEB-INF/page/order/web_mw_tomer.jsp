<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" isELIgnored="false"%>
<%@ taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<%@ include file="/WEB-INF/page/meta_mw.jsp" %>
  <title>支付结果通知</title>
</head>
<body>
<div class="contain">
  <div class="top_logo"><img src="${pageContext.request.contextPath}/static/images/newpage_2014/logo.png" /></div>
  <div class="clr"></div>
  <div class="yd_tip">移动话费支付（通信账户）交易订单</div>

  <div class="nr_cnt">
    <div class="cnt_left">
      <div class="dd_title">订单信息</div>
      <dl>
        <dt><span>订单号：</span>${order.orderId }</dt>
        <dt>
          <span>商户名称：</span>
          <c:choose>
            <c:when test="${order.merName2!=null && order.merName2!=''}">
            ${order.merName2 }
            </c:when>
            <c:otherwise>
            ${order.merName }
            </c:otherwise>
          </c:choose>
        </dt>
        
        <c:if test="${order.goodsId!=null && order.goodsId!=''}">
        <dt><span>商品名称：</span>
          <c:choose>
            <c:when test="${order.goodsName2!=null && order.goodsName2!=''}">
            ${order.goodsName2 }
            </c:when>
            <c:otherwise>
            ${order.goodsName }
            </c:otherwise>
          </c:choose>
        </dt>
        </c:if>
        
        <dt><span>商品价格：</span><span class="red_cor">${order.amount4dollar }
          <c:choose>
            <c:when test="${order.servType == '2'}">元/次</c:when>
            <c:when test="${order.servType == '3'}">元/<c:if test="${order.servMonth != '1'}">${order.servMonth}</c:if>月</c:when>
          </c:choose>
        </span></dt>
        
        <dt><span>计费类型：</span>
          <c:choose>
            <c:when test="${order.servType == '2'}">按次</c:when>
            <c:when test="${order.servType == '3'}">包月</c:when>
          </c:choose>
        </dt>
        <dt><span>下单日期：</span>${order.merDate }</dt>
        <dt><span>客服电话：</span>${order.cusPhone }</dt>
      </dl>
    </div>
       
    <div class="cnt_right2">
      <div class="suce_img"><img src="${pageContext.request.contextPath}/static/images/newpage_2014/right.png" />交易成功！</div>
      <div class="suce_txt">感谢您使用中国移动（通信账户）提供的支付服务。</div>
      <div class="suce_txt">您可以访问 <a href="http://vip.umpay.com">http://vip.umpay.com</a> 查询通信账户交易记录，并获得帮助！</div>
      <div class="suce_btn">
        <a href="${order.retUrl}" class="lq_btn">返回商家</a>
        <span id='autoReturn'>3秒后自动返回</span>
      </div>
       <div class="btm_gg" ><a href="http://yxb.umpay.com/sll/T9MhNBQM?from=PCpahd"><img src="${pageContext.request.contextPath}/static/images/newpage_2014/pingan.jpg" /></a></div>
    </div>   
  </div>
 </div>
 
 <div class="clr"></div>

<%@ include file="../footer.jsp"%>
</body>
</html>

<script type="text/javascript">
var wait = 5; //停留时间
//document.getElementById('autoReturn').setAttribute("href","javascript:void(0)");
function updateinfo(){
	if(wait == 0){
		var retUrl = '${order.retUrl}';
		window.location.href = retUrl;
		//document.getElementById('autoReturn').setAttribute("href","${order.retUrl}");
    }else{
		document.getElementById('autoReturn').innerHTML = wait+"秒后自动返回";
		wait--;
		window.setTimeout("updateinfo()", 1000);
    }
}
updateinfo();
</script>