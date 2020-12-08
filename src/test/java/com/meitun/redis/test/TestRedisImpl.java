package com.elsa.redis.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.elsa.redis.util.JedisCacheUtil;
import com.elsa.redis.util.JedisDBInterface;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:spring.xml" })
public class TestRedisImpl {

	private static final String I = "i";

	@Autowired
	JedisCacheUtil jedisCacheUtil;

	@Autowired
	JedisDBInterface jedisDBUtil;

	@Autowired
	JedisDBInterface jedisClusterDBUtil;

	// @Autowired
	// B b;
	//
	// @Autowired
	// C c;

	@Test
	public void testAll() throws InterruptedException {
		String key = "lhs_lock_001_789";
		// boolean lock = jedisCacheUtil.lock(key, 30);
		// System.out.println(lock);
		// boolean unLock = jedisCacheUtil.unLock(key);
		// System.out.println(unLock);
		//
		// PersonDO p = new PersonDO();
		// p.setName("name1");
		// p.setCode("code1");

		//boolean setDBString = jedisDBUtil.setDBString(key, "0");
		//System.out.println(setDBString);
		// jedisDBUtil.setDBExpire(key,60);
		//Long ttl = jedisDBUtil.ttl(key);
		//System.out.println(ttl);

		key = "db_key_0001_0";
		// boolean setDB = jedisDBUtil.setDB(key, p, 60);
		// System.out.println(setDB);
		// PersonDO dbNew = (PersonDO) jedisDBUtil.getDBNew(key, 100);
		// Thread.sleep(2000);
		// Long ttl = jedisDBUtil.ttl(key);
		// System.out.println(ttl);
		// System.out.println(dbNew);

		key = "db_key_0001_010";
		// jedisCacheUtil.setCache(key, p, 60);
		// PersonDO cache = (PersonDO) jedisCacheUtil.getCache(key);
		// System.out.println(cache);

		// key = "db_key_0001_010_str";
		// jedisCacheUtil.setCacheString(key, "lllllll", 60);
		// String cacheString = jedisCacheUtil.getCacheString(key);
		// System.out.println(cacheString);

		// PersonDO db = (PersonDO) jedisClusterDBUtil.getDB(key);
		// System.out.println(db);
		// Long ttl = jedisClusterDBUtil.ttl(key);
		// System.out.println(ttl);
		//
		// jedisClusterDBUtil.setDBString("longhaisheng_0001",
		// "hello world",60);
		// Thread.sleep(5000);
		// long t = jedisClusterDBUtil.ttl("longhaisheng_0001");
		// System.out.println(t);
		// String key = "db_key_0001";
		// PersonDO db = (PersonDO) jedisDBUtil.getDB(key);
		// boolean setDB = false;
		// if (null == db) {
		// System.out.println("null");
		// setDB = jedisDBUtil.setDB(key, p, 60);
		// System.out.println("1:"+setDB);
		// }
		// System.out.println(System.currentTimeMillis());
		// Thread.sleep(5000);
		// db = (PersonDO) jedisDBUtil.getDB(key);
		// System.out.println("1 get=>"+db);
		// p.setCode("new code");
		// setDB = jedisDBUtil.setDB(key, p, 60);
		// System.out.println(System.currentTimeMillis());
		// System.out.println("2:"+setDB);
		// db = (PersonDO) jedisDBUtil.getDB(key);
		// System.out.println(db);
		// boolean deleteDBKey = jedisDBUtil.deleteDBKey(key);
		// System.out.println(deleteDBKey);

		key = "key_lhs";
		int maxNum = 6;
		int expireSeconds = 30;
		for (int i = 0; i < 20; i++) {
			boolean watchMethodCall = jedisCacheUtil.watchMethodCall(key, maxNum, expireSeconds);
			System.out.println(watchMethodCall);
			Thread.sleep(1000);
			// jedisCacheUtil.setCacheString(key + i, key + i);
		}

		// for (int i = 0; i < 30; i++) {
		// ExecutorService es = Executors.newFixedThreadPool(10);
		// for (int i = 0; i < 10; i++) {
		// es.execute(new Runnable() {
		// public void run() {
		// for (int j = 0; j < 1000; j++) {
		// if (j % 200 == 0) {
		// Thread.sleep(1010);
		// }
		// boolean watchMethodCall =
		// jedisCacheUtil.watchMethodCall("lhs_watch_key", 20, 1);
		// System.out.println(watchMethodCall);
		// }
		// }
		// });
		// }

		// es.shutdown();

		// jedisCacheUtil.setCacheString("cache_key" + i, "中国国加另中dfdf", 3);
		// }
		// for (int i = 0; i < 30; i++) {
		// System.out.println(jedisCacheUtil.getCacheString("cache_key" + i));
		// }

		// Map<String, JedisShardInfo> shardInfoList =
		// ApplicationContextBeans.getBeansOfType(redis.clients.jedis.JedisShardInfo.class);
		// for (Map.Entry<String, JedisShardInfo> entry :
		// shardInfoList.entrySet()) {
		// String host = entry.getValue().getHost();
		// int port = entry.getValue().getPort();
		// Jedis jedis = null;
		// try {
		// jedis = new Jedis(host, port);
		// String isOk = jedis.set("listen_key_" + host + port, "1");
		// if (isOk.equalsIgnoreCase("OK")) {
		// System.out.println("=============" + isOk);
		// }
		// } catch (Exception e) {
		//
		// } finally {
		// if (null != jedis) {
		// try {
		// jedis.disconnect();
		// jedis.close();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }
		// }
		// }

		// ExtendShardedJedisPool onePool = null;
		// Map<String, ExtendShardedJedisPool> pools = ApplicationContextBeans
		// .getBeansOfType(com.elsa.redis.extend.ExtendShardedJedisPool.class);
		// for (Map.Entry<String, ExtendShardedJedisPool> entry :
		// pools.entrySet()) {
		// onePool = entry.getValue();
		// List<JedisShardInfo> jedisShardInfoList =
		// onePool.getJedisShardInfos();
		// for (Iterator<JedisShardInfo> iterator =
		// jedisShardInfoList.iterator(); iterator.hasNext();) {
		// JedisShardInfo shardInfo = (JedisShardInfo) iterator.next();
		// if (shardInfo.getHost().equals("127.0.0.1") && shardInfo.getPort() ==
		// 6382) {
		// iterator.remove();
		// }
		// }
		// for (Iterator<JedisShardInfo> iterator =
		// jedisShardInfoList.iterator(); iterator.hasNext();) {
		// JedisShardInfo jedisShardInfo = (JedisShardInfo) iterator.next();
		// System.out.println(jedisShardInfo.getHost());
		// System.out.println(jedisShardInfo.getPort());
		// }
		// onePool.setJedisShardInfos(jedisShardInfoList);
		// }

		// System.out.println("===================start  60000=>" +
		// System.currentTimeMillis() / 1000);
		// Thread.sleep(60000);
		// System.out.println("===================end =>" +
		// System.currentTimeMillis() / 1000);
		// for (int i = 0; i < 10; i++) {
		// jedisCacheUtil.setCacheString("cache_key" + i, "中国国加另中dfdf", 3);
		// }
		// Thread.sleep(1000);
		// for (int i = 0; i < 10; i++) {
		// System.out.println(jedisCacheUtil.getCacheString("cache_key" + i));
		// }

		// ClassPathXmlApplicationContext context = new
		// ClassPathXmlApplicationContext("spring/spring-db-redis.xml");
		// DefaultListableBeanFactory defaultListableBeanFactory =
		// (DefaultListableBeanFactory) context.getBeanFactory();
		// Map<String, JedisPool> jm =
		// defaultListableBeanFactory.getBeansOfType(redis.clients.jedis.JedisPool.class);
		// for (Map.Entry<String, JedisPool> entry : jm.entrySet()) {
		// System.out.println(entry.getKey());
		// JedisPool value = entry.getValue();
		// }

		// //将applicationContext转换为ConfigurableApplicationContext
		// ConfigurableApplicationContext configurableApplicationContext =
		// (ConfigurableApplicationContext) context;
		//
		// // 获取bean工厂并转换为DefaultListableBeanFactory
		// defaultListableBeanFactory = (DefaultListableBeanFactory)
		// configurableApplicationContext
		// .getBeanFactory();
		//
		// // 通过BeanDefinitionBuilder创建bean定义
		// BeanDefinitionBuilder beanDefinitionBuilder =
		// BeanDefinitionBuilder.genericBeanDefinition(UserService.class);
		// // 设置属性userAcctDAO,此属性引用已经定义的bean:userAcctDAO
		// beanDefinitionBuilder.addPropertyReference("userAcctDAO",
		// "UserAcctDAO");
		//
		// // 注册bean
		// defaultListableBeanFactory.registerBeanDefinition("sdfds",
		// beanDefinitionBuilder.getRawBeanDefinition());
		//
		// defaultListableBeanFactory.registerBeanDefinition(beanName,
		// beanDefinition);

		// BeanDefinition beanDefinition =
		// defaultListableBeanFactory.getBeanDefinition(beanName);
		// beanDefinition.setBeanClassName(beanClassName);
		// defaultListableBeanFactory.registerBeanDefinition(beanName,
		// beanDefinition);

		// List<String> list = new ArrayList<String>();
		// list.add("str9");
		// list.add("str8");
		// list.add("str7");
		// list.add("str6");
		//
		// DBJedisList<String> strList = new
		// DBJedisList<String>("test-user-000002");
		// int count = strList.watchAddAll(list);
		// strList.add("aaaaa");
		// for (String str : strList.getList()) {
		// System.out.println(str);
		// }
		// System.out.println("count=>" + count);

		// String key = "key";
		// jedisCacheUtil.setCacheString(key, "value", 1);
		// System.out.println(jedisCacheUtil.getCacheString(key));
		// Thread.currentThread().sleep(5000);
		// Long keyExpire = jedisCacheUtil.getKeyExpire(key);
		// System.out.println("keyExpire=>" + keyExpire);
		// boolean lock = jedisCacheUtil.lock(key);
		// if (lock) {
		// System.out.println("lock");
		// } else {
		// System.out.println("unlock");
		// }
		// Thread.currentThread().sleep(5000);
		// lock = jedisCacheUtil.lock(key);
		// if (lock) {
		// System.out.println(";;;lock");
		// } else {
		// System.out.println(";;;unlock");
		// }
		// jedisCacheUtil.unLock(key);

		// jedisCacheUtil.setCacheString("db1", "value1");
		// System.out.println(jedisCacheUtil.getCacheString("db1"));

		// JedisPool jds = new JedisPool("172.16.1.62", 6379);

		// String key2 = "cache_key2";
		// jedisCacheUtil.setCacheString(key2, "cache_value2", 10000);
		//
		// // System.out.println(jedisCacheUtil.getCacheString(key2));
		// System.out.println("===="+jedisCacheUtil.getKeyExpire(key2));
		//
		// jedisDBUtil.setDB("name1", "value1");
		// System.out.println(jedisDBUtil.getDB("name1"));
		//
		// jedisDBUtil.setDBString("name2", "value2");
		// System.out.println(jedisDBUtil.getDBString("name2"));

		// JedisPool jds = new JedisPool("172.16.1.62", 6379);

		// int user_id = 12;
		// DBJedisList<PersonDO> redis = new DBJedisList<PersonDO>("uid_" +
		// user_id, user_id);
		// redis.clear();
		//
		// PersonDO p = new PersonDO(1, "user_001");
		// p.setId(1);
		// p.setName("name");
		//
		// redis.add(p);
		// PersonDO p1 = redis.get(0);
		// System.out.println(p1);
		// System.out.println(p1.equals(p));
		//
		// // System.out.println(redis.remove(p1));
		// p.setName("name1");
		// System.out.println(redis.set(0, p));
		//
		// PersonDO p2 = redis.get(0);
		// System.out.println(p2);
		// System.out.println("==============");
		//
		// PersonDO p3 = p2;
		// p3.setName("name_3");
		// redis.add(p3);
		//
		// for (PersonDO person : redis.getList()) {
		// System.out.println(":" + person.getName());
		// }
		//
		// System.out.println(redis.remove(3));
		//
		// PersonDO p4 = new PersonDO();
		// p4.setId(4);
		// p4.setName("name4");
		// System.out.println(redis.remove(p4));
		//
		// System.out.println(redis.size());
		// System.out.println(redis.get(5));
		//
		// List<PersonDO> new_list = new ArrayList<PersonDO>();
		// new_list.add(p4);
		// int i = redis.addAll(new_list);
		// System.out.println("==>" + i);

		//
		// for (PersonDO person : redis.getList()) {
		// System.out.println(person.getName());
		// }

		// System.out.println(redis.getList().toArray());

		// long i=redis.add(p);
		// System.out.println(i);
		// System.out.println(redis.get(0));

		// // redis.close();
		//
		// List<String> list = redis.getList();
		// for (String string : list) {
		// System.out.println(string);
		// }
		// redis.remove(0);
		// System.out.println("===== remove 0 ======");
		// for (String string : redis.getList()) {
		// System.out.println(string);
		// }
		//
		// redis.clear();
		// redis.set(16, "aa");
		// System.out.println("===== clear all =====");
		// for (String string : redis.getList()) {
		// System.out.println(string);
		// }
		// List<String> carts = new ArrayList<String>();
		// for (int i = 0; i < 10; i++) {
		// carts.add("test" + i);
		// }
		//
		// for (String string : carts) {
		// System.out.println(string);
		// }

		// Jedis jedis = redis.getJedis();
		// jedis.set("person:100".getBytes(), SerializeUtil.serialize(new
		// PersonDO(100, "zhangsan")));
		// jedis.set("person:101".getBytes(), SerializeUtil.serialize(new
		// PersonDO(101, "bruce")));
		//
		// byte[] data100 = jedis.get(("person:100").getBytes());
		// PersonDO person100 = (PersonDO) SerializeUtil.unserialize(data100);
		// System.out.println(String.format("person:100->id=%s,name=%s",
		// person100.getId(), person100.getName()));
		//
		// byte[] data101 = jedis.get(("person:101").getBytes());
		// PersonDO person101 = (PersonDO) SerializeUtil.unserialize(data101);
		// System.out.println(String.format("person:101->id=%s,name=%s",
		// person101.getId(), person101.getName()));

	}

	@Test
	public void test2Trans() {
		// List<String> list = new ArrayList<String>();
		// list.add("a");
		// list.add("b");
		// list.add("c");
		// list.add("d");
		// for (String str : list) {
		// System.out.println(str);
		// }

		// Jedis jedis = new Jedis("172.16.200.3", 6379);
		// try {
		// jedis.connect();
		// jedis.auth("redis");
		// long start = System.currentTimeMillis();
		// Transaction tx = jedis.multi();
		// for (int i = 0; i < 10; i++) {
		// String string = "t" + i;
		// tx.lpush(string.getBytes(), string.getBytes());
		// // tx.set("t" + i, "t" + i);
		// }
		// List<Object> results = tx.exec();
		// long end = System.currentTimeMillis();
		// System.out.println("Transaction SET: " + ((end - start) / 1000.0) +
		// " seconds");
		// } catch (Exception e) {
		// e.printStackTrace();
		// } finally {
		// System.out.println("close()");
		// jedis.disconnect();
		// jedis.close();
		// }

	}
}

class WatchRun implements Runnable {

	@Override
	public void run() {

	}

}
