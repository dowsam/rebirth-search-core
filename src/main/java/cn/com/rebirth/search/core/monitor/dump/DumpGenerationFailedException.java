/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core DumpGenerationFailedException.java 2012-3-29 15:00:50 l.xue.nong$$
 */


package cn.com.rebirth.search.core.monitor.dump;


/**
 * The Class DumpGenerationFailedException.
 *
 * @author l.xue.nong
 */
public class DumpGenerationFailedException extends DumpException {

    /**
     * Instantiates a new dump generation failed exception.
     *
     * @param msg the msg
     */
    public DumpGenerationFailedException(String msg) {
        super(msg);
    }

    /**
     * Instantiates a new dump generation failed exception.
     *
     * @param msg the msg
     * @param cause the cause
     */
    public DumpGenerationFailedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
