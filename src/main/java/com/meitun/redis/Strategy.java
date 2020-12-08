package com.elsa.redis;

public interface Strategy {
	
	String getJedisPoolKey(long fieldNum);

}
