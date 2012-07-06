/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core AbstractClient.java 2012-3-29 15:01:34 l.xue.nong$$
 */


package cn.com.rebirth.search.core.client.support;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.search.core.action.Action;
import cn.com.rebirth.search.core.action.ActionFuture;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.ActionRequestBuilder;
import cn.com.rebirth.search.core.action.ActionResponse;
import cn.com.rebirth.search.core.action.bulk.BulkAction;
import cn.com.rebirth.search.core.action.bulk.BulkRequest;
import cn.com.rebirth.search.core.action.bulk.BulkRequestBuilder;
import cn.com.rebirth.search.core.action.bulk.BulkResponse;
import cn.com.rebirth.search.core.action.count.CountAction;
import cn.com.rebirth.search.core.action.count.CountRequest;
import cn.com.rebirth.search.core.action.count.CountRequestBuilder;
import cn.com.rebirth.search.core.action.count.CountResponse;
import cn.com.rebirth.search.core.action.delete.DeleteAction;
import cn.com.rebirth.search.core.action.delete.DeleteRequest;
import cn.com.rebirth.search.core.action.delete.DeleteRequestBuilder;
import cn.com.rebirth.search.core.action.delete.DeleteResponse;
import cn.com.rebirth.search.core.action.deletebyquery.DeleteByQueryAction;
import cn.com.rebirth.search.core.action.deletebyquery.DeleteByQueryRequest;
import cn.com.rebirth.search.core.action.deletebyquery.DeleteByQueryRequestBuilder;
import cn.com.rebirth.search.core.action.deletebyquery.DeleteByQueryResponse;
import cn.com.rebirth.search.core.action.get.GetAction;
import cn.com.rebirth.search.core.action.get.GetRequest;
import cn.com.rebirth.search.core.action.get.GetRequestBuilder;
import cn.com.rebirth.search.core.action.get.GetResponse;
import cn.com.rebirth.search.core.action.get.MultiGetAction;
import cn.com.rebirth.search.core.action.get.MultiGetRequest;
import cn.com.rebirth.search.core.action.get.MultiGetRequestBuilder;
import cn.com.rebirth.search.core.action.get.MultiGetResponse;
import cn.com.rebirth.search.core.action.index.IndexAction;
import cn.com.rebirth.search.core.action.index.IndexRequest;
import cn.com.rebirth.search.core.action.index.IndexRequestBuilder;
import cn.com.rebirth.search.core.action.index.IndexResponse;
import cn.com.rebirth.search.core.action.mlt.MoreLikeThisAction;
import cn.com.rebirth.search.core.action.mlt.MoreLikeThisRequest;
import cn.com.rebirth.search.core.action.mlt.MoreLikeThisRequestBuilder;
import cn.com.rebirth.search.core.action.percolate.PercolateAction;
import cn.com.rebirth.search.core.action.percolate.PercolateRequest;
import cn.com.rebirth.search.core.action.percolate.PercolateRequestBuilder;
import cn.com.rebirth.search.core.action.percolate.PercolateResponse;
import cn.com.rebirth.search.core.action.search.MultiSearchAction;
import cn.com.rebirth.search.core.action.search.MultiSearchRequest;
import cn.com.rebirth.search.core.action.search.MultiSearchRequestBuilder;
import cn.com.rebirth.search.core.action.search.MultiSearchResponse;
import cn.com.rebirth.search.core.action.search.SearchAction;
import cn.com.rebirth.search.core.action.search.SearchRequest;
import cn.com.rebirth.search.core.action.search.SearchRequestBuilder;
import cn.com.rebirth.search.core.action.search.SearchResponse;
import cn.com.rebirth.search.core.action.search.SearchScrollAction;
import cn.com.rebirth.search.core.action.search.SearchScrollRequest;
import cn.com.rebirth.search.core.action.search.SearchScrollRequestBuilder;
import cn.com.rebirth.search.core.action.update.UpdateAction;
import cn.com.rebirth.search.core.action.update.UpdateRequest;
import cn.com.rebirth.search.core.action.update.UpdateRequestBuilder;
import cn.com.rebirth.search.core.action.update.UpdateResponse;
import cn.com.rebirth.search.core.client.internal.InternalClient;


/**
 * The Class AbstractClient.
 *
 * @author l.xue.nong
 */
