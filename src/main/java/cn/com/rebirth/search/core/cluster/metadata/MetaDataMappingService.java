/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MetaDataMappingService.java 2012-7-6 14:30:25 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.metadata;

import static cn.com.rebirth.search.core.cluster.ClusterState.newClusterStateBuilder;
import static cn.com.rebirth.search.core.cluster.metadata.IndexMetaData.newIndexMetaDataBuilder;
import static cn.com.rebirth.search.core.cluster.metadata.MetaData.newMetaDataBuilder;
import static cn.com.rebirth.search.core.index.mapper.DocumentMapper.MergeFlags.mergeFlags;
import static com.google.common.collect.Maps.newHashMap;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import cn.com.rebirth.commons.compress.CompressedString;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.ClusterStateUpdateTask;
import cn.com.rebirth.search.core.cluster.ProcessedClusterStateUpdateTask;
import cn.com.rebirth.search.core.cluster.action.index.NodeMappingCreatedAction;
import cn.com.rebirth.search.core.cluster.routing.IndexRoutingTable;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.mapper.DocumentMapper;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.mapper.MergeMappingException;
import cn.com.rebirth.search.core.index.service.IndexService;
import cn.com.rebirth.search.core.indices.IndexMissingException;
import cn.com.rebirth.search.core.indices.IndicesService;
import cn.com.rebirth.search.core.indices.InvalidTypeNameException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * The Class MetaDataMappingService.
 *
 * @author l.xue.nong
 */
public class MetaDataMappingService extends AbstractComponent {

	/** The cluster service. */
	private final ClusterService clusterService;

	/** The indices service. */
	private final IndicesService indicesService;

	/** The mapping created action. */
	private final NodeMappingCreatedAction mappingCreatedAction;

	/** The indices and types to refresh. */
	private final Map<String, Set<String>> indicesAndTypesToRefresh = Maps.newHashMap();

	/**
	 * Instantiates a new meta data mapping service.
	 *
	 * @param settings the settings
	 * @param clusterService the cluster service
	 * @param indicesService the indices service
	 * @param mappingCreatedAction the mapping created action
	 */
	@Inject
	public MetaDataMappingService(Settings settings, ClusterService clusterService, IndicesService indicesService,
			NodeMappingCreatedAction mappingCreatedAction) {
		super(settings);
		this.clusterService = clusterService;
		this.indicesService = indicesService;
		this.mappingCreatedAction = mappingCreatedAction;
	}

	/**
	 * Refresh mapping.
	 *
	 * @param index the index
	 * @param types the types
	 */
	public void refreshMapping(final String index, final String... types) {
		synchronized (indicesAndTypesToRefresh) {
			Set<String> sTypes = indicesAndTypesToRefresh.get(index);
			if (sTypes == null) {
				sTypes = Sets.newHashSet();
				indicesAndTypesToRefresh.put(index, sTypes);
			}
			sTypes.addAll(Arrays.asList(types));
		}
		clusterService.submitStateUpdateTask("refresh-mapping [" + index + "][" + Arrays.toString(types) + "]",
				new ClusterStateUpdateTask() {
					@Override
					public ClusterState execute(ClusterState currentState) {
						boolean createdIndex = false;
						try {
							Set<String> sTypes;
							synchronized (indicesAndTypesToRefresh) {
								sTypes = indicesAndTypesToRefresh.remove(index);
							}

							if (sTypes == null || sTypes.isEmpty()) {
								return currentState;
							}

							final IndexMetaData indexMetaData = currentState.metaData().index(index);
							if (indexMetaData == null) {

								return currentState;
							}

							IndexService indexService = indicesService.indexService(index);
							if (indexService == null) {

								indexService = indicesService.createIndex(indexMetaData.index(),
										indexMetaData.settings(), currentState.nodes().localNode().id());
								createdIndex = true;
								for (String type : sTypes) {

									if (indexMetaData.mappings().containsKey(type)) {
										indexService.mapperService().add(type,
												indexMetaData.mappings().get(type).source().string());
									}
								}
							}
							IndexMetaData.Builder indexMetaDataBuilder = newIndexMetaDataBuilder(indexMetaData);
							List<String> updatedTypes = Lists.newArrayList();
							for (String type : sTypes) {
								DocumentMapper mapper = indexService.mapperService().documentMapper(type);
								if (!mapper.mappingSource().equals(indexMetaData.mappings().get(type).source())) {
									updatedTypes.add(type);
									indexMetaDataBuilder.putMapping(new MappingMetaData(mapper));
								}
							}

							if (updatedTypes.isEmpty()) {
								return currentState;
							}

							logger.warn("[{}] re-syncing mappings with cluster state for types [{}]", index,
									updatedTypes);
							MetaData.Builder builder = newMetaDataBuilder().metaData(currentState.metaData());
							builder.put(indexMetaDataBuilder);
							return newClusterStateBuilder().state(currentState).metaData(builder).build();
						} catch (Exception e) {
							logger.warn("failed to dynamically refresh the mapping in cluster_state from shard", e);
							return currentState;
						} finally {
							if (createdIndex) {
								indicesService.cleanIndex(index, "created for mapping processing");
							}
						}
					}
				});
	}

