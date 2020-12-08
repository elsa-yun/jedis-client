package com.elsa.redis.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Transaction;

import com.elsa.redis.DBShardJedisPool;

/**
 *
 * @author longhaisheng
 *
 */
public class JedisDBUtil implements JedisDBInterface {

	private final Log log = LogFactory.getLog(JedisDBUtil.class);

	private static final int MAX_VALUE_SIZE = 1024 * 1024;

	private static final int LOCK_EXPIRE_SECONDS = 5 * 60;

	/** 主库线程池 */
	private DBShardJedisPool dbMasterShardJedisPool;

	/** 从库线程池 */
	private DBShardJedisPool dbSlaveShardJedisPool;

	/** 项目应用名(key前缀) */
	private String projectPrefixKey;

	private String getCompleteKey(String key) {
		if (null != this.projectPrefixKey && !"".equals(this.projectPrefixKey)) {
			return this.projectPrefixKey + ":" + key.trim();
		}
		return key;
	}

	public void setProjectPrefixKey(String projectPrefixKey) {
		this.projectPrefixKey = projectPrefixKey;
	}

	private DBShardJedisPool getDbSlaveShardJedisPool() {
		return dbSlaveShardJedisPool;
	}

	public void setDbSlaveJedisPool(DBShardJedisPool dbSlaveJedisPool) {
		this.dbSlaveShardJedisPool = dbSlaveJedisPool;
	}

	private DBShardJedisPool getDbMasterShardJedisPool() {
		return dbMasterShardJedisPool;
	}

	public void setDbMasterShardJedisPool(DBShardJedisPool dbMasterJedisPool) {
		this.dbMasterShardJedisPool = dbMasterJedisPool;
	}

	private JedisPool getMasterJedisPool(String key) {
		key = this.getCompleteKey(key);
		int intKey = strToInt(key);
		return this.getDbMasterShardJedisPool().getJedisPool(intKey);
	}

	private String getJedisStrategyKey(String key) {
		key = this.getCompleteKey(key);
		int intKey = strToInt(key);
		return this.getDbMasterShardJedisPool().getStrategy().getJedisPoolKey(intKey);
	}

	private JedisPool getMasterJedisPoolWithStrategyKey(String strategyKey) {
		return this.getDbMasterShardJedisPool().getJedisPool(strategyKey);
	}

	private JedisPool getSlaveJedisPool(String key) {// 先从 从库池拿，没有再从主库池中拿
		key = this.getCompleteKey(key);
		int intKey = strToInt(key);
		if (null != this.getDbSlaveShardJedisPool()) {
			return this.getDbSlaveShardJedisPool().getJedisPool(intKey);
		}
		return this.getDbMasterShardJedisPool().getJedisPool(intKey);
	}

