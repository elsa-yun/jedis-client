package com.elsa.redis.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.elsa.redis.ApplicationContextBeans;
import com.elsa.redis.DBShardJedisPool;

/**
 * 
 * @author longhaisheng
 *
 */
public class DBHashMap<T> {

	private static final String UTF_8 = "UTF-8";

	private Log log = LogFactory.getLog(DBHashMap.class);

	private final static String DB_JEDIS_POOL_NAME = "dbMasterShardJedisPool";

	private byte[] key;

	private DBShardJedisPool dbJedisPool;

	private JedisPool jedisPool;

	private int shardfieldValue;

	private Integer expireSeconds;

	private boolean everyTimeExpire = true;

	private boolean hasSetTimeExpire = false;

	private int getShardfieldValue() {
		return shardfieldValue;
	}

	private void setShardfieldValue(int shardfieldValue) {
		this.shardfieldValue = shardfieldValue;
	}

	private void setDbJedisPool(DBShardJedisPool dbJedisPool) {
		this.dbJedisPool = dbJedisPool;
	}

	private JedisPool getDbJedisPool() {
		if (null == this.jedisPool) {
			this.jedisPool = dbJedisPool.getJedisPool(getShardfieldValue());
			return this.jedisPool;
		}
		return this.jedisPool;
	}

	private static byte[] stringToByteArray(String str) {
		try {
			return str.getBytes(UTF_8);
		} catch (UnsupportedEncodingException e) {

		}
		return null;
	}

	/**
	 * 
	 * @param key
	 * @param shardfieldValue
	 * 
	 * 
	 */
	public DBHashMap(String key, int shardfieldValue, String db_jedis_pool_name) {
		this(key, shardfieldValue, db_jedis_pool_name, null);
	}

	/**
	 * 
	 * @param key
	 * @param shardfieldValue
	 * 
	 * 
	 */
	public DBHashMap(String key, int shardfieldValue) {
		this(key, shardfieldValue, DB_JEDIS_POOL_NAME, null);
	}

	public DBHashMap(String key, int shardfieldValue, Integer expireSeconds) {
		this(key, shardfieldValue, DB_JEDIS_POOL_NAME, expireSeconds);
	}

	public DBHashMap(String key, int shardfieldValue, Integer expireSeconds, boolean everyTimeSetExpire) {
		this(key, shardfieldValue, DB_JEDIS_POOL_NAME, expireSeconds, everyTimeSetExpire);
	}

	private DBHashMap(String key, int shardfieldValue, String db_jedis_pool_name, Integer expireSeconds) {
		this(key, shardfieldValue, db_jedis_pool_name, expireSeconds, true);
	}

	private DBHashMap(String key, int shardfieldValue, String db_jedis_pool_name, Integer expireSeconds, boolean everyTimeExpire) {
		this.key = stringToByteArray(key);
		DBShardJedisPool dbJedisPool = (DBShardJedisPool) ApplicationContextBeans.getBean(db_jedis_pool_name);
		setDbJedisPool(dbJedisPool);
		this.setShardfieldValue(shardfieldValue);
		this.setFieldValue(shardfieldValue);
		this.everyTimeExpire = everyTimeExpire;
		if (expireSeconds != null && expireSeconds.intValue() > 0) {
			this.expireSeconds = expireSeconds;
		}
	}

	/**
	 * 设置后 用于在 dbJedisPool中的计算，以便从多个池中取出一个池
	 * 
	 * @param shardfieldValue
	 *            字段值
	 * @param routeValue
	 *            路由值
	 */
	private void setFieldValue(int shardfieldValue) {
		dbJedisPool.setFieldValue(shardfieldValue);
	}

	/**
	 * 添加 对象 o 至field字段中
	 * 
	 * @param field
	 *            字段名
	 * @param o
	 *            对应 field的value
	 * @return boolean
	 */
	public boolean put(String field, T o) {
		boolean operation = false;
		if (null != o) {
			try {
				byte[] b = serialize(o);
				Jedis jedis = null;
				boolean isSuccess = true;
				try {
					jedis = this.getDbJedisPool().getResource();
					jedis.hset(this.key, stringToByteArray(field), b);
					if (null != this.expireSeconds && this.expireSeconds.intValue() > 0) {
						if (this.everyTimeExpire) {
							jedis.expire(this.key, this.expireSeconds);
						} else {
							if (!this.hasSetTimeExpire) {
								jedis.expire(this.key, this.expireSeconds);
								this.hasSetTimeExpire = true;
							}
						}
					}
					operation = true;
				} catch (Exception ex) {
					isSuccess = false;
					log.info(ex.getMessage(), ex);
				} finally {
					if (isSuccess) {
						this.getDbJedisPool().returnResource(jedis);
					} else {
						this.getDbJedisPool().returnBrokenResource(jedis);
					}
				}
			} catch (Exception e) {
				log.info(e.getMessage(), e);
			}
		}
		return operation;
	}

