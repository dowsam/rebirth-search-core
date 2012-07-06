/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TransportChannel.java 2012-3-29 15:01:12 l.xue.nong$$
 */


package cn.com.rebirth.search.core.transport;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.Streamable;


/**
 * The Interface TransportChannel.
 *
 * @author l.xue.nong
 */
public interface TransportChannel {

    
    /**
     * Action.
     *
     * @return the string
     */
    String action();

    
    /**
     * Send response.
     *
     * @param message the message
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void sendResponse(Streamable message) throws IOException;

    
    /**
     * Send response.
     *
     * @param message the message
     * @param options the options
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void sendResponse(Streamable message, TransportResponseOptions options) throws IOException;

    
    /**
     * Send response.
     *
     * @param error the error
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void sendResponse(Throwable error) throws IOException;
}
