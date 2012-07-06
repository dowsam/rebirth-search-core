/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core OptimizeGlobalFacetCollector.java 2012-7-6 14:30:48 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet;

import java.io.IOException;

import cn.com.rebirth.search.core.search.internal.SearchContext;

/**
 * The Interface OptimizeGlobalFacetCollector.
 *
 * @author l.xue.nong
 */
public interface OptimizeGlobalFacetCollector {

	/**
	 * Optimized global execution.
	 *
	 * @param searchContext the search context
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	void optimizedGlobalExecution(SearchContext searchContext) throws IOException;
}