	/**
	 * 删除field域
	 * 
	 * @param field
	 *            字段名
	 * @return boolean
	 */
	public boolean remove(String field) {
		Jedis jedis = null;
		boolean isSuccess = true;
		try {
			jedis = this.getDbJedisPool().getResource();
			Long value = jedis.hdel(this.key, stringToByteArray(field));
			if (value != null && value.intValue() > 0) {
				if (null != this.expireSeconds && this.expireSeconds.intValue() > 0) {
					if (this.everyTimeExpire) {
						jedis.expire(this.key, this.expireSeconds);
					} else {
						if (!this.hasSetTimeExpire) {
							jedis.expire(this.key, this.expireSeconds);
							this.hasSetTimeExpire = true;
						}
					}
				}
				return true;
			}
		} catch (Exception ex) {
			isSuccess = false;
			log.info(ex.getMessage(), ex);
		} finally {
			if (isSuccess) {
				this.getDbJedisPool().returnResource(jedis);
			} else {
				this.getDbJedisPool().returnBrokenResource(jedis);
			}
		}
		return false;
	}

	/**
	 * 取得field域对应的value
	 * 
	 * @param field
	 *            字段名
	 * @return T
	 */
	public T get(String field) {
		Jedis jedis = null;
		boolean isSuccess = true;
		try {
			jedis = this.getDbJedisPool().getResource();
			byte[] value = jedis.hget(this.key, stringToByteArray(field));
			if (null != value) {
				return unserialize(value);
			}
		} catch (Exception ex) {
			isSuccess = false;
			log.info(ex.getMessage(), ex);
		} finally {
			if (isSuccess) {
				this.getDbJedisPool().returnResource(jedis);
			} else {
				this.getDbJedisPool().returnBrokenResource(jedis);
			}
		}
		return null;
	}

	/**
	 * 返回此map中所有的field
	 * 
	 * @param field
	 *            字段名
	 * @return set
	 */
	public Set<String> fieldSet() {
		Jedis jedis = null;
		boolean isSuccess = true;
		Set<String> fieldSet = new HashSet<String>();
		try {
			jedis = this.getDbJedisPool().getResource();
			Set<byte[]> keys = jedis.hkeys(this.key);
			if (!keys.isEmpty()) {
				for (byte[] bs : keys) {
					String str = new String(bs, UTF_8);
					fieldSet.add(str);
				}
			}
			return fieldSet;

		} catch (Exception ex) {
			isSuccess = false;
			log.info(ex.getMessage(), ex);
		} finally {
			if (isSuccess) {
				this.getDbJedisPool().returnResource(jedis);
			} else {
				this.getDbJedisPool().returnBrokenResource(jedis);
			}
		}
		return fieldSet;
	}

	/**
	 * 是否包含field字段
	 * 
	 * @param field
	 *            字段名
	 * @return boolean
	 */
	public boolean containsField(String field) {
		Jedis jedis = null;
		boolean isSuccess = true;
		boolean operation = false;
		try {
			jedis = this.getDbJedisPool().getResource();
			operation = jedis.hexists(this.key, stringToByteArray(field));
		} catch (Exception ex) {
			isSuccess = false;
			log.info(ex.getMessage(), ex);
		} finally {
			if (isSuccess) {
				this.getDbJedisPool().returnResource(jedis);
			} else {
				this.getDbJedisPool().returnBrokenResource(jedis);
			}
		}
		return operation;
	}

	/**
	 * 是否包含对象 t,对象 t需重载 hashcode 和 equals方法
	 * 
	 * @param field
	 *            字段名
	 * @return boolean
	 */
	public boolean containsValue(T t) {
		Jedis jedis = null;
		boolean isSuccess = true;
		boolean contains = false;
		try {
			jedis = this.getDbJedisPool().getResource();
			List<byte[]> values = jedis.hvals(this.key);
			if (!values.isEmpty()) {
				for (byte[] bs : values) {
					if (unserialize(bs).equals(t)) {
						contains = true;
						break;
					}
				}
			}
		} catch (Exception ex) {
			isSuccess = false;
			log.info(ex.getMessage(), ex);
		} finally {
			if (isSuccess) {
				this.getDbJedisPool().returnResource(jedis);
			} else {
				this.getDbJedisPool().returnBrokenResource(jedis);
			}
		}
		return contains;
	}

	/**
	 * 返回此map中的所有 值
	 * 
	 * @return list
	 */
	public List<T> values() {
		Jedis jedis = null;
		boolean isSuccess = true;
		List<T> valueList = new ArrayList<T>();
		try {
			jedis = this.getDbJedisPool().getResource();
			List<byte[]> values = jedis.hvals(this.key);
			if (!values.isEmpty()) {
				for (byte[] bs : values) {
					T str = unserialize(bs);
					valueList.add(str);
				}
			}
		} catch (Exception ex) {
			isSuccess = false;
			log.info(ex.getMessage(), ex);
		} finally {
			if (isSuccess) {
				this.getDbJedisPool().returnResource(jedis);
			} else {
				this.getDbJedisPool().returnBrokenResource(jedis);
			}
		}
		return valueList;
	}

