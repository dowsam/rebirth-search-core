/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SimpleHashFunction.java 2012-7-6 14:29:02 l.xue.nong$$
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
	 * @see cn.com.rebirth.search.core.cluster.routing.operation.hash.HashFunction#hash(java.lang.String)
	 */
	@Override
	public int hash(String routing) {
		return routing.hashCode();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.operation.hash.HashFunction#hash(java.lang.String, java.lang.String)
	 */
	@Override
	public int hash(String type, String id) {
		return type.hashCode() + 31 * id.hashCode();
	}
}
