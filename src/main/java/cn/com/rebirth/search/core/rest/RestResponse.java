/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RestResponse.java 2012-3-29 15:02:42 l.xue.nong$$
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
