/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestIndicesStatsAction.java 2012-7-6 14:29:06 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.action.admin.indices.stats;

import java.io.IOException;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.stats.IndicesStats;
import cn.com.rebirth.search.core.action.admin.indices.stats.IndicesStatsRequest;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.rest.BaseRestHandler;
import cn.com.rebirth.search.core.rest.RestChannel;
import cn.com.rebirth.search.core.rest.RestController;
import cn.com.rebirth.search.core.rest.RestHandler;
import cn.com.rebirth.search.core.rest.RestRequest;
import cn.com.rebirth.search.core.rest.XContentRestResponse;
import cn.com.rebirth.search.core.rest.XContentThrowableRestResponse;
import cn.com.rebirth.search.core.rest.action.support.RestXContentBuilder;
import static cn.com.rebirth.search.core.rest.RestRequest.Method.GET;
import static cn.com.rebirth.search.core.rest.RestStatus.OK;
import static cn.com.rebirth.search.core.rest.action.support.RestActions.*;

/**
 * The Class RestIndicesStatsAction.
 *
 * @author l.xue.nong
 */
public class RestIndicesStatsAction extends BaseRestHandler {

	/**
	 * Instantiates a new rest indices stats action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestIndicesStatsAction(Settings settings, Client client, RestController controller) {
		super(settings, client);
		controller.registerHandler(GET, "/_stats", this);
		controller.registerHandler(GET, "/{index}/_stats", this);

		controller.registerHandler(GET, "_stats/docs", new RestDocsStatsHandler());
		controller.registerHandler(GET, "/{index}/_stats/docs", new RestDocsStatsHandler());

		controller.registerHandler(GET, "/_stats/store", new RestStoreStatsHandler());
		controller.registerHandler(GET, "/{index}/_stats/store", new RestStoreStatsHandler());

		controller.registerHandler(GET, "/_stats/indexing", new RestIndexingStatsHandler());
		controller.registerHandler(GET, "/{index}/_stats/indexing", new RestIndexingStatsHandler());
		controller.registerHandler(GET, "/_stats/indexing/{indexingTypes1}", new RestIndexingStatsHandler());
		controller.registerHandler(GET, "/{index}/_stats/indexing/{indexingTypes2}", new RestIndexingStatsHandler());

		controller.registerHandler(GET, "/_stats/search", new RestSearchStatsHandler());
		controller.registerHandler(GET, "/{index}/_stats/search", new RestSearchStatsHandler());
		controller.registerHandler(GET, "/_stats/search/{searchGroupsStats1}", new RestSearchStatsHandler());
		controller.registerHandler(GET, "/{index}/_stats/search/{searchGroupsStats2}", new RestSearchStatsHandler());

		controller.registerHandler(GET, "/_stats/get", new RestGetStatsHandler());
		controller.registerHandler(GET, "/{index}/_stats/get", new RestGetStatsHandler());

		controller.registerHandler(GET, "/_stats/refresh", new RestRefreshStatsHandler());
		controller.registerHandler(GET, "/{index}/_stats/refresh", new RestRefreshStatsHandler());

		controller.registerHandler(GET, "/_stats/merge", new RestMergeStatsHandler());
		controller.registerHandler(GET, "/{index}/_stats/merge", new RestMergeStatsHandler());

		controller.registerHandler(GET, "/_stats/flush", new RestFlushStatsHandler());
		controller.registerHandler(GET, "/{index}/_stats/flush", new RestFlushStatsHandler());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		IndicesStatsRequest indicesStatsRequest = new IndicesStatsRequest();
		boolean clear = request.paramAsBoolean("clear", false);
		if (clear) {
			indicesStatsRequest.clear();
		}
		boolean all = request.paramAsBoolean("all", false);
		if (all) {
			indicesStatsRequest.all();
		}
		indicesStatsRequest.indices(splitIndices(request.param("index")));
		indicesStatsRequest.types(splitTypes(request.param("types")));
		if (request.hasParam("groups")) {
			indicesStatsRequest.groups(Strings.splitStringByCommaToArray(request.param("groups")));
		}
		indicesStatsRequest.docs(request.paramAsBoolean("docs", indicesStatsRequest.docs()));
		indicesStatsRequest.store(request.paramAsBoolean("store", indicesStatsRequest.store()));
		indicesStatsRequest.indexing(request.paramAsBoolean("indexing", indicesStatsRequest.indexing()));
		indicesStatsRequest.search(request.paramAsBoolean("search", indicesStatsRequest.search()));
		indicesStatsRequest.get(request.paramAsBoolean("get", indicesStatsRequest.get()));
		indicesStatsRequest.merge(request.paramAsBoolean("merge", indicesStatsRequest.merge()));
		indicesStatsRequest.refresh(request.paramAsBoolean("refresh", indicesStatsRequest.refresh()));
		indicesStatsRequest.flush(request.paramAsBoolean("flush", indicesStatsRequest.flush()));

		client.admin().indices().stats(indicesStatsRequest, new ActionListener<IndicesStats>() {
			@Override
			public void onResponse(IndicesStats response) {
				try {
					XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
					builder.startObject();
					builder.field("ok", true);
					buildBroadcastShardsHeader(builder, response);
					response.toXContent(builder, request);
					builder.endObject();
					channel.sendResponse(new XContentRestResponse(request, OK, builder));
				} catch (Exception e) {
					onFailure(e);
				}
			}

			@Override
			public void onFailure(Throwable e) {
				try {
					channel.sendResponse(new XContentThrowableRestResponse(request, e));
				} catch (IOException e1) {
					logger.error("Failed to send failure response", e1);
				}
			}
		});
	}

	/**
	 * The Class RestDocsStatsHandler.
	 *
	 * @author l.xue.nong
	 */
	class RestDocsStatsHandler implements RestHandler {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
		 */
		@Override
		public void handleRequest(final RestRequest request, final RestChannel channel) {
			IndicesStatsRequest indicesStatsRequest = new IndicesStatsRequest();
			indicesStatsRequest.clear().docs(true);
			indicesStatsRequest.indices(splitIndices(request.param("index")));
			indicesStatsRequest.types(splitTypes(request.param("types")));

			client.admin().indices().stats(indicesStatsRequest, new ActionListener<IndicesStats>() {
				@Override
				public void onResponse(IndicesStats response) {
					try {
						XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
						builder.startObject();
						builder.field("ok", true);
						buildBroadcastShardsHeader(builder, response);
						response.toXContent(builder, request);
						builder.endObject();
						channel.sendResponse(new XContentRestResponse(request, OK, builder));
					} catch (Exception e) {
						onFailure(e);
					}
				}

				@Override
				public void onFailure(Throwable e) {
					try {
						channel.sendResponse(new XContentThrowableRestResponse(request, e));
					} catch (IOException e1) {
						logger.error("Failed to send failure response", e1);
					}
				}
			});
		}
	}

