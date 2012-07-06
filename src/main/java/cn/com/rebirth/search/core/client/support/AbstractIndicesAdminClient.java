/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core AbstractIndicesAdminClient.java 2012-3-29 15:01:26 l.xue.nong$$
 */


package cn.com.rebirth.search.core.client.support;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.search.core.action.ActionFuture;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.ActionRequestBuilder;
import cn.com.rebirth.search.core.action.ActionResponse;
import cn.com.rebirth.search.core.action.admin.indices.IndicesAction;
import cn.com.rebirth.search.core.action.admin.indices.alias.IndicesAliasesAction;
import cn.com.rebirth.search.core.action.admin.indices.alias.IndicesAliasesRequest;
import cn.com.rebirth.search.core.action.admin.indices.alias.IndicesAliasesRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.alias.IndicesAliasesResponse;
import cn.com.rebirth.search.core.action.admin.indices.analyze.AnalyzeAction;
import cn.com.rebirth.search.core.action.admin.indices.analyze.AnalyzeRequest;
import cn.com.rebirth.search.core.action.admin.indices.analyze.AnalyzeRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.analyze.AnalyzeResponse;
import cn.com.rebirth.search.core.action.admin.indices.cache.clear.ClearIndicesCacheAction;
import cn.com.rebirth.search.core.action.admin.indices.cache.clear.ClearIndicesCacheRequest;
import cn.com.rebirth.search.core.action.admin.indices.cache.clear.ClearIndicesCacheRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.cache.clear.ClearIndicesCacheResponse;
import cn.com.rebirth.search.core.action.admin.indices.close.CloseIndexAction;
import cn.com.rebirth.search.core.action.admin.indices.close.CloseIndexRequest;
import cn.com.rebirth.search.core.action.admin.indices.close.CloseIndexRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.close.CloseIndexResponse;
import cn.com.rebirth.search.core.action.admin.indices.create.CreateIndexAction;
import cn.com.rebirth.search.core.action.admin.indices.create.CreateIndexRequest;
import cn.com.rebirth.search.core.action.admin.indices.create.CreateIndexRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.create.CreateIndexResponse;
import cn.com.rebirth.search.core.action.admin.indices.delete.DeleteIndexAction;
import cn.com.rebirth.search.core.action.admin.indices.delete.DeleteIndexRequest;
import cn.com.rebirth.search.core.action.admin.indices.delete.DeleteIndexRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.delete.DeleteIndexResponse;
import cn.com.rebirth.search.core.action.admin.indices.exists.IndicesExistsAction;
import cn.com.rebirth.search.core.action.admin.indices.exists.IndicesExistsRequest;
import cn.com.rebirth.search.core.action.admin.indices.exists.IndicesExistsRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.exists.IndicesExistsResponse;
import cn.com.rebirth.search.core.action.admin.indices.flush.FlushAction;
import cn.com.rebirth.search.core.action.admin.indices.flush.FlushRequest;
import cn.com.rebirth.search.core.action.admin.indices.flush.FlushRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.flush.FlushResponse;
import cn.com.rebirth.search.core.action.admin.indices.gateway.snapshot.GatewaySnapshotAction;
import cn.com.rebirth.search.core.action.admin.indices.gateway.snapshot.GatewaySnapshotRequest;
import cn.com.rebirth.search.core.action.admin.indices.gateway.snapshot.GatewaySnapshotRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.gateway.snapshot.GatewaySnapshotResponse;
import cn.com.rebirth.search.core.action.admin.indices.mapping.delete.DeleteMappingAction;
import cn.com.rebirth.search.core.action.admin.indices.mapping.delete.DeleteMappingRequest;
import cn.com.rebirth.search.core.action.admin.indices.mapping.delete.DeleteMappingRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.mapping.delete.DeleteMappingResponse;
import cn.com.rebirth.search.core.action.admin.indices.mapping.put.PutMappingAction;
import cn.com.rebirth.search.core.action.admin.indices.mapping.put.PutMappingRequest;
import cn.com.rebirth.search.core.action.admin.indices.mapping.put.PutMappingRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.mapping.put.PutMappingResponse;
import cn.com.rebirth.search.core.action.admin.indices.open.OpenIndexAction;
import cn.com.rebirth.search.core.action.admin.indices.open.OpenIndexRequest;
import cn.com.rebirth.search.core.action.admin.indices.open.OpenIndexRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.open.OpenIndexResponse;
import cn.com.rebirth.search.core.action.admin.indices.optimize.OptimizeAction;
import cn.com.rebirth.search.core.action.admin.indices.optimize.OptimizeRequest;
import cn.com.rebirth.search.core.action.admin.indices.optimize.OptimizeRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.optimize.OptimizeResponse;
import cn.com.rebirth.search.core.action.admin.indices.refresh.RefreshAction;
import cn.com.rebirth.search.core.action.admin.indices.refresh.RefreshRequest;
import cn.com.rebirth.search.core.action.admin.indices.refresh.RefreshRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.refresh.RefreshResponse;
import cn.com.rebirth.search.core.action.admin.indices.segments.IndicesSegmentResponse;
import cn.com.rebirth.search.core.action.admin.indices.segments.IndicesSegmentsAction;
import cn.com.rebirth.search.core.action.admin.indices.segments.IndicesSegmentsRequest;
import cn.com.rebirth.search.core.action.admin.indices.segments.IndicesSegmentsRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.settings.UpdateSettingsAction;
import cn.com.rebirth.search.core.action.admin.indices.settings.UpdateSettingsRequest;
import cn.com.rebirth.search.core.action.admin.indices.settings.UpdateSettingsRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.settings.UpdateSettingsResponse;
import cn.com.rebirth.search.core.action.admin.indices.stats.IndicesStats;
import cn.com.rebirth.search.core.action.admin.indices.stats.IndicesStatsAction;
import cn.com.rebirth.search.core.action.admin.indices.stats.IndicesStatsRequest;
import cn.com.rebirth.search.core.action.admin.indices.stats.IndicesStatsRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.status.IndicesStatusAction;
import cn.com.rebirth.search.core.action.admin.indices.status.IndicesStatusRequest;
import cn.com.rebirth.search.core.action.admin.indices.status.IndicesStatusRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.status.IndicesStatusResponse;
import cn.com.rebirth.search.core.action.admin.indices.template.delete.DeleteIndexTemplateAction;
import cn.com.rebirth.search.core.action.admin.indices.template.delete.DeleteIndexTemplateRequest;
import cn.com.rebirth.search.core.action.admin.indices.template.delete.DeleteIndexTemplateRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.template.delete.DeleteIndexTemplateResponse;
import cn.com.rebirth.search.core.action.admin.indices.template.put.PutIndexTemplateAction;
import cn.com.rebirth.search.core.action.admin.indices.template.put.PutIndexTemplateRequest;
import cn.com.rebirth.search.core.action.admin.indices.template.put.PutIndexTemplateRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.template.put.PutIndexTemplateResponse;
import cn.com.rebirth.search.core.action.admin.indices.validate.query.ValidateQueryAction;
import cn.com.rebirth.search.core.action.admin.indices.validate.query.ValidateQueryRequest;
import cn.com.rebirth.search.core.action.admin.indices.validate.query.ValidateQueryRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.validate.query.ValidateQueryResponse;
import cn.com.rebirth.search.core.client.internal.InternalIndicesAdminClient;


