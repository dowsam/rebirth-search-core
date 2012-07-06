/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TokenFilterFactory.java 2012-7-6 14:29:33 l.xue.nong$$
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
