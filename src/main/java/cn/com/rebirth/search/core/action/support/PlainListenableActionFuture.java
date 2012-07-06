/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core PlainListenableActionFuture.java 2012-3-29 15:01:24 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.support;

import cn.com.rebirth.search.core.threadpool.ThreadPool;


/**
 * The Class PlainListenableActionFuture.
 *
 * @param <T> the generic type
 * @author l.xue.nong
 */
public class PlainListenableActionFuture<T> extends AbstractListenableActionFuture<T, T> {

    
    /**
     * Instantiates a new plain listenable action future.
     *
     * @param listenerThreaded the listener threaded
     * @param threadPool the thread pool
     */
    public PlainListenableActionFuture(boolean listenerThreaded, ThreadPool threadPool) {
        super(listenerThreaded, threadPool);
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.action.support.AdapterActionFuture#convert(java.lang.Object)
     */
    @Override
    protected T convert(T response) {
        return response;
    }
}
