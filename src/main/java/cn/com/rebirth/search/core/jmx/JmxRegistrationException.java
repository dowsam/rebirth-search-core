/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core JmxRegistrationException.java 2012-3-29 15:02:44 l.xue.nong$$
 */


package cn.com.rebirth.search.core.jmx;


/**
 * The Class JmxRegistrationException.
 *
 * @author l.xue.nong
 */
public class JmxRegistrationException extends JmxException {

    /**
     * Instantiates a new jmx registration exception.
     *
     * @param message the message
     */
    public JmxRegistrationException(String message) {
        super(message);
    }

    /**
     * Instantiates a new jmx registration exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public JmxRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}