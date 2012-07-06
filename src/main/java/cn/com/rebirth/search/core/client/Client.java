/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core Client.java 2012-3-29 15:00:57 l.xue.nong$$
 */

package cn.com.rebirth.search.core.client;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.search.core.action.Action;
import cn.com.rebirth.search.core.action.ActionFuture;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.ActionRequestBuilder;
import cn.com.rebirth.search.core.action.ActionResponse;
import cn.com.rebirth.search.core.action.bulk.BulkRequest;
import cn.com.rebirth.search.core.action.bulk.BulkRequestBuilder;
import cn.com.rebirth.search.core.action.bulk.BulkResponse;
import cn.com.rebirth.search.core.action.count.CountRequest;
import cn.com.rebirth.search.core.action.count.CountRequestBuilder;
import cn.com.rebirth.search.core.action.count.CountResponse;
import cn.com.rebirth.search.core.action.delete.DeleteRequest;
import cn.com.rebirth.search.core.action.delete.DeleteRequestBuilder;
import cn.com.rebirth.search.core.action.delete.DeleteResponse;
import cn.com.rebirth.search.core.action.deletebyquery.DeleteByQueryRequest;
import cn.com.rebirth.search.core.action.deletebyquery.DeleteByQueryRequestBuilder;
import cn.com.rebirth.search.core.action.deletebyquery.DeleteByQueryResponse;
import cn.com.rebirth.search.core.action.get.GetRequest;
import cn.com.rebirth.search.core.action.get.GetRequestBuilder;
import cn.com.rebirth.search.core.action.get.GetResponse;
import cn.com.rebirth.search.core.action.get.MultiGetRequest;
import cn.com.rebirth.search.core.action.get.MultiGetRequestBuilder;
import cn.com.rebirth.search.core.action.get.MultiGetResponse;
import cn.com.rebirth.search.core.action.index.IndexRequest;
import cn.com.rebirth.search.core.action.index.IndexRequestBuilder;
import cn.com.rebirth.search.core.action.index.IndexResponse;
import cn.com.rebirth.search.core.action.mlt.MoreLikeThisRequest;
import cn.com.rebirth.search.core.action.mlt.MoreLikeThisRequestBuilder;
import cn.com.rebirth.search.core.action.percolate.PercolateRequest;
import cn.com.rebirth.search.core.action.percolate.PercolateRequestBuilder;
import cn.com.rebirth.search.core.action.percolate.PercolateResponse;
import cn.com.rebirth.search.core.action.search.MultiSearchRequest;
import cn.com.rebirth.search.core.action.search.MultiSearchRequestBuilder;
import cn.com.rebirth.search.core.action.search.MultiSearchResponse;
import cn.com.rebirth.search.core.action.search.SearchRequest;
import cn.com.rebirth.search.core.action.search.SearchRequestBuilder;
import cn.com.rebirth.search.core.action.search.SearchResponse;
import cn.com.rebirth.search.core.action.search.SearchScrollRequest;
import cn.com.rebirth.search.core.action.search.SearchScrollRequestBuilder;
import cn.com.rebirth.search.core.action.update.UpdateRequest;
import cn.com.rebirth.search.core.action.update.UpdateRequestBuilder;
import cn.com.rebirth.search.core.action.update.UpdateResponse;

/**
 * The Interface Client.
 *
 * @author l.xue.nong
 */
public interface Client {

	/**
	 * Close.
	 */
	void close();

	/**
	 * Admin.
	 *
	 * @return the admin client
	 */
	AdminClient admin();

	/**
	 * Execute.
	 *
	 * @param <Request> the generic type
	 * @param <Response> the generic type
	 * @param <RequestBuilder> the generic type
	 * @param action the action
	 * @param request the request
	 * @return the action future
	 */
	<Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response>> ActionFuture<Response> execute(
			final Action<Request, Response, RequestBuilder> action, final Request request);

	/**
	 * Execute.
	 *
	 * @param <Request> the generic type
	 * @param <Response> the generic type
	 * @param <RequestBuilder> the generic type
	 * @param action the action
	 * @param request the request
	 * @param listener the listener
	 */
	<Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response>> void execute(
			final Action<Request, Response, RequestBuilder> action, final Request request,
			ActionListener<Response> listener);

	/**
	 * Prepare execute.
	 *
	 * @param <Request> the generic type
	 * @param <Response> the generic type
	 * @param <RequestBuilder> the generic type
	 * @param action the action
	 * @return the request builder
	 */
	<Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response>> RequestBuilder prepareExecute(
			final Action<Request, Response, RequestBuilder> action);

	/**
	 * Index.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<IndexResponse> index(IndexRequest request);

	/**
	 * Index.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void index(IndexRequest request, ActionListener<IndexResponse> listener);

	/**
	 * Prepare index.
	 *
	 * @return the index request builder
	 */
	IndexRequestBuilder prepareIndex();

	/**
	 * Update.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<UpdateResponse> update(UpdateRequest request);

	/**
	 * Update.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void update(UpdateRequest request, ActionListener<UpdateResponse> listener);

	/**
	 * Prepare update.
	 *
	 * @return the update request builder
	 */
	UpdateRequestBuilder prepareUpdate();