	/**
	 * 返回map大小
	 * 
	 * @return
	 */
	public int size() {
		Jedis jedis = null;
		boolean isSuccess = true;
		try {
			jedis = this.getDbJedisPool().getResource();
			Long values = jedis.hlen(this.key);
			return values.intValue();
		} catch (Exception ex) {
			isSuccess = false;
			log.info(ex.getMessage(), ex);
		} finally {
			if (isSuccess) {
				this.getDbJedisPool().returnResource(jedis);
			} else {
				this.getDbJedisPool().returnBrokenResource(jedis);
			}
		}
		return 0;
	}

	/**
	 * 根据步长增长某个字段的值 字段的值须为整数
	 * 
	 * @param field
	 *            字段
	 * @param step
	 *            步长
	 * @return
	 */
	public int incrField(String field, int step) {
		Jedis jedis = null;
		boolean isSuccess = true;
		try {
			jedis = this.getDbJedisPool().getResource();
			Long values = jedis.hincrBy(this.key, stringToByteArray(field), step);
			if (null != this.expireSeconds && this.expireSeconds.intValue() > 0) {
				if (this.everyTimeExpire) {
					jedis.expire(this.key, this.expireSeconds);
				} else {
					if (!this.hasSetTimeExpire) {
						jedis.expire(this.key, this.expireSeconds);
						this.hasSetTimeExpire = true;
					}
				}
			}
			return values.intValue();
		} catch (Exception ex) {
			isSuccess = false;
			log.info(ex.getMessage(), ex);
		} finally {
			if (isSuccess) {
				this.getDbJedisPool().returnResource(jedis);
			} else {
				this.getDbJedisPool().returnBrokenResource(jedis);
			}
		}
		return 0;
	}

	/**
	 * 自增某个字段的值 字段的值须为整数
	 * 
	 * @param field
	 * @return
	 */
	public int incrField(String field) {
		return this.incrField(field, 1);
	}

	/**
	 * 返回此map中的所有key和values
	 * 
	 * @return list
	 */
	public Map<String, T> keyValues() {
		Jedis jedis = null;
		boolean isSuccess = true;
		Map<String, T> keyValues = new HashMap<String, T>();
		try {
			jedis = this.getDbJedisPool().getResource();
			Map<byte[], byte[]> values = jedis.hgetAll(this.key);
			if (!values.isEmpty()) {
				for (Map.Entry<byte[], byte[]> bs : values.entrySet()) {
					byte[] key2 = bs.getKey();
					byte[] value = bs.getValue();
					if (null != value && null != key2) {
						T en = unserialize(value);
						keyValues.put(new String(key2, UTF_8), en);
					}
				}
			}

		} catch (Exception ex) {
			isSuccess = false;
			log.info(ex.getMessage(), ex);
		} finally {
			if (isSuccess) {
				this.getDbJedisPool().returnResource(jedis);
			} else {
				this.getDbJedisPool().returnBrokenResource(jedis);
			}
		}
		return keyValues;
	}

	private T unserialize(byte[] value) throws Exception {
		return (T) SerializeUtil.unserialize(value);
	}

	private byte[] serialize(T t) {
		return SerializeUtil.serialize(t);
	}

	public boolean clear() {
		return this.clearAndDelKey();
	}
	
	public boolean clearFields() {
		return this.clearAllFields();
	}

	/**
	 * 清除此map
	 * 
	 * @return
	 */
	public boolean clearAndDelKey() {
		Jedis jedis = null;
		boolean isSuccess = true;
		boolean isDelete = false;
		try {
			jedis = this.getDbJedisPool().getResource();
			Long op = jedis.del(this.key);
			if (null != op && op.intValue() > 0) {
				isDelete = true;
			}
		} catch (Exception ex) {
			isSuccess = false;
			log.info(ex.getMessage(), ex);
		} finally {
			if (isSuccess) {
				this.getDbJedisPool().returnResource(jedis);
			} else {
				this.getDbJedisPool().returnBrokenResource(jedis);
			}
		}
		return isDelete;
	}

	/**
	 * 清除此map中所有fields
	 * 
	 * @return
	 */
	public boolean clearAllFields() {
		Jedis jedis = null;
		boolean isSuccess = true;
		boolean isClear = false;
		try {
			jedis = this.getDbJedisPool().getResource();
			Set<byte[]> keys = jedis.hkeys(this.key);
			if (!keys.isEmpty()) {
				byte[][] bytes = new byte[keys.size()][];
				int num = 0;
				for (byte[] bs : keys) {
					bytes[num] = bs;
					num++;
				}
				Long i = jedis.hdel(this.key, bytes);
				if (null != i && i.intValue() > 0) {
					isClear = true;
				}
			} else {
				isClear = true;
			}
		} catch (Exception ex) {
			isSuccess = false;
			log.info(ex.getMessage(), ex);
		} finally {
			if (isSuccess) {
				this.getDbJedisPool().returnResource(jedis);
			} else {
				this.getDbJedisPool().returnBrokenResource(jedis);
			}
		}
		return isClear;
	}

}
