<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" isELIgnored="false"%>
<%@ taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="Cache-Control" content="no-cache">
<meta name="viewport" content="initial-scale=1.0,minimum-scale=1.0,maximum-scale=2.0,user-scalable=no">
<meta name="MobileOptimized" content="240">
<title>订单支付</title>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/static/css/wap_yzm_style.css">
<script type="text/javascript" src="${pageContext.request.contextPath}/static/js/jquery.js"/></script>
<script type="text/JavaScript">
//<!--
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

<body style="background-color:#f5f5f5; ">
 <div class="contain">				   
		 <div class="ctn_bg"> 
			<dl>
			    <dt style="border-bottom:1px  dashed  #bab9b8; padding:10px 0px 0px 0px;">订单价格：<span class="red_color">￥${order.amount4dollar}元</span></dt>
			    <dt>商品名称：${goodsName}</dt>
		    </dl>         
		</div> 	  
	 <form action="${pageContext.request.contextPath}/pay/H5VerifyCodePay.do"  id="hfForm1" name="hfForm1" method="post">
      <div class="ctn_tnr">
        <dl>
          <dt style="margin:10px 0px 10px 0px;">手机号码</dt>
          <dt style="margin:0px 0px 20px 0px"><input type="text" id="mobileId" name="mobileId" onchange="checkmid()" class="tel_input" value="${mobileId}" onmouseout="this.style.borderColor=''" onFocus="if (value =='仅支持中国移动手机号码'){value =''}" onBlur="if (value ==''){value='仅支持中国移动手机号码'}" maxlength="11" msg="请
                                                              输入正确的中国移动手机号"/></dt>
          <div id="mess" style="display: none">
			    <ul class="bline">
					<li class="t_l" style="margin:0px 0px 20px 0px"><span class="org"  >
					    手机号码输入有误，请输入11位移动手机号！
					 </span></li>	
				</ul>
		  </div>
		   <div id="getfalse" style="display: none">
			    <ul class="bline">
					<li class="t_l" style="margin:0px 0px 20px 0px"><span class="org"  >
					获取验证码失败，请您稍后再试！
					 </span></li>	
				</ul>
		  </div>
          <dt >
            <input name="captcha" id="captcha"  type="tel" class="yzm_input" value="验证码"  onblur="clearErr();"  onmouseover="this.style.borderColor=''" onmouseout="this.style.borderColor=''" onFocus="if (value =='验证码'){value =''}" onBlur="if (value ==''){value='验证码'}"  maxlength="6" regexp="^[0-9]{6}$"
                    msg="验证码不正确，请重新输入"/>
          	<input name="btn" id="btn" onclick="check(this)" class="yzm_btn" value="获取验证码" type="button" />
          </dt>
            <div id="chekyzm" style="display: none">
			    <ul class="bline">
					<li class="t_l" style="margin:0px 0px 20px 0px"><span class="org"  >
					验证码输入格式有误，请重新输入！
					 </span></li>	
				</ul>
		  </div>
          <c:if test="${retCode != '0000'}">
			<div class="blank"></div>
			  <div id="back_error"  >
			    <ul class="bline">
					<li class="t_l" style="margin:0px 0px 20px 0px"><span class="org"  >
					${retMsg}
					 </span></li>	
				</ul>
		     </div>
          </c:if>
          <dt id="tips" style="color:#f53c3d; clear:both;margin:10px 0px 20px 0px; font-size:16px;"></dt>						
          <dt><input name="confirmPay" id="confirmPay" type="button" onclick="qrzf()"class="qr_bt" value="确认支付"/></dt>                                       
          <dt style="margin:10px 0px 0px 0px;">1、支付手机将用于接收券码。</dt>
          <dt>2、电子票券、兑换券类商品不支持退换货。</dt>
        </dl>
      </div>
       <div class="btm_img"><img src="${pageContext.request.contextPath}/static/images/wap_yzm/btm_logo.png"/></div>
	   <input type="hidden" name="merId" value="${order.merId}"/>
       <input type="hidden" name="goodsId" value="${order.goodsId}"/>
 
	</form>		   
											 
  </div>

 
