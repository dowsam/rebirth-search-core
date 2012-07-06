/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ActionNotFoundTransportException.java 2012-3-29 15:01:12 l.xue.nong$$
 */


package cn.com.rebirth.search.core.transport;


/**
 * The Class ActionNotFoundTransportException.
 *
 * @author l.xue.nong
 */
public class ActionNotFoundTransportException extends TransportException {

    /** The action. */
    private final String action;

    /**
     * Instantiates a new action not found transport exception.
     *
     * @param action the action
     */
    public ActionNotFoundTransportException(String action) {
        super("No handler for action [" + action + "]");
        this.action = action;
    }

    /**
     * Action.
     *
     * @return the string
     */
    public String action() {
        return this.action;
    }
}
