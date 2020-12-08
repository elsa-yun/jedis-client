package com.elsa.redis.test;

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import redis.clients.jedis.JedisShardInfo;

public class Bean3  implements BeanFactoryPostProcessor{
	public void t() {
		System.out.println("Bean3");
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		Map<String, Bean1> jedisShardInfoMap= beanFactory.getBeansOfType(Bean1.class);	
		for (Map.Entry<String, Bean1> entry : jedisShardInfoMap.entrySet()) {
			entry.getValue().t();
		}
	}

}
