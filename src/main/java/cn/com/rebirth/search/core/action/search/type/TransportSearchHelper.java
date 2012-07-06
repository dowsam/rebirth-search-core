/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportSearchHelper.java 2012-7-6 14:29:25 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.search.type;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.Unicode;
import cn.com.rebirth.commons.collect.Tuple;
import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.commons.exception.RebirthIllegalStateException;
import cn.com.rebirth.search.commons.Base64;
import cn.com.rebirth.search.core.action.search.SearchRequest;
import cn.com.rebirth.search.core.action.search.SearchScrollRequest;
import cn.com.rebirth.search.core.action.search.SearchType;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.search.SearchPhaseResult;
import cn.com.rebirth.search.core.search.internal.InternalScrollSearchRequest;
import cn.com.rebirth.search.core.search.internal.InternalSearchRequest;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * The Class TransportSearchHelper.
 *
 * @author l.xue.nong
 */
public abstract class TransportSearchHelper {

	/**
	 * Internal search request.
	 *
	 * @param shardRouting the shard routing
	 * @param numberOfShards the number of shards
	 * @param request the request
	 * @param filteringAliases the filtering aliases
	 * @param nowInMillis the now in millis
	 * @return the internal search request
	 */
	public static InternalSearchRequest internalSearchRequest(ShardRouting shardRouting, int numberOfShards,
			SearchRequest request, String[] filteringAliases, long nowInMillis) {
		InternalSearchRequest internalRequest = new InternalSearchRequest(shardRouting, numberOfShards,
				request.searchType());
		internalRequest.source(request.source(), request.sourceOffset(), request.sourceLength());
		internalRequest.extraSource(request.extraSource(), request.extraSourceOffset(), request.extraSourceLength());
		internalRequest.scroll(request.scroll());
		internalRequest.filteringAliases(filteringAliases);
		internalRequest.types(request.types());
		internalRequest.nowInMillis(nowInMillis);
		return internalRequest;
	}

	/**
	 * Internal scroll search request.
	 *
	 * @param id the id
	 * @param request the request
	 * @return the internal scroll search request
	 */
	public static InternalScrollSearchRequest internalScrollSearchRequest(long id, SearchScrollRequest request) {
		InternalScrollSearchRequest internalRequest = new InternalScrollSearchRequest(id);
		internalRequest.scroll(request.scroll());
		return internalRequest;
	}

	/**
	 * Builds the scroll id.
	 *
	 * @param searchType the search type
	 * @param searchPhaseResults the search phase results
	 * @param attributes the attributes
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String buildScrollId(SearchType searchType,
			Collection<? extends SearchPhaseResult> searchPhaseResults, @Nullable Map<String, String> attributes)
			throws IOException {
		if (searchType == SearchType.DFS_QUERY_THEN_FETCH || searchType == SearchType.QUERY_THEN_FETCH) {
			return buildScrollId(ParsedScrollId.QUERY_THEN_FETCH_TYPE, searchPhaseResults, attributes);
		} else if (searchType == SearchType.QUERY_AND_FETCH || searchType == SearchType.DFS_QUERY_AND_FETCH) {
			return buildScrollId(ParsedScrollId.QUERY_AND_FETCH_TYPE, searchPhaseResults, attributes);
		} else if (searchType == SearchType.SCAN) {
			return buildScrollId(ParsedScrollId.SCAN, searchPhaseResults, attributes);
		} else {
			throw new RebirthIllegalStateException();
		}
	}

	/**
	 * Builds the scroll id.
	 *
	 * @param type the type
	 * @param searchPhaseResults the search phase results
	 * @param attributes the attributes
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String buildScrollId(String type, Collection<? extends SearchPhaseResult> searchPhaseResults,
			@Nullable Map<String, String> attributes) throws IOException {
		StringBuilder sb = new StringBuilder().append(type).append(';');
		sb.append(searchPhaseResults.size()).append(';');
		for (SearchPhaseResult searchPhaseResult : searchPhaseResults) {
			sb.append(searchPhaseResult.id()).append(':').append(searchPhaseResult.shardTarget().nodeId()).append(';');
		}
		if (attributes == null) {
			sb.append("0;");
		} else {
			sb.append(attributes.size()).append(";");
			for (Map.Entry<String, String> entry : attributes.entrySet()) {
				sb.append(entry.getKey()).append(':').append(entry.getValue()).append(';');
			}
		}
		return Base64.encodeBytes(Unicode.fromStringAsBytes(sb.toString()), Base64.URL_SAFE);
	}

	/**
	 * Parses the scroll id.
	 *
	 * @param scrollId the scroll id
	 * @return the parsed scroll id
	 */
	public static ParsedScrollId parseScrollId(String scrollId) {
		try {
			scrollId = Unicode.fromBytes(Base64.decode(scrollId, Base64.URL_SAFE));
		} catch (IOException e) {
			throw new RebirthIllegalArgumentException("Failed to decode scrollId", e);
		}
		String[] elements = Strings.splitStringToArray(scrollId, ';');
		int index = 0;
		String type = elements[index++];
		int contextSize = Integer.parseInt(elements[index++]);
		@SuppressWarnings({ "unchecked" })
		Tuple<String, Long>[] context = new Tuple[contextSize];
		for (int i = 0; i < contextSize; i++) {
			String element = elements[index++];
			int sep = element.indexOf(':');
			if (sep == -1) {
				throw new RebirthIllegalArgumentException("Malformed scrollId [" + scrollId + "]");
			}
			context[i] = new Tuple<String, Long>(element.substring(sep + 1), Long.parseLong(element.substring(0, sep)));
		}
		Map<String, String> attributes;
		int attributesSize = Integer.parseInt(elements[index++]);
		if (attributesSize == 0) {
			attributes = ImmutableMap.of();
		} else {
			attributes = Maps.newHashMapWithExpectedSize(attributesSize);
			for (int i = 0; i < attributesSize; i++) {
				String element = elements[index++];
				int sep = element.indexOf(':');
				attributes.put(element.substring(0, sep), element.substring(sep + 1));
			}
		}
		return new ParsedScrollId(scrollId, type, context, attributes);
	}

	/**
	 * Instantiates a new transport search helper.
	 */
	private TransportSearchHelper() {

	}

}
