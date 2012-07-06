/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core HashFunction.java 2012-7-6 14:30:32 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.routing.operation.hash;

/**
 * The Interface HashFunction.
 *
 * @author l.xue.nong
 */
public interface HashFunction {

	/**
	 * Hash.
	 *
	 * @param routing the routing
	 * @return the int
	 */
	int hash(String routing);

	/**
	 * Hash.
	 *
	 * @param type the type
	 * @param id the id
	 * @return the int
	 */
	int hash(String type, String id);
}
