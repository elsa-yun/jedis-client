package com.elsa.redis.util;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

public class JedisClusterDBUtil implements JedisDBInterface {

	private final Log log = LogFactory.getLog(JedisClusterDBUtil.class);

	private static final int MAX_VALUE_SIZE = 1024 * 1024;

	private static final int LOCK_EXPIRE_SECONDS = 5 * 60;

	private JedisCluster jedisCluster;

	private String projectPrefixKey;

	public String getProjectPrefixKey() {
		return projectPrefixKey;
	}

	public void setProjectPrefixKey(String projectPrefixKey) {
		this.projectPrefixKey = projectPrefixKey;
	}

	public JedisCluster getJedisCluster() {
		return jedisCluster;
	}

	public void setJedisCluster(JedisCluster jedisCluster) {
		this.jedisCluster = jedisCluster;
	}

	private String getCompleteKey(String key) {
		if (null != this.projectPrefixKey && !"".equals(this.projectPrefixKey)) {
			return this.projectPrefixKey + ":" + key.trim();
		}
		return key;
	}

	public boolean setDB(String key, Object value) {
		if (null == key || "".equals(key.trim())) {
			return false;
		}
		if (null == value) {
			return false;
		}
		key = this.getCompleteKey(key);
		byte[] serializeValue = SerializeUtil.serialize(value);
		if (serializeValue.length > MAX_VALUE_SIZE) {
			log.error(key + " value is more than 1M ");
			return false;
		}
		try {
			String set = jedisCluster.set(key.getBytes(), serializeValue);
			if (set != null && set.equalsIgnoreCase("ok")) {
				return true;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}

	public boolean setDB(String key, Object value, Integer expireSeconds) {
		if (null == key || "".equals(key.trim())) {
			return false;
		}
		if (null == value) {
			return false;
		}
		key = this.getCompleteKey(key);
		byte[] serializeValue = SerializeUtil.serialize(value);
		if (serializeValue.length > MAX_VALUE_SIZE) {
			log.error(key + " value is more than 1M ");
			return false;
		}
		try {
			String set = null;
			if (null != expireSeconds) {
				set = jedisCluster.set(key.getBytes(), serializeValue, "NX".getBytes(), "EX".getBytes(), expireSeconds);
				if (null == set) {
					set = jedisCluster.set(key.getBytes(), serializeValue, "XX".getBytes(), "EX".getBytes(), expireSeconds);
				}
			}
			if (set != null && set.equalsIgnoreCase("ok")) {
				return true;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}

	public Object getDB(String key) {
		return this.getDB(key, false);
	}

	public Object getDB(String key, boolean readSlave) {
		if (null == key || "".equals(key.trim())) {
			return null;
		}
		key = this.getCompleteKey(key);
		try {
			byte[] bytes = jedisCluster.get(key.getBytes());
			if (null != bytes) {
				return SerializeUtil.unserialize(bytes);
			}
			return null;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	public Object getDBNew(String key, int expireSeconds) {
		return this.getDBNew(key, expireSeconds, false);
	}

	public Object getDBNew(String key, int expireSeconds, boolean readSlave) {
		if (null == key || "".equals(key.trim())) {
			return null;
		}
		key = this.getCompleteKey(key);
		try {
			byte[] bytes = jedisCluster.get(key.getBytes());
			jedisCluster.expire(key.getBytes(), expireSeconds);
			if (null != bytes) {
				return SerializeUtil.unserialize(bytes);
			}
			return null;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	public boolean setDBString(String key, String value) {
		if (null == key || "".equals(key.trim())) {
			return false;
		}
		if (null == value || "".equals(value.trim())) {
			return false;
		}
		if (value.getBytes().length > MAX_VALUE_SIZE) {
			log.error(key + " value is more than 1M ");
			return false;
		}
		key = this.getCompleteKey(key);
		try {
			String isOk = jedisCluster.set(key, value);
			return "ok".equalsIgnoreCase(isOk);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}

	public String getDBString(String key) {
		return this.getDBString(key, false);
	}

	public String getDBString(String key, boolean readSlave) {
		if (null == key || "".equals(key.trim())) {
			return null;
		}
		key = this.getCompleteKey(key);
		try {
			return jedisCluster.get(key);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	public Long ttl(String key) {
		return this.getDBKeyExpire(key);
	}

	public Long getDBKeyExpire(String key) {
		return this.getDBKeyExpire(key, false);
	}

	public Long getDBKeyExpire(String key, boolean readSlave) {
		if (null == key || "".equals(key.trim())) {
			return null;
		}
		key = this.getCompleteKey(key);
		try {
			return jedisCluster.ttl(key.getBytes());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return 0L;
	}

	public boolean deleteDBKey(String key) {
		if (null == key || "".equals(key.trim())) {
			return false;
		}
		key = this.getCompleteKey(key);
		try {
			return jedisCluster.del(key.getBytes()) > 0;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}

	public boolean lock(String key, int expireSeconds) {
		if (null == key || "".equals(key.trim())) {
			return false;
		}
		if (expireSeconds == 0) {
			expireSeconds = LOCK_EXPIRE_SECONDS;
		}
		key = this.getCompleteKey(key);
		try {
			Long setnx = jedisCluster.setnx(key, System.currentTimeMillis() + "");
			jedisCluster.expire(key, expireSeconds);
			if (setnx.intValue() == 1) {
				return true;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}

	public boolean lock(String key) {
		return this.lock(key, LOCK_EXPIRE_SECONDS);
	}

	public boolean unLock(String key) {
		if (null == key || "".equals(key.trim())) {
			return false;
		}
		key = this.getCompleteKey(key);
		try {
			long i = jedisCluster.del(key);
			if (i == 1) {
				return true;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}

	public Long incr(String key) {
		if (null == key || "".equals(key.trim())) {
			return null;
		}
		key = this.getCompleteKey(key);
		try {
			return jedisCluster.incr(key);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	public Long incrBy(String key, int step) {
		if (null == key || "".equals(key.trim())) {
			return null;
		}
		key = this.getCompleteKey(key);
		try {
			return jedisCluster.incrBy(key, step);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	public boolean batchSetDBString(Map<String, String> map, Integer expireSeconds) {
		// cluster不支持事务
		return false;
	}

	@Override
	public boolean setDBString(String key, String value, int expireSeconds) {
		if (null == key || "".equals(key.trim())) {
			return false;
		}
		if (null == value || "".equals(value.trim())) {
			return false;
		}
		if (value.getBytes().length > MAX_VALUE_SIZE) {
			log.error(key + " value is more than 1M ");
			return false;
		}
		key = this.getCompleteKey(key);
		try {
			String isOk = jedisCluster.set(key, value);
			jedisCluster.expire(key, expireSeconds);
			return "ok".equalsIgnoreCase(isOk);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}

	public boolean setDBExpire(String key, int value) {
		if (null == key || "".equals(key.trim())) {
			return false;
		}
		key = this.getCompleteKey(key);
		try {
			Long expire = jedisCluster.expire(key, value);
			return expire.intValue() == 1;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}

}