	/**
	 * The Class RestStoreStatsHandler.
	 *
	 * @author l.xue.nong
	 */
	class RestStoreStatsHandler implements RestHandler {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
		 */
		@Override
		public void handleRequest(final RestRequest request, final RestChannel channel) {
			IndicesStatsRequest indicesStatsRequest = new IndicesStatsRequest();
			indicesStatsRequest.clear().store(true);
			indicesStatsRequest.indices(splitIndices(request.param("index")));
			indicesStatsRequest.types(splitTypes(request.param("types")));

			client.admin().indices().stats(indicesStatsRequest, new ActionListener<IndicesStats>() {
				@Override
				public void onResponse(IndicesStats response) {
					try {
						XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
						builder.startObject();
						builder.field("ok", true);
						buildBroadcastShardsHeader(builder, response);
						response.toXContent(builder, request);
						builder.endObject();
						channel.sendResponse(new XContentRestResponse(request, OK, builder));
					} catch (Exception e) {
						onFailure(e);
					}
				}

				@Override
				public void onFailure(Throwable e) {
					try {
						channel.sendResponse(new XContentThrowableRestResponse(request, e));
					} catch (IOException e1) {
						logger.error("Failed to send failure response", e1);
					}
				}
			});
		}
	}

	/**
	 * The Class RestIndexingStatsHandler.
	 *
	 * @author l.xue.nong
	 */
	class RestIndexingStatsHandler implements RestHandler {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
		 */
		@Override
		public void handleRequest(final RestRequest request, final RestChannel channel) {
			IndicesStatsRequest indicesStatsRequest = new IndicesStatsRequest();
			indicesStatsRequest.clear().indexing(true);
			indicesStatsRequest.indices(splitIndices(request.param("index")));
			if (request.hasParam("types")) {
				indicesStatsRequest.types(splitTypes(request.param("types")));
			} else if (request.hasParam("indexingTypes1")) {
				indicesStatsRequest.types(splitTypes(request.param("indexingTypes1")));
			} else if (request.hasParam("indexingTypes2")) {
				indicesStatsRequest.types(splitTypes(request.param("indexingTypes2")));
			}

			client.admin().indices().stats(indicesStatsRequest, new ActionListener<IndicesStats>() {
				@Override
				public void onResponse(IndicesStats response) {
					try {
						XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
						builder.startObject();
						builder.field("ok", true);
						buildBroadcastShardsHeader(builder, response);
						response.toXContent(builder, request);
						builder.endObject();
						channel.sendResponse(new XContentRestResponse(request, OK, builder));
					} catch (Exception e) {
						onFailure(e);
					}
				}

				@Override
				public void onFailure(Throwable e) {
					try {
						channel.sendResponse(new XContentThrowableRestResponse(request, e));
					} catch (IOException e1) {
						logger.error("Failed to send failure response", e1);
					}
				}
			});
		}
	}

