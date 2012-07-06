/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NodeService.java 2012-3-29 16:18:56 l.xue.nong$$
 */

package cn.com.rebirth.search.core.node.service;

import java.net.InetAddress;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.network.NetworkUtils;
import cn.com.rebirth.search.core.action.admin.cluster.node.info.NodeInfo;
import cn.com.rebirth.search.core.action.admin.cluster.node.stats.NodeStats;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.discovery.Discovery;
import cn.com.rebirth.search.core.http.HttpServer;
import cn.com.rebirth.search.core.indices.IndicesService;
import cn.com.rebirth.search.core.monitor.MonitorService;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.TransportService;

import com.google.common.collect.ImmutableMap;

/**
 * The Class NodeService.
 *
 * @author l.xue.nong
 */
public class NodeService extends AbstractComponent {

	/** The thread pool. */
	private final ThreadPool threadPool;

	/** The monitor service. */
	private final MonitorService monitorService;

	/** The cluster service. */
	private final ClusterService clusterService;

	/** The transport service. */
	private final TransportService transportService;

	/** The indices service. */
	private final IndicesService indicesService;
	@Nullable
	private HttpServer httpServer;

	/** The service attributes. */
	private volatile ImmutableMap<String, String> serviceAttributes = ImmutableMap.of();

	/** The hostname. */
	@Nullable
	private String hostname;

	/**
	 * Instantiates a new node service.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param monitorService the monitor service
	 * @param discovery the discovery
	 * @param clusterService the cluster service
	 * @param transportService the transport service
	 * @param indicesService the indices service
	 */
	@Inject
	public NodeService(Settings settings, ThreadPool threadPool, MonitorService monitorService, Discovery discovery,
			ClusterService clusterService, TransportService transportService, IndicesService indicesService) {
		super(settings);
		this.threadPool = threadPool;
		this.monitorService = monitorService;
		this.clusterService = clusterService;
		this.transportService = transportService;
		this.indicesService = indicesService;
		discovery.setNodeService(this);
		InetAddress address = NetworkUtils.getLocalAddress();
		if (address != null) {
			this.hostname = address.getHostName();
		}
	}

	public void setHttpServer(@Nullable HttpServer httpServer) {
		this.httpServer = httpServer;
	}

	/**
	 * Put node attribute.
	 *
	 * @param key the key
	 * @param value the value
	 */
	@Deprecated
	public void putNodeAttribute(String key, String value) {
		putAttribute(key, value);
	}

	/**
	 * Removes the node attribute.
	 *
	 * @param key the key
	 */
	@Deprecated
	public void removeNodeAttribute(String key) {
		removeAttribute(key);
	}

	/**
	 * Put attribute.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public synchronized void putAttribute(String key, String value) {
		serviceAttributes = new MapBuilder<String, String>().putAll(serviceAttributes).put(key, value).immutableMap();
	}

	/**
	 * Removes the attribute.
	 *
	 * @param key the key
	 */
	public synchronized void removeAttribute(String key) {
		serviceAttributes = new MapBuilder<String, String>().putAll(serviceAttributes).remove(key).immutableMap();
	}

	/**
	 * Attributes.
	 *
	 * @return the immutable map
	 */
	public ImmutableMap<String, String> attributes() {
		return this.serviceAttributes;
	}

	/**
	 * Info.
	 *
	 * @return the node info
	 */
	public NodeInfo info() {
		return new NodeInfo(hostname, clusterService.state().nodes().localNode(), serviceAttributes, settings,
				monitorService.osService().info(), monitorService.processService().info(), monitorService.jvmService()
						.info(), threadPool.info(), monitorService.networkService().info(), transportService.info(),
				httpServer == null ? null : httpServer.info());
	}

	/**
	 * Info.
	 *
	 * @param settings the settings
	 * @param os the os
	 * @param process the process
	 * @param jvm the jvm
	 * @param threadPool the thread pool
	 * @param network the network
	 * @param transport the transport
	 * @param http the http
	 * @return the node info
	 */
	public NodeInfo info(boolean settings, boolean os, boolean process, boolean jvm, boolean threadPool,
			boolean network, boolean transport, boolean http) {
		return new NodeInfo(hostname, clusterService.state().nodes().localNode(), serviceAttributes,
				settings ? this.settings : null, os ? monitorService.osService().info() : null,
				process ? monitorService.processService().info() : null, jvm ? monitorService.jvmService().info()
						: null, threadPool ? this.threadPool.info() : null, network ? monitorService.networkService()
						.info() : null, transport ? transportService.info() : null, http ? (httpServer == null ? null
						: httpServer.info()) : null);
	}

	/**
	 * Stats.
	 *
	 * @return the node stats
	 */
	public NodeStats stats() {

		return new NodeStats(clusterService.state().nodes().localNode(), hostname, indicesService.stats(true),
				monitorService.osService().stats(), monitorService.processService().stats(), monitorService
						.jvmService().stats(), threadPool.stats(), monitorService.networkService().stats(),
				monitorService.fsService().stats(), transportService.stats(), httpServer == null ? null
						: httpServer.stats());
	}

	/**
	 * Stats.
	 *
	 * @param indices the indices
	 * @param os the os
	 * @param process the process
	 * @param jvm the jvm
	 * @param threadPool the thread pool
	 * @param network the network
	 * @param fs the fs
	 * @param transport the transport
	 * @param http the http
	 * @return the node stats
	 */
	public NodeStats stats(boolean indices, boolean os, boolean process, boolean jvm, boolean threadPool,
			boolean network, boolean fs, boolean transport, boolean http) {

		return new NodeStats(clusterService.state().nodes().localNode(), hostname, indices ? indicesService.stats(true)
				: null, os ? monitorService.osService().stats() : null, process ? monitorService.processService()
				.stats() : null, jvm ? monitorService.jvmService().stats() : null, threadPool ? this.threadPool.stats()
				: null, network ? monitorService.networkService().stats() : null, fs ? monitorService.fsService()
				.stats() : null, transport ? transportService.stats() : null, http ? (httpServer == null ? null
				: httpServer.stats()) : null);
	}
}
