/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core BytesRestResponse.java 2012-3-29 15:01:31 l.xue.nong$$
 */


package cn.com.rebirth.search.core.rest;

import java.io.IOException;

/**
 * The Class BytesRestResponse.
 *
 * @author l.xue.nong
 */
public class BytesRestResponse extends AbstractRestResponse {

    /** The bytes. */
    private final byte[] bytes;

    /** The content type. */
    private final String contentType;

    /**
     * Instantiates a new bytes rest response.
     *
     * @param bytes the bytes
     * @param contentType the content type
     */
    public BytesRestResponse(byte[] bytes, String contentType) {
        this.bytes = bytes;
        this.contentType = contentType;
    }

    /* (non-Javadoc)
     * @see cn.com.summall.search.core.rest.RestResponse#contentThreadSafe()
     */
    @Override
    public boolean contentThreadSafe() {
        return true;
    }

    /* (non-Javadoc)
     * @see cn.com.summall.search.core.rest.RestResponse#contentType()
     */
    @Override
    public String contentType() {
        return contentType;
    }

    /* (non-Javadoc)
     * @see cn.com.summall.search.core.rest.RestResponse#content()
     */
    @Override
    public byte[] content() throws IOException {
        return bytes;
    }

    /* (non-Javadoc)
     * @see cn.com.summall.search.core.rest.RestResponse#contentLength()
     */
    @Override
    public int contentLength() throws IOException {
        return bytes.length;
    }

    /* (non-Javadoc)
     * @see cn.com.summall.search.core.rest.RestResponse#status()
     */
    @Override
    public RestStatus status() {
        return RestStatus.OK;
    }
}