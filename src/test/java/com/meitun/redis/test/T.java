package com.elsa.redis.test;

import java.util.HashSet;
import java.util.Set;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

public class T {

	private static final String USER_ID_0001 = "user_id_0001";

	private static final String USER_ID_0002 = "user_id_0002";

	public static void main(String args[]) throws InterruptedException {
		Jedis jds = new Jedis("127.0.0.1", 6381);
		String string = jds.get(USER_ID_0001);
		System.out.println("str:" + string);
		String set = jds.set(USER_ID_0001, "value1");
		if (set.equals("OK")) {
			System.out.println("=========");
			jds.expire(USER_ID_0001, 5);
			Long ttl = jds.ttl(USER_ID_0001);
			System.out.println(ttl);
		}
		Thread.sleep(10000);
		Long ttl = jds.ttl(USER_ID_0001);
		System.out.println("1:" + ttl);

		set = jds.set(USER_ID_0002, "value2");
		ttl = jds.ttl(USER_ID_0002);
		System.out.println("2:" + ttl);
		jds.close();

		Set<HostAndPort> ss = new HashSet<HostAndPort>();
		JedisCluster jc = new JedisCluster(ss);
	}
}
