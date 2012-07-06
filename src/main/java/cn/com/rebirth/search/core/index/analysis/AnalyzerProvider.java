/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core AnalyzerProvider.java 2012-3-29 15:00:47 l.xue.nong$$
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
     * @see cn.com.summall.search.commons.inject.Provider#get()
     */
    T get();
}
