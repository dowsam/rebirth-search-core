/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TimestampParsingException.java 2012-3-29 15:01:24 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action;

import cn.com.rebirth.commons.exception.RestartException;


/**
 * The Class TimestampParsingException.
 *
 * @author l.xue.nong
 */
public class TimestampParsingException extends RestartException {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 2178449641102540276L;
	
	
	/** The timestamp. */
	private final String timestamp;

    
    /**
     * Instantiates a new timestamp parsing exception.
     *
     * @param timestamp the timestamp
     */
    public TimestampParsingException(String timestamp) {
        super("failed to parse timestamp [" + timestamp + "]");
        this.timestamp = timestamp;
    }

    
    /**
     * Timestamp.
     *
     * @return the string
     */
    public String timestamp() {
        return timestamp;
    }
}