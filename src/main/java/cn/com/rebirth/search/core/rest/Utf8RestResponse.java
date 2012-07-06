/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core Utf8RestResponse.java 2012-3-29 15:01:43 l.xue.nong$$
 */


package cn.com.rebirth.search.core.rest;

import org.apache.lucene.util.UnicodeUtil;


/**
 * The Class Utf8RestResponse.
 *
 * @author l.xue.nong
 */
public class Utf8RestResponse extends AbstractRestResponse implements RestResponse {

    /** The Constant EMPTY. */
    public static final UnicodeUtil.UTF8Result EMPTY;

    static {
        UnicodeUtil.UTF8Result temp = new UnicodeUtil.UTF8Result();
        temp.result = new byte[0];
        temp.length = 0;
        EMPTY = temp;
    }

    /** The status. */
    private final RestStatus status;

    /** The utf8 result. */
    private final UnicodeUtil.UTF8Result utf8Result;

    /** The prefix utf8 result. */
    private final UnicodeUtil.UTF8Result prefixUtf8Result;

    /** The suffix utf8 result. */
    private final UnicodeUtil.UTF8Result suffixUtf8Result;

    /**
     * Instantiates a new utf8 rest response.
     *
     * @param status the status
     */
    public Utf8RestResponse(RestStatus status) {
        this(status, EMPTY);
    }

    /**
     * Instantiates a new utf8 rest response.
     *
     * @param status the status
     * @param utf8Result the utf8 result
     */
    public Utf8RestResponse(RestStatus status, UnicodeUtil.UTF8Result utf8Result) {
        this(status, utf8Result, null, null);
    }

    /**
     * Instantiates a new utf8 rest response.
     *
     * @param status the status
     * @param utf8Result the utf8 result
     * @param prefixUtf8Result the prefix utf8 result
     * @param suffixUtf8Result the suffix utf8 result
     */
    public Utf8RestResponse(RestStatus status, UnicodeUtil.UTF8Result utf8Result,
                            UnicodeUtil.UTF8Result prefixUtf8Result, UnicodeUtil.UTF8Result suffixUtf8Result) {
        this.status = status;
        this.utf8Result = utf8Result;
        this.prefixUtf8Result = prefixUtf8Result;
        this.suffixUtf8Result = suffixUtf8Result;
    }

    /* (non-Javadoc)
     * @see cn.com.summall.search.core.rest.RestResponse#contentThreadSafe()
     */
    @Override
    public boolean contentThreadSafe() {
        return false;
    }

    /* (non-Javadoc)
     * @see cn.com.summall.search.core.rest.RestResponse#contentType()
     */
    @Override
    public String contentType() {
        return "text/plain; charset=UTF-8";
    }

    /* (non-Javadoc)
     * @see cn.com.summall.search.core.rest.RestResponse#content()
     */
    @Override
    public byte[] content() {
        return utf8Result.result;
    }

    /* (non-Javadoc)
     * @see cn.com.summall.search.core.rest.RestResponse#contentLength()
     */
    @Override
    public int contentLength() {
        return utf8Result.length;
    }

    /* (non-Javadoc)
     * @see cn.com.summall.search.core.rest.RestResponse#status()
     */
    @Override
    public RestStatus status() {
        return status;
    }

    /* (non-Javadoc)
     * @see cn.com.summall.search.core.rest.AbstractRestResponse#prefixContent()
     */
    @Override
    public byte[] prefixContent() {
        return prefixUtf8Result != null ? prefixUtf8Result.result : null;
    }

    /* (non-Javadoc)
     * @see cn.com.summall.search.core.rest.AbstractRestResponse#prefixContentLength()
     */
    @Override
    public int prefixContentLength() {
        return prefixUtf8Result != null ? prefixUtf8Result.length : 0;
    }

    /* (non-Javadoc)
     * @see cn.com.summall.search.core.rest.AbstractRestResponse#suffixContent()
     */
    @Override
    public byte[] suffixContent() {
        return suffixUtf8Result != null ? suffixUtf8Result.result : null;
    }

    /* (non-Javadoc)
     * @see cn.com.summall.search.core.rest.AbstractRestResponse#suffixContentLength()
     */
    @Override
    public int suffixContentLength() {
        return suffixUtf8Result != null ? suffixUtf8Result.length : 0;
    }
}