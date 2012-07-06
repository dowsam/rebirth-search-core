/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ZenPingException.java 2012-3-29 15:02:34 l.xue.nong$$
 */


package cn.com.rebirth.search.core.discovery.zen.ping;

import cn.com.rebirth.search.core.discovery.DiscoveryException;


/**
 * The Class ZenPingException.
 *
 * @author l.xue.nong
 */
public class ZenPingException extends DiscoveryException {

    
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 472826406523647401L;

	
    /**
     * Instantiates a new zen ping exception.
     *
     * @param message the message
     */
    public ZenPingException(String message) {
        super(message);
    }

    
    /**
     * Instantiates a new zen ping exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public ZenPingException(String message, Throwable cause) {
        super(message, cause);
    }
}
