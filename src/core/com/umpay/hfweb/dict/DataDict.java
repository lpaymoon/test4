package com.umpay.hfweb.dict;

public class DataDict {

	//网络信息start
	public static final String NET_CLIENTIP = "clientIp";
	public static final String NET_CLIENTPORT = "clientPort";
	public static final String NET_USERIP = "userIp";
	public static final String NET_SERVERIP = "serverIp";
	public static final String NET_SERVERPORT = "serverPort";
	public static final String NET_REFERER = "referer";
	//网络信息end
	//请求时间
	public static final String REQ_TIME = "reqTime";
	public static final String REQ_MER_RPID = "rpid";
	public static final String FUNCODE = "funCode";
	public static final String REQ_USE_TIME = "useTime";
	//系统错误
	public static final String SYSTEM_ERROR_CODE = "9999";
	//Rest未返回信息
	public static final String SYSTEM_ERROR_REST_NULL = "9998";
	//Rest返回异常信息
	public static final String SYSTEM_ERROR_REST_ERROR = "9997";
	//Rest返回类型有误
	public static final String SYSTEM_ERROR_REST_ERROR_TYPE = "9996";
	//Trade未返回信息
	public static final String SYSTEM_ERROR_TRADE_NULL = "9995";
	//Web系统业务处理超时
	public static final String SYSTEM_ALARM_TIMEOUT = "9994";
	//短信解析服务器未返回信息
	public static final String SYSTEM_ERROR_SMS_NULL = "9993";
	//成功返回码
	public static final String SUCCESS_RET_CODE = "0000";
	//返回码标签
	public static final String RET_CODE = "retCode";
	//返回消息标签
	public static final String RET_MSG = "retMsg";
	//后端系统返回码标签
	public static final String RET_CODE_BUSSI = "retcodeBussi";
	//平台签名key路径标签
	public static final String PLATEFORM_KEY = "PlatformKey";
	
	//返回给商户参数
	public static final String MER_RESP_ERROR_MESSAGE = "errorMessage";
	public static final String MER_RESP_BANKID = "bankId";
	public static final String MER_RESP_BANK_FLAG = "bankFlag";
	public static final String CHECK_SIGN_FLAG = "checkSignFlag";
	public static final String CHECK_TRADE_FLAG = "checkTradeFlag";
	//商户下单所需数据参数
	public static final String FUNCODE_ORDER_PARAM = "pageCmd";
	
	//无线接入商户相关
	public static final String FUNCODE_WX_WXXD = "WXXD";
	public static final String FUNCODE_WX_XD = "R4XD";
	public static final String FUNCODE_WX_PLATXD = "WXPLATXD";
	public static final String FUNCODE_WX_PLATCX = "WXPLATCX";
	public static final String FUNCODE_WX_WXDDCX = "WXDDCX";
	public static final String FUNCODE_WX_JCGX = "JCGX";
	public static final String FUNCODE_WX_YHXX = "WXYHXX";
	public static final String FUNCODE_WX_WXHFXD= "WXHFXD";
	public static final String FUNCODE_WX_DXCF = "WXDXCF";
	public static final String FUNCODE_WX_SMSVERIFY = "WXSMSVERIFY";
	public static final String FUNCODE_WX_HISTORYTRANS = "WXHISTORYTRANS";
	public static final String FUNCODE_WX_YHCX	= "WXYHCX";
	public static final String FUNCODE_WX_RANDOMVERIFY = "WXMMYZ";//无线密码验证
	public static final String FUNCODE_WX_ZJL = "WXZJL";//无线装机量

	//话付宝UF支付
	public static final String FUNCODE_WXUFZF = "WXUFZF";
	//话付宝Uf商户结果通知
	public static final String FUNCODE_WXUFTZ="WXUFTZ";
	
