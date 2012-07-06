/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ClusterDumpContributor.java 2012-7-6 14:30:46 l.xue.nong$$
 */

package cn.com.rebirth.search.core.monitor.dump.cluster;

import java.io.PrintWriter;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodes;
import cn.com.rebirth.search.core.cluster.routing.RoutingTable;
import cn.com.rebirth.search.core.monitor.dump.Dump;
import cn.com.rebirth.search.core.monitor.dump.DumpContributionFailedException;
import cn.com.rebirth.search.core.monitor.dump.DumpContributor;

/**
 * The Class ClusterDumpContributor.
 *
 * @author l.xue.nong
 */
public class ClusterDumpContributor implements DumpContributor {

	/** The Constant CLUSTER. */
	public static final String CLUSTER = "cluster";

	/** The name. */
	private final String name;

	/** The cluster service. */
	private final ClusterService clusterService;

	/**
	 * Instantiates a new cluster dump contributor.
	 *
	 * @param clusterService the cluster service
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public ClusterDumpContributor(ClusterService clusterService, @Assisted String name, @Assisted Settings settings) {
		this.clusterService = clusterService;
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.monitor.dump.DumpContributor#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.monitor.dump.DumpContributor#contribute(cn.com.rebirth.search.core.monitor.dump.Dump)
	 */
	@Override
	public void contribute(Dump dump) throws DumpContributionFailedException {
		ClusterState clusterState = clusterService.state();
		DiscoveryNodes nodes = clusterState.nodes();
		RoutingTable routingTable = clusterState.routingTable();

		PrintWriter writer = new PrintWriter(dump.createFileWriter("cluster.txt"));

		writer.println("===== CLUSTER NODES ======");
		writer.print(nodes.prettyPrint());

		writer.println("===== ROUTING TABLE ======");
		writer.print(routingTable.prettyPrint());

		writer.close();
	}
}
