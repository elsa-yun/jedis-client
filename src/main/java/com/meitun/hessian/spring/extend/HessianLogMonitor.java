package com.elsa.hessian.spring.extend;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.PriorityOrdered;

import com.elsa.hessian.spring.dto.HessianDTO;
import com.elsa.redis.util.JedisCacheUtil;

/**
 * @author longhaisheng
 *
 */
public class HessianLogMonitor implements DisposableBean, BeanFactoryPostProcessor, PriorityOrdered {

	private static final Log logger = LogFactory.getLog(HessianLogMonitor.class);

	private static final int INITIAL_DELAY = 30;

	private static final int SCHEDULE_DELAY = 20 * 60;

	private static volatile boolean NEED_RUN = true;

	private static volatile boolean WRITE_OTHER = false;

	private JedisCacheUtil jedisCacheUtil;

	private ScheduledExecutorService scheduledExecutorService;

	public void watcher() {
		if (logger.isInfoEnabled()) {
			logger.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ write hessian log to redis scheduler start @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		}
		scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
			public void run() {
				if (NEED_RUN) {
					logger.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@RUN@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
					if (WRITE_OTHER) {
						Map<String, AtomicLong> interfaceMethodMapTimes = MeitunHessianProxyFactoryBean.getInterfaceMethodMapTimes();//
						Map<String, Long> interfaceMethodMapMaxSchedule = MeitunHessianProxyFactoryBean.getInterfaceMethodMapMaxSchedule();//
						Map<String, Long> interfaceMethodMapMinSchedule = MeitunHessianProxyFactoryBean.getInterfaceMethodMapMinSchedule();//
						Map<String, AtomicLong> interfaceMethodMapTotalSchedule = MeitunHessianProxyFactoryBean.getInterfaceMethodMapTotalSchedule();//
						for (Map.Entry<String, AtomicLong> ent : interfaceMethodMapTimes.entrySet()) {
							if (null != ent.getValue()) {
								String key = "hessian." + ent.getKey();
								String cacheString = jedisCacheUtil.getCacheString(key);
								long cache_value = 0;
								if (null != cacheString) {
									cache_value = Long.valueOf(cacheString);
								}
								long new_value = ent.getValue().get() + cache_value;
								jedisCacheUtil.setCacheString(key, new_value + "");
							}
						}
						for (Map.Entry<String, AtomicLong> ent : interfaceMethodMapTotalSchedule.entrySet()) {
							if (null != ent.getValue()) {
								String key = "hessian." + ent.getKey();
								String cacheString = jedisCacheUtil.getCacheString(key);
								long cache_value = 0;
								if (null != cacheString) {
									cache_value = Long.valueOf(cacheString);
								}
								long new_value = ent.getValue().get() + cache_value;
								jedisCacheUtil.setCacheString(key, new_value + "");
							}
						}
						for (Map.Entry<String, Long> ent : interfaceMethodMapMaxSchedule.entrySet()) {
							if (null != ent.getValue()) {
								jedisCacheUtil.setCacheString("hessian." + ent.getKey(), ent.getValue() + "");
							}
						}
						for (Map.Entry<String, Long> ent : interfaceMethodMapMinSchedule.entrySet()) {
							if (null != ent.getValue()) {
								jedisCacheUtil.setCacheString("hessian." + ent.getKey(), ent.getValue() + "");
							}
						}
					}

					Map<String, HessianDTO> getInterfaceMethodMap = MeitunHessianProxyFactoryBean.get_interface_method_map();
					for (Map.Entry<String, HessianDTO> ent : getInterfaceMethodMap.entrySet()) {
						jedisCacheUtil.setCache("hessian." + ent.getKey(), ent.getValue());
					}
				} else {
					return;
				}
			}
		}, INITIAL_DELAY, SCHEDULE_DELAY, TimeUnit.SECONDS);
	}

	@Override
	public int getOrder() {
		return LOWEST_PRECEDENCE - 11;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		try {
			jedisCacheUtil = beanFactory.getBean(com.elsa.redis.util.JedisCacheUtil.class);
			if (null != jedisCacheUtil) {
				scheduledExecutorService = Executors.newScheduledThreadPool(1);
				this.watcher();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void destroy() throws Exception {
		if (null != scheduledExecutorService) {
			NEED_RUN = false;
			scheduledExecutorService.shutdown();
			if (logger.isInfoEnabled()) {
				logger.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ write hessian log to redis scheduler shutdown @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			}
		}
	}
}