	/**
	 * The Class RestSearchStatsHandler.
	 *
	 * @author l.xue.nong
	 */
	class RestSearchStatsHandler implements RestHandler {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
		 */
		@Override
		public void handleRequest(final RestRequest request, final RestChannel channel) {
			IndicesStatsRequest indicesStatsRequest = new IndicesStatsRequest();
			indicesStatsRequest.clear().search(true);
			indicesStatsRequest.indices(splitIndices(request.param("index")));
			if (request.hasParam("groups")) {
				indicesStatsRequest.groups(Strings.splitStringByCommaToArray(request.param("groups")));
			} else if (request.hasParam("searchGroupsStats1")) {
				indicesStatsRequest.groups(Strings.splitStringByCommaToArray(request.param("searchGroupsStats1")));
			} else if (request.hasParam("searchGroupsStats2")) {
				indicesStatsRequest.groups(Strings.splitStringByCommaToArray(request.param("searchGroupsStats2")));
			}

			client.admin().indices().stats(indicesStatsRequest, new ActionListener<IndicesStats>() {
				@Override
				public void onResponse(IndicesStats response) {
					try {
						XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
						builder.startObject();
						builder.field("ok", true);
						buildBroadcastShardsHeader(builder, response);
						response.toXContent(builder, request);
						builder.endObject();
						channel.sendResponse(new XContentRestResponse(request, OK, builder));
					} catch (Exception e) {
						onFailure(e);
					}
				}

				@Override
				public void onFailure(Throwable e) {
					try {
						channel.sendResponse(new XContentThrowableRestResponse(request, e));
					} catch (IOException e1) {
						logger.error("Failed to send failure response", e1);
					}
				}
			});
		}
	}

	/**
	 * The Class RestGetStatsHandler.
	 *
	 * @author l.xue.nong
	 */
	class RestGetStatsHandler implements RestHandler {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
		 */
		@Override
		public void handleRequest(final RestRequest request, final RestChannel channel) {
			IndicesStatsRequest indicesStatsRequest = new IndicesStatsRequest();
			indicesStatsRequest.clear().get(true);
			indicesStatsRequest.indices(splitIndices(request.param("index")));

			client.admin().indices().stats(indicesStatsRequest, new ActionListener<IndicesStats>() {
				@Override
				public void onResponse(IndicesStats response) {
					try {
						XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
						builder.startObject();
						builder.field("ok", true);
						buildBroadcastShardsHeader(builder, response);
						response.toXContent(builder, request);
						builder.endObject();
						channel.sendResponse(new XContentRestResponse(request, OK, builder));
					} catch (Exception e) {
						onFailure(e);
					}
				}

				@Override
				public void onFailure(Throwable e) {
					try {
						channel.sendResponse(new XContentThrowableRestResponse(request, e));
					} catch (IOException e1) {
						logger.error("Failed to send failure response", e1);
					}
				}
			});
		}
	}

