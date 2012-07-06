/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportClient.java 2012-7-6 14:29:51 l.xue.nong$$
 */

package cn.com.rebirth.search.core.client.transport;

import java.util.concurrent.TimeUnit;

import cn.com.rebirth.commons.collect.Tuple;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.thread.ThreadLocals;
import cn.com.rebirth.search.commons.CacheRecycler;
import cn.com.rebirth.search.commons.inject.Injector;
import cn.com.rebirth.search.commons.inject.ModulesBuilder;
import cn.com.rebirth.search.commons.io.CachedStreams;
import cn.com.rebirth.search.commons.network.NetworkModule;
import cn.com.rebirth.search.commons.settings.ImmutableSettings;
import cn.com.rebirth.search.commons.settings.SettingsModule;
import cn.com.rebirth.search.commons.transport.TransportAddress;
import cn.com.rebirth.search.core.action.Action;
import cn.com.rebirth.search.core.action.ActionFuture;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.ActionModule;
import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.ActionRequestBuilder;
import cn.com.rebirth.search.core.action.ActionResponse;
import cn.com.rebirth.search.core.action.bulk.BulkRequest;
import cn.com.rebirth.search.core.action.bulk.BulkResponse;
import cn.com.rebirth.search.core.action.count.CountRequest;
import cn.com.rebirth.search.core.action.count.CountResponse;
import cn.com.rebirth.search.core.action.delete.DeleteRequest;
import cn.com.rebirth.search.core.action.delete.DeleteResponse;
import cn.com.rebirth.search.core.action.deletebyquery.DeleteByQueryRequest;
import cn.com.rebirth.search.core.action.deletebyquery.DeleteByQueryResponse;
import cn.com.rebirth.search.core.action.get.GetRequest;
import cn.com.rebirth.search.core.action.get.GetResponse;
import cn.com.rebirth.search.core.action.get.MultiGetRequest;
import cn.com.rebirth.search.core.action.get.MultiGetResponse;
import cn.com.rebirth.search.core.action.index.IndexRequest;
import cn.com.rebirth.search.core.action.index.IndexResponse;
import cn.com.rebirth.search.core.action.mlt.MoreLikeThisRequest;
import cn.com.rebirth.search.core.action.percolate.PercolateRequest;
import cn.com.rebirth.search.core.action.percolate.PercolateResponse;
import cn.com.rebirth.search.core.action.search.MultiSearchRequest;
import cn.com.rebirth.search.core.action.search.MultiSearchResponse;
import cn.com.rebirth.search.core.action.search.SearchRequest;
import cn.com.rebirth.search.core.action.search.SearchResponse;
import cn.com.rebirth.search.core.action.search.SearchScrollRequest;
import cn.com.rebirth.search.core.action.update.UpdateRequest;
import cn.com.rebirth.search.core.action.update.UpdateResponse;
import cn.com.rebirth.search.core.client.AdminClient;
import cn.com.rebirth.search.core.client.support.AbstractClient;
import cn.com.rebirth.search.core.client.transport.support.InternalTransportClient;
import cn.com.rebirth.search.core.cluster.ClusterNameModule;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.env.Environment;
import cn.com.rebirth.search.core.env.EnvironmentModule;
import cn.com.rebirth.search.core.monitor.MonitorService;
import cn.com.rebirth.search.core.node.internal.InternalSettingsPerparer;
import cn.com.rebirth.search.core.search.TransportSearchModule;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.threadpool.ThreadPoolModule;
import cn.com.rebirth.search.core.transport.TransportModule;
import cn.com.rebirth.search.core.transport.TransportService;

import com.google.common.collect.ImmutableList;

/**
 * The Class TransportClient.
 *
 * @author l.xue.nong
 */
public class TransportClient extends AbstractClient {

	/** The injector. */
	private final Injector injector;

	/** The settings. */
	private final Settings settings;

	/** The environment. */
	private final Environment environment;

	/** The nodes service. */
	private final TransportClientNodesService nodesService;

	/** The internal client. */
	private final InternalTransportClient internalClient;

	/**
	 * Instantiates a new transport client.
	 *
	 * @throws RebirthException the rebirth exception
	 */
	public TransportClient() throws RebirthException {
		this(ImmutableSettings.Builder.EMPTY_SETTINGS, true);
	}

	/**
	 * Instantiates a new transport client.
	 *
	 * @param settings the settings
	 */
	public TransportClient(Settings settings) {
		this(settings, true);
	}

	/**
	 * Instantiates a new transport client.
	 *
	 * @param settings the settings
	 */
	public TransportClient(Settings.Builder settings) {
		this(settings.build(), true);
	}

	/**
	 * Instantiates a new transport client.
	 *
	 * @param settings the settings
	 * @param loadConfigSettings the load config settings
	 * @throws RebirthException the rebirth exception
	 */
	public TransportClient(Settings.Builder settings, boolean loadConfigSettings) throws RebirthException {
		this(settings.build(), loadConfigSettings);
	}

