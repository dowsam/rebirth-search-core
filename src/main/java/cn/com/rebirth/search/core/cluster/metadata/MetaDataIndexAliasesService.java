/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MetaDataIndexAliasesService.java 2012-3-29 15:01:06 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster.metadata;

import static cn.com.rebirth.search.core.cluster.ClusterState.newClusterStateBuilder;
import static cn.com.rebirth.search.core.cluster.metadata.IndexMetaData.newIndexMetaDataBuilder;
import static cn.com.rebirth.search.core.cluster.metadata.MetaData.newMetaDataBuilder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.exception.RestartIllegalArgumentException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentFactory;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.ProcessedClusterStateUpdateTask;
import cn.com.rebirth.search.core.cluster.action.index.NodeAliasesUpdatedAction;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.query.IndexQueryParserService;
import cn.com.rebirth.search.core.index.service.IndexService;
import cn.com.rebirth.search.core.indices.IndexMissingException;
import cn.com.rebirth.search.core.indices.IndicesService;
import cn.com.rebirth.search.core.indices.InvalidAliasNameException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


/**
 * The Class MetaDataIndexAliasesService.
 *
 * @author l.xue.nong
 */
public class MetaDataIndexAliasesService extends AbstractComponent {

	/** The cluster service. */
	private final ClusterService clusterService;

	/** The indices service. */
	private final IndicesService indicesService;

	/** The alias operation performed action. */
	private final NodeAliasesUpdatedAction aliasOperationPerformedAction;

	/**
	 * Instantiates a new meta data index aliases service.
	 *
	 * @param settings the settings
	 * @param clusterService the cluster service
	 * @param indicesService the indices service
	 * @param aliasOperationPerformedAction the alias operation performed action
	 */
	@Inject
	public MetaDataIndexAliasesService(Settings settings, ClusterService clusterService, IndicesService indicesService,
			NodeAliasesUpdatedAction aliasOperationPerformedAction) {
		super(settings);
		this.clusterService = clusterService;
		this.indicesService = indicesService;
		this.aliasOperationPerformedAction = aliasOperationPerformedAction;
	}

