/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TransportRequestHandler.java 2012-3-29 15:00:57 l.xue.nong$$
 */


package cn.com.rebirth.search.core.transport;

import cn.com.rebirth.commons.io.stream.Streamable;


/**
 * The Interface TransportRequestHandler.
 *
 * @param <T> the generic type
 * @author l.xue.nong
 */
public interface TransportRequestHandler<T extends Streamable> {

    
    /**
     * New instance.
     *
     * @return the t
     */
    T newInstance();

    
    /**
     * Message received.
     *
     * @param request the request
     * @param channel the channel
     * @throws Exception the exception
     */
    void messageReceived(T request, TransportChannel channel) throws Exception;

    
    /**
     * Executor.
     *
     * @return the string
     */
    String executor();
}
