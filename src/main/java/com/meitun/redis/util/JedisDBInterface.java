package com.elsa.redis.util;

import java.util.Map;

public interface JedisDBInterface {

	/**
	 * 将序列化对象存储到 redis db中
	 *
	 * @param key
	 * @param value
	 * @return true：成功 false：失败
	 */
	boolean setDB(String key, Object value);

	/**
	 * 将序列化对象存储到 redis db中
	 *
	 * @param key
	 * @param value
	 * @param expireSeconds
	 *            单位：秒 实现 java序列化的对象
	 * @return true：成功 false：失败
	 */
	boolean setDB(String key, Object value, Integer expireSeconds);

	/**
	 * 从 从库 redis db中读取 key的值
	 *
	 * @param key
	 * @return 反序列化后的 Object
	 */
	Object getDB(String key);

	/**
	 * 从 redis db中读取 key的值
	 *
	 * @param key
	 * @param readSlave
	 *            true为读从库，false读主库
	 * @return 反序列化后的 Object
	 */
	Object getDB(String key, boolean readSlave);

	/**
	 * 从 redis db中读取 key的值
	 *
	 * @param key
	 * @param expireSeconds
	 *            过期时间,单位秒
	 * @param readSlave
	 *            true为读从库，false读主库
	 * @return 反序列化后的 Object
	 */
	Object getDBNew(String key, int expireSeconds, boolean readSlave);

	/**
	 * 从 redis db中读取 key的值
	 *
	 * @param key
	 * @param expireSeconds
	 *            过期时间,单位秒
	 * @return 反序列化后的 Object
	 */
	Object getDBNew(String key, int expireSeconds);

	/**
	 * 将 字符串 存储到 redis db中
	 *
	 * @param key
	 * @param value
	 * @return true：成功 false：失败
	 */
	boolean setDBString(String key, String value);
	
	boolean setDBExpire(String key, int value);
	
	/**
	 * 将 字符串 存储到 redis db中
	 *
	 * @param key
	 * @param value
	 * @param expireSeconds 有效时间，单位(秒)
	 * @return true：成功 false：失败
	 */
	boolean setDBString(String key, String value,int expireSeconds);

	/**
	 * 从 从库 redis db中读取 key的值
	 *
	 * @param key
	 * @return String
	 */
	String getDBString(String key);

	/**
	 * 从 redis db中读取 key的值
	 *
	 * @param key
	 * @param readSlave
	 *            true为读从库，false读主库
	 * @return String
	 */
	String getDBString(String key, boolean readSlave);

	/**
	 * 获取key的有效期时间 ：当key 不存在时，返回-2 当key 存在但没有设置剩余生存时间时，返回-1 。 否则，以秒为单位，返回key
	 * 的剩余生存时间。
	 *
	 * @param key
	 *            适用于setDB方法保存的key
	 * @param expireSeconds
	 * @return
	 */
	Long getDBKeyExpire(String key, boolean readSlave);

	/**
	 * 获取key的有效期时间 ：当key 不存在时，返回-2 当key 存在但没有设置剩余生存时间时，返回-1 。 否则，以秒为单位，返回key
	 * 的剩余生存时间。
	 *
	 * @param key
	 *         适用于setDB方法保存的key
	 * @param expireSeconds
	 * @return
	 */
	Long ttl(String key);
	
	/**
	 * 获取key的有效期时间 ：当key 不存在时，返回-2 当key 存在但没有设置剩余生存时间时，返回-1 。 否则，以秒为单位，返回key
	 * 的剩余生存时间。
	 *
	 * @param key
	 *            适用于setDB方法保存的key
	 * @return
	 */
	Long getDBKeyExpire(String key);

	/**
	 * 删除key 适用于setDB方法保存的key
	 *
	 * @param key
	 *
	 * @return
	 */
	boolean deleteDBKey(String key);

	/**
	 * 取得有效时间内的锁
	 *
	 * @param key
	 * @param expireSeconds
	 *            有效时间 单位 秒
	 * @return
	 */
	boolean lock(String key, int expireSeconds);

	/**
	 * 获取锁,默认锁定五分钟
	 *
	 * @param key
	 * @return
	 */
	boolean lock(String key);

	/**
	 * 释放锁
	 *
	 * @param key
	 * @return
	 */
	boolean unLock(String key);

	/**
	 * 自增加1
	 *
	 * @param key
	 * @return
	 */
	Long incr(String key);

	/**
	 * 自增加 步长step
	 *
	 * @param key
	 * @param step
	 *            步长
	 * @return
	 */
	Long incrBy(String key, int step);

	/**
	 * 此方法包装批量setDBString方法,单取时，可以使用getDBString方法取出该方法存取的单条数据
	 * 
	 * @param map
	 *            key为key,value为value
	 * @return
	 */
	boolean batchSetDBString(Map<String, String> map, Integer expireSeconds);

}