	/**
	 * Instantiates a new transport client.
	 *
	 * @param pSettings the settings
	 * @param loadConfigSettings the load config settings
	 * @throws RebirthException the rebirth exception
	 */
	public TransportClient(Settings pSettings, boolean loadConfigSettings) throws RebirthException {
		Tuple<Settings, Environment> tuple = InternalSettingsPerparer.prepareSettings(pSettings, loadConfigSettings);
		this.settings = ImmutableSettings.settingsBuilder().put(tuple.v1()).put("network.server", false)
				.put("node.client", true).build();
		this.environment = tuple.v2();

		ModulesBuilder modules = new ModulesBuilder();
		modules.add(new EnvironmentModule(environment));
		modules.add(new SettingsModule(settings));
		modules.add(new NetworkModule());
		modules.add(new ClusterNameModule(settings));
		modules.add(new ThreadPoolModule(settings));
		modules.add(new TransportSearchModule());
		modules.add(new TransportModule(settings));
		modules.add(new ActionModule(true));
		modules.add(new ClientTransportModule());

		injector = modules.createInjector();

		injector.getInstance(TransportService.class).start();

		nodesService = injector.getInstance(TransportClientNodesService.class);
		internalClient = injector.getInstance(InternalTransportClient.class);
	}

	/**
	 * Transport addresses.
	 *
	 * @return the immutable list
	 */
	public ImmutableList<TransportAddress> transportAddresses() {
		return nodesService.transportAddresses();
	}

	/**
	 * Connected nodes.
	 *
	 * @return the immutable list
	 */
	public ImmutableList<DiscoveryNode> connectedNodes() {
		return nodesService.connectedNodes();
	}

	/**
	 * Adds the transport address.
	 *
	 * @param transportAddress the transport address
	 * @return the transport client
	 */
	public TransportClient addTransportAddress(TransportAddress transportAddress) {
		nodesService.addTransportAddress(transportAddress);
		return this;
	}

