<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<script type="text/javascript" src="${pageContext.request.contextPath}/static/js/jquery.js"></script>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/css.css" type="text/css" />
<script type="text/javascript">   
   $(document).ready(function(){       
      $("#tip").click(function(){        
         $("#help").toggle();
      });               
   });      
</script>
<title></title>
</head>
<body>
<div class="cz_bg">
	<div class="head">充值流程咨询电话：400-612-5880<br />
    	金山会员客服电话：400-610-7777
    </div>
	<div class="czmain">
		<div class="czm yd">
			<p class="czm4">
				${order.nextDirect }
				<c:if test="${order.orderState == 0 }">
					 如您未收到10658008的短信提示请点击<a id="tip" href="javascript:void(null);">重新接收</a><br />
				</c:if>
			</p>
			<div class="cz8" id="help" style="display: none">
				未收到信息，请咨询客服中心电话：4006125880（只收市话费）125880<br />
				（只支持移动手机，0.3元/分，无长途话费）有短信签名功能的手机，请在回复前去掉<br />签名的自动附加
				功能，以免回复信息系统无法识别拒绝支付
		    </div>
               <p class="gry1"><br/>用户使用中国移动电子商务支付，无免费试用，支付成功，即刻扣费。<br />
               	包月产品如需退订，请发送0000至10658008。
               </p>
               <p class="gry">
			</p>
			<div class="ydlogo"></div>
		</div>
	</div>
	<div class="cz3"><a href="http://pcsafe.shop.kingsoft.com/dp.php?passport=" target="_blank">每月18元 = 会员 + 5000 元电脑硬件保险</a></div>
</div>
</body>
</html>