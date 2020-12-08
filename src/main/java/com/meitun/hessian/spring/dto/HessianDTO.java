package com.elsa.hessian.spring.dto;

import java.io.Serializable;

/**
 * @author longhaisheng
 *
 */
public class HessianDTO implements Serializable {
	
	private static final long serialVersionUID = 5443784265524500824L;

	private String interfaceName;

	private String methodName;

	private String customerIp;
	
	private String serverHost;

	private long methodAvgSchedule;

	private long methodParamsCount;

	private long maxSchedule;

	private long minSchedule;

	private long totalSchedule;

	private long callTotalTimes;

	private long startSchedule;

	private long endSchedule;

	public String getInterfaceName() {
		return interfaceName;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getCustomerIp() {
		return customerIp;
	}

	public void setCustomerIp(String customerIp) {
		this.customerIp = customerIp;
	}

	public long getMethodAvgSchedule() {
		return methodAvgSchedule;
	}

	public void setMethodAvgSchedule(long methodAvgSchedule) {
		this.methodAvgSchedule = methodAvgSchedule;
	}

	public long getMethodParamsCount() {
		return methodParamsCount;
	}

	public void setMethodParamsCount(long methodParamsCount) {
		this.methodParamsCount = methodParamsCount;
	}

	public String getServerHost() {
		return serverHost;
	}

	public void setServerHost(String serverHost) {
		this.serverHost = serverHost;
	}

	public long getMaxSchedule() {
		return maxSchedule;
	}

	public void setMaxSchedule(long maxSchedule) {
		this.maxSchedule = maxSchedule;
	}

	public long getMinSchedule() {
		return minSchedule;
	}

	public void setMinSchedule(long minSchedule) {
		this.minSchedule = minSchedule;
	}

	public long getTotalSchedule() {
		return totalSchedule;
	}

	public void setTotalSchedule(long totalSchedule) {
		this.totalSchedule = totalSchedule;
	}

	public long getCallTotalTimes() {
		return callTotalTimes;
	}

	public void setCallTotalTimes(long callTotalTimes) {
		this.callTotalTimes = callTotalTimes;
	}

	public long getStartSchedule() {
		return startSchedule;
	}

	public void setStartSchedule(long startSchedule) {
		this.startSchedule = startSchedule;
	}

	public long getEndSchedule() {
		return endSchedule;
	}

	public void setEndSchedule(long endSchedule) {
		this.endSchedule = endSchedule;
	}

}