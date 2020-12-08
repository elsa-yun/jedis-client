package com.elsa.redis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.util.Hashing;
import redis.clients.util.Pool;

public class ShardedJedisSentinelPool extends Pool<ShardedJedis> {

	public static final int MAX_RETRY_SENTINEL = 10;

	private static final Log log = LogFactory.getLog(ShardedJedisSentinelPool.class);

	protected GenericObjectPoolConfig poolConfig;

	protected int timeout = Protocol.DEFAULT_TIMEOUT;

	private int sentinelRetry = 0;

	protected String password;

	protected int database = Protocol.DEFAULT_DATABASE;

	protected Set<MasterListener> masterListeners = new HashSet<MasterListener>();

	private volatile List<HostAndPort> currentHostMasters;

	private volatile ShardedJedisFactory shardedJedisFactory;

	public ShardedJedisSentinelPool(List<String> masters, Set<String> sentinels) {
		this(masters, sentinels, new GenericObjectPoolConfig(), Protocol.DEFAULT_TIMEOUT, null, Protocol.DEFAULT_DATABASE);
	}

	public ShardedJedisSentinelPool(List<String> masters, Set<String> sentinels, String password) {
		this(masters, sentinels, new GenericObjectPoolConfig(), Protocol.DEFAULT_TIMEOUT, password);
	}

	public ShardedJedisSentinelPool(final GenericObjectPoolConfig poolConfig, List<String> masters, Set<String> sentinels) {
		this(masters, sentinels, poolConfig, Protocol.DEFAULT_TIMEOUT, null, Protocol.DEFAULT_DATABASE);
	}

	public ShardedJedisSentinelPool(List<String> masters, Set<String> sentinels, final GenericObjectPoolConfig poolConfig, int timeout, final String password) {
		this(masters, sentinels, poolConfig, timeout, password, Protocol.DEFAULT_DATABASE);
	}

	public ShardedJedisSentinelPool(List<String> masters, Set<String> sentinels, final GenericObjectPoolConfig poolConfig, final int timeout) {
		this(masters, sentinels, poolConfig, timeout, null, Protocol.DEFAULT_DATABASE);
	}

	public ShardedJedisSentinelPool(List<String> masters, Set<String> sentinels, final GenericObjectPoolConfig poolConfig, final String password) {
		this(masters, sentinels, poolConfig, Protocol.DEFAULT_TIMEOUT, password);
	}

	public ShardedJedisSentinelPool(List<String> masters, Set<String> sentinels, final GenericObjectPoolConfig poolConfig, int timeout, final String password, final int database) {
		this.poolConfig = poolConfig;
		this.timeout = timeout;
		this.password = password;
		this.database = database;

		List<HostAndPort> masterList = initSentinels(sentinels, masters);
		initPool(masterList);
	}

	public void destroy() {
		for (MasterListener m : masterListeners) {
			m.shutdown();
		}

		super.destroy();
	}

	public List<HostAndPort> getCurrentHostMaster() {
		return currentHostMasters;
	}

	private void initPool(List<HostAndPort> masters) {
		if (!equals(currentHostMasters, masters)) {
			StringBuffer sb = new StringBuffer();
			for (HostAndPort master : masters) {
				sb.append(master.toString());
				sb.append(" ");
			}
			log.info("Created ShardedJedisPool to master ===> [" + sb.toString() + "]");
			List<JedisShardInfo> shardMasters = makeShardInfoList(masters);
			if (null == shardedJedisFactory) {
				shardedJedisFactory = new ShardedJedisFactory(shardMasters, Hashing.MURMUR_HASH, null);
				initPool(poolConfig, shardedJedisFactory);
			} else {
				shardedJedisFactory.setShards(shardMasters);
				internalPool.clear();
			}
			currentHostMasters = masters;
		}
	}

	private static boolean equals(List<HostAndPort> currentShardMasters, List<HostAndPort> shardMasters) {
		if (currentShardMasters != null && shardMasters != null) {
			if (currentShardMasters.size() == shardMasters.size()) {
				for (int i = 0; i < currentShardMasters.size(); i++) {
					if (!currentShardMasters.get(i).equals(shardMasters.get(i)))
						return false;
				}
				return true;
			}
		}
		return false;
	}