	/**
	 * Indices aliases.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	public void indicesAliases(final Request request, final Listener listener) {
		clusterService.submitStateUpdateTask("index-aliases", new ProcessedClusterStateUpdateTask() {
			@Override
			public ClusterState execute(ClusterState currentState) {

				for (AliasAction aliasAction : request.actions) {
					if (!currentState.metaData().hasIndex(aliasAction.index())) {
						listener.onFailure(new IndexMissingException(new Index(aliasAction.index())));
						return currentState;
					}
					if (currentState.metaData().hasIndex(aliasAction.alias())) {
						listener.onFailure(new InvalidAliasNameException(new Index(aliasAction.index()), aliasAction
								.alias(), "an index exists with the same name as the alias"));
						return currentState;
					}
					if (aliasAction.indexRouting() != null && aliasAction.indexRouting().indexOf(',') != -1) {
						listener.onFailure(new RestartIllegalArgumentException("alias [" + aliasAction.alias()
								+ "] has several routing values associated with it"));
						return currentState;
					}
				}

				List<String> indicesToClose = Lists.newArrayList();
				Map<String, IndexService> indices = Maps.newHashMap();
				try {
					boolean changed = false;
					MetaData.Builder builder = newMetaDataBuilder().metaData(currentState.metaData());
					for (AliasAction aliasAction : request.actions) {
						IndexMetaData indexMetaData = builder.get(aliasAction.index());
						if (indexMetaData == null) {
							throw new IndexMissingException(new Index(aliasAction.index()));
						}
						IndexMetaData.Builder indexMetaDataBuilder = newIndexMetaDataBuilder(indexMetaData);
						if (aliasAction.actionType() == AliasAction.Type.ADD) {
							String filter = aliasAction.filter();
							if (Strings.hasLength(filter)) {
								
								IndexService indexService = indices.get(indexMetaData.index());
								if (indexService == null) {
									indexService = indicesService.indexService(indexMetaData.index());
									if (indexService == null) {
										
										try {
											indexService = indicesService.createIndex(indexMetaData.index(),
													indexMetaData.settings(), currentState.nodes().localNode().id());
										} catch (Exception e) {
											logger.warn(
													"[{}] failed to temporary create in order to apply alias action",
													e, indexMetaData.index());
											continue;
										}
										indicesToClose.add(indexMetaData.index());
									}
									indices.put(indexMetaData.index(), indexService);
								}

								
								IndexQueryParserService indexQueryParser = indexService.queryParserService();
								try {
									XContentParser parser = XContentFactory.xContent(filter).createParser(filter);
									try {
										indexQueryParser.parseInnerFilter(parser);
									} finally {
										parser.close();
									}
								} catch (Exception e) {
									listener.onFailure(new RestartIllegalArgumentException(
											"failed to parse filter for [" + aliasAction.alias() + "]", e));
									return currentState;
								}
							}
							AliasMetaData newAliasMd = AliasMetaData.newAliasMetaDataBuilder(aliasAction.alias())
									.filter(filter).indexRouting(aliasAction.indexRouting())
									.searchRouting(aliasAction.searchRouting()).build();
							
							AliasMetaData aliasMd = indexMetaData.aliases().get(aliasAction.alias());
							if (aliasMd != null && aliasMd.equals(newAliasMd)) {
								
								continue;
							}
							indexMetaDataBuilder.putAlias(newAliasMd);
						} else if (aliasAction.actionType() == AliasAction.Type.REMOVE) {
							if (!indexMetaData.aliases().containsKey(aliasAction.alias())) {
								
								continue;
							}
							indexMetaDataBuilder.removerAlias(aliasAction.alias());
						}
						changed = true;
						builder.put(indexMetaDataBuilder);
					}

					if (changed) {
						ClusterState updatedState = newClusterStateBuilder().state(currentState).metaData(builder)
								.build();
						
						int responseCount = updatedState.getNodes().size();
						long version = updatedState.version() + 1;
						logger.trace("Waiting for [{}] notifications with version [{}]", responseCount, version);
						aliasOperationPerformedAction.add(new CountDownListener(responseCount, listener, version),
								request.timeout);

						return updatedState;
					} else {
						
						listener.onResponse(new Response(true));
						return currentState;
					}
				} finally {
					for (String index : indicesToClose) {
						indicesService.cleanIndex(index, "created for alias processing");
					}
				}
			}

			@Override
			public void clusterStateProcessed(ClusterState clusterState) {
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
	 * The Class Request.
	 *
	 * @author l.xue.nong
	 */
	public static class Request {

		/** The actions. */
		final AliasAction[] actions;

		/** The timeout. */
		final TimeValue timeout;

		/**
		 * Instantiates a new request.
		 *
		 * @param actions the actions
		 * @param timeout the timeout
		 */
		public Request(AliasAction[] actions, TimeValue timeout) {
			this.actions = actions;
			this.timeout = timeout;
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
	private class CountDownListener implements NodeAliasesUpdatedAction.Listener {

		/** The notified. */
		private final AtomicBoolean notified = new AtomicBoolean();
		
		/** The count down. */
		private final AtomicInteger countDown;
		
		/** The listener. */
		private final Listener listener;
		
		/** The version. */
		private final long version;

		/**
		 * Instantiates a new count down listener.
		 *
		 * @param countDown the count down
		 * @param listener the listener
		 * @param version the version
		 */
		public CountDownListener(int countDown, Listener listener, long version) {
			this.countDown = new AtomicInteger(countDown);
			this.listener = listener;
			this.version = version;
		}

		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.cluster.action.index.NodeAliasesUpdatedAction.Listener#onAliasesUpdated(cn.com.summall.search.core.cluster.action.index.NodeAliasesUpdatedAction.NodeAliasesUpdatedResponse)
		 */
		@Override
		public void onAliasesUpdated(NodeAliasesUpdatedAction.NodeAliasesUpdatedResponse response) {
			if (version <= response.version()) {
				logger.trace("Received NodeAliasesUpdatedResponse with version [{}] from [{}]", response.version(),
						response.nodeId());
				if (countDown.decrementAndGet() == 0) {
					aliasOperationPerformedAction.remove(this);
					if (notified.compareAndSet(false, true)) {
						listener.onResponse(new Response(true));
					}
				}
			}
		}

		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.cluster.action.index.NodeAliasesUpdatedAction.Listener#onTimeout()
		 */
		@Override
		public void onTimeout() {
			aliasOperationPerformedAction.remove(this);
			if (notified.compareAndSet(false, true)) {
				listener.onResponse(new Response(false));
			}
		}
	}
}
