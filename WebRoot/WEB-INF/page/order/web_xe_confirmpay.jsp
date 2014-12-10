<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" isELIgnored="false"%>
<%@ taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import='java.util.Date'%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<%@ include file="/WEB-INF/page/meta.jsp" %>
	<title>中国移动通信账户支付 - 网上支付 安全快速！</title>
	<script type="text/javascript" src="${pageContext.request.contextPath}/static/js/validator.js"></script>
	<script type="text/javascript">
	function qrzf(){
		document.getElementById('btnQrzf').disabled=true;
		document.forms.hfForm.submit();
	}
	function clearCodeErr(){
			document.getElementById('j_captcha_response_error').style.display='none';
			document.getElementById("backError").style.display='none';
	}
	</script>
</head>
<body>
 <div class="contain">
    <div class="top_logo"><img src="${pageContext.request.contextPath}/static/images/newpage_2014/logo_xe.png" /></div>
    <div class="clr"></div>
    <div class="yd_tip">移动话费支付（通信账户）交易订单</div>

    <div class="nr_cnt">
      <div class="cnt_left">
         <div class="dd_title">订单信息</div>
         <dl>
           <dt><span>订单号：</span> ${order.orderId }</dt>
           <dt><span>商户名称：</span>
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
         <div class="tab_name">话费支付</div>
         <div class="ctn_nr">
	         <c:if test="${qrCodeUrl!=null && qrCodeUrl!=''}">
	          <div style="width:130px; height:40px;    float:right;"><img src="${pageContext.request.contextPath}/static/images/newpage_2014/right_gg.gif" /></div>
	         </c:if>
       <form name="hfForm" method="post" action="${pageContext.request.contextPath}/pay/saveOrder.do" onSubmit="return Validator.Validate(this,2)">
         <c:choose>
         <c:when test="${isAuthCodeMer == null}">
         <div class="ctn_nr_left">
          <dl>
            <dt class="tel_ipt"><span>手机号码：</span><input name="mobileId" readonly="readonly" type="text" value="${order.mobileId }"/></dt>
            <dt style="margin:71px 0px 0px 0px;"><em style="float:left;"><span></span></em></dt> 
            <dt><input id="btnQrzf" type="button" onclick="qrzf();" value="确认支付" class="qrzf_btn"/></dt>
          </dl>          
          <c:if test="${isQRCodeOrder}">
          <div class="red_cor" style=" text-align:center; margin:5px 0px 0px 0px; padding:0px;  ">*您已通过二维码提交订单，请在话付宝app中完成支付</div>
          </c:if>
         </div>
         </c:when>
         <c:otherwise>
         <div class="ctn_nr_left">
          <dl>
            <dt class="tel_ipt"><span>手机号码：</span><input name="mobileId" readonly="readonly" type="text" value="${order.mobileId }"/></dt>
            <dt >            
                 <em style="float:left;"><span>验证码：</span><input class="yzm_ipt" name="j_captcha_response" type="text" value=""
                    maxlength="6" dataType="Custom" regexp="^[A-Za-z0-9]{6}$"
                    msg="验证码不正确，请重新输入，字母不区分大小写" onblur="clearCodeErr();" />
              </em>
              <img src="${pageContext.request.contextPath}/jcaptcha?now=<%=new Date().getTime()%>" alt="点击图片更换验证码" 
                    onclick="this.src='${pageContext.request.contextPath}/jcaptcha?now=' + new Date().getTime()"  class="yzm_img" />
             
              <input type="hidden" name="isAuthCodeMer" value="${isAuthCodeMer }" />
            </dt>
            <dt style="margin:0px 0px 0px 0px;">
               <em id="backError" class="zs_font">
                <c:if test="${jcaptchaError != null}">                             
                                                      验证码不正确，请重新输入，字母不区分大小写                                       
                </c:if>
              </em>
              <em id="j_captcha_response_error" class="zs_font" style="display:none;height:28px; line-height:28px; float:left;color:red;">请输入6位有效验证码，字母不区分大小写</em>
            </dt>
           <dt><input id="btnQrzf" type="submit" value="确认支付" class="qrzf_btn_sec"/></dt>
          </dl>
          <c:if test="${isQRCodeOrder}">
          <div class="red_cor" style=" text-align:center; margin:22px 0px 0px 0px; padding:0px; background：red  ">*您已通过二维码提交订单，请在话付宝app中完成支付</div>
          </c:if>
         </div>
         </c:otherwise>
         </c:choose>
       </form>
        
        <c:if test="${qrCodeUrl!=null && qrCodeUrl!=''}">
         <form action="${pageContext.request.contextPath}/pay/qRCodePayResult.do" method="post" id="statusForm">
         <div class="ctn_nr_right">
            <div class="right_down_sty"><img src="${pageContext.request.contextPath}/static/images/newpage_2014/icon_sj.jpg" style="margin:0px 3px 0px 0px" />您也可以选择通过“话付宝”<span class="down_sty"><a href="http://h.umpay.com/" target="_blank">[下载]</a></span>扫码，一键完成支付！轻松快捷，并有好礼相送！<span class="red_cor">（仅限左侧手机号本机扫描）</span></div>
            <div style=" text-align:center;"><img src="${qrCodeUrl}" /></div>
            <div><input name="btnzfwc" type="submit"  value="支付完成" class="zfwc_btn_index"/></div>
            <div class="red_cor" style=" text-align:center; margin:5px 0px 0px 0px; padding:0px;  ">*请使用话付宝app扫描二维码并完成支付</div>
         </div>
         </form>
         </c:if>
       
       </div>
    </div>
    </div>
    
 </div>
 
   <div class="clr"></div>
 <div class="wenxin_tip">
   <dl>
     <dt class="tip_title"><span>温馨提示：</span></dt>
     <dt>1、中国移动通信账户支付是中国移动电子商务的支付方式之一。10658008是中国移动通信账户支付专用短信端口号码</dt>
     <dt>2、本服务支持移动全球通、动感地带、神州行等品牌。用户使用中国移动通信账户支付时无免费试用，支付成功即可扣费（商品价格均不含通信费）</dt>
     <dt>3、业务使用中会产生0.1元/条（或按照您参与的运营商话费套餐标准）短信通信费，具体请咨询10086</dt>
     <dt>4、部分省份“赠送”“返还”的话费不可用于购买商品。如：北京、黑龙江等</dt>
     <dt>5、部分商品可用话费购买的数量有限，若购买失败，请选择其他支付方式</dt>
     <dt>6、中国移动通信账户支付客服电话：4006125880（只收市话费，无长途话费）</dt>
   </dl>
 </div>
 
<%@include file="../footer.jsp" %>
</body>
</html>