/**
 * The Class AbstractIndicesAdminClient.
 *
 * @author l.xue.nong
 */
public abstract class AbstractIndicesAdminClient implements InternalIndicesAdminClient {

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#prepareExecute(cn.com.summall.search.core.action.admin.indices.IndicesAction)
	 */
	@Override
	public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response>> RequestBuilder prepareExecute(
			final IndicesAction<Request, Response, RequestBuilder> action) {
		return action.newRequestBuilder(this);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#exists(cn.com.summall.search.core.action.admin.indices.exists.IndicesExistsRequest)
	 */
	@Override
	public ActionFuture<IndicesExistsResponse> exists(final IndicesExistsRequest request) {
		return execute(IndicesExistsAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#exists(cn.com.summall.search.core.action.admin.indices.exists.IndicesExistsRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void exists(final IndicesExistsRequest request, final ActionListener<IndicesExistsResponse> listener) {
		execute(IndicesExistsAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#prepareExists(java.lang.String[])
	 */
	@Override
	public IndicesExistsRequestBuilder prepareExists(String... indices) {
		return new IndicesExistsRequestBuilder(this, indices);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#aliases(cn.com.summall.search.core.action.admin.indices.alias.IndicesAliasesRequest)
	 */
	@Override
	public ActionFuture<IndicesAliasesResponse> aliases(final IndicesAliasesRequest request) {
		return execute(IndicesAliasesAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#aliases(cn.com.summall.search.core.action.admin.indices.alias.IndicesAliasesRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void aliases(final IndicesAliasesRequest request, final ActionListener<IndicesAliasesResponse> listener) {
		execute(IndicesAliasesAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#prepareAliases()
	 */
	@Override
	public IndicesAliasesRequestBuilder prepareAliases() {
		return new IndicesAliasesRequestBuilder(this);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#clearCache(cn.com.summall.search.core.action.admin.indices.cache.clear.ClearIndicesCacheRequest)
	 */
	@Override
	public ActionFuture<ClearIndicesCacheResponse> clearCache(final ClearIndicesCacheRequest request) {
		return execute(ClearIndicesCacheAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#clearCache(cn.com.summall.search.core.action.admin.indices.cache.clear.ClearIndicesCacheRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void clearCache(final ClearIndicesCacheRequest request,
			final ActionListener<ClearIndicesCacheResponse> listener) {
		execute(ClearIndicesCacheAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#prepareClearCache(java.lang.String[])
	 */
	@Override
	public ClearIndicesCacheRequestBuilder prepareClearCache(String... indices) {
		return new ClearIndicesCacheRequestBuilder(this).setIndices(indices);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#create(cn.com.summall.search.core.action.admin.indices.create.CreateIndexRequest)
	 */
	@Override
	public ActionFuture<CreateIndexResponse> create(final CreateIndexRequest request) {
		return execute(CreateIndexAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#create(cn.com.summall.search.core.action.admin.indices.create.CreateIndexRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void create(final CreateIndexRequest request, final ActionListener<CreateIndexResponse> listener) {
		execute(CreateIndexAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#prepareCreate(java.lang.String)
	 */
	@Override
	public CreateIndexRequestBuilder prepareCreate(String index) {
		return new CreateIndexRequestBuilder(this, index);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#delete(cn.com.summall.search.core.action.admin.indices.delete.DeleteIndexRequest)
	 */
	@Override
	public ActionFuture<DeleteIndexResponse> delete(final DeleteIndexRequest request) {
		return execute(DeleteIndexAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#delete(cn.com.summall.search.core.action.admin.indices.delete.DeleteIndexRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void delete(final DeleteIndexRequest request, final ActionListener<DeleteIndexResponse> listener) {
		execute(DeleteIndexAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#prepareDelete(java.lang.String[])
	 */
	@Override
	public DeleteIndexRequestBuilder prepareDelete(String... indices) {
		return new DeleteIndexRequestBuilder(this, indices);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#close(cn.com.summall.search.core.action.admin.indices.close.CloseIndexRequest)
	 */
	@Override
	public ActionFuture<CloseIndexResponse> close(final CloseIndexRequest request) {
		return execute(CloseIndexAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#close(cn.com.summall.search.core.action.admin.indices.close.CloseIndexRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void close(final CloseIndexRequest request, final ActionListener<CloseIndexResponse> listener) {
		execute(CloseIndexAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#prepareClose(java.lang.String)
	 */
	@Override
	public CloseIndexRequestBuilder prepareClose(String index) {
		return new CloseIndexRequestBuilder(this, index);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#open(cn.com.summall.search.core.action.admin.indices.open.OpenIndexRequest)
	 */
	@Override
	public ActionFuture<OpenIndexResponse> open(final OpenIndexRequest request) {
		return execute(OpenIndexAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#open(cn.com.summall.search.core.action.admin.indices.open.OpenIndexRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void open(final OpenIndexRequest request, final ActionListener<OpenIndexResponse> listener) {
		execute(OpenIndexAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#prepareOpen(java.lang.String)
	 */
	@Override
	public OpenIndexRequestBuilder prepareOpen(String index) {
		return new OpenIndexRequestBuilder(this, index);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#flush(cn.com.summall.search.core.action.admin.indices.flush.FlushRequest)
	 */
	@Override
	public ActionFuture<FlushResponse> flush(final FlushRequest request) {
		return execute(FlushAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#flush(cn.com.summall.search.core.action.admin.indices.flush.FlushRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void flush(final FlushRequest request, final ActionListener<FlushResponse> listener) {
		execute(FlushAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#prepareFlush(java.lang.String[])
	 */
	@Override
	public FlushRequestBuilder prepareFlush(String... indices) {
		return new FlushRequestBuilder(this).setIndices(indices);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#gatewaySnapshot(cn.com.summall.search.core.action.admin.indices.gateway.snapshot.GatewaySnapshotRequest)
	 */
	@Override
	public ActionFuture<GatewaySnapshotResponse> gatewaySnapshot(final GatewaySnapshotRequest request) {
		return execute(GatewaySnapshotAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#gatewaySnapshot(cn.com.summall.search.core.action.admin.indices.gateway.snapshot.GatewaySnapshotRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void gatewaySnapshot(final GatewaySnapshotRequest request,
			final ActionListener<GatewaySnapshotResponse> listener) {
		execute(GatewaySnapshotAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#prepareGatewaySnapshot(java.lang.String[])
	 */
	@Override
	public GatewaySnapshotRequestBuilder prepareGatewaySnapshot(String... indices) {
		return new GatewaySnapshotRequestBuilder(this).setIndices(indices);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#putMapping(cn.com.summall.search.core.action.admin.indices.mapping.put.PutMappingRequest)
	 */
	@Override
	public ActionFuture<PutMappingResponse> putMapping(final PutMappingRequest request) {
		return execute(PutMappingAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#putMapping(cn.com.summall.search.core.action.admin.indices.mapping.put.PutMappingRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void putMapping(final PutMappingRequest request, final ActionListener<PutMappingResponse> listener) {
		execute(PutMappingAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#preparePutMapping(java.lang.String[])
	 */
	@Override
	public PutMappingRequestBuilder preparePutMapping(String... indices) {
		return new PutMappingRequestBuilder(this).setIndices(indices);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#deleteMapping(cn.com.summall.search.core.action.admin.indices.mapping.delete.DeleteMappingRequest)
	 */
	@Override
	public ActionFuture<DeleteMappingResponse> deleteMapping(final DeleteMappingRequest request) {
		return execute(DeleteMappingAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#deleteMapping(cn.com.summall.search.core.action.admin.indices.mapping.delete.DeleteMappingRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void deleteMapping(final DeleteMappingRequest request, final ActionListener<DeleteMappingResponse> listener) {
		execute(DeleteMappingAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#prepareDeleteMapping(java.lang.String[])
	 */
	@Override
	public DeleteMappingRequestBuilder prepareDeleteMapping(String... indices) {
		return new DeleteMappingRequestBuilder(this).setIndices(indices);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#optimize(cn.com.summall.search.core.action.admin.indices.optimize.OptimizeRequest)
	 */
	@Override
	public ActionFuture<OptimizeResponse> optimize(final OptimizeRequest request) {
		return execute(OptimizeAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#optimize(cn.com.summall.search.core.action.admin.indices.optimize.OptimizeRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void optimize(final OptimizeRequest request, final ActionListener<OptimizeResponse> listener) {
		execute(OptimizeAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#prepareOptimize(java.lang.String[])
	 */
	@Override
	public OptimizeRequestBuilder prepareOptimize(String... indices) {
		return new OptimizeRequestBuilder(this).setIndices(indices);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#refresh(cn.com.summall.search.core.action.admin.indices.refresh.RefreshRequest)
	 */
	@Override
	public ActionFuture<RefreshResponse> refresh(final RefreshRequest request) {
		return execute(RefreshAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#refresh(cn.com.summall.search.core.action.admin.indices.refresh.RefreshRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void refresh(final RefreshRequest request, final ActionListener<RefreshResponse> listener) {
		execute(RefreshAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#prepareRefresh(java.lang.String[])
	 */
	@Override
	public RefreshRequestBuilder prepareRefresh(String... indices) {
		return new RefreshRequestBuilder(this).setIndices(indices);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#stats(cn.com.summall.search.core.action.admin.indices.stats.IndicesStatsRequest)
	 */
	@Override
	public ActionFuture<IndicesStats> stats(final IndicesStatsRequest request) {
		return execute(IndicesStatsAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#stats(cn.com.summall.search.core.action.admin.indices.stats.IndicesStatsRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void stats(final IndicesStatsRequest request, final ActionListener<IndicesStats> listener) {
		execute(IndicesStatsAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#prepareStats(java.lang.String[])
	 */
	@Override
	public IndicesStatsRequestBuilder prepareStats(String... indices) {
		return new IndicesStatsRequestBuilder(this).setIndices(indices);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#status(cn.com.summall.search.core.action.admin.indices.status.IndicesStatusRequest)
	 */
	@Override
	public ActionFuture<IndicesStatusResponse> status(final IndicesStatusRequest request) {
		return execute(IndicesStatusAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#status(cn.com.summall.search.core.action.admin.indices.status.IndicesStatusRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void status(final IndicesStatusRequest request, final ActionListener<IndicesStatusResponse> listener) {
		execute(IndicesStatusAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#prepareStatus(java.lang.String[])
	 */
	@Override
	public IndicesStatusRequestBuilder prepareStatus(String... indices) {
		return new IndicesStatusRequestBuilder(this).setIndices(indices);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#segments(cn.com.summall.search.core.action.admin.indices.segments.IndicesSegmentsRequest)
	 */
	@Override
	public ActionFuture<IndicesSegmentResponse> segments(final IndicesSegmentsRequest request) {
		return execute(IndicesSegmentsAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#segments(cn.com.summall.search.core.action.admin.indices.segments.IndicesSegmentsRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void segments(final IndicesSegmentsRequest request, final ActionListener<IndicesSegmentResponse> listener) {
		execute(IndicesSegmentsAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#prepareSegments(java.lang.String[])
	 */
	@Override
	public IndicesSegmentsRequestBuilder prepareSegments(String... indices) {
		return new IndicesSegmentsRequestBuilder(this).setIndices(indices);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#updateSettings(cn.com.summall.search.core.action.admin.indices.settings.UpdateSettingsRequest)
	 */
	@Override
	public ActionFuture<UpdateSettingsResponse> updateSettings(final UpdateSettingsRequest request) {
		return execute(UpdateSettingsAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#updateSettings(cn.com.summall.search.core.action.admin.indices.settings.UpdateSettingsRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void updateSettings(final UpdateSettingsRequest request,
			final ActionListener<UpdateSettingsResponse> listener) {
		execute(UpdateSettingsAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#prepareUpdateSettings(java.lang.String[])
	 */
	@Override
	public UpdateSettingsRequestBuilder prepareUpdateSettings(String... indices) {
		return new UpdateSettingsRequestBuilder(this).setIndices(indices);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#analyze(cn.com.summall.search.core.action.admin.indices.analyze.AnalyzeRequest)
	 */
	@Override
	public ActionFuture<AnalyzeResponse> analyze(final AnalyzeRequest request) {
		return execute(AnalyzeAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#analyze(cn.com.summall.search.core.action.admin.indices.analyze.AnalyzeRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void analyze(final AnalyzeRequest request, final ActionListener<AnalyzeResponse> listener) {
		execute(AnalyzeAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#prepareAnalyze(java.lang.String, java.lang.String)
	 */
	@Override
	public AnalyzeRequestBuilder prepareAnalyze(@Nullable String index, String text) {
		return new AnalyzeRequestBuilder(this, index, text);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#prepareAnalyze(java.lang.String)
	 */
	@Override
	public AnalyzeRequestBuilder prepareAnalyze(String text) {
		return new AnalyzeRequestBuilder(this, null, text);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#putTemplate(cn.com.summall.search.core.action.admin.indices.template.put.PutIndexTemplateRequest)
	 */
	@Override
	public ActionFuture<PutIndexTemplateResponse> putTemplate(final PutIndexTemplateRequest request) {
		return execute(PutIndexTemplateAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#putTemplate(cn.com.summall.search.core.action.admin.indices.template.put.PutIndexTemplateRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void putTemplate(final PutIndexTemplateRequest request,
			final ActionListener<PutIndexTemplateResponse> listener) {
		execute(PutIndexTemplateAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#preparePutTemplate(java.lang.String)
	 */
	@Override
	public PutIndexTemplateRequestBuilder preparePutTemplate(String name) {
		return new PutIndexTemplateRequestBuilder(this, name);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#deleteTemplate(cn.com.summall.search.core.action.admin.indices.template.delete.DeleteIndexTemplateRequest)
	 */
	@Override
	public ActionFuture<DeleteIndexTemplateResponse> deleteTemplate(final DeleteIndexTemplateRequest request) {
		return execute(DeleteIndexTemplateAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#deleteTemplate(cn.com.summall.search.core.action.admin.indices.template.delete.DeleteIndexTemplateRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void deleteTemplate(final DeleteIndexTemplateRequest request,
			final ActionListener<DeleteIndexTemplateResponse> listener) {
		execute(DeleteIndexTemplateAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#prepareDeleteTemplate(java.lang.String)
	 */
	@Override
	public DeleteIndexTemplateRequestBuilder prepareDeleteTemplate(String name) {
		return new DeleteIndexTemplateRequestBuilder(this, name);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#validateQuery(cn.com.summall.search.core.action.admin.indices.validate.query.ValidateQueryRequest)
	 */
	@Override
	public ActionFuture<ValidateQueryResponse> validateQuery(final ValidateQueryRequest request) {
		return execute(ValidateQueryAction.INSTANCE, request);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#validateQuery(cn.com.summall.search.core.action.admin.indices.validate.query.ValidateQueryRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	public void validateQuery(final ValidateQueryRequest request, final ActionListener<ValidateQueryResponse> listener) {
		execute(ValidateQueryAction.INSTANCE, request, listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.IndicesAdminClient#prepareValidateQuery(java.lang.String[])
	 */
	@Override
	public ValidateQueryRequestBuilder prepareValidateQuery(String... indices) {
		return new ValidateQueryRequestBuilder(this).setIndices(indices);
	}
}