	/**
	 * Update mapping.
	 *
	 * @param index the index
	 * @param type the type
	 * @param mappingSource the mapping source
	 * @param listener the listener
	 */
	public void updateMapping(final String index, final String type, final CompressedString mappingSource,
			final Listener listener) {
		clusterService.submitStateUpdateTask("update-mapping [" + index + "][" + type + "]",
				new ProcessedClusterStateUpdateTask() {
					@Override
					public ClusterState execute(ClusterState currentState) {
						boolean createdIndex = false;
						try {

							final IndexMetaData indexMetaData = currentState.metaData().index(index);
							if (indexMetaData == null) {

								return currentState;
							}
							if (indexMetaData.mappings().containsKey(type)
									&& indexMetaData.mapping(type).source().equals(mappingSource)) {
								return currentState;
							}

							IndexService indexService = indicesService.indexService(index);
							if (indexService == null) {

								indexService = indicesService.createIndex(indexMetaData.index(),
										indexMetaData.settings(), currentState.nodes().localNode().id());
								createdIndex = true;

								if (indexMetaData.mappings().containsKey(type)) {
									indexService.mapperService().add(type,
											indexMetaData.mappings().get(type).source().string());
								}
							}
							MapperService mapperService = indexService.mapperService();

							DocumentMapper existingMapper = mapperService.documentMapper(type);

							DocumentMapper updatedMapper = mapperService.parse(type, mappingSource.string());
							if (existingMapper == null) {
								existingMapper = updatedMapper;
							} else {

								existingMapper.merge(updatedMapper, mergeFlags().simulate(false));
							}

							if (indexMetaData.mappings().containsKey(type)
									&& indexMetaData.mapping(type).source().equals(existingMapper.mappingSource())) {
								return currentState;
							}

							if (logger.isDebugEnabled()) {
								try {
									logger.debug("[" + index + "] update_mapping [{}] (dynamic) with source [{}]",
											type, existingMapper.mappingSource().string());
								} catch (IOException e) {

								}
							} else if (logger.isInfoEnabled()) {
								logger.info("[{}] update_mapping [{}] (dynamic)", index, type);
							}

							MetaData.Builder builder = newMetaDataBuilder().metaData(currentState.metaData());
							builder.put(newIndexMetaDataBuilder(indexMetaData).putMapping(
									new MappingMetaData(existingMapper)));
							return newClusterStateBuilder().state(currentState).metaData(builder).build();
						} catch (Exception e) {
							logger.warn("failed to dynamically update the mapping in cluster_state from shard", e);
							listener.onFailure(e);
							return currentState;
						} finally {
							if (createdIndex) {
								indicesService.cleanIndex(index, "created for mapping processing");
							}
						}
					}

					@Override
					public void clusterStateProcessed(ClusterState clusterState) {
						listener.onResponse(new Response(true));
					}
				});
	}

	/**
	 * Removes the mapping.
	 *
	 * @param request the request
	 */
	public void removeMapping(final RemoveRequest request) {
		clusterService.submitStateUpdateTask("remove-mapping [" + request.mappingType + "]",
				new ProcessedClusterStateUpdateTask() {
					@Override
					public ClusterState execute(ClusterState currentState) {
						if (request.indices.length == 0) {
							throw new IndexMissingException(new Index("_all"));
						}

						MetaData.Builder builder = newMetaDataBuilder().metaData(currentState.metaData());
						boolean changed = false;
						for (String indexName : request.indices) {
							IndexMetaData indexMetaData = currentState.metaData().index(indexName);
							if (indexMetaData != null) {
								if (indexMetaData.mappings().containsKey(request.mappingType)) {
									builder.put(newIndexMetaDataBuilder(indexMetaData).removeMapping(
											request.mappingType));
									changed = true;
								}
							}
						}

						if (!changed) {
							return currentState;
						}

						logger.info("[{}] remove_mapping [{}]", request.indices, request.mappingType);

						return ClusterState.builder().state(currentState).metaData(builder).build();
					}

					@Override
					public void clusterStateProcessed(ClusterState clusterState) {

					}
				});
	}

