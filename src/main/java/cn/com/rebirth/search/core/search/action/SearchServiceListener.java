/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SearchServiceListener.java 2012-3-29 15:02:39 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.action;


/**
 * The listener interface for receiving searchService events.
 * The class that is interested in processing a searchService
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addSearchServiceListener<code> method. When
 * the searchService event occurs, that object's appropriate
 * method is invoked.
 *
 * @param <T> the generic type
 * @see SearchServiceEvent
 */
public interface SearchServiceListener<T> {

    /**
     * On result.
     *
     * @param result the result
     */
    void onResult(T result);

    /**
     * On failure.
     *
     * @param t the t
     */
    void onFailure(Throwable t);
}
