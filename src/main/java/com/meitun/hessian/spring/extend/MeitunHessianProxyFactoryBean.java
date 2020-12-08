package com.elsa.hessian.spring.extend;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.remoting.caucho.HessianClientInterceptor;
import org.springframework.remoting.caucho.HessianProxyFactoryBean;
import org.springframework.util.ReflectionUtils;

import com.caucho.hessian.client.HessianProxy;
import com.elsa.hessian.spring.dto.HessianDTO;
import com.elsa.redis.util.IPUtils;

/**
 * @author longhaisheng
 *
 */
public class MeitunHessianProxyFactoryBean extends HessianProxyFactoryBean {

	public static final Log logger = LogFactory.getLog(MeitunHessianProxyFactoryBean.class);

	public static final String TOTAL_TIMES = "total_times";

	public static final String TOTAL_SCHEDULE = "total_schedule";

	public static final String MAX_SCHEDULE = "max_schedule";

	public static final String MIN_SCHEDULE = "min_schedule";

	public static final String SPLIT_STR = "@@@@";

	private static final String H = "h";

	private static final String INTERFACE = "interface";

	private static final String _TYPE = "_type";

	private static final String HESSIAN_PROXY = "hessianProxy";

	private static final int LOG_METHOD_MAX_MILLISECONDS = 500;

	private static final int lOG_METHOD_MIN_MILLISECONDS = 2;

	private final static ConcurrentMap<String, HessianDTO> INTERFACE_METHOD_MAP = new ConcurrentHashMap<String, HessianDTO>(512);

	private final static ConcurrentMap<String, AtomicLong> INTERFACE_METHOD_MAP_COUNT = new ConcurrentHashMap<String, AtomicLong>(512);

	private final static ConcurrentMap<String, AtomicLong> INTERFACE_METHOD_MAP_TIMES = new ConcurrentHashMap<String, AtomicLong>(512);

	private final static ConcurrentMap<String, AtomicLong> INTERFACE_METHOD_MAP_TOTAL_SCHEDULE = new ConcurrentHashMap<String, AtomicLong>(512);

	private final static Map<String, Long> INTERFACE_METHOD_MAP_MAX_SCHEDULE = new HashMap<String, Long>(512);

	private final static Map<String, Long> INTERFACE_METHOD_MAP_MIN_SCHEDULE = new HashMap<String, Long>(512);

	private int maxMethodTime = LOG_METHOD_MAX_MILLISECONDS;

	private boolean printLog = false;

	private boolean needWrite = false;

