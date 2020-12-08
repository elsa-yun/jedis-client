//package com.elsa.redis.test;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//import com.elsa.redis.util.JedisCacheUtil;
//import com.elsa.redis.util.JedisDBUtil;
//
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = { "classpath*:spring.xml" })
//public class TestDBMethod {
//
//	@Autowired
//	JedisCacheUtil jedisCacheUtil;
//
//	@Autowired
//	JedisDBUtil jedisDBUtil;
//
//	@Test
//	public void testAll() throws InterruptedException {
//		// DBJedisList<String> strList = new
//		// DBJedisList<String>("test-user-000001", 1);
//		// for (int i = 0; i < 100; i++) {
//		// boolean j = strList.watchMethodCall(50, 60);
//		// System.out.println("j===>" + j);
//		// // Thread.sleep(1000);
//		// }
//
//		// for (int i = 0; i < 100; i++) {
//		// boolean j = jedisCacheUtil.watchMethodCall("user_id_0001", 30, 60);
//		// System.out.println("j===>" + j);
//		// // Thread.sleep(1000);
//		// }
//
//		// PersonDO p=new PersonDO();
//		// p.setCode("code");
//		// p.setName("name22");
//		// jedisDBUtil.setDB("user_0000000123456789", p,300);
//		// System.out.println(jedisDBUtil.getDB("user_0000000123456789"));
//		// Thread.sleep(2000);
//		// System.out.println(jedisDBUtil.getDBKeyExpire("user_0000000123456789"));
//
//		Map<String, String> map = new HashMap<String, String>();
//		for (int i = 0; i < 10; i++) {
//			String str = "longhaisheng" + i;
//			map.put(str, str);
//		}
//
//		jedisDBUtil.batchSetDBString(map, 60 * 5);
//		for (int i = 0; i < 10; i++) {
//			String s = jedisDBUtil.getDBString("longhaisheng" + i);
//			System.out.println(s);
//		}
//
//	}
//}
