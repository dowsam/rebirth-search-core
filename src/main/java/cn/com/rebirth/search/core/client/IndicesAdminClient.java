/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndicesAdminClient.java 2012-7-6 14:29:19 l.xue.nong$$
 */

package cn.com.rebirth.search.core.client;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.search.core.action.ActionFuture;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.ActionRequestBuilder;
import cn.com.rebirth.search.core.action.ActionResponse;
import cn.com.rebirth.search.core.action.admin.indices.IndicesAction;
import cn.com.rebirth.search.core.action.admin.indices.alias.IndicesAliasesRequest;
import cn.com.rebirth.search.core.action.admin.indices.alias.IndicesAliasesRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.alias.IndicesAliasesResponse;
import cn.com.rebirth.search.core.action.admin.indices.analyze.AnalyzeRequest;
import cn.com.rebirth.search.core.action.admin.indices.analyze.AnalyzeRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.analyze.AnalyzeResponse;
import cn.com.rebirth.search.core.action.admin.indices.cache.clear.ClearIndicesCacheRequest;
import cn.com.rebirth.search.core.action.admin.indices.cache.clear.ClearIndicesCacheRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.cache.clear.ClearIndicesCacheResponse;
import cn.com.rebirth.search.core.action.admin.indices.close.CloseIndexRequest;
import cn.com.rebirth.search.core.action.admin.indices.close.CloseIndexRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.close.CloseIndexResponse;
import cn.com.rebirth.search.core.action.admin.indices.create.CreateIndexRequest;
import cn.com.rebirth.search.core.action.admin.indices.create.CreateIndexRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.create.CreateIndexResponse;
import cn.com.rebirth.search.core.action.admin.indices.delete.DeleteIndexRequest;
import cn.com.rebirth.search.core.action.admin.indices.delete.DeleteIndexRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.delete.DeleteIndexResponse;
import cn.com.rebirth.search.core.action.admin.indices.exists.IndicesExistsRequest;
import cn.com.rebirth.search.core.action.admin.indices.exists.IndicesExistsRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.exists.IndicesExistsResponse;
import cn.com.rebirth.search.core.action.admin.indices.flush.FlushRequest;
import cn.com.rebirth.search.core.action.admin.indices.flush.FlushRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.flush.FlushResponse;
import cn.com.rebirth.search.core.action.admin.indices.gateway.snapshot.GatewaySnapshotRequest;
import cn.com.rebirth.search.core.action.admin.indices.gateway.snapshot.GatewaySnapshotRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.gateway.snapshot.GatewaySnapshotResponse;
import cn.com.rebirth.search.core.action.admin.indices.mapping.delete.DeleteMappingRequest;
import cn.com.rebirth.search.core.action.admin.indices.mapping.delete.DeleteMappingRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.mapping.delete.DeleteMappingResponse;
import cn.com.rebirth.search.core.action.admin.indices.mapping.put.PutMappingRequest;
import cn.com.rebirth.search.core.action.admin.indices.mapping.put.PutMappingRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.mapping.put.PutMappingResponse;
import cn.com.rebirth.search.core.action.admin.indices.open.OpenIndexRequest;
import cn.com.rebirth.search.core.action.admin.indices.open.OpenIndexRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.open.OpenIndexResponse;
import cn.com.rebirth.search.core.action.admin.indices.optimize.OptimizeRequest;
import cn.com.rebirth.search.core.action.admin.indices.optimize.OptimizeRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.optimize.OptimizeResponse;
import cn.com.rebirth.search.core.action.admin.indices.refresh.RefreshRequest;
import cn.com.rebirth.search.core.action.admin.indices.refresh.RefreshRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.refresh.RefreshResponse;
import cn.com.rebirth.search.core.action.admin.indices.segments.IndicesSegmentResponse;
import cn.com.rebirth.search.core.action.admin.indices.segments.IndicesSegmentsRequest;
import cn.com.rebirth.search.core.action.admin.indices.segments.IndicesSegmentsRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.settings.UpdateSettingsRequest;
import cn.com.rebirth.search.core.action.admin.indices.settings.UpdateSettingsRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.settings.UpdateSettingsResponse;
import cn.com.rebirth.search.core.action.admin.indices.stats.IndicesStats;
import cn.com.rebirth.search.core.action.admin.indices.stats.IndicesStatsRequest;
import cn.com.rebirth.search.core.action.admin.indices.stats.IndicesStatsRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.status.IndicesStatusRequest;
import cn.com.rebirth.search.core.action.admin.indices.status.IndicesStatusRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.status.IndicesStatusResponse;
import cn.com.rebirth.search.core.action.admin.indices.template.delete.DeleteIndexTemplateRequest;
import cn.com.rebirth.search.core.action.admin.indices.template.delete.DeleteIndexTemplateRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.template.delete.DeleteIndexTemplateResponse;
import cn.com.rebirth.search.core.action.admin.indices.template.put.PutIndexTemplateRequest;
import cn.com.rebirth.search.core.action.admin.indices.template.put.PutIndexTemplateRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.template.put.PutIndexTemplateResponse;
import cn.com.rebirth.search.core.action.admin.indices.validate.query.ValidateQueryRequest;
import cn.com.rebirth.search.core.action.admin.indices.validate.query.ValidateQueryRequestBuilder;
import cn.com.rebirth.search.core.action.admin.indices.validate.query.ValidateQueryResponse;

