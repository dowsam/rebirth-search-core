/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FutureTransportResponseHandler.java 2012-3-29 15:02:32 l.xue.nong$$
 */


package cn.com.rebirth.search.core.transport;

import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.threadpool.ThreadPool;


/**
 * The Class FutureTransportResponseHandler.
 *
 * @param <T> the generic type
 * @author l.xue.nong
 */
public abstract class FutureTransportResponseHandler<T extends Streamable> extends BaseTransportResponseHandler<T> {

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.transport.TransportResponseHandler#handleResponse(cn.com.summall.search.commons.io.stream.Streamable)
     */
    @Override
    public void handleResponse(T response) {
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.transport.TransportResponseHandler#handleException(cn.com.summall.search.core.transport.TransportException)
     */
    @Override
    public void handleException(TransportException exp) {
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.transport.TransportResponseHandler#executor()
     */
    @Override
    public String executor() {
        return ThreadPool.Names.SAME;
    }
}