	// 卡父卡 小额平台下单  模拟商户接收请求
	public static final String FUNCODE_KFKXD = "KFKXD";
	//FunCode
	//后台直连下单
	public static final String FUNCODE_DIRECT_HTXD = "HTXD";
	//小页面下单（后台直连下单）
	public static final String FUNCODE_CLIENT_XD = "HTXDJ";//XYMXD-->HTXDJ
	//不带手机号页面下单
	public static final String FUNCODE_PAGE_XDNOMBL = "YMXD2";//NOMBLXD-->YMXD2
	//带手机号页面下单
	public static final String FUNCODE_PAGE_XD = "YMXD";//PAGEXD-->YMXD
	//WEB保存订单
	public static final String FUNCODE_PAGE_ORDSV = "QRZF";//PAGEORDERSV-->QRZF
	//前台支付结果通知
	public static final String FUNCODE_PAGE_NOTIFY = "JGTZ";//PAGENOTIFY-->JGTZ
	//前台短信重发
	public static final String FUNCODE_PAGE_SMSRESEND = "DXCF";//PAGESMSRESEND-->DXCF
	//wap页面下单
	public static final String FUNCODE_WAPPAGEXD = "WAPXD";
	//wap版保存订单
	public static final String FUNCODE_WAP_ORDSV = "QRZFQ";//WAPORDSV-->QRZFQ
	//WAP版支付结果通知
	public static final String FUNCODE_WAP_NOTIFY = "JGTZQ";//WAPNOTIFY-->JGTZQ
	//WAP版短信重发
	public static final String FUNCODE_WAP_SMSRESEND = "DXCFQ";//WAPSMSRESEND-->DXCFQ
	//商户取对账文件地址（交易对账+包月取消信息）
	public static final String FUNCODE_MER_TRADE_BILL_V2 = "V2JY2";//DAYTRANSBILL2-->V2JY2
	//商户取交易文件地址(包含地区信息的交易对账)
	public static final String FUNCODE_MER_TRADE2_BILL_V2 = "V2JY1";//JYXZ2-->V2JY1
	//商户取成功计费文件
	public static final String FUNCODE_MER_DAY_SUCCESS_BILL = "V2JFCG";//CGJF2-->V2JFCG
	//商户获取注册用户文件
	public static final String FUNCODE_MER_MONTH_REGIST_BILL = "V2YHZC";//ZCYH2-->V2YHZC
	//商户获取注销用户文件
	public static final String FUNCODE_MER_MONTH_CANCELREG_BILL = "YHZX";//ZXYH-->V2YHZX-->YHZX
	//商户取交易对账文件
	public static final String FUNCODE_MER_DAY_BILL = "JYXZ";
	//商户取清算对账文件
	public static final String FUNCODE_MER_SETTLE_BILL = "QSXZ";
	//包月用户取消
	public static final String FUNCODE_MER_CANCEL_USER = "BYQX";
	public static final String FUNCODE_MER_CANCEL_USER_V2 = "V2BYQX";//BYQX2-->V2BYQX
	//查询包月用户
	public static final String FUNCODE_MER_QUERY_MONTHSUB = "BYCX";//QRBY-->BYCX
	//查询包月用户V2
	public static final String FUNCODE_MER_QUERY_MONTHSUB_V2 = "V2BYCX";//QRBY2-->V2BYCX
	//查询订单
	public static final String FUNCODE_MER_QUERY_ORDER = "DDCX";//QROD-->DDCX
	//查询余额
	public static final String FUNCODE_MER_QUERY_BALANCE = "YECX";//QXYE-->YECX
	//查询可支付余额
	public static final String FUNCODE_MER_QUERY_PAYMENT = "KZFCX";
	//查询移动用户信息
	public static final String FUNCODE_MER_QUERY_USERINFO = "SJCX";
	//下发短信
	public static final String FUNCODE_MER_PUSH_SMS_V2 = "V2DXXF";//XFDX2-->V2DXXF
	//全网按次V2
	public static final String FUNCODE_DIRECT_HTXD_MWPER_V2 = "V2QWAC";//MWPER2-->V2QWAC
	//小额按次V2
	public static final String FUNCODE_DIRECT_HTXD_XEPER_V2 = "V2XEAC";//XEPER2-->V2XEAC
	//全网包月
	public static final String FUNCODE_DIRECT_HTXD_MONTH_V2 = "V2QWBY";//MWBY2-->V2QWBY
	
	// 刷新报备信息缓存
	public static final String FUNCODE_DIRECT_SXURL = "SXURL";
	//商户直接话费支付
	public static final String FUNCODE_QDHFZF = "HFZJZF";//QDHFZF-->HFZJZF
	//商户直接话费支付
	public static final String FUNCODE_QDHFCZ = "HFZJCZ";//QDHFCZ-->HFZJCZ
	//查询订单及交易返回码
	public static final String FUNCODE_MER_QUERY_ORDERTRANS = "DDJYCX";//查询用户订单及交易状态
	
