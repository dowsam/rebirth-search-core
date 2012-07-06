/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RestFilterChain.java 2012-3-29 15:01:12 l.xue.nong$$
 */


package cn.com.rebirth.search.core.rest;


/**
 * The Interface RestFilterChain.
 *
 * @author l.xue.nong
 */
public interface RestFilterChain {

    
    /**
     * Continue processing.
     *
     * @param request the request
     * @param channel the channel
     */
    void continueProcessing(RestRequest request, RestChannel channel);
}