	public static int strToInt(String key) {
		if (null != key && !"".equals(key)) {
			int num = 0;
			for (int i = 0; i < key.length(); i++) {
				num += key.charAt(i);
			}
			return num;
		}
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.elsa.redis.util.JedisDBInterface#setDB(java.lang.String,
	 * java.lang.Object)
	 */
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
		Jedis jds = null;
		JedisPool masterJedisPool = getMasterJedisPool(key);
		boolean isSuccess = true;
		try {
			jds = masterJedisPool.getResource();
			String set = jds.set(key.getBytes(), serializeValue);
			if ("ok".equalsIgnoreCase(set)) {
				return true;
			}
		} catch (Exception e) {
			isSuccess = false;
			log.error(e.getMessage(), e);
		} finally {
			if (null != masterJedisPool) {
				if (isSuccess) {
					masterJedisPool.returnResource(jds);
				} else {
					masterJedisPool.returnBrokenResource(jds);
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.elsa.redis.util.JedisDBInterface#setDB(java.lang.String,
	 * java.lang.Object, java.lang.Integer)
	 */
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
		Jedis jds = null;
		JedisPool masterJedisPool = getMasterJedisPool(key);
		boolean isSuccess = true;
		try {
			jds = masterJedisPool.getResource();
			if (null != expireSeconds) {
				Transaction trans = jds.multi();
				trans.set(key.getBytes(), serializeValue);
				trans.expire(key.getBytes(), expireSeconds);
				List<Object> results = trans.exec();
				if ("ok".equalsIgnoreCase(results.get(0).toString()) && Integer.valueOf(results.get(1).toString()) == 1) {
					return true;
				}
			} else {
				String set = jds.set(key.getBytes(), serializeValue);
				if ("ok".equalsIgnoreCase(set)) {
					return true;
				}
			}
		} catch (Exception e) {
			isSuccess = false;
			log.error(e.getMessage(), e);
		} finally {
			if (null != masterJedisPool) {
				if (isSuccess) {
					masterJedisPool.returnResource(jds);
				} else {
					masterJedisPool.returnBrokenResource(jds);
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.elsa.redis.util.JedisDBInterface#getDB(java.lang.String)
	 */
	public Object getDB(String key) {
		return this.getDB(key, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.elsa.redis.util.JedisDBInterface#getDB(java.lang.String,
	 * boolean)
	 */
	public Object getDB(String key, boolean readSlave) {
		if (null == key || "".equals(key.trim())) {
			return null;
		}
		key = this.getCompleteKey(key);
		Jedis jds = null;
		JedisPool jedisPool = null;
		boolean isSuccess = true;
		try {
			if (readSlave) {
				jedisPool = getSlaveJedisPool(key);
			} else {
				jedisPool = getMasterJedisPool(key);
			}
			jds = jedisPool.getResource();
			byte[] bytes = jds.get(key.getBytes());
			if (null != bytes) {
				return SerializeUtil.unserialize(bytes);
			}
			return null;
		} catch (Exception e) {
			isSuccess = false;
			log.error(e.getMessage(), e);
		} finally {
			if (null != jedisPool) {
				if (isSuccess) {
					jedisPool.returnResource(jds);
				} else {
					jedisPool.returnBrokenResource(jds);
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.elsa.redis.util.JedisDBInterface#getDBNew(java.lang.String,
	 * int, boolean)
	 */
	public Object getDBNew(String key, int expireSeconds, boolean readSlave) {
		if (null == key || "".equals(key.trim())) {
			return null;
		}
		key = this.getCompleteKey(key);
		Jedis jds = null;
		JedisPool jedisPool = null;
		boolean isSuccess = true;
		try {
			if (readSlave) {
				jedisPool = getSlaveJedisPool(key);
			} else {
				jedisPool = getMasterJedisPool(key);
			}
			jds = jedisPool.getResource();
			Pipeline pipelined = jds.pipelined();
			pipelined.get(key.getBytes());
			pipelined.expire(key.getBytes(), expireSeconds);
			List<Object> syncAndReturnAll = pipelined.syncAndReturnAll();
			if (!syncAndReturnAll.isEmpty() && null != syncAndReturnAll.get(0)) {
				byte[] bytes = (byte[]) syncAndReturnAll.get(0);
				return SerializeUtil.unserialize(bytes);
			}
			return null;
		} catch (Exception e) {
			isSuccess = false;
			log.error(e.getMessage(), e);
		} finally {
			if (null != jedisPool) {
				if (isSuccess) {
					jedisPool.returnResource(jds);
				} else {
					jedisPool.returnBrokenResource(jds);
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.elsa.redis.util.JedisDBInterface#getDBNew(java.lang.String,
	 * int)
	 */
	public Object getDBNew(String key, int expireSeconds) {
		return this.getDBNew(key, expireSeconds, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.elsa.redis.util.JedisDBInterface#setDBString(java.lang.String,
	 * java.lang.String)
	 */
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
		Jedis jds = null;
		boolean isSuccess = true;
		JedisPool masterJedisPool = getMasterJedisPool(key);
		try {
			jds = masterJedisPool.getResource();
			String isOk = jds.set(key, value);
			return "ok".equalsIgnoreCase(isOk);
		} catch (Exception e) {
			isSuccess = false;
			log.error(e.getMessage(), e);
		} finally {
			if (null != masterJedisPool) {
				if (isSuccess) {
					masterJedisPool.returnResource(jds);
				} else {
					masterJedisPool.returnBrokenResource(jds);
				}
			}
		}
		return false;
	}

	public boolean setDBExpire(String key, int value) {
		if (null == key || "".equals(key.trim())) {
			return false;
		}
		key = this.getCompleteKey(key);
		Jedis jds = null;
		boolean isSuccess = true;
		JedisPool masterJedisPool = getMasterJedisPool(key);
		try {
			jds = masterJedisPool.getResource();
			Long expire = jds.expire(key, value);
			return expire.intValue() == 1;
		} catch (Exception e) {
			isSuccess = false;
			log.error(e.getMessage(), e);
		} finally {
			if (null != masterJedisPool) {
				if (isSuccess) {
					masterJedisPool.returnResource(jds);
				} else {
					masterJedisPool.returnBrokenResource(jds);
				}
			}
		}
		return false;
	}

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
		Jedis jds = null;
		boolean isSuccess = true;
		JedisPool masterJedisPool = getMasterJedisPool(key);
		try {
			jds = masterJedisPool.getResource();
			Pipeline pipelined = jds.pipelined();
			pipelined.set(key, value);
			pipelined.expire(key, expireSeconds);
			List<Object> syncAndReturnAll = pipelined.syncAndReturnAll();
			if (!syncAndReturnAll.isEmpty() && null != syncAndReturnAll.get(0)) {
				return "ok".equalsIgnoreCase(syncAndReturnAll.get(0).toString());
			}
		} catch (Exception e) {
			isSuccess = false;
			log.error(e.getMessage(), e);
		} finally {
			if (null != masterJedisPool) {
				if (isSuccess) {
					masterJedisPool.returnResource(jds);
				} else {
					masterJedisPool.returnBrokenResource(jds);
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.elsa.redis.util.JedisDBInterface#getDBString(java.lang.String)
	 */
	public String getDBString(String key) {
		return this.getDBString(key, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.elsa.redis.util.JedisDBInterface#getDBString(java.lang.String,
	 * boolean)
	 */
	public String getDBString(String key, boolean readSlave) {
		if (null == key || "".equals(key.trim())) {
			return null;
		}
		key = this.getCompleteKey(key);
		Jedis jds = null;
		JedisPool jedisPool = null;
		boolean isSuccess = true;
		try {
			if (readSlave) {
				jedisPool = getSlaveJedisPool(key);
			} else {
				jedisPool = getMasterJedisPool(key);
			}
			jds = jedisPool.getResource();
			return jds.get(key);
		} catch (Exception e) {
			isSuccess = false;
			log.error(e.getMessage(), e);
		} finally {
			if (null != jedisPool) {
				if (isSuccess) {
					jedisPool.returnResource(jds);
				} else {
					jedisPool.returnBrokenResource(jds);
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.elsa.redis.util.JedisDBInterface#getDBKeyExpire(java.lang.String,
	 * boolean)
	 */
	public Long getDBKeyExpire(String key, boolean readSlave) {
		if (null == key || "".equals(key.trim())) {
			return null;
		}
		key = this.getCompleteKey(key);
		Jedis jds = null;
		JedisPool jedisPool = null;
		boolean isSuccess = true;
		try {
			if (readSlave) {
				jedisPool = getSlaveJedisPool(key);
			} else {
				jedisPool = getMasterJedisPool(key);
			}
			jds = jedisPool.getResource();
			return jds.ttl(key.getBytes());
		} catch (Exception e) {
			isSuccess = false;
			log.error(e.getMessage(), e);
		} finally {
			if (null != jedisPool) {
				if (isSuccess) {
					jedisPool.returnResource(jds);
				} else {
					jedisPool.returnBrokenResource(jds);
				}
			}
		}
		return 0L;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.elsa.redis.util.JedisDBInterface#getDBKeyExpire(java.lang.String)
	 */
	public Long getDBKeyExpire(String key) {
		return getDBKeyExpire(key, true);
	}

	public Long ttl(String key) {
		return getDBKeyExpire(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.elsa.redis.util.JedisDBInterface#deleteDBKey(java.lang.String)
	 */
	public boolean deleteDBKey(String key) {
		if (null == key || "".equals(key.trim())) {
			return false;
		}
		key = this.getCompleteKey(key);
		Jedis jds = null;
		JedisPool jedisPool = null;
		boolean isSuccess = true;
		try {
			jedisPool = getMasterJedisPool(key);
			jds = jedisPool.getResource();
			return jds.del(key.getBytes()) > 0;
		} catch (Exception e) {
			isSuccess = false;
			log.error(e.getMessage(), e);
		} finally {
			if (null != jedisPool) {
				if (isSuccess) {
					jedisPool.returnResource(jds);
				} else {
					jedisPool.returnBrokenResource(jds);
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.elsa.redis.util.JedisDBInterface#lock(java.lang.String, int)
	 */
	public boolean lock(String key, int expireSeconds) {
		if (null == key || "".equals(key.trim())) {
			return false;
		}
		if (expireSeconds == 0) {
			expireSeconds = LOCK_EXPIRE_SECONDS;
		}
		key = this.getCompleteKey(key);
		Jedis jds = null;
		JedisPool jedisPool = null;
		boolean isSuccess = true;
		try {
			jedisPool = getMasterJedisPool(key);
			jds = jedisPool.getResource();
			Transaction trans = jds.multi();
			trans.setnx(key, System.currentTimeMillis() + "");
			trans.expire(key, expireSeconds);
			List<Object> results = trans.exec();
			if ("1".equals(results.get(0).toString()) && Integer.valueOf(results.get(1).toString()) == 1) {
				return true;
			}
		} catch (Exception e) {
			isSuccess = false;
			log.error(e.getMessage(), e);
		} finally {
			if (null != jedisPool) {
				if (isSuccess) {
					jedisPool.returnResource(jds);
				} else {
					jedisPool.returnBrokenResource(jds);
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.elsa.redis.util.JedisDBInterface#lock(java.lang.String)
	 */
	public boolean lock(String key) {
		return this.lock(key, LOCK_EXPIRE_SECONDS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.elsa.redis.util.JedisDBInterface#unLock(java.lang.String)
	 */
	public boolean unLock(String key) {
		if (null == key || "".equals(key.trim())) {
			return false;
		}
		key = this.getCompleteKey(key);
		Jedis jds = null;
		JedisPool jedisPool = null;
		boolean isSuccess = true;
		try {
			jedisPool = getMasterJedisPool(key);
			jds = jedisPool.getResource();
			Long i = jds.del(key);
			if (i.intValue() == 1) {
				return true;
			}
		} catch (Exception e) {
			isSuccess = false;
			log.error(e.getMessage(), e);
		} finally {
			if (null != jedisPool) {
				if (isSuccess) {
					jedisPool.returnResource(jds);
				} else {
					jedisPool.returnBrokenResource(jds);
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.elsa.redis.util.JedisDBInterface#incr(java.lang.String)
	 */
	public Long incr(String key) {
		if (null == key || "".equals(key.trim())) {
			return null;
		}
		key = this.getCompleteKey(key);
		Jedis jds = null;
		JedisPool jedisPool = null;
		boolean isSuccess = true;
		try {
			jedisPool = getMasterJedisPool(key);
			jds = jedisPool.getResource();
			return jds.incr(key);
		} catch (Exception e) {
			isSuccess = false;
			log.error(e.getMessage(), e);
		} finally {
			if (null != jedisPool) {
				if (isSuccess) {
					jedisPool.returnResource(jds);
				} else {
					jedisPool.returnBrokenResource(jds);
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.elsa.redis.util.JedisDBInterface#incrBy(java.lang.String, int)
	 */
	public Long incrBy(String key, int step) {
		if (null == key || "".equals(key.trim())) {
			return null;
		}
		key = this.getCompleteKey(key);
		Jedis jds = null;
		JedisPool jedisPool = null;
		boolean isSuccess = true;
		try {
			jedisPool = getMasterJedisPool(key);
			jds = jedisPool.getResource();
			return jds.incrBy(key, step);
		} catch (Exception e) {
			isSuccess = false;
			log.error(e.getMessage(), e);
		} finally {
			if (null != jedisPool) {
				if (isSuccess) {
					jedisPool.returnResource(jds);
				} else {
					jedisPool.returnBrokenResource(jds);
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.elsa.redis.util.JedisDBInterface#batchSetDBString(java.util.Map,
	 * java.lang.Integer)
	 */
	public boolean batchSetDBString(Map<String, String> map, Integer expireSeconds) {
		if (map.isEmpty()) {
			return false;
		}
		if (map.size() > 300) {
			return false;
		}

		boolean isSuccess = true;
		Map<String, Map<String, String>> newMap = new HashMap<String, Map<String, String>>();
		for (Map.Entry<String, String> ent : map.entrySet()) {
			String key = ent.getKey();
			String jedisStrategyKey = this.getJedisStrategyKey(key);
			Map<String, String> m = newMap.get(jedisStrategyKey);
			if (null == m) {
				m = new HashMap<String, String>();
			}
			if (null != ent.getKey() && null != ent.getValue()) {
				m.put(ent.getKey().trim(), ent.getValue().trim());
			}
			newMap.put(jedisStrategyKey, m);
		}

		for (Map.Entry<String, Map<String, String>> en : newMap.entrySet()) {
			String strategyKey = en.getKey();
			Jedis jds = null;
			JedisPool jedisPool = null;
			try {
				jedisPool = getMasterJedisPoolWithStrategyKey(strategyKey);
				jds = jedisPool.getResource();

				Map<String, String> value = en.getValue();
				if (!value.isEmpty()) {
					Transaction trans = jds.multi();
					for (Map.Entry<String, String> mm : value.entrySet()) {
						String key = mm.getKey();
						key = this.getCompleteKey(key);
						trans.set(key, mm.getValue());
						if (null != expireSeconds) {
							trans.expire(key, expireSeconds);
						}
					}
					trans.exec();
				}

			} catch (Exception e) {
				isSuccess = false;
				log.error(e.getMessage(), e);
			} finally {
				if (null != jedisPool) {
					if (isSuccess) {
						jedisPool.returnResource(jds);
					} else {
						jedisPool.returnBrokenResource(jds);
					}
				}
			}
		}
		return true;
	}

}
