<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" isELIgnored="false"%>
<%@ taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<%@ include file="/WEB-INF/page/meta.jsp" %>
<title>中国移动通信账户支付 - 网上支付 安全快速！</title>
<script type="text/javascript">
function openwin(type){
	if(type==1){
		if(document.getElementById('smsSub') != null){
			document.getElementById('smsSub').submit();
		}
	}
	if(type==2){
		//var isShow5iPlay = '${show5iPlay}';
		//if(isShow5iPlay == 'true'){
			//window.open ('http://page.5iplay.cn/upload/file/2011/07/4e166073769e5.htm', 'newwindow', 'height=350, width=450, top=0,left=0, toolbar=no, menubar=no, scrollbars=no,resizable=no,location=no, status=no');
		//}
		document.getElementById('statusForm').submit();
	}	
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
       
       
    <div class="cnt_right2">
    <c:if test="${exAction == 'pageOrderAction'}">
      <div class="right_title">尊敬的<span class="red_cor"> ${order.mobileId} </span>用户您好！</div>
      <div class="tip_ctn">
        <c:choose>
          <c:when test="${order.orderState == 0}">
            <c:if test="${order.retCode == 0000}">
              <img src="${pageContext.request.contextPath}/static/images/newpage_2014/bz_img2.png" />
              <p>请注意查收来自“<span class="red_cor">${smsSub}</span>”的短信，并根据提示确认支付，谢谢！</p>
              <c:if test="${sessionScope.smsParam.sendCount <= 2 && order.bankId!='XE791000'}">
              <form action="${pageContext.request.contextPath}/pay/sms.do" name="smsSub" id="smsSub">
                <p>如1分钟内未成功接收短信，请点击<a href="javascript:void(0);" onclick="openwin(1)" class="js_btn">重新接收</a></p>
              </form>
              </c:if>
              <c:if test="${order.bankId=='XE791000'}">
                <p>如1分钟内未成功接收短信，请重新下订单订购</p>
              </c:if>
            </c:if>
            <c:if test="${order.retCode == 1163}">
              <p>尊敬的用户，您不必重复下单，请按短信提示完成支付！</p>
            </c:if>
          </c:when>
          <c:otherwise>
            <!-- 错误时出现 -->
            <c:when test="${order.orderState == 1}">
              <p>您不用重复下单，系统正在受理您的支付请求，请注意查收“<span>${smsSub}</span>”下发的提示短信！</p>
            </c:when>
            <c:when test="${order.orderState==2 && order.payRetCode=='0000'}">
              <c:if test="${order.retCode == 1163}">
                <p>订单已成功支付，不必重复下订单。</p>
              </c:if>
              <c:if test="${order.retCode == 1165}">
                <p>您已定制该服务，不必重复下单。</p>
              </c:if>
            </c:when>
            <c:when test="${order.orderState == 3}">
              <p>非常抱歉！由于系统忙，订单支付失败！请您稍候再试。</p>
            </c:when>
            <c:when test="${order.orderState == 4}">
              <p>请您重新下订单。如仍有疑问，请咨询客服中心：<span>4006125880</span></p>
            </c:when>
            <c:otherwise>
              <p> 非常抱歉！由于系统忙，订单支付失败！请您稍候再试。</p>
            </c:otherwise>
          </c:otherwise>
        </c:choose>
      </div>
    </c:if>
        
    <c:if test="${exAction != 'pageOrderAction'}">
      <div class="right_title">尊敬的用户您好！</div>
      <div class="tip_ctn">
        <c:choose>
          <c:when test="${order.orderState == 0}">
            <c:choose>
              <c:when test="${order.bankId=='XE791000'}">
                <p>您的订单尚未支付成功，请注意查收“<span>${smsSub}</span>”下发的提示短信，并按短信提示进行操作，即可完成支付</p>
              </c:when>
              <c:otherwise> 
                <p>您尚未回复订单支付的确认短信，请注意查收“<span>${smsSub}</span>”下发的提示短信，并按短信提示进行回复，即可完成支付</p>
              </c:otherwise>
            </c:choose>
            <c:if test="${sessionScope.smsParam.sendCount <= 2  && order.bankId!='XE791000'}">
              <form action="${pageContext.request.contextPath}/pay/sms.do" name="smsSub" id="smsSub">
                <p>
                  <span>如1分钟内未成功接收短信，请点击</span>
                  <a href="javascript:void(0);" onclick="openwin(1)" class="js_btn">重新接收</a>
                </p>
              </form>
            </c:if>
            <c:if test="${order.bankId=='XE791000'}">
              <p>
                <span>如1分钟内未成功接收短信，请重新下订单订购</span>
              </p>
            </c:if>
          </c:when>
          <c:when test="${order.orderState == 1}">
            <p>系统正在受理您的支付请求，请注意查收“<span>${smsSub}</span>”下发的短信，感谢您耐心等待</p>
          </c:when>
          <c:when test="${order.orderState==2 && order.payRetCode=='0000'}">
            <p>请注意查收“<span>${smsSub}</span>”支付信息短信</p>
          </c:when>
          <c:otherwise>
            <p><span>订单支付失败！</span></p>
            <p>请查收“<span>${smsSub}</span>”下发的通知短信，如提示回复错误，重新回复后点击“支付完成”即可完成支付。其他失败原因请咨询<span>4006125880</span>
          </c:otherwise>
        </c:choose>
      </div>
    </c:if>
    
    <div class="clr"></div>
    <div class="tip_ctn_txt2">
    <form action="${pageContext.request.contextPath}/pay/payResult.do" method="post" id="statusForm">
      <c:choose>
      <c:when test="${order.orderState!=0 && order.orderState!=1 && order.orderState!=2}">
        <a id="buyButton" href="javascript:void(0)" onclick="openwin(2);" class="zfwc_btn">支付完成</a>
        <a id="closeButton" class="re_back" href="${errorRetUrl}">返回商家</a>
      </c:when>
      <c:otherwise>
        <a id="buyButton" href="javascript:void(0)" onclick="openwin(2);" class="zfwc_btn">支付完成</a>
      </c:otherwise>
      </c:choose>
    </form>
    </div>
  </div>
 </div>
 </div>
 <div class="clr"></div>

<!--footer start-->
<%@ include file="../footer.jsp"%>
<!--footer ending-->
</body>
</html>
<script type="text/javascript">
var wait = 5; //停留时间
document.getElementById('buyButton').disabled = true;
document.getElementById('buyButton').setAttribute("onclick","return false;");//buyButton第三个属性（onclick）禁用
function updateinfo(){
	if(wait == 0){
    document.getElementById('buyButton').disabled = false;
    document.getElementById('buyButton').innerHTML = "支付完成";
    document.getElementById('buyButton').onclick=function () {openwin(2)};
  }else{
		document.getElementById('buyButton').innerHTML = "请稍后"+wait;
		wait--;
		window.setTimeout("updateinfo()",1000);
  }
}
updateinfo();
</script>