	/**
	 * Prepare update.
	 *
	 * @param index the index
	 * @param type the type
	 * @param id the id
	 * @return the update request builder
	 */
	UpdateRequestBuilder prepareUpdate(String index, String type, String id);

	/**
	 * Prepare index.
	 *
	 * @param index the index
	 * @param type the type
	 * @return the index request builder
	 */
	IndexRequestBuilder prepareIndex(String index, String type);

	/**
	 * Prepare index.
	 *
	 * @param index the index
	 * @param type the type
	 * @param id the id
	 * @return the index request builder
	 */
	IndexRequestBuilder prepareIndex(String index, String type, @Nullable String id);

	/**
	 * Delete.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<DeleteResponse> delete(DeleteRequest request);

	/**
	 * Delete.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void delete(DeleteRequest request, ActionListener<DeleteResponse> listener);

	/**
	 * Prepare delete.
	 *
	 * @return the delete request builder
	 */
	DeleteRequestBuilder prepareDelete();

	/**
	 * Prepare delete.
	 *
	 * @param index the index
	 * @param type the type
	 * @param id the id
	 * @return the delete request builder
	 */
	DeleteRequestBuilder prepareDelete(String index, String type, String id);

	/**
	 * Bulk.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<BulkResponse> bulk(BulkRequest request);

	/**
	 * Bulk.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void bulk(BulkRequest request, ActionListener<BulkResponse> listener);

	/**
	 * Prepare bulk.
	 *
	 * @return the bulk request builder
	 */
	BulkRequestBuilder prepareBulk();

	/**
	 * Delete by query.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<DeleteByQueryResponse> deleteByQuery(DeleteByQueryRequest request);

	/**
	 * Delete by query.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void deleteByQuery(DeleteByQueryRequest request, ActionListener<DeleteByQueryResponse> listener);

	/**
	 * Prepare delete by query.
	 *
	 * @param indices the indices
	 * @return the delete by query request builder
	 */
	DeleteByQueryRequestBuilder prepareDeleteByQuery(String... indices);

	/**
	 * Gets the.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<GetResponse> get(GetRequest request);

	/**
	 * Gets the.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void get(GetRequest request, ActionListener<GetResponse> listener);

	/**
	 * Prepare get.
	 *
	 * @return the gets the request builder
	 */
	GetRequestBuilder prepareGet();

	/**
	 * Prepare get.
	 *
	 * @param index the index
	 * @param type the type
	 * @param id the id
	 * @return the gets the request builder
	 */
	GetRequestBuilder prepareGet(String index, @Nullable String type, String id);

	/**
	 * Multi get.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<MultiGetResponse> multiGet(MultiGetRequest request);

	/**
	 * Multi get.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void multiGet(MultiGetRequest request, ActionListener<MultiGetResponse> listener);

	/**
	 * Prepare multi get.
	 *
	 * @return the multi get request builder
	 */
	MultiGetRequestBuilder prepareMultiGet();

	/**
	 * Count.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<CountResponse> count(CountRequest request);

	/**
	 * Count.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void count(CountRequest request, ActionListener<CountResponse> listener);

	/**
	 * Prepare count.
	 *
	 * @param indices the indices
	 * @return the count request builder
	 */
	CountRequestBuilder prepareCount(String... indices);

	/**
	 * Search.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<SearchResponse> search(SearchRequest request);

	/**
	 * Search.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void search(SearchRequest request, ActionListener<SearchResponse> listener);

	/**
	 * Prepare search.
	 *
	 * @param indices the indices
	 * @return the search request builder
	 */
	SearchRequestBuilder prepareSearch(String... indices);

	/**
	 * Search scroll.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<SearchResponse> searchScroll(SearchScrollRequest request);

	/**
	 * Search scroll.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void searchScroll(SearchScrollRequest request, ActionListener<SearchResponse> listener);

	/**
	 * Prepare search scroll.
	 *
	 * @param scrollId the scroll id
	 * @return the search scroll request builder
	 */
	SearchScrollRequestBuilder prepareSearchScroll(String scrollId);

	/**
	 * Multi search.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<MultiSearchResponse> multiSearch(MultiSearchRequest request);

	/**
	 * Multi search.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void multiSearch(MultiSearchRequest request, ActionListener<MultiSearchResponse> listener);

	/**
	 * Prepare multi search.
	 *
	 * @return the multi search request builder
	 */
	MultiSearchRequestBuilder prepareMultiSearch();

	/**
	 * More like this.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<SearchResponse> moreLikeThis(MoreLikeThisRequest request);

	/**
	 * More like this.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void moreLikeThis(MoreLikeThisRequest request, ActionListener<SearchResponse> listener);

	/**
	 * Prepare more like this.
	 *
	 * @param index the index
	 * @param type the type
	 * @param id the id
	 * @return the more like this request builder
	 */
	MoreLikeThisRequestBuilder prepareMoreLikeThis(String index, String type, String id);

	/**
	 * Percolate.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<PercolateResponse> percolate(PercolateRequest request);

	/**
	 * Percolate.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void percolate(PercolateRequest request, ActionListener<PercolateResponse> listener);

	/**
	 * Prepare percolate.
	 *
	 * @param index the index
	 * @param type the type
	 * @return the percolate request builder
	 */
	PercolateRequestBuilder preparePercolate(String index, String type);
}