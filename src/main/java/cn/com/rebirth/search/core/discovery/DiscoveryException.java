/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core DiscoveryException.java 2012-3-29 15:01:00 l.xue.nong$$
 */


package cn.com.rebirth.search.core.discovery;

import cn.com.rebirth.commons.exception.RestartException;


/**
 * The Class DiscoveryException.
 *
 * @author l.xue.nong
 */
public class DiscoveryException extends RestartException {

    
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5602425330312982330L;

	
	/**
	 * Instantiates a new discovery exception.
	 *
	 * @param message the message
	 */
	public DiscoveryException(String message) {
        super(message);
    }

    
    /**
     * Instantiates a new discovery exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public DiscoveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
