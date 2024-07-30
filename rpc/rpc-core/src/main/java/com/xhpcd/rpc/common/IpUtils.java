package com.xhpcd.rpc.common;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * 用户获取正确真实IP
 */
public class IpUtils {
    public static String getRealIp()  {
        String ip=null;
        Enumeration<NetworkInterface> networkInterfaces = null;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();
                if (inetAddress.isLoopbackAddress()) {//回路地址，如127.0.0.1
//                    System.out.println("loop addr:" + inetAddress);
                } else if (inetAddress.isLinkLocalAddress()) {//169.254.x.x
//                    System.out.println("link addr:" + inetAddress);
                } else {
                    //非链接和回路真实ip
                    String localname = inetAddress.getHostName();
                    String localip = inetAddress.getHostAddress();
                    ip=localip;
                    return ip;
                }
            }
        }
        return ip;
    }
}