	private List<JedisShardInfo> makeShardInfoList(List<HostAndPort> masters) {
		List<JedisShardInfo> shardMasters = new ArrayList<JedisShardInfo>();
		for (HostAndPort master : masters) {
			JedisShardInfo jedisShardInfo = new JedisShardInfo(master.getHost(), master.getPort(), timeout);
			jedisShardInfo.setPassword(password);
			shardMasters.add(jedisShardInfo);
		}
		return shardMasters;
	}

	private List<HostAndPort> initSentinels(Set<String> sentinelList, final List<String> masterList) {

		Map<String, HostAndPort> masterMap = new HashMap<String, HostAndPort>();
		List<HostAndPort> shardMasters = new ArrayList<HostAndPort>();

		log.info("Trying to find all master from available Sentinels::::::");

		for (String masterName : masterList) {
			HostAndPort master = null;
			boolean fetched = false;

			while (!fetched && sentinelRetry < MAX_RETRY_SENTINEL) {
				for (String sentinel : sentinelList) {
					final HostAndPort hap = toHostAndPort(Arrays.asList(sentinel.split(":")));

					log.info("Connecting to Sentinel ==>" + hap);
					Jedis jedis = null;
					try {
						jedis = new Jedis(hap.getHost(), hap.getPort());
						master = masterMap.get(masterName);
						if (master == null) {
							List<String> hostAndPort = jedis.sentinelGetMasterAddrByName(masterName);
							if (hostAndPort != null && hostAndPort.size() > 0) {
								master = toHostAndPort(hostAndPort);
								log.info("Find Redis master name ==>" + master);
								shardMasters.add(master);
								masterMap.put(masterName, master);
								fetched = true;
								jedis.disconnect();
								break;
							}
						}
					} catch (JedisConnectionException e) {
						log.error("Cannot connect to sentinel running @ " + hap + ". Trying next one.");
					}
				}

				if (null == master) {
					try {
						log.info("All sentinels down, cannot determine where is [" + masterName + "] master is running,sleeping 1000ms, Will try again.");
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					fetched = false;
					sentinelRetry++;
				}
			}

			// Try MAX_RETRY_SENTINEL times.
			if (!fetched && sentinelRetry >= MAX_RETRY_SENTINEL) {
				log.error("All sentinels has down and try [" + MAX_RETRY_SENTINEL + "] times, Abort all.");
				throw new JedisConnectionException("Cannot connect all sentinels, Abort all.");
			}
		}

		// All shards master must been accessed.
		if (masterList.size() != 0 && masterList.size() == shardMasters.size()) {

			log.info("Starting Sentinel listeners...");
			for (String sentinel : sentinelList) {
				final HostAndPort hap = toHostAndPort(Arrays.asList(sentinel.split(":")));
				MasterListener masterListener = new MasterListener(masterList, hap.getHost(), hap.getPort());
				masterListeners.add(masterListener);
				masterListener.start();
			}
		}

		return shardMasters;
	}

	private static HostAndPort toHostAndPort(List<String> getMasterAddrByNameResult) {
		String host = getMasterAddrByNameResult.get(0);
		int port = Integer.parseInt(getMasterAddrByNameResult.get(1));
		return new HostAndPort(host, port);
	}

	/**
	 * PoolableObjectFactory custom impl.
	 */
	protected static class ShardedJedisFactory implements PooledObjectFactory<ShardedJedis> {

		private List<JedisShardInfo> shards;

		private Hashing algo;

		private Pattern keyTagPattern;

		public List<JedisShardInfo> getShards() {
			return shards;
		}

		public void setShards(List<JedisShardInfo> shards) {
			this.shards = shards;
		}

		public ShardedJedisFactory(List<JedisShardInfo> shards, Hashing algo, Pattern keyTagPattern) {
			this.shards = shards;
			this.algo = algo;
			this.keyTagPattern = keyTagPattern;
		}

		public PooledObject<ShardedJedis> makeObject() throws Exception {
			ShardedJedis jedis = new ShardedJedis(shards, algo, keyTagPattern);
			return new DefaultPooledObject<ShardedJedis>(jedis);
		}

		public void destroyObject(PooledObject<ShardedJedis> pooledShardedJedis) throws Exception {
			final ShardedJedis shardedJedis = pooledShardedJedis.getObject();
			for (Jedis jedis : shardedJedis.getAllShards()) {
				try {
					try {
						jedis.quit();
					} catch (Exception e) {

					}
					jedis.disconnect();
				} catch (Exception e) {

				}
			}
		}

		public boolean validateObject(PooledObject<ShardedJedis> pooledShardedJedis) {
			try {
				ShardedJedis jedis = pooledShardedJedis.getObject();
				for (Jedis shard : jedis.getAllShards()) {
					if (!shard.ping().equals("PONG")) {
						return false;
					}
				}
				return true;
			} catch (Exception ex) {
				return false;
			}
		}

		public void activateObject(PooledObject<ShardedJedis> p) throws Exception {

		}

		public void passivateObject(PooledObject<ShardedJedis> p) throws Exception {

		}
	}

	protected class JedisPubSubAdapter extends JedisPubSub {
		@Override
		public void onMessage(String channel, String message) {
		}

		@Override
		public void onPMessage(String pattern, String channel, String message) {
		}

		@Override
		public void onPSubscribe(String pattern, int subscribedChannels) {
		}

		@Override
		public void onPUnsubscribe(String pattern, int subscribedChannels) {
		}

		@Override
		public void onSubscribe(String channel, int subscribedChannels) {
		}

		@Override
		public void onUnsubscribe(String channel, int subscribedChannels) {
		}
	}

	protected class MasterListener extends Thread {

		protected List<String> masters;
		protected String host;
		protected int port;
		protected long subscribeRetryWaitTimeMillis = 5000;
		protected Jedis jedis;
		protected AtomicBoolean running = new AtomicBoolean(false);

		protected MasterListener() {
		}

		public MasterListener(List<String> masters, String host, int port) {
			this.masters = masters;
			this.host = host;
			this.port = port;
		}

		public MasterListener(List<String> masters, String host, int port, long subscribeRetryWaitTimeMillis) {
			this(masters, host, port);
			this.subscribeRetryWaitTimeMillis = subscribeRetryWaitTimeMillis;
		}

		public void run() {

			running.set(true);
			while (running.get()) {
				jedis = new Jedis(host, port);
				try {
					jedis.subscribe(new JedisPubSubAdapter() {
						@Override
						public void onMessage(String channel, String message) {
							log.info("Sentinel ]" + host + ":" + port + "] published==>" + message);
							String[] switchMasterMsg = message.split(" ");
							if (switchMasterMsg.length > 3) {
								int index = masters.indexOf(switchMasterMsg[0]);
								if (index >= 0) {
									HostAndPort newHostMaster = toHostAndPort(Arrays.asList(switchMasterMsg[3], switchMasterMsg[4]));
									List<HostAndPort> newHostMasterList = new ArrayList<HostAndPort>();
									for (int i = 0; i < masters.size(); i++) {
										newHostMasterList.add(null);
									}
									Collections.copy(newHostMasterList, currentHostMasters);
									newHostMasterList.set(index, newHostMaster);
									initPool(newHostMasterList);
								} else {
									StringBuffer sb = new StringBuffer();
									for (String masterName : masters) {
										sb.append(masterName);
										sb.append(",");
									}
									log.info("Ignoring message on +switch-master for master name [" + switchMasterMsg[0] + "], our monitor master name are [" + sb + "]");
								}

							} else {
								log.info("Invalid message received on Sentinel [" + host + ":" + port + "] on channel +switch-master: " + message);
							}
						}
					}, "+switch-master");

				} catch (JedisConnectionException e) {
					if (running.get()) {
						log.info("Lost connection to Sentinel at [" + host + ":" + port + "] Sleeping 5000ms and retrying.");
						try {
							Thread.sleep(subscribeRetryWaitTimeMillis);
						} catch (InterruptedException e1) {
							log.error(e.getMessage(), e);
						}
					} else {
						log.info("Unsubscribing from Sentinel at [" + host + ":" + port + "]");
					}
				}
			}
		}

		public void shutdown() {
			try {
				log.info("Shutting down listener on [" + host + ":" + port + "]");
				running.set(false);
				// This isn't good, the Jedis object is not thread safe
				jedis.disconnect();
			} catch (Exception e) {
				log.error("Caught exception while shutting down: " + e.getMessage(), e);
			}
		}
	}
}