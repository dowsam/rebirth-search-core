/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core JmxClusterService.java 2012-7-6 14:30:43 l.xue.nong$$
 */

package cn.com.rebirth.search.core.jmx;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

import java.util.concurrent.ExecutorService;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import cn.com.rebirth.commons.concurrent.EsExecutors;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.core.cluster.ClusterChangedEvent;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterStateListener;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.jmx.action.GetJmxServiceUrlAction;

/**
 * The Class JmxClusterService.
 *
 * @author l.xue.nong
 */
public class JmxClusterService extends AbstractComponent {

	/** The cluster service. */
	private final ClusterService clusterService;

	/** The jmx service. */
	private final JmxService jmxService;

	/** The get jmx service url action. */
	private final GetJmxServiceUrlAction getJmxServiceUrlAction;

	/** The cluster nodes jmx updater. */
	private final ExecutorService clusterNodesJmxUpdater;

	/**
	 * Instantiates a new jmx cluster service.
	 *
	 * @param settings the settings
	 * @param clusterService the cluster service
	 * @param jmxService the jmx service
	 * @param getJmxServiceUrlAction the get jmx service url action
	 */
	public JmxClusterService(Settings settings, ClusterService clusterService, JmxService jmxService,
			final GetJmxServiceUrlAction getJmxServiceUrlAction) {
		super(settings);
		this.clusterService = clusterService;
		this.jmxService = jmxService;
		this.getJmxServiceUrlAction = getJmxServiceUrlAction;

		this.clusterNodesJmxUpdater = newSingleThreadExecutor(EsExecutors.daemonThreadFactory(settings,
				"jmxService#updateTask"));

		if (jmxService.publishUrl() != null) {
			clusterService.add(new JmxClusterEventListener());
			for (final DiscoveryNode node : clusterService.state().nodes()) {
				clusterNodesJmxUpdater.execute(new Runnable() {
					@Override
					public void run() {
						String nodeServiceUrl = getJmxServiceUrlAction.obtainPublishUrl(node);
						registerNode(node, nodeServiceUrl);
					}
				});
			}
		}
	}

	/**
	 * Close.
	 */
	public void close() {
		if (clusterNodesJmxUpdater != null) {
			clusterNodesJmxUpdater.shutdownNow();
		}
	}

	/**
	 * Register node.
	 *
	 * @param node the node
	 * @param nodeServiceUrl the node service url
	 */
	private void registerNode(DiscoveryNode node, String nodeServiceUrl) {
		try {
			JMXServiceURL jmxServiceURL = new JMXServiceURL(nodeServiceUrl);
			JMXConnector jmxConnector = JMXConnectorFactory.connect(jmxServiceURL, null);

			MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();

		} catch (Exception e) {
			logger.warn("Failed to register node [" + node + "] with serviceUrl [" + nodeServiceUrl + "]", e);
		}
	}

	/**
	 * The listener interface for receiving jmxClusterEvent events.
	 * The class that is interested in processing a jmxClusterEvent
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addJmxClusterEventListener<code> method. When
	 * the jmxClusterEvent event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see JmxClusterEventEvent
	 */
	private class JmxClusterEventListener implements ClusterStateListener {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.cluster.ClusterStateListener#clusterChanged(cn.com.rebirth.search.core.cluster.ClusterChangedEvent)
		 */
		@Override
		public void clusterChanged(ClusterChangedEvent event) {
			if (!event.nodesChanged()) {
				return;
			}
			for (final DiscoveryNode node : event.nodesDelta().addedNodes()) {
				clusterNodesJmxUpdater.execute(new Runnable() {
					@Override
					public void run() {
						String nodeServiceUrl = getJmxServiceUrlAction.obtainPublishUrl(node);
						registerNode(node, nodeServiceUrl);
					}
				});
			}
		}
	}
}
