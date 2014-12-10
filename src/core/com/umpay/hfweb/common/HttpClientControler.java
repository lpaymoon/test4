package com.umpay.hfweb.common;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;
import com.umpay.hfweb.dict.DataDict;
import com.umpay.hfweb.util.ObjectUtil;
import com.umpay.hfweb.util.SessionThreadLocal;
import com.umpay.hfweb.util.XmlParserUtil;

public class HttpClientControler {
	private static final Logger log = Logger.getLogger(HttpClientControler.class);
	//相比new XStream(new DomDriver())，性能要高
	private static final XStream xstream = new XStream();
	private MultiThreadedHttpConnectionManager httpConnectionManager;
	private HttpClient client;
	
	public HttpClientControler(int maxCon,int timeout){
		if(maxCon < 1){
			maxCon = 10;
		}
		if(timeout < 1){
			timeout = 1000;
		}
		httpConnectionManager = new MultiThreadedHttpConnectionManager();
		client = new HttpClient(httpConnectionManager);
		//每主机最大连接数和总共最大连接数，通过hosfConfiguration设置host来区分每个主机  
	    client.getHttpConnectionManager().getParams().setDefaultMaxConnectionsPerHost(maxCon);
	    client.getHttpConnectionManager().getParams().setMaxTotalConnections(maxCon);
	    //client.getHttpConnectionManager().getParams().setConnectionTimeout(timeout);
	    //client.getHttpConnectionManager().getParams().setSoTimeout(10000);
	    client.getHttpConnectionManager().getParams().setConnectionTimeout(12000);
	    client.getHttpConnectionManager().getParams().setSoTimeout(timeout);
	    client.getHttpConnectionManager().getParams().setTcpNoDelay(true);
	    client.getHttpConnectionManager().getParams().setLinger(-1);
	    client.getHttpConnectionManager().getParams().setStaleCheckingEnabled(true);
	    //失败的情况下不会再尝试
	    //client.getHttpConnectionManager().getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0,false));
	}
	
