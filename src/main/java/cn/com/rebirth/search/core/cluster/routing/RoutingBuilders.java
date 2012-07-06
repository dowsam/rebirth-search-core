/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RoutingBuilders.java 2012-7-6 14:30:28 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.routing;

/**
 * The Class RoutingBuilders.
 *
 * @author l.xue.nong
 */
public final class RoutingBuilders {

	/**
	 * Instantiates a new routing builders.
	 */
	private RoutingBuilders() {

	}

	/**
	 * Routing table.
	 *
	 * @return the routing table. builder
	 */
	public static RoutingTable.Builder routingTable() {
		return new RoutingTable.Builder();
	}

	/**
	 * Index routing table.
	 *
	 * @param index the index
	 * @return the index routing table. builder
	 */
	public static IndexRoutingTable.Builder indexRoutingTable(String index) {
		return new IndexRoutingTable.Builder(index);
	}
}
