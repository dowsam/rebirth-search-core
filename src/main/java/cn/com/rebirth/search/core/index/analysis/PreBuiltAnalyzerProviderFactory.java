/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core PreBuiltAnalyzerProviderFactory.java 2012-3-29 15:02:13 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.Analyzer;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.index.analysis.AnalyzerScope;


/**
 * A factory for creating PreBuiltAnalyzerProvider objects.
 */
public class PreBuiltAnalyzerProviderFactory implements AnalyzerProviderFactory {

    
    /** The analyzer provider. */
    private final PreBuiltAnalyzerProvider analyzerProvider;

    
    /**
     * Instantiates a new pre built analyzer provider factory.
     *
     * @param name the name
     * @param scope the scope
     * @param analyzer the analyzer
     */
    public PreBuiltAnalyzerProviderFactory(String name, AnalyzerScope scope, Analyzer analyzer) {
        this(new PreBuiltAnalyzerProvider<Analyzer>(name, scope, analyzer));
    }

    
    /**
     * Instantiates a new pre built analyzer provider factory.
     *
     * @param analyzerProvider the analyzer provider
     */
    public PreBuiltAnalyzerProviderFactory(PreBuiltAnalyzerProvider<Analyzer> analyzerProvider) {
        this.analyzerProvider = analyzerProvider;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.analysis.AnalyzerProviderFactory#create(java.lang.String, cn.com.summall.search.commons.settings.Settings)
     */
    @Override
    public AnalyzerProvider<Analyzer> create(String name, Settings settings) {
        return analyzerProvider;
    }

    
    /**
     * Analyzer.
     *
     * @return the analyzer
     */
    public Analyzer analyzer() {
        return analyzerProvider.get();
    }
}
