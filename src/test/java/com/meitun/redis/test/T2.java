package com.elsa.redis.test;

import redis.clients.jedis.Jedis;

public class T2 {

	private static final String AAAA = "aaaa";

	public static void main(String[] args) {

		Jedis j = new Jedis("172.16.1.65", 6379);
		j.auth("redis");
		j.set(AAAA, "vvvvv");
		j.expire(AAAA, 10);
		Long ttl = j.ttl(AAAA);
		System.out.println(ttl);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ttl = j.ttl(AAAA);
		System.out.println(ttl);
		j.expire(AAAA, 20);
		ttl = j.ttl(AAAA);
		System.out.println(ttl);
	}
}
