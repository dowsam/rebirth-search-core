/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core AnalyzerProvider.java 2012-7-6 14:30:20 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.Analyzer;

import cn.com.rebirth.search.commons.inject.Provider;
import cn.com.rebirth.search.index.analysis.AnalyzerScope;

/**
 * The Interface AnalyzerProvider.
 *
 * @param <T> the generic type
 * @author l.xue.nong
 */
public interface AnalyzerProvider<T extends Analyzer> extends Provider<T> {

	/**
	 * Name.
	 *
	 * @return the string
	 */
	String name();

	/**
	 * Scope.
	 *
	 * @return the analyzer scope
	 */
	AnalyzerScope scope();

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.Provider#get()
	 */
	T get();
}
