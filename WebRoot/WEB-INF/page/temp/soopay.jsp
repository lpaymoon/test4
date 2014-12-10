<div align="center" >
		<a href="${adMesaage.adHref}" target="_blank"></a><img style="cursor:pointer" src="${pageContext.request.contextPath}/static/upload/ad/${adMesaage.adSrc}" onclick="newClick()" />
</div>
<script>
function newClick(){
	window.open('${adMesaage.adHref}');
}
</script>

