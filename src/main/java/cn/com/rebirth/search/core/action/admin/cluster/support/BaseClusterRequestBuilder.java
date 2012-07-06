/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core BaseClusterRequestBuilder.java 2012-3-29 15:01:04 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.cluster.support;

import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.ActionRequestBuilder;
import cn.com.rebirth.search.core.action.ActionResponse;
import cn.com.rebirth.search.core.action.ListenableActionFuture;
import cn.com.rebirth.search.core.action.support.PlainListenableActionFuture;
import cn.com.rebirth.search.core.client.ClusterAdminClient;
import cn.com.rebirth.search.core.client.internal.InternalClusterAdminClient;


/**
 * The Class BaseClusterRequestBuilder.
 *
 * @param <Request> the generic type
 * @param <Response> the generic type
 * @author l.xue.nong
 */
public abstract class BaseClusterRequestBuilder<Request extends ActionRequest, Response extends ActionResponse> implements ActionRequestBuilder<Request, Response> {

    
    /** The client. */
    protected final InternalClusterAdminClient client;

    
    /** The request. */
    protected final Request request;

    
    /**
     * Instantiates a new base cluster request builder.
     *
     * @param client the client
     * @param request the request
     */
    protected BaseClusterRequestBuilder(ClusterAdminClient client, Request request) {
        this.client = (InternalClusterAdminClient) client;
        this.request = request;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.action.ActionRequestBuilder#request()
     */
    @Override
    public Request request() {
        return request;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.action.ActionRequestBuilder#execute()
     */
    @Override
    public ListenableActionFuture<Response> execute() {
        PlainListenableActionFuture<Response> future = new PlainListenableActionFuture<Response>(request.listenerThreaded(), client.threadPool());
        execute(future);
        return future;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.action.ActionRequestBuilder#execute(cn.com.summall.search.core.action.ActionListener)
     */
    @Override
    public void execute(ActionListener<Response> listener) {
        doExecute(listener);
    }

    
    /**
     * Do execute.
     *
     * @param listener the listener
     */
    protected abstract void doExecute(ActionListener<Response> listener);
}