	//wap页面直接支付--商户请求处理
	public static final String FUNCODE_WAPZJCL="WAPZJCL";
	//wap页面直接支付--发送验证码
	public static final String FUNCODE_WAPCAPTCHA = "WAPCAPTCHA";
	//wap页面直接支付--下单并支付
	public static final String FUNCODE_WAPZJZF = "WAPZJZF";
	
	
	//wap通用验证码(YZM)支付--商户请求
	public static final String FUNCODE_WAPYZMCL="WAPYZMCL";
	//wap通用验证码支付--发送验证码
	public static final String FUNCODE_WAPYZM="WAPYZM";
	//wap通用验证码支付--下单并支付
	public static final String FUNCODE_WAPYZMZF="WAPYZMZF";
	//无线商户R8版业务流程
	public static final String FUNCODE_WX_R8XD="R8XD";
	public static final String FUNCODE_WX_R8PAY = "R8PAY";
	
	//验证码信息
	public static final String SEND_SMS_CAPTCHAINFO="captchaInfo";
	//验证码
	public static final String SMS_TAG_CAPTCHA="captcha";
	
	//预授权商户请求扣款
	public static final String FUNCODE_PREAUTH_HFZF = "HFPREZF";//HFPREZF-->DCZF
	
	//福建12580买Q币
	public static final String FUNCODE_TSQDZF="TSQDZF";//特殊渠道支付（福建12580买Q币提出的需求）
	
	public static final String FUNCODE_QDXD="QDXD";//渠道下单支付业务
	
	public static final String FUNCODE_MER_REQ_SMS_MO2 = "MO2DXCF";//重发确认支付短信
	
	public static final String FUNCODE_WX_YHFK= "WXYHFK";//用户信息反馈
	
	public static final String FUNCODE_MER_REQ_QDDDCX="QDDDCX";//渠道订单查询
	
	public static final String FUNCODE_MER_REQ_QDDZ="QDDZ";//渠道商户取对账文件 潘兴武 20130604
	

	public static final String FUNCODE_UNDO_DAY_BILL = "CZXZ";// 商户取冲正交易对账文件 20130712
	
	public static final String FUNCODE_WX_VERIFY="WXYZM";//话付宝验证码支付请求
	
	public static final String FUNCODE_WX_VERIFY_XIAFA="WXYZMXF";//话付宝验证码下发
	
	public static final String FUNCODE_WX_VERIFY_ZHIFU="WXYZMZF";//话付宝验证码下发
	
	public static final String FUNCODE_HNCP_XD="HNCPXD";//海南彩票下单
	
	//短信API下单
	public static final String FUNCODE_DXAPIXD = "DXAPIXD";
	//qd扫描二维码之后通用验证码(YZM)支付--渠道请求
	public static final String FUNCODE_H5YZMCL="H5YZMCL";
	//qd扫描二维码之后通用验证码--发送验证码
	public static final String FUNCODE_H5YZM="H5YZM";
	//qd扫描二维码之后通用验证码--下单并支付
	public static final String FUNCODE_H5YZMZF="H5YZMZF";
	

	//商户请求参数
	public static final String MER_REQ_MERID = "merId";
	//R5临时订单号
	public static final String MER_REQ_PLATORDERID = "platOrderId";
	public static final String MER_REQ_MERNAME = "merName";
	public static final String MER_REQ_GOODSID = "goodsId";
	public static final String MER_REQ_GOODSINF = "goodsInf";
	public static final String MER_REQ_MOBILEID = "mobileId";
	public static final String MER_REQ_ORDERID = "orderId";
	public static final String MER_REQ_MERDATE = "merDate";
	public static final String MER_REQ_AMOUNT = "amount";
	public static final String MER_REQ_AMTTYPE = "amtType";
	public static final String MER_REQ_BANKTYPE = "bankType";
	public static final String MER_REQ_GATEID = "gateId";
	public static final String MER_REQ_RETURL = "retUrl";
	public static final String MER_REQ_NOTIFYURL = "notifyUrl";
	public static final String MER_REQ_MERPRIV = "merPriv";
	public static final String MER_REQ_PAYDATE = "payDate";
	public static final String MER_REQ_CANCELDATE = "cancelDate";
	public static final String MER_REQ_SETTLEDATE = "settleDate";
	public static final String MER_REQ_EXPAND = "expand";
	public static final String MER_REQ_BUSINESSTYPE = "businessType";
	public static final String MER_REQ_VERSION = "version";
	public static final String MER_REQ_SIGN = "sign";
	public static final String MER_REQ_BANKID = "bankId";
	public static final String MER_REQ_TRANSSTATE = "transState";
	
