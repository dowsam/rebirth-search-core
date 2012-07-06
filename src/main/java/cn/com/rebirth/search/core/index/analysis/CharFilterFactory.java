/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core CharFilterFactory.java 2012-7-6 14:29:25 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.CharStream;

/**
 * A factory for creating CharFilter objects.
 */
public interface CharFilterFactory {

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
	 * @return the char stream
	 */
	CharStream create(CharStream tokenStream);
}
