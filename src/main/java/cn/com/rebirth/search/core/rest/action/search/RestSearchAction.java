/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RestSearchAction.java 2012-3-29 15:01:39 l.xue.nong$$
 */


package cn.com.rebirth.search.core.rest.action.search;

import static cn.com.rebirth.search.core.rest.RestRequest.Method.GET;
import static cn.com.rebirth.search.core.rest.RestRequest.Method.POST;
import static cn.com.rebirth.search.core.rest.RestStatus.BAD_REQUEST;
import static cn.com.rebirth.search.core.rest.action.support.RestXContentBuilder.restContentBuilder;

import java.io.IOException;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.exception.RestartIllegalArgumentException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.search.SearchOperationThreading;
import cn.com.rebirth.search.core.action.search.SearchRequest;
import cn.com.rebirth.search.core.action.search.SearchResponse;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.index.query.QueryBuilders;
import cn.com.rebirth.search.core.index.query.QueryStringQueryBuilder;
import cn.com.rebirth.search.core.rest.BaseRestHandler;
import cn.com.rebirth.search.core.rest.RestChannel;
import cn.com.rebirth.search.core.rest.RestController;
import cn.com.rebirth.search.core.rest.RestRequest;
import cn.com.rebirth.search.core.rest.XContentRestResponse;
import cn.com.rebirth.search.core.rest.XContentThrowableRestResponse;
import cn.com.rebirth.search.core.rest.action.support.RestActions;
import cn.com.rebirth.search.core.search.Scroll;
import cn.com.rebirth.search.core.search.builder.SearchSourceBuilder;
import cn.com.rebirth.search.core.search.sort.SortOrder;


/**
 * The Class RestSearchAction.
 *
 * @author l.xue.nong
 */
