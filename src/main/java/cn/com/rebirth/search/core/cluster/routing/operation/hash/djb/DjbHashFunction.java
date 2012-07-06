/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DjbHashFunction.java 2012-7-6 14:29:59 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.routing.operation.hash.djb;

import cn.com.rebirth.search.core.cluster.routing.operation.hash.HashFunction;

/**
 * The Class DjbHashFunction.
 *
 * @author l.xue.nong
 */
public class DjbHashFunction implements HashFunction {

	/**
	 * Djb hash.
	 *
	 * @param value the value
	 * @return the int
	 */
	public static int DJB_HASH(String value) {
		long hash = 5381;

		for (int i = 0; i < value.length(); i++) {
			hash = ((hash << 5) + hash) + value.charAt(i);
		}

		return (int) hash;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.operation.hash.HashFunction#hash(java.lang.String)
	 */
	@Override
	public int hash(String routing) {
		return DJB_HASH(routing);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.operation.hash.HashFunction#hash(java.lang.String, java.lang.String)
	 */
	@Override
	public int hash(String type, String id) {
		long hash = 5381;

		for (int i = 0; i < type.length(); i++) {
			hash = ((hash << 5) + hash) + type.charAt(i);
		}

		for (int i = 0; i < id.length(); i++) {
			hash = ((hash << 5) + hash) + id.charAt(i);
		}

		return (int) hash;
	}
}