	public void destory(){
		if(client!=null){
			MultiThreadedHttpConnectionManager.shutdownAll();
			client=null;
			log.warn("MultiThreadedHttpConnectionManager Destoryed");
		}
	}
	
	
	/** ********************************************
	 * method name   : getHttpResXstreamGet 
	 * description   : Xstream序列化方式发生http请求_Get
	 * @return       : Map
	 * @param        : @param get
	 * @param        : @param m
	 * @see          : 
	 * ********************************************/      
	public Object getHttpResGet_Form(Map m,GetMethod get){
		try {
			get.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,new DefaultHttpMethodRetryHandler(0, false));
			long beginTime = System.currentTimeMillis();
			get.getParams().setVersion(HttpVersion.HTTP_1_1);
    		ObjectUtil.logInfo(log,"getHttpResGet_Form URI 请求路径:%s ",get.getURI());
			NameValuePair[] data = getData(m);
    		get.setQueryString(data);
    		ObjectUtil.logInfo(log,"getHttpResGet_Form Param 请求参数:%s ",m);
    		client.executeMethod(get);
    		long time2 = System.currentTimeMillis();
    		ObjectUtil.logWarn(log,"executeMethod useTime:%s ",time2-beginTime);
    		if (get.getStatusCode() == HttpStatus.SC_OK){ 
    			Object obj = xstream.fromXML(new InputStreamReader(get.getResponseBodyAsStream(),"UTF-8"));
    			ObjectUtil.logWarn(log,"xstream.fromXML useTime:%s ",System.currentTimeMillis()-time2);
    			ObjectUtil.logInfo(log,"getHttpResGet_Form Response 响应包体:%s ",obj);
    			return obj;
    		}else{
    			return null;
    		}
		} catch (Exception e) {
			log.error(ObjectUtil.handlerException(e, getRpid()));
			return null;
		} finally {
			get.releaseConnection();
		}
	}
	
	/** ********************************************
	 * method name   : getHttpResXstreamPost 
	 * description   : Xstream序列化方式发生http请求Post Method提交，XStream解析
	 * @return       : Map
	 * @param        : @param post
	 * @param        : @param m
	 * @see          : 
	 * ********************************************/      
	public Object getHttpResPost_Xstream(Map m,PostMethod post){
		try {
			post.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,new DefaultHttpMethodRetryHandler(0, false));
			long beginTime = System.currentTimeMillis();
			String body = xstream.toXML(m);
			RequestEntity entity= new StringRequestEntity(body, "text/xml", "utf-8");
			post.setRequestEntity(entity);
			ObjectUtil.logInfo(log,"getHttpResPost_Xstream URI 请求路径:%s ",post.getURI());
			ObjectUtil.logInfo(log,"getHttpResPost_Xstream Data 请求数据:%s ",m);
    		client.executeMethod(post);
    		long time2 = System.currentTimeMillis();
    		ObjectUtil.logWarn(log,"executeMethod useTime:%s ",time2-beginTime);
    		if (post.getStatusCode() == HttpStatus.SC_OK){ 
    			Object obj = xstream.fromXML(new InputStreamReader(post.getResponseBodyAsStream(),"UTF-8"));
    			ObjectUtil.logWarn(log,"xstream.fromXML useTime:%s ",System.currentTimeMillis()-time2);
    			ObjectUtil.logInfo(log,"getHttpResPost_Xstream Response 响应包体:%s ",obj);
    			return obj;
    		}else{
    			return null;
    		}
		} catch (Exception e) {
			log.error(ObjectUtil.handlerException(e, getRpid()));
			return null;
		} finally {
			post.releaseConnection();
		}
	}
	
	/** ********************************************
	 * method name   : getHttpResXstreamPost 
	 * description   : Xstream序列化方式发生http请求Post Method提交，XStream解析
	 * @return       : Map
	 * @param        : @param post
	 * @param        : @param m
	 * @see          : 
	 * ********************************************/      
	public Object getHttpResPost_Xstream(List m,PostMethod post){
		try {
			post.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,new DefaultHttpMethodRetryHandler(0, false));
			long beginTime = System.currentTimeMillis();
			String body = xstream.toXML(m);
			RequestEntity entity= new StringRequestEntity(body, "text/xml", "utf-8");
			post.setRequestEntity(entity);
			ObjectUtil.logInfo(log,"getHttpResPost_Xstream URI 请求路径:%s ",post.getURI());
			ObjectUtil.logInfo(log,"getHttpResPost_Xstream Data 请求数据:%s ",m);
    		client.executeMethod(post);
    		long time2 = System.currentTimeMillis();
    		ObjectUtil.logWarn(log,"executeMethod useTime:%s ",time2-beginTime);
    		if (post.getStatusCode() == HttpStatus.SC_OK){ 
    			Object obj = xstream.fromXML(new InputStreamReader(post.getResponseBodyAsStream(),"UTF-8"));
    			ObjectUtil.logWarn(log,"xstream.fromXML useTime:%s ",System.currentTimeMillis()-time2);
    			ObjectUtil.logInfo(log,"getHttpResPost_Xstream Response 响应包体:%s ",obj);
    			return obj;
    		}else{
    			return null;
    		}
		} catch (Exception e) {
			log.error(ObjectUtil.handlerException(e, getRpid()));
			return null;
		} finally {
			post.releaseConnection();
		}
	}	
	
	/** ********************************************
	 * method name   : getHttpResForm 
	 * description   : Form形式Post提交http请求,XStrem解析
	 * @return       : Map
	 * @param        : @param post
	 * @param        : @param m
	 * @see          : 
	 * ********************************************/      
	public Object getHttpResPost_Form(Map m,PostMethod post) {
		try {
			post.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,new DefaultHttpMethodRetryHandler(0, false));
			long beginTime = System.currentTimeMillis();
			NameValuePair[] data = getData(m);
			post.setRequestBody(data);
    		ObjectUtil.logInfo(log,"getHttpResPost_Form URI 请求路径:%s ",post.getURI());
    		ObjectUtil.logInfo(log,"getHttpResPost_Form Data 请求数据:%s ",m);
			int statusCode = client.executeMethod(post);
			long time2 = System.currentTimeMillis();
			ObjectUtil.logWarn(log,"executeMethod useTime:%s ",time2-beginTime);
			if(statusCode == HttpStatus.SC_OK){
				Object obj = xstream.fromXML(new InputStreamReader(post.getResponseBodyAsStream(),"UTF-8"));
				ObjectUtil.logWarn(log,"xstream.fromXML useTime:%s ",System.currentTimeMillis()-time2);
				ObjectUtil.logInfo(log,"getHttpResPost_Form Response 响应包体:%s ",obj);
				return obj;
            }else{
            	return null;
            }
		} catch (Exception e) {
			log.error(ObjectUtil.handlerException(e, getRpid()));
			return null;
		} finally {
			post.releaseConnection();
		}
	}
	
	/** ********************************************
	 * method name   : getHttpResForm 
	 * description   : form表单Post提交,返回XML,XmlParser解析
	 * @return       : Map
	 * @param        : @param post
	 * @param        : @param m
	 * ********************************************/      
	public Object getHttpResPost_XML(Map m,PostMethod post) {
		try {
			long beginTime = System.currentTimeMillis();
			XmlParserUtil xmlParser = new XmlParserUtil();
			NameValuePair[] data = getData(m);
			post.setRequestBody(data);
			post.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,new DefaultHttpMethodRetryHandler(0, false));

    		ObjectUtil.logInfo(log,"getHttpResPost_XML URI 请求路径:%s ",post.getURI());
    		ObjectUtil.logInfo(log,"getHttpResPost_XML Data 请求数据:%s ",m);
			int statusCode = client.executeMethod(post);
			long time2 = System.currentTimeMillis();
			ObjectUtil.logWarn(log,"executeMethod useTime:%s ",time2-beginTime);
			if(statusCode == HttpStatus.SC_OK){
				Object obj = xmlParser.decode(post.getResponseBodyAsString().getBytes());
				ObjectUtil.logWarn(log,"xmlParser.decode useTime:%s ",System.currentTimeMillis()-time2);
				ObjectUtil.logInfo(log,"getHttpResPost_XML Response 响应包体:%s ",obj);
				return obj;
            }else{
            	return null;
            }
		} catch (Exception e) {
			log.error(ObjectUtil.handlerException(e, getRpid()));
			return null;
		} finally {
			post.releaseConnection();
		}
	}


	public static NameValuePair[] getData(Map mp){
		//过滤空值
		Map map = new HashMap();
		Iterator it = mp.entrySet().iterator();
		int i = 0;
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			Object key = entry.getKey();
			Object value = entry.getValue();
			if(value!=null){
				map.put(key, value);
			}
		}
		NameValuePair[] data = new NameValuePair[map.size()];
		it = map.entrySet().iterator();
		int j = 0;
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			Object key = entry.getKey();
			Object value = entry.getValue();
			if(value!=null){
				data[j] = new NameValuePair(key.toString(), value.toString());
				j++;
			}
		}
		return data;
	}
	
	private String getRpid(){
		return ObjectUtil.trim(SessionThreadLocal.getSessionValue(DataDict.REQ_MER_RPID));
	}
}
