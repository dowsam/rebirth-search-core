/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core Requests.java 2012-7-6 14:30:18 l.xue.nong$$
 */

package cn.com.rebirth.search.core.client;

import cn.com.rebirth.search.commons.xcontent.XContentType;
import cn.com.rebirth.search.core.action.admin.cluster.health.ClusterHealthRequest;
import cn.com.rebirth.search.core.action.admin.cluster.node.info.NodesInfoRequest;
import cn.com.rebirth.search.core.action.admin.cluster.node.restart.NodesRestartRequest;
import cn.com.rebirth.search.core.action.admin.cluster.node.shutdown.NodesShutdownRequest;
import cn.com.rebirth.search.core.action.admin.cluster.node.stats.NodesStatsRequest;
import cn.com.rebirth.search.core.action.admin.cluster.reroute.ClusterRerouteRequest;
import cn.com.rebirth.search.core.action.admin.cluster.settings.ClusterUpdateSettingsRequest;
import cn.com.rebirth.search.core.action.admin.cluster.state.ClusterStateRequest;
import cn.com.rebirth.search.core.action.admin.indices.alias.IndicesAliasesRequest;
import cn.com.rebirth.search.core.action.admin.indices.cache.clear.ClearIndicesCacheRequest;
import cn.com.rebirth.search.core.action.admin.indices.close.CloseIndexRequest;
import cn.com.rebirth.search.core.action.admin.indices.create.CreateIndexRequest;
import cn.com.rebirth.search.core.action.admin.indices.delete.DeleteIndexRequest;
import cn.com.rebirth.search.core.action.admin.indices.exists.IndicesExistsRequest;
import cn.com.rebirth.search.core.action.admin.indices.flush.FlushRequest;
import cn.com.rebirth.search.core.action.admin.indices.gateway.snapshot.GatewaySnapshotRequest;
import cn.com.rebirth.search.core.action.admin.indices.mapping.delete.DeleteMappingRequest;
import cn.com.rebirth.search.core.action.admin.indices.mapping.put.PutMappingRequest;
import cn.com.rebirth.search.core.action.admin.indices.open.OpenIndexRequest;
import cn.com.rebirth.search.core.action.admin.indices.optimize.OptimizeRequest;
import cn.com.rebirth.search.core.action.admin.indices.refresh.RefreshRequest;
import cn.com.rebirth.search.core.action.admin.indices.segments.IndicesSegmentsRequest;
import cn.com.rebirth.search.core.action.admin.indices.settings.UpdateSettingsRequest;
import cn.com.rebirth.search.core.action.admin.indices.status.IndicesStatusRequest;
import cn.com.rebirth.search.core.action.bulk.BulkRequest;
import cn.com.rebirth.search.core.action.count.CountRequest;
import cn.com.rebirth.search.core.action.delete.DeleteRequest;
import cn.com.rebirth.search.core.action.deletebyquery.DeleteByQueryRequest;
import cn.com.rebirth.search.core.action.get.GetRequest;
import cn.com.rebirth.search.core.action.index.IndexRequest;
import cn.com.rebirth.search.core.action.mlt.MoreLikeThisRequest;
import cn.com.rebirth.search.core.action.search.SearchRequest;
import cn.com.rebirth.search.core.action.search.SearchScrollRequest;

/**
 * The Class Requests.
 *
 * @author l.xue.nong
 */
public class Requests {

	/** The content type. */
	public static XContentType CONTENT_TYPE = XContentType.SMILE;

	/** The index content type. */
	public static XContentType INDEX_CONTENT_TYPE = XContentType.JSON;

	/**
	 * Index request.
	 *
	 * @return the index request
	 */
	public static IndexRequest indexRequest() {
		return new IndexRequest();
	}

	/**
	 * Index request.
	 *
	 * @param index the index
	 * @return the index request
	 */
	public static IndexRequest indexRequest(String index) {
		return new IndexRequest(index);
	}

