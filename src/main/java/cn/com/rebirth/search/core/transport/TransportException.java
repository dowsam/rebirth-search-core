/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TransportException.java 2012-3-29 15:01:12 l.xue.nong$$
 */


package cn.com.rebirth.search.core.transport;

import cn.com.rebirth.commons.exception.RestartException;


/**
 * The Class TransportException.
 *
 * @author l.xue.nong
 */
public class TransportException extends RestartException {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6300393876815181484L;

	
    /**
     * Instantiates a new transport exception.
     *
     * @param msg the msg
     */
    public TransportException(String msg) {
        super(msg);
    }

    
    /**
     * Instantiates a new transport exception.
     *
     * @param msg the msg
     * @param cause the cause
     */
    public TransportException(String msg, Throwable cause) {
        super(msg, cause);
    }
}