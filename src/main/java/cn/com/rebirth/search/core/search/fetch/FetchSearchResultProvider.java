/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FetchSearchResultProvider.java 2012-7-6 14:28:44 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.fetch;

import cn.com.rebirth.search.core.search.SearchPhaseResult;

/**
 * The Interface FetchSearchResultProvider.
 *
 * @author l.xue.nong
 */
public interface FetchSearchResultProvider extends SearchPhaseResult {

	/**
	 * Fetch result.
	 *
	 * @return the fetch search result
	 */
	FetchSearchResult fetchResult();
}