/**
 * The Interface IndicesAdminClient.
 *
 * @author l.xue.nong
 */
public interface IndicesAdminClient {

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
			final IndicesAction<Request, Response, RequestBuilder> action, final Request request);

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
			final IndicesAction<Request, Response, RequestBuilder> action, final Request request,
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
			final IndicesAction<Request, Response, RequestBuilder> action);

	/**
	 * Exists.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<IndicesExistsResponse> exists(IndicesExistsRequest request);

	/**
	 * Exists.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void exists(IndicesExistsRequest request, ActionListener<IndicesExistsResponse> listener);

	/**
	 * Prepare exists.
	 *
	 * @param indices the indices
	 * @return the indices exists request builder
	 */
	IndicesExistsRequestBuilder prepareExists(String... indices);

	/**
	 * Stats.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<IndicesStats> stats(IndicesStatsRequest request);

	/**
	 * Stats.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void stats(IndicesStatsRequest request, ActionListener<IndicesStats> listener);

	/**
	 * Prepare stats.
	 *
	 * @param indices the indices
	 * @return the indices stats request builder
	 */
	IndicesStatsRequestBuilder prepareStats(String... indices);

	/**
	 * Status.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<IndicesStatusResponse> status(IndicesStatusRequest request);

	/**
	 * Status.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void status(IndicesStatusRequest request, ActionListener<IndicesStatusResponse> listener);

	/**
	 * Prepare status.
	 *
	 * @param indices the indices
	 * @return the indices status request builder
	 */
	IndicesStatusRequestBuilder prepareStatus(String... indices);

	/**
	 * Segments.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<IndicesSegmentResponse> segments(IndicesSegmentsRequest request);

	/**
	 * Segments.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void segments(IndicesSegmentsRequest request, ActionListener<IndicesSegmentResponse> listener);

	/**
	 * Prepare segments.
	 *
	 * @param indices the indices
	 * @return the indices segments request builder
	 */
	IndicesSegmentsRequestBuilder prepareSegments(String... indices);

	/**
	 * Creates the.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<CreateIndexResponse> create(CreateIndexRequest request);

	/**
	 * Creates the.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void create(CreateIndexRequest request, ActionListener<CreateIndexResponse> listener);

	/**
	 * Prepare create.
	 *
	 * @param index the index
	 * @return the creates the index request builder
	 */
	CreateIndexRequestBuilder prepareCreate(String index);

	/**
	 * Delete.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<DeleteIndexResponse> delete(DeleteIndexRequest request);

	/**
	 * Delete.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void delete(DeleteIndexRequest request, ActionListener<DeleteIndexResponse> listener);

	/**
	 * Prepare delete.
	 *
	 * @param indices the indices
	 * @return the delete index request builder
	 */
	DeleteIndexRequestBuilder prepareDelete(String... indices);

	/**
	 * Close.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<CloseIndexResponse> close(CloseIndexRequest request);

	/**
	 * Close.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void close(CloseIndexRequest request, ActionListener<CloseIndexResponse> listener);

	/**
	 * Prepare close.
	 *
	 * @param index the index
	 * @return the close index request builder
	 */
	CloseIndexRequestBuilder prepareClose(String index);

	/**
	 * Open.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<OpenIndexResponse> open(OpenIndexRequest request);

	/**
	 * Open.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void open(OpenIndexRequest request, ActionListener<OpenIndexResponse> listener);

	/**
	 * Prepare open.
	 *
	 * @param index the index
	 * @return the open index request builder
	 */
	OpenIndexRequestBuilder prepareOpen(String index);

	/**
	 * Refresh.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<RefreshResponse> refresh(RefreshRequest request);

	/**
	 * Refresh.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void refresh(RefreshRequest request, ActionListener<RefreshResponse> listener);

	/**
	 * Prepare refresh.
	 *
	 * @param indices the indices
	 * @return the refresh request builder
	 */
	RefreshRequestBuilder prepareRefresh(String... indices);

	/**
	 * Flush.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<FlushResponse> flush(FlushRequest request);

	/**
	 * Flush.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void flush(FlushRequest request, ActionListener<FlushResponse> listener);

	/**
	 * Prepare flush.
	 *
	 * @param indices the indices
	 * @return the flush request builder
	 */
	FlushRequestBuilder prepareFlush(String... indices);

	/**
	 * Optimize.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<OptimizeResponse> optimize(OptimizeRequest request);

	/**
	 * Optimize.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void optimize(OptimizeRequest request, ActionListener<OptimizeResponse> listener);

	/**
	 * Prepare optimize.
	 *
	 * @param indices the indices
	 * @return the optimize request builder
	 */
	OptimizeRequestBuilder prepareOptimize(String... indices);

	/**
	 * Put mapping.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<PutMappingResponse> putMapping(PutMappingRequest request);

	/**
	 * Put mapping.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void putMapping(PutMappingRequest request, ActionListener<PutMappingResponse> listener);

	/**
	 * Prepare put mapping.
	 *
	 * @param indices the indices
	 * @return the put mapping request builder
	 */
	PutMappingRequestBuilder preparePutMapping(String... indices);

	/**
	 * Delete mapping.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<DeleteMappingResponse> deleteMapping(DeleteMappingRequest request);

	/**
	 * Delete mapping.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void deleteMapping(DeleteMappingRequest request, ActionListener<DeleteMappingResponse> listener);

	/**
	 * Prepare delete mapping.
	 *
	 * @param indices the indices
	 * @return the delete mapping request builder
	 */
	DeleteMappingRequestBuilder prepareDeleteMapping(String... indices);

	/**
	 * Gateway snapshot.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<GatewaySnapshotResponse> gatewaySnapshot(GatewaySnapshotRequest request);

	/**
	 * Gateway snapshot.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void gatewaySnapshot(GatewaySnapshotRequest request, ActionListener<GatewaySnapshotResponse> listener);

	/**
	 * Prepare gateway snapshot.
	 *
	 * @param indices the indices
	 * @return the gateway snapshot request builder
	 */
	GatewaySnapshotRequestBuilder prepareGatewaySnapshot(String... indices);

	/**
	 * Aliases.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<IndicesAliasesResponse> aliases(IndicesAliasesRequest request);

	/**
	 * Aliases.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void aliases(IndicesAliasesRequest request, ActionListener<IndicesAliasesResponse> listener);

	/**
	 * Prepare aliases.
	 *
	 * @return the indices aliases request builder
	 */
	IndicesAliasesRequestBuilder prepareAliases();

	/**
	 * Clear cache.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<ClearIndicesCacheResponse> clearCache(ClearIndicesCacheRequest request);

	/**
	 * Clear cache.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void clearCache(ClearIndicesCacheRequest request, ActionListener<ClearIndicesCacheResponse> listener);

	/**
	 * Prepare clear cache.
	 *
	 * @param indices the indices
	 * @return the clear indices cache request builder
	 */
	ClearIndicesCacheRequestBuilder prepareClearCache(String... indices);

	/**
	 * Update settings.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<UpdateSettingsResponse> updateSettings(UpdateSettingsRequest request);

	/**
	 * Update settings.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void updateSettings(UpdateSettingsRequest request, ActionListener<UpdateSettingsResponse> listener);

	/**
	 * Prepare update settings.
	 *
	 * @param indices the indices
	 * @return the update settings request builder
	 */
	UpdateSettingsRequestBuilder prepareUpdateSettings(String... indices);

	/**
	 * Analyze.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<AnalyzeResponse> analyze(AnalyzeRequest request);

	/**
	 * Analyze.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void analyze(AnalyzeRequest request, ActionListener<AnalyzeResponse> listener);

	/**
	 * Prepare analyze.
	 *
	 * @param index the index
	 * @param text the text
	 * @return the analyze request builder
	 */
	AnalyzeRequestBuilder prepareAnalyze(@Nullable String index, String text);

	/**
	 * Prepare analyze.
	 *
	 * @param text the text
	 * @return the analyze request builder
	 */
	AnalyzeRequestBuilder prepareAnalyze(String text);

	/**
	 * Put template.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<PutIndexTemplateResponse> putTemplate(PutIndexTemplateRequest request);

	/**
	 * Put template.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void putTemplate(PutIndexTemplateRequest request, ActionListener<PutIndexTemplateResponse> listener);

	/**
	 * Prepare put template.
	 *
	 * @param name the name
	 * @return the put index template request builder
	 */
	PutIndexTemplateRequestBuilder preparePutTemplate(String name);

	/**
	 * Delete template.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<DeleteIndexTemplateResponse> deleteTemplate(DeleteIndexTemplateRequest request);

	/**
	 * Delete template.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void deleteTemplate(DeleteIndexTemplateRequest request, ActionListener<DeleteIndexTemplateResponse> listener);

	/**
	 * Prepare delete template.
	 *
	 * @param name the name
	 * @return the delete index template request builder
	 */
	DeleteIndexTemplateRequestBuilder prepareDeleteTemplate(String name);

	/**
	 * Validate query.
	 *
	 * @param request the request
	 * @return the action future
	 */
	ActionFuture<ValidateQueryResponse> validateQuery(ValidateQueryRequest request);

	/**
	 * Validate query.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	void validateQuery(ValidateQueryRequest request, ActionListener<ValidateQueryResponse> listener);

	/**
	 * Prepare validate query.
	 *
	 * @param indices the indices
	 * @return the validate query request builder
	 */
	ValidateQueryRequestBuilder prepareValidateQuery(String... indices);
}