	/**
	 * Removes the transport address.
	 *
	 * @param transportAddress the transport address
	 * @return the transport client
	 */
	public TransportClient removeTransportAddress(TransportAddress transportAddress) {
		nodesService.removeTransportAddress(transportAddress);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.Client#close()
	 */
	@Override
	public void close() {
		injector.getInstance(TransportClientNodesService.class).close();
		injector.getInstance(TransportService.class).close();
		try {
			injector.getInstance(MonitorService.class).close();
		} catch (Exception e) {

		}

		injector.getInstance(ThreadPool.class).shutdown();
		try {
			injector.getInstance(ThreadPool.class).awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {

		}
		try {
			injector.getInstance(ThreadPool.class).shutdownNow();
		} catch (Exception e) {

		}

		CacheRecycler.clear();
		CachedStreams.clear();
		ThreadLocals.clearReferencesThreadLocals();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.internal.InternalClient#threadPool()
	 */
	@Override
	public ThreadPool threadPool() {
		return internalClient.threadPool();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.Client#admin()
	 */
	@Override
	public AdminClient admin() {
		return internalClient.admin();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.Client#execute(cn.com.rebirth.search.core.action.Action, cn.com.rebirth.search.core.action.ActionRequest)
	 */
	@Override
	public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response>> ActionFuture<Response> execute(
			Action<Request, Response, RequestBuilder> action, Request request) {
		return internalClient.execute(action, request);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.Client#execute(cn.com.rebirth.search.core.action.Action, cn.com.rebirth.search.core.action.ActionRequest, cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response>> void execute(
			Action<Request, Response, RequestBuilder> action, Request request, ActionListener<Response> listener) {
		internalClient.execute(action, request, listener);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.support.AbstractClient#index(cn.com.rebirth.search.core.action.index.IndexRequest)
	 */
	@Override
	public ActionFuture<IndexResponse> index(IndexRequest request) {
		return internalClient.index(request);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.support.AbstractClient#index(cn.com.rebirth.search.core.action.index.IndexRequest, cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	public void index(IndexRequest request, ActionListener<IndexResponse> listener) {
		internalClient.index(request, listener);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.support.AbstractClient#update(cn.com.rebirth.search.core.action.update.UpdateRequest)
	 */
	@Override
	public ActionFuture<UpdateResponse> update(UpdateRequest request) {
		return internalClient.update(request);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.support.AbstractClient#update(cn.com.rebirth.search.core.action.update.UpdateRequest, cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	public void update(UpdateRequest request, ActionListener<UpdateResponse> listener) {
		internalClient.update(request, listener);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.support.AbstractClient#delete(cn.com.rebirth.search.core.action.delete.DeleteRequest)
	 */
	@Override
	public ActionFuture<DeleteResponse> delete(DeleteRequest request) {
		return internalClient.delete(request);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.support.AbstractClient#delete(cn.com.rebirth.search.core.action.delete.DeleteRequest, cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	public void delete(DeleteRequest request, ActionListener<DeleteResponse> listener) {
		internalClient.delete(request, listener);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.support.AbstractClient#bulk(cn.com.rebirth.search.core.action.bulk.BulkRequest)
	 */
	@Override
	public ActionFuture<BulkResponse> bulk(BulkRequest request) {
		return internalClient.bulk(request);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.support.AbstractClient#bulk(cn.com.rebirth.search.core.action.bulk.BulkRequest, cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	public void bulk(BulkRequest request, ActionListener<BulkResponse> listener) {
		internalClient.bulk(request, listener);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.support.AbstractClient#deleteByQuery(cn.com.rebirth.search.core.action.deletebyquery.DeleteByQueryRequest)
	 */
	@Override
	public ActionFuture<DeleteByQueryResponse> deleteByQuery(DeleteByQueryRequest request) {
		return internalClient.deleteByQuery(request);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.support.AbstractClient#deleteByQuery(cn.com.rebirth.search.core.action.deletebyquery.DeleteByQueryRequest, cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	public void deleteByQuery(DeleteByQueryRequest request, ActionListener<DeleteByQueryResponse> listener) {
		internalClient.deleteByQuery(request, listener);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.support.AbstractClient#get(cn.com.rebirth.search.core.action.get.GetRequest)
	 */
	@Override
	public ActionFuture<GetResponse> get(GetRequest request) {
		return internalClient.get(request);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.support.AbstractClient#get(cn.com.rebirth.search.core.action.get.GetRequest, cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	public void get(GetRequest request, ActionListener<GetResponse> listener) {
		internalClient.get(request, listener);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.support.AbstractClient#multiGet(cn.com.rebirth.search.core.action.get.MultiGetRequest)
	 */
	@Override
	public ActionFuture<MultiGetResponse> multiGet(MultiGetRequest request) {
		return internalClient.multiGet(request);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.support.AbstractClient#multiGet(cn.com.rebirth.search.core.action.get.MultiGetRequest, cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	public void multiGet(MultiGetRequest request, ActionListener<MultiGetResponse> listener) {
		internalClient.multiGet(request, listener);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.support.AbstractClient#count(cn.com.rebirth.search.core.action.count.CountRequest)
	 */
	@Override
	public ActionFuture<CountResponse> count(CountRequest request) {
		return internalClient.count(request);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.support.AbstractClient#count(cn.com.rebirth.search.core.action.count.CountRequest, cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	public void count(CountRequest request, ActionListener<CountResponse> listener) {
		internalClient.count(request, listener);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.support.AbstractClient#search(cn.com.rebirth.search.core.action.search.SearchRequest)
	 */
	@Override
	public ActionFuture<SearchResponse> search(SearchRequest request) {
		return internalClient.search(request);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.support.AbstractClient#search(cn.com.rebirth.search.core.action.search.SearchRequest, cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	public void search(SearchRequest request, ActionListener<SearchResponse> listener) {
		internalClient.search(request, listener);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.support.AbstractClient#searchScroll(cn.com.rebirth.search.core.action.search.SearchScrollRequest)
	 */
	@Override
	public ActionFuture<SearchResponse> searchScroll(SearchScrollRequest request) {
		return internalClient.searchScroll(request);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.support.AbstractClient#searchScroll(cn.com.rebirth.search.core.action.search.SearchScrollRequest, cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	public void searchScroll(SearchScrollRequest request, ActionListener<SearchResponse> listener) {
		internalClient.searchScroll(request, listener);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.support.AbstractClient#multiSearch(cn.com.rebirth.search.core.action.search.MultiSearchRequest)
	 */
	@Override
	public ActionFuture<MultiSearchResponse> multiSearch(MultiSearchRequest request) {
		return internalClient.multiSearch(request);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.support.AbstractClient#multiSearch(cn.com.rebirth.search.core.action.search.MultiSearchRequest, cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	public void multiSearch(MultiSearchRequest request, ActionListener<MultiSearchResponse> listener) {
		internalClient.multiSearch(request, listener);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.support.AbstractClient#moreLikeThis(cn.com.rebirth.search.core.action.mlt.MoreLikeThisRequest)
	 */
	@Override
	public ActionFuture<SearchResponse> moreLikeThis(MoreLikeThisRequest request) {
		return internalClient.moreLikeThis(request);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.support.AbstractClient#moreLikeThis(cn.com.rebirth.search.core.action.mlt.MoreLikeThisRequest, cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	public void moreLikeThis(MoreLikeThisRequest request, ActionListener<SearchResponse> listener) {
		internalClient.moreLikeThis(request, listener);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.support.AbstractClient#percolate(cn.com.rebirth.search.core.action.percolate.PercolateRequest)
	 */
	@Override
	public ActionFuture<PercolateResponse> percolate(PercolateRequest request) {
		return internalClient.percolate(request);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.support.AbstractClient#percolate(cn.com.rebirth.search.core.action.percolate.PercolateRequest, cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	public void percolate(PercolateRequest request, ActionListener<PercolateResponse> listener) {
		internalClient.percolate(request, listener);
	}
}