	/**
	 * Delete request.
	 *
	 * @param index the index
	 * @return the delete request
	 */
	public static DeleteRequest deleteRequest(String index) {
		return new DeleteRequest(index);
	}

	/**
	 * Bulk request.
	 *
	 * @return the bulk request
	 */
	public static BulkRequest bulkRequest() {
		return new BulkRequest();
	}

	/**
	 * Delete by query request.
	 *
	 * @param indices the indices
	 * @return the delete by query request
	 */
	public static DeleteByQueryRequest deleteByQueryRequest(String... indices) {
		return new DeleteByQueryRequest(indices);
	}

	/**
	 * Gets the request.
	 *
	 * @param index the index
	 * @return the request
	 */
	public static GetRequest getRequest(String index) {
		return new GetRequest(index);
	}

	/**
	 * Count request.
	 *
	 * @param indices the indices
	 * @return the count request
	 */
	public static CountRequest countRequest(String... indices) {
		return new CountRequest(indices);
	}

	/**
	 * More like this request.
	 *
	 * @param index the index
	 * @return the more like this request
	 */
	public static MoreLikeThisRequest moreLikeThisRequest(String index) {
		return new MoreLikeThisRequest(index);
	}

	/**
	 * Search request.
	 *
	 * @param indices the indices
	 * @return the search request
	 */
	public static SearchRequest searchRequest(String... indices) {
		return new SearchRequest(indices);
	}

	/**
	 * Search scroll request.
	 *
	 * @param scrollId the scroll id
	 * @return the search scroll request
	 */
	public static SearchScrollRequest searchScrollRequest(String scrollId) {
		return new SearchScrollRequest(scrollId);
	}

	/**
	 * Indices status request.
	 *
	 * @param indices the indices
	 * @return the indices status request
	 */
	public static IndicesStatusRequest indicesStatusRequest(String... indices) {
		return new IndicesStatusRequest(indices);
	}

	/**
	 * Indices segments request.
	 *
	 * @param indices the indices
	 * @return the indices segments request
	 */
	public static IndicesSegmentsRequest indicesSegmentsRequest(String... indices) {
		return new IndicesSegmentsRequest(indices);
	}

	/**
	 * Indices exists request.
	 *
	 * @param indices the indices
	 * @return the indices exists request
	 */
	public static IndicesExistsRequest indicesExistsRequest(String... indices) {
		return new IndicesExistsRequest(indices);
	}

	/**
	 * Creates the index request.
	 *
	 * @param index the index
	 * @return the creates the index request
	 */
	public static CreateIndexRequest createIndexRequest(String index) {
		return new CreateIndexRequest(index);
	}

	/**
	 * Delete index request.
	 *
	 * @param index the index
	 * @return the delete index request
	 */
	public static DeleteIndexRequest deleteIndexRequest(String index) {
		return new DeleteIndexRequest(index);
	}

	/**
	 * Close index request.
	 *
	 * @param index the index
	 * @return the close index request
	 */
	public static CloseIndexRequest closeIndexRequest(String index) {
		return new CloseIndexRequest(index);
	}

	/**
	 * Open index request.
	 *
	 * @param index the index
	 * @return the open index request
	 */
	public static OpenIndexRequest openIndexRequest(String index) {
		return new OpenIndexRequest(index);
	}

	/**
	 * Put mapping request.
	 *
	 * @param indices the indices
	 * @return the put mapping request
	 */
	public static PutMappingRequest putMappingRequest(String... indices) {
		return new PutMappingRequest(indices);
	}

	/**
	 * Delete mapping request.
	 *
	 * @param indices the indices
	 * @return the delete mapping request
	 */
	public static DeleteMappingRequest deleteMappingRequest(String... indices) {
		return new DeleteMappingRequest(indices);
	}

	/**
	 * Index aliases request.
	 *
	 * @return the indices aliases request
	 */
	public static IndicesAliasesRequest indexAliasesRequest() {
		return new IndicesAliasesRequest();
	}

