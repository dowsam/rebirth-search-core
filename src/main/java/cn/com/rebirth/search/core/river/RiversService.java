/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RiversService.java 2012-3-29 15:02:09 l.xue.nong$$
 */


package cn.com.rebirth.search.core.river;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.commons.exception.ExceptionsHelper;
import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.component.AbstractLifecycleComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.Injector;
import cn.com.rebirth.search.commons.inject.Injectors;
import cn.com.rebirth.search.commons.inject.ModulesBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentFactory;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.NoShardAvailableActionException;
import cn.com.rebirth.search.core.action.WriteConsistencyLevel;
import cn.com.rebirth.search.core.action.admin.indices.mapping.delete.DeleteMappingResponse;
import cn.com.rebirth.search.core.action.get.GetResponse;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockException;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.indices.IndexMissingException;
import cn.com.rebirth.search.core.plugins.PluginsService;
import cn.com.rebirth.search.core.river.cluster.RiverClusterChangedEvent;
import cn.com.rebirth.search.core.river.cluster.RiverClusterService;
import cn.com.rebirth.search.core.river.cluster.RiverClusterState;
import cn.com.rebirth.search.core.river.cluster.RiverClusterStateListener;
import cn.com.rebirth.search.core.river.routing.RiverRouting;
import cn.com.rebirth.search.core.threadpool.ThreadPool;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;


/**
 * The Class RiversService.
 *
 * @author l.xue.nong
 */
