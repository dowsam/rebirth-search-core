/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RestMoreLikeThisAction.java 2012-3-29 15:02:49 l.xue.nong$$
 */


package cn.com.rebirth.search.core.rest.action.mlt;

import java.io.IOException;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.mlt.MoreLikeThisRequest;
import cn.com.rebirth.search.core.action.search.SearchResponse;
import cn.com.rebirth.search.core.action.search.SearchType;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.client.Requests;
import cn.com.rebirth.search.core.rest.BaseRestHandler;
import cn.com.rebirth.search.core.rest.RestChannel;
import cn.com.rebirth.search.core.rest.RestController;
import cn.com.rebirth.search.core.rest.RestRequest;
import cn.com.rebirth.search.core.rest.RestStatus;
import cn.com.rebirth.search.core.rest.XContentRestResponse;
import cn.com.rebirth.search.core.rest.XContentThrowableRestResponse;
import cn.com.rebirth.search.core.rest.RestRequest.Method;
import cn.com.rebirth.search.core.rest.action.support.RestXContentBuilder;
import cn.com.rebirth.search.core.search.Scroll;


/**
 * The Class RestMoreLikeThisAction.
 *
 * @author l.xue.nong
 */
public class RestMoreLikeThisAction extends BaseRestHandler {

	
	/**
	 * Instantiates a new rest more like this action.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param controller the controller
	 */
	@Inject
	public RestMoreLikeThisAction(Settings settings, Client client, RestController controller) {
		super(settings, client);
		controller.registerHandler(Method.GET, "/{index}/{type}/{id}/_mlt", this);
		controller.registerHandler(Method.POST, "/{index}/{type}/{id}/_mlt", this);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.rest.RestHandler#handleRequest(cn.com.summall.search.core.rest.RestRequest, cn.com.summall.search.core.rest.RestChannel)
	 */
	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel) {
		MoreLikeThisRequest mltRequest = Requests.moreLikeThisRequest(request.param("index"))
				.type(request.param("type")).id(request.param("id"));
		try {
			mltRequest.fields(request.paramAsStringArray("mlt_fields", null));
			mltRequest.percentTermsToMatch(request.paramAsFloat("percent_terms_to_match", -1));
			mltRequest.minTermFreq(request.paramAsInt("min_term_freq", -1));
			mltRequest.maxQueryTerms(request.paramAsInt("max_query_terms", -1));
			mltRequest.stopWords(request.paramAsStringArray("stop_words", null));
			mltRequest.minDocFreq(request.paramAsInt("min_doc_freq", -1));
			mltRequest.maxDocFreq(request.paramAsInt("max_doc_freq", -1));
			mltRequest.minWordLen(request.paramAsInt("min_word_len", -1));
			mltRequest.maxWordLen(request.paramAsInt("max_word_len", -1));
			mltRequest.boostTerms(request.paramAsFloat("boost_terms", -1));

			mltRequest.searchType(SearchType.fromString(request.param("search_type")));
			mltRequest.searchIndices(request.paramAsStringArray("search_indices", null));
			mltRequest.searchTypes(request.paramAsStringArray("search_types", null));
			mltRequest.searchQueryHint(request.param("search_query_hint"));
			mltRequest.searchSize(request.paramAsInt("search_size", mltRequest.searchSize()));
			mltRequest.searchFrom(request.paramAsInt("search_from", mltRequest.searchFrom()));
			String searchScroll = request.param("search_scroll");
			if (searchScroll != null) {
				mltRequest.searchScroll(new Scroll(TimeValue.parseTimeValue(searchScroll, null)));
			}
			if (request.hasContent()) {
				mltRequest.searchSource(request.contentByteArray(), request.contentByteArrayOffset(),
						request.contentLength(), request.contentUnsafe());
			} else {
				String searchSource = request.param("search_source");
				if (searchSource != null) {
					mltRequest.searchSource(searchSource);
				}
			}
		} catch (Exception e) {
			try {
				XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
				channel.sendResponse(new XContentRestResponse(request, RestStatus.BAD_REQUEST, builder.startObject()
						.field("error", e.getMessage()).endObject()));
			} catch (IOException e1) {
				logger.error("Failed to send failure response", e1);
			}
			return;
		}

		client.moreLikeThis(mltRequest, new ActionListener<SearchResponse>() {
			@Override
			public void onResponse(SearchResponse response) {
				try {
					XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
					builder.startObject();
					response.toXContent(builder, request);
					builder.endObject();
					channel.sendResponse(new XContentRestResponse(request, RestStatus.OK, builder));
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
