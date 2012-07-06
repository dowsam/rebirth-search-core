/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FailedToGenerateSourceMapperException.java 2012-3-29 15:00:56 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.mapper;


/**
 * The Class FailedToGenerateSourceMapperException.
 *
 * @author l.xue.nong
 */
public class FailedToGenerateSourceMapperException extends MapperException {

    /**
     * Instantiates a new failed to generate source mapper exception.
     *
     * @param message the message
     */
    public FailedToGenerateSourceMapperException(String message) {
        super(message);
    }

    /**
     * Instantiates a new failed to generate source mapper exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public FailedToGenerateSourceMapperException(String message, Throwable cause) {
        super(message, cause);
    }
}
