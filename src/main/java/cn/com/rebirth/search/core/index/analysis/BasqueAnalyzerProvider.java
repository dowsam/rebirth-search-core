/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core BasqueAnalyzerProvider.java 2012-3-29 15:01:04 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.eu.BasqueAnalyzer;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.env.Environment;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;


/**
 * The Class BasqueAnalyzerProvider.
 *
 * @author l.xue.nong
 */
public class BasqueAnalyzerProvider extends AbstractIndexAnalyzerProvider<BasqueAnalyzer> {

    
    /** The analyzer. */
    private final BasqueAnalyzer analyzer;

    
    /**
     * Instantiates a new basque analyzer provider.
     *
     * @param index the index
     * @param indexSettings the index settings
     * @param env the env
     * @param name the name
     * @param settings the settings
     */
    @Inject
    public BasqueAnalyzerProvider(Index index, @IndexSettings Settings indexSettings, Environment env, @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettings, name, settings);
        analyzer = new BasqueAnalyzer(version,
                Analysis.parseStopWords(env, settings, BasqueAnalyzer.getDefaultStopSet(), version),
                Analysis.parseStemExclusion(settings, CharArraySet.EMPTY_SET));
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.analysis.AnalyzerProvider#get()
     */
    @Override
    public BasqueAnalyzer get() {
        return this.analyzer;
    }
}