	/**
	 * Put mapping.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	public void putMapping(final PutRequest request, final Listener listener) {
		final AtomicBoolean notifyOnPostProcess = new AtomicBoolean();
		clusterService.submitStateUpdateTask("put-mapping [" + request.mappingType + "]",
				new ProcessedClusterStateUpdateTask() {
					@Override
					public ClusterState execute(ClusterState currentState) {
						List<String> indicesToClose = Lists.newArrayList();
						try {
							if (request.indices.length == 0) {
								throw new IndexMissingException(new Index("_all"));
							}
							for (String index : request.indices) {
								if (!currentState.metaData().hasIndex(index)) {
									listener.onFailure(new IndexMissingException(new Index(index)));
								}
							}

							for (String index : request.indices) {
								if (indicesService.hasIndex(index)) {
									continue;
								}
								final IndexMetaData indexMetaData = currentState.metaData().index(index);
								IndexService indexService = indicesService.createIndex(indexMetaData.index(),
										indexMetaData.settings(), currentState.nodes().localNode().id());
								indicesToClose.add(indexMetaData.index());

								if (indexMetaData.mappings().containsKey(request.mappingType)) {
									indexService.mapperService().add(request.mappingType,
											indexMetaData.mappings().get(request.mappingType).source().string());
								}
							}

							Map<String, DocumentMapper> newMappers = newHashMap();
							Map<String, DocumentMapper> existingMappers = newHashMap();
							for (String index : request.indices) {
								IndexService indexService = indicesService.indexService(index);
								if (indexService != null) {

									DocumentMapper newMapper = indexService.mapperService().parse(request.mappingType,
											request.mappingSource);
									newMappers.put(index, newMapper);
									DocumentMapper existingMapper = indexService.mapperService().documentMapper(
											request.mappingType);
									if (existingMapper != null) {

										DocumentMapper.MergeResult mergeResult = existingMapper.merge(newMapper,
												mergeFlags().simulate(true));

										if (!request.ignoreConflicts && mergeResult.hasConflicts()) {
											throw new MergeMappingException(mergeResult.conflicts());
										}
										existingMappers.put(index, existingMapper);
									}
								} else {
									throw new IndexMissingException(new Index(index));
								}
							}

							String mappingType = request.mappingType;
							if (mappingType == null) {
								mappingType = newMappers.values().iterator().next().type();
							} else if (!mappingType.equals(newMappers.values().iterator().next().type())) {
								throw new InvalidTypeNameException(
										"Type name provided does not match type name within mapping definition");
							}
							if (!MapperService.DEFAULT_MAPPING.equals(mappingType) && mappingType.charAt(0) == '_') {
								throw new InvalidTypeNameException("Document mapping type name can't start with '_'");
							}

							final Map<String, MappingMetaData> mappings = newHashMap();
							for (Map.Entry<String, DocumentMapper> entry : newMappers.entrySet()) {
								String index = entry.getKey();

								DocumentMapper newMapper = entry.getValue();
								if (existingMappers.containsKey(entry.getKey())) {

									DocumentMapper existingMapper = existingMappers.get(entry.getKey());
									CompressedString existingSource = existingMapper.mappingSource();

									existingMapper.merge(newMapper, mergeFlags().simulate(false));

									CompressedString updatedSource = existingMapper.mappingSource();
									if (existingSource.equals(updatedSource)) {

									} else {

										mappings.put(index, new MappingMetaData(existingMapper));
										if (logger.isDebugEnabled()) {
											logger.debug("[" + index + "] update_mapping [{}] with source [{}]",
													existingMapper.type(), updatedSource);
										} else if (logger.isInfoEnabled()) {
											logger.info("[{}] update_mapping [{}]", index, existingMapper.type());
										}
									}
								} else {
									CompressedString newSource = newMapper.mappingSource();
									mappings.put(index, new MappingMetaData(newMapper));

									IndexService indexService = indicesService.indexService(index);
									indexService.mapperService().add(newMapper.type(),
											newMapper.mappingSource().string());
									if (logger.isDebugEnabled()) {
										logger.debug("[" + index + "] create_mapping [{}] with source [{}]",
												newMapper.type(), newSource);
									} else if (logger.isInfoEnabled()) {
										logger.info("[{}] create_mapping [{}]", index, newMapper.type());
									}
								}
							}

							if (mappings.isEmpty()) {

								listener.onResponse(new Response(true));
								return currentState;
							}

							MetaData.Builder builder = newMetaDataBuilder().metaData(currentState.metaData());
							for (String indexName : request.indices) {
								IndexMetaData indexMetaData = currentState.metaData().index(indexName);
								if (indexMetaData == null) {
									throw new IndexMissingException(new Index(indexName));
								}
								MappingMetaData mappingMd = mappings.get(indexName);
								if (mappingMd != null) {
									builder.put(newIndexMetaDataBuilder(indexMetaData).putMapping(mappingMd));
								}
							}

							ClusterState updatedState = newClusterStateBuilder().state(currentState).metaData(builder)
									.build();

							int counter = 0;
							for (String index : request.indices) {
								IndexRoutingTable indexRoutingTable = updatedState.routingTable().index(index);
								if (indexRoutingTable != null) {
									counter += indexRoutingTable.numberOfNodesShardsAreAllocatedOn(updatedState.nodes()
											.masterNodeId());
								}
							}

							if (counter == 0) {
								notifyOnPostProcess.set(true);
								return updatedState;
							}
							mappingCreatedAction.add(new CountDownListener(counter, listener), request.timeout);
							return updatedState;
						} catch (Exception e) {
							listener.onFailure(e);
							return currentState;
						} finally {
							for (String index : indicesToClose) {
								indicesService.cleanIndex(index, "created for mapping processing");
							}
						}
					}

					@Override
					public void clusterStateProcessed(ClusterState clusterState) {
						if (notifyOnPostProcess.get()) {
							listener.onResponse(new Response(true));
						}
					}
				});
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
	 * The Class RemoveRequest.
	 *
	 * @author l.xue.nong
	 */
	public static class RemoveRequest {

