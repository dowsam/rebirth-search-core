/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core InternalNode.java 2012-7-6 14:29:14 l.xue.nong$$
 */

package cn.com.rebirth.search.core.node.internal;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.rebirth.commons.StopWatch;
import cn.com.rebirth.commons.collect.Tuple;
import cn.com.rebirth.commons.component.Lifecycle;
import cn.com.rebirth.commons.component.LifecycleComponent;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.io.CachedStreams;
import cn.com.rebirth.commons.settings.ImmutableSettings;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.thread.ThreadLocals;
import cn.com.rebirth.core.inject.Injector;
import cn.com.rebirth.core.inject.Injectors;
import cn.com.rebirth.core.inject.ModulesBuilder;
import cn.com.rebirth.core.monitor.MonitorService;
import cn.com.rebirth.core.monitor.jvm.JvmInfo;
import cn.com.rebirth.core.settings.SettingsModule;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.core.threadpool.ThreadPoolModule;
import cn.com.rebirth.search.commons.CacheRecycler;
import cn.com.rebirth.search.commons.network.NetworkModule;
import cn.com.rebirth.search.commons.network.NetworkService;
import cn.com.rebirth.search.core.RestartSearchCoreVersion;
import cn.com.rebirth.search.core.action.ActionModule;
import cn.com.rebirth.search.core.cache.NodeCache;
import cn.com.rebirth.search.core.cache.NodeCacheModule;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.client.node.NodeClientModule;
import cn.com.rebirth.search.core.cluster.ClusterModule;
import cn.com.rebirth.search.core.cluster.ClusterNameModule;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.routing.RoutingService;
import cn.com.rebirth.search.core.discovery.DiscoveryModule;
import cn.com.rebirth.search.core.discovery.DiscoveryService;
import cn.com.rebirth.search.core.env.Environment;
import cn.com.rebirth.search.core.env.EnvironmentModule;
import cn.com.rebirth.search.core.env.NodeEnvironment;
import cn.com.rebirth.search.core.env.NodeEnvironmentModule;
import cn.com.rebirth.search.core.gateway.GatewayModule;
import cn.com.rebirth.search.core.gateway.GatewayService;
import cn.com.rebirth.search.core.http.HttpServer;
import cn.com.rebirth.search.core.http.HttpServerModule;
import cn.com.rebirth.search.core.indices.IndicesModule;
import cn.com.rebirth.search.core.indices.IndicesService;
import cn.com.rebirth.search.core.indices.cache.filter.IndicesFilterCache;
import cn.com.rebirth.search.core.indices.cluster.IndicesClusterStateService;
import cn.com.rebirth.search.core.indices.memory.IndexingMemoryController;
import cn.com.rebirth.search.core.indices.ttl.IndicesTTLService;
import cn.com.rebirth.search.core.jmx.JmxModule;
import cn.com.rebirth.search.core.jmx.JmxService;
import cn.com.rebirth.search.core.monitor.MonitorModule;
import cn.com.rebirth.search.core.node.Node;
import cn.com.rebirth.search.core.plugins.PluginsModule;
import cn.com.rebirth.search.core.plugins.PluginsService;
import cn.com.rebirth.search.core.rest.RestController;
import cn.com.rebirth.search.core.rest.RestModule;
import cn.com.rebirth.search.core.river.RiversManager;
import cn.com.rebirth.search.core.river.RiversModule;
import cn.com.rebirth.search.core.script.ScriptModule;
import cn.com.rebirth.search.core.script.ScriptService;
import cn.com.rebirth.search.core.search.SearchModule;
import cn.com.rebirth.search.core.search.SearchService;
import cn.com.rebirth.search.core.transport.TransportModule;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class InternalNode.
 *
 * @author l.xue.nong
 */
public final class InternalNode implements Node {

	/** The logger. */
	private final Logger logger = LoggerFactory.getLogger(getClass());

	/** The lifecycle. */
	private final Lifecycle lifecycle = new Lifecycle();

	/** The injector. */
	private final Injector injector;

	/** The settings. */
	private final Settings settings;

	/** The environment. */
	private final Environment environment;

	/** The plugins service. */
	private final PluginsService pluginsService;

	/** The client. */
	private final Client client;

	/**
	 * Instantiates a new internal node.
	 *
	 * @throws RebirthException the rebirth exception
	 */
	public InternalNode() throws RebirthException {
		this(ImmutableSettings.Builder.EMPTY_SETTINGS, true);
	}

