/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MetaDataCreateIndexService.java 2012-3-29 15:01:44 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster.metadata;

import static cn.com.rebirth.search.core.cluster.ClusterState.newClusterStateBuilder;
import static cn.com.rebirth.search.core.cluster.metadata.IndexMetaData.SETTING_AUTO_EXPAND_REPLICAS;
import static cn.com.rebirth.search.core.cluster.metadata.IndexMetaData.SETTING_NUMBER_OF_REPLICAS;
import static cn.com.rebirth.search.core.cluster.metadata.IndexMetaData.SETTING_NUMBER_OF_SHARDS;
import static cn.com.rebirth.search.core.cluster.metadata.IndexMetaData.SETTING_VERSION_CREATED;
import static cn.com.rebirth.search.core.cluster.metadata.IndexMetaData.newIndexMetaDataBuilder;
import static cn.com.rebirth.search.core.cluster.metadata.MetaData.newMetaDataBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.compress.CompressedString;
import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.regex.Regex;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.io.Streams;
import cn.com.rebirth.search.commons.settings.ImmutableSettings;
import cn.com.rebirth.search.commons.xcontent.XContentFactory;
import cn.com.rebirth.search.commons.xcontent.XContentHelper;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.RestartSearchCoreVersion;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.ProcessedClusterStateUpdateTask;
import cn.com.rebirth.search.core.cluster.action.index.NodeIndexCreatedAction;
import cn.com.rebirth.search.core.cluster.block.ClusterBlock;
import cn.com.rebirth.search.core.cluster.block.ClusterBlocks;
import cn.com.rebirth.search.core.cluster.metadata.IndexMetaData.State;
import cn.com.rebirth.search.core.cluster.routing.IndexRoutingTable;
import cn.com.rebirth.search.core.cluster.routing.RoutingTable;
import cn.com.rebirth.search.core.cluster.routing.allocation.AllocationService;
import cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation;
import cn.com.rebirth.search.core.env.Environment;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.mapper.DocumentMapper;
import cn.com.rebirth.search.core.index.mapper.MapperParsingException;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.percolator.PercolatorService;
import cn.com.rebirth.search.core.index.service.IndexService;
import cn.com.rebirth.search.core.indices.IndexAlreadyExistsException;
import cn.com.rebirth.search.core.indices.IndicesService;
import cn.com.rebirth.search.core.indices.InvalidIndexNameException;
import cn.com.rebirth.search.core.river.RiverIndexName;
import cn.com.rebirth.search.core.threadpool.ThreadPool;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Closeables;


/**
 * The Class MetaDataCreateIndexService.
 *
 * @author l.xue.nong
 */
