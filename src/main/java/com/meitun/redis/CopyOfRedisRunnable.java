//package com.elsa.redis;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentMap;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//
//import redis.clients.jedis.Jedis;
//import redis.clients.jedis.JedisShardInfo;
//
///**
// * @author longhaisheng
// *
// */
//public class CopyOfRedisRunnable implements Runnable {
//
//	private static final Log logger = LogFactory.getLog(MonitorJedis.class);
//
//	private volatile boolean flag = true;
//
//	private Map<String, JedisShardInfo> defaultShardInfoMap = new HashMap<String, JedisShardInfo>();// durable
//
//	private Map<String, CacheShardedJedisPool> cacheShardedJedisPoolMap;
//
//	private ConcurrentMap<String, RedisNode> lastFailMap = new ConcurrentHashMap<String, RedisNode>();
//
//	private ConcurrentMap<String, RedisNode> lastSuccessMap = new ConcurrentHashMap<String, RedisNode>();
//
//	public void run() {
//		if (flag) {
//			List<RedisNode> retuenDelayNodes = retuenAllWatchNodes();
//			reload_pool_new(retuenDelayNodes);
//			// boolean needReload = needReload(retuenDelayNodes);
//			// if (needReload) {
//			// logger.info("#############################################" +
//			// needReload);
//			// reloadPool(retuenDelayNodes);
//			// }
//		}
//	}
//
//	// private boolean needReload(List<RedisNode> allWatchNodeList) {
//	// boolean needRload = false;
//	// List<RedisNode> failNodes = returnFailNodes(allWatchNodeList);
//	// if (failNodes.size() > 0) {
//	// for (RedisNode failNode : failNodes) {
//	// for (Map.Entry<String, RedisNode> entry : lastSuccessMap.entrySet()) {
//	// RedisNode lastAliveNode = entry.getValue();
//	// if (failNode.getHost().equals(lastAliveNode.getHost()) &&
//	// failNode.getPort() == lastAliveNode.getPort()) {
//	// needRload = true;
//	// break;
//	// }
//	// }
//	// }
//	// }
//	//
//	// List<RedisNode> successNodes = retuenSuccessNodes(allWatchNodeList);
//	// if (successNodes.size() > 0) {
//	// for (RedisNode aliveNode : successNodes) {
//	// if (!lastFailMap.isEmpty()) {
//	// for (Map.Entry<String, RedisNode> entry : lastFailMap.entrySet()) {
//	// RedisNode failNode = entry.getValue();
//	// if (aliveNode.getHost().equals(failNode.getHost()) && aliveNode.getPort()
//	// == failNode.getPort()) {
//	// needRload = true;
//	// break;
//	// }
//	// }
//	// }
//	// }
//	// }
//	//
//	// if (needRload) {
//	// if (failNodes.size() > 0) {
//	// lastFailMap.clear();
//	// for (RedisNode node : failNodes) {
//	// lastFailMap.put(buildMapKey(node.getHost(), node.getPort()), node);
//	// }
//	// }
//	// if (successNodes.size() > 0) {
//	// lastSuccessMap.clear();
//	// for (RedisNode node : successNodes) {
//	// lastSuccessMap.put(buildMapKey(node.getHost(), node.getPort()), node);
//	// }
//	// }
//	// }
//	//
//	// return needRload;
//	// }
//
//	public boolean success_node_in_cacheshardedjedispoolmap(RedisNode node) {
//		boolean is = false;
//		for (Map.Entry<String, JedisShardInfo> entry : defaultShardInfoMap.entrySet()) {
//			JedisShardInfo value = entry.getValue();
//			if (node.getHost().equals(value.getHost()) && node.getPort() == value.getPort()) {
//				is = true;
//			}
//		}
//
//		boolean in = false;
//		for (Map.Entry<String, CacheShardedJedisPool> entry : cacheShardedJedisPoolMap.entrySet()) {
//			CacheShardedJedisPool onePool = entry.getValue();
//			List<JedisShardInfo> jedisShardInfoList = onePool.getJedisShardInfos();
//			for (Iterator<JedisShardInfo> iterator = jedisShardInfoList.iterator(); iterator.hasNext();) {
//				JedisShardInfo shardInfo = (JedisShardInfo) iterator.next();
//				if (shardInfo.getHost().equals(node.getHost()) && shardInfo.getPort() == node.getPort()) {
//					in = true;
//				}
//			}
//
//		}
//
//		if (is && in) {
//			return true;
//		}
//
//		return false;
//	}
//
//	public boolean fail_node_in_cacheshardedjedispoolmap(RedisNode node) {
//		boolean is = false;
//		for (Map.Entry<String, JedisShardInfo> entry : defaultShardInfoMap.entrySet()) {
//			JedisShardInfo value = entry.getValue();
//			if (node.getHost().equals(value.getHost()) && node.getPort() == value.getPort()) {
//				is = true;
//			}
//		}
//
//		boolean in = false;
//		for (Map.Entry<String, CacheShardedJedisPool> entry : cacheShardedJedisPoolMap.entrySet()) {
//			CacheShardedJedisPool onePool = entry.getValue();
//			List<JedisShardInfo> jedisShardInfoList = onePool.getJedisShardInfos();
//			for (Iterator<JedisShardInfo> iterator = jedisShardInfoList.iterator(); iterator.hasNext();) {
//				JedisShardInfo shardInfo = (JedisShardInfo) iterator.next();
//				if (shardInfo.getHost().equals(node.getHost()) && shardInfo.getPort() == node.getPort()) {
//					in = true;
//				}
//			}
//
//		}
//
//		if (is && in) {
//			return true;
//		}
//
//		return false;
//	}
//
//	private synchronized void reload_pool_new(List<RedisNode> allNodeList) {
//		List<RedisNode> successNodes = retuenSuccessNodes(allNodeList);
//		List<RedisNode> failNodes = returnFailNodes(allNodeList);
//		if (!successNodes.isEmpty()) {
//			for (Iterator<RedisNode> iterator = successNodes.iterator(); iterator.hasNext();) {
//				RedisNode n = (RedisNode) iterator.next();
//				if (success_node_in_cacheshardedjedispoolmap(n)) {
//					iterator.remove();
//				}
//			}
//		}
//
//		if (!failNodes.isEmpty()) {
//			for (Iterator<RedisNode> it = failNodes.iterator(); it.hasNext();) {
//				RedisNode n = (RedisNode) it.next();
//				if (!fail_node_in_cacheshardedjedispoolmap(n)) {
//					it.remove();
//				}
//			}
//		}
//
//		if (failNodes.size() > 0 || successNodes.size() > 0) {
//
//			Map<String, JedisShardInfo> successJedisShardInfoMap = new HashMap<String, JedisShardInfo>();
//			if (!successNodes.isEmpty()) {
//				for (RedisNode node : successNodes) {
//					for (Map.Entry<String, JedisShardInfo> entry : defaultShardInfoMap.entrySet()) {
//						JedisShardInfo value = entry.getValue();
//						if (node.getHost().equals(value.getHost()) && node.getPort() == value.getPort()) {
//							successJedisShardInfoMap.put(entry.getKey(), value);
//						}
//					}
//				}
//			}
//
//			for (Map.Entry<String, CacheShardedJedisPool> entry : cacheShardedJedisPoolMap.entrySet()) {
//
//				CacheShardedJedisPool onePool = entry.getValue();
//				List<JedisShardInfo> jedisShardInfoList = onePool.getJedisShardInfos();
//
//				if (null != jedisShardInfoList) {
//					for (Iterator<JedisShardInfo> iterator = jedisShardInfoList.iterator(); iterator.hasNext();) {// 移除失败的
//						JedisShardInfo shardInfo = (JedisShardInfo) iterator.next();
//						if (!failNodes.isEmpty()) {
//							for (RedisNode node : failNodes) {
//								if (shardInfo.getHost().equals(node.getHost()) && shardInfo.getPort() == node.getPort()) {
//									iterator.remove();
//									if (logger.isInfoEnabled()) {
//										logger.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ remove node==>" + node.getHost() + "=>" + node.getPort());
//									}
//								}
//							}
//						}
//					}
//				}
//
//				if (!successJedisShardInfoMap.isEmpty()) {
//					List<JedisShardInfo> needInsertList = new ArrayList<JedisShardInfo>();
//					for (Map.Entry<String, JedisShardInfo> en : successJedisShardInfoMap.entrySet()) {
//						JedisShardInfo suc = en.getValue();
//						boolean isInLast = false;
//						for (JedisShardInfo jedisShardInfo : jedisShardInfoList) {
//							if (suc.getHost().equals(jedisShardInfo.getHost()) && suc.getPort() == jedisShardInfo.getPort()) {
//								isInLast = true;
//								break;
//							}
//						}
//						if (!isInLast) {
//							needInsertList.add(suc);
//						}
//					}
//
//					if (!needInsertList.isEmpty()) {
//						jedisShardInfoList.addAll(needInsertList);
//					}
//
//				}
//
//				if (null != jedisShardInfoList && !jedisShardInfoList.isEmpty() && jedisShardInfoList.size() > 0) {
//					onePool.setJedisShardInfos(jedisShardInfoList);
//					if (logger.isInfoEnabled()) {
//						StringBuffer sb = new StringBuffer("");
//						for (JedisShardInfo j : jedisShardInfoList) {
//							sb.append(buildMapKey(j.getHost(), j.getPort()));
//							sb.append(",");
//						}
//						logger.info("**************************************************reloadPool() start *************************************************" + sb.toString());
//					}
//					onePool.reloadPool();
//					if (logger.isInfoEnabled()) {
//						logger.info("**************************************************reloadPool() end ***************************************************" + jedisShardInfoList.size());
//					}
//				}
//
//			}
//		}
//
//	}
//
//	// private synchronized void reloadPool(List<RedisNode> allNodeList) {
//	// List<RedisNode> failNodes = returnFailNodes(allNodeList);
//	// List<RedisNode> successNodes = retuenSuccessNodes(allNodeList);
//	//
//	// Map<String, JedisShardInfo> successJedisShardInfoMap = new
//	// HashMap<String, JedisShardInfo>();
//	// if (!successNodes.isEmpty()) {
//	// for (RedisNode node : successNodes) {
//	// for (Map.Entry<String, JedisShardInfo> entry :
//	// defaultShardInfoMap.entrySet()) {
//	// JedisShardInfo value = entry.getValue();
//	// if (node.getHost().equals(value.getHost()) && node.getPort() ==
//	// value.getPort()) {
//	// successJedisShardInfoMap.put(entry.getKey(), value);
//	// }
//	// }
//	// }
//	// }
//	//
//	// for (Map.Entry<String, CacheShardedJedisPool> entry :
//	// cacheShardedJedisPoolMap.entrySet()) {
//	//
//	// CacheShardedJedisPool onePool = entry.getValue();
//	// List<JedisShardInfo> jedisShardInfoList = onePool.getJedisShardInfos();
//	//
//	// for (Iterator<JedisShardInfo> iterator = jedisShardInfoList.iterator();
//	// iterator.hasNext();) {
//	// JedisShardInfo shardInfo = (JedisShardInfo) iterator.next();
//	// if (!failNodes.isEmpty()) {
//	// for (RedisNode node : failNodes) {
//	// if (shardInfo.getHost().equals(node.getHost()) && shardInfo.getPort() ==
//	// node.getPort()) {
//	// iterator.remove();
//	// if (logger.isInfoEnabled()) {
//	// logger.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ remove node==>"
//	// + node.getHost() + "=>" + node.getPort());
//	// }
//	// }
//	// }
//	// }
//	// }
//	//
//	// if (!successJedisShardInfoMap.isEmpty()) {
//	// List<JedisShardInfo> needInsertList = new ArrayList<JedisShardInfo>();
//	// for (Map.Entry<String, JedisShardInfo> en :
//	// successJedisShardInfoMap.entrySet()) {
//	// boolean isInLast = false;
//	// JedisShardInfo value2 = en.getValue();
//	// for (JedisShardInfo jedisShardInfo : jedisShardInfoList) {
//	// JedisShardInfo value = value2;
//	// if (value.getHost().equals(jedisShardInfo.getHost()) && value.getPort()
//	// == jedisShardInfo.getPort()) {
//	// isInLast = true;
//	// break;
//	// }
//	// }
//	// if (!isInLast) {
//	// needInsertList.add(value2);
//	// }
//	// }
//	//
//	// if (!needInsertList.isEmpty()) {
//	// jedisShardInfoList.addAll(needInsertList);
//	// }
//	// // jedisShardInfoList.addAll(successJedisShardInfoMap.values());
//	// }
//	//
//	// if (null != jedisShardInfoList && !jedisShardInfoList.isEmpty() &&
//	// jedisShardInfoList.size() > 0) {
//	// onePool.setJedisShardInfos(jedisShardInfoList);
//	// if (logger.isInfoEnabled()) {
//	// StringBuffer sb = new StringBuffer("");
//	// for (JedisShardInfo j : jedisShardInfoList) {
//	// sb.append(buildMapKey(j.getHost(), j.getPort()));
//	// sb.append(",");
//	// }
//	// logger.info("**************************************************reloadPool() start *************************************************"
//	// + sb.toString());
//	// }
//	// onePool.reloadPool();
//	// if (logger.isInfoEnabled()) {
//	// logger.info("**************************************************reloadPool() end ***************************************************"
//	// + jedisShardInfoList.size());
//	// }
//	// }
//	// }
//	//
//	// }
//
//	private String buildMapKey(String host, int port) {
//		return host + "_" + port;
//	}
//
//	private List<RedisNode> returnFailNodes(List<RedisNode> nodeList) {
//		List<RedisNode> returnList = new ArrayList<RedisNode>();
//		for (RedisNode node : nodeList) {
//			boolean alive = node.isAlive();
//			if (!alive) {
//				returnList.add(node);
//			}
//		}
//		return returnList;
//	}
//
//	private List<RedisNode> retuenSuccessNodes(List<RedisNode> nodeList) {
//		List<RedisNode> returnList = new ArrayList<RedisNode>();
//		for (RedisNode node : nodeList) {
//			boolean alive = node.isAlive();
//			if (alive) {
//				returnList.add(node);
//			}
//		}
//		return returnList;
//	}
//
//	private List<RedisNode> retuenAllWatchNodes() {
//		List<RedisNode> nodeList = new ArrayList<RedisNode>();
//		for (Map.Entry<String, JedisShardInfo> entry : defaultShardInfoMap.entrySet()) {
//			String host = entry.getValue().getHost();
//			int port = entry.getValue().getPort();
//			boolean alive = monitorOneRedis(host, port);
//			RedisNode node = new RedisNode();
//			logger.info(buildMapKey(host, port) + "===================>" + alive);
//			node.setAlive(alive);
//			node.setHost(host);
//			node.setPort(port);
//			nodeList.add(node);
//		}
//		return nodeList;
//	}
//
//	private long getOnceTimeOut(long waitTime, long timeout) {
//		long onceTimeOut = 2000;
//		long remainTime = timeout - waitTime;
//		if (onceTimeOut > remainTime) {
//			onceTimeOut = remainTime;
//		}
//		return onceTimeOut;
//	}
//
//	private boolean monitorOneRedis(String host, int port) {// 此处只有debug开启才打印日志，否则日志日久会积压太多
//		Jedis jedis = null;
//		int timeout = 3000;
//		long waitTime = 0;
//		int retryTimes = 2;
//		if (logger.isDebugEnabled()) {
//			logger.debug("设定的监控的重试次数为：" + retryTimes);
//		}
//
//		int tryCount = 0;
//		int failTimes = 0;
//		while (0 == timeout || timeout > waitTime) {
//			tryCount++;
//			if (tryCount > retryTimes + 1) {
//				if (logger.isDebugEnabled()) {
//					logger.warn("已经到达了设定的重试次数" + retryTimes + "次");
//				}
//				break;
//			}
//			if (logger.isDebugEnabled()) {
//				logger.debug("监控redis host=>" + host + ",port=>" + port + "，第" + tryCount + "次尝试, waitTime:" + waitTime);
//			}
//
//			long onceTimeOut = getOnceTimeOut(waitTime, timeout);
//			waitTime += onceTimeOut;
//
//			try {
//				jedis = new Jedis(host, port);
//				String isOk = jedis.set("jedis_listen_key_" + host + port, "1");
//				if (null != isOk && !isOk.equalsIgnoreCase("OK")) {
//					failTimes++;
//				}
//			} catch (Exception e) {
//				failTimes++;
//				if (logger.isDebugEnabled()) {
//					logger.error(e.getMessage(), e);
//				}
//			} finally {
//				if (null != jedis) {
//					try {
//						jedis.quit();
//						jedis.disconnect();
//						jedis.close();
//					} catch (Exception e) {
//						// ingone
//					}
//				}
//			}
//		}
//		if (failTimes >= tryCount) {
//			logger.info("watch fail node redis host=>" + host + ",port=>" + port + "");
//			return false;
//		}
//		return true;
//	}
//
//	public void setDefaultShardInfoMap(Map<String, JedisShardInfo> defaultShardInfoMap) {
//		this.defaultShardInfoMap = defaultShardInfoMap;
//		if (!defaultShardInfoMap.isEmpty()) {
//			for (Map.Entry<String, JedisShardInfo> entry : defaultShardInfoMap.entrySet()) {
//				String host = entry.getValue().getHost();
//				int port = entry.getValue().getPort();
//				RedisNode n = new RedisNode(host, port, true);
//				this.lastSuccessMap.put(buildMapKey(host, port), n);
//			}
//		}
//	}
//
//	public void setFlag(boolean flag) {
//		this.flag = flag;
//	}
//
//	public void setCacheShardedJedisPoolMap(Map<String, CacheShardedJedisPool> pools) {
//		this.cacheShardedJedisPoolMap = pools;
//	}
//
//}