	/**
	 * Instantiates a new internal node.
	 *
	 * @param pSettings the settings
	 * @param loadConfigSettings the load config settings
	 * @throws RebirthException the rebirth exception
	 */
	public InternalNode(Settings pSettings, boolean loadConfigSettings) throws RebirthException {
		Tuple<Settings, Environment> tuple = InternalSettingsPerparer.prepareSettings(pSettings, loadConfigSettings);

		Logger logger = LoggerFactory.getLogger(Node.class);
		logger.info("{{}}[{}]: initializing ...", new RestartSearchCoreVersion().getModuleVersion(), JvmInfo.jvmInfo()
				.pid());

		this.pluginsService = new PluginsService(tuple.v1(), tuple.v2());
		this.settings = pluginsService.updatedSettings();
		this.environment = tuple.v2();

		NodeEnvironment nodeEnvironment = new NodeEnvironment(this.settings, this.environment);

		ModulesBuilder modules = new ModulesBuilder();
		modules.add(new PluginsModule(settings, pluginsService));
		modules.add(new SettingsModule(settings));
		modules.add(new NodeModule(this));
		modules.add(new NetworkModule());
		modules.add(new NodeCacheModule(settings));
		modules.add(new ScriptModule(settings));
		modules.add(new JmxModule(settings));
		modules.add(new EnvironmentModule(environment));
		modules.add(new NodeEnvironmentModule(nodeEnvironment));
		modules.add(new ClusterNameModule(settings));
		modules.add(new ThreadPoolModule(settings));
		modules.add(new DiscoveryModule(settings));
		modules.add(new ClusterModule(settings));
		modules.add(new RestModule(settings));
		modules.add(new TransportModule(settings));
		if (settings.getAsBoolean("http.enabled", true)) {
			modules.add(new HttpServerModule(settings));
		}
		modules.add(new RiversModule(settings));
		modules.add(new IndicesModule(settings));
		modules.add(new SearchModule());
		modules.add(new ActionModule(false));
		modules.add(new MonitorModule(settings));
		modules.add(new GatewayModule(settings));
		modules.add(new NodeClientModule());

		injector = modules.createInjector();

		client = injector.getInstance(Client.class);

		logger.info("{{}}[{}]: initialized", new RestartSearchCoreVersion().getModuleVersion(), JvmInfo.jvmInfo().pid());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.node.Node#settings()
	 */
	@Override
	public Settings settings() {
		return this.settings;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.node.Node#client()
	 */
	@Override
	public Client client() {
		return client;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.node.Node#start()
	 */
	public Node start() {
		if (!lifecycle.moveToStarted()) {
			return this;
		}

		logger.info("{{}}[{}]: starting ...", new RestartSearchCoreVersion().getModuleVersion(), JvmInfo.jvmInfo()
				.pid());

		for (Class<? extends LifecycleComponent> plugin : pluginsService.services()) {
			injector.getInstance(plugin).start();
		}

		injector.getInstance(IndicesService.class).start();
		injector.getInstance(IndexingMemoryController.class).start();
		injector.getInstance(IndicesClusterStateService.class).start();
		injector.getInstance(IndicesTTLService.class).start();
		injector.getInstance(RiversManager.class).start();
		injector.getInstance(ClusterService.class).start();
		injector.getInstance(RoutingService.class).start();
		injector.getInstance(SearchService.class).start();
		injector.getInstance(MonitorService.class).start();
		injector.getInstance(RestController.class).start();
		injector.getInstance(TransportService.class).start();
		DiscoveryService discoService = injector.getInstance(DiscoveryService.class).start();

		injector.getInstance(GatewayService.class).start();

		if (settings.getAsBoolean("http.enabled", true)) {
			injector.getInstance(HttpServer.class).start();
		}

		injector.getInstance(JmxService.class).connectAndRegister(discoService.nodeDescription(),
				injector.getInstance(NetworkService.class));

		logger.info("{{}}[{}]: started", new RestartSearchCoreVersion().getModuleVersion(), JvmInfo.jvmInfo().pid());

		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.node.Node#stop()
	 */
	@Override
	public Node stop() {
		if (!lifecycle.moveToStopped()) {
			return this;
		}
		logger.info("{{}}[{}]: stopping ...", new RestartSearchCoreVersion().getModuleVersion(), JvmInfo.jvmInfo()
				.pid());

		injector.getInstance(RiversManager.class).stop();
		if (settings.getAsBoolean("http.enabled", true)) {
			injector.getInstance(HttpServer.class).stop();
		}
		injector.getInstance(IndicesClusterStateService.class).stop();

		injector.getInstance(IndexingMemoryController.class).stop();
		injector.getInstance(IndicesTTLService.class).stop();
		injector.getInstance(IndicesService.class).stop();

		injector.getInstance(RoutingService.class).stop();
		injector.getInstance(ClusterService.class).stop();
		injector.getInstance(DiscoveryService.class).stop();
		injector.getInstance(MonitorService.class).stop();
		injector.getInstance(GatewayService.class).stop();
		injector.getInstance(SearchService.class).stop();
		injector.getInstance(RestController.class).stop();
		injector.getInstance(TransportService.class).stop();
		injector.getInstance(JmxService.class).close();

		for (Class<? extends LifecycleComponent> plugin : pluginsService.services()) {
			injector.getInstance(plugin).stop();
		}

		logger.info("{{}}[{}]: stopped", new RestartSearchCoreVersion().getModuleVersion(), JvmInfo.jvmInfo().pid());

		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.node.Node#close()
	 */
	public void close() {
		if (lifecycle.started()) {
			stop();
		}
		if (!lifecycle.moveToClosed()) {
			return;
		}

		logger.info("{{}}[{}]: closing ...", new RestartSearchCoreVersion().getModuleVersion(), JvmInfo.jvmInfo().pid());

		StopWatch stopWatch = new StopWatch("node_close");

		stopWatch.start("http");
		if (settings.getAsBoolean("http.enabled", true)) {
			injector.getInstance(HttpServer.class).close();
		}

		stopWatch.stop().start("rivers");
		injector.getInstance(RiversManager.class).close();

		stopWatch.stop().start("client");
		injector.getInstance(Client.class).close();
		stopWatch.stop().start("indices_cluster");
		injector.getInstance(IndicesClusterStateService.class).close();
		stopWatch.stop().start("indices");
		injector.getInstance(IndicesFilterCache.class).close();
		injector.getInstance(IndexingMemoryController.class).close();
		injector.getInstance(IndicesTTLService.class).close();
		injector.getInstance(IndicesService.class).close();
		stopWatch.stop().start("routing");
		injector.getInstance(RoutingService.class).close();
		stopWatch.stop().start("cluster");
		injector.getInstance(ClusterService.class).close();
		stopWatch.stop().start("discovery");
		injector.getInstance(DiscoveryService.class).close();
		stopWatch.stop().start("monitor");
		injector.getInstance(MonitorService.class).close();
		stopWatch.stop().start("gateway");
		injector.getInstance(GatewayService.class).close();
		stopWatch.stop().start("search");
		injector.getInstance(SearchService.class).close();
		stopWatch.stop().start("rest");
		injector.getInstance(RestController.class).close();
		stopWatch.stop().start("transport");
		injector.getInstance(TransportService.class).close();

		for (Class<? extends LifecycleComponent> plugin : pluginsService.services()) {
			stopWatch.stop().start("plugin(" + plugin.getName() + ")");
			injector.getInstance(plugin).close();
		}

		stopWatch.stop().start("node_cache");
		injector.getInstance(NodeCache.class).close();

		stopWatch.stop().start("script");
		injector.getInstance(ScriptService.class).close();

		stopWatch.stop().start("thread_pool");
		injector.getInstance(ThreadPool.class).shutdown();
		try {
			injector.getInstance(ThreadPool.class).awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {

		}
		stopWatch.stop().start("thread_pool_force_shutdown");
		try {
			injector.getInstance(ThreadPool.class).shutdownNow();
		} catch (Exception e) {

		}
		stopWatch.stop();

		CacheRecycler.clear();
		CachedStreams.clear();
		ThreadLocals.clearReferencesThreadLocals();

		if (logger.isTraceEnabled()) {
			logger.trace("Close times for each service:\n{}", stopWatch.prettyPrint());
		}

		injector.getInstance(NodeEnvironment.class).close();
		Injectors.close(injector);

		logger.info("{{}}[{}]: closed", new RestartSearchCoreVersion().getModuleVersion(), JvmInfo.jvmInfo().pid());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.node.Node#isClosed()
	 */
	@Override
	public boolean isClosed() {
		return lifecycle.closed();
	}

	/**
	 * Injector.
	 *
	 * @return the injector
	 */
	public Injector injector() {
		return this.injector;
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {
		final InternalNode node = new InternalNode();
		node.start();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				node.close();
			}
		});
	}
}