	/**
	 * The Class RestMergeStatsHandler.
	 *
	 * @author l.xue.nong
	 */
	class RestMergeStatsHandler implements RestHandler {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
		 */
		@Override
		public void handleRequest(final RestRequest request, final RestChannel channel) {
			IndicesStatsRequest indicesStatsRequest = new IndicesStatsRequest();
			indicesStatsRequest.clear().merge(true);
			indicesStatsRequest.indices(splitIndices(request.param("index")));
			indicesStatsRequest.types(splitTypes(request.param("types")));

			client.admin().indices().stats(indicesStatsRequest, new ActionListener<IndicesStats>() {
				@Override
				public void onResponse(IndicesStats response) {
					try {
						XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
						builder.startObject();
						builder.field("ok", true);
						buildBroadcastShardsHeader(builder, response);
						response.toXContent(builder, request);
						builder.endObject();
						channel.sendResponse(new XContentRestResponse(request, OK, builder));
					} catch (Exception e) {
						onFailure(e);
					}
				}

				@Override
				public void onFailure(Throwable e) {
					try {
						channel.sendResponse(new XContentThrowableRestResponse(request, e));
					} catch (IOException e1) {
						logger.error("Failed to send failure response", e1);
					}
				}
			});
		}
	}

	/**
	 * The Class RestFlushStatsHandler.
	 *
	 * @author l.xue.nong
	 */
	class RestFlushStatsHandler implements RestHandler {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
		 */
		@Override
		public void handleRequest(final RestRequest request, final RestChannel channel) {
			IndicesStatsRequest indicesStatsRequest = new IndicesStatsRequest();
			indicesStatsRequest.clear().flush(true);
			indicesStatsRequest.indices(splitIndices(request.param("index")));
			indicesStatsRequest.types(splitTypes(request.param("types")));

			client.admin().indices().stats(indicesStatsRequest, new ActionListener<IndicesStats>() {
				@Override
				public void onResponse(IndicesStats response) {
					try {
						XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
						builder.startObject();
						builder.field("ok", true);
						buildBroadcastShardsHeader(builder, response);
						response.toXContent(builder, request);
						builder.endObject();
						channel.sendResponse(new XContentRestResponse(request, OK, builder));
					} catch (Exception e) {
						onFailure(e);
					}
				}

				@Override
				public void onFailure(Throwable e) {
					try {
						channel.sendResponse(new XContentThrowableRestResponse(request, e));
					} catch (IOException e1) {
						logger.error("Failed to send failure response", e1);
					}
				}
			});
		}
	}

	/**
	 * The Class RestRefreshStatsHandler.
	 *
	 * @author l.xue.nong
	 */
	class RestRefreshStatsHandler implements RestHandler {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.rest.RestHandler#handleRequest(cn.com.rebirth.search.core.rest.RestRequest, cn.com.rebirth.search.core.rest.RestChannel)
		 */
		@Override
		public void handleRequest(final RestRequest request, final RestChannel channel) {
			IndicesStatsRequest indicesStatsRequest = new IndicesStatsRequest();
			indicesStatsRequest.clear().refresh(true);
			indicesStatsRequest.indices(splitIndices(request.param("index")));
			indicesStatsRequest.types(splitTypes(request.param("types")));

			client.admin().indices().stats(indicesStatsRequest, new ActionListener<IndicesStats>() {
				@Override
				public void onResponse(IndicesStats response) {
					try {
						XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
						builder.startObject();
						builder.field("ok", true);
						buildBroadcastShardsHeader(builder, response);
						response.toXContent(builder, request);
						builder.endObject();
						channel.sendResponse(new XContentRestResponse(request, OK, builder));
					} catch (Exception e) {
						onFailure(e);
					}
				}

				@Override
				public void onFailure(Throwable e) {
					try {
						channel.sendResponse(new XContentThrowableRestResponse(request, e));
					} catch (IOException e1) {
						logger.error("Failed to send failure response", e1);
					}
				}
			});
		}
	}
}
