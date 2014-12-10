<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" isELIgnored="false"%>
<%@ taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import='java.util.Date'%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  	<%@ include file="/WEB-INF/page/meta_mw.jsp" %>
  	<title>中国移动通信账户支付 - 网上支付 安全快速！</title>
	<script type="text/javascript" src="${pageContext.request.contextPath}/static/js/validator.js"></script>
	<script>
		function clearMobileIdErr(){
			document.getElementById('userMobileId_error').style.display='none';
		}
		
		function clearCodeErr(){
			document.getElementById('j_captcha_response_error').style.display='none';
			document.getElementById("backError").style.display='none';
		}
	</script>
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
           <dt><span>订单号：</span> ${order.orderId }</dt>
           <dt>
             <span>商户名称：</span>
             <c:choose>
               <c:when test="${order.merName2!=null && order.merName2!=''}">${order.merName2 }</c:when>
               <c:otherwise>${order.merName }</c:otherwise>
             </c:choose>
           </dt>
           
           <c:if test="${order.goodsId!=null && order.goodsId!=''}">
           <dt><span>商品名称：</span>
             <c:choose>
               <c:when test="${order.goodsName2!=null && order.goodsName2!=''}">${order.goodsName2 }</c:when>
               <c:otherwise>${order.goodsName }</c:otherwise>
             </c:choose>
           </dt>
           </c:if>
           
           <dt><span>商品价格：</span><span class="red_cor"> ${order.amount4dollar }
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
           <dt><span>下单日期：</span> ${order.merDate }</dt>
           <dt><span>客服电话：</span> ${order.cusPhone }</dt>
         </dl>
       </div>
       
       
       <div class="cnt_right">
       <form name="hfForm" method="post" action="${pageContext.request.contextPath}/pay/page2.do" onSubmit="return Validator.Validate(this,2)">
         <input type="hidden" name="merId" value="${order.merId }" />
         <div class="tab_name">话费支付</div>
         <div class="ctn_nr">
         <div class="ctn_nr_left">
          <dl>
            <c:if test="${isLevel2}">
            <dt><strong>请先购买等值的${order.goodsName2}，用于支付您选购的商品。</strong></dt>
            </c:if>
            <dt class="tel_ipt">
              <em>
              <span>手机号码：</span>
              <input name="userMobileId" type="text"
                size="11" maxlength="11" dataType="Custom" msg="请输入正确的中国移动手机号"
                regexp="${mobileIdRegex }" value="${userMobileId }" onblur="clearMobileIdErr();""/>
              </em>
              <c:if test="${mobileIdError != null}">
                <span class="zs_font"  style="display:none; height:28px; line-height:28px; float:left;color:red;">请输入正确的中国移动手机号</span>
              </c:if>
              <em id="userMobileId_error" class="zs_font" style="display:none; color:red;">请输入正确的中国移动手机号</em>
            </dt>
            <dt >
              <em style="float:left;">
                <span>验证码：</span>
                <input class="yzm_ipt" name="j_captcha_response" type="text" value=""
                    maxlength="6" dataType="Custom" regexp="^[A-Za-z0-9]{6}$"
                    msg="验证码不正确，请重新输入，字母不区分大小写" onblur="clearCodeErr();" />
              </em>
              <img src="${pageContext.request.contextPath}/jcaptcha?now=<%=new Date().getTime()%>" alt="点击图片更换验证码" 
                    onclick="this.src='${pageContext.request.contextPath}/jcaptcha?now=' + new Date().getTime()"  class="yzm_img" />
              
         
              
            </dt>
            <dt style="margin:0px 0px 0px 0px;">
                <em id="backError" class="zs_font" >
                <c:if test="${jcaptchaError != null}">                             
                                                      验证码不正确，请重新输入，字母不区分大小写                                       
                </c:if>
              </em>
              <em id="j_captcha_response_error"  class="zs_font"  style="display:none; height:28px; line-height:28px; float:left;color:red;">请输入6位有效验证码，字母不区分大小写</em>
            </dt>
             <div class="clr"></div>           
          </dl>
             <div class="clr"></div> 
             <div style=" float:left;"><input type="submit" value="确认支付" class="qrzf_btn"/></div>  
         </div>
       
         </div>
       </form>
       
       </div>
    </div>
    
 </div>
 
   <div class="clr"></div>
 <div class="wenxin_tip">
   <dl>
     <dt class="tip_title"><span>温馨提示：</span></dt>
     <dt>1、中国移动通信账户支付是中国移动电子商务的支付方式之一。10658008是中国移动通信账户支付专用短信端口号码</dt>
     <dt>2、本服务支持移动全球通、动感地带、神州行等品牌的用户，用户使用中国移动通信账户支付：无免费试用；支付成功，即刻扣费；商品价格不含通信费</dt>
     <dt>3、业务使用中会产生0.1元/条（或按照您参与的运营商话费套餐标准）短信通信费，具体请咨询10086</dt>
     <dt>4、部分省份“赠送”“返还”的话费不可用于购买商品。如：北京、黑龙江等</dt>
     <dt>5、部分商品可用话费购买的数量有限，若购买失败，请选择其他支付方式</dt>
     <dt>6、中国移动通信账户支付客服电话：4006125880（只收市话费，无长途话费）</dt>
   </dl>
 </div>
 
<%@include file="../footer.jsp" %>
</body>
</html>