<script type="text/javascript">

function clearErr(){
	document.getElementById("back_error").style.display='none';
}

function checkmid(){
	document.getElementById("mess").style.display="none";
    var mobileid= $("#mobileId").val();
  //  $("#mobileId").val(mobileid);
    var ch  = /^(13[4-9]|15[0,1,2,7,8,9]|18[2,3,4,7,8]|147){1}[0-9]{8}$/;//验证号段格
    if(mobileid==""||!(ch.test(mobileid) )){
        document.getElementById("mess").style.display="";
	return;
   }
}

  var InterValObj; //timer变量，控制时间
  var count = 60; //间隔函数，1秒执行   60秒
  var curCount;//当前剩余秒数
	function check(val){
	    document.getElementById("getfalse").style.display="none";  //默认不显示
		document.getElementById("mess").style.display="none";
    	var mobileid= $("#mobileId").val();
   // 	$("#mobileId").val(mobileid);
    	var ch  = /^(13[4-9]|15[0,1,2,7,8,9]|18[2,3,4,7,8]|147){1}[0-9]{8}$/;//验证号段格
    	if(mobileid==""||!(ch.test(mobileid) )){
    		document.getElementById("mess").style.display="";
			return;
   		} 
   		curCount = count;   
   		val.setAttribute("disabled", true); 
   		$("#btn").val(curCount + "S");
       	InterValObj = window.setInterval(SetRemainTime, 1000); //启动计时器，1秒执行一次//向后台发送处理数据
	   $.post( "${pageContext.request.contextPath}/pay/H5VerifyCode.do",
	   			{merId : "${order.merId}",
	   			 goodsId : "${order.goodsid}",
	   			 mobileId : mobileid},
	   			   function(data){   			   
	   			     if(data == "false"){
	   			    	window.clearInterval(InterValObj);//停止计时器
		              	document.getElementById("btn").disabled=false; //启用按钮
	   			        document.getElementById("getfalse").style.display="";
	   			        $("#btn").val("重新获取");
	   			     }          
		          }
		          );
	 
     
	}
	//timer处理函数
	function SetRemainTime() {
	            if (curCount == 0) {                
	                window.clearInterval(InterValObj);//停止计时器
	              	document.getElementById("btn").disabled=false; //启用按钮
	              	document.getElementById("getfalse").style.display="none"; 
	              	$("#btn").val("获取验证码");
	            }
	            else {
	                curCount--;
	              	$("#btn").val(curCount + "S");
	            }
	 }
	
	function qrzf(){
		document.getElementById("confirmPay").disabled=true;
		document.getElementById("mess").style.display="none";
		document.getElementById("chekyzm").style.display="none";  //默认不显示
		if(document.getElementById("back_error")!=null){
		    document.getElementById("back_error").style.display="none";  //默认不显示
		}
	    var mobileid= $("#mobileId").val();
	  //  $("#mobileId").val(mobileid);
	    var ch  = /^(13[4-9]|15[0,1,2,7,8,9]|18[2,3,4,7,8]|147){1}[0-9]{8}$/;//验证号段格
	    if(mobileid==""||!(ch.test(mobileid) )){
	        document.getElementById("mess").style.display="";
	        document.getElementById("confirmPay").disabled=false;
		    return;
	   }
	    
	    var captcha= $("#captcha").val();
		  //  $("#mobileId").val(mobileid);
		    var chd  = /^[0-9]{6}$/;//验证码格式校验
		    if(captcha==""||!(chd.test(captcha) )){
		        document.getElementById("chekyzm").style.display="";
		        document.getElementById("confirmPay").disabled=false;
			    return;
		   }
		document.forms.hfForm1.submit();
	}

</script>

</body>

</html>
