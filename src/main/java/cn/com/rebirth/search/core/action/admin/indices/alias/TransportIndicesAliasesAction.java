/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportIndicesAliasesAction.java 2012-7-6 14:28:43 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.alias;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockException;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockLevel;
import cn.com.rebirth.search.core.cluster.metadata.AliasAction;
import cn.com.rebirth.search.core.cluster.metadata.MetaDataIndexAliasesService;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.TransportService;

import com.google.common.collect.Sets;

/**
 * The Class TransportIndicesAliasesAction.
 *
 * @author l.xue.nong
 */
public class TransportIndicesAliasesAction extends
		TransportMasterNodeOperationAction<IndicesAliasesRequest, IndicesAliasesResponse> {

	/** The index aliases service. */
	private final MetaDataIndexAliasesService indexAliasesService;

	/**
	 * Instantiates a new transport indices aliases action.
	 *
	 * @param settings the settings
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 * @param threadPool the thread pool
	 * @param indexAliasesService the index aliases service
	 */
	@Inject
	public TransportIndicesAliasesAction(Settings settings, TransportService transportService,
			ClusterService clusterService, ThreadPool threadPool, MetaDataIndexAliasesService indexAliasesService) {
		super(settings, transportService, clusterService, threadPool);
		this.indexAliasesService = indexAliasesService;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#executor()
	 */
	@Override
	protected String executor() {
		return ThreadPool.Names.MANAGEMENT;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#transportAction()
	 */
	@Override
	protected String transportAction() {
		return IndicesAliasesAction.NAME;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#newRequest()
	 */
	@Override
	protected IndicesAliasesRequest newRequest() {
		return new IndicesAliasesRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#newResponse()
	 */
	@Override
	protected IndicesAliasesResponse newResponse() {
		return new IndicesAliasesResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#checkBlock(cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest, cn.com.rebirth.search.core.cluster.ClusterState)
	 */
	@Override
	protected ClusterBlockException checkBlock(IndicesAliasesRequest request, ClusterState state) {
		Set<String> indices = Sets.newHashSet();
		for (AliasAction aliasAction : request.aliasActions()) {
			indices.add(aliasAction.index());
		}
		return state.blocks().indicesBlockedException(ClusterBlockLevel.METADATA,
				indices.toArray(new String[indices.size()]));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#masterOperation(cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest, cn.com.rebirth.search.core.cluster.ClusterState)
	 */
	@Override
	protected IndicesAliasesResponse masterOperation(IndicesAliasesRequest request, ClusterState state)
			throws RebirthException {
		final AtomicReference<IndicesAliasesResponse> responseRef = new AtomicReference<IndicesAliasesResponse>();
		final AtomicReference<Throwable> failureRef = new AtomicReference<Throwable>();
		final CountDownLatch latch = new CountDownLatch(1);
		indexAliasesService.indicesAliases(
				new MetaDataIndexAliasesService.Request(request.aliasActions().toArray(
						new AliasAction[request.aliasActions().size()]), request.timeout()),
				new MetaDataIndexAliasesService.Listener() {
					@Override
					public void onResponse(MetaDataIndexAliasesService.Response response) {
						responseRef.set(new IndicesAliasesResponse(response.acknowledged()));
						latch.countDown();
					}

					@Override
					public void onFailure(Throwable t) {
						failureRef.set(t);
						latch.countDown();
					}
				});

		try {
			latch.await();
		} catch (InterruptedException e) {
			failureRef.set(e);
		}

		if (failureRef.get() != null) {
			if (failureRef.get() instanceof RebirthException) {
				throw (RebirthException) failureRef.get();
			} else {
				throw new RebirthException(failureRef.get().getMessage(), failureRef.get());
			}
		}

		return responseRef.get();
	}
}
