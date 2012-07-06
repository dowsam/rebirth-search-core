/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ActionRequest.java 2012-3-29 15:02:17 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action;

import cn.com.rebirth.commons.io.stream.Streamable;


/**
 * The Interface ActionRequest.
 *
 * @author l.xue.nong
 */
public interface ActionRequest extends Streamable {

    
    /**
     * Validate.
     *
     * @return the action request validation exception
     */
    ActionRequestValidationException validate();

    
    /**
     * Listener threaded.
     *
     * @return true, if successful
     */
    boolean listenerThreaded();

    
    /**
     * Listener threaded.
     *
     * @param listenerThreaded the listener threaded
     * @return the action request
     */
    ActionRequest listenerThreaded(boolean listenerThreaded);
}
