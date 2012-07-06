/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SearchPhase.java 2012-3-29 15:01:56 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search;

import java.util.Map;

import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.search.core.search.internal.SearchContext;


/**
 * The Interface SearchPhase.
 *
 * @author l.xue.nong
 */
public interface SearchPhase {

    
    /**
     * Parses the elements.
     *
     * @return the map< string,? extends search parse element>
     */
    Map<String, ? extends SearchParseElement> parseElements();

    
    /**
     * Pre process.
     *
     * @param context the context
     */
    void preProcess(SearchContext context);

    
    /**
     * Execute.
     *
     * @param context the context
     * @throws SumMallSearchException the sum mall search exception
     */
    void execute(SearchContext context) throws RestartException;
}
