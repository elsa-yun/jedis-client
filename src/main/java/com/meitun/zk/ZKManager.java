//package com.elsa.zk;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.CountDownLatch;
//
//import org.apache.log4j.Logger;
//import org.apache.zookeeper.WatchedEvent;
//import org.apache.zookeeper.Watcher;
//import org.apache.zookeeper.Watcher.Event.KeeperState;
//import org.apache.zookeeper.ZooDefs;
//import org.apache.zookeeper.ZooDefs.Ids;
//import org.apache.zookeeper.ZooKeeper;
//import org.apache.zookeeper.ZooKeeper.States;
//import org.apache.zookeeper.data.ACL;
//import org.apache.zookeeper.data.Id;
//import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
//import org.springframework.beans.factory.DisposableBean;
//
///**
// * @author longhaisheng
// * 
// */
//public class ZKManager implements DisposableBean {
//
//	private Logger logger = Logger.getLogger(ZKManager.class);
//
//	public static final String SEPARATOR = "/";
//
//	private ZooKeeper zookeeper;
//
//	private List<ACL> acl = new ArrayList<ACL>();
//
//	private ZkConfig zkConfig;
//
//	private boolean repeatRegisterWatcher = true;
//
//	public ZKManager() {
//
//	}
//
//	public void init() {
//		try {
//			this.connect();
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//		}
//
//	}
//
//	public void close() {
//		try {
//			if (null != this.zookeeper) {
//				this.zookeeper.close();
//				this.zookeeper = null;
//			}
//		} catch (InterruptedException e) {
//			logger.error(e.getMessage(), e);
//		}
//	}
//
//	private void connect() throws Exception {
//		CountDownLatch connectionLatch = new CountDownLatch(1);
//		createZooKeeper(connectionLatch);
//		connectionLatch.await();
//	}
//
//	public void reConnection() throws Exception {
//		if (this.zookeeper != null) {
//			this.zookeeper.close();
//			this.setZookeeper(null);
//		}
//		this.connect();
//		if (isRepeatRegisterWatcher()) {
//			// clientManager.repeat_regist_local_job_group_name_all_watchers();
//		}
//	}
//
//	private void sessionEvent(CountDownLatch connectionLatch, WatchedEvent event) {
//		if (event.getState() == KeeperState.SyncConnected) {
//			logger.info("收到ZK连接成功事件！");
//			connectionLatch.countDown();
//		} else if (event.getState() == KeeperState.Expired) {
//			logger.error("会话超时，等待重新建立ZK连接...");
//			try {
//				reConnection();
//			} catch (Exception e) {
//				logger.error(e.getMessage(), e);
//			}
//		} // Disconnected：Zookeeper会自动处理Disconnected状态重连
//	}
//
//	public void createZooKeeper(final CountDownLatch connectionLatch) throws Exception {
//		if (zookeeper == null) {
//			String authString = zkConfig.getUserName().trim() + ":" + zkConfig.getPassWord().trim();
//			zookeeper = new ZooKeeper(zkConfig.getConnectString().trim(), zkConfig.getSessionTimeout(), new Watcher() {
//				@Override
//				public void process(WatchedEvent event) {
//					logger.info("event ==========" + event.getType());
//					sessionEvent(connectionLatch, event);
//				}
//			});
//			String charset = zkConfig.getCharset();
//			if (charset == null) {
//				zookeeper.addAuthInfo("digest", authString.getBytes());
//			} else {
//				zookeeper.addAuthInfo("digest", authString.getBytes(charset));
//			}
//			String generateDigest = DigestAuthenticationProvider.generateDigest(authString);
//			acl.clear();
//			acl.add(new ACL(ZooDefs.Perms.ALL, new Id("digest", generateDigest)));
//			acl.add(new ACL(ZooDefs.Perms.READ, Ids.ANYONE_ID_UNSAFE));
//		}
//	}
//
//	private void setZookeeper(ZooKeeper zk) {
//		this.zookeeper = zk;
//	}
//
//	public ZooKeeper getZookeeper() {
//		try {
//			if (this.checkZookeeperState() == false) {
//				reConnection();
//			}
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//		}
//		return zookeeper;
//	}
//
//	public boolean checkZookeeperState() {
//		return zookeeper != null && zookeeper.getState() == States.CONNECTED;
//	}
//
//	public List<ACL> getAcl() {
//		return acl;
//	}
//
//	public void setAcl(List<ACL> acl) {
//		this.acl = acl;
//	}
//
//	public ZkConfig getZkConfig() {
//		return zkConfig;
//	}
//
//	public void setZkConfig(ZkConfig zkConfig) {
//		this.zkConfig = zkConfig;
//	}
//
//	public boolean isRepeatRegisterWatcher() {
//		return repeatRegisterWatcher;
//	}
//
//	public void setRepeatRegisterWatcher(boolean repeatRegisterWatcher) {
//		this.repeatRegisterWatcher = repeatRegisterWatcher;
//	}
//
//	@Override
//	public void destroy() throws Exception {
//		this.close();
//	}
//
//}