		/** The indices. */
		final String[] indices;

		/** The mapping type. */
		final String mappingType;

		/**
		 * Instantiates a new removes the request.
		 *
		 * @param indices the indices
		 * @param mappingType the mapping type
		 */
		public RemoveRequest(String[] indices, String mappingType) {
			this.indices = indices;
			this.mappingType = mappingType;
		}
	}

	/**
	 * The Class PutRequest.
	 *
	 * @author l.xue.nong
	 */
	public static class PutRequest {

		/** The indices. */
		final String[] indices;

		/** The mapping type. */
		final String mappingType;

		/** The mapping source. */
		final String mappingSource;

		/** The ignore conflicts. */
		boolean ignoreConflicts = false;

		/** The timeout. */
		TimeValue timeout = TimeValue.timeValueSeconds(10);

		/**
		 * Instantiates a new put request.
		 *
		 * @param indices the indices
		 * @param mappingType the mapping type
		 * @param mappingSource the mapping source
		 */
		public PutRequest(String[] indices, String mappingType, String mappingSource) {
			this.indices = indices;
			this.mappingType = mappingType;
			this.mappingSource = mappingSource;
		}

		/**
		 * Ignore conflicts.
		 *
		 * @param ignoreConflicts the ignore conflicts
		 * @return the put request
		 */
		public PutRequest ignoreConflicts(boolean ignoreConflicts) {
			this.ignoreConflicts = ignoreConflicts;
			return this;
		}

		/**
		 * Timeout.
		 *
		 * @param timeout the timeout
		 * @return the put request
		 */
		public PutRequest timeout(TimeValue timeout) {
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

		/**
		 * Instantiates a new response.
		 *
		 * @param acknowledged the acknowledged
		 */
		public Response(boolean acknowledged) {
			this.acknowledged = acknowledged;
		}

		/**
		 * Acknowledged.
		 *
		 * @return true, if successful
		 */
		public boolean acknowledged() {
			return acknowledged;
		}
	}

	/**
	 * The listener interface for receiving countDown events.
	 * The class that is interested in processing a countDown
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addCountDownListener<code> method. When
	 * the countDown event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see CountDownEvent
	 */
	private class CountDownListener implements NodeMappingCreatedAction.Listener {

		/** The notified. */
		private final AtomicBoolean notified = new AtomicBoolean();

		/** The count down. */
		private final AtomicInteger countDown;

		/** The listener. */
		private final Listener listener;

		/**
		 * Instantiates a new count down listener.
		 *
		 * @param countDown the count down
		 * @param listener the listener
		 */
		public CountDownListener(int countDown, Listener listener) {
			this.countDown = new AtomicInteger(countDown);
			this.listener = listener;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.cluster.action.index.NodeMappingCreatedAction.Listener#onNodeMappingCreated(cn.com.rebirth.search.core.cluster.action.index.NodeMappingCreatedAction.NodeMappingCreatedResponse)
		 */
		@Override
		public void onNodeMappingCreated(NodeMappingCreatedAction.NodeMappingCreatedResponse response) {
			if (countDown.decrementAndGet() == 0) {
				mappingCreatedAction.remove(this);
				if (notified.compareAndSet(false, true)) {
					listener.onResponse(new Response(true));
				}
			}
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.cluster.action.index.NodeMappingCreatedAction.Listener#onTimeout()
		 */
		@Override
		public void onTimeout() {
			mappingCreatedAction.remove(this);
			if (notified.compareAndSet(false, true)) {
				listener.onResponse(new Response(false));
			}
		}
	}
}
