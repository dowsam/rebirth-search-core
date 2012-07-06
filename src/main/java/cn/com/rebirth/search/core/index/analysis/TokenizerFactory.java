/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TokenizerFactory.java 2012-7-6 14:30:04 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.Tokenizer;

import java.io.Reader;

/**
 * A factory for creating Tokenizer objects.
 */
public interface TokenizerFactory {

	/**
	 * Name.
	 *
	 * @return the string
	 */
	String name();

	/**
	 * Creates the.
	 *
	 * @param reader the reader
	 * @return the tokenizer
	 */
	Tokenizer create(Reader reader);
}
