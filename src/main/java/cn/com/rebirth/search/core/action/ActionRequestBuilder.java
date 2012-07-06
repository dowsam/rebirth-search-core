/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ActionRequestBuilder.java 2012-3-29 15:00:46 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action;


/**
 * The Interface ActionRequestBuilder.
 *
 * @param <Request> the generic type
 * @param <Response> the generic type
 * @author l.xue.nong
 */
public interface ActionRequestBuilder<Request extends ActionRequest, Response extends ActionResponse> {

    
    /**
     * Request.
     *
     * @return the request
     */
    Request request();

    
    /**
     * Execute.
     *
     * @return the listenable action future
     */
    ListenableActionFuture<Response> execute();

    
    /**
     * Execute.
     *
     * @param listener the listener
     */
    void execute(ActionListener<Response> listener);
}
