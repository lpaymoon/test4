package com.umpay.hfweb.util.test;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;


public class IPGET {
   public static void main(String[] args) throws Exception{
	   System.out.println("��第一种方法获得的IP："+InetAddress.getLocalHost().getHostAddress().toString());
	   System.out.println("=================================================================");
	   System.out.println("机器中有多个网卡的情况：");
	   System.out.println("我现在默认取得本机eth2网卡的IP可以任意修改，也可做成函数传入所要的网卡");
	   Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();   
       String mm = null;
	   while (interfaces.hasMoreElements()) {   
          NetworkInterface ni = interfaces.nextElement();   
          String m[]=ni.toString().split("/"); 
          if(ni.getName().toString().equals("eth2")){
          for(int i=0;i<m.length;i++){
        	   mm=m[1];
          }
       }   
      
	   }
	   System.out.println(mm);
   }
}