	//商户请求参数V2
	public static final String MER_REQ_MERID_V2 = "SPID";
	public static final String MER_REQ_GOODSID_V2 = "GOODSID";
	public static final String MER_REQ_MOBILEID_V2	= "MOBILEID";
	public static final String MER_REQ_RDPWD_V2	= "RDPWD";
	public static final String MER_REQ_SIGN_V2 = "SIGN";
	public static final String MER_REQ_AMOUNT_V2 = "AMOUNT";
	public static final String MER_REQ_REMARK_V2 = "REMARK";
	public static final String MER_REQ_REMARK2_V2 = "REMARK2";
	public static final String MER_REQ_FUNCODE_V2 = "FUNCODE";
	public static final String MER_REQ_ORDERID_V2 = "ORDERID";
	public static final String MER_REQ_DATETIME_V2 = "DATETIME";
	public static final String MER_REQ_URL_V2 = "URL";
    public static final String MER_REQ_REQDATE_V2 = "REQDATE";
    public static final String MER_REQ_TAG_V2 = "TAG";
    public static final String MER_REQ_CHNLID = "chnlId";
    public static final String MER_REQ_CHNLORDERID = "chnlOrderId";
    public static final String MER_REQ_CHNLDATE = "chnlDate";
    public static final String MER_REQ_CHNLPRIV = "chnlPriv";
	public static final String MER_REQ_ORDERDATE = "orderDate"; //20130410 panxingwu
	public static final String MER_REQ_UNSIGN = "unSign";// 20130410 panxingwu
	public static final String MER_REQ_GOODSNAME="goodsName";//20130930 panxingwu
	public static final String MER_REQ_VERIFYCODE = "verifyCode";//20130930 panxingwu
	public static final String MER_REQ_CODE = "code";//20130930 panxingwu
	
	//新增客户端专用参数   panxingwu新增于20130506
	public static final String WX_REQ_IMEI = "IMEI";//手机串号
	public static final String WX_REQ_IMSI = "IMSI";//sim卡唯一标识
	public static final String WX_ERQ_REPLYTIME = "replyTime";//用户反馈时间
	public static final String WX_ERQ_REPLYINFO = "replyInfo";
	public static final String WX_ERQ_CONTACTINFO = "contactInfo";
	public static final String WX_REQ_CLIENTNAME = "clientName";
	public static final String WX_REQ_PLATTYPE = "platType";
	public static final String WX_REQ_VERSIONCODE="versionCode";
	public static final String WX_REQ_VERSIONNAME="versionName";
	public static final String WX_REQ_PAYCHNL="payChnl";
	public static final String WX_REQ_CLIENTID = "cid";//客户端ID
	public static final String WX_REQ_ICCID    = "iccid";
	
	//资源服务配置key
	public static final String REST_SRV_URL = "REST.SRV.URL";
	//业务层服务配置key
	public static final String TRADE_SRV_URL = "TRADE.SRV.URL";
	//短信服务配置key
	public static final String SMS_SRV_URL = "SMS.SRV.URL";
	
	//商户清算对账路径3.0
	public static final String FILEPATH_BILL_SETTLE="FILEPATH.BILL.SETTLE";
	//商户交易对账路径3.0
	public static final String FILEPATH_BILL_TRADE="FILEPATH.BILL.TRADE";
	//2.0商户交易对账路径
	public static final String FILEPATH_BILL_TRADE_V2="FILEPATH.BILL.TRADE.V2";
	//2.0商户交易对账路径(包含地区信息)
	public static final String FILEPATH_BILL_TRADE2_V2="FILEPATH.BILL.TRADE2.V2";
	//包月取消对账文件路径
	public static final String FILEPATH_BILL_BYQX="FILEPATH.BILL.BYQX";
	//2.0商户获取成功计费文件路径
	public static final String FILEPATH_BILL_CHARGE_SUCCESS_V2="FILEPATH.BILL.CHARGE.SUCCESS.V2";
	 //2.0商户获取注册用户文件路径
	public static final String FILEPATH_BILL_SYNUSER_V2="FILEPATH.BILL.SYNUSER.V2";
	//渠道对账文件路径
	public static final String FILEPATH_BILL_QUDAO="FILEPATH.BILL.QUDAO";
	