public class RiversService extends AbstractLifecycleComponent<RiversService> {

	
	/** The river index name. */
	private final String riverIndexName;

	
	/** The client. */
	private Client client;

	
	/** The thread pool. */
	private final ThreadPool threadPool;

	
	/** The cluster service. */
	private final ClusterService clusterService;

	
	/** The types registry. */
	private final RiversTypesRegistry typesRegistry;

	
	/** The injector. */
	private final Injector injector;

	
	/** The rivers injectors. */
	private final Map<RiverName, Injector> riversInjectors = Maps.newHashMap();

	
	/** The rivers. */
	private volatile ImmutableMap<RiverName, River> rivers = ImmutableMap.of();

	
	/**
	 * Instantiates a new rivers service.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param threadPool the thread pool
	 * @param clusterService the cluster service
	 * @param typesRegistry the types registry
	 * @param riverClusterService the river cluster service
	 * @param injector the injector
	 */
	@Inject
	public RiversService(Settings settings, Client client, ThreadPool threadPool, ClusterService clusterService,
			RiversTypesRegistry typesRegistry, RiverClusterService riverClusterService, Injector injector) {
		super(settings);
		this.riverIndexName = RiverIndexName.Conf.indexName(settings);
		this.client = client;
		this.threadPool = threadPool;
		this.clusterService = clusterService;
		this.typesRegistry = typesRegistry;
		this.injector = injector;
		riverClusterService.add(new ApplyRivers());
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.AbstractLifecycleComponent#doStart()
	 */
	@Override
	protected void doStart() throws RestartException {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.AbstractLifecycleComponent#doStop()
	 */
	@Override
	protected void doStop() throws RestartException {
		ImmutableSet<RiverName> indices = ImmutableSet.copyOf(this.rivers.keySet());
		final CountDownLatch latch = new CountDownLatch(indices.size());
		for (final RiverName riverName : indices) {
			threadPool.generic().execute(new Runnable() {
				@Override
				public void run() {
					try {
						closeRiver(riverName);
					} catch (Exception e) {
						logger.warn("failed to delete river on stop [" + riverName.type() + "]/[" + riverName.name()
								+ "]", e);
					} finally {
						latch.countDown();
					}
				}
			});
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.AbstractLifecycleComponent#doClose()
	 */
	@Override
	protected void doClose() throws RestartException {
	}

	
	/**
	 * Creates the river.
	 *
	 * @param riverName the river name
	 * @param settings the settings
	 * @throws SumMallSearchException the sum mall search exception
	 */
	public synchronized void createRiver(RiverName riverName, Map<String, Object> settings)
			throws RestartException {
		if (riversInjectors.containsKey(riverName)) {
			logger.warn("ignoring river [{}][{}] creation, already exists", riverName.type(), riverName.name());
			return;
		}

		logger.debug("creating river [{}][{}]", riverName.type(), riverName.name());

		try {
			ModulesBuilder modules = new ModulesBuilder();
			modules.add(new RiverNameModule(riverName));
			modules.add(new RiverModule(riverName, settings, this.settings, typesRegistry));
			modules.add(new RiversPluginsModule(this.settings, injector.getInstance(PluginsService.class)));

			Injector indexInjector = modules.createChildInjector(injector);
			riversInjectors.put(riverName, indexInjector);
			River river = indexInjector.getInstance(River.class);
			rivers = MapBuilder.newMapBuilder(rivers).put(riverName, river).immutableMap();

			
			
			river.start();

			XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
			builder.field("ok", true);

			builder.startObject("node");
			builder.field("id", clusterService.localNode().id());
			builder.field("name", clusterService.localNode().name());
			builder.field("transport_address", clusterService.localNode().address().toString());
			builder.endObject();

			builder.endObject();

			client.prepareIndex(riverIndexName, riverName.name(), "_status")
					.setConsistencyLevel(WriteConsistencyLevel.ONE).setSource(builder).execute().actionGet();
		} catch (Exception e) {
			logger.warn("failed to create river [" + riverName.type() + "][" + riverName.name() + "]", e);

			try {
				XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
				builder.field("error", ExceptionsHelper.detailedMessage(e));

				builder.startObject("node");
				builder.field("id", clusterService.localNode().id());
				builder.field("name", clusterService.localNode().name());
				builder.field("transport_address", clusterService.localNode().address().toString());
				builder.endObject();

				client.prepareIndex(riverIndexName, riverName.name(), "_status")
						.setConsistencyLevel(WriteConsistencyLevel.ONE).setSource(builder).execute().actionGet();
			} catch (Exception e1) {
				logger.warn("failed to write failed status for river creation", e);
			}
		}
	}

	
	/**
	 * Close river.
	 *
	 * @param riverName the river name
	 * @throws SumMallSearchException the sum mall search exception
	 */
	public synchronized void closeRiver(RiverName riverName) throws RestartException {
		Injector riverInjector;
		River river;
		synchronized (this) {
			riverInjector = riversInjectors.remove(riverName);
			if (riverInjector == null) {
				throw new RiverException(riverName, "missing");
			}
			logger.debug("closing river [{}][{}]", riverName.type(), riverName.name());

			Map<RiverName, River> tmpMap = Maps.newHashMap(rivers);
			river = tmpMap.remove(riverName);
			rivers = ImmutableMap.copyOf(tmpMap);
		}

		river.close();

		Injectors.close(injector);
	}

	
	/**
	 * The Class ApplyRivers.
	 *
	 * @author l.xue.nong
	 */
	private class ApplyRivers implements RiverClusterStateListener {

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.river.cluster.RiverClusterStateListener#riverClusterChanged(cn.com.summall.search.core.river.cluster.RiverClusterChangedEvent)
		 */
		@Override
		public void riverClusterChanged(RiverClusterChangedEvent event) {
			DiscoveryNode localNode = clusterService.localNode();
			RiverClusterState state = event.state();

			
			for (final RiverName riverName : rivers.keySet()) {
				RiverRouting routing = state.routing().routing(riverName);
				if (routing == null || !localNode.equals(routing.node())) {
					
					closeRiver(riverName);
					
					try {
						client.prepareGet(riverIndexName, riverName.name(), "_meta").setListenerThreaded(true)
								.execute(new ActionListener<GetResponse>() {
									@Override
									public void onResponse(GetResponse getResponse) {
										if (!getResponse.exists()) {
											
											client.admin().indices().prepareDeleteMapping(riverIndexName)
													.setType(riverName.name())
													.execute(new ActionListener<DeleteMappingResponse>() {
														@Override
														public void onResponse(
																DeleteMappingResponse deleteMappingResponse) {
															
														}

														@Override
														public void onFailure(Throwable e) {
															logger.debug(
																	"failed to (double) delete river [{}] content", e,
																	riverName.name());
														}
													});
										}
									}

									@Override
									public void onFailure(Throwable e) {
										logger.debug("failed to (double) delete river [{}] content", e,
												riverName.name());
									}
								});
					} catch (IndexMissingException e) {
						
					} catch (Exception e) {
						logger.warn("unexpected failure when trying to verify river [{}] deleted", e, riverName.name());
					}
				}
			}

			for (final RiverRouting routing : state.routing()) {
				
				if (routing.node() == null) {
					continue;
				}
				
				if (!routing.node().equals(localNode)) {
					continue;
				}
				
				if (rivers.containsKey(routing.riverName())) {
					continue;
				}
				client.prepareGet(riverIndexName, routing.riverName().name(), "_meta").setListenerThreaded(true)
						.execute(new ActionListener<GetResponse>() {
							@Override
							public void onResponse(GetResponse getResponse) {
								if (!rivers.containsKey(routing.riverName())) {
									if (getResponse.exists()) {
										
										createRiver(routing.riverName(), getResponse.sourceAsMap());
									}
								}
							}

							@Override
							public void onFailure(Throwable e) {
								
								
								
								Throwable failure = ExceptionsHelper.unwrapCause(e);
								if ((failure instanceof NoShardAvailableActionException)
										|| (failure instanceof ClusterBlockException)
										|| (failure instanceof IndexMissingException)) {
									logger.debug("failed to get _meta from [" + routing.riverName().type() + "]/["
											+ routing.riverName().name() + "], retrying...", e);
									final ActionListener<GetResponse> listener = this;
									threadPool.schedule(TimeValue.timeValueSeconds(5), ThreadPool.Names.SAME,
											new Runnable() {
												@Override
												public void run() {
													client.prepareGet(riverIndexName, routing.riverName().name(),
															"_meta").setListenerThreaded(true).execute(listener);
												}
											});
								} else {
									logger.warn("failed to get _meta from [" + routing.riverName().type() + "]/["
											+ routing.riverName().name() + "]", e);
								}
							}
						});
			}
		}
	}
}
