package com.elsa.redis.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

public class IPUtils {

	public static String getLocalIP() {
		List<String> ipList = getLocalIPList();
		for (Iterator<String> iterator = ipList.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			if ("127.0.0.1".equals(string)) {
				iterator.remove();
			}
		}
		if (ipList.size() > 0) {
			return ipList.get(0);
		}
		return "127.0.0.1";
	}

	public static List<String> getLocalIPList() {
		List<String> ipList = new ArrayList<String>();
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			NetworkInterface networkInterface;
			Enumeration<InetAddress> inetAddresses;
			InetAddress inetAddress;
			String ip;
			while (networkInterfaces.hasMoreElements()) {
				networkInterface = networkInterfaces.nextElement();
				inetAddresses = networkInterface.getInetAddresses();
				while (inetAddresses.hasMoreElements()) {
					inetAddress = inetAddresses.nextElement();
					if (inetAddress != null && inetAddress instanceof Inet4Address) {
						ip = inetAddress.getHostAddress();
						ipList.add(ip);
					}
				}
			}
		} catch (SocketException e) {

		}
		if (!ipList.isEmpty()) {
			Collections.sort(ipList, new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					return o1.compareTo(o2);
				}
			});
		}
		return ipList;
	}
}
