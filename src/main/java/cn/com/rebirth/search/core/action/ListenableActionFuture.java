/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ListenableActionFuture.java 2012-3-29 15:02:45 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action;


/**
 * The Interface ListenableActionFuture.
 *
 * @param <T> the generic type
 * @author l.xue.nong
 */
public interface ListenableActionFuture<T> extends ActionFuture<T> {

    
    /**
     * Adds the listener.
     *
     * @param listener the listener
     */
    void addListener(final ActionListener<T> listener);

    
    /**
     * Adds the listener.
     *
     * @param listener the listener
     */
    void addListener(final Runnable listener);
}