public class MetaDataCreateIndexService extends AbstractComponent {

	
	/** The environment. */
	private final Environment environment;

	
	/** The thread pool. */
	private final ThreadPool threadPool;

	
	/** The cluster service. */
	private final ClusterService clusterService;

	
	/** The indices service. */
	private final IndicesService indicesService;

	
	/** The allocation service. */
	private final AllocationService allocationService;

	
	/** The node index created action. */
	private final NodeIndexCreatedAction nodeIndexCreatedAction;

	
	/** The meta data service. */
	private final MetaDataService metaDataService;

	
	/** The river index name. */
	private final String riverIndexName;

	
	/**
	 * Instantiates a new meta data create index service.
	 *
	 * @param settings the settings
	 * @param environment the environment
	 * @param threadPool the thread pool
	 * @param clusterService the cluster service
	 * @param indicesService the indices service
	 * @param allocationService the allocation service
	 * @param nodeIndexCreatedAction the node index created action
	 * @param metaDataService the meta data service
	 * @param riverIndexName the river index name
	 */
	@Inject
	public MetaDataCreateIndexService(Settings settings, Environment environment, ThreadPool threadPool,
			ClusterService clusterService, IndicesService indicesService, AllocationService allocationService,
			NodeIndexCreatedAction nodeIndexCreatedAction, MetaDataService metaDataService,
			@RiverIndexName String riverIndexName) {
		super(settings);
		this.environment = environment;
		this.threadPool = threadPool;
		this.clusterService = clusterService;
		this.indicesService = indicesService;
		this.allocationService = allocationService;
		this.nodeIndexCreatedAction = nodeIndexCreatedAction;
		this.metaDataService = metaDataService;
		this.riverIndexName = riverIndexName;
	}

	
	/**
	 * Creates the index.
	 *
	 * @param request the request
	 * @param userListener the user listener
	 */
	public void createIndex(final Request request, final Listener userListener) {
		ImmutableSettings.Builder updatedSettingsBuilder = ImmutableSettings.settingsBuilder();
		for (Map.Entry<String, String> entry : request.settings.getAsMap().entrySet()) {
			if (!entry.getKey().startsWith("index.")) {
				updatedSettingsBuilder.put("index." + entry.getKey(), entry.getValue());
			} else {
				updatedSettingsBuilder.put(entry.getKey(), entry.getValue());
			}
		}
		request.settings(updatedSettingsBuilder.build());

		
		
		MetaDataService.MdLock mdLock = metaDataService.indexMetaDataLock(request.index);
		try {
			mdLock.lock();
		} catch (InterruptedException e) {
			userListener.onFailure(e);
			return;
		}

		final CreateIndexListener listener = new CreateIndexListener(mdLock, request, userListener);

		clusterService.submitStateUpdateTask("create-index [" + request.index + "], cause [" + request.cause + "]",
				new ProcessedClusterStateUpdateTask() {
					@Override
					public ClusterState execute(ClusterState currentState) {
						try {
							try {
								validate(request, currentState);
							} catch (Exception e) {
								listener.onFailure(e);
								return currentState;
							}

							
							
							List<IndexTemplateMetaData> templates = findTemplates(request, currentState);

							
							Map<String, Map<String, Object>> mappings = Maps.newHashMap();
							for (Map.Entry<String, String> entry : request.mappings.entrySet()) {
								mappings.put(entry.getKey(), parseMapping(entry.getValue()));
							}

							
							for (IndexTemplateMetaData template : templates) {
								for (Map.Entry<String, CompressedString> entry : template.mappings().entrySet()) {
									if (mappings.containsKey(entry.getKey())) {
										XContentHelper.mergeDefaults(mappings.get(entry.getKey()), parseMapping(entry
												.getValue().string()));
									} else {
										mappings.put(entry.getKey(), parseMapping(entry.getValue().string()));
									}
								}
							}

							
							File mappingsDir = new File(environment.configFile(), "mappings");
							if (mappingsDir.exists() && mappingsDir.isDirectory()) {
								
								File indexMappingsDir = new File(mappingsDir, request.index);
								if (indexMappingsDir.exists() && indexMappingsDir.isDirectory()) {
									addMappings(mappings, indexMappingsDir);
								}

								
								File defaultMappingsDir = new File(mappingsDir, "_default");
								if (defaultMappingsDir.exists() && defaultMappingsDir.isDirectory()) {
									addMappings(mappings, defaultMappingsDir);
								}
							}

							ImmutableSettings.Builder indexSettingsBuilder = ImmutableSettings.settingsBuilder();
							
							for (int i = templates.size() - 1; i >= 0; i--) {
								indexSettingsBuilder.put(templates.get(i).settings());
							}
							
							indexSettingsBuilder.put(request.settings);

							if (request.index.equals(PercolatorService.INDEX_NAME)) {
								
								indexSettingsBuilder.put(SETTING_NUMBER_OF_SHARDS, 1);
							} else {
								if (indexSettingsBuilder.get(SETTING_NUMBER_OF_SHARDS) == null) {
									if (request.index.equals(riverIndexName)) {
										indexSettingsBuilder.put(SETTING_NUMBER_OF_SHARDS,
												settings.getAsInt(SETTING_NUMBER_OF_SHARDS, 1));
									} else {
										indexSettingsBuilder.put(SETTING_NUMBER_OF_SHARDS,
												settings.getAsInt(SETTING_NUMBER_OF_SHARDS, 5));
									}
								}
							}
							if (request.index.equals(PercolatorService.INDEX_NAME)) {
								
								indexSettingsBuilder.put(SETTING_NUMBER_OF_REPLICAS, 0);
								indexSettingsBuilder.put(SETTING_AUTO_EXPAND_REPLICAS, "0-all");
							} else {
								if (indexSettingsBuilder.get(SETTING_NUMBER_OF_REPLICAS) == null) {
									if (request.index.equals(riverIndexName)) {
										indexSettingsBuilder.put(SETTING_NUMBER_OF_REPLICAS,
												settings.getAsInt(SETTING_NUMBER_OF_REPLICAS, 1));
									} else {
										indexSettingsBuilder.put(SETTING_NUMBER_OF_REPLICAS,
												settings.getAsInt(SETTING_NUMBER_OF_REPLICAS, 1));
									}
								}
							}

							indexSettingsBuilder.put(SETTING_VERSION_CREATED, new RestartSearchCoreVersion().getModuleVersion());

							Settings actualIndexSettings = indexSettingsBuilder.build();

							

							
							indicesService.createIndex(request.index, actualIndexSettings, clusterService.state()
									.nodes().localNode().id());
							
							IndexService indexService = indicesService.indexServiceSafe(request.index);
							MapperService mapperService = indexService.mapperService();
							
							if (mappings.containsKey(MapperService.DEFAULT_MAPPING)) {
								try {
									mapperService.add(
											MapperService.DEFAULT_MAPPING,
											XContentFactory.jsonBuilder()
													.map(mappings.get(MapperService.DEFAULT_MAPPING)).string());
								} catch (Exception e) {
									indicesService.deleteIndex(request.index,
											"failed on parsing default mapping on index creation");
									throw new MapperParsingException("mapping [" + MapperService.DEFAULT_MAPPING + "]",
											e);
								}
							}
							for (Map.Entry<String, Map<String, Object>> entry : mappings.entrySet()) {
								if (entry.getKey().equals(MapperService.DEFAULT_MAPPING)) {
									continue;
								}
								try {
									mapperService.add(entry.getKey(),
											XContentFactory.jsonBuilder().map(entry.getValue()).string());
								} catch (Exception e) {
									indicesService.deleteIndex(request.index,
											"failed on parsing mappings on index creation");
									throw new MapperParsingException("mapping [" + entry.getKey() + "]", e);
								}
							}
							
							Map<String, MappingMetaData> mappingsMetaData = Maps.newHashMap();
							for (DocumentMapper mapper : mapperService) {
								MappingMetaData mappingMd = new MappingMetaData(mapper);
								mappingsMetaData.put(mapper.type(), mappingMd);
							}

							final IndexMetaData.Builder indexMetaDataBuilder = newIndexMetaDataBuilder(request.index)
									.settings(actualIndexSettings);
							for (MappingMetaData mappingMd : mappingsMetaData.values()) {
								indexMetaDataBuilder.putMapping(mappingMd);
							}
							indexMetaDataBuilder.state(request.state);
							final IndexMetaData indexMetaData = indexMetaDataBuilder.build();

							MetaData newMetaData = newMetaDataBuilder().metaData(currentState.metaData())
									.put(indexMetaData, false).build();

							logger.info("[" + request.index + "] creating index, cause [" + request.cause
									+ "], shards [" + indexMetaData.numberOfShards() + "]/[{}], mappings {}",
									indexMetaData.numberOfReplicas(), mappings.keySet());

							ClusterBlocks.Builder blocks = ClusterBlocks.builder().blocks(currentState.blocks());
							if (!request.blocks.isEmpty()) {
								for (ClusterBlock block : request.blocks) {
									blocks.addIndexBlock(request.index, block);
								}
							}
							if (request.state == State.CLOSE) {
								blocks.addIndexBlock(request.index, MetaDataStateIndexService.INDEX_CLOSED_BLOCK);
							}

							ClusterState updatedState = newClusterStateBuilder().state(currentState).blocks(blocks)
									.metaData(newMetaData).build();

							if (request.state == State.OPEN) {
								RoutingTable.Builder routingTableBuilder = RoutingTable.builder().routingTable(
										updatedState.routingTable());
								IndexRoutingTable.Builder indexRoutingBuilder = new IndexRoutingTable.Builder(
										request.index).initializeEmpty(updatedState.metaData().index(request.index),
										true);
								routingTableBuilder.add(indexRoutingBuilder);
								RoutingAllocation.Result routingResult = allocationService
										.reroute(newClusterStateBuilder().state(updatedState)
												.routingTable(routingTableBuilder).build());
								updatedState = newClusterStateBuilder().state(updatedState)
										.routingResult(routingResult).build();
							}

							
							final AtomicInteger counter = new AtomicInteger(currentState.nodes().size());

							final NodeIndexCreatedAction.Listener nodeIndexCreatedListener = new NodeIndexCreatedAction.Listener() {
								@Override
								public void onNodeIndexCreated(String index, String nodeId) {
									if (index.equals(request.index)) {
										if (counter.decrementAndGet() == 0) {
											listener.onResponse(new Response(true, indexMetaData));
											nodeIndexCreatedAction.remove(this);
										}
									}
								}
							};

							nodeIndexCreatedAction.add(nodeIndexCreatedListener);

							listener.future = threadPool.schedule(request.timeout, ThreadPool.Names.SAME,
									new Runnable() {
										@Override
										public void run() {
											listener.onResponse(new Response(false, indexMetaData));
											nodeIndexCreatedAction.remove(nodeIndexCreatedListener);
										}
									});

							return updatedState;
						} catch (Exception e) {
							logger.warn("[{}] failed to create", e, request.index);
							listener.onFailure(e);
							return currentState;
						}
					}

					@Override
					public void clusterStateProcessed(ClusterState clusterState) {
					}
				});
	}

	
	/**
	 * The listener interface for receiving createIndex events.
	 * The class that is interested in processing a createIndex
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addCreateIndexListener<code> method. When
	 * the createIndex event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see CreateIndexEvent
	 */
	class CreateIndexListener implements Listener {

		
		/** The notified. */
		private final AtomicBoolean notified = new AtomicBoolean();

		
		/** The md lock. */
		private final MetaDataService.MdLock mdLock;

		
		/** The request. */
		private final Request request;

		
		/** The listener. */
		private final Listener listener;

		
		/** The future. */
		volatile ScheduledFuture future;

		
		/**
		 * Instantiates a new creates the index listener.
		 *
		 * @param mdLock the md lock
		 * @param request the request
		 * @param listener the listener
		 */
		private CreateIndexListener(MetaDataService.MdLock mdLock, Request request, Listener listener) {
			this.mdLock = mdLock;
			this.request = request;
			this.listener = listener;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.cluster.metadata.MetaDataCreateIndexService.Listener#onResponse(cn.com.summall.search.core.cluster.metadata.MetaDataCreateIndexService.Response)
		 */
		@Override
		public void onResponse(final Response response) {
			if (notified.compareAndSet(false, true)) {
				mdLock.unlock();
				if (future != null) {
					future.cancel(false);
				}
				listener.onResponse(response);
			}
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.cluster.metadata.MetaDataCreateIndexService.Listener#onFailure(java.lang.Throwable)
		 */
		@Override
		public void onFailure(Throwable t) {
			if (notified.compareAndSet(false, true)) {
				mdLock.unlock();
				if (future != null) {
					future.cancel(false);
				}
				listener.onFailure(t);
			}
		}
	}

	
	/**
	 * Parses the mapping.
	 *
	 * @param mappingSource the mapping source
	 * @return the map
	 * @throws Exception the exception
	 */
	private Map<String, Object> parseMapping(String mappingSource) throws Exception {
		return XContentFactory.xContent(mappingSource).createParser(mappingSource).mapAndClose();
	}

	
	/**
	 * Adds the mappings.
	 *
	 * @param mappings the mappings
	 * @param mappingsDir the mappings dir
	 */
	private void addMappings(Map<String, Map<String, Object>> mappings, File mappingsDir) {
		File[] mappingsFiles = mappingsDir.listFiles();
		for (File mappingFile : mappingsFiles) {
			if (mappingFile.isHidden()) {
				continue;
			}
			String mappingType = mappingFile.getName().substring(0, mappingFile.getName().lastIndexOf('.'));
			try {
				String mappingSource = Streams.copyToString(new FileReader(mappingFile));
				if (mappings.containsKey(mappingType)) {
					XContentHelper.mergeDefaults(mappings.get(mappingType), parseMapping(mappingSource));
				} else {
					mappings.put(mappingType, parseMapping(mappingSource));
				}
			} catch (Exception e) {
				logger.warn("failed to read / parse mapping [" + mappingType + "] from location [" + mappingFile
						+ "], ignoring...", e);
			}
		}
	}

	
	/**
	 * Find templates.
	 *
	 * @param request the request
	 * @param state the state
	 * @return the list
	 */
	private List<IndexTemplateMetaData> findTemplates(Request request, ClusterState state) {
		List<IndexTemplateMetaData> templates = Lists.newArrayList();
		for (IndexTemplateMetaData template : state.metaData().templates().values()) {
			if (Regex.simpleMatch(template.template(), request.index)) {
				templates.add(template);
			}
		}

		
		File templatesDir = new File(environment.configFile(), "templates");
		if (templatesDir.exists() && templatesDir.isDirectory()) {
			File[] templatesFiles = templatesDir.listFiles();
			if (templatesFiles != null) {
				for (File templatesFile : templatesFiles) {
					XContentParser parser = null;
					try {
						byte[] templatesData = Streams.copyToByteArray(templatesFile);
						parser = XContentHelper.createParser(templatesData, 0, templatesData.length);
						IndexTemplateMetaData template = IndexTemplateMetaData.Builder.fromXContentStandalone(parser);
						if (Regex.simpleMatch(template.template(), request.index)) {
							templates.add(template);
						}
					} catch (Exception e) {
						logger.warn("[{}] failed to read template [{}] from config", request.index,
								templatesFile.getAbsolutePath());
					} finally {
						Closeables.closeQuietly(parser);
					}
				}
			}
		}

		Collections.sort(templates, new Comparator<IndexTemplateMetaData>() {
			@Override
			public int compare(IndexTemplateMetaData o1, IndexTemplateMetaData o2) {
				return o2.order() - o1.order();
			}
		});
		return templates;
	}

	
	/**
	 * Validate.
	 *
	 * @param request the request
	 * @param state the state
	 * @throws SumMallSearchException the sum mall search exception
	 */
	private void validate(Request request, ClusterState state) throws RestartException {
		if (state.routingTable().hasIndex(request.index)) {
			throw new IndexAlreadyExistsException(new Index(request.index));
		}
		if (state.metaData().hasIndex(request.index)) {
			throw new IndexAlreadyExistsException(new Index(request.index));
		}
		if (request.index.contains(" ")) {
			throw new InvalidIndexNameException(new Index(request.index), request.index, "must not contain whitespace");
		}
		if (request.index.contains(",")) {
			throw new InvalidIndexNameException(new Index(request.index), request.index, "must not contain ',");
		}
		if (request.index.contains("#")) {
			throw new InvalidIndexNameException(new Index(request.index), request.index, "must not contain '#");
		}
		if (!request.index.equals(riverIndexName) && !request.index.equals(PercolatorService.INDEX_NAME)
				&& request.index.charAt(0) == '_') {
			throw new InvalidIndexNameException(new Index(request.index), request.index, "must not start with '_'");
		}
		if (!request.index.toLowerCase().equals(request.index)) {
			throw new InvalidIndexNameException(new Index(request.index), request.index, "must be lowercase");
		}
		if (!Strings.validFileName(request.index)) {
			throw new InvalidIndexNameException(new Index(request.index), request.index,
					"must not contain the following characters " + Strings.INVALID_FILENAME_CHARS);
		}
		if (state.metaData().aliases().containsKey(request.index)) {
			throw new InvalidIndexNameException(new Index(request.index), request.index,
					"an alias with the same name already exists");
		}
	}

	
	/**
	 * The Interface Listener.
	 *
	 * @author l.xue.nong
	 */
	public static interface Listener {

		
		/**
		 * On response.
		 *
		 * @param response the response
		 */
		void onResponse(Response response);

		
		/**
		 * On failure.
		 *
		 * @param t the t
		 */
		void onFailure(Throwable t);
	}

	
	/**
	 * The Class Request.
	 *
	 * @author l.xue.nong
	 */
	public static class Request {

		
		/** The cause. */
		final String cause;

		
		/** The index. */
		final String index;

		
		/** The state. */
		State state = State.OPEN;

		
		/** The settings. */
		Settings settings = ImmutableSettings.Builder.EMPTY_SETTINGS;

		
		/** The mappings. */
		Map<String, String> mappings = Maps.newHashMap();

		
		/** The timeout. */
		TimeValue timeout = TimeValue.timeValueSeconds(5);

		
		/** The blocks. */
		Set<ClusterBlock> blocks = Sets.newHashSet();

		
		/**
		 * Instantiates a new request.
		 *
		 * @param cause the cause
		 * @param index the index
		 */
		public Request(String cause, String index) {
			this.cause = cause;
			this.index = index;
		}

		
		/**
		 * Settings.
		 *
		 * @param settings the settings
		 * @return the request
		 */
		public Request settings(Settings settings) {
			this.settings = settings;
			return this;
		}

		
		/**
		 * Mappings.
		 *
		 * @param mappings the mappings
		 * @return the request
		 */
		public Request mappings(Map<String, String> mappings) {
			this.mappings.putAll(mappings);
			return this;
		}

		
		/**
		 * Mappings meta data.
		 *
		 * @param mappings the mappings
		 * @return the request
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public Request mappingsMetaData(Map<String, MappingMetaData> mappings) throws IOException {
			for (Map.Entry<String, MappingMetaData> entry : mappings.entrySet()) {
				this.mappings.put(entry.getKey(), entry.getValue().source().string());
			}
			return this;
		}

		
		/**
		 * Mappings compressed.
		 *
		 * @param mappings the mappings
		 * @return the request
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public Request mappingsCompressed(Map<String, CompressedString> mappings) throws IOException {
			for (Map.Entry<String, CompressedString> entry : mappings.entrySet()) {
				this.mappings.put(entry.getKey(), entry.getValue().string());
			}
			return this;
		}

		
		/**
		 * Blocks.
		 *
		 * @param blocks the blocks
		 * @return the request
		 */
		public Request blocks(Set<ClusterBlock> blocks) {
			this.blocks.addAll(blocks);
			return this;
		}

		
		/**
		 * State.
		 *
		 * @param state the state
		 * @return the request
		 */
		public Request state(State state) {
			this.state = state;
			return this;
		}

		
		/**
		 * Timeout.
		 *
		 * @param timeout the timeout
		 * @return the request
		 */
		public Request timeout(TimeValue timeout) {
			this.timeout = timeout;
			return this;
		}
	}

	
	/**
	 * The Class Response.
	 *
	 * @author l.xue.nong
	 */
	public static class Response {

		
		/** The acknowledged. */
		private final boolean acknowledged;

		
		/** The index meta data. */
		private final IndexMetaData indexMetaData;

		
		/**
		 * Instantiates a new response.
		 *
		 * @param acknowledged the acknowledged
		 * @param indexMetaData the index meta data
		 */
		public Response(boolean acknowledged, IndexMetaData indexMetaData) {
			this.acknowledged = acknowledged;
			this.indexMetaData = indexMetaData;
		}

		
		/**
		 * Acknowledged.
		 *
		 * @return true, if successful
		 */
		public boolean acknowledged() {
			return acknowledged;
		}

		
		/**
		 * Index meta data.
		 *
		 * @return the index meta data
		 */
		public IndexMetaData indexMetaData() {
			return indexMetaData;
		}
	}
}
