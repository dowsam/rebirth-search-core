/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ElectMasterService.java 2012-7-6 14:29:55 l.xue.nong$$
 */

package cn.com.rebirth.search.core.discovery.zen.elect;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import cn.com.rebirth.commons.component.AbstractComponent;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.core.cluster.metadata.MetaData;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.node.settings.NodeSettingsService;

import com.google.common.collect.Lists;

/**
 * The Class ElectMasterService.
 *
 * @author l.xue.nong
 */
public class ElectMasterService extends AbstractComponent {

	static {
		MetaData.addDynamicSettings("discovery.zen.minimum_master_nodes");
	}

	/** The node comparator. */
	private final NodeComparator nodeComparator = new NodeComparator();

	/** The minimum master nodes. */
	private volatile int minimumMasterNodes;

	/**
	 * Instantiates a new elect master service.
	 *
	 * @param settings the settings
	 * @param nodeSettingsService the node settings service
	 */
	public ElectMasterService(Settings settings, NodeSettingsService nodeSettingsService) {
		super(settings);
		this.minimumMasterNodes = settings.getAsInt("discovery.zen.minimum_master_nodes", -1);
		logger.debug("using minimum_master_nodes [{}]", minimumMasterNodes);
		nodeSettingsService.addListener(new ApplySettings());
	}

	/**
	 * Checks for enough master nodes.
	 *
	 * @param nodes the nodes
	 * @return true, if successful
	 */
	public boolean hasEnoughMasterNodes(Iterable<DiscoveryNode> nodes) {
		if (minimumMasterNodes < 1) {
			return true;
		}
		int count = 0;
		for (DiscoveryNode node : nodes) {
			if (node.masterNode()) {
				count++;
			}
		}
		return count >= minimumMasterNodes;
	}

	/**
	 * Next possible masters.
	 *
	 * @param nodes the nodes
	 * @param numberOfPossibleMasters the number of possible masters
	 * @return the discovery node[]
	 */
	public DiscoveryNode[] nextPossibleMasters(Iterable<DiscoveryNode> nodes, int numberOfPossibleMasters) {
		List<DiscoveryNode> sortedNodes = sortedMasterNodes(nodes);
		if (sortedNodes == null) {
			return new DiscoveryNode[0];
		}
		List<DiscoveryNode> nextPossibleMasters = Lists.newArrayListWithCapacity(numberOfPossibleMasters);
		int counter = 0;
		for (DiscoveryNode nextPossibleMaster : sortedNodes) {
			if (++counter >= numberOfPossibleMasters) {
				break;
			}
			nextPossibleMasters.add(nextPossibleMaster);
		}
		return nextPossibleMasters.toArray(new DiscoveryNode[nextPossibleMasters.size()]);
	}

	/**
	 * Elect master.
	 *
	 * @param nodes the nodes
	 * @return the discovery node
	 */
	public DiscoveryNode electMaster(Iterable<DiscoveryNode> nodes) {
		List<DiscoveryNode> sortedNodes = sortedMasterNodes(nodes);
		if (sortedNodes == null || sortedNodes.isEmpty()) {
			return null;
		}
		return sortedNodes.get(0);
	}

	/**
	 * Sorted master nodes.
	 *
	 * @param nodes the nodes
	 * @return the list
	 */
	private List<DiscoveryNode> sortedMasterNodes(Iterable<DiscoveryNode> nodes) {
		List<DiscoveryNode> possibleNodes = Lists.newArrayList(nodes);
		if (possibleNodes.isEmpty()) {
			return null;
		}

		for (Iterator<DiscoveryNode> it = possibleNodes.iterator(); it.hasNext();) {
			DiscoveryNode node = it.next();
			if (!node.masterNode()) {
				it.remove();
			}
		}
		Collections.sort(possibleNodes, nodeComparator);
		return possibleNodes;
	}

	/**
	 * The Class ApplySettings.
	 *
	 * @author l.xue.nong
	 */
	class ApplySettings implements NodeSettingsService.Listener {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.node.settings.NodeSettingsService.Listener#onRefreshSettings(cn.com.rebirth.commons.settings.Settings)
		 */
		@Override
		public void onRefreshSettings(Settings settings) {
			int minimumMasterNodes = settings.getAsInt("discovery.zen.minimum_master_nodes",
					ElectMasterService.this.minimumMasterNodes);
			if (minimumMasterNodes != ElectMasterService.this.minimumMasterNodes) {
				logger.info("updating [discovery.zen.minimum_master_nodes] from [{}] to [{}]",
						ElectMasterService.this.minimumMasterNodes, minimumMasterNodes);
				ElectMasterService.this.minimumMasterNodes = minimumMasterNodes;
			}
		}
	}

	/**
	 * The Class NodeComparator.
	 *
	 * @author l.xue.nong
	 */
	private static class NodeComparator implements Comparator<DiscoveryNode> {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(DiscoveryNode o1, DiscoveryNode o2) {
			return o1.id().compareTo(o2.id());
		}
	}
}
