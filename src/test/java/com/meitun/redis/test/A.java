package com.elsa.redis.test;

import org.springframework.beans.factory.DisposableBean;

public abstract class A implements DisposableBean{
	
	public abstract String get();
	
	@Override
	public void destroy() throws Exception {
		System.out.println(get());
	}
}
