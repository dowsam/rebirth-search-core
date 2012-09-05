/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MultiSearchRequest.java 2012-7-6 14:29:21 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.search;

import java.io.IOException;
import java.util.List;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.xcontent.XContent;
import cn.com.rebirth.commons.xcontent.XContentFactory;
import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.ValidateActions;

import com.google.common.collect.Lists;

/**
 * The Class MultiSearchRequest.
 *
 * @author l.xue.nong
 */
public class MultiSearchRequest implements ActionRequest {

	/** The requests. */
	private List<SearchRequest> requests = Lists.newArrayList();

	/** The listener threaded. */
	private boolean listenerThreaded = false;

	/**
	 * Adds the.
	 *
	 * @param request the request
	 * @return the multi search request
	 */
	public MultiSearchRequest add(SearchRequestBuilder request) {
		requests.add(request.request());
		return this;
	}

	/**
	 * Adds the.
	 *
	 * @param request the request
	 * @return the multi search request
	 */
	public MultiSearchRequest add(SearchRequest request) {
		requests.add(request);
		return this;
	}

	/**
	 * Adds the.
	 *
	 * @param data the data
	 * @param from the from
	 * @param length the length
	 * @param contentUnsafe the content unsafe
	 * @param indices the indices
	 * @param types the types
	 * @return the multi search request
	 * @throws Exception the exception
	 */
	public MultiSearchRequest add(byte[] data, int from, int length, boolean contentUnsafe, @Nullable String[] indices,
			@Nullable String[] types) throws Exception {
		XContent xContent = XContentFactory.xContent(data, from, length);
		byte marker = xContent.streamSeparator();
		while (true) {
			int nextMarker = findNextMarker(marker, from, data, length);
			if (nextMarker == -1) {
				break;
			}

			if (nextMarker == 0) {
				from = nextMarker + 1;
				continue;
			}

			SearchRequest searchRequest = new SearchRequest(indices);
			if (types != null && types.length > 0) {
				searchRequest.types(types);
			}

			if (nextMarker - from > 0) {
				XContentParser parser = xContent.createParser(data, from, nextMarker - from);
				try {

					XContentParser.Token token = parser.nextToken();
					if (token != null) {
						assert token == XContentParser.Token.START_OBJECT;
						String currentFieldName = null;
						while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
							if (token == XContentParser.Token.FIELD_NAME) {
								currentFieldName = parser.currentName();
							} else if (token.isValue()) {
								if ("index".equals(currentFieldName) || "indices".equals(currentFieldName)) {
									searchRequest.indices(Strings.splitStringByCommaToArray(parser.text()));
								} else if ("type".equals(currentFieldName) || "types".equals(currentFieldName)) {
									searchRequest.types(Strings.splitStringByCommaToArray(parser.text()));
								} else if ("search_type".equals(currentFieldName)
										|| "searchType".equals(currentFieldName)) {
									searchRequest.searchType(parser.text());
								} else if ("preference".equals(currentFieldName)) {
									searchRequest.preference(parser.text());
								} else if ("routing".equals(currentFieldName)) {
									searchRequest.routing(parser.text());
								} else if ("query_hint".equals(currentFieldName)
										|| "queryHint".equals(currentFieldName)) {
									searchRequest.queryHint(parser.text());
								}
							}
						}
					}
				} finally {
					parser.close();
				}
			}

			from = nextMarker + 1;

			nextMarker = findNextMarker(marker, from, data, length);
			if (nextMarker == -1) {
				break;
			}

			searchRequest.source(data, from, nextMarker - from, contentUnsafe);

			from = nextMarker + 1;

			add(searchRequest);
		}

		return this;
	}

	/**
	 * Find next marker.
	 *
	 * @param marker the marker
	 * @param from the from
	 * @param data the data
	 * @param length the length
	 * @return the int
	 */
	private int findNextMarker(byte marker, int from, byte[] data, int length) {
		for (int i = from; i < length; i++) {
			if (data[i] == marker) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Requests.
	 *
	 * @return the list
	 */
	public List<SearchRequest> requests() {
		return this.requests;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionRequest#validate()
	 */
	@Override
	public ActionRequestValidationException validate() {
		ActionRequestValidationException validationException = null;
		if (requests.isEmpty()) {
			validationException = ValidateActions.addValidationError("no requests added", validationException);
		}
		for (int i = 0; i < requests.size(); i++) {
			ActionRequestValidationException ex = requests.get(i).validate();
			if (ex != null) {
				if (validationException == null) {
					validationException = new ActionRequestValidationException();
				}
				validationException.addValidationErrors(ex.validationErrors());
			}
		}

		return validationException;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionRequest#listenerThreaded()
	 */
	@Override
	public boolean listenerThreaded() {
		return listenerThreaded;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionRequest#listenerThreaded(boolean)
	 */
	@Override
	public MultiSearchRequest listenerThreaded(boolean listenerThreaded) {
		this.listenerThreaded = listenerThreaded;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		int size = in.readVInt();
		for (int i = 0; i < size; i++) {
			SearchRequest request = new SearchRequest();
			request.readFrom(in);
			requests.add(request);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeVInt(requests.size());
		for (SearchRequest request : requests) {
			request.writeTo(out);
		}
	}
}