	/**
	 * Refresh request.
	 *
	 * @param indices the indices
	 * @return the refresh request
	 */
	public static RefreshRequest refreshRequest(String... indices) {
		return new RefreshRequest(indices);
	}

	/**
	 * Flush request.
	 *
	 * @param indices the indices
	 * @return the flush request
	 */
	public static FlushRequest flushRequest(String... indices) {
		return new FlushRequest(indices);
	}

	/**
	 * Optimize request.
	 *
	 * @param indices the indices
	 * @return the optimize request
	 */
	public static OptimizeRequest optimizeRequest(String... indices) {
		return new OptimizeRequest(indices);
	}

	/**
	 * Gateway snapshot request.
	 *
	 * @param indices the indices
	 * @return the gateway snapshot request
	 */
	public static GatewaySnapshotRequest gatewaySnapshotRequest(String... indices) {
		return new GatewaySnapshotRequest(indices);
	}

	/**
	 * Clear indices cache request.
	 *
	 * @param indices the indices
	 * @return the clear indices cache request
	 */
	public static ClearIndicesCacheRequest clearIndicesCacheRequest(String... indices) {
		return new ClearIndicesCacheRequest(indices);
	}

	/**
	 * Update settings request.
	 *
	 * @param indices the indices
	 * @return the update settings request
	 */
	public static UpdateSettingsRequest updateSettingsRequest(String... indices) {
		return new UpdateSettingsRequest(indices);
	}

	/**
	 * Cluster state request.
	 *
	 * @return the cluster state request
	 */
	public static ClusterStateRequest clusterStateRequest() {
		return new ClusterStateRequest();
	}

	/**
	 * Cluster reroute request.
	 *
	 * @return the cluster reroute request
	 */
	public static ClusterRerouteRequest clusterRerouteRequest() {
		return new ClusterRerouteRequest();
	}

	/**
	 * Cluster update settings request.
	 *
	 * @return the cluster update settings request
	 */
	public static ClusterUpdateSettingsRequest clusterUpdateSettingsRequest() {
		return new ClusterUpdateSettingsRequest();
	}

	/**
	 * Cluster health request.
	 *
	 * @param indices the indices
	 * @return the cluster health request
	 */
	public static ClusterHealthRequest clusterHealthRequest(String... indices) {
		return new ClusterHealthRequest(indices);
	}

	/**
	 * Nodes info request.
	 *
	 * @return the nodes info request
	 */
	public static NodesInfoRequest nodesInfoRequest() {
		return new NodesInfoRequest();
	}

	/**
	 * Nodes info request.
	 *
	 * @param nodesIds the nodes ids
	 * @return the nodes info request
	 */
	public static NodesInfoRequest nodesInfoRequest(String... nodesIds) {
		return new NodesInfoRequest(nodesIds);
	}

	/**
	 * Nodes stats request.
	 *
	 * @param nodesIds the nodes ids
	 * @return the nodes stats request
	 */
	public static NodesStatsRequest nodesStatsRequest(String... nodesIds) {
		return new NodesStatsRequest(nodesIds);
	}

	/**
	 * Nodes shutdown request.
	 *
	 * @return the nodes shutdown request
	 */
	public static NodesShutdownRequest nodesShutdownRequest() {
		return new NodesShutdownRequest();
	}

	/**
	 * Nodes shutdown request.
	 *
	 * @param nodesIds the nodes ids
	 * @return the nodes shutdown request
	 */
	public static NodesShutdownRequest nodesShutdownRequest(String... nodesIds) {
		return new NodesShutdownRequest(nodesIds);
	}

	/**
	 * Nodes restart request.
	 *
	 * @return the nodes restart request
	 */
	public static NodesRestartRequest nodesRestartRequest() {
		return new NodesRestartRequest();
	}

	/**
	 * Nodes restart request.
	 *
	 * @param nodesIds the nodes ids
	 * @return the nodes restart request
	 */
	public static NodesRestartRequest nodesRestartRequest(String... nodesIds) {
		return new NodesRestartRequest(nodesIds);
	}
}
