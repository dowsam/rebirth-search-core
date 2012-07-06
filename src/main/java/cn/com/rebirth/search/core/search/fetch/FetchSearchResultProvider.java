/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FetchSearchResultProvider.java 2012-3-29 15:02:29 l.xue.nong$$
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