	//商户冲正交易对账路径3.0
	public static final String FILEPATH_BILL_UNDO="FILEPATH.BILL.UNDO";

	//业务区分，web接入的各个业务线的业务号
	//标商户统一支付页面
	public static final String BUSI_STANDARD_WEB = "0001";
	//渠道统web支付
	public static final String BUSI_QUDAO_WEB = "0002";
	//金山小页面
	public static final String BUSI_JINSHAN_WEB = "0003";
	//江西web异步支付
	public static final String BUSI_JX_WEB = "0004";
	//wap统一支付页面
	public static final String BUSI_STANDARD_WAP = "0005";
	//江西WAP支付
	public static final  String BUSI_JX_WAP = "0006";
	//话付宝验证码支付
	public static final String BUSI_WX_YZM = "0209";
	//话付宝U付支付
	public static final String BUSI_WX_UF = "0206";//add by panxingwu 20131017
	//wap直接支付（北京wap验证码支付）
	public static final String BUSI_WAP_ZJZF = "0207";//add by panxingwu 20131017
	//wap验证码支付（通用）
	public static final String BUSI_WAP_YZMZF = "0208";//add by panxingwu 20131017
	//sdk快捷支付
	public static final String BUSI_WX_KJZF = "0216";//add by chenwei 20140804
	//标商后台直连下单
	public static final String BUSI_BSZL = "0301";//add by panxingwu 20131017
	//渠道直联
	public static final String BUSI_QDZL = "0302";//add by panxingwu 20131017
	//清结算支付
	public static final String BUSI_QJSZF = "0303";//add by panxingwu 20131017
	//小额直接支付
	public static final String BUSI_XEZJZF = "0304";//add by panxingwu 20131017
	//海南基地彩票业务
	public static final String BUSI_HNCP = "0307";//add by zhuoyangyang 20140311
	//短信api支付业务
	public static final String BUSI_DXAPI = "0106";//add by chenwei 20140327
	//R4无卡支付
	public static final String BUSI_R4WKZF = "0211";//add by panxingwu 20140116
	//R8无线业务
	public static final String BUSI_R8 = "0212";//add by panxingwu 20140225
	
	//话付宝扫码下单
	public static final String BUSI_QRCODE = "0108";//add by xuwei 20140619
	//渠道验证码支付
	public static final String BUSI_QD_YZMZF = "0109";//add by xuwei 20140619
	
	//wap线下扫码 验证码支付（通用）
	public static final String BUSI_WAP_CRCODE_YZMZF = "0219";//add by xuwei 20140821
	
	//渠道验证码支付
	public static final String FUNCODE_QD_VERIFY_ZF = "QDYZMZF";//add by xuwei 20140619
	public static final String FUNCODE_QD_VERIFY_XD = "QDYZMXD";//add by xuwei 20140619
	
	//12580 SDK 客户端相关
	public static final String FUNCODE_WX_SDKXD = "WXSDKXD";//无线SDK 客户端下单
	public static final String FUNCODE_WX_SDKBD	= "WXSDKBD";//无线SDK 上传客户端公钥
	public static final String FUNCODE_WX_SDKPAY = "WXSDKPAY";//无线SDK 客户端支付
	
	public static final String MER_REQ_PORDERID = "porderId";//平台流水
	//用户全网交易累计统计
	public static final String FUNCODE_WX_MWUSERLTD = "MWUSERLTD";//用户全网交易累计统计
	//话付宝2维码下单
	public static final String FUNCODE_WX_QRCODE = "QRCODEXD";//话付宝2维码下单
	
	//话付宝2维码下单 (POST方式请求接口)
	public static final String FUNCODE_QRCODE = "QRV2XD";//话付宝2维码下单
	//短信api支付相关
	public static final String SMS_API_DDDXZL = "smsCon";//订单短信指令
	public static final String SMS_API_DXCALLED	= "smsCalled";//上行短信端口号
	public static final String SMS_API_DXTIME = "smsTime";//短信指令生成时间

} 
