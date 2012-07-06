/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core AbstractRestResponse.java 2012-7-6 14:29:10 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest;

/**
 * The Class AbstractRestResponse.
 *
 * @author l.xue.nong
 */
public abstract class AbstractRestResponse implements RestResponse {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestResponse#prefixContent()
	 */
	@Override
	public byte[] prefixContent() {
		return null;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestResponse#prefixContentLength()
	 */
	@Override
	public int prefixContentLength() {
		return -1;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestResponse#suffixContent()
	 */
	@Override
	public byte[] suffixContent() {
		return null;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestResponse#suffixContentLength()
	 */
	@Override
	public int suffixContentLength() {
		return -1;
	}
}