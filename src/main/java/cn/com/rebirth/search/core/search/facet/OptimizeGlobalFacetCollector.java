/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core OptimizeGlobalFacetCollector.java 2012-3-29 15:02:11 l.xue.nong$$
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