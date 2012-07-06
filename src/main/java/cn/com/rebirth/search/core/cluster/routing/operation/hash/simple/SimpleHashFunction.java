/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SimpleHashFunction.java 2012-3-29 15:01:37 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster.routing.operation.hash.simple;

import cn.com.rebirth.search.core.cluster.routing.operation.hash.HashFunction;


/**
 * The Class SimpleHashFunction.
 *
 * @author l.xue.nong
 */
public class SimpleHashFunction implements HashFunction {

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.operation.hash.HashFunction#hash(java.lang.String)
	 */
	@Override
	public int hash(String routing) {
		return routing.hashCode();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.operation.hash.HashFunction#hash(java.lang.String, java.lang.String)
	 */
	@Override
	public int hash(String type, String id) {
		return type.hashCode() + 31 * id.hashCode();
	}
}
