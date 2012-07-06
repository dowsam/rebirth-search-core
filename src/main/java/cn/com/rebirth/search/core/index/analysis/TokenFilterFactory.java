/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TokenFilterFactory.java 2012-3-29 15:02:06 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.TokenStream;


/**
 * A factory for creating TokenFilter objects.
 */
public interface TokenFilterFactory {

	
	/**
	 * Name.
	 *
	 * @return the string
	 */
	String name();

	
	/**
	 * Creates the.
	 *
	 * @param tokenStream the token stream
	 * @return the token stream
	 */
	TokenStream create(TokenStream tokenStream);
}
