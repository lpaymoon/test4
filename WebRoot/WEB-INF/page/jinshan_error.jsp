<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<script type="text/javascript" src="${pageContext.request.contextPath}/static/js/forward.js"></script>
<title>无标题文档</title>
<style type="text/css">
<!--
.STYLE3 {
	font-size: 12px;
	line-height: 17px;
}

.STYLE4 {
	font-size: 12px;
	color: #FF0000;
}

.STYLE6 {
	font-size: 14px;
	font-weight: bold;
}

.STYLE8 {
	color: #FF0000
}

.STYLE9 {
	font-size: 11px;
	color: #666666;
}
-->
</style>

<style type="text/css">
<!--
.STYLE10 {
	font-size: 12px;
	color: #000000;
}

.STYLE11 {
	font-size: 12px;
	color: #0066B3;
}

.STYLE14 {
	font-size: 11px;
	color: #FF0000;
}
-->
</style>
</head>

<body>
<table width="500" border="0" cellpadding="0" cellspacing="0">
	<tr>
		<td height="300" align="center" valign="top" bgcolor="f9f8f7">
		<table width="100%" height="46" border="0" cellpadding="0"
			cellspacing="0">
			<tr>
				<td height="46" valign="top"
					background="${pageContext.request.contextPath}/static/images/jinshan/xian.jpg">
				<table width="100%" border="0" cellspacing="0" cellpadding="0">
					<tr>
						<td width="33%"></td>
						<td width="67%" align="right">
						<table width="78%" border="0" cellspacing="0" cellpadding="0">
							<tr>
								<td align="center"><span class="STYLE11">客服中心：4006125880
								125880</span></td>
							</tr>
						</table>
						</td>
					</tr>

				</table>
				</td>
			</tr>
		</table>
		<table width="100%" border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td height="25" align="center" valign="middle">
				<span class="STYLE6">
					<img src="${pageContext.request.contextPath}/static/images/jinshan/cg.jpg" width="18" height="18" />
				</span></td>
			</tr>
			<tr>
				<td height="29" align="center" valign="top">
				<p align="center" class="STYLE4">${nextDirect }</p>
				</td>
			</tr>			 
		</table>		 
		</td>
	</tr>
</table>
</body>
</html>
