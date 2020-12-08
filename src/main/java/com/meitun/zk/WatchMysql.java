//package com.elsa.zk;
//
//import java.lang.reflect.Field;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.springframework.beans.BeansException;
//import org.springframework.beans.factory.DisposableBean;
//import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
//import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
//import org.springframework.core.PriorityOrdered;
//import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
//
//public class WatchMysql implements DisposableBean, BeanFactoryPostProcessor, PriorityOrdered {
//
//	private Map<String, AbstractRoutingDataSource> dataSourceMap = new HashMap<String, AbstractRoutingDataSource>();
//	
//	private Map<String, AbstractRoutingDataSource> defaultMap = new HashMap<String, AbstractRoutingDataSource>();
//
//	@Override
//	public int getOrder() {
//		return LOWEST_PRECEDENCE - 12;
//	}
//
//	@Override
//	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
//		dataSourceMap = beanFactory.getBeansOfType(org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource.class);
//		defaultMap.putAll(dataSourceMap);
//		for (Map.Entry<String, AbstractRoutingDataSource> entry : dataSourceMap.entrySet()) {
//
//		}
//	}
//
//	@Override
//	public void destroy() throws Exception {
//
//	}
//
//	public static HashMap<String, Class> init(PersonDO cls) {
//		HashMap<String, Class> fieldHashMap = new HashMap<String, Class>();
//		Field[] fieldlist = cls.getClass().getDeclaredFields();
//		for (int i = 0; i < fieldlist.length; i++) {
//			Field f = fieldlist[i];
//			f.setAccessible(true);
//			String type = f.getType().toString();
//			String name = f.getName();
//			fieldHashMap.put(name, f.getType());
//			System.out.println("name = " + name);
//			System.out.println("decl class = " + f.getDeclaringClass());
//			System.out.println("type = " + type);
//			System.out.println("-----");
//			if (name.equals("name")) {
//				try {
//					f.set(cls, "long");
//				} catch (IllegalArgumentException e) {
//					e.printStackTrace();
//				} catch (IllegalAccessException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//		return fieldHashMap;
//	}
//
//	public static void main(String args[]) {
//		com.elsa.zk.PersonDO p = new com.elsa.zk.PersonDO();
//		init(p);
//		System.out.println(p.getName());
//	}
//
//}
