/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestActions.java 2012-7-6 14:28:45 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.action.support;

import java.io.IOException;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.search.commons.io.BytesStream;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.action.ShardOperationFailedException;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationResponse;
import cn.com.rebirth.search.core.index.query.QueryBuilders;
import cn.com.rebirth.search.core.index.query.QueryStringQueryBuilder;
import cn.com.rebirth.search.core.rest.RestRequest;

/**
 * The Class RestActions.
 *
 * @author l.xue.nong
 */
public class RestActions {

	/**
	 * Parses the version.
	 *
	 * @param request the request
	 * @return the long
	 */
	public static long parseVersion(RestRequest request) {
		if (request.hasParam("version")) {
			return request.paramAsLong("version", 0);
		}
		String ifMatch = request.header("If-Match");
		if (ifMatch != null) {
			return Long.parseLong(ifMatch);
		}
		return 0;
	}

	/**
	 * Builds the broadcast shards header.
	 *
	 * @param builder the builder
	 * @param response the response
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void buildBroadcastShardsHeader(XContentBuilder builder, BroadcastOperationResponse response)
			throws IOException {
		builder.startObject("_shards");
		builder.field("total", response.totalShards());
		builder.field("successful", response.successfulShards());
		builder.field("failed", response.failedShards());
		if (!response.shardFailures().isEmpty()) {
			builder.startArray("failures");
			for (ShardOperationFailedException shardFailure : response.shardFailures()) {
				builder.startObject();
				if (shardFailure.index() != null) {
					builder.field("index", shardFailure.index(), XContentBuilder.FieldCaseConversion.NONE);
				}
				if (shardFailure.shardId() != -1) {
					builder.field("shard", shardFailure.shardId());
				}
				builder.field("reason", shardFailure.reason());
				builder.endObject();
			}
			builder.endArray();
		}
		builder.endObject();
	}

	/**
	 * Parses the query source.
	 *
	 * @param request the request
	 * @return the bytes stream
	 */
	public static BytesStream parseQuerySource(RestRequest request) {
		String queryString = request.param("q");
		if (queryString == null) {
			return null;
		}
		QueryStringQueryBuilder queryBuilder = QueryBuilders.queryString(queryString);
		queryBuilder.defaultField(request.param("df"));
		queryBuilder.analyzer(request.param("analyzer"));
		String defaultOperator = request.param("default_operator");
		if (defaultOperator != null) {
			if ("OR".equals(defaultOperator)) {
				queryBuilder.defaultOperator(QueryStringQueryBuilder.Operator.OR);
			} else if ("AND".equals(defaultOperator)) {
				queryBuilder.defaultOperator(QueryStringQueryBuilder.Operator.AND);
			} else {
				throw new RebirthIllegalArgumentException("Unsupported defaultOperator [" + defaultOperator
						+ "], can either be [OR] or [AND]");
			}
		}
		return queryBuilder.buildAsBytes();
	}

	/**
	 * Split indices.
	 *
	 * @param indices the indices
	 * @return the string[]
	 */
	public static String[] splitIndices(String indices) {
		if (indices == null) {
			return Strings.EMPTY_ARRAY;
		}
		return Strings.splitStringByCommaToArray(indices);
	}

	/**
	 * Split types.
	 *
	 * @param typeNames the type names
	 * @return the string[]
	 */
	public static String[] splitTypes(String typeNames) {
		if (typeNames == null) {
			return Strings.EMPTY_ARRAY;
		}
		return Strings.splitStringByCommaToArray(typeNames);
	}

	/**
	 * Split nodes.
	 *
	 * @param nodes the nodes
	 * @return the string[]
	 */
	public static String[] splitNodes(String nodes) {
		if (nodes == null) {
			return Strings.EMPTY_ARRAY;
		}
		return Strings.splitStringByCommaToArray(nodes);
	}
}
