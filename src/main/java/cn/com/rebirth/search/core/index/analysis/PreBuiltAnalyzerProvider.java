/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core PreBuiltAnalyzerProvider.java 2012-3-29 15:00:53 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.Analyzer;

import cn.com.rebirth.search.index.analysis.AnalyzerScope;


/**
 * The Class PreBuiltAnalyzerProvider.
 *
 * @param <T> the generic type
 * @author l.xue.nong
 */
public class PreBuiltAnalyzerProvider<T extends Analyzer> implements AnalyzerProvider<T> {

    
    /** The name. */
    private final String name;

    
    /** The scope. */
    private final AnalyzerScope scope;

    
    /** The analyzer. */
    private final T analyzer;

    
    /**
     * Instantiates a new pre built analyzer provider.
     *
     * @param name the name
     * @param scope the scope
     * @param analyzer the analyzer
     */
    public PreBuiltAnalyzerProvider(String name, AnalyzerScope scope, T analyzer) {
        this.name = name;
        this.scope = scope;
        this.analyzer = analyzer;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.analysis.AnalyzerProvider#name()
     */
    @Override
    public String name() {
        return name;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.analysis.AnalyzerProvider#scope()
     */
    @Override
    public AnalyzerScope scope() {
        return scope;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.analysis.AnalyzerProvider#get()
     */
    @Override
    public T get() {
        return analyzer;
    }
}
