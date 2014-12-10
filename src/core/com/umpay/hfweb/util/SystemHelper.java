package com.umpay.hfweb.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 * 
 * 本机系统信息
 * 
 */
public final class SystemHelper {
	private static final Logger logger = Logger.getLogger(SystemHelper.class);

	// 获得系统属性集
	public static Properties props = System.getProperties();
	// 操作系统名称
	public static String OS_NAME = getPropertery("os.name");
	// 行分页符
	public static String OS_LINE_SEPARATOR = getPropertery("line.separator");
	// 文件分隔符号
	public static String OS_FILE_SEPARATOR = getPropertery("file.separator");

	/**
	 * 
	 * 根据系统的类型获取本服务器的ip地址
	 * 
	 * InetAddress inet = InetAddress.getLocalHost(); 但是上述代码在Linux下返回127.0.0.1。
	 * 主要是在linux下返回的是/etc/hosts中配置的localhost的ip地址，
	 * 而不是网卡的绑定地址。后来改用网卡的绑定地址，可以取到本机的ip地址：）：
	 * 
	 * @throws UnknownHostException
	 */
	public static InetAddress getSystemLocalIp() throws UnknownHostException {
		InetAddress inet = null;
		String osname = getSystemOSName();
		try {
			String temp = osname.toLowerCase();
			// 针对window系统
			if(temp.contains("windows")){
				inet = getWinLocalIp();
				
			} else{//针对linux系统
				inet = getUnixLocalIp();
			}
			if (null == inet) {
				throw new UnknownHostException("主机的ip地址未知");
			}
		} catch (SocketException e) {
			logger.error("获取本机ip错误" + e.getMessage());
			throw new UnknownHostException("获取本机ip错误" + e.getMessage());
		}
		return inet;
	}
	
	/**
	 * 获取FTP的配置操作系统
	 * 
	 * @return
	 * @throws UnknownHostException 
	 */
	public static String getSystemHostName() throws UnknownHostException {
		return InetAddress.getLocalHost().getHostName();
	}

	/**
	 * 获取操作系统名称
	 * 
	 * @return
	 */
	public static String getSystemOSName() {
		// 获得系统属性集
		Properties props = System.getProperties();
		// 操作系统名称
		String osname = props.getProperty("os.name");
		return osname;
	}

	/**
	 * 获取属性的值
	 * 
	 * @param propertyName
	 * @return
	 */
	public static String getPropertery(String propertyName) {
		return props.getProperty(propertyName);
	}

	/**
	 * 获取window 本地ip地址
	 * 
	 * @return
	 * @throws UnknownHostException
	 */
	private static InetAddress getWinLocalIp() throws UnknownHostException {
		InetAddress inet = InetAddress.getLocalHost();
		return inet;
	}

	/**
	 * 
	 * 可能多多个ip地址只获取一个ip地址 获取Linux 本地IP地址
	 * 
	 * @return
	 * @throws SocketException
	 */
	private static InetAddress getUnixLocalIp() throws SocketException {
		Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
		netInterfaces = NetworkInterface.getNetworkInterfaces();   
		while (netInterfaces.hasMoreElements()) {   
		    NetworkInterface ni = netInterfaces.nextElement(); 
		    Enumeration<InetAddress> ips = ni.getInetAddresses();   
		    while (ips.hasMoreElements()) {   
		    	InetAddress tempIP = ips.nextElement();
		    	logger.info("IP:"+ tempIP.getHostAddress()+" isSiteLocalAddress:"+tempIP.isSiteLocalAddress()+" isLoopbackAddress:"+tempIP.isLoopbackAddress()+" indexOf"+tempIP.getHostAddress().indexOf(":"));   
		        //!tempIP.isSiteLocalAddress() && 
		        if (!tempIP.isLoopbackAddress() && tempIP.getHostAddress().indexOf(":") == -1) {
		       	 	return tempIP;
		        }
		    }   
		 } 
		return null;
//		Enumeration<NetworkInterface> netInterfaces = NetworkInterface
//				.getNetworkInterfaces();
//		InetAddress ip = null;
//		while (netInterfaces.hasMoreElements()) {
//			NetworkInterface ni = (NetworkInterface) netInterfaces.nextElement();
//			ip = (InetAddress) ni.getInetAddresses().nextElement();
//			if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {
//				return ip;
//			} else {
//				ip = null;
//			}
//		}
//		return null;
	}

	/**
	 * 
	 * 获取当前运行程序的内存信息
	 * 
	 * @return
	 */
	public static final String getRAMinfo() {
		Runtime rt = Runtime.getRuntime();
		return "RAM: " + rt.totalMemory() + " bytes total, " + rt.freeMemory() + " bytes free.";
	}
}
