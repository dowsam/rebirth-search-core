/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core StringRestResponse.java 2012-3-29 15:01:07 l.xue.nong$$
 */


package cn.com.rebirth.search.core.rest;

import org.apache.lucene.util.UnicodeUtil;

import cn.com.rebirth.commons.thread.ThreadLocals;


/**
 * The Class StringRestResponse.
 *
 * @author l.xue.nong
 */
public class StringRestResponse extends Utf8RestResponse {

	
	/** The cache. */
	private static ThreadLocal<ThreadLocals.CleanableValue<UnicodeUtil.UTF8Result>> cache = new ThreadLocal<ThreadLocals.CleanableValue<UnicodeUtil.UTF8Result>>() {
		@Override
		protected ThreadLocals.CleanableValue<UnicodeUtil.UTF8Result> initialValue() {
			return new ThreadLocals.CleanableValue<UnicodeUtil.UTF8Result>(new UnicodeUtil.UTF8Result());
		}
	};

	
	/**
	 * Instantiates a new string rest response.
	 *
	 * @param status the status
	 */
	public StringRestResponse(RestStatus status) {
		super(status);
	}

	
	/**
	 * Instantiates a new string rest response.
	 *
	 * @param status the status
	 * @param content the content
	 */
	public StringRestResponse(RestStatus status, String content) {
		super(status, convert(content));
	}

	
	/**
	 * Convert.
	 *
	 * @param content the content
	 * @return the unicode util. ut f8 result
	 */
	private static UnicodeUtil.UTF8Result convert(String content) {
		UnicodeUtil.UTF8Result result = cache.get().get();
		UnicodeUtil.UTF16toUTF8(content, 0, content.length(), result);
		return result;
	}
}