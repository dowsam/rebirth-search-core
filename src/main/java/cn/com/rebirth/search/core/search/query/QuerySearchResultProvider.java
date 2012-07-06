/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core QuerySearchResultProvider.java 2012-3-29 15:00:51 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.query;

import cn.com.rebirth.search.core.search.SearchPhaseResult;


/**
 * The Interface QuerySearchResultProvider.
 *
 * @author l.xue.nong
 */
public interface QuerySearchResultProvider extends SearchPhaseResult {

    
    /**
     * Include fetch.
     *
     * @return true, if successful
     */
    boolean includeFetch();

    
    /**
     * Query result.
     *
     * @return the query search result
     */
    QuerySearchResult queryResult();
}
