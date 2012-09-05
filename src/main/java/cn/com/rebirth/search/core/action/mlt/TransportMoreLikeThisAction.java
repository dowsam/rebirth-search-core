/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportMoreLikeThisAction.java 2012-7-6 14:28:47 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.mlt;

import static cn.com.rebirth.search.core.client.Requests.getRequest;
import static cn.com.rebirth.search.core.client.Requests.searchRequest;
import static cn.com.rebirth.search.core.index.query.QueryBuilders.boolQuery;
import static cn.com.rebirth.search.core.index.query.QueryBuilders.moreLikeThisFieldQuery;
import static cn.com.rebirth.search.core.index.query.QueryBuilders.termQuery;
import static cn.com.rebirth.search.core.search.builder.SearchSourceBuilder.searchSource;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.Term;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.get.GetRequest;
import cn.com.rebirth.search.core.action.get.GetResponse;
import cn.com.rebirth.search.core.action.get.TransportGetAction;
import cn.com.rebirth.search.core.action.search.SearchRequest;
import cn.com.rebirth.search.core.action.search.SearchResponse;
import cn.com.rebirth.search.core.action.search.TransportSearchAction;
import cn.com.rebirth.search.core.action.support.TransportAction;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.index.get.GetField;
import cn.com.rebirth.search.core.index.mapper.DocumentMapper;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.index.mapper.FieldMappers;
import cn.com.rebirth.search.core.index.mapper.InternalMapper;
import cn.com.rebirth.search.core.index.mapper.SourceToParse;
import cn.com.rebirth.search.core.index.mapper.internal.SourceFieldMapper;
import cn.com.rebirth.search.core.index.query.BoolQueryBuilder;
import cn.com.rebirth.search.core.index.query.MoreLikeThisFieldQueryBuilder;
import cn.com.rebirth.search.core.indices.IndicesService;
import cn.com.rebirth.search.core.transport.BaseTransportRequestHandler;
import cn.com.rebirth.search.core.transport.TransportChannel;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class TransportMoreLikeThisAction.
 *
 * @author l.xue.nong
 */
public class TransportMoreLikeThisAction extends TransportAction<MoreLikeThisRequest, SearchResponse> {

	/** The search action. */
	private final TransportSearchAction searchAction;

	/** The get action. */
	private final TransportGetAction getAction;

	/** The indices service. */
	private final IndicesService indicesService;

	/** The cluster service. */
	private final ClusterService clusterService;

	/**
	 * Instantiates a new transport more like this action.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param searchAction the search action
	 * @param getAction the get action
	 * @param clusterService the cluster service
	 * @param indicesService the indices service
	 * @param transportService the transport service
	 */
	@Inject
	public TransportMoreLikeThisAction(Settings settings, ThreadPool threadPool, TransportSearchAction searchAction,
			TransportGetAction getAction, ClusterService clusterService, IndicesService indicesService,
			TransportService transportService) {
		super(settings, threadPool);
		this.searchAction = searchAction;
		this.getAction = getAction;
		this.indicesService = indicesService;
		this.clusterService = clusterService;

		transportService.registerHandler(MoreLikeThisAction.NAME, new TransportHandler());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.TransportAction#doExecute(cn.com.rebirth.search.core.action.ActionRequest, cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(final MoreLikeThisRequest request, final ActionListener<SearchResponse> listener) {

		ClusterState clusterState = clusterService.state();

		final String concreteIndex = clusterState.metaData().concreteIndex(request.index());

		Set<String> getFields = newHashSet();
		if (request.fields() != null) {
			Collections.addAll(getFields, request.fields());
		}

		getFields.add(SourceFieldMapper.NAME);

		GetRequest getRequest = getRequest(concreteIndex).fields(getFields.toArray(new String[getFields.size()]))
				.type(request.type()).id(request.id()).listenerThreaded(true).operationThreaded(true);

		request.beforeLocalFork();
		getAction.execute(getRequest, new ActionListener<GetResponse>() {
			@Override
			public void onResponse(GetResponse getResponse) {
				if (!getResponse.exists()) {
					listener.onFailure(new RebirthException("document missing"));
					return;
				}
				final BoolQueryBuilder boolBuilder = boolQuery();
				try {
					DocumentMapper docMapper = indicesService.indexServiceSafe(concreteIndex).mapperService()
							.documentMapper(request.type());
					final Set<String> fields = newHashSet();
					if (request.fields() != null) {
						for (String field : request.fields()) {
							FieldMappers fieldMappers = docMapper.mappers().smartName(field);
							if (fieldMappers != null) {
								fields.add(fieldMappers.mapper().names().indexName());
							} else {
								fields.add(field);
							}
						}
					}

					if (!fields.isEmpty()) {

						for (Iterator<String> it = fields.iterator(); it.hasNext();) {
							String field = it.next();
							GetField getField = getResponse.field(field);
							if (getField != null) {
								for (Object value : getField.values()) {
									addMoreLikeThis(request, boolBuilder, getField.name(), value.toString());
								}
								it.remove();
							}
						}
						if (!fields.isEmpty()) {

							parseSource(getResponse, boolBuilder, docMapper, fields, request);
						}
					} else {

						parseSource(getResponse, boolBuilder, docMapper, fields, request);
					}

					if (!boolBuilder.hasClauses()) {

						listener.onFailure(new RebirthException("No fields found to fetch the 'likeText' from"));
						return;
					}

					Term uidTerm = docMapper.uidMapper().term(request.type(), request.id());
					boolBuilder.mustNot(termQuery(uidTerm.field(), uidTerm.text()));
				} catch (Exception e) {
					listener.onFailure(e);
					return;
				}

				String[] searchIndices = request.searchIndices();
				if (searchIndices == null) {
					searchIndices = new String[] { request.index() };
				}
				String[] searchTypes = request.searchTypes();
				if (searchTypes == null) {
					searchTypes = new String[] { request.type() };
				}
				int size = request.searchSize() != 0 ? request.searchSize() : 10;
				int from = request.searchFrom() != 0 ? request.searchFrom() : 0;
				SearchRequest searchRequest = searchRequest(searchIndices).types(searchTypes)
						.searchType(request.searchType()).scroll(request.searchScroll())
						.extraSource(searchSource().query(boolBuilder).from(from).size(size))
						.listenerThreaded(request.listenerThreaded());

				if (request.searchSource() != null) {
					searchRequest.source(request.searchSource(), request.searchSourceOffset(),
							request.searchSourceLength(), request.searchSourceUnsafe());
				}
				searchAction.execute(searchRequest, new ActionListener<SearchResponse>() {
					@Override
					public void onResponse(SearchResponse response) {
						listener.onResponse(response);
					}

					@Override
					public void onFailure(Throwable e) {
						listener.onFailure(e);
					}
				});

			}

			@Override
			public void onFailure(Throwable e) {
				listener.onFailure(e);
			}
		});
	}

