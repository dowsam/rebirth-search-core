/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestResponse.java 2012-7-6 14:30:21 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest;

import java.io.IOException;

/**
 * The Interface RestResponse.
 *
 * @author l.xue.nong
 */
public interface RestResponse {

	/**
	 * Content thread safe.
	 *
	 * @return true, if successful
	 */
	boolean contentThreadSafe();

	/**
	 * Content type.
	 *
	 * @return the string
	 */
	String contentType();

	/**
	 * Content.
	 *
	 * @return the byte[]
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	byte[] content() throws IOException;

	/**
	 * Content length.
	 *
	 * @return the int
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	int contentLength() throws IOException;

	/**
	 * Prefix content.
	 *
	 * @return the byte[]
	 */
	byte[] prefixContent();

	/**
	 * Prefix content length.
	 *
	 * @return the int
	 */
	int prefixContentLength();

	/**
	 * Suffix content.
	 *
	 * @return the byte[]
	 */
	byte[] suffixContent();

	/**
	 * Suffix content length.
	 *
	 * @return the int
	 */
	int suffixContentLength();

	/**
	 * Status.
	 *
	 * @return the rest status
	 */
	RestStatus status();
}
