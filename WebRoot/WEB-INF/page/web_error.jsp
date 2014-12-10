<%@ page language="java"  pageEncoding="UTF-8" 	contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"> 
<head>
<%@ include file="/WEB-INF/page/meta_mw.jsp" %>
<title>中国移动通信账户支付 - 网上支付 安全快速！</title>
</head>
<body>
 <div class="contain">
   <div class="top_logo"><img src="${pageContext.request.contextPath}/static/images/newpage_2014/logo.png" /></div>
   <div class="clr"></div>
   <div class="yd_tip">移动话费支付（通信账户）交易订单</div>

   <div class="nr_cnt">
     <div class="cnt_center">
       <div class="center_img"><img src="${pageContext.request.contextPath}/static/images/newpage_2014//dis.png" alt="" style="float:left;" /><span>交易失败！</span></div>
       <div class="center_txt">${errorMessage }!</div>
       <c:if test="${errorRetUrl!=null && errorRetUrl!=''}">
         <div class="center_btn"><a href="${errorRetUrl}" class="syserror_btn">返回商家</a></div>
       </c:if>
     </div>
   </div>
 </div>

<div class="clr"></div>
<!--footer start-->
<%@ include file="footer.jsp"%> 
<!--footer ending-->
</body>
</html>