	/**
	 * Parses the source.
	 *
	 * @param getResponse the get response
	 * @param boolBuilder the bool builder
	 * @param docMapper the doc mapper
	 * @param fields the fields
	 * @param request the request
	 */
	private void parseSource(GetResponse getResponse, final BoolQueryBuilder boolBuilder, DocumentMapper docMapper,
			final Set<String> fields, final MoreLikeThisRequest request) {
		if (getResponse.source() == null) {
			return;
		}
		docMapper.parse(
				SourceToParse
						.source(getResponse.sourceRef().bytes(), getResponse.sourceRef().offset(),
								getResponse.sourceRef().length()).type(request.type()).id(request.id()),
				new DocumentMapper.ParseListenerAdapter() {
					@Override
					public boolean beforeFieldAdded(FieldMapper fieldMapper, Fieldable field, Object parseContext) {
						if (fieldMapper instanceof InternalMapper) {
							return true;
						}
						String value = fieldMapper.valueAsString(field);
						if (value == null) {
							return false;
						}

						if (fields.isEmpty() || fields.contains(field.name())) {
							addMoreLikeThis(request, boolBuilder, fieldMapper, field);
						}

						return false;
					}
				});
	}

	/**
	 * Adds the more like this.
	 *
	 * @param request the request
	 * @param boolBuilder the bool builder
	 * @param fieldMapper the field mapper
	 * @param field the field
	 */
	private void addMoreLikeThis(MoreLikeThisRequest request, BoolQueryBuilder boolBuilder, FieldMapper fieldMapper,
			Fieldable field) {
		addMoreLikeThis(request, boolBuilder, field.name(), fieldMapper.valueAsString(field));
	}

	/**
	 * Adds the more like this.
	 *
	 * @param request the request
	 * @param boolBuilder the bool builder
	 * @param fieldName the field name
	 * @param likeText the like text
	 */
	private void addMoreLikeThis(MoreLikeThisRequest request, BoolQueryBuilder boolBuilder, String fieldName,
			String likeText) {
		MoreLikeThisFieldQueryBuilder mlt = moreLikeThisFieldQuery(fieldName).likeText(likeText)
				.percentTermsToMatch(request.percentTermsToMatch()).boostTerms(request.boostTerms())
				.minDocFreq(request.minDocFreq()).maxDocFreq(request.maxDocFreq()).minWordLen(request.minWordLen())
				.maxWordLen(request.maxWordLen()).minTermFreq(request.minTermFreq())
				.maxQueryTerms(request.maxQueryTerms()).stopWords(request.stopWords());
		boolBuilder.should(mlt);
	}

	/**
	 * The Class TransportHandler.
	 *
	 * @author l.xue.nong
	 */
	private class TransportHandler extends BaseTransportRequestHandler<MoreLikeThisRequest> {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public MoreLikeThisRequest newInstance() {
			return new MoreLikeThisRequest();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#messageReceived(cn.com.rebirth.commons.io.stream.Streamable, cn.com.rebirth.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(MoreLikeThisRequest request, final TransportChannel channel) throws Exception {

			request.listenerThreaded(false);
			execute(request, new ActionListener<SearchResponse>() {
				@Override
				public void onResponse(SearchResponse result) {
					try {
						channel.sendResponse(result);
					} catch (Exception e) {
						onFailure(e);
					}
				}

				@Override
				public void onFailure(Throwable e) {
					try {
						channel.sendResponse(e);
					} catch (Exception e1) {
						logger.warn("Failed to send response for get", e1);
					}
				}
			});
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#executor()
		 */
		@Override
		public String executor() {
			return ThreadPool.Names.SAME;
		}
	}
}