public abstract class AbstractClient implements InternalClient {

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#prepareExecute(cn.com.summall.search.core.action.Action)
	 */
	@Override
	public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response>> RequestBuilder prepareExecute(
			final Action<Request, Response, RequestBuilder> action) {
		return action.newRequestBuilder(this);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#index(cn.com.summall.search.core.action.index.IndexRequest)
	 */
	@Override
	public ActionFuture<IndexResponse> index(final IndexRequest request) {
		return execute(IndexAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#index(cn.com.summall.search.core.action.index.IndexRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void index(final IndexRequest request, final ActionListener<IndexResponse> listener) {
		execute(IndexAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#prepareIndex()
	 */
	@Override
	public IndexRequestBuilder prepareIndex() {
		return new IndexRequestBuilder(this, null);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#prepareIndex(java.lang.String, java.lang.String)
	 */
	@Override
	public IndexRequestBuilder prepareIndex(String index, String type) {
		return prepareIndex(index, type, null);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#prepareIndex(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public IndexRequestBuilder prepareIndex(String index, String type, @Nullable String id) {
		return prepareIndex().setIndex(index).setType(type).setId(id);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#update(cn.com.summall.search.core.action.update.UpdateRequest)
	 */
	@Override
	public ActionFuture<UpdateResponse> update(final UpdateRequest request) {
		return execute(UpdateAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#update(cn.com.summall.search.core.action.update.UpdateRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void update(final UpdateRequest request, final ActionListener<UpdateResponse> listener) {
		execute(UpdateAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#prepareUpdate()
	 */
	@Override
	public UpdateRequestBuilder prepareUpdate() {
		return new UpdateRequestBuilder(this, null, null, null);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#prepareUpdate(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public UpdateRequestBuilder prepareUpdate(String index, String type, String id) {
		return new UpdateRequestBuilder(this, index, type, id);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#delete(cn.com.summall.search.core.action.delete.DeleteRequest)
	 */
	@Override
	public ActionFuture<DeleteResponse> delete(final DeleteRequest request) {
		return execute(DeleteAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#delete(cn.com.summall.search.core.action.delete.DeleteRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void delete(final DeleteRequest request, final ActionListener<DeleteResponse> listener) {
		execute(DeleteAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#prepareDelete()
	 */
	@Override
	public DeleteRequestBuilder prepareDelete() {
		return new DeleteRequestBuilder(this, null);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#prepareDelete(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public DeleteRequestBuilder prepareDelete(String index, String type, String id) {
		return prepareDelete().setIndex(index).setType(type).setId(id);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#bulk(cn.com.summall.search.core.action.bulk.BulkRequest)
	 */
	@Override
	public ActionFuture<BulkResponse> bulk(final BulkRequest request) {
		return execute(BulkAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#bulk(cn.com.summall.search.core.action.bulk.BulkRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void bulk(final BulkRequest request, final ActionListener<BulkResponse> listener) {
		execute(BulkAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#prepareBulk()
	 */
	@Override
	public BulkRequestBuilder prepareBulk() {
		return new BulkRequestBuilder(this);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#deleteByQuery(cn.com.summall.search.core.action.deletebyquery.DeleteByQueryRequest)
	 */
	@Override
	public ActionFuture<DeleteByQueryResponse> deleteByQuery(final DeleteByQueryRequest request) {
		return execute(DeleteByQueryAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#deleteByQuery(cn.com.summall.search.core.action.deletebyquery.DeleteByQueryRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void deleteByQuery(final DeleteByQueryRequest request, final ActionListener<DeleteByQueryResponse> listener) {
		execute(DeleteByQueryAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#prepareDeleteByQuery(java.lang.String[])
	 */
	@Override
	public DeleteByQueryRequestBuilder prepareDeleteByQuery(String... indices) {
		return new DeleteByQueryRequestBuilder(this).setIndices(indices);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#get(cn.com.summall.search.core.action.get.GetRequest)
	 */
	@Override
	public ActionFuture<GetResponse> get(final GetRequest request) {
		return execute(GetAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#get(cn.com.summall.search.core.action.get.GetRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void get(final GetRequest request, final ActionListener<GetResponse> listener) {
		execute(GetAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#prepareGet()
	 */
	@Override
	public GetRequestBuilder prepareGet() {
		return new GetRequestBuilder(this, null);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#prepareGet(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public GetRequestBuilder prepareGet(String index, String type, String id) {
		return prepareGet().setIndex(index).setType(type).setId(id);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#multiGet(cn.com.summall.search.core.action.get.MultiGetRequest)
	 */
	@Override
	public ActionFuture<MultiGetResponse> multiGet(final MultiGetRequest request) {
		return execute(MultiGetAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#multiGet(cn.com.summall.search.core.action.get.MultiGetRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void multiGet(final MultiGetRequest request, final ActionListener<MultiGetResponse> listener) {
		execute(MultiGetAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#prepareMultiGet()
	 */
	@Override
	public MultiGetRequestBuilder prepareMultiGet() {
		return new MultiGetRequestBuilder(this);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#search(cn.com.summall.search.core.action.search.SearchRequest)
	 */
	@Override
	public ActionFuture<SearchResponse> search(final SearchRequest request) {
		return execute(SearchAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#search(cn.com.summall.search.core.action.search.SearchRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void search(final SearchRequest request, final ActionListener<SearchResponse> listener) {
		execute(SearchAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#prepareSearch(java.lang.String[])
	 */
	@Override
	public SearchRequestBuilder prepareSearch(String... indices) {
		return new SearchRequestBuilder(this).setIndices(indices);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#searchScroll(cn.com.summall.search.core.action.search.SearchScrollRequest)
	 */
	@Override
	public ActionFuture<SearchResponse> searchScroll(final SearchScrollRequest request) {
		return execute(SearchScrollAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#searchScroll(cn.com.summall.search.core.action.search.SearchScrollRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void searchScroll(final SearchScrollRequest request, final ActionListener<SearchResponse> listener) {
		execute(SearchScrollAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#prepareSearchScroll(java.lang.String)
	 */
	@Override
	public SearchScrollRequestBuilder prepareSearchScroll(String scrollId) {
		return new SearchScrollRequestBuilder(this, scrollId);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#multiSearch(cn.com.summall.search.core.action.search.MultiSearchRequest)
	 */
	@Override
	public ActionFuture<MultiSearchResponse> multiSearch(MultiSearchRequest request) {
		return execute(MultiSearchAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#multiSearch(cn.com.summall.search.core.action.search.MultiSearchRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void multiSearch(MultiSearchRequest request, ActionListener<MultiSearchResponse> listener) {
		execute(MultiSearchAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#prepareMultiSearch()
	 */
	@Override
	public MultiSearchRequestBuilder prepareMultiSearch() {
		return new MultiSearchRequestBuilder(this);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#count(cn.com.summall.search.core.action.count.CountRequest)
	 */
	@Override
	public ActionFuture<CountResponse> count(final CountRequest request) {
		return execute(CountAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#count(cn.com.summall.search.core.action.count.CountRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void count(final CountRequest request, final ActionListener<CountResponse> listener) {
		execute(CountAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#prepareCount(java.lang.String[])
	 */
	@Override
	public CountRequestBuilder prepareCount(String... indices) {
		return new CountRequestBuilder(this).setIndices(indices);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#moreLikeThis(cn.com.summall.search.core.action.mlt.MoreLikeThisRequest)
	 */
	@Override
	public ActionFuture<SearchResponse> moreLikeThis(final MoreLikeThisRequest request) {
		return execute(MoreLikeThisAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#moreLikeThis(cn.com.summall.search.core.action.mlt.MoreLikeThisRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void moreLikeThis(final MoreLikeThisRequest request, final ActionListener<SearchResponse> listener) {
		execute(MoreLikeThisAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#prepareMoreLikeThis(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public MoreLikeThisRequestBuilder prepareMoreLikeThis(String index, String type, String id) {
		return new MoreLikeThisRequestBuilder(this, index, type, id);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#percolate(cn.com.summall.search.core.action.percolate.PercolateRequest)
	 */
	@Override
	public ActionFuture<PercolateResponse> percolate(final PercolateRequest request) {
		return execute(PercolateAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#percolate(cn.com.summall.search.core.action.percolate.PercolateRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void percolate(final PercolateRequest request, final ActionListener<PercolateResponse> listener) {
		execute(PercolateAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.Client#preparePercolate(java.lang.String, java.lang.String)
	 */
	@Override
	public PercolateRequestBuilder preparePercolate(String index, String type) {
		return new PercolateRequestBuilder(this, index, type);
	}
}
