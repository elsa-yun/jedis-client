//package com.elsa.redis.test;
//
//import java.util.UUID;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//import redis.clients.jedis.Jedis;
//
//import com.elsa.redis.util.JedisCacheUtil;
//import com.elsa.redis.util.JedisDBUtil;
//
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = { "classpath*:spring.xml" })
//public class TestRedisRun {
//
//	private static final Log logger = LogFactory.getLog(TestRedisRun.class);
//
//	@Autowired
//	JedisCacheUtil jedisCacheUtil;
//
//	@Autowired
//	JedisDBUtil jedisDBUtil;
//
//	public static void main(String args[]) {
//		Jedis j = new Jedis("172.16.1.65", 6379, 2000);
//		j.auth("redis");
//
//		for (int i = 0; i < 0; i++) {
//			String set = j.set("lhs.cache.key." + i, UUID.randomUUID().toString());
//			String get = j.get("lhs.cache.key." + i);
//			System.out.println(get);
//		}
//
//		j.quit();
//		j.disconnect();
//		j.close();
//
//	}
//
//	@Test
//	public void testAll() throws InterruptedException {
//
//		// for (int i = 0; i < 1; i++) {
//		// jedisDBUtil.setDBString("cache_key" + i, "中国国加另中dfdf");
//		// }
//		// Thread.sleep(1000);
//		// for (int i = 0; i < 1; i++) {
//		// logger.info(jedisDBUtil.getDBString("cache_key" + i));
//		// }
//
//		// PersonDO p=new PersonDO();
//		// p.setCode("code");
//		// p.setName("name");
//		// String key="cache_key";
//		// jedisDBUtil.setDB(key, p,10);
//		// Thread.sleep(5000);
//		// Long dbKeyExpire = jedisDBUtil.getDBKeyExpire(key);
//		// System.out.println(dbKeyExpire);
//		// jedisDBUtil.getDBNew(key, 10);
//		// dbKeyExpire = jedisDBUtil.getDBKeyExpire(key);
//		// System.out.println(dbKeyExpire);
//
//		Jedis j = new Jedis("172.16.1.65", 6379, 2000);
//		j.auth("redis");
//
//		for (int i = 0; i < 0; i++) {
//			String set = j.set("lhs.cache.key." + i, UUID.randomUUID().toString());
//			String get = j.get("lhs.cache.key." + i);
//			System.out.println(get);
//		}
//
//		j.quit();
//		j.disconnect();
//		j.close();
//
//		// logger.info("===================start 15000=>" +
//		// System.currentTimeMillis() / 1000);
//		// Thread.sleep(15000);
//		// logger.info("===================end =>" + System.currentTimeMillis()
//		// / 1000);
//		// for (int i = 0; i < 10; i++) {
//		// jedisCacheUtil.setCacheString("cache_key" + i, "中国国加另中dfdf");
//		// }
//		// Thread.sleep(10000);
//		// for (int i = 0; i < 10; i++) {
//		// logger.info(jedisCacheUtil.getCacheString("cache_key" + i));
//		// }
//		//
//		//
//		// logger.info("===================start  20000=>" +
//		// System.currentTimeMillis() / 1000);
//		// Thread.sleep(20000);
//		// logger.info("===================end =>" + System.currentTimeMillis()
//		// / 1000);
//		// for (int i = 0; i < 10; i++) {
//		// jedisCacheUtil.setCacheString("cache_key" + i, "中国国加另中dfdf", 3);
//		// }
//		// Thread.sleep(1000);
//		// for (int i = 0; i < 10; i++) {
//		// logger.info(jedisCacheUtil.getCacheString("cache_key" + i));
//		// }
//
//	}
//
//}