public class RestSearchAction extends BaseRestHandler {

	
	/**
	 * Instantiates a new rest search action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestSearchAction(Settings settings, Client client, RestController controller) {
		super(settings, client);
		controller.registerHandler(GET, "/_search", this);
		controller.registerHandler(POST, "/_search", this);
		controller.registerHandler(GET, "/{index}/_search", this);
		controller.registerHandler(POST, "/{index}/_search", this);
		controller.registerHandler(GET, "/{index}/{type}/_search", this);
		controller.registerHandler(POST, "/{index}/{type}/_search", this);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.rest.RestHandler#handleRequest(cn.com.summall.search.core.rest.RestRequest, cn.com.summall.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		SearchRequest searchRequest;
		try {
			searchRequest = parseSearchRequest(request);
			searchRequest.listenerThreaded(false);
			SearchOperationThreading operationThreading = SearchOperationThreading.fromString(
					request.param("operation_threading"), null);
			if (operationThreading != null) {
				if (operationThreading == SearchOperationThreading.NO_THREADS) {
					
					operationThreading = SearchOperationThreading.SINGLE_THREAD;
				}
				searchRequest.operationThreading(operationThreading);
			}
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("failed to parse search request parameters", e);
			}
			try {
				XContentBuilder builder = restContentBuilder(request);
				channel.sendResponse(new XContentRestResponse(request, BAD_REQUEST, builder.startObject()
						.field("error", e.getMessage()).endObject()));
			} catch (IOException e1) {
				logger.error("Failed to send failure response", e1);
			}
			return;
		}
		client.search(searchRequest, new ActionListener<SearchResponse>() {
			@Override
			public void onResponse(SearchResponse response) {
				try {
					XContentBuilder builder = restContentBuilder(request);
					builder.startObject();
					response.toXContent(builder, request);
					builder.endObject();
					channel.sendResponse(new XContentRestResponse(request, response.status(), builder));
				} catch (Exception e) {
					if (logger.isDebugEnabled()) {
						logger.debug("failed to execute search (building response)", e);
					}
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
	 * Parses the search request.
	 *
	 * @param request the request
	 * @return the search request
	 */
	private SearchRequest parseSearchRequest(RestRequest request) {
		String[] indices = RestActions.splitIndices(request.param("index"));
		SearchRequest searchRequest = new SearchRequest(indices);
		
		if (request.hasContent()) {
			searchRequest.source(request.contentByteArray(), request.contentByteArrayOffset(), request.contentLength(),
					request.contentUnsafe());
		} else {
			String source = request.param("source");
			if (source != null) {
				searchRequest.source(source);
			}
		}
		
		searchRequest.extraSource(parseSearchSource(request));

		searchRequest.searchType(request.param("search_type"));

		String scroll = request.param("scroll");
		if (scroll != null) {
			searchRequest.scroll(new Scroll(TimeValue.parseTimeValue(scroll, null)));
		}

		searchRequest.types(RestActions.splitTypes(request.param("type")));
		searchRequest.queryHint(request.param("query_hint"));
		searchRequest.routing(request.param("routing"));
		searchRequest.preference(request.param("preference"));

		return searchRequest;
	}

	
	/**
	 * Parses the search source.
	 *
	 * @param request the request
	 * @return the search source builder
	 */
	private SearchSourceBuilder parseSearchSource(RestRequest request) {
		SearchSourceBuilder searchSourceBuilder = null;
		String queryString = request.param("q");
		if (queryString != null) {
			QueryStringQueryBuilder queryBuilder = QueryBuilders.queryString(queryString);
			queryBuilder.defaultField(request.param("df"));
			queryBuilder.analyzer(request.param("analyzer"));
			queryBuilder.analyzeWildcard(request.paramAsBoolean("analyze_wildcard", false));
			queryBuilder.lowercaseExpandedTerms(request.paramAsBoolean("lowercase_expanded_terms", true));
			String defaultOperator = request.param("default_operator");
			if (defaultOperator != null) {
				if ("OR".equals(defaultOperator)) {
					queryBuilder.defaultOperator(QueryStringQueryBuilder.Operator.OR);
				} else if ("AND".equals(defaultOperator)) {
					queryBuilder.defaultOperator(QueryStringQueryBuilder.Operator.AND);
				} else {
					throw new RestartIllegalArgumentException("Unsupported defaultOperator [" + defaultOperator
							+ "], can either be [OR] or [AND]");
				}
			}
			if (searchSourceBuilder == null) {
				searchSourceBuilder = new SearchSourceBuilder();
			}
			searchSourceBuilder.query(queryBuilder);
		}

		int from = request.paramAsInt("from", -1);
		if (from != -1) {
			if (searchSourceBuilder == null) {
				searchSourceBuilder = new SearchSourceBuilder();
			}
			searchSourceBuilder.from(from);
		}
		int size = request.paramAsInt("size", -1);
		if (size != -1) {
			if (searchSourceBuilder == null) {
				searchSourceBuilder = new SearchSourceBuilder();
			}
			searchSourceBuilder.size(size);
		}

		if (request.hasParam("explain")) {
			if (searchSourceBuilder == null) {
				searchSourceBuilder = new SearchSourceBuilder();
			}
			searchSourceBuilder.explain(request.paramAsBooleanOptional("explain", null));
		}
		if (request.hasParam("version")) {
			if (searchSourceBuilder == null) {
				searchSourceBuilder = new SearchSourceBuilder();
			}
			searchSourceBuilder.version(request.paramAsBooleanOptional("version", null));
		}
		if (request.hasParam("timeout")) {
			if (searchSourceBuilder == null) {
				searchSourceBuilder = new SearchSourceBuilder();
			}
			searchSourceBuilder.timeout(request.paramAsTime("timeout", null));
		}

		String sField = request.param("fields");
		if (sField != null) {
			if (searchSourceBuilder == null) {
				searchSourceBuilder = new SearchSourceBuilder();
			}
			if (!Strings.hasText(sField)) {
				searchSourceBuilder.noFields();
			} else {
				String[] sFields = Strings.splitStringByCommaToArray(sField);
				if (sFields != null) {
					for (String field : sFields) {
						searchSourceBuilder.field(field);
					}
				}
			}
		}

		String sSorts = request.param("sort");
		if (sSorts != null) {
			if (searchSourceBuilder == null) {
				searchSourceBuilder = new SearchSourceBuilder();
			}
			String[] sorts = Strings.splitStringByCommaToArray(sSorts);
			for (String sort : sorts) {
				int delimiter = sort.lastIndexOf(":");
				if (delimiter != -1) {
					String sortField = sort.substring(0, delimiter);
					String reverse = sort.substring(delimiter + 1);
					if ("asc".equals(reverse)) {
						searchSourceBuilder.sort(sortField, SortOrder.ASC);
					} else if ("desc".equals(reverse)) {
						searchSourceBuilder.sort(sortField, SortOrder.DESC);
					}
				} else {
					searchSourceBuilder.sort(sort);
				}
			}
		}

		String sIndicesBoost = request.param("indices_boost");
		if (sIndicesBoost != null) {
			if (searchSourceBuilder == null) {
				searchSourceBuilder = new SearchSourceBuilder();
			}
			String[] indicesBoost = Strings.splitStringByCommaToArray(sIndicesBoost);
			for (String indexBoost : indicesBoost) {
				int divisor = indexBoost.indexOf(',');
				if (divisor == -1) {
					throw new RestartIllegalArgumentException("Illegal index boost [" + indexBoost + "], no ','");
				}
				String indexName = indexBoost.substring(0, divisor);
				String sBoost = indexBoost.substring(divisor + 1);
				try {
					searchSourceBuilder.indexBoost(indexName, Float.parseFloat(sBoost));
				} catch (NumberFormatException e) {
					throw new RestartIllegalArgumentException("Illegal index boost [" + indexBoost
							+ "], boost not a float number");
				}
			}
		}

		String sStats = request.param("stats");
		if (sStats != null) {
			if (searchSourceBuilder == null) {
				searchSourceBuilder = new SearchSourceBuilder();
			}
			searchSourceBuilder.stats(Strings.splitStringByCommaToArray(sStats));
		}

		return searchSourceBuilder;
	}
}
