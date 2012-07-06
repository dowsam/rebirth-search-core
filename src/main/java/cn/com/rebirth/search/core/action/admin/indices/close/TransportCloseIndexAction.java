/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TransportCloseIndexAction.java 2012-3-29 15:02:31 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.close;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockException;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockLevel;
import cn.com.rebirth.search.core.cluster.metadata.MetaDataStateIndexService;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.TransportService;


/**
 * The Class TransportCloseIndexAction.
 *
 * @author l.xue.nong
 */
public class TransportCloseIndexAction extends TransportMasterNodeOperationAction<CloseIndexRequest, CloseIndexResponse> {

    
    /** The state index service. */
    private final MetaDataStateIndexService stateIndexService;

    
    /**
     * Instantiates a new transport close index action.
     *
     * @param settings the settings
     * @param transportService the transport service
     * @param clusterService the cluster service
     * @param threadPool the thread pool
     * @param stateIndexService the state index service
     */
    @Inject
    public TransportCloseIndexAction(Settings settings, TransportService transportService, ClusterService clusterService,
                                     ThreadPool threadPool, MetaDataStateIndexService stateIndexService) {
        super(settings, transportService, clusterService, threadPool);
        this.stateIndexService = stateIndexService;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.action.support.master.TransportMasterNodeOperationAction#executor()
     */
    @Override
    protected String executor() {
        return ThreadPool.Names.MANAGEMENT;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.action.support.master.TransportMasterNodeOperationAction#transportAction()
     */
    @Override
    protected String transportAction() {
        return CloseIndexAction.NAME;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.action.support.master.TransportMasterNodeOperationAction#newRequest()
     */
    @Override
    protected CloseIndexRequest newRequest() {
        return new CloseIndexRequest();
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.action.support.master.TransportMasterNodeOperationAction#newResponse()
     */
    @Override
    protected CloseIndexResponse newResponse() {
        return new CloseIndexResponse();
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.action.support.master.TransportMasterNodeOperationAction#checkBlock(cn.com.summall.search.core.action.support.master.MasterNodeOperationRequest, cn.com.summall.search.core.cluster.ClusterState)
     */
    @Override
    protected ClusterBlockException checkBlock(CloseIndexRequest request, ClusterState state) {
        request.index(clusterService.state().metaData().concreteIndex(request.index()));
        return state.blocks().indexBlockedException(ClusterBlockLevel.METADATA, request.index());
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.action.support.master.TransportMasterNodeOperationAction#masterOperation(cn.com.summall.search.core.action.support.master.MasterNodeOperationRequest, cn.com.summall.search.core.cluster.ClusterState)
     */
    @Override
    protected CloseIndexResponse masterOperation(CloseIndexRequest request, ClusterState state) throws RestartException {
        final AtomicReference<CloseIndexResponse> responseRef = new AtomicReference<CloseIndexResponse>();
        final AtomicReference<Throwable> failureRef = new AtomicReference<Throwable>();
        final CountDownLatch latch = new CountDownLatch(1);
        stateIndexService.closeIndex(new MetaDataStateIndexService.Request(request.index()).timeout(request.timeout()), new MetaDataStateIndexService.Listener() {
            @Override
            public void onResponse(MetaDataStateIndexService.Response response) {
                responseRef.set(new CloseIndexResponse(response.acknowledged()));
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
            if (failureRef.get() instanceof RestartException) {
                throw (RestartException) failureRef.get();
            } else {
                throw new RestartException(failureRef.get().getMessage(), failureRef.get());
            }
        }

        return responseRef.get();
    }
}