	public Object invoke(MethodInvocation invocation) throws Throwable {
		long start = System.currentTimeMillis();
		Object o = super.invoke(invocation);
		long end = System.currentTimeMillis();

		Method method = invocation.getMethod();
		String methodName = method.getName();
		Object[] arguments = invocation.getArguments();
		String interface_name = null;
		URL remotingHost = null;
		try {
			Field proxy = ReflectionUtils.findField(HessianClientInterceptor.class, HESSIAN_PROXY);
			if (null != proxy) {
				proxy.setAccessible(true);
				Object field = ReflectionUtils.getField(proxy, this);

				HessianProxy jdkDynamicProxyTargetObject = getJdkDynamicProxyTargetObjectNew(field);

				Field type_field = ReflectionUtils.findField(jdkDynamicProxyTargetObject.getClass(), _TYPE);
				type_field.setAccessible(true);
				Object targetInterface = ReflectionUtils.getField(type_field, jdkDynamicProxyTargetObject);
				remotingHost = jdkDynamicProxyTargetObject.getURL();

				if (null != targetInterface) {
					interface_name = targetInterface.toString().replace(INTERFACE, "");
					interface_name = interface_name.trim();
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		if (null != interface_name && null != remotingHost) {
			if (maxMethodTime < lOG_METHOD_MIN_MILLISECONDS) {
				maxMethodTime = lOG_METHOD_MIN_MILLISECONDS;
			}
			long onece_schedule = end - start;
			String customerIp = IPUtils.getLocalIP();
			if (onece_schedule > LOG_METHOD_MAX_MILLISECONDS) {
				if (INTERFACE_METHOD_MAP.size() < 12000) {
					String key = interface_name + SPLIT_STR + methodName + SPLIT_STR + customerIp + SPLIT_STR + arguments.length;
					HessianDTO hdo = INTERFACE_METHOD_MAP.get(key);
					if (null == hdo) {
						hdo = new HessianDTO();
						INTERFACE_METHOD_MAP.putIfAbsent(key, hdo);
						hdo.setInterfaceName(interface_name);
						hdo.setServerHost(remotingHost.toString());
						hdo.setMethodName(methodName);
						hdo.setMethodParamsCount(arguments.length);
						hdo.setCustomerIp(customerIp);
						if (start != 0) {
							hdo.setStartSchedule(start);
						}
					}

					hdo.setTotalSchedule(hdo.getTotalSchedule() + onece_schedule);
					hdo.setCallTotalTimes(hdo.getCallTotalTimes() + 1);
					hdo.setEndSchedule(end);
					if (onece_schedule > hdo.getMaxSchedule()) {
						hdo.setMaxSchedule(onece_schedule);
					}
					if (hdo.getMinSchedule() == 0) {
						hdo.setMinSchedule(onece_schedule);
					} else {
						if (onece_schedule < hdo.getMinSchedule()) {
							hdo.setMinSchedule(onece_schedule);
						}
					}

					long methodAvgSchedule = divide(hdo.getTotalSchedule(), hdo.getCallTotalTimes()).longValue();
					hdo.setMethodAvgSchedule(methodAvgSchedule);

					if (needWrite) {
						String times_key = get_total_time_key(key);
						String schedule_key = get_schedule_key(key);
						String max_schedule_key = get_max_schedule_key(key);
						String min_schedule_key = get_min_schedule_key(key);
						AtomicLong timesAtomic = INTERFACE_METHOD_MAP_TIMES.get(times_key);
						if (null == timesAtomic) {
							timesAtomic = new AtomicLong(1);
						} else {
							timesAtomic.incrementAndGet();
						}
						INTERFACE_METHOD_MAP_TIMES.put(times_key, timesAtomic);

						AtomicLong scheduleAtomic = INTERFACE_METHOD_MAP_TOTAL_SCHEDULE.get(schedule_key);
						if (null == scheduleAtomic) {
							scheduleAtomic = new AtomicLong(0);
						} else {
							scheduleAtomic.addAndGet(scheduleAtomic.get());
						}
						INTERFACE_METHOD_MAP_TOTAL_SCHEDULE.put(schedule_key, scheduleAtomic);

						Long max_schedule = INTERFACE_METHOD_MAP_MAX_SCHEDULE.get(max_schedule_key);
						if (null == max_schedule) {
							INTERFACE_METHOD_MAP_MAX_SCHEDULE.put(max_schedule_key, onece_schedule);
						} else {
							if (onece_schedule > max_schedule) {
								INTERFACE_METHOD_MAP_MAX_SCHEDULE.put(max_schedule_key, onece_schedule);
							}
						}

						Long min_schedule = INTERFACE_METHOD_MAP_MIN_SCHEDULE.get(min_schedule_key);
						if (null == min_schedule) {
							INTERFACE_METHOD_MAP_MIN_SCHEDULE.put(min_schedule_key, onece_schedule);
						} else {
							if (onece_schedule < min_schedule) {
								INTERFACE_METHOD_MAP_MIN_SCHEDULE.put(min_schedule_key, onece_schedule);
							}
						}
					}
				}
			}
			if (isPrintLog() && onece_schedule > maxMethodTime) {
				StringBuilder sb = new StringBuilder();
				sb.append("interface>>>");
				sb.append(interface_name);
				sb.append("==method>>>");
				sb.append(methodName);
				sb.append("==args>>>");
				sb.append(arguments.toString());
				sb.append("==stime>>>");
				sb.append(start);
				sb.append("==etime>>>");
				sb.append(end);
				sb.append("==tTime>>>");
				sb.append(onece_schedule);
				sb.append("==url>>>");
				sb.append(remotingHost);
				sb.append("==ip>>>");
				sb.append(customerIp);
				logger.info(sb.toString());
			}
		}

		return o;
	}

	private static BigDecimal divide(long num, long dividend) {
		BigDecimal b1 = new BigDecimal(num);
		BigDecimal b2 = new BigDecimal(dividend);
		return b1.divide(b2, 2, BigDecimal.ROUND_HALF_EVEN);
	}

	public static String get_min_schedule_key(String key) {
		return key + SPLIT_STR + MIN_SCHEDULE;
	}

	public static String get_max_schedule_key(String key) {
		return key + SPLIT_STR + MAX_SCHEDULE;
	}

	public static String get_schedule_key(String key) {
		return key + SPLIT_STR + TOTAL_SCHEDULE;
	}

	public static String get_total_time_key(String key) {
		return key + SPLIT_STR + TOTAL_TIMES;
	}

	public static Map<String, HessianDTO> get_interface_method_map() {
		return Collections.unmodifiableMap(INTERFACE_METHOD_MAP);
	}

	public static Map<String, AtomicLong> getInterfaceMethodMapCount() {
		return Collections.unmodifiableMap(INTERFACE_METHOD_MAP_COUNT);
	}

	public static Map<String, AtomicLong> getInterfaceMethodMapTimes() {
		return Collections.unmodifiableMap(INTERFACE_METHOD_MAP_TIMES);
	}

	public static Map<String, AtomicLong> getInterfaceMethodMapTotalSchedule() {
		return Collections.unmodifiableMap(INTERFACE_METHOD_MAP_TOTAL_SCHEDULE);
	}

	public static Map<String, Long> getInterfaceMethodMapMaxSchedule() {
		return Collections.unmodifiableMap(INTERFACE_METHOD_MAP_MAX_SCHEDULE);
	}

	public static Map<String, Long> getInterfaceMethodMapMinSchedule() {
		return Collections.unmodifiableMap(INTERFACE_METHOD_MAP_MIN_SCHEDULE);
	}

	public static Map<String, AtomicLong> get_interface_method_map_count() {
		return Collections.unmodifiableMap(INTERFACE_METHOD_MAP_COUNT);
	}

	private static HessianProxy getJdkDynamicProxyTargetObjectNew(Object proxy) throws Exception {
		Field h = proxy.getClass().getSuperclass().getDeclaredField(H);
		h.setAccessible(true);
		return (HessianProxy) h.get(proxy);
	}

	public int getMaxMethodTime() {
		return maxMethodTime;
	}

	public void setMaxMethodTime(int maxMethodTime) {
		this.maxMethodTime = maxMethodTime;
	}

	public boolean isPrintLog() {
		return printLog;
	}

	public void setPrintLog(boolean printLog) {
		this.printLog = printLog;
	}

	public boolean isNeedWrite() {
		return needWrite;
	}

	public void setNeedWrite(boolean needWrite) {
		this.needWrite = needWrite;
	}

}
// private static Object getCglibProxyTargetObjectnNew(Object proxy) throws
// Exception {
// Field h = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
// h.setAccessible(true);
// return h.get(proxy);
// }
// private static Object getCglibProxyTargetObject(Object proxy) throws
// Exception {
// Field h = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
// h.setAccessible(true);
// Object dynamicAdvisedInterceptor = h.get(proxy);
// Field advised =
// dynamicAdvisedInterceptor.getClass().getDeclaredField("advised");
// advised.setAccessible(true);
//
// return
// ((AdvisedSupport)advised.get(dynamicAdvisedInterceptor)).getTargetSource().getTarget();
// }
//
//
// private static Object getJdkDynamicProxyTargetObject(Object proxy) throws
// Exception {
// Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
// h.setAccessible(true);
// AopProxy aopProxy = (AopProxy) h.get(proxy);
// Field advised = aopProxy.getClass().getDeclaredField("advised");
// advised.setAccessible(true);
//
// return
// ((AdvisedSupport)advised.get(aopProxy)).getTargetSource().getTarget();
// }
//
// private static Object getTarget(Object proxy) throws Exception {
//
// if (!AopUtils.isAopProxy(proxy)) {
// return proxy;
// }
//
// if (AopUtils.isJdkDynamicProxy(proxy)) {
// return getJdkDynamicProxyTargetObject(proxy);
// } else {
// return getCglibProxyTargetObject(proxy);
